import java.io.BufferedWriter;
import java.io.File;
import java.io.FileOutputStream;
import java.io.IOException;
import java.io.OutputStreamWriter;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.HashMap;
import java.util.HashSet;
import java.util.Map;
import java.util.Random;
import java.util.Set;

import org.apache.commons.math3.util.Pair;


public class CDCLSolverUpdated {

    private HashMap<Integer, Integer> decisionLevelAssigned;         // keeps track of which variable has been assigned at which decision level
    private ArrayList<Variable> valuesAlreadyAssigned;               // Keeps track of all the variables assigned and the values of those variables
    private int numberOfVariablesAssigned;                           // Total number of variables assigned as of the instant
    private int totalNumberOfClauses;                                // Total number of clauses in the formula
    private int totalNumberOfVariables;                              // Total numbe of variables
    private ArrayList<Clause> formula;                               // The actual formula
    private int currentDecisionLevel;                                // Current decision level tracker
    private ArrayList<Variable> lastDecidedVariables;                // Keeps track of all the variables guessed
    private HashMap<Integer, Integer> variablesAssignment;           // Keeps track of how many times a variable has been assigned a value
    private int[][] implicationGraph;                                // Implication graph representation
    private int number = 0;                                          // Tracker to keep track of variables when guessing randomly so that same variables are not guessed again.
    private ArrayList<Variable> tempUnitPropVariables;               // Tracker to keep track of the Variables which have been assigned solely by unit propogation.
    private HashMap<Integer, Clause> UIPtrack;                       // Keeps track of the antecedants of a variable.
    private int[] clause2;                                           // Used for branching heuristics
    int numberVariables = 0;
    ArrayList<Clause> unsatProof;
    HashMap<Integer, Integer> variableToCodeMapping;

    // Constructor
    public CDCLSolverUpdated(int totalNumberOfClauses, int totalNumberOfVariables, ArrayList<Clause> formula) {
        this.totalNumberOfClauses = totalNumberOfClauses;
        this.totalNumberOfVariables = totalNumberOfVariables;
        this.variableToCodeMapping = new HashMap<>();
        this.formula = convertRawFormula(formula);
        this.numberOfVariablesAssigned = 0;
        this.decisionLevelAssigned = new HashMap<Integer, Integer>();
        this.currentDecisionLevel = 0;
        this.valuesAlreadyAssigned = new ArrayList<Variable>();
        this.variablesAssignment = new HashMap<Integer, Integer>();
        this.lastDecidedVariables = new ArrayList<Variable>();
        this.implicationGraph = new int[totalNumberOfVariables + 1][totalNumberOfVariables + 1];
        this.tempUnitPropVariables = new ArrayList<Variable>();
        this.UIPtrack = new HashMap<Integer, Clause>();
        this.clause2 = new int[totalNumberOfVariables + 1];
        this.unsatProof = new ArrayList<>();
    }

    public ArrayList<Clause> convertRawFormula(ArrayList<Clause> fomula) {
        int i = 1;
        for(Clause c : fomula) {
            for(Variable v : c.getOrVariables()){
                Variable vdash = v.modVariableName();
                if(!variableToCodeMapping.containsKey(vdash.getVariableName())) {
                    variableToCodeMapping.put(vdash.getVariableName(), i);
                    i++;
                }
            }
        }

        ArrayList<Clause> formulaUpdated = new ArrayList<>();
        for(Clause c : fomula) {
            ArrayList<Variable> clauseUpdated = new ArrayList<>();
            for(Variable v : c.getOrVariables()) {
                Variable vdash = v.modVariableName();
                if(v.getVariableName() < 0) {
                    clauseUpdated.add(new Variable((variableToCodeMapping.get(vdash.getVariableName()) * -1), v.variableValue));
                } else {
                    clauseUpdated.add(new Variable(variableToCodeMapping.get(vdash.getVariableName()), v.variableValue));
                }
            }
            Clause newClause = new Clause(clauseUpdated);
            formulaUpdated.add(newClause);
        }
        return formulaUpdated;
    }

