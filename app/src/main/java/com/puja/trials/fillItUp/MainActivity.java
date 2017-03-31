package com.puja.trials.fillItUp;

import android.app.Dialog;
import android.app.ProgressDialog;
import android.graphics.Color;
import android.graphics.Typeface;
import android.os.Bundle;
import android.support.v7.app.AppCompatActivity;
import android.text.Editable;
import android.text.Spannable;
import android.text.Spanned;
import android.text.TextPaint;
import android.text.method.LinkMovementMethod;
import android.text.style.ClickableSpan;
import android.util.Log;
import android.view.View;
import android.view.animation.Animation;
import android.view.animation.AnimationUtils;
import android.widget.AdapterView;
import android.widget.Button;
import android.widget.GridView;
import android.widget.ImageView;
import android.widget.TextView;

import com.puja.trials.fillItUp.utils.NetworkConnectivity;
import com.puja.trials.fillItUp.utils.PatternEditableBuilder;
import com.puja.trials.fillItUp.utils.Utilities;

import java.util.ArrayList;
import java.util.Collections;
import java.util.HashMap;
import java.util.Map;
import java.util.regex.Pattern;

public class MainActivity extends AppCompatActivity implements GetWikipediaContentAsync.AsyncResponse {

    TextView mainText;
    GridView optionsGrid;
    ImageView iv_submit, iv_refresh;
    ProgressDialog progressDialog;
    Animation bottomUp, bottomDown;
    Map<Integer, Word> blankWordMap;
    GetWikipediaContentAsync getContent;
    OptionsAdapter adapter;
    final String[] blankAnswer = new String[3];
    Utilities utilities;
    int score = 0;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_main);

        android.support.v7.app.ActionBar actionBar = getSupportActionBar();
        actionBar.setDisplayOptions(actionBar.getDisplayOptions() | android.support.v7.app.ActionBar.DISPLAY_SHOW_CUSTOM);
        actionBar.setTitle(R.string.app_name);
        actionBar.setCustomView(R.layout.custom_titleview);

        utilities = new Utilities(this);
        progressDialog = new ProgressDialog(this);


    /*    while (!NetworkConnectivity.isNetworkAvailable(getApplicationContext())){
            progressDialog.setMessage("Please check your Internet Connection..\n Unable to resume..");
            progressDialog.show();
         //   utilities.longToast("No Internet Connection Available!");
        }  */

        if (NetworkConnectivity.isNetworkAvailable(getApplicationContext())){

            mainText = (TextView) findViewById(R.id.main_textview);
            mainText.setMovementMethod(LinkMovementMethod.getInstance());
            optionsGrid = (GridView) findViewById(R.id.options_gridview);
            iv_submit = (ImageView) findViewById(R.id.iv_submit);
            iv_submit.setEnabled(false);
            iv_refresh = (ImageView) findViewById(R.id.iv_refresh);
            bottomUp = AnimationUtils.loadAnimation(MainActivity.this, R.anim.bottom_up);
            bottomDown = AnimationUtils.loadAnimation(MainActivity.this, R.anim.bottom_down);

            onAppRefresh();

            Typeface textFont = Typeface.createFromAsset(getAssets(), "fonts/Bariol_Regular.otf");
            mainText.setTypeface(textFont);

            iv_submit.setOnClickListener(new View.OnClickListener() {
                @Override
                public void onClick(View view) {

                    final Dialog scoreDialog = new Dialog(MainActivity.this);
                    scoreDialog.setCancelable(false);
                    scoreDialog.setContentView(R.layout.custom_dialog);
                    scoreDialog.setTitle("Score");
                    TextView scoreText = (TextView)scoreDialog.findViewById(R.id.tv_scoreMessage);
                    ImageView scoreEmoticon = (ImageView)scoreDialog.findViewById(R.id.iv_scoreEmoticon);
                    Button replayButton = (Button)scoreDialog.findViewById(R.id.button_replay);

                    if (score < 5){
                        scoreEmoticon.setImageResource(R.drawable.ic_action_mood_bad);
                        scoreText.setText("Your score is " + score + ".\n Better Luck next time!");
                    }
                    else {
                        scoreEmoticon.setImageResource(R.drawable.ic_action_mood);
                        scoreText.setText("Your score is " + score + ".\n Well Played!");
                    }

                    replayButton.setOnClickListener(new View.OnClickListener(){
                        @Override
                        public void onClick(View v) {
                            scoreDialog.dismiss();
                            onAppRefresh();
                        }
                    });
                    scoreDialog.show();

                    utilities.longToast("Score-" + score);
                }
            });

            iv_refresh.setOnClickListener(new View.OnClickListener() {
                @Override
                public void onClick(View view) {
                    onAppRefresh();
                }});
        }
        else {
            utilities.longToast("No Internet Connection Available!");
        }
    }

    @Override
    public void processFinish(final String modifiedParagraph, ArrayList<String> options) {

        final ArrayList<String> blankOptions = options;

        if (modifiedParagraph == null)
        {
            utilities.shortToast("Could not find any text! Please refresh");
        }else {
            mainText.setText(modifiedParagraph, TextView.BufferType.EDITABLE);
            Editable mainPara = (Editable) mainText.getText();

            if (options != null){
                Collections.shuffle(options);
                adapter = new OptionsAdapter(this, options);
                optionsGrid.setAdapter(adapter);
            }else {
                utilities.longToast("No options to show!");
            }

            for (int i = 0; i < 10; i++) {
             //   int newStart = blankWordMap.get(i).getStartIndex();
             //   int newEnd = blankWordMap.get(i).getEndIndex();
                int z = i+1;
                int newStart, newEnd;
                newStart = mainPara.toString().indexOf("(" + z + ") ") + 4;
                newEnd = newStart + 5;
                if (z == 10){
                 newStart = mainPara.toString().indexOf("(" + z + ") ") + 5;
                  newEnd  = newStart + 5;
                }

                ClickableSpan clickSpan = setBlanksClickable(i, newStart, newEnd, mainPara, blankOptions);
                mainPara.setSpan(clickSpan, newStart, newEnd, Spannable.SPAN_EXCLUSIVE_EXCLUSIVE);
            }
       }
    }

    private ClickableSpan setBlanksClickable(final int index, final int bStart,
                                           final int bEnd, final Editable mainPara, final ArrayList<String> blankOptions) {
        return new ClickableSpan() {
            int start = bStart;
            int end = bEnd;

            @Override
            public void onClick(View widget) {
                iv_submit.setEnabled(true);
                Log.d("Clicked Blank", start + "--" + end);

                if (optionsGrid.getVisibility() != View.VISIBLE) {
                    optionsGrid.setVisibility(View.VISIBLE);
                    optionsGrid.startAnimation(bottomUp);
                }

                findBlankClicked(start, end);

                optionsGrid.setOnItemClickListener(new AdapterView.OnItemClickListener() {
                    @Override
                    public void onItemClick(AdapterView<?> adapterView, View view, int i, long l) {
                                /*    if (view.getAlpha() == 1.0f) {
                                        view.setAlpha(0.50f);
                                    }else {
                                        view.setAlpha(1.0f);
                                    }  */
                        blankAnswer[1] = blankWordMap.get(i).getWord();
                        blankAnswer[2] = blankOptions.get(i);
                        blankAnswer[0] = blankAnswer[2];
                    //    updateWordHashMap(start, end, blankAnswer[2]);

                        optionsGrid.startAnimation(bottomDown);
                        optionsGrid.setVisibility(View.GONE);

                        int newEnd = blankWordMap.get(Integer.parseInt(blankAnswer[0])).getEndIndex();
                        int z = index+1;
                        int s = mainPara.toString().indexOf("(" + z + ") ") + 4;
                        if (mainPara.charAt(s) == '_'){
                            mainPara.replace(s, s+5 , blankAnswer[2]);
                        }else {

                        }

                        end = s + blankAnswer[2].length();
                        mainPara.setSpan(this, start, end,
                                Spannable.SPAN_EXCLUSIVE_EXCLUSIVE);

                        modifyScore(blankAnswer);
                    }
                });
            }
        };
    }

    private void updateWordHashMap(int start, int end, String option) {

        int len = option.length();
        for (int lineNo = 0; lineNo < blankWordMap.size() ; lineNo++){
            Word blankWord = blankWordMap.get(lineNo);

            if (blankWord != null && (start >= blankWord.getStartIndex() && end <= blankWord.getEndIndex())){
                    blankWord.setStartIndex(start);
                    blankWord.setEndIndex(start + len);

                    for (int i = lineNo+1; i < blankWordMap.size(); i++){
                        blankWordMap.get(i).setStartIndex(blankWordMap.get(i).getStartIndex() + len -5);
                        blankWordMap.get(i).setEndIndex(blankWordMap.get(i).getEndIndex()+ len - 5);
                    }
            }
        }
    }


    private void findBlankClicked(int start, int end) {

        for (int lineNo = 0; lineNo < blankWordMap.size() ; lineNo++){
            Word blankWord = blankWordMap.get(lineNo);
            if (blankWord != null && (start >= blankWord.getStartIndex() && end <= blankWord.getEndIndex())){
                blankAnswer[0] = String.valueOf(lineNo);
                blankAnswer[1] =  blankWord.getWord();
            }
        }
        if (blankAnswer[0] == null ){
            utilities.shortToast("No match!");
        }
    }

    private void onAppRefresh()
    {
        blankWordMap = new HashMap<Integer, Word>();
        score = 0;

        if (optionsGrid.getVisibility() == View.VISIBLE){
            optionsGrid.setVisibility(View.GONE);
        }

        getContent = new GetWikipediaContentAsync(MainActivity.this, this, blankWordMap);
        getContent.execute();
    }

    private void modifyScore(String[] blankAnswer) {
        if (blankAnswer[1].equals(blankAnswer[2])){
            score += 1;
        }
    }

    @Override
    protected void onPause() {
        super.onPause();
        if (progressDialog.isShowing()){
            progressDialog.dismiss();
        }
        if (getContent.mDialog.isShowing()){
            getContent.mDialog.cancel();
        }
    }

    @Override
    protected void onSaveInstanceState(Bundle outState) {
        super.onSaveInstanceState(outState);
    }

    @Override
    protected void onDestroy() {
        super.onDestroy();
        if (progressDialog.isShowing()){
            progressDialog.dismiss();
        }
        if (getContent.mDialog.isShowing()){
            getContent.mDialog.cancel();
        }
        getContent.cancel(true);
    }

    @Override
    protected void onStop() {
        super.onStop();
    }
}