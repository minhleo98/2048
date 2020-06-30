package com.example.a2048_game;

import androidx.annotation.NonNull;
import androidx.appcompat.app.AlertDialog;
import androidx.appcompat.app.AppCompatActivity;

import android.content.Context;
import android.content.DialogInterface;
import android.content.SharedPreferences;
import android.media.MediaPlayer;
import android.os.Bundle;
import android.os.PersistableBundle;
import android.util.Log;
import android.view.MotionEvent;
import android.view.View;
import android.widget.GridView;
import android.widget.TextView;

public class MainActivity extends AppCompatActivity {
    private GridView gameGrid;
    private TitleAdapter adapter;
    private View.OnTouchListener listener;
    private float X, Y;
    Boolean con = false;
    Boolean win = false;
    Boolean alert = false;
    MediaPlayer mediaPlayer, interactMedia;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_main);

        compareAndGet();
        init();
        setData();
    }

    @Override
    protected void onSaveInstanceState(@NonNull Bundle outState) {
        super.onSaveInstanceState(outState);
        outState.putAll(outState);
        mediaPlayer.stop();
    }

    @Override
    public void onRestoreInstanceState(Bundle savedInstanceState) {
        NewGame();
    }


    private void compareAndGet(){
        gameGrid = (GridView)findViewById(R.id.gameGrid);
    }

    private void init(){
//        Log.d(String.valueOf(mediaPlayer.getAudioSessionId()), "media");
        mediaPlayer = MediaPlayer.create(MainActivity.this, R.raw.astronomia);
        mediaPlayer.start();
        if(mediaPlayer.isPlaying()){
            Log.d(String.valueOf(mediaPlayer), "playing");
        }
        Data.getData().init(MainActivity.this);
        adapter = new TitleAdapter(MainActivity.this, 0, Data.getData().getArrData());
        SharedPreferences settings = getSharedPreferences("GAME_DATA", Context.MODE_PRIVATE);
        int high = settings.getInt("HIGH_SCORE", 0);
        TextView highScore = findViewById(R.id.highscore);
        highScore.setText(Integer.toString(high));
        listener = new View.OnTouchListener(){
            @Override
            public boolean onTouch(View v, MotionEvent event) {
                TextView curScore = findViewById(R.id.score);
                TextView highScore = findViewById(R.id.highscore);
                interactMedia = MediaPlayer.create(MainActivity.this, R.raw.woosh);

                switch (event.getAction()){
                    case MotionEvent.ACTION_DOWN:
                        X = event.getX();
                        Y = event.getY();
                        break;
                    case MotionEvent.ACTION_UP:
                        interactMedia.start();
                        if(Math.abs(event.getX()- X) > Math.abs(event.getY()-Y)){
                            if(event.getX() > X){
                                Data.getData().swipeRight();
                                adapter.notifyDataSetChanged();
                            } else {
                                Data.getData().swipeLeft();
                                adapter.notifyDataSetChanged();
                            }
                        } else {
                            if(event.getY() > Y){
                                Data.getData().swipeUp();
                                adapter.notifyDataSetChanged();
                            } else {
                                Data.getData().swipeDown();
                                adapter.notifyDataSetChanged();
                            }
                        }
                        curScore.setText(Integer.toString(Data.getData().getScore()));
                        //highscore setting
                        SharedPreferences settings = getSharedPreferences("GAME_DATA", Context.MODE_PRIVATE);
                        int high = settings.getInt("HIGH_SCORE", 0);
                        highScore.setText(Integer.toString(high));
                        if (Data.getData().getScore() > high) {
                            highScore.setText(Integer.toString(Data.getData().getScore()));
                            //save
                            SharedPreferences.Editor editor = settings.edit();
                            editor.putInt("HIGH_SCORE", Data.getData().getScore());
                            editor.commit();
                        }

                        if (!win) {
                            int curArr[][] = Data.getData().getArr();
                            for (int i = 0; i < 4; i++) {
                                for (int j = 0; j < 4; j++)
                                    if (curArr[i][j] == 2048 && !con) {
                                        alertWin();
                                        break;
                                    }
                                if (alert) {
                                    alert = false;
                                    break;
                                }
                            }
                        }

                }
                if (Data.getData().checkOutOfMove() == 0){
                    alertLose();
                    if(mediaPlayer.isPlaying()){
                        mediaPlayer.stop();
                    }
                }
                return true;
            }
        };
    }

    private void setData(){
        gameGrid.setAdapter(adapter);
        gameGrid.setOnTouchListener(listener);
    }

    public void NewGame() {
        TextView curScore = findViewById(R.id.score);
        curScore.setText("0");
        compareAndGet();
        init();
        setData();
        win = false;
        con = false;
        alert = false;
    }

    public void newGame(View view) {
        NewGame();
    }

    public void alertWin() {
        AlertDialog.Builder builder
                = new AlertDialog
                .Builder(MainActivity.this);
        // Set the message show for the Alert time
        builder.setMessage("Do you wish to continue playing?");
        builder.setTitle("Congratulation. You have won the game!");
        builder.setCancelable(false);
        builder
                .setPositiveButton(
                        "No, start a new game",
                        new DialogInterface
                                .OnClickListener() {
                            @Override
                            public void onClick(DialogInterface dialog, int which)
                            {
                                win = false;
                                con = false;
                                alert = true;
                                dialog.cancel();
                                NewGame();
                            }
                        });

        builder
                .setNegativeButton(
                        "Yes",
                        new DialogInterface
                                .OnClickListener() {
                            @Override
                            public void onClick(DialogInterface dialog, int which)
                            {
                                win = true;
                                con = true;
                                alert = true;
                                dialog.cancel();
                            }
                        });
        // Create the Alert dialog
        AlertDialog alertDialog = builder.create();
        // Show the Alert Dialog box
        alertDialog.show();
    }

    public void alertLose() {
        AlertDialog.Builder builder
                = new AlertDialog
                .Builder(MainActivity.this);
        // Set the message show for the Alert time
        builder.setMessage("Better luck next time donkey");
        builder.setTitle("Boo. You lose!");
        builder.setCancelable(false);
        builder
                .setPositiveButton(
                        "New game",
                        new DialogInterface
                                .OnClickListener() {
                            @Override
                            public void onClick(DialogInterface dialog, int which)
                            {
                                win = false;
                                con = false;
                                alert = true;
                                NewGame();
                                dialog.cancel();
                                NewGame();
                            }
                        });
        // Create the Alert dialog
        AlertDialog alertDialog = builder.create();
        // Show the Alert Dialog box
        alertDialog.show();
    }

    public void stopMusic(View view) {
        if(mediaPlayer.isPlaying()){
            mediaPlayer.pause();
            view.setBackground(getResources().getDrawable(R.drawable.ic_action_mute));
            Log.d("mute", String.valueOf(getResources().getDrawable(R.drawable.ic_action_mute)));
        }
        else{
            Log.d("playing", String.valueOf(getResources().getDrawable(R.drawable.ic_action_sound)));
            view.setBackground(getResources().getDrawable(R.drawable.ic_action_sound));
            mediaPlayer.start();
        }
    }
}