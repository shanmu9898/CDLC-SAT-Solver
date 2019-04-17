import java.lang.reflect.Array;
import java.util.ArrayList;
import java.util.Collections;
import java.util.HashMap;
import java.util.Random;

import com.sun.tools.doclets.formats.html.SourceToHTMLConverter;

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
    int[][] implicationGraph;
    int number = 0;
    ArrayList<Variable> tempUnitPropVariables;

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
        this.implicationGraph = new int[totalNumberOfVariables + 1][totalNumberOfVariables + 1];
        this.tempUnitPropVariables = new ArrayList<Variable>();


    }

    public String solution() {
        initialSetUp(formula); // This is to input all the variables into the decisionLevelAssigned HashMap with variable , -1 value;

        if(unitpropogation().getKey() == -1) {
            return "UNSAT";
        }

        ArrayList<Variable> test = new ArrayList<Variable>();
        test.add(new Variable(10, true));
        test.add(new Variable(19, true));
        test.add(new Variable(18, true));

        currentDecisionLevel = 0;
        int backtracked = 0;

        while(numberOfVariablesAssigned != totalNumberOfVariables) {
            if(backtracked == 0) {
                guessABranchingVariable(formula);
            }


            //chooseABranchingVariable(test);
            Pair<Integer, Variable> unitPropValue = unitpropogation();
            backtracked = 0;
            if(unitPropValue.getKey() == -1) {
                Pair<Integer, ArrayList<Variable>> decisionLevelToBackTrack = conflictAnalysis(formula, valuesAlreadyAssigned, implicationGraph, unitPropValue.getValue(), variablesAssignment);
                if(decisionLevelToBackTrack.getKey() < 0) {
                    return "UNSAT";
                } else {
                    int backtrackingLevel = backtrack(formula, valuesAlreadyAssigned, decisionLevelToBackTrack.getKey(), decisionLevelToBackTrack.getValue());
                    tempUnitPropVariables.clear();
                    if(backtrackingLevel > -1) {
                        backtracked = 1;
                        currentDecisionLevel = backtrackingLevel;
                    } else {
                        return "UNSAT";
                    }

                }
            }

            if(backtracked == 0) {
                currentDecisionLevel++;
            }

        }
        //printArrayList(valuesAlreadyAssigned);
        return "SAT";


    }

    private int backtrack(ArrayList<Clause> formula, ArrayList<Variable> valuesAlreadyAssigned, Integer decisionToLevelToBackTrack, ArrayList<Variable> variablesToLearn) {
        System.out.println("Back Tracking to level" + decisionToLevelToBackTrack);
        ArrayList<Variable> variableArrayListClone = (ArrayList<Variable>) valuesAlreadyAssigned.clone();
        int finalBackTrack = 0;
        if(decisionToLevelToBackTrack == -1 || lastDecidedVariables.size() == 0) {
            finalBackTrack = -1;
            return finalBackTrack;
        }
        if(variablesAssignment.get(lastDecidedVariables.get(lastDecidedVariables.size() - 1).getVariableName()) == 2) {

            finalBackTrack = backtrack(formula, valuesAlreadyAssigned, decisionToLevelToBackTrack - 1, variablesToLearn);
            return finalBackTrack;
        } else if (variablesAssignment.get(lastDecidedVariables.get(lastDecidedVariables.size() - 1).getVariableName()) == 1) {
            for(Variable v : variableArrayListClone) {
                if(decisionLevelAssigned.get(v.getVariableName()).equals(decisionToLevelToBackTrack)){
                    valuesAlreadyAssigned.remove(v);
                    if(lastDecidedVariables.contains(v)) {
                        lastDecidedVariables.remove(v);
                        variablesAssignment.put(v.getVariableName(), 2);
                        if(v.getVariableValue()) {
                            v.setVariableValue(false);
                        }else{
                            v.setVariableValue(true);
                        }
                        valuesAlreadyAssigned.add(v);
                    } else {
                        variablesAssignment.put(v.getVariableName(), 0);
                        decisionLevelAssigned.put(v.getVariableName(), -1);
                        for(int i = 1; i < totalNumberOfVariables; i++) {
                            implicationGraph[i][v.getVariableName()] = 0;
                        }
                        numberOfVariablesAssigned--;
                    }

                } else if (decisionLevelAssigned.get(v.getVariableName()) >  decisionToLevelToBackTrack) {
                    valuesAlreadyAssigned.remove(v);
                    if(lastDecidedVariables.contains(v)) {
                        lastDecidedVariables.remove(v);
                    }
                    variablesAssignment.put(v.getVariableName(), 0);
                    decisionLevelAssigned.put(v.getVariableName(), -1);
                    numberOfVariablesAssigned--;
                    for(int i = 1; i < totalNumberOfVariables; i++) {
                        implicationGraph[i][v.getVariableName()] = 0;
                    }
                }
            }
        }
        return decisionToLevelToBackTrack;

    }

    private void printArrayList(ArrayList<Variable> valuesAlreadyAssigned) {

        System.out.println("Final Values are");
        for(Variable v : valuesAlreadyAssigned) {
            System.out.println(v);
        }

    }

    private Pair<Integer, ArrayList<Variable>> conflictAnalysis(ArrayList<Clause> formula, ArrayList<Variable> valuesAlreadyAssigned, int[][] implicationGraph, Variable value, HashMap<Integer, Integer> variablesAssignment) {
        ArrayList<Variable> variablesToLearn = conflictLearn(formula, implicationGraph,valuesAlreadyAssigned, value,variablesAssignment);
        //        int decisionLevelToBackTrack = 0;
        //        if(variablesToLearn.size() == 0) {
        //                decisionLevelToBackTrack = currentDecisionLevel;
        //        } else {
        //            decisionLevelToBackTrack = findDecisionLevelToBackTrack(variablesToLearn);
        //        }
        int decisionLevelToBackTrack = currentDecisionLevel;
        return new Pair<>(decisionLevelToBackTrack, variablesToLearn);
    }

    private int findDecisionLevelToBackTrack(ArrayList<Variable> variablesToLearn) {
        int lowestDecisionLevel = Integer.MAX_VALUE;
        for(Variable v : variablesToLearn) {
            if(decisionLevelAssigned.get(v.getVariableName()) < lowestDecisionLevel) {
                lowestDecisionLevel = decisionLevelAssigned.get(v.getVariableName());
            }
        }
        return lowestDecisionLevel;
    }


    private ArrayList<Variable> conflictLearn(ArrayList<Clause> formula, int[][] implicationGraph, ArrayList<Variable> valuesAlreadyAssigned, Variable conflictingVariableTemp, HashMap<Integer, Integer> variablesAssignment) {
        ArrayList<Variable> variablesToLearn = new ArrayList<Variable>();
        Variable conflictingVariable = conflictingVariableTemp.modVariableName();
        if(conflictingVariable.getVariableName() != 0) {
            for(int i = 1; i <= totalNumberOfVariables; i++) {
                if(implicationGraph[i][conflictingVariable.getVariableName()] == 1 && i != conflictingVariable.getVariableName()) {
                    Variable v = findVariable(i, valuesAlreadyAssigned);
                    if(v != null) {
                        Variable temp = v.modVariableName();
                        variablesToLearn.add(temp);
                    }

                }
            }

            ArrayList<Variable> clauseToLearn = new ArrayList<Variable>();
            for(Variable v : variablesToLearn) {
                if(v.getVariableValue() == true) {
                    Variable temp = new Variable(v.getVariableName() * -1 , true);
                    clauseToLearn.add(temp);
                } else {
                    Variable temp = new Variable(v.getVariableName(), true);
                    clauseToLearn.add(temp);
                }
            }

            Clause newClauseLearnt = new Clause(clauseToLearn);

            if(!formula.contains(newClauseLearnt) && newClauseLearnt.getOrVariables().size() != 0){
                System.out.println("Clause not present and learned is " + newClauseLearnt);
                formula.add(0,newClauseLearnt);
                totalNumberOfClauses++;
                return variablesToLearn;
            } else {
                System.out.println("old formula is being learnt");
                return new ArrayList<Variable>();
            }

        } else {
            System.out.println("Clause learnt was empty because it had 0 variables to learn from");
            return variablesToLearn;
        }



    }

    private Variable findVariable(int variableName, ArrayList<Variable> valuesAlreadyAssigned){

        for(Variable v : valuesAlreadyAssigned) {
            if(v.getVariableName() == variableName) {
                return v;
            }
        }
        return null;

    }

    private void  initialSetUp(ArrayList<Clause> formula) {
        for(Clause c: formula) {
            for (int i=0; i<c.getOrVariables().size(); i++) {
                Variable current = c.getOrVariables().get(i);
                Variable temp = current.modVariableName();
                decisionLevelAssigned.put(temp.getVariableName(), -1);
                variablesAssignment.put(temp.getVariableName(), 0);
            }
        }
    }

    private Pair<Integer, Variable> unitpropogation() {
        int unitPropDone = 0;
        while (unitPropDone == 0) {
            for(Clause c : formula) {
                Pair<Integer, Variable> unassignedVariableCount = checkAndHandleClause(c);
                if(unassignedVariableCount.getKey() == 1 && tempUnitPropVariables.size() == 0) {
                    tempUnitPropVariables.add(unassignedVariableCount.getValue());
                } else if (unassignedVariableCount.getKey() == 1 && tempUnitPropVariables.size() == 1) {
                    if(tempUnitPropVariables.contains(unassignedVariableCount.getValue())) {
                        int code = checkForConflict(unassignedVariableCount.getValue(),tempUnitPropVariables.get(0));
                        if(code == -1) {
                            return new Pair<Integer, Variable>(-1, unassignedVariableCount.getValue());
                        }
                    }
                } else if (unassignedVariableCount.getKey() == 0) {
                    if(!calculateTruthOfClause(c.getOrVariables())) {
                        return new Pair<Integer, Variable>(-1, unassignedVariableCount.getValue());
                    }
                }
            }

            if(tempUnitPropVariables.size() > 0) {
                unitPropDone = 0;
                valuesAlreadyAssigned.addAll(tempUnitPropVariables);
                for(Variable v : tempUnitPropVariables) {
                    System.out.println("Variable being added by unit prop is" + v);
                    decisionLevelAssigned.put(v.getVariableName(), currentDecisionLevel);
                    numberOfVariablesAssigned++;
                    variablesAssignment.put(v.getVariableName(), 1);
                    addToImplicationGraph(v);
                }

                tempUnitPropVariables.clear();
            } else {
                tempUnitPropVariables.clear();
                unitPropDone = 1;
            }

        }
        return new Pair<>(0, new Variable(0, false));
    }

    private void addToImplicationGraph(Variable v) {
        for(Clause c : formula) {
            if(c.getOrVariables().contains(v)) {
                for(Variable variable : c.getOrVariables()) {
                    if(variable != v) {
                        Variable temp = variable.modVariableName();
                        implicationGraph[temp.getVariableName()][v.getVariableName()] = 1;
                    }
                }
            }
        }
    }

    private int checkForConflict(Variable value, Variable variable) {
        int code = 0;
        if(value.getVariableValue() != variable.getVariableValue()) {
            code = -1;
        }
        return code;
    }

    private Pair<Integer,Variable> checkAndHandleClause(Clause c) {
        int numberUnassigned = 0;
        Variable unassignedVariable = new Variable(0, false);
        if(!calculateTruthOfClause(c.getOrVariables())) {
            for(Variable v : c.getOrVariables()) {
                Variable temp = v.modVariableName();
                if(decisionLevelAssigned.get(temp.getVariableName()) == -1) {
                    numberUnassigned++;
                    unassignedVariable = v;
                }
            }
            if(numberUnassigned == 1) {
                if(unassignedVariable.getVariableName() < 0) {
                    unassignedVariable.setVariableValue(false);
                } else if (unassignedVariable.getVariableName() > 0) {
                    unassignedVariable.setVariableValue(true);
                }
            }
            Variable temp = unassignedVariable.modVariableName();
            return new Pair<>(numberUnassigned, temp);
        } else {
            return new Pair<>(-1, new Variable(0, false));
        }
    }



    private String printVector(ArrayList<Variable> vector) {
        String str = "";
        for(Variable v : vector) {
            str = str + " " + v.getVariableName();
        }

        return str;
    }


    //Calculate if the clause is true or false until now based on which the unassigned value can be identified.
    private boolean calculateTruthOfClause(ArrayList<Variable> tempdisjunc) {
        int numberOfTrueVariables = 0;
        if(tempdisjunc.size() == 0) {
            return false;
        }
        for(Variable v : tempdisjunc) {
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
                    if (valuesAlreadyAssigned.get(valuesAlreadyAssigned.indexOf(temp)).getVariableValue()) {
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
        int value = pickRandomVariable(c);
        while (value == 0) {
            index = randomClauseGenerator.nextInt(formula.size());
            c = formula.get(index);
            value = pickRandomVariable(c);
        }
    }

    private void chooseABranchingVariable(ArrayList<Variable> variables) {
        Variable v = variables.get(number);
        numberOfVariablesAssigned++;
        number++;
        Variable temp = v.modVariableName();
        decisionLevelAssigned.put(temp.getVariableName(), currentDecisionLevel);
        valuesAlreadyAssigned.add(temp);
        variablesAssignment.put(temp.getVariableName(), 1);
        lastDecidedVariables.add(temp);
        System.out.println(temp);
    }


    private int pickChosenVariable(int name, boolean value) {
        Variable v = new Variable(name, value);
        numberOfVariablesAssigned++;
        Variable temp = v.modVariableName();
        // System.out.println(temp);
        decisionLevelAssigned.put(temp.getVariableName(), currentDecisionLevel);
        valuesAlreadyAssigned.add(temp);
        variablesAssignment.put(temp.getVariableName(), 1);
        lastDecidedVariables.add(temp);
        return 1;

    }

    private int pickRandomVariable(Clause c) {
        c.getOrVariables();
        Random randomVariableGenerator = new Random();
        int index = randomVariableGenerator.nextInt(c.getOrVariables().size());
        Variable var = c.getOrVariables().get(index);
        Variable cloneVar = var.modVariableName();
        int number = 0;
        while (valuesAlreadyAssigned.contains(cloneVar)) {
            index = randomVariableGenerator.nextInt(c.getOrVariables().size());
            var = c.getOrVariables().get(index);
            cloneVar = var.modVariableName();
            // System.out.println("In a loop");
            number++;
            if (number > 5) {
                return 0;
            }
        }
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
        System.out.println(temp + " with decision level " + currentDecisionLevel);
        decisionLevelAssigned.put(temp.getVariableName(), currentDecisionLevel);
        valuesAlreadyAssigned.add(temp);
        variablesAssignment.put(temp.getVariableName(), 1);
        lastDecidedVariables.add(temp);
        return 1;
    }
}