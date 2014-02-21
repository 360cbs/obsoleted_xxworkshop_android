/*
 * Copyright (c) 2014 xxworkshop. All rights reserved.
 * Created by Broche Xu on 2/19/14 8:33 PM.
 */

package com.xxworkshop.ui;

import android.view.ViewGroup;

import java.util.Observable;

/**
 * Created by brochexu on 2/19/14.
 */
public abstract class XXPullToRefreshDelegate extends Observable {
    public abstract void OnStateChanged(ViewGroup headerView, XXPullToRefreshState state);
    public abstract void OnRefresh();
}
