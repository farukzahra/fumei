package fumei.faruk.dev.br.ui

import androidx.compose.material3.AlertDialog
import androidx.compose.material3.Text
import androidx.compose.material3.TextButton
import androidx.compose.runtime.Composable
import androidx.compose.ui.Modifier
import androidx.compose.ui.platform.testTag

@Composable
fun UpdateAvailableDialog(
    onUpdate: () -> Unit,
    onLater: () -> Unit,
) {
    AlertDialog(
        modifier = Modifier.testTag("update_dialog"),
        onDismissRequest = onLater,
        title = { Text("Tem atualização disponível") },
        text = { Text("Baixa em segundo plano, sem sair do app. Depois você reinicia quando quiser.") },
        confirmButton = {
            TextButton(
                onClick = onUpdate,
                modifier = Modifier.testTag("update_dialog_update_button"),
            ) {
                Text("Atualizar")
            }
        },
        dismissButton = {
            TextButton(
                onClick = onLater,
                modifier = Modifier.testTag("update_dialog_later_button"),
            ) {
                Text("Agora não")
            }
        },
    )
}

@Composable
fun RestartReadyDialog(
    onRestart: () -> Unit,
    onLater: () -> Unit,
) {
    AlertDialog(
        modifier = Modifier.testTag("restart_dialog"),
        onDismissRequest = onLater,
        title = { Text("Atualização pronta") },
        text = { Text("O download terminou. Reinicie o app para usar a versão nova.") },
        confirmButton = {
            TextButton(
                onClick = onRestart,
                modifier = Modifier.testTag("restart_dialog_restart_button"),
            ) {
                Text("Reiniciar")
            }
        },
        dismissButton = {
            TextButton(
                onClick = onLater,
                modifier = Modifier.testTag("restart_dialog_later_button"),
            ) {
                Text("Depois")
            }
        },
    )
}
