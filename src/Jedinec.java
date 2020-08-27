import java.util.Random;

public class Jedinec implements Comparable<Jedinec> {
    private byte[] bunky = new byte[64];
    public int fitness = 0;
    public int poklady = 0;
    public String cesta;

    @Override
    public int compareTo(Jedinec o) {
        return this.fitness == o.fitness ? 0 : this.fitness > o.fitness ? 1 : -1;
    }

    public Jedinec(int pocet_genov) {
        this.fitness = 0;
        this.cesta = cesta;
        Random rand = new Random();
        for (int i = 0; i < pocet_genov; i++) {
            this.bunky[i] = (byte) rand.nextInt(256);
        }
    }

    public Jedinec() { }

    public byte[] getBunky() { return bunky; }

    public void setBunky(byte[] bunky) { this.bunky = bunky; }

    public int getFitness() { return fitness; }

    public void setFitness(int fitness) { this.fitness = fitness; }

    public int getPoklady() { return poklady; }

    public void setPoklady(int poklady) { this.poklady = poklady; }

    public String getCesta() { return cesta; }

    public void setCesta(String cesta) { this.cesta = cesta; }

    private static final int zvys = 0b00_000000;
    private static final int zniz = 0b01_000000;
    private static final int skok = 0b10_000000;
    private static final int vypis = 0b11_000000;

    private static int urciAdresu(int value) { return value & 0b00_111111; }

    /**
     * zisti aka instrukcia sa ma vykonat (zvys/zniz/skok/vypis)
     * @param value
     * @return
     */
    private static int ziskajInstrukciu(int value) { return value & 0b11_000000; }

    /**
     * urci sa hodnota fitness jedinca a cesta hladaca
     * @param plocha
     * @param nastavenia
     * @param x
     * @param y
     * @param jedinec
     * @param poradie
     */
    public static void urciFitnessACestuJedinca(Plocha plocha, Nastavenia nastavenia, int x, int y, Jedinec jedinec, int poradie) {
        int fitness = 0, poklady = 0, index = 0;
        byte[] working = jedinec.bunky;

        boolean[][] poklad = new boolean[Start.X][Start.Y];
        for (int y_pom = 0; y_pom < Start.X; y_pom++) {
            for (int x_pom = 0; x_pom < Start.Y; x_pom++) {
                if (plocha.getPoklad()[x_pom][y_pom])
                    poklad[x_pom][y_pom] = true;
                else poklad[x_pom][y_pom] = false;
            }
        }
        StringBuilder cesta = new StringBuilder();

        for (int pocet_instrukcii=0; pocet_instrukcii < nastavenia.max_instrukcii;pocet_instrukcii++) {
            if (index > 63) {
                index = 0;
            }
            byte value = working[index];
            switch (ziskajInstrukciu(value)) {
                case zvys:
                    --working[urciAdresu(value)];
                    break;
                case zniz:
                    ++working[urciAdresu(value)];
                    break;
                case skok:
                    index = urciAdresu(value);
                    continue;
                case vypis:
                    String policko = "";
                    if ((working[urciAdresu(value)] & 0b11) == 0b01) {
                        policko = "D";
                        ++y;
                    } else if ((working[urciAdresu(value)] & 0b11) == 0b00) {
                        policko = "H";
                        --y;
                    } else if ((working[urciAdresu(value)] & 0b11) == 0b10) {
                        policko = "P";
                        ++x;
                    } else if ((working[urciAdresu(value)] & 0b11) == 0b11) {
                        policko = "L";
                        --x;
                    }

                    if (x < 0 || y < 0 || x >= plocha.sirka || y >= plocha.vyska) {
                        fitness -= nastavenia.fitness.vyjdenie_z_plochy;
                        Start.aktualnaGeneraciaJedincov[poradie].setPoklady(poklady);
                        Start.aktualnaGeneraciaJedincov[poradie].setFitness(fitness);
                        Start.aktualnaGeneraciaJedincov[poradie].setCesta(String.valueOf(cesta));
                        return;
                    }
                    else {
                        cesta.append(policko);
                        fitness -= nastavenia.fitness.krok;

                        if (poklad[x][y]==true) {
                            cesta.append(Start.ANSI_CYAN + '#' + Start.ANSI_RESET);
                            fitness += nastavenia.fitness.poklad;
                            if (++poklady == plocha.pocet_pokladov) {
                                Start.aktualnaGeneraciaJedincov[poradie].setPoklady(poklady);
                                Start.aktualnaGeneraciaJedincov[poradie].setFitness(fitness);
                                Start.aktualnaGeneraciaJedincov[poradie].setCesta(String.valueOf(cesta));
                                return;
                            }
                            poklad[x][y] = false;
                        }
                        break;
                    }
            }
            //pocet_instrukcii++;
            index++;
        }
        Start.aktualnaGeneraciaJedincov[poradie].setPoklady(poklady);
        Start.aktualnaGeneraciaJedincov[poradie].setFitness(fitness);
        Start.aktualnaGeneraciaJedincov[poradie].setCesta(String.valueOf(cesta));
        return;
    }

    /**
     * na nahodny bit a nahodnu bunku sa uplatni xor (v pomere podla nastaveni)
     * @param nastavenia
     */
    public final void zmutujJedinca(Nastavenia nastavenia) {
        Random Rand= new Random();
        var random = Rand.nextInt(nastavenia.mutacia.celkova_mutacia);
        int pomoc = 0;
        int index = Rand.nextInt(64);
        if (random < (pomoc += nastavenia.mutacia.getBez_mutovania())) {
        }
        else if (random < (pomoc += nastavenia.mutacia.getRandom_bit_xor())) {
            int bit = 1 << Rand.nextInt(8);
            bunky[index] ^= (byte) bit;
        }
        else bunky[index] ^= 0xFF;
        setBunky(bunky);
    }

    /**
     * zkrizia sa 2 jedince, bod krizenia je vygenerovany nahodne v rozmedzi minimalneho a maximalneho bodu krizenia
     * @param dalsi_jedinec -jedinec z ktorym krizime
     * @param nastavenia
     * @return vrati zkrizeneho jedinca
     */
    public final Jedinec krizenieJedinca(Jedinec dalsi_jedinec, Nastavenia nastavenia) {
        Random Rand = new Random();
        Jedinec krizeny_jedinec = new Jedinec();
        int i = 0;
        while (i < Rand.nextInt(((nastavenia.max_bod_krizenia-nastavenia.min_bod_krizenia)+1)+nastavenia.min_bod_krizenia)) {
            krizeny_jedinec.bunky[i] = bunky[i];
            i++;
        }
        while (i < 64) {
            krizeny_jedinec.bunky[i] = dalsi_jedinec.bunky[i];
            i++;
        }
        return krizeny_jedinec ;
    }
}
