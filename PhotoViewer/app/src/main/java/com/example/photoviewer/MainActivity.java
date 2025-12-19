package com.example.photoviewer;

import android.graphics.Bitmap;
import android.graphics.BitmapFactory;
import android.os.AsyncTask;
import android.os.Bundle;
import android.view.View;
import android.widget.TextView;
import android.widget.Toast;

import androidx.appcompat.app.AppCompatActivity;
import androidx.recyclerview.widget.LinearLayoutManager;
import androidx.recyclerview.widget.RecyclerView;

import org.json.JSONArray;
import org.json.JSONException;
import org.json.JSONObject;

import java.io.BufferedReader;
import java.io.DataOutputStream;
import java.io.File;
import java.io.FileInputStream;
import java.io.FileOutputStream;
import java.io.IOException;
import java.io.InputStream;
import java.io.InputStreamReader;
import java.net.HttpURLConnection;
import java.net.URL;
import java.util.ArrayList;
import java.util.List;

public class MainActivity extends AppCompatActivity {

    TextView textView;
    String site_url = "http://10.0.2.2:8000";

    CloadImage taskDownload;
    PutPost taskUpload;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_main);
        textView = findViewById(R.id.textView);
    }

    // ========================== DOWNLOAD BUTTON =============================
    public void onClickDownload(View v) {

        if (taskDownload != null && taskDownload.getStatus() == AsyncTask.Status.RUNNING) {
            taskDownload.cancel(true);
        }

        taskDownload = new CloadImage();
        taskDownload.execute(site_url + "/api_root/Post/");
        Toast.makeText(getApplicationContext(), "Downloading…", Toast.LENGTH_LONG).show();
    }

    // ============================ UPLOAD BUTTON =============================
    public void onClickUpload(View v) {
        taskUpload = new PutPost();
        taskUpload.execute(site_url + "/api_root/Post/");
        Toast.makeText(getApplicationContext(), "Uploading…", Toast.LENGTH_LONG).show();
    }


    // ========================================================================
    //                                DOWNLOAD
    // ========================================================================

    private class CloadImage extends AsyncTask<String, Integer, List<String>> {

        @Override
        protected List<String> doInBackground(String... urls) {

            List<String> imageUrls = new ArrayList<>();

            try {
                String apiUrl = urls[0];
                String token = "1d2635363469363c7d2c2f2a61518b163ee153f2";

                URL urlAPI = new URL(apiUrl);
                HttpURLConnection conn = (HttpURLConnection) urlAPI.openConnection();
                conn.setRequestProperty("Authorization", "Token " + token);
                conn.setRequestMethod("GET");
                conn.setConnectTimeout(3000);
                conn.setReadTimeout(3000);

                int responseCode = conn.getResponseCode();
                if (responseCode == HttpURLConnection.HTTP_OK) {

                    BufferedReader reader =
                            new BufferedReader(new InputStreamReader(conn.getInputStream()));

                    StringBuilder result = new StringBuilder();
                    String line;

                    while ((line = reader.readLine()) != null) {
                        result.append(line);
                    }

                    JSONArray jsonArray = new JSONArray(result.toString());

                    for (int i = 0; i < jsonArray.length(); i++) {
                        JSONObject item = jsonArray.getJSONObject(i);

                        // image field example: "/media/blog_image/2025/11/18/myphoto.png"
                        String imagePath = item.getString("image");

                        imageUrls.add(imagePath);
                    }
                }

            } catch (IOException | JSONException e) {
                e.printStackTrace();
            }

            return imageUrls;
        }

        @Override
        protected void onPostExecute(List<String> imageUrls) {

            if (imageUrls.isEmpty()) {
                textView.setText("불러올 이미지가 없습니다.");
                return;
            }

            textView.setText("이미지 로드 성공!");

            RecyclerView recyclerView = findViewById(R.id.recyclerView);
            ImageAdapter adapter = new ImageAdapter(imageUrls);
            recyclerView.setLayoutManager(new LinearLayoutManager(MainActivity.this));
            recyclerView.setAdapter(adapter);
        }
    }



    // ========================================================================
    //                                UPLOAD
    // ========================================================================

    private class PutPost extends AsyncTask<String, Void, Void> {

        @Override
        protected Void doInBackground(String... urls) {
            try {
                String apiUrl = urls[0];
                String token = "1d2635363469363c7d2c2f2a61518b163ee153f2";

                // Load sample image
                Bitmap bitmap = BitmapFactory.decodeResource(getResources(), R.drawable.test);
                if (bitmap == null) return null;

                // Save to cache file
                File file = new File(getCacheDir(), "upload.png");
                FileOutputStream out = new FileOutputStream(file);
                bitmap.compress(Bitmap.CompressFormat.PNG, 100, out);
                out.close();

                String boundary = "===" + System.currentTimeMillis() + "===";
                String LINE_FEED = "\r\n";

                HttpURLConnection conn =
                        (HttpURLConnection) new URL(apiUrl).openConnection();
                conn.setDoOutput(true);
                conn.setRequestMethod("POST");
                conn.setRequestProperty("Authorization", "Token " + token);
                conn.setRequestProperty("Accept", "application/json");
                conn.setRequestProperty("Content-Type",
                        "multipart/form-data; boundary=" + boundary);

                DataOutputStream os = new DataOutputStream(conn.getOutputStream());

                // ----- TITLE -----
                os.writeBytes("--" + boundary + LINE_FEED);
                os.writeBytes("Content-Disposition: form-data; name=\"title\"" + LINE_FEED);
                os.writeBytes(LINE_FEED);
                os.writeBytes("Android Upload Test" + LINE_FEED);

                // ----- TEXT -----
                os.writeBytes("--" + boundary + LINE_FEED);
                os.writeBytes("Content-Disposition: form-data; name=\"text\"" + LINE_FEED);
                os.writeBytes(LINE_FEED);
                os.writeBytes("Uploaded via Android client" + LINE_FEED);

                // ----- IMAGE -----
                os.writeBytes("--" + boundary + LINE_FEED);
                os.writeBytes("Content-Disposition: form-data; name=\"image\"; filename=\"upload.png\"" + LINE_FEED);
                os.writeBytes("Content-Type: image/png" + LINE_FEED);
                os.writeBytes(LINE_FEED);

                FileInputStream fis = new FileInputStream(file);
                byte[] buffer = new byte[4096];
                int bytesRead;
                while ((bytesRead = fis.read(buffer)) != -1) {
                    os.write(buffer, 0, bytesRead);
                }
                fis.close();
                os.writeBytes(LINE_FEED);

                // CLOSE
                os.writeBytes("--" + boundary + "--" + LINE_FEED);
                os.flush();
                os.close();

                System.out.println("UPLOAD RESPONSE CODE = " + conn.getResponseCode());

            } catch (Exception e) {
                e.printStackTrace();
            }
            return null;
        }
    }
}
