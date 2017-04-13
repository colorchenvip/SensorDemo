package table.colorchen.com.sensordemo;

import android.content.Context;
import android.content.Intent;
import android.hardware.Sensor;
import android.hardware.SensorEvent;
import android.hardware.SensorEventListener;
import android.hardware.SensorManager;
import android.os.Bundle;
import android.os.Environment;
import android.support.design.widget.FloatingActionButton;
import android.support.design.widget.NavigationView;
import android.support.design.widget.Snackbar;
import android.support.v4.view.GravityCompat;
import android.support.v4.widget.DrawerLayout;
import android.support.v7.app.AppCompatActivity;
import android.support.v7.widget.Toolbar;
import android.text.TextUtils;
import android.util.Log;
import android.view.Menu;
import android.view.MenuItem;
import android.view.View;
import android.widget.Button;
import android.widget.EditText;
import android.widget.TextView;
import android.widget.Toast;

import java.io.File;
import java.io.FileOutputStream;
import java.text.SimpleDateFormat;
import java.util.Date;
import java.util.Timer;
import java.util.TimerTask;

import static android.support.v4.widget.DrawerLayout.LOCK_MODE_LOCKED_CLOSED;
import static table.colorchen.com.sensordemo.R.id.settingTime;

public class MainActivity extends AppCompatActivity
        implements NavigationView.OnNavigationItemSelectedListener {
    private final String TAG = "summer";
    private SensorManager mSensorManager;
    private Sensor mSensor;
    private float[] mValues = new float[3];


    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_main);
        initView();
        setListener();

        //初始化陀螺仪
        mSensorManager = (SensorManager) getSystemService(Context.SENSOR_SERVICE);
        mSensor = mSensorManager.getDefaultSensor(Sensor.TYPE_ACCELEROMETER);
        if (false) Log.d(TAG, "create " + mSensorManager);

    }

    private Button save;
    private Button savSettingTimee;
    private Button start;
    private Button end;
    private EditText editTextFile;
    private EditText editTextTime;
    private TextView contentData;
    private TextView remindLab;

    private String fileName = null;
    private int settingTimeNum = 1;//秒
    private boolean isSaveTime = false;
    private boolean isSaveFile = false;//
    private boolean isShowData = false;//是否显示采集的数据
    private String filePath;

    private void setListener() {
        editTextFile = (EditText) findViewById(R.id.editText);
        editTextTime = (EditText) findViewById(R.id.editTextTime);

        save = (Button) findViewById(R.id.save);
        savSettingTimee = (Button) findViewById(settingTime);
        start = (Button) findViewById(R.id.start);
        end = (Button) findViewById(R.id.end);
        save.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {
                if (editTextFile.getText() == null || TextUtils.isEmpty(editTextFile.getText().toString().trim())) {
                    Toast.makeText(getApplicationContext(), "请先输入文件名字", Toast.LENGTH_SHORT).show();
                    return;
                }
                fileName = editTextFile.getText().toString().trim();
                if (isSaveFile) {
                    isSaveFile = false;
                    save.setText("确定");
                    editTextFile.setEnabled(true);
                } else {
                    isSaveFile = true;
                    save.setText("修改");
                    editTextFile.setEnabled(false);
                }
                editTextFile.setText(fileName);
            }
        });

        savSettingTimee.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {
                if (editTextTime.getText() == null || TextUtils.isEmpty(editTextTime.getText().toString().trim())) {
                    Toast.makeText(getApplicationContext(), "请先设置时间", Toast.LENGTH_SHORT).show();
                    return;
                }
                settingTimeNum = Integer.parseInt(editTextTime.getText().toString().trim());
                if (isSaveTime) {
                    isSaveTime = false;
                    savSettingTimee.setText("确定");
                    editTextTime.setEnabled(true);
                } else {
                    isSaveTime = true;
                    savSettingTimee.setText("修改");
                    editTextTime.setEnabled(false);
                }
                editTextTime.setText(settingTimeNum + "");

            }
        });
        start.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {
                if (!isShowData) {
                    if (fileName == null || TextUtils.isEmpty(fileName)) {
                        Toast.makeText(getApplicationContext(), "请先填写文件名字", Toast.LENGTH_SHORT).show();
                        return;
                    }
                    isShowData = true;
                    startTimerTask();
                    end.setClickable(true);
                    start.setClickable(false);
                }
            }
        });
        end.setClickable(false);
        end.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {
                if (isShowData) {
                    isShowData = false;
                    cancelTimerTask();
                    end.setClickable(false);
                    start.setClickable(true);
                    if (!TextUtils.isEmpty(fileContent.toString())) {
                        writeDataToLocal(fileContent.toString(), fileName);
                    }
                }
            }
        });

    }

    private void initView() {
        Toolbar toolbar = (Toolbar) findViewById(R.id.toolbar);
        setSupportActionBar(toolbar);

        FloatingActionButton fab = (FloatingActionButton) findViewById(R.id.fab);
        fab.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {

                if (filePath != null || !TextUtils.isEmpty(filePath)) {
                    FileUtils.openFile(getApplicationContext(),new File(filePath));
                }else{
                    Snackbar.make(view, "没有地址信息，请按照步骤操作！", Snackbar.LENGTH_LONG)
                            .setAction("Action", null).show();
                }
            }
        });

        contentData = (TextView) findViewById(R.id.contentData);
        remindLab = (TextView) findViewById(R.id.remindLab);
        DrawerLayout drawer = (DrawerLayout) findViewById(R.id.drawer_layout);
        drawer.setDrawerLockMode(LOCK_MODE_LOCKED_CLOSED);
    }

    private Timer timer;
    private MyTimerTask myTimerTask;

    private void startTimerTask() {
        Log.d(TAG, "开始定时线程，每隔" + settingTimeNum + "秒一次....");
        if (myTimerTask == null && timer == null) {
            timer = new Timer();
            myTimerTask = new MyTimerTask();
            timer.schedule(myTimerTask, 0, settingTimeNum * 1000);
        }
    }

    private void cancelTimerTask() {
        Log.d(TAG, "停止定时线程....");
        if (timer != null && myTimerTask != null) {
            timer.cancel();
            timer.purge();
            timer = null;
            myTimerTask = null;
        }
    }

    public class MyTimerTask extends TimerTask {
        @Override
        public void run() {
            //要做的事情
            try {
                SimpleDateFormat formatter = new SimpleDateFormat("yyyy年MM月dd日  HH:mm:ss");
                Date curDate = new Date(System.currentTimeMillis());
                String str = formatter.format(curDate);
                Log.d(TAG, "开始写入，文件名字：" + fileName + "== X:" + mValues[0] + "Y:" + mValues[1] + "Z:" + mValues[2] + "\n");

                fileContent.append(str + "\n");
                fileContent.append("X:" + mValues[0] + " Y:" + mValues[1] + " Z:" + mValues[2] + "\n");
            } catch (Exception e) {
                e.printStackTrace();
                Log.d(TAG, "写入异常：" + e.toString());
            }
        }
    }

    private StringBuffer fileContent = new StringBuffer();

    private void writeDataToLocal(String content, String fileName) {

        try {
            if (Environment.getExternalStorageState().equals(Environment.MEDIA_MOUNTED)) {
                filePath = new File(Environment.getExternalStorageDirectory(), "Download").getAbsolutePath() + "/" + fileName + ".txt";
                Log.d(TAG, "文件路径：" + filePath);
                //这里就不要用openFileOutput了,那个是往手机内存中写数据的
                FileOutputStream output = null;
                output = new FileOutputStream(filePath);
                output.write(content.getBytes());
                //将String字符串以字节流的形式写入到输出流中
                output.close();
                remindLab.setText("温馨提示：文件地址（" + filePath + ")");
            } else Toast.makeText(getApplicationContext(), "SD卡不存在或者不可读写", Toast.LENGTH_SHORT).show();
            //关闭输出流
        } catch (Exception e) {
            e.printStackTrace();
            Log.d(TAG, "写入本地异常：" + e.toString());
        }

    }


    private final SensorEventListener mListener = new SensorEventListener() {

        private final float[] mScale = new float[]{2, 2.5f, 0.5f};   // accel
        private float[] mPrev = new float[3];
        private long mLastGestureTime;

        public void onSensorChanged(SensorEvent event) {
            boolean show = false;
            float[] diff = new float[3];

            for (int i = 0; i < 3; i++) {
                diff[i] = Math.round(mScale[i] * (event.values[i] - mPrev[i]) * 0.45f);
                if (Math.abs(diff[i]) > 0) {
                    show = true;
                }
                mPrev[i] = event.values[i];
            }

            if (show) {
                // only shows if we think the delta is big enough, in an attempt
                // to detect "serious" moves left/right or up/down
                /*Log.e(TAG, "sensorChanged " + event.sensor.getName() + " ("
                        + event.values[0] + ", " + event.values[1] + ", " +
                        event.values[2] + ")" + " diff(" + diff[0] +
                        " " + diff[1] + " " + diff[2] + ")");*/
               /* contentData.setText("陀螺仪： " + event.sensor.getName() + ":\n"
                        +"X :"+ event.values[0] + "\n"
                        +"Y :"+ event.values[1] + "\n"
                        +"Z :"+ event.values[2] + "\n");*/

            }
            mValues[0] = event.values[0];
            mValues[1] = event.values[1];
            mValues[2] = event.values[2];
            if (isShowData) {
                contentData.setText("陀螺仪： " + event.sensor.getName() + ":\n"
                        + "X :" + event.values[0] + "\n"
                        + "Y :" + event.values[1] + "\n"
                        + "Z :" + event.values[2] + "\n");
            }

            long now = android.os.SystemClock.uptimeMillis();
            if (now - mLastGestureTime > 1000) {
                mLastGestureTime = 0;

                float x = diff[0];
                float y = diff[1];
                boolean gestX = Math.abs(x) > 3;
                boolean gestY = Math.abs(y) > 3;

                if ((gestX || gestY) && !(gestX && gestY)) {
                    if (gestX) {
                        if (x < 0) {
                            Log.e("test", "<<<<<<<< LEFT <<<<<<<<<<<<");
                        } else {
                            Log.e("test", ">>>>>>>>> RITE >>>>>>>>>>>");
                        }
                    } else {
                        if (y < -2) {
                            Log.e("test", "<<<<<<<< UP <<<<<<<<<<<<");
                        } else {
                            Log.e("test", ">>>>>>>>> DOWN >>>>>>>>>>>");
                        }
                    }
                    mLastGestureTime = now;
                }
            }
        }

        public void onAccuracyChanged(Sensor sensor, int accuracy) {
        }
    };

    @Override
    protected void onResume() {
        super.onResume();
        mSensorManager.registerListener(mListener, mSensor, SensorManager.SENSOR_DELAY_FASTEST);
        if (false) Log.d(TAG, "resume " + mSensorManager);
    }

    @Override
    protected void onStop() {
        mSensorManager.unregisterListener(mListener);
        super.onStop();
        if (false) Log.d(TAG, "stop " + mSensorManager);
    }

    @Override
    public void onBackPressed() {
        DrawerLayout drawer = (DrawerLayout) findViewById(R.id.drawer_layout);
        if (drawer.isDrawerOpen(GravityCompat.START)) {
            drawer.closeDrawer(GravityCompat.START);
        } else {
            super.onBackPressed();
        }
    }

    @Override
    public boolean onCreateOptionsMenu(Menu menu) {
        // Inflate the menu; this adds items to the action bar if it is present.
        getMenuInflater().inflate(R.menu.main, menu);
        return true;
    }

    @Override
    public boolean onOptionsItemSelected(MenuItem item) {
        // Handle action bar item clicks here. The action bar will
        // automatically handle clicks on the Home/Up button, so long
        // as you specify a parent activity in AndroidManifest.xml.
        int id = item.getItemId();

        //noinspection SimplifiableIfStatement
        if (id == R.id.action_settings) {

            return true;
        }

        return super.onOptionsItemSelected(item);
    }

    @SuppressWarnings("StatementWithEmptyBody")
    @Override
    public boolean onNavigationItemSelected(MenuItem item) {
        // Handle navigation view item clicks here.
        int id = item.getItemId();

        if (id == R.id.nav_slideshow) {
            startActivity(new Intent(getApplicationContext(), Gyroscope2Activity.class));
        } else if (id == R.id.nav_manage) {
            startActivity(new Intent(getApplicationContext(), GyroscopeActivity.class));
        } else if (id == R.id.nav_share) {

        } else if (id == R.id.nav_send) {

        }

        DrawerLayout drawer = (DrawerLayout) findViewById(R.id.drawer_layout);
        drawer.closeDrawer(GravityCompat.START);
        return true;
    }

    @Override
    protected void onDestroy() {
        cancelTimerTask();
        super.onDestroy();
    }
}
