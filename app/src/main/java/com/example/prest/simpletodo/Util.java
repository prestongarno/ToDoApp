package com.example.prest.simpletodo;

import android.app.Activity;
import android.os.Handler;
import android.view.View;
import android.view.animation.AlphaAnimation;
import android.view.animation.AnimationUtils;
import android.view.inputmethod.InputMethodManager;
import android.widget.ArrayAdapter;
import android.widget.ListView;

/**
 * Created by prest on 8/1/2016.
 */
public class Util {

    private static Util ourInstance = new Util();

    //animations for fading in and out views
    private AlphaAnimation fadeOut, fadeIn;

    public static Util getInstance() {
        return ourInstance;
    }

    private Util() {
    }


    public AlphaAnimation getFadeOut() {
        return fadeOut;
    }

    public void setFadeOut(AlphaAnimation fadeOut) {
        this.fadeOut = fadeOut;
    }

    public AlphaAnimation getFadeIn() {
        return fadeIn;
    }

    public void setFadeIn(AlphaAnimation fadeIn) {
        this.fadeIn = fadeIn;
    }

    public void hideKeyboard(Activity activity) {
        InputMethodManager imm = (InputMethodManager) activity.getSystemService(Activity.INPUT_METHOD_SERVICE);
        //Find the currently focused view, so we can grab the correct window token from it.
        View view = activity.getCurrentFocus();
        //If no view currently has focus, create a new one, just so we can grab a window token from it
        if (view == null) {
            view = new View(activity);
        }
        imm.hideSoftInputFromWindow(view.getWindowToken(), 0);
    }

    public void fadeListViewChildViewOut(ListView view, final ArrayAdapter<?> adapter, int index, int duration) {
        //fade the view
        view.getChildAt(index).startAnimation(AnimationUtils.loadAnimation(view.getContext(), R.anim.fade_out));
        //now post handler to update the view (might need the adapter as a parameter to pull this off correctly)
        new Handler().postDelayed(new Runnable() {
            @Override
            public void run() {
                adapter.notifyDataSetChanged();
            }
        }, duration);
    }

    public void fadeListViewChildViewIn(ListView view, final ArrayAdapter<?> adapter, int index, int duration) {
        //fade the view
        view.getChildAt(index).startAnimation(AnimationUtils.loadAnimation(view.getContext(), R.anim.fade_in));
        //now post handler to update the view (might need the adapter as a parameter to pull this off correctly)
        new Handler().postDelayed(new Runnable() {
            @Override
            public void run() {
                adapter.notifyDataSetChanged();
            }
        }, duration);
    }

    public void fadeToHalfTransparent(View v) {
        v.startAnimation(AnimationUtils.loadAnimation(v.getContext(), R.anim.fade_to_half_transparent));
    }
    public void fadeFromHalfTransparent(View v) {
        v.startAnimation(AnimationUtils.loadAnimation(v.getContext(), R.anim.fade_from_half_transparent));
    }
}
