package fumei.faruk.dev.br

import android.os.Bundle
import androidx.activity.ComponentActivity
import androidx.activity.compose.setContent
import androidx.activity.enableEdgeToEdge
import androidx.activity.viewModels
import androidx.compose.runtime.collectAsState
import androidx.compose.runtime.getValue
import fumei.faruk.dev.br.data.AppDatabase
import fumei.faruk.dev.br.data.PuffRepository
import fumei.faruk.dev.br.ui.MainScreen
import fumei.faruk.dev.br.ui.MainViewModel
import fumei.faruk.dev.br.ui.MainViewModelFactory
import fumei.faruk.dev.br.ui.theme.FumeiTheme

class MainActivity : ComponentActivity() {
    private val viewModel: MainViewModel by viewModels {
        val repository = PuffRepository(AppDatabase.getInstance(applicationContext).puffDao())
        MainViewModelFactory(repository)
    }

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        enableEdgeToEdge()
        setContent {
            FumeiTheme {
                val uiState by viewModel.uiState.collectAsState()
                MainScreen(
                    uiState = uiState,
                    onFumeiClick = viewModel::onFumeiClick,
                    onEditPuff = viewModel::onEditPuff,
                    onDeletePuff = viewModel::onDeletePuff,
                )
            }
        }
    }
}
