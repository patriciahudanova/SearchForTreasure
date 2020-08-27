import java.util.Scanner;

public class Nastavenia {

    static int pocet_random;
    static int pocet_max;
    static int typ_selekcie;
    static int stop;
    static boolean elitizmus;
    static Fitness fitness = new Fitness();
    static int elit_hodnota;
    static int min_bod_krizenia;
    static int max_bod_krizenia;
    static Mutacia mutacia = new Mutacia();
    static int max_instrukcii;
    
    public static Fitness getFitness() {
        return fitness;
    }

    public static void setFitness(Fitness fitness) { Nastavenia.fitness = fitness; }

    public static int getStop() { return stop; }

    public static void setStop(int stop) { Nastavenia.stop = stop; }

    public Nastavenia(int pocet_random, int pocet_max, int typ_selekcie, int stop, boolean elitizmus, int elit_hodnota, int min_bod_krizenia, int max_bod_krizenia){
        this.pocet_random=pocet_random;
        this.pocet_max=pocet_max;
        this.max_bod_krizenia=max_bod_krizenia;
        this.min_bod_krizenia=min_bod_krizenia;
        this.typ_selekcie=typ_selekcie;
        this.stop=stop;
        this.elitizmus=elitizmus;
        this.elit_hodnota= elit_hodnota;
        this.fitness = new Fitness(150, 1, 3);
        this.mutacia= new Mutacia(55, 20, 25);
        this.max_instrukcii=500;
    }
}
