package com.animetoolbox.app;

import android.Manifest;
import android.app.Activity;
import android.app.Notification;
import android.app.NotificationChannel;
import android.app.NotificationManager;
import android.content.ClipData;
import android.content.ClipboardManager;
import android.content.ContentResolver;
import android.content.ContentValues;
import android.content.Context;
import android.content.Intent;
import android.content.pm.PackageManager;
import android.graphics.Color;
import android.graphics.Insets;
import android.net.Uri;
import android.os.Build;
import android.os.Bundle;
import android.os.Environment;
import android.provider.MediaStore;
import android.text.TextUtils;
import android.view.Gravity;
import android.view.View;
import android.view.WindowInsets;
import android.view.WindowInsetsController;
import android.webkit.JavascriptInterface;
import android.webkit.MimeTypeMap;
import android.webkit.ValueCallback;
import android.webkit.WebChromeClient;
import android.webkit.WebResourceRequest;
import android.webkit.WebResourceResponse;
import android.webkit.WebSettings;
import android.webkit.WebView;
import android.webkit.WebViewClient;
import android.widget.Button;
import android.widget.LinearLayout;
import android.widget.TextView;
import android.widget.Toast;

import java.io.ByteArrayInputStream;
import java.io.File;
import java.io.FileOutputStream;
import java.io.InputStream;
import java.io.OutputStream;
import java.net.URLConnection;
import java.nio.charset.StandardCharsets;

public class MainActivity extends Activity {

    private static final String HOME_URL =
            "https://appassets.androidplatform.net/assets/index.html";

    private static final String ASSET_HOST =
            "appassets.androidplatform.net";

    private static final int FILE_CHOOSER_REQUEST = 1001;
    private static final int NOTIFICATION_PERMISSION_REQUEST = 1002;

    private static final String CHANNEL_ID =
            "anime_toolbox_agenda";

    private static final int TOOLBAR_COLOR =
            Color.rgb(79, 55, 139);

    private static final int NAVIGATION_COLOR =
            Color.rgb(23, 21, 31);

    private static final int APP_BACKGROUND_COLOR =
            Color.rgb(245, 243, 251);

    private WebView webView;
    private TextView titleView;

    private ValueCallback<Uri[]> filePathCallback;

    private String pendingNotificationTitle;
    private String pendingNotificationBody;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);

        configureSystemBars();
        createNotificationChannel();

        buildUi();
        configureWebView();

        webView.loadUrl(HOME_URL);
    }

    /**
     * Configura as cores e a aparência das barras do sistema.
     *
     * No Android 15, a janela passa a trabalhar em edge-to-edge.
     * Os espaçadores criados em buildUi() cuidam para que a interface
     * do aplicativo não fique por baixo das barras do sistema.
     */
    private void configureSystemBars() {

        getWindow().setStatusBarColor(TOOLBAR_COLOR);
        getWindow().setNavigationBarColor(NAVIGATION_COLOR);

        /*
         * Mantém os ícones da barra de status e da barra de navegação
         * claros, pois utilizamos fundos escuros nessas regiões.
         */
        if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.R) {

            WindowInsetsController controller =
                    getWindow().getInsetsController();

            if (controller != null) {

                controller.setSystemBarsAppearance(
                        0,
                        WindowInsetsController.APPEARANCE_LIGHT_STATUS_BARS
                                | WindowInsetsController.APPEARANCE_LIGHT_NAVIGATION_BARS
                );
            }
        }
    }

    /**
     * Converte dp para pixels.
     */
    private int dp(int value) {
        return Math.round(
                value * getResources().getDisplayMetrics().density
        );
    }

    /**
     * Monta a interface nativa principal:
     *
     * ┌─────────────────────────┐
     * │ Barra de status Android │
     * ├─────────────────────────┤
     * │ ‹  ⌂  Anime Toolbox     │
     * ├─────────────────────────┤
     * │                         │
     * │       WebView           │
     * │                         │
     * ├─────────────────────────┤
     * │ Navegação Android       │
     * └─────────────────────────┘
     *
     * No Android 15+, os espaçadores recebem automaticamente
     * o tamanho correto das barras do sistema.
     */
    private void buildUi() {

        LinearLayout root = new LinearLayout(this);
        root.setOrientation(LinearLayout.VERTICAL);
        root.setBackgroundColor(APP_BACKGROUND_COLOR);

        /*
         * Espaçador superior.
         *
         * No Android 15 ele ocupará exatamente a área da barra
         * de notificações/status.
         */
        View statusBarSpacer = new View(this);
        statusBarSpacer.setBackgroundColor(TOOLBAR_COLOR);

        root.addView(
                statusBarSpacer,
                new LinearLayout.LayoutParams(
                        LinearLayout.LayoutParams.MATCH_PARENT,
                        0
                )
        );

        /*
         * Toolbar do aplicativo.
         */
        LinearLayout toolbar = new LinearLayout(this);

        toolbar.setOrientation(LinearLayout.HORIZONTAL);
        toolbar.setGravity(Gravity.CENTER_VERTICAL);

        toolbar.setPadding(
                dp(2),
                0,
                dp(6),
                0
        );

        toolbar.setBackgroundColor(TOOLBAR_COLOR);

        root.addView(
                toolbar,
                new LinearLayout.LayoutParams(
                        LinearLayout.LayoutParams.MATCH_PARENT,
                        dp(50)
                )
        );

        /*
         * Botão VOLTAR.
         */
        Button back = toolbarButton("‹");

        back.setContentDescription("Voltar");

        back.setOnClickListener(v -> goBack());

        toolbar.addView(
                back,
                new LinearLayout.LayoutParams(
                        dp(44),
                        LinearLayout.LayoutParams.MATCH_PARENT
                )
        );

        /*
         * Botão HOME.
         */
        Button home = toolbarButton("⌂");

        home.setContentDescription("Início");

        home.setOnClickListener(
                v -> webView.loadUrl(HOME_URL)
        );

        toolbar.addView(
                home,
                new LinearLayout.LayoutParams(
                        dp(44),
                        LinearLayout.LayoutParams.MATCH_PARENT
                )
        );

        /*
         * Título da ferramenta atual.
         */
        titleView = new TextView(this);

        titleView.setText("Anime Toolbox");
        titleView.setTextColor(Color.WHITE);

        titleView.setTextSize(15.5f);

        titleView.setGravity(Gravity.CENTER_VERTICAL);

        titleView.setSingleLine(true);

        titleView.setEllipsize(
                TextUtils.TruncateAt.END
        );

        titleView.setPadding(
                dp(6),
                0,
                0,
                0
        );

        toolbar.addView(
                titleView,
                new LinearLayout.LayoutParams(
                        0,
                        LinearLayout.LayoutParams.MATCH_PARENT,
                        1
                )
        );

        /*
         * WebView principal.
         */
        webView = new WebView(this);

        root.addView(
                webView,
                new LinearLayout.LayoutParams(
                        LinearLayout.LayoutParams.MATCH_PARENT,
                        0,
                        1
                )
        );

        /*
         * Espaçador inferior.
         *
         * Evita que o conteúdo do aplicativo termine por baixo
         * da barra de navegação/gestos do Android.
         */
        View navigationBarSpacer = new View(this);

        navigationBarSpacer.setBackgroundColor(
                NAVIGATION_COLOR
        );

        root.addView(
                navigationBarSpacer,
                new LinearLayout.LayoutParams(
                        LinearLayout.LayoutParams.MATCH_PARENT,
                        