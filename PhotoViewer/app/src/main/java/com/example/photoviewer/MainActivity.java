package com.example.photoviewer;

import android.content.ContentValues;
import android.content.Context;
import android.content.Intent;
import android.graphics.Bitmap;
import android.graphics.BitmapFactory;
import android.net.Uri;
import android.os.AsyncTask;
import android.os.Bundle;
import android.os.Environment;
import android.provider.MediaStore;
import android.util.Log;
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
import java.io.IOException;
import java.io.InputStream;
import java.io.InputStreamReader;
import java.io.OutputStream;
import java.net.HttpURLConnection;
import java.net.URL;
import java.util.ArrayList;
import java.util.List;

public class MainActivity extends AppCompatActivity {

    TextView textView;
    String site_url = "http://10.0.2.2:8000"; //link to django
    CloadImage taskDownload;
    CloadImage filterPerson;
    DownloadImagesTask imageListDownload;
    DownloadPersonImagesTask personDownload;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_main);
        textView = findViewById(R.id.textView);
    }

    public void onClickImageListDownload(View v) {
        imageListDownload = new DownloadImagesTask(this);
        imageListDownload.execute(site_url + "/api/posts/");
    }

    public void onClickDownload(View v) {

        if (taskDownload != null && taskDownload.getStatus() == AsyncTask.Status.RUNNING) {
            taskDownload.cancel(true);
        }

        taskDownload = new CloadImage();
        taskDownload.execute(site_url + "/api_root/Post/");
        Toast.makeText(getApplicationContext(), "Downloading…", Toast.LENGTH_LONG).show();
    }

    public class DownloadImagesTask extends AsyncTask<String, Void, Void> {

        private Context context;

        public DownloadImagesTask(Context context) {
            this.context = context;
        }

        @Override
        protected Void doInBackground(String... urls) {
            String apiUrl = urls[0];

            try {
                // Step 1: Fetch JSON list of posts
                URL url = new URL(apiUrl);
                HttpURLConnection conn = (HttpURLConnection) url.openConnection();
                conn.setRequestMethod("GET");

                BufferedReader reader = new BufferedReader(new InputStreamReader(conn.getInputStream()));
                StringBuilder response = new StringBuilder();
                String line;
                while ((line = reader.readLine()) != null) {
                    response.append(line);
                }
                reader.close();

                // Step 2: Parse JSON (assuming array of posts with "image" field)
                JSONArray posts = new JSONArray(response.toString());
                for (int i = 0; i < posts.length(); i++) {
                    JSONObject post = posts.getJSONObject(i);
                    String imageUrl = post.getString("image"); // adjust key if needed

                    // Step 3: Download each image into a Bitmap
                    Bitmap bitmap = downloadBitmap(imageUrl);

                    // Step 4: Save Bitmap into public Pictures folder
                    if (bitmap != null) {
                        saveImageToGallery(context, bitmap, "post_" + i + ".jpg");
                    }
                }

            } catch (Exception e) {
                e.printStackTrace();
            }

            return null;
        }

        // Helper: download image from URL into Bitmap
        private Bitmap downloadBitmap(String imageUrl) {
            try {
                URL url = new URL(imageUrl);
                HttpURLConnection connection = (HttpURLConnection) url.openConnection();
                connection.connect();
                InputStream input = connection.getInputStream();
                return BitmapFactory.decodeStream(input);
            } catch (Exception e) {
                e.printStackTrace();
                return null;
            }
        }

        // Helper: save Bitmap into public Pictures folder via MediaStore
        private void saveImageToGallery(Context context, Bitmap bitmap, String fileName) {
            ContentValues values = new ContentValues();
            values.put(MediaStore.Images.Media.DISPLAY_NAME, fileName);
            values.put(MediaStore.Images.Media.MIME_TYPE, "image/jpeg");
            values.put(MediaStore.Images.Media.RELATIVE_PATH, Environment.DIRECTORY_PICTURES);

            Uri uri = context.getContentResolver().insert(
                    MediaStore.Images.Media.EXTERNAL_CONTENT_URI, values
            );

            try (OutputStream out = context.getContentResolver().openOutputStream(uri)) {
                bitmap.compress(Bitmap.CompressFormat.JPEG, 100, out);
                Log.d("DownloadImagesTask", "Saved: " + fileName);
            } catch (Exception e) {
                e.printStackTrace();
            }
        }

        @Override
        protected void onPostExecute(Void result) {
            Toast.makeText(context, "Images downloaded to Gallery!", Toast.LENGTH_LONG).show();
        }
    }


    public class DownloadPersonImagesTask extends AsyncTask<String, Void, Void> {

        private Context context;

        public DownloadPersonImagesTask(Context context) {
            this.context = context;
        }

        @Override
        protected Void doInBackground(String... urls) {
            String apiUrl = urls[0];

            try {
                // Step 1: Fetch JSON list of posts
                URL url = new URL(apiUrl);
                HttpURLConnection conn = (HttpURLConnection) url.openConnection();
                conn.setRequestMethod("GET");

                BufferedReader reader = new BufferedReader(new InputStreamReader(conn.getInputStream()));
                StringBuilder response = new StringBuilder();
                String line;
                while ((line = reader.readLine()) != null) {
                    response.append(line);
                }
                reader.close();

                // Step 2: Parse JSON (assuming array of posts with "image" field)
                JSONArray posts = new JSONArray(response.toString());
                for (int i = 0; i < posts.length(); i++) {
                    JSONObject post = posts.getJSONObject(i);
                    String imageUrl = post.getString("image"); // adjust key if needed

                    // Step 3: Download each image into a Bitmap
                    Bitmap bitmap = downloadBitmap(imageUrl);

                    // Step 4: Save Bitmap into public Pictures folder
                    if (bitmap != null) {
                        saveImageToGallery(context, bitmap, "person_" + i + ".jpg");
                    }
                }

            } catch (Exception e) {
                e.printStackTrace();
            }

            return null;
        }

        // Helper: download image from URL into Bitmap
        private Bitmap downloadBitmap(String imageUrl) {
            try {
                URL url = new URL(imageUrl);
                HttpURLConnection connection = (HttpURLConnection) url.openConnection();
                connection.connect();
                InputStream input = connection.getInputStream();
                return BitmapFactory.decodeStream(input);
            } catch (Exception e) {
                e.printStackTrace();
                return null;
            }
        }

        // Helper: save Bitmap into public Pictures folder via MediaStore
        private void saveImageToGallery(Context context, Bitmap bitmap, String fileName) {
            ContentValues values = new ContentValues();
            values.put(MediaStore.Images.Media.DISPLAY_NAME, fileName);
            values.put(MediaStore.Images.Media.MIME_TYPE, "image/jpeg");
            values.put(MediaStore.Images.Media.RELATIVE_PATH, Environment.DIRECTORY_PICTURES);

            Uri uri = context.getContentResolver().insert(
                    MediaStore.Images.Media.EXTERNAL_CONTENT_URI, values
            );

            try (OutputStream out = context.getContentResolver().openOutputStream(uri)) {
                bitmap.compress(Bitmap.CompressFormat.JPEG, 100, out);
                Log.d("DownloadImagesTask", "Saved: " + fileName);
            } catch (Exception e) {
                e.printStackTrace();
            }

            // 👇 Force Gallery/Photos to notice the new image
            context.sendBroadcast(new Intent(Intent.ACTION_MEDIA_SCANNER_SCAN_FILE, uri));
        }


        @Override
        protected void onPostExecute(Void result) {
            Toast.makeText(context, "Images downloaded to Gallery!", Toast.LENGTH_LONG).show();
        }

    }
    public void onFilterPerson(View v) {

        // Create a new task
        filterPerson = new CloadImage();

        // Execute with the filter parameter ?title=person
        filterPerson.execute(site_url + "/api_person/posts/");

        Toast.makeText(getApplicationContext(), "Filtering person posts…", Toast.LENGTH_LONG).show();
    }

    public void onDownloadPerson(View v){

        personDownload = new DownloadPersonImagesTask(this);
        personDownload.execute(site_url + "/api_person/posts/");
    }
    // AsyncTask to fetch and display images
    private class CloadImage extends AsyncTask<String, Integer, List<String>> {

        @Override
        protected List<String> doInBackground(String... urls) {
            List<String> imageUrls = new ArrayList<>();

            try {
                String apiUrl = urls[0];
                String token = "1d2635363469363c7d2c2f2a61518b163ee153f2"; // replace with your token

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

                        // prepend site_url if needed
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
                textView.setText("불러올 이미지가 없습니다."); // "No images to load"
                return;
            }

            textView.setText("이미지 로드 성공!"); // "Images loaded successfully!"

            RecyclerView recyclerView = findViewById(R.id.recyclerView);
            ImageAdapter adapter = new ImageAdapter(imageUrls);
            recyclerView.setLayoutManager(new LinearLayoutManager(MainActivity.this));
            recyclerView.setAdapter(adapter);
        }
    }
}