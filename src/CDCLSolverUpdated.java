import java.lang.reflect.Array;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.Collections;
import java.util.HashMap;
import java.util.HashSet;
import java.util.Random;
import java.util.Set;

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
    HashMap<Integer, Clause> UIPtrack;
    int[] clause2;

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
        this.UIPtrack = new HashMap<Integer, Clause>();
        this.clause2 = new int[totalNumberOfVariables + 1];


    }

    public String solution() {
        initialSetUp(formula); // This is to input all the variables into the decisionLevelAssigned HashMap with variable , -1 value;

        if(unitpropogation().getKey() == -1) {
            return "UNSAT";
        }

//        ArrayList<Variable> test = new ArrayList<Variable>();
//        test.add(new Variable(7, false));
//        test.add(new Variable(8, false));
//        test.add(new Variable(9, false));
//        test.add(new Variable(1, false));
//        test.add(new Variable(2, false));
//        test.add(new Variable(3, false));
//        test.add(new Variable(4, false));
//        test.add(new Variable(5, false));
//        test.add(new Variable(6, false));


//        Clause testc = new Clause(test);
//
//        ArrayList<Variable> test2 = new ArrayList<Variable>();
//        test2.add(new Variable(-2, true));
//        test2.add(new Variable(-3, false));
//        Clause testc2 = new Clause(test2);

//        boolean valeu = testc2.compareClauses(testc);

        currentDecisionLevel = 0;
        int backtracked = 0;

        while(numberOfVariablesAssigned != totalNumberOfVariables) {
            if(backtracked == 0) {
                //guessABranchingVariable(formula);
                //chooseABranchingVariable(test);
                //guess2CBranchingVariable(formula);
                guessAllCBranchingVariable(formula);
            }


            //
            Pair<Integer, Variable> unitPropValue = unitpropogation();
            //System.out.println("Unit prop is over and the unitProp value is " + unitPropValue.getKey() + " because of " + unitPropValue.getValue());
            backtracked = 0;
            if(unitPropValue.getKey() == -1) {
                //Pair<Integer, ArrayList<Variable>> decisionLevelToBackTrack = conflictAnalysis(formula, valuesAlreadyAssigned, implicationGraph, unitPropValue.getValue(), variablesAssignment);
                Pair<Integer, Clause> decisionLevelToBackTrack = conflictAnalysisUIP(formula, valuesAlreadyAssigned, implicationGraph, unitPropValue.getValue(), variablesAssignment);
                if(decisionLevelToBackTrack.getKey() < 0) {
                    return "UNSAT";
                }else {
                    int backtrackingLevel = backtrackUIP(decisionLevelToBackTrack.getKey(), decisionLevelToBackTrack.getValue());
                    //System.out.println("back tracking is done and the size of values assigned is " + valuesAlreadyAssigned.size());
                    //int backtrackingLevel = backtrack(formula, valuesAlreadyAssigned, decisionLevelToBackTrack.getKey(), decisionLevelToBackTrack.getValue());
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

    private int backtrackUIP(Integer key, Clause value) {
////        Variable variable = null;
////        for(Variable temp : value.getOrVariables()) {
////            Variable v = temp.modVariableName();
////            if(decisionLevelAssigned.get(v.getVariableName()) == currentDecisionLevel) {
////                variable = temp;
////            }
////        }
//
//        for(Variable v : variableArrayListClone) {
//            if(decisionLevelAssigned.get(v.getVariableName()) > key) {
//                valuesAlreadyAssigned.remove(v);
//                if(lastDecidedVariables.contains(v)) {
//                    lastDecidedVariables.remove(v);
//                }
//                variablesAssignment.put(v.getVariableName(),0);
//                decisionLevelAssigned.put(v.getVariableName(),-1);
//                numberOfVariablesAssigned--;
//                UIPtrack.put(v.getVariableName(),null);
//                for(int i = 1; i < totalNumberOfVariables; i++) {
//                    implicationGraph[i][v.getVariableName()] = 0;
//                }
//                System.out.println("Variable " + v + " has been removed");
//            } else if (decisionLevelAssigned.get(v.getVariableName()) == key && (lastDecidedVariables.contains(v) || value.getOrVariables().contains(v))){
//                valuesAlreadyAssigned.remove(v);
//                decisionLevelAssigned.put(v.getVariableName(), -1);
//                variablesAssignment.put(v.getVariableName(), 0);
//                if(lastDecidedVariables.contains(v)) {
//                    lastDecidedVariables.remove(v);
//                }
//                numberOfVariablesAssigned--;
//                for(int i = 1; i < totalNumberOfVariables; i++) {
//                    implicationGraph[i][v.getVariableName()] = 0;
//                }
//                UIPtrack.put(v.getVariableName(),null);
//                System.out.println("Variable " + v + " has been removed");
//
//            }
//


//        if(variable.getVariableName() < 0){
//            if(variable.getVariableValue()) {
//                variable.setVariableValue(false);
//            }else {
//                variable.setVariableValue(true);
//            }
//        }
//        variable = variable.modVariableName();
//        System.out.println("Variable " + variable + " has been added from conflict level" +  currentDecisionLevel);
//
//        if(!valuesAlreadyAssigned.contains(variable)) {
//            valuesAlreadyAssigned.add(variable.modVariableName());
//            decisionLevelAssigned.put(variable.modVariableName().getVariableName(), key);
//            variablesAssignment.put(variable.modVariableName().getVariableName(),1);
//            numberOfVariablesAssigned++;
//            UIPtrack.put(variable.modVariableName().getVariableName() , value);
//        }
        ArrayList<Variable> variableArrayListClone = (ArrayList<Variable>) valuesAlreadyAssigned.clone();
        for(Variable v : variableArrayListClone) {
            if(decisionLevelAssigned.get(v.getVariableName()) > key) {
                valuesAlreadyAssigned.remove(v);
                if(lastDecidedVariables.contains(v)) {
                    lastDecidedVariables.remove(v);
                }
                decisionLevelAssigned.put(v.getVariableName(), -1);
                variablesAssignment.put(v.getVariableName(), 0);
                numberOfVariablesAssigned--;
             //   System.out.println("Variable " + v + " has been removed from decision level ");
                for(int i = 1; i < totalNumberOfVariables; i++) {
                    implicationGraph[i][v.getVariableName()] = 0;
                }
            }

        }

//        for(Variable v : variableArrayListClone) {
//            if (decisionLevelAssigned.get(v.getVariableName()) == key) {
//                if(lastDecidedVariables.contains(v)) {
//                    if(variablesAssignment.get(v.getVariableName()) == 1) {
//                        valuesAlreadyAssigned.remove(v);
//                        variablesAssignment.put(v.getVariableName(), 2);
//                        if (v.getVariableValue()) {
//                            v.setVariableValue(false);
//                        } else {
//                            v.setVariableValue(true);
//                        }
//                        valuesAlreadyAssigned.add(v);
//                        System.out.println("Variable " + v + " value has been reversed");
//
//                    } else  if (variablesAssignment.get(v.getVariableName()) == 2) {
//                        int backTrackLevel = backtrackUIP((key - 1), value);
//                        return backTrackLevel;
//                    }
//
//                } else {
//                    valuesAlreadyAssigned.remove(v);
//                    decisionLevelAssigned.put(v.getVariableName(), -1);
//                    variablesAssignment.put(v.getVariableName(), 0);
//                    numberOfVariablesAssigned--;
//                    for(int i = 1; i < totalNumberOfVariables; i++) {
//                        implicationGraph[i][v.getVariableName()] = 0;
//                    }
//                }
//            }
//
//        }
        if(value.getOrVariables().size() == 1) {
            Variable modv = value.getOrVariables().get(0).modVariableName();
            if(valuesAlreadyAssigned.contains(modv) ){
                int decisionLevelofValueVariable = decisionLevelAssigned.get(modv.getVariableName());
                ArrayList<Variable> newVariableClone = (ArrayList<Variable>) valuesAlreadyAssigned.clone();
                for(Variable v : newVariableClone) {
                    if(decisionLevelAssigned.get(v.getVariableName()) >= decisionLevelofValueVariable) {
                        valuesAlreadyAssigned.remove(v);
                        if(lastDecidedVariables.contains(v)) {
                            lastDecidedVariables.remove(v);
                        }
                        decisionLevelAssigned.put(v.getVariableName(), -1);
                        variablesAssignment.put(v.getVariableName(), 0);
                        numberOfVariablesAssigned--;
                    }
                }
            }
        }

        return key;

    }

    private int backtrack(ArrayList<Clause> formula, ArrayList<Variable> valuesAlreadyAssigned, Integer decisionToLevelToBackTrack, ArrayList<Variable> variablesToLearn) {
       // System.out.println("Back Tracking to level" + decisionToLevelToBackTrack);
        ArrayList<Variable> variableArrayListClone = (ArrayList<Variable>) valuesAlreadyAssigned.clone();
        int finalBackTrack = 0;
        if(decisionToLevelToBackTrack == -1 || lastDecidedVariables.size() == 0) {
            finalBackTrack = -1;
            return finalBackTrack;
        }

            if(variablesAssignment.get(lastDecidedVariables.get(lastDecidedVariables.size() - 1).getVariableName()) == 2){
                for(Variable v : variableArrayListClone) {
                    if(decisionLevelAssigned.get(v.getVariableName()) == decisionToLevelToBackTrack) {
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
                      //  System.out.println("Variable " + v + " has been removed");

                    }
                }
                decisionToLevelToBackTrack--;
                int decisionLevelToGoBack = backtrack(formula, valuesAlreadyAssigned, decisionToLevelToBackTrack, variablesToLearn);
                return decisionLevelToGoBack;
            } else if (variablesAssignment.get(lastDecidedVariables.get(lastDecidedVariables.size() - 1).getVariableName()) == 1) {
                for(Variable v : variableArrayListClone) {
                    if(decisionLevelAssigned.get(v.getVariableName()) == decisionToLevelToBackTrack) {
                        valuesAlreadyAssigned.remove(v);
                        if (lastDecidedVariables.contains(v)) {
                            variablesAssignment.put(v.getVariableName(), 2);
                            if (v.getVariableValue()) {
                                v.setVariableValue(false);
                            } else {
                                v.setVariableValue(true);
                            }
                            valuesAlreadyAssigned.add(v);
                        //    System.out.println("Variable " + v + " value has been reversed");
                        } else {
                            variablesAssignment.put(v.getVariableName(), 0);
                            decisionLevelAssigned.put(v.getVariableName(), -1);
                            for (int i = 1; i < totalNumberOfVariables; i++) {
                                implicationGraph[i][v.getVariableName()] = 0;
                            }
                            numberOfVariablesAssigned--;
                         //   System.out.println("Variable " + v + " has been removed");
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

    private Pair<Integer, Clause> conflictAnalysisUIP(ArrayList<Clause> formula, ArrayList<Variable> valuesAlreadyAssigned, int[][] implicationGraph, Variable value, HashMap<Integer, Integer> variablesAssignment) {
       // System.out.println("Conflict Analysis UIP is called");
        Clause c = conflictLearnUIP(implicationGraph, valuesAlreadyAssigned, formula, value, variablesAssignment);
        if(c == null || c.getOrVariables().size() == 0) {
            return new Pair<>(-1, c);
        }
        int decisionLevelToBackTrackUIP = getDecisionLevelTobackTrack(c);
      //  System.out.println("Should backtrack to decision level " + decisionLevelToBackTrackUIP);
        return new Pair<>(decisionLevelToBackTrackUIP, c);

    }

    private int getDecisionLevelTobackTrack(Clause c) {
        ArrayList<Variable> cClone =(ArrayList<Variable>) c.getOrVariables().clone();
        Variable variableInDecisionLevel = null;
        if(c == null) {
            return -1;
        }
        if(c.orVariables.size() == 1) {
            int decisionLevel = decisionLevelAssigned.get(c.getOrVariables().get(0).modVariableName().getVariableName());
            return decisionLevel;


        }
        for(Variable temp : cClone) {
            Variable v = temp.modVariableName();
            if(decisionLevelAssigned.get(v.getVariableName()) == currentDecisionLevel) {
                variableInDecisionLevel = v;
            }
        }
        cClone.remove(variableInDecisionLevel);
        int decisionLevel = findMaxDecisionLevel(cClone);
        return decisionLevel;
    }

    private int findMaxDecisionLevel(ArrayList<Variable> cClone) {
        int index = 0;
        for(Variable temp : cClone) {
         Variable v = temp.modVariableName();
         if(decisionLevelAssigned.get(v.getVariableName()) > index) {
             index = decisionLevelAssigned.get(v.getVariableName());
         }
        }
        return index;
    }

    private Clause conflictLearnUIP(int[][] implicationGraph, ArrayList<Variable> valuesAlreadyAssigned, ArrayList<Clause> formula, Variable value, HashMap<Integer, Integer> variablesAssignment) {
     //   System.out.println("Trying to learn");
        Clause conflictingClause = findConflictingClause(value);
     //   System.out.println("soConflictClause is  " + conflictingClause);
        if(conflictingClause == null) {
            return null;
        }
        Variable lastGuessedVariablethroughUnitProp = getLastGuessedVariableInC(conflictingClause);
     //   System.out.println("lastGuessedVariable of this conflict clause is  " + lastGuessedVariablethroughUnitProp);
        Clause antecedantOfLastGuessedVariable = antecedant(lastGuessedVariablethroughUnitProp);
     //   System.out.println("antecend of the last guessed variable is " + antecedantOfLastGuessedVariable);
        Clause finalClauseToAdd = resolve(conflictingClause, antecedantOfLastGuessedVariable);
     //   System.out.println("Resolving " + conflictingClause +" and antecendant " + antecedantOfLastGuessedVariable+ " to give " + finalClauseToAdd);
        if(conflictingClause == null || finalClauseToAdd == null) {
            boolean ting = finalClauseToAdd.compare(conflictingClause);
//            System.out.println("final Clause " +  finalClauseToAdd);
//            System.out.println("conflicting Clause " +  conflictingClause);
//            System.out.println("print " + ting);
//            System.out.println("This has entered here");
            return null;
        }
        if(finalClauseToAdd.getOrVariables().size() != 0) {
          //  System.out.println("ADDING " +  finalClauseToAdd);
            formula.add(0, finalClauseToAdd);
            totalNumberOfClauses++;
        }

        return finalClauseToAdd;
    }

    private Clause resolve(Clause conflictingClause, Clause antecedant) {
       // System.out.println("So we are resolving " + conflictingClause + " antecedant " +  antecedant);

        if(antecedant == null || conflictingClause == null || terminatingCondition(conflictingClause) || conflictingClause.getOrVariables().size() == 0) {
            return conflictingClause;
        } else {
            Clause intermediateClause = applyResolution(conflictingClause, antecedant);
            intermediateClause.removeDuplicates();
        //    System.out.println("Resolution has been applied. Intermediate Clause is " + intermediateClause);
            Variable lastGuessedVariable = getLastGuessedVariableInC(intermediateClause);
        //    System.out.println("Last guessed variable in this intermediate clause is " + lastGuessedVariable);
            Clause resolvedClause = resolve(intermediateClause, antecedant(lastGuessedVariable));
            return resolvedClause;
        }
    }

    private boolean terminatingCondition(Clause conflictingClause) {
       // System.out.println("Checking for terminating condition of " + conflictingClause);
        ArrayList<Variable> conflictingClauseOrVariables = new ArrayList<>();
        Set<Variable> set = new HashSet<>(conflictingClause.getOrVariables());
        conflictingClauseOrVariables.addAll(set);
        conflictingClause = new Clause(conflictingClauseOrVariables);
        int numberOfVariablesIncurrentDecisionLevel = 0;
        for(Variable temp : conflictingClause.getOrVariables()){
            Variable v = temp.modVariableName();
            if(decisionLevelAssigned.get(v.getVariableName()) == currentDecisionLevel) {
                numberOfVariablesIncurrentDecisionLevel++;
            }
        }

        if(numberOfVariablesIncurrentDecisionLevel == 1) {
           // System.out.println("Terminating Condition is returning true");
            return true;
        } else {
           // System.out.println("Terminating Condition is returning false because number is " + numberOfVariablesIncurrentDecisionLevel);
            return false;
        }
    }

    private Clause applyResolution(Clause conflictingClause, Clause antecedant) {
        ArrayList<Variable> variablesToFormClause = new ArrayList<>();

        ArrayList<Variable> conflictingClauseClone =(ArrayList<Variable>) conflictingClause.getOrVariables().clone();
        ArrayList<Variable> antecedantClauseClone =(ArrayList<Variable>) antecedant.getOrVariables().clone();

        for(Variable v : conflictingClauseClone) {
            for(Variable v1 : antecedantClauseClone) {
                if((v.getVariableName() *  -1) == v1.getVariableName()) {
                    conflictingClause.getOrVariables().remove(v);
                    antecedant.getOrVariables().remove(v1);
                }
            }
        }
        variablesToFormClause.addAll(conflictingClause.getOrVariables());
        variablesToFormClause.addAll(antecedant.getOrVariables());
        Set<Variable> set = new HashSet<>(variablesToFormClause);
        variablesToFormClause.clear();
        variablesToFormClause.addAll(set);
        Clause resolvedClause = new Clause(variablesToFormClause);
        return resolvedClause;
    }

    private ArrayList<Variable> combineClauses(ArrayList<Variable> orVariables, ArrayList<Variable> orVariables1, Variable v) {
        ArrayList<Variable> returningValues = new ArrayList<>();
        orVariables.remove(v);
        orVariables1.remove(v);
        returningValues.addAll(orVariables);
        returningValues.addAll(orVariables1);
        Set<Variable> set = new HashSet<>(returningValues);
        returningValues.clear();
        returningValues.addAll(set);
        return returningValues;

    }

    private Clause antecedant(Variable lastGuessedVariablethroughUnitProp) {
        Variable temp = lastGuessedVariablethroughUnitProp.modVariableName();
        return UIPtrack.get(temp.getVariableName());
    }

    private Variable getLastGuessedVariableInC(Clause conflictingClause) {
        Variable lastGuessedVariablevariable = new Variable(0, false);
        int index = 0;
        for(Variable v : conflictingClause.getOrVariables()){
            int indexOfV = valuesAlreadyAssigned.indexOf(v);
            if(indexOfV > index) {
                lastGuessedVariablevariable = v;
                index = indexOfV;
            }
        }
        return lastGuessedVariablevariable;
    }

    private Clause findConflictingClause(Variable value) {
        ArrayList<Variable> variablesToLearn = new ArrayList<Variable>();
        Variable conflictingVariable = value.modVariableName();
        if(conflictingVariable.getVariableName() != 0) {
            for (int i = 1; i <= totalNumberOfVariables; i++) {
                if (implicationGraph[i][conflictingVariable.getVariableName()] == 1) {
                    Variable v = findVariable(i, valuesAlreadyAssigned);
                    if (v != null) {
                        Variable temp = v.modVariableName();
                        variablesToLearn.add(temp);
                    }

                }
            }
            ArrayList<Variable> clauseToLearn = new ArrayList<Variable>();
            for (Variable v : variablesToLearn) {
                if (v.getVariableValue() == true) {
                    Variable temp = new Variable(v.getVariableName() * -1, true);
                    clauseToLearn.add(temp);
                } else {
                    Variable temp = new Variable(v.getVariableName(), true);
                    clauseToLearn.add(temp);
                }
            }

            Clause newClauseLearnt = new Clause(clauseToLearn);
            return newClauseLearnt;
        }
        return null;
    }


    private Pair<Integer, ArrayList<Variable>> conflictAnalysis(ArrayList<Clause> formula, ArrayList<Variable> valuesAlreadyAssigned, int[][] implicationGraph, Variable value, HashMap<Integer, Integer> variablesAssignment) {
        ArrayList<Variable> variablesToLearn = conflictLearn(formula, implicationGraph,valuesAlreadyAssigned, value,variablesAssignment);
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
                if(implicationGraph[i][conflictingVariable.getVariableName()] == 1) {
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
          //  System.out.println("Clause being added to formula is " +newClauseLearnt);
            formula.add(0,newClauseLearnt);
            totalNumberOfClauses++;
            return variablesToLearn;

        } else {
         //   System.out.println("Clause learnt was empty because it had 0 variables to learn from");
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
                UIPtrack.put(temp.getVariableName(),null);
            }
        }
    }

    private Pair<Integer, Variable> unitpropogation() {
      //  System.out.println("size of valuesAlreadyAssigned " + valuesAlreadyAssigned.size());
        int unitPropDone = 0;
        while (unitPropDone == 0) {
            ArrayList<Variable> addAllVariables = new ArrayList<>();
            for(Clause c : formula) {
                Pair<Integer, Variable> unassignedVariableCount = checkAndHandleClause(c);
                if(unassignedVariableCount.getKey() == 1 && tempUnitPropVariables.size() == 0) {
                    tempUnitPropVariables.add(unassignedVariableCount.getValue());
                    addAllVariables.addAll(c.getOrVariables());
                  //  System.out.println("added " + unassignedVariableCount.getValue() + " and the clause " + c + " to to UIPTrack");
                } else if (unassignedVariableCount.getKey() == 1 && tempUnitPropVariables.size() == 1) {
                    if(tempUnitPropVariables.contains(unassignedVariableCount.getValue())) {
                        int code = checkForConflict(unassignedVariableCount.getValue(),tempUnitPropVariables.get(0));
                        if(code == -1) {
                            addToImplicationGraphConflict(tempUnitPropVariables.get(0));
                            addAllVariables.clear();
                            return new Pair<Integer, Variable>(-1, unassignedVariableCount.getValue());
                        }
                    }
                } else if (unassignedVariableCount.getKey() == 0) {

                    if(!calculateTruthOfClause(c.getOrVariables())) {
                     //   System.out.println("Clause is 2 " + c);
                        return new Pair<Integer, Variable>(-1, unassignedVariableCount.getValue());
                    }
                }
            }
            Clause cdash = new Clause(addAllVariables);
            cdash.removeDuplicates();


            if(cdash.getOrVariables().size() != 0) {
                UIPtrack.put(tempUnitPropVariables.get(0).getVariableName(), cdash);
            }


            if(tempUnitPropVariables.size() > 0) {
                unitPropDone = 0;
                valuesAlreadyAssigned.addAll(tempUnitPropVariables);
                for(Variable v : tempUnitPropVariables) {
                  //  System.out.println("Variable being added by unit prop is" + v);
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

    private void addToImplicationGraphConflict(Variable variable2) {
        for(Clause c : formula) {
            if(c.getOrVariables().contains(variable2)  && (calculateNumberOfUnassigned(c.getOrVariables()) == 1)) {
                for(Variable variable1 : c.getOrVariables()) {
                    Variable variable = variable1.modVariableName();
                    if(!variable.equals(variable2)) {
                        Variable temp = variable.modVariableName();
                        implicationGraph[temp.getVariableName()][variable2.getVariableName()] = 1;
                    }
                }
            }
        }
    }

    private void addToImplicationGraph(Variable v) {
        for(Clause c : formula) {
            if(c.getOrVariables().contains(v) && calculateTruthOfClause(c.getOrVariables()) && (calculateNumberOfUnassigned(c.getOrVariables()) == 0)) {
                for(Variable variable1 : c.getOrVariables()) {
                    Variable variable = variable1.modVariableName();
                    if(!variable.equals(v)) {
                        Variable temp = variable.modVariableName();
                        implicationGraph[temp.getVariableName()][v.getVariableName()] = 1;
                    }
                }
            }
        }
    }

    private int calculateNumberOfUnassigned(ArrayList<Variable> orVariables) {
        int unassigned = 0;
        for(Variable v : orVariables) {
            Variable temp = v.modVariableName();
            if(decisionLevelAssigned.get(temp.getVariableName()) == -1) {
                unassigned++;
            }
        }
        return unassigned;
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

    private void guess2CBranchingVariable(ArrayList<Clause> formula) {
        for(Clause c : formula) {
            if(c.getOrVariables().size() == 2) {
                for(Variable temp: c.getOrVariables()) {
                    Variable v = temp.modVariableName();
                    if(decisionLevelAssigned.get(v.getVariableName()) == -1 && !valuesAlreadyAssigned.contains(v)) {
                        clause2[v.getVariableName()] = clause2[v.getVariableName()] + 1;
                    }
                }
            }
        }

        ArrayList<Integer> numbers = new ArrayList<>();
        int max = 0;
        for(int i = 0 ; i < totalNumberOfVariables + 1; i++) {
            if(clause2[i] > max) {
                max = clause2[i];
            }
        }
        for(int i = 1 ; i < totalNumberOfVariables + 1; i++) {
            if(clause2[i] == max && !valuesAlreadyAssigned.contains(new Variable(i, false))) {
                numbers.add(i);
            }
        }
        Random randomClauseGenerator = new Random();
        int index = randomClauseGenerator.nextInt(numbers.size());
        Variable v = new Variable(numbers.get(index), false);
        decisionLevelAssigned.put(v.getVariableName(), currentDecisionLevel);
        valuesAlreadyAssigned.add(v);
        variablesAssignment.put(v.getVariableName(), 1);
        lastDecidedVariables.add(v);
        numberOfVariablesAssigned++;
        Arrays.fill(clause2, 0);
       // System.out.println("Variable is guessed " + v);

    }

    private void guessAllCBranchingVariable(ArrayList<Clause> formula) {
        for(Clause c : formula) {

                for(Variable temp: c.getOrVariables()) {
                    Variable v = temp.modVariableName();
                    if(decisionLevelAssigned.get(v.getVariableName()) == -1 && !valuesAlreadyAssigned.contains(v)) {
                        clause2[v.getVariableName()] = clause2[v.getVariableName()] + 1;
                    }
                }

        }

        ArrayList<Integer> numbers = new ArrayList<>();
        int max = 0;
        for(int i = 0 ; i < totalNumberOfVariables + 1; i++) {
            if(clause2[i] > max) {
                max = clause2[i];
            }
        }
        for(int i = 1 ; i < totalNumberOfVariables + 1; i++) {
            if(clause2[i] == max && !valuesAlreadyAssigned.contains(new Variable(i, false))) {
                numbers.add(i);
            }
        }
        Random randomClauseGenerator = new Random();
        int index = randomClauseGenerator.nextInt(numbers.size());
        Variable v = new Variable(numbers.get(index), false);
        decisionLevelAssigned.put(v.getVariableName(), currentDecisionLevel);
        valuesAlreadyAssigned.add(v);
        variablesAssignment.put(v.getVariableName(), 1);
        lastDecidedVariables.add(v);
        numberOfVariablesAssigned++;
        Arrays.fill(clause2, 0);
        // System.out.println("Variable is guessed " + v);

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
   //     System.out.println(temp + " with decision level " + currentDecisionLevel);
        decisionLevelAssigned.put(temp.getVariableName(), currentDecisionLevel);
        valuesAlreadyAssigned.add(temp);
        variablesAssignment.put(temp.getVariableName(), 1);
        lastDecidedVariables.add(temp);
        return 1;
    }
}