package com.eanyatonic.cctvViewer;

import static androidx.constraintlayout.motion.utils.Oscillator.TAG;

import com.eanyatonic.cctvViewer.FileUtils;

import android.Manifest;
import androidx.core.content.ContextCompat;
import androidx.core.app.ActivityCompat;
import android.content.pm.PackageManager;
import android.content.Context;
import android.content.Intent;
import android.content.SharedPreferences;
import android.content.pm.PackageInfo;
import android.graphics.Bitmap;
import android.media.AudioManager;
import android.os.Build;
import android.os.Bundle;
import android.os.Handler;
import android.os.Looper;
import android.os.SystemClock;
import android.util.DisplayMetrics;
import android.util.Log;
import android.view.Gravity;
import android.view.KeyEvent;
import android.view.MotionEvent;
import android.view.View;
import android.view.ViewGroup;
import android.widget.Button;
import android.widget.FrameLayout;
import android.widget.LinearLayout;
import android.widget.TextView;
import android.widget.Toast;

import androidx.appcompat.app.AppCompatActivity;
import androidx.preference.PreferenceManager;

// X5内核代码
import com.tencent.smtt.export.external.TbsCoreSettings;
import com.tencent.smtt.export.external.extension.interfaces.IX5WebSettingsExtension;
import com.tencent.smtt.export.external.interfaces.ConsoleMessage;
import com.tencent.smtt.export.external.interfaces.IX5WebChromeClient;
import com.tencent.smtt.sdk.QbSdk;
import com.tencent.smtt.sdk.WebChromeClient;
import com.tencent.smtt.sdk.WebSettings;
import com.tencent.smtt.sdk.WebView;
import com.tencent.smtt.sdk.WebViewClient;

import java.util.ArrayDeque;
import java.util.HashMap;
import java.util.Map;
import java.util.Objects;
import java.util.WeakHashMap;

// WebView内核代码
//import android.webkit.SslErrorHandler;
//import android.webkit.WebChromeClient;
//import android.webkit.WebSettings;
//import android.webkit.WebView;
//import android.webkit.WebViewClient;

public class MainActivity extends AppCompatActivity {

    private AudioManager audioManager;

    private WebView webView0; // 导入 WebView
    private WebView webView1; // 导入备用 WebView
    private boolean canLoadX5 = false;

    private int currentWebView = 0; // 正在使用的webView
    private boolean isChanging = false; // 是否正在换台

    private final String[] liveUrls = { "https://tv.cctv.com/live/cctv1/", "https://tv.cctv.com/live/cctv2/",
            "https://tv.cctv.com/live/cctv3/", "https://tv.cctv.com/live/cctv4/", "https://tv.cctv.com/live/cctv5/",
            "https://tv.cctv.com/live/cctv6/", "https://tv.cctv.com/live/cctv7/", "https://tv.cctv.com/live/cctv8/",
            "https://tv.cctv.com/live/cctvjilu", "https://tv.cctv.com/live/cctv10/", "https://tv.cctv.com/live/cctv11/",
            "https://tv.cctv.com/live/cctv12/", "https://tv.cctv.com/live/cctv13/",
            "https://tv.cctv.com/live/cctvchild", "https://tv.cctv.com/live/cctv15/",
            "https://tv.cctv.com/live/cctv16/", "https://tv.cctv.com/live/cctv17/",
            "https://tv.cctv.com/live/cctv5plus/", "https://tv.cctv.com/live/cctveurope",
            "https://tv.cctv.com/live/cctvamerica/", "https://www.yangshipin.cn/tv/home?pid=600002309",
            "https://www.yangshipin.cn/tv/home?pid=600002521", "https://www.yangshipin.cn/tv/home?pid=600002483",
            "https://www.yangshipin.cn/tv/home?pid=600002520", "https://www.yangshipin.cn/tv/home?pid=600002475",
            "https://www.yangshipin.cn/tv/home?pid=600002508", "https://www.yangshipin.cn/tv/home?pid=600002485",
            "https://www.yangshipin.cn/tv/home?pid=600002509", "https://www.yangshipin.cn/tv/home?pid=600002498",
            "https://www.yangshipin.cn/tv/home?pid=600002506", "https://www.yangshipin.cn/tv/home?pid=600002531",
            "https://www.yangshipin.cn/tv/home?pid=600002481", "https://www.yangshipin.cn/tv/home?pid=600002516",
            "https://www.yangshipin.cn/tv/home?pid=600002525", "https://www.yangshipin.cn/tv/home?pid=600002484",
            "https://www.yangshipin.cn/tv/home?pid=600002490", "https://www.yangshipin.cn/tv/home?pid=600002503",
            "https://www.yangshipin.cn/tv/home?pid=600002505", "https://www.yangshipin.cn/tv/home?pid=600002532",
            "https://www.yangshipin.cn/tv/home?pid=600002493", "https://www.yangshipin.cn/tv/home?pid=600002513", };

    private final String[] channelNames = { "1 CCTV-1 综合", "2 CCTV-2 财经", "3 CCTV-3 综艺", "4 CCTV-4 中文国际（亚）",
            "5 CCTV-5 体育", "6 CCTV-6 电影", "7 CCTV-7 国防军事", "8 CCTV-8 电视剧", "9 CCTV-9 纪录", "10 CCTV-10 科教",
            "11 CCTV-11 戏曲", "12 CCTV-12 社会与法", "13 CCTV-13 新闻", "14 CCTV-14 少儿", "15 CCTV-15 音乐", "16 CCTV-16 奥林匹克",
            "17 CCTV-17 农业农村", "18 CCTV-5+ 体育赛事", "19 CCTV-4 中文国际（欧）", "20 CCTV-4 中文国际（美）", "21 北京卫视", "22 江苏卫视",
            "23 东方卫视", "24 浙江卫视", "25 湖南卫视", "26 湖北卫视", "27 广东卫视", "28 广西卫视", "29 黑龙江卫视", "30 海南卫视", "31 重庆卫视",
            "32 深圳卫视", "33 四川卫视", "34 河南卫视", "35 福建东南卫视", "36 贵州卫视", "37 江西卫视", "38 辽宁卫视", "39 安徽卫视", "40 河北卫视",
            "41 山东卫视", };

    private int currentLiveIndex;

    private static final String PREF_NAME = "MyPreferences";
    private static final String PREF_KEY_LIVE_INDEX = "currentLiveIndex";

    private boolean doubleBackToExitPressedOnce = false;

    private StringBuilder digitBuffer = new StringBuilder(); // 用于缓存按下的数字键
    private static final long DIGIT_TIMEOUT = 3000; // 超时时间（毫秒）

    private TextView inputTextView; // 用于显示正在输入的数字的 TextView

    // 初始化透明的View
    private View loadingOverlay;

    // 频道显示view
    private TextView overlayTextView;

    private String info = "";

    // 在 MainActivity 中添加一个 Handler
    private final Handler handler = new Handler();

    private boolean isMenuOverlayVisible = false;
    private boolean isDrawerOverlayVisible = false;

    private LinearLayout menuOverlay;
    private LinearLayout DrawerLayout;
    private LinearLayout DrawerLayoutDetailed;
    private LinearLayout SubMenuCCTV;
    private LinearLayout SubMenuLocal;
    private TextView CoreText;

    private int menuOverlaySelectedIndex = 0;
    private int DrawerLayoutSelectedIndex = 0;
    private int SubMenuCCTVSelectedIndex = 0;
    private int SubMenuLocalSelectedIndex = 0;

    // 可自定义设置项
    private int TEXT_SIZE = 22;
    private Boolean enableDualWebView = true;
    private Boolean enableDirectChannelChange = false;
    private Boolean enableDirectBack = false;

    // 调试开关（设置-其他设置，重启生效）
    private boolean disableFastLoading = false;
    private boolean disableAutoFullscreen = false;
    private boolean disableLocalReload = false;
    private boolean diagOverlayEnabled = false;

    // 加载代次：把延迟回调与页面回调绑定到"发起时"的那次加载，避免快速换台互相干扰
    private int loadGeneration = 0;
    private final Map<WebView, Integer> viewLoadGeneration = new WeakHashMap<>();

    // 页面触发 HTML5 全屏时的自定义 View（WebChromeClient.onShowCustomView）
    private View customFullscreenView;
    private IX5WebChromeClient.CustomViewCallback customFullscreenCallback;

    // 诊断悬浮层
    private TextView diagTextView;
    private final ArrayDeque<String> eventLog = new ArrayDeque<>();
    private String webViewVersionText = "";

