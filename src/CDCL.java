import java.io.File;
import java.io.IOException;
import java.util.ArrayList;
import java.util.Scanner;
import java.time.Instant;
import java.time.Duration;

public class CDCL {
    public static void main(String[] args) throws IOException {
        Scanner sc = new Scanner(System.in);
        String inputFileName = sc.nextLine();
        File f = new File(inputFileName);
        if (f.exists()) {
            int total = 0;
            // Branching heuristics has three choices :-  Random, 2C, AllC
            // Conflict Analysis has two choices      :-  GRASP, 1UIP
            // Solver below can be used to change the parameters and run accordingly

            long elapesedTimeTotal = 0;
            for(int i = 0; i < 1; i ++) {
                InputParser parser = new InputParser();
                ArrayList<Clause> formula = parser.parse(inputFileName);
                CDCLSolverUpdated cdclSolver = new CDCLSolverUpdated(parser.numberOfClauses, parser.numberOfVariables, formula);
                Instant start = Instant.now();
                String solution = cdclSolver.solution("Random", "GRASP", true);
                Instant finish = Instant.now();
                System.out.println(solution);
                long elapesedTime = Duration.between(start, finish).toNanos();
                elapesedTimeTotal += elapesedTime;
                total = total + cdclSolver.numberVariables;
                //System.out.println("Time taken is " + elapesedTimeTotal); //uncomment this to calculate timing
            }

        } else {
            System.out.println("Path does not exist. Please input full file path");
        }
    }
}