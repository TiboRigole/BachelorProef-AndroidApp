package com.retailsonar.retailsonar.regio;

import android.Manifest;
import android.content.ContentResolver;
import android.content.Intent;
import android.content.pm.PackageManager;
import android.database.Cursor;
import android.graphics.Bitmap;
import android.graphics.BitmapFactory;
import android.graphics.Matrix;
import android.graphics.drawable.BitmapDrawable;
import android.net.Uri;
import android.os.AsyncTask;
import android.os.Bundle;

import android.os.Environment;
import android.os.StrictMode;
import android.provider.MediaStore;
import android.support.annotation.Nullable;
import android.support.design.internal.BottomNavigationItemView;
import android.support.v4.app.ActivityCompat;
import android.support.v4.app.Fragment;
import android.support.v4.content.FileProvider;
import android.support.v4.view.ViewPager;
import android.support.v7.app.AppCompatActivity;
import android.util.Base64;
import android.util.Log;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.Button;
import android.widget.ImageView;
import android.widget.Toast;

import com.google.gson.JsonElement;
import com.google.gson.JsonObject;
import com.retailsonar.retailsonar.AppConstants;
import com.retailsonar.retailsonar.R;
import com.retailsonar.retailsonar.entities.Pand;
import com.retailsonar.retailsonar.entities.User;

import com.retailsonar.retailsonar.services.PandService;
import com.retailsonar.retailsonar.services.UserService;

import java.io.ByteArrayOutputStream;
import java.io.File;
import java.io.FileInputStream;
import java.io.FileNotFoundException;
import java.io.FileOutputStream;
import java.io.IOException;
import java.nio.file.Files;
import java.nio.file.Path;
import java.nio.file.Paths;
import java.text.SimpleDateFormat;
import java.util.Date;


import javax.xml.transform.Result;

import retrofit2.Call;
import retrofit2.Callback;
import retrofit2.Response;
import retrofit2.Retrofit;
import retrofit2.converter.gson.GsonConverterFactory;

import static android.app.Activity.RESULT_OK;

/**
 * created by Aaron Hallaert on 4/3/2018.
 *
 * Deze activity zorgt voor het wijzigen van foto van een pand
 *
 */

public class TakePicture_Activity extends AppCompatActivity {

    // plaats in layout waar gekozen of getrokken afbeelding komt
    ImageView imageView;

    // HTTP request help
    Retrofit.Builder builder= new Retrofit.Builder()
            .baseUrl(AppConstants.BASE_URL_SERVER)
            .addConverterFactory(GsonConverterFactory.create());
    Retrofit retrofit=builder.build();
    PandService pandService= retrofit.create(PandService.class);

    // huidige pand
    Pand huidigPand;
    long currentPandId;


    // path van genomen photo
    String mCurrentPhotoPath;
    // tijdelijke file genomen afbeelding
    File image;



    //  byte array en bitmap gegenereerd na nemen van foto
    Bitmap genomenAfbeeldingBitmap;
    byte[] afbeelding;

    // bitmap van huidige afbeelding die in database staat opgeslaan
    Bitmap afbeeldingBitMap;


    // waardes om acties te controleren
    static final int REQUEST_TAKE_PHOTO = 1;
    static final int SELECT_PHOTO = 10;
    static final int SIZE_LIMIT=200;




