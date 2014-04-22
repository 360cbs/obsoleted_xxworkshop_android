/*
 * Copyright (c) 2014 xxworkshop. All rights reserved.
 * Created by Broche Xu on 4/22/14 7:42 PM.
 */
package com.xxworkshop.ui.welcome;

import com.xxworkshop.common.formatter.Anchor;

public final class ElementState {
    public Anchor anchor = Anchor.LeftTop;
    public int x = 0;
    public int y = 0;
    public int w = 0;
    public int h = 0;
    public int a = 0;

    public ElementState setAnchor(Anchor anchor) {
        this.anchor = anchor;
        return this;
    }

    public ElementState setX(int x) {
        this.x = x;
        return this;
    }

    public ElementState setY(int y) {
        this.y = y;
        return this;
    }

    public ElementState setW(int w) {
        this.w = w;
        return this;
    }

    public ElementState setH(int h) {
        this.h = h;
        return this;
    }

    public ElementState setA(int a) {
        this.a = a;
        return this;
    }

    @Override
    public ElementState clone() {
        return new ElementState().setX(x).setY(y).setW(w).setH(h).setA(a).setAnchor(anchor);
    }
}
