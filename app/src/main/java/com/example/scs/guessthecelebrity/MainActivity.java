package com.example.scs.guessthecelebrity;

import android.graphics.Bitmap;
import android.graphics.BitmapFactory;
import android.os.AsyncTask;
import android.support.v7.app.AppCompatActivity;
import android.os.Bundle;
import android.support.v7.widget.GridLayout;
import android.view.View;
import android.widget.Button;
import android.widget.ImageView;
import android.widget.LinearLayout;
import android.widget.TextView;
import android.widget.Toast;

import java.io.InputStream;
import java.io.InputStreamReader;
import java.net.HttpURLConnection;
import java.net.URL;
import java.util.ArrayList;
import java.util.Random;
import java.util.regex.Matcher;
import java.util.regex.Pattern;

public class MainActivity extends AppCompatActivity {

    ArrayList<String> celebUrl = new ArrayList<String>();
    ArrayList<String> celebNames = new ArrayList<String>();
    int choosenCeleb = 0, locationOfCorrectAnswer = 0, questionNo = 0, correctAnswers = 0;
    String[] answers = new String[4];
    Button[] button = new Button[4];
    ImageView celebImg;
    TextView scoreText;
    GridLayout gridLayout;
    LinearLayout linearLayout;
    DownloadTask downloadTask;
    DownloadImage downloadImage;

    public void chooseCeleb(View view){

        if(questionNo != 20){

        if(view.getTag().toString().equals(Integer.toString(locationOfCorrectAnswer))){
            Toast.makeText(this, "Correct!", Toast.LENGTH_SHORT).show();
            correctAnswers++;
        }
        else{
            Toast.makeText(this, "No it was " + celebNames.get(choosenCeleb), Toast.LENGTH_SHORT).show();
        }

        questionNo++;
        newQuestion();
        }
        else{
            gridLayout = (GridLayout) findViewById(R.id.gridlayout);
            linearLayout = (LinearLayout) findViewById(R.id.scorelayout);
            scoreText = (TextView) findViewById(R.id.score);

            gridLayout.setVisibility(View.INVISIBLE);
            linearLayout.setVisibility(View.VISIBLE);
            scoreText.setText(correctAnswers + "/20");
        }
    }

    public void newQuestion(){

        downloadImage = new DownloadImage();
        Random random = new Random();
        choosenCeleb = random.nextInt(celebUrl.size());

        try {

            Bitmap celebBitmap = downloadImage.execute(celebUrl.get(choosenCeleb)).get();
            celebImg.setImageBitmap(celebBitmap);

            locationOfCorrectAnswer = random.nextInt(4);
            int notChoosenCeleb;

            for (int i = 0; i <= 3; i++) {
                if (i == locationOfCorrectAnswer)
                    answers[i] = celebNames.get(choosenCeleb);
                else {
                    notChoosenCeleb = random.nextInt(celebUrl.size());

                    while (choosenCeleb == notChoosenCeleb)
                        notChoosenCeleb = random.nextInt(celebUrl.size());

                    answers[i] = celebNames.get(notChoosenCeleb);
                }
            }

            for (int i = 0; i <= 3; i++)
                button[i].setText(answers[i]);
        }
        catch (Exception e){
            e.printStackTrace();
        }
    }

    public class DownloadImage extends AsyncTask<String, Void, Bitmap>{

        @Override
        protected Bitmap doInBackground(String... urls) {

            try {
                URL url = new URL(urls[0]);
                HttpURLConnection urlConnection = (HttpURLConnection) url.openConnection();
                urlConnection.connect();
                InputStream input = urlConnection.getInputStream();
                Bitmap imgBitmap = BitmapFactory.decodeStream(input);

                return imgBitmap;

            } catch (Exception e) {
                e.printStackTrace();
            }

            return null;
        }
    }

    public class DownloadTask extends AsyncTask<String, Void, String>{

        @Override
        protected String doInBackground(String... urls) {

            String result = null;

            try {
                URL url = new URL(urls[0]);
                HttpURLConnection urlConnection = (HttpURLConnection) url.openConnection();
                InputStream input = urlConnection.getInputStream();
                InputStreamReader reader = new InputStreamReader(input);
                int data = reader.read();

                while (data != -1){

                    char currentChar = (char) data;
                    result += currentChar;
                    data = reader.read();
                }

                return result;

            } catch (Exception e) {
                e.printStackTrace();
            }

            return null;
        }
    }

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_main);

        downloadTask = new DownloadTask();

        celebImg = (ImageView) findViewById(R.id.img);
        button[0] = (Button) findViewById(R.id.button0);
        button[1] = (Button) findViewById(R.id.button1);
        button[2] = (Button) findViewById(R.id.button2);
        button[3] = (Button) findViewById(R.id.button3);

        String result = null;

        try {
            result = downloadTask.execute("http://www.posh24.se/kandisar").get();

            String[] splitResult = result.split("<div class=\"sidebarContainer\">");
            Pattern p = Pattern.compile("<img src=\"(.*?)\"");
            Matcher m = p.matcher(splitResult[0]);

            while (m.find())
                celebUrl.add(m.group(1));

            p = Pattern.compile("alt=\"(.*?)\"");
            m = p.matcher(splitResult[0]);

            while (m.find())
                celebNames.add(m.group(1));

            newQuestion();
        }
        catch (Exception e) {
            e.printStackTrace();
        }
    }

}