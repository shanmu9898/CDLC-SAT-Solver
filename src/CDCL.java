import java.util.ArrayList;
import java.util.Scanner;

public class CDCL {
    public static void main(String[] args) {
        Scanner sc = new Scanner(System.in);
        String inputFileName = sc.nextLine();
        InputParser parser = new InputParser();
        ArrayList<Clause> formula = parser.parse(inputFileName);
        CDCLSolverUpdated cdclSolver = new CDCLSolverUpdated(parser.numberOfClauses, parser.numberOfVariables, formula);
        String solution = cdclSolver.solution();
        System.out.println(solution);
    }

}
