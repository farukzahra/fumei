# Fale conosco — cartão de sugestão no Sobre

Data: 16/11/2026. Status: **proposto, aguardando implementação neste repo**.
Origem: pedido do usuário em 16/11/2026, já implementado no irmão
`muslim-pro-plus` (Android/Compose). Este documento é autocontido: o agente do
`fumei` não precisa abrir o outro repositório.

## O que muda para quem usa

Em **Mais → Sobre**, logo abaixo do cartão **Apoie o projeto**, entra um cartão
**Fale conosco**. Tocar abre um diálogo com um campo de texto livre onde a
pessoa escreve sugestão, reclamação ou ideia. **Enviar** abre o app de e-mail do
aparelho com destinatário, assunto e corpo já preenchidos; a pessoa revisa e
envia por lá.

## Por que sem backend

O Fumei não tem conta, servidor nem analytics. Criar endpoint para receber
mensagem exigiria hospedagem, LGPD e chave de API, sem ganho sobre o e-mail.
O contato do projeto é o mesmo já usado no Muslim Plus:
`farukz@gmail.com`.

## Decisões

1. **Sem backend, sem conta.** `ACTION_SENDTO` + `mailto:` codificado. O app
   nunca envia nada sozinho.
2. **Texto simples.** `OutlinedTextField` multilinha (4 a 8 linhas visíveis),
   sem rich text, markdown ou anexos. Rich text em Compose exigiria biblioteca
   externa e conversão para HTML; e-mail de sugestão não precisa.
3. **Limite de 2.000 caracteres** com contador visível (`0/2000`).
4. **Mínimo de 3 caracteres** (depois de `trim`) para habilitar **Enviar**.
5. **Dados técnicos automáticos e avisados.** O corpo leva uma assinatura com
   `Fumei <versão> (<versionCode>)`, `Android <versão> · <modelo>` e o idioma.
   O diálogo avisa que isso vai junto.
6. **Fallback sem app de e-mail.** Consultar `resolveActivity` antes de abrir;
   sem cliente, o diálogo continua aberto, mostra o endereço e oferece
   **Copiar e-mail**. Nada de `ActivityNotFoundException`.
7. **`<queries>` no manifesto.** Sem a consulta de `SENDTO`/`mailto`, o Android
   11+ esconde os clientes de e-mail do `resolveActivity` e o fallback
   apareceria sempre. Isso é obrigatório para a decisão 6 funcionar.
8. **Card no Sobre, não no hub do Mais**, para ficar ao lado de Apoie o projeto,
   como o usuário pediu. Sem rota nova, sem mudança de navegação.
9. **pt-BR hardcoded**, como o resto do `AboutScreen`. O `strings.xml` do fumei
   só tem `app_name`; não abrir frente de i18n por causa desta feature.

## Arquivos

| Arquivo | Ação |
| --- | --- |
| `app/src/main/java/fumei/faruk/dev/br/ui/ContactMessage.kt` | novo, regras puras (sem Android) |
| `app/src/main/java/fumei/faruk/dev/br/ui/ContactDialog.kt` | novo, diálogo + intent |
| `app/src/main/java/fumei/faruk/dev/br/ui/AboutScreen.kt` | cartão novo entre `SupportSection` e `ReleaseHistorySection` |
| `app/src/main/AndroidManifest.xml` | bloco `<queries>` de `SENDTO`/`mailto` |
| `app/src/test/java/fumei/faruk/dev/br/ui/ContactMessageTest.kt` | novo, JVM |
| `app/src/androidTest/java/fumei/faruk/dev/br/ui/ContactDialogE2ETest.kt` | novo, Compose instrumentado |

## Contrato do `ContactMessage` (copiar como está, ajustando só o pacote)

