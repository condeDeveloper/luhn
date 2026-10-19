package br.com.conde.luhn;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertFalse;
import static org.junit.jupiter.api.Assertions.assertThrows;
import static org.junit.jupiter.api.Assertions.assertTrue;

import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;

/**
 * O algoritmo sozinho, contra números que se pode conferir à mão e contra os de sempre.
 */
class LuhnTest {

    @Test
    @DisplayName("o exemplo da wikipedia, 79927398713, fecha")
    void exemploConhecido() {
        assertTrue(Luhn.valido("79927398713"));

        /* E os vizinhos dele, com o ultimo digito trocado, nao fecham. */
        for (int ultimo = 0; ultimo <= 9; ultimo++)
            if (ultimo != 3)
                assertFalse(Luhn.valido("7992739871" + ultimo), "terminando em " + ultimo);
    }

    @Test
    @DisplayName("numeros de cartao de teste sao validos, e um digito errado derruba")
    void cartoesDeTeste() {
        assertTrue(Luhn.valido("4539148803436467"));
        assertTrue(Luhn.valido("4111111111111111"));
        assertTrue(Luhn.valido("5500005555555559"));

        assertFalse(Luhn.valido("4539148803436466"));
        assertFalse(Luhn.valido("4111111111111112"));
    }

    @Test
    @DisplayName("a tabela do dobro e dobrar e somar os algarismos")
    void tabelaDoDobro() {
        /* So a soma de um digito na posicao que dobra: um numero de dois digitos onde o
         * primeiro dobra e o segundo e zero. */
        for (int digito = 0; digito <= 9; digito++) {
            int dobroSomado = digito * 2 >= 10 ? digito * 2 - 9 : digito * 2;

            assertEquals(dobroSomado, Luhn.soma(new int[] {digito, 0}), "digito " + digito);
        }
    }

    @Test
    @DisplayName("digito fora de 0 a 9 nao existe")
    void digitoInvalido() {
        assertThrows(IllegalArgumentException.class, () -> Luhn.soma(new int[] {1, 10}));
        assertThrows(IllegalArgumentException.class, () -> Luhn.soma(new int[] {-1}));
        assertThrows(IllegalArgumentException.class, () -> Luhn.valido("12a4"));
    }

    @Test
    @DisplayName("O VERIFICADOR SEMPRE FECHA, em todo numero de quatro digitos")
    void verificadorSempreFecha() {
        for (int valor = 0; valor < 10000; valor++) {
            int[] sem = Medidas.doNumero(valor, 4);

            int[] com = Luhn.comVerificador(sem);

            assertTrue(Luhn.valido(com), Luhn.paraTexto(com));
            assertEquals(5, com.length);

            /* E o verificador e unico: nenhum outro digito fecha. */
            for (int outro = 0; outro <= 9; outro++) {
                if (outro == com[4]) continue;

                int[] torto = com.clone();

                torto[4] = outro;

                assertFalse(Luhn.valido(torto), Luhn.paraTexto(torto));
            }
        }
    }

    @Test
    @DisplayName("acrescentar um digito muda quem dobra, porque a conta e da direita")
    void contaDaDireita() {
        /* Em "12", o 1 dobra. Em "012", o 1 continua dobrando, mas em "123" quem dobra e o 2. */
        assertEquals(Luhn.soma(Luhn.paraDigitos("12")), Luhn.soma(Luhn.paraDigitos("012")));

        /* Em "123", contando da direita: o 3 nao dobra, o 2 dobra e vira 4, o 1 nao dobra. */
        assertEquals(1 + 4 + 3, Luhn.soma(Luhn.paraDigitos("123")));

        /* Ja em "1234": o 4 nao dobra, o 3 dobra e vira 6, o 2 nao dobra, o 1 dobra e vira 2.
         * O MESMO "123" do comeco agora tem outros dobrados, porque a conta e da direita. */
        assertEquals(2 + 2 + 6 + 4, Luhn.soma(Luhn.paraDigitos("1234")));
    }

    @Test
    @DisplayName("texto e digitos vao e voltam")
    void idaEVolta() {
        for (int valor = 0; valor < 1000; valor++) {
            String texto = String.format("%06d", valor);

            assertEquals(texto, Luhn.paraTexto(Luhn.paraDigitos(texto)));
        }
    }
}
