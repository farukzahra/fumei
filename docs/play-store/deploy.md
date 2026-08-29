# Deploy — Fumei (Play Store produção)

## Artefatos (v1.1.0 / versionCode 2)

| Arquivo | Caminho |
|---------|---------|
| AAB (produção) | `app/build/outputs/bundle/release/app-release.aab` |
| APK (instalação direta) | `app/build/outputs/apk/release/app-release.apk` |

Gerar de novo:

```powershell
cd C:\repo\fumei
.\gradlew test connectedDebugAndroidTest bundleRelease assembleRelease
```

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

## Publicar em produção (Play Console)

**Notas da versão (pt-BR):** `docs/play-store/release-notes-pt-BR.txt`

1. Abrir [Play Console](https://play.google.com/console) → app **Quantos fumei**
2. **Testar e lançar** → **Produção** → **Criar nova versão**
3. Upload do AAB: `app-release.aab` (versionCode **2**)
4. Colar notas da versão
5. **Revisar versão** → **Iniciar lançamento para produção**

### Automação futura

Salvar `service-account.json` em `C:\repo\secrets\google-play\` e configurar MCP conforme `docs/play-store/mcp-setup.md` para upload via API.
