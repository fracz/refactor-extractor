/*
 * Copyright (c) 2012 Bump Technologies Inc. All rights reserved.
 */
package com.bumptech.photos.resize;

import android.graphics.Bitmap;
import android.graphics.BitmapFactory;
import android.graphics.Canvas;
import android.graphics.Matrix;
import android.graphics.Paint;
import android.media.ExifInterface;
import android.os.Build;
import com.bumptech.photos.resize.cache.SizedBitmapCache;
import com.bumptech.photos.util.Log;

import java.io.BufferedInputStream;
import java.io.FileInputStream;
import java.io.FileNotFoundException;
import java.io.IOException;
import java.io.InputStream;

/**
 * A class for synchronously resizing bitmaps with or without Bitmaps to recycle
 */
public class ImageResizer {
    private final SizedBitmapCache bitmapCache;

    /**
     * Creates a new resizer that will not recycle Bitmaps
     */
    public ImageResizer() {
        this(null);
    }

    /**
     * Creates a new resizer that will attempt to recycle {@link android.graphics.Bitmap}s if any are available in the given dimensions
     *
     * @param bitmapCache The cache to try to recycle {@link android.graphics.Bitmap}s from
     */
    public ImageResizer(SizedBitmapCache bitmapCache){
        this.bitmapCache = bitmapCache;
    }

    /**
     * Load the image at the given path at approximately the given dimensions, maintaining the original proportions,
     * and then crop the image down so that it fills the given dimensions
     *
     * @param path The path where the image is located
     * @param width The width the final image will fill
     * @param height The height the final image will fill
     * @return The resized image
     */
    public Bitmap resizeCenterCrop(final String path, final int width, final int height){
        final Bitmap streamed = streamIn(path, width, height);

        if (streamed.getWidth() == width && streamed.getHeight() == height) {
            return streamed;
        }

        return centerCrop(getRecycled(width, height), streamed, width, height);
    }

    /**
     * Load the image at the given path at approximately the given dimensions, maintaining the original proportions,
     * and then shrink the image down, again maintaining the original proportions, so that it fits entirely within the
     * given dimensions.
     *
     * @param path The path where the image is located
     * @param width The width the final image will fit within
     * @param height The height the final image will fit within
     * @return The resized image
     */
    public Bitmap fitInSpace(final String path, final int width, final int height){
        final Bitmap streamed = streamIn(path, width > height ? 1 : width, height > width ? 1 : height);
        return fitInSpace(streamed, width, height);
    }

    /**
     * Load the image at the given path at approximately the given dimensions, maintaining the original proportions
     *
     * @param path The path where the image is located
     * @param width The target width
     * @param height The target height
     * @return The resized image
     */
    public Bitmap loadApproximate(final String path, final int width, final int height){
        return streamIn(path, width, height);
    }

    /**
     * Load the image represented by the given InputStreams at its original size. Use the first InputStream to
     * try to determine the proportions of the image so that we can try to retrieve a recycled Bitmap of the correct
     * size. Use the second InputStream to actually load the image into a Bitmap. Note both InputStreams must represent
     * the same image and this method will close both InputStreams.
     *
     * @param is1 The InputStream used to get the dimensions of the image
     * @param is2 The InputStream used to load the image into memory
     * @return The loaded image
     */
    public Bitmap loadAsIs(final InputStream is1, final InputStream is2) {
        int[] dimens = new int[] {-1, -1};
        try {
            dimens = getDimension(is1);
        } finally {
            try {
                is1.close();
            } catch (IOException e) {
                e.printStackTrace();
            }
        }
        Bitmap resized = null;
        try {
            resized = load(is2, getRecycled(dimens));
        } finally {
            try {
                is2.close();
            } catch (IOException e) {
                e.printStackTrace();
            }
        }
        return resized;
    }

