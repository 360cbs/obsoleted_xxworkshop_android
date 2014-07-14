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

import javax.crypto.Mac;
import javax.crypto.spec.SecretKeySpec;
import java.io.ByteArrayInputStream;
import java.io.ByteArrayOutputStream;
import java.io.IOException;
import java.security.InvalidKeyException;
import java.security.MessageDigest;
import java.security.NoSuchAlgorithmException;
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

    public final static byte[] base64Encode(byte[] data, int flags) {
        return Base64.encode(data, flags);
    }

    public final static byte[] base64Decode(byte[] data) {
        return Base64.decode(data, Base64.DEFAULT);
    }

    public final static byte[] base64Decode(byte[] data, int flags) {
        return Base64.decode(data, flags);
    }

    private final static String HMAC_SHA1 = "HmacSHA1";

    public final static byte[] hmacSHA1(byte[] data, byte[] key) {
        SecretKeySpec signingKey = new SecretKeySpec(key, HMAC_SHA1);
        try {
            Mac mac = Mac.getInstance(HMAC_SHA1);
            mac.init(signingKey);
            byte[] rawHmac = mac.doFinal(data);
            return rawHmac;
        } catch (NoSuchAlgorithmException e) {
            e.printStackTrace();
            return new byte[0];
        } catch (InvalidKeyException e) {
            e.printStackTrace();
            return new byte[0];
        }
    }

    private final static char hexDigits[] = {'0', '1', '2', '3', '4', '5', '6', '7', '8', '9', 'A', 'B', 'C', 'D', 'E', 'F'};

    public final static String md5(byte[] data) {

        try {
            MessageDigest md = MessageDigest.getInstance("MD5");
            md.update(data);
            byte[] result = md.digest();
            return new String(bytes2String(result));
        } catch (NoSuchAlgorithmException e) {
            e.printStackTrace();
            return "";
        }
    }

    public final static String bytes2String(byte[] data) {
        char[] c = new char[data.length * 2];
        for (int i = 0; i <= data.length - 1; i++) {
            byte b = data[i];
            c[i * 2] = hexDigits[b >> 4 & 0xF];
            c[i * 2 + 1] = hexDigits[b & 0xF];
        }
        return new String(c);
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

    public static String time2String(double timestamp) {
        double timeInterval = S.getTimeStamp() - timestamp;
        if (timeInterval < 60) {
            return "1分钟内";
        } else if (timeInterval < 3600) {//1小时内
            return (int) timeInterval / 60 + "分钟前";
        } else if (timeInterval < 3600 * 24) {//24小时内
            return (int) timeInterval / 3600 + "小时前";
        } else if (timeInterval < 3600 * 24 * 2) {
            return "昨天";
        } else if (timeInterval < 3600 * 24 * 3) {
            return "2天前";
        } else if (timeInterval < 3600 * 24 * 4) {
            return "3天前";
        } else if (timeInterval < 3600 * 24 * 5) {
            return "4天前";
        } else if (timeInterval < 3600 * 24 * 6) {
            return "5天前";
        } else if (timeInterval < 3600 * 24 * 7) {
            return "6天前";
        } else if (timeInterval < 3600 * 24 * 15) {
            return "1周前";
        } else if (timeInterval < 3600 * 24 * 16) {
            return "2周前";
        } else if (timeInterval < 3600 * 24 * 31) {
            return "半月前";
        } else {
            Integer year1 = Integer.parseInt(new SimpleDateFormat("yyyy").format(new Date()));
            Integer year2 = Integer.parseInt(F.double2Date(timestamp, "yyyy"));
            Integer month1 = Integer.parseInt(new SimpleDateFormat("MM").format(new Date()));
            Integer month2 = Integer.parseInt(F.double2Date(timestamp, "MM"));
            int monthCount = (year1 - year2 - 1) * 12 + (12 - month2) + month1;
            if (monthCount / 12 > 0) {
                return monthCount / 12 + "年前";
            } else {
                return monthCount + "月前";
            }
        }
    }
}