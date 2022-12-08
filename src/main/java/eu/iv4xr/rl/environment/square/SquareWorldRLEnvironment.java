package eu.iv4xr.rl.environment.square;

import eu.iv4xr.rl.environment.generic.*;

import java.util.Arrays;
import java.util.HashMap;

import static eu.iv4xr.rl.environment.square.SquareWorldGymInterface.*;

/**
 * This class provided a simple example implementation of an
 * OpenAI Gym-compliant RL Environment on top of the SquareWorld system under
 * test. It used the Action, Observation, Reward and EndCondition classes
 * implemented by SquareWorldGymInterface.
 */
public class SquareWorldRLEnvironment extends RLEnvironment<
        SquareWorldGymInterface.Action, SquareWorldGymInterface.Observation,
        SquareWorldGymInterface.Reward, SquareWorldGymInterface.EndCondition,
        RLDiscreteSpace, RLBoxSpace> {
    private final SquareWorld squareWorldSUT;
    private int agentId = 0;
    /**
     * Construct an environment.
     */
    public SquareWorldRLEnvironment() {
        super(new RLEnvSpec<>("SquareWorld", ACTION_SPACE, STATE_SPACE));
        rewardFunction = new Reward();
        endCondition = new EndCondition();
        squareWorldSUT = new SquareWorld(WORLD_SIZE);
    }

    @Override
    public SquareWorldGymInterface.Observation reset() {
        return new SquareWorldGymInterface.Observation(squareWorldSUT.reset());
    }

    @Override
    public RLStepOutput<SquareWorldGymInterface.Observation> step(SquareWorldGymInterface.Action action) {
        var currentState = new SquareWorldGymInterface.Observation(squareWorldSUT.currentLocation);
        var nextState = new SquareWorldGymInterface.Observation(squareWorldSUT.step(action.asCommand(agentId)));
        var info = new HashMap<String, String>();
        info.put("goal_achieved", String.valueOf(goalAchieved(nextState.getWomState())));
        info.put("off_grid", String.valueOf(offTheGrid(nextState.getWomState())));
        return new RLStepOutput<>(
                nextState,
                rewardFunction.computeReward(currentState, action, nextState),
                endCondition.isDone(nextState),
                info
        );
    }

    // just for testing
    public static void main(String[] args) {
        SquareWorldRLEnvironment rlEnv = new SquareWorldRLEnvironment();
        SquareWorld.debug = true;
        System.out.println("Testing RESET");
        var state = rlEnv.reset();
        System.out.println("Initial state: " + Arrays.toString(state.getRawObservation()));
        System.out.println("Testing STEP");
        var rlAction = new Action(0);
        System.out.println("Taking action: " + rlAction.getRawAction() + " [" + rlAction.asCommand(0) + "]");
        var stepOutput = rlEnv.step(rlAction);
        System.out.println("New state: " + Arrays.toString(stepOutput.getNextObservation().getRawObservation()));
        System.out.println("Reward: " + stepOutput.getReward());
        System.out.println("Done: " + stepOutput.isDone());
        System.out.println("Info: " + stepOutput.getInfo());
    }
}
