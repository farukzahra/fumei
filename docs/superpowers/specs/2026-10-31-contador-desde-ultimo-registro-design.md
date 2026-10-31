# Contador ao vivo desde o ultimo registro

## Problema

Quem usa o app quer saber ha quanto tempo fumou pela ultima vez sem abrir a
timeline e calcular de cabeca. O tempo entre sessoes so aparece dentro dos
registros, e a tela inicial nao mostra nada sobre o ultimo uso.

## Comportamento

- Abaixo de "Toque para registrar" aparece uma linha: `Ha 0h00m00s desde o ultimo
  registro`.
- O valor comeca em `0h00m00s` quando o ultimo registro acabou de acontecer e
  sobe sozinho com a tela aberta, segundo a segundo, sem precisar tocar em nada.
- Vale o ultimo registro de qualquer dia. Se o ultimo foi ontem, o contador
  continua contando as horas, por exemplo `26h03m07s`.
- Registrar de novo zera o contador. Editar ou apagar o registro mais recente
  tambem atualiza o valor.
- Sem nenhum registro, a linha fica oculta: nao existe "ultimo" para contar.
- Registros com data no futuro contam como `0h00m00s`.

## Formato

Horas totais, minutos e segundos com dois digitos: `0h00m00s`, `0h00m05s`,
`1h05m09s`, `26h03m07s`. Segundos incompletos sao descartados.

## Implementacao

- `PuffDao.observeLatestPuff` busca o registro com maior `timestamp`.
- `PuffRepository.observeLatestPuffAt` expoe o timestamp para a camada de UI.
- `ElapsedSinceLastPuff` formata o tempo e combina o ultimo registro com um
  tick de 1 segundo.
- `MainViewModel.elapsedSinceLastPuff` fica em `WhileSubscribed(5_000)`, entao o
  tick para quando a tela sai de cena e volta atualizado ao reabrir.
- `RegisterActionButton` coleta o estado com `collectAsStateWithLifecycle`, para
  recompor so a linha do contador e nao a tela toda.
- A linha usa `Smoke400` no texto corrido e `Ember300` em monospace no tempo, com
  testTag `elapsed_since_last_puff`.

## Validacao

- Unitarios em `ElapsedSinceLastPuffTest`: formato com zero a esquerda, virada de
  dia, segundos incompletos, data futura, tick a cada segundo e reset ao
  registrar.
- E2E em `FumeiAppE2ETest`: contador com registro de 26h atras no formato com
  segundos, reset para `0h00m` depois de tocar em "Fumei agora", linha oculta sem
  registros e valor mudando com a tela parada.
- Conferido no emulador Pixel6: o valor avanca a cada segundo com a tela parada.

## Fora de escopo

Notificacao, widget, contador em segundo plano com o app fechado e alarme de
intervalo.
