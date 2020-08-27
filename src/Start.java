import java.io.*;
import java.util.Arrays;
import java.util.Random;
import java.util.Scanner;
import java.util.stream.IntStream;

public class Start {
    public static final String ANSI_RESET = "\u001B[0m";
    public static final String ANSI_GREEN = "\u001B[32m";
    public static final String ANSI_CYAN = "\u001B[36m";

    public static Jedinec[] aktualnaGeneraciaJedincov;
    public static Jedinec[] novaGeneraciaJedincov;
    public static Jedinec[] rodic;

    private static Random Rand = new Random();
    private static Jedinec top_fitness_jedinec = new Jedinec();
    private static Scanner vstup;
    public static int X, Y, X_start,Y_start, sucet_fitness, ruleta_index;

    /**
     * vypise plochu s pokladmi a zaciatocnym stavom
     * @param mapa -plochy zadana pouzivatelom
     */
    private static void vypisPlochu(int[][] mapa) {
        System.out.println("\n");
        for(int y = 0; y<Y ; y++) {
            for(int x = 0; x < X ; x++) {
                if(mapa[x][y] == 1)System.out.print("P ");
                else if(mapa[x][y] == 2)System.out.print("S ");
                else System.out.print(mapa[x][y] + " ");
                if(mapa[x][y]<=9 && mapa[x][y] >= 0)System.out.print(" ");
            }
            System.out.println();
        }
        System.out.println();
    }

    /**
     * vyber noveho jedinca proporcialnou selekciou
     * @param nastavenia
     * @param pom_sucet_fitness
     * @return
     */
    private static Jedinec selekciaRuleta(Nastavenia nastavenia,int pom_sucet_fitness) {
        Jedinec jedinec_a;
        int last = 0;
        var index = Rand.nextInt(pom_sucet_fitness);
        jedinec_a = null;
        for (int i = 0; i < nastavenia.pocet_max; i++) {
            if (i == ruleta_index) {
                continue;
            }
            if (index < aktualnaGeneraciaJedincov[i].getFitness()+ last) {
                jedinec_a = aktualnaGeneraciaJedincov[i];
                ruleta_index=i;
                break;
            }
            last += aktualnaGeneraciaJedincov[i].getFitness();
        }
        if (jedinec_a == null) {
            jedinec_a = aktualnaGeneraciaJedincov[nastavenia.pocet_max-1];
            ruleta_index = nastavenia.pocet_max - 1;
        }
        return jedinec_a;
    }

    /**
     * vyber 2 novych jedincov selekciou ohodnotenim
     * @param nastavenia
     */
    private static void selekciaTurnaj(Nastavenia nastavenia) {
        Jedinec[] vyber= new Jedinec[4];

        while (vyber[3] == null) {
            Jedinec dalsi = aktualnaGeneraciaJedincov[Rand.nextInt(nastavenia.pocet_max)];
            if (vyber[0] == dalsi || vyber[1]== dalsi ||vyber[2]== dalsi || vyber[3]== dalsi ) {
                continue;
            }
            for (int i=0; i<4; i++){
                if (vyber[i]==null){
                    vyber[i]=dalsi;
                }
            }
        }
        Arrays.sort(vyber);
        rodic[0] = vyber[3];
        rodic[1] = vyber[2];
    }

    /**
     * vytvorenie novej generacie- najprv sa vyberu 2 novy jedinci (turnajom alebo ruletou podla volby pouzivatela)
     * nasledne sa skrizia a zmutuju (v pomere podla nastaveni pouzivatela)
     * @param nastavenia
     * @param index
     */
    private static void vytvorNovuGeneraciu(Nastavenia nastavenia, int index) {
        rodic= new Jedinec[2];
        rodic[0] = new Jedinec();
        rodic[1] = new Jedinec();
        for (int i = index; i < nastavenia.pocet_max; i++) {
            if (nastavenia.typ_selekcie== 0) {
                selekciaTurnaj(nastavenia);
            }
            else{
                ruleta_index=-1;
                rodic[0]=selekciaRuleta(nastavenia,sucet_fitness);
                rodic[1]=selekciaRuleta(nastavenia,sucet_fitness-rodic[0].getFitness());
            }
            Jedinec novyJedinec = rodic[0].krizenieJedinca(rodic[1], nastavenia);

            novyJedinec.zmutujJedinca(nastavenia);

            novaGeneraciaJedincov[i] = novyJedinec;
        }
    }

    /**
     * vytvorenie novej generacie- kazdemu jedincovi sa podla nastaveni randomne vygeneruje niekolko buniek
     * @param nastavenia
     */
    private static void vytvorPrvuGeneraciu(Nastavenia nastavenia) {
        aktualnaGeneraciaJedincov = new Jedinec[nastavenia.pocet_max];
        for (int i = 0; i < nastavenia.pocet_max; ++i) {
            aktualnaGeneraciaJedincov[i] = new Jedinec(nastavenia.pocet_random);
        }
    }

