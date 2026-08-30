# Enviar para o celular (Downloads)

Gera o APK **debug** atualizado, **valida a versão**, copia para **Downloads** e confirma que o arquivo transferido é idêntico ao build.

## Quando usar

- Usuário pediu `/enviar-celular`, "manda pro celular", "upload no meu celular" ou equivalente.
- **Não** exige USB/adb: o fluxo padrão é build + verificação + cópia para Downloads.

## Passos (agente)

1. Rodar o script (release-check + build + verify + cópia + verify):

```powershell
cd C:\repo\fumei
powershell -NoProfile -ExecutionPolicy Bypass -File scripts\enviar-celular.ps1
```

2. O script deve terminar com:
   - `release-check OK: <versionName> (<versionCode>)`
   - `apk-verify OK: <versionName> (<versionCode>) fumei.faruk.dev.br` (duas vezes: build e cópia)
   - `APK pronto para o celular:` com caminho e versão

3. Conferir artefatos:

```powershell
Get-Item "$env:USERPROFILE\Downloads\fumei-debug.apk", "$env:USERPROFILE\Downloads\fumei-debug.apk.meta.json" | Format-List FullName, Length, LastWriteTime
Get-Content "$env:USERPROFILE\Downloads\fumei-debug.apk.meta.json"
```

4. Informar ao usuário:
   - Caminho: `%USERPROFILE%\Downloads\fumei-debug.apk`
   - Versão esperada no app: **Mais → Sobre** deve mostrar a mesma `versionName` / build do `.meta.json`
   - Instalar no celular (WhatsApp, cabo, nuvem, etc.)

## Como a verificação funciona

| Etapa | O que valida |
|-------|----------------|
| `scripts/release-check.ps1` | `docs/release-history.json` alinhado com `app/build.gradle.kts` |
| `scripts/verify-apk.ps1` (build) | `aapt dump badging` no APK: `applicationId`, `versionCode`, `versionName` = Gradle |
| Cópia + `verify-apk -RequireCopyMatch` | SHA256 do APK em Downloads = SHA256 do build em `app/build/outputs/...` |
| `fumei-debug.apk.meta.json` | Registro da versão e hash para auditoria |

Verificação manual extra (opcional):

```powershell
$adb = "$env:LOCALAPPDATA\Android\Sdk\platform-tools\adb.exe"
& powershell -File scripts\verify-apk.ps1 -ApkPath "$env:USERPROFILE\Downloads\fumei-debug.apk"
& $adb -d shell dumpsys package fumei.faruk.dev.br | Select-String "versionCode|versionName"
```

No celular, sem USB: **Mais → Sobre → Versão** deve bater com o `versionName` do meta.

## Opcional: instalar via USB

Se `adb devices` listar o celular (não só emulador), após o script:

```powershell
$adb = "$env:LOCALAPPDATA\Android\Sdk\platform-tools\adb.exe"
& $adb -d install -r "$env:USERPROFILE\Downloads\fumei-debug.apk"
& $adb -d shell am start -n fumei.faruk.dev.br/fumei.faruk.dev.br.MainActivity
```

## Artefatos

| Item | Caminho |
|------|---------|
| Build | `app/build/outputs/apk/debug/app-debug.apk` |
| Celular | `%USERPROFILE%\Downloads\fumei-debug.apk` |
| Meta | `%USERPROFILE%\Downloads\fumei-debug.apk.meta.json` |
| Scripts | `scripts/enviar-celular.ps1`, `scripts/verify-apk.ps1` |

## Falhas comuns

- **Versão errada no celular:** APK antigo em Downloads ou instalação não concluída. Rodar `/enviar-celular` de novo e reinstalar.
- **apk-verify failed:** Gradle e APK dessincronizados; rodar `.\gradlew clean assembleDebug` e repetir.
- **copy mismatch:** arquivo em Downloads truncado; apagar `fumei-debug.apk` e rodar o script novamente.
