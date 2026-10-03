package fumei.faruk.dev.br.update

import org.junit.Assert.assertEquals
import org.junit.Assert.assertNull
import org.junit.Test

class UpdatePromptPolicyTest {
    @Test
    fun dialogFor_returnsNullWhenPlayReportsNoUpdate() {
        val signals = UpdateSignals(UpdateAvailability.None, 0, 0)

        assertNull(UpdatePromptPolicy.dialogFor(signals, promptDismissed = false, restartPostponed = false))
    }

    @Test
    fun dialogFor_returnsNullWhilePlayHasNotAnswered() {
        val signals = UpdateSignals(UpdateAvailability.Unknown, 0, 0)

        assertNull(UpdatePromptPolicy.dialogFor(signals, promptDismissed = false, restartPostponed = false))
    }

    @Test
    fun dialogFor_returnsAvailableWhenUpdateIsWaiting() {
        val signals = UpdateSignals(UpdateAvailability.Available, 0, 0)

        assertEquals(
            UpdateDialogKind.Available,
            UpdatePromptPolicy.dialogFor(signals, promptDismissed = false, restartPostponed = false),
        )
    }

    @Test
    fun dialogFor_returnsNullWhenTheUserAlreadyDismissedThePrompt() {
        val signals = UpdateSignals(UpdateAvailability.Available, 0, 0)

        assertNull(UpdatePromptPolicy.dialogFor(signals, promptDismissed = true, restartPostponed = false))
    }

    @Test
    fun dialogFor_returnsRestartWhenTheDownloadFinished() {
        val signals = UpdateSignals(UpdateAvailability.Downloaded, 100, 100)

        assertEquals(
            UpdateDialogKind.ReadyToRestart,
            UpdatePromptPolicy.dialogFor(signals, promptDismissed = true, restartPostponed = false),
        )
    }

    @Test
    fun dialogFor_returnsNullWhenTheRestartWasPostponed() {
        val signals = UpdateSignals(UpdateAvailability.Downloaded, 100, 100)

        assertNull(UpdatePromptPolicy.dialogFor(signals, promptDismissed = false, restartPostponed = true))
    }

    @Test
    fun dialogFor_returnsNullWhileTheDownloadIsRunning() {
        val signals = UpdateSignals(UpdateAvailability.Downloading, 50, 100)

        assertNull(UpdatePromptPolicy.dialogFor(signals, promptDismissed = false, restartPostponed = false))
    }

    @Test
    fun cardFor_hidesTheCardWhenPlayDidNotAnswer() {
        assertEquals(
            UpdateCardState.Hidden,
            UpdatePromptPolicy.cardFor(UpdateSignals(UpdateAvailability.Unknown, 0, 0)),
        )
    }

    @Test
    fun cardFor_mapsEveryAvailability() {
        assertEquals(
            UpdateCardState.UpToDate,
            UpdatePromptPolicy.cardFor(UpdateSignals(UpdateAvailability.None, 0, 0)),
        )
        assertEquals(
            UpdateCardState.Available,
            UpdatePromptPolicy.cardFor(UpdateSignals(UpdateAvailability.Available, 0, 0)),
        )
        assertEquals(
            UpdateCardState.Downloading,
            UpdatePromptPolicy.cardFor(UpdateSignals(UpdateAvailability.Downloading, 10, 100)),
        )
        assertEquals(
            UpdateCardState.ReadyToInstall,
            UpdatePromptPolicy.cardFor(UpdateSignals(UpdateAvailability.Downloaded, 100, 100)),
        )
    }

    @Test
    fun clearsDismissal_onlyWhenPlayHasNothingPending() {
        assertEquals(true, UpdatePromptPolicy.clearsDismissal(UpdateSignals(UpdateAvailability.None, 0, 0)))
        assertEquals(false, UpdatePromptPolicy.clearsDismissal(UpdateSignals(UpdateAvailability.Unknown, 0, 0)))
        assertEquals(false, UpdatePromptPolicy.clearsDismissal(UpdateSignals(UpdateAvailability.Available, 0, 0)))
        assertEquals(false, UpdatePromptPolicy.clearsDismissal(UpdateSignals(UpdateAvailability.Downloaded, 1, 1)))
    }

    @Test
    fun downloadPercent_returnsNullWithoutATotalSize() {
        assertNull(UpdatePromptPolicy.downloadPercent(UpdateSignals(UpdateAvailability.Downloading, 500, 0)))
    }

    @Test
    fun downloadPercent_clampsToWholePercent() {
        assertEquals(50, UpdatePromptPolicy.downloadPercent(UpdateSignals(UpdateAvailability.Downloading, 50, 100)))
        assertEquals(0, UpdatePromptPolicy.downloadPercent(UpdateSignals(UpdateAvailability.Downloading, 1, 300)))
        assertEquals(100, UpdatePromptPolicy.downloadPercent(UpdateSignals(UpdateAvailability.Downloading, 300, 300)))
    }

    @Test
    fun cardLabels_areNullWhenTheCardIsHidden() {
        assertNull(UpdateCardLabels.of(UpdateSignals(UpdateAvailability.Unknown, 0, 0)))
    }

    @Test
    fun cardLabels_describeEveryVisibleState() {
        assertEquals(
            UpdateCardUi("Você está na versão mais recente", null, null),
            UpdateCardLabels.of(UpdateSignals(UpdateAvailability.None, 0, 0)),
        )
        assertEquals(
            UpdateCardUi("Versão nova disponível", "Atualizar", UpdateCardAction.StartUpdate),
            UpdateCardLabels.of(UpdateSignals(UpdateAvailability.Available, 0, 0)),
        )
        assertEquals(
            UpdateCardUi("Baixando 50%", null, null),
            UpdateCardLabels.of(UpdateSignals(UpdateAvailability.Downloading, 50, 100)),
        )
        assertEquals(
            UpdateCardUi("Baixando", null, null),
            UpdateCardLabels.of(UpdateSignals(UpdateAvailability.Downloading, 50, 0)),
        )
        assertEquals(
            UpdateCardUi("Pronta para instalar", "Reiniciar", UpdateCardAction.Restart),
            UpdateCardLabels.of(UpdateSignals(UpdateAvailability.Downloaded, 100, 100)),
        )
    }
}