    // 页面加载期间的"轻量化清理"脚本：
    // 只在播放器就绪前运行，且最多运行 10 秒；不再清空 <script src>（对已开始加载的脚本无效且可能打断页面初始化）
    private static final String FAST_LOADING_JS = """
            (function () {
                if (window.__cctvFastLoading) { return; }
                window.__cctvFastLoading = { start: Date.now(), done: false };
                var MAX_MS = 10000;
                function playerReady() {
                    try {
                        return !!(document.querySelector('video')
                                || document.querySelector('[id^="player_pagefullscreen_"]')
                                || document.querySelector('[isPageFullscreen]')
                                || document.querySelector('.videoFull'));
                    } catch (e) {
                        return true;
                    }
                }
                function clean() {
                    try {
                        var imgs = document.getElementsByTagName('img');
                        for (var i = 0; i < imgs.length; i++) {
                            if (imgs[i] && imgs[i].getAttribute('src')) { imgs[i].src = ''; }
                        }
                        var classNames = ['newmap', 'newtopbz', 'newtopbzTV', 'column_wrapper'];
                        for (var c = 0; c < classNames.length; c++) {
                            var els = document.getElementsByClassName(classNames[c]);
                            for (var k = els.length - 1; k >= 0; k--) {
                                if (els[k] && els[k].innerHTML) { els[k].innerHTML = ''; }
                            }
                        }
                    } catch (e) {
                    }
                }
                function tick() {
                    var state = window.__cctvFastLoading;
                    if (state.done) { return; }
                    if (playerReady() || Date.now() - state.start > MAX_MS) {
                        state.done = true;
                        return;
                    }
                    clean();
                    setTimeout(tick, 100);
                }
                tick();
            })();
            """;

    // 自动全屏：等待播放器按钮出现后最多尝试 3 次点击，30 秒后放弃；
    // 兼容央视（player_pagefullscreen_* + isPageFullscreen 属性）与央视频（.videoFull/.videoFull_ac）
    private static final String AUTO_FULLSCREEN_JS = """
            (function () {
                if (window.__cctvAutoFullscreen) { return; }
                var st = window.__cctvAutoFullscreen = { start: Date.now(), done: false, clicks: 0 };
                var MAX_MS = 30000;
                function cctvBtn() {
                    try { return document.querySelector('[id^="player_pagefullscreen_"]'); } catch (e) { return null; }
                }
                function cctvOn(el) {
                    try { return el.getAttribute('isPageFullscreen') === 'yes'; } catch (e) { return false; }
                }
                function yspEnter() {
                    try { return document.querySelector('.videoFull:not([id])'); } catch (e) { return null; }
                }
                function yspOn() {
                    try { return !!(document.querySelector('.videoFull[id]') || document.querySelector('.videoFull_ac')); } catch (e) { return false; }
                }
                function setVolume() {
                    try { var v = document.querySelector('video'); if (v) { v.volume = 1; } } catch (e) { }
                }
                function tick() {
                    if (st.done) { return; }
                    if (Date.now() - st.start > MAX_MS) { st.done = true; return; }
                    var b = cctvBtn();
                    if (b) {
                        if (cctvOn(b)) { st.done = true; setVolume(); return; }
                        if (st.clicks < 3) { st.clicks++; try { b.click(); } catch (e) { } setVolume(); setTimeout(tick, 900); return; }
                        st.done = true; return;
                    }
                    if (yspOn()) { st.done = true; setVolume(); return; }
                    var yb = yspEnter();
                    if (yb) {
                        if (st.clicks < 3) { st.clicks++; try { yb.click(); } catch (e) { } setVolume(); setTimeout(tick, 900); return; }
                        st.done = true; return;
                    }
                    setTimeout(tick, 100);
                }
                tick();
            })();
            """;

    // 菜单"切换全屏"：按页面实际状态进入/退出，兼容两种页面
    private static final String TOGGLE_FULLSCREEN_JS = """
            (function () {
                try {
                    var cctv = document.querySelector('[id^="player_pagefullscreen_"]');
                    if (cctv) { cctv.click(); return 'cctv'; }
                    var ac = document.querySelector('.videoFull_ac');
                    if (ac) { ac.click(); return 'ysp-exit'; }
                    var enter = document.querySelector('.videoFull:not([id])');
                    if (enter) { enter.click(); return 'ysp-enter'; }
                    var rootFs = document.querySelector('.videoFull[id]');
                    if (rootFs) { return 'ysp-already'; }
                } catch (e) {
                    return 'err:' + e;
                }
                return 'none';
            })();
            """;