    // Actual algorithm of CDCL. Calls the necessary helper functions
    public String solution(String branchingHeursitics, String conflictAnalysisHeuristics, boolean proofNeeded) throws IOException {
        initialSetUp(formula); // This is to input all the variables into the decisionLevelAssigned HashMap with variable , -1 value;



        if(unitpropogation().getKey() == -1) {
            System.out.println("size is " + unsatProof.size());
            return "UNSAT";
        }


        currentDecisionLevel = 0;
        int backtracked = 0;

        while(numberOfVariablesAssigned != totalNumberOfVariables) {
            if(backtracked == 0) {
                if(branchingHeursitics.equals("2C")) {
                    guess2CBranchingVariable(formula);
                } else if (branchingHeursitics.equals("AllC")) {
                    guessAllCBranchingVariable(formula);
                } else if (branchingHeursitics.equals("Random")) {
                    guessABranchingVariable(formula);
                }
            }


            Pair<Integer, Variable> unitPropValue = unitpropogation();
            backtracked = 0;
            if(unitPropValue.getKey() == -1) {
                if (conflictAnalysisHeuristics.equals("GRASP")) {
                    Pair<Integer, ArrayList<Variable>> decisionLevelToBackTrack = conflictAnalysis(formula, valuesAlreadyAssigned, implicationGraph, unitPropValue.getValue(), variablesAssignment);
                    if (decisionLevelToBackTrack.getKey() < 0) {
                        return "UNSAT";
                    } else {
                        int backtrackingLevel = backtrack(formula, valuesAlreadyAssigned, decisionLevelToBackTrack.getKey(), decisionLevelToBackTrack.getValue());
                        tempUnitPropVariables.clear();
                        if (backtrackingLevel > -1) {
                            backtracked = 1;
                            currentDecisionLevel = backtrackingLevel;
                        } else {
                            return "UNSAT";
                        }

                    }
                } else if (conflictAnalysisHeuristics.equals("1UIP")) {
                    Pair<Integer, Clause> decisionLevelToBackTrack = conflictAnalysisUIP(formula, valuesAlreadyAssigned, implicationGraph, unitPropValue.getValue(), variablesAssignment);
                    if (decisionLevelToBackTrack.getKey() < 0) {
                        if(proofNeeded) {
                            processResolution(unsatProof);

                        }
                        return "UNSAT";
                    } else {
                        int backtrackingLevel = backtrackUIP(decisionLevelToBackTrack.getKey(), decisionLevelToBackTrack.getValue());
                        tempUnitPropVariables.clear();
                        if (backtrackingLevel > -1) {
                            backtracked = 1;
                            currentDecisionLevel = backtrackingLevel;
                        } else {
                            if(proofNeeded) {
                                processResolution(unsatProof);
                            }
                            return "UNSAT";
                        }

                    }
                }
            }
            if(backtracked == 0) {
                currentDecisionLevel++;
            }
        }
        printArrayListAnswers(valuesAlreadyAssigned);
        return "SAT";

    }

    private void processResolution(ArrayList<Clause> unsatProof) throws IOException {
        unsatProof.remove((unsatProof.size() - 1));
        unsatProof.remove((unsatProof.size() - 1));
        unsatProof.remove((unsatProof.size() - 1));
        ArrayList<Clause> unsatProofUnique = new ArrayList<>();
        for(Clause c : unsatProof) {
            if(!unsatProofUnique.contains(c)) {
                unsatProofUnique.add(c);
            }
        }
        ArrayList<Clause> unsatProofUniqueClone = (ArrayList<Clause>) unsatProofUnique.clone();
        for(Clause c : unsatProofUniqueClone) {
            if(c.getOrVariables().size() == 0) {
                unsatProofUnique.remove(c);
            }
        }
        for(Clause c : unsatProofUnique) {
            Clause cdash = c.negateClause();
            if(unsatProofUnique.contains(cdash) && cdash.orVariables.size() != 0){
                unsatProof.add(c);
                unsatProof.add(cdash);
                unsatProof.add(new Clause(new ArrayList<>()));
            }
        }

        File fout = new File("resolutionProof.txt");
        FileOutputStream fos = new FileOutputStream(fout);

        BufferedWriter bw = new BufferedWriter(new OutputStreamWriter(fos));

        bw.write("v " + unsatProofUnique.size());
        bw.newLine();
        for (int i = 0; i < unsatProofUnique.size(); i++) {
            bw.write(unsatProofUnique.get(i).toString());
            bw.newLine();
        }
        for(int i = 0; i <= unsatProof.size() - 3; i+=3) {
            for(int j = i ; j < i + 3; j++) {
               if(unsatProof.get(j).getOrVariables().size() == 0) {
                   bw.write("-1 ");
               } else {
                   bw.write((unsatProofUnique.indexOf(unsatProof.get(j)) + 1) + " ");
               }
            }
            bw.newLine();
        }
        bw.close();
    }


