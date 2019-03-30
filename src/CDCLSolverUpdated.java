import java.lang.reflect.Array;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.Random;

import javafx.util.Pair;

public class CDCLSolverUpdated {
    // While not all variables are assigned or the forumula is not satisfied
    //      guess a variable
    //      increase the decision level
    //      do unit propogation
    //      if there is a conflict do conflict analysis

    HashMap<Integer, Integer> decisionLevelAssigned;
    ArrayList<Variable> valuesAlreadyAssigned;
    int numberOfVariablesAssigned;
    int totalNumberOfClauses;
    int totalNumberOfVariables;
    ArrayList<Clause> formula;
    int currentDecisionLevel;
    Clause lastDecidedClause;
    ArrayList<Variable> lastDecidedVariables;
    HashMap<Integer, Integer> variablesAssignment;
    boolean guessHasStarted = false;


    public CDCLSolverUpdated(int totalNumberOfClauses, int totalNumberOfVariables, ArrayList<Clause> formula) {
        this.totalNumberOfClauses = totalNumberOfClauses;
        this.totalNumberOfVariables = totalNumberOfVariables;
        this.formula = formula;
        this.numberOfVariablesAssigned = 0;
        this.decisionLevelAssigned = new HashMap<Integer, Integer>();
        this.currentDecisionLevel = 0;
        this.valuesAlreadyAssigned = new ArrayList<Variable>();
        this.variablesAssignment = new HashMap<Integer, Integer>(); // Should variables assignment be increased when it is unit propogated?
        this.lastDecidedVariables = new ArrayList<Variable>();


    }

    public String solution() {
        initialSetUp(formula); // This is to input all the variables into the decisionLevelAssigned HashMap with variable , -1 value;

        if(unitpropogation() == -1) {
            return "UNSAT";
        }

        currentDecisionLevel = 0;

        while(numberOfVariablesAssigned != totalNumberOfVariables) {
            guessABranchingVariable(formula);
            currentDecisionLevel++;
            if(unitpropogation() == -1) {
                int decisionLevelToBackTrack = conflictAnalysis(formula, valuesAlreadyAssigned);
                if(decisionLevelToBackTrack < 0) {
                    return "UNSAT";
                } else {
                    int code = backtrack(formula, valuesAlreadyAssigned, decisionLevelToBackTrack);
                    if(code == 1) {
                        currentDecisionLevel = decisionLevelToBackTrack;
                    } else {
                        return "UNSAT";
                    }

                }
            }

        }
        return "SAT";


    }

    private void initialSetUp(ArrayList<Clause> formula) {
        for(Clause c: formula) {
            for (int i=0; i<c.getOrVariables().size(); i++) {
                Variable current = c.getOrVariables().get(i);
                Variable temp = current.modVariableName();
                decisionLevelAssigned.put(temp.getVariableName(), -1);
                variablesAssignment.put(temp.getVariableName(), 0);
            }
        }
    }

    private int backtrack(ArrayList<Clause> formula, ArrayList<Variable> valuesAlreadyAssigned, int decisionLevelToBackTrack) {
        int code = 0;
        for(Variable v : valuesAlreadyAssigned) {
            if(decisionLevelAssigned.get(v.getVariableName()) > decisionLevelToBackTrack) {
                variablesAssignment.put(v.getVariableName(), 0);
                decisionLevelAssigned.put(v.getVariableName(), -1);
                valuesAlreadyAssigned.remove(v);
                lastDecidedVariables.remove(v);
                numberOfVariablesAssigned--;
                code = 1;

            } else if (decisionLevelAssigned.get(v.getVariableName()) == decisionLevelToBackTrack) {
                if(lastDecidedVariables.contains(v)) {
                    if(variablesAssignment.get(v.getVariableName()) == 1) {
                        Variable temp = v.modVariableName();
                        valuesAlreadyAssigned.remove(v);
                        lastDecidedVariables.remove(v);
                        if(temp.getVariableValue()) {
                            temp.setVariableValue(false);
                        } else {
                            temp.setVariableValue(true);
                        }
                        valuesAlreadyAssigned.add(temp);
                        lastDecidedVariables.add(temp);
                        variablesAssignment.put(temp.getVariableName(),2);
                        code = 1;
                    }else if (variablesAssignment.get(v.getVariableName()) == 2) {
                        code = 2;
                        return code;

                    }
                }

            }

        }
        return code;


    }

