package com.example.question1;
import android.os.Bundle;
import android.view.View;
import android.widget.Button;
import android.widget.EditText;
import android.widget.Spinner;
import android.widget.TextView;
import android.widget.ArrayAdapter;
import androidx.appcompat.app.AppCompatActivity;
import java.util.HashMap;

public class MainActivity extends AppCompatActivity {


    private EditText inputValue;
    private Spinner fromSpinner;
    private Spinner toSpinner;
    private Button convertButton;
    private TextView resultText;

    // HashMap to store conversion factors to meters
    private HashMap<String, Double> conversionFactors;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_main);

        // Link the UI elements to our code
        inputValue = findViewById(R.id.input_value);
        fromSpinner = findViewById(R.id.from_spinner);
        toSpinner = findViewById(R.id.to_spinner);
        convertButton = findViewById(R.id.convert_button);
        resultText = findViewById(R.id.result_text);

        // Set up the conversion factors (how many meters in each unit)
        conversionFactors = new HashMap<>();
        conversionFactors.put("Meters", 1.0);       // 1 meter = 1 meter
        conversionFactors.put("Centimeters", 0.01); // 1 cm = 0.01 meters
        conversionFactors.put("Inches", 0.0254);    // 1 inch = 0.0254 meters
        conversionFactors.put("Feet", 0.3048);      // 1 foot = 0.3048 meters
        conversionFactors.put("Yards", 0.9144);     // 1 yard = 0.9144 meters

        // List of units for the spinners
        String[] units = {"Centimeters", "Feet", "Inches", "Meters", "Yards"};

        // Set up the spinners with the units using the custom layout
        ArrayAdapter<String> adapter = new ArrayAdapter<>(this,
                R.layout.spinner_item, units);
        adapter.setDropDownViewResource(R.layout.spinner_item);
        fromSpinner.setAdapter(adapter);
        toSpinner.setAdapter(adapter);

        // Make the button do something when clicked
        convertButton.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                doConversion();
            }
        });
    }

    // Method to handle the conversion
    private void doConversion() {
        String inputText = inputValue.getText().toString();

        // Check if the user entered anything
        if (inputText.isEmpty()) {
            resultText.setText("Hey, please enter a number!");
            return;
        }

        try {
            // Get the number and the units
            double value = Double.parseDouble(inputText);
            String fromUnit = fromSpinner.getSelectedItem().toString();
            String toUnit = toSpinner.getSelectedItem().toString();

            // Convert to meters first
            double meters = value * conversionFactors.get(fromUnit);
            // Then convert from meters to the target unit
            double result = meters / conversionFactors.get(toUnit);

            // Show the result with 4 decimal places
            resultText.setText("Result: " + String.format("%.4f", result) + " " + toUnit);
        } catch (NumberFormatException e) {
            resultText.setText("Oops, that’s not a valid number!");
        }
    }
}