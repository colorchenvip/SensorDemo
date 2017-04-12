package table.colorchen.com.sensordemo;

import android.os.Bundle;
import android.support.v7.app.AppCompatActivity;
import android.view.WindowManager;

/**
 * 陀螺仪3
 * Created by color on 2017/4/12 21:46.
 */
public class Gyroscope2Activity extends AppCompatActivity {
    ParallelViewHelper parallelViewHelper;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        getWindow().setFlags(WindowManager.LayoutParams.FLAG_FULLSCREEN,
                WindowManager.LayoutParams.FLAG_FULLSCREEN);
        setContentView(R.layout.gyroscope2_activity);

        parallelViewHelper = new ParallelViewHelper(this, findViewById(R.id.main_image_background));
    }

    @Override
    protected void onResume() {
        super.onResume();
        parallelViewHelper.start();
    }

    @Override
    protected void onPause() {
        super.onPause();
        parallelViewHelper.stop();
    }
}
