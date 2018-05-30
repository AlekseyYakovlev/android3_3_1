package ru.spb.yakovlev.android3_3_1;

import android.Manifest;
import android.content.Intent;
import android.content.pm.PackageManager;
import android.graphics.Bitmap;
import android.graphics.BitmapFactory;
import android.net.Uri;
import android.os.Bundle;
import android.os.Environment;
import android.support.design.widget.FloatingActionButton;
import android.support.design.widget.Snackbar;
import android.support.v4.app.ActivityCompat;
import android.support.v7.app.AppCompatActivity;
import android.util.Log;
import android.view.View;
import android.widget.ImageView;

import java.io.File;
import java.io.FileNotFoundException;
import java.io.FileOutputStream;
import java.io.InputStream;

import io.reactivex.Observable;

public class MainActivity extends AppCompatActivity {

    private ImageView imageView;
    private final int Pick_image = 1;
    private View.OnClickListener onFabClickListener;
    private View.OnLongClickListener onFabLongClickListener;
    private Bitmap bitmap = null;


    {
        onFabClickListener = view -> {
            Intent photoPickerIntent = new Intent(Intent.ACTION_PICK);
            photoPickerIntent.setType("image/*");
            startActivityForResult(photoPickerIntent, Pick_image);
        };

        onFabLongClickListener = view -> {

            if (ActivityCompat.checkSelfPermission(this, Manifest.permission.WRITE_EXTERNAL_STORAGE)
                    != PackageManager.PERMISSION_GRANTED) {
                Log.d("********************", "Requesting permission");
                ActivityCompat.requestPermissions(this, new String[]{Manifest.permission.WRITE_EXTERNAL_STORAGE},
                        1);
            }
            if (ActivityCompat.checkSelfPermission(this, Manifest.permission.WRITE_EXTERNAL_STORAGE)
                    == PackageManager.PERMISSION_GRANTED && bitmap != null) {

                File path = Environment.getExternalStoragePublicDirectory(Environment.DIRECTORY_PICTURES);
                File file = new File(path, "savedBitmap.png");

                try {
                    FileOutputStream fos = null;
                    try {
                        fos = new FileOutputStream(file);
                        bitmap.compress(Bitmap.CompressFormat.PNG, 100, fos);
                        Log.d("********************", file.getPath());
                        fos.flush();
                        Snackbar.make(view, file.getPath() + " successfully saved", Snackbar.LENGTH_LONG).show();
                    } finally {
                        if (fos != null) fos.close();
                    }
                } catch (Exception e) {
                    Log.e("********************", e.toString());
                }
                Log.d("********************", "SUCCESS");

                ActivityCompat.requestPermissions(this, new String[]{Manifest.permission.WRITE_EXTERNAL_STORAGE},
                        1);

            } else {
                if (bitmap == null)
                    Snackbar.make(view, "No image to save", Snackbar.LENGTH_LONG).show();
                else Snackbar.make(view, "Permission required", Snackbar.LENGTH_LONG).show();

            }


            return true;
        };
    }

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_main);

        imageView = findViewById(R.id.imageView);
        FloatingActionButton fab = findViewById(R.id.fab);

        fab.setOnClickListener(onFabClickListener);
        fab.setOnLongClickListener(onFabLongClickListener);
    }


    @Override
    protected void onActivityResult(int requestCode, int resultCode, Intent data) {
        super.onActivityResult(requestCode, resultCode, data);
        switch (requestCode) {
            case Pick_image:
                if (resultCode == RESULT_OK) {
                    try {
                        final Uri imageUri = data.getData();
                        Log.d("filename ", imageUri.getPath());
                        final InputStream imageStream = getContentResolver().openInputStream(imageUri);
                        bitmap = BitmapFactory.decodeStream(imageStream);
                        imageView.setImageBitmap(bitmap);
                    } catch (FileNotFoundException e) {
                        e.printStackTrace();
                    }
                }
        }
    }
}
