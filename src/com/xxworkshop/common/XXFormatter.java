/*
 * Copyright (c) 2014 xxworkshop. All rights reserved.
 * Created by Broche Xu on 2/12/14 5:42 PM.
 */

package com.xxworkshop.common;

import android.util.Base64;
import org.json.JSONArray;
import org.json.JSONException;
import org.json.JSONObject;

import java.io.ByteArrayInputStream;
import java.io.ByteArrayOutputStream;
import java.io.IOException;
import java.util.Map;
import java.util.zip.GZIPInputStream;
import java.util.zip.GZIPOutputStream;

public final class XXFormatter {
    public final static String jsonEncode(JSONObject jobj) {
        return jobj.toString();
    }

    public final static String jsonEncode(JSONArray jarr) {
        return jarr.toString();
    }

    public final static JSONObject jsonObjectDecode(String json) {
        JSONObject jobj = null;
        try {
            jobj = new JSONObject(json);
        } catch (JSONException e) {
            e.printStackTrace();
        }
        return jobj;
    }

    public final static JSONArray jsonArrayDecode(String json) {
        JSONArray jarr = null;
        try {
            jarr = new JSONArray(json);
        } catch (JSONException e) {
            e.printStackTrace();
        }
        return jarr;
    }

    public final static String map2String(Map data, String itemSplitter, String sectionSplitter) {
        StringBuffer sb = new StringBuffer();
        for (Object key : data.keySet()) {
            sb.append(key);
            sb.append(itemSplitter);
            sb.append(data.get(key));
            sb.append(sectionSplitter);
        }
        if (sb.length() > 0) {
            sb.delete(sb.length() - sectionSplitter.length(), sb.length());
        }
        return sb.toString();
    }

    public final static byte[] zip(byte[] data) {
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

    public final static byte[] unZip(byte[] data) {
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

    public final static byte[] base64Encode(byte[] data) {
        return Base64.encode(data, Base64.DEFAULT);
    }

    public final static byte[] base64Decode(byte[] data) {
        return Base64.decode(data, Base64.DEFAULT);
    }

    public final static byte[] zipAndBaseEncode2(byte[] data) {
        return base64Encode(base64Encode(zip(data)));
    }
}