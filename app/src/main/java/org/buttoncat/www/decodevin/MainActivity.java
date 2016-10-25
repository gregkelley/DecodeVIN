package org.buttoncat.www.decodevin;

import android.support.v7.app.AppCompatActivity;
import android.os.Bundle;
import android.view.View;
import android.widget.Button;
import android.widget.EditText;
import android.widget.TextView;
import android.widget.Toast;

import org.buttoncat.www.decodevin.command.getNHTSAData;

public class MainActivity extends AppCompatActivity {

    private Button vinLookupButton;
    private EditText textVINValue;
    private TextView output;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_main);

        vinLookupButton = (Button) findViewById(R.id.btnSend);
        textVINValue = (EditText) findViewById(R.id.textVINValue);
        output = (TextView) findViewById(R.id.output);
        textVINValue.setText("6G2VX12G44L204054");  // give initial value so I don't have to type
    }


    public void onVINDecodeClick(View view) {
        String vinText = String.valueOf(textVINValue.getText()).toUpperCase();
        String x = "Entered: " + vinText;
        Toast.makeText(this, x, Toast.LENGTH_SHORT).show();

        // WebServer Request URL.
        // These URLs are not currently interchangeable. If you want to test against a "test server"
        // the pythonanywhere code will need to be changed to send back the excel string like the NHTSA
        // sends.
        String serverURL = "https://vpic.nhtsa.dot.gov/decoder/Decoder/ExportToExcel";
        //String serverURL = "http://gak.pythonanywhere.com/vin_lookup";  // ?vin=2G1WH55K3Y9210869";


        TextView make = (TextView)findViewById(R.id.VehicleMakeVal);
        TextView model = (TextView)findViewById(R.id.VehicleModelVal);
        TextView year = (TextView)findViewById(R.id.VehicleYearVal);

        // Use AsyncTask execute Method To Prevent ANR Problem
        getNHTSAData gnd = new getNHTSAData(make, model, year);
        gnd.execute(serverURL, vinText);

        make.setVisibility(View.VISIBLE);
        model.setVisibility(View.VISIBLE);
        year.setVisibility(View.VISIBLE);
    }

}
