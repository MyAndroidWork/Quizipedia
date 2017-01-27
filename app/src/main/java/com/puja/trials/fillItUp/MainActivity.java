package com.puja.trials.fillItUp;

import android.app.ProgressDialog;
import android.graphics.Color;
import android.graphics.Typeface;
import android.os.Bundle;
import android.support.v7.app.AppCompatActivity;
import android.text.Editable;
import android.text.Spanned;
import android.util.Log;
import android.view.View;
import android.view.animation.Animation;
import android.view.animation.AnimationUtils;
import android.widget.AdapterView;
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
    Map<Integer, Word> blankWordMap;
    GetWikipediaContentAsync getContent;
    OptionsAdapter adapter;
    final String[] blankAnswer = new String[3];
    Utilities utilities;
    ProgressDialog progressDialog;
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
            optionsGrid = (GridView) findViewById(R.id.options_gridview);
            iv_submit = (ImageView) findViewById(R.id.iv_submit);
            iv_refresh = (ImageView) findViewById(R.id.iv_refresh);

            onAppRefresh();

            Typeface textFont = Typeface.createFromAsset(getAssets(), "fonts/Bariol_Regular.otf");
            mainText.setTypeface(textFont);

            iv_submit.setOnClickListener(new View.OnClickListener() {
                @Override
                public void onClick(View view) {

                    utilities.longToast("Score-" + score);
                }
            });

            iv_refresh.setOnClickListener(new View.OnClickListener() {
                @Override
                public void onClick(View view) {
                    onAppRefresh();
                }
            });


        }
        else {
            utilities.longToast("No Internet Connection Available!");
            //TODO - open network settings dialog to toggle wifi
        }
    }

    @Override
    public void processFinish(final String modifiedParagraph, ArrayList<String> options) {

        final ArrayList<String> blankOptions = options;

        if (modifiedParagraph == null)
        {
            utilities.shortToast("Could not find any text!");
        }else {
            mainText.setText(modifiedParagraph);

            if (options != null){
                Collections.shuffle(options);
                adapter = new OptionsAdapter(this, options);
                optionsGrid.setAdapter(adapter);
            }else {
                utilities.longToast("No options to show!");
            }

            new PatternEditableBuilder().addPattern(Pattern.compile("_____"), Color.parseColor("#ff0099cc"),
                    new PatternEditableBuilder.SpannableClickedListener() {

                        @Override
                        public void onSpanClicked(final String text, final View view, final int start, final int end) {
                            Log.d("Clicked Blank", start + "--" + end);
                            blankAnswer[0] = text;

                            if (optionsGrid.getVisibility() != View.VISIBLE)
                            {
                                optionsGrid.setVisibility(View.VISIBLE);
                                Animation bottomUp = AnimationUtils.loadAnimation(MainActivity.this, R.anim.bottom_up  );
                                optionsGrid.startAnimation(bottomUp);
                            }

                            findBlankClicked(start, end);
                            if (blankAnswer[0] == null ){
                                utilities.shortToast("No match!");

                            }

                            optionsGrid.setOnItemClickListener(new AdapterView.OnItemClickListener() {
                                @Override
                                public void onItemClick(AdapterView<?> adapterView, View view, int i, long l) {
                                /*    if (view.getAlpha() == 1.0f) {
                                        view.setAlpha(0.50f);
                                    }else {
                                        view.setAlpha(1.0f);
                                    }  */
                                    blankAnswer[2] = blankOptions.get(i);
                                    Animation bottomDown = AnimationUtils.loadAnimation(MainActivity.this, R.anim.bottom_down  );
                                    optionsGrid.startAnimation(bottomDown);
                                    optionsGrid.setVisibility(View.GONE);

                                    text.replace("_____", blankAnswer[2]);

                                    modifyScore(blankAnswer);

                                /*    Editable textViews = (Editable) view;
                                    Log.d("Values - ", start + "----" + end );
                                    textViews.replace(start, end, blankAnswer[1]);
                                /*    Spanned span = (Spanned) textViews.getText();
                                    String text = String.valueOf(span.subSequence(start, end));
                                    text.replace(text, blankAnswer[1]);

                                /*    if (blankAnswer[2]!= null && blankAnswer[2].length() > 0){
                                        blankAnswer[0].replace(blankAnswer[0], blankAnswer[1]);
                                    } */
                                }
                            });


                        }

                    }).into(mainText);
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
    }

    private void onAppRefresh()
    {
        blankWordMap = new HashMap<Integer, Word>();

        if (optionsGrid.getVisibility() == View.VISIBLE){
            optionsGrid.setVisibility(View.GONE);
        }

        getContent = new GetWikipediaContentAsync(MainActivity.this, this, blankWordMap);
        getContent.execute();
    }

    private void modifyScore(String[] blankAnswer) {
        if (blankWordMap.get(Integer.parseInt(blankAnswer[0])).getWord().equals(blankAnswer[2])){
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