    /**
     * Load the image at the given path at its original size. Assume that the dimensions of the image at the given path
     * will match the given dimensions and use the given dimensions to retrieve a recycled Bitmap of the correct size.
     * Note this method will throw an exception if the dimensions of the image at the given path do not exactly match
     * the given dimensions and there is a bitmap of the given dimensions available to be recycled.
     *
     * The dimensions are given to avoid opening an InputStream specifically to determine the size of the image at the
     * given path and should be used when the dimensions of the image are known.
     *
     * @param path The path where the image is stored
     * @param width The width of the image at the given path
     * @param height The height of the image at the given path
     * @return The loaded image
     */
    public Bitmap loadAsIs(final String path, final int width, final int height) {
        return load(path, getRecycled(width, height));
    }

    /**
     * Load the image at the given path at its original size. Will create a second InputStream to first try to determine
     * the size of the image to attempt to retrieve a recycled Bitmap.
     *
     * @param path The path where the image is stored
     * @return The loaded image
     */
    public Bitmap loadAsIs(final String path){
        int[] dimens = getDimensions(path);
        return load(path, getRecycled(dimens));
    }

    private Bitmap getRecycled(int[] dimens) {
        return getRecycled(dimens[0], dimens[1]);
    }

    private Bitmap getRecycled(int width, int height) {
        Bitmap result = null;
        if (bitmapCache != null) {
            result = bitmapCache.get(width, height);
        }
        return result;
    }

    /**
     * An expensive operation to crop the given Bitmap so that it fills the given dimensions. This will not maintain
     * the original proportions of the image
     *
     * @param toCrop The Bitmap to crop
     * @param width The width of the final Bitmap
     * @param height The height of the final Bitmap
     * @return The resized image
     */
    public static Bitmap centerCrop(Bitmap toCrop, int width, int height) {
        return centerCrop(null, toCrop, width, height);
    }

    /**
     * A potentially expensive operation to crop the given Bitmap so that it fills the given dimensions. This operation
     * is significantly less expensive in terms of memory if a mutable Bitmap with the given dimensions is passed in
     * as well.
     *
     * @param recycled A mutable Bitmap with dimensions width and height that we can load the cropped portion of toCrop
     *                 into
     * @param toCrop The Bitmap to resize
     * @param width The width of the final Bitmap
     * @param height The height of the final Bitmap
     * @return The resized Bitmap (will be recycled if recycled is not null)
     */
    public static Bitmap centerCrop(Bitmap recycled, Bitmap toCrop, int width, int height) {
        if (toCrop.getWidth() == width && toCrop.getHeight() == height) {
            return toCrop;
        }
        //from ImageView/Bitmap.createScaledBitmap
        //https://android.googlesource.com/platform/frameworks/base/+/refs/heads/master/core/java/android/widget/ImageView.java
        //https://android.googlesource.com/platform/frameworks/base/+/refs/heads/master/graphics/java/android/graphics/Bitmap.java
        final float scale;
        float dx = 0, dy = 0;
        Matrix m = new Matrix();
        if (toCrop.getWidth() * height > width * toCrop.getHeight()) {
            scale = (float) height / (float) toCrop.getHeight();
            dx = (width - toCrop.getWidth() * scale) * 0.5f;
        } else {
            scale = (float) width / (float) toCrop.getWidth();
            dy = (height - toCrop.getHeight() * scale) * 0.5f;
        }

        m.setScale(scale, scale);
        m.postTranslate((int) dx + 0.5f, (int) dy + 0.5f);
        Bitmap result = recycled != null ? recycled : Bitmap.createBitmap(width, height, toCrop.getConfig());
        Canvas canvas = new Canvas(result);
        Paint paint = new Paint();
        //only if scaling up
        paint.setFilterBitmap(false);
        paint.setAntiAlias(true);
        canvas.drawBitmap(toCrop, m, paint);
        return result;
    }

    /**
     * An expensive operation to crop the given Bitmap to the given width by removing equal amounts from either side
     * so that the center of image remains
     *
     * @param toCrop The Bitmap to crop
     * @param width The width to crop the Bitmap to
     * @return A new Bitmap cropped to the given width, or toCrop if toCrop's width is equivalent to the given width
     */
    public static Bitmap cropToWidth(Bitmap toCrop, int width) {
        Bitmap cropped = toCrop;
        if (toCrop.getWidth() > width) {
            int extraWidth = toCrop.getWidth() - width;
            cropped = Bitmap.createBitmap(toCrop, extraWidth / 2, 0, width, toCrop.getHeight());
        }
        return cropped;
    }

