package com.weather.clima;

import android.content.Context;
import android.graphics.Bitmap;
import android.graphics.BitmapFactory;
import android.os.AsyncTask;
import android.os.Bundle;
import android.support.v7.app.AppCompatActivity;
import android.view.View;
import android.widget.Button;
import android.widget.EditText;
import android.widget.ImageView;
import android.widget.TextView;

import com.weather.clima.com.weather.clima.service.JSONClient;
import com.weather.clima.com.weather.clima.service.JSONDataParser;

import org.json.JSONException;
import org.json.JSONObject;

import java.io.FileInputStream;
import java.io.FileOutputStream;
import java.io.IOException;
import java.io.InputStream;
import java.net.HttpURLConnection;
import java.net.URL;

public class MainActivity extends AppCompatActivity {

    TextView city_name_text, date_text, time_text, cur_temp_text, high_temp_text, min_temp_text,
            description_text, day_text;
    EditText city_name_edit_text;
    ImageView icon;
    Button ok;
    JSONObject data;
    String FILENAME = "current_city";


    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_main);
        city_name_text = (TextView) findViewById(R.id.city_name);
        date_text = (TextView) findViewById(R.id.date);
        time_text = (TextView) findViewById(R.id.time);
        cur_temp_text = (TextView) findViewById(R.id.current_temperature);
        high_temp_text = (TextView) findViewById(R.id.high_temperature);
        min_temp_text = (TextView) findViewById(R.id.low_temperature);
        description_text = (TextView) findViewById(R.id.description);
        day_text = (TextView) findViewById(R.id.day);
        city_name_edit_text = (EditText) findViewById(R.id.enter_city_name);
        ok = (Button) findViewById(R.id.ok);
        ok.setOnClickListener(new View.OnClickListener() {
            public void onClick(View v) {
                String user_city = city_name_edit_text.getText().toString();
                System.out.println("user_city = " + user_city);
                getJSON(user_city);
                try {
                    saveCurrentCityNameInInternalStorage(user_city);
                } catch (IOException e) {
                    e.printStackTrace();
                }

            }
        });
        icon = (ImageView) findViewById(R.id.weather_icon);

    }

    public void saveCurrentCityNameInInternalStorage(String city_name) throws IOException {
        FileOutputStream fos = openFileOutput(FILENAME, Context.MODE_PRIVATE);
        fos.write(city_name.getBytes());
        fos.close();
    }

    public String getWeatherOfLastCityEntered() throws IOException {
        FileInputStream fin = openFileInput(FILENAME);
        int c;
        String city_name = "";
        while ((c = fin.read()) != -1) {
            city_name = city_name + Character.toString((char) c);
        }
        return city_name;
    }

    public void getJSON(final String city_name) {
        new AsyncTask<Void, Void, Void>() {

            @Override
            protected Void doInBackground(Void... params) {
                JSONClient jsonClient = new JSONClient();
                try {
                    data = jsonClient.getObject(city_name);
                } catch (IOException e) {
                    e.printStackTrace();
                } catch (JSONException e) {
                    e.printStackTrace();
                }
                return null;
            }

            @Override
            protected void onPostExecute(Void aVoid) {
                super.onPostExecute(aVoid);

                try {
                    getData(data);
                } catch (JSONException e) {
                    e.printStackTrace();
                }
            }

        }.execute();

    }

    private void getData(final JSONObject data) throws JSONException {
        city_name_text.setText(JSONDataParser.getCityName(data));

        cur_temp_text.setText(JSONDataParser.getCurrentTemperature(data) + "°F");
        // high_temp_text.setText(JSONDataParser.getCurrentHighTemperature(data) + "°F/");
        // min_temp_text.setText(JSONDataParser.getCurrentMinTemperature(data) + "°F");
        date_text.setText(JSONDataParser.getCurrentDateAndTime(data));
        description_text.setText(JSONDataParser.getWeatherDescription(data));
        System.out.println("JSONDataParser.getIconUrl(data) = " + JSONDataParser.getIconUrl(data));
        Thread thread = new Thread(new Runnable() {
            @Override
            public void run() {
                try {
                    Bitmap image = getBitmapFromURL(JSONDataParser.getIconUrl(data));
                    icon.setImageBitmap(image);
                } catch (Exception e) {
                    e.printStackTrace();
                }
            }
        });
        thread.start();
    }

    @Override
    protected void onStart() {
        super.onStart();
        String city_name = "";
        try {
            city_name = getWeatherOfLastCityEntered();
        } catch (IOException e) {
            e.printStackTrace();
        }
        // auto load weather of last city entered by user.
        if (city_name != null && !city_name.isEmpty()) {
            getJSON(city_name);
        }
    }

    public Bitmap getBitmapFromURL(String imageUrl) {
        try {
            URL url = new URL(imageUrl);
            HttpURLConnection connection = (HttpURLConnection) url.openConnection();
            connection.setDoInput(true);
            connection.connect();
            InputStream input = connection.getInputStream();
            Bitmap myBitmap = BitmapFactory.decodeStream(input);
            return myBitmap;
        } catch (IOException e) {
            e.printStackTrace();
            return null;
        }
    }
}



