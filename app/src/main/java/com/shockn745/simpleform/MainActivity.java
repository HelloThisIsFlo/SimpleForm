package com.shockn745.simpleform;

import android.app.DatePickerDialog;
import android.os.Bundle;
import android.support.v7.app.AppCompatActivity;
import android.text.Editable;
import android.text.TextWatcher;
import android.view.View;
import android.widget.Button;
import android.widget.DatePicker;
import android.widget.EditText;
import android.widget.TextView;
import android.widget.Toast;

import java.text.SimpleDateFormat;
import java.util.Calendar;
import java.util.Locale;

public class MainActivity extends AppCompatActivity {

    private EditText mNameEditText;
    private EditText mSurnameEditText;
    private EditText mBirthdayTextView;
    private Button mOkButton;

    private Calendar mCalendar;
    private boolean ageOver18 = false;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_main);

        // Init the calendar
        mCalendar = Calendar.getInstance();

        // Find views by id
        mNameEditText = (EditText) findViewById(R.id.name_edit_text);
        mSurnameEditText = (EditText) findViewById(R.id.surname_edit_text);
        mBirthdayTextView = (EditText) findViewById(R.id.birthday_edit_text);
        mOkButton = (Button) findViewById(R.id.ok_button);

        // Init the editText
        initEditText(mNameEditText);
        initEditText(mSurnameEditText);

        mOkButton.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                String nom = mNameEditText.getText().toString();
                Toast.makeText(MainActivity.this, nom, Toast.LENGTH_SHORT).show();
            }
        });



        //////////////////////////////////////////////////
        // Show DatePicker when click on Birthday Field //
        //////////////////////////////////////////////////

        // Create the listener
        final DatePickerDialog.OnDateSetListener dateListener =
                new DatePickerDialog.OnDateSetListener() {
            @Override
            public void onDateSet(DatePicker view,
                                  int year,
                                  int monthOfYear,
                                  int dayOfMonth) {
                // Save the date
                mCalendar.set(Calendar.YEAR, year);
                mCalendar.set(Calendar.MONTH, monthOfYear);
                mCalendar.set(Calendar.DAY_OF_MONTH, dayOfMonth);

                // Update label
                updateLabel();

                // Check age
                // TODO Check age
                if (true) {
                    // Age > 18
                    ageOver18 = true;
                    activateButtonIfValidInput();
                } else {
                    // Age < 18
                    ageOver18 = false;
                    Toast.makeText(
                            MainActivity.this,
                            "You must be 18 or older to continue!",
                            Toast.LENGTH_LONG
                    ).show();
                }
            }

        };

        // Show the DatePicker
        mBirthdayTextView.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                new DatePickerDialog(MainActivity.this,
                        dateListener,
                        mCalendar.get(Calendar.YEAR),
                        mCalendar.get(Calendar.MONTH),
                        mCalendar.get(Calendar.DAY_OF_MONTH)).show();
            }
        });

    }

    /**
     * Update the label on the birthday field.
     * Used when a date is set.
     */
    private void updateLabel(){
        String myFormat = "MM/dd/yy"; //In which you need put here
        SimpleDateFormat sdf = new SimpleDateFormat(myFormat, Locale.US);

        // Check if calendar is set to prevent IllegalArgumentException
        if (mCalendar.isSet(Calendar.YEAR)
                && mCalendar.isSet(Calendar.MONTH)
                && mCalendar.isSet(Calendar.DAY_OF_MONTH)) {
            mBirthdayTextView.setText(sdf.format(mCalendar.getTime()));
        }
    }

    /**
     * Activate button if data is valid
     * Data valid :
     *  - Name non empty
     *  - Surname non empty
     *  - Age over 18
     */
    private void activateButtonIfValidInput() {
        if (mNameEditText.getText().length() != 0
                && mSurnameEditText.getText().length() != 0
                && ageOver18) {
            mOkButton.setEnabled(true);
        } else {
            mOkButton.setEnabled(false);
        }
    }


    /**
     * Init the editText with a listener that activate the button if data is valid
     * @param editText EditText to init
     */
    private void initEditText(EditText editText) {
        editText.addTextChangedListener(new TextWatcher() {
            @Override
            public void afterTextChanged(Editable s) {
                activateButtonIfValidInput();
            }

            @Override
            public void beforeTextChanged(CharSequence s, int start, int count, int after) {}
            @Override
            public void onTextChanged(CharSequence s, int start, int before, int count) {}
        });
    }
}
