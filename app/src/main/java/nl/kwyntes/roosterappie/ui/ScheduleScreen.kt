package nl.kwyntes.roosterappie.ui

import androidx.compose.foundation.background
import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.lazy.items
import androidx.compose.material.*
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.Settings
import androidx.compose.runtime.*
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.style.TextAlign
import androidx.compose.ui.unit.dp
import androidx.navigation.NavController
import nl.kwyntes.roosterappie.lib.AHScheduleService
import nl.kwyntes.roosterappie.lib.MonthYear
import nl.kwyntes.roosterappie.lib.Shift
import nl.kwyntes.roosterappie.ui.theme.Blue
import java.time.LocalDate
import java.time.format.DateTimeFormatter

val DATE_FORMATTER: DateTimeFormatter = DateTimeFormatter.ofPattern("dd-MM-yyyy")
val TIME_FORMATTER: DateTimeFormatter = DateTimeFormatter.ofPattern("hh:mm")

@Composable
fun ScheduleScreen(navController: NavController, ahScheduleService: AHScheduleService) {
    var monthYear by remember { mutableStateOf(MonthYear.now()) }
    var shifts: List<Shift> by remember { mutableStateOf(listOf()) }

    LaunchedEffect(monthYear) {
        shifts = ahScheduleService.getSchedule()
    }

    Scaffold(
        topBar = {
            TopAppBar(
                backgroundColor = MaterialTheme.colors.primary,
                title = { Text(monthYear.format()) },
                actions = {
                    IconButton(onClick = { navController.navigate("settings") }) {
                        Icon(Icons.Filled.Settings, contentDescription = "Instellingen")
                    }
                }
            )
        }
    ) { contentPadding ->
        LazyColumn(modifier = Modifier.padding(contentPadding)) {
            items(shifts) { shift ->
                Column(
                    modifier = Modifier
                        // TODO: Different colors for authorised shifts
                        // TODO: Make colors lighter
                        .background(if (shift.date.isEqual(LocalDate.now())) Blue else Color.Unspecified)
                        .clickable {}
                        .fillMaxWidth()
                        .padding(horizontal = 20.dp, vertical = 5.dp),

                    content = {
                        Text(shift.date.format(DATE_FORMATTER), fontWeight = FontWeight.Bold)
                        Text(
                            "${shift.start.format(TIME_FORMATTER)} - ${
                                shift.end.format(
                                    TIME_FORMATTER
                                )
                            }"
                        )
                    }
                )
            }
        }
    }
}
