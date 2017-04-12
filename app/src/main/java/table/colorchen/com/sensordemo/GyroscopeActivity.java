package table.colorchen.com.sensordemo;

import android.content.Context;
import android.hardware.Sensor;
import android.hardware.SensorEvent;
import android.hardware.SensorEventListener;
import android.hardware.SensorManager;
import android.os.Bundle;
import android.support.annotation.Nullable;
import android.support.v7.app.AppCompatActivity;
import android.util.Log;
import android.widget.Button;
import android.widget.TextView;

/**
 * 陀螺仪2
 * Created by color on 2017/4/12 21:46.
 */
public class GyroscopeActivity extends AppCompatActivity implements SensorEventListener {
    public static final String TAG ="GyroscopeActivity";
    private Button btn;
    private TextView tvContext;
    private TextView tvContext2;

    private Sensor mAccelerometer;
    private SensorManager mSensorManager;

    @Override
    protected void onCreate(@Nullable Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.gyroscope_activity);
        initView();
        mSensorManager = (SensorManager) getSystemService(Context.SENSOR_SERVICE);
        mAccelerometer = mSensorManager.getDefaultSensor(Sensor.TYPE_GYROSCOPE);

    }

    private void initView() {
        btn = (Button)findViewById(R.id.btn);
        tvContext = (TextView) findViewById(R.id.tvContext);
        tvContext2 = (TextView) findViewById(R.id.tvContext2);
    }



    @Override
    protected void onResume() {
        super.onResume();
        mSensorManager.registerListener(this,mAccelerometer,SensorManager.SENSOR_DELAY_NORMAL);
    }

    @Override
    protected void onStop() {
        mSensorManager.unregisterListener(this);
        super.onStop();
    }

    private float timestamp;

    float[] angle = new float[3];

    private static final float NS2S = 1.0f / 1000000000.0f;

    @Override
    public void onSensorChanged(SensorEvent event) {
        //从 x、y、z 轴的正向位置观看处于原始方位的设备，如果设备逆时针旋转，将会收到正值；否则，为负值
                tvContext.setText("sensorChanged " + event.sensor.getName() + ":\n"
                +"X :"+ event.values[0] + "\n"
                +"Y :"+ event.values[1] + "\n"
                +"Z :"+ event.values[2] + "\n"
                );
        Log.i(TAG, "X:" + event.values[0] + "----Y:" + event.values[1] + "----Z:" + event.values[2]);
        if (timestamp != 0) {

            // 得到两次检测到手机旋转的时间差（纳秒），并将其转化为秒
            final float dT = (event.timestamp - timestamp) * NS2S;
            // 将手机在各个轴上的旋转角度相加，即可得到当前位置相对于初始位置的旋转弧度
            angle[0] += event.values[0] * dT;
            angle[1] += event.values[1] * dT;
            angle[2] += event.values[2] * dT;

            Log.d(TAG, "X:" + angle[0] + "----Y:" + angle[1] + "----Z:" + angle[2]);
            tvContext2.setText("当前位置相对于初始位置的旋转弧度:\n"
                    +"X :"+ angle[0] + "\n"
                    +"Y :"+ angle[1] + "\n"
                    +"Z :"+ angle[2] + "\n");
            // 将弧度转化为角度
            float anglex = (float) Math.toDegrees(angle[0]);
            float angley = (float) Math.toDegrees(angle[1]);
            float anglez = (float) Math.toDegrees(angle[2]);
            btn.setText("弧度转化为角度:\n"
                    +"X :"+ anglex + "\n"
                    +"Y :"+ angley + "\n"
                    +"Z :"+ anglez + "\n");
            Log.e(TAG, "X:" + anglex + "----Y:" + angley + "----Z:" + anglez);
        }
        timestamp = event.timestamp;
    }

    @Override
    public void onAccuracyChanged(Sensor sensor, int i) {

    }
}
