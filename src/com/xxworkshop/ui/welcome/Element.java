/*
 * Copyright (c) 2014 xxworkshop. All rights reserved.
 * Created by Broche Xu on 4/22/14 7:42 PM.
 */
package com.xxworkshop.ui.welcome;

import android.content.Context;
import android.os.Handler;
import android.os.Message;
import android.widget.FrameLayout;
import android.widget.ImageView;
import com.xxworkshop.common.F;
import com.xxworkshop.common.formatter.Rect;

import java.util.Hashtable;

public abstract class Element {
    protected Context context;
    protected FrameLayout parentView;
    protected ImageView imageView;

    private Hashtable<Integer, Boolean> animateFlags = new Hashtable<Integer, Boolean>();
    private TransformHelper transformHelper = null;
    private ElementState currentState = new ElementState();

    public Element(Context context, FrameLayout parent) {
        this.context = context;
        this.parentView = parent;
        initStates();
        initTransformations();
        initView();
    }

    protected abstract int getInitStep();

    protected abstract int getResId();

    protected abstract ElementState getState(int step);

    protected abstract ElementTransformation getTransformationForward(int step);

    protected abstract ElementTransformation getTransformationBackward(int step);

    protected void initStates() {

    }

    protected void initTransformations() {

    }

    protected void initView() {
        ElementState state = getState(getInitStep());

        imageView = new ImageView(context);
        imageView.setImageResource(getResId());
        imageView.setAlpha(state.a);

        Rect rect = F.convertRect(new Rect(state.x, state.y, state.w, state.h), state.anchor);
        FrameLayout.LayoutParams layoutParams = new FrameLayout.LayoutParams(rect.w, rect.h);
        layoutParams.leftMargin = rect.x;
        layoutParams.topMargin = rect.y;
        parentView.addView(imageView, layoutParams);

        currentState.x = rect.x;
        currentState.y = rect.y;
        currentState.w = rect.w;
        currentState.h = rect.h;
        currentState.a = state.a;
    }

    protected void animateOnce(int step) {
        animateFlags.put(step, true);
    }

    protected void animateAways(int step) {
    }

    protected boolean getAnimateFlag(int step) {
        if (animateFlags.containsKey(step)) {
            return animateFlags.get(step);
        } else {
            return false;
        }
    }

    protected int getDimension(int resId) {
        return (int) context.getResources().getDimension(resId);
    }

    public ImageView getView() {
        return imageView;
    }

    public void OnTouchMove(int step, float delta) {
        if (transformHelper != null) {
            return;
        }

        boolean isForward = delta < 0;

        delta = Math.abs(delta);
        ElementState startState = getState(step);
        ElementState endState = null;
        ElementTransformation transformation = null;
        if (isForward) {
            endState = getState(step + 1);
            transformation = getTransformationForward(step);
        } else {
            endState = getState(step - 1);
            transformation = getTransformationBackward(step);
        }
        if (endState == null || transformation == null) {
            return;
        }

        Rect startRect = F.convertRect(new Rect(startState.x, startState.y, startState.w, startState.h), startState.anchor);
        Rect endRect = F.convertRect(new Rect(endState.x, endState.y, endState.w, endState.h), endState.anchor);

        float ox = 0;
        float oy = 0;
        float ow = 0;
        float oh = 0;
        float oa = 0;
        if (delta > transformation.xOffset) {
            ox = (delta - transformation.xOffset) * transformation.x;
        }
        if (delta > transformation.yOffset) {
            oy = (delta - transformation.yOffset) * transformation.y;
        }
        if (delta > transformation.wOffset) {
            ow = (delta - transformation.wOffset) * transformation.w;
        }
        if (delta > transformation.hOffset) {
            oh = (delta - transformation.hOffset) * transformation.h;
        }
        if (delta > transformation.aOffset) {
            oa = (delta - transformation.aOffset) * transformation.a;
        }

        int dx = (int) (startRect.x + ox);
        int dy = (int) (startRect.y + oy);
        int dw = (int) (startRect.w + ow);
        int dh = (int) (startRect.h + oh);
        int da = (int) (startState.a + oa);

        currentState.x = transformation.x > 0 ? (dx > endRect.x ? endRect.x : dx) : (dx < endRect.x ? endRect.x : dx);
        currentState.y = transformation.y > 0 ? (dy > endRect.y ? endRect.y : dy) : (dy < endRect.y ? endRect.y : dy);
        currentState.w = transformation.w > 0 ? (dw > endRect.w ? endRect.w : dw) : (dw < endRect.w ? endRect.w : dw);
        currentState.h = transformation.h > 0 ? (dh > endRect.h ? endRect.h : dh) : (dh < endRect.h ? endRect.h : dh);
        currentState.a = transformation.a > 0 ? (da > endState.a ? endState.a : da) : (da < endState.a ? endState.a : da);

        FrameLayout.LayoutParams layoutParams = (FrameLayout.LayoutParams) imageView.getLayoutParams();
        layoutParams.leftMargin = currentState.x;
        layoutParams.topMargin = currentState.y;
        layoutParams.width = currentState.w;
        layoutParams.height = currentState.h;
        imageView.setLayoutParams(layoutParams);
        imageView.setAlpha(currentState.a);
    }

