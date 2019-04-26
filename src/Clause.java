import java.util.ArrayList;
import java.util.HashSet;
import java.util.Set;

public class Clause {
    ArrayList<Variable> orVariables;

    public Clause() {
        this.orVariables = new ArrayList<Variable>();
    }

    public Clause(ArrayList<Variable> orVariables) {
        this.orVariables = orVariables;
    }

    public ArrayList<Variable> getOrVariables() {
        return orVariables;
    }

    public void setDisjunctions(ArrayList<Variable> disjunctions) {
        this.orVariables = disjunctions;
    }

    public void addDisjunctions(Variable variable) {
        this.orVariables.add(variable);
    }

    public boolean compare(Clause c) {
        int number = 0;
        for(Variable v : orVariables) {
            for(Variable vdash : c.getOrVariables()) {
                if(v.getVariableName() == vdash.getVariableName()) {
                    number++;
                }
            }
        }
        if(number == orVariables.size() && number == c.getOrVariables().size()) {
            return true;
        }else {
            return false;
        }
    }

    public void removeDuplicates() {
        ArrayList<Variable> nonDuplicateArray= new ArrayList<>();
        for(Variable v : orVariables) {
            int ting = 0;
            for(Variable v1 : nonDuplicateArray){
                if(v1.getVariableName() == v.getVariableName()) {
                    ting++;
                }
            }
            if(ting == 0) {
                nonDuplicateArray.add(v);
            }
        }
        orVariables = nonDuplicateArray;
    }

    public Clause negateClause() {
        ArrayList<Variable> orVariablesNeg = new ArrayList<>();
        for(Variable v : orVariables){
            orVariablesNeg.add(new Variable((v.getVariableName()*-1),v.variableValue));
        }
        Clause cdash = new Clause(orVariablesNeg);
        return cdash;
    }


    @Override
    public boolean equals(Object o) {
        if (this == o) return true;
        if (o == null || getClass() != o.getClass()) return false;
        Clause clauseTesting = (Clause) o;
        int number = 0;
        for(Variable v : orVariables) {
            for(Variable vdash : clauseTesting.getOrVariables()) {
                if(v.getVariableName() == vdash.getVariableName()) {
                    number++;
                }
            }
        }
        if(number == orVariables.size() && number == clauseTesting.getOrVariables().size()) {
            return true;
        }else {
            return false;
        }
    }

    @Override
    public String toString() {
        return "Clause{" +
                "disjunctions=" + orVariables +
                '}';
    }
}
