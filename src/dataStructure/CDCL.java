package dataStructure;

import java.util.ArrayList;
import java.util.Stack;
import java.util.Vector;

/**
 * Created by Benjamin Vollmer 3590766 und Kien-Van Quang 3746183 on 12.12.16.
 */
public class CDCL {

    //set this flag, if you want more output in your tests
    public boolean verboseOutput;

    protected Stack<Variable> stack;
    protected ClauseSet instance;
    private Vector<Clause> learnedClauses;
    private boolean DEFAULT_ASSING = false;
    private int MAX_ITERATIONS = 10000;
    private int iterationCounter = 0;


    /**
     * Constructor
     *
     * @param instance Clause Set instance the CDCL works on
     */
    public CDCL(ClauseSet instance) {
        this.instance = instance;
        this.stack = new Stack<Variable>();
        this.learnedClauses = new Stack<Clause>();
    }

    /**
     * Performs CDCL on given instance.
     *
     * @return Returns true if SAT
     */
    public boolean solve() {
        stack = new Stack<Variable>();
        while (true) {
            if (verboseOutput) {
                printResultStack();
            }
            Clause emptyClause = instance.unitPropagation(stack);
            if (emptyClause != null) {
                if (verboseOutput)
                    System.out.println("!: Empty Clause! " + emptyClause.toString() + "\nAnalyse conflict..");

                if (!handleEmptyClause(emptyClause)) {
                    System.out.println("Could not find a solution for empty clause: UNSAT");
                    if (verboseOutput)
                        printUNSATClauses();
                    return false; //UNSAT
                }
            } else {
                if (modelFound()) {
                    System.out.println("Model for DIMACS equation was found!");
                    printResultStack();
                    return true; //SAT
                }

                instance.setGlobalLevel(instance.getGlobalLevel() + 1);
                Variable nextVar = getNextVar();
                if (nextVar == null) {
                    System.out.println(">> No next or OPEN variable!: UNSAT");
                    if (verboseOutput)
                        printUNSATClauses();
                    return false;
                }
                if (verboseOutput)
                    System.out.println("> Next decision Var: " + nextVar.getId());
                lowerActivityOfAllVariables();
                instance.decisionAssignVariable(nextVar, DEFAULT_ASSING);
                stack.push(nextVar);
            }

            //safety feature
            iterationCounter++;
            if (iterationCounter > MAX_ITERATIONS) {
                System.out.println("Maximum iterations reached!!!");
                break;
            }
        }
        return false;
    }

    /**
     * Prints stack of set variables
     */
    public void printResultStack() {
        System.out.print(this.toString());
        System.out.flush();
    }

    /**
     * Prints all unsatisfied clauses
     */
    public void printUNSATClauses() {
        System.out.println("UNSAT Clauses");
        for (Clause clause : instance.getClauses()) {
            if (clause.getState(instance.getVariables()) != Clause.ClauseState.SAT) {
                System.out.println(clause.toString() + " STATE: " + clause.getState(instance.getVariables()));
            }
        }
    }

    /**
     * Handles empty clauses.
     *
     * @param emptyClause the empty clause of the conflict
     * @return false if no clause could be learned OR backtracklevel == -1
     */
    private boolean handleEmptyClause(Clause emptyClause) {
        //remove if unit
        if (instance.getUnits().contains(emptyClause)) {
            instance.getUnits().remove(instance.getUnits().indexOf(emptyClause));
        }
        //learn new clause
        @SuppressWarnings("unchecked") Clause learnedClause =
                analyseConflict(emptyClause, (Stack<Variable>) stack.clone());
        if (learnedClause == null) {
            return false;
        } else {
            int backTrackLevel = getBackTrackLevel(learnedClause);
            if (backTrackLevel == -1) {
                return false; //UNSAT
            }
            backtrack(backTrackLevel);
            instance.setGlobalLevel(Math.max(backTrackLevel, 0));

            if (!clauseAlreadyLearned(learnedClause)) {
                //clause not learned yet
                //init its watched literals
                Clause.ClauseState learnedClauseState = learnedClause.initWatch(instance.getVariables());
                if (learnedClauseState == Clause.ClauseState.UNIT) {
                    //instance.getUnits().add(0, learnedClause);
                    instance.getUnits().add(learnedClause);
                }
                if (verboseOutput)
                    System.out.println("> Learned new Clause " + learnedClause.toString() + " STATE: " + learnedClauseState);
                raiseActivityOfVariablesInClause(learnedClause);
                //add to list of clauses
                instance.getClauses().add(learnedClause);
                //add to list of learned clauses
                learnedClauses.add(learnedClause);
            }
            if (verboseOutput)
                System.out.println("Backtrack level: " + instance.getGlobalLevel());
        }
        return true;
    }

