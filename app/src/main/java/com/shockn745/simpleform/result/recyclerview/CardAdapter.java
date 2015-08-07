package com.shockn745.simpleform.result.recyclerview;

import android.support.v7.widget.RecyclerView;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.TextView;

import com.shockn745.simpleform.R;
import com.shockn745.simpleform.result.recyclerview.animation.SwipeDismissRecyclerViewTouchListener;

import java.util.ArrayList;

/**
 * Adapter for the list of cards
 *
 * @author Florian Kempenich
 */
public class CardAdapter extends RecyclerView.Adapter<RecyclerView.ViewHolder> implements
        SwipeDismissRecyclerViewTouchListener.DismissCallbacks {

    public static class CardVH extends RecyclerView.ViewHolder {
        public final TextView mHeaderTextView;
        public final TextView mContentTextView;

        public CardVH(View itemView) {
            super(itemView);
            mHeaderTextView = (TextView) itemView.findViewById(R.id.card_header_text_view);
            mContentTextView = (TextView) itemView.findViewById(R.id.card_content_text_view);
        }
    }

    private final ArrayList<Card> mDataSet;

    public CardAdapter(ArrayList<Card> dataSet) {
        // Init the dataset
        mDataSet = dataSet;
    }


    /**
     * Create the viewHolder
     *
     * @return ViewHolder created
     */
    @Override
    public RecyclerView.ViewHolder onCreateViewHolder(ViewGroup parent, int viewType) {
        View itemView = LayoutInflater
                .from(parent.getContext())
                .inflate(R.layout.card, parent, false);

        return new CardVH(itemView);
    }

    /**
     * Replace the content of the view
     *
     * @param holder viewHolder
     * @param position Position of the data
     */
    @Override
    public void onBindViewHolder(final RecyclerView.ViewHolder holder, int position) {

        CardVH cardHolder = (CardVH) holder;

        Card card = mDataSet.get(position);

        cardHolder.mHeaderTextView.setText(card.getHeader());
        cardHolder.mContentTextView.setText(card.getContent());
    }


    @Override
    public int getItemCount() {
        return mDataSet.size();
    }



    //////////////////////////////////////////
    // Methods to implement DismissCallback //
    //////////////////////////////////////////

    /**
     * Determine whether a card wan be dismissed or not
     * @param position Position of the card
     * @return true if dismissable
     */
    @Override
    public boolean canDismiss(int position) {
        return mDataSet.get(position).canDismiss();
    }

    @Override
    public void onDismiss(RecyclerView recyclerView, int position) {
        removeCard(position);
    }

    ///////////////////////////////////
    // Methods to handle the dataset //
    ///////////////////////////////////

    /**
     * Method used to add a card at the end.
     * Handle the insertion in the dataset and in the adapter.
     * Triggers the animation.
     * @param toAdd Card to add at the end
     */
    public void addCard(Card toAdd) {
        // Element inserted at the end
        // So size of dataset before insertion == position of inserted element
        int position = mDataSet.size();

        mDataSet.add(toAdd);
        notifyItemInserted(position);
    }

    /**
     * Method used to remove a card.
     * Handle the deletion from the dataset and the adapter
     * Triggers the animation.
     * @param position Position of the card to remove
     */
    private void removeCard(int position) {
        mDataSet.remove(position);

        // Notify Adapter to refresh (also starts the animation)
        notifyItemRemoved(position);
    }

}
