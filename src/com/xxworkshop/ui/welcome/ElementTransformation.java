/*
 * Copyright (c) 2014 xxworkshop. All rights reserved.
 * Created by Broche Xu on 4/22/14 7:42 PM.
 */
package com.xxworkshop.ui.welcome;

public final class ElementTransformation {
    public float x = 0f;
    public float xOffset = 0f;
    public float y = 0f;
    public float yOffset = 0f;
    public float w = 0f;
    public float wOffset = 0f;
    public float h = 0f;
    public float hOffset = 0f;
    public float a = 0f;
    public float aOffset = 0f;

    public ElementTransformation setxOffset(float xOffset) {
        this.xOffset = xOffset;
        return this;
    }

    public ElementTransformation setyOffset(float yOffset) {
        this.yOffset = yOffset;
        return this;
    }

    public ElementTransformation setwOffset(float wOffset) {
        this.wOffset = wOffset;
        return this;
    }

    public ElementTransformation sethOffset(float hOffset) {
        this.hOffset = hOffset;
        return this;
    }

    public ElementTransformation setaOffset(float aOffset) {
        this.aOffset = aOffset;
        return this;
    }

    public ElementTransformation setX(float x) {
        this.x = x;
        return this;
    }

    public ElementTransformation setY(float y) {
        this.y = y;
        return this;
    }

    public ElementTransformation setW(float w) {
        this.w = w;
        return this;
    }

    public ElementTransformation setH(float h) {
        this.h = h;
        return this;
    }

    public ElementTransformation setA(float a) {
        this.a = a;
        return this;
    }
}
