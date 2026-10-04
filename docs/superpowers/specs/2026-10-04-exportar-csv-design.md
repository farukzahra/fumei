# Exportar registros em CSV conforme a visão das estatísticas

## Objetivo

Extrair os registros do app em CSV, respeitando o período que está na tela de
estatísticas: mês selecionado, ano selecionado ou todos os anos.

## Comportamento

- Botão na aba Estatísticas, logo abaixo do card do período, com rótulo que diz
  o que será exportado: "Exportar CSV do mês", "Exportar CSV do ano" ou
  "Exportar CSV de tudo".
- Mês: registros cujo timestamp cai no mês selecionado, no fuso do aparelho.
- Ano: registros do ano selecionado.
- Todos os anos: todos os registros.
- O arquivo é salvo pelo seletor do Android (Storage Access Framework), no local
  que a pessoa escolher. Nenhuma permissão nova: o app continua sem acesso
  direto ao armazenamento.
- Nome sugerido: `fumei-2026-08.csv`, `fumei-2026.csv` ou `fumei-completo.csv`.

## Formato do arquivo

```
data;hora;gramas
01/08/2026;10:00;0,3
02/08/2026;13:00;0,5
```

- Separador `;`, decimal com vírgula, datas em `dd/MM/aaaa`, horas em `HH:mm`.
- Cabeçalho sempre presente, registros em ordem cronológica crescente.
- Fim de linha CRLF e conteúdo em UTF-8, para abrir direto no Excel pt-BR.

## Implementação

- Pacote `export`: `PuffCsv.build(puffs, zone)` (puro), `exportRangeFor(scope,
  month, year, zone)`, `fileNameFor(...)` e `exportLabelFor(scope)`.
- `PuffRepository` ganha `allPuffs()` e `puffsBetween(start, end)` sobre as
  consultas que já existiam no DAO.
- `StatsViewModel` expõe `exportRequest: StateFlow<CsvExport?>` com
  `onExportClick()` e `onExportHandled()`; o rótulo vai em `StatsUiState`.
- `MainActivity` usa `ActivityResultContracts.CreateDocument("text/csv")`, grava
  o conteúdo na URI escolhida e limpa o pedido depois.

## Validação

- Unitários em `PuffCsvTest`: cabeçalho, ordem crescente, decimal com vírgula,
  gramas inteiras sem decimais, fuso horário, limites de mês e ano, ausência de
  limite em todos os anos, nome do arquivo e rótulo do botão.
- E2E em `FumeiAppE2ETest`: exportação no mês usa só o mês (arquivo
  `fumei-2026-08.csv`), no ano usa só o ano (`fumei-2026.csv`) e em todos os anos
  usa tudo (`fumei-completo.csv`), conferindo o conteúdo do CSV.
- Conferido no emulador: arquivo salvo em Downloads com 1 cabeçalho + 22 linhas,
  igual ao total mostrado no card.

## Fora de escopo

Importar CSV, escolher colunas, exportar em outro formato e compartilhar por
outros apps.
