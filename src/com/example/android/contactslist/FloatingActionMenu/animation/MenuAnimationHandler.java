package com.example.android.contactslist.FloatingActionMenu.animation;

/*
 *   The MIT License (MIT)

Copyright (c) 2014 Oğuz Bilgener

Permission is hereby granted, free of charge, to any person obtaining a copy
of this software and associated documentation files (the "Software"), to deal
in the Software without restriction, including without limitation the rights
to use, copy, modify, merge, publish, distribute, sublicense, and/or sell
copies of the Software, and to permit persons to whom the Software is
furnished to do so, subject to the following conditions:

The above copyright notice and this permission notice shall be included in
all copies or substantial portions of the Software.

THE SOFTWARE IS PROVIDED "AS IS", WITHOUT WARRANTY OF ANY KIND, EXPRESS OR
IMPLIED, INCLUDING BUT NOT LIMITED TO THE WARRANTIES OF MERCHANTABILITY,
FITNESS FOR A PARTICULAR PURPOSE AND NONINFRINGEMENT. IN NO EVENT SHALL THE
AUTHORS OR COPYRIGHT HOLDERS BE LIABLE FOR ANY CLAIM, DAMAGES OR OTHER
LIABILITY, WHETHER IN AN ACTION OF CONTRACT, TORT OR OTHERWISE, ARISING FROM,
OUT OF OR IN CONNECTION WITH THE SOFTWARE OR THE USE OR OTHER DEALINGS IN
THE SOFTWARE.

https://github.com/oguzbilgener/CircularFloatingActionMenu
 */

        import android.animation.Animator;
        import android.graphics.Point;
        import android.view.ViewGroup;
        import android.widget.FrameLayout;

        import com.example.android.contactslist.FloatingActionMenu.FloatingActionMenu;

/**
 * An abstract class that is a prototype for the actual animation handlers
 */
public abstract class MenuAnimationHandler {

    // There are only two distinct animations at the moment.
    protected enum ActionType {OPENING, CLOSING}

    protected FloatingActionMenu menu;

    public MenuAnimationHandler() {
    }

    public void setMenu(FloatingActionMenu menu) {
        this.menu = menu;
    }

    /**
     * Starts the opening animation
     * Should be overriden by children
     * @param center
     */
    public void animateMenuOpening(Point center) {
        if(menu == null) {
            throw new NullPointerException("MenuAnimationHandler cannot animate without a valid FloatingActionMenu.");
        }

    }

    /**
     * Ends the opening animation
     * Should be overriden by children
     * @param center
     */
    public void animateMenuClosing(Point center) {
        if(menu == null) {
            throw new NullPointerException("MenuAnimationHandler cannot animate without a valid FloatingActionMenu.");
        }
    }

    /**
     * Restores the specified sub action view to its final state, accoding to the current actionType
     * Should be called after an animation finishes.
     * @param subActionItem
     * @param actionType
     */
    protected void restoreSubActionViewAfterAnimation(FloatingActionMenu.Item subActionItem, ActionType actionType) {
        FrameLayout.LayoutParams params = (FrameLayout.LayoutParams) subActionItem.view.getLayoutParams();
        subActionItem.view.setTranslationX(0);
        subActionItem.view.setTranslationY(0);
        subActionItem.view.setRotation(0);
        subActionItem.view.setScaleX(1);
        subActionItem.view.setScaleY(1);
        subActionItem.view.setAlpha(1);
        if(actionType == ActionType.OPENING) {
            params.setMargins(subActionItem.x, subActionItem.y, 0, 0);
            subActionItem.view.setLayoutParams(params);
        }
        else if(actionType == ActionType.CLOSING) {
            Point center = menu.getActionViewCenter();
            params.setMargins(center.x - subActionItem.width / 2, center.y - subActionItem.height / 2, 0, 0);
            subActionItem.view.setLayoutParams(params);
            ((ViewGroup) menu.getActivityContentView()).removeView(subActionItem.view);
        }
    }

    /**
     * A special animation listener that is intended to listen the last of the sequential animations.
     * Changes the animating property of children.
     */
    public class LastAnimationListener implements Animator.AnimatorListener {

        @Override
        public void onAnimationStart(Animator animation) {
            setAnimating(true);
        }

        @Override
        public void onAnimationEnd(Animator animation) {
            setAnimating(false);
        }

        @Override
        public void onAnimationCancel(Animator animation) {
            setAnimating(false);
        }

        @Override
        public void onAnimationRepeat(Animator animation) {
            setAnimating(true);
        }
    }

    public abstract boolean isAnimating();
    protected abstract void setAnimating(boolean animating);
}