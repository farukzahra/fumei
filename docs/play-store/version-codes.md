# Version codes — Play Store (Fumei)

Política completa: **`versioning.md`** (versionCode vs versionName).

Fonte canônica de códigos Play: `version-codes.json`.

## Regra rápida

| Campo | Regra |
|-------|--------|
| **versionCode** | Inteiro; sobe +1 a cada upload; nunca reutilizar (Play trava no envio). |
| **versionName** | Semver público; **1.0.0** no code **10**; próximo: 1.0.1 / 1.1.0 / 2.0.0 conforme o tipo de mudança. |

## Histórico Play (versionCode)

| versionCode | versionName (loja) | Data | Status |
|-------------|-------------------|------|--------|
| 1–7 | dev (1.0.0–1.4.2) | 2026-08 | consumidos (builds internos) |
| 8 | 1.4.3 | 2026-09-06 | consumido (upload teste) |
| 9 | 1.4.4 | 2026-09-06 | consumido (upload teste) |
| 10 | 1.0.0 | 2026-09-06 | consumido (upload) |
| **12** | **1.0.0** | **2026-09-06** | **atual — usar este** |

**Próximo versionCode:** 13 (automático no `play-release.ps1`)

## Alinhamento obrigatório

- `app/build.gradle.kts`
- `docs/release-history.json`
- `docs/play-store/version-codes.json` (entrada `playStore: "current"`)

`powershell -File scripts/release-check.ps1`
