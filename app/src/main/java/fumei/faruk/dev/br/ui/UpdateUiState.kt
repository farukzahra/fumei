package fumei.faruk.dev.br.ui

import fumei.faruk.dev.br.update.UpdateCardUi
import fumei.faruk.dev.br.update.UpdateDialogKind

data class UpdateUiState(
    val dialog: UpdateDialogKind? = null,
    val card: UpdateCardUi? = null,
)
