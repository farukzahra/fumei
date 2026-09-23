# Símbolos nativos (aviso da Play Console)

## O aviso

```
Este App Bundle contém código nativo, e você não fez upload dos símbolos de depuração.
```

**É recomendação, não bloqueio.** O app publica normalmente.

## Por que aparece no Fumei

O Jetpack Compose puxa `libandroidx.graphics.path.so` (biblioteca `androidx.graphics:graphics-path`). O `.so` vem **sem tabela de símbolos** do Maven; o AGP não consegue gerar metadata de debug para a Play.

Issue conhecida: [Google Issue Tracker #356109544](https://issuetracker.google.com/issues/356109544)

## O que já está configurado no projeto

Em `app/build.gradle.kts`:

- `ndkVersion = "28.2.13676358"` (NDK instalado no SDK; sem isso o strip/símbolos falham silenciosamente)
- `ndk { debugSymbolLevel = "FULL" }` no release (símbolos embutidos no AAB quando a lib tiver metadata)

Requisito local: pasta `$ANDROID_SDK/ndk/28.2.13676358` (Android Studio → SDK Manager → NDK).

## O que fazer na Play Console

1. **Pode ignorar** e publicar se não houver crash nativo seu (só lib do AndroidX).
2. Não há `native-debug-symbols.zip` útil para subir manualmente neste app hoje.
3. Quando o AndroidX publicar `.so` com símbolos, um rebuild já deve limpar o aviso.

## Verificar no build

```powershell
.\gradlew :app:mergeReleaseNativeDebugMetadata --info
```

Se aparecer `Unable to extract native debug metadata ... already been stripped`, é o caso do `graphics-path`.