    private void backtrack(int backTrackLevel) {
        while (!stack.isEmpty() && stack.peek().getLevel() > backTrackLevel) {
            Variable backTrackVar = stack.pop();
            backTrackVar.unassign();
        }
        //instance.updateUnits();
    }

    private boolean modelFound() {
        for (Clause clause : instance.getClauses()) {
            if (clause.getState(instance.getVariables()) != Clause.ClauseState.SAT) {
                return false;
            }
        }
        return true;
    }

    /**
     * Calculates Resovle of c1 and c2
     *
     * @param c1 Clause
     * @param c2 Clause
     * @return resolvent clause
     */
    private Clause resolve(Clause c1, Clause c2) {
        Vector<Integer> resolvedClause = new Vector<Integer>();
        resolvedClause.addAll(c1.getLiterals());
        for (Integer integer : c2.getLiterals()) {
            if (resolvedClause.contains(integer * -1)) {
                resolvedClause.remove(resolvedClause.indexOf(integer * -1));
                continue;
            }
            if (!resolvedClause.contains(integer)) {
                resolvedClause.add(resolvedClause.size(), integer);
            }

        }
        if (resolvedClause.size() != 0) {
            return new Clause(resolvedClause);
        }
        // If after applying a resolution rule the empty
        // clause is derived, the original formula
        // is unsatisfiable (or contradictory).
        return null;
    }

    /**
     * Calculates 1UP Clause on the basis of an empty clause and a reason clause
     * Unassigned given variable
     *
     * @param conflict       Empty Clause
     * @param reasonVariable Clause
     * @return returns 1UIP OR Null if failed
     */
    private Clause get1UIP(Clause conflict, Variable reasonVariable, Stack<Variable> stackCopy) {
        Clause resolvedClause = null;
        if (reasonVariable.getReason() == null) {
            //was decision
            return learnedClauseFromDecision(reasonVariable);
        } else {
            if (conflict.containsVariable(reasonVariable)) {
                resolvedClause = resolve(conflict, reasonVariable.getReason());
                if (resolvedClause != null && checkIf1UIP(resolvedClause)) {
                    //reasonVariable.unassign(instance.getVariables(), instance.getUnits());
                    return resolvedClause;
                }
            }
            if (!stackCopy.isEmpty()) {
                Variable var = stackCopy.pop();
                if (resolvedClause != null) {
                    return get1UIP(resolvedClause, var, stackCopy);
                } else {
                    return get1UIP(conflict, var, stackCopy);
                }
            }

        }
        return null;
    }

    private Clause learnedClauseFromDecision(Variable decisionVariable) {
        Vector<Integer> uipLiteral = new Vector<Integer>();
        if (decisionVariable.getState() == Variable.State.FALSE) {
            uipLiteral.add(decisionVariable.getId());
        } else {
            uipLiteral.add(decisionVariable.getId() * -1);
        }
        return new Clause(uipLiteral);
    }

    /**
     * Definition 1UIP: only one variable has highest level
     *
     * @param clause
     * @return true if clause is 1UIP
     */
    private boolean checkIf1UIP(Clause clause) {
        int maxLevel = 0;
        boolean onlyOneMaxLevel = true;
        for (Integer literal : clause.getLiterals()) {
            Variable var = instance.getVariables().get(Math.abs(literal));
            if (var.getLevel() > maxLevel) {
                maxLevel = var.getLevel();
                onlyOneMaxLevel = true;
            } else if (var.getLevel() == maxLevel) {
                onlyOneMaxLevel = false;
            }
        }
        return onlyOneMaxLevel;
    }


