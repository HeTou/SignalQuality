package com.zft.signal;

import android.content.BroadcastReceiver;
import android.content.Context;
import android.content.Intent;
import android.content.IntentFilter;
import android.graphics.Color;
import android.net.ConnectivityManager;
import android.net.NetworkInfo;
import android.net.wifi.WifiInfo;
import android.net.wifi.WifiManager;
import android.telephony.PhoneStateListener;
import android.telephony.SignalStrength;
import android.telephony.TelephonyManager;
import android.util.AttributeSet;
import android.util.Log;
import android.util.TypedValue;
import android.view.View;
import android.widget.ImageView;
import android.widget.TextView;

import androidx.annotation.Nullable;
import androidx.constraintlayout.widget.ConstraintLayout;

import java.lang.reflect.Method;

import static com.zft.signal.SignalView.NetWorkType.NET2G;
import static com.zft.signal.SignalView.NetWorkType.NET3G;
import static com.zft.signal.SignalView.NetWorkType.NET4G;
import static com.zft.signal.SignalView.NetWorkType.NET5G;
import static com.zft.signal.SignalView.NetWorkType.UNKONW;

/***
 * 信号控件
 */
public class SignalView extends ConstraintLayout {


    /***网络类型*/
    private final String WIFI = "WIFI";
    private final String MOBILE = "MOBILE";

    /***是否开启移动网络标识*/
    private boolean mobileEnableReflex;


    private final String TAG = "SignalView";
    private TelephonyManager telephonyManager;
    private WifiManager wifiManager;
    private ImageView ivSignal;
    private ImageView ivWifi;
    private TextView tvNetType;

    public SignalView(Context context) {
        super(context);
        init();
    }

    public SignalView(Context context, @Nullable AttributeSet attrs) {
        this(context, attrs, 0);
    }

    public SignalView(Context context, @Nullable AttributeSet attrs, int defStyleAttr) {
        super(context, attrs, defStyleAttr);
        init();
    }

    private void init() {

        //网络类型文本
        tvNetType = new TextView(getContext());
//        tvNetType.setText("wifi");
        tvNetType.setTextColor(Color.WHITE);
        tvNetType.setTextSize(9);
        LayoutParams netLp = new LayoutParams(LayoutParams.WRAP_CONTENT, LayoutParams.WRAP_CONTENT);
        netLp.rightToLeft = 10000;
        netLp.topToTop = LayoutParams.PARENT_ID;
        netLp.rightMargin = (int) TypedValue.applyDimension(TypedValue.COMPLEX_UNIT_PX, -3, getContext().getResources().getDisplayMetrics());
        //信号格
        ivSignal = new ImageView(getContext());
//        ivSignal.setImageResource(R.drawable.icon_signal_4);
        ivSignal.setId(10000);
        LayoutParams signLp = new LayoutParams(LayoutParams.WRAP_CONTENT, LayoutParams.WRAP_CONTENT);
        signLp.bottomToBottom = LayoutParams.PARENT_ID;
        signLp.leftToLeft = LayoutParams.PARENT_ID;
        signLp.rightToLeft = 10001;
        signLp.leftMargin = (int) TypedValue.applyDimension(TypedValue.COMPLEX_UNIT_PX, 50, getContext().getResources().getDisplayMetrics());
        signLp.rightMargin = (int) TypedValue.applyDimension(TypedValue.COMPLEX_UNIT_DIP, 10, getContext().getResources().getDisplayMetrics());
        //wifi格
        ivWifi = new ImageView(getContext());
        ivWifi.setId(10001);
//        ivWifi.setImageResource(R.drawable.icon_wifi_3);
        LayoutParams wifiLp = new LayoutParams(LayoutParams.WRAP_CONTENT, LayoutParams.WRAP_CONTENT);
        wifiLp.topToTop = LayoutParams.PARENT_ID;
        wifiLp.bottomToBottom = LayoutParams.PARENT_ID;
        wifiLp.rightToRight = LayoutParams.PARENT_ID;

        addView(ivWifi, wifiLp);
        addView(ivSignal, signLp);
        addView(tvNetType, netLp);


        telephonyManager = (TelephonyManager) getContext().getSystemService(Context.TELEPHONY_SERVICE);
        wifiManager = (WifiManager) getContext().getApplicationContext().getSystemService(Context.WIFI_SERVICE);


    }

