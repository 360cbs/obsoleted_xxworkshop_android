/*
 * Copyright (c) 2014 xxworkshop. All rights reserved.
 * Created by Broche Xu on 2/14/14 10:02 AM.
 */

package com.xxworkshop.common;

import java.util.UUID;

public final class XXGenerator {
    public final static String getUUID() {
        return UUID.randomUUID().toString().toUpperCase();
    }
}
