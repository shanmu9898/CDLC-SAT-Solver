import java.io.BufferedReader;
import java.io.FileReader;
import java.io.IOException;
import java.util.ArrayList;


public class InputParser {

    int numberOfVariables;
    int numberOfClauses;
    ArrayList<Clause> formula;

    public InputParser() {
        this.numberOfVariables = 0;                  // Got from the parser
        this.numberOfClauses = 0;                    // Got from the parser
        this.formula = new ArrayList<Clause>();      // Made after parsing the dimacs file
    }

    // Function to parse based on file name
    public ArrayList<Clause> parse(String filename){
        BufferedReader reader;
        try {
            reader = new BufferedReader(new FileReader(filename));
            String line = reader.readLine();
            line.trim();
            int count = 1;
            while(line != null){
                System.out.println(line);
                line = line.trim();
                String[] brokenVariables = line.split("\\s+");
                if(brokenVariables[0].equals("p")){
                    this.numberOfVariables = Integer.parseInt(brokenVariables[2]);
                    this.numberOfClauses = Integer.parseInt(brokenVariables[3]);
                } else if (brokenVariables[0].equals("c")) {
                    //TODO: Do nothing
                } else {
                   Clause clause = transformToClause(brokenVariables);
                   formula.add(clause);
                }
                line = reader.readLine();
            }
        } catch (IOException e) {
            e.printStackTrace();
        }

        return formula;

    }


    // Function to transform a string array into a clause
    private Clause transformToClause(String[] brokenVariables) {
        ArrayList<Variable> tempClause = new ArrayList<Variable>();
        for(String s : brokenVariables){
            if(!s.equals("0")){
                int variableIdentifier = Integer.parseInt(s);
                Variable variable;
                variable =new  Variable(variableIdentifier, true);
                tempClause.add(variable);
            }

        }
        return new Clause(tempClause);

    }

    // Function to print clauses
    @Override
    public String toString() {
        return "InputParser{" +
                "formula=" + formula +
                '}';
    }
}
