package com.weather.clima;

import android.content.Context;
import android.content.pm.PackageManager;
import android.graphics.Bitmap;
import android.graphics.BitmapFactory;
import android.location.Location;
import android.os.AsyncTask;
import android.os.Bundle;
import android.support.annotation.NonNull;
import android.support.annotation.Nullable;
import android.support.v4.app.ActivityCompat;
import android.support.v7.app.AppCompatActivity;
import android.util.Log;
import android.view.View;
import android.widget.Button;
import android.widget.EditText;
import android.widget.ImageView;
import android.widget.TextView;

import com.google.android.gms.common.ConnectionResult;
import com.google.android.gms.common.api.GoogleApiClient;
import com.google.android.gms.common.api.GoogleApiClient.ConnectionCallbacks;
import com.google.android.gms.common.api.GoogleApiClient.OnConnectionFailedListener;
import com.google.android.gms.location.LocationServices;
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

public class MainActivity extends AppCompatActivity implements
        ConnectionCallbacks, OnConnectionFailedListener {

    private static final String TAG = "MainActivity";

    private TextView city_name_text, date_text, time_text, cur_temp_text, high_temp_text, min_temp_text,
            description_text, day_text;
    private EditText city_name_edit_text;
    private ImageView icon;
    private Button ok, locate_me;
    private String FILENAME = "current_city";


    // Provides the entry point to Google Play services.
    private GoogleApiClient mGoogleApiClient;

    // Represents a geographical location.
    private Location mLastLocation;

    private Double mLatitudeLabel;
    private Double mLongitudeLabel;


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
        icon = (ImageView) findViewById(R.id.weather_icon);
        ok = (Button) findViewById(R.id.ok);
        locate_me = (Button) findViewById(R.id.locate_me);

        buildGoogleApiClient();

        ok.setOnClickListener(new View.OnClickListener() {
            public void onClick(View v) {
                if (city_name_edit_text != null && !city_name_edit_text.getText().toString().isEmpty()) {
                    String user_city = city_name_edit_text.getText().toString();
                    System.out.println("user_city = " + user_city);
                    getJSONByCityName(user_city);
                }
            }
        });
        locate_me.setOnClickListener(new View.OnClickListener() {
            public void onClick(View v) {

                if (mLatitudeLabel != null && mLongitudeLabel != null) {
                    getJSONByCoordinate(mLatitudeLabel, mLongitudeLabel);
                }
            }
        });

    }

    private void saveCurrentCityNameInInternalStorage(String city_name) throws IOException {
        FileOutputStream fos = openFileOutput(FILENAME, Context.MODE_PRIVATE);
        fos.write(city_name.getBytes());
        fos.close();
    }

    private String getWeatherOfLastCityEntered() throws IOException {
        FileInputStream fin = openFileInput(FILENAME);
        int c;
        String city_name = "";
        while ((c = fin.read()) != -1) {
            city_name = city_name + Character.toString((char) c);
        }
        return city_name;
    }

    private void getJSONByCityName(final String city_name) {
        new AsyncTask<Void, Void, Void>() {
            JSONObject data;

            @Override
            protected Void doInBackground(Void... params) {
                JSONClient jsonClient = new JSONClient();
                try {
                    data = jsonClient.getObjectByCityName(city_name);
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

    private void getJSONByCoordinate(final Double lat, final Double log) {
        new AsyncTask<Void, Void, Void>() {
            JSONObject data;

            @Override
            protected Void doInBackground(Void... params) {
                JSONClient jsonClient = new JSONClient();
                try {
                    data = jsonClient.getObjectByCoordinates(lat, log);
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
        date_text.setText(JSONDataParser.getCurrentDate(data));
        description_text.setText(JSONDataParser.getWeatherDescription(data));
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

        try {
            saveCurrentCityNameInInternalStorage(JSONDataParser.getCityName(data));
        } catch (IOException e) {
            e.printStackTrace();
        }
    }

    @Override
    protected void onStart() {
        super.onStart();

        if (mGoogleApiClient != null) {
            mGoogleApiClient.connect();
        }

        String city_name = "";
        try {
            city_name = getWeatherOfLastCityEntered();
        } catch (IOException e) {
            e.printStackTrace();
        }
        // auto load weather of last city entered by user.
        if (city_name != null && !city_name.isEmpty()) {
            getJSONByCityName(city_name);
        }
    }

    @Override
    protected void onStop() {
        super.onStop();
        if (mGoogleApiClient.isConnected()) {
            mGoogleApiClient.disconnect();
        }
    }


    private Bitmap getBitmapFromURL(String imageUrl) {
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

    // Builds a GoogleApiClient. Uses the addApi() method to request the LocationServices API.
    protected synchronized void buildGoogleApiClient() {
        mGoogleApiClient = new GoogleApiClient.Builder(this)
                .addConnectionCallbacks(this)
                .addOnConnectionFailedListener(this)
                .addApi(LocationServices.API)
                .build();
    }

    // Runs when a GoogleApiClient object successfully connects.
    @Override
    public void onConnected(@Nullable Bundle bundle) {
        // Provides a simple way of getting a device's location and is well suited for         // applications that do not require a fine-grained location and that do not need location
        // updates. Gets the best and most recent location currently available, which may be null
        // in rare cases when a location is not available.
        if (ActivityCompat.checkSelfPermission(this, android.Manifest.permission.ACCESS_FINE_LOCATION) != PackageManager.PERMISSION_GRANTED && ActivityCompat.checkSelfPermission(this, android.Manifest.permission.ACCESS_COARSE_LOCATION) != PackageManager.PERMISSION_GRANTED) {
            // TODO: Consider calling
            return;
        }
        mLastLocation = LocationServices.FusedLocationApi.getLastLocation(mGoogleApiClient);
        if (mLastLocation != null) {
            mLatitudeLabel = mLastLocation.getLatitude();
            mLongitudeLabel = mLastLocation.getLongitude();
            Log.i(TAG, "Latitude" + mLatitudeLabel + ", Longitude" + mLongitudeLabel);
        }

    }

    @Override
    public void onConnectionSuspended(int i) {
        // The connection to Google Play services was lost for some reason. We call connect() to
        // attempt to re-establish the connection.
        Log.i(TAG, "Connection suspended");
        mGoogleApiClient.connect();
    }

    @Override
    public void onConnectionFailed(@NonNull ConnectionResult connectionResult) {
        // Refer to the javadoc for ConnectionResult to see what error codes might be returned in
        // onConnectionFailed.
        Log.i(TAG, "Connection failed: ConnectionResult.getErrorCode() = " + connectionResult.getErrorCode());
    }
}



