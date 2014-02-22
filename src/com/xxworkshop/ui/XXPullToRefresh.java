/*
 * Copyright (c) 2014 xxworkshop. All rights reserved.
 * Created by Broche Xu on 2/19/14 8:29 PM.
 */

package com.xxworkshop.ui;

import android.content.Context;
import android.os.Handler;
import android.os.Message;
import android.util.AttributeSet;
import android.view.LayoutInflater;
import android.view.MotionEvent;
import android.view.View;
import android.view.ViewGroup;
import android.widget.LinearLayout;
import android.widget.ScrollView;

import java.util.Observable;
import java.util.Observer;

public class XXPullToRefresh extends LinearLayout implements Observer {
    private int DONEDELAY = 500;

    private XXPullToRefreshDelegate delegate;
    private OnTouchListener scrollViewOnTouchListener;

    private LayoutInflater inflater;
    private Context context;
    private Handler handler = new Handler() {
        @Override
        public void handleMessage(Message msg) {
            if (!XXPullToRefresh.this.onTimerTick()) {
                handler.sendMessageDelayed(Message.obtain(), 5);
            }
        }
    };

    private ViewGroup headerView;
    private ScrollView scrollView;
    private ViewGroup bodyView;

    private XXPullToRefreshState state = XXPullToRefreshState.DEfault;
    private int headerHeight;
    private boolean isDragging = false;
    private int doneDelay = 0;


    public XXPullToRefresh(Context context) {
        super(context);
        this.context = context;
        this.setOrientation(VERTICAL);
    }

    public XXPullToRefresh(Context context, AttributeSet attrs) {
        super(context, attrs);
        this.context = context;
        this.setOrientation(VERTICAL);
    }

    @Override
    public void update(Observable observable, Object o) {
        if (observable == delegate) {
            doneDelay = 0;
            state = XXPullToRefreshState.DONE;
            this.delegate.OnStateChanged(headerView, state);
            handler.sendMessage(Message.obtain());
        }
    }

    public void init(int headerid, int bodyid, XXPullToRefreshDelegate delegate) {
        inflater = (LayoutInflater) context.getSystemService(Context.LAYOUT_INFLATER_SERVICE);
        this.delegate = delegate;
        if (delegate != null) {
            delegate.addObserver(this);
        }

        // header view
        headerView = (ViewGroup) inflater.inflate(headerid, null);
        measureView(headerView);
        headerHeight = headerView.getMeasuredHeight();
        headerView.setPadding(0, -headerHeight, 0, 0);

        // scroll view
        scrollView = new ScrollView(context);
        scrollView.setLayoutParams(new ViewGroup.LayoutParams(ViewGroup.LayoutParams.FILL_PARENT, ViewGroup.LayoutParams.WRAP_CONTENT));
        bodyView = (ViewGroup) inflater.inflate(bodyid, null);
        scrollView.addView(bodyView);

        this.addView(headerView);
        this.addView(scrollView);

        scrollView.setOnTouchListener(new OnTouchListener() {
            private int startEventY;
            private int lastHeaderPaddingY;

            @Override
            public boolean onTouch(View view, MotionEvent motionEvent) {
                int action = motionEvent.getAction();
                if (action == MotionEvent.ACTION_DOWN) {
                    startEventY = (int) motionEvent.getY();
                    lastHeaderPaddingY = headerView.getPaddingTop();
                } else if (action == MotionEvent.ACTION_MOVE) {
                    if (scrollView.getScrollY() <= 0) {
                        if (state != XXPullToRefreshState.REFRESHING) {
                            int offset = (int) motionEvent.getY() - startEventY;
                            if (!isDragging && offset > 0) {
                                isDragging = true;
                                startEventY = (int) motionEvent.getY();
                                lastHeaderPaddingY = headerView.getPaddingTop();
                                offset = 0;
                            }
                            if (isDragging) {
                                int headerPaddingY = offset + lastHeaderPaddingY;
                                headerPaddingY = headerPaddingY <= -headerHeight ? -headerHeight : headerPaddingY;
                                headerPaddingY = headerPaddingY >= 0 ? 0 : headerPaddingY;
                                headerView.setPadding(0, headerPaddingY, 0, 0);
                                lastHeaderPaddingY = headerPaddingY;
                                if (headerPaddingY < 0 && state != XXPullToRefreshState.PULL_TO_REFRESH) {
                                    state = XXPullToRefreshState.PULL_TO_REFRESH;
                                    XXPullToRefresh.this.delegate.OnStateChanged(headerView, state);
                                } else if (headerPaddingY >= 0 && state != XXPullToRefreshState.RELEASE_TO_REFRESH) {
                                    state = XXPullToRefreshState.RELEASE_TO_REFRESH;
                                    XXPullToRefresh.this.delegate.OnStateChanged(headerView, state);
                                }
                            }
                        }
                    } else {
                        isDragging = false;
                    }
                } else if (action == MotionEvent.ACTION_UP) {
                    if (state != XXPullToRefreshState.REFRESHING && isDragging) {
                        if (state == XXPullToRefreshState.PULL_TO_REFRESH) {
                            handler.sendMessage(Message.obtain());
                        } else if (state == XXPullToRefreshState.RELEASE_TO_REFRESH) {
                            state = XXPullToRefreshState.REFRESHING;
                            XXPullToRefresh.this.delegate.OnStateChanged(headerView, state);
                            XXPullToRefresh.this.delegate.OnRefresh();
                            handler.sendMessage(Message.obtain());
                        }
                    }
                    isDragging = false;
                }

                boolean flag = isDragging;
                if (scrollViewOnTouchListener != null) {
                    flag = flag || scrollViewOnTouchListener.onTouch(view, motionEvent);
                }
                return flag;
            }
        });
    }

