package com.yirmak.photobook;

import androidx.activity.result.ActivityResult;
import androidx.activity.result.ActivityResultCallback;
import androidx.activity.result.ActivityResultLauncher;
import androidx.activity.result.contract.ActivityResultContracts;
import androidx.appcompat.app.AppCompatActivity;
import androidx.core.app.ActivityCompat;
import androidx.core.content.ContextCompat;

import android.Manifest;
import android.app.Activity;
import android.content.Intent;
import android.content.pm.PackageManager;
import android.database.Cursor;
import android.database.sqlite.SQLiteDatabase;
import android.database.sqlite.SQLiteStatement;
import android.graphics.Bitmap;
import android.graphics.BitmapFactory;
import android.graphics.ImageDecoder;
import android.net.Uri;
import android.os.Build;
import android.os.Bundle;
import android.provider.MediaStore;
import android.view.View;
import android.widget.Toast;

import com.google.android.material.snackbar.Snackbar;
import com.yirmak.photobook.databinding.ActivityPhotoBinding;

import java.io.ByteArrayOutputStream;

public class PhotoActivity extends AppCompatActivity {

    private ActivityPhotoBinding binding;
    ActivityResultLauncher<Intent> activityResultLauncher; //to go to gallery
    ActivityResultLauncher<String> permissionLauncher; // asking for permission
    Bitmap selectedimage;
    SQLiteDatabase database;


    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        binding = ActivityPhotoBinding.inflate(getLayoutInflater());
        View view = binding.getRoot();
        setContentView(view);

        registerLauncher();

        database = this.openOrCreateDatabase("Arts", MODE_PRIVATE,null);

        Intent intent = getIntent();
        String info = intent.getStringExtra("info");

        if (info.equals("new")) {

            binding.nameText.setText("");
            binding.nameText2.setText("");
            binding.yearText.setText("");
            binding.button.setVisibility(View.VISIBLE);
            binding.imageView.setImageResource(R.drawable.selectimage);

        } else {
            int artId = intent.getIntExtra("artId",0);
            binding.button.setVisibility(View.INVISIBLE);

            try {

                Cursor cursor = database.rawQuery("SELECT * FROM arts WHERE id = ?", new String[] {String.valueOf(artId)});
                int artNameIx = cursor.getColumnIndex("artname");
                int painterNameIx = cursor.getColumnIndex("paintername");
                int yearIx = cursor.getColumnIndex("year");
                int imageIx = cursor.getColumnIndex("image");

                while (cursor.moveToNext()) {
                    binding.nameText.setText(cursor.getString(artNameIx));
                    binding.nameText2.setText(cursor.getString(painterNameIx));
                    binding.yearText.setText(cursor.getString(yearIx));

                    byte[] bytes = cursor.getBlob(imageIx);
                    Bitmap bitmap = BitmapFactory.decodeByteArray(bytes,0,bytes.length);
                    binding.imageView.setImageBitmap(bitmap);

                }

                cursor.close();

            } catch (Exception e) {
                e.printStackTrace();
            }

        }

    }


    public void save(View view) {

        String name = binding.nameText.getText().toString();
        String artistname = binding.nameText2.getText().toString();
        String year = binding.yearText.getText().toString();

        Bitmap simage = makeSimage(selectedimage,300);

        ByteArrayOutputStream outputStream = new ByteArrayOutputStream();
        simage.compress(Bitmap.CompressFormat.PNG,50,outputStream);
        byte[] byteArray =  outputStream.toByteArray();

        try {

            database.execSQL("CREATE TABLE IF NOT EXISTS arts (id INTEGER PRIMARY KEY, artname VARCHAR, paintername VARCHAR, year VARCHAR, image  BLOB)");

            String sqlstring = "INSERT INTO arts (artname, paintername, year, image) VALUES(?, ?, ?, ?)";
            SQLiteStatement sqLiteStatement = database.compileStatement(sqlstring);
            sqLiteStatement.bindString(1,name);
            sqLiteStatement.bindString(2,artistname);
            sqLiteStatement.bindString(3,year);
            sqLiteStatement.bindBlob(4,byteArray);
            sqLiteStatement.execute();

        } catch (Exception e) {
            e.printStackTrace();
        }

        Intent intent = new Intent(PhotoActivity.this, MainActivity.class);
        intent.addFlags(Intent.FLAG_ACTIVITY_CLEAR_TOP);
        startActivity(intent);

    }

    public Bitmap makeSimage(Bitmap image, int maxSize) {
        int width = image.getWidth();
        int height = image.getHeight();

        float bitmapRatio = (float) width / (float) height;

        if (bitmapRatio > 1) {
            //landscape
            width = maxSize;
            height = (int) (width / bitmapRatio);
        } else {
            //portrait
            height = maxSize;
            width = (int) (height * bitmapRatio);
        }

        return image.createScaledBitmap(image,width,height,true);
    }
    public void selectImage(View view) {

        if (ContextCompat.checkSelfPermission(this, Manifest.permission.READ_EXTERNAL_STORAGE) != PackageManager.PERMISSION_GRANTED) {

            if (ActivityCompat.shouldShowRequestPermissionRationale(this,Manifest.permission.READ_EXTERNAL_STORAGE)) {

                Snackbar.make(view,"Permission is required for the gallery!",Snackbar.LENGTH_INDEFINITE).setAction("Give Permission", new View.OnClickListener() {
                    @Override
                    public void onClick(View view) {
                        //request permission
                        permissionLauncher.launch(Manifest.permission.READ_EXTERNAL_STORAGE);

                    }
                }).show();

            } else {
                //request permission
            }   permissionLauncher.launch(Manifest.permission.READ_EXTERNAL_STORAGE);

        } else {
            //gallery
            Intent intentGallery = new Intent(Intent.ACTION_PICK, MediaStore.Images.Media.EXTERNAL_CONTENT_URI);
            activityResultLauncher.launch(intentGallery);
        }

    }

    private void registerLauncher() {

        activityResultLauncher = registerForActivityResult(new ActivityResultContracts.StartActivityForResult(), new ActivityResultCallback<ActivityResult>() {
            @Override
            public void onActivityResult(ActivityResult result) {
                if (result.getResultCode() == RESULT_OK) {
                    Intent intentResult = result.getData();
                    if (intentResult != null) {
                        Uri imageData = intentResult.getData();

                        try {
                            if (Build.VERSION.SDK_INT >=28) {

                                ImageDecoder.Source source = ImageDecoder.createSource(getContentResolver(), imageData);
                                selectedimage = ImageDecoder.decodeBitmap(source);
                                binding.imageView.setImageBitmap(selectedimage);

                            } else {
                                selectedimage = MediaStore.Images.Media.getBitmap(PhotoActivity.this.getContentResolver(),imageData);
                                binding.imageView.setImageBitmap(selectedimage);
                            }

                        } catch (Exception e) {
                            e.printStackTrace();
                        }
                    }
                }
            }
        });

        permissionLauncher = registerForActivityResult(new ActivityResultContracts.RequestPermission(), new ActivityResultCallback<Boolean>() {
            @Override
            public void onActivityResult(Boolean result) {
                if (result) {
                    // permission granted
                    Intent intentGallery = new Intent(Intent.ACTION_PICK, MediaStore.Images.Media.EXTERNAL_CONTENT_URI);
                    activityResultLauncher.launch(intentGallery);

                } else {
                    // permission denied
                    Toast.makeText(PhotoActivity.this,"Permission needed!",Toast.LENGTH_LONG).show();

                }
            }
        });

    }

}