import java.io.File;
import java.io.FileNotFoundException;
import java.io.IOException;
import java.nio.file.Files;
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

            for(int i = 0; i < 1; i ++) {
                InputParser parser = new InputParser();
                ArrayList<Clause> formula = parser.parse(inputFileName);
                CDCLSolverUpdated cdclSolver = new CDCLSolverUpdated(parser.numberOfClauses, parser.numberOfVariables, formula);
                String solution = cdclSolver.solution("AllC", "1UIP", true);
                System.out.println(solution);
//            System.out.println("iteration " + i);
                total = total + cdclSolver.numberVariables;


            }

//        System.out.println("values is " + total/1);

        } else {
            System.out.println("Path does not exist. Please input full file path");
        }



    }

}
