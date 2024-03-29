package nl.kwyntes.roosterappie.ui

import android.app.DatePickerDialog
import android.content.Context
import android.widget.Toast
import androidx.compose.foundation.background
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.lazy.items
import androidx.compose.material.*
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.Settings
import androidx.compose.runtime.*
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.dp
import androidx.datastore.preferences.core.edit
import androidx.navigation.NavController
import com.google.accompanist.pager.ExperimentalPagerApi
import com.google.accompanist.pager.HorizontalPager
import com.google.accompanist.pager.rememberPagerState
import nl.kwyntes.roosterappie.PREF_LOGIN_COOKIE
import nl.kwyntes.roosterappie.PREF_PASSWORD
import nl.kwyntes.roosterappie.PREF_PNL
import nl.kwyntes.roosterappie.dataStore
import nl.kwyntes.roosterappie.lib.*
import nl.kwyntes.roosterappie.ui.theme.LightBlue
import nl.kwyntes.roosterappie.ui.theme.LightGreen
import java.time.LocalDate
import kotlin.coroutines.resume
import kotlin.coroutines.suspendCoroutine

@ExperimentalPagerApi
@Composable
fun ScheduleScreen(navController: NavController, ahScheduleService: AHScheduleService, initialMonthYear: MonthYear?) {
    val context = LocalContext.current

    suspend fun logout() {
        context.dataStore.edit {
            it.remove(PREF_PNL)
            it.remove(PREF_PASSWORD)
            it.remove(PREF_LOGIN_COOKIE)
        }
        ahScheduleService.logout()

        navController.backQueue.clear()
        navController.navigate("login")

        Toast.makeText(context, "Daar ging iets mis! (wachwoord gewijzigd?)", Toast.LENGTH_SHORT).show()
    }

    val pagerState = rememberPagerState(initialPage = 1)

    var monthYear by remember { mutableStateOf(initialMonthYear ?: MonthYear.now()) }
    var prevShifts: List<Shift> by remember { mutableStateOf(listOf()) }
    var currShifts: List<Shift> by remember { mutableStateOf(listOf()) }
    var nextShifts: List<Shift> by remember { mutableStateOf(listOf()) }

    LaunchedEffect(Unit) {
        try {
            prevShifts = ahScheduleService.getSchedule(monthYear.previous())
            currShifts = ahScheduleService.getSchedule(monthYear)
            nextShifts = ahScheduleService.getSchedule(monthYear.next())
        } catch (e: IncorrectCredentialsException) {
            logout()
        }
    }

    LaunchedEffect(pagerState.currentPage) {
        if (pagerState.currentPage == 0) {
            // When swiping left (going back)
            // This has to be at the top to prevent weird update glitches
            pagerState.scrollToPage(1)

            monthYear = monthYear.previous()

            nextShifts = currShifts
            currShifts = prevShifts
            try {
                prevShifts = ahScheduleService.getSchedule(monthYear.previous())
            } catch (e: IncorrectCredentialsException) {
                logout()
            }
        } else if (pagerState.currentPage == 2) {
            // When swiping right (going forward)
            // This has to be at the top to prevent weird update glitches
            pagerState.scrollToPage(1)

            monthYear = monthYear.next()

            prevShifts = currShifts
            currShifts = nextShifts
            try {
                nextShifts = ahScheduleService.getSchedule(monthYear.next())
            } catch (e: IncorrectCredentialsException) {
                logout()
            }
        }
    }

    HorizontalPager(count = 3, state = pagerState) { page ->
        Scaffold(
            topBar = {
                TopAppBar(
                    backgroundColor = MaterialTheme.colors.primary,
                    title = {
                        Text(
                            when (page) {
                                0 -> monthYear.previous()
                                1 -> monthYear
                                2 -> monthYear.next()
                                else -> monthYear /* never */
                            }.format()
                        )
                    },
                    actions = {
                        // -- Not yet finished --
//                        IconButton(onClick = { navController.navigate("calculator?lastMonthYear=${monthYear}") }) {
//                            Icon(Icons.Filled.Calculate, contentDescription = "Rekenmachine")
//                        }

                        IconButton(onClick = { navController.navigate("settings?lastMonthYear=${monthYear}") }) {
                            Icon(Icons.Filled.Settings, contentDescription = "Instellingen")
                        }
                    }
                )
            }
        ) { contentPadding ->
            LazyColumn(modifier = Modifier
                .fillMaxHeight()
                .padding(contentPadding)) {
                items(
                    when (page) {
                        0 -> prevShifts; 1 -> currShifts; 2 -> nextShifts; else -> listOf() /* never */
                    }
                ) { shift ->
                    Column(
                        modifier = Modifier
                            .background(
                                when {
                                    shift.date.isEqual(LocalDate.now()) -> LightBlue
                                    shift.authorisedStatus == AuthorisedStatus.Authorised -> LightGreen
                                    else -> Color.Unspecified
                                }
                            )
                            .fillMaxWidth()
                            .padding(horizontal = 20.dp, vertical = 5.dp),

                        content = {
                            Text(shift.formatDate(), fontWeight = FontWeight.Bold)
                            Row {
                                Text(shift.formatTimeFrame() + " ")
                                Text(
                                    when (shift.authorisedStatus) {
                                        AuthorisedStatus.None -> ""
                                        AuthorisedStatus.Authorised -> "(geautoriseerd)"
                                        AuthorisedStatus.NotAuthorised -> "(niet-geautoriseerd)"
                                    }
                                )
                            }
                        }
                    )
                    Spacer(modifier = Modifier.height(1.dp))
                }
            }
        }
    }
}

suspend fun calculateWorkedHours(context: Context, ahScheduleService: AHScheduleService): Float {
    val now = LocalDate.now()

    val pickedDate: LocalDate = suspendCoroutine { continuation ->
        DatePickerDialog(
            context,
            { _, year, month, dayOfMonth ->
                continuation.resume(LocalDate.of(year, month, dayOfMonth))
            },
            now.year, now.monthValue, now.dayOfMonth
        ).run {
            datePicker.maxDate = now.toEpochDay()
            show()
        }
    }

    return ahScheduleService.calculateAuthorisedHoursSince(pickedDate)
}
