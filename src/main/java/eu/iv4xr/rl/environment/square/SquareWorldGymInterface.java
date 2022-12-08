package eu.iv4xr.rl.environment.square;

import eu.iv4xr.rl.environment.generic.*;

import static eu.iv4xr.rl.environment.square.SquareWorld.*;

/**
 * This class provides a simple example of the RL Interfacing classes on the
 * SquareWorld environment, defined as static classes below.
 */
public class SquareWorldGymInterface {

    public static int WORLD_SIZE = 6;
    public static RLDiscreteSpace ACTION_SPACE = new RLDiscreteSpace(4);
    public static RLBoxSpace STATE_SPACE = new RLBoxSpace(
            0.0, (double)WORLD_SIZE, 2);
    public static class Action extends RLAction<Integer, String> {
        /**
         * Constructor.
         *
         * @param rawAction action, with the raw RL representation.
         */
        public Action(Integer rawAction) {
            super(rawAction);
        }

        @Override
        public String asCommand(int agentId) {
            switch (rawAction) {
                case 0 -> {
                    return UP;
                }
                case 1 -> {
                    return DOWN;
                }
                case 2 -> {
                    return LEFT;
                }
                case 3 -> {
                    return RIGHT;
                }
                default -> throw new IllegalStateException("Illegal action");
            }
        }
    };

    public static class Observation extends RLObservation<int[], Location> {

        /**
         * Construct a RL observation from a World Object Model state.
         *
         * @param womState current state of the World Object Model.
         */
        public Observation(SquareWorld.Location womState) {
            super(womState);
        }

        @Override
        public RLObservation<int[], SquareWorld.Location> clone() {
            return new Observation(this.getWomState());
        }

        @Override
        public int[] initRawObservation() {
            return new int[]{womState.x, womState.y};
        }
    };

    public static class Reward implements RLReward<Observation, Action> {
        @Override
        public double computeReward(Observation state, Action action, Observation nextState) {
            if (goalAchieved(state.getWomState()))
                return 1.0;
            else
                return 0.0;
        }
    }

    public static class EndCondition implements RLEndCondition<Observation> {

        @Override
        public boolean isDone(Observation state) {
            return goalAchieved(state.getWomState()) || offTheGrid(state.getWomState());
        }
    }

    public static boolean goalAchieved(SquareWorld.Location loc) {
        return loc.x == WORLD_SIZE-1 && loc.y == WORLD_SIZE-1;
    }
    public static boolean offTheGrid(SquareWorld.Location loc) {
        return loc.x < 0 || loc.x >= WORLD_SIZE || loc.y < 0 || loc.y >= WORLD_SIZE;
    }
}
