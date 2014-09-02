/*
 * Copyright (c) 2014 xxworkshop. All rights reserved.
 * Created by Broche Xu on 2/13/14 4:50 PM.
 */

package com.xxworkshop.network;

import android.os.Handler;
import android.os.Message;
import com.xxworkshop.common.F;
import com.xxworkshop.common.L;
import com.xxworkshop.common.S;
import com.xxworkshop.network.decoder.Decoder;
import com.xxworkshop.network.decoder.TextDecoder;

import java.io.IOException;
import java.io.OutputStream;
import java.io.OutputStreamWriter;
import java.net.HttpURLConnection;
import java.net.URL;
import java.net.URLEncoder;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.Hashtable;

public final class HttpConnection {
    public static String Host = "";
    public static boolean Debug = true;
    public static int DefaultCacheTimeout = 10;
    public static boolean SessionEnabled = true;

    private MessageHandler messageHandler = new MessageHandler();

    private static HttpConnection instance = new HttpConnection();

    public static HttpConnection getInstance() {
        return instance;
    }

    private Hashtable<String, ArrayList<ResponseHandler>> handlers;

    private HttpConnection() {
        handlers = new Hashtable<String, ArrayList<ResponseHandler>>();
    }

    private Hashtable<String, CacheItem> caches = new Hashtable<String, CacheItem>(50);

    private String cookie = "";

    private Decoder defaultDecoder = new TextDecoder();

    public void sendRequest(String url, Hashtable<String, String> params) {
        sendRequest(url, params, HttpMethod.Get, new Hashtable<String, String>(), null, null, false, 0);
    }

    public void sendRequest(String url, Hashtable<String, String> params, ResponseHandler handler) {
        sendRequest(url, params, HttpMethod.Get, new Hashtable<String, String>(), null, handler, false, 0);
    }

    public void sendRequest(String url, Hashtable<String, String> params, boolean useCache) {
        int cacheTimeout = 0;
        if (useCache) {
            cacheTimeout = DefaultCacheTimeout;
        }
        sendRequest(url, params, HttpMethod.Get, new Hashtable<String, String>(), null, null, useCache, cacheTimeout);
    }

    public void sendRequest(String url, Hashtable<String, String> params, boolean useCache, int cacheTimeout) {
        sendRequest(url, params, HttpMethod.Get, new Hashtable<String, String>(), null, null, useCache, cacheTimeout);
    }

    public void sendRequest(String url, Hashtable<String, String> params, ResponseHandler handler, boolean useCache) {
        int cacheTimeout = 0;
        if (useCache) {
            cacheTimeout = DefaultCacheTimeout;
        }
        sendRequest(url, params, HttpMethod.Get, new Hashtable<String, String>(), null, handler, useCache, cacheTimeout);
    }

    public void sendRequest(String url, Hashtable<String, String> params, ResponseHandler handler, boolean useCache, int cacheTimeout) {
        sendRequest(url, params, HttpMethod.Get, new Hashtable<String, String>(), null, handler, useCache, cacheTimeout);
    }

    public void sendRequest(String url, Hashtable<String, String> params, String method, Hashtable<String, String> headers, Decoder decoder, ResponseHandler handler, boolean useCache, int cacheTimeout) {
        XXHttpThread thread = new XXHttpThread(url, params, method, headers, decoder, handler, useCache, cacheTimeout);
        thread.start();
    }


    public void addResponseHandler(ResponseHandler handler, String url) {
        if (handlers.containsKey(url)) {
            ArrayList<ResponseHandler> handlerList = handlers.get(url);
            handlerList.add(handler);
        } else {
            ArrayList<ResponseHandler> handlerList = new ArrayList<ResponseHandler>();
            handlerList.add(handler);
            handlers.put(url, handlerList);
        }
    }

    public void removeResponseHandler(ResponseHandler handler, String url) {
        if (handlers.containsKey(url)) {
            ArrayList<ResponseHandler> handlerList = handlers.get(url);
            if (handlerList.contains(handler)) {
                handlerList.remove(handler);
            }
        }
    }

    private class XXHttpThread extends Thread {
        private String url;
        private Hashtable<String, String> params;
        private Hashtable<String, String> headers;
        private Decoder decoder;
        private String method;
        private ResponseHandler handler;
        private boolean useCache;
        private int cacheTimeout;

        public XXHttpThread(String url, Hashtable<String, String> params, String method, Hashtable<String, String> headers, Decoder decoder, ResponseHandler handler, boolean useCache, int cacheTimeout) {
            this.url = url;
            this.params = params;
            this.headers = headers;
            this.decoder = decoder;
            this.method = method;
            this.handler = handler;
            this.useCache = useCache;
            this.cacheTimeout = cacheTimeout;
        }

