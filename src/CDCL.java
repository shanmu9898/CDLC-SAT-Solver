import java.io.FileNotFoundException;
import java.time.Duration;
import java.time.Instant;
import java.util.ArrayList;
import java.util.Scanner;

import com.sun.tools.doclets.formats.html.SourceToHTMLConverter;

public class CDCL {
    public static void main(String[] args) throws FileNotFoundException{
        Scanner sc = new Scanner(System.in);
        String inputFileName = sc.nextLine();
        InputParser parser = new InputParser();
        ArrayList<Clause> formula = parser.parse(inputFileName);
        CDCLSolverUpdated cdclSolver = new CDCLSolverUpdated(parser.numberOfClauses, parser.numberOfVariables, formula);
        long elapesedTimeTotal = 0;
        for(int k = 0; k < 1; k++) {
            Instant start = Instant.now();
            System.out.println("Iteration : " + k);
            String solution = cdclSolver.solution();
            Instant finish = Instant.now();
            System.out.println(solution);
            long elapesedTime = Duration.between(start, finish).toNanos();
            //System.out.println("Time taken is " + elapesedTime);
            elapesedTimeTotal += elapesedTime;

        }
        System.out.println("Total Average time is  " + (elapesedTimeTotal/1));


    }

}
