package eu.iv4xr.rl.connector;
import javax.lang.model.type.NullType;

/**
 * Base request class when connecting to a Python RL Agent.
 *
 * @param <ResponseType>
 */
public class RLAgentRequest<ResponseType> {
	/**
	 * Java can not determine the class of ResponseType at runtime.
	 * In this case, storing an instance of Class<ResponseType> to cast the response object is seen as good practice.
	 */
	public transient final Class<ResponseType> responseType;

	public RLAgentRequestType cmd;
	public Object arg;

	/**
	 * Initialize a request with response type, command and argument.
	 */
	private RLAgentRequest(Class<ResponseType> responseType, RLAgentRequestType cmd, Object arg) {
		this.responseType = responseType;
		this.cmd = cmd;
		this.arg = arg;
	}

	/**
	 * Send a request to the agent without waiting for a response.
	 * @param requestType type of the request.
	 * @param arg argument of the request.
	 */
	public static RLAgentRequest<NullType> plainRequest(RLAgentRequestType requestType, Object arg) {
		return new RLAgentRequest<>(NullType.class, requestType, arg);
	}
}
