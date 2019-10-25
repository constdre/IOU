package com.ammuyutan.iou.Util;

import android.graphics.Bitmap;
import android.graphics.BitmapFactory;
import android.graphics.Matrix;
import android.media.ExifInterface;
import android.util.Log;
import android.widget.ImageView;

import java.io.IOException;

public class PhotoUtil {


    public static Bitmap rotateBitmap(String filePath, Bitmap sourceBitmap){
        Bitmap rotatedBitmap;
        try{
            ExifInterface exif = new ExifInterface(filePath);
            int rotation = exif.getAttributeInt(ExifInterface.TAG_ORIENTATION, ExifInterface.ORIENTATION_UNDEFINED);
            int rotationInDegrees = getExifRotationInDegrees(rotation);
            Matrix matrix = new Matrix();
            if (rotation != 0f) {
                //Why does it have to be float?
                matrix.postRotate(rotationInDegrees);
            }
            rotatedBitmap = Bitmap.createBitmap(sourceBitmap,0,0,sourceBitmap.getWidth(), sourceBitmap.getHeight(),matrix,true);
            return rotatedBitmap;
        }catch(IOException ex){
            ex.printStackTrace();
        }
        return null;
    }

    public static int getExifRotationInDegrees(int exifOrientation){
        switch(exifOrientation){
            case ExifInterface.ORIENTATION_ROTATE_90:return 90;
            case ExifInterface.ORIENTATION_ROTATE_180:return 180;
            case ExifInterface.ORIENTATION_ROTATE_270:return 270;
            case ExifInterface.ORIENTATION_NORMAL: return exifOrientation;
        }
        return 0;
    }
    public static Bitmap scaleDownBitmapToImageView(String filePath, ImageView imageView){

        // Get the dimensions of the View
        int targetW = imageView.getWidth();
        int targetH = imageView.getHeight();
        Log.i(Statics.LOG_TAG_MAIN, "ImageView width: " + targetW + ", height: " + targetH);


        // Get the dimensions of the bitmap
        BitmapFactory.Options opts = new BitmapFactory.Options();
        opts.inJustDecodeBounds = true; //to get the size (outWidth and outHeight) properties.
        BitmapFactory.decodeFile(filePath, opts);//decode the file into a Bitmap

        //the bitmap's full width and height:
        int bitmapW = opts.outWidth;
        int bitmapH = opts.outHeight;
        Log.i(Statics.LOG_TAG_MAIN, "bitmap width: " + bitmapW + ", height: " + bitmapH);


//         Determine how much to scale down the image
//        int scaleFactor = Math.min(bitmapW/targetW, bitmapH/targetH);
//        Log.i(Statics.LOG_TAG_MAIN, "scale Factor = " + scaleFactor);

        // Decode the image file into a Bitmap sized to fill the View
        opts.inJustDecodeBounds = false;
        opts.inSampleSize = 4; //e.g. 4 - image is scaled-down to its 1/4 size.

        Bitmap scaledBitmap = BitmapFactory.decodeFile(filePath, opts);
        //correct orientation of bitmap if necessary
        return rotateBitmap(filePath,scaledBitmap);
    }

}
