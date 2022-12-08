package eu.iv4xr.rl.environment.square;

import java.util.Random;

/**
 * This class provides a simple example implementation of a system under test to be
 * wrapped by a RLEnvironment. It is adapted from the SquareWorld environment
 * provided with the japyre library.
 *
 * <p>
 * The provided Gym is called SquareWorld. It is a tiled NxN grid, where a robot
 * is dropped in its center. Its goal is to reach the top-right corner of the
 * grid (location (N-1,N-1)). The robot can move left/right/up/down. The robot
 * itself has no intelligence, so has no idea what to do. The method execute(a)
 * allows you to control the robot.
 *
 * <p>
 * Moving off the grid causes the robot to be broken.
 *
 * @author Wish
 */
public class SquareWorld {

    public static class Location {
        int x ;
        int y ;
        public Location(int x, int y) { this.x = x ; this.y = y ; }
        @Override
        public String toString() {
            return "<" + x + "," + y + ">" ;
        }
    }

    public int size ;
    public Location currentLocation ;

    public int randomSeed = 3373 ;
    public int stepCount = 0 ;

    Random rnd = new Random(randomSeed) ;

    public SquareWorld(int size) {
        this.size = size ;
        currentLocation = new Location(size/2,size/2) ;
    }

    static final String UP    = "up" ;
    static final String DOWN  = "down" ;
    static final String LEFT  = "left" ;
    static final String RIGHT = "right" ;

    public static boolean deterministic = true ;
    public static boolean debug = false ;

    public Location reset() {
        currentLocation.x = size/2 ;
        currentLocation.y = size/2 ;
        rnd = new Random(randomSeed) ;
        stepCount = 0 ;
        return currentLocation ;
    }

    public boolean goalAchieved() {
        return (currentLocation.x == size-1 && currentLocation.y == size-1) ;
    }

    public boolean offTheGrid() {
        return currentLocation.x < 0 || currentLocation.x >= size ||
                currentLocation.y < 0 || currentLocation.y >= size ;
    }

    public boolean isTerminalState() {
        return goalAchieved() || offTheGrid() ;
    }

    public Location step(String action) {
        if (this.isTerminalState()) {
            if (debug) {
                System.out.println("## the robot is in a terminal state. No action is possible.") ;
            }
            return null ;
        }
        switch(action) {
            case "left" :
                currentLocation.x -- ;
                break ;
            case "right" :
                currentLocation.x ++ ;
                break ;
            case "down" :
                currentLocation.y -- ;
                break ;
            case "up" :
                if (deterministic) {
                    currentLocation.y ++ ;
                }
                else {
                    if (rnd.nextFloat() <= 0.15) {
                        currentLocation.y -- ;
                    }
                    else {
                        currentLocation.y ++ ;
                    }
                }
        }
        if (debug) {
            System.out.print("## " + stepCount + ":" + action) ;
            if (offTheGrid()) {
                System.out.println(", the robot CRASHES.") ;
            }
            else if (goalAchieved()) {
                System.out.println(", SUCCESS reaching the goal.") ;
            }
            else {
                System.out.println("") ;
            }
        }
        stepCount++ ;

        return currentLocation;
    }

    // just for testing
    public static void main(String[] args) {
        SquareWorld sw = new SquareWorld(6) ;
        SquareWorld.debug = true ;
        Location o  ;
        o = sw.step(RIGHT) ; System.out.println(">> " + o);
        //o = sw.step(RIGHT) ; System.out.println(">> " + o);
        o = sw.step(UP)    ; System.out.println(">> " + o);
        o = sw.step(RIGHT) ; System.out.println(">> " + o);
        o = sw.step(UP)    ; System.out.println(">> " + o);
        o = sw.step(RIGHT) ; System.out.println(">> " + o);
    }

}