    private boolean checkIfValid() { //if at least 1 variable in each clause is assigned
        boolean isValid = true;
        for(Clause c : formula){
            int numberOfUnassignedVariables = 0;
            ArrayList<Variable> tempdisjunc = c.getOrVariables();
            for (Variable v : tempdisjunc) {
                Variable temp = v.modVariableName();
                if(decisionLevelAssigned.get(temp.getVariableName()) == -1) {
                    numberOfUnassignedVariables++;
                }
            }
            if(numberOfUnassignedVariables == tempdisjunc.size()) {
                return false;
            } else {
                isValid = isValid && calculateTruthOfClause(tempdisjunc);
            }

        }
        return isValid;


    }

    // Handles Unit Propogation and also returns if Unit propogation has been done or not
    private int unitpropogation() {
        int value = 0;
        for (Clause c : formula) {
            Pair<Integer, ArrayList<Variable>> unitLiteralAvailable = checkAndHandleUnitLiteral(c);

            if(unitLiteralAvailable.getKey() == 1) {
                Pair<Integer, ArrayList<Variable>> isConflicting = checkConflict(unitLiteralAvailable.getValue(), valuesAlreadyAssigned);
                if(isConflicting.getKey() == 1) {
                    return -1;
                } else {
                    for(Variable v : unitLiteralAvailable.getValue()) {
                        Variable temp = v.modVariableName();
                        valuesAlreadyAssigned.add(temp);
                        decisionLevelAssigned.put(temp.getVariableName(), currentDecisionLevel);
                        numberOfVariablesAssigned++;
                    }
                    unitpropogation();
                }
                value = 1;
            }
        }
        System.out.println("value is" + value);

        return value;
    }

    private void conflictLearn(Clause c, ArrayList<Variable> value) {
        ArrayList<Variable> clauseToLearn = new ArrayList<Variable>();
        for(Variable v : lastDecidedClause.getOrVariables()) {
            clauseToLearn.add(v);
        }
        for(Variable v : c.getOrVariables()) {
            if(value.contains(v)) {
                clauseToLearn.add(v);
            }
        }
        formula.add(new Clause(value));
    }



