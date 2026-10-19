package br.com.conde.luhn;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertTrue;

import java.util.Set;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;

/** Os números que aparecem escritos no README, travados em teste. */
class MedidasTest {

    /* ===================== 1. correcao ===================== */

    @Test
    @DisplayName("um milhao de numeros gerados, nenhum invalido")
    void correcao() {
        Medidas.Correcao m = Medidas.correcao();

        assertEquals(1_000_000, m.numeros());
        assertEquals(0, m.invalidos());
        assertEquals(0, m.verificadorErrado());
    }

    @Test
    @DisplayName("os dez digitos verificadores saem igualmente, cem mil vezes cada")
    void distribuicaoUniforme() {
        Medidas.Correcao m = Medidas.correcao();

        assertEquals(10, m.distribuicao().size());

        for (int digito = 0; digito <= 9; digito++)
            assertEquals(100_000, m.distribuicao().get(digito), "digito " + digito);
    }

    /* ===================== 2. um digito ===================== */

    @Test
    @DisplayName("PEGA TODO ERRO DE UM DIGITO SO: 63 milhoes de 63 milhoes")
    void umDigito() {
        Medidas.Deteccao d = Medidas.deteccao("um digito so");

        assertEquals(63_000_000L, d.erros());
        assertEquals(63_000_000L, d.pegos());
        assertEquals(1.0, d.taxa(), 1e-12);

        assertTrue(d.escaparam().isEmpty());
    }

    /* ===================== 3. transposicao ===================== */

    @Test
    @DisplayName("DEIXA PASSAR EXATAMENTE 09 E 90, e mais nenhum par")
    void soEscapaZeroENove() {
        Medidas.Deteccao d = Medidas.deteccao("vizinhos trocados");

        assertEquals(5_400_000L, d.erros());
        assertEquals(5_280_000L, d.pegos());

        assertEquals(Set.of("09", "90"), d.escaparam().keySet());

        assertEquals(60_000L, d.escaparam().get("09"));
        assertEquals(60_000L, d.escaparam().get("90"));

        assertEquals(0.977778, d.taxa(), 1e-6);
    }

    @Test
    @DisplayName("e da para ver por que: 0 e 9 dao a mesma coisa dobrados ou nao")
    void porQueZeroENove() {
        /* A contribuicao de um digito na posicao que NAO dobra e ele mesmo; na que dobra,
         * e o dobro menos nove quando passa de nove. */
        for (int digito = 0; digito <= 9; digito++) {
            int dobrado = digito * 2 >= 10 ? digito * 2 - 9 : digito * 2;

            boolean iguais = dobrado == digito;

            assertEquals(digito == 0 || digito == 9, iguais, "digito " + digito);
        }
    }

    @Test
    @DisplayName("NAO PEGA NENHUMA troca com salto: zero de 4,5 milhoes")
    void saltoNaoPegaNada() {
        Medidas.Deteccao d = Medidas.deteccao("troca com salto");

        assertEquals(4_500_000L, d.erros());
        assertEquals(0L, d.pegos());
        assertEquals(0.0, d.taxa(), 1e-12);
    }

    /* ===================== 4. gemeos ===================== */

    @Test
    @DisplayName("nos pares de iguais pega 93,33%, e escapam os seis do meio")
    void paresDeIguais() {
        Medidas.Deteccao d = Medidas.deteccao("par de iguais");

        assertEquals(5_400_000L, d.erros());
        assertEquals(5_040_000L, d.pegos());

        assertEquals(0.933333, d.taxa(), 1e-6);

        /* Escapam 22, 33, 44, 55, 66 e 77: os que ao dobrar um deles caem no outro. */
        assertEquals(Set.of("22", "33", "44", "55", "66", "77"), d.escaparam().keySet());

        for (String par : d.escaparam().keySet())
            assertEquals(60_000L, d.escaparam().get(par), par);
    }

    /* ===================== 5. alternativas ===================== */

    @Test
    @DisplayName("A SOMA SIMPLES nao pega NENHUMA troca de vizinhos")
    void somaSimplesNaoPegaOrdem() {
        Medidas.Comparacao soma = Medidas.alternativa("soma simples");

        assertEquals(1.0, soma.umDigito(), 1e-12);
        assertEquals(0.0, soma.vizinhos(), 1e-12);

        /* E o luhn, com a mesma garantia de um digito, pega quase todas. */
        assertEquals(1.0, Medidas.alternativa("luhn").umDigito(), 1e-12);
        assertTrue(Medidas.alternativa("luhn").vizinhos() > 0.97);
    }

    @Test
    @DisplayName("O MODULO 11 NAO pega todo erro de um digito, apesar da fama")
    void moduloOnzeNaoEhPerfeito() {
        Medidas.Comparacao onze = Medidas.alternativa("modulo 11");

        assertTrue(onze.umDigito() < 1.0, "pegou " + onze.umDigito());
        assertEquals(0.9848, onze.umDigito(), 0.001);

        /* Ele ganha do luhn em troca com salto, onde o luhn zera. */
        assertTrue(onze.comSalto() > 0.98);
        assertEquals(0.0, Medidas.alternativa("luhn").comSalto(), 1e-12);
    }

    @Test
    @DisplayName("a soma com pesos e PIOR que a soma simples em erro de um digito")
    void pesosSaoPiores() {
        Medidas.Comparacao pesos = Medidas.alternativa("soma com pesos");

        assertTrue(pesos.umDigito() < Medidas.alternativa("soma simples").umDigito());
        assertEquals(0.8704, pesos.umDigito(), 0.001);
    }
}
