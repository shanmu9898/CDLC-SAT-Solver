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
    ArrayList<Variable> unitPropogationVariables;
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
        this.unitPropogationVariables = new ArrayList<Variable>();
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
                    int code = backtrack(formula, valuesAlreadyAssigned, decisionLevelToBackTrack.getKey(), decisionLevelToBackTrack.getValue());
                    if(code == 1) {
                        backtracked = 1;
                        currentDecisionLevel = decisionLevelToBackTrack.getKey();
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

    private void printArrayList(ArrayList<Variable> valuesAlreadyAssigned) {

        System.out.println("Final Values are");
        for(Variable v : valuesAlreadyAssigned) {
            System.out.println(v);
        }

    }

    private Pair<Integer, ArrayList<Variable>> conflictAnalysis(ArrayList<Clause> formula, ArrayList<Variable> valuesAlreadyAssigned, int[][] implicationGraph, Variable value, HashMap<Integer, Integer> variablesAssignment) {
        ArrayList<Variable> variablesToLearn = conflictLearn(formula, implicationGraph,valuesAlreadyAssigned, value,variablesAssignment);
        int decisionLevelToBackTrack = 0;
        if(variablesToLearn.size() == 0) {
            if(lastDecidedVariables.size() > 0 && variablesAssignment.get(lastDecidedVariables.get(lastDecidedVariables.size() -1).modVariableName().getVariableName()) == 1){
                decisionLevelToBackTrack = currentDecisionLevel;
            } else {
                decisionLevelToBackTrack = currentDecisionLevel - 1;
            }

        } else {
            decisionLevelToBackTrack = findDecisionLevelToBackTrack(variablesToLearn);
        }

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


    private ArrayList<Variable> conflictLearn(ArrayList<Clause> formula, int[][] implicationGraph, ArrayList<Variable> valuesAlreadyAssigned, Variable conflictingVariable, HashMap<Integer, Integer> variablesAssignment) {
        ArrayList<Variable> variablesToLearn = new ArrayList<Variable>();
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

           formula.add(newClauseLearnt);
           System.out.println("Clause learned is " + newClauseLearnt);
           totalNumberOfClauses++;
           return variablesToLearn;
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

    private int backtrack(ArrayList<Clause> formula, ArrayList<Variable> valuesAlreadyAssigned, int decisionLevelToBackTrack, ArrayList<Variable> variablesToLearn) {
        System.out.println("back track to " + decisionLevelToBackTrack +" has started here");
        int code = 0;
        if(decisionLevelToBackTrack < 0) {
            code = 2;
            return code;
        }
        ArrayList<Variable> valuesAlreadyAssignedClone = (ArrayList<Variable>) valuesAlreadyAssigned.clone();
        System.out.println("size of values already assigned clone" + valuesAlreadyAssignedClone.size());
        for(Variable v : valuesAlreadyAssignedClone) {
            if(decisionLevelAssigned.get(v.getVariableName()) > decisionLevelToBackTrack) {
                currentDecisionLevel = decisionLevelToBackTrack;
                //edit
                //if(lastDecidedVariables.contains(v) && variablesToLearn.contains(v))
                if(variablesToLearn.contains(v)) {
                    variablesAssignment.put(v.getVariableName(), 1);
                    decisionLevelAssigned.put(v.getVariableName(), decisionLevelToBackTrack);

                    Variable temp = v.modVariableName();
                    if(temp.getVariableValue() == true) {
                        temp.setVariableValue(false);
                    } else {
                        temp.setVariableValue(true);
                    }
                    valuesAlreadyAssigned.remove(v);
                    valuesAlreadyAssigned.add(temp);
                    if(lastDecidedVariables.contains(v)) {
                        lastDecidedVariables.remove(v);
                    }
                    for(int i =0; i <= totalNumberOfVariables; i++) {
                        implicationGraph[i][v.getVariableName()] = 0;
                    }
                    code = 1;
                    System.out.println(v + " has been removed, guessed variable, higher decision level than needed");
                } else {
                    variablesAssignment.put(v.getVariableName(), 0);
                    decisionLevelAssigned.put(v.getVariableName(), -1);
                    valuesAlreadyAssigned.remove(v);
                    lastDecidedVariables.remove(v);
                    numberOfVariablesAssigned--;
                    System.out.println(v + " has been removed, non guessed variable, higher decision level than needed");
                    for(int i =0; i <= totalNumberOfVariables; i++) {
                        implicationGraph[i][v.getVariableName()] = 0;
                    }
                    code = 1;
                    for(int i = 1; i <= totalNumberOfVariables; i++) {
                        implicationGraph[i][v.getVariableName()] = 0;
                    }
                }

            } else if (decisionLevelAssigned.get(v.getVariableName()) == decisionLevelToBackTrack) {
                if(decisionLevelToBackTrack > 0) {
                    currentDecisionLevel = decisionLevelToBackTrack;
                }
                //edit
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
                        System.out.println(v + " value changed to opposite value");
                    }else if (variablesAssignment.get(v.getVariableName()) == 2) {
                        System.out.println("backtrack to previous level cauz both values have been exhausted");
                        code = backtrack(formula,valuesAlreadyAssigned,decisionLevelToBackTrack - 1, variablesToLearn);
                        return code;

                    }
                } else {
                    variablesAssignment.put(v.getVariableName(), 0);
                    decisionLevelAssigned.put(v.getVariableName(), -1);
                    valuesAlreadyAssigned.remove(v);
                    lastDecidedVariables.remove(v);
                    System.out.println(v + " has been removed, same decision level, deduced variable");
                    numberOfVariablesAssigned--;
                    for(int i =0; i <= totalNumberOfVariables; i++) {
                        implicationGraph[i][v.getVariableName()] = 0;
                    }
                    code = 1;
                    for(int i = 1; i <= totalNumberOfVariables; i++) {
                        implicationGraph[i][v.getVariableName()] = 0;
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
//    private Pair<Integer, Variable> unitpropogation() {
//        Pair<Integer, Variable> value = new Pair<>(0, new Variable(0,false));
//        for (Clause c : formula) {
//            Pair<Integer, ArrayList<Variable>> unitLiteralAvailable = checkAndHandleUnitLiteral(c);
//            if(unitLiteralAvailable.getKey() == 1) {
//                Pair<Integer, ArrayList<Variable>> isConflicting = checkConflict(unitLiteralAvailable.getValue(), valuesAlreadyAssigned);
//                if(isConflicting.getKey() == 1) {
//                    System.out.println("It is conflicting because of " + c);
//                    value = new Pair<>(-1, isConflicting.getValue().get(0));
//                    return value;
//                } else {
//                    for(Variable v : unitLiteralAvailable.getValue()) {
//                        Variable temp = v.modVariableName();
//                        System.out.println("Through unit prop " + temp + " has been added and from clause " + c);
//                        valuesAlreadyAssigned.add(temp);
//                        unitPropogationVariables.add(temp);
//                        decisionLevelAssigned.put(temp.getVariableName(), currentDecisionLevel);
//                        numberOfVariablesAssigned++;
//                    }
//
//                }
//                value = new Pair<>(1, new Variable(0,false));
//            } else if (unitLiteralAvailable.getKey() == -1) {
//                System.out.println("It is conflicting because of " + c);
//                value = new Pair<>(-1, unitPropogationVariables.get(unitPropogationVariables.size() - 1));
//                return value;
//            }
//        }
//        if(value.getKey() == 1) {
//            Pair<Integer,Variable> returnUnitProp = unitpropogation();
//            if(returnUnitProp.getKey() == -1) {
//                System.out.println("recursive unit prop conflict ");
//                value = new Pair<>(-1, unitPropogationVariables.get(unitPropogationVariables.size() - 1));
//                return value;
//            }
//        }
//
//        return value;
//    }

    private Pair<Integer, Variable> unitpropogation() {
        int UnitPropDone = 0;
        while(UnitPropDone == 0) {
            for(Clause c : formula) {
                int unAssignedVariablesCount = checkNumberOfUnassigned(c);
                if(unAssignedVariablesCount ==  1) {
                    boolean truthValueOfClause = calculateTruthOfClause(c.getOrVariables());
                    if(!truthValueOfClause) {
                        Pair<Integer, Variable> handledOrNot = handleUnAssginedVariable(c);
                        if(handledOrNot.getKey() == -1) {
                            return new Pair<>(-1, handledOrNot.getValue());
                        }
                    }
                } else if (unAssignedVariablesCount == 0) {
                    boolean truthValueOfClause = calculateTruthOfClause(c.getOrVariables());
                    if(!truthValueOfClause){
                        return new Pair<>(-1, new Variable(0, false));
                    }
                }
            }
            if(tempUnitPropVariables.size() > 0) {
                System.out.println("Values propogated through Unit Prop are after all variables have been run are " + printVector(tempUnitPropVariables));
                UnitPropDone = 0;
                valuesAlreadyAssigned.addAll(tempUnitPropVariables);
                for(Variable v : tempUnitPropVariables) {
                    decisionLevelAssigned.put(v.getVariableName(), currentDecisionLevel);
                    numberOfVariablesAssigned++;
                    variablesAssignment.put(v.getVariableName(), 1);
                }
                tempUnitPropVariables.clear();
            } else {
                System.out.println("Unit Propogation exiting");
                UnitPropDone = 1;
            }
        }
        return new Pair<>(0, new Variable(0, false));



    }

    private String printVector(ArrayList<Variable> vector) {
        String str = "";
        for(Variable v : vector) {
            str = str + " " + v.getVariableName();
        }

        return str;
    }


    private Pair<Integer, Variable> handleUnAssginedVariable(Clause c) {
        Variable unassignedVariable = new Variable(0,false);
        for(Variable v : c.getOrVariables()) {
            Variable temp = v.modVariableName();
            if(decisionLevelAssigned.get(temp.getVariableName()) ==  -1) {
                unassignedVariable = v;
            }
        }

        Variable temp = unassignedVariable.modVariableName();
        if(tempUnitPropVariables.contains(temp)) {
            int index = tempUnitPropVariables.indexOf(temp);
            Variable v = tempUnitPropVariables.get(index);
            if(v.getVariableValue() != unassignedVariable.getVariableValue()) {
               for(Variable vdash : c.getOrVariables()) {
                    if(vdash.modVariableName() != temp) {
                        implicationGraph[vdash.modVariableName().getVariableName()][temp.getVariableName()] = 1;
                    }
                }
                System.out.println("Conflict is here because of " + v.getVariableName());
                return new Pair<>(-1, v);
            }
        } else {
            if(unassignedVariable.getVariableName() < 0) {
                temp.setVariableValue(false);
                tempUnitPropVariables.add(temp);
                System.out.println("The clause used to derive " + temp +" is "+ c);
                for(Variable vdash : c.getOrVariables()) {
                    if(vdash.modVariableName() != temp) {
                        implicationGraph[vdash.modVariableName().getVariableName()][temp.getVariableName()] = 1;
                    }
                }


            } else {
                temp.setVariableValue(true);
                tempUnitPropVariables.add(temp);
                System.out.println("The clause used to derive " + temp +" is "+ c);
                for(Variable vdash : c.getOrVariables()) {
                    if(vdash.modVariableName() != temp) {
                        implicationGraph[vdash.modVariableName().getVariableName()][temp.getVariableName()] = 1;
                    }
                }
            }
        }
        return new Pair<>(1, new Variable(0, false));

    }

    private int checkNumberOfUnassigned(Clause c) {
        int unassignedVariable = 0;
        for(Variable v : c.getOrVariables()) {
            Variable temp = v.modVariableName();
            if(decisionLevelAssigned.get(temp.getVariableName()) ==  -1) {
                unassignedVariable++;
            }
        }
        return unassignedVariable;
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
        Pair<Integer, ArrayList<Variable>> returnValues = new Pair<Integer, ArrayList<Variable>>(0, new ArrayList<Variable>());;
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
            ArrayList<Variable> variablesAssignedFromUnitProp = new ArrayList<Variable>();
            if(numberOfUnassignedVariables == 1) {
                variablesAssignedFromUnitProp = checkAndInputValueForVariable(unassignedVariable, c);
                if(variablesAssignedFromUnitProp.size() != 0) {
                    returnValues = new Pair<Integer, ArrayList<Variable>>(1, variablesAssignedFromUnitProp);
                    for(Variable v : tempdisjunc) {
                        Variable temp = v.modVariableName();
                        implicationGraph[temp.getVariableName()][variablesAssignedFromUnitProp.get(0).getVariableName()] = 1;
                    }
                } else {
                    returnValues = new Pair<Integer, ArrayList<Variable>>(0, new ArrayList<Variable>());
                }


            } else if (numberOfUnassignedVariables == 0) {
                boolean value = calculateTruthOfClause(c.getOrVariables());
                if(!value) {
                    returnValues = new Pair<Integer, ArrayList<Variable>>(-1, variablesAssignedFromUnitProp);
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