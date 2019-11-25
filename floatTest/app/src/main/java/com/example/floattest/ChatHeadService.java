package com.example.floattest;

import android.app.Notification;
import android.app.Service;
import android.content.BroadcastReceiver;
import android.content.Context;
import android.content.Intent;
import android.content.IntentFilter;
import android.content.pm.PackageInfo;
import android.content.pm.PackageManager;
import android.graphics.PixelFormat;
import android.graphics.Rect;
import android.graphics.drawable.Drawable;
import android.net.Uri;
import android.os.Build;
import android.os.IBinder;
import android.provider.Settings;
import android.util.DisplayMetrics;
import android.util.Log;
import android.view.Gravity;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.view.WindowManager;
import android.widget.ImageView;
import android.widget.LinearLayout;
import android.widget.Toast;

import androidx.core.app.NotificationCompat;
import androidx.core.app.NotificationManagerCompat;
import androidx.localbroadcastmanager.content.LocalBroadcastManager;

import com.example.library.FloatingViewListener;
import com.example.library.FloatingViewManager;

import java.util.ArrayList;
import java.util.List;
import java.util.Set;


/**
 * ChatHead Service
 */
public class ChatHeadService extends Service implements FloatingViewListener, View.OnClickListener {

    /**
     * Debugging Log Tags
     */
    private static final String TAG = "ChatHeadService";

    /**
     * Intent key (Cutout safe area)
     */
    public static final String EXTRA_CUTOUT_SAFE_AREA = "cutout_safe_area";

    /**
     * Notification ID
     */
    private static final int NOTIFICATION_ID = 9083150;

    /**
     * FloatingViewManager
     */
    private FloatingViewManager mFloatingViewManager;


    /**
     * {@inheritDoc}
     */
    private ImageView iconView;
    private ArrayList<ImageView> floatList;

    private static final String SETTINGS_PLAYER_JSON = "settings_item_json";
    private Context context ;

    static List<PackageInfo> packInfoList = null;

    @Override
    public void onCreate(){
        context = this;
        //브로드케스트로부터 메세지 받기
        packInfoList = MainActivity.packInfoList;
        LocalBroadcastManager.getInstance(this).registerReceiver(mMessageReceiver,
                new IntentFilter("message_to_Activity"));
        LocalBroadcastManager.getInstance(this).registerReceiver(popupRecevier,
                new IntentFilter("visibility"));
    }


    @Override
    public int onStartCommand(Intent intent, int flags, int startId) {

        MainActivity.serviceOn =1;
        // Do nothing if the Manager already exists
        if (mFloatingViewManager != null) {
            return START_STICKY;
        }
        Toast.makeText(this, "서비스 시작", Toast.LENGTH_SHORT).show();
        //화면의 해상도를 구한다.
        final DisplayMetrics metrics = new DisplayMetrics();
        //윈도우의 최상위 뷰를 가져온다.
        final LayoutInflater inflater = LayoutInflater.from(this);
        iconView = (ImageView) inflater.inflate(R.layout.widget_chathead, null, false);
        iconView.setOnClickListener(this);


        //FloatingViewManager를 이용해 iconView를 윈도우에 추가 및 삭제 액티비티 삽입.
        mFloatingViewManager = new FloatingViewManager(this, this);
        mFloatingViewManager.setFixedTrashIconImage(R.drawable.ic_trash_fixed);
        mFloatingViewManager.setActionTrashIconImage(R.drawable.ic_trash_action);
        mFloatingViewManager.setSafeInsetRect((Rect) intent.getParcelableExtra(EXTRA_CUTOUT_SAFE_AREA));
        final FloatingViewManager.Options options = new FloatingViewManager.Options();
        options.overMargin = (int) (16 * metrics.density);
        mFloatingViewManager.addViewToWindow(iconView, options);
        mFloatingViewManager.setDisplayMode(FloatingViewManager.DISPLAY_MODE_SHOW_ALWAYS);



        // resident start
        startForeground(NOTIFICATION_ID, createNotification(this));

        return START_REDELIVER_INTENT;
    }

    /**
     * {@inheritDoc}
     */
    @Override
    public void onDestroy() {
        Toast.makeText(this, "모든 서비스 종료", Toast.LENGTH_SHORT).show();
        MainActivity.serviceOn = 0;
        LocalBroadcastManager.getInstance(this).unregisterReceiver(mMessageReceiver);
        super.onDestroy();
    }

    /**
     * {@inheritDoc}
     */
    @Override
    public IBinder onBind(Intent intent) {
        return null;
    }

    /**
     * {@inheritDoc}
     */
    @Override
    public void onFinishFloatingView() {
        stopSelf();
        Log.d(TAG, getString(R.string.finish_deleted));
    }

    /**
     * {@inheritDoc}
     */
    @Override
    public void onTouchFinished(boolean isFinishing, int x, int y) {
        if (isFinishing) {
            Log.d(TAG, getString(R.string.deleted_soon));
        } else {
            Log.d(TAG, getString(R.string.touch_finished_position, x, y));
        }
    }


    /**
     * Displays notifications.
     * Describe click action.
     */

