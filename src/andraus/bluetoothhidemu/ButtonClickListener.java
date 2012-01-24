package andraus.bluetoothhidemu;

import andraus.bluetoothhidemu.sock.SocketManager;
import andraus.bluetoothhidemu.sock.payload.HidPointerPayload;
import andraus.bluetoothhidemu.util.DoLog;
import android.content.Context;
import android.graphics.LightingColorFilter;
import android.os.Vibrator;
import android.view.View;
import android.view.View.OnClickListener;
import android.view.View.OnLongClickListener;
import android.view.animation.Animation;
import android.view.animation.AnimationSet;
import android.view.animation.DecelerateInterpolator;
import android.view.animation.ScaleAnimation;
import android.widget.ImageView;

/**
 * 
 */
public class ButtonClickListener implements OnClickListener, OnLongClickListener {
    
    private static final String TAG = BluetoothHidEmuActivity.TAG;
    
    private SocketManager mSocketManager = null;
    private int mButton = HidPointerPayload.MOUSE_BUTTON_NONE;
    
    private HidPointerPayload mHidPayload = null;
    
    private AnimationSet mClickAnimation = null;
    private Vibrator mVibrator;
    
    private boolean mIsButtonLocked = false;
    private boolean mIsLockable = false;

    /**
     * 
     * @param context
     * @param socketManager
     * @param button
     * @param isLockable
     */
    public ButtonClickListener(Context context, SocketManager socketManager, int button, boolean isLockable, HidPointerPayload hidPayload) {
        super();

        mSocketManager = socketManager;
        mButton = button;
        mIsLockable = isLockable;
        mHidPayload = hidPayload;
        
        mClickAnimation = new AnimationSet(true);
        mClickAnimation.addAnimation(new ScaleAnimation(1f, 0.9f, 1f, 0.9f, Animation.RELATIVE_TO_SELF, 0.5f, Animation.RELATIVE_TO_SELF, 0.5f));
        mClickAnimation.setInterpolator(new DecelerateInterpolator(10f));
        mClickAnimation.setDuration(Constants.CLICK_VIBRATE_MS);
        mClickAnimation.setRepeatCount(1);
        mClickAnimation.setRepeatMode(Animation.REVERSE);
        
        mVibrator = (Vibrator) context.getSystemService(Context.VIBRATOR_SERVICE);
        
    }
    

    /**
     * 
     */
    @Override
    public void onClick(View view) {
        
        drawButton(view,  false);
        mHidPayload.resetBytes();
        
        view.startAnimation(mClickAnimation);
        if (mVibrator != null) {
            mVibrator.vibrate(Constants.CLICK_VIBRATE_MS);
        }
        
        if (!mIsButtonLocked) {
            mHidPayload.setButton(mButton);
            mSocketManager.sendPayload(mHidPayload);
        } else {
            mIsButtonLocked = false;
        }
        mHidPayload.setButton(HidPointerPayload.MOUSE_BUTTON_NONE);
        mSocketManager.sendPayload(mHidPayload);

    }


    @Override
    public boolean onLongClick(View view) {
        
        if (mIsLockable) {
            if (mIsButtonLocked) {
                onClick(view);
            } else {
                drawButton(view, true);
                mHidPayload.resetBytes();
                
                mIsButtonLocked = true;
                DoLog.d(TAG, "set button locked to " + mIsButtonLocked);
                mSocketManager.sendPayload(mHidPayload);
            }
        } else {
            onClick(view);
        }
        
        return true;
    }

    /**
     * Set properties for the button to be drawn as normal or "on hold".
     * @param view
     * @param isHold
     */
    private void drawButton(View view, boolean isHold) {
        ImageView imgView = (ImageView) view;
        if (isHold) {
            imgView.setColorFilter(new LightingColorFilter(0xfcc0000, 0xff555555));
        } else {
            imgView.clearColorFilter();
        }
    }

}