# Card de estatisticas: numeros com rotulo e media destacada

## Problema

O resumo do periodo nas estatisticas empilha tres dados em um unico `Text`
(`bodyMedium`, mesmo peso e tamanho): "140 no mes", "42 g no mes" e a frase da
media. Nada separa um dado do outro, "no mes" se repete em duas linhas seguidas
e o valor da media fica perdido no meio da frase. Alem disso o card usa
`primaryContainer`, que o tema nao define, entao ele herda o roxo padrao do
Material 3 em vez da paleta cinza e brasa do app.

## Layout aprovado (opcao A)

```
        <      Setembro de 2026      >
        ┌───────────┬───────────┐
        │    140    │  42 g     │
        │ SESSOES   │ GRAMAS    │
        │ NO MES    │ NO MES    │
        └───────────┴───────────┘
        ---------------------------
        MEDIA ENTRE SESSOES DO
             MESMO DIA
             2h2m
        Toque no titulo para ver o ano
```

- Cada numero com rotulo proprio embaixo, em caixa alta, letra espacada.
- Divisor vertical fino entre os dois blocos.
- Media em bloco separado por linha horizontal: rotulo curto e valor em destaque.
- Card nas cores do app: fundo `Ash850`, borda `Ash750`, numeros em `Paper100` e
  `Ember500`, media em `Ember300`. Nada de `primaryContainer`.
- Titulo, setas e a dica de zoom ficam iguais.

## Textos

| Dado | Valor | Rotulo |
|---|---|---|
| Sessoes | total do periodo | SESSOES NO MES / NO ANO / NO TOTAL |
| Gramas | `13,5 g` | GRAMAS NO MES / NO ANO / NO TOTAL |
| Media | `2h2m` ou `sem dados` | MEDIA ENTRE SESSOES DO MESMO DIA |

O rotulo da media so aparece no escopo mensal, como hoje. Quando nao existe par
de sessoes no mesmo dia, o valor vira `sem dados` no lugar da frase
"Registre mais sessoes para calcular a media".

## Implementacao

Pacote `stats`:

- Trocar `periodGramsLabel(totalGrams, scope)` por `periodCountCaption(scope)` e
  `periodGramsCaption(scope)`, cada uma devolvendo o rotulo do escopo.
- Valores formatados com `ConsumptionFormat.formatGramsTotal` +
  `SessionIntervalFormat.formatElapsedMillis`.

Pacote `ui`:

- `StatsSummaryStat(valueLabel, captionLabel)` novo.
- `StatsUiState` passa a expor `sessionsStat`, `gramsStat` e `averageStat`
  (nulo fora do escopo mensal) no lugar de `periodTotalLabel` e
  `periodGramsLabel`.
- `StatsViewModel` monta os tres blocos nos tres escopos.
- `StatsPeriodHeader` passa a desenhar o layout aprovado, com testTags
  `stats_summary_sessions`, `stats_summary_grams` e `stats_summary_average`.

## Validacao

- Unitarios em `StatsGramsAggregatorTest`: rotulos por escopo.
- E2E em `FumeiAppE2ETest`: resumo do mes mostra os dois numeros com seus
  rotulos; media do mesmo dia aparece como valor com rotulo proprio; mes sem par
  mostra `sem dados`; escopo anual nao mostra media.
- Conferir no emulador com os dados do seeder.

## Fora de escopo

Calendario, navegacao de periodo, escopos anual e de todos os anos seguem como
estao, so o texto do resumo muda de forma.
