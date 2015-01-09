package com.okcommunity.contacts.cultivate;


import com.okcommunity.contacts.cultivate.util.ObservableScrollView;

public interface ScrollViewListener {
    void onScrollChanged(ObservableScrollView scrollView, int x, int y, int oldx, int oldy);
}