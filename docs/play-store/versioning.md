# Versionamento — Fumei (Play Store)

Dois números independentes. Não misturar.

## versionCode (inteiro, Play Console)

- Só serve para a **Google Play** ordenar uploads.
- **Nunca reutilizar.** A Play consome o código no upload, mesmo se você apagar o bundle.
- Histórico interno de uploads começou em **1** (dev); códigos **8–9** consumidos em testes.
- **Lançamento público 1.0.0 = versionCode 10.**
- Próximo upload: **11**, depois **12**, sempre +1.

Registro: `version-codes.json`

## versionName (semver, usuário e loja)

- O que aparece em **Mais → Sobre** e no campo **Nome da versão** da Play.
- **1.0.0** é o primeiro lançamento público (pareado com versionCode **10**).
- Próximas mudanças (combinar com o usuário antes de bump):

| Tipo | Exemplo | Quando |
|------|---------|--------|
| Correção | 1.0.0 → **1.0.1** | bugfix, ajuste pequeno |
| Feature | 1.0.x → **1.1.0** | funcionalidade nova |
| Major | 1.x.x → **2.0.0** | mudança grande / quebra de expectativa |

- `versionCode` sobe **sempre** +1 em todo upload novo, independente do tipo de `versionName`.

## Checklist ao gerar release

1. Definir `versionName` (semver) com o usuário se não for óbvio.
2. `versionCode` = `playStoreMaxVersionCode + 1` em `version-codes.json`.
3. Alinhar `app/build.gradle.kts`, `release-history.json`, `version-codes.json`.
4. `powershell -File scripts/play-release.ps1`
5. Entregar AAB + nome + notas em blocos copiáveis (`copy-paste-fields.mdc`).

## Estado atual

| versionCode | versionName | Status |
|-------------|-------------|--------|
| **11** | **1.0.0** | atual — lançamento Play |

Próximo par previsto: **12** + semver a definir (ex. 1.0.1 ou 1.1.0).
