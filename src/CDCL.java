import java.io.FileNotFoundException;
import java.util.ArrayList;
import java.util.Scanner;
import java.time.Instant;
import java.time.Duration;

public class CDCL {
    public static void main(String[] args) throws FileNotFoundException{
        Scanner sc = new Scanner(System.in);
        String inputFileName = sc.nextLine();
        InputParser parser = new InputParser();                          //Input parser
        ArrayList<Clause> formula = parser.parse(inputFileName);         // Parses file in dimacs format
        CDCLSolverUpdated cdclSolver = new CDCLSolverUpdated(parser.numberOfClauses, parser.numberOfVariables, formula);  //CDCL solver

        // Branching heuristics has three choices :-  Random, 2C, AllC
        // Conflict Analysis has two choices      :-  GRASP, 1UIP
        // Solver below can be used to change the parameters and run accordingly

        //String solution = cdclSolver.solution("Random", "GRASP");
        //System.out.println(solution);
        long elapesedTimeTotal = 0;

        for(int k = 0; k < 1; k++) {
            Instant start = Instant.now();
            System.out.println("Iteration : " + k);
            //String solution = cdclSolver.solution();
            String solution = cdclSolver.solution("Random", "GRASP");
            Instant finish = Instant.now();
            System.out.println(solution);
            long elapesedTime = Duration.between(start, finish).toNanos();
            //System.out.println("Time taken is " + elapesedTime);
            elapesedTimeTotal += elapesedTime;

        }
        System.out.println("Total Average time is  " + (elapesedTimeTotal/1));

    }
}
