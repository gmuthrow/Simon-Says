package com.example.connors_yu_final;

import androidx.appcompat.app.AppCompatActivity;

import android.content.Context;
import android.content.Intent;
import android.graphics.Bitmap;
import android.graphics.BitmapFactory;
import android.graphics.Color;
import android.os.Bundle;
import android.os.Handler;
import android.os.Message;
import android.view.View;
import android.widget.Button;
import android.widget.ImageView;
import android.widget.TextView;
import android.widget.Toast;

import java.util.ArrayList;
import java.util.HashMap;
import java.util.ListIterator;
import java.util.Random;

public class simon_motion extends AppCompatActivity {

private final static int GREEN = 1;
    private final static int RED = 2;
    private final static int YELLOW = 3;
    private final static int BLUE = 4;

State curr_state = new State("");

    private static Bitmap GreenUnlit, GreenLit;
    private static Bitmap RedUnlit, RedLit;
    private static Bitmap BlueUnlit, BlueLit;
    private static Bitmap YellowUnlit, YellowLit;
    private ImageView gameover;
    Button inputButton, restartButton;

    private static TextView Score;

private static ArrayList<Integer> gameSequence;
    private static ArrayList<Integer> playerSequence;

    private static long mLastMove;
    private static long mMoveDelay;
    private static int playSeqCounter;
    private static int colorTouched;
    private static boolean winOrLose;
    private static int scoreCounter;


    private HashMap<String,Integer> colorToInt = new HashMap<>();
    private ArrayList<Integer> bb;

    private Accelerometer accelerometer;
    private Gyroscope gyroscope;
    Context context;
    buttonHandler SimonButtonHandler = new buttonHandler();


    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_simon);
        context = this;
        getWindow().getDecorView().setBackgroundColor(Color.BLACK);
        accelerometer=new Accelerometer(this);
        gyroscope = new Gyroscope(this);
        colorToInt.put("RED",2);
        colorToInt.put("GREEN",1);
        colorToInt.put("YELLOW",3);
        colorToInt.put("BLUE",4);
        inputButton = findViewById(R.id.motionButton);
        gameover = findViewById(R.id.gameover);
        Score = (TextView)findViewById(R.id.scoreString);
        restartButton = findViewById(R.id.restartButton);
