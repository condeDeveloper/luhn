package br.com.conde.luhn;

/**
 * OUTROS DÍGITOS VERIFICADORES, para o Luhn ter com quem ser comparado.
 *
 * <p>Dizer que o Luhn pega 100% dos erros de um dígito só não significa nada sozinho: a soma
 * mais burra possível também pega. O que separa um esquema do outro são os erros de DUAS
 * posições, e é para medir isso que estas alternativas existem.
 */
public final class Alternativas {

    private Alternativas() {}

    /** O que cada esquema sabe fazer. */
    public interface Esquema {
        int digito(int[] semVerificador);

        default boolean valido(int[] comVerificador) {
            int[] sem = new int[comVerificador.length - 1];

            System.arraycopy(comVerificador, 0, sem, 0, sem.length);

            return digito(sem) == comVerificador[comVerificador.length - 1];
        }

        String nome();
    }

    /** O de Luhn. */
    public static final Esquema LUHN = new Esquema() {
        @Override
        public int digito(int[] sem) {
            return Luhn.digitoVerificador(sem);
        }

        @Override
        public String nome() {
            return "luhn";
        }
    };

    /**
     * A SOMA SIMPLES: some tudo, pegue o resto por dez, complete para dez.
     *
     * <p>Ela pega todo erro de um dígito só, igual ao Luhn. E não pega NENHUMA troca de
     * vizinhos, porque somar não liga para ordem. É a comparação que mostra para que serve o
     * "dobra um sim, outro não".
     */
    public static final Esquema SOMA = new Esquema() {
        @Override
        public int digito(int[] sem) {
            int total = 0;

            for (int digito : sem) total += digito;

            return (10 - total % 10) % 10;
        }

        @Override
        public String nome() {
            return "soma simples";
        }
    };

    /**
     * SOMA COM PESO PELA POSIÇÃO, módulo 10.
     *
     * <p>Multiplicar cada dígito pela posição mais um faz a ordem importar, então ela pega
     * trocas de vizinhos. Mas módulo 10 tem um problema estrutural: 2, 5 e 10 não são primos
     * entre si com 10, e os pesos pares perdem informação.
     */
    public static final Esquema PESOS = new Esquema() {
        @Override
        public int digito(int[] sem) {
            int total = 0;

            for (int posicao = 0; posicao < sem.length; posicao++)
                total += sem[posicao] * (posicao + 2);

            return (10 - total % 10) % 10;
        }

        @Override
        public String nome() {
            return "soma com pesos";
        }
    };

    /**
     * MÓDULO 11, que é o do CPF e do boleto.
     *
     * <p>Com 11 primo, todo peso é invertível e a matemática fica limpa: ele pega os erros de
     * um dígito e todas as trocas de vizinhos. O preço aparece quando o resto dá 10, que não é
     * um dígito: cada sistema inventa uma saída, e aqui ela é usar o zero.
     *
     * <p>Essa saída é um remendo, e ela custa: os números cujo verificador "deveria" ser 10
     * passam a compartilhar verificador com os de verificador 0, e o esquema perde força nesses
     * casos. A quinta medida conta quanto.
     */
    public static final Esquema MODULO11 = new Esquema() {
        @Override
        public int digito(int[] sem) {
            int total = 0;

            for (int posicao = 0; posicao < sem.length; posicao++)
                total += sem[sem.length - 1 - posicao] * (posicao + 2);

            int resto = 11 - total % 11;

            return resto >= 10 ? 0 : resto;
        }

        @Override
        public String nome() {
            return "modulo 11";
        }
    };

    public static final Esquema[] TODOS = {LUHN, SOMA, PESOS, MODULO11};
}