    /**
     * urci sa fitness a cesta kazdeho jedinca v generacii (v paralelnom for cykle)
     * @param plocha
     * @param nastavenia
     * @param x
     * @param y
     * @return jedinec s najlepsou hodnotou fitness
     */
    private static Jedinec vypocitajHodnotuFitness(Plocha plocha, Nastavenia nastavenia, int x, int y) {
        top_fitness_jedinec= new Jedinec();
        top_fitness_jedinec.setFitness(-5000);

        IntStream.range(0, (nastavenia.pocet_max)).parallel().forEach(i -> {
            aktualnaGeneraciaJedincov[i].urciFitnessACestuJedinca(plocha, nastavenia, x, y, aktualnaGeneraciaJedincov[i],i);
            sucet_fitness+= aktualnaGeneraciaJedincov[i].getFitness();
            if (aktualnaGeneraciaJedincov[i].getFitness()>top_fitness_jedinec.getFitness() ){
                top_fitness_jedinec= aktualnaGeneraciaJedincov[i];
                if (aktualnaGeneraciaJedincov[i].getPoklady()==plocha.getPocet_pokladov()){
                    return ;
                }
            }
        });
        return top_fitness_jedinec;
    }

    /**
     * ak je povoleny elitizmus, do novej generacie sa vyberu elitny jedinci (pocet zavisi od nastavenia- defaultne 15%)
     * @param nastavenia
     * @return vrati index posledneho elitneho jedinca v novej generacii
     */
    public static int vyberElitu(Nastavenia nastavenia){
        if (!nastavenia.elitizmus) {
            return -1;
        }

        for (int i=0; i < (nastavenia.elit_hodnota * nastavenia.pocet_max / 100); i++) {
            novaGeneraciaJedincov[i]= aktualnaGeneraciaJedincov[nastavenia.pocet_max-1-i];
        }
        return nastavenia.elit_hodnota * nastavenia.pocet_max / 100;
    }

