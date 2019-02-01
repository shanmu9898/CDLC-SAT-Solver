import java.util.ArrayList;

public class Clause {
    ArrayList<Variable> disjunctions;

    public Clause() {
        this.disjunctions = new ArrayList<Variable>();
    }

    public Clause(ArrayList<Variable> disjunctions) {
        this.disjunctions = disjunctions;
    }

    public ArrayList<Variable> getDisjunctions() {
        return disjunctions;
    }

    public void setDisjunctions(ArrayList<Variable> disjunctions) {
        this.disjunctions = disjunctions;
    }

    public void addDisjunctions(Variable variable) {
        this.disjunctions.add(variable);
    }

    @Override
    public String toString() {
        return "Clause{" +
                "disjunctions=" + disjunctions +
                '}';
    }
}
