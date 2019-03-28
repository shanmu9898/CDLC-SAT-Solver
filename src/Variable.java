public class Variable {
    int variableName;
    boolean variableValue;
    int assignmentCount;


    public Variable(int variableName, boolean variableValue){
        this.variableName = variableName;
        this.variableValue = variableValue;
        int assigntmentCount = 0;

    }

    public int getAssignmentCount() {
        return assignmentCount;
    }

    public Variable modVariableName() {
        /* boolean newValue;
        if (variableName<0) {
            if (this.variableValue == false) {
                newValue = true;
            } else {
                newValue = false;
            }
        }
        else {
            newValue = this.variableValue;
        } */
       int newVariableName = Math.abs(variableName);
       boolean newValue = this.variableValue;
       return new Variable(newVariableName, newValue);
    }

    public void incrAssignmentCount() {
        assignmentCount++;
    }

    public void decAssignmentCount() {
        assignmentCount--;
    }

    public void clearAssignmentCount() {
        assignmentCount = 0;
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
