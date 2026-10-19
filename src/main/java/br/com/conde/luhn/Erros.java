package br.com.conde.luhn;

import java.util.ArrayList;
import java.util.List;

/**
 * OS ERROS QUE AS PESSOAS COMETEM AO COPIAR UM NÚMERO.
 *
 * <p>Um dígito verificador não serve para pegar erro qualquer: serve para pegar os erros que
 * acontecem de verdade quando alguém digita ou dita uma sequência. A literatura de 1954 já
 * separava esses erros por frequência, e os dois primeiros da lista respondem pela maioria:
 *
 * <ul>
 *   <li>TROCAR UM DÍGITO: escrever 7 onde era 1. É o mais comum de todos.
 *   <li>TROCAR DOIS VIZINHOS DE LUGAR: escrever 21 onde era 12.
 *   <li>TROCAR UM PAR DE IGUAIS: escrever 22 onde era 11.
 *   <li>TROCAR DOIS SEPARADOS POR UM: escrever 321 onde era 123.
 * </ul>
 *
 * <p>Esta classe gera cada tipo de erro exaustivamente sobre um número, para as medidas
 * contarem quantos o Luhn pega. Nenhum erro é sorteado.
 */
public final class Erros {

    private Erros() {}

    /** Um número errado, e o que foi feito com ele. */
    public record Erro(int[] digitos, String tipo, int posicao, String detalhe) {}

    /**
     * TODO ERRO DE UM DÍGITO SÓ: cada posição, cada valor diferente do original.
     *
     * <p>Num número de n dígitos são {@code 9n} erros possíveis, e a lista é completa.
     */
    public static List<Erro> umDigito(int[] certo) {
        List<Erro> saida = new ArrayList<>();

        for (int posicao = 0; posicao < certo.length; posicao++)
            for (int novo = 0; novo <= 9; novo++) {
                if (novo == certo[posicao]) continue;

                int[] errado = certo.clone();

                errado[posicao] = novo;

                saida.add(new Erro(errado, "um digito", posicao,
                        certo[posicao] + " virou " + novo));
            }

        return saida;
    }

    /**
     * TODA TROCA DE VIZINHOS: cada par de posições coladas.
     *
     * <p>Trocar dois dígitos iguais não muda nada, então esses pares não são erro e ficam de
     * fora da lista. Contá-los como "pego" inflaria a estatística de graça.
     */
    public static List<Erro> vizinhos(int[] certo) {
        List<Erro> saida = new ArrayList<>();

        for (int posicao = 0; posicao + 1 < certo.length; posicao++) {
            if (certo[posicao] == certo[posicao + 1]) continue;

            int[] errado = certo.clone();

            errado[posicao] = certo[posicao + 1];
            errado[posicao + 1] = certo[posicao];

            saida.add(new Erro(errado, "vizinhos trocados", posicao,
                    "" + certo[posicao] + certo[posicao + 1] + " virou "
                            + certo[posicao + 1] + certo[posicao]));
        }

        return saida;
    }

    /**
     * TODO PAR DE IGUAIS VIRANDO OUTRO PAR DE IGUAIS: 11 virando 22.
     *
     * <p>É o erro de quem repete a tecla errada, e ele muda dois dígitos de uma vez, o que o
     * coloca fora da garantia de "um dígito só".
     */
    public static List<Erro> gemeos(int[] certo) {
        List<Erro> saida = new ArrayList<>();

        for (int posicao = 0; posicao + 1 < certo.length; posicao++) {
            if (certo[posicao] != certo[posicao + 1]) continue;

            for (int novo = 0; novo <= 9; novo++) {
                if (novo == certo[posicao]) continue;

                int[] errado = certo.clone();

                errado[posicao] = novo;
                errado[posicao + 1] = novo;

                saida.add(new Erro(errado, "par de iguais", posicao,
                        "" + certo[posicao] + certo[posicao] + " virou " + novo + novo));
            }
        }

        return saida;
    }

    /** TROCA COM SALTO: o primeiro e o terceiro trocam de lugar. */
    public static List<Erro> comSalto(int[] certo) {
        List<Erro> saida = new ArrayList<>();

        for (int posicao = 0; posicao + 2 < certo.length; posicao++) {
            if (certo[posicao] == certo[posicao + 2]) continue;

            int[] errado = certo.clone();

            errado[posicao] = certo[posicao + 2];
            errado[posicao + 2] = certo[posicao];

            saida.add(new Erro(errado, "troca com salto", posicao,
                    "" + certo[posicao] + " e " + certo[posicao + 2] + " trocaram"));
        }

        return saida;
    }

    /** Todos os tipos de uma vez. */
    public static List<Erro> todos(int[] certo) {
        List<Erro> saida = new ArrayList<>();

        saida.addAll(umDigito(certo));
        saida.addAll(vizinhos(certo));
        saida.addAll(gemeos(certo));
        saida.addAll(comSalto(certo));

        return saida;
    }
}