    /**
     * Tracks reason of conflict with the last variable on the stack.
     * Learns a new clause (1UP) from the conflict and returns the level
     * to which one has to jump back
     * Adds learned clauses to ClauseSet
     *
     * @param conflict Empty clause
     * @return Learned Clause
     */
    private Clause analyseConflict(Clause conflict, Stack<Variable> stackCopy) {
        //if (!stackCopy.isEmpty() && instance.getGlobalLevel() != 0) {
        if (!stackCopy.isEmpty()) {
            //last non conflict assigned
            Variable reasonVariable = null;
            reasonVariable = stackCopy.pop();
            return get1UIP(conflict, reasonVariable, stackCopy);
        }
        return null;
    }


    private int getBackTrackLevel(Clause clause) {
        if (clause == null) {
            return -1;
        }
        if (clause.getLiterals().size() == 1) {
            //decision clause
            return instance.getVariables().get(Math.abs(clause.getLiterals().get(0))).getLevel() - 1;
        }
        int maxLevel = -1;
        int secondMaxLevel = -1;
        for (Integer literal : clause.getLiterals()) {
            Variable var = instance.getVariables().get(Math.abs(literal));
            if (var.getLevel() > maxLevel) {
                secondMaxLevel = maxLevel;
                maxLevel = var.getLevel();
            } else if (var.getLevel() > secondMaxLevel) {
                secondMaxLevel = var.getLevel();
            }
        }
        return secondMaxLevel;
    }


    /**
     * Returns varianle with highest activity
     *
     * @return next variable that has to be assigned.
     * Null if no matching variable found
     * && if no State.OPEN variable found
     */
    protected Variable getNextVar() {
        Variable maxActivityVariable = null;
        for (Variable var : this.instance.getVariables().values()) {
            if (var.getState() != Variable.State.OPEN) {
                //skip variable if it is not open
                continue;
            }
            if (maxActivityVariable == null) {
                maxActivityVariable = var;
                continue;
            }
            if (var.getActivity() > maxActivityVariable.getActivity()) {
                maxActivityVariable = var;
            }
        }
        return maxActivityVariable;
    }

    private void raiseActivityOfVariablesInClause(Clause clause) {
        for (Integer literal : clause.getLiterals()) {
            Variable variable = instance.getVariables().get(Math.abs(literal));
            variable.setActivity(variable.getActivity() * 1.10f);
        }
    }

    /**
     * Lowers the activity of all variables with
     * factor 0.95 float
     */
    private void lowerActivityOfAllVariables() {
        for (Variable variable : instance.getVariables().values()) {
            variable.setActivity(variable.getActivity() * 0.95f);
        }
    }

    private boolean clauseAlreadyLearned(Clause c) {
        for (Clause clause : learnedClauses) {
            if (clause.equals(c)) {
                return true;
            }
        }
        return false;
    }

    @Override
    public String toString() {
        String result = "Iterations: " + iterationCounter + "\n";
        result += String.format("%5s|%5s|%10s|%10s|%50s|\n", "Nr", "Lvl", "ID", "STATE", "Reason");
        result += String.format("%84s|\n", "").replace(" ", "-");
        ArrayList<Variable> stackList = new ArrayList<Variable>(stack);
        int rowIndex = 1;
        for (Variable var : stackList) {
            //Variable var = (Variable) copiedStack.pop();
            if (var.getReason() == null) {
                result += String.format("%5d|%5d|%10d|%10s|%50s|\n", rowIndex, var.getLevel(),
                        var.getId(), var.getState().toString(), "Decision");
            } else {
                Clause reason = var.getReason();
                result += String.format("%5d|%5d|%10d|%10s|%50s|\n", rowIndex,
                        var.getLevel(), var.getId(), var.getState(),
                        reason.toString());
            }
            rowIndex++;
        }
        result += "Learned clauses:\n";
        for (Clause learned : learnedClauses) {
            result += learned.toString() + "\n";
        }
        return result;
    }

    public void setDEFAULT_ASSING(boolean DEFAULT_ASSING) {
        this.DEFAULT_ASSING = DEFAULT_ASSING;
    }
}
