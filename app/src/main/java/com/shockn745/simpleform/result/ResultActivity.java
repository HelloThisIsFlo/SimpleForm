package com.shockn745.simpleform.result;

import android.support.v7.app.ActionBar;
import android.os.Bundle;
import android.support.v7.app.AppCompatActivity;
import android.support.v7.widget.LinearLayoutManager;
import android.support.v7.widget.RecyclerView;
import android.widget.TextView;

import com.shockn745.simpleform.R;
import com.shockn745.simpleform.main.MainActivity;
import com.shockn745.simpleform.result.recyclerview.Card;
import com.shockn745.simpleform.result.recyclerview.CardAdapter;
import com.shockn745.simpleform.result.recyclerview.animation.CardAnimator;
import com.shockn745.simpleform.result.recyclerview.animation.CardScheduler;
import com.shockn745.simpleform.result.recyclerview.animation.SwipeDismissRecyclerViewTouchListener;

import java.text.DateFormat;
import java.util.ArrayList;
import java.util.Date;

public class ResultActivity extends AppCompatActivity {

    private TextView mTempTextView;

    private CardAdapter mAdapter;
    private RecyclerView mRecyclerView;

    private String mName;
    private String mSurname;
    private String mBirthday;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_result);

        // Find views by id
        mRecyclerView = (RecyclerView) findViewById(R.id.cards_recycler_view);

        initRecyclerView();

        // Retrieve name & surname
        mName = getIntent().getStringExtra(MainActivity.NAME_KEY);
        mSurname = getIntent().getStringExtra(MainActivity.SURNAME_KEY);

        // Retrieve & format birthday
        long birthdayMs = getIntent().getLongExtra(MainActivity.BIRTHDAY_KEY, 0);
        Date birthday = new Date(birthdayMs);
        DateFormat dateFormat = DateFormat.getDateInstance();
        mBirthday = dateFormat.format(birthday);

        // Set full name in ActionBar
        ActionBar actionBar = getSupportActionBar();
        if (actionBar != null) {
            actionBar.setTitle(mName + " " + mSurname);
        }


        // Create cards
        Card nameCard = new Card(getString(R.string.name), mName);
        Card surnameCard = new Card(getString(R.string.surname), mSurname);
        Card birthdayCard = new Card(getString(R.string.birthday), mBirthday);

        // Schedule cards
        CardScheduler scheduler = new CardScheduler(
                mAdapter,
                getResources().getInteger(R.integer.scheduler_add_duration),
                mRecyclerView
        );

        scheduler.addCardToList(nameCard);
        scheduler.addCardToList(surnameCard);
        scheduler.addCardToList(birthdayCard);

        // Animate cards
        scheduler.displayPendingCards();
    }


    private void initRecyclerView() {
        // Set the adapter with empty dataset
        mAdapter = new CardAdapter(new ArrayList<Card>());
        mRecyclerView.setAdapter(mAdapter);

        // Notify the recyclerView that its size won't change (better perfs)
        mRecyclerView.setHasFixedSize(true);

        // Set the OnTouchListener
        SwipeDismissRecyclerViewTouchListener touchListener =
                new SwipeDismissRecyclerViewTouchListener(
                        mRecyclerView,
                        mAdapter,
                        this
                );
        mRecyclerView.setOnTouchListener(touchListener);

        // Setting this scroll listener is required to ensure that during ListView scrolling,
        // we don't look for swipes.
        mRecyclerView.setOnScrollListener(touchListener.makeScrollListener());

        mRecyclerView.setLayoutManager(new LinearLayoutManager(this));
        mRecyclerView.setItemAnimator(new CardAnimator(this));
    }

}
