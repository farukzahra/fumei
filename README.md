# Fumei

App Android para registrar quantas vezes você fumou no dia.

**Projeto:** `fumei.faruk.dev.br`  
**Package:** `fumei.faruk.dev.br`

## Funcionalidades

- Botão **Fumei** para registrar cada sessão
- Contador do dia: **Hoje: N**
- Lista de horários (formato 24h)
- Dados salvos localmente (SQLite via Room), sem internet

## Build

Requisitos: Android SDK, JDK 17+

```bash
# Windows
set JAVA_HOME=C:\Program Files\Android\Android Studio\jbr
gradlew.bat assembleDebug
```

APK gerado em `app/build/outputs/apk/debug/app-debug.apk`

## Testes

```bash
gradlew.bat test
gradlew.bat connectedAndroidTest
```

## Play Store

Ver `docs/play-store/` para listing, política de privacidade e setup do MCP.
