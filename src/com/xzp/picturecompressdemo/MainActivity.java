package com.xzp.picturecompressdemo;

import android.Manifest;
import android.annotation.SuppressLint;
import android.app.Activity;
import android.content.ContentUris;
import android.content.Intent;
import android.content.pm.PackageManager;
import android.database.Cursor;
import android.graphics.Bitmap;
import android.graphics.BitmapFactory;
import android.net.Uri;
import android.os.Build;
import android.os.Environment;
import android.provider.DocumentsContract;
import android.provider.MediaStore;
import android.support.v4.app.ActivityCompat;
import android.support.v4.content.ContextCompat;
import android.os.Bundle;
import android.view.View;
import android.widget.Button;
import android.widget.ImageView;
import android.widget.Toast;




import java.io.ByteArrayOutputStream;
import java.io.File;
import java.io.FileOutputStream;
import java.io.IOException;



public class MainActivity extends Activity {



    private Button album_btn;

    private ImageView image_iv;



    @Override

    protected void onCreate(Bundle savedInstanceState) {

        super.onCreate(savedInstanceState);

        setContentView(R.layout.activity_main);



        initView();

    }



    private void initView() {



        album_btn = (Button) findViewById(R.id.album);

        image_iv = (ImageView) findViewById(R.id.image);



        initData();

    }



    private void initData() {



        album_btn.setOnClickListener(new View.OnClickListener() {

            @Override

            public void onClick(View v) {


                    openAblum();

               

            }

        });

    }



    private void openAblum() {

        Intent intent = new Intent("android.intent.action.GET_CONTENT");

        intent.setType("image/*");

        startActivityForResult(intent, 0x02);

    }






    @Override

    protected void onActivityResult(int requestCode, int resultCode,  Intent data) {

        if (requestCode == 0x02 && resultCode == RESULT_OK) {

            if (Build.VERSION.SDK_INT >= 19) {

                handleImageOnKitKat(data);

            } else {

                handleImageBeforeKitkat(data);

            }

        }

    }



   

    @SuppressLint("NewApi") private void handleImageOnKitKat(Intent data) {

        String imagePath = null;

        Uri uri = data.getData();
        


        if (DocumentsContract.isDocumentUri(MainActivity.this, uri)) {

            String documentId = DocumentsContract.getDocumentId(uri);

            if ("com.android.providers.media.documents".equals(uri.getAuthority())) {

                String id = documentId.split(":")[1];

                String selection = MediaStore.Images.Media._ID + "=" + id;

                imagePath = getImagePath(MediaStore.Images.Media.EXTERNAL_CONTENT_URI, selection);
                Toast.makeText(MainActivity.this, "222"+imagePath, Toast.LENGTH_LONG).show();


            } else if ("com.android.providers.downloads.documents".equals(uri.getAuthority())) {

                Uri contentUri = ContentUris.withAppendedId(Uri.parse("content://downloads/public_downloads"), Long.valueOf(documentId));

                imagePath = getImagePath(contentUri, null);
                Toast.makeText(MainActivity.this, "222"+uri.toString(), Toast.LENGTH_LONG).show();
            }





        } else if ("content".equalsIgnoreCase(uri.getScheme())) {

            imagePath = getImagePath(uri, null);





        } else if ("file".equalsIgnoreCase(uri.getScheme())) {

            imagePath = uri.getPath();





        }



        displayPath(imagePath);

    }



    private void handleImageBeforeKitkat(Intent data) {

        Uri uri = data.getData();

        String imagePath = getImagePath(uri, null);
        Toast.makeText(MainActivity.this, "111"+imagePath, Toast.LENGTH_LONG).show();
        displayPath(imagePath);

    }



    private void displayPath(String imagePath) {

        if (imagePath != null) {

            Bitmap bitmap = BitmapFactory.decodeFile(imagePath);

            //压缩图片

            Bitmap bitmap1 = compressBySampleSize(bitmap, 2, true);

            image_iv.setImageBitmap(bitmap1);

            //保存图片

            saveBmp2Gallery(bitmap1, System.currentTimeMillis() + "");

        } else {

            Toast.makeText(MainActivity.this, "获得图片失败", Toast.LENGTH_SHORT).show();

        }

    }



    private String getImagePath(Uri uri, String selection) {

        String path = null;

        Cursor cursor = getContentResolver().query(uri, null, selection, null, null);

        if (cursor != null) {

            if (cursor.moveToFirst()) {

                path = cursor.getString(cursor.getColumnIndex(MediaStore.Images.Media.DATA));

            }

            cursor.close();

        }



        return path;

    }





    /**

     * @param bmp     获取的bitmap数据

     * @param picName 自定义的图片名

     */

    public void saveBmp2Gallery(Bitmap bmp, String picName) {
        String fileName = null;
        //系统相册目录
        String galleryPath = Environment.getExternalStorageDirectory()
                + File.separator + Environment.DIRECTORY_DCIM
                + File.separator + "Camera" + File.separator;
        // 声明文件对象
        File file = null;
        // 声明输出流
        FileOutputStream outStream = null;
        try {

            // 如果有目标文件，直接获得文件对象，否则创建一个以filename为名称的文件
            file = new File(galleryPath, picName + ".jpg");
            // 获得文件相对路径

            fileName = file.toString();

            // 获得输出流，如果文件中有内容，追加内容

            outStream = new FileOutputStream(fileName);

            if (null != outStream) {

                bmp.compress(Bitmap.CompressFormat.JPEG, 30, outStream);

            }
        } catch (Exception e) {

            e.getStackTrace();

        } finally {

            try {

                if (outStream != null) {

                    outStream.close();

                }

            } catch (IOException e) {

                e.printStackTrace();

            }

        }
        //通知相册更新

        MediaStore.Images.Media.insertImage(MainActivity.this.getContentResolver(),

                bmp, fileName, null);

        Intent intent = new Intent(Intent.ACTION_MEDIA_SCANNER_SCAN_FILE);

        Uri uri = Uri.fromFile(file);

        intent.setData(uri);

        MainActivity.this.sendBroadcast(intent);



        Toast.makeText(MainActivity.this, "已保存"+fileName, Toast.LENGTH_LONG).show();
        
         

    }



    //采样压缩

    public Bitmap compressBySampleSize(final Bitmap src,final int sampleSize, final boolean recycle) {

        if (src == null) {

            return null;

        }

        BitmapFactory.Options options = new BitmapFactory.Options();

        options.inSampleSize = sampleSize;

        ByteArrayOutputStream baos = new ByteArrayOutputStream();

        src.compress(Bitmap.CompressFormat.JPEG, 100, baos);

        byte[] bytes = baos.toByteArray();

        if (recycle && !src.isRecycled()) {

            src.recycle();

        }

        return BitmapFactory.decodeByteArray(bytes, 0, bytes.length, options);

    }





}