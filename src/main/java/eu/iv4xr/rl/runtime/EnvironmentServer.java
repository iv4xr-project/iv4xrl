package eu.iv4xr.rl.runtime;

import eu.iv4xr.rl.connector.RLAgentConfig;
import eu.iv4xr.rl.connector.RLAgentRequestType;
import eu.iv4xr.rl.connector.RLAgentSocketConnector;
import eu.iv4xr.rl.environment.generic.RLEnvironment;
import eu.iv4xr.rl.environment.square.SquareWorldGymInterface;
import eu.iv4xr.rl.environment.square.SquareWorldRLEnvironment;

/**
 * This class provides a simple example implementation of an OpenAI-Gym
 * compliant environment server used to train an agent with Python code
 * over the Java-based training Gym SquareWorldRLEnvironment.
 * The dependency on the Action type prevents it to be fully generic, but this
 * simple implementation can be easily adapted to other environments.
 * @param <Environment> the class of the Gym environment.
 */
public class EnvironmentServer<Environment extends RLEnvironment<SquareWorldGymInterface.Action, ?, ?, ?, ?, ?>> {
    private final Environment env;
    private final int maxEpisodes;
    private final RLAgentSocketConnector connector;

    public EnvironmentServer(Environment env, int maxEpisodes, RLAgentConfig config) {
        this.env = env;
        this.maxEpisodes = maxEpisodes;
        this.connector = new RLAgentSocketConnector(
                config.host, config.port, SquareWorldGymInterface.Action.class);
    }

    public void run() {
        int numEpisodes = 0;
        while (numEpisodes < maxEpisodes) {
            var request = connector.pollRequest();
            if (request.cmd == RLAgentRequestType.GET_SPEC) {
                connector.sendRLAgentResponse(env.getEnvSpec());
            } else if (request.cmd == RLAgentRequestType.RESET) {
                var state = env.reset();
                connector.sendRLAgentResponse(state);
                numEpisodes += 1;
            } else if (request.cmd == RLAgentRequestType.STEP) {
                var stepOutput = env.step((SquareWorldGymInterface.Action) request.arg);
                connector.sendRLAgentResponse(stepOutput);
            }
        }
        this.connector.close();
    }

    public static void main(String[] args) {
        var envServer = new EnvironmentServer<>(new SquareWorldRLEnvironment(), 1000, new RLAgentConfig());
        envServer.run();
    }
}
