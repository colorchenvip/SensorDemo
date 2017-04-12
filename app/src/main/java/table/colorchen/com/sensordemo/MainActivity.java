package table.colorchen.com.sensordemo;

import android.content.Context;
import android.content.Intent;
import android.hardware.Sensor;
import android.hardware.SensorEvent;
import android.hardware.SensorEventListener;
import android.hardware.SensorManager;
import android.os.Bundle;
import android.os.Handler;
import android.support.design.widget.FloatingActionButton;
import android.support.design.widget.NavigationView;
import android.support.design.widget.Snackbar;
import android.support.v4.view.GravityCompat;
import android.support.v4.widget.DrawerLayout;
import android.support.v7.app.ActionBarDrawerToggle;
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

import static table.colorchen.com.sensordemo.R.id.settingTime;

public class MainActivity extends AppCompatActivity
        implements NavigationView.OnNavigationItemSelectedListener {
    private final String TAG = "MainActivity";
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

    private String fileName ;
    private int settingTimeNum = 1;//秒
    private boolean isSaveTime = false;
    private boolean isSaveFile = false;

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
                if (TextUtils.isEmpty(editTextFile.getText().toString().trim())) {
                    Toast.makeText(getApplicationContext(), "请先输入文件名字", Toast.LENGTH_SHORT).show();
                }
                fileName = editTextFile.getText().toString().trim();
                if (isSaveFile) {
                    isSaveFile = false;
                    save.setText("确定");
                } else {
                    isSaveFile = true;
                    save.setText("修改");
                }
                editTextFile.setText(fileName);
            }
        });

        savSettingTimee.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {
                if (TextUtils.isEmpty(editTextTime.getText().toString().trim())) {
                    Toast.makeText(getApplicationContext(), "请先设置时间", Toast.LENGTH_SHORT).show();
                }
                settingTimeNum = Integer.parseInt(editTextTime.getText().toString().trim());
                if (isSaveTime) {
                    isSaveTime = false;
                    savSettingTimee.setText("确定");
                } else {
                    isSaveTime = true;
                    savSettingTimee.setText("修改");
                }
                editTextTime.setText(settingTimeNum + "");

            }
        });

        start.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {
                if (fileName == null) {
                    Toast.makeText(getApplicationContext(), "请先填写文件名字", Toast.LENGTH_SHORT).show();
                }
                startTimerTask();
            }
        });
        end.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {
                cancelTimerTask();
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
                Snackbar.make(view, "Replace with your own action", Snackbar.LENGTH_LONG)
                        .setAction("Action", null).show();
            }
        });

        contentData = (TextView) findViewById(R.id.contentData);
        DrawerLayout drawer = (DrawerLayout) findViewById(R.id.drawer_layout);
        ActionBarDrawerToggle toggle = new ActionBarDrawerToggle(
                this, drawer, toolbar, R.string.navigation_drawer_open, R.string.navigation_drawer_close);
        drawer.setDrawerListener(toggle);
        toggle.syncState();

        NavigationView navigationView = (NavigationView) findViewById(R.id.nav_view);
        navigationView.setNavigationItemSelectedListener(this);
    }
    Handler handler=new Handler();
    Runnable runnable=new Runnable() {
        @Override
        public void run() {
            //要做的事情
            try {
                FileUtils.savaFileToSD(fileName, "X:" + mValues[0] + "Y:" + mValues[1] + "Z:" + mValues[2] + "\n", getApplicationContext());
            } catch (Exception e) {
                e.printStackTrace();
            }
            handler.postDelayed(this, settingTime * 1000);
        }
    };

    private void startTimerTask() {

        handler.postDelayed(runnable, settingTime * 1000);//每两秒执行一次runnable.
    }

    private void cancelTimerTask() {
        handler.removeCallbacks(runnable);
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
                Log.e(TAG, "sensorChanged " + event.sensor.getName() + " ("
                        + event.values[0] + ", " + event.values[1] + ", " +
                        event.values[2] + ")" + " diff(" + diff[0] +
                        " " + diff[1] + " " + diff[2] + ")");
               /* contentData.setText("陀螺仪： " + event.sensor.getName() + ":\n"
                        +"X :"+ event.values[0] + "\n"
                        +"Y :"+ event.values[1] + "\n"
                        +"Z :"+ event.values[2] + "\n");*/

            }
            mValues[0] = event.values[0];
            mValues[1] = event.values[1];
            mValues[2] = event.values[2];
            contentData.setText("陀螺仪： " + event.sensor.getName() + ":\n"
                    + "X :" + event.values[0] + "\n"
                    + "Y :" + event.values[1] + "\n"
                    + "Z :" + event.values[2] + "\n");

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
