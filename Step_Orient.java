package life;

import android.annotation.SuppressLint;
import android.app.Activity;
import android.content.Context;
import android.content.pm.PackageManager;
import android.os.Bundle;
import android.os.Handler;
import android.os.Message;
import android.support.v4.app.ActivityCompat;
import android.support.v7.app.AppCompatActivity;
import android.util.Log;
import android.view.View;
import android.widget.Button;
import android.widget.TextView;
import android.widget.Toast;
import android.widget.*;

import com.Indoor.HA.HAModel;
import com.Indoor.HA.Response.HEDestroyResponseEvent;
import com.Indoor.HA.Response.HEInitResponseEvent;
import com.Indoor.HA.Response.HEStartResponseEvent;
import com.Indoor.HA.Response.HEStopResponseEvent;
import com.Indoor.HA.Result.HEResultEvent;
import com.Indoor.ModeRecognition.Response.modeDestroyResponseEvent;
import com.Indoor.ModeRecognition.Response.modeInitResponseEvent;
import com.Indoor.ModeRecognition.Response.modeStartResponseEvent;
import com.Indoor.ModeRecognition.Response.modeStopResponseEvent;
import com.Indoor.ModeRecognition.Result.modeResultEvent;
import com.Indoor.ModeRecognition.modeModel;
import com.KF.KFModel;
import com.KF.Response.KFDestroyResponseEvent;
import com.KF.Response.KFInitResponseEvent;
import com.KF.Response.KFStartResponseEvent;
import com.KF.Response.KFStopResponseEvent;
import com.KF.Result.KFResultEvent;
import com.Sensors.Response.SensorsDestroyResponseEvent;
import com.Sensors.Response.SensorsInitResponseEvent;
import com.Sensors.Response.SensorsStartResponseEvent;
import com.Sensors.Response.SensorsStopResponseEvent;
import com.Sensors.Result.SensorsResultEvent;
import com.Sensors.SensorsModel;

import org.gispower.eventbus.Subscribe;
import org.gispower.eventbus.ThreadMode;

import java.text.DecimalFormat;

import cn.edu.whu.lmars.loccore.event.AbstractEvent.ModelContext;
import cn.edu.whu.lmars.loccore.event.AbstractEvent.Result.ModelResultEvent;
import cn.edu.whu.lmars.loccore.event.LocEventFactory;
import cn.edu.whu.lmars.loccore.model.ILocContext;
import life.orient.OrientSensor;
import life.step.PDRresult;
import life.step.PDRstream;
import life.step.Response.pdrDestroyResponseEvent;
import life.step.Response.pdrInitResponseEvent;
import life.step.Response.pdrResultEvent;
import life.step.Response.pdrStartResponseEvent;
import life.step.Response.pdrStopResponseEvent;
import life.step.StepLengthEstimation;
import life.step.StepSensorAcceleration;
import life.step.StepSensorBase;
import life.step.pdrModel;
import life.util.SensorUtil;
import cn.edu.whu.lmars.loccore.model.ILocModel;
import life.step.StepDetectionNormal;

//StepSensorBase.StepCallBack,
public class MainActivity extends AppCompatActivity implements  OrientSensor.OrientCallBack {
    private TextView mStepText;
    private TextView mOrientText;
    private TextView direction;
    private TextView steplength;
    private TextView tv_pdr;
    private Button startbutton;
    private StepView mStepView;
    private StepSensorBase mStepSensor; // 计步传感器
    private OrientSensor mOrientSensor; // 方向传感器
    private int mStepLen = 50; // 步长
    public int Degree;
    public double x;

    private ILocModel locSensor;
    private ILocModel locMode;
    private ILocModel locHA;
    private ILocModel locKF;
    private Context context;
    private ILocModel locPDR;

