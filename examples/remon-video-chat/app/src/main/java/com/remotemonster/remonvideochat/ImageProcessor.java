/*
 * @author    Lucas Choi <lucas@remotemonster.com>
 * Copyright (c) 2017 RemoteMonster, inc. All Right Reserved.
 */

package com.remotemonster.remonvideochat;

import android.app.Activity;
import android.content.Context;
import android.database.Cursor;
import android.graphics.Bitmap;
import android.graphics.BitmapFactory;
import android.graphics.Matrix;
import android.media.ExifInterface;
import android.net.Uri;
import android.provider.MediaStore;
import android.support.v4.content.CursorLoader;
import android.util.Base64;
import android.util.Log;

import java.io.ByteArrayInputStream;
import java.io.ByteArrayOutputStream;
import java.io.File;
import java.io.FileOutputStream;
import java.io.IOException;
import java.io.OutputStream;

/**
 * Class description goes here.
 *
 * @author Lucas Choi <lucas@remotemonster.com>
 * @version 2017-05-16.
 */
public class ImageProcessor {
    public static File getWebPViaBitmap(Activity activity, String id, Bitmap bitmap) {
        File fileDir = activity.getFilesDir();
        File imageFile = new File(fileDir, id + ".webp");
        OutputStream os;
        try {
            os = new FileOutputStream(imageFile);
            bitmap.compress(Bitmap.CompressFormat.WEBP, 80, os);
            os.flush();
            os.close();
        } catch (Exception e) {
            Log.e("getWebPViaBitmap", e.toString());
        }
        return imageFile;
    }

    public static Bitmap resizeBitmapImageFn(Bitmap bmpSource, int maxResolution){
        int iWidth = bmpSource.getWidth();      //비트맵이미지의 넓이
        int iHeight = bmpSource.getHeight();     //비트맵이미지의 높이
        int newWidth = iWidth ;
        int newHeight = iHeight ;
        float rate = 0.0f;

        //이미지의 가로 세로 비율에 맞게 조절
        if(iWidth > iHeight ){
            if(maxResolution < iWidth ){
                rate = maxResolution / (float) iWidth ;
                newHeight = (int) (iHeight * rate);
                newWidth = maxResolution;
            }
        }else{
            if(maxResolution < iHeight ){
                rate = maxResolution / (float) iHeight ;
                newWidth = (int) (iWidth * rate);
                newHeight = maxResolution;
            }
        }
        return Bitmap.createScaledBitmap(bmpSource, newWidth, newHeight, true);
    }

    public static Bitmap getRotatedBitmap(String path, Bitmap bitmap){
        Bitmap rotatedBitmap = null;
        Matrix m = new Matrix();
        ExifInterface exif = null;
        int orientation = 1;

        try {
            if(path!=null){
                exif = new ExifInterface(path);
            }
            if(exif!=null){
                orientation = exif.getAttributeInt(ExifInterface.TAG_ORIENTATION, 0);
                switch(orientation){
                    case ExifInterface.ORIENTATION_ROTATE_270:
                        m.preRotate(270);
                        break;

                    case ExifInterface.ORIENTATION_ROTATE_90:
                        m.preRotate(90);
                        break;
                    case ExifInterface.ORIENTATION_ROTATE_180:
                        m.preRotate(180);
                        break;
                }
                rotatedBitmap = Bitmap.createBitmap(bitmap,0,0,bitmap.getWidth(),bitmap.getHeight(),m,true);
            }
        } catch (IOException e) {
            e.printStackTrace();
        }
        return rotatedBitmap;
    }

    public static String getRealPathFromURI(Context ctx, Uri contentUri) {
        String[] proj = { MediaStore.Images.Media.DATA };

        CursorLoader cursorLoader = new CursorLoader(ctx, contentUri, proj, null, null, null);
        Cursor cursor = cursorLoader.loadInBackground();

        int column_index = cursor.getColumnIndexOrThrow(MediaStore.Images.Media.DATA);
        cursor.moveToFirst();
        return cursor.getString(column_index);
    }

    public static String bitmapToBase64(Bitmap btBitmap) {
        ByteArrayOutputStream baos = new ByteArrayOutputStream();
        btBitmap.compress(Bitmap.CompressFormat.PNG, 100, baos);
        byte[] bImage = baos.toByteArray();
        return Base64.encodeToString(bImage, 0);
    }

    public static Bitmap Base64ToBitmap(String base64) {
        byte[] bImage = Base64.decode(base64, 0);
        ByteArrayInputStream bais = new ByteArrayInputStream(bImage);
        return  BitmapFactory.decodeStream(bais);
    }
}
