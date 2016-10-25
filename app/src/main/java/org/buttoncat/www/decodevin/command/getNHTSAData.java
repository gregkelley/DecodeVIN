package org.buttoncat.www.decodevin.command;

import android.net.Uri;
import android.os.AsyncTask;
import android.util.Log;
import android.widget.TextView;

import java.io.BufferedInputStream;
import java.io.BufferedReader;
import java.io.BufferedWriter;
import java.io.IOException;
import java.io.InputStream;
import java.io.InputStreamReader;
import java.io.OutputStream;
import java.io.OutputStreamWriter;
import java.net.HttpURLConnection;
import java.net.MalformedURLException;
import java.net.URL;
import java.util.ArrayList;
import java.util.Hashtable;
import java.util.List;

/**
 * Created by Greg on 9/21/2016.
 * send a URL request to the NHTSA site and request data on the supplied VIN
 * The web service returns csv as a string. Sort of Excel format
 *
 */
//
// the types "AsyncTask<String, String, String>" correspond to
// 1: - this is passed to doInBackground
// 2: - what is passed to PostExecute / Progress
// 3: - what the doInBackground task returns (Result)
//
public class getNHTSAData extends AsyncTask<String, String, String > {
    List<String> srList = new ArrayList<>();
    TextView make;
    TextView model;
    TextView year;

    // constructor
    public getNHTSAData(TextView make, TextView model, TextView year) {
        this.make = make;
        this.model = model;
        this.year = year;
    }

    // doInBackground -- copied from stackoverflow.
    // supposedly this creates a separate thread which sends off a URL request. When the request
    // comes back, onPostExecute is invoked. Which is where the returned data should be handled.
    @Override
    protected String doInBackground(String... args) {

        String postParam = args[1];  // the VIN from user.
        String postParamKey = "vin";

        try {
            URL url = new URL(args[0]);
            HttpURLConnection urlConnection = (HttpURLConnection) url.openConnection();
            urlConnection.setRequestMethod("POST");
            urlConnection.setDoInput(true);
            urlConnection.setDoOutput(true);
            Uri.Builder builder = new Uri.Builder().appendQueryParameter(postParamKey, postParam);
            String query = builder.build().getEncodedQuery();

            OutputStream os = urlConnection.getOutputStream();
            BufferedWriter writer = new BufferedWriter(new OutputStreamWriter(os, "UTF-8"));
            writer.write(query);
            writer.flush();
            writer.close();
            os.close();
            urlConnection.connect();

            int responseCode = urlConnection.getResponseCode();

            if (responseCode == HttpURLConnection.HTTP_OK) {
                try {
                    InputStream inputStream = new BufferedInputStream(urlConnection.getInputStream());
                    BufferedReader readThis = new BufferedReader(new InputStreamReader(inputStream));
                    String l;
                    while ((l = readThis.readLine()) != null) {
                        srList.add(l);
                    }
                } finally {
                    urlConnection.disconnect();
                }

                Log.v("CatalogClient", srList.toString());
            }

        } catch (MalformedURLException e) {
            e.printStackTrace();
        } catch (IOException e) {
            e.printStackTrace();
        }
        return null;
    }

    // parse the returned data to extract specific vehicle info
    @Override
    protected void onPostExecute(String s) {
        super.onPostExecute(s);
        for( String l : srList ) {
            Log.v("Response", l);
        }
        Log.v("Finished", " dumping server response");
        Hashtable<String, String> vCodes = getCodes(srList);
        Log.v("Make ", vCodes.get("make"));
        make.setText(vCodes.get("make"));
        Log.v("Model ", vCodes.get("model"));
        model.setText(vCodes.get("model"));
        Log.v("Year ", vCodes.get("year"));
        year.setText(vCodes.get("year"));
    }

    // actually parse the data returned from web service. Extract specific pieces into a hash
    // table. Return has table to caller.
    private Hashtable<String, String> getCodes( List<String> lines ) {

        Log.v("Enter", "getCodes routine");
        Hashtable<String, String> vCodes = new Hashtable<>();
        // keep track of how many of the items we have found. Once we have everything, leave.
        int count = 0;
        for( String ll : lines ) {
            // the data we want is on lines that start with "General"
            if( ll.startsWith("General")) {
                String[] y = ll.split(",");
                Log.v("start", ll);
                if( y[1].equals("Make")) {
                    vCodes.put("make", y[2]);
                    Log.v("Make", y[2]);
                    ++count;
                }
                else if( y[1].equals("Model")) {
                    //System.out.println("Model: " + y[2]);
                    //model.setText(y[2]);
                    //model.setVisibility(View.VISIBLE);
                    vCodes.put("model", y[2]);
                    Log.v("Model", y[2]);
                    ++count;
                }
                else if( y[1].equals("Model Year")) {
                    vCodes.put("year", y[2]);
                    ++count;
                }
                if( count >= 3) {
                    break;
                }
            }
        }
        Log.v("Exit", "getCodes routine");
        return vCodes;
    }

}