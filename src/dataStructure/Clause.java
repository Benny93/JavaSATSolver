package dataStructure;

import dataStructure.Variable.State;

import java.util.HashMap;
import java.util.Vector;

/**
 * A clause.
 */
public class Clause {
    /* Literals of the clause */
    private Vector<Integer> literals;


    public enum ClauseState {
        SAT, EMPTY, UNIT, SUCCESS
    }


    /**
     * Two watched literals, that points to two open variables in this clause
     */
    private int lit1 = 0;
    private int lit2 = 0;

    /**
     * Creates a new clause with the given literals.
     *
     * @param literals literals of the clause
     */
    public Clause(Vector<Integer> literals) {
        this.literals = literals;
    }


    /**
     * This method initializes the watched literals for this clause.
     *
     * @param variables hashmap of the clausset variables
     * @return State of clause
     */
    public ClauseState initWatch(HashMap<Integer, Variable> variables) {

    	/* If this Clause has no literals */
        if (literals.size() == 0) {

            return ClauseState.EMPTY;
        }

    	/* If this clause is unit*/
        if (literals.size() == 1) {
            // put both watched pointer on this single literal
            lit1 = literals.get(0);
            lit2 = literals.get(0);
            variables.get(Math.abs(lit1)).getWatchedIterator().add(this);
            return ClauseState.UNIT;
        }

        //if called after a new clause was learned
        int openCount = 0;
        for (Integer literal : literals) {
            if (variables.get(Math.abs(literal)).getState() == State.OPEN) {
                openCount++;
                if (lit1 == 0) {
                    lit1 = literal;
                } else if (lit2 == 0) {
                    lit2 = literal;
                }
            }
        }
        //default set
        if (lit1 == 0)
            lit1 = literals.get(0);
        if (lit2 == 0)
            lit2 = literals.get(1);


        Variable var1 = variables.get(Math.abs(lit1));
        Variable var2 = variables.get(Math.abs(lit2));

        if (!var1.getWatched().contains(this)) {
            var1.getWatchedIterator().add(this);
        }
        if (!var2.getWatched().contains(this)) {
            var2.getWatchedIterator().add(this);
        }

        if (openCount == 1) {
            //lit 1 was set before,
            //but lit 2 was not set in this case
            lit2 = lit1;
            return ClauseState.UNIT;
        }


        return ClauseState.SUCCESS;
    }

    /**
     * This function find a new watched Literal in the hashmap by replacing the lit param
     *
     * @param variables hashmap of variables
     * @param lit literal that was set
     * @return new State of clause
     */
    public ClauseState reWatch(HashMap<Integer, Variable> variables, int lit) {
        if (literals.size() == 1) {
            //watched lit stays the same
            lit1 = literals.get(0);
            lit2 = literals.get(0);
            Variable variable = variables.get(Math.abs(lit));
            if (variable.getState() == State.OPEN) {
                return ClauseState.UNIT;
            }
            if (evaluatesPositive(lit,variables)) {
                return ClauseState.SAT;
            }else{ //works because we know that state is not open
                return ClauseState.EMPTY;
            }
        }

    	/*Check which watched Literal has to the replaced*/
        for (Integer literalInClause : literals) {
            //search only in not watched literals
            if (literalInClause != lit1 && literalInClause != lit2) {

                // If the new literal is open , or it will evaluate to 1, set as watched
                Variable variable = variables.get(Math.abs(literalInClause));
                boolean evaluatesToPositiveValue = evaluatesPositive(literalInClause,variables);
                if (variable.getState() == State.OPEN
                        || evaluatesToPositiveValue) {

                    //set the new watched literal, depending on which has to be replaced
                    if (lit == lit1) {
                        lit1 = literalInClause;
                    } else {
                        lit2 = literalInClause;
                    }
                    //removes current clause it is working on from watched.
                    variables.get(Math.abs(lit)).getWatchedIterator().remove();
                    variable.getWatchedIterator().add(this);
                    //found new watched literal
                    if (evaluatesToPositiveValue) {
                        //pointer was moved on a sat variable
                        return ClauseState.SAT;
                    }
                    return ClauseState.SUCCESS;
                }
            }
        }

        Integer otherWatchedLit = lit == lit1 ? lit2 : lit1;
        Variable otherWatchedVar = variables.get(Math.abs(otherWatchedLit));

        // If no watched literal is found and the other watched literal is not set
        if (otherWatchedVar.getState() == State.OPEN) {
            return ClauseState.UNIT;
        }

        //No new watched literal found. Check if the other watched literal evaluates to 0
        if (evaluatesNegative(otherWatchedLit,variables)) {
            //will eval to 0
            return ClauseState.EMPTY;
        }else {
            //other watched evals to true (positive)
            return ClauseState.SAT;
        }
    }

