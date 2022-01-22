// Logic mostly taken from https://github.com/Netfloex/Albert-Heijn-Timesheet

package nl.kwyntes.roosterappie.lib

import it.skrape.core.htmlDocument
import it.skrape.selects.ElementNotFoundException
import okhttp3.*
import java.io.IOException
import java.time.LocalDate
import java.time.LocalTime
import java.time.format.DateTimeFormatter
import kotlin.coroutines.suspendCoroutine

const val TIMESHEET_URL = "https://sam.ahold.com/wrkbrn_jct/etm/time/timesheet/etmTnsMonth.jsp"
const val LOGIN_URL = "https://sam.ahold.com/pkmslogin.form"

data class AuthData(val pnl: String?, val password: String?, val loginCookie: String? = null)

class IncorrectCredentialsException : Exception()

class AHScheduleService {
    private val client = OkHttpClient.Builder()
        .followRedirects(false)
        .build()

    private var pnl: String? = null
    private var password: String? = null
    private var loginCookie: String? = null

    fun importAuthData(authData: AuthData) {
        pnl = authData.pnl ?: pnl
        password = authData.password ?: password
        loginCookie = authData.loginCookie ?: loginCookie
    }

    fun logout() {
        pnl = null
        password = null
        loginCookie = null
    }

    fun isLoggedIn(): Boolean {
        return loginCookie != null
    }

    private suspend fun newSessionCookie(): String {
        val req = Request.Builder().url(TIMESHEET_URL).build()

        return suspendCoroutine { continuation ->
            client.newCall(req).enqueue(object : Callback {
                override fun onResponse(call: Call, response: Response) {
                    response.body?.close()

                    // We might want to handle a situation where the cookie header is missing,
                    // but I don't think that will ever happen.
                    // And I probably shouldn't think that.
                    val sessionCookie = response.header("set-cookie").orEmpty().substringBefore(";")

                    continuation.resumeWith(Result.success(sessionCookie))
                }

                override fun onFailure(call: Call, e: IOException) {
                    continuation.resumeWith(Result.failure(e))
                }
            })
        }
    }

    private suspend fun newLoginCookie(): String {
        val sessionCookie = newSessionCookie()

        val req = Request.Builder()
            .url(LOGIN_URL)
            .header("Cookie", sessionCookie)
            .post(FormBody.Builder()
                .add("username", pnl!!)
                .add("password", password!!)
                .add("login-form-type", "pwd")
                .build())
            .build()

        return suspendCoroutine { continuation ->
            client.newCall(req).enqueue(object : Callback {
                override fun onResponse(call: Call, response: Response) {
                    response.body?.close()

                    // Handle incorrect credentials
                    if (response.code != 302) {
                        continuation.resumeWith(Result.failure(IncorrectCredentialsException()))
                        return
                    }

                    val loginCookie =
                        response.header("set-cookie").orEmpty().substringBefore(";")
                    continuation.resumeWith(Result.success(loginCookie))
                }

                override fun onFailure(call: Call, e: IOException) {
                    continuation.resumeWith(Result.failure(e))
                }
            })
        }
    }

    suspend fun performLogin(): String {
        loginCookie = newLoginCookie()
        return loginCookie!!
    }

    suspend fun tryPerformLoginIfNeeded() {
        if (loginCookie == null && pnl != null && password != null) {
            performLogin()
        }
    }

    suspend fun getSchedule(monthYear: MonthYear): List<Shift> {
        val req = Request.Builder()
            .url(TIMESHEET_URL + "?NEW_MONTH_YEAR=${monthYear.month}/${monthYear.year}")
            .header("Cookie", loginCookie!!)
            // When the login cookie is expired, ask for a JSON response. Otherwise this header is ignored.
            .header("Accept", "application/json")
            .build()

        val body: String = suspendCoroutine { continuation ->
            client.newCall(req).enqueue(object : Callback {
                override fun onResponse(call: Call, response: Response) {
                    val bodyString = response.body?.string().orEmpty()
                    response.body?.close()
                    continuation.resumeWith(Result.success(bodyString))
                }

                override fun onFailure(call: Call, e: IOException) {
                    continuation.resumeWith(Result.failure(e))
                }
            })
        }

        // Response when login cookie has expired
        if (body == "{\n    \"operation\" : \"login\"\n}\n") {
            performLogin()
            return getSchedule(monthYear)
        }

        try {
            return htmlDocument(body) {
                // Black magic
                findAll("td[class*=calendarCellRegular]:not(.calendarCellRegularCurrent:has(.calCellData)) table") {
                    map {
                        val dateString = it.attribute("title")
                            .replace("Details van ", "")

                        Shift(
                            date = LocalDate.parse(
                                dateString,
                                DateTimeFormatter.ofPattern("MM/dd/yyyy")
                            ),
                            start = LocalTime.parse(it.findFirst("span span").text),
                            end = LocalTime.parse(it.findSecond("span span").text),
                            authorisedStatus = when {
                                it.text.contains("niet-geautoriseerd") -> AuthorisedStatus.NotAuthorised
                                it.text.contains("geautoriseerd") -> AuthorisedStatus.Authorised
                                else -> AuthorisedStatus.None
                            }
                        )
                    }
                }
            }
        } catch (e: ElementNotFoundException) {
            // Apparantly there's no nice way of doing things
            return listOf()
        }
    }
}
