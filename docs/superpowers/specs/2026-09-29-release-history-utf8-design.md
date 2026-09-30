# Design: corrigir charset do histórico de releases

## Contexto

A aba Mais exibe títulos e resumos do histórico de releases com mojibake, embora
os arquivos de histórico verificados contenham texto UTF-8 válido. O repositório
carrega o JSON do asset sem declarar explicitamente o charset e o E2E atual usa
dados de histórico simulados.

## Opções consideradas

1. **Declarar UTF-8 e testar o asset real (escolhida):** tornar a decodificação
   determinística e cobrir o fluxo que alimenta a interface.
2. Corrigir apenas visualmente no emulador: não protege contra regressão.
3. Revisar todos os documentos do repositório com mojibake: amplia o escopo além
   do defeito visível na tela Mais.

## Design

- Ler `release-history.json` com `Charsets.UTF_8` explicitamente em
  `ReleaseHistoryRepository`.
- Adicionar teste de integração Android usando o asset real e verificar strings
  acentuadas do release atual, incluindo “Sessões em gramas” e “Configurações”.
- Registrar em `AGENTS.md` que arquivos de texto/JSON do app devem permanecer em
  UTF-8, leitores de conteúdo externo devem declarar o charset, e conteúdo
  destinado à interface deve ser inspecionado contra mojibake.
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

- Corrigir textos internos com mojibake que não aparecem no fluxo/tela afetado.
- Fazer mudanças de arquitetura no repositório de histórico.
