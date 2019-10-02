package uk.co.vaguelycompetent.hackerman;

import android.content.DialogInterface;
import android.graphics.Typeface;
import android.os.Bundle;
import android.support.design.widget.BottomSheetBehavior;
import android.support.v4.app.FragmentManager;
import android.support.v4.content.ContextCompat;
import android.support.v4.widget.NestedScrollView;
import android.support.v7.app.AlertDialog;
import android.support.v7.app.AppCompatActivity;
import android.support.v7.widget.CardView;
import android.view.Gravity;
import android.view.KeyEvent;
import android.view.View;
import android.widget.ImageView;
import android.widget.LinearLayout;
import android.widget.TextView;
import android.widget.Toast;

import org.json.JSONArray;
import org.json.JSONException;
import org.json.JSONObject;

import java.io.IOException;
import java.io.InputStream;
import java.io.InputStreamReader;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.Locale;
import java.util.Map;
import java.util.Random;

import static android.util.TypedValue.COMPLEX_UNIT_PT;


public class GameActivity extends AppCompatActivity{
    public LinearLayout questionSelectLL;
    public LinearLayout answersLL;
    public NestedScrollView answerSheet;
    public TextView questionTitle;
    private BottomSheetBehavior mBottomSheetBehavior;
    boolean showingCats = true;
    boolean showingQuestion = false;

    HashMap<String, Integer> categories;

    ArrayList<Question> easyQuestions;
    ArrayList<Question> mediumQuestions;
    ArrayList<Question> hardQuestions;
    ArrayList<Question> specialQuestions;

