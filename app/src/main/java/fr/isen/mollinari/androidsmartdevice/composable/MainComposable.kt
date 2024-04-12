package fr.isen.mollinari.androidsmartdevice.composable

import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.padding
import androidx.compose.material3.ExperimentalMaterial3Api
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Scaffold
import androidx.compose.material3.Text
import androidx.compose.material3.TopAppBar
import androidx.compose.material3.TopAppBarDefaults
import androidx.compose.runtime.Composable
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.res.stringResource
import androidx.compose.ui.unit.dp
import fr.isen.mollinari.androidsmartdevice.R

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun CustomToolBar(
    content: @Composable () -> Unit,
    verticalArrangement: Arrangement.Vertical = Arrangement.Top
) {
    Scaffold(
        topBar = {
            TopAppBar(
                colors = TopAppBarDefaults.topAppBarColors(
                    containerColor = MaterialTheme.colorScheme.primary,
                    titleContentColor = MaterialTheme.colorScheme.secondary,
                ),
                title = {
                    Text(text = stringResource(id = R.string.app_name), color = Color.White)
                }
            )
        }

    ) { innerPadding ->
        Column(
            modifier = Modifier
                .padding(innerPadding)
                .padding(0.dp, 24.dp)
                .fillMaxSize(),
            verticalArrangement = verticalArrangement
        ) {
            content()
        }
    }
}