    // 诊断悬浮层数据采集
    private static final String DIAG_JS = """
            (function () {
                try {
                    var out = {
                        fast: (window.__cctvFastLoading ? (window.__cctvFastLoading.done ? 'stopped' : 'running') : 'n/a'),
                        auto: (window.__cctvAutoFullscreen ? (window.__cctvAutoFullscreen.done ? 'done' : 'running') : 'n/a')
                    };
                    var fs = document.querySelector('[id^="player_pagefullscreen_"]');
                    if (fs) { out.fs = fs.getAttribute('isPageFullscreen') || 'no-attr'; }
                    else if (document.querySelector('.videoFull_ac')) { out.fs = 'ysp-ac'; }
                    else if (document.querySelector('.videoFull')) { out.fs = 'ysp-btn'; }
                    else { out.fs = 'none'; }
                    var v = document.querySelector('video');
                    if (v) {
                        out.rs = v.readyState;
                        out.size = v.videoWidth + 'x' + v.videoHeight;
                        try {
                            out.buf = v.buffered.length ? Math.round((v.buffered.end(v.buffered.length - 1) - v.currentTime) * 10) / 10 : 0;
                        } catch (e) { out.buf = -1; }
                        if (v.getVideoPlaybackQuality) {
                            var q = v.getVideoPlaybackQuality();
                            out.drop = q.droppedVideoFrames + '/' + q.totalVideoFrames;
                        }
                    } else {
                        out.video = 'none';
                    }
                    return JSON.stringify(out);
                } catch (e) {
                    return 'diagErr:' + e;
                }
            })();
            """;


    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_main);

        // 加载设置
        // 获取 SharedPreferences
        SharedPreferences sharedPreferences = PreferenceManager.getDefaultSharedPreferences(this);

        // 读取字体大小
        String selectedOption = sharedPreferences.getString("text_size", "1");
        switch (selectedOption) {
            case "0":
                TEXT_SIZE = 18;
                break;
            case "1":
                TEXT_SIZE = 22;
                break;
            case "2":
                TEXT_SIZE = 25;
                break;
            case "3":
                TEXT_SIZE = 30;
                break;

        }

        // 读取直接频道切换设置
        enableDirectChannelChange = sharedPreferences.getBoolean("direct_channel_change", false);

        // 读取直接返回设置
        enableDirectBack = sharedPreferences.getBoolean("direct_back", true);

        // 读取双缓冲设置
        enableDualWebView = sharedPreferences.getBoolean("dual_webview", false);

        // 读取调试/诊断开关（重启生效）
        disableFastLoading = sharedPreferences.getBoolean("disable_fast_loading", false);
        disableAutoFullscreen = sharedPreferences.getBoolean("disable_auto_fullscreen", false);
        disableLocalReload = sharedPreferences.getBoolean("disable_local_reload", false);
        diagOverlayEnabled = sharedPreferences.getBoolean("diag_overlay", false);
        if (diagOverlayEnabled || sharedPreferences.getBoolean("debug_mode", false)) {
            try {
                WebView.setWebContentsDebuggingEnabled(true);
            } catch (Throwable ignored) {
            }
            if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.KITKAT) {
                try {
                    android.webkit.WebView.setWebContentsDebuggingEnabled(true);
                } catch (Throwable ignored) {
                }
            }
        }


        // 读取WebView设置
        Boolean forceSysWebView = sharedPreferences.getBoolean("sys_webview", true);

        // 获取 AudioManager 实例
        audioManager = (AudioManager) getSystemService(AUDIO_SERVICE);

        // 初始化 WebView
        webView0 = findViewById(R.id.webView0);
        webView1 = findViewById(R.id.webView1);

        // 初始化显示正在输入的数字的 TextView
        inputTextView = findViewById(R.id.inputTextView);

        // 初始化 loadingOverlay
        loadingOverlay = findViewById(R.id.loadingOverlay);

        // 初始化 overlayTextView
        overlayTextView = findViewById(R.id.overlayTextView);

        // 初始化 菜单
        menuOverlay = findViewById(R.id.menuOverlay);

        // 初始化 DrawerLayout
        DrawerLayout = findViewById(R.id.DrawerLayout);

        // 初始化 DrawerLayoutDetailed
        DrawerLayoutDetailed = findViewById(R.id.DrawerLayoutDetailed);

        // 初始化 CCTV 子菜单
        SubMenuCCTV = findViewById(R.id.subMenuCCTV);

        // 初始化 Local 子菜单
        SubMenuLocal = findViewById(R.id.subMenuLocal);

        // 初始化 CoreText
        CoreText = findViewById(R.id.CoreText);

        LinearLayout DrawerLayout = findViewById(R.id.DrawerLayout);

        // 中央台频道列表
        String[] firstDrawer = {
                "央视频道", "地方频道"
        };

        // 动态生成中央台按钮和地方台按钮
        for (String channel : firstDrawer) {
            Button button = new Button(this);
            button.setText(channel);
            // 创建 LayoutParams 并设置 margin
            LinearLayout.LayoutParams layoutParams = new LinearLayout.LayoutParams(
                    LinearLayout.LayoutParams.MATCH_PARENT, LinearLayout.LayoutParams.WRAP_CONTENT);
            layoutParams.setMargins(4, 4, 4, 4);
            // 设置按钮属性
            button.setLayoutParams(layoutParams);
            button.setPadding(16, 16, 16, 16);
            button.setTextColor(getResources().getColor(android.R.color.white));
            button.setBackground(getResources().getDrawable(R.drawable.detailed_channel_selector));
            button.setTextSize(TEXT_SIZE);
            DrawerLayout.addView(button);
        }

        // 添加设置按钮
        Button SettingButton = new Button(this);
        SettingButton.setText("打开设置");
        // 创建 LayoutParams 并设置 margin
        LinearLayout.LayoutParams layoutParams0 = new LinearLayout.LayoutParams(
                LinearLayout.LayoutParams.MATCH_PARENT, LinearLayout.LayoutParams.WRAP_CONTENT);
        layoutParams0.setMargins(4, 28, 4, 4);
        // 设置按钮属性
        SettingButton.setLayoutParams(layoutParams0);
        SettingButton.setPadding(16, 16, 16, 16);
        SettingButton.setTextColor(getResources().getColor(android.R.color.white));
        SettingButton.setBackground(getResources().getDrawable(R.drawable.detailed_channel_selector));
        SettingButton.setTextSize(TEXT_SIZE);
        DrawerLayout.addView(SettingButton);

        LinearLayout subMenuCCTV = findViewById(R.id.subMenuCCTV);
        LinearLayout subMenuLocal = findViewById(R.id.subMenuLocal);

        // 中央台频道列表
        String[] cctvChannels = {
                "CCTV-1 综合", "CCTV-2 财经", "CCTV-3 综艺", "CCTV-4 中文国际（亚）",
                "CCTV-5 体育", "CCTV-6 电影", "CCTV-7 国防军事", "CCTV-8 电视剧",
                "CCTV-9 纪录", "CCTV-10 科教", "CCTV-11 戏曲", "CCTV-12 社会与法",
                "CCTV-13 新闻", "CCTV-14 少儿", "CCTV-15 音乐", "CCTV-16 奥林匹克",
                "CCTV-17 农业农村", "CCTV-5+ 体育赛事", "CCTV-4 中文国际（欧）", "CCTV-4 中文国际（美）"
        };

        // 动态生成中央台按钮
        for (String channel : cctvChannels) {
            Button button = new Button(this);
            button.setText(channel);
            // 创建 LayoutParams 并设置 margin
            LinearLayout.LayoutParams layoutParams = new LinearLayout.LayoutParams(
                    LinearLayout.LayoutParams.MATCH_PARENT, LinearLayout.LayoutParams.WRAP_CONTENT);
            layoutParams.setMargins(4, 4, 4, 4);
            // 设置按钮属性
            button.setLayoutParams(layoutParams);
            button.setPadding(16, 16, 16, 16);
            button.setTextColor(getResources().getColor(android.R.color.white));
            button.setBackground(getResources().getDrawable(R.drawable.detailed_channel_selector));
            button.setTextSize(TEXT_SIZE);
            subMenuCCTV.addView(button);
        }

        // 地方台频道列表
        String[] localChannels = {
                "北京卫视", "江苏卫视", "东方卫视", "浙江卫视", "湖南卫视", "湖北卫视",
                "广东卫视", "广西卫视", "黑龙江卫视", "海南卫视", "重庆卫视", "深圳卫视",
                "四川卫视", "河南卫视", "福建东南卫视", "贵州卫视", "江西卫视", "辽宁卫视",
                "安徽卫视", "河北卫视", "山西卫视"
        };

        // 动态生成地方台按钮
        for (String channel : localChannels) {
            Button button = new Button(this);
            button.setText(channel);
            // 创建 LayoutParams 并设置 margin
            LinearLayout.LayoutParams layoutParams = new LinearLayout.LayoutParams(
                    LinearLayout.LayoutParams.MATCH_PARENT, LinearLayout.LayoutParams.WRAP_CONTENT);
            layoutParams.setMargins(4, 4, 4, 4);
            // 设置按钮属性
            button.setLayoutParams(layoutParams);
            button.setPadding(16, 16, 16, 16);
            button.setTextColor(getResources().getColor(android.R.color.white));
            button.setBackground(getResources().getDrawable(R.drawable.detailed_channel_selector));
            button.setTextSize(TEXT_SIZE);
            subMenuLocal.addView(button);
        }

        // https://developer.android.com/reference/android/webkit/WebView.html#getCurrentWebViewPackage()
        if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.O) { // Android 8.0+
            PackageInfo pkgInfo = WebView.getCurrentWebViewPackage();
            if (pkgInfo != null) {
                webViewVersionText = String.valueOf(pkgInfo.versionName);
                CoreText.setText("当前程序运行在系统WebView上，版本号：" + pkgInfo.versionName);
            }
        }

        // X5内核代码
        if (!forceSysWebView) {
            QbSdk.unForceSysWebView();
            requestPermission();

            Log.d("versionX5",String.valueOf(QbSdk.getTbsVersion(getApplicationContext())));
            canLoadX5 = QbSdk.canLoadX5(getApplicationContext());
            Log.d("canLoadX5", String.valueOf(canLoadX5));
            if (canLoadX5) {
                CoreText.setText("当前程序运行在腾讯X5内核上");
            } else {
                Intent intent = new Intent(MainActivity.this, LoadingActivity.class);
                startActivity(intent);
                finish(); // 销毁 MainActivity
            }
        }
        else{
            QbSdk.forceSysWebView();
        }

        HashMap<String, Object> map = new HashMap<>(2);
        map.put(TbsCoreSettings.TBS_SETTINGS_USE_SPEEDY_CLASSLOADER, true);
        map.put(TbsCoreSettings.TBS_SETTINGS_USE_DEXLOADER_SERVICE, true);
        QbSdk.initTbsSettings(map);

        // 配置 WebView 设置
        WebSettings webSettings = webView0.getSettings();
        webSettings.setJavaScriptEnabled(true);
        webSettings.setDomStorageEnabled(true);
        webSettings.setDatabaseEnabled(true);
        webSettings.setLoadsImagesAutomatically(false); // 禁用自动加载图片
        webSettings.setBlockNetworkImage(true); // 禁用网络图片加载
        webSettings.setMediaPlaybackRequiresUserGesture(false);
        webSettings.setUserAgent(
                "Mozilla/5.0 (Windows NT 10.0; Win64; x64) AppleWebKit/537.36 (KHTML, like Gecko) Chrome/119.0.0.0 Safari/537.36");

        // 启用缓存
        webSettings.setCacheMode(WebSettings.LOAD_DEFAULT);

        // 启用 JavaScript 自动点击功能
        webSettings.setJavaScriptCanOpenWindowsAutomatically(true);

        // 配置 WebView 设置
        WebSettings webSettings1 = webView1.getSettings();
        webSettings1.setJavaScriptEnabled(true);
        webSettings1.setDomStorageEnabled(true);
        webSettings1.setDatabaseEnabled(true);
        webSettings1.setLoadsImagesAutomatically(false); // 禁用自动加载图片
        webSettings1.setBlockNetworkImage(true); // 禁用网络图片加载
        webSettings1.setMediaPlaybackRequiresUserGesture(false);
        webSettings1.setUserAgent(
                "Mozilla/5.0 (Windows NT 10.0; Win64; x64) AppleWebKit/537.36 (KHTML, like Gecko) Chrome/119.0.0.0 Safari/537.36");

        // 启用缓存
        webSettings1.setCacheMode(WebSettings.LOAD_DEFAULT);

        // 启用 JavaScript 自动点击功能
        webSettings1.setJavaScriptCanOpenWindowsAutomatically(true);

        if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.LOLLIPOP) {
            // X5内核代码
            webSettings.setMixedContentMode(com.tencent.smtt.sdk.WebSettings.LOAD_NORMAL);
            webSettings1.setMixedContentMode(com.tencent.smtt.sdk.WebSettings.LOAD_NORMAL);
            // 系统WebView内核代码
            // webView.getSettings().setMixedContentMode(WebSettings.MIXED_CONTENT_ALWAYS_ALLOW);
        }

        // 设置 WebViewClient 和 WebChromeClient
        webView0.setWebViewClient(new WebViewClient() {
            // X5内核代码
            @Override
            public void onReceivedSslError(com.tencent.smtt.sdk.WebView webView,
                    com.tencent.smtt.export.external.interfaces.SslErrorHandler handler,
                    com.tencent.smtt.export.external.interfaces.SslError error) {
                handler.proceed(); // 忽略 SSL 错误
            }

            // 系统Webview内核代码
            // @Override
            // public void onReceivedSslError(WebView view, SslErrorHandler handler,
            // SslError error) {
            // handler.proceed(); // 忽略 SSL 错误
            // }

            @Override
            public void onPageStarted(WebView view, String url, Bitmap favicon) {
                if (url == null || url.startsWith("about:")) {
                    super.onPageStarted(view, url, favicon);
                    return;
                }
                if (!disableFastLoading) {
                    view.evaluateJavascript(FAST_LOADING_JS, null);
                }
                super.onPageStarted(view, url, favicon);
            }

            // 设置 WebViewClient，监听页面加载完成事件
            @Override
            public void onPageFinished(WebView view, String url) {
                if (Objects.equals(url, "about:blank")) {
                    return;
                }
                // 只处理"当前代次"的加载结果，过期回调直接丢弃（快速换台时防止互相干扰）
                Integer finishGeneration = viewLoadGeneration.get(view);
                if (finishGeneration == null || finishGeneration.intValue() != loadGeneration) {
                    return;
                }
                addEvent("finish #" + finishGeneration + " " + shortUrl(url));
                // 清空info
                info = "";

                if (currentLiveIndex <= 19) {
                    // 获取节目预告和当前节目
                    view.evaluateJavascript("document.querySelector('#jiemu > li.cur.act').innerText", value -> {
                        // 处理获取到的元素值
                        if (!value.equals("null") && !value.isEmpty()) {
                            String elementValueNow = value.replace("\"", ""); // 去掉可能的引号
                            info += elementValueNow + "\n";
                        }
                    });
                    view.evaluateJavascript("document.querySelector('#jiemu > li:nth-child(4)').innerText", value -> {
                        // 处理获取到的元素值
                        if (!value.equals("null") && !value.isEmpty()) {
                            String elementValueNext = value.replace("\"", ""); // 去掉可能的引号
                            info += elementValueNext;
                        }
                    });
                } else if (currentLiveIndex <= 40) {
                    // 获取当前节目
                    view.evaluateJavascript(
                            "document.getElementsByClassName(\"tvSelectJiemu\")[0].innerHTML + \" \" + document.getElementsByClassName(\"tvSelectJiemu\")[1].innerHTML",
                            value -> {
                                if (!value.equals("null") && !value.isEmpty()) {
                                    String elementValueNow = value.replace("\"", ""); // 去掉可能的引号
                                    info += elementValueNow;
                                }
                            });
                }
                if (!disableAutoFullscreen) {
                    view.evaluateJavascript(AUTO_FULLSCREEN_JS, null);
                }
                final int genForUi = finishGeneration.intValue();
                new Handler(Looper.getMainLooper()).postDelayed(() -> {
                    Integer currentGen = viewLoadGeneration.get(view);
                    if (currentGen == null || currentGen.intValue() != genForUi || isFinishing() || isDestroyed()) {
                        return;
                    }
                    // 隐藏加载的 View
                    loadingOverlay.setVisibility(View.GONE);
                    webView0.setVisibility(View.VISIBLE);
                    webView1.setVisibility(View.GONE);
                    webView1.loadUrl("about:blank");

                    isChanging = false;

                    // 显示覆盖层，传入当前频道信息
                    showOverlay(channelNames[currentLiveIndex] + "\n" + info);
                }, 500);
            }
        });

        webView1.setWebViewClient(new WebViewClient() {
            // X5内核代码
            @Override
            public void onReceivedSslError(com.tencent.smtt.sdk.WebView webView,
                    com.tencent.smtt.export.external.interfaces.SslErrorHandler handler,
                    com.tencent.smtt.export.external.interfaces.SslError error) {
                handler.proceed(); // 忽略 SSL 错误
            }

            // 系统Webview内核代码
            // @Override
            // public void onReceivedSslError(WebView view, SslErrorHandler handler,
            // SslError error) {
            // handler.proceed(); // 忽略 SSL 错误
            // }

            @Override
            public void onPageStarted(WebView view, String url, Bitmap favicon) {
                if (url == null || url.startsWith("about:")) {
                    super.onPageStarted(view, url, favicon);
                    return;
                }
                if (!disableFastLoading) {
                    view.evaluateJavascript(FAST_LOADING_JS, null);
                }
                super.onPageStarted(view, url, favicon);
            }

            // 设置 WebViewClient，监听页面加载完成事件
            @Override
            public void onPageFinished(WebView view, String url) {
                if (Objects.equals(url, "about:blank")) {
                    return;
                }
                // 只处理"当前代次"的加载结果，过期回调直接丢弃（快速换台时防止互相干扰）
                Integer finishGeneration = viewLoadGeneration.get(view);
                if (finishGeneration == null || finishGeneration.intValue() != loadGeneration) {
                    return;
                }
                addEvent("finish #" + finishGeneration + " " + shortUrl(url));
                // 清空info
                info = "";

                if (currentLiveIndex <= 19) {
                    // 获取节目预告和当前节目
                    view.evaluateJavascript("document.querySelector('#jiemu > li.cur.act').innerText", value -> {
                        // 处理获取到的元素值
                        if (!value.equals("null") && !value.isEmpty()) {
                            String elementValueNow = value.replace("\"", ""); // 去掉可能的引号
                            info += elementValueNow + "\n";
                        }
                    });
                    view.evaluateJavascript("document.querySelector('#jiemu > li:nth-child(4)').innerText", value -> {
                        // 处理获取到的元素值
                        if (!value.equals("null") && !value.isEmpty()) {
                            String elementValueNext = value.replace("\"", ""); // 去掉可能的引号
                            info += elementValueNext;
                        }
                    });
                } else if (currentLiveIndex <= 40) {
                    // 获取当前节目
                    view.evaluateJavascript(
                            "document.getElementsByClassName(\"tvSelectJiemu\")[0].innerHTML + \" \" + document.getElementsByClassName(\"tvSelectJiemu\")[1].innerHTML",
                            value -> {
                                if (!value.equals("null") && !value.isEmpty()) {
                                    String elementValueNow = value.replace("\"", ""); // 去掉可能的引号
                                    info += elementValueNow;
                                }
                            });
                }
                if (!disableAutoFullscreen) {
                    view.evaluateJavascript(AUTO_FULLSCREEN_JS, null);
                }
                final int genForUi = finishGeneration.intValue();
                new Handler(Looper.getMainLooper()).postDelayed(() -> {
                    Integer currentGen = viewLoadGeneration.get(view);
                    if (currentGen == null || currentGen.intValue() != genForUi || isFinishing() || isDestroyed()) {
                        return;
                    }
                    // 隐藏加载的 View
                    loadingOverlay.setVisibility(View.GONE);

                    if (enableDualWebView) {
                        webView1.setVisibility(View.VISIBLE);
                        webView0.setVisibility(View.GONE);
                        webView0.loadUrl("about:blank");
                    }

                    isChanging = false;

                    // 显示覆盖层，传入当前频道信息
                    showOverlay(channelNames[currentLiveIndex] + "\n" + info);
                }, 1000);
            }
        });

        // 禁用缩放
        webSettings.setSupportZoom(false);
        webSettings.setBuiltInZoomControls(false);
        webSettings.setDisplayZoomControls(false);
        webSettings1.setSupportZoom(false);
        webSettings1.setBuiltInZoomControls(false);
        webSettings1.setDisplayZoomControls(false);

        // 在 Android TV 上，需要禁用焦点自动导航
        webView0.setFocusable(false);
        webView1.setFocusable(false);

        // 开启无图（X5内核）
        if (canLoadX5) {
            webView0.getSettingsExtension().setPicModel(IX5WebSettingsExtension.PicModel_NoPic);
            webView1.getSettingsExtension().setPicModel(IX5WebSettingsExtension.PicModel_NoPic);
        }
        // 设置 WebView 客户端（含 HTML5 全屏接管与页面 console 日志）
        webView0.setWebChromeClient(new FullscreenWebChromeClient());
        webView1.setWebChromeClient(new FullscreenWebChromeClient());

        // 按照设置关闭双缓冲
        if (!enableDualWebView) {
            webView0.destroy();
        }

        // 加载上次保存的位置
        loadLastLiveIndex();

        // 启动定时任务，每隔一定时间执行一次
        // startPeriodicTask();

        // 诊断悬浮层（默认关闭）
        if (diagOverlayEnabled) {
            startDiagOverlay();
        }

    }

    // 启动自动播放定时任务
    private void startPeriodicTask() {
        // 使用 postDelayed 方法设置定时任务
        handler.postDelayed(periodicTask, 2000); // 2000 毫秒，即 2 秒钟
    }

    // 定时任务具体操作
    private final Runnable periodicTask = new Runnable() {
        @Override
        public void run() {
            // 获取 div 元素的 display 属性，并执行相应的操作
            getDivDisplayPropertyAndDoSimulateTouch();

            // 完成后再次调度定时任务
            handler.postDelayed(this, 2000); // 2000 毫秒，即 2 秒钟
        }
    };

    // 获取 div 元素的 display 属性并执行相应的操作
    private void getDivDisplayPropertyAndDoSimulateTouch() {
        if (webView0 != null) {
            if (currentLiveIndex <= 19) {
                webView0.evaluateJavascript("document.getElementById('play_or_pause_play_player').style.display",
                        value -> {
                            // 处理获取到的 display 属性值
                            if (value.equals("\"block\"")) {
                                // 执行点击操作
                                simulateTouch(webView0, 0.5f, 0.5f);
                            }
                        });
            } else if (currentLiveIndex <= 40) {
                String scriptPlay = """
                        try{
                        if(document.querySelector('.voice.on').style.display == 'none'){
                            document.querySelector('.voice.on').click();
                        }
                        document.querySelector('.play.play1').click();
                        } catch(e) {
                        }
                        """;
                webView0.evaluateJavascript(scriptPlay, null);
            }
        }
    }

    @Override
    public boolean dispatchKeyEvent(KeyEvent event) {
        if (event.getAction() == KeyEvent.ACTION_DOWN) {
            if (event.getKeyCode() == KeyEvent.KEYCODE_VOLUME_UP) {
                audioManager.adjustStreamVolume(AudioManager.STREAM_MUSIC, AudioManager.ADJUST_RAISE,
                        AudioManager.FLAG_SHOW_UI);
            } else if (event.getKeyCode() == KeyEvent.KEYCODE_VOLUME_DOWN) {
                audioManager.adjustStreamVolume(AudioManager.STREAM_MUSIC, AudioManager.ADJUST_LOWER,
                        AudioManager.FLAG_SHOW_UI);
            } else if (menuOverlay.hasFocus()) {
                // menuOverlay具有焦点
                if (event.getKeyCode() == KeyEvent.KEYCODE_BACK || event.getKeyCode() == KeyEvent.KEYCODE_B) {
                    // 按下返回键
                    showMenuOverlay();
                    return true;
                } else if (event.getKeyCode() == KeyEvent.KEYCODE_DPAD_LEFT
                        || event.getKeyCode() == KeyEvent.KEYCODE_DPAD_RIGHT) {
                    // 方向键,切换五个按钮选择
                    if (event.getKeyCode() == KeyEvent.KEYCODE_DPAD_LEFT) {
                        if (menuOverlaySelectedIndex == 0) {
                            menuOverlaySelectedIndex = 5;
                        } else {
                            menuOverlaySelectedIndex--;
                        }
                    } else if (event.getKeyCode() == KeyEvent.KEYCODE_DPAD_RIGHT) {
                        if (menuOverlaySelectedIndex == 5) {
                            menuOverlaySelectedIndex = 0;
                        } else {
                            menuOverlaySelectedIndex++;
                        }
                    }
                    menuOverlay.getChildAt(menuOverlaySelectedIndex).requestFocus();
                    return true;
                } else if (event.getKeyCode() == KeyEvent.KEYCODE_DPAD_CENTER
                        || event.getKeyCode() == KeyEvent.KEYCODE_ENTER) {
                    // 中间键,执行按钮操作
                    switch (menuOverlaySelectedIndex) {
                        case 0:
                            // 刷新页面
                            getCurrentWebview().reload();
                            showMenuOverlay();
                            break;
                        case 1:
                            // 播放
                            if (currentLiveIndex <= 19) {
                                View currentView = getCurrentWebview();
                                simulateTouch(currentView, currentView.getWidth() / 2f, currentView.getHeight() / 2f);
                            } else if (currentLiveIndex <= 40) {
                                String scriptPause = """
                                        try{
                                        document.querySelector('.play.play2').click();
                                        } catch(e) {
                                        document.querySelector('.play.play1').click();
                                        }
                                        """;
                                getCurrentWebview().evaluateJavascript(scriptPause, null);
                            }
                            showMenuOverlay();
                            break;
                        case 2:
                            // 切换全屏（按页面实际状态进入/退出，兼容央视与央视频）
                            getCurrentWebview().evaluateJavascript(TOGGLE_FULLSCREEN_JS,
                                    value -> addEvent("toggleFullscreen -> " + value));
                            break;
                        case 3:
                            // 放大
                            String scriptZoomIn = """
                                    // 获取当前页面的缩放比例
                                    function getZoom() {
                                      return parseFloat(document.body.style.zoom) || 1;
                                    }

                                    // 设置页面的缩放比例
                                    function setZoom(zoom) {
                                      document.body.style.zoom = zoom;
                                    }

                                    // 页面放大函数
                                    function zoomIn() {
                                      var zoom = getZoom();
                                      setZoom(zoom + 0.1);
                                    }

                                    zoomIn();
                                    """;
                            getCurrentWebview().evaluateJavascript(scriptZoomIn, null);
                            break;
                        case 4:
                            // 缩小
                            String scriptZoomOut = """
                                    // 获取当前页面的缩放比例
                                    function getZoom() {
                                      return parseFloat(document.body.style.zoom) || 1;
                                    }

                                    // 设置页面的缩放比例
                                    function setZoom(zoom) {
                                      document.body.style.zoom = zoom;
                                    }

                                    // 页面缩小函数
                                    function zoomOut() {
                                      var zoom = getZoom();
                                      if (zoom > 0.2) {
                                        setZoom(zoom - 0.1);
                                      }
                                    }

                                    zoomOut();
                                    """;
                            getCurrentWebview().evaluateJavascript(scriptZoomOut, null);
                            break;
                        case 5:
                            // 打开设置
                            Intent intent = new Intent(this, SettingsActivity.class);
                            startActivity(intent);
                            break;
                    }
                    return true;
                }
                return true;
            }
            if (DrawerLayout.hasFocus() && !SubMenuCCTV.hasFocus() && !SubMenuLocal.hasFocus()
                    && !DrawerLayoutDetailed.hasFocus()) {
                // DrawerLayout具有焦点
                if (event.getKeyCode() == KeyEvent.KEYCODE_BACK || event.getKeyCode() == KeyEvent.KEYCODE_DPAD_LEFT
                        || event.getKeyCode() == KeyEvent.KEYCODE_B) {
                    // 按下返回键
                    showChannelList();
                    return true;
                } else if (event.getKeyCode() == KeyEvent.KEYCODE_DPAD_UP
                        || event.getKeyCode() == KeyEvent.KEYCODE_DPAD_DOWN) {
                    // 方向键,切换频道选择
                    if (event.getKeyCode() == KeyEvent.KEYCODE_DPAD_UP) {
                        if (DrawerLayoutSelectedIndex == 0) {
                            DrawerLayoutSelectedIndex = 2;
                        } else {
                            DrawerLayoutSelectedIndex--;
                        }
                    } else if (event.getKeyCode() == KeyEvent.KEYCODE_DPAD_DOWN) {
                        if (DrawerLayoutSelectedIndex == 2) {
                            DrawerLayoutSelectedIndex = 0;
                        } else {
                            DrawerLayoutSelectedIndex++;
                        }
                    }
                    DrawerLayout.getChildAt(DrawerLayoutSelectedIndex).requestFocus();
                    return true;
                } else if (event.getKeyCode() == KeyEvent.KEYCODE_DPAD_CENTER
                        || event.getKeyCode() == KeyEvent.KEYCODE_ENTER
                        || event.getKeyCode() == KeyEvent.KEYCODE_DPAD_RIGHT) {
                    // 中间键,执行按钮操作
                    switch (DrawerLayoutSelectedIndex) {
                        case 0:
                            // 中央频道
                            DrawerLayoutDetailed.setVisibility(View.VISIBLE);
                            findViewById(R.id.subMenuCCTV).setVisibility(View.VISIBLE);
                            findViewById(R.id.CCTVScroll).setVisibility(View.VISIBLE);
                            findViewById(R.id.subMenuLocal).setVisibility(View.GONE);
                            findViewById(R.id.LocalScroll).setVisibility(View.GONE);
                            SubMenuCCTV.getChildAt(SubMenuCCTVSelectedIndex).requestFocus();
                            break;
                        case 1:
                            // 地方频道
                            DrawerLayoutDetailed.setVisibility(View.VISIBLE);
                            findViewById(R.id.subMenuCCTV).setVisibility(View.GONE);
                            findViewById(R.id.CCTVScroll).setVisibility(View.GONE);
                            findViewById(R.id.subMenuLocal).setVisibility(View.VISIBLE);
                            findViewById(R.id.LocalScroll).setVisibility(View.VISIBLE);
                            SubMenuLocal.getChildAt(SubMenuLocalSelectedIndex).requestFocus();
                            break;
                        case 2:
                            // 打开设置
                            showChannelList();
                            showMenuOverlay();
                            break;
                    }
                    return true;
                }
            } else if (SubMenuCCTV.hasFocus()) {
                if (event.getKeyCode() == KeyEvent.KEYCODE_BACK || event.getKeyCode() == KeyEvent.KEYCODE_B) {
                    // 按下返回键
                    if (enableDirectBack) {
                        showChannelList();
                        return true;
                    } else {
                        DrawerLayout.getChildAt(DrawerLayoutSelectedIndex).requestFocus();
                        SubMenuCCTV.setVisibility(View.GONE);
                        DrawerLayoutDetailed.setVisibility(View.GONE);
                        findViewById(R.id.CCTVScroll).setVisibility(View.GONE);
                        return true;
                    }
                } else if (event.getKeyCode() == KeyEvent.KEYCODE_DPAD_LEFT) {
                    DrawerLayout.getChildAt(DrawerLayoutSelectedIndex).requestFocus();
                    SubMenuCCTV.setVisibility(View.GONE);
                    DrawerLayoutDetailed.setVisibility(View.GONE);
                    findViewById(R.id.CCTVScroll).setVisibility(View.GONE);
                    return true;
                } else if (event.getKeyCode() == KeyEvent.KEYCODE_DPAD_UP
                        || event.getKeyCode() == KeyEvent.KEYCODE_DPAD_DOWN) {
                    // 方向键,切换频道选择
                    if (event.getKeyCode() == KeyEvent.KEYCODE_DPAD_UP) {
                        if (SubMenuCCTVSelectedIndex == 0) {
                            SubMenuCCTVSelectedIndex = 19;
                        } else {
                            SubMenuCCTVSelectedIndex--;
                        }
                    } else if (event.getKeyCode() == KeyEvent.KEYCODE_DPAD_DOWN) {
                        if (SubMenuCCTVSelectedIndex == 19) {
                            SubMenuCCTVSelectedIndex = 0;
                        } else {
                            SubMenuCCTVSelectedIndex++;
                        }
                    }
                    SubMenuCCTV.getChildAt(SubMenuCCTVSelectedIndex).requestFocus();
                    return true;
                } else if (event.getKeyCode() == KeyEvent.KEYCODE_DPAD_CENTER
                        || event.getKeyCode() == KeyEvent.KEYCODE_ENTER
                        || event.getKeyCode() == KeyEvent.KEYCODE_DPAD_RIGHT) {
                    // 中间键,执行按钮操作
                    currentLiveIndex = SubMenuCCTVSelectedIndex;
                    loadLiveUrl();
                    saveCurrentLiveIndex();
                    showChannelList();
                    return true;
                }
            } else if (SubMenuLocal.hasFocus()) {
                if (event.getKeyCode() == KeyEvent.KEYCODE_BACK || event.getKeyCode() == KeyEvent.KEYCODE_B) {
                    if (enableDirectBack) {
                        showChannelList();
                        return true;
                    } else {
                        // 按下返回键
                        DrawerLayout.getChildAt(DrawerLayoutSelectedIndex).requestFocus();
                        SubMenuLocal.setVisibility(View.GONE);
                        DrawerLayoutDetailed.setVisibility(View.GONE);
                        findViewById(R.id.LocalScroll).setVisibility(View.GONE);
                        return true;
                    }
                } else if (event.getKeyCode() == KeyEvent.KEYCODE_DPAD_LEFT) {
                    DrawerLayout.getChildAt(DrawerLayoutSelectedIndex).requestFocus();
                    SubMenuLocal.setVisibility(View.GONE);
                    DrawerLayoutDetailed.setVisibility(View.GONE);
                    findViewById(R.id.LocalScroll).setVisibility(View.GONE);
                    return true;
                } else if (event.getKeyCode() == KeyEvent.KEYCODE_DPAD_UP
                        || event.getKeyCode() == KeyEvent.KEYCODE_DPAD_DOWN) {
                    // 方向键,切换频道选择
                    if (event.getKeyCode() == KeyEvent.KEYCODE_DPAD_UP) {
                        if (SubMenuLocalSelectedIndex == 0) {
                            SubMenuLocalSelectedIndex = 20;
                        } else {
                            SubMenuLocalSelectedIndex--;
                        }
                    } else if (event.getKeyCode() == KeyEvent.KEYCODE_DPAD_DOWN) {
                        if (SubMenuLocalSelectedIndex == 20) {
                            SubMenuLocalSelectedIndex = 0;
                        } else {
                            SubMenuLocalSelectedIndex++;
                        }
                    }
                    SubMenuLocal.getChildAt(SubMenuLocalSelectedIndex).requestFocus();
                    return true;
                } else if (event.getKeyCode() == KeyEvent.KEYCODE_DPAD_CENTER
                        || event.getKeyCode() == KeyEvent.KEYCODE_ENTER
                        || event.getKeyCode() == KeyEvent.KEYCODE_DPAD_RIGHT) {
                    // 中间键,执行按钮操作
                    currentLiveIndex = SubMenuLocalSelectedIndex + 20;
                    loadLiveUrl();
                    saveCurrentLiveIndex();
                    showChannelList();
                    return true;
                }
            } else if (DrawerLayoutDetailed.hasFocus()) {
                return true;
            } else if (event.getKeyCode() == KeyEvent.KEYCODE_DPAD_UP && !isChanging) {
                // 执行上一个直播地址的操作
                navigateToPreviousLive();
                return true; // 返回 true 表示事件已处理，不传递给 WebView
            } else if (event.getKeyCode() == KeyEvent.KEYCODE_DPAD_DOWN && !isChanging) {
                // 执行下一个直播地址的操作
                navigateToNextLive();
                return true; // 返回 true 表示事件已处理，不传递给 WebView
            } else if ((event.getKeyCode() == KeyEvent.KEYCODE_ENTER
                    || event.getKeyCode() == KeyEvent.KEYCODE_DPAD_CENTER) && !isChanging) {
                // 换台菜单
                showChannelList();
                // 显示节目列表
                showOverlay(channelNames[currentLiveIndex] + "\n" + info);
                return true; // 返回 true 表示事件已处理，不传递给 WebView
            } else if ((event.getKeyCode() == KeyEvent.KEYCODE_MENU || event.getKeyCode() == KeyEvent.KEYCODE_M)
                    && !isChanging) {
                // 显示菜单
                showMenuOverlay();

                return true; // 返回 true 表示事件已处理，不传递给 WebView
            } else if ((event.getKeyCode() >= KeyEvent.KEYCODE_0 && event.getKeyCode() <= KeyEvent.KEYCODE_9)
                    && !isChanging) {
                int numericKey = event.getKeyCode() - KeyEvent.KEYCODE_0;

                // 将按下的数字键追加到缓冲区
                digitBuffer.append(numericKey);

                // 使用 Handler 来在超时后处理输入的数字
                new Handler().postDelayed(() -> handleNumericInput(), DIGIT_TIMEOUT);

                // 更新显示正在输入的数字的 TextView
                updateInputTextView();

                return true; // 事件已处理，不传递给 WebView
            } else if (event.getKeyCode() == KeyEvent.KEYCODE_BACK || event.getKeyCode() == KeyEvent.KEYCODE_B) {
                if (customFullscreenView != null) {
                    // 页面 HTML5 全屏状态下，先退出全屏
                    hideCustomFullscreen();
                    return true;
                }
                if (doubleBackToExitPressedOnce) {
                    super.onBackPressed();
                    System.exit(0);
                    return true;
                }

                this.doubleBackToExitPressedOnce = true;
                Toast.makeText(this, "再按一次返回键退出应用", Toast.LENGTH_SHORT).show();

                new Handler().postDelayed(() -> doubleBackToExitPressedOnce = false, 2000);
                // 如果两秒内再次按返回键，则退出应用
            }
        }
        return true; // 不处理其余事件
    }

    // 显示底部菜单
    private void showMenuOverlay() {
        if (!isMenuOverlayVisible) {
            menuOverlay.getChildAt(menuOverlaySelectedIndex).requestFocus();
            menuOverlay.setVisibility(View.VISIBLE);
            isMenuOverlayVisible = true;
        } else {
            menuOverlay.setVisibility(View.GONE);
            menuOverlaySelectedIndex = 0;
            isMenuOverlayVisible = false;
        }
    }

    // 频道选择列表
    private void showChannelList() {
        // 显示频道抽屉
        if (!isDrawerOverlayVisible) {
            DrawerLayoutDetailed.setVisibility(View.VISIBLE);
            DrawerLayout.setVisibility(View.VISIBLE);
            isDrawerOverlayVisible = true;
            if (currentLiveIndex < 20) {
                SubMenuCCTV.setVisibility(View.VISIBLE);
                findViewById(R.id.CCTVScroll).setVisibility(View.VISIBLE);
                SubMenuCCTV.getChildAt(currentLiveIndex).requestFocus();
                SubMenuCCTVSelectedIndex = currentLiveIndex;
                DrawerLayoutSelectedIndex = 0;
            } else {
                SubMenuLocal.setVisibility(View.VISIBLE);
                findViewById(R.id.LocalScroll).setVisibility(View.VISIBLE);
                SubMenuLocal.getChildAt(currentLiveIndex - 20).requestFocus();
                SubMenuLocalSelectedIndex = currentLiveIndex - 20;
                DrawerLayoutSelectedIndex = 1;
            }
        } else {
            DrawerLayout.setVisibility(View.GONE);
            SubMenuCCTV.setVisibility(View.GONE);
            SubMenuLocal.setVisibility(View.GONE);
            findViewById(R.id.LocalScroll).setVisibility(View.GONE);
            findViewById(R.id.CCTVScroll).setVisibility(View.GONE);
            DrawerLayoutDetailed.setVisibility(View.GONE);
            isDrawerOverlayVisible = false;
        }
    }

    private void showChannelListDPAD(int selectIndex) {
        // 显示频道抽屉
        if (!isDrawerOverlayVisible) {
            DrawerLayoutDetailed.setVisibility(View.VISIBLE);
            DrawerLayout.setVisibility(View.VISIBLE);
            isDrawerOverlayVisible = true;
            if (selectIndex < 20) {
                SubMenuCCTV.setVisibility(View.VISIBLE);
                findViewById(R.id.CCTVScroll).setVisibility(View.VISIBLE);
                SubMenuCCTV.getChildAt(selectIndex).requestFocus();
                SubMenuCCTVSelectedIndex = selectIndex;
                DrawerLayoutSelectedIndex = 0;
            } else {
                SubMenuLocal.setVisibility(View.VISIBLE);
                findViewById(R.id.LocalScroll).setVisibility(View.VISIBLE);
                SubMenuLocal.getChildAt(selectIndex - 20).requestFocus();
                SubMenuLocalSelectedIndex = selectIndex - 20;
                DrawerLayoutSelectedIndex = 1;
            }
        } else {
            DrawerLayout.setVisibility(View.GONE);
            SubMenuCCTV.setVisibility(View.GONE);
            SubMenuLocal.setVisibility(View.GONE);
            findViewById(R.id.LocalScroll).setVisibility(View.GONE);
            findViewById(R.id.CCTVScroll).setVisibility(View.GONE);
            DrawerLayoutDetailed.setVisibility(View.GONE);
            isDrawerOverlayVisible = false;
        }
    }

    private void handleNumericInput() {
        // 将缓冲区中的数字转换为整数
        if (digitBuffer.length() > 0) {
            int numericValue = Integer.parseInt(digitBuffer.toString());

            // 检查数字是否在有效范围内
            if (numericValue > 0 && numericValue <= liveUrls.length) {
                currentLiveIndex = numericValue - 1;
                loadLiveUrl();
                saveCurrentLiveIndex(); // 保存当前位置
            }

            // 重置缓冲区
            digitBuffer.setLength(0);

            // 取消显示正在输入的数字
            inputTextView.setVisibility(View.INVISIBLE);
        }
    }

    private void updateInputTextView() {
        // 在 TextView 中显示当前正在输入的数字
        inputTextView.setVisibility(View.VISIBLE);
        inputTextView.setText("换台：" + digitBuffer.toString());
    }

    private void loadLastLiveIndex() {
        SharedPreferences preferences = getSharedPreferences(PREF_NAME, Context.MODE_PRIVATE);
        currentLiveIndex = preferences.getInt(PREF_KEY_LIVE_INDEX, 0); // 默认值为0
        loadLiveUrl(); // 加载上次保存的位置的直播地址
    }

    private void saveCurrentLiveIndex() {
        SharedPreferences preferences = getSharedPreferences(PREF_NAME, Context.MODE_PRIVATE);
        SharedPreferences.Editor editor = preferences.edit();
        editor.putInt(PREF_KEY_LIVE_INDEX, currentLiveIndex);
        editor.apply();
    }

    private WebView getCurrentWebview() {
        if (!enableDualWebView) {
            return webView1;
        }
        if (currentWebView == 0) {
            return webView0;
        } else {
            return webView1;
        }
    }

    private void loadLiveUrl() {
        if (currentLiveIndex >= 0 && currentLiveIndex < liveUrls.length) {
            // 显示加载的View
            loadingOverlay.setVisibility(View.VISIBLE);

            isChanging = true;

            if (currentWebView == 0) {
                currentWebView = 1;
            } else {
                currentWebView = 0;
            }

            // 代次 + 记录目标 WebView，避免延迟 reload 打到换台后的新页面上
            loadGeneration++;
            final int generation = loadGeneration;
            final WebView target = getCurrentWebview();
            viewLoadGeneration.put(target, generation);

            target.setInitialScale(getMinimumScale());
            target.loadUrl(liveUrls[currentLiveIndex]);
            addEvent("load #" + generation + " " + channelNames[currentLiveIndex]);

            if (currentLiveIndex > 19 && !disableLocalReload) {
                new Handler(Looper.getMainLooper()).postDelayed(() -> {
                    if (isFinishing() || isDestroyed()) {
                        return;
                    }
                    Integer currentGen = viewLoadGeneration.get(target);
                    if (currentGen != null && currentGen.intValue() == generation && target == getCurrentWebview()) {
                        target.setInitialScale(getMinimumScale());
                        target.reload();
                        addEvent("reload #" + generation);
                    }
                }, 1000);
            }
        }
    }

    private void navigateToPreviousLive() {
        if (enableDirectChannelChange) {
            currentLiveIndex = (currentLiveIndex - 1 + liveUrls.length) % liveUrls.length;
            loadLiveUrl();
            saveCurrentLiveIndex(); // 保存当前位置
        } else {
            showChannelListDPAD((currentLiveIndex - 1 + liveUrls.length) % liveUrls.length);
        }
    }

    private void navigateToNextLive() {
        if (enableDirectChannelChange) {
            currentLiveIndex = (currentLiveIndex + 1 + liveUrls.length) % liveUrls.length;
            loadLiveUrl();
            saveCurrentLiveIndex(); // 保存当前位置
        } else {
            showChannelListDPAD((currentLiveIndex + 1 + liveUrls.length) % liveUrls.length);
        }
    }

    private int getMinimumScale() {
        DisplayMetrics displayMetrics = new DisplayMetrics();
        getWindowManager().getDefaultDisplay().getMetrics(displayMetrics);
        int screenWidth = displayMetrics.widthPixels;
        int screenHeight = displayMetrics.heightPixels;

        // 计算缩放比例，使用 double 类型进行计算
        double scale = Math.min((double) screenWidth / 1920.0, (double) screenHeight / 1080.0) * 100;

        Log.d("scale", "scale: " + scale);
        // 四舍五入并转为整数
        return (int) Math.round(scale);
    }

    // 在需要模拟触摸的地方调用该方法
    public void simulateTouch(View view, float x, float y) {
        long downTime = SystemClock.uptimeMillis();
        long eventTime = SystemClock.uptimeMillis() + 100;

        // 构造 ACTION_DOWN 事件
        MotionEvent downEvent = MotionEvent.obtain(downTime, eventTime, MotionEvent.ACTION_DOWN, x, y, 0);
        view.dispatchTouchEvent(downEvent);

        // 构造 ACTION_UP 事件
        MotionEvent upEvent = MotionEvent.obtain(downTime, eventTime + 100, MotionEvent.ACTION_UP, x, y, 0);
        view.dispatchTouchEvent(upEvent);

        // 释放事件对象
        downEvent.recycle();
        upEvent.recycle();
    }

    // 接管页面触发的 HTML5 全屏（无此实现时，requestFullScreen 会静默失败）
    private class FullscreenWebChromeClient extends WebChromeClient {
        @Override
        public void onShowCustomView(View view, IX5WebChromeClient.CustomViewCallback callback) {
            showCustomFullscreen(view, callback);
        }

        @Override
        public void onShowCustomView(View view, int requestedOrientation,
                IX5WebChromeClient.CustomViewCallback callback) {
            showCustomFullscreen(view, callback);
        }

        @Override
        public void onHideCustomView() {
            hideCustomFullscreen();
        }

        @Override
        public boolean onConsoleMessage(ConsoleMessage consoleMessage) {
            try {
                if (consoleMessage != null && consoleMessage.messageLevel() == ConsoleMessage.MessageLevel.ERROR) {
                    addEvent("JS " + consoleMessage.message());
                }
            } catch (Throwable ignored) {
            }
            return true;
        }
    }

    private void showCustomFullscreen(View view, IX5WebChromeClient.CustomViewCallback callback) {
        if (customFullscreenView != null) {
            if (callback != null) {
                callback.onCustomViewHidden();
            }
            return;
        }
        customFullscreenView = view;
        customFullscreenCallback = callback;
        try {
            ViewGroup root = findViewById(android.R.id.content);
            if (root != null) {
                FrameLayout.LayoutParams lp = new FrameLayout.LayoutParams(
                        ViewGroup.LayoutParams.MATCH_PARENT, ViewGroup.LayoutParams.MATCH_PARENT);
                root.addView(view, lp);
            }
        } catch (Throwable t) {
            Log.e("fullscreen", "showCustomFullscreen addView failed", t);
        }
        setImmersiveMode(true);
        addEvent("fullscreen enter");
    }

    private void hideCustomFullscreen() {
        if (customFullscreenView == null) {
            return;
        }
        try {
            ViewGroup root = findViewById(android.R.id.content);
            if (root != null) {
                root.removeView(customFullscreenView);
            }
        } catch (Throwable t) {
            Log.e("fullscreen", "hideCustomFullscreen removeView failed", t);
        }
        customFullscreenView = null;
        IX5WebChromeClient.CustomViewCallback callback = customFullscreenCallback;
        customFullscreenCallback = null;
        if (callback != null) {
            try {
                callback.onCustomViewHidden();
            } catch (Throwable ignored) {
            }
        }
        setImmersiveMode(false);
        addEvent("fullscreen exit");
    }

    private void setImmersiveMode(boolean enable) {
        try {
            View decorView = getWindow().getDecorView();
            if (enable) {
                int flags = View.SYSTEM_UI_FLAG_FULLSCREEN | View.SYSTEM_UI_FLAG_HIDE_NAVIGATION;
                if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.JELLY_BEAN) {
                    flags |= View.SYSTEM_UI_FLAG_LAYOUT_STABLE | View.SYSTEM_UI_FLAG_LAYOUT_FULLSCREEN
                            | View.SYSTEM_UI_FLAG_LAYOUT_HIDE_NAVIGATION;
                }
                if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.KITKAT) {
                    flags |= View.SYSTEM_UI_FLAG_IMMERSIVE_STICKY;
                }
                decorView.setSystemUiVisibility(flags);
            } else {
                decorView.setSystemUiVisibility(View.SYSTEM_UI_FLAG_VISIBLE);
            }
        } catch (Throwable t) {
            Log.e("fullscreen", "setImmersiveMode failed", t);
        }
    }

    // 诊断悬浮层：默认关闭，在设置-其他设置里开启（重启生效）
    private void startDiagOverlay() {
        if (diagTextView != null) {
            return;
        }
        try {
            ViewGroup root = findViewById(android.R.id.content);
            if (root == null) {
                return;
            }
            diagTextView = new TextView(this);
            diagTextView.setTextSize(11);
            diagTextView.setTextColor(0xFFFFFFFF);
            diagTextView.setBackgroundColor(0x88000000);
            diagTextView.setPadding(16, 10, 16, 10);
            FrameLayout.LayoutParams lp = new FrameLayout.LayoutParams(
                    ViewGroup.LayoutParams.WRAP_CONTENT, ViewGroup.LayoutParams.WRAP_CONTENT,
                    Gravity.TOP | Gravity.END);
            lp.setMargins(16, 16, 16, 16);
            root.addView(diagTextView, lp);
            handler.post(diagTask);
        } catch (Throwable t) {
            Log.e("diag", "startDiagOverlay failed", t);
        }
    }

    private void stopDiagOverlay() {
        handler.removeCallbacks(diagTask);
    }

    private final Runnable diagTask = new Runnable() {
        @Override
        public void run() {
            updateDiagOverlay();
            handler.postDelayed(this, 2000);
        }
    };

    private void updateDiagOverlay() {
        if (diagTextView == null) {
            return;
        }
        WebView webView = getCurrentWebview();
        if (webView == null) {
            return;
        }
        webView.evaluateJavascript(DIAG_JS, value -> {
            if (diagTextView == null) {
                return;
            }
            StringBuilder sb = new StringBuilder();
            try {
                sb.append("ch#").append(loadGeneration).append(' ')
                        .append(channelNames[currentLiveIndex]).append('\n');
            } catch (Throwable ignored) {
            }
            sb.append("webview ").append(webViewVersionText).append('\n');
            String diag = value == null ? "null" : value;
            if (diag.length() > 160) {
                diag = diag.substring(0, 160);
            }
            sb.append(diag).append('\n');
            int shown = 0;
            for (String line : eventLog) {
                sb.append(line).append('\n');
                if (++shown >= 6) {
                    break;
                }
            }
            diagTextView.setText(sb.toString());
        });
    }

    private void addEvent(String event) {
        try {
            String line = new java.text.SimpleDateFormat("HH:mm:ss", java.util.Locale.US)
                    .format(new java.util.Date()) + " " + event;
            eventLog.addFirst(line);
            while (eventLog.size() > 30) {
                eventLog.removeLast();
            }
        } catch (Throwable ignored) {
        }
    }

    private static String shortUrl(String url) {
        try {
            java.net.URL parsed = new java.net.URL(url);
            String path = parsed.getPath();
            return path == null || path.isEmpty() ? url : path;
        } catch (Throwable t) {
            return url == null ? "" : (url.length() > 60 ? url.substring(0, 60) : url);
        }
    }

    private void showOverlay(String channelInfo) {
        // 设置覆盖层内容
        overlayTextView.setText(channelInfo);

        findViewById(R.id.overlayTextView).setVisibility(View.VISIBLE);

        // 使用 Handler 延时隐藏覆盖层
        new Handler().postDelayed(() -> {
            findViewById(R.id.overlayTextView).setVisibility(View.GONE);
        }, 8000);
    }

    @Override
    protected void onDestroy() {
        // 清理所有延迟任务（延迟 reload、诊断刷新等）
        handler.removeCallbacksAndMessages(null);
        stopDiagOverlay();
        diagTextView = null;
        customFullscreenView = null;
        customFullscreenCallback = null;
        // 在销毁活动时，释放 WebView 资源
        if (webView0 != null) {
            webView0.destroy();
        }
        if (webView1 != null) {
            webView1.destroy();
        }
        super.onDestroy();
    }

    private boolean checkPermission() {
        return ContextCompat.checkSelfPermission(this,
                Manifest.permission.WRITE_EXTERNAL_STORAGE) == PackageManager.PERMISSION_GRANTED;
    }

    private void requestPermission() {
        if (!checkPermission()) {
            ActivityCompat.requestPermissions(this, new String[] { Manifest.permission.WRITE_EXTERNAL_STORAGE , Manifest.permission.READ_EXTERNAL_STORAGE}, 1);
        }
    }
}
