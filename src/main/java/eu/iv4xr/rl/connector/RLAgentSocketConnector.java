package eu.iv4xr.rl.connector;

import com.google.gson.Gson;
import com.google.gson.GsonBuilder;
import eu.iv4xr.rl.environment.generic.RLAction;
import org.zeromq.SocketType;
import org.zeromq.ZContext;
import org.zeromq.ZMQ;
import eu.iv4xr.rl.environment.generic.DictSpaceSerializer;
import eu.iv4xr.rl.environment.generic.RLDictSpace;
import zmq.ZError;

import java.lang.reflect.Modifier;
import java.util.logging.Level;
import java.util.logging.Logger;

/**
 * Connector of the iv4XR RL Environment to a Python RL Agent.
 * The Gym interface is provided to the RL Agent through RESET and STEP orders.
 * For interoperability, The ZeroMQ library is used with TCP Socket as backend
 * and JSON as message format.
 */
public class RLAgentSocketConnector {
	
	/**
	 * An elementary operation with the RL Agent. This is mostly used for wrapping
	 * JSON serialization.
	 */
	static public class RLAgentOperation {
		
		/**
		 * A unique id identifying whoever invokes this operation. E.g. agentid.actionid.
		 */
		public String invokerId ;
		
		/**
		 * A unique id identifying the entity in the real environment to which this operation
		 * is targeted.
		 */
		public String targetId ;
		
		/**
		 * The name of the command that this operation represents.
		 */
		public String command ;
		
		/**
		 * The argment of the operation, if any.
		 */
		public Object arg ;
		
		/**
		 * Used to store the result of the operation, if any.
		 */
		public Object result = null ;
		
		public RLAgentOperation(String invokerId, String targetId, String command, Object arg) {
			this.invokerId = invokerId ;
			this.targetId = targetId ;
			this.command = command ;
			this.arg = arg ;
		}
	}

	private static final Logger LOGGER = Logger.getLogger(RLAgentSocketConnector.class.getName());
	public static final Level logLevel = Level.FINEST; // Use info to see the message logs
	
	// transient modifiers should be excluded, otherwise they will be sent with json
	private static Gson gson = new GsonBuilder()
		.serializeNulls()
		.excludeFieldsWithModifiers(Modifier.TRANSIENT)
		.registerTypeAdapter(RLDictSpace.class, new DictSpaceSerializer())
		.create();
	
	// initialise socket and input output streams
	ZContext context;
	ZMQ.Socket socket;

	private final Class<?> actionClass;
	/**
	 * Initialize the connector with the given mode.
	 *
	 * @param address serving address.
	 * @param port listening port.
	 * @param actionClass class of the agent's actions, required here because
	 *                    Java can't infer it at runtime.
	 */
	public RLAgentSocketConnector(String address, int port, Class<? extends RLAction<?, ?>> actionClass) {
		this.actionClass = actionClass;
		try {
			context = new ZContext();
			socket = context.createSocket(SocketType.REP);
			socket.bind("tcp://" + address + ":" + port);
		} catch (ZError.CtxTerminatedException | ZError.IOException | ZError.InstantiationException u) {
			System.out.println(u);
		}
	}
	
	/**
	 * Close the socket and input output streams
	 */
	public boolean close() {
		try {
			if (socket != null)
				socket.close();

			System.out.println(String.format("Disconnected from the host."));
		} catch (ZError.IOException e) {
			System.out.println(e.getMessage());
			System.out.println(String.format("Could not disconnect from the host by closing the socket."));
			return false;
		}

		return true;
	}

	/**
	 * Poll the next request from the RL Agent.
	 *
	 * @return request from the RL Agent.
	 */
	public RLAgentRequest<?> pollRequest() {
		try {
			String messageReceived = socket.recvStr();
			LOGGER.log(logLevel, "message received: " + messageReceived);
			var agentOperation = gson.fromJson(messageReceived, RLAgentOperation.class);
			var commandType = RLAgentRequestType.valueOf(agentOperation.command);
			if (commandType == RLAgentRequestType.GET_SPEC || commandType == RLAgentRequestType.RESET) {
				return RLAgentRequest.plainRequest(commandType, agentOperation.arg);
			} else if (commandType == RLAgentRequestType.STEP) {
				// TODO: make it generic or easily extended to other action types
				var action = gson.fromJson(
						String.valueOf(agentOperation.arg), actionClass);
				return RLAgentRequest.plainRequest(commandType, action);
			} else {
				throw new IllegalArgumentException("Illegal command received: " + agentOperation.command);
			}
		} catch (ZError.IOException ex) {
			System.out.println("I/O error: " + ex.getMessage());
			return null;
		}
	}

	/**
	 * Send a response to the RL Agent (as JSON).
	 *
	 * @param response response object.
	 * @param <T> response object type.
	 */
	public <T> void sendRLAgentResponse(T response) {
		String jsonMessage = gson.toJson(response);
		// write to the socket
		try {
			socket.send(jsonMessage);
			LOGGER.log(logLevel, "message sent: " + jsonMessage);
		} catch (ZError.IOException ex) {
			System.out.println("I/O error: " + ex.getMessage());
		}
	}
}
