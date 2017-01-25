package com.puja.trials.fillItUp;

/**
 * Created by puja on 21/01/17.
 */

public class Word {

    int startLineIndex;
    int endLineIndex;
    String word;

    public int getEndIndex() {
        return endLineIndex;
    }

    public void setEndIndex(int endIndex) {
        this.endLineIndex = endIndex;
    }

    public int getStartIndex() {
        return startLineIndex;
    }

    public void setStartIndex(int startIndex) {
        this.startLineIndex = startIndex;
    }

    public String getWord() {
        return word;
    }

    public void setWord(String word) {
        this.word = word;
    }


}
