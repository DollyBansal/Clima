package com.weather.clima.com.weather.clima.service;

import org.json.JSONException;
import org.json.JSONObject;

import java.io.BufferedReader;
import java.io.IOException;
import java.io.InputStreamReader;
import java.net.HttpURLConnection;
import java.net.URL;


public class JSONClient {


    public JSONObject getObject(String city) throws IOException, JSONException {
        JSONObject data;
        try {
            URL url = new URL("http://api.openweathermap.org/data/2.5/weather?q=" + city+",US" + "&units=imperial&APPID=92a6296f9454be5cc4fdc04cd99d54e4");

            System.out.println("url = " + url);
            HttpURLConnection connection = (HttpURLConnection) url.openConnection();

            BufferedReader reader =
                    new BufferedReader(new InputStreamReader(connection.getInputStream()));

            StringBuffer json = new StringBuffer();
            String line;

            while ((line = reader.readLine()) != null)
                json.append(line).append("\n");
            reader.close();

            data = new JSONObject(json.toString());
            System.out.println("data = " + data);
            if (!data.has("cod") || data.getInt("cod") != 200) {
                System.out.println("Data not available");
                return null;
            }

        } catch (IOException e) {
            e.printStackTrace();
            return null;
        } catch (JSONException e) {
            e.printStackTrace();
            return null;
        }

        return data;
    }


}
