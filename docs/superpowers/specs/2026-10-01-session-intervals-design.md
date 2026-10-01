# Intervalos entre sessões na timeline

## Objetivo

Exibir na timeline de Hoje quanto tempo decorreu entre cada registro e o registro
mais antigo seguinte, facilitando a leitura dos intervalos de consumo.

## Comportamento

- Os registros continuam em ordem decrescente, do mais recente para o mais antigo.
- Para cada registro que tenha outro registro mais antigo no mesmo dia, calcular
  o tempo decorrido entre os dois timestamps.
- Exibir a duração sem til: `1h42m` para uma hora ou mais com minutos restantes,
  `1h` para uma hora exata e `59m` para intervalos menores que uma hora.
- Descartar segundos incompletos ao formatar a duração.
- Não exibir duração para o registro mais antigo do dia.
- Não calcular intervalo entre dias.

## Apresentação

- Manter a linha vertical da timeline contínua entre os pontos.
- Posicionar a duração na horizontal, à direita da linha.
- Centralizar verticalmente o rótulo entre os centros dos horários dos dois registros correspondentes.
- Usar tamanho de texto legível e proporcional aos horários e à timeline, sem
  deslocar ou sobrepor o horário, a quantidade em gramas ou as ações de editar e
  excluir.
- Preservar o layout quando houver somente um registro.

## Implementação e validação

- Derivar o texto de intervalo na camada de apresentação com base nos
  `PuffListItem` ordenados pelo timestamp.
- Manter a formatação de duração numa função pura testável.
- Escrever primeiro testes unitários para os limites de duração e um E2E Compose
  com três registros no mesmo dia; validar a ausência no mais antigo e medir o
  centro dos rótulos contra o ponto médio dos horários adjacentes.
- Instalar e abrir o app no emulador Pixel6, conferir visualmente a aba Hoje e
  deixar a alteração local. Não enviar à Play Store nem publicar sem pedido
  posterior do usuário.

## Fora de escopo

- Alterar dados persistidos ou o esquema do banco.
- Incluir sessões do dia anterior no cálculo.
- Fazer release, push ou publicação externa nesta etapa.
