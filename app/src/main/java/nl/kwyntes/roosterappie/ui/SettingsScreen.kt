package nl.kwyntes.roosterappie.ui

import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.*
import androidx.compose.material.*
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.ArrowBack
import androidx.compose.runtime.Composable
import androidx.compose.runtime.rememberCoroutineScope
import androidx.compose.ui.Modifier
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.unit.dp
import androidx.datastore.preferences.core.edit
import androidx.navigation.NavController
import kotlinx.coroutines.launch
import nl.kwyntes.roosterappie.PREF_LOGIN_COOKIE
import nl.kwyntes.roosterappie.PREF_PASSWORD
import nl.kwyntes.roosterappie.PREF_PNL
import nl.kwyntes.roosterappie.dataStore
import nl.kwyntes.roosterappie.lib.AHScheduleService
import nl.kwyntes.roosterappie.lib.MonthYear

@Composable
fun SettingsScreen(navController: NavController, ahScheduleService: AHScheduleService, lastMonthYear: MonthYear) {
    val context = LocalContext.current
    val coroutineScope = rememberCoroutineScope()

    Scaffold(
        topBar = {
            TopAppBar(
                backgroundColor = MaterialTheme.colors.primary,
                title = { Text("Instellingen") },
                navigationIcon = {
                    IconButton(onClick = { navController.navigate("schedule?monthYear=${lastMonthYear}") }) {
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
                    .borderTopAndBottomOnly()
                    .clickable {
                        coroutineScope.launch {
                            context.dataStore.edit {
                                it.remove(PREF_PNL)
                                it.remove(PREF_PASSWORD)
                                it.remove(PREF_LOGIN_COOKIE)
                            }
                            ahScheduleService.logout()

                            navController.backQueue.clear()
                            navController.navigate("login")
                        }
                    }
                    .fillMaxWidth()
                    .padding(all = 20.dp)
            )
        }
    }
}
