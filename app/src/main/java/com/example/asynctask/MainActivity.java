package com.example.asynctask;

import androidx.appcompat.app.AppCompatActivity;

import android.app.ProgressDialog;
import android.app.Service;
import android.content.Context;
import android.content.Intent;
import android.graphics.Bitmap;
import android.graphics.BitmapFactory;
import android.net.ConnectivityManager;
import android.net.NetworkInfo;
import android.os.AsyncTask;
import android.os.Bundle;
import android.os.Handler;
import android.os.ResultReceiver;
import android.view.View;
import android.widget.Button;
import android.widget.ImageView;
import android.widget.Toast;

import java.io.FileOutputStream;
import java.io.IOException;
import java.io.InputStream;
import java.io.OutputStream;
import java.net.HttpURLConnection;
import java.net.URL;

import static android.os.Environment.getExternalStorageDirectory;

public class MainActivity extends AppCompatActivity {
    String url1 = "https://zdnet3.cbsistatic.com/hub/i/2019/03/16/e118b0c5-cf3c-4bdb-be71-103228677b25/android-logo.png";
    String url2 = "https://zdnet3.cbsistatic.com/hub/i/2019/03/16/e118b0c5-cf3c-4bdb-be71-103228677b25/android-logo.png";
    ImageView image;
    Button downloadbutton;
    Button downloadusingservice;


    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_main);
        initUi();

        downloadbutton.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View arg0) {
                if (isConnected()) {
                    final DownloadImage downloadImage = new DownloadImage();
                    downloadImage.execute(url1);
                } else {
                    Toast.makeText(MainActivity.this, "Internet not connected", Toast.LENGTH_SHORT).show();
                }

            }
        });

        downloadusingservice.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                Intent intent = new Intent(MainActivity.this, Downloadservice.class);
                intent.putExtra("url", url2);
                intent.putExtra("receiver", new DownloadReceiver(new Handler()));
                startService(intent);
            }
        });

    }

    private void initUi() {
        image = findViewById(R.id.downloaded_image);
        downloadbutton = findViewById(R.id.download);
        downloadusingservice = findViewById(R.id.downloadservice);
    }

    private class DownloadReceiver extends ResultReceiver {
        public DownloadReceiver(Handler handler) {
            super(handler);
        }

        @Override
        protected void onReceiveResult(int resultCode, Bundle resultData) {
            super.onReceiveResult(resultCode, resultData);
            if (resultCode == Downloadservice.UPDATE_PROGRESS) {
                int progress = resultData.getInt("progress");

                if (progress == 100) {
                    image.setImageBitmap(BitmapFactory.decodeFile(getExternalStorageDirectory().getAbsolutePath() + "/" + "myImage1.jpg"));

                }
            }
        }
    }

    public boolean isConnected() {
        Context context = getApplicationContext();
        ConnectivityManager connectivity = (ConnectivityManager) context.getSystemService(Service.CONNECTIVITY_SERVICE);
        if (connectivity != null) {
            NetworkInfo info = connectivity.getActiveNetworkInfo();
            if (info != null) {
                if (info.getState() == NetworkInfo.State.CONNECTED) {
                    return true;
                }
            }
        }
        return false;
    }

    private class DownloadImage extends AsyncTask<String, Integer, String> {
        android.app.ProgressDialog ProgressDialog;

        private String path;


        @Override
        protected void onPreExecute() {
            super.onPreExecute();

            ProgressDialog = new ProgressDialog(MainActivity.this);
            ProgressDialog.setMessage("Loading...");
            ProgressDialog.setIndeterminate(false);
            ProgressDialog.show();

        }

        @Override
        protected void onProgressUpdate(Integer... values) {
            super.onProgressUpdate(values);
            ProgressDialog.setIndeterminate(false);
            ProgressDialog.setMax(100);
            ProgressDialog.setProgress(values[0]);
        }

        @Override
        protected String doInBackground(String... surl) {

            InputStream input = null;
            OutputStream output = null;
            HttpURLConnection connection = null;
            try {
                URL url = new URL(surl[0]);
                connection = (HttpURLConnection) url.openConnection();
                connection.connect();


                int fileLength = connection.getContentLength();


                input = connection.getInputStream();
                path = getExternalStorageDirectory().getAbsolutePath() + "/" + "myImage.jpg";
                output = new FileOutputStream(path);

                byte data[] = new byte[409600];
                long total = 0;
                int count;
                while ((count = input.read(data)) != -1) {
                    while (!isConnected()) {

                    }

                    if (isCancelled()) {
                        input.close();
                        return null;
                    }

                    total += count;
                    publishProgress((int) total * 100 / fileLength);
                    output.write(data, 0, count);


                }
            } catch (Exception e) {
                return e.toString();
            } finally {
                try {
                    if (output != null)
                        output.close();
                    if (input != null)
                        input.close();
                } catch (IOException ignored) {
                }

                if (connection != null)
                    connection.disconnect();
            }
            return null;
        }


        @Override
        protected void onPostExecute(String s) {


            if (isConnected()) {
                path = getExternalStorageDirectory().getAbsolutePath() + "/" + "myImage.jpg";
//
                Bitmap bitmap = BitmapFactory.decodeFile(path);
                image.setImageBitmap(bitmap);
            }


        }
    }
}