```kotlin
package fumei.faruk.dev.br.ui

import java.nio.charset.StandardCharsets

object ContactMessage {
    const val MAX_LENGTH = 2000
    const val MIN_LENGTH = 3
    const val DETAILS_SEPARATOR = "\n\n-- \n"

    private const val HEX = "0123456789ABCDEF"

    fun normalize(raw: String): String =
        raw.replace("\r\n", "\n").replace('\r', '\n').trim()

    fun isValid(raw: String): Boolean = normalize(raw).length >= MIN_LENGTH

    fun limit(raw: String): String = raw.take(MAX_LENGTH)

    fun body(message: String, header: List<String> = emptyList()): String {
        val text = normalize(message)
        val details = header.map { it.trim() }.filter { it.isNotEmpty() }.joinToString("\n")
        return if (details.isEmpty()) text else text + DETAILS_SEPARATOR + details
    }

    fun mailto(email: String, subject: String, body: String): String =
        "mailto:${email.trim()}?subject=${encode(subject)}&body=${encode(body)}"

    fun encode(value: String): String {
        val out = StringBuilder()
        value.toByteArray(StandardCharsets.UTF_8).forEach { byte ->
            val code = byte.toInt() and 0xFF
            if (code < 0x80 && isUnreserved(code.toChar())) {
                out.append(code.toChar())
            } else {
                out.append('%')
                out.append(HEX[code shr 4])
                out.append(HEX[code and 0x0F])
            }
        }
        return out.toString()
    }

    private fun isUnreserved(char: Char): Boolean =
        char in 'A'..'Z' || char in 'a'..'z' || char in '0'..'9' || char in "-._~"
}
```

Regras que os testes precisam cobrir:

- `normalize` apara espaço e converte `\r\n`/`\r` em `\n`.
- `isValid` falso para `""`, `"  a  "`; verdadeiro para `"oi"`? **Não**:
  `"oi"` tem 2 caracteres, então falso; `"oi!"` é verdadeiro.
- `body("oi", emptyList()) == "oi"` (sem separador solto).
- `body("oi", listOf("Fumei 1.4.0 (24)", "Android 14 · Pixel 6", "pt-BR"))`
  termina com `"\n\n-- \nFumei 1.4.0 (24)\nAndroid 14 · Pixel 6\npt-BR"`.
- `mailto("a@b.c", "Oi ção", "linha 1\nlinha 2")` ==
  `"mailto:a@b.c?subject=Oi%20%C3%A7%C3%A3o&body=linha%201%0Alinha%202"`.
- Espaço vira `%20`, nunca `+`.

## Intent e envio

```kotlin
const val CONTACT_EMAIL = "farukz@gmail.com"   // no topo de ContactDialog.kt

fun contactEmailIntent(subject: String, body: String, email: String = CONTACT_EMAIL): Intent =
    Intent(Intent.ACTION_SENDTO, ContactMessage.mailto(email, subject, body).toUri())

fun contactDetails(
    appName: String,
    versionName: String,
    versionCode: Int,
    device: String = Build.MODEL,
    android: String = Build.VERSION.RELEASE,
    language: String = Locale.getDefault().toLanguageTag(),
): List<String> = listOf(
    "$appName $versionName ($versionCode)",
    "Android $android · $device",
    language,
)

fun sendContactEmail(context: Context, message: String): Boolean {
    val subject = "Fumei " + BuildConfig.VERSION_NAME + " · sugestão"
    val details = contactDetails("Fumei", BuildConfig.VERSION_NAME, BuildConfig.VERSION_CODE)
    val intent = contactEmailIntent(subject, ContactMessage.body(message, details))
    if (intent.resolveActivity(context.packageManager) == null) return false
    val chooser = Intent.createChooser(intent, "Enviar e-mail")
        .addFlags(Intent.FLAG_ACTIVITY_NEW_TASK)
    return runCatching { context.startActivity(chooser) }.isSuccess
}
```

`BuildConfig.VERSION_NAME`/`VERSION_CODE` já existem (`buildConfig = true` e
`MainActivity` já usa).

## Manifesto

Dentro de `<manifest>`, antes de `<application>`:

```xml
<queries>
    <intent>
        <action android:name="android.intent.action.SENDTO" />
        <data android:scheme="mailto" />
    </intent>
</queries>
```

## Diálogo

`ContactDialog(onDismiss: () -> Unit, onSend: ((String) -> Boolean)? = null)`.

- `onSend` nulo usa `sendContactEmail(context, texto)`; parâmetro existe para o
  teste não abrir app externo.
- Estado: `text` e `failed` em `rememberSaveable`.
- Estrutura: `AlertDialog` com ícone `Icons.Outlined.MailOutline`, título
  **Fale conosco**, `Column` com `verticalScroll` contendo:
  - texto de apoio: "Escreva sua sugestão, reclamação ou ideia. A mensagem abre
    no seu app de e-mail para você revisar antes de enviar.";
  - `OutlinedTextField` com `minLines = 4`, `maxLines = 7`,
    placeholder "Sua mensagem", `onValueChange` aplicando
    `ContactMessage.limit(it)` e limpando `failed`; `supportingText` com
    `"${text.length}/2000"`;
  - aviso: "Vão junto o nome e a versão do app, a versão do Android e o modelo
    do aparelho.";
  - quando `failed`: texto "Nenhum app de e-mail encontrado. Copie o endereço e
    escreva de onde quiser." + `farukz@gmail.com` + botão **Copiar e-mail**
    (`LocalClipboardManager` + `Toast` "E-mail copiado").