    public boolean isFinished() {
        return transformHelper == null;
    }

    public void forward(int step) {
        OnTouchUp(step + 1);
    }

    public void reset(int step) {
        OnTouchUp(step);
    }

    public void backward(int step) {
        OnTouchUp(step - 1);
    }

    private void OnTouchUp(int step) {
        ElementState endState = getState(step);
        if (transformHelper != null) {
            return;
        }
        if (endState == null) {
            return;
        }

        transformHelper = new TransformHelper(step);
        transformHelper.sendMessage(Message.obtain());
    }

    private class TransformHelper extends Handler {
        private int step;
        private ElementState endState = null;

        public TransformHelper(int step) {
            ElementState endState = getState(step);
            this.endState = endState;
            this.step = step;
        }

        @Override
        public void handleMessage(Message msg) {
            Rect endRect = F.convertRect(new Rect(endState.x, endState.y, endState.w, endState.h), endState.anchor);
            FrameLayout.LayoutParams layoutParams = (FrameLayout.LayoutParams) imageView.getLayoutParams();

            int ox = (int) ((endRect.x - currentState.x) / 3.0f);
            int oy = (int) ((endRect.y - currentState.y) / 3.0f);
            int ow = (int) ((endRect.w - currentState.w) / 3.0f);
            int oh = (int) ((endRect.h - currentState.h) / 3.0f);
            int oa = (int) ((endState.a - currentState.a) / 3.0f);

            ox = Math.abs(ox) <= 1 ? 0 : ox;
            oy = Math.abs(oy) <= 1 ? 0 : oy;
            ow = Math.abs(ow) <= 1 ? 0 : ow;
            oh = Math.abs(oh) <= 1 ? 0 : oh;
            oa = Math.abs(oa) <= 1 ? 0 : oa;

            currentState.x = currentState.x + ox;
            currentState.y = currentState.y + oy;
            currentState.w = currentState.w + ow;
            currentState.h = currentState.h + oh;
            currentState.a = currentState.a + oa;

            layoutParams.leftMargin = currentState.x;
            layoutParams.topMargin = currentState.y;
            layoutParams.width = currentState.w;
            layoutParams.height = currentState.h;
            imageView.setLayoutParams(layoutParams);
            imageView.setAlpha(currentState.a);
            if ((ox + oy + ow + oh + oa) == 0) {
                if (!animateFlags.containsKey(step) || !animateFlags.get(step)) {
                    animateOnce(step);
                }
                animateAways(step);
                transformHelper = null;
            } else {
                sendMessageDelayed(Message.obtain(), 20);
            }
        }
    }
}
