package dataStructure;

import dimacs.parser.DIMACSParser;

import java.util.HashMap;
import java.util.Stack;
import java.util.Vector;

/**
 * A set of clauses.
 */
public class ClauseSet {
    /* Number of variables */
    private int varNum;

    /* Clauses of this set */
    private Vector<Clause> clauses;

    private Vector<Clause> units;

    //Vector to keep track of new variables
    private Vector<Clause> newUnitsQueue;

    /* List of all variables */
    private HashMap<Integer, Variable> variables;

    private int globalLevel = 0;

    /**
     * Constructs a clause set from the given DIMACS file.
     *
     * @param filePath file path of the DIMACS file.
     */
    public ClauseSet(String filePath) {
        variables = new HashMap<Integer, Variable>();
        clauses = new Vector<Clause>();
        units = new Vector<Clause>();
        DIMACSParser parser = new DIMACSParser(filePath);
        Vector<Vector<Integer>> parsedForumla = parser.parseFile();
        for (Vector<Integer> clauseVec : parsedForumla) {
            //Create new clause
            Clause clause = new Clause(clauseVec);
            for (Integer varId : clauseVec) {
                int id = Math.abs(varId);
                updateVariablesByIdMap(id);
            }
            //check initial watched literals
            Clause.ClauseState cState = clause.initWatch(variables);
            if (cState == Clause.ClauseState.UNIT) {
                units.addElement(clause);
            }
            clauses.addElement(clause);

        }

    }

    void updateVariablesByIdMap(Integer id) {
        if (!variables.containsKey(id)) {
            Variable variable = new Variable(id);
            variable.setActivity(1.0f); //first occurrence
            variables.put(id, variable);
        } else {
            variables.get(id).setActivity(variables.get(id).getActivity() + 1.0f);
        }
    }

    /**
     * Executes unit propagation and checks for the existence of an empty
     * clause.
     *
     * @return Empty clause if it exists else null
     */
    public Clause unitPropagation(Stack<Variable> variableStack) {
        updateUnits();
        if (newUnitsQueue == null) {
            //create new vector
            newUnitsQueue = new Vector<Clause>();
        } else if (newUnitsQueue.size() != 0) {
            units.addAll(newUnitsQueue);
            //clear newUnitsQueue because it was
            //already added to the units vector
            newUnitsQueue = new Vector<Clause>();
        }
        if (units.size() == 0) {
            return null;
        }
        int unitIndex = -1;
        while (newUnitsQueue.size() != 0 || unitIndex == -1) {
            newUnitsQueue = new Vector<Clause>();
            unitIndex = 0;
            while (unitIndex < units.size()) {
                //System.out.println("unitIndex : " + unitIndex);
                Clause unit = units.get(unitIndex);
                //System.out.println("Unit: " + unit.toString());
                Clause emptyClause = propagate(unit, variableStack);
                if (emptyClause != null) {
                    units.remove(units.indexOf(unit));
                    //units = new Vector<Clause>();
                    return emptyClause;
                }
                unitIndex++;
                //System.out.println("---");
            }
            //delete already processed units
            unitIndex = 0;
            units = new Vector<Clause>();
            units.addAll(newUnitsQueue);
        }
        //Unit propagation complete
        //no empty clause found
        return null;
    }

    private void updateUnits() {
        if (units == null)
            units = new Vector<Clause>();
        for(Clause clause: clauses) {
            Clause.ClauseState clauseState = clause.getState(variables);
            //add or remove new/old units
            if (clauseState == Clause.ClauseState.UNIT && !units.contains(clause)) {
                units.addElement(clause);
            } else if (clauseState != Clause.ClauseState.UNIT && units.contains(clause)) {
                units.remove(units.indexOf(clause));
            }
        }
    }


    private Clause propagate(Clause currentUC, Stack<Variable> variableStack) {
        Clause.ClauseState state = currentUC.getState(variables);
        if(state == Clause.ClauseState.EMPTY){
            return currentUC;
        }
        if(state != Clause.ClauseState.UNIT){
            //not a unit
            return null;
        }
        int literal = currentUC.getUnassigned(variables);
        if (literal != 0) { // check if no unassigned
            Clause emptyClause;
            if (literal > 0) {
                emptyClause = variables.get(literal).assign(true, variables, newUnitsQueue, globalLevel, currentUC);
            } else {
                emptyClause = variables.get(Math.abs(literal)).assign(false, variables, newUnitsQueue, globalLevel, currentUC);
            }
            variableStack.push(variables.get(Math.abs(literal)));
            return emptyClause;
        } else {
            //Unit Clause has become SAT while UP
            System.out.println("Unit " + currentUC.toString() + " has no unassigned watched literals");
        }
        return null;
    }

    /**
     * assign variable from decision
     * Reason is null by default
     *
     * @param variable variable to be assigned
     * @param value value of decision
     */
    public void decisionAssignVariable(Variable variable, boolean value) {
        if (newUnitsQueue == null) {
            newUnitsQueue = new Vector<Clause>();
        }
        variable.assign(value, variables, newUnitsQueue, globalLevel, null);
    }

    @Override
    public String toString() {
        return clausesToString() + "\n\n" + varsToString();
    }

    /**
     * Returns all clauses as string representation.
     *
     * @return a string representation of all clauses.
     */
    public String clausesToString() {
        String res = "";
        for (Clause clause : clauses)
            res += clause + "\n";
        return res;
    }

    /**
     * Returns all variables as string representation.
     *
     * @return a string representation of all variables.
     */
    public String varsToString() {
        String res = "";
        for (int i = 1; i <= varNum; i++)
            res += "Variable " + i + ": " + variables.get(i) + "\n\n";
        return res;
    }

    public HashMap<Integer, Variable> getVariables() {
        return variables;
    }

    public Vector<Clause> getClauses() {
        return clauses;
    }

    public int getGlobalLevel() {
        return globalLevel;
    }

    public void setGlobalLevel(int globalLevel) {
        this.globalLevel = globalLevel;
    }

    public Vector<Clause> getUnits() {
        return units;
    }
}
