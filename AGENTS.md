# Fumei — instruções para agentes

App Android: Kotlin, Jetpack Compose, Material 3, Room, MVVM. Package: `fumei.faruk.dev.br`.

## Superpowers (obrigatório)

Antes de implementar feature ou bugfix:

1. **brainstorming** — se for trabalho criativo/novo comportamento
2. **test-driven-development** — teste falhando primeiro, depois código
3. **verification-before-completion** — rodar comandos e citar evidência antes de dizer que passou
4. **humanizer** — ao escrever textos de UI/marketing; sem travessão (—), tom direto

## Testes

| Tipo | Pasta | Comando |
|------|-------|---------|
| Unitário | `app/src/test/` | `.\gradlew test` |
| DAO instrumentado | `app/src/androidTest/.../data/` | `.\gradlew connectedDebugAndroidTest` |
| E2E UI Compose | `app/src/androidTest/.../ui/*E2ETest.kt` | `.\gradlew connectedDebugAndroidTest` |

**Regra:** toda mudança em `ui/` exige E2E Compose novo ou atualizado, escrito **antes** da implementação (TDD).

E2E usa `createComposeRule()`, `PuffRepository` com banco in-memory e `MainViewModel` real.

### testTags úteis

- `fumei_button` — botão principal
- `entries_card` — lista de registros
- `empty_entries_message` — estado vazio
- `confirm_delete_button` / `confirm_edit_button` — diálogos

## Ao terminar task (obrigatório)

**Sempre** que concluir qualquer task (feature, bugfix, refactor, docs com impacto em build):

1. Subir ambiente local se estiver off (emulador **Pixel6** — ver `docs/emulator.md`)
2. `.\gradlew installDebug`
3. Abrir o app: `adb shell am start -n fumei.faruk.dev.br/fumei.faruk.dev.br.MainActivity`
4. **Conferir** que subiu (processo em foreground ou tela visível no emulador) — não basta instalar
5. Informar ao usuário que o app está rodando no emulador

Ver também `.cursor/rules/emulator-after-task.mdc`.

## Seed de dados local (só debug)

Código em `app/src/debug/` — **não compila no release**.

Na primeira abertura do APK debug, insere ~5 anos de registros (0–10 por dia, horários aleatórios). Roda uma vez; para repetir: limpar dados do app.

Não roda durante testes instrumentados.

## Build e emulador

```bash
.\gradlew assembleDebug installDebug
```

AVD documentado em `docs/emulator.md` (Pixel6, Android 14).

## Commit e push (`/commit-push`)

Ver `.cursor/commands/commit-push.md`. Após push em `main`, validar workflow **Android CI** no GitHub.

## Enviar APK ao celular (`/enviar-celular`)

Ver `.cursor/commands/enviar-celular.md`. Build debug + cópia para `Downloads\fumei-debug.apk`.

## Idioma

UI e mensagens ao usuário: **pt-BR**. Commits: Conventional Commits em **inglês**.

## Sobre e histórico (obrigatório)

Toda mudança visível ao usuário deve atualizar a aba **Mais** (`AboutScreen.kt`) e `docs/release-history.json` (+ `app/src/main/assets/release-history.json`). Ver `.cursor/rules/update-about-on-change.mdc`.
