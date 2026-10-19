package br.com.conde.luhn;

import java.util.ArrayList;
import java.util.List;
import java.util.Map;
import java.util.TreeMap;

/**
 * AS MEDIDAS: todos os números de seis dígitos, com verificador, e todo erro possível em cada
 * um deles. Nada é sorteado.
 */
public final class Medidas {

    private Medidas() {}

    /** Quantos dígitos antes do verificador. Seis dá um milhão de números. */
    public static final int DIGITOS = 6;

    public static int quantosNumeros() {
        int total = 1;

        for (int passo = 0; passo < DIGITOS; passo++) total *= 10;

        return total;
    }

    /** O número de ordem vira os dígitos, com zeros à esquerda. */
    public static int[] doNumero(int valor, int digitos) {
        int[] saida = new int[digitos];

        for (int posicao = digitos - 1; posicao >= 0; posicao--) {
            saida[posicao] = valor % 10;
            valor /= 10;
        }

        return saida;
    }

    /* ===================== 1. correcao ===================== */

    public record Correcao(int numeros, int invalidos, int verificadorErrado,
            Map<Integer, Integer> distribuicao) {}

    private static Correcao correcao;

    /**
     * O VERIFICADOR FAZ O QUE PROMETE.
     *
     * <p>Duas conferências. Primeiro, todo número gerado com verificador é válido: isso é a
     * definição fechando em cima de si mesma, e se falhasse não haveria o que medir depois.
     *
     * <p>Segundo, a distribuição dos dígitos verificadores. Um verificador que preferisse
     * alguns dígitos seria um verificador pior, porque concentraria colisões. A tabela mostra
     * que os dez saem igualmente, cem mil vezes cada.
     */
    public static Correcao correcao() {
        if (correcao != null) return correcao;

        int invalidos = 0;
        int errados = 0;

        Map<Integer, Integer> distribuicao = new TreeMap<>();

        for (int valor = 0; valor < quantosNumeros(); valor++) {
            int[] sem = doNumero(valor, DIGITOS);
            int[] com = Luhn.comVerificador(sem);

            if (!Luhn.valido(com)) invalidos++;

            int verificador = com[DIGITOS];

            distribuicao.merge(verificador, 1, Integer::sum);

            if (Luhn.digitoVerificador(sem) != verificador) errados++;
        }

        return correcao = new Correcao(quantosNumeros(), invalidos, errados, distribuicao);
    }

    /* ===================== 2, 3 e 4: os tipos de erro ===================== */

    public record Deteccao(String tipo, long erros, long pegos, Map<String, Long> escaparam) {

        public double taxa() {
            return erros == 0 ? 0 : pegos / (double) erros;
        }
    }

    private static List<Deteccao> deteccao;

    /**
     * QUANTOS ERROS DE CADA TIPO O LUHN PEGA.
     *
     * <p>Um dígito verificador não promete pegar erro qualquer: promete pegar os erros que
     * acontecem quando gente copia número. A tabela separa por tipo e, quando algum escapa,
     * guarda exatamente qual foi.
     */
    public static List<Deteccao> deteccao() {
        if (deteccao != null) return deteccao;

        long[] erros = new long[4];
        long[] pegos = new long[4];

        List<Map<String, Long>> escaparam = new ArrayList<>();

        for (int tipo = 0; tipo < 4; tipo++) escaparam.add(new TreeMap<>());

        for (int valor = 0; valor < quantosNumeros(); valor++) {
            int[] certo = Luhn.comVerificador(doNumero(valor, DIGITOS));

            contar(Erros.umDigito(certo), 0, erros, pegos, escaparam, false);
            contar(Erros.vizinhos(certo), 1, erros, pegos, escaparam, true);
            contar(Erros.gemeos(certo), 2, erros, pegos, escaparam, true);
            contar(Erros.comSalto(certo), 3, erros, pegos, escaparam, true);
        }

        String[] nomes = {"um digito so", "vizinhos trocados", "par de iguais",
            "troca com salto"};

        List<Deteccao> saida = new ArrayList<>();

        for (int tipo = 0; tipo < 4; tipo++)
            saida.add(new Deteccao(nomes[tipo], erros[tipo], pegos[tipo],
                    Map.copyOf(escaparam.get(tipo))));

        return deteccao = List.copyOf(saida);
    }

    private static void contar(List<Erros.Erro> lista, int tipo, long[] erros, long[] pegos,
            List<Map<String, Long>> escaparam, boolean guardarQuemEscapa) {
        for (Erros.Erro erro : lista) {
            erros[tipo]++;

            if (!Luhn.valido(erro.digitos())) {
                pegos[tipo]++;
                continue;
            }

            if (!guardarQuemEscapa) continue;

            String chave = erro.detalhe().split(" ")[0];

            escaparam.get(tipo).merge(chave, 1L, Long::sum);
        }
    }

    public static Deteccao deteccao(String tipo) {
        return deteccao().stream().filter(x -> x.tipo().equals(tipo))
                .findFirst().orElseThrow();
    }

    /* ===================== 5. alternativas ===================== */

    public record Comparacao(String esquema, double umDigito, double vizinhos, double gemeos,
            double comSalto) {}

    private static List<Comparacao> alternativas;

    /** Quantos números para a comparação: cinco dígitos mais o verificador. */
    public static final int DIGITOS_COMPARACAO = 5;

    /**
     * O LUHN CONTRA OUTROS TRÊS ESQUEMAS.
     *
     * <p>Dizer que o Luhn pega 100% dos erros de um dígito não diz nada sozinho: a soma mais
     * burra também pega. A tabela existe para mostrar onde eles se separam, e para desfazer a
     * ideia de que módulo 11 é sempre melhor.
     */
    public static List<Comparacao> alternativas() {
        if (alternativas != null) return alternativas;

        int total = 1;

        for (int passo = 0; passo < DIGITOS_COMPARACAO; passo++) total *= 10;

        List<Comparacao> saida = new ArrayList<>();

        for (Alternativas.Esquema esquema : Alternativas.TODOS) {
            long[] erros = new long[4];
            long[] pegos = new long[4];

            for (int valor = 0; valor < total; valor++) {
                int[] sem = doNumero(valor, DIGITOS_COMPARACAO);

                int[] certo = new int[DIGITOS_COMPARACAO + 1];

                System.arraycopy(sem, 0, certo, 0, DIGITOS_COMPARACAO);

                certo[DIGITOS_COMPARACAO] = esquema.digito(sem);

                medir(esquema, Erros.umDigito(certo), 0, erros, pegos);
                medir(esquema, Erros.vizinhos(certo), 1, erros, pegos);
                medir(esquema, Erros.gemeos(certo), 2, erros, pegos);
                medir(esquema, Erros.comSalto(certo), 3, erros, pegos);
            }

            saida.add(new Comparacao(esquema.nome(), taxa(pegos[0], erros[0]),
                    taxa(pegos[1], erros[1]), taxa(pegos[2], erros[2]),
                    taxa(pegos[3], erros[3])));
        }

        return alternativas = List.copyOf(saida);
    }

    private static void medir(Alternativas.Esquema esquema, List<Erros.Erro> lista, int tipo,
            long[] erros, long[] pegos) {
        for (Erros.Erro erro : lista) {
            erros[tipo]++;

            if (!esquema.valido(erro.digitos())) pegos[tipo]++;
        }
    }

    private static double taxa(long pegos, long erros) {
        return erros == 0 ? 0 : pegos / (double) erros;
    }

    public static Comparacao alternativa(String nome) {
        return alternativas().stream().filter(x -> x.esquema().equals(nome))
                .findFirst().orElseThrow();
    }
}
