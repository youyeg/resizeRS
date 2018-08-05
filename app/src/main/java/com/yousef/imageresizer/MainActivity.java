package com.yousef.imageresizer;

import android.content.Intent;
import android.database.Cursor;
import android.graphics.Bitmap;
import android.graphics.BitmapFactory;
import android.net.Uri;
import android.provider.MediaStore;
import android.support.v7.app.AppCompatActivity;
import android.support.v8.renderscript.*;
import android.os.Bundle;
import android.view.View;
import android.widget.Button;
import android.widget.TextView;

public class MainActivity extends AppCompatActivity {

    private Bitmap mBitmap;
    private RenderScript rs;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        rs=RenderScript.create(this);
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_main);
    }

    public void onButtonImportClicked(View view){
        Intent i = new Intent(
                Intent.ACTION_PICK,
                android.provider.MediaStore.Images.Media.EXTERNAL_CONTENT_URI);

        startActivityForResult(i, 101);
    }

    @Override
    protected void onActivityResult(int requestCode, int resultCode, Intent data) {
        super.onActivityResult(requestCode, resultCode, data);

        if (requestCode == 101 && resultCode == RESULT_OK && null != data) {
            Uri selectedImage = data.getData();
            String[] filePathColumn = { MediaStore.Images.Media.DATA };

            Cursor cursor = getContentResolver().query(selectedImage,
                    filePathColumn, null, null, null);
            cursor.moveToFirst();

            int columnIndex = cursor.getColumnIndex(filePathColumn[0]);
            String picturePath = cursor.getString(columnIndex);
            cursor.close();
            mBitmap = BitmapFactory.decodeFile(picturePath);
            TextView textImportSucceed = findViewById(R.id.text_import_succeed);
            Button buttonMethod1 = findViewById(R.id.button_method_1);
            Button buttonMethod2 = findViewById(R.id.button_method_2);
            textImportSucceed.setVisibility(View.VISIBLE);
            buttonMethod1.setEnabled(true);
            buttonMethod2.setEnabled(true);
        }
    }

    public void onButtonMethodOneClicked(View view){
        long startTime = System.nanoTime();
        // use mBitmap and do the calculation here
        for(long i = 0; i<10000000;i++){
            long b = 2*i;
        }
        Bitmap res = imageopsRS.bmpresize(rs,mBitmap,200,500);
        Bitmap res2 = imageopsRS.resizeBitmap2(rs,mBitmap,500);
        long difference = System.nanoTime() - startTime;
        TextView textResult1 = findViewById(R.id.text_method_1_result);
        textResult1.setText("It took : "+difference+" Nano second");
    }

    public void onButtonMethodTwoClicked(View view){
        long startTime = System.nanoTime();
        for(long i = 0; i<20000000;i++){
            long b = 2*i;
        }
        long difference = System.nanoTime() - startTime;
        TextView textResult2 = findViewById(R.id.text_method_2_result);
        textResult2.setText("It took : "+difference+" Nano second");
    }
}
