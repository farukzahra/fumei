# MCP — Google Play Console

Para automatizar publicação e metadados via Cursor, use o MCP **play-console**.

## Recomendado

[`@orellbuehler/play-console-mcp`](https://github.com/OrellBuehler/play-console-mcp)

## Pré-requisitos

1. Conta Google Play Console (empresa) — já configurada
2. Projeto no Google Cloud com API **Google Play Android Developer API** habilitada
3. Service account com chave JSON
4. Service account convidada no Play Console com permissões de release

## Configuração no Cursor

Adicionar em `.cursor/mcp.json` (ou config global do MCP):

```json
{
  "mcpServers": {
    "play-console": {
      "command": "npx",
      "args": ["-y", "@orellbuehler/play-console-mcp"],
      "env": {
        "GOOGLE_SERVICE_ACCOUNT_KEY_PATH": "C:\\caminho\\para\\service-account.json",
        "GOOGLE_PLAY_PACKAGE_NAME": "fumei.faruk.dev.br"
      }
    }
  }
}
```

## Skills instaladas

- `play-developer-console` — automação via CLI `play`
- `android-playstore-setup` — setup de conta e permissões

## Primeiro release

1. Gerar AAB: `gradlew.bat bundleRelease`
2. Criar app no Play Console com package `fumei.faruk.dev.br`
3. Upload para faixa **internal testing**
4. Preencher listing, screenshots, política de privacidade
5. Promover para produção quando validado

## Assinatura release

Para produção, configurar keystore em `app/build.gradle.kts` (signingConfigs). Por enquanto o debug APK/AAB usa assinatura de debug.
