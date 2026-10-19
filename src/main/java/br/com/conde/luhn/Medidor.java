package br.com.conde.luhn;

import java.util.Locale;

/** O MEDIDOR: roda as varreduras e imprime as tabelas do README. */
public final class Medidor {

    private Medidor() {}

    public static void main(String[] argumentos) {
        Locale.setDefault(Locale.US);

        String passo = argumentos.length > 0 ? argumentos[0] : "tudo";

        switch (passo) {
            case "correcao" -> correcao();
            case "digito" -> digito();
            case "transposicao" -> transposicao();
            case "gemeos" -> gemeos();
            case "alternativas" -> alternativas();
            default -> {
                correcao();
                digito();
                transposicao();
                gemeos();
                alternativas();
            }
        }
    }

    private static void titulo(String texto) {
        System.out.println();
        System.out.println(texto);
        System.out.println("=".repeat(texto.length()));
        System.out.println();
    }

    static void correcao() {
        titulo("1. o verificador faz o que promete");

        Medidas.Correcao m = Medidas.correcao();

        System.out.printf("numeros gerados         %d%n", m.numeros());
        System.out.printf("saiu invalido           %d%n", m.invalidos());
        System.out.printf("verificador diferente   %d%n", m.verificadorErrado());

        System.out.println();
        System.out.println("como os digitos verificadores se distribuem:");
        System.out.println();

        m.distribuicao().forEach((digito, quantos) ->
                System.out.printf("  digito %d  %7d%n", digito, quantos));

        System.out.println();
        System.out.println("os dez saem igualmente. um verificador que preferisse alguns");
        System.out.println("concentraria colisao, e seria pior sem parecer pior.");
    }

    static void digito() {
        titulo("2. erro de um digito so");

        Medidas.Deteccao d = Medidas.deteccao("um digito so");

        System.out.printf("erros possiveis   %d%n", d.erros());
        System.out.printf("pegos             %d%n", d.pegos());
        System.out.printf("taxa              %.4f%%%n", 100 * d.taxa());

        System.out.println();
        System.out.println("todo erro de um digito so e pego, sem uma excecao em 63 milhoes.");
        System.out.println("e esse e o erro mais comum de quem digita numero.");
    }

    static void transposicao() {
        titulo("3. trocar dois vizinhos de lugar");

        Medidas.Deteccao d = Medidas.deteccao("vizinhos trocados");

        System.out.printf("erros possiveis   %d%n", d.erros());
        System.out.printf("pegos             %d%n", d.pegos());
        System.out.printf("taxa              %.4f%%%n", 100 * d.taxa());

        System.out.println();
        System.out.println("os pares que ESCAPARAM, e sao so estes:");

        d.escaparam().forEach((par, quantos) ->
                System.out.printf("  %s  escapou %d vezes%n", par, quantos));

        System.out.println();
        System.out.println("0 e 9 sao o unico par em que dobrar e nao dobrar da a mesma");
        System.out.println("contribuicao: 0 dobrado e 0, e 9 dobrado e 1 mais 8, que e 9.");
        System.out.println("a troca nao muda a soma, e o digito nao tem como perceber.");

        Medidas.Deteccao salto = Medidas.deteccao("troca com salto");

        System.out.println();
        System.out.println("E TEM PIOR: trocar dois separados por um digito");
        System.out.printf("  erros possiveis   %d%n", salto.erros());
        System.out.printf("  pegos             %d%n", salto.pegos());
        System.out.printf("  taxa              %.4f%%%n", 100 * salto.taxa());
        System.out.println();
        System.out.println("zero. nenhum. posicoes separadas por uma casa caem no MESMO lado");
        System.out.println("do dobra sim dobra nao, entao a soma nao muda nunca.");
    }

    static void gemeos() {
        titulo("4. trocar um par de iguais por outro par de iguais");

        Medidas.Deteccao d = Medidas.deteccao("par de iguais");

        System.out.printf("erros possiveis   %d%n", d.erros());
        System.out.printf("pegos             %d%n", d.pegos());
        System.out.printf("taxa              %.4f%%%n", 100 * d.taxa());

        System.out.println();
        System.out.println("os que escaparam:");

        d.escaparam().forEach((par, quantos) ->
                System.out.printf("  %s  escapou %d vezes%n", par, quantos));

        System.out.println();
        System.out.println("este erro muda DOIS digitos, entao ele nunca esteve coberto pela");
        System.out.println("garantia de um digito so. que ele pegue 93% e sorte boa, nao projeto.");
    }

    static void alternativas() {
        titulo("5. o luhn contra outros tres esquemas");

        System.out.printf("todos os numeros de %d digitos mais verificador%n%n",
                Medidas.DIGITOS_COMPARACAO);

        System.out.println("esquema           um digito  vizinhos  par de iguais  com salto");

        for (Medidas.Comparacao c : Medidas.alternativas())
            System.out.printf("%-16s  %8.2f%%  %7.2f%%  %12.2f%%  %8.2f%%%n", c.esquema(),
                    100 * c.umDigito(), 100 * c.vizinhos(), 100 * c.gemeos(),
                    100 * c.comSalto());

        System.out.println();
        System.out.println("a soma simples pega todo erro de um digito e NENHUMA troca de");
        System.out.println("vizinhos: somar nao liga para ordem. e por isso que existe o");
        System.out.println("dobra sim dobra nao.");
        System.out.println();
        System.out.println("e o modulo 11, que tem fama de ser o certo, NAO pega todo erro de");
        System.out.println("um digito: o remendo de transformar resto 10 em zero custa isso.");
    }
}
