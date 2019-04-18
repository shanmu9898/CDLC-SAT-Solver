import java.util.ArrayList;

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



    @Override
    public boolean equals(Object o) {
        if (this == o) return true;
        if (o == null || getClass() != o.getClass()) return false;
        Clause clauseTesting = (Clause) o;
        return clauseTesting.getOrVariables().equals(getOrVariables());
    }

    @Override
    public String toString() {
        return "Clause{" +
                "disjunctions=" + orVariables +
                '}';
    }
}
