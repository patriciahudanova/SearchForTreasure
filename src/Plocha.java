public class Plocha{
    public int vyska;
    public int pocet_pokladov;
    public boolean[][] poklad;
    public int sirka;

    public Plocha(int sirka, int vyska, int pocet_pokladov, int[][] mapa){
        this.sirka = sirka;
        this.vyska = vyska;
        this.poklad = new boolean[sirka][vyska];
        this.pocet_pokladov= pocet_pokladov;
        for(int y = 0; y<vyska ; y++) {
            for(int x = 0; x < sirka ; x++) {
                if(mapa[x][y] == 1)
                    this.poklad[x][y] = true;
                else this.poklad[x][y] = false;
            }
        }
    }

    public Plocha() {}

    public int getVyska() { return vyska; }

    public void setVyska(int vyska) { this.vyska = vyska; }

    public int getPocet_pokladov() { return pocet_pokladov; }

    public int getSirka() { return sirka; }

    public void setSirka(int sirka) { this.sirka = sirka; }

    public boolean[][] getPoklad() { return poklad; }

    public void setPoklad(boolean[][] poklad) { this.poklad = poklad; }

    public void setPocet_pokladov(int pocet_pokladov) { this.pocet_pokladov = pocet_pokladov; }
}

