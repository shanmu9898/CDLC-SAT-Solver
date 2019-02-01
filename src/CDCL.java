import java.util.Scanner;

public class CDCL {
    public static void main(String[] args) {
        Scanner sc = new Scanner(System.in);
        String inputFileName = sc.nextLine();
        InputParser parser = new InputParser();
        String formula = parser.parse(inputFileName).toString();
        System.out.println(formula);
        System.out.println(parser.numberOfClauses);
        System.out.println(parser.numberOfVariables);
    }

}
