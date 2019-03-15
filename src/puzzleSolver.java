public class puzzleSolver {

    // converting einstein puzzle to cnf
    // direct console output to einsteinPuzzle.out

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

        // house position
        int firstHouse = 25;
        int secondHouse = 26;
        int thirdHouse = 27;
        int fourthHouse = 28;
        int fifthHouse = 29;

        // encoding the rules

        // there are 5 houses in 5 different colours
        for (int i=0; i<5; i++) {
            for (int j=0; j<5; j++) {
                System.out.print(houseColour(i,j+1));
            }
            System.out.println("0");
            for (int j=0; j<5; j++) {
                //System.out.println(houseColour(i,j+1) + "0");
                for (int k=0; k<j; k++) {
                    System.out.println(-(houseColour(i, j+1)) +  "," + -(houseColour(i, k+1)) + "0");
                }
                for (int k=0; k<5; k++) {
                    if(!(i==k)) {
                        System.out.println(-(houseColour(i, j+1)) +  "," + -(houseColour(k, j+1)) + "0");
                    }
                }
            }
        }

        // in each house, lives a man with a different nationality
        for (int i=0; i<5; i++) {
            for (int j=0; j<5; j++) {
                System.out.print(nationality(i,j+1));
            }
            System.out.println("0");
            for (int j=0; j<5; j++) {
                //System.out.println(nationality(i,j+1) + "0");
                for (int k=0; k<j; k++) {
                    System.out.println(-(nationality(i, j+1)+25) +  "," + -(nationality(i, k+1)+25) + "0");
                }
                for (int k=0; k<5; k++) {
                    if(!(i==k)) {
                        System.out.println(-(nationality(i, j+1)+25) +  "," + -(nationality(k, j+1)+25) + "0");
                    }
                }
            }
        }

        // each owner drinks a certain type of beverage
        for (int i=0; i<5; i++) {
            for (int j=0; j<5; j++) {
                System.out.print(beverage(i,j+1));
            }
            System.out.println("0");
            for (int j=0; j<5; j++) {
                //System.out.println(beverage(i,j+1) + "0");
                for (int k=0; k<j; k++) {
                    System.out.println(-(beverage(i, j+1)+50) +  "," + -(beverage(i, k+1)+50) + "0");
                }
                for (int k=0; k<5; k++) {
                    if(!(i==k)) {
                        System.out.println(-(beverage(i, j+1)+50) +  "," + -(beverage(k, j+1)+50) + "0");
                    }
                }
            }
        }

        // each owner drinks a certain brand of cigar
        for (int i=0; i<5; i++) {
            for (int j=0; j<5; j++) {
                System.out.print(cigar(i,j+1));
            }
            System.out.println("0");
            for (int j=0; j<5; j++) {
                //System.out.println(cigar(i,j+1) + "0");
                for (int k=0; k<j; k++) {
                    System.out.println(-(cigar(i, j+1)+75) +  "," + -(cigar(i, k+1)+75) + "0");
                }
                for (int k=0; k<5; k++) {
                    if(!(i==k)) {
                        System.out.println(-(cigar(i, j+1)+75) +  "," + -(cigar(k, j+1)+75) + "0");
                    }
                }
            }
        }

        // each owner keeps a certain pet
        for (int i=0; i<5; i++) {
            for (int j=0; j<5; j++) {
                System.out.print(pet(i,j+1));
            }
            System.out.println("0");
            for (int j=0; j<5; j++) {
                //System.out.println(pet(i,j+1) + "0");
                for (int k=0; k<j; k++) {
                    System.out.println(-(pet(i, j+1)+100) +  "," + -(pet(i, k+1)+100) + "0");
                }
                for (int k=0; k<5; k++) {
                    if(!(i==k)) {
                        System.out.println(-(pet(i, j+1)+100) +  "," + -(pet(k, j+1)+100) + "0");
                    }
                }
            }
        }

        // first 7 hints
        for(int i=0; i<5; i++) {
            System.out.println(-(nationality(brit, i+1)) + "," + -(houseColour(red, i+1)));
            System.out.println(-(nationality(swede, i+1)) + "," + -(pet(dog, i+1)));
            System.out.println(-(nationality(dane, i+1)) + "," + -(beverage(tea, i+1)));

            // hint 4
            System.out.println(-(houseColour(white, firstHouse)) + "," + -(houseColour(green, secondHouse)));
            System.out.println(-(houseColour(white, secondHouse)) + "," + -(houseColour(green, thirdHouse)));
            System.out.println(-(houseColour(white, thirdHouse)) + "," + -(houseColour(green, fourthHouse)));
            System.out.println(-(houseColour(white, fourthHouse)) + "," + -(houseColour(green, fifthHouse)));

            System.out.println(-(houseColour(green, i+1)) + "," + -(beverage(coffee, i+1)));
            System.out.println(-(cigar(pallMall, i+1)) + "," + -(pet(bird, i+1)));
            System.out.println(-(houseColour(yellow, i+1)) + "," + -(cigar(dunhill, i+1)));
        }

        // hints 8 and 9
        System.out.println(-(beverage(milk, thirdHouse)));
        System.out.println(-(nationality(norwegian, firstHouse)));

        // hint 10
        System.out.println(-(cigar(blends, firstHouse)) + "," + -(pet(cat, secondHouse)));
        System.out.println(-(cigar(blends, secondHouse)) + "," + -(pet(cat, thirdHouse)));
        System.out.println(-(cigar(blends, thirdHouse)) + "," + -(pet(cat, fourthHouse)));
        System.out.println(-(cigar(blends, fourthHouse)) + "," + -(pet(cat, fifthHouse)));

        // hint 11
        System.out.println(-(pet(horse, firstHouse)) + "," + -(cigar(dunhill, secondHouse)));
        System.out.println(-(pet(horse, secondHouse)) + "," + -(cigar(dunhill, thirdHouse)));
        System.out.println(-(pet(horse, thirdHouse)) + "," + -(cigar(dunhill, fourthHouse)));
        System.out.println(-(pet(horse, fourthHouse)) + "," + -(cigar(dunhill, fifthHouse)));
        System.out.println(-(pet(horse, secondHouse)) + "," + -(cigar(dunhill, firstHouse)));
        System.out.println(-(pet(horse, thirdHouse)) + "," + -(cigar(dunhill, secondHouse)));
        System.out.println(-(pet(horse, fourthHouse)) + "," + -(cigar(dunhill, thirdHouse)));
        System.out.println(-(pet(horse, fifthHouse)) + "," + -(cigar(dunhill, fourthHouse)));

        // hints 12 and 13
        for(int i=0; i<5; i++) {
            System.out.println(-(cigar(bluemasters, i + 1)) + "," + -(beverage(beer, i + 1)));
            System.out.println(-(nationality(german, i + 1)) + "," + -(cigar(prince, i + 1)));
        }

        // hint 14
        System.out.println(-(nationality(norwegian, firstHouse)) + "," + -(houseColour(blue, secondHouse)));
        System.out.println(-(nationality(norwegian, secondHouse)) + "," + -(houseColour(blue, thirdHouse)));
        System.out.println(-(nationality(norwegian, thirdHouse)) + "," + -(houseColour(blue, fourthHouse)));
        System.out.println(-(nationality(norwegian, fourthHouse)) + "," + -(houseColour(blue, fifthHouse)));
        System.out.println(-(nationality(norwegian, secondHouse)) + "," + -(houseColour(blue, firstHouse)));
        System.out.println(-(nationality(norwegian, thirdHouse)) + "," + -(houseColour(blue, secondHouse)));
        System.out.println(-(nationality(norwegian, fourthHouse)) + "," + -(houseColour(blue, thirdHouse)));
        System.out.println(-(nationality(norwegian, fifthHouse)) + "," + -(houseColour(blue, fourthHouse)));

        // hint 15
        System.out.println(-(cigar(blends, firstHouse)) + "," + -(beverage(water, secondHouse)));
        System.out.println(-(cigar(blends, secondHouse)) + "," + -(beverage(water, thirdHouse)));
        System.out.println(-(cigar(blends, thirdHouse)) + "," + -(beverage(water, fourthHouse)));
        System.out.println(-(cigar(blends, fourthHouse)) + "," + -(beverage(water, fifthHouse)));
        System.out.println(-(cigar(blends, secondHouse)) + "," + -(beverage(water, firstHouse)));
        System.out.println(-(cigar(blends, thirdHouse)) + "," + -(beverage(water, secondHouse)));
        System.out.println(-(cigar(blends, fourthHouse)) + "," + -(beverage(water, thirdHouse)));
        System.out.println(-(cigar(blends, fifthHouse)) + "," + -(beverage(water, fourthHouse)));
    }

    public int houseColour(int a, int b) {
        return 5*a + b;
    }

    public int nationality(int a, int b) {
        return 5*a + b;
    }

    public int beverage(int a, int b) {
        return 5*a + b;
    }

    public int cigar(int a, int b) {
        return 5*a + b;
    }

    public int pet(int a, int b) {
        return 5*a + b;
    }

}
