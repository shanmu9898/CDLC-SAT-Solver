import java.util.ArrayList;
import java.util.HashMap;
import java.util.Random;

import javafx.util.Pair;

public class CDCLSolver {
    // While not all variables are assigned or the forumula is not satisfied
    //      guess a variable
    //      increase the decision level
    //      do unit propogation
    //      if there is a conflict do conflict analysis

    HashMap<Integer, Integer> decisionLevelAssigned;
    ArrayList<Variable> valuesAlreadyAssigned;
    boolean[] solution;
    int numberOfVariablesAssigned;
    int totalNumberOfClauses;
    int totalNumberOfVariables;
    ArrayList<Clause> formula;
    int currentDecisionLevel;
    Clause lastDecidedClause;
    ArrayList<Variable> lastDecidedVariables;
    HashMap<Integer, Integer> variablesAssigment;

    public CDCLSolver(int totalNumberOfClauses, int totalNumberOfVariables, ArrayList<Clause> formula) {
        this.totalNumberOfClauses = totalNumberOfClauses;
        this.totalNumberOfVariables = totalNumberOfVariables;
        this.formula = formula;
        this.numberOfVariablesAssigned = 0;
        this.solution = new boolean[totalNumberOfVariables];
        this.decisionLevelAssigned = new HashMap<Integer, Integer>();
        this.currentDecisionLevel = 0;
        this.valuesAlreadyAssigned = new ArrayList<Variable>();
    }

    public String solution() {
        initalSetUp(); // This is to input all the variables into the decisionLevelAssigned HashMap with variable , -1 value;
        while(numberOfVariablesAssigned != totalNumberOfVariables){
            int value = unitpropogation();

            if(value == 0) {
                guessABranchingVariable(formula);
                currentDecisionLevel++;
                //TODO: increment the number of variables assigned
                //TODO: add it to the decisionLevelAssigned DS (with variable name, decisionLevel)
                //TODO: add this to the valuesAlreadyAssigned Map
            } else if (value == 1) {
                if(checkIfValid()) {
                    if(lastDecidedVariables.size() == 0) {
                        return "UNSAT";
                    }

                    backtrack();

                    if(valuesAlreadyAssigned.size() == 0) {
                        return "UNSAT";
                    }
                }
            } else if (value == -1){

                if(lastDecidedVariables.size() == 0) {
                    return "UNSAT";
                }

                backtrack();
            }
        }
        return "SAT";

    }

    private void backtrack() {
        int counter = 0;
        while(counter == 0 && valuesAlreadyAssigned.size() != 0) {
            for(int i = valuesAlreadyAssigned.size() - 1; i >= 0 ; i--) {
                // This is the deduced variables
                Variable variable = valuesAlreadyAssigned.remove(i);
                if(!(lastDecidedVariables.get(lastDecidedVariables.size() - 1) == (valuesAlreadyAssigned.get(i)))) {
                    variablesAssigment.put(variable.getVariableName(), 0);
                    counter = 0;

                }else {
                    lastDecidedVariables.remove(lastDecidedVariables.size() - 1);

                    /* If 'true' has already been selected we now choose 'false'*/
                    if(variablesAssigment.get(variable.getVariableName()) == 1)
                    {
                        if(variable.getVariableValue()) {
                            variable.setVariableValue(false);
                        } else {
                            variable.setVariableValue(true);
                        }

                        valuesAlreadyAssigned.add(variable);
                        variablesAssigment.put(variable.getVariableName(), 2);

                        lastDecidedVariables.add(variable);

                    }
                    /* Both values chosen. Restore working set for backtrack */
                    else
                    {
                        lastDecidedVariables.remove(lastDecidedVariables.size() - 1);
                        variablesAssigment.put(variable.getVariableName(), 0);
                    }
                }
            }

        }


    }

    private boolean checkIfValid() {
        for(Clause c : formula){
            int numberOfUnassignedVariables = 0;
            ArrayList<Variable> tempdisjunc = c.getOrVariables();
            for (Variable v : tempdisjunc) {
                if(decisionLevelAssigned.get(v.getVariableName()) == -1) {
                    numberOfUnassignedVariables++;
                }
            }
            if(numberOfUnassignedVariables == 0) {
              if(!calculateTruthOfClause(tempdisjunc)){
                  //TODO:Can include some kind of heuristic to learn here
                  return true;
              }
            }

        }
        return false;


    }

    // Handles Unit Propogation and also returns if Unit propogation has been done or not
    private int unitpropogation() {
        int value = 0;
        for (Clause c : formula) {
            Pair<Integer, ArrayList<Variable>> unitLiteralAvailable = checkAndHandleUnitLiteral(c);

            if(unitLiteralAvailable.getKey() == 1) {
                Pair<Integer, ArrayList<Variable>> isConflicting = checkConflict(unitLiteralAvailable.getValue(), valuesAlreadyAssigned);
                if(isConflicting.getKey() == 1) {
                    conflictLearn(c, isConflicting.getValue());
                    return value = -1;
                } else {
                    for(Variable v : unitLiteralAvailable.getValue()) {
                        v.incrAssignmentCount();
                        v.modVariableName();
                        valuesAlreadyAssigned.add(v);
                        lastDecidedClause = c;
                    }
                }
                value = 1;
            }
        }
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
            if(decisionLevelAssigned.get((Math.abs(variableNameToDeal)))  != 1) {
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
                v.modVariableName();
                if(!valuesAlreadyAssigned.get(valuesAlreadyAssigned.indexOf(v)).getVariableValue()) {
                    numberOfTrueVariables++;
                }
            } else {
                if(!valuesAlreadyAssigned.get(valuesAlreadyAssigned.indexOf(v)).getVariableValue()) {
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

    private void guessABranchingVariable(ArrayList<Clause> formula ) {
        Random randomClauseGenerator = new Random();
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
        decisionLevelAssigned.put(var.getVariableName(), currentDecisionLevel);
        valuesAlreadyAssigned.add(var);
    }
}