    // 动态获取存储权限，存储日志需要
    private static final int REQUEST_EXTERNAL_STORAGE = 1;
    private static String[] PERMISSIONS_STORAGE = {
            "android.permission.READ_EXTERNAL_STORAGE",
            "android.permission.WRITE_EXTERNAL_STORAGE"};


/*    @Override
    public void Step(int stepNum) {
        //  计步回调
        mStepText.setText("步数:" + stepNum);
        if (Degree >= 70 && Degree <= 110){
            x += 0.2;
            String.format("%.1f",x);

        }else if (Degree >= 250 && Degree < 290){
            x += -0.2;
           String.format("%.1f",x);
        }
        mStepView.autoAddPoint(mStepLen);
        System.out.println(x);

    }*/

    //这个orient就是传过来的degree
    @Override
    public void Orient(int orient) {
        // 方向回调
        mOrientText.setText("方向:" + orient);
        if((orient >= 340 && orient < 360) || (orient >= 0 && orient <20)){
            //orient = 358;
            direction.setText("北"+orient);
        }else if (orient >= 160 && orient <= 200){
            //orient = 180;
            direction.setText("南"+orient);
        }else if (orient >= 70 && orient <= 110){
            //orient = 90;
            direction.setText("东"+orient);
        }else if (orient >= 250 && orient < 290){
            //orient = 270;
            direction.setText("西"+orient);
        }
//        获取手机转动停止后的方向
//        orient = SensorUtil.getInstance().getRotateEndOrient(orient);
        mStepView.autoDrawArrow(orient);
        Degree = orient;
       // System.out.println(Degree);
    }

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        verifyStoragePermissions(this);
        SensorUtil.getInstance().printAllSensor(this); // 打印所有可用传感器
        setContentView(R.layout.activity_main);
        //mStepText = (TextView) findViewById(R.id.step_text);
        mOrientText = (TextView) findViewById(R.id.orient_text);
        mStepView = (StepView) findViewById(R.id.step_surfaceView);
        direction = (TextView) findViewById(R.id.direction);
        tv_pdr = (TextView) findViewById(R.id.tv_pdr);
        steplength = (TextView) findViewById(R.id.step_length);
        startbutton = (Button) findViewById(R.id.init);

