# Fumei — Testar no emulador

## Emulador disponível

| Item | Valor |
|------|-------|
| AVD | `Pixel6` (Android 14) |
| Dispositivo | `emulator-5554` |

## Subir emulador + instalar app

```powershell
$env:JAVA_HOME = "C:\Program Files\Android\Android Studio\jbr"
$env:ANDROID_HOME = "$env:LOCALAPPDATA\Android\Sdk"

# Iniciar emulador (se não estiver rodando)
Start-Process "$env:ANDROID_HOME\emulator\emulator.exe" -ArgumentList "-avd","Pixel6"

# Aguardar boot e instalar
cd C:\repo\fumei
.\gradlew.bat installDebug

# Abrir o app
& "$env:ANDROID_HOME\platform-tools\adb.exe" shell am start -n fumei.faruk.dev.br/.MainActivity
```

## APK debug (instalar no celular físico)

**Fluxo padrão:** comando `/enviar-celular` ou:

```powershell
cd C:\repo\fumei
powershell -NoProfile -ExecutionPolicy Bypass -File scripts\enviar-celular.ps1
```

Copia para `%USERPROFILE%\Downloads\fumei-debug.apk` — abra no celular e instale.

Build direto (sem cópia):

```
C:\repo\fumei\app\build\outputs\apk\debug\app-debug.apk
```

## Android Studio (alternativa)

1. Abrir pasta `C:\repo\fumei` no Android Studio
2. Menu **Device Manager** → iniciar **Pixel6**
3. Botão **Run** (▶) ou `Shift+F10`