    /**
     * funkcia main s nacitanim vstupu a s kostrou hlavneho algoritmu ->blizsi komentar je v dokumentacii
     * @param args
     * @throws IOException
     */
    public static void main(String[] args) throws IOException {
        boolean ELITIZMUS = false;
        int ELIT_HODNOTA=15, MIN_BOD_KRIZENIA=22,MAX_BOD_KRIZENIA=40,POCET_RANDOM=16,POCET_MAX=250,TYP_SELEKCIE=0,STOP=200;
        int poklady;
        int[] fitness_graf= new int[100000];
        int graf_index=0;
        int[][] mapa;
        vstup = new Scanner(System.in);
        System.out.println("Zadajte typ vstupu: \n" +
                "0 -> nacitanie zo suboru vstup.txt\n" +
                "1 -> nacitanie vstupu z konzoly");

        int typ_vstupu= vstup.nextInt();
        if(typ_vstupu==1) {
            System.out.println("Zadajte typ vyberu jedinca: \n" +
                    "0 -> SELEKCIA OHODNOTENIM (turnaj)\n" +
                    "1 -> PROPORCIALNA SELEKCIA (ruleta)");

            TYP_SELEKCIE = vstup.nextInt();
            System.out.print("Zadajte veklost mapy\nx -> ");
            X = vstup.nextInt();
            System.out.print("y -> ");
            Y = vstup.nextInt();
            mapa = new int[X][Y];

            for (int y = 0; y < Y; y++) {
                for (int x = 0; x < X; x++) {
                    mapa[x][y] = 0;
                }
            }
            System.out.print("Zadajte pocet pokladov -> ");
            poklady = vstup.nextInt();
            System.out.println("Suradnice policok s pokladmi v tvare 'x y'");
            for (int i = 1; i <= poklady; i++) {
                int x, y;
                System.out.print(String.valueOf(i) + ". poklad -> ");
                x = vstup.nextInt();
                y = vstup.nextInt();
                mapa[x][y] = 1;
            }
            System.out.print("Zadajte suradnice zaciatocneho policka v tvare 'x y' -> ");
            X_start = vstup.nextInt();
            Y_start = vstup.nextInt();
            mapa[X_start][Y_start] = 2;
        }
        else{
            String filePath = "vstup.txt";

            String line;
            BufferedReader b = new BufferedReader(new FileReader(filePath));

            TYP_SELEKCIE = Integer.parseInt(b.readLine());

            String[] parts = b.readLine().split(":", 2);

            X = Integer.parseInt(parts[0]);
            Y = Integer.parseInt(parts[1]);

            mapa = new int[X][Y];

            for (int y = 0; y < Y; y++) {
                for (int x = 0; x < X; x++) {
                    mapa[x][y] = 0;
                }
            }

            poklady = Integer.parseInt(b.readLine());

            for (int i = 1; i <= poklady; i++) {
                int x, y;
                parts = b.readLine().split(":", 2);
                x = Integer.parseInt(parts[0]);
                y = Integer.parseInt(parts[1]);
                mapa[x][y] = 1;
            }

            parts = b.readLine().split(":", 2);
            X_start = Integer.parseInt(parts[0]);
            Y_start = Integer.parseInt(parts[1]);
            mapa[X_start][Y_start] = 2;
        }
        vypisPlochu(mapa);

        File file = new File("nastavenia.txt");

        BufferedReader br = new BufferedReader(new FileReader(file));

        while ((br.readLine()) != null) {
            POCET_RANDOM = Integer.parseInt(br.readLine());
            POCET_MAX=Integer.parseInt(br.readLine());
            STOP=Integer.parseInt(br.readLine());
            ELITIZMUS= Boolean.parseBoolean(br.readLine());
            ELIT_HODNOTA=Integer.parseInt(br.readLine());
            MIN_BOD_KRIZENIA=Integer.parseInt(br.readLine());
            MAX_BOD_KRIZENIA=Integer.parseInt(br.readLine());
        }
        Nastavenia nastavenia=new Nastavenia(POCET_RANDOM,POCET_MAX,TYP_SELEKCIE,STOP,ELITIZMUS,ELIT_HODNOTA,MIN_BOD_KRIZENIA,MAX_BOD_KRIZENIA);

        while(true){
            vytvorPrvuGeneraciu(nastavenia);
            Plocha plocha = new Plocha(X, Y, poklady, mapa);

            for(int gen=0;;++gen){
                System.out.println("GENERACIA c."+String.valueOf(gen));
                sucet_fitness=0;
                if (gen >= nastavenia.stop) {
                    String vypis = "";
                    for (int i=0; i<gen;i++){
                        vypis=vypis.concat(String.valueOf(fitness_graf[i])+"\n");
                    }
                    try {
                        FileWriter myWriter = new FileWriter("vyvoj_suctu_fitness.txt");
                        myWriter.write(vypis);
                        myWriter.close();
                    } catch (IOException e) {
                        System.out.println("An error occurred.");
                        e.printStackTrace();
                    }
                    System.out.println("Pocet generacii dosiahol hranicu:"+ String.valueOf(gen)+" generacii");
                    System.out.println( ANSI_CYAN + "Najlepsi najdeny jedinec: \n" +
                            "Hodnota fitness->"+ String.valueOf(novaGeneraciaJedincov[nastavenia.pocet_max-1].getFitness())+ "\n" +
                            "Cesta->"+ ANSI_RESET+ novaGeneraciaJedincov[nastavenia.pocet_max-1].getCesta() );
                    System.out.println("Pokracovat v hladani lepsieho jedinca -> P \n" +
                            "Ukoncit hladanie -> K");
                    String dalej=vstup.next();
                    switch (dalej){
                        case ("K"):
                            System.exit(2);
                            break;
                        case ("P"):
                            nastavenia.setStop(gen*2);
                            continue;
                    }
                }

                Jedinec result = vypocitajHodnotuFitness(plocha, nastavenia, X_start, Y_start);

                fitness_graf[graf_index++]=sucet_fitness;

                Arrays.sort(aktualnaGeneraciaJedincov);

                for (int i=0; i<nastavenia.pocet_max;i++) {
                    System.out.println("Jedinec c."+String.valueOf((i+1)) +". : Hodnota fitness->"+ String.valueOf(aktualnaGeneraciaJedincov[i].getFitness())+ ", Cesta ->" +ANSI_RESET+ aktualnaGeneraciaJedincov[i].getCesta());
                }

                if (result.getPoklady() == plocha.pocet_pokladov) {
                    String vypis = "";
                    for (int i=0; i<gen;i++){
                        vypis=vypis.concat(String.valueOf(fitness_graf[i])+"\n");
                    }
                    try {
                        FileWriter myWriter = new FileWriter("vyvoj_suctu_fitness.txt");
                        myWriter.write(vypis);
                        myWriter.close();
                    } catch (IOException e) {
                        System.out.println("An error occurred.");
                        e.printStackTrace();
                    }

                    System.out.println(ANSI_GREEN+"Vsetky poklady najdene.\n"+ ANSI_CYAN+"GENERACIA c."+String.valueOf(gen));
                    System.out.println("Najlepsi jedinec: \n" +
                            "Hodnota fitness->"+ String.valueOf(aktualnaGeneraciaJedincov[nastavenia.pocet_max-1].getFitness())+ "\n" +
                            "Cesta->"+ANSI_RESET+ aktualnaGeneraciaJedincov[nastavenia.pocet_max-1].getCesta());
                    System.exit(1);
                }

                novaGeneraciaJedincov = new Jedinec[nastavenia.pocet_max];
                for (int i = 0; i < nastavenia.pocet_max; ++i) {
                    novaGeneraciaJedincov[i] = new Jedinec();
                }

                int novy_index=vyberElitu(nastavenia);
                if (novy_index== -1) novy_index=0;

                vytvorNovuGeneraciu(nastavenia,novy_index);

                var pomoc_generacia= aktualnaGeneraciaJedincov;
                aktualnaGeneraciaJedincov = novaGeneraciaJedincov;
                novaGeneraciaJedincov =pomoc_generacia;
            }
        }
    }
}
