/*
 * Copyright (c) 2014 xxworkshop. All rights reserved.
 * Created by Broche Xu on 2/12/14 5:42 PM.
 */

package com.xxworkshop.common;

import android.util.Base64;
import com.xxworkshop.common.formatter.Anchor;
import com.xxworkshop.common.formatter.Rect;
import org.json.JSONArray;
import org.json.JSONException;
import org.json.JSONObject;

import java.io.ByteArrayInputStream;
import java.io.ByteArrayOutputStream;
import java.io.IOException;
import java.text.SimpleDateFormat;
import java.util.*;
import java.util.zip.GZIPInputStream;
import java.util.zip.GZIPOutputStream;

public final class F {
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

    public final static String double2Date(double timestamp, String format) {
        SimpleDateFormat sdf = new SimpleDateFormat(format);
        sdf.setCalendar(Calendar.getInstance(TimeZone.getTimeZone("Asia/Shanghai")));
        long ltimestamp = (long) (timestamp * 1000);
        return sdf.format(new Date(ltimestamp));
    }


    public final static Rect convertRect(Rect origin, Anchor anchor) {
        Rect rect = new Rect();
        rect.w = origin.w;
        rect.h = origin.h;
        if (anchor == Anchor.LeftTop) {
            rect.x = origin.x;
            rect.y = origin.y;
        } else if (anchor == Anchor.CenterTop) {
            rect.x = origin.x - origin.w / 2;
            rect.y = origin.y;
        } else if (anchor == Anchor.RightTop) {
            rect.x = origin.x - origin.w;
            rect.y = origin.y;
        } else if (anchor == Anchor.LeftCenter) {
            rect.x = origin.x;
            rect.y = origin.y - origin.h / 2;
        } else if (anchor == Anchor.Center) {
            rect.x = origin.x - origin.w / 2;
            rect.y = origin.y - origin.h / 2;
        } else if (anchor == Anchor.RightCenter) {
            rect.x = origin.x - origin.w;
            rect.y = origin.y - origin.h / 2;
        } else if (anchor == Anchor.LeftBottom) {
            rect.x = origin.x;
            rect.y = origin.y - origin.h;
        } else if (anchor == Anchor.CenterBottom) {
            rect.x = origin.x - origin.w / 2;
            rect.y = origin.y - origin.h;
        } else if (anchor == Anchor.RightBottom) {
            rect.x = origin.x - origin.w;
            rect.y = origin.y - origin.h;
        } else {
            rect.x = origin.x;
            rect.y = origin.y;
        }
        return rect;
    }

    public final static JSONObject hashtable2JsonObject(Hashtable<String, String> hashtable) {
        JSONObject jsonObject = new JSONObject();
        try {
            for (String key : hashtable.keySet()) {
                jsonObject.put(key, hashtable.get(key));
            }
        } catch (JSONException e) {
            return new JSONObject();
        }
        return jsonObject;
    }

    public final static Hashtable<String, String> jsonObject2Hashtable(JSONObject jsonObject) {
        Hashtable<String, String> hashtable = new Hashtable<String, String>();
        try {
            Iterator<String> iterator = jsonObject.keys();
            while (iterator.hasNext()) {
                String key = iterator.next();
                hashtable.put(key, jsonObject.getString(key));
            }
        } catch (JSONException e) {
            return new Hashtable<String, String>();
        }
        return hashtable;
    }

    public final static JSONArray list2JsonArray(List<Hashtable<String, String>> list) {
        JSONArray jsonArray = new JSONArray();
        for (Hashtable<String, String> hashtable : list) {
            jsonArray.put(hashtable2JsonObject(hashtable));
        }
        return jsonArray;
    }

    public final static List<Hashtable<String, String>> jsonArray2List(JSONArray jsonArray) {
        List<Hashtable<String, String>> list = new ArrayList<Hashtable<String, String>>();
        for (int i = 0; i <= jsonArray.length() - 1; i++) {
            try {
                list.add(jsonObject2Hashtable(jsonArray.getJSONObject(i)));
            } catch (JSONException e) {
                e.printStackTrace();
            }
        }
        return list;
    }
}