    @Override
    protected void onAttachedToWindow() {
        super.onAttachedToWindow();
        telephonyManager.listen(phoneStateListener, PhoneStateListener.LISTEN_SIGNAL_STRENGTHS);
        regitsterWifiReceiver();
    }

    @Override
    protected void onDetachedFromWindow() {
        super.onDetachedFromWindow();
        telephonyManager.listen(phoneStateListener, PhoneStateListener.LISTEN_NONE);
        unRegitsterWifiReceiver();
    }

    PhoneStateListener phoneStateListener = new PhoneStateListener() {
        //这个是我们的主角，就是获取对应网络信号强度
        @Override
        public void onSignalStrengthsChanged(SignalStrength signalStrength) {
            int level = 0;
            if (android.os.Build.VERSION.SDK_INT >= android.os.Build.VERSION_CODES.M) {
                level = signalStrength.getLevel();
            } else {
                try {
                    Method getLteLevelMethod = signalStrength.getClass().getMethod("getLteLevel");
                    Object invoke = getLteLevelMethod.invoke(signalStrength);
                    if (invoke instanceof Integer) {
                        level = (int) invoke;
                    }
                } catch (Exception e) {
                    e.printStackTrace();
                }
            }
            ivSignal.setVisibility(View.VISIBLE);
            if (level >= 4) {
                ivSignal.setImageResource(R.drawable.icon_signal_4);
            } else if (level == 3) {
                ivSignal.setImageResource(R.drawable.icon_signal_3);
            } else if (level == 2) {
                ivSignal.setImageResource(R.drawable.icon_signal_2);
            } else if (level == 1) {
                ivSignal.setImageResource(R.drawable.icon_signal_1);
            } else {
                ivSignal.setImageResource(R.drawable.icon_signal_0);
            }

            if (isMobileEnableReflex(getContext())) {
                int networkType = telephonyManager.getNetworkType();
                tvNetType.setVisibility(View.VISIBLE);
                NetWorkType mobleType = getMobleType(networkType, null);
                tvNetType.setText(mobleType.getValue());
            }else {
                tvNetType.setVisibility(View.INVISIBLE);
            }
            Log.d(TAG, "onSignalStrengthsChanged() called with: " + "信号格数 = [" + level + "]");
//            /*** 当进入飞行模式时，不会有回调了。*/
//            Log.d("NetWorkUtil", "onSignalStrengthsChanged() called with: " + "signalStrength = [" + signalStrength.toString() + "]");
//            //这个ltedbm 是4G信号的值
//            String signalinfo = signalStrength.toString();
//            String[] parts = signalinfo.split(" ");
//            int ltedbm = Integer.parseInt(parts[9]);
//            //这个dbm 是2G和3G信号的值
//            int asu = signalStrength.getGsmSignalStrength();
//            int dbm = -113 + 2 * asu;
//
//            if (telephonyManager.getNetworkType() == TelephonyManager.NETWORK_TYPE_LTE) {
//                Log.i("NetWorkUtil", "网络：LTE 信号强度：" + ltedbm + "======");
//            } else if (telephonyManager.getNetworkType() == TelephonyManager.NETWORK_TYPE_HSDPA ||
//                    telephonyManager.getNetworkType() == TelephonyManager.NETWORK_TYPE_HSPA ||
//                    telephonyManager.getNetworkType() == TelephonyManager.NETWORK_TYPE_HSUPA ||
//                    telephonyManager.getNetworkType() == TelephonyManager.NETWORK_TYPE_UMTS) {
//                String bin;
//                if (dbm > -75) {
//                    bin = "网络很好";
//                } else if (dbm > -85) {
//                    bin = "网络不错";
//                } else if (dbm > -95) {
//                    bin = "网络还行";
//                } else if (dbm > -100) {
//                    bin = "网络很差";
//                } else {
//                    bin = "网络错误";
//                }
//                Log.i("NetWorkUtil", "网络：WCDMA 信号值：" + dbm + "========强度：" + bin + "======Detail:" + signalinfo);
//            } else {
//                String bin;
//                if (asu < 0 || asu >= 99) bin = "网络错误";
//                else if (asu >= 16) bin = "网络很好";
//                else if (asu >= 8) bin = "网络不错";
//                else if (asu >= 4) bin = "网络还行";
//                else bin = "网络很差";
//                Log.i("NetWorkUtil", "网络：GSM 信号值：" + dbm + "========强度：" + bin + "======Detail:" + signalinfo);
//            }
            super.onSignalStrengthsChanged(signalStrength);
        }
    };


