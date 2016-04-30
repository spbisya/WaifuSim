/**
 * You can modify and use this source freely
 * only for the development of application related Live2D.
 * <p/>
 * (c) Live2D Inc. All rights reserved.
 */
package com.okunev.waifusim;

import android.content.Context;
import android.content.res.AssetManager;
import android.opengl.GLSurfaceView;
import android.util.Log;
import android.view.MotionEvent;
import android.view.View;
import android.view.WindowManager;
import android.widget.Toast;

import java.io.IOException;
import java.io.InputStream;

import javax.microedition.khronos.egl.EGLConfig;
import javax.microedition.khronos.opengles.GL10;

import jp.live2d.android.Live2DModelAndroid;
import jp.live2d.android.UtOpenGL;
import jp.live2d.motion.Live2DMotion;
import jp.live2d.motion.MotionQueueManager;
import jp.live2d.util.UtSystem;

public class SampleGLSurfaceView extends GLSurfaceView {
    private SampleGLRenderer renderer;
    private float x = 0, y = 0;

    public SampleGLSurfaceView(final Context context, final float screen, final float height) {
        super(context);
        renderer = new SampleGLRenderer(context);
        setOnTouchListener(new OnTouchListener() {
            @Override
            public boolean onTouch(View v, MotionEvent me) {
                if (me.getAction() == MotionEvent.ACTION_DOWN) {
                    x = me.getX();
                    y = me.getY();
                    float newValueX = me.getRawX();
                    float newValueY = me.getRawY();
                    float screenSize = screen;

                    renderer.screen = screenSize;
                    renderer.sinus = 2 * (newValueX / screenSize) - 1;
                    renderer.cosinus = 1 - (newValueY / height);
                    //  Toast.makeText(context, "X = " + oldXvalue + " Y = " + oldYvalue +
                    //        " RawX = " + me.getRawX() + " RawY = " + me.getRawY(), Toast.LENGTH_LONG).show();


                } else if (me.getAction() == MotionEvent.ACTION_MOVE) {
                    float newValueX = me.getRawX();
                    float newValueY = me.getRawY();
                    float screenSize = screen;
                    renderer.screen = screenSize;
                    renderer.sinus = 2 * (newValueX / screenSize) - 1;
                    renderer.cosinus = 1 - (newValueY / height);

                    //  renderer.setSinus(oldXvalue);
                    //  renderer.setCosinus(oldYvalue);
                }
                return true;
            }
        });

        setRenderer(renderer);
    }

    class SampleGLRenderer implements Renderer {
        private Live2DModelAndroid live2DModel;
        Live2DMotion motion;
        MotionQueueManager motionMgr;
        Context context;

        private final String MODEL_PATH = "Mirai/model.moc";
        private final String TEXTURE_PATHS[] =
                {
                        "Mirai/model.1024/texture_00.png"
                };
        final String MOTION_PATH = "Mirai/motion/idle.mtn";

        public float sinus = 0;
        public float cosinus = 0;
        public float screen = 0;

        public SampleGLRenderer(Context context) {
            this.context = context;
            motionMgr = new MotionQueueManager();
        }

        @Override
        public void onDrawFrame(GL10 gl) {
            gl.glMatrixMode(GL10.GL_MODELVIEW);
            gl.glLoadIdentity();
            gl.glClear(GL10.GL_COLOR_BUFFER_BIT);

            double t = (UtSystem.getUserTimeMSec() / 1000.0) * 2 * Math.PI;
            double cycle = 3.0;
            double sin = Math.sin(t / cycle);
            double cos = Math.sin(cosinus);

            live2DModel.loadParam();

            if (motionMgr.isFinished()) {
                motionMgr.startMotion(motion, false);
            } else {
                motionMgr.updateParam(live2DModel);
            }

            live2DModel.saveParam();

            live2DModel.setParamFloat("PARAM_ANGLE_X", 30 * sinus);
            live2DModel.setParamFloat("PARAM_EYE_BALL_X", sinus);
            live2DModel.setParamFloat("PARAM_EYE_BALL_Y", cosinus);
            live2DModel.setParamFloat("PARAM_BODY_X", 10 * sinus);
            // Log.d("DRE", "Sin = " + sin + " 30*sin = " + 30 * (float) sin);
            //   Log.d("DRE", "X = " + sinus + " Y = " + cosinus+" Screen = "+screen);
            live2DModel.setParamFloat("PARAM_ANGLE_Y", 30 * cosinus);

            live2DModel.setGL(gl);

            live2DModel.update();
            live2DModel.draw();

        }


        @Override
        public void onSurfaceChanged(GL10 gl, int width, int height) {
            gl.glViewport(0, 0, width, height);
            gl.glMatrixMode(GL10.GL_PROJECTION);
            gl.glLoadIdentity();

            float modelWidth = live2DModel.getCanvasWidth();
            float aspect = (float) width / height;

            gl.glOrthof(0, modelWidth, modelWidth / aspect, 0, 0.5f, -0.5f);
        }


        @Override
        public void onSurfaceCreated(GL10 gl, EGLConfig config) {
            AssetManager mngr = context.getAssets();
            try {
                InputStream in = getContext().getAssets().open(MODEL_PATH);
                live2DModel = Live2DModelAndroid.loadModel(in);
                in.close();

                for (int i = 0; i < TEXTURE_PATHS.length; i++) {
                    InputStream tin = getContext().getAssets().open(TEXTURE_PATHS[i]);
                    int texNo = UtOpenGL.loadTexture(gl, tin, true);
                    live2DModel.setTexture(i, texNo);
                }
            } catch (IOException e) {
                e.printStackTrace();
            }

            try {
                InputStream in = mngr.open(MOTION_PATH);
                motion = Live2DMotion.loadMotion(in);
                in.close();
            } catch (Exception e) {
                e.printStackTrace();
            }
        }
    }
}
