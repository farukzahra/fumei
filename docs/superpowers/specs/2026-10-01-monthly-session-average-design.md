# Média entre sessões nas estatísticas mensais

## Objetivo

Mostrar nas estatísticas mensais quanto tempo, em média, passou entre sessões
no mês selecionado.

## Comportamento

- Considerar somente sessões cujo timestamp, no fuso horário do aparelho,
  pertence ao mês selecionado.
- Ordenar as sessões por timestamp e calcular cada intervalo entre pares
  consecutivos, incluindo intervalos que atravessam a meia-noite.
- Não calcular intervalos com sessões fora do mês, incluindo a passagem para o
  mês anterior ou seguinte.
- Calcular a média aritmética dos intervalos do mês e apresentá-la em horas e
  minutos inteiros, descartando segundos e frações de minuto conforme a
  formatação existente para intervalos.
- Exibir no resumo mensal: “Você levou em média X entre as sessões neste mês.”
- Com menos de duas sessões, exibir: “Registre mais sessões para calcular a
  média”.
- Não mostrar essa informação nos escopos anual e de todos os anos.

## Apresentação

Adicionar a frase no cartão do período mensal, junto ao total e às gramas. O
texto acompanha o mês atualmente selecionado; navegar para outro mês atualiza
o cálculo.

## Implementação

Derivar o rótulo no `StatsViewModel` a partir dos registros já observados pelo
repositório e expor o resultado em `StatsUiState`. A tela somente apresenta o
rótulo recebido. Não alterar o banco nem a estrutura persistida.

## Validação

- Testes unitários cobrem ordenação, média, intervalos entre dias, exclusão de
  registros de meses adjacentes e o caso com menos de duas sessões.
- Um teste E2E Compose verifica o texto da média no escopo mensal e a mensagem
  quando há sessões insuficientes.
- Os escopos anual e de todos os anos permanecem sem essa informação.
