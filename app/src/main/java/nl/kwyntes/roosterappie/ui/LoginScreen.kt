package nl.kwyntes.roosterappie.ui

import android.widget.Toast
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.text.KeyboardOptions
import androidx.compose.material.Button
import androidx.compose.material.MaterialTheme
import androidx.compose.material.Text
import androidx.compose.material.TextField
import androidx.compose.runtime.*
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.text.input.KeyboardType
import androidx.compose.ui.text.input.PasswordVisualTransformation
import androidx.compose.ui.unit.dp
import androidx.datastore.preferences.core.edit
import androidx.navigation.NavController
import kotlinx.coroutines.launch
import nl.kwyntes.roosterappie.PREF_LOGIN_COOKIE
import nl.kwyntes.roosterappie.PREF_PASSWORD
import nl.kwyntes.roosterappie.PREF_PNL
import nl.kwyntes.roosterappie.dataStore
import nl.kwyntes.roosterappie.lib.AHScheduleService
import nl.kwyntes.roosterappie.lib.AuthData
import nl.kwyntes.roosterappie.lib.IncorrectCredentialsException

@Composable
fun LoginScreen(navController: NavController, ahScheduleService: AHScheduleService) {
    val context = LocalContext.current
    val coroutineScope = rememberCoroutineScope()

    var pnl by remember { mutableStateOf("") }
    var password by remember { mutableStateOf("") }

    Column(modifier = Modifier.padding(all = 20.dp)) {
        Text("Inloggen", style = MaterialTheme.typography.h4)

        Spacer(modifier = Modifier.height(100.dp))

        TextField(
            label = { Text("PNL") },
            modifier = Modifier.fillMaxWidth(),

            value = pnl,
            onValueChange = { pnl = it },
        )
        Spacer(modifier = Modifier.height(10.dp))
        TextField(
            label = { Text("Wachtwoord") },
            modifier = Modifier.fillMaxWidth(),
            keyboardOptions = KeyboardOptions(keyboardType = KeyboardType.Password),
            visualTransformation = PasswordVisualTransformation(),

            value = password,
            onValueChange = { password = it },
        )

        Spacer(modifier = Modifier.height(20.dp))

        Button(
            content = { Text("Inloggen") },
            modifier = Modifier.align(Alignment.End),

            onClick = {
                coroutineScope.launch {
                    ahScheduleService.importAuthData(AuthData(pnl, password))
                    try {
                        val loginCookie = ahScheduleService.performLogin()

                        context.dataStore.edit { settings ->
                            settings[PREF_PNL] = pnl
                            settings[PREF_PASSWORD] = password
                            settings[PREF_LOGIN_COOKIE] = loginCookie
                        }

                        navController.navigate("schedule")
                    } catch (e: IncorrectCredentialsException) {
                        Toast.makeText(context, "Daar ging iets mis! (onjuiste PNL en/of wachtwoord?)", Toast.LENGTH_SHORT).show()
                    }
                }
            },
        )
    }
}
