# Enviar para o celular (Downloads)

Gera o APK **debug** atualizado, valida a versão, copia para **Downloads do PC** e **tenta enviar para a pasta Download do celular** via USB/adb.

## Quando usar

- Usuário pediu `/enviar-celular`, "manda pro celular", "upload no meu celular" ou equivalente.

## Pré-requisito no celular

- Cabo USB conectado
- **Depuração USB** ativa
- Autorizar o computador no prompt do celular (se aparecer)

## Passos (agente)

1. Rodar o script:

```powershell
cd C:\repo\fumei
powershell -NoProfile -ExecutionPolicy Bypass -File scripts\enviar-celular.ps1
```

2. O script deve terminar com:

   - `release-check OK: <versionName> (<versionCode>)`
   - `apk-verify OK: ...` (build e cópia no PC)
   - `phone-push OK: /sdcard/Download/fumei-debug.apk` **se** o celular USB estiver conectado
   - `APK pronto:` com caminhos no PC e no celular (quando aplicável)

3. Conferir artefatos:

```powershell
Get-Item "$env:USERPROFILE\Downloads\fumei-debug.apk", "$env:USERPROFILE\Downloads\fumei-debug.apk.meta.json" | Format-List FullName, Length, LastWriteTime
Get-Content "$env:USERPROFILE\Downloads\fumei-debug.apk.meta.json"
```

4. Informar ao usuário:

   - **Celular:** Arquivos → Download → `fumei-debug.apk` (se `phoneDownloadPath` no meta não for null)
   - **PC (fallback):** `%USERPROFILE%\Downloads\fumei-debug.apk`
   - Versão esperada: **Mais → Sobre** = mesma `versionName` do `.meta.json`

## Como a verificação funciona

| Etapa | O que valida |
|-------|----------------|
| `scripts/release-check.ps1` | `docs/release-history.json` alinhado com `app/build.gradle.kts` |
| `scripts/verify-apk.ps1` (build) | `aapt dump badging`: `applicationId`, `versionCode`, `versionName` = Gradle |
| Cópia PC + `verify-apk -RequireCopyMatch` | SHA256 do APK em Downloads do PC = build |
| `adb push` + tamanho remoto | APK no celular com mesmo tamanho em bytes que o local |
| `fumei-debug.apk.meta.json` | Versão, hash, `phoneSerial`, `phoneDownloadPath` |

## Dispositivos

- **adb push** quando `adb devices` lista celular físico (serial sem `emulator-`)
- **MTP (Explorer)** quando o celular aparece em Este Computador mas adb não (ex.: S24 só em modo arquivos)
- Ignora emulador para o push
- Vários celulares: definir `$env:ANDROID_SERIAL` antes do script

## Instalação no celular

1. Abra **Arquivos → Download → fumei-debug.apk**
2. Toque em instalar/atualizar
3. Confira **Mais → Sobre → Versão**

**"Atualizar" não funciona?** O APK debug usa assinatura diferente da Play Store. **Desinstale o Fumei** e instale o `fumei-debug.apk` de novo (isso apaga os registros). Se a assinatura for a mesma, atualizar por cima **mantém** o banco.

**Banco de dados:** o APK não traz registros de exemplo. Instalação nova começa vazia.

Verificação manual:

```powershell
$adb = "$env:LOCALAPPDATA\Android\Sdk\platform-tools\adb.exe"
& $adb devices -l
& $adb -d shell ls -l /sdcard/Download/fumei-debug.apk
& $adb -d shell dumpsys package fumei.faruk.dev.br | Select-String "versionCode|versionName"
```

## Opcional: instalar direto (sem abrir Download)

```powershell
$adb = "$env:LOCALAPPDATA\Android\Sdk\platform-tools\adb.exe"
& $adb -d install -r "$env:USERPROFILE\Downloads\fumei-debug.apk"
& $adb -d shell am start -n fumei.faruk.dev.br/fumei.faruk.dev.br.MainActivity
```

## Artefatos

| Item | Caminho |
|------|---------|
| Build | `app/build/outputs/apk/debug/app-debug.apk` |
| PC | `%USERPROFILE%\Downloads\fumei-debug.apk` |
| Celular | `/sdcard/Download/fumei-debug.apk` |
| Meta | `%USERPROFILE%\Downloads\fumei-debug.apk.meta.json` |
| Scripts | `scripts/enviar-celular.ps1`, `scripts/verify-apk.ps1` |

## Falhas comuns

- **APK não aparece no celular:** USB em modo arquivos/MTP; o script tenta MTP via Explorer se adb falhar.
- **Atualizar não instala:** desinstalar app antigo (assinatura diferente) e instalar o APK debug de novo.
- **Versão errada no celular:** instalar o `fumei-debug.apk` novo da pasta Download do celular.
- **apk-verify failed:** `.\gradlew clean assembleDebug` e repetir.
