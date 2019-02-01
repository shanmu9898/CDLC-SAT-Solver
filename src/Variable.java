public class Variable {
    int variableIdentifier;
    boolean variableValue;

    public Variable(int variableIdentifier, boolean variableValue){
        this.variableIdentifier = variableIdentifier;
        this.variableValue = variableValue;
    }

    public int getVariableIdentifier() {
        return variableIdentifier;
    }

    public void setVariableIdentifier(int variableIdentifier) {
        this.variableIdentifier = variableIdentifier;
    }

    public boolean getVariableValue() {
        return variableValue;
    }

    public void setVariableValue(boolean variableValue) {
        this.variableValue = variableValue;
    }

    @Override
    public boolean equals(Object o) {
        if (this == o) return true;
        if (o == null || getClass() != o.getClass()) return false;
        Variable variable = (Variable) o;
        return variableIdentifier == variable.variableIdentifier;
    }

    @Override
    public String toString() {
        return "Variable{" +
                "variableIdentifier=" + variableIdentifier +
                ", variableValue=" + variableValue +
                '}';
    }
}