    /***
     * wifi信号监听
     */
    BroadcastReceiver wifiReceiver = new BroadcastReceiver() {


        @Override
        public void onReceive(Context context, Intent intent) {
            Log.d(TAG, "onReceive() called with: " + "context = [" + context + "], intent = [" + intent + "]");
            String action = intent.getAction();
            if (WifiManager.WIFI_STATE_CHANGED_ACTION.equals(action)) {
                int wifistate = intent.getIntExtra(WifiManager.EXTRA_WIFI_STATE, WifiManager.WIFI_STATE_DISABLED);
                if (wifistate == WifiManager.WIFI_STATE_DISABLED) {
                    Log.i(TAG, "系统关闭wifi");
                } else if (wifistate == WifiManager.WIFI_STATE_ENABLED) {
                    Log.i(TAG, "系统开启wifi");
                }
            } else if (WifiManager.RSSI_CHANGED_ACTION.equals(action)) {
                /***wifi 信号质量监听*/
                WifiInfo info = wifiManager.getConnectionInfo();
                //信号格数 0~ -50 ；-50 ~ -70； <-70 ;
                Log.d(TAG, "onReceive() called with: " + "info = [" + info + "]");
                int rssi = info.getRssi();
                if (rssi > -55) {
                    //满格
                    ivWifi.setImageResource(R.drawable.icon_wifi_3);
                } else if (rssi > -70) {
                    //2格
                    ivWifi.setImageResource(R.drawable.icon_wifi_2);
                } else if (rssi > -90) {
                    //1格
                    ivWifi.setImageResource(R.drawable.icon_wifi_1);
                } else {
                    //0格
                    ivWifi.setImageResource(R.drawable.icon_wifi_0);
                }
            } else if ("android.net.conn.CONNECTIVITY_CHANGE".equals(action)) {
                NetWorkType netWorkType = getNetWorkType(getContext());
                mobileEnableReflex = isMobileEnableReflex(context);
                switch (netWorkType) {
                    case WIFI:
                        tvNetType.setVisibility(View.VISIBLE);
                        ivWifi.setVisibility(View.VISIBLE);
                        tvNetType.setVisibility(View.INVISIBLE);
                        break;
                    case NET2G:
                    case NET3G:
                    case NET4G:
                    case NET5G:
                        tvNetType.setText(netWorkType.getValue());
                        ivWifi.setVisibility(View.INVISIBLE);
                        tvNetType.setVisibility(View.VISIBLE);
                        ivSignal.setVisibility(View.VISIBLE);
                        break;
                    case UNKONW:
                        /***没有网络*/
                        tvNetType.setVisibility(View.INVISIBLE);
                        ivSignal.setVisibility(View.INVISIBLE);
                        ivWifi.setVisibility(View.INVISIBLE);
                        break;
                }
            } else {

            }
        }
    };

    private void regitsterWifiReceiver() {
        IntentFilter filter = new IntentFilter();
        filter.addAction(ConnectivityManager.CONNECTIVITY_ACTION);
        /***监听网络状态改变*/
        filter.addAction("android.net.conn.CONNECTIVITY_CHANGE");
        /****/
        filter.addAction(WifiManager.WIFI_STATE_CHANGED_ACTION);
        filter.addAction(WifiManager.RSSI_CHANGED_ACTION);
        getContext().registerReceiver(wifiReceiver, filter);
    }

    private void unRegitsterWifiReceiver() {
        getContext().unregisterReceiver(wifiReceiver);
    }


    /***
     * 获取网络类型 wifi ,2g,3g,4g,5g
     * @param context
     * @return
     */
    private NetWorkType getNetWorkType(Context context) {
        NetWorkType netWorkType = UNKONW;
        ConnectivityManager connectivityManager = (ConnectivityManager) context.getSystemService(Context.CONNECTIVITY_SERVICE);
        NetworkInfo networkInfo = connectivityManager.getActiveNetworkInfo();
        Log.d(TAG, "onReceive() called with: " + "networkInfo = [" + networkInfo + "]");
        if (networkInfo != null) {
            String typeName = networkInfo.getTypeName(); // WIFI | MOBILE
            if (WIFI.equals(typeName)) {
                //wifi
                netWorkType = NetWorkType.WIFI;
            } else if (MOBILE.equals(typeName)) {
                int subtype = networkInfo.getSubtype();
                String subtypeName = networkInfo.getSubtypeName();
                netWorkType = getMobleType(subtype, subtypeName);
            }
        } else {
            // ==null ,表示没有链接网络

        }

        return netWorkType;
    }

