package com.weather.clima.com.weather.clima.service;

import org.json.JSONException;
import org.json.JSONObject;

import java.text.DateFormat;
import java.util.Date;
import java.util.Locale;

public class JSONDataParser {

    //these are the fields we require in the app
    private final static String CITY_NAME = "name";
    private final static String Main = "main";
    private final static String CURRENT_TEMPERATURE = "temp";
    private final static String HIGH_TEMPERATURE = "temp_max";
    private final static String MIN_TEMPERATURE = "temp_min";
    private final static String WEATHER = "weather";
    private final static String WEATHER_DESCRIPTION = "description";
    private final static String ICON = "icon";


    private final static String CURRENT_DATE_TIME = "dt";

    public static String getWeatherDescription(JSONObject data) throws JSONException {
        JSONObject weather = data.getJSONArray(WEATHER).getJSONObject(0);
        String description = weather.getString(WEATHER_DESCRIPTION).toUpperCase(Locale.US);
        return description;
    }

    public static String getCurrentTemperature(JSONObject data) throws JSONException {
        JSONObject main = getJsonElement(data, Main);
        String cur_temperature = main.getString(CURRENT_TEMPERATURE);
        return cur_temperature;
    }

    public static String getCurrentHighTemperature(JSONObject data) throws JSONException {
        JSONObject main = getJsonElement(data, Main);
        String max_temperature = main.getString(HIGH_TEMPERATURE);
        return max_temperature;
    }

    public static String getCurrentMinTemperature(JSONObject data) throws JSONException {
        JSONObject main = getJsonElement(data, Main);
        String min_temperature = main.getString(MIN_TEMPERATURE);
        return min_temperature;
    }

    public static String getCityName(JSONObject data) throws JSONException {
        String city_name = data.getString(CITY_NAME);
        return city_name.toString();
    }

    public static String getIconUrl(JSONObject data) throws JSONException {
        JSONObject weather = data.getJSONArray(WEATHER).getJSONObject(0);
        String icon = weather.getString(ICON);
        String iconUrl = "http://openweathermap.org/img/w/" + icon + ".png";
        return iconUrl;
    }

    public static String getCurrentDateAndTime(JSONObject data) throws JSONException {
        DateFormat df = DateFormat.getDateTimeInstance();
        String updatedOn = df.format(new Date(data.getLong(CURRENT_DATE_TIME) * 1000));
        System.out.println("updatedOn = " + updatedOn);
        return updatedOn;
    }

    private static JSONObject getJsonElement(JSONObject data, String name) throws JSONException {
        if (data.has(name)) return data.getJSONObject(name);
        return null;
    }
}