    //Backtracking function in Case of UIP
    private int backtrackUIP(Integer key, Clause value) {

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
                for(int i = 1; i < totalNumberOfVariables; i++) {
                    implicationGraph[i][v.getVariableName()] = 0;
                }
            }
        }

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

    //Backtracking function in case of Normal conflict analysis
    private int backtrack(ArrayList<Clause> formula, ArrayList<Variable> valuesAlreadyAssigned, Integer decisionToLevelToBackTrack, ArrayList<Variable> variablesToLearn) {
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

                    } else {
                        variablesAssignment.put(v.getVariableName(), 0);
                        decisionLevelAssigned.put(v.getVariableName(), -1);
                        for (int i = 1; i < totalNumberOfVariables; i++) {
                            implicationGraph[i][v.getVariableName()] = 0;
                        }
                        numberOfVariablesAssigned--;
                    }
                }
            }
        }
        return decisionToLevelToBackTrack;
    }

    //Helper to print the answers
    private void printArrayListAnswers(ArrayList<Variable> valuesAlreadyAssigned) {

        System.out.println("Final Values are");
        for(Variable v : valuesAlreadyAssigned) {
            for(Map.Entry<Integer,Integer> k : variableToCodeMapping.entrySet()){
                if(k.getValue().equals(v.getVariableName())) {
                    Variable vdash = new Variable(k.getKey(), v.variableValue);
                    System.out.println(vdash);
                }
            }
        }
    }

    // Analysing and adding clauses with 1st UIP
    private Pair<Integer, Clause> conflictAnalysisUIP(ArrayList<Clause> formula, ArrayList<Variable> valuesAlreadyAssigned, int[][] implicationGraph, Variable value, HashMap<Integer, Integer> variablesAssignment) {
        Clause c = conflictLearnUIP(implicationGraph, valuesAlreadyAssigned, formula, value, variablesAssignment);

        if(c == null || c.getOrVariables().size() == 0) {
            return new Pair<>(-1, c);
        }
        int decisionLevelToBackTrackUIP = getDecisionLevelTobackTrack(c);
        return new Pair<>(decisionLevelToBackTrackUIP, c);

    }

    // Helper function to get what level to back track to in case of 1stUIP
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

    //Helper function to retrived the max level to back track to in the learnt clause
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

    // Actual helper to find the clause to learn
    private Clause conflictLearnUIP(int[][] implicationGraph, ArrayList<Variable> valuesAlreadyAssigned, ArrayList<Clause> formula, Variable value, HashMap<Integer, Integer> variablesAssignment) {
        Clause conflictingClause = findConflictingClause(value);
        if(conflictingClause == null) {
            return null;
        }
        Variable lastGuessedVariablethroughUnitProp = getLastGuessedVariableInC(conflictingClause);
        Clause antecedantOfLastGuessedVariable = antecedant(lastGuessedVariablethroughUnitProp);
        Clause finalClauseToAdd = resolve(conflictingClause, antecedantOfLastGuessedVariable);
        if(conflictingClause == null || finalClauseToAdd == null) {
            return null;
        }
        if(finalClauseToAdd.getOrVariables().size() != 0) {
            formula.add(0, finalClauseToAdd);
            totalNumberOfClauses++;
        } else {

        }
        return finalClauseToAdd;
    }

    // Function to do resolution
    private Clause resolve(Clause conflictingClause, Clause antecedant) {
        if(antecedant != null) {
            Clause conflictingClauseClone = new Clause((ArrayList<Variable>) conflictingClause.getOrVariables().clone());
            Clause antecedantClauseClone = new Clause((ArrayList<Variable>) antecedant.getOrVariables().clone());
            unsatProof.add(conflictingClauseClone);
            unsatProof.add(antecedantClauseClone);

        } else {
            Clause conflictingClauseClone = new Clause((ArrayList<Variable>) conflictingClause.getOrVariables().clone());
            Clause antecedantClauseClone = new Clause(new ArrayList<>());
            unsatProof.add(conflictingClauseClone);
            unsatProof.add(antecedantClauseClone);
        }
        if(antecedant == null || conflictingClause == null || terminatingCondition(conflictingClause)) {
            Clause conflictingClauseClone = new Clause((ArrayList<Variable>) conflictingClause.getOrVariables().clone());
            unsatProof.add(conflictingClauseClone);
            return conflictingClause;
        } else if (conflictingClause.getOrVariables().size() == 0) {
            Clause conflictingClauseClone = new Clause((ArrayList<Variable>) conflictingClause.getOrVariables().clone());
            unsatProof.add(conflictingClauseClone);
            return conflictingClause;
        }else {
            Clause intermediateClause = applyResolution(conflictingClause, antecedant);
            intermediateClause.removeDuplicates();
            Clause intermediateClauseClone = new Clause((ArrayList<Variable>) intermediateClause.getOrVariables().clone());
            unsatProof.add(intermediateClauseClone);
            Variable lastGuessedVariable = getLastGuessedVariableInC(intermediateClause);
            Clause antecedantProof = antecedant(lastGuessedVariable);
            Clause resolvedClause = resolve(intermediateClause, antecedantProof);
            return resolvedClause;
        }
    }

    // Check for terminating condition in the case of 1st UIP [Checks if only one variable is from the current decision level of conflict]
    private boolean terminatingCondition(Clause conflictingClause) {
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
            return true;
        } else {
            return false;
        }
    }

    // Helper to do resolution
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

    // Retrives the antecedant of the last guessed variable
    private Clause antecedant(Variable lastGuessedVariablethroughUnitProp) {
        Variable temp = lastGuessedVariablethroughUnitProp.modVariableName();
        return UIPtrack.get(temp.getVariableName());
    }

    // Finds the last guessed variable in a clause
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

    // Gets the conflicting clause from the implication graph
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

    // Conflict analysis in the case of non UIP method
    private Pair<Integer, ArrayList<Variable>> conflictAnalysis(ArrayList<Clause> formula, ArrayList<Variable> valuesAlreadyAssigned, int[][] implicationGraph, Variable value, HashMap<Integer, Integer> variablesAssignment) {
        ArrayList<Variable> variablesToLearn = conflictLearn(formula, implicationGraph,valuesAlreadyAssigned, value,variablesAssignment);
        int decisionLevelToBackTrack = currentDecisionLevel;
        return new Pair<>(decisionLevelToBackTrack, variablesToLearn);
    }


    // Learns a particular clause in the non UIP method
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
            formula.add(0,newClauseLearnt);
            totalNumberOfClauses++;
            return variablesToLearn;

        } else {
            return variablesToLearn;
        }
    }

    //Helper function to find a variable in an Arraylist of variables
    private Variable findVariable(int variableName, ArrayList<Variable> valuesAlreadyAssigned){
        for(Variable v : valuesAlreadyAssigned) {
            if(v.getVariableName() == variableName) {
                return v;
            }
        }
        return null;
    }

    // Implementation of initial setUp required to run the algorithm
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

    //Unit propogation method
    private Pair<Integer, Variable> unitpropogation() {
        int unitPropDone = 0;
        while (unitPropDone == 0) {
            ArrayList<Variable> addAllVariables = new ArrayList<>();
            for(Clause c : formula) {
                Pair<Integer, Variable> unassignedVariableCount = checkAndHandleClause(c);
                if(unassignedVariableCount.getKey() == 1 && tempUnitPropVariables.size() == 0) {
                    tempUnitPropVariables.add(unassignedVariableCount.getValue());
                    addAllVariables.addAll(c.getOrVariables());
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

    // Helper function to add to implication graph when there is a conflict
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

    // Helper function to add to implication graph when there is no conflict
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

    // Helper function to calculate the number of variables unassigned in an Arraylist of Variables
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

    // Function to check for conflict between two variables
    private int checkForConflict(Variable value, Variable variable) {
        int code = 0;
        if(value.getVariableValue() != variable.getVariableValue()) {
            code = -1;
        }
        return code;
    }

    // Handles Clause to check if its a unit clause or not and then handles the unassigned variable accordingly
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

    // 2 Clause heuristic function
    private void guess2CBranchingVariable(ArrayList<Clause> formula) {
        numberVariables++;
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

    }


    // All Clause heuristic function
    private void guessAllCBranchingVariable(ArrayList<Clause> formula) {
        numberVariables++;
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
    }


    // Random Guessing of a branching variable [Baseline heuristic]
    private void guessABranchingVariable(ArrayList<Clause> formula ) {
        Random randomClauseGenerator = new Random();
        int index = randomClauseGenerator.nextInt(formula.size());
        Clause c = formula.get(index);
        int value = pickRandomVariable(c);
        numberVariables++;
        while (value == 0) {
            index = randomClauseGenerator.nextInt(formula.size());
            c = formula.get(index);
            value = pickRandomVariable(c);
        }

    }

    // Helper function to chose a variable from a clause randomly
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
        decisionLevelAssigned.put(temp.getVariableName(), currentDecisionLevel);
        valuesAlreadyAssigned.add(temp);
        variablesAssignment.put(temp.getVariableName(), 1);
        lastDecidedVariables.add(temp);
        return 1;
    }

    // Helper function to debug. Inputs variables in the required order
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

    // Helper function to debug and input chosen variables
    private int pickChosenVariable(int name, boolean value) {
        Variable v = new Variable(name, value);
        numberOfVariablesAssigned++;
        Variable temp = v.modVariableName();
        decisionLevelAssigned.put(temp.getVariableName(), currentDecisionLevel);
        valuesAlreadyAssigned.add(temp);
        variablesAssignment.put(temp.getVariableName(), 1);
        lastDecidedVariables.add(temp);
        return 1;

    }
}