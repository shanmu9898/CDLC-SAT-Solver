import java.io.FileNotFoundException;
import java.io.IOException;
import java.util.ArrayList;
import java.util.Scanner;

public class CDCL {
    public static void main(String[] args) throws IOException {
        Scanner sc = new Scanner(System.in);
        String inputFileName = sc.nextLine();
        //Input parser
             // Parses file in dimacs format
         //CDCL solver

        // Branching heuristics has three choices :-  Random, 2C, AllC
        // Conflict Analysis has two choices      :-  Chaff, 1UIP
        // Solver below can be used to change the parameters and run accordingly
        int total = 0;
        for(int i = 0; i < 1; i ++) {
            InputParser parser = new InputParser();
            ArrayList<Clause> formula = parser.parse(inputFileName);
            CDCLSolverUpdated cdclSolver = new CDCLSolverUpdated(parser.numberOfClauses, parser.numberOfVariables, formula);
            String solution = cdclSolver.solution("AllC", "1UIP", true);
            System.out.println(solution);
            System.out.println("iteration " + i);
            total = total + cdclSolver.numberVariables;


        }

        System.out.println("values is " + total/4);



    }

}
