package com.okunev.waifusim;

import android.app.Activity;
import android.os.Bundle;
import android.support.v7.app.AppCompatActivity;
import android.util.Log;
import android.view.View;
import android.view.ViewGroup;
import android.view.Window;
import android.widget.FrameLayout;
import android.widget.ImageButton;
import android.widget.LinearLayout;
import android.widget.RelativeLayout;
import android.widget.Toast;

import com.okunev.waifusim.utils.FileManager;
import com.okunev.waifusim.utils.LAppDefine;
import com.okunev.waifusim.utils.LAppLive2DManager;
import com.okunev.waifusim.utils.LAppView;
import com.okunev.waifusim.utils.SoundManager;

import butterknife.Bind;
import butterknife.ButterKnife;
import jp.live2d.Live2D;

/**
 * Created by gwa on 5/3/16.
 */
public class ModelActivity extends AppCompatActivity {
    @Bind(R.id.layout)
    RelativeLayout relativeLayout;
    @Bind(R.id.imageButton8)
    ImageButton iBtn;
    private LAppLive2DManager live2DMgr;
    static private AppCompatActivity instance;

    public ModelActivity() {
        instance = this;
        if (LAppDefine.DEBUG_LOG) {
            Log.d("", "==============================================\n");
            Log.d("", "   Live2D Sample  \n");
            Log.d("", "==============================================\n");
        }

        SoundManager.init(this);
        live2DMgr = new LAppLive2DManager();
    }

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        getSupportActionBar().hide();
        //   requestWindowFeature(Window.FEATURE_NO_TITLE);
        setupGUI();
        FileManager.init(this.getApplicationContext());

    }

    void setupGUI() {
        setContentView(R.layout.activity_waifu);
        ButterKnife.bind(this);
        LAppView view = live2DMgr.createView(this);


        view.setLayoutParams(new RelativeLayout.LayoutParams(RelativeLayout.LayoutParams.MATCH_PARENT, RelativeLayout.LayoutParams.MATCH_PARENT));

        relativeLayout.addView(view);


        ClickListener listener = new ClickListener();
        iBtn.setOnClickListener(listener);
    }


    class ClickListener implements View.OnClickListener {

        @Override
        public void onClick(View v) {
            Toast.makeText(getApplicationContext(), "change model", Toast.LENGTH_SHORT).show();
            live2DMgr.changeModel();//Live2D Event
        }
    }


    @Override
    protected void onResume() {
        //live2DMgr.onResume() ;
        super.onResume();
    }


    @Override
    protected void onPause() {
        live2DMgr.onPause();
        super.onPause();
    }
}
