package com.example.mywebviewtest;

    import android.Manifest;
    import android.app.AlertDialog;
    import android.content.DialogInterface;
    import android.content.pm.PackageManager;
    import android.os.Build;
    import android.os.Bundle;
    import android.view.KeyEvent;
    import android.view.MotionEvent;
    import android.view.View;
    import android.webkit.JavascriptInterface;
    import android.webkit.WebView;
    import android.webkit.WebViewClient;

    import androidx.activity.EdgeToEdge;
    import androidx.appcompat.app.AppCompatActivity;
    import androidx.core.app.ActivityCompat;
    import androidx.core.content.ContextCompat;
    import androidx.core.graphics.Insets;
    import androidx.core.view.ViewCompat;
    import androidx.core.view.WindowInsetsCompat;

    import android.app.NotificationManager;
    import android.app.NotificationChannel;
    import android.content.Context;
    import androidx.core.app.NotificationCompat;
    import com.example.mywebviewtest.R;

    import java.util.Calendar;

    public class MainActivity extends AppCompatActivity {

        private static final int PERMISSION_REQUEST_CODE = 1;
        private static final String CHANNEL_ID = "my_channel";
        private int notificationId = 0;
        @Override
        protected void onCreate(Bundle savedInstanceState) {
            super.onCreate(savedInstanceState);
            EdgeToEdge.enable(this);
            setContentView(R.layout.activity_main);

            // 권한ID를 가져옵니다
            int permission = ContextCompat.checkSelfPermission(this,
                    Manifest.permission.ACCESS_NOTIFICATION_POLICY);

            int permission2 = ContextCompat.checkSelfPermission(this,
                    Manifest.permission.POST_NOTIFICATIONS);

            // 권한이 열려있는지 확인
            if (permission == PackageManager.PERMISSION_DENIED || permission2 == PackageManager.PERMISSION_DENIED) {
                // 마쉬멜로우 이상버전부터 권한을 물어본다
                if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.M) {
                    // 권한 체크(READ_PHONE_STATE의 requestCode를 1000으로 세팅
                    requestPermissions(
                            new String[]{Manifest.permission.ACCESS_NOTIFICATION_POLICY, Manifest.permission.POST_NOTIFICATIONS},
                            1000);
                }
                return;
            }

            // WebView 초기화
            WebView webView = findViewById(R.id.webview);
            webView.getSettings().setJavaScriptEnabled(true); // 자바스크립트 활성화
            webView.loadUrl("https://kr2.nanu.cc/webviewclient/FCM.html"); // 웹뷰에 웹 페이지 로드

            // 외부 링크가 WebView 내부에서 열리도록 WebViewClient 설정
            webView.setWebViewClient(new WebViewClient() {
                @Override
                public boolean shouldOverrideUrlLoading(WebView view, String url) {
                    // 외부 링크를 WebView 내부에서 열도록 설정
                    view.loadUrl(url);
                    return true;
                }

                @Override
                public void onPageFinished(WebView view, String url) {
                    super.onPageFinished(view, url);
                    webView.setOnKeyListener(new View.OnKeyListener() {
                        @Override
                        public boolean onKey(View v, int keyCode, KeyEvent event) {
                            if (keyCode == KeyEvent.KEYCODE_BACK && event.getAction() == MotionEvent.ACTION_UP && webView.canGoBack()) {
                                webView.goBack();
                                return true;
                            }
                            return false;
                        }
                    });
                }
            });

            // Edge-to-Edge 적용
            ViewCompat.setOnApplyWindowInsetsListener(findViewById(R.id.main), (v, insets) -> {
                Insets systemBars = insets.getInsets(WindowInsetsCompat.Type.systemBars());
                v.setPadding(systemBars.left, systemBars.top, systemBars.right, systemBars.bottom);
                return insets;
            });

            // JavaScript Interface 추가
            webView.addJavascriptInterface(new WebAppInterface(), "Android");

            // 알림 권한 요청
            requestNotificationPermission();
        }

        // JavaScript Interface 클래스 정의
        public class WebAppInterface {
            @JavascriptInterface
            public void showAlert(String title, String message) {
                // AlertDialog로 알림 표시
                new AlertDialog.Builder(MainActivity.this)
                        .setTitle(title)
                        .setMessage(message)
                        .setPositiveButton("확인", null)
                        .show();
            }

            @JavascriptInterface
            public void notifyAlert(String title, String message) {
                showNotification(title,message);
            }
        }

        // 알림 권한 요청 메서드
        private void requestNotificationPermission() {
            if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.M) {
                if (ContextCompat.checkSelfPermission(this, Manifest.permission.VIBRATE) != PackageManager.PERMISSION_GRANTED) {
                    ActivityCompat.requestPermissions(this, new String[]{Manifest.permission.VIBRATE}, PERMISSION_REQUEST_CODE);
                }
            }
        }

        // 알림 권한 요청 결과 처리
        @Override
        public void onRequestPermissionsResult(int requestCode, String[] permissions, int[] grantResults) {
            super.onRequestPermissionsResult(requestCode, permissions, grantResults);
            if (requestCode == PERMISSION_REQUEST_CODE) {
                if (grantResults.length > 0 && grantResults[0] == PackageManager.PERMISSION_GRANTED) {
                    // 권한 허용됨
                } else {
                    // 권한 거부됨
                    // 사용자에게 권한이 필요하다는 메시지를 표시할 수 있음
                }
            }
        }

        private void showNotification(String title, String content) {
            NotificationManager notificationManager =
                    (NotificationManager) getSystemService(Context.NOTIFICATION_SERVICE);

            // Notification Channel 생성 (API 26+)
            if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.O) {
                NotificationChannel channel = new NotificationChannel(
                        CHANNEL_ID,
                        "My Channel",
                        NotificationManager.IMPORTANCE_DEFAULT
                );
                notificationManager.createNotificationChannel(channel);
            }

            // 알림 빌더 생성
            NotificationCompat.Builder builder =
                    new NotificationCompat.Builder(this, CHANNEL_ID)
                            .setSmallIcon(R.mipmap.ic_notification)
                            .setContentTitle(title)
                            .setContentText(content)
                            .setPriority(NotificationCompat.PRIORITY_DEFAULT);

            // 알림 표시
            notificationManager.notify(getUniqueNotificationId(), builder.build());
        }
        private int getUniqueNotificationId() {
            return (int) Calendar.getInstance().getTimeInMillis();
        }
    }
