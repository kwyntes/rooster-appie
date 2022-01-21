package nl.kwyntes.roosterappie.ui

import androidx.compose.foundation.border
import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.*
import androidx.compose.material.*
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.ArrowBack
import androidx.compose.runtime.Composable
import androidx.compose.runtime.rememberCoroutineScope
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.drawBehind
import androidx.compose.ui.geometry.Offset
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.unit.dp
import androidx.datastore.preferences.core.edit
import androidx.navigation.NavController
import kotlinx.coroutines.coroutineScope
import kotlinx.coroutines.launch
import nl.kwyntes.roosterappie.PREF_LOGIN_COOKIE
import nl.kwyntes.roosterappie.PREF_PASSWORD
import nl.kwyntes.roosterappie.PREF_PNL
import nl.kwyntes.roosterappie.dataStore
import nl.kwyntes.roosterappie.lib.AHScheduleService

@Composable
fun SettingsScreen(navController: NavController, ahScheduleService: AHScheduleService) {
    val context = LocalContext.current
    val coroutineScope = rememberCoroutineScope()

    Scaffold(
        topBar = {
            TopAppBar(
                backgroundColor = MaterialTheme.colors.primary,
                title = { Text("Instellingen") },
                navigationIcon = {
                    IconButton(onClick = { navController.navigate("schedule") }) {
                        Icon(Icons.Filled.ArrowBack, contentDescription = "Terug")
                    }
                }
            )
        }
    ) { contentPadding ->
        Column(modifier = Modifier.padding(contentPadding)) {
            Spacer(modifier = Modifier.height(20.dp))

            Text(
                "Uitloggen",

                modifier = Modifier
                    .clickable {
                        coroutineScope.launch {
                            context.dataStore.edit {
                                it.remove(PREF_PNL)
                                it.remove(PREF_PASSWORD)
                                it.remove(PREF_LOGIN_COOKIE)
                            }
                            ahScheduleService.logout()

                            navController.navigate("login")
                        }
                    }
                    // Draw border only on top and bottom
                    .drawBehind {
                        val y = size.height - density / 2

                        drawLine(
                            Color.LightGray,
                            Offset(0f, 0f),
                            Offset(size.width, 0f),
                            density
                        )
                        drawLine(
                            Color.LightGray,
                            Offset(0f, y),
                            Offset(size.width, y),
                            density
                        )
                    }
                    .fillMaxWidth()
                    .padding(all = 20.dp)
            )
        }
    }
}
