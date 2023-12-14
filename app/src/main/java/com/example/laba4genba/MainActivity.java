package com.example.laba4genba;

import androidx.appcompat.app.AlertDialog;
import androidx.appcompat.app.AppCompatActivity;

import android.content.ContentValues;
import android.content.DialogInterface;
import android.content.Intent;
import android.database.Cursor;
import android.database.sqlite.SQLiteDatabase;
import android.media.MediaPlayer;
import android.os.AsyncTask;
import android.os.Build;
import android.os.Bundle;
import android.os.Handler;
import android.os.SystemClock;
import android.view.LayoutInflater;
import android.view.View;
import android.webkit.WebView;
import android.widget.Button;
import android.widget.Chronometer;
import android.widget.ImageView;
import android.widget.TextView;
import android.widget.Toast;

import com.google.android.material.textfield.TextInputEditText;

import org.jsoup.Jsoup;
import org.jsoup.nodes.Document;
import org.jsoup.nodes.Element;
import org.jsoup.select.Elements;

import java.io.IOException;
import java.text.SimpleDateFormat;
import java.util.Calendar;
import java.util.concurrent.TimeUnit;

public class MainActivity extends AppCompatActivity {
    Button btnSongsList;
    private TextView song;
    private Chronometer chronometer;
    private ImageView imagePlayPause;
    private MediaPlayer mediaPlayer;
    DatabaseHelper databaseHelper;
    SQLiteDatabase db;
    Cursor userCursor;
    private long PauseOffSet = 0;

    private Handler handler = new Handler();

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_main);

        //Создание БД
        databaseHelper = new DatabaseHelper(getApplicationContext());
        db = databaseHelper.getReadableDatabase();

        btnSongsList = findViewById(R.id.btnSongsList);

        song = findViewById(R.id.song);

        chronometer = findViewById(R.id.chronometer);

        imagePlayPause = findViewById(R.id.imagePlayPause);
        mediaPlayer = new MediaPlayer();

        handler.removeCallbacks(timeUpdaterRunnable);
        handler.postDelayed(timeUpdaterRunnable, 100);

        imagePlayPause.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {
                if(mediaPlayer.isPlaying()){
                    chronometer.stop();
                    PauseOffSet = SystemClock.elapsedRealtime() - chronometer.getBase();

                    mediaPlayer.pause();
                    imagePlayPause.setImageResource(R.drawable.baseline_play_circle_24);
                } else {
                    chronometer.setBase(SystemClock.elapsedRealtime() - PauseOffSet);
                    chronometer.start();

                    mediaPlayer.start();
                    imagePlayPause.setImageResource(R.drawable.baseline_pause_circle_24);
                }
            }
        });

        prepareMediaPlayer();

        btnSongsList.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {
                Intent intent = new Intent(MainActivity.this, SongsActivity.class);
                startActivity(intent);
            }
        });

    }
    private  void prepareMediaPlayer(){
        try {
            mediaPlayer.setDataSource("http://77.234.212.71:8002/live");
            mediaPlayer.prepare();
        } catch (Exception exception){
            Toast.makeText(this, "Нет подключения к интернету", Toast.LENGTH_SHORT).show();
        }
    }

    private Runnable timeUpdaterRunnable = new Runnable() {

        public void run() {
            SongTask songTask = new SongTask();
            songTask.execute();

            handler.postDelayed(this, 5000);
        }
    };

    @Override
    protected void onPause() {
        handler.removeCallbacks(timeUpdaterRunnable);
        super.onPause();
    }

    @Override
    protected void onResume() {
        super.onResume();
        handler.postDelayed(timeUpdaterRunnable, 5000);
    }

    class SongTask extends AsyncTask<Void, String, String> {
        private Document doc;
        private String songInfo;
        @Override
        protected void onPreExecute() {
            super.onPreExecute();
        }

        @Override
        protected String doInBackground(Void... url) {

            try {
                doc = Jsoup.connect("https://media.itmo.ru/includes/get_song.php").get();
                songInfo = doc.text();
            } catch (IOException e) {
                e.printStackTrace();
            }

            return songInfo;
        }

        @Override
        protected void onPostExecute(String result) {
            super.onPostExecute(result);

            song.setText(result);

            Calendar c = Calendar.getInstance();
            SimpleDateFormat df = new SimpleDateFormat("dd.MM.yyyy HH:mm:ss");
            String formattedDate = df.format(c.getTime());

            String[] songInfos = song.getText().toString().split(" - ");

            if(songInfos.length != 0){
                db = databaseHelper.getReadableDatabase();
                userCursor = db.rawQuery("SELECT * FROM songs WHERE name = ?" , new String[] {songInfos[1].toString()});
                int value = userCursor.getCount() + 1;
                userCursor.close();
                db.close();

                if(value == 1){
                    db = databaseHelper.getReadableDatabase();

                    ContentValues cv = new ContentValues();
                    cv.put(DatabaseHelper.COLUMN_MUSICIAN, songInfos[0]);
                    cv.put(DatabaseHelper.COLUMN_NAME, songInfos[1]);
                    cv.put(DatabaseHelper.COLUMN_TIMEADD, formattedDate);

                    db.insert(DatabaseHelper.TABLE, null, cv);
                    db.close();
                }
            }
        }
    }
}