        /*startbutton.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                                 // TODO Auto-generated method stub
                                 Log.i("匿名内部类", StartModel(view));

             }
            });*/
        // 注册计步监听
//        mStepSensor = new StepSensorPedometer(this, this);
//        if (!mStepSensor.registerStep()) {
        //mStepSensor = new StepSensorAcceleration(this, this);
        /*if (!mStepSensor.registerStep()) {
            Toast.makeText(this, "计步功能不可用！", Toast.LENGTH_SHORT).show();
        }*/
//        }
        // 注册方向监听
        mOrientSensor = new OrientSensor(this, this);
        if (!mOrientSensor.registerOrient()) {
            Toast.makeText(this, "方向功能不可用！", Toast.LENGTH_SHORT).show();
        }
        //on123();
        StartModel();
    }

    /**
     * 传感器数据采集模块
     */

    // 传感器模块初始化事件
    @Subscribe(threadMode = ThreadMode.BACKGROUND)
    public void SensorInitEvent(SensorsInitResponseEvent e) {
        // 事件响应
        switch (e.getState()){
            case OK:
                // 事件响应成功
                System.out.println(e.getMsg());    // 打印附带的信息
                break;
            case Fail:
                // 事件响应失败
                break;
        }
    }
    // 传感器模块开启事件
    @Subscribe(threadMode = ThreadMode.BACKGROUND)
    public void SensorStartEvent(SensorsStartResponseEvent e) {
        // 事件响应
        switch (e.getState()){
            case OK:
                // 事件响应成功
                System.out.println(e.getMsg());    // 打印附带的信息
                break;
            case Fail:
                // 事件响应失败
                break;
        }
    }
    // 传感器模块关闭事件
    @Subscribe(threadMode = ThreadMode.BACKGROUND)
    public void SensorStopEvent(SensorsStopResponseEvent e) {
        // 事件响应
        switch (e.getState()){
            case OK:
                // 事件响应成功
                System.out.println(e.getMsg());    // 打印附带的信息
                break;
            case Fail:
                // 事件响应失败
                break;
        }
    }
    // 传感器模块销毁事件
    @Subscribe(threadMode = ThreadMode.BACKGROUND)
    public void SensorDestroyEvent(SensorsDestroyResponseEvent e) {
        // 事件响应
        switch (e.getState()){
            case OK:
                // 事件响应成功
                System.out.println(e.getMsg());    // 打印附带的信息
                break;
            case Fail:
                // 事件响应失败
                break;
        }
    }
    // 传感器模块返回结果
    @Subscribe(threadMode = ThreadMode.BACKGROUND)
    public void SensorResultEvent(SensorsResultEvent e) {
        System.out.println(e.getRsltType());    // 打印结果类型
        System.out.println("SensorData:"+e.getRslt());  //打印模块输出结果
    }

    /**
     * ！？手机模式识别模块
     */

    // 模式识别模块初始化事件
    @Subscribe(threadMode = ThreadMode.BACKGROUND)
    public void ModeInitEvent(modeInitResponseEvent e) {
        // 事件响应
        switch (e.getState()){
            case OK:
                // 事件响应成功
                System.out.println(e.getMsg());    // 打印附带的信息
                break;
            case Fail:
                // 事件响应失败
                break;
        }
    }
    // 模式识别模块开启事件
    @Subscribe(threadMode = ThreadMode.BACKGROUND)
    public void ModeStartEvent(modeStartResponseEvent e) {
        // 事件响应
        switch (e.getState()){
            case OK:
                // 事件响应成功
                System.out.println(e.getMsg());    // 打印附带的信息
                break;
            case Fail:
                // 事件响应失败
                break;
        }
    }
    // 模式识别模块关闭事件
    @Subscribe(threadMode = ThreadMode.BACKGROUND)
    public void ModeStopEvent(modeStopResponseEvent e) {
        // 事件响应
        switch (e.getState()){
            case OK:
                // 事件响应成功
                System.out.println(e.getMsg());    // 打印附带的信息
                break;
            case Fail:
                // 事件响应失败
                break;
        }
    }
    // 模式识别模块销毁事件
    @Subscribe(threadMode = ThreadMode.BACKGROUND)
    public void ModeDestroyEvent(modeDestroyResponseEvent e) {
        // 事件响应
        switch (e.getState()){
            case OK:
                // 事件响应成功
                System.out.println(e.getMsg());    // 打印附带的信息
                break;
            case Fail:
                // 事件响应失败
                break;
        }
    }
    // 模式识别模块返回结果
    @Subscribe(threadMode = ThreadMode.BACKGROUND)
    public void ModeResultEvent(modeResultEvent e) {
        System.out.println(e.getRsltType());    // 打印结果类型
        System.out.println("phonePose:"+e.getResult());   //打印模块输出结果
    }

    /**
     * KF航向估计模块
     */

    // KF航向估计模块初始化事件
    @Subscribe(threadMode = ThreadMode.BACKGROUND)
    public void KFInitEvent(KFInitResponseEvent e) {
        // 事件响应
        switch (e.getState()){
            case OK:
                // 事件响应成功
                System.out.println(e.getMsg());    // 打印附带的信息
                break;
            case Fail:
                // 事件响应失败
                break;
        }
    }
    // KF航向估计模块开启事件
    @Subscribe(threadMode = ThreadMode.BACKGROUND)
    public void KFStartEvent(KFStartResponseEvent e) {
        // 事件响应
        switch (e.getState()){
            case OK:
                // 事件响应成功
                System.out.println(e.getMsg());    //打印附带的信息
                break;
            case Fail:
                // 事件响应失败
                break;
        }
    }
    // KF航向估计模块关闭事件
    @Subscribe(threadMode = ThreadMode.BACKGROUND)
    public void KFStopEvent(KFStopResponseEvent e) {
        // 事件响应
        switch (e.getState()){
            case OK:
                // 事件响应成功
                System.out.println(e.getMsg());    // 打印附带的信息
                break;
            case Fail:
                // 事件响应失败
                break;
        }
    }
    // KF航向估计模块销毁事件
    @Subscribe(threadMode = ThreadMode.BACKGROUND)
    public void KFDestroyEvent(KFDestroyResponseEvent e) {
        // 事件响应
        switch (e.getState()){
            case OK:
                // 事件响应成功
                System.out.println(e.getMsg());    // 打印附带的信息
                break;
            case Fail:
                // 事件响应失败
                break;
        }
    }
    // KF航向估计模块返回结果
    @Subscribe(threadMode = ThreadMode.BACKGROUND)
    public void KFResultEvent(KFResultEvent e) {
        System.out.println(e.getRsltType());    // 打印结果类型
        System.out.println("KF:"+e.getResult()[0]);        // 打印模块输出结果
    }

    /**
     * EKF航向估计模块
     */

    // EKF航向估计模块初始化事件
    @Subscribe(threadMode = ThreadMode.BACKGROUND)
    public void HAInitEvent(HEInitResponseEvent e) {
        // 事件响应
        switch (e.getState()){
            case OK:
                // 事件响应成功
                System.out.println(e.getMsg());    // 打印附带的信息
                break;
            case Fail:
                //事件响应失败
                break;
        }
    }
    // EKF航向估计模块开启事件
    @Subscribe(threadMode = ThreadMode.BACKGROUND)
    public void HAStartEvent(HEStartResponseEvent e) {
        // 事件响应
        switch (e.getState()){
            case OK:
                // 事件响应成功
                System.out.println(e.getMsg());    // 打印附带的信息
                break;
            case Fail:
                // 事件响应失败
                break;
        }
    }
    // EKF航向估计模块关闭事件
    @Subscribe(threadMode = ThreadMode.BACKGROUND)
    public void HAStopEvent(HEStopResponseEvent e) {
        // 事件响应
        switch (e.getState()){
            case OK:
                // 事件响应成功
                System.out.println(e.getMsg());    // 打印附带的信息
                break;
            case Fail:
                // 事件响应失败
                break;
        }
    }
    // EKF航向估计模块销毁事件
    @Subscribe(threadMode = ThreadMode.BACKGROUND)
    public void HADestroyEvent(HEDestroyResponseEvent e) {
        // 事件响应
        switch (e.getState()){
            case OK:
                // 事件响应成功
                System.out.println(e.getMsg());    //打印附带的信息
                break;
            case Fail:
                // 事件响应失败
                break;
        }
    }
    // EKF航向估计模块返回结果
    @Subscribe(threadMode = ThreadMode.BACKGROUND)
    public void HAResultEvent(HEResultEvent e) {
        System.out.println(e.getRsltType());    // 打印结果类型
        System.out.println("HA:"+e.getResult()[0]);        // 打印模块输出结果
    }

    /**
     * PDR定位模块
     */

    // PDR定位模块初始化事件
    @Subscribe(threadMode = ThreadMode.BACKGROUND)
    public void pdrInitEvent(pdrInitResponseEvent e) {
        // 事件响应
        switch (e.getState()){
            case OK:
                // 事件响应成功
                System.out.println(e.getMsg());    // 打印附带的信息
                break;
            case Fail:
                // 事件响应失败
                break;
        }
    }
    // PDR定位模块开启事件
    @Subscribe(threadMode = ThreadMode.BACKGROUND)
    public void pdrStartEvent(pdrStartResponseEvent e) {
        // 事件响应
        switch (e.getState()){
            case OK:
                // 事件响应成功
                System.out.println(e.getMsg());    // 打印附带的信息
                break;
            case Fail:
                // 事件响应失败
                break;
        }
    }
    // PDR定位模块关闭事件
    @Subscribe(threadMode = ThreadMode.BACKGROUND)
    public void pdrStopEvent(pdrStopResponseEvent e) {
        // 事件响应
        switch (e.getState()){
            case OK:
                // 事件响应成功
                System.out.println(e.getMsg());    // 打印附带的信息
                break;
            case Fail:
                // 事件响应失败
                break;
        }
    }
    // PDR定位模块销毁事件
    @Subscribe(threadMode = ThreadMode.BACKGROUND)
    public void pdrDestroyEvent(pdrDestroyResponseEvent e) {
        // 事件响应
        switch (e.getState()){
            case OK:
                // 事件响应成功
                System.out.println(e.getMsg());    // 打印附带的信息
                break;
            case Fail:
                // 事件响应失败
                break;
        }
    }
    // PDR定位模块返回结果
    @Subscribe(threadMode = ThreadMode.BACKGROUND)
    public void pdrResultEvent(pdrResultEvent e) {
        System.out.println(e.getRsltType());    // 打印结果类型
        System.out.println("StepLen: "+e.getResult()[2]);
        System.out.println("posX："+e.getResult()[3]);
        System.out.println("posY："+e.getResult()[4]); // 打印模块输出结果
    }

    // 请求写文件权限
    public static void verifyStoragePermissions(Activity activity) {
        try {
            // 检测是否有写的权限
            int permission = ActivityCompat.checkSelfPermission(activity,
                    "android.permission.WRITE_EXTERNAL_STORAGE");
            if (permission != PackageManager.PERMISSION_GRANTED) {
                // 没有写的权限，去申请写的权限，会弹出对话框
                ActivityCompat.requestPermissions(activity, PERMISSIONS_STORAGE, REQUEST_EXTERNAL_STORAGE);
            }
        } catch (Exception e) {
            e.printStackTrace();
        }
    }

    // 返回结果可视化
    @Subscribe(threadMode = ThreadMode.MAIN)
    public void Result(ModelResultEvent e) {
        switch (e.getRsltType()) {

            // EKF航向角解算
            case BLELoc:
                double[] HA = ((double[]) e.getResult()).clone();
                DecimalFormat df3 = new DecimalFormat("#0.000");

                String str1= df3.format(Double.parseDouble(String.valueOf(HA[0])));
                Message HA_msg = new Message();
                HA_msg.what = 1;
                HA_msg.obj = str1;
                this.mhandler.sendMessage(HA_msg);

                break;

            // KF航向角解算
            case PhonePose:
                double[] KF = ((double[]) e.getResult());
                DecimalFormat dff3 = new DecimalFormat("#0.000");

                String str2= dff3.format(Double.parseDouble(String.valueOf(KF[0])));

                Message KF_msg = new Message();
                KF_msg.what = 2;
                KF_msg.obj = str2;
                this.mhandler.sendMessage(KF_msg);

                break;

            case CameraLoc:

                String mode= null;
                if( e.getResult() != null) {
                    mode = (String) e.getResult();
                }

                Message mode_msg = new Message();
                mode_msg.what = 3;
                mode_msg.obj = mode;
                this.mhandler.sendMessage(mode_msg);

                break;


            case PDRHandLoc:
                double[] pdr = ((double[]) e.getResult()).clone();
                DecimalFormat df = new DecimalFormat("#0.00");
                Message str3_msg = new Message();
                Message str4_msg = new Message();
                Message str5_msg = new Message();
                Message str6_msg = new Message();

                String str3 = df.format(Double.parseDouble(String.valueOf(pdr[2]))); // 步长
                String str4 = df.format(Double.parseDouble(String.valueOf(pdr[3]))); // 坐标X
                String str5 = df.format(Double.parseDouble(String.valueOf(pdr[4]))); // 坐标Y
                String str6 = String.valueOf((int)pdr[7]); // 步数

                str3_msg.what = 4;
                str3_msg.obj = str3;
                this.mhandler.sendMessage(str3_msg);

                str4_msg.what = 5;
                str4_msg.obj = str4;
                this.mhandler.sendMessage(str4_msg);

                str5_msg.what = 6;
                str5_msg.obj = str5;
                this.mhandler.sendMessage(str5_msg);

                str6_msg.what = 7;
                str6_msg.obj = str6;
                this.mhandler.sendMessage(str6_msg);

//                Pos pos = new Pos();
//                pos.x = (float) Double.parseDouble(str4);
//                pos.y = (float) Double.parseDouble(str5);
//                vpos.add(pos);

                //repaint(vpos, canvas, paint, baseBitmap, iv_map);

                break;

        }
    }


    @SuppressLint("HandlerLeak")
    public Handler mhandler = new Handler() {
        public void handleMessage(Message msg) {
            switch (msg.what) {

                case 1:
                    System.out.println("EKF角度是多少？"+msg.obj);
                   // tv_ekf.setText("EKF角度[度]：" + msg.obj);
                    break;
                case 2:
                    System.out.println("KF角度是多少？"+msg.obj);
                    //tv_kf.setText("KF角度[度]：" + msg.obj);
                    break;
                case 3:
                    System.out.println("手机模式是什么？"+msg.obj);
                    //tv_mode.setText("手机模式：" + msg.obj);
                    break;
                case 4:
                    System.out.println("步长是多少？"+msg.obj);
                    //tv_pdr.setText("步长：" + msg.obj);
                    // tv_pdr.setText("步长[米]：" + "1243");
                    break;
                case 5:
                    //tv_x.setText("X坐标[米]：" + msg.obj);
                    break;
                case 6:
                   // tv_y.setText("Y坐标[米]：" + msg.obj);
                    break;
                case 7:
                    System.out.println("步数是多少？"+msg.obj);
                    //tv_step.setText("步数：" + msg.obj);
                    break;
            }

        }
    };


    public void InitModel() {
        Toast.makeText(MainActivity.this,"ok", Toast.LENGTH_LONG).show();
        // 初始化模块的Context
        ILocContext locContext = new ModelContext(this);

        locSensor = new SensorsModel();
        locSensor.modelLoad(locContext);   // 初始系统环境
        LocEventFactory.publishEvent(locSensor.getModelInitEvent());

        locHA = new HAModel();
        locHA.modelLoad(locContext);   // 初始系统环境
        LocEventFactory.publishEvent(locHA.getModelInitEvent());

        locMode = new modeModel(context);
        locMode.modelLoad(locContext);   // 初始系统环境
        LocEventFactory.publishEvent(locMode.getModelInitEvent());

        locKF = new KFModel();
        locKF.modelLoad(locContext);   // 初始系统环境
        LocEventFactory.publishEvent(locKF.getModelInitEvent());


        locPDR = new pdrModel();
        locPDR.modelLoad(locContext);   // 初始系统环境
        LocEventFactory.publishEvent(locPDR.getModelInitEvent());

//        tv_ekf.setText("EKF角度[度]："+ 0.0);
//        tv_kf.setText("KF角度[度]："+ 0.0);
//        tv_mode.setText("手机模式："+ null);
//        tv_pdr.setText("步长[米]："+ 0.0);
//        tv_x.setText("X坐标[米]："+ 0.0);
//        tv_y.setText("Y坐标[米]："+ 0.0);
//        tv_step.setText("步数：" + 0);
    }
    public void StartModel() {
        InitModel();
        LocEventFactory.publishEvent(locSensor.getModelStartEvent());
        LocEventFactory.publishEvent(locHA.getModelStartEvent());
        LocEventFactory.publishEvent(locMode.getModelStartEvent());
//        LocEventFactory.publishEvent(locKF.getModelStartEvent());
        LocEventFactory.publishEvent(locPDR.getModelStartEvent());
    }

    @Override
    protected void onDestroy() {
        super.onDestroy();
        // 注销传感器监听
        mStepSensor.unregisterStep();
        mOrientSensor.unregisterOrient();
    }


    /*protected double on123(){
        InitModel(view);
        PDRresult p = new PDRresult();
        PDRstream ps = new PDRstream();
        StepLengthEstimation s = new StepLengthEstimation(ps);
        double d = s.StepLength(p);
        steplength.setText(String.valueOf(d));
        System.out.println(d+"wendanqiqiqiqiqi");
        return d;
    }*/
}
