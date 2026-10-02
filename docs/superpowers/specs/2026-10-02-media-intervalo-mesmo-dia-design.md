# Média entre sessões apenas dentro do mesmo dia

## Problema

A média mensal entre sessões soma intervalos que atravessam a meia-noite.
`averageSessionIntervalMillis` ordena os registros do mês e mede a distância
entre pares consecutivos sem considerar a data. O vão entre a última sessão de
um dia e a primeira do dia seguinte (a noite inteira) entra na conta e infla o
resultado. Com dados reais de setembro de 2026 o app exibiu 3h20m de média,
sendo que a maior parte do tempo estava dormindo.

## Regra

Um intervalo só entra na média se os dois registros estiverem na **mesma data
local** do aparelho. Qualquer par que cruze a meia-noite é descartado.

- 23:00 e 23:30 no mesmo dia: conta 30m.
- 23:50 e 23:59 no mesmo dia: conta 9m.
- 23:50 e 00:10 do dia seguinte: não conta.
- Registros fora do mês selecionado continuam ignorados.

## Comportamento

- Manter o filtro do mês selecionado no fuso do aparelho.
- Ordenar por timestamp e descartar pares de datas diferentes.
- Média aritmética dos intervalos restantes, formatada em horas e minutos
  inteiros por `SessionIntervalFormat`.
- Sem nenhum par válido no mês, exibir "Registre mais sessões para calcular a
  média". Isso passa a acontecer também em meses com várias sessões, desde que
  nenhuma data tenha dois registros.
- Texto do cartão: "Você levou em média X entre as sessões do mesmo dia neste
  mês." A palavra "mesmo dia" existe para o número não parecer arbitrário.

## Fora de escopo

O timeline da tela Hoje não muda. `observeTodayWithYesterdayCount` entrega
somente os registros do dia corrente, então o registro mais antigo do dia já
aparece sem rótulo de intervalo e nenhum intervalo cruza a meia-noite ali.
Um teste E2E passa a travar esse comportamento com registros de ontem e de
hoje.

## Implementação

`StatsAggregator.averageSessionIntervalMillis` compara as datas locais dos dois
lados do par e ignora os pares divergentes. `StatsViewModel` só ajusta o texto
do rótulo. Nada muda no banco nem na estrutura persistida.

## Validação

- Unitários em `StatsAggregatorTest`: intervalo dentro do dia, pares cruzando a
  meia-noite descartados, média com pares válidos misturados a pares
  descartados, exclusão de registros de meses adjacentes, `null` sem par válido.
- E2E em `FumeiAppE2ETest`: média de 3h para dois registros do mesmo dia;
  mensagem de fallback no mês sem pares válidos; timeline da Hoje com registro
  de ontem e de hoje mostrando rótulos apenas entre registros de hoje.
- `release-history.json` (docs e assets) e, se o texto mudar, o hero do Sobre.