- `confirmButton`: **Enviar**, `enabled = ContactMessage.isValid(text)`,
  `onClick = { if (send(text)) onDismiss() else failed = true }`.
- `dismissButton`: **Cancelar**, fecha sem enviar.
- Cores no padrão do app: `AppColors.Ember500` no ícone/botões,
  `AppColors.Smoke400` no texto de apoio, `AppColors.Paper100` no campo.
  Tipografia em `FumeiType.body`.

### TestTags (usar exatamente estes)

`contact_dialog`, `contact_field`, `contact_counter`, `contact_send`,
`contact_cancel`, `contact_failed`, `contact_email`, `contact_copy_email`,
`about_contact_card`.

## Cartão no Sobre

Em `AboutScreen`, dentro do `LazyColumn`, entre `SupportSection` e
`ReleaseHistorySection`:

```kotlin
item {
    ContactSection(onOpen = { showContact = true })
}
```

`ContactSection` usa `AboutSectionCard(title = "Fale conosco")` com
`Modifier.testTag("about_contact_card").clickable(onClick = onOpen)`, texto de
apoio "Sugestão, reclamação ou ideia. Abre no seu app de e-mail." e ícone
`Icons.Outlined.MailOutline` em `AppColors.Ember500`.

Estado `var showContact by rememberSaveable { mutableStateOf(false) }` em
`AboutScreen` e, no fim da composição, `if (showContact) ContactDialog(...)`.

## Testes

JVM (`app/src/test/`):

- `ContactMessageTest` com os casos listados acima.

Instrumentado (`app/src/androidTest/`, `createComposeRule()`):

1. `cardOpensDialogWithSendDisabled` — renderiza `AboutScreen` com
   `AboutUiState(versionName = "1.4.0", versionCode = 24)`, rola até
   `about_contact_card`, clica; `contact_dialog` visível, `contact_counter` com
   texto `0/2000`, `contact_send` desabilitado (`assertIsNotEnabled`).
2. `typingEnablesSendAndCancelCloses` — digita "quero um gráfico semanal",
   `contact_send` habilitado; **Cancelar** fecha e não envia.
3. `sendFailureKeepsDialogAndShowsEmail` — renderiza `ContactDialog(onSend = { false })`
   direto, digita, toca Enviar; diálogo continua, `contact_failed` e
   `contact_email` aparecem, `contact_copy_email` existe.
4. `sendSuccessClosesDialog` — `ContactDialog(onSend = { true })`; digita,
   Enviar, `contact_dialog` deixa de existir. Guardar o texto recebido e
   comparar com o digitado (prova que o callback recebe a mensagem).
5. `intentCarriesMailtoWithEncodedBody` — `contactEmailIntent("Fumei 1.4.0 · sugestão", "oi\nfim")`:
   `action == Intent.ACTION_SENDTO`, `data.scheme == "mailto"`,
   `data.toString()` contém `subject=` e `body=oi%0Afim`.

## Critérios de aceite

- [ ] Cartão **Fale conosco** logo abaixo de **Apoie o projeto**, no Sobre.
- [ ] Enviar desabilitado com menos de 3 caracteres e com campo só de espaços.
- [ ] Contador não passa de `2000/2000`; o campo não aceita o 2001º caractere.
- [ ] `mailto:` com assunto, corpo codificado e assinatura com versão, Android,
      modelo e idioma.
- [ ] Sem app de e-mail, o diálogo mostra o endereço e copia a chave.
- [ ] `gradlew test` e `gradlew connectedDebugAndroidTest` verdes.
- [ ] `gradlew installDebug` e app aberto no emulador Pixel6 (regra do
      `AGENTS.md` do fumei).
- [ ] Sem push, sem publicação: pedido explícito de deixar local.

## Fora de escopo

- Endpoint HTTP, banco de mensagens, resposta automática.
- Anexos, prints, logs.
- Rich text, markdown, emoji picker.
- Tradução para en/ar.
- Mudar a navegação do Mais ou criar tela cheia.
