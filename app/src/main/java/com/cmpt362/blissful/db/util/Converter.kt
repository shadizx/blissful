package com.cmpt362.blissful.db.util
import androidx.room.TypeConverter
import java.util.Calendar

object Converters {

    /**
     * A type converter between Calendar Type (type we used in MyRuns3) and datestamp (time converted into milliseconds).
     * The millisec time value can be obtained by (e.g. current time): ```System.currentTimeMillis()```
     */
    @TypeConverter
    fun calendarToDatestamp(calendar: Calendar): Long = calendar.timeInMillis

    @TypeConverter
    fun datestampToCalendar(value: Long): Calendar =
        Calendar.getInstance().apply { timeInMillis = value }
}