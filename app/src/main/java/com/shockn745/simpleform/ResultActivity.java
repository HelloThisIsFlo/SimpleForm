package com.shockn745.simpleform;

import android.support.v7.app.ActionBar;
import android.support.v7.app.ActionBarActivity;
import android.os.Bundle;
import android.support.v7.app.AppCompatActivity;
import android.view.Menu;
import android.view.MenuItem;
import android.widget.TextView;

import java.text.DateFormat;
import java.util.Date;

public class ResultActivity extends AppCompatActivity {

    private TextView mTempTextView;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_result);

        // Find views by id
        mTempTextView = (TextView) findViewById(R.id.temp_text_view);

        String name = getIntent().getStringExtra(MainActivity.NAME_KEY);
        String surname = getIntent().getStringExtra(MainActivity.SURNAME_KEY);
        long birthdayMs = getIntent().getLongExtra(MainActivity.BIRTHDAY_KEY, 0);
        Date birthday = new Date(birthdayMs);

        ActionBar actionBar = getSupportActionBar();
        if (actionBar != null) {
            actionBar.setTitle(name + " " + surname);
        }

        DateFormat dateFormat = DateFormat.getDateInstance();

        mTempTextView.setText(name
                + "\n"
                + surname
                + "\n"
                + dateFormat.format(birthday)
        );


    }
}
