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
import android.view.View;
import android.widget.ImageView;

import java.io.File;
import java.io.FileOutputStream;
import java.io.InputStream;

import io.reactivex.Observable;
import io.reactivex.android.schedulers.AndroidSchedulers;
import io.reactivex.disposables.Disposable;
import io.reactivex.schedulers.Schedulers;
import timber.log.Timber;

public class MainActivity extends AppCompatActivity {

    private ImageView imageView;
    private final int Pick_image = 1;


    private Bitmap bitmap1 = null;

    private final View.OnClickListener onFabClickListener = view -> {
        Intent photoPickerIntent = new Intent(Intent.ACTION_PICK);
        photoPickerIntent.setType("image/*");
        startActivityForResult(photoPickerIntent, Pick_image);
    };

    private final View.OnLongClickListener onFabLongClickListener = view -> {

        saveImageToDisk(view);

        return true;
    };


    private boolean checkPermissionGrantedWriteExternalStorage() {
        return ActivityCompat.checkSelfPermission(this, Manifest.permission.WRITE_EXTERNAL_STORAGE)
                == PackageManager.PERMISSION_GRANTED;
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


                    Disposable d = getBitmapFromFile(data)
                            .subscribeOn(Schedulers.io())
                            .observeOn(AndroidSchedulers.mainThread())
                            .subscribe(bitmap -> {
                                imageView.setImageBitmap(bitmap);
                                bitmap1 = bitmap;
                                Timber.d(bitmap1 == null ? "bitmap is null" : "bitmap is NOT null");
                            }, Timber::e);
                    // d.dispose();
                }
        }
    }

    public Observable<Bitmap> getBitmapFromFile(Intent data) {
        return Observable.fromCallable(() -> {
            final Uri imageUri = data.getData();
            Timber.d("filename " + imageUri.getPath());
            final InputStream imageStream = getContentResolver().openInputStream(imageUri);
            return BitmapFactory.decodeStream(imageStream);
        });
    }

    private void saveImageToDisk(View view) {
        if (!checkPermissionGrantedWriteExternalStorage()) {
            Timber.d("Requesting permission");
            ActivityCompat.requestPermissions(this, new String[]{Manifest.permission.WRITE_EXTERNAL_STORAGE}, 1);
        }
        if (checkPermissionGrantedWriteExternalStorage() && bitmap1 != null) {

            try {


                Disposable d = Observable.fromCallable(() -> Environment.getExternalStoragePublicDirectory(Environment.DIRECTORY_PICTURES))
                        .subscribeOn(Schedulers.io())
                        .map(path -> new File(path, "savedBitmap.png"))
                        .map(FileOutputStream::new)
                        .map(fos -> {

//                        Observable cancelFlag = Observable.just(false);
//                        Boolean cancelFlag = false;
                            Snackbar.make(view, "Saving file", Snackbar.LENGTH_LONG)
                                    .setAction("Cancel", view1 -> Timber.d("button clicked"))
//                                .setAction("Cancel", v -> {throw new IllegalStateException("Button Cancel pressed");})
                                    .show();
                            Thread.sleep(4000);
//                        cancelFlag.doOnComplete(null);
//                            if (cancelFlag.subscribe(bb ->return bb)) return false;
                            bitmap1.compress(Bitmap.CompressFormat.PNG, 100, fos);
                            fos.flush();
                            Timber.d("Success!");
                            return true;
                        })
                        .observeOn(AndroidSchedulers.mainThread())
                        .subscribe(bool -> Snackbar
                                        .make(view, "File " + (bool ? "successfully" : "NOT") + " saved", Snackbar.LENGTH_LONG)
                                        .show()
                                , Timber::e);
            } catch (Exception e) {
                Timber.e(e.getLocalizedMessage());
            }
            //d.dispose();

        } else

        {
            if (bitmap1 == null)
                Snackbar.make(view, "No image to save", Snackbar.LENGTH_LONG).show();
            else Snackbar.make(view, "Permission required", Snackbar.LENGTH_LONG).show();

        }
    }
}
