/*
 * Copyright (c) 2014 xxworkshop. All rights reserved.
 * Created by Broche Xu on 4/22/14 7:42 PM.
 */
package com.xxworkshop.ui;

import android.app.Activity;
import android.os.Bundle;
import android.os.Handler;
import android.os.Message;
import android.view.MotionEvent;
import android.view.View;
import android.widget.FrameLayout;
import com.xxworkshop.ui.welcome.Element;

import java.util.ArrayList;
import java.util.List;

public abstract class WelcomeHelper {
    protected Activity activity;
    private RootTouchListener rootTouchListener = new RootTouchListener();
    private FrameLayout root;
    private int step = 0;
    private List<Element> elements = new ArrayList<Element>();

    public WelcomeHelper(Activity activity) {
        this.activity = activity;
    }

    /**
     * Called when the activity is first created.
     */
    public void onCreate() {
        activity.setContentView(getLayoutResId());

        root = (FrameLayout) activity.findViewById(getRootViewId());
        new Handler() {
            @Override
            public void handleMessage(Message msg) {
                initElements(elements, root);
            }
        }.sendEmptyMessageDelayed(0, 50);

        root.setOnTouchListener(rootTouchListener);
    }

    protected abstract int getLayoutResId();

    protected abstract int getRootViewId();

    protected abstract int getStepCount();

    protected abstract void initElements(List<Element> elements, FrameLayout parentView);

    public abstract void OnLastPageFinish();

    public void forward() {
        if (step == getStepCount() - 1) {
            OnLastPageFinish();
        } else {
            for (Element element : elements) {
                element.forward(step);
            }
            step++;
            step = step > (getStepCount() - 1) ? (getStepCount() - 1) : step;
        }
    }

    public void backward() {
        if (step != 0) {
            for (Element element : elements) {
                element.backward(step);
            }
            step--;
            step = step < 0 ? 0 : step;
        }
    }

    private class RootTouchListener implements View.OnTouchListener {
        private float startX;
        private boolean isDragging = false;

        @Override
        public boolean onTouch(View view, MotionEvent motionEvent) {
            boolean flag = true;
            for (Element element : elements) {
                flag = element.isFinished() && flag;
                if (!flag) {
                    break;
                }
            }

            if (flag) {
                int action = motionEvent.getAction();
                if (action == MotionEvent.ACTION_DOWN) {
                    startX = motionEvent.getX();
                    isDragging = true;
                } else if (action == MotionEvent.ACTION_MOVE) {
                    if (isDragging) {
                        float x = motionEvent.getX();
                        float deltaX = x - startX;
                        for (Element element : elements) {
                            element.OnTouchMove(step, deltaX);
                        }
                    }
                } else if (action == MotionEvent.ACTION_UP) {
                    if (isDragging) {
                        float x = motionEvent.getX();
                        float deltaX = x - startX;
                        if (deltaX < -100) {
                            forward();
                        } else if (deltaX > 100) {
                            backward();
                        } else {
                            for (Element element : elements) {
                                element.reset(step);
                            }
                        }
                        isDragging = false;
                    }
                }
            }
            return true;
        }
    }
}
