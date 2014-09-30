package com.example.android.contactslist;


import com.example.android.contactslist.util.ObservableScrollView;

public interface ScrollViewListener {
    void onScrollChanged(ObservableScrollView scrollView, int x, int y, int oldx, int oldy);

}