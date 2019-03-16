import java.util.ArrayList;
import java.util.HashMap;

import javafx.util.Pair;

public class CDCLSolver {
    // While not all variables are assigned or the forumula is not satisfied
    //      guess a variable
    //      increase the decision level
    //      do unit propogation
    //      if there is a conflict do conflict analysis

    HashMap<Integer, Integer> decisionLevelAssigned;
    HashMap<Integer, Integer> valuesAlreadyAssigned;
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
        this.valuesAlreadyAssigned = new HashMap<Integer, Integer>();
    }

    public String solution() {
        initalSetUp(); // This is to input all the variables into the decisionLevelAssigned HashMap with variable , -1 value;
        while(numberOfVariablesAssigned != totalNumberOfVariables){
            int value = unitpropogation();

            if(value == 0) {
                guessABranchingVariable();
                currentDecisionLevel++;
                //TODO: increment the number of variables assigned
                //TODO: add it to the decisionLevelAssigned DS (with variable name, decisionLevel)
                //TODO: add this to the valuesAlreadyAssigned Map
            } else if (value == 1) {
                //TODO: UpdateValues to the Valuesalreadyassgined
                //TODO: Update The total number of assignments etc
            } else if (value == -1){
                conflictAnalysis();
            }
        }
        return "SAT";

    }

    // Handles Unit Propogation and also returns if Unit propogation has been done or not
    private int unitpropogation() {
        int value = 0;
        for (Clause c : formula) {
            Pair<Integer, ArrayList<Variable>> unitLiteralAvailable = checkAndHandleUnitLiteral(c);
            if(unitLiteralAvailable.getKey() == 1) {
                boolean isConflicting = checkConflict(unitLiteralAvailable.getValue(), valuesAlreadyAssigned);
                if(isConflicting) {
                    return value = -1;
                }
                value = 1;
            }
        }
        return value;
    }

    private boolean checkConflict(ArrayList<Variable> value, HashMap<Integer, Integer> valuesAlreadyAssigned) {
        boolean isConflict = false;
        for(Variable v : value) {
            int variableNameToDeal = v.getVariableName();
            if(decisionLevelAssigned.get((Math.abs(variableNameToDeal)))  != 1) {
                int variableValueToCompare;
                if(v.getVariableValue() == true) {
                    variableValueToCompare = 1;
                } else {
                    variableValueToCompare = 0;
                }

                isConflict = isConflict && (variableValueToCompare == valuesAlreadyAssigned.get((Math.abs(variableNameToDeal))));

            }
        }
        return isConflict;
    }


    // Tries to find if there is only one unassigned Variable and return true or false.
    // It also tries to assign a value to the unassigned value if there is only one such variable and return it.
    private Pair<Integer, ArrayList<Variable>> checkAndHandleUnitLiteral(Clause c) {
        ArrayList<Variable> tempdisjunc = c.getOrVariables();
        Pair<Integer, ArrayList<Variable>> returnValues;
        Variable unassignedVariable = null;
        int numberOfUnassignedVariables = 0;
        if(tempdisjunc.size() == 1) {
            if(decisionLevelAssigned.get(tempdisjunc.get(0).getVariableName()) == -1) {
                unassignedVariable = tempdisjunc.get(0);
                ArrayList<Variable> valuesAssignedFromUnitProp = checkAndInputValueForVariable(unassignedVariable, c);
                returnValues = new Pair<Integer, ArrayList<Variable>>(1, valuesAssignedFromUnitProp);
            } else {
                returnValues = new Pair<Integer, ArrayList<Variable>>(0, new ArrayList<Variable>());
            }
        } else {
            for (Variable v : tempdisjunc) {
                if(decisionLevelAssigned.get(v.getVariableName()) == -1) {
                    numberOfUnassignedVariables++;
                    unassignedVariable = v;
                }
            }
            if(numberOfUnassignedVariables == 1) {
                ArrayList<Variable> valuesAssignedFromUnitProp = checkAndInputValueForVariable(unassignedVariable, c);
                returnValues = new Pair<Integer, ArrayList<Variable>>(1, valuesAssignedFromUnitProp);
            } else {
                returnValues = new Pair<Integer, ArrayList<Variable>>(0, new ArrayList<Variable>());
            }
        }
        return returnValues;

    }

    // Trying to assign the unassigned variables identified through unitPropogation
    private ArrayList<Variable> checkAndInputValueForVariable(Variable unassignedVariable, Clause c) {
        ArrayList<Variable> valuesAssignedFromUnitProp = new ArrayList<Variable>();
        if(c.getOrVariables().size() == 1) {
            if(unassignedVariable.getVariableName() < 0) {
                unassignedVariable.setVariableValue(false);
            } else {
                unassignedVariable.setVariableValue(true);
            }
            valuesAssignedFromUnitProp.add(unassignedVariable);
            return valuesAssignedFromUnitProp;
        } else {
            ArrayList<Variable> tempdisjunc = c.getOrVariables();
            tempdisjunc.remove(unassignedVariable);
            boolean truthValue = calculateTruthOfClause(tempdisjunc);
            if(truthValue = false) {
                if(unassignedVariable.getVariableName() < 0) {
                    unassignedVariable.setVariableValue(false);
                } else {
                    unassignedVariable.setVariableValue(true);
                }
                valuesAssignedFromUnitProp.add(unassignedVariable);
            } else {
                if(unassignedVariable.getVariableName() < 0) {
                    unassignedVariable.setVariableValue(false);
                } else {
                    unassignedVariable.setVariableValue(true);
                }
                valuesAssignedFromUnitProp.add(unassignedVariable);
            }
        }

        return valuesAssignedFromUnitProp;

    }

    //Calculate if the clause is true or false until now based on which the unassigned value can be identified.
    private boolean calculateTruthOfClause(ArrayList<Variable> tempdisjunc) {
        int numberOfTrueVariables = 0;
        for(Variable v : tempdisjunc) {
            if(v.getVariableName() < 0) {
                if(valuesAlreadyAssigned.get(v.getVariableName() * -1) == 0) {
                    numberOfTrueVariables++;
                }
            } else {
                if(valuesAlreadyAssigned.get(v.getVariableName() * -1) == 1) {
                    numberOfTrueVariables++;
                }
            }
        }
        if(numberOfTrueVariables > 0) {
            return true;
        } else {
            return false;
        }
    }

}
