package br.com.conde.luhn;

/**
 * O DÍGITO VERIFICADOR DE LUHN, de 1954, que está no seu cartão de crédito.
 *
 * <p>A receita: da direita para a esquerda, dobre um dígito sim, outro não, começando pelo
 * penúltimo. Se o dobro passar de 9, subtraia 9. Some tudo. O número é válido quando essa soma
 * é múltiplo de 10.
 *
 * <p>O "subtraia 9" é a parte que parece arbitrária e não é: dobrar 7 dá 14, e 1 mais 4 é 5,
 * que é 14 menos 9. Somar os algarismos do dobro e subtrair 9 do dobro são a mesma conta,
 * porque todo número de dois algarismos aqui começa com 1.
 *
 * <p>Este repositório não pergunta se o algoritmo funciona. Pergunta QUAIS erros ele pega, e a
 * resposta é um conjunto bem definido com uma falha bem definida dentro.
 */
public final class Luhn {

    private Luhn() {}

    /** A tabela do dobro já com o menos nove: dobrar e somar os algarismos. */
    private static final int[] DOBRO = {0, 2, 4, 6, 8, 1, 3, 5, 7, 9};

    /**
     * A soma de Luhn de uma sequência de dígitos, contando da DIREITA.
     *
     * <p>A posição a partir da direita é o que decide quem dobra, e é por isso que acrescentar
     * um dígito no fim muda quem dobra em todo o resto. Um algoritmo que contasse da esquerda
     * seria outro algoritmo, e daria outras respostas.
     */
    public static int soma(int[] digitos) {
        int total = 0;

        for (int posicao = 0; posicao < digitos.length; posicao++) {
            int digito = digitos[digitos.length - 1 - posicao];

            if (digito < 0 || digito > 9)
                throw new IllegalArgumentException("digito fora de 0 a 9: " + digito);

            total += posicao % 2 == 1 ? DOBRO[digito] : digito;
        }

        return total;
    }

    /** O número está bom: a soma fecha em múltiplo de dez. */
    public static boolean valido(int[] digitos) {
        return soma(digitos) % 10 == 0;
    }

    /** O dígito que, posto no fim, faz o número fechar. */
    public static int digitoVerificador(int[] semVerificador) {
        int[] comZero = new int[semVerificador.length + 1];

        System.arraycopy(semVerificador, 0, comZero, 0, semVerificador.length);

        return (10 - soma(comZero) % 10) % 10;
    }

    /** O número com o dígito verificador no fim. */
    public static int[] comVerificador(int[] semVerificador) {
        int[] saida = new int[semVerificador.length + 1];

        System.arraycopy(semVerificador, 0, saida, 0, semVerificador.length);

        saida[semVerificador.length] = digitoVerificador(semVerificador);

        return saida;
    }

    public static boolean valido(String digitos) {
        return valido(paraDigitos(digitos));
    }

    public static int[] paraDigitos(String texto) {
        int[] saida = new int[texto.length()];

        for (int indice = 0; indice < texto.length(); indice++) {
            char letra = texto.charAt(indice);

            if (letra < '0' || letra > '9')
                throw new IllegalArgumentException("nao e digito: " + letra);

            saida[indice] = letra - '0';
        }

        return saida;
    }

    public static String paraTexto(int[] digitos) {
        StringBuilder saida = new StringBuilder();

        for (int digito : digitos) saida.append((char) ('0' + digito));

        return saida.toString();
    }
}
