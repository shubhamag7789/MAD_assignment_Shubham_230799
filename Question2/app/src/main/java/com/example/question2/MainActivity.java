package com.example.question2;

import android.content.Intent;
import android.content.SharedPreferences;
import android.os.Bundle;
import android.view.Menu;
import android.view.MenuItem;
import android.view.View;
import android.widget.ArrayAdapter;
import android.widget.Button;
import android.widget.EditText;
import android.widget.Spinner;
import android.widget.TextView;
import androidx.appcompat.app.AppCompatActivity;
import androidx.appcompat.app.AppCompatDelegate;
import androidx.preference.PreferenceManager;
import java.util.HashMap;

public class MainActivity extends AppCompatActivity {

    private EditText inputValue;
    private Spinner fromSpinner;
    private Spinner toSpinner;
    private Button convertButton;
    private TextView resultText;
    private HashMap<String, Double> conversionFactors;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        // Apply the saved theme before creating the activity
        SharedPreferences prefs = PreferenceManager.getDefaultSharedPreferences(this);
        boolean isDarkMode = prefs.getBoolean("isDarkMode", false);
        AppCompatDelegate.setDefaultNightMode(
                isDarkMode ? AppCompatDelegate.MODE_NIGHT_YES : AppCompatDelegate.MODE_NIGHT_NO
        );
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_main);

        // Initialize UI elements
        inputValue = findViewById(R.id.input_value);
        fromSpinner = findViewById(R.id.from_spinner);
        toSpinner = findViewById(R.id.to_spinner);
        convertButton = findViewById(R.id.convert_button);
        resultText = findViewById(R.id.result_text);

        // Define conversion factors (meters as base unit)
        conversionFactors = new HashMap<>();
        conversionFactors.put("Meters", 1.0);
        conversionFactors.put("Centimeters", 0.01);
        conversionFactors.put("Inches", 0.0254);
        conversionFactors.put("Feet", 0.3048);
        conversionFactors.put("Yards", 0.9144);

        // Populate spinners
        String[] units = {"Centimeters", "Feet", "Inches", "Meters", "Yards"};
        ArrayAdapter<String> adapter = new ArrayAdapter<>(this, R.layout.spinner_item, units);
        adapter.setDropDownViewResource(R.layout.spinner_item);
        fromSpinner.setAdapter(adapter);
        toSpinner.setAdapter(adapter);

        // Set up convert button listener
        convertButton.setOnClickListener(v -> doConversion());
    }

    private void doConversion() {
        String inputText = inputValue.getText().toString();
        if (inputText.isEmpty()) {
            resultText.setText("Hey, please enter a number!");
            return;
        }
        try {
            double value = Double.parseDouble(inputText);
            String fromUnit = fromSpinner.getSelectedItem().toString();
            String toUnit = toSpinner.getSelectedItem().toString();
            double meters = value * conversionFactors.get(fromUnit);
            double result = meters / conversionFactors.get(toUnit);
            resultText.setText("Result: " + String.format("%.4f", result) + " " + toUnit);
        } catch (NumberFormatException e) {
            resultText.setText("Oops, that’s not a valid number!");
        }
    }

    @Override
    public boolean onCreateOptionsMenu(Menu menu) {
        getMenuInflater().inflate(R.menu.main_menu, menu);
        return true;
    }

    @Override
    public boolean onOptionsItemSelected(MenuItem item) {
        if (item.getItemId() == R.id.action_settings) {
            Intent intent = new Intent(this, SettingsActivity.class);
            startActivity(intent);
            return true;
        }
        return super.onOptionsItemSelected(item);
    }
}