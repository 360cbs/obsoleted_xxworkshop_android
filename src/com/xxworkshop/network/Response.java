/*
 * Copyright (c) 2014 xxworkshop. All rights reserved.
 * Created by Broche Xu on 2/13/14 5:56 PM.
 */

package com.xxworkshop.network;

import java.util.Hashtable;

public class Response {
    public String url;
    public Hashtable<String, String> params;
    public boolean status;
    public String result;

    public Response(String url, Hashtable<String, String> params, boolean status, String result) {
        this.url = url;
        this.params = params;
        this.status = status;
        this.result = result;
    }
}