    // start pagina
    @Override
    public void onCreate(Bundle savedInstanceState) {

        super.onCreate(savedInstanceState);
        setContentView(R.layout.add_picture);


        //opvragen huidige pand
        currentPandId= (long) (getIntent().getExtras().get("pandId"));
        Call<Pand> pandRequest= pandService.getPandById(currentPandId);
        pandRequest.enqueue(new Callback<Pand>() {
            @Override
            public void onResponse(Call<Pand> call, Response<Pand> response) {
                huidigPand=new Pand(response.body());
            }

            @Override
            public void onFailure(Call<Pand> call, Throwable t) {
                t.printStackTrace();
            }
        });


        // camera openen
        Button btnCamera=(Button) findViewById(R.id.btnCamera);
        // foto in imageview opslaan
        Button btnPersist=(Button) findViewById(R.id.btnPersist);
        // foto zoeken in gallery
        Button btnZoek= (Button) findViewById(R.id.btnZoek);
        // rotate picture
        Button btnRotate= (Button) findViewById(R.id.btnRotate);
        // imageView
        imageView= (ImageView) findViewById(R.id.imageView);

        // opvragen van huidige afbeelding in databank en in imageView zetten
        Call<JsonObject> afbeeldingCall=pandService.getAfbeeldingPand(currentPandId);
        afbeeldingCall.enqueue(new Callback<JsonObject>() {
            @Override
            public void onResponse(Call<JsonObject> call, Response<JsonObject> response) {
                //your codes here

                int SDK_INT = android.os.Build.VERSION.SDK_INT;
                if (SDK_INT > 8)
                {
                    StrictMode.ThreadPolicy policy = new StrictMode.ThreadPolicy.Builder()
                            .permitAll().build();
                    StrictMode.setThreadPolicy(policy);


                    try {
                        if(response.body()!=null) {
                            afbeelding = Base64.decode(response.body().get("afbeelding").getAsString(), Base64.DEFAULT);
                            afbeeldingBitMap = BitmapFactory.decodeByteArray(afbeelding, 0, afbeelding.length);
                            imageView.setImageBitmap(afbeeldingBitMap);

                        }
                        else{
                            imageView.setImageResource(R.drawable.noimage);
                        }

                    } catch (Exception e) {
                        e.printStackTrace();
                    }


                }
            }

            @Override
            public void onFailure(Call<JsonObject> call, Throwable t) {
                t.printStackTrace();
            }
        });

        /**
         * draaien van bitmap en in imageview zetten
         */
        btnRotate.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {
                Matrix matrix = new Matrix();
                matrix.postRotate(-90);
                Bitmap current= ((BitmapDrawable)imageView.getDrawable()).getBitmap();

                Bitmap rotatedBitmap = Bitmap.createBitmap(current, 0, 0, current.getWidth(), current.getHeight(), matrix, true);
                imageView.setImageBitmap(rotatedBitmap);
            }
        });


        /**
         * camera starten en resultaat opvangen bij "onActivityResult"
         */
        btnCamera.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {
                Intent takePictureIntent = new Intent(MediaStore.ACTION_IMAGE_CAPTURE);
                // Ensure that there's a camera activity to handle the intent
                if (takePictureIntent.resolveActivity(getPackageManager()) != null) {
                    // Create the File where the photo should go
                    File photoFile = null;
                    try {
                        photoFile = createImageFile();
                    } catch (IOException ex) {
                        // Error occurred while creating the File

                    }
                    // Continue only if the File was successfully created
                    if (photoFile != null) {
                        Uri photoURI = FileProvider.getUriForFile((getApplicationContext()),
                                "com.retailsonar.retailsonar.fileprovider",
                                photoFile);
                        takePictureIntent.putExtra(MediaStore.EXTRA_OUTPUT, photoURI);
                        setResult(RESULT_OK, takePictureIntent);
                        startActivityForResult(takePictureIntent, REQUEST_TAKE_PHOTO);

                    }
                }

            }
        });


        btnPersist.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {
                // als de imageview niet leeg is, is het nuttig om te saven
                if(((BitmapDrawable)imageView.getDrawable()).getBitmap()!=null) {
                    saveImage();
                }
            }
        });

        btnZoek.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {

                try {
                    if (ActivityCompat.checkSelfPermission(getApplicationContext(), Manifest.permission.READ_EXTERNAL_STORAGE) != PackageManager.PERMISSION_GRANTED) {
                        ActivityCompat.requestPermissions(TakePicture_Activity.this, new String[]{Manifest.permission.READ_EXTERNAL_STORAGE, Manifest.permission.WRITE_EXTERNAL_STORAGE}, SELECT_PHOTO);
                    } else {
                        Intent kiesPictureIntent = new Intent(Intent.ACTION_PICK);
                        kiesPictureIntent.setType("image/*");
                        startActivityForResult(kiesPictureIntent, SELECT_PHOTO);
                    }
                } catch (Exception e) {
                    e.printStackTrace();
                }

            }
        });


    }

    @Override
    public void onActivityResult(int requestCode, int resultCode, Intent data) {

        if(requestCode==REQUEST_TAKE_PHOTO ) {
            compressBitmap(image, 1,100);
            String filePath = image.getPath();

            Bitmap bitmap = BitmapFactory.decodeFile(filePath);
            if(bitmap!=null) {
                Matrix matrix = new Matrix();

                matrix.postRotate(90);

                genomenAfbeeldingBitmap = Bitmap.createBitmap(bitmap, 0, 0, bitmap.getWidth(), bitmap.getHeight(), matrix, true);

                genomenAfbeeldingBitmap=Bitmap.createBitmap(genomenAfbeeldingBitmap, 0,0,genomenAfbeeldingBitmap.getWidth(), genomenAfbeeldingBitmap.getWidth());
                imageView.setImageBitmap(genomenAfbeeldingBitmap);
            }

            super.onActivityResult(requestCode, resultCode, data);

        }

        if(requestCode==SELECT_PHOTO && data != null){
            // Let's read picked image data - its URI
            Uri pickedImage = data.getData();
            // Let's read picked image path using content resolver
            String[] filePath = { MediaStore.Images.Media.DATA };
            Cursor cursor = getContentResolver().query(pickedImage, filePath, null, null, null);
            cursor.moveToFirst();
            String imagePath = cursor.getString(cursor.getColumnIndex(filePath[0]));

            BitmapFactory.Options options = new BitmapFactory.Options();
            options.inPreferredConfig = Bitmap.Config.ARGB_8888;
            Bitmap bitmap = BitmapFactory.decodeFile(imagePath, options);
            Matrix matrix = new Matrix();

            matrix.postRotate(90);

            bitmap = Bitmap.createBitmap(bitmap, 0, 0, bitmap.getWidth(), bitmap.getHeight(), matrix, true);
            // Do something with the bitmap
            bitmap=Bitmap.createBitmap(bitmap,0,0, bitmap.getWidth(), bitmap.getWidth());
            ImageView img= findViewById(R.id.imageView);


            img.setImageBitmap(bitmap);

            // At the end remember to close the cursor or you will end with the RuntimeException!
            cursor.close();
        }
     }




    private File createImageFile() throws IOException {

        // Create an image file name
        String timeStamp = new SimpleDateFormat("yyyyMMdd_HHmmss").format(new Date());
        String imageFileName = "JPEG_" + timeStamp + "_";
        File storageDir = getExternalFilesDir(Environment.DIRECTORY_PICTURES);
        image = File.createTempFile(
                imageFileName,  /* prefix */
                ".jpg",         /* suffix */
                storageDir      /* directory */
        );

        // Save a file: path for use with ACTION_VIEW intents
        mCurrentPhotoPath = image.getAbsolutePath();
        return image;
    }


     public void saveImage(){
        // bedoeling om image die in imageView staat, door te sturen naar de database
         // bitmap -> byte array -> file ... compress ... -> byte array -> base64
        AsyncTask.execute(new Runnable() {
            @Override
            public void run() {


                    // Create the File where the photo should go
                    File photoFile = null;
                    try {
                        photoFile = createImageFile();
                    } catch (IOException ex) {
                        // Error occurred while creating the File

                    }
                    // Continue only if the File was successfully created
                    if (photoFile != null) {
                        Uri photoURI = FileProvider.getUriForFile(getApplicationContext(),
                                "com.retailsonar.retailsonar.fileprovider",
                                photoFile);

                    }







                // json object waar base 64 string afbeelding in moet
                JsonObject obj = new JsonObject();

                // aanmaken van outputstream voor FILE
                FileOutputStream fileOutputStream= null;
                try {
                    fileOutputStream = new FileOutputStream(photoFile);
                } catch (FileNotFoundException e) {
                    e.printStackTrace();
                }



                // voor conversie bitmap -> byte array
                ByteArrayOutputStream stream = new ByteArrayOutputStream();

                // BITMAP
                Bitmap bitmapFromView = ((BitmapDrawable)imageView.getDrawable()).getBitmap();

                // bitmap naar file
                //bitmapFromView.compress(Bitmap.CompressFormat.PNG, 100, fileOutputStream );

                // bitmap naar byte array
                bitmapFromView.compress(Bitmap.CompressFormat.PNG, 100, stream);
                byte[] byteArray = stream.toByteArray();


                try {
                    fileOutputStream.write(byteArray);
                }
                catch(IOException e){
                    e.printStackTrace();
                }

                compressBitmap(photoFile, 1, 100);


                // file naar byte array

                try {
                    Path path = null;
                    if (android.os.Build.VERSION.SDK_INT >= android.os.Build.VERSION_CODES.O) {
                        path = Paths.get(photoFile.getAbsolutePath());
                        byteArray = Files.readAllBytes(path);
                    }

                }
                catch (IOException e){
                    e.printStackTrace();
                }

                obj.addProperty("afbeelding", Base64.encodeToString(byteArray,
                        Base64.NO_WRAP));
                System.out.println(currentPandId);


                Call<Pand> setAfbeelding = pandService.setAfbeeldingPandId(obj, currentPandId);
                System.out.println(obj.get("afbeelding").toString());
                System.out.println(setAfbeelding.request().toString());
                setAfbeelding.enqueue(new Callback<Pand>() {
                    @Override
                    public void onResponse(Call<Pand> call, Response<Pand> response) {
                        System.out.println("done");
                        Toast.makeText(TakePicture_Activity.this, "set afbeelding", Toast.LENGTH_SHORT).show();

                    }

                    @Override
                    public void onFailure(Call<Pand> call, Throwable t) {
                        Toast.makeText(TakePicture_Activity.this, "set afbeelding mislukt", Toast.LENGTH_SHORT).show();
                    }
                });
            }
        });





     }


    public void compressBitmap(File file, int sampleSize, int quality) {
        try {
            BitmapFactory.Options options = new BitmapFactory.Options();
            options.inSampleSize = sampleSize;
            FileInputStream inputStream = new FileInputStream(file);

            Bitmap selectedBitmap = BitmapFactory.decodeStream(inputStream, null, options);
            inputStream.close();

            FileOutputStream outputStream = new FileOutputStream(image.getPath());


            selectedBitmap.compress(Bitmap.CompressFormat.JPEG, quality, outputStream);


            outputStream.close();
            long lengthInKb = file.length() / 1024; //in kb
            if (lengthInKb > SIZE_LIMIT) {
                compressBitmap(file, (sampleSize*2), (quality/4));
            }

            selectedBitmap.recycle();
        } catch (Exception e) {
            e.printStackTrace();
        }
    }


}
