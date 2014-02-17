/*
 * Copyright (c) 2014 xxworkshop. All rights reserved.
 * Created by Broche Xu on 2/13/14 4:50 PM.
 */

package com.xxworkshop.network;

import android.os.Handler;
import android.os.Message;
import com.xxworkshop.common.XXFormatter;
import com.xxworkshop.common.XXLog;

import java.io.*;
import java.net.HttpURLConnection;
import java.net.URL;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.Hashtable;

public final class XXHttpConnection {
    public static String Host = "";
    public static boolean Debug = false;
    private MessageHandler messageHandler = new MessageHandler();

    private static XXHttpConnection instance = new XXHttpConnection();

    public static XXHttpConnection getInstance() {
        return instance;
    }

    private Hashtable<String, ArrayList<XXResponseHandler>> handlers;

    private XXHttpConnection() {
        handlers = new Hashtable<String, ArrayList<XXResponseHandler>>();
    }

    public void sendRequest(String url, Hashtable<String, String> params) {
        sendRequest(url, params, XXHttpMethod.Get, null);
    }

    public void sendRequest(String url, Hashtable<String, String> params, XXResponseHandler handler) {
        sendRequest(url, params, XXHttpMethod.Get, handler);
    }

    public void sendRequest(String url, Hashtable<String, String> params, String method, XXResponseHandler handler) {
        XXHttpThread thread = new XXHttpThread(url, params, method, handler);
        thread.start();
    }

    public void addResponseHandler(XXResponseHandler handler, String url) {
        if (handlers.containsKey(url)) {
            ArrayList<XXResponseHandler> handlerList = handlers.get(url);
            handlerList.add(handler);
        } else {
            ArrayList<XXResponseHandler> handlerList = new ArrayList<XXResponseHandler>();
            handlerList.add(handler);
            handlers.put(url, handlerList);
        }
    }

    public void removeResponseHandler(XXResponseHandler handler, String url) {
        if (handlers.containsKey(url)) {
            ArrayList<XXResponseHandler> handlerList = handlers.get(url);
            if (handlerList.contains(handler)) {
                handlerList.remove(handler);
            }
        }
    }

    class XXHttpThread extends Thread {
        private String url;
        private Hashtable<String, String> params;
        private String method;
        private XXResponseHandler handler;

        public XXHttpThread(String url, Hashtable<String, String> params, String method, XXResponseHandler handler) {
            this.url = url;
            this.params = params;
            this.method = method;
            this.handler = handler;
        }

        @Override
        public void run() {
            String surl = Host + url;
            String sparams = XXFormatter.map2String(params, "=", "&");

            try {
                HttpURLConnection connection;
                if (method.equals(XXHttpMethod.Post)) {
                    connection = (HttpURLConnection) (new URL(surl)).openConnection();
                    connection.setDoInput(true);
                    connection.setDoOutput(true);
                    connection.setRequestMethod(method);
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
                    connection.setRequestMethod(method);
                    connection.connect();
                }
                if (Debug) {
                    XXLog.log("==========>\nurl: "+surl+"\nparams: "+sparams+"\nmethod: "+method);
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
                if (Debug) {
                    XXLog.log("<==========\nresult: "+sb.toString());
                }

                XXResponse response = new XXResponse(url, params, true, sb.toString());
                HashMap<String, Object> map = new HashMap<String, Object>();
                map.put("response", response);
                map.put("handler", handler);
                Message message = Message.obtain();
                message.obj = map;
                messageHandler.sendMessage(message);
            } catch (IOException e) {
                e.printStackTrace();
                XXResponse response = new XXResponse(url, params, false, "");
                HashMap<String, Object> map = new HashMap<String, Object>();
                map.put("response", response);
                map.put("handler", handler);
                Message message = Message.obtain();
                message.obj = map;
                messageHandler.sendMessage(message);
            }
        }
    }

    class MessageHandler extends Handler {
        @Override
        public void handleMessage(Message msg) {
            HashMap<String, Object> map = (HashMap<String, Object>) msg.obj;
            XXResponse response = (XXResponse) map.get("response");
            XXResponseHandler handler = (XXResponseHandler) map.get("handler");

            if (handler != null) {
                handler.handleResponse(response);
            }
            if (handlers.containsKey(response.url)) {
                ArrayList<XXResponseHandler> handlerList = handlers.get(response.url);
                for (XXResponseHandler thandler : handlerList) {
                    thandler.handleResponse(response);
                }
            }
        }
    }
}