    private boolean onTimerTick() {
        boolean flag = false;
        if (state == XXPullToRefreshState.PULL_TO_REFRESH) {
            int paddingTop = headerView.getPaddingTop();
            int offset = paddingTop + headerHeight;
            if (offset > 0) {
                if (offset > 16) {
                    headerView.setPadding(0, paddingTop - offset / 2, 0, 0);
                } else {
                    headerView.setPadding(0, -headerHeight, 0, 0);
                }
            } else {
                state = XXPullToRefreshState.DEfault;
                delegate.OnStateChanged(headerView, state);
                flag = true;
            }
        } else if (state == XXPullToRefreshState.DONE) {
            if (doneDelay >= DONEDELAY) {
                int paddingTop = headerView.getPaddingTop();
                int offset = paddingTop + headerHeight;
                if (offset > 0) {
                    if (offset > 16) {
                        headerView.setPadding(0, paddingTop - offset / 2, 0, 0);
                    } else {
                        headerView.setPadding(0, -headerHeight, 0, 0);
                    }
                } else {
                    state = XXPullToRefreshState.DEfault;
                    delegate.OnStateChanged(headerView, state);
                    flag = true;
                }
            } else {
                doneDelay += 5;
            }
        }
        return flag;
    }

    // calculate view height manually, because header height is not available in OnCreate
    private void measureView(View childView) {
        ViewGroup.LayoutParams p = childView.getLayoutParams();
        if (p == null) {
            p = new ViewGroup.LayoutParams(ViewGroup.LayoutParams.FILL_PARENT, ViewGroup.LayoutParams.WRAP_CONTENT);
        }
        int childWidthSpec = ViewGroup.getChildMeasureSpec(0, 0 + 0, p.width);
        int height = p.height;
        int childHeightSpec;
        if (height > 0) {
            childHeightSpec = MeasureSpec.makeMeasureSpec(height, MeasureSpec.EXACTLY);
        } else {
            childHeightSpec = MeasureSpec.makeMeasureSpec(0, MeasureSpec.UNSPECIFIED);
        }
        childView.measure(childWidthSpec, childHeightSpec);
    }

    public ViewGroup getHeaderView() {
        return headerView;
    }

    public ViewGroup getBodyView() {
        return bodyView;
    }

    public ScrollView getScrollView() {
        return scrollView;
    }

    public void setDoneDelay(int doneDelay) {
        this.DONEDELAY = doneDelay;
    }

    public void setScrollViewOnTouchListener(OnTouchListener scrollViewOnTouchListener) {
        this.scrollViewOnTouchListener = scrollViewOnTouchListener;
    }
}
