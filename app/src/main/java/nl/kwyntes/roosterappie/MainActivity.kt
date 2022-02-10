package nl.kwyntes.roosterappie

import android.content.Context
import android.content.Intent
import android.net.Uri
import android.os.Bundle
import androidx.activity.ComponentActivity
import androidx.activity.compose.setContent
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material.Card
import androidx.compose.material.Surface
import androidx.compose.material.Text
import androidx.compose.material.TextButton
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.setValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.unit.dp
import androidx.compose.ui.window.Dialog
import androidx.datastore.core.DataStore
import androidx.datastore.preferences.core.Preferences
import androidx.datastore.preferences.core.stringPreferencesKey
import androidx.datastore.preferences.preferencesDataStore
import androidx.navigation.NavType
import androidx.navigation.compose.NavHost
import androidx.navigation.compose.composable
import androidx.navigation.compose.rememberNavController
import androidx.navigation.navArgument
import com.google.accompanist.pager.ExperimentalPagerApi
import kotlinx.coroutines.flow.first
import kotlinx.coroutines.flow.map
import kotlinx.coroutines.runBlocking
import nl.kwyntes.roosterappie.lib.AHScheduleService
import nl.kwyntes.roosterappie.lib.AuthData
import nl.kwyntes.roosterappie.lib.MonthYear
import nl.kwyntes.roosterappie.lib.checkUpdates
import nl.kwyntes.roosterappie.ui.CalculatorScreen
import nl.kwyntes.roosterappie.ui.LoginScreen
import nl.kwyntes.roosterappie.ui.ScheduleScreen
import nl.kwyntes.roosterappie.ui.SettingsScreen
import nl.kwyntes.roosterappie.ui.theme.RoosterAppieTheme

val Context.dataStore: DataStore<Preferences> by preferencesDataStore(name = "settings")
val PREF_PNL = stringPreferencesKey("pnl")
val PREF_PASSWORD = stringPreferencesKey("password")
val PREF_LOGIN_COOKIE = stringPreferencesKey("login_cookie")

@ExperimentalPagerApi
class MainActivity : ComponentActivity() {
    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)

        val ahScheduleService = AHScheduleService()
        var updateInfo: Pair<Boolean, String>

        runBlocking {
            updateInfo = checkUpdates()

            val prefAuthData = applicationContext.dataStore.data.map {
                AuthData(
                    pnl = it[PREF_PNL],
                    password = it[PREF_PASSWORD],
                    loginCookie = it[PREF_LOGIN_COOKIE]
                )
            }.first()
            ahScheduleService.importAuthData(prefAuthData)
            ahScheduleService.tryPerformLoginIfNeeded()
        }

        setContent {
            val navController = rememberNavController()
            var showUpdate by remember { mutableStateOf(updateInfo.first) }

            RoosterAppieTheme {
                Surface {
                    if (showUpdate) {
                        Dialog(onDismissRequest = { showUpdate = false }) {
                            Card(elevation = 8.dp, shape = RoundedCornerShape(12.dp), modifier = Modifier.height(118.dp)) {
                                Column(modifier = Modifier.padding(20.dp)) {
                                    Text("Een nieuwe versie (v${updateInfo.second}) is beschikbaar!")

                                    TextButton(
                                        onClick = {
                                            startActivity(
                                                Intent(
                                                    Intent.ACTION_VIEW,
                                                    Uri.parse("https://kwyntes.nl/rooster-appie/r")
                                                )
                                            )
                                        },
                                        modifier = Modifier.align(Alignment.End)
                                    ) {
                                        Text("Downloaden")
                                    }
                                }
                            }
                        }
                    }

                    NavHost(
                        navController,
                        startDestination = if (ahScheduleService.isLoggedIn()) "schedule?monthYear={monthYear}" else "login"
                    ) {
                        composable("login") { LoginScreen(navController, ahScheduleService) }
                        composable(
                            "schedule?monthYear={monthYear}",
                            arguments = listOf(navArgument("monthYear") {
                                type = NavType.StringType
                                nullable = true
                            })
                        ) { backStackEntry ->
                            ScheduleScreen(
                                navController,
                                ahScheduleService,
                                initialMonthYear = backStackEntry.arguments
                                    ?.getString("monthYear")
                                    ?.let { MonthYear.fromString(it) }
                            )
                        }
                        composable(
                            "settings?lastMonthYear={lastMonthYear}",
                            arguments = listOf(navArgument("lastMonthYear") { type = NavType.StringType })
                        ) {
                            SettingsScreen(
                                navController,
                                ahScheduleService,
                                lastMonthYear = MonthYear.fromString(
                                    it.arguments?.getString("lastMonthYear")!!
                                )
                            )
                        }
                        composable(
                            "calculator?lastMonthYear={lastMonthYear}",
                            arguments = listOf(navArgument("lastMonthYear") { type = NavType.StringType })
                        ) {
                            CalculatorScreen(
                                navController,
                                ahScheduleService,
                                lastMonthYear = MonthYear.fromString(
                                    it.arguments?.getString("lastMonthYear")!!
                                )
                            )
                        }
                    }
                }
            }
        }
    }
}