    /**
     * Returns the literals of this clause.
     *
     * @return literals of this clause
     */
    public Vector<Integer> getLiterals() {
        return literals;
    }


    /**
     * Returns an unassigned literal of this clause.
     *
     * @param variables variable objects
     * @return an unassigned literal, if one exists, 0 otherwise
     */
    public int getUnassigned(HashMap<Integer, Variable> variables) {
        //check only the two watched literals
        if (variables.containsKey(Math.abs(lit1))) {
            if (variables.get(Math.abs(lit1)).getState() == State.OPEN) {
                return lit1;    //return the literal number
            }
        }
        if (variables.containsKey(Math.abs(lit2))) {
            if (variables.get(Math.abs(lit2)).getState() == State.OPEN) {
                return lit2;    //return the literal number
            }
        }
        return 0;
    }


    /**
     * Returns the phase of the variable within this clause.
     *
     * @param num variable ID (>= 1)
     * @return true, if variable is positive within this clause, otherwise false
     */
    public boolean getPolarity(int num) {
        return literals.contains(num);
    }

    /**
     * Variable id's are always positive. Literals can be negative
     * This function finds the literal with the positive id
     *
     * @param ID of the variable
     * @return returns the literal
     */
    public Integer getLiteralFromVariableId(Integer ID) {
        //Check if literal is positiv in this clause
        if (this.getPolarity(ID)) {
            return ID;
        }
        //if not
        for (Integer lit : literals) {
            if (Math.abs(lit) == ID) {
                return lit;
            }
        }

        //clause does not contain literal with this id
        return 0;
    }

    public ClauseState getState(HashMap<Integer, Variable> variables) {
        int openCount = 0;
        for (Integer literal : literals) {
            if(evaluatesPositive(literal,variables)){
                return ClauseState.SAT;
            }
            if (variables.get(Math.abs(literal)).getState() == State.OPEN) {
                openCount++;
            }
        }
        if(openCount == 1){
            return ClauseState.UNIT;
        }
        if(openCount == 0) {
            //no open variables and not SAT
            return ClauseState.EMPTY;
        }
        //open variables available
        //return default state
        return ClauseState.SUCCESS;
    }

    /**
     * evaluates literal
     *
     * @param literal literal to be evaluated
     * @param variables hashmap of variables
     * @return if literal will eval to 1
     */
    private boolean evaluatesPositive(Integer literal, HashMap<Integer, Variable> variables) {
        Variable variable = variables.get(Math.abs(literal));
        return variable.getState() == State.FALSE && literal < 0 // true because false and negative
                || variable.getState() == State.TRUE && literal > 0;
    }

    /**
     *
     * @param literal literal to be evaluated
     * @param variables hashmap of variables
     * @return if literal will evaluate to 0
     */
    private boolean evaluatesNegative(Integer literal, HashMap<Integer, Variable> variables) {
        Variable variable = variables.get(Math.abs(literal));
        return variable.getState() == State.FALSE && literal > 0 // false because false and pos
                || variable.getState() == State.TRUE && literal < 0;
    }

    /**
     *
     * @param variable variable
     * @return true if clause contains variables
     */
    public boolean containsVariable(Variable variable){
        int id = variable.getId();
        for (Integer literal: literals){
            if(Math.abs(literal) == id){
                return  true;
            }
        }
        return false;
    }

    /**
     * Returns the size of this clause.
     *
     * @return size of this clause.
     */
    public int size() {
        return literals.size();
    }

    @Override
    public String toString() {
        String res = "{ ";
        for (Integer i : literals)
            res += i + " ";
        return res + "}";
    }

    public boolean equals(Clause otherClause) {
        return this.literals.containsAll(otherClause.getLiterals())
                && otherClause.getLiterals().containsAll(this.literals);

    }

}