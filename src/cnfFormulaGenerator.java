/**
 * Created by hyma on 9/2/19.
 */

import java.util.Random;
import java.util.Scanner;

public class cnfFormulaGenerator {

    public void main (String args[]) {
        Scanner reader = new Scanner(System.in);
        System.out.println("Enter number of variables, N:");
        int numVar = reader.nextInt();
        System.out.println("Enter number of clauses, L:");
        int numClauses = reader.nextInt();

        int clauses[][];
        int count = 0;
        Variable var;
        Random rand = new Random();
        boolean negation;
        while (count < 3) {
            Variable variable;
            int n = rand.nextInt(numVar) + 1;
            var.setVariableIndentifier(n);
            int a = rand.nextInt(1);
            if (a == 1){
                negation = false;
            }
            else {
                negation = true;
            }
            var.setVariableValue(negation);

        }
    }
}