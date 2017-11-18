package master.flame.danmaku.ui.activity;

import android.app.Activity;
import android.os.Bundle;
import android.view.Menu;
import master.flame.danmaku.activity.R;
import master.flame.danmaku.ui.widget.DanmakuSurfaceView;

public class MainActivity extends Activity {

    private DanmakuSurfaceView mDanmakuView;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_main);
        findViews();
    }

    private void findViews() {
        mDanmakuView = (DanmakuSurfaceView) findViewById(R.id.sv_danmaku);
        if (mDanmakuView != null)
            mDanmakuView.enableMultiThread(false);
    }

    @Override
    protected void onDestroy() {
        if (mDanmakuView != null)
            mDanmakuView.release();
        super.onDestroy();
    }

    @Override
    public boolean onCreateOptionsMenu(Menu menu) {
        getMenuInflater().inflate(R.menu.main, menu);
        return true;
    }



}