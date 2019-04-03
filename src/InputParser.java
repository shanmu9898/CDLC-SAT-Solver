import static java.lang.Boolean.TRUE;

import java.io.BufferedReader;
import java.io.FileNotFoundException;
import java.io.FileReader;
import java.io.IOException;
import java.util.ArrayList;

import sun.tools.jstat.Literal;

public class InputParser {

    int numberOfVariables;
    int numberOfClauses;
    ArrayList<Clause> formula;

    public InputParser() {
        this.numberOfVariables = 0;
        this.numberOfClauses = 0;
        this.formula = new ArrayList<Clause>();
    }

    public ArrayList<Clause> parse(String filename){
        BufferedReader reader;
        try {
            reader = new BufferedReader(new FileReader(filename));
            String line = reader.readLine();
            line.trim();
            int count = 1;
            while(line != null){
                System.out.println(line);
                line.trim();
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

    @Override
    public String toString() {
        return "InputParser{" +
                "formula=" + formula +
                '}';
    }
}
