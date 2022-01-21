package nl.kwyntes.roosterappie

import android.content.Context
import android.os.Bundle
import androidx.activity.ComponentActivity
import androidx.activity.compose.setContent
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.padding
import androidx.compose.material.Scaffold
import androidx.compose.material.Surface
import androidx.compose.material.Text
import androidx.compose.material.TopAppBar
import androidx.compose.ui.Modifier
import androidx.datastore.core.DataStore
import androidx.datastore.preferences.core.Preferences
import androidx.datastore.preferences.core.stringPreferencesKey
import androidx.datastore.preferences.preferencesDataStore
import androidx.navigation.compose.NavHost
import androidx.navigation.compose.composable
import androidx.navigation.compose.rememberNavController
import kotlinx.coroutines.flow.first
import kotlinx.coroutines.flow.map
import kotlinx.coroutines.runBlocking
import nl.kwyntes.roosterappie.lib.AHScheduleService
import nl.kwyntes.roosterappie.lib.AuthData
import nl.kwyntes.roosterappie.ui.LoginScreen
import nl.kwyntes.roosterappie.ui.ScheduleScreen
import nl.kwyntes.roosterappie.ui.theme.RoosterAppieTheme

val Context.dataStore: DataStore<Preferences> by preferencesDataStore(name = "settings")
val PREF_PNL = stringPreferencesKey("pnl")
val PREF_PASSWORD = stringPreferencesKey("password")
val PREF_LOGIN_COOKIE = stringPreferencesKey("login_cookie")

class MainActivity : ComponentActivity() {
    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)

        val ahScheduleService = AHScheduleService()

        runBlocking {
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

            RoosterAppieTheme {
                Surface {
                    NavHost(
                        navController,
                        startDestination = if (ahScheduleService.isLoggedIn()) "schedule" else "login"
                    ) {
                        composable("login") { LoginScreen(navController, ahScheduleService) }
                        composable("schedule") { ScheduleScreen(navController, ahScheduleService) }
                    }
                }
            }
        }
    }
}