    //crops a section height pixels tall in the center of the image with equal
    //amounts discarded above and below

    /**
     * An expensive operation to crop the given Bitmap to the given height by removing equal amounts from the top and
     * bottom so that the center of the image remains
     *
     * @param toCrop The Bitmap to crop
     * @param height The height to crop the Bitmap to
     * @return A new Bitmap cropped to the given height, or toCrop if toCrop's height is equivalent to the given height
     */
    public static Bitmap cropToHeight(Bitmap toCrop, int height){
        Bitmap cropped = toCrop;
        if (toCrop.getHeight() > height){
            int extraHeight = toCrop.getHeight() - height;
            cropped = Bitmap.createBitmap(toCrop, 0, extraHeight / 2, toCrop.getWidth(), height);
        }
        return cropped;
    }

    //shrinks to the given width, shrinking the height to maintain the original image proportions

    /**
     * An expensive operation to resize the given image, maintaining the original proportions, so that its width
     * matches the given width
     *
     * @param toShrink The Bitmap to shrink
     * @param width The width of the final Bitmap
     * @return A new Bitmap shrunk to the given width, or toShrink if toShrink's width is equivalent to the given width
     */
    public static Bitmap shrinkToWidth(Bitmap toShrink, int width){
        Bitmap shrunk = toShrink;
        float widthPercent = ((float) width/toShrink.getWidth());
        if (widthPercent != 1) {
            int shrunkImageHeight = Math.round(widthPercent * toShrink.getHeight());
            shrunk = Bitmap.createScaledBitmap(toShrink, width, shrunkImageHeight, true);
        }
        return shrunk;
    }

    /**
     * An expensive operation to resize the given image, maintaining the original proportions, so that its height
     * matches the given height
     *
     * @param toShrink The Bitmap to shrink
     * @param height The height of the final Bitmap
     * @return A new Bitmap shrunk to the given height, or toShrink if toShink's height is equivalent to the given
     *          height
     */
    public static Bitmap shrinkToHeight(Bitmap toShrink, int height){
        Bitmap shrunk = toShrink;
        float heightPercent = ((float) height/toShrink.getHeight());
        if (heightPercent != 1) {
            int shrunkImageWidth = Math.round(heightPercent * toShrink.getWidth());
            shrunk = Bitmap.createScaledBitmap(toShrink, shrunkImageWidth, height, true);
        }
        return shrunk;
    }

    /**
     * An expensive operation to resize the given Bitmap down so that it fits within the given dimensions maintaining
     * the original proportions
     *
     * @param toFit The Bitmap to shrink
     * @param width The width the final image will fit within
     * @param height The height the final image will fit within
     * @return A new Bitmap shrunk to fit within the given dimesions, or toFit if toFit's width or height matches the
     * given dimensions and toFit fits within the given dimensions
     */
    public static Bitmap fitInSpace(Bitmap toFit, int width, int height){
        if (height > width){
            return shrinkToWidth(toFit, width);
        } else {
            return shrinkToHeight(toFit, height);
        }
    }

    /**
     * An expensive operation to load the image at the given path
     *
     * @param path The path where the image is stored
     * @return A Bitmap representing the image at the given path
     */
    public static Bitmap load(String path) {
        return load(path, null);
    }

    /**
     * A potentially expensive operation to load the image at the given path. If a recycled Bitmap whose dimensions
     * exactly match those of the image at the given path is provided, the operation is much less expensive in terms
     * of memory.
     *
     * Note this method will throw an exception of a Bitmap with dimensions not matching those of the image at path
     * is provided.
     *
     * @param path The path where the image is stored
     * @param recycle A Bitmap we can load the image into, or null
     * @return A new bitmap containing the image at the given path, or recycle if recycle is not null
     */
    public static Bitmap load(String path, Bitmap recycle) {
        Bitmap result = null;
        try {
            result = load(new FileInputStream(path), recycle);
        } catch (FileNotFoundException e) {
            Log.d("PSR: file not found loading bitmap at: " + path);
            e.printStackTrace();
        }
        return result == null ? null : orientImage(path, result);
    }