    /***
     * 判断移动网络的类型
     * @param subtype
     * @return
     */
    private NetWorkType getMobleType(int subtype, String subtypeName) {
        NetWorkType netWorkType = NetWorkType.UNKONW;
        //手机卡
        //手机卡网络信号类型

        switch (subtype) {

            case TelephonyManager.NETWORK_TYPE_GSM:
            case TelephonyManager.NETWORK_TYPE_GPRS: //(2G)~ 100 kbps
            case TelephonyManager.NETWORK_TYPE_EDGE: //(2G)~ 50-100 kbps
            case TelephonyManager.NETWORK_TYPE_CDMA: //(2G)~ 14-64 kbps
            case TelephonyManager.NETWORK_TYPE_1xRTT: //(2G)~ 50-100 kbps
            case TelephonyManager.NETWORK_TYPE_IDEN: //(2G)

                /***2G*/
                netWorkType = NET2G;
                break;
            case TelephonyManager.NETWORK_TYPE_TD_SCDMA:
            case TelephonyManager.NETWORK_TYPE_UMTS: //(3G)~ 400-7000 kbps
            case TelephonyManager.NETWORK_TYPE_EVDO_0: //(3G)~ 400-1000 kbps
            case TelephonyManager.NETWORK_TYPE_EVDO_A: //(3G)~ 600-1400 kbps
            case TelephonyManager.NETWORK_TYPE_HSDPA: //(3G)
            case TelephonyManager.NETWORK_TYPE_HSUPA://(3G)
            case TelephonyManager.NETWORK_TYPE_HSPA://(3G)
            case TelephonyManager.NETWORK_TYPE_EVDO_B://(3G)
            case TelephonyManager.NETWORK_TYPE_EHRPD: //(3G)
            case TelephonyManager.NETWORK_TYPE_HSPAP://(3G)

                /***3G*/
                netWorkType = NET3G;
                break;
            case TelephonyManager.NETWORK_TYPE_IWLAN:
            case TelephonyManager.NETWORK_TYPE_LTE: //(4G)

                /***4G*/
                netWorkType = NET4G;
                break;
//                            case TelephonyManager.NETWORK_TYPE_LTE_CA:
//                                break;
            case TelephonyManager.NETWORK_TYPE_NR:
                /***5G */
                netWorkType = NET5G;
                break;
            case TelephonyManager.NETWORK_TYPE_UNKNOWN:
                break;
            default:

                if (subtypeName.equalsIgnoreCase("TD-SCDMA")
                        || subtypeName.equalsIgnoreCase("WCDMA")
                        || subtypeName.equalsIgnoreCase("CDMA2000")) {
                    /***3G*/
                    netWorkType = NET3G;
                } else {
                    /***未知*/
                }
                break;
        }
        return netWorkType;
    }

    public enum NetWorkType {
        UNKONW(""),
        WIFI("wifi"),
        NET2G("2G"),
        NET3G("3G"),
        NET4G("4G"),
        NET5G("5G");

        String value;

        NetWorkType(String s) {
            value = s;
        }

        public String getValue() {
            return value;
        }

        public void setValue(String value) {
            this.value = value;
        }
    }

    /***
     * 判断移动数据是否开启
     * @param context
     * @return
     */
    public static boolean isMobileEnableReflex(Context context) {
        try {
            ConnectivityManager connectivityManager = (ConnectivityManager) context.getApplicationContext().getSystemService(Context.CONNECTIVITY_SERVICE);
            Method getMobileDataEnabledMethod = ConnectivityManager.class.getDeclaredMethod("getMobileDataEnabled");
            getMobileDataEnabledMethod.setAccessible(true);
            return (Boolean) getMobileDataEnabledMethod.invoke(connectivityManager);
        } catch (Exception e) {
            e.printStackTrace();
            return false;
        }
    }
}

