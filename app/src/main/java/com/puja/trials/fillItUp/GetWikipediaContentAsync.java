package com.puja.trials.fillItUp;

import android.app.ProgressDialog;
import android.content.Context;
import android.os.AsyncTask;
import android.util.Log;

import org.json.JSONException;
import org.json.JSONObject;

import java.io.IOException;
import java.text.BreakIterator;
import java.util.ArrayList;
import java.util.Locale;
import java.util.Map;
import java.util.Random;
import java.util.regex.Matcher;
import java.util.regex.Pattern;

import okhttp3.OkHttpClient;
import okhttp3.Request;
import okhttp3.Response;

/**
 * Created by puja on 22/01/17.
 */

public class GetWikipediaContentAsync extends AsyncTask<Void, Void, String> {

    Context context;
    public AsyncResponse mDelegate = null;
    public ProgressDialog mDialog;
    Map<Integer, Word> blankWordMap;
    ArrayList<String> options;
    private static final String url = "https://en.wikipedia.org/w/api.php?format=json&action=query&generator=random&grnnamespace=0&prop=extracts&explaintext=";

    public interface AsyncResponse {
        void processFinish(String paragraph, ArrayList<String> options1);
    }

    public GetWikipediaContentAsync(Context context, AsyncResponse delegate, Map<Integer, Word> blankWordMap)
    {
        this.context = context;
        this.mDelegate = delegate;
        this.blankWordMap = blankWordMap;
        mDialog = new ProgressDialog(context);
        options = new ArrayList<String>();
    }

    @Override
    protected void onProgressUpdate(Void... values) {
        super.onProgressUpdate(values);

    }

    @Override
    protected void onPreExecute() {
        super.onPreExecute();
        mDialog.setMessage("Loading..");
        mDialog.setCancelable(false);
        mDialog.show();
    }

    @Override
    protected void onCancelled() {
        super.onCancelled();
        if (mDialog.isShowing()) {
            mDialog.cancel();
        }
    }

    @Override
    protected String doInBackground(Void... voids) {

        String data = null;
            try {
                do {
                    OkHttpClient client = new OkHttpClient();
                    Request request = new Request.Builder()
                                .url(url)
                                .build();

                    Response response = client.newCall(request).execute();
                    data = response.body().string();

                    JSONObject jsonObject = new JSONObject(data).getJSONObject("query").getJSONObject("pages");
                    String key = jsonObject.keys().next();
                    data = jsonObject.getJSONObject(key).getString("extract");

                } while (data == null || data.length() < 2000);

                data = data.replaceAll("\\[[^\\]]*\\]", "");
                data = findBlankWords(data);

                Log.d("doInBackground", "finished");
            } catch (IOException e) {
                e.printStackTrace();
            } catch (JSONException e) {
                e.printStackTrace();
            }

        return data;
    }

    @Override
    protected void onPostExecute(String result) {
        super.onPostExecute(result);
        Log.d("onPostExecute", "finished");
        if (mDialog.isShowing()) {
            mDialog.dismiss();
        }
        mDelegate.processFinish(result, options);
    }

    private String findBlankWords(String text)
    {
        String modifiedText = "", modifiedLine = "";
        BreakIterator iterator = BreakIterator.getSentenceInstance(Locale.US);
        iterator.setText(text);

        int start = iterator.first();
        int noOfLines = 0, blankNo = 0, noOfWords=0;
        int endIndexWord, startIndexWord;
//noOfLines < 10
        for (int end = iterator.next(); end != BreakIterator.DONE && blankWordMap.size() != 10; start = end, end = iterator.next()) {
            String line = text.substring(start, end);

            if (line.length() >1 )
            {
                Log.d("Line -", start + "-" + line + "-" + end);
                String currentWord = null;
                String[] wordsInLine = Pattern.compile("\\s+").split(line);
                noOfWords = wordsInLine.length;

                do {
                    if (noOfWords > 1)
                    {
                        int position = generateRandomNumber(noOfWords);
                        currentWord = wordsInLine[position];

                        if (checkAllowedWord(currentWord))
                        {
                            startIndexWord = line.indexOf(currentWord);
                            endIndexWord = startIndexWord + currentWord.length() - 1 ;
                            if (startIndexWord > 0 && line.charAt(endIndexWord+1) == ' '){

                            }
                            if ((startIndexWord == 0 || line.charAt(startIndexWord-1) == ' ') && line.charAt(endIndexWord+1) == ' ') {
                                blankNo = noOfLines + 1;
                                String toReplace = "(" + blankNo + ") " + "_____";
                                //addWordToHashMap(noOfLines, start, end, currentWord);
                                modifiedLine = line.replaceFirst(currentWord, toReplace);
                                modifiedText = modifiedText.concat(modifiedLine);
                                startIndexWord = modifiedText.indexOf(toReplace) + 4; //+4 to incorporate the parentheses&num
                                //endIndexWord = startIndexWord + toReplace.length() - 1;
                                endIndexWord = startIndexWord + 5;
                                addWordToHashMap(noOfLines, startIndexWord, endIndexWord, currentWord);

                                noOfLines++;
                            }
                        }
                        else {
                            currentWord = null;
                        }
                    }
                }while(currentWord == null);

            }else {
                modifiedText.concat("\n");
            }
        }
        return modifiedText;
    }

    private void addWordToHashMap(int lineNumber, int startIndex, int endIndex, String input)
    {
        Word word = new Word();
        word.setStartIndex(startIndex);
        word.setEndIndex(endIndex);
        word.setWord(input);

        Log.d("Word - ", word.getWord() + startIndex + " " + endIndex);

        blankWordMap.put(lineNumber, word);
        options.add(input);
    }

    private boolean checkAllowedWord(String inputWord) {

        Pattern pattern = Pattern.compile("[^a-z ]", Pattern.CASE_INSENSITIVE);
        Matcher matcher = pattern.matcher(inputWord);
        if (matcher.find())
            return false;

        return true;
    }

    private int generateRandomNumber(int endRange)
    {
        Random random = new Random();
        int position = random.nextInt(endRange);

        return position;
    }

}
