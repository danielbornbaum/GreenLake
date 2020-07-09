package de.dhbw.greenlake_data_generator;

import org.json.JSONObject;

import java.util.Calendar;

public class CalendarConverter {
    public JSONObject ConvertToJson(Calendar calendar) {
        JSONObject json = new JSONObject()
                .put("day", calendar.get(Calendar.DAY_OF_MONTH))
                .put("month", (calendar.get(Calendar.MONTH) + 1))
                .put("year", calendar.get(Calendar.YEAR))
                .put("hour", calendar.get(Calendar.HOUR_OF_DAY))
                .put("minute", calendar.get(Calendar.MINUTE))
                .put("second", calendar.get(Calendar.SECOND));
        return json;
    }

    public Calendar ConvertToCalendar(JSONObject json) {
        Calendar calendar = Calendar.getInstance();
        calendar.set(Calendar.DAY_OF_MONTH, json.getInt("day"));
        calendar.set(Calendar.MONTH, (json.getInt("month") - 1));
        calendar.set(Calendar.YEAR, json.getInt("year"));
        calendar.set(Calendar.HOUR_OF_DAY, json.getInt("hour"));
        calendar.set(Calendar.MINUTE, json.getInt("minute"));
        calendar.set(Calendar.SECOND, json.getInt("second"));
        return calendar;
    }
}
