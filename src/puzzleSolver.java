/**
 * Created by hyma on 15/3/19.
 */
public class puzzleSolver {

    // converting einstein puzzle to cnf

    public void main (String args[]) {
        // nationalities
        int brit = 0;
        int swede = 1;
        int dane = 2;
        int norwegian = 3;
        int german = 4;

        // house colours
        int red = 5;
        int green = 6;
        int white = 7;
        int yellow = 8;
        int blue = 9;

        // pets
        int dog = 10;
        int bird = 11;
        int cat = 12;
        int horse = 13;

        // beverages
        int tea = 15;
        int coffee = 16;
        int milk = 17;
        int beer = 18;
        int water = 19;

        // cigar
        int pallMall = 20;
        int dunhill = 21;
        int blends = 22;
        int bluemasters = 23;
        int prince = 24;

        // encoding the rules

        // there are 5 houses in 5 different colours
        for (int i=0; i<5; i++) {
            for (int j=0; j<5; j++) {
                System.out.println(houseColour(i,j+1) + "0");
                for (int k=0; k<j; k++) {
                    System.out.println("-" + houseColour(i, j+1) +  "," + houseColour(i, k+1) + "0");
                }
                for (int k=0; k<5; k++) {
                    if(!(i==k)) {
                        System.out.println("-" + houseColour(i, j+1) +  "," + houseColour(k, j+1) + "0");
                    }
                }
            }
        }


    }

    public int houseColour(int a, int b) {
        return 5*a + b;
    }

}
