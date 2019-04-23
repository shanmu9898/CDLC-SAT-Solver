import java.io.FileNotFoundException;
import java.util.ArrayList;
import java.util.Scanner;

public class CDCL {
    public static void main(String[] args) throws FileNotFoundException{
        Scanner sc = new Scanner(System.in);
        String inputFileName = sc.nextLine();
        InputParser parser = new InputParser();                          //Input parser
        ArrayList<Clause> formula = parser.parse(inputFileName);         // Parses file in dimacs format
        CDCLSolverUpdated cdclSolver = new CDCLSolverUpdated(parser.numberOfClauses, parser.numberOfVariables, formula);  //CDCL solver

        // Branching heuristics has three choices :-  Random, 2C, AllC
        // Conflict Analysis has two choices      :-  Chaff, 1UIP
        // Solver below can be used to change the parameters and run accordingly

        String solution = cdclSolver.solution("Random", "Chaff");
        System.out.println(solution);


    }

}
