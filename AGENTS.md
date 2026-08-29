# Fumei — instruções para agentes

App Android: Kotlin, Jetpack Compose, Material 3, Room, MVVM. Package: `fumei.faruk.dev.br`.

## Superpowers (obrigatório)

Antes de implementar feature ou bugfix:

1. **brainstorming** — se for trabalho criativo/novo comportamento
2. **test-driven-development** — teste falhando primeiro, depois código
3. **verification-before-completion** — rodar comandos e citar evidência antes de dizer que passou

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

## Build e emulador

```bash
.\gradlew assembleDebug installDebug
```

AVD documentado em `docs/emulator.md` (Pixel6, Android 14).

## Commit e push (`/commit-push`)

Ver `.cursor/commands/commit-push.md`. Após push em `main`, validar workflow **Android CI** no GitHub.

## Idioma

UI e mensagens ao usuário: **pt-BR**. Commits: Conventional Commits em **inglês**.
