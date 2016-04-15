/**
 * You can modify and use this source freely
 * only for the development of application related Live2D.
 * <p/>
 * (c) Live2D Inc. All rights reserved.
 */
package com.okunev.waifusim;

import android.app.Activity;
import android.os.Bundle;
import android.view.MotionEvent;
import android.view.View;
import android.widget.Toast;

import jp.live2d.Live2D;


public class SampleActivity extends Activity {

    @Override
    public void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        Live2D.init();

        SampleGLSurfaceView view = new SampleGLSurfaceView(this, (float) (getWindowManager().getDefaultDisplay().getWidth()),
                (float) (getWindowManager().getDefaultDisplay().getHeight()));
        setContentView(view);
    }

}