    @Override
    public void onClick(View v) {
        iconView.setVisibility(View.INVISIBLE);
        iconView.setClickable(false);

        childService();
    }




    public void childService(){

        //화면의 해상도를 구한다.
        final DisplayMetrics metrics = new DisplayMetrics();
        //윈도우의 최상위 뷰를 가져온다.
        final WindowManager windowManager = (WindowManager) getSystemService(Context.WINDOW_SERVICE);
        windowManager.getDefaultDisplay().getMetrics(metrics);

        final LayoutInflater inflater = LayoutInflater.from(this);

        //파라미터를 이용해 각 뷰의 위치를 정적으로 고정시켜 놓음.
        int type = 0;
        if (Build.VERSION.SDK_INT < Build.VERSION_CODES.O) {
            type = WindowManager.LayoutParams.TYPE_PHONE;
        } else {
            type = WindowManager.LayoutParams.TYPE_APPLICATION_OVERLAY;
        }

        WindowManager.LayoutParams params = new WindowManager.LayoutParams(
                WindowManager.LayoutParams.WRAP_CONTENT,
                WindowManager.LayoutParams.WRAP_CONTENT,
                type,
                WindowManager.LayoutParams.FLAG_NOT_FOCUSABLE |
                        WindowManager.LayoutParams.FLAG_NOT_TOUCH_MODAL |
                        WindowManager.LayoutParams.FLAG_HARDWARE_ACCELERATED,
                PixelFormat.TRANSLUCENT);
        floatList = new ArrayList();
        int y_loc = -400;
        for(int i=0;i<packInfoList.size();i++){
            String name = packInfoList.get(i).packageName.replaceAll("\\.", "");
            params.gravity = Gravity.RIGHT | Gravity.CENTER;
            params.y = y_loc;
            ImageView floatView = (ImageView)inflater.inflate(R.layout.widget_floating,null,false);
            floatList.add(floatView);
            floatView.setTag(name);
            floatView.setOnClickListener(new View.OnClickListener() {
                @Override
                public void onClick(View view) {
                    ArrayList<String> DBlist = Array2String.getStringArrayPref(context,SETTINGS_PLAYER_JSON);
                    String tag = view.getTag().toString();
                    Log.i("태그를확인",tag);
                    if(DBlist.contains(tag)){
                        Intent intent = new Intent(context,PopupWindow.class);
                        intent.addFlags(Intent.FLAG_ACTIVITY_NEW_TASK);
                        startActivity(intent);
                        invisibleView();
                    }else{
                        Toast.makeText(context, "no message", Toast.LENGTH_SHORT).show();
                    }
                }
            });
            Drawable icon = getPackageManager().getApplicationIcon(packInfoList.get(i).applicationInfo);

            floatView.setImageDrawable(icon);
            params.height = 150;
            params.width = 150;

            windowManager.addView(floatView,params);


            y_loc += 200;
        }
        ImageView iconHome = (ImageView)inflater.inflate(R.layout.widget_chatheadsub, null, false);
        floatList.add(iconHome);
        iconHome.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {
                iconView.setVisibility(View.VISIBLE);
                iconView.setClickable(true);
                for(int i=0;i<floatList.size();i++){
                    windowManager.removeViewImmediate(floatList.get(i));
                }
            }
        });
        params.y = y_loc;
        windowManager.addView(iconHome,params);

    }
    public void invisibleView(){
        for(int i=0;i<floatList.size();i++){
            if(floatList.get(i).getVisibility()==View.VISIBLE){
                floatList.get(i).setVisibility(View.INVISIBLE);
                floatList.get(i).setClickable(false);
            }else{
                floatList.get(i).setVisibility(View.VISIBLE);
                floatList.get(i).setClickable(true);
            }
        }
    }

    //브로드케스트 메세지 리시버
    private BroadcastReceiver mMessageReceiver = new BroadcastReceiver() {
        @Override
        public void onReceive(Context context, Intent intent) {

                String message = intent.getStringExtra("message");

                ArrayList<String> DBlist = Array2String.getStringArrayPref(context,SETTINGS_PLAYER_JSON);
                if(DBlist==null)
                    DBlist = new ArrayList();
                if(!DBlist.contains(message)){
                    DBlist.add(message);
                }
                else{
                    DBlist.remove(message);
                    DBlist.add(message);
                }
                Array2String.setStringArrayPref(context,SETTINGS_PLAYER_JSON,DBlist);
        }
    };
    private BroadcastReceiver popupRecevier = new BroadcastReceiver() {
        @Override
        public void onReceive(Context context, Intent intent) {
          invisibleView();
        }
    };


    private static Notification createNotification(Context context) {
        final NotificationCompat.Builder builder = new NotificationCompat.Builder(context, context.getString(R.string.default_floatingview_channel_id));
        builder.setWhen(System.currentTimeMillis());
        builder.setSmallIcon(R.mipmap.ic_launcher);
        builder.setContentTitle(context.getString(R.string.chathead_content_title));
        builder.setContentText(context.getString(R.string.content_text));
        builder.setOngoing(true);
        builder.setPriority(NotificationCompat.PRIORITY_MIN);
        builder.setCategory(NotificationCompat.CATEGORY_SERVICE);

        return builder.build();
    }


}
