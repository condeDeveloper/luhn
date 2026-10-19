# Dígito verificador de Luhn

O algoritmo de Luhn, de 1954, que está no seu cartão de crédito, com **todos** os números de seis dígitos e **todos** os erros possíveis em cada um deles.

A pergunta aqui não é se ele funciona. É quais erros ele pega, e a resposta tem uma falha de nome e sobrenome: ele deixa passar exatamente `09` e `90`. E tem uma falha maior, que quase ninguém cita, porque ele não pega **nenhuma** troca entre dígitos separados por uma casa.

```
mvn -q exec:java -Dexec.args=transposicao
```

## A receita

Da direita para a esquerda, dobre um dígito sim, outro não, começando pelo penúltimo. Se o dobro passar de 9, subtraia 9. Some tudo. O número é válido quando a soma fecha em múltiplo de 10.

O "subtraia 9" parece arbitrário e não é: dobrar 7 dá 14, e 1 mais 4 é 5, que é 14 menos 9. Somar os algarismos do dobro e subtrair 9 são a mesma conta, porque todo dobro de dois algarismos aqui começa com 1.

## As medidas

### 1. O verificador faz o que promete

```
números gerados         1.000.000
saiu inválido                   0
verificador diferente           0
```

E os dez dígitos verificadores saem **igualmente**, cem mil vezes cada. Um verificador que preferisse alguns concentraria colisão, e seria pior sem parecer pior.

### 2. Erro de um dígito só

```
erros possíveis   63.000.000
pegos             63.000.000
taxa                 100,0000%
```

Todo erro de um dígito é pego, sem uma exceção em 63 milhões. Esse é o erro mais comum de quem digita número, e é a promessa principal do algoritmo.

### 3. Trocar dois vizinhos de lugar

```
erros possíveis    5.400.000
pegos              5.280.000
taxa                 97,7778%
```

Os pares que escaparam, e são só estes dois:

| par | escapou |
|---|---:|
| 09 | 60.000 |
| 90 | 60.000 |

E dá para ver exatamente por quê. A contribuição de um dígito muda conforme ele dobra ou não, **exceto** para 0 e para 9: zero dobrado é zero, e nove dobrado é 18, que vira 1 mais 8, que é nove. Nos dois, dobrar não muda nada. Trocar um pelo outro não mexe na soma, e o verificador não tem como perceber.

**E tem pior.** Trocar dois dígitos separados por uma casa, como escrever `321` onde era `123`:

```
erros possíveis    4.500.000
pegos                      0
taxa                  0,0000%
```

Zero. Nenhum. Posições separadas por uma casa caem no **mesmo lado** do dobra sim dobra não, então a soma não muda nunca. Não é uma falha estatística como o 09: é uma classe inteira de erro em que o algoritmo é cego por construção.

### 4. Trocar um par de iguais por outro par de iguais

```
erros possíveis    5.400.000
pegos              5.040.000
taxa                 93,3333%
```

Escapam exatamente seis pares: `22`, `33`, `44`, `55`, `66` e `77`, sessenta mil vezes cada.

Vale a honestidade: este erro muda **dois** dígitos, então ele nunca esteve coberto pela garantia de "um dígito só". Que o Luhn pegue 93% dele é sorte boa, não projeto.

### 5. O Luhn contra outros três esquemas

Todos os números de 5 dígitos mais verificador:

| esquema | um dígito | vizinhos | par de iguais | com salto |
|---|---:|---:|---:|---:|
| Luhn | **100,00%** | 97,78% | 93,33% | 0,00% |
| soma simples | **100,00%** | 0,00% | 88,89% | 0,00% |
| soma com pesos | 87,04% | 91,11% | 91,11% | 88,89% |
| módulo 11 | 98,48% | 98,14% | 78,51% | 98,13% |

Esta tabela existe porque "o Luhn pega 100% dos erros de um dígito" não diz nada sozinho. A soma mais burra possível também pega.

Onde eles se separam é na ordem: a **soma simples não pega nenhuma** troca de vizinhos, porque somar não liga para ordem. É exatamente para isso que existe o dobra sim dobra não.

E o **módulo 11**, que tem fama de ser o esquema certo, não pega todo erro de um dígito: 98,48%. O remendo de transformar resto 10 em zero custa isso, e é um custo que raramente aparece junto com a fama.

## O que as medidas me corrigiram

**A falha famosa do Luhn não é a pior falha do Luhn.** Todo mundo cita o `09` e `90`, que são 0,22% das trocas de vizinhos. Quase ninguém cita que ele não pega **uma única** troca entre dígitos separados por uma casa, que é 100% de uma classe inteira. A falha menor é a que virou folclore porque tem nome.

**"Pega 100% dos erros de um dígito" é um elogio vazio sem comparação.** A soma dos algarismos, sem dobrar nada, também pega 100%. O mérito do Luhn está inteiro na coluna de vizinhos.

**Módulo 11 não é estritamente melhor.** Ele ganha do Luhn em troca com salto, onde o Luhn zera, e perde no erro mais comum de todos por causa de um remendo de implementação, não de matemática.

## Rodar

```bash
mvn -q verify                                # 17 testes
mvn -q exec:java                             # todas as medidas, uns 6 segundos
mvn -q exec:java -Dexec.args=correcao        # 1
mvn -q exec:java -Dexec.args=digito          # 2
mvn -q exec:java -Dexec.args=transposicao    # 3
mvn -q exec:java -Dexec.args=gemeos          # 4
mvn -q exec:java -Dexec.args=alternativas    # 5
```

Java 21, sem dependência fora do JUnit. Nenhum número deste README foi digitado à mão: todos saem de `Medidas`, e os testes conferem os que aparecem escritos aqui.
