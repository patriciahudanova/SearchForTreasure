public class Mutacia {
    private int bez_mutovania;
    private int random_bunka_xor;
    private int random_bit_xor;
    public int celkova_mutacia;

    public Mutacia() { }

    public Mutacia(int bez_mutovania, int random_bunka_xor, int random_bit_xor) {
        this.bez_mutovania = bez_mutovania;
        this.random_bit_xor = random_bit_xor;
        this.random_bunka_xor = random_bunka_xor;
        this.celkova_mutacia = bez_mutovania + random_bit_xor + random_bunka_xor;
    }

    public final int getBez_mutovania() { return bez_mutovania; }

    public final int getRandom_bunka_xor() { return random_bunka_xor; }

    public final int getRandom_bit_xor() { return random_bit_xor; }
}

