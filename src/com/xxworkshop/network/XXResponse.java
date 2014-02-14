/*
 * Copyright (c) 2014 xxworkshop. All rights reserved.
 * Created by Broche Xu on 2/13/14 5:56 PM.
 */

package com.xxworkshop.network;

import java.util.HashMap;

public class XXResponse {
    public String url;
    public HashMap<String, String> params;
    public boolean status;
    public String result;

    public XXResponse(String url, HashMap<String, String> params, boolean status, String result) {
        this.url = url;
        this.params = params;
        this.status = status;
        this.result = result;
    }
}