        @Override
        public void run() {
            String surl = Host + url;
            String sparams = F.map2String(params, "=", "&");

            if (Debug) {
                L.log("==========>\nurl: " + surl + "\nparams: " + sparams + "\ncookie: " + cookie + "\nmethod: " + method);
            }

            String cacheKey = surl + "?" + sparams;
            if (useCache) {
                if (caches.containsKey(cacheKey)) {
                    CacheItem ci = caches.get(cacheKey);
                    if (S.getTimeStamp() - ci.timestamp <= cacheTimeout) {
                        if (Debug) {
                            L.log("<==========\nresult(cache): " + ci.content);
                        }
                        Response response = new Response(url, params, true, ci.content);
                        HashMap<String, Object> map = new HashMap<String, Object>();
                        map.put("response", response);
                        map.put("handler", handler);
                        Message message = Message.obtain();
                        message.obj = map;
                        messageHandler.sendMessage(message);
                        return;
                    }
                }
            }

            try {
                HttpURLConnection connection;
                if (method.equals(HttpMethod.Post)) {
                    for (String key : params.keySet()) {
                        params.put(key, URLEncoder.encode(params.get(key)));
                    }
                    sparams = F.map2String(params, "=", "&");
                    connection = (HttpURLConnection) (new URL(surl)).openConnection();
                    connection.setDoInput(true);
                    connection.setDoOutput(true);
                    connection.setRequestMethod(method);
                    connection.setConnectTimeout(5000);
                    connection.setReadTimeout(10000);
                    if (SessionEnabled && !cookie.equals("")) {
                        connection.setRequestProperty("Cookie", cookie);
                    }
                    if (headers != null) {
                        for (String key : headers.keySet()) {
                            connection.setRequestProperty(key, headers.get(key));
                        }
                    }
                    connection.connect();

                    OutputStream os = connection.getOutputStream();
                    OutputStreamWriter osw = new OutputStreamWriter(os, "UTF-8");
                    osw.write(sparams);
                    osw.flush();
                    os.flush();
                    os.close();
                    osw.close();
                } else {
                    for (String key : params.keySet()) {
                        params.put(key, URLEncoder.encode(params.get(key)));
                    }
                    sparams = F.map2String(params, "=", "&");
                    String fullurl = surl + "?" + sparams;
                    connection = (HttpURLConnection) (new URL(fullurl)).openConnection();
                    connection.setDoInput(true);
                    connection.setConnectTimeout(5000);
                    connection.setReadTimeout(10000);
                    if (SessionEnabled && !cookie.equals("")) {
                        connection.setRequestProperty("Cookie", cookie);
                    }
                    if (headers != null) {
                        for (String key : headers.keySet()) {
                            connection.setRequestProperty(key, headers.get(key));
                        }
                    }
                    connection.connect();
                }

                if (SessionEnabled) {
                    String tcookie = connection.getHeaderField("Set-Cookie");
                    if (tcookie != null && !tcookie.equals("")) {
                        cookie = tcookie;
                    }
                }

                L.log("response code: " + connection.getResponseCode());

                Object result = null;
                if (decoder == null) {
                    result = defaultDecoder.decode(connection.getInputStream());
                } else {
                    result = decoder.decode(connection.getInputStream());
                }
                connection.disconnect();

                if (Debug) {
                    L.log("<==========\nresult: " + result.toString());
                }

                // cache
                if (result != null) {
                    if (caches.containsKey(cacheKey)) {
                        CacheItem ci = caches.get(cacheKey);
                        ci.content = result;
                        ci.timestamp = S.getTimeStamp();
                    } else {
                        CacheItem ci = new CacheItem();
                        ci.content = result;
                        ci.timestamp = S.getTimeStamp();
                        caches.put(cacheKey, ci);
                    }
                }

                Response response = new Response(url, params, true, result);
                HashMap<String, Object> map = new HashMap<String, Object>();
                map.put("response", response);
                map.put("handler", handler);
                Message message = Message.obtain();
                message.obj = map;
                messageHandler.sendMessage(message);
            } catch (IOException e) {
                e.printStackTrace();
                Response response = new Response(url, params, false, null);
                HashMap<String, Object> map = new HashMap<String, Object>();
                map.put("response", response);
                map.put("handler", handler);
                Message message = Message.obtain();
                message.obj = map;
                messageHandler.sendMessage(message);
            }
        }
    }

    private class MessageHandler extends Handler {
        @Override
        public void handleMessage(Message msg) {
            HashMap<String, Object> map = (HashMap<String, Object>) msg.obj;
            Response response = (Response) map.get("response");
            ResponseHandler handler = (ResponseHandler) map.get("handler");

            if (handlers.containsKey(response.url)) {
                ArrayList<ResponseHandler> handlerList = handlers.get(response.url);
                for (ResponseHandler thandler : handlerList) {
                    thandler.handleResponse(response);
                }
            }
            if (handler != null) {
                handler.handleResponse(response);
            }
        }
    }

    private class CacheItem {
        public double timestamp;
        public Object content;
    }
}
