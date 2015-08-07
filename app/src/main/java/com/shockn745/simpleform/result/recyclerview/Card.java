package com.shockn745.simpleform.result.recyclerview;

/**
 * Card to display informations
 *
 * @author Florian Kempenich
 */
public class Card {

    private final String mHeader;
    private final String mContent;

    public Card(String header, String content) {
        this.mHeader = header;
        this.mContent = content;
    }

    public String getHeader() {
        return mHeader;
    }

    public String getContent() {
        return mContent;
    }

    public boolean canDismiss() {
        return true;
    }
}