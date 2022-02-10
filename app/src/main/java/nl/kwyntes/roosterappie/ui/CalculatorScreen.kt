package nl.kwyntes.roosterappie.ui

import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.padding
import androidx.compose.material.*
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.ArrowBack
import androidx.compose.runtime.Composable
import androidx.compose.runtime.rememberCoroutineScope
import androidx.compose.ui.Modifier
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.unit.dp
import androidx.navigation.NavController
import nl.kwyntes.roosterappie.lib.AHScheduleService
import nl.kwyntes.roosterappie.lib.MonthYear

@Composable
fun CalculatorScreen(navController: NavController, ahScheduleService: AHScheduleService, lastMonthYear: MonthYear) {
    val context = LocalContext.current
    val coroutineScope = rememberCoroutineScope()

    Scaffold(
        topBar = {
            TopAppBar(
                backgroundColor = MaterialTheme.colors.primary,
                title = { Text("Rekenmachine") },
                navigationIcon = {
                    IconButton(onClick = { navController.navigate("schedule?monthYear=${lastMonthYear}") }) {
                        Icon(Icons.Filled.ArrowBack, contentDescription = "Terug")
                    }
                }
            )
        }
    ) { contentPadding ->
        Column(modifier = Modifier.padding(contentPadding).padding(all = 20.dp)) {
            Spacer(modifier = Modifier.height(20.dp))

            TextField(
                label = { Text("Van") },
                readOnly = true,

                value = "Datum kiezen",
                onValueChange = {}
            )
            Spacer(modifier = Modifier.height(10.dp))
            TextField(
                label = { Text("Tot") },
                readOnly = true,

                value = "Datum kiezen",
                onValueChange = {}
            )

            Spacer(modifier = Modifier.height(20.dp))

            Button(onClick = { /*TODO*/ }) {
                Text("Gewerkte uren berekenen")
            }
        }
    }
}
