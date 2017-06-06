package org.openintents.timesheet.animation;

import android.content.Context;
import android.view.View;
import android.view.animation.Animation;
import android.view.animation.Animation.AnimationListener;
import android.view.animation.AnimationUtils;

import org.openintents.timesheet.R;

public class FadeAnimation {

    public static void fadeIn(Context context, View view) {
        Animation anim = AnimationUtils.loadAnimation(context, R.anim.fade_in);
        anim.setAnimationListener(new C00411());
        view.setVisibility(View.VISIBLE);
        view.startAnimation(anim);
    }

    public static void fadeOut(Context context, View view) {
        Animation anim = AnimationUtils.loadAnimation(context, R.anim.fade_out);
        anim.setAnimationListener(new C00422(view));
        view.startAnimation(anim);
    }

    /* renamed from: org.openintents.timesheet.animation.FadeAnimation.1 */
    static class C00411 implements AnimationListener {
        C00411() {
        }

        public void onAnimationEnd(Animation animation) {
        }

        public void onAnimationRepeat(Animation animation) {
        }

        public void onAnimationStart(Animation animation) {
        }
    }

    /* renamed from: org.openintents.timesheet.animation.FadeAnimation.2 */
    static class C00422 implements AnimationListener {
        private final /* synthetic */ View val$view;

        C00422(View view) {
            this.val$view = view;
        }

        public void onAnimationEnd(Animation animation) {
            this.val$view.setVisibility(8);
        }

        public void onAnimationRepeat(Animation animation) {
        }

        public void onAnimationStart(Animation animation) {
        }
    }
}
