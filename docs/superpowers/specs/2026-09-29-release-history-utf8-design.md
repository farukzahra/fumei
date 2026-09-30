# Design: corrigir charset do histórico de releases

## Contexto

A aba Mais exibe títulos e resumos do histórico de releases com mojibake. O
repositório carregava o asset sem declarar explicitamente o charset e o script
de bump lia JSON UTF-8 com `Get-Content` sem `-Encoding UTF8`. No Windows
PowerShell 5.1, isso decodifica os bytes pela página ANSI e regrava texto
corrompido como UTF-8. O teste E2E existente usa dados simulados.

## Opções consideradas

1. **Declarar UTF-8 e testar o asset real (escolhida):** tornar a decodificação
   determinística e cobrir o fluxo que alimenta a interface.
2. Corrigir apenas visualmente no emulador: não protege contra regressão.
3. Revisar todos os documentos do repositório com mojibake: amplia o escopo além
   do defeito visível na tela Mais.

## Design

- Ler `release-history.json` com `Charsets.UTF_8` explicitamente em
  `ReleaseHistoryRepository`.
- Ler JSON UTF-8 com `-Encoding UTF8` nos scripts de bump e validação de release.
- Validar em Python que `docs/release-history.json` e o asset sejam UTF-8 válido,
  iguais e sem marcadores de mojibake. Executar o validador no script de release
  e no Android CI.
- Adicionar testes de integração Android para o asset real e testes Python para
  strings acentuadas, mojibake, JSON inválido e divergência entre os arquivos.
- Registrar em `AGENTS.md` que arquivos de texto/JSON do app devem permanecer em
  UTF-8, leitores de conteúdo externo devem declarar o charset, e executar o
  validador automatizado.
- Manter `docs/release-history.json` e o asset sincronizados. O versionamento e a
  entrada de histórico de release serão tratados no fluxo `/commit-push`.

## Verificação e entrega

- Rodar o novo teste de integração e os testes existentes relevantes.
- Gerar o AAB pelo script documentado de Play Store, que incrementa
  `versionCode`.
- Instalar e abrir o app no emulador Pixel6 e conferir a aba Mais.
- Executar `/commit-push`, validar o workflow Android CI e verificar a página da
  Play Store conforme a configuração do repositório.

## Fora de escopo

- Fazer mudanças de arquitetura no repositório de histórico.