    /**
     * An expensive operation to load the image from the given InputStream
     *
     * @param is The input stream representing the image data
     * @return A Bitmap representing the image at the given path
     */
    public static Bitmap load(InputStream is) {
        return load(is, null);
    }

    /**
     * A potentially expensive operation to load the image at the given path. If a recycled Bitmap whose dimensions
     * exactly match those of the image at the given path is provided, the operation is much less expensive in terms
     * of memory.
     *
     * Note this method will throw an exception of a Bitmap with dimensions not matching those of the image at path
     * is provided.
     *
     * @param is The InputStream representing the image data
     * @param recycle A Bitmap we can load the image into, or null
     * @return A new bitmap containing the image from the given InputStream, or recycle if recycle is not null
     */
    public static Bitmap load(InputStream is, Bitmap recycle){
        final BitmapFactory.Options decodeBitmapOptions = new BitmapFactory.Options();
        decodeBitmapOptions.inSampleSize = 1;
        decodeBitmapOptions.inPreferredConfig = Bitmap.Config.RGB_565;
        decodeBitmapOptions.inDither = false;
        if (Build.VERSION.SDK_INT >= 11) {
            decodeBitmapOptions.inMutable = true; //required or next attempt to recycle will fail
            if (recycle != null) {
                decodeBitmapOptions.inBitmap = recycle; //we can load photo without a bitmap to recycle,
                                                        //its just less efficient
            }
        }
        InputStream stream;
        Bitmap result = null;
        try {
            stream = new BufferedInputStream(is);
            result = BitmapFactory.decodeStream(stream, null, decodeBitmapOptions);
            stream.close();
        } catch (IOException e) {
            Log.d("PSR: io exception: " + e + " loading bitmap");
        }
        return result;
    }

    /**
     * A method for getting the dimensions of an image at the given path
     *
     * @param path The path wher ethe image is stored
     * @return an array containing the dimensions of the image in the form {width, height}
     */
    public static int[] getDimensions(String path) {
        int[] dimens = new int[]{-1, -1};
        try {
            InputStream is = new BufferedInputStream(new FileInputStream(path));
            dimens = getDimension(is);
            is.close();
        } catch (FileNotFoundException e) {
            e.printStackTrace();  //To change body of catch statement use File | Settings | File Templates.
        } catch (IOException e) {
            e.printStackTrace();  //To change body of catch statement use File | Settings | File Templates.
        }
        return dimens;
    }

     /**
     * A method for getting the dimensions of an image from the given InputStream
     *
     * @param is The InputStream representing the image
     * @return an array containing the dimensions of the image in the form {width, height}
     */
     public static int[] getDimension(InputStream is) {
        final BitmapFactory.Options decodeBoundsOptions = new BitmapFactory.Options();
        decodeBoundsOptions.inJustDecodeBounds = true;
        BitmapFactory.decodeStream(is, null, decodeBoundsOptions); //doesn't load, just sets the decodeBounds
        return new int[] { decodeBoundsOptions.outWidth, decodeBoundsOptions.outHeight };
    }


