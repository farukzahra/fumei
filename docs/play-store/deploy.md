# Deploy — Fumei (Play Store produção)

## Gerar versão (obrigatório antes de subir na Play)

```powershell
cd C:\repo\fumei
powershell -File scripts/play-release.ps1
```

O script valida versão, roda testes, gera o AAB e imprime os três campos da Play Console:

| Campo Play Console | Fonte |
|---|---|
| Arquivo (AAB) | `app/build/outputs/bundle/release/app-release.aab` |
| Nome da versão * | `versionName` em `app/build.gradle.kts` |
| Notas da versão | `docs/play-store/release-notes-pt-BR.txt` |

Manifesto salvo em `docs/play-store/last-release.json`.

Release preparado: **1.1.0** (versionCode **17**). Próximo code: **18**.

## Artefatos

| Arquivo | Caminho |
|---------|---------|
| AAB (produção) | `app/build/outputs/bundle/release/app-release.aab` |
| APK (instalação direta) | `app/build/outputs/apk/release/app-release.apk` |

## Instalar no celular (USB)

1. No celular: **Opções do desenvolvedor** → **Depuração USB** ativada
2. Conectar cabo USB e escolher modo **Transferência de arquivos** (não só carregar)
3. Aceitar o popup **“Permitir depuração USB?”**
4. Verificar:

```powershell
$adb = "$env:LOCALAPPDATA\Android\Sdk\platform-tools\adb.exe"
& $adb devices -l
```

Deve aparecer uma linha com o modelo do celular (não só `emulator-5554`).

5. Instalar release assinado:

```powershell
cd C:\repo\fumei
.\gradlew installRelease
# ou
& $adb install -r app\build\outputs\apk\release\app-release.apk
```

## Lançamento atual em produção

Versão **1.1.0**, versionCode **17**, publicada na faixa **Produção**.

Notas publicadas: `docs/play-store/release-notes-pt-BR.txt`.

Para preparar a próxima versão, use `powershell -File scripts/play-release.ps1`.
O comando incrementa o `versionCode`, valida os arquivos de release, roda os
testes e gera o AAB. O próximo código é **18**.

O envio à Play Console usa a Android Publisher API com a service account local
em `..\secrets\google-play\service-account.json`. Nunca adicione essa chave ao
repositório.
