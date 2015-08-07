package com.shockn745.simpleform.result.recyclerview.animation;

import android.app.Activity;
import android.os.Handler;
import android.support.design.widget.Snackbar;
import android.view.View;

import com.shockn745.simpleform.R;
import com.shockn745.simpleform.result.recyclerview.Card;
import com.shockn745.simpleform.result.recyclerview.CardAdapter;

import java.util.ArrayList;

/**
 * Class that schedules cards one after the other to display them in a fluid motion.
 * Also directly cache cards previously masked
 *
 * @author Kempenich Florian
 */
public class CardScheduler {

    private final ArrayList<Card> pendingCards = new ArrayList<>();
    private final CardAdapter mAdapter;
    private final long mAddDuration;
    private final Handler mHandler;
    private final View mView;

    /**
     * Create a new CardScheduler
     * @param cardAdapter Adapter to hold the cards
     * @param addDuration addDuration of the recyclerView
     */
    public CardScheduler(CardAdapter cardAdapter, long addDuration, View view) {
        this.mAdapter = cardAdapter;
        this.mAddDuration = addDuration;
        this.mView = view;
        mHandler = new Handler();

    }

    public void addCardToList(Card cardToAdd) {
        pendingCards.add(cardToAdd);
    }

    /**
     * Display cards one after the other
     */
    public void displayPendingCards() {
        int i = 1;
        for (final Card card : pendingCards) {
            mHandler.postDelayed(new Runnable() {
                @Override
                public void run() {
                    mAdapter.addCard(card);
                }
            }, mAddDuration * i);

            i++;
        }
        pendingCards.clear();

        // Display hint
        mHandler.postDelayed(new Runnable() {
            @Override
            public void run() {
                Snackbar.make(
                        mView,
                        R.string.snackbar,
                        Snackbar.LENGTH_SHORT
                ).show();
            }
        }, mAddDuration * i);
    }
}
