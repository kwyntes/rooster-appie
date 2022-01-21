package nl.kwyntes.roosterappie.lib

import java.time.LocalDate

data class MonthYear(val month: Int, val year: Int) {
    fun format(): String {
        return when (month) {
            1 -> "Januari"
            2 -> "Februari"
            3 -> "Maart"
            4 -> "April"
            5 -> "Mei"
            6 -> "Juni"
            7 -> "Juli"
            8 -> "Augustus"
            9 -> "September"
            10 -> "Oktober"
            11 -> "November"
            12 -> "December"
            else -> ""
        } + " $year"
    }

    fun previous() = if (month == 1) MonthYear(12, year - 1) else MonthYear(month - 1, year)
    fun next() = if (month == 12) MonthYear(1, year + 1) else MonthYear(month + 1, year)

    companion object {
        fun now(): MonthYear {
            val date = LocalDate.now()
            return MonthYear(date.monthValue, date.year)
        }
    }
}
