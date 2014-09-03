package com.studio1r.retrofitsandbox.db;

import android.content.ContentValues;
import android.content.Context;
import android.database.Cursor;
import android.util.Base64;
import android.util.Log;

import com.studio1r.retrofitsandbox.api.model.VideoDetail;
import com.studio1r.retrofitsandbox.db.video.contract.VideoDetailContract;

import java.io.ByteArrayInputStream;
import java.io.ByteArrayOutputStream;
import java.io.IOException;
import java.io.ObjectInputStream;
import java.io.ObjectOutputStream;
import java.io.Serializable;

/**
 * Created by nelsonramirez on 9/2/14.
 */
public class DBHelper {

    private static final String TAG = "DBHelper";

    public static void insertVideoDetail(Context context, VideoDetail video) {
        ContentValues cv = new ContentValues();
        cv.put(VideoDetailContract.INTERNAL, video.code);
        cv.put(VideoDetailContract.CODE, video.code);
        cv.put(VideoDetailContract.SERIALIZED_OBJECT, toEncodedString(video));
        context.getContentResolver().update(VideoDetailContract.CONTENT_URI, cv,
                VideoDetailContract.CODE + " = ? ", new String[]{video.code});
    }

    private static String toEncodedString(Serializable s) {
        ByteArrayOutputStream byteArrayOutputStream = new ByteArrayOutputStream();
        try {
            ObjectOutputStream objectOutputStream = new ObjectOutputStream(byteArrayOutputStream);
            objectOutputStream.writeObject(s);
            objectOutputStream.close();
            return new String(Base64.encodeToString(byteArrayOutputStream.toByteArray(), 0));
        } catch (IOException e) {
            e.printStackTrace();
        }
        return "";
    }

    private static Object fromEncodedString(String s) {
        byte[] data = Base64.decode(s, 0);
        try {
            ObjectInputStream ois = new ObjectInputStream(
                    new ByteArrayInputStream(data));
            Object o = ois.readObject();
            ois.close();
            return o;
        } catch (IOException e) {
            e.printStackTrace();
        } catch (ClassNotFoundException e) {
            e.printStackTrace();
        }
        return null;
    }

    public static VideoDetail getVideoDetail(Context context, String id) {
        Cursor cursor = context.getContentResolver().query(VideoDetailContract.CONTENT_URI,
                null, VideoDetailContract.CODE + " = ? ", new String[]{id}, "");
        if (cursor != null && cursor.moveToFirst()) {
            VideoDetail detail = (VideoDetail) fromEncodedString(cursor.getString(
                    cursor.getColumnIndex(VideoDetailContract.SERIALIZED_OBJECT)));
            cursor.close();
            return detail;
        }
        return null;

    }
}