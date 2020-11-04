/* Copyright 2016 Google Inc.
 *
 * Licensed under the Apache License, Version 2.0 (the "License");
 * you may not use this file except in compliance with the License.
 * You may obtain a copy of the License at
 *
 *     http://www.apache.org/licenses/LICENSE-2.0
 *
 * Unless required by applicable law or agreed to in writing, software
 * distributed under the License is distributed on an "AS IS" BASIS,
 * WITHOUT WARRANTIES OR CONDITIONS OF ANY KIND, either express or implied.
 * See the License for the specific language governing permissions and
 * limitations under the License.
 */

package com.google.engedu.wordstack;

import android.content.res.AssetManager;
import android.graphics.Color;
import android.support.v7.app.AppCompatActivity;
import android.os.Bundle;
import android.view.DragEvent;
import android.view.MotionEvent;
import android.view.View;
import android.view.ViewGroup;
import android.widget.Button;
import android.widget.LinearLayout;
import android.widget.TextView;
import android.widget.Toast;

import java.io.BufferedReader;
import java.io.IOException;
import java.io.InputStream;
import java.io.InputStreamReader;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.Random;
import java.util.Stack;

import static android.graphics.Color.WHITE;
import static com.google.engedu.wordstack.R.color.design_default_color_primary_dark;

/* code bugs:
sometimes we are given more than 10 letters to place - occurs when you ask for a new game before finishing the old one
*/

public class MainActivity extends AppCompatActivity {

    private static final int WORD_LENGTH = 5;
    public static final int LIGHT_BLUE = Color.rgb(176, 200, 255);
    public static final int LIGHT_GREEN = Color.rgb(200, 255, 200);
    public static final int LIGHT_RED = Color.rgb(255,114,111);
    private ArrayList<String> words = new ArrayList<>();
    private HashMap<Integer,ArrayList<String>> wordsMap = new HashMap();
    private Random random = new Random();
    private StackedLayout stackedLayout;
    private String word1, word2;

