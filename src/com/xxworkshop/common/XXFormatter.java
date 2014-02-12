/*
 * Copyright (c) 2014 xxworkshop. All rights reserved.
 * Created by Broche Xu on 2/12/14 5:42 PM.
 */

package com.xxworkshop.common;

import android.util.Base64;
import android.util.Base64InputStream;
import org.json.JSONArray;
import org.json.JSONException;
import org.json.JSONObject;

import java.io.*;
import java.util.zip.GZIPInputStream;
import java.util.zip.GZIPOutputStream;

public final class XXFormatter {
    public static String JsonEncode(JSONObject jobj) {
        return jobj.toString();
    }

    public static String JsonEncode(JSONArray jarr) {
        return jarr.toString();
    }

    public static JSONObject JsonObjectDecode(String json) {
        JSONObject jobj = null;
        try {
            jobj = new JSONObject(json);
        } catch (JSONException e) {
            e.printStackTrace();
        }
        return jobj;
    }

    public static JSONArray JsonArrayDecode(String json) {
        JSONArray jarr = null;
        try {
            jarr = new JSONArray(json);
        } catch (JSONException e) {
            e.printStackTrace();
        }
        return jarr;
    }

    public static byte[] Zip(byte[] data) {
        byte[] result = null;
        try {
            ByteArrayInputStream bais = new ByteArrayInputStream(data);
            ByteArrayOutputStream baos = new ByteArrayOutputStream();
            GZIPOutputStream gos = new GZIPOutputStream(baos);
            byte[] buffer = new byte[2048];
            int readSize = 0;
            while (-1 != (readSize = bais.read(buffer, 0, buffer.length))) {
                gos.write(buffer, 0, readSize);
            }
            gos.finish();
            gos.flush();
            baos.flush();
            result = baos.toByteArray();
            bais.close();
            baos.close();
            gos.close();
        } catch (IOException e) {
            e.printStackTrace();
        }
        return result;
    }

    public static byte[] UnZip(byte[] data) {
        byte[] result = null;
        try {
            ByteArrayInputStream bais = new ByteArrayInputStream(data);
            GZIPInputStream gis = new GZIPInputStream(bais);
            ByteArrayOutputStream baos = new ByteArrayOutputStream();
            byte[] buffer = new byte[2048];
            int readSize = 0;
            while (-1 != (readSize = gis.read(buffer, 0, buffer.length))) {
                baos.write(buffer, 0, readSize);
            }
            baos.flush();
            result = baos.toByteArray();
            gis.close();
            bais.close();
            baos.close();
        } catch (IOException e) {
            e.printStackTrace();
        }
        return result;
    }

    public static byte[] Base64Encode(byte[] data) {
        return Base64.encode(data, Base64.DEFAULT);
    }

    public static byte[] Base64Decode(byte[] data) {
        return Base64.decode(data, Base64.DEFAULT);
    }

    public static byte[] ZipAndBaseEncode2(byte[] data) {
        return Base64Encode(Base64Encode(Zip(data)));
    }
}