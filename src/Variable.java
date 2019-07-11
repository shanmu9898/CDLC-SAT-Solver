public class Variable {
    int variableName;
    boolean variableValue;

    public Variable(int variableName, boolean variableValue){
        this.variableName = variableName;
        this.variableValue = variableValue;

    }

    public int getVariableName() {
        return variableName;
    }

    public boolean getVariableValue() {
        return variableValue;
    }

    public void setVariableValue(boolean variableValue) {
        this.variableValue = variableValue;
    }

    // Function to get the absolute vale of the variable name. Creates a duplicate variable and returns the duplicate.
    public Variable modVariableName() {
        int newVariableName = Math.abs(variableName);
        boolean newValue = this.variableValue;
        return new Variable(newVariableName, newValue);
    }

    @Override
    public boolean equals(Object o) {
        if (this == o) return true;
        if (o == null || getClass() != o.getClass()) return false;
        Variable variable = (Variable) o;
        return Math.abs(variableName) == Math.abs(variable.variableName);
    }

    @Override
    public String toString() {
        return "Variable{" +
                "variableName=" + variableName +
                ", variableValue=" + variableValue +
                '}';
    }
}