    private Pair<Integer, ArrayList<Variable>> checkConflict(ArrayList<Variable> value, ArrayList<Variable> valuesAlreadyAssigned) {
        int isConflict = 0;
        ArrayList<Variable> variablesToAdd = new ArrayList<Variable>();
        for(Variable v : value) {
            int variableNameToDeal = v.getVariableName();
            if(decisionLevelAssigned.get((Math.abs(variableNameToDeal)))  != -1) {
                int index = valuesAlreadyAssigned.indexOf(v);
                Variable variableInAssigned = valuesAlreadyAssigned.get(index);

                if(variableInAssigned.getVariableValue() != v.getVariableValue()) {
                    variablesToAdd.add(v);
                    isConflict = 1;
                }



            }
        }
        return new Pair<Integer, ArrayList<Variable>>(isConflict, variablesToAdd);
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
                ArrayList<Variable> variablesAssignedFromUnitProp = checkAndInputValueForVariable(unassignedVariable, c);
                returnValues = new Pair<Integer, ArrayList<Variable>>(1, variablesAssignedFromUnitProp);
            } else {
                returnValues = new Pair<Integer, ArrayList<Variable>>(0, new ArrayList<Variable>());
            }
        } else {
            for (Variable v : tempdisjunc) {
                Variable temp = v.modVariableName();
                if(decisionLevelAssigned.get(temp.getVariableName()) == -1) {
                    numberOfUnassignedVariables++;
                    unassignedVariable = v;
                }
            }
            if(numberOfUnassignedVariables == 1) {
                ArrayList<Variable> variablesAssignedFromUnitProp = checkAndInputValueForVariable(unassignedVariable, c);
                if(variablesAssignedFromUnitProp.size() != 0) {
                    returnValues = new Pair<Integer, ArrayList<Variable>>(1, variablesAssignedFromUnitProp);
                } else {
                    returnValues = new Pair<Integer, ArrayList<Variable>>(0, new ArrayList<Variable>());
                }

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
            Variable temp = unassignedVariable.modVariableName();
            if(unassignedVariable.getVariableName() < 0) {
                temp.setVariableValue(false);
            } else {
                temp.setVariableValue(true);
            }
            valuesAssignedFromUnitProp.add(temp);
            return valuesAssignedFromUnitProp;
        } else {
            ArrayList<Variable> tempdisjunc1 = c.getOrVariables();
            ArrayList<Variable> tempdisjunc = (ArrayList<Variable>) tempdisjunc1.clone();
            tempdisjunc.remove(unassignedVariable);
            boolean truthValue = calculateTruthOfClause(tempdisjunc);
            if(truthValue == false) {
                Variable temp = unassignedVariable.modVariableName();
                if(unassignedVariable.getVariableName() < 0) {
                    temp.setVariableValue(false);
                } else {
                    temp.setVariableValue(true);
                }
                valuesAssignedFromUnitProp.add(temp);
            }
        }

        return valuesAssignedFromUnitProp;

    }

    //Calculate if the clause is true or false until now based on which the unassigned value can be identified.
    private boolean calculateTruthOfClause(ArrayList<Variable> tempdisjunc) {
        int numberOfTrueVariables = 0;
        for(Variable v : tempdisjunc) {
            //System.out.println(v);
            if(v.getVariableName() < 0) {
                Variable temp = v.modVariableName();
                if (valuesAlreadyAssigned.indexOf(temp) >= 0) {
                    if (!valuesAlreadyAssigned.get(valuesAlreadyAssigned.indexOf(temp)).getVariableValue()) {
                        numberOfTrueVariables++;
                    }
                }
            } else {
                Variable temp = v.modVariableName();
                if (valuesAlreadyAssigned.indexOf(temp) >= 0) {
                    if (!valuesAlreadyAssigned.get(valuesAlreadyAssigned.indexOf(temp)).getVariableValue()) {
                        numberOfTrueVariables++;
                    }
                }
            }
        }
        if(numberOfTrueVariables > 0) {
            return true;
        } else {
            return false;
        }
    }

    private void guessABranchingVariable(ArrayList<Clause> formula ) {
        Random randomClauseGenerator = new Random();
        guessHasStarted = true;
        int index = randomClauseGenerator.nextInt(formula.size());
        Clause c = formula.get(index);
        pickRandomVariable(c);
    }

    private void pickRandomVariable(Clause c) {
        c.getOrVariables();
        Random randomVariableGenerator = new Random();
        int index = randomVariableGenerator.nextInt(c.getOrVariables().size());
        Variable var = c.getOrVariables().get(index);
        Random ranValue = new Random();
        int value = ranValue.nextInt(2);
        boolean val = false;
        if (value == 0) {
            val = false;
        } else {
            val = true;
        }
        var.setVariableValue(val);
        numberOfVariablesAssigned++;
        Variable temp = var.modVariableName();
        System.out.println(temp);
        decisionLevelAssigned.put(temp.getVariableName(), currentDecisionLevel);
        valuesAlreadyAssigned.add(temp);
        variablesAssignment.put(temp.getVariableName(), 1);
        lastDecidedVariables.add(temp);
    }
}