setBitMaps();
}

    private class State {
        String state = "";
        public State() {
        }

        public State(String state) {
            this.state = state;
        }

    }

    public void setAccelerometerListener(){
        accelerometer.setListener(new Accelerometer.Listener() {
            @Override
            public void onTranslation(float tx, float ty, float tz) {

                Toast.makeText(context, "x = " + tx + " y = " + ty + " z = " + tz , Toast.LENGTH_LONG);
                if(tx>1.0f && ty>1.0f){
                    getWindow().getDecorView().setBackgroundColor(Color.RED);
                    System.out.println("x = " + tx + " y = " + ty + " z = " + tz);
                    bb.add(2);

                    accelerometer.unregister();
                }
                else if(tx>1.0f && ty<-1.0f){
                    getWindow().getDecorView().setBackgroundColor(Color.BLUE);
                    System.out.println("x = " + tx + " y = " + ty + " z = " + tz);
                    bb.add(4);
                    accelerometer.unregister();
                }
                else if(tx<-1.0f && ty>1.0f){
                    getWindow().getDecorView().setBackgroundColor(Color.GREEN);
                    System.out.println("x = " + tx + " y = " + ty + " z = " + tz);
                    bb.add(1);

                    accelerometer.unregister();
                } else if(tx<-1.0f && ty<-1.0f){
                    getWindow().getDecorView().setBackgroundColor(Color.YELLOW);
                    System.out.println("x = " + tx + " y = " + ty + " z = " + tz);
                    bb.add(3);
                    accelerometer.unregister();
                }

            }
        });

    }

    public void setGyroscopeListener(){
        gyroscope.setListener(new Gyroscope.Listener() {
            @Override
            public void onTranslation(float tx, float ty, float tz) {
if(tx>2.0f && ty>2.0f && (curr_state.state.equals( "Guess"))){
                    getWindow().getDecorView().setBackgroundColor(Color.RED);
                    bb.add(2);
                    System.out.println("x = " + tx + " y = " + ty + " z = " + tz);
                    gyroscope.unregister();
                }
                else if(tx>2.0f && ty<-2.0f && (curr_state.state.equals(  "Guess"))){
                    getWindow().getDecorView().setBackgroundColor(Color.BLUE);
                    bb.add(4);
                    System.out.println("x = " + tx + " y = " + ty + " z = " + tz);
                    gyroscope.unregister();
                }
                else if(tx<-2.0f && ty>2.0f && (curr_state.state.equals( "Guess"))){
                    getWindow().getDecorView().setBackgroundColor(Color.GREEN);
                    bb.add(1);
                    System.out.println("x = " + tx + " y = " + ty + " z = " + tz);
                    gyroscope.unregister();
                } else if(tx<-2.0f && ty<-2.0f && (curr_state.state.equals(  "Guess"))){
                    getWindow().getDecorView().setBackgroundColor(Color.YELLOW);
                    bb.add(3);
                    System.out.println("x = " + tx + " y = " + ty + " z = " + tz);
                    gyroscope.unregister();
                }

            }
        });
}


    class buttonHandler extends Handler{
        @Override
        public void handleMessage(Message msg) {
            simon_motion.this.update();
        }

        public void sleep(long delayMillis) {
            this.removeMessages(0);
            sendMessageDelayed(obtainMessage(0), delayMillis);
        }
    }

    public void initGame(View v)
    {
        gameover.setVisibility(View.INVISIBLE);
        inputButton.setVisibility(View.VISIBLE);
        restartButton.setVisibility(View.INVISIBLE);
        gameSequence = new ArrayList<Integer>();
        bb = new ArrayList<Integer>();
        mMoveDelay = 750;
        playSeqCounter = 0;
        colorTouched = 0;
        scoreCounter = 0;
        curr_state.state = "Start";
        update();
    }

    private void update()
    {
        getWindow().getDecorView().setBackgroundColor(Color.BLACK);
if (curr_state.state.equals("Start")) {
            clearPlayerSequence();
            generateSequence();
            playSeqCounter = 0;curr_state.state = "Play";

        }

if (curr_state.state.equals("Play")) {
            playSequence();
            curr_state.state = "Darken";
            if (playSeqCounter == gameSequence.size())
                curr_state.state = "Guess";
        } else if ((curr_state.state.equals("Darken"))){
            darkSequence();
            curr_state.state = "Play";
        }

if ((curr_state.state.equals("Guess")))
        {
            darkenLastButton();

            if (colorTouched != 0) {
                playerSequence.add(colorTouched);
                colorTouched = 0;}

            if (bb.size() == gameSequence.size()) {
                winOrLose = compareSequence();

                if (!winOrLose) {
                    curr_state.state = "Done";
                    gameOver();
                }
                else {
                    curr_state.state = "Start";
                    scoreCounter++;
                    Score.setText("Score: " + Integer.toString(scoreCounter));
                }
            }

        }
if ((curr_state.state.equals(  "Guess")))
            SimonButtonHandler.sleep(mMoveDelay);
else
            SimonButtonHandler.sleep(mMoveDelay);
    }



    public void tap(View view)
    {

setAccelerometerListener();
        accelerometer.register();

    }


    private void clearPlayerSequence()
    {
        colorTouched = 0;
        bb.clear();
    }

private boolean compareSequence()
    {
        ListIterator<Integer> gameSeqITR = gameSequence.listIterator();

        ListIterator<Integer> playerSeqITR = bb.listIterator();


        int gameSeqPointer, playerSeqPointer;

        while (gameSeqITR.hasNext() && playerSeqITR.hasNext())
        {
            gameSeqPointer = gameSeqITR.next();
            playerSeqPointer = playerSeqITR.next();

            System.out.println(gameSeqPointer + " : " + playerSeqPointer);

            if (gameSeqPointer != playerSeqPointer)
                return false;
        }
        return true;
    }

