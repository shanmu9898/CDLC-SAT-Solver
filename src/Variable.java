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

    public void setVariableName(int variableName) {
        this.variableName = variableName;
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
        return variableName == variable.variableName;
    }

    @Override
    public String toString() {
        return "Variable{" +
                "variableName=" + variableName +
                ", variableValue=" + variableValue +
                '}';
    }
}
