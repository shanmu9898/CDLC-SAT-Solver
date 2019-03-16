import java.util.ArrayList;
import java.util.HashMap;

import sun.tools.jstat.Literal;

public class CDCLSolver {
    // While not all variables are assigned or the forumula is not satisfied
    //      guess a variable
    //      increase the decision level
    //      do unit propogation
    //      if there is a conflict do conflict analysis

    HashMap<Integer, Integer> decisionLevelAssigned;
    boolean[] solution;
    int numberOfVariablesAssigned;
    int totalNumberOfClauses;
    int totalNumberOfVariables;
    ArrayList<Clause> formula;
    int currentDecisionLevel;

    public CDCLSolver(int totalNumberOfClauses, int totalNumberOfVariables, ArrayList<Clause> formula) {
        this.totalNumberOfClauses = totalNumberOfClauses;
        this.totalNumberOfVariables = totalNumberOfVariables;
        this.formula = formula;
        this.numberOfVariablesAssigned = 0;
        this.solution = new boolean[totalNumberOfVariables];
        this.decisionLevelAssigned = new HashMap<Integer, Integer>();
        this.currentDecisionLevel = 0;
    }

    public String solution() {
        while(numberOfVariablesAssigned != totalNumberOfVariables){
            int value = unitpropogation();

            if(value == 0) {
                guess();
            } else if (value == 1) {
                if(conflict()) {
                    conflictAnalysis();
                }

            } else {
                return "UNSAT";
            }
        }
        return "UNSAT";

    }

    private int unitpropogation() {
        for (Clause c : formula) {
            boolean unitLiteralAvailable = checkAndHandleUnitLiteral(c);
            if(unitLiteralAvailable) {
                return 1;
            } else {
                return 0;
            }
        }
    }

    private boolean checkAndHandleUnitLiteral(Clause c) {
        ArrayList<Variable> tempdisjunc = c.getDisjunctions();
        Variable unassignedVariable = null;
        int numberOfUnassignedVariables = 0;
        if(tempdisjunc.size() == 1) {
            unassignedVariable = tempdisjunc.get(0);

        } else {
            for (Variable v : tempdisjunc) {
                if(decisionLevelAssigned.get(v.getVariableIdentifier()) == -1) {
                    numberOfUnassignedVariables++;
                    unassignedVariable = v;
                }
            }
            if(numberOfUnassignedVariables == 1) {
                checkAndInputValueForVariable(unassignedVariable, c);
            }
        }

    }

    private void checkAndInputValueForVariable(Variable unassignedVariable, Clause c) {
        if(clauseIsFalse(c)) {

        }
    }

}