    @Override
    @SuppressWarnings("unchecked")
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_game);

        categories = new HashMap<>();
        easyQuestions = new ArrayList<>();
        mediumQuestions = new ArrayList<>();
        hardQuestions = new ArrayList<>();
        specialQuestions = new ArrayList<>();

        if(savedInstanceState != null){
            // This gives Unchecked assignment warnings but I've suppressed them because I trust me
            categories = (HashMap)savedInstanceState.getSerializable("categories");
            easyQuestions = (ArrayList)savedInstanceState.getSerializable("easyQ");
            mediumQuestions = (ArrayList)savedInstanceState.getSerializable("medQ");
            hardQuestions = (ArrayList)savedInstanceState.getSerializable("hardQ");
            specialQuestions = (ArrayList)savedInstanceState.getSerializable("specQ");
        } else {
            parseJSON();
        }


        questionSelectLL = (LinearLayout) findViewById(R.id.questionSelectLL);

        answerSheet = (NestedScrollView) findViewById(R.id.bottom_sheet);
        answersLL = (LinearLayout) findViewById(R.id.answersLL);
        mBottomSheetBehavior = BottomSheetBehavior.from(answerSheet);

        displayCategories();
    }

    @Override
    public boolean onKeyDown(int keyCode, KeyEvent e) {
        // back button is pressed and we showing the categories
        if (keyCode == KeyEvent.KEYCODE_BACK && showingCats) {
            // Ask the user if they really want to leave
            AlertDialog.Builder builder = new AlertDialog.Builder(this);
            builder.setTitle(R.string.leave_game_title);
            builder.setMessage(R.string.are_sure);

            builder.setPositiveButton(R.string.yes, new DialogInterface.OnClickListener() {
                public void onClick(DialogInterface dialog, int which) {
                    finish();
                    dialog.dismiss();
                }
            });
            builder.setNegativeButton(R.string.no, new DialogInterface.OnClickListener() {
                @Override
                public void onClick(DialogInterface dialog, int which) {
                    // Do nothing
                    dialog.dismiss();
                }
            });
            AlertDialog alert = builder.create();
            alert.show();
            return true;
        } else if(keyCode == KeyEvent.KEYCODE_BACK && showingQuestion){
            Toast.makeText(this, R.string.back_question, Toast.LENGTH_LONG).show();
            return true; // Don't let users escape a question they don't like
        } else if (keyCode == KeyEvent.KEYCODE_BACK){ // User pressed a category but wants to go back
            displayCategories(); // Show the categories again, user probably pressed the wrong one
            return true; // Don't let it continue cause it'll just close the app
        }
        return super.onKeyDown(keyCode, e);
    }

    public void displayCategories(){
        showingQuestion = false;
        showingCats = true;
        questionSelectLL.removeAllViews(); // Probs make this whole block its own method but tbh idc
        try {
            answersLL.removeViews(1, 4);
        } catch (Exception e){
            // Not an issue, just the first run or something weird that I don't care about
        }
        answerSheet.setVisibility(View.GONE);
        mBottomSheetBehavior.setState(BottomSheetBehavior.STATE_COLLAPSED);

        for (Map.Entry<String, Integer> entries:categories.entrySet()) {
            // These aren't questions but the method handles it fine soo
            if(entries.getValue() != 0) { // if there's no more questions matching this category don't you dare show it
                Question temp = new Question(4, entries.getKey(), entries.getValue().toString() + getString(R.string.questions_available), 0, null);
                createHeaderCard(temp, questionSelectLL, false);
            }
        }
    }

    public void displayQuestions(String category){
        showingCats = false;
        questionSelectLL.removeAllViews(); // Probs make this whole block its own method but tbh idc
        try {
            answersLL.removeViews(1, 4);
        } catch (Exception e){
            // Not an issue, just the first run or something weird that I don't care about
        }
        answerSheet.setVisibility(View.GONE);
        mBottomSheetBehavior.setState(BottomSheetBehavior.STATE_COLLAPSED);
        ArrayList<Question> tempList; // Used to allow modification of the list without actually modifying the list

        Random rng = new Random();

        easySelection: //
        if(easyQuestions.size() > 0) {
            Question q;
            tempList = new ArrayList<>(easyQuestions);

            q = tempList.get(0); // init with first object in list
            tempList.remove(q); // remove object so it doesn't get processed again
            for(;;) { // this all looks horrible but it works
                if (q.selectorSubText.equalsIgnoreCase(category)) {
                    createHeaderCard(q, questionSelectLL, true);
                    break easySelection;
                }
                else{
                    if(tempList.isEmpty()) {
                        break easySelection;
                    }
                    q = tempList.get(rng.nextInt(tempList.size()));
                    tempList.remove(q);
                }
            }
        }
        mediumSelection: //
        if(mediumQuestions.size() > 0) {
            Question q;
            tempList = new ArrayList<>(mediumQuestions);

            q = tempList.get(0); // init with first object in list
            tempList.remove(q); // remove object so it doesn't get processed again
            for(;;){ // this all looks horrible but it works?
                if (q.selectorSubText.equalsIgnoreCase(category)) {
                    createHeaderCard(q, questionSelectLL, true);
                    break mediumSelection;
                }
                else{
                    if(tempList.isEmpty())
                        break mediumSelection;
                    q = tempList.get(rng.nextInt(tempList.size()));
                    tempList.remove(q);
                }
            }
        }
        hardSelection: //
        if(hardQuestions.size() > 0) {
            Question q;
            tempList = new ArrayList<>(hardQuestions);

            q = tempList.get(0); // init with first object in list
            tempList.remove(q); // remove object so it doesn't get processed again
            for(;;){ // this all looks horrible but it works?
                if (q.selectorSubText.equalsIgnoreCase(category)) {
                    createHeaderCard(q, questionSelectLL, true);
                    break hardSelection;
                }
                else{
                    if(tempList.isEmpty())
                        break hardSelection;
                    q = tempList.get(rng.nextInt(tempList.size()));
                    tempList.remove(q);
                }
            }
        }
        specialSelection:
        if(specialQuestions.size() > 0) {
            Question q;
            tempList = new ArrayList<>(specialQuestions);

            q = tempList.get(0); // init with first object in list
            tempList.remove(q); // remove object so it doesn't get processed again
            for(;;){ // this all looks horrible but it works?
                if (q.selectorSubText.equalsIgnoreCase(category)) {
                    createHeaderCard(q, questionSelectLL, true);
                    break specialSelection;
                }
                else{
                    if(tempList.isEmpty())
                        break specialSelection;
                    q = tempList.get(rng.nextInt(tempList.size()));
                    tempList.remove(q);
                }
            }
        }
    }

    public void createHeaderCard(final Question q, LinearLayout target, boolean isQuestion){
        CardView questionCard = new CardView(this);
        LinearLayout cardLL = new LinearLayout(this);
        ImageView sideImage = new ImageView(this);
        LinearLayout textLL = new LinearLayout(this);
        TextView headerTV = new TextView(this);
        TextView subTV = new TextView(this);

        LinearLayout.LayoutParams cardParams = new LinearLayout.LayoutParams(LinearLayout.LayoutParams.MATCH_PARENT, LinearLayout.LayoutParams.WRAP_CONTENT);
        cardParams.bottomMargin = 10;
        questionCard.setLayoutParams(cardParams);
        questionCard.setCardBackgroundColor(ContextCompat.getColor(this, R.color.cardview_dark_background));
        questionCard.setForeground(ContextCompat.getDrawable(this, R.drawable.ripple));

        LinearLayout.LayoutParams imageParams = new LinearLayout.LayoutParams(100,100);
        imageParams.gravity= Gravity.CENTER_VERTICAL;
        sideImage.setLayoutParams(imageParams);

        textLL.setPadding(5,0,0,0);
        textLL.setOrientation(LinearLayout.VERTICAL);
        headerTV.setTextSize(COMPLEX_UNIT_PT, 15);
        headerTV.setTypeface(Typeface.DEFAULT, 1);
        subTV.setTextSize(COMPLEX_UNIT_PT, 10);
        subTV.setTypeface(Typeface.DEFAULT, 2);


        textLL.addView(headerTV);
        textLL.addView(subTV);

        cardLL.addView(sideImage);
        cardLL.addView(textLL);

        questionCard.addView(cardLL);

        target.addView(questionCard);

        if(isQuestion) { // creating a question
            subTV.setText(q.selectorSubText);
            switch (q.difficulty) {
                case 1:
                    sideImage.setImageDrawable(ContextCompat.getDrawable(this, R.drawable.star_outline));
                    headerTV.setText(R.string.easy);
                    break;
                case 2:
                    sideImage.setImageDrawable(ContextCompat.getDrawable(this, R.drawable.star_half));
                    headerTV.setText(R.string.medium);
                    break;
                case 3:
                    sideImage.setImageDrawable(ContextCompat.getDrawable(this, R.drawable.star));
                    headerTV.setText(R.string.hard);
                    break;
                case 4:
                    sideImage.setImageDrawable(ContextCompat.getDrawable(this, R.drawable.star_circle));
                    headerTV.setText(R.string.special);
                    break;

            }

            View.OnClickListener Listener = new View.OnClickListener() {
                @Override
                public void onClick(View v) {
                    answerSheet.setVisibility(View.VISIBLE);
                    mBottomSheetBehavior.setState(BottomSheetBehavior.STATE_EXPANDED);
                    createQuestionCards(q);

                    // decrement the counter for this category
                    categories.put(q.selectorSubText, (categories.get(q.selectorSubText) - 1));
                }
            };
            questionCard.setOnClickListener(Listener);
        } else { // creating a category
            // frankly this should be nicer but it works so eh whatevs
            // yeah i use Question objects weird to make this work please don't hate
            headerTV.setText(q.selectorSubText);
            subTV.setText(q.questionTitle);
            View.OnClickListener Listener = new View.OnClickListener() {
                @Override
                public void onClick(View v) {
                    String r = q.selectorSubText;
                    displayQuestions(r);
                }
            };
            questionCard.setOnClickListener(Listener);
        }
    }


    public void createQuestionCards(final Question q){
        showingQuestion = true;
        // Protect against testing
        if(q.answers == null)
            return;

        questionTitle = (TextView) findViewById(R.id.questionTitle);
        questionTitle.setText(q.questionTitle);
        for (int i = 0; i < q.answers.length; i++) {
            CardView answerCard = new CardView(this);
            TextView answerText = new TextView(this);
            final int answerId = i;
            final int correct = q.correctAnswer;

            LinearLayout.LayoutParams params = new LinearLayout.LayoutParams(
                    LinearLayout.LayoutParams.MATCH_PARENT,
                    LinearLayout.LayoutParams.WRAP_CONTENT);
            params.setMargins(16,16,16,0);

            answerText.setText(q.answers[i]);
            answerText.setPadding(16,16,16,16);
            answerText.setTextSize(COMPLEX_UNIT_PT, 12);

            answerCard.addView(answerText);
            answerCard.setLayoutParams(params);
            answerCard.setPadding(16,16,16,16);

            answersLL.addView(answerCard);

            View.OnClickListener Listener = new View.OnClickListener() {
                @Override
                public void onClick(View v) {
                    if(answerId == correct){
                        FragmentManager fm = getSupportFragmentManager();
                        AnswerDialog dialogFragment = new AnswerDialog ();
                        Bundle args = new Bundle();
                        args.putBoolean("correct", true);
                        args.putInt("difficulty", q.difficulty);
                        dialogFragment.setArguments(args);
                        dialogFragment.show(fm, "correct");
                    } else {
                        FragmentManager fm = getSupportFragmentManager();
                        AnswerDialog dialogFragment = new AnswerDialog ();
                        Bundle args = new Bundle();
                        args.putBoolean("correct", false);
                        args.putInt("difficulty", q.difficulty);
                        dialogFragment.setArguments(args);
                        dialogFragment.show(fm, "wrong");
                    }
                    displayCategories(); // return back to the category selector
                }
            };
            answerCard.setOnClickListener(Listener);
            switch (q.difficulty){ // remove question from list, prevent repeats
                case 1: easyQuestions.remove(q);
                    break;
                case 2: mediumQuestions.remove(q);
                    break;
                case 3: hardQuestions.remove(q);
                    break;
                case 4: specialQuestions.remove(q);
                    break;
            }
        }
    }

    private String getJSONString()
    {
        String str = "";
        try
        {
            InputStream in;
            if(Locale.getDefault().getLanguage().equalsIgnoreCase("nl")) { // use dutch questions
                in = getResources().openRawResource(R.raw.question_nl);
            } else { // just use the english ones its k
                in = getResources().openRawResource(R.raw.question_en);
            }
            InputStreamReader isr = new InputStreamReader(in);
            char [] inputBuffer = new char[100];

            int charRead;
            while((charRead = isr.read(inputBuffer))>0)
            {
                String readString = String.copyValueOf(inputBuffer,0,charRead);
                str += readString;
            }
        }
        catch(IOException ioe)
        {
            // Hopefully this never happens because this doesn't count as handled
            Toast.makeText(this, "Something went very wrong, please try again", Toast.LENGTH_LONG).show();
        }
        return str;
    }

    public void parseJSON()
    {
        try {
            JSONObject json = new JSONObject(getJSONString()); // Get the full json object
            for(int i = 1; i <= 4; i++){
                JSONArray questions = new JSONArray();
                switch (i){ // get the appropriate array
                    case 1: questions = json.getJSONArray("easy");
                        break;
                    case 2: questions = json.getJSONArray("medium");
                        break;
                    case 3: questions = json.getJSONArray("hard");
                        break;
                    case 4: questions = json.getJSONArray("special");
                        break;
                }

                for (int j = 0; j < questions.length(); j++){
                    JSONObject o = questions.getJSONObject(j);
                    JSONObject answersJson = o.getJSONObject("answers");
                    String category = o.getString("category");
                    String[] answers = new String[answersJson.length()];
                    for(int k = 0; k < answersJson.length(); k++) {
                        answers[k] = answersJson.getString(Integer.toString(k));
                    }
                    Question q = new Question(i, category, o.getString("title"), o.getInt("correctAnswer"), answers);
                    if(categories.containsKey(category)) {
                        // this category has already been recorded, increment it instead
                        categories.put(category, categories.get(category) + 1);
                    } else {
                        categories.put(category, 1);
                    }

                    switch (i){ // add question to the appropriate list
                        case 1: easyQuestions.add(j, q);
                            break;
                        case 2: mediumQuestions.add(j, q);
                            break;
                        case 3: hardQuestions.add(j, q);
                            break;
                        case 4: specialQuestions.add(j, q);
                            break;
                    }
                }
            }
        } catch (JSONException e) { // Lets just hope this doesn't happen cause lol I ain't handling that
            e.printStackTrace();
        }
    }


    @Override
    public void onSaveInstanceState(Bundle savedInstanceState) {
        // Always call the superclass so it can save the view hierarchy state
        super.onSaveInstanceState(savedInstanceState);
        savedInstanceState.putSerializable("categories", categories);
        savedInstanceState.putSerializable("easyQ", easyQuestions);
        savedInstanceState.putSerializable("medQ", mediumQuestions);
        savedInstanceState.putSerializable("hardQ", hardQuestions);
        savedInstanceState.putSerializable("specQ", specialQuestions);
    }
}