    /**
     * Load the image at the given path at nearly the given dimensions maintaining the original proportions. Will also
     * rotate the image according to the orientation in the images EXIF data if available.
     *
     * from http://stackoverflow.com/questions/7051025/how-do-i-scale-a-streaming-bitmap-in-place-without-reading-the-whole-image-first
     *
     * @param path The path where the image is stored
     * @param width The target width
     * @param height The target height
     * @return A Bitmap containing the image
     */
    public static Bitmap streamIn(String path, int width, int height) {
        int orientation = getOrientation(path);
        if(orientation == 90 || orientation == 270) {
            //Swap width and height for initial downsample calculation if its oriented so.
            //The image will then be rotated back to normal.
            int w = width;
            width = height;
            height = w;
        }

        Bitmap result = null;
        try {
            final int[] dimens = getDimensions(path);
            final int originalWidth = dimens[0];
            final int originalHeight = dimens[1];

            // inSampleSize prefers multiples of 2, but we prefer to prioritize memory savings
            int sampleSize = Math.min(originalHeight / height, originalWidth / width);

            final BitmapFactory.Options decodeBitmapOptions = new BitmapFactory.Options();
            // For further memory savings, you may want to consider using this option
            decodeBitmapOptions.inPreferredConfig = Bitmap.Config.RGB_565; // Uses 2-bytes instead of default 4 per pixel
            decodeBitmapOptions.inDither = false;

            InputStream is = new BufferedInputStream(new FileInputStream(path), 16384);

            decodeBitmapOptions.inSampleSize = sampleSize;
            if (Build.VERSION.SDK_INT > 11) {
                decodeBitmapOptions.inMutable = true;
            }
            result = BitmapFactory.decodeStream(is, null, decodeBitmapOptions);
            if (orientation != 0) {
                result = rotateImage(result, orientation);
            }
            is.close();
        } catch (Exception e){
            Log.d("PSR: error decoding image: " + e);
        } catch (OutOfMemoryError e){
            Log.d("PSR: not enough memory to resize image at " + path);
            Log.d(e.toString());
        }
        return result;
    }

   /**
     * Returns a matrix with rotation set based on Exif orientation tag.
     * If the orientation is undefined or 0 null is returned.
     *
     * @param pathToOriginal Path to original image file that may have exif data.
     * @return  A rotation in degrees based on exif orientation
     */
    public static int getOrientation(String pathToOriginal) {
        int degreesToRotate = 0;
        try{
            ExifInterface exif = new ExifInterface(pathToOriginal);
            int orientation = exif.getAttributeInt(ExifInterface.TAG_ORIENTATION, ExifInterface.ORIENTATION_UNDEFINED);
            if (orientation == ExifInterface.ORIENTATION_ROTATE_90){
                degreesToRotate = 90;
            } else if (orientation == ExifInterface.ORIENTATION_ROTATE_180){
                degreesToRotate = 180;
            } else if (orientation == ExifInterface.ORIENTATION_ROTATE_270){
                degreesToRotate = 270;
            }
        } catch (IOException e){
            Log.w("IOException for image with filePath=" + pathToOriginal);
        } catch (Exception e) {
            Log.w("Exception when trying to get image orientation matrix");
            e.printStackTrace();
        }
        return degreesToRotate;
    }

    /**
     * This is an expensive operation that copies the image in place with the pixels rotated.
     * If possible rather use getOrientationMatrix, and set that as the imageMatrix on an ImageView.
     *
     * @param pathToOriginal Path to original image file that may have exif data.
     * @param imageToOrient Image Bitmap to orient.
     * @return The oriented bitmap. May be the imageToOrient without modification, or a new Bitmap.
     */
    public static Bitmap orientImage(String pathToOriginal, Bitmap imageToOrient){
        int degreesToRotate = getOrientation(pathToOriginal);
        return rotateImage(imageToOrient, degreesToRotate);
    }

    /**
     * This is an expensive operation that copies the image in place with the pixels rotated.
     * If possible rather use getOrientationMatrix, and set that as the imageMatrix on an ImageView.
     *
     * @param imageToOrient Image Bitmap to orient.
     * @param degreesToRotate number of degrees to rotate the image by. If zero the original image is returned unmodified.
     * @return The oriented bitmap. May be the imageToOrient without modification, or a new Bitmap.
     */
    public static Bitmap rotateImage(Bitmap imageToOrient, int degreesToRotate) {
        try{
            if(degreesToRotate != 0) {
                Matrix matrix = new Matrix();
                matrix.setRotate(degreesToRotate);
                imageToOrient = Bitmap.createBitmap(
                        imageToOrient,
                        0,
                        0,
                        imageToOrient.getWidth(),
                        imageToOrient.getHeight(),
                        matrix,
                        true);
            }
        } catch (Exception e) {
            Log.w("Exception when trying to orient image");
            e.printStackTrace();
        }
        return imageToOrient;
    }

}