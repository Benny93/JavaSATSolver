package dataStructure;


import java.util.HashMap;
import java.util.ListIterator;
import java.util.Vector;

/**
 * A variable.
 */
public class Variable {

    /* Assignment states of a variable */
    public enum State {
        TRUE, FALSE, OPEN
    }

    /* Current assignment */
    private State state;

    /* Variable ID (range from 1 to n) */
    private int id;

    /* Clauses containing this variable */
    private Vector<Clause> watched;
    //iterates over watched
    private ListIterator<Clause> watchedIterator;

    //activity of this variable
    private float activity;

    //saves the reason clause
    private Clause reason;

    //saves the level of this variable
    private int level;


    /**
     * Creates a variable with the given ID.
     *
     * @param id ID of the variable
     */
    public Variable(int id) {
        this.id = id;
        this.state = State.OPEN;    //Open State at the beginning
        //initialize a new Vector of Clauses
        //the values will be set in ClauseSet (via the getter)
        this.watched = new Vector<Clause>();
        watchedIterator = watched.listIterator();
        this.level = -1; // no level assigned yet
        this.reason = null; //no reason assigned yet
    }

    /**
     * Returns the current assignment state of this variable.
     *
     * @return current assignment state
     */
    public State getState() {
        return state;
    }

    /**
     * Returns the ID of this variable.
     *
     * @return ID of this variable
     */
    public int getId() {
        return id;
    }

    /**
     * Returns the adjacency list of this variable.
     *
     * @return adjacency list of this variable
     */
    public Vector<Clause> getWatched() {
        return watched;
    }

    public ListIterator<Clause> getWatchedIterator() {
        return watchedIterator;
    }

    /**
     * This is the new assign method.
     *
     * @param val assignment value
     * @param variables hashmap of variables
     * @param newUnitsQueue queue to store new units
     * @return returns empty clause OR null
     */
    public Clause assign(boolean val, HashMap<Integer, Variable> variables, Vector<Clause> newUnitsQueue, int level, Clause reason) {

        // assign variable
        if (val) {
            this.state = State.TRUE;
        } else {
            this.state = State.FALSE;
        }

        this.level = level;
        this.reason = reason;
        // Check how the literals of this variable evaluate in watched
        watchedIterator = watched.listIterator();
        //for (Clause clause : watched) {
        while (watchedIterator.hasNext()) {
            Clause clause = watchedIterator.next();
            if (!val && clause.getPolarity(this.id) || val && !clause.getPolarity(this.id)) {
                //literal will evaluate to 0
                Clause.ClauseState newState = clause.reWatch(variables, clause.getLiteralFromVariableId(this.id));
                //if reWatch return an empty clause, this method returns the empty clause
                if (newState == Clause.ClauseState.EMPTY)
                    return clause;

                //if rewatch returns a unit clause, this method adds the
                // unit clause to the units vector
                if (newState == Clause.ClauseState.UNIT) {
                    newUnitsQueue.addElement(clause);
                }
            }

        }
        return null;
    }

    /**
     * Unassigned the variable
     * used for backtracking
     */
    public void unassign() {
        this.state = State.OPEN;
        this.level = -1;
        this.reason = null;
        //learned from slides: watched pointer do not move on backtrack
    }

    @Override
    public String toString() {
        String res = "id: " + this.id + " [" + state + " ";
        res += "\n\tWatched List: " + watched + ", Level: " + level + ", Reason: " + reason;
        return res + "\n]";
    }

    public int getLevel() {
        return level;
    }

    public float getActivity() {
        return activity;
    }

    public Clause getReason() {
        return reason;
    }

    public void setActivity(float activity) {
        this.activity = activity;
    }

}