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

import java.io.*;
import java.net.HttpURLConnection;
import java.net.URL;
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

    public void sendRequest(String url, Hashtable<String, String> params) {
        sendRequest(url, params, HttpMethod.Get, null, false, 0);
    }

    public void sendRequest(String url, Hashtable<String, String> params, ResponseHandler handler) {
        sendRequest(url, params, HttpMethod.Get, handler, false, 0);
    }

    public void sendRequest(String url, Hashtable<String, String> params, boolean useCache) {
        int cacheTimeout = 0;
        if (useCache) {
            cacheTimeout = DefaultCacheTimeout;
        }
        sendRequest(url, params, HttpMethod.Get, null, useCache, cacheTimeout);
    }

    public void sendRequest(String url, Hashtable<String, String> params, boolean useCache, int cacheTimeout) {
        sendRequest(url, params, HttpMethod.Get, null, useCache, cacheTimeout);
    }

    public void sendRequest(String url, Hashtable<String, String> params, ResponseHandler handler, boolean useCache) {
        int cacheTimeout = 0;
        if (useCache) {
            cacheTimeout = DefaultCacheTimeout;
        }
        sendRequest(url, params, HttpMethod.Get, handler, useCache, cacheTimeout);
    }

    public void sendRequest(String url, Hashtable<String, String> params, ResponseHandler handler, boolean useCache, int cacheTimeout) {
        sendRequest(url, params, HttpMethod.Get, handler, useCache, cacheTimeout);
    }

    public void sendRequest(String url, Hashtable<String, String> params, String method, ResponseHandler handler, boolean useCache, int cacheTimeout) {
        XXHttpThread thread = new XXHttpThread(url, params, method, handler, useCache, cacheTimeout);
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
        private String method;
        private ResponseHandler handler;
        private boolean useCache;
        private int cacheTimeout;

        public XXHttpThread(String url, Hashtable<String, String> params, String method, ResponseHandler handler, boolean useCache, int cacheTimeout) {
            this.url = url;
            this.params = params;
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
                    connection = (HttpURLConnection) (new URL(surl)).openConnection();
                    connection.setDoInput(true);
                    connection.setDoOutput(true);
                    connection.setRequestMethod(method);
                    connection.setConnectTimeout(5000);
                    connection.setReadTimeout(10000);
                    if (SessionEnabled && !cookie.equals("")) {
                        connection.setRequestProperty("Cookie", cookie);
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
                    connection = (HttpURLConnection) (new URL(surl + "?" + sparams)).openConnection();
                    connection.setDoInput(true);
                    connection.setDoOutput(true);
                    connection.setConnectTimeout(5000);
                    connection.setReadTimeout(10000);
                    connection.setRequestMethod(method);
                    if (SessionEnabled && !cookie.equals("")) {
                        connection.setRequestProperty("Cookie", cookie);
                    }
                    connection.connect();
                }

                if (SessionEnabled) {
                    String tcookie = connection.getHeaderField("Set-Cookie");
                    if (tcookie != null && !tcookie.equals("")) {
                        cookie = tcookie;
                    }
                }

                BufferedReader br = new BufferedReader(new InputStreamReader(connection.getInputStream(), "UTF-8"));
                String line = null;
                StringBuilder sb = new StringBuilder();
                while (null != (line = br.readLine())) {
                    sb.append(line);
                    sb.append("\n");
                }
                br.close();
                connection.disconnect();

                // cache
                if (caches.containsKey(cacheKey)) {
                    CacheItem ci = caches.get(cacheKey);
                    ci.content = sb.toString();
                    ci.timestamp = S.getTimeStamp();
                } else {
                    CacheItem ci = new CacheItem();
                    ci.content = sb.toString();
                    ci.timestamp = S.getTimeStamp();
                    caches.put(cacheKey, ci);
                }

                Response response = new Response(url, params, true, sb.toString());
                HashMap<String, Object> map = new HashMap<String, Object>();
                map.put("response", response);
                map.put("handler", handler);
                Message message = Message.obtain();
                message.obj = map;
                messageHandler.sendMessage(message);
            } catch (IOException e) {
                e.printStackTrace();
                Response response = new Response(url, params, false, "");
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
        public String content;
    }
}