public void quitButton(View view)
    {
        finish();
    }

private void gameOver()
    {
inputButton.setVisibility(View.INVISIBLE);
        gameover.setVisibility(View.VISIBLE);
        restartButton.setVisibility(View.VISIBLE);
    }

private void generateSequence()
    {
        Random RNG = new Random();gameSequence.add(RNG.nextInt(4) + 1);
}

private void playSequence()
    {
        long time = System.currentTimeMillis();

        if (time - mLastMove > mMoveDelay) {
if (playSeqCounter < gameSequence.size()) {
                lightButton(playSeqCounter);
                System.out.println(playSeqCounter);
                playSeqCounter++;
            }
            mLastMove = time;
        }
    }

    private void darkSequence(){
        long time = System.currentTimeMillis();

        if (time - mLastMove > mMoveDelay) {
            if(playSeqCounter != 0){
                darkenButton(playSeqCounter -1);
            }
        }
        mLastMove = time;
    }

private void darkenLastButton()
    {
        long time = System.currentTimeMillis();

        if (time - mLastMove > mMoveDelay) {
            darkenButton(playSeqCounter-1);
        }
    }

    private void lightButton(int index) {
        ImageView litImage;

        if (gameSequence.get(index).equals(GREEN)){
            litImage = (ImageView) findViewById(R.id.greenButton);
            litImage.setImageBitmap(GreenLit);
        }
        if (gameSequence.get(index).equals(RED)){
            litImage = (ImageView) findViewById(R.id.redButton);
            litImage.setImageBitmap(RedLit);
        }
        if (gameSequence.get(index).equals(BLUE)){
            litImage = (ImageView) findViewById(R.id.blueButton);
            litImage.setImageBitmap(BlueLit);
        }
        if (gameSequence.get(index).equals(YELLOW)){
            litImage = (ImageView) findViewById(R.id.yellowButton);
            litImage.setImageBitmap(YellowLit);
        }
    }


    private void darkenButton(int index) {
        ImageView litImage;

if (gameSequence.get(index).equals(GREEN)){
            litImage = (ImageView) findViewById(R.id.greenButton);
            litImage.setImageBitmap(GreenUnlit);
        }
        if (gameSequence.get(index).equals(RED)){
            litImage = (ImageView) findViewById(R.id.redButton);
            litImage.setImageBitmap(RedUnlit);
        }
        if (gameSequence.get(index).equals(BLUE)){
            litImage = (ImageView) findViewById(R.id.blueButton);
            litImage.setImageBitmap(BlueUnlit);
        }
        if (gameSequence.get(index).equals(YELLOW)){
            litImage = (ImageView) findViewById(R.id.yellowButton);
            litImage.setImageBitmap(YellowUnlit);
        }
    }

private void setBitMaps()
    {
        GreenUnlit = BitmapFactory.decodeResource(getResources(),R.drawable.green_unlit);
        GreenLit = BitmapFactory.decodeResource(getResources(),R.drawable.green_lit);
        RedUnlit = BitmapFactory.decodeResource(getResources(),R.drawable.red_unlit);
        RedLit = BitmapFactory.decodeResource(getResources(),R.drawable.red_lit);
        BlueUnlit = BitmapFactory.decodeResource(getResources(),R.drawable.blue_unlit);
        BlueLit = BitmapFactory.decodeResource(getResources(),R.drawable.blue_lit);
        YellowUnlit = BitmapFactory.decodeResource(getResources(),R.drawable.yellow_unlit);
        YellowLit = BitmapFactory.decodeResource(getResources(),R.drawable.yellow_lit);
    }

    @Override
    protected void onResume(){
        super.onResume();

        accelerometer.register();
        gyroscope.register();
    }

    @Override
    protected void onPause(){
        super.onPause();

        accelerometer.unregister();
        gyroscope.unregister();
    }


}
