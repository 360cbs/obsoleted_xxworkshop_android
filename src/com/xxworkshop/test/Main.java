/*
 * Copyright (c) 2014 xxworkshop. All rights reserved.
 * Created by Broche Xu on 2/12/14 6:22 PM.
 */

package com.xxworkshop.test;

import com.xxworkshop.common.XXFormatter;

public class Main {
    public static void main(String[] args) {
        XXFormatter f = new XXFormatter();

        String str = "abcdefg你好，你大爷，你妹妹的。";
        byte[] data = str.getBytes();
        byte[] zdata = f.Base64Encode(data);
        String zstr = new String(zdata);
        System.out.println(zstr);

        byte[] uzdata = f.Base64Decode(zdata);
        String uzstr = new String(uzdata);
        System.out.println(uzstr);
    }
}
