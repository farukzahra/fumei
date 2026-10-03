package fumei.faruk.dev.br.update

enum class UpdateAvailability {
    Unknown,
    None,
    Available,
    Downloading,
    Downloaded,
}

data class UpdateSignals(
    val availability: UpdateAvailability = UpdateAvailability.Unknown,
    val bytesDownloaded: Long = 0,
    val totalBytes: Long = 0,
)

enum class UpdateDialogKind {
    Available,
    ReadyToRestart,
}

enum class UpdateCardState {
    Hidden,
    UpToDate,
    Available,
    Downloading,
    ReadyToInstall,
}

object UpdatePromptPolicy {
    fun dialogFor(
        signals: UpdateSignals,
        promptDismissed: Boolean,
        restartPostponed: Boolean,
    ): UpdateDialogKind? = when {
        signals.availability == UpdateAvailability.Downloaded && !restartPostponed ->
            UpdateDialogKind.ReadyToRestart

        signals.availability == UpdateAvailability.Available && !promptDismissed ->
            UpdateDialogKind.Available

        else -> null
    }

    fun cardFor(signals: UpdateSignals): UpdateCardState = when (signals.availability) {
        UpdateAvailability.Unknown -> UpdateCardState.Hidden
        UpdateAvailability.None -> UpdateCardState.UpToDate
        UpdateAvailability.Available -> UpdateCardState.Available
        UpdateAvailability.Downloading -> UpdateCardState.Downloading
        UpdateAvailability.Downloaded -> UpdateCardState.ReadyToInstall
    }

    fun clearsDismissal(signals: UpdateSignals): Boolean =
        signals.availability == UpdateAvailability.None

    fun downloadPercent(signals: UpdateSignals): Int? {
        if (signals.totalBytes <= 0L) return null
        val percent = signals.bytesDownloaded * 100 / signals.totalBytes
        return percent.coerceIn(0L, 100L).toInt()
    }
}

data class UpdateCardUi(
    val statusLabel: String,
    val actionLabel: String?,
    val action: UpdateCardAction?,
)

enum class UpdateCardAction {
    StartUpdate,
    Restart,
}

object UpdateCardLabels {
    fun of(signals: UpdateSignals): UpdateCardUi? = when (UpdatePromptPolicy.cardFor(signals)) {
        UpdateCardState.Hidden -> null
        UpdateCardState.UpToDate -> UpdateCardUi("Você está na versão mais recente", null, null)
        UpdateCardState.Available ->
            UpdateCardUi("Versão nova disponível", "Atualizar", UpdateCardAction.StartUpdate)

        UpdateCardState.Downloading -> {
            val percent = UpdatePromptPolicy.downloadPercent(signals)
            UpdateCardUi(if (percent == null) "Baixando" else "Baixando $percent%", null, null)
        }

        UpdateCardState.ReadyToInstall ->
            UpdateCardUi("Pronta para instalar", "Reiniciar", UpdateCardAction.Restart)
    }
}
