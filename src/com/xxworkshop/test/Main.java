/*
 * Copyright (c) 2014 xxworkshop. All rights reserved.
 * Created by Broche Xu on 2/12/14 6:22 PM.
 */

package com.xxworkshop.test;

import com.xxworkshop.common.Formatter;

import java.util.HashMap;
import java.util.Map;

public class Main {
    public static void main(String[] args) {
        Formatter f = new Formatter();
        Map m = new HashMap();
        m.put("aaa", 111);
        m.put("bbb", "abc");
        System.out.println(f.map2String(m, "=##", "&%%"));
    }
}