    private Boolean ended = false;
    private int level = 1;
    private Stack<LetterTile> placedTiles = new Stack<>();


    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_main);
        AssetManager assetManager = getAssets();
        try {
            InputStream inputStream = assetManager.open("words.txt");
            BufferedReader in = new BufferedReader(new InputStreamReader(inputStream));
            String line = null;
            while((line = in.readLine()) != null) {
                String word = line.trim();
                if (wordsMap.containsKey(word.length())){
                    wordsMap.get(word.length()).add(word);
                }
                else {
                    ArrayList<String> newList = new ArrayList<>();
                    newList.add(word);
                    wordsMap.put(word.length(),newList);
                }
                if (word.length()==WORD_LENGTH){
                    words.add(word);
                }
            }
        } catch (IOException e) {
            Toast toast = Toast.makeText(this, "Could not load dictionary", Toast.LENGTH_LONG);
            toast.show();
        }
        LinearLayout verticalLayout = (LinearLayout) findViewById(R.id.vertical_layout);
        stackedLayout = new StackedLayout(this);
        verticalLayout.addView(stackedLayout, 3);
        toggleUndoButton(false);

        View word1LinearLayout = findViewById(R.id.word1);
        //word1LinearLayout.setOnTouchListener(new TouchListener());
        word1LinearLayout.setOnDragListener(new DragListener());
        View word2LinearLayout = findViewById(R.id.word2);
        //word2LinearLayout.setOnTouchListener(new TouchListener());
        word2LinearLayout.setOnDragListener(new DragListener());
    }

    private class TouchListener implements View.OnTouchListener {

        @Override
        public boolean onTouch(View v, MotionEvent event) {
            if (event.getAction() == MotionEvent.ACTION_DOWN && !stackedLayout.empty()) {
                LetterTile tile = (LetterTile) stackedLayout.peek();
                tile.moveToViewGroup((ViewGroup) v);
                if (stackedLayout.empty()) {
                    TextView messageBox = (TextView) findViewById(R.id.message_box);
                    messageBox.setText(word1 + " " + word2);
                }
                placedTiles.push(tile);
                return true;
            }
            return false;
        }
    }

    private class DragListener implements View.OnDragListener {

        public boolean onDrag(View v, DragEvent event) {
            int action = event.getAction();
            switch (event.getAction()) {
                case DragEvent.ACTION_DRAG_STARTED:
                    v.setBackgroundColor(LIGHT_BLUE);
                    v.invalidate();
                    return true;
                case DragEvent.ACTION_DRAG_ENTERED:
                    v.setBackgroundColor(LIGHT_GREEN);
                    v.invalidate();
                    return true;
                case DragEvent.ACTION_DRAG_EXITED:
                    v.setBackgroundColor(LIGHT_BLUE);
                    v.invalidate();
                    return true;
                case DragEvent.ACTION_DRAG_ENDED:
                    if(!ended){
                        v.setBackgroundColor(WHITE);
                    }
                    v.invalidate();
                    return true;
                case DragEvent.ACTION_DROP:
                    // Dropped, reassign Tile to the target Layout
                    LetterTile tile = (LetterTile) event.getLocalState();
                    Integer childCount = (((ViewGroup) v).getChildCount());
                    if (childCount<WORD_LENGTH) {
                        tile.moveToViewGroup((ViewGroup) v);
                        toggleUndoButton(true);
                        if (stackedLayout.empty()) {
                            TextView messageBox = findViewById(R.id.message_box);
                            LinearLayout word1LinearLayout = findViewById(R.id.word1);
                            LinearLayout word2LinearLayout = findViewById(R.id.word2);
                            String firstBoxWord = findLetters((ViewGroup) word1LinearLayout);
                            String secondBoxWord = findLetters((ViewGroup) word2LinearLayout);
                            String userWord = findLetters((ViewGroup) v);
                            Boolean colourChangeCondition = firstBoxWord.equals(word1) || firstBoxWord.equals(word2);
                            if (colourChangeCondition){
                                messageBox.setTextColor(Color.GREEN);
                                word1LinearLayout.setBackgroundColor(LIGHT_GREEN);
                                word2LinearLayout.setBackgroundColor(LIGHT_GREEN);
                                messageBox.setText(R.string.correct_guess_text);
                                level+=1;
                            }else if(words.contains(firstBoxWord)&&words.contains(secondBoxWord)){
                                messageBox.setTextColor(LIGHT_BLUE);
                                word1LinearLayout.setBackgroundColor(LIGHT_BLUE);
                                word2LinearLayout.setBackgroundColor(LIGHT_BLUE);
                                messageBox.setText("These words are valid! The intended words were: "+word1 + ", " + word2);
                                level+=1;
                            }
                            else {
                                messageBox.setTextColor(Color.RED);
                                word1LinearLayout.setBackgroundColor(LIGHT_RED);
                                word2LinearLayout.setBackgroundColor(LIGHT_RED);
                                messageBox.setText("Unlucky, the words were: "+word1 + ", " + word2);
                                level = 1;
                            }
                            //messageBox.setText(word1 + " " + word2);
                            //messageBox.setText(firstBoxWord);
                            toggleUndoButton(false);
                            ended =true;
                        }
                        placedTiles.push(tile);
                    }
                    //probably should be in the if statement -  check if any bugs come up
                    return true;
            }
            return false;
        }
    }

    public boolean onStartGame(View view) {
        placedTiles.clear();
        ended=false;
        Button startButton = findViewById(R.id.start_button);
        startButton.setText("NEW GAME");
        toggleUndoButton(false);
        LinearLayout word1LinearLayout = findViewById(R.id.word1);
        word1LinearLayout.removeAllViews();
        word1LinearLayout.setBackgroundColor(WHITE);
        LinearLayout word2LinearLayout = findViewById(R.id.word2);
        word2LinearLayout.removeAllViews();
        word2LinearLayout.setBackgroundColor(WHITE);
        stackedLayout.removeAllViews();
        stackedLayout.clear();
        TextView messageBox = findViewById(R.id.message_box);
        messageBox.setTextColor(getResources().getColor(design_default_color_primary_dark));
        messageBox.setText("Level "+ level +": The words are "+(level+4)+" characters long");
        ArrayList<String> possibleWords = wordsMap.get(level+4);
        word1 = possibleWords.get(random.nextInt(possibleWords.size()));
        word2 = possibleWords.get(random.nextInt(possibleWords.size()));
        /*
        messageBox.setText("The words are "+WORD_LENGTH+" characters long");
        int wordsSize =words.size();
        word1 =words.get(random.nextInt(wordsSize));
        word2 = words.get(random.nextInt(wordsSize));*/
        int word1_counter = 0;
        int word2_counter = 0;
        int word1_size = word1.length();
        int word2_size = word2.length();
        StringBuilder scrambledString = new StringBuilder(word1_size+word2_size);
        while (word1_counter<word1_size && word2_counter<word2_size){
            int chosenWord = random.nextInt(2)+1;
            if (chosenWord==1){
                scrambledString.append(word1.charAt(word1_counter));
                word1_counter++;
            }
            else if (chosenWord==2){
                scrambledString.append(word2.charAt(word2_counter));
                word2_counter++;
            }
            else{
                throw new IndexOutOfBoundsException("generating random numbers outside of 1-2");
            }
        }
        while (word1_counter<word1_size){
            scrambledString.append(word1.charAt(word1_counter));
            word1_counter++;
        }
        while (word2_counter<word2_size){
            scrambledString.append(word2.charAt(word2_counter));
            word2_counter++;
        }
        //messageBox.setText(word1+ " "+ word2+" "+ scrambledString);
        char[] stringScrambled = scrambledString.reverse().toString().toCharArray();
        for (char c: stringScrambled){
            LetterTile letterTile = new LetterTile(this,c);
            stackedLayout.push(letterTile);
        }
        return true;
    }

    public boolean onUndo(View view) {
        if (!placedTiles.empty()) {
            LetterTile unplacedTile = placedTiles.pop();
            unplacedTile.moveToViewGroup(stackedLayout);
        }
        if (placedTiles.empty())
            {toggleUndoButton(false);}
        return true;

    }

    private void toggleUndoButton(boolean enable){
        Button undoButton =findViewById(R.id.undo_button);
        undoButton.setEnabled(enable);
    }

    private String findLetters(ViewGroup v){
        StringBuilder res = new StringBuilder();
        for (int i = 0; i < v.getChildCount(); i++) {
            LetterTile child = (LetterTile) v.getChildAt(i);
            res.append(child.getLetter());
        }
        return String.valueOf(res);
    }
}
