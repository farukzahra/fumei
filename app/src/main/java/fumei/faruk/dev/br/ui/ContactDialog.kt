package fumei.faruk.dev.br.ui

import android.content.Context
import android.content.Intent
import android.os.Build
import android.widget.Toast
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.rememberScrollState
import androidx.compose.foundation.verticalScroll
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.outlined.MailOutline
import androidx.compose.material3.AlertDialog
import androidx.compose.material3.Icon
import androidx.compose.material3.OutlinedTextField
import androidx.compose.material3.OutlinedTextFieldDefaults
import androidx.compose.material3.Text
import androidx.compose.material3.TextButton
import androidx.compose.runtime.Composable
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.rememberCoroutineScope
import androidx.compose.runtime.saveable.rememberSaveable
import androidx.compose.runtime.setValue
import androidx.compose.ui.Modifier
import androidx.compose.ui.platform.LocalClipboardManager
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.platform.testTag
import androidx.compose.ui.text.AnnotatedString
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import androidx.core.net.toUri
import fumei.faruk.dev.br.BuildConfig
import fumei.faruk.dev.br.ui.theme.AppColors
import fumei.faruk.dev.br.ui.theme.FumeiType
import kotlinx.coroutines.launch
import java.util.Locale

const val CONTACT_EMAIL = "farukz@gmail.com"

enum class ContactSendState { IDLE, SENDING, FAILED }

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

@Composable
fun ContactDialog(
    onDismiss: () -> Unit,
    onSend: ContactSender? = null,
    onEmailFallback: ((String) -> Boolean)? = null,
) {
    val context = LocalContext.current
    val clipboard = LocalClipboardManager.current
    val scope = rememberCoroutineScope()
    val sender = onSend ?: remember { Web3FormsSender() }
    val fallback: (String) -> Boolean = onEmailFallback ?: { message -> sendContactEmail(context, message) }
    var text by rememberSaveable { mutableStateOf("") }
    var state by rememberSaveable { mutableStateOf(ContactSendState.IDLE) }

    val sending = state == ContactSendState.SENDING
    val canSend = ContactMessage.isValid(text) && !sending

    AlertDialog(
        onDismissRequest = onDismiss,
        modifier = Modifier.testTag("contact_dialog"),
        icon = {
            Icon(
                imageVector = Icons.Outlined.MailOutline,
                contentDescription = null,
                tint = AppColors.Ember500,
            )
        },
        title = {
            Text(
                text = "Fale conosco",
                style = FumeiType.displayLabel,
                color = AppColors.Paper100,
            )
        },
        text = {
            Column(
                modifier = Modifier.verticalScroll(rememberScrollState()),
                verticalArrangement = Arrangement.spacedBy(10.dp),
            ) {
                Text(
                    text = "Escreva sua sugestão, reclamação ou ideia. A mensagem abre no seu app de e-mail para você revisar antes de enviar.",
                    style = FumeiType.body.copy(fontSize = 14.sp),
                    color = AppColors.Smoke400,
                )
                OutlinedTextField(
                    value = text,
                    onValueChange = {
                        text = ContactMessage.limit(it)
                        if (state == ContactSendState.FAILED) state = ContactSendState.IDLE
                    },
                    modifier = Modifier
                        .fillMaxWidth()
                        .testTag("contact_field"),
                    enabled = !sending,
                    placeholder = {
                        Text(
                            text = "Sua mensagem",
                            style = FumeiType.body,
                            color = AppColors.Smoke400,
                        )
                    },
                    supportingText = {
                        Text(
                            text = "${text.length}/${ContactMessage.MAX_LENGTH}",
                            modifier = Modifier.testTag("contact_counter"),
                            style = FumeiType.body.copy(fontSize = 12.sp),
                            color = AppColors.Smoke400,
                        )
                    },
                    minLines = 4,
                    maxLines = 7,
                    textStyle = FumeiType.body,
                    colors = OutlinedTextFieldDefaults.colors(
                        focusedTextColor = AppColors.Paper100,
                        unfocusedTextColor = AppColors.Paper100,
                        cursorColor = AppColors.Ember500,
                        focusedBorderColor = AppColors.Ember500,
                        unfocusedBorderColor = AppColors.Smoke400,
                    ),
                )
                Text(
                    text = "Vão junto o nome e a versão do app, a versão do Android e o modelo do aparelho.",
                    style = FumeiType.body.copy(fontSize = 12.sp),
                    color = AppColors.Smoke400,
                )
                if (state == ContactSendState.FAILED) {
                    Column(verticalArrangement = Arrangement.spacedBy(4.dp)) {
                        Text(
                            text = "Não deu para enviar agora. Verifique a conexão e tente de novo.",
                            modifier = Modifier.testTag("contact_failed"),
                            style = FumeiType.body.copy(fontSize = 13.sp),
                            color = AppColors.Ember300,
                        )
                        Text(
                            text = CONTACT_EMAIL,
                            modifier = Modifier.testTag("contact_email"),
                            style = FumeiType.timestamp.copy(fontSize = 14.sp),
                            color = AppColors.Paper100,
                        )
                        TextButton(
                            onClick = { state = ContactSendState.IDLE },
                            modifier = Modifier.testTag("contact_retry"),
                        ) {
                            Text(
                                text = "Tentar de novo",
                                style = FumeiType.button,
                                color = AppColors.Ember500,
                            )
                        }
                        TextButton(
                            onClick = {
                                if (fallback(text)) onDismiss()
                            },
                            modifier = Modifier.testTag("contact_use_email"),
                        ) {
                            Text(
                                text = "Usar app de e-mail",
                                style = FumeiType.button,
                                color = AppColors.Ember500,
                            )
                        }
                        TextButton(
                            onClick = {
                                clipboard.setText(AnnotatedString(CONTACT_EMAIL))
                                Toast.makeText(context, "E-mail copiado", Toast.LENGTH_SHORT).show()
                            },
                            modifier = Modifier.testTag("contact_copy_email"),
                        ) {
                            Text(
                                text = "Copiar e-mail",
                                style = FumeiType.button,
                                color = AppColors.Ember500,
                            )
                        }
                    }
                }
            }
        },
        confirmButton = {
            TextButton(
                onClick = {
                    scope.launch {
                        state = ContactSendState.SENDING
                        val sent = runCatching { sender.send(text) }.getOrDefault(false)
                        if (sent) {
                            Toast.makeText(context, "Mensagem enviada. Obrigado!", Toast.LENGTH_SHORT).show()
                            onDismiss()
                        } else {
                            state = ContactSendState.FAILED
                        }
                    }
                },
                enabled = canSend,
                modifier = Modifier.testTag("contact_send"),
            ) {
                Text(
                    text = if (sending) "Enviando…" else "Enviar",
                    style = FumeiType.button,
                    color = if (canSend) AppColors.Ember500 else AppColors.Smoke400,
                )
            }
        },
        dismissButton = {
            TextButton(
                onClick = onDismiss,
                enabled = !sending,
                modifier = Modifier.testTag("contact_cancel"),
            ) {
                Text(
                    text = "Cancelar",
                    style = FumeiType.button,
                    color = AppColors.Ember500,
                )
            }
        },
    )
}
