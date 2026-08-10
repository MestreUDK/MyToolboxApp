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
import android.net.Uri;
import android.os.Build;
import android.os.Bundle;
import android.os.Environment;
import android.provider.MediaStore;
import android.view.Gravity;
import android.view.View;
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
    private static final String HOME_URL = "https://appassets.androidplatform.net/assets/index.html";
    private static final String ASSET_HOST = "appassets.androidplatform.net";
    private static final int FILE_CHOOSER_REQUEST = 1001;
    private static final int NOTIFICATION_PERMISSION_REQUEST = 1002;
    private static final String CHANNEL_ID = "anime_toolbox_agenda";

    private WebView webView;
    private TextView titleView;
    private ValueCallback<Uri[]> filePathCallback;
    private String pendingNotificationTitle;
    private String pendingNotificationBody;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        getWindow().setStatusBarColor(Color.rgb(79, 55, 139));
        getWindow().setNavigationBarColor(Color.rgb(23, 21, 31));
        createNotificationChannel();
        buildUi();
        configureWebView();
        webView.loadUrl(HOME_URL);
    }

    private int dp(int value) {
        return Math.round(value * getResources().getDisplayMetrics().density);
    }

    private void buildUi() {
        LinearLayout root = new LinearLayout(this);
        root.setOrientation(LinearLayout.VERTICAL);
        root.setBackgroundColor(Color.rgb(245, 243, 251));

        LinearLayout toolbar = new LinearLayout(this);
        toolbar.setOrientation(LinearLayout.HORIZONTAL);
        toolbar.setGravity(Gravity.CENTER_VERTICAL);
        toolbar.setPadding(dp(4), 0, dp(8), 0);
        toolbar.setBackgroundColor(Color.rgb(79, 55, 139));
        root.addView(toolbar, new LinearLayout.LayoutParams(LinearLayout.LayoutParams.MATCH_PARENT, dp(54)));

        Button back = toolbarButton("‹");
        back.setContentDescription("Voltar");
        back.setOnClickListener(v -> goBack());
        toolbar.addView(back, new LinearLayout.LayoutParams(dp(52), LinearLayout.LayoutParams.MATCH_PARENT));

        Button home = toolbarButton("⌂");
        home.setContentDescription("Início");
        home.setOnClickListener(v -> webView.loadUrl(HOME_URL));
        toolbar.addView(home, new LinearLayout.LayoutParams(dp(52), LinearLayout.LayoutParams.MATCH_PARENT));

        titleView = new TextView(this);
        titleView.setText("Anime Toolbox");
        titleView.setTextColor(Color.WHITE);
        titleView.setTextSize(17);
        titleView.setGravity(Gravity.CENTER_VERTICAL);
        titleView.setSingleLine(true);
        titleView.setPadding(dp(8), 0, 0, 0);
        toolbar.addView(titleView, new LinearLayout.LayoutParams(0, LinearLayout.LayoutParams.MATCH_PARENT, 1));

        webView = new WebView(this);
        root.addView(webView, new LinearLayout.LayoutParams(LinearLayout.LayoutParams.MATCH_PARENT, 0, 1));
        setContentView(root);
    }

    private Button toolbarButton(String text) {
        Button button = new Button(this);
        button.setText(text);
        button.setTextColor(Color.WHITE);
        button.setTextSize(26);
        button.setGravity(Gravity.CENTER);
        button.setBackgroundColor(Color.TRANSPARENT);
        button.setAllCaps(false);
        button.setPadding(0, 0, 0, 0);
        return button;
    }

    private void configureWebView() {
        WebSettings settings = webView.getSettings();
        settings.setJavaScriptEnabled(true);
        settings.setDomStorageEnabled(true);
        settings.setDatabaseEnabled(true);
        settings.setAllowFileAccess(false);
        settings.setAllowContentAccess(true);
        settings.setJavaScriptCanOpenWindowsAutomatically(false);
        settings.setMediaPlaybackRequiresUserGesture(true);
        settings.setBuiltInZoomControls(false);
        settings.setDisplayZoomControls(false);
        settings.setLoadWithOverviewMode(true);
        settings.setUseWideViewPort(true);

        webView.addJavascriptInterface(new AndroidBridge(this), "Android");
        webView.setWebViewClient(new LocalWebViewClient());
        webView.setWebChromeClient(new WebChromeClient() {
            @Override
            public boolean onShowFileChooser(WebView view, ValueCallback<Uri[]> callback, FileChooserParams params) {
                if (filePathCallback != null) filePathCallback.onReceiveValue(null);
                filePathCallback = callback;
                Intent intent;
                try {
                    intent = params.createIntent();
                } catch (Exception e) {
                    intent = new Intent(Intent.ACTION_OPEN_DOCUMENT);
                    intent.addCategory(Intent.CATEGORY_OPENABLE);
                    intent.setType("*/*");
                }
                try {
                    startActivityForResult(Intent.createChooser(intent, "Selecionar arquivo"), FILE_CHOOSER_REQUEST);
                    return true;
                } catch (Exception e) {
                    filePathCallback = null;
                    Toast.makeText(MainActivity.this, "Não foi possível abrir o seletor de arquivos.", Toast.LENGTH_SHORT).show();
                    return false;
                }
            }
        });

        webView.setDownloadListener((url, userAgent, contentDisposition, mimeType, contentLength) -> {
            try {
                Intent intent = new Intent(Intent.ACTION_VIEW, Uri.parse(url));
                startActivity(intent);
            } catch (Exception e) {
                Toast.makeText(this, "Download externo não pôde ser aberto.", Toast.LENGTH_SHORT).show();
            }
        });
    }

    private class LocalWebViewClient extends WebViewClient {
        @Override
        public WebResourceResponse shouldInterceptRequest(WebView view, WebResourceRequest request) {
            Uri uri = request.getUrl();
            if ("https".equalsIgnoreCase(uri.getScheme()) && ASSET_HOST.equalsIgnoreCase(uri.getHost())) {
                String path = uri.getPath();
                if (path != null && path.startsWith("/assets/")) {
                    String assetPath = Uri.decode(path.substring("/assets/".length()));
                    if (assetPath.contains("..")) return null;
                    try {
                        InputStream input = getAssets().open(assetPath);
                        String mime = guessMimeType(assetPath);
                        String encoding = isTextMime(mime) ? "UTF-8" : null;
                        return new WebResourceResponse(mime, encoding, input);
                    } catch (Exception ignored) {
                        return new WebResourceResponse("text/plain", "UTF-8", 404, "Not Found", null,
                                new ByteArrayInputStream("Not found".getBytes(StandardCharsets.UTF_8)));
                    }
                }
            }
            return super.shouldInterceptRequest(view, request);
        }

        @Override
        public boolean shouldOverrideUrlLoading(WebView view, WebResourceRequest request) {
            Uri uri = request.getUrl();
            if (ASSET_HOST.equalsIgnoreCase(uri.getHost())) return false;
            String scheme = uri.getScheme();
            if ("http".equalsIgnoreCase(scheme) || "https".equalsIgnoreCase(scheme) ||
                    "mailto".equalsIgnoreCase(scheme) || "tg".equalsIgnoreCase(scheme)) {
                try {
                    startActivity(new Intent(Intent.ACTION_VIEW, uri));
                } catch (Exception e) {
                    Toast.makeText(MainActivity.this, "Não foi possível abrir esse link.", Toast.LENGTH_SHORT).show();
                }
                return true;
            }
            return false;
        }

        @Override
        public void onPageFinished(WebView view, String url) {
            super.onPageFinished(view, url);
            String title = view.getTitle();
            titleView.setText(title == null || title.trim().isEmpty() ? "Anime Toolbox" : title);
        }
    }

    private String guessMimeType(String assetPath) {
        String ext = MimeTypeMap.getFileExtensionFromUrl(assetPath);
        String mime = MimeTypeMap.getSingleton().getMimeTypeFromExtension(ext == null ? "" : ext.toLowerCase());
        if (mime != null) return mime;
        if (assetPath.endsWith(".js")) return "application/javascript";
        if (assetPath.endsWith(".json")) return "application/json";
        if (assetPath.endsWith(".css")) return "text/css";
        if (assetPath.endsWith(".html")) return "text/html";
        return URLConnection.guessContentTypeFromName(assetPath) != null ? URLConnection.guessContentTypeFromName(assetPath) : "application/octet-stream";
    }

    private boolean isTextMime(String mime) {
        return mime != null && (mime.startsWith("text/") || mime.contains("javascript") || mime.contains("json") || mime.contains("xml"));
    }

    private void goBack() {
        if (webView.canGoBack()) webView.goBack();
        else webView.loadUrl(HOME_URL);
    }

    @Override
    public void onBackPressed() {
        if (webView != null && webView.canGoBack()) webView.goBack();
        else super.onBackPressed();
    }

    @Override
    protected void onActivityResult(int requestCode, int resultCode, Intent data) {
        super.onActivityResult(requestCode, resultCode, data);
        if (requestCode == FILE_CHOOSER_REQUEST && filePathCallback != null) {
            Uri[] result = WebChromeClient.FileChooserParams.parseResult(resultCode, data);
            filePathCallback.onReceiveValue(result);
            filePathCallback = null;
        }
    }

    private void createNotificationChannel() {
        if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.O) {
            NotificationChannel channel = new NotificationChannel(
                    CHANNEL_ID, "Agenda de animes", NotificationManager.IMPORTANCE_DEFAULT);
            channel.setDescription("Lembretes e listas da Agenda Semanal de Animes");
            NotificationManager manager = getSystemService(NotificationManager.class);
            manager.createNotificationChannel(channel);
        }
    }

    private void requestNotificationPermissionNative() {
        if (Build.VERSION.SDK_INT >= 33 && checkSelfPermission(Manifest.permission.POST_NOTIFICATIONS) != PackageManager.PERMISSION_GRANTED) {
            requestPermissions(new String[]{Manifest.permission.POST_NOTIFICATIONS}, NOTIFICATION_PERMISSION_REQUEST);
        }
    }

    private void showNotificationNative(String title, String body) {
        if (Build.VERSION.SDK_INT >= 33 && checkSelfPermission(Manifest.permission.POST_NOTIFICATIONS) != PackageManager.PERMISSION_GRANTED) {
            pendingNotificationTitle = title;
            pendingNotificationBody = body;
            requestNotificationPermissionNative();
            return;
        }
        Notification.Builder builder = Build.VERSION.SDK_INT >= Build.VERSION_CODES.O
                ? new Notification.Builder(this, CHANNEL_ID)
                : new Notification.Builder(this);
        Notification notification = builder
                .setSmallIcon(R.drawable.ic_notification)
                .setContentTitle(title == null || title.isEmpty() ? "Anime Toolbox" : title)
                .setContentText(body == null ? "" : body)
                .setStyle(new Notification.BigTextStyle().bigText(body == null ? "" : body))
                .setAutoCancel(true)
                .build();
        NotificationManager manager = (NotificationManager) getSystemService(Context.NOTIFICATION_SERVICE);
        manager.notify((int) (System.currentTimeMillis() & 0x7fffffff), notification);
    }

    @Override
    public void onRequestPermissionsResult(int requestCode, String[] permissions, int[] grantResults) {
        super.onRequestPermissionsResult(requestCode, permissions, grantResults);
        if (requestCode == NOTIFICATION_PERMISSION_REQUEST) {
            if (grantResults.length > 0 && grantResults[0] == PackageManager.PERMISSION_GRANTED && pendingNotificationTitle != null) {
                String t = pendingNotificationTitle;
                String b = pendingNotificationBody;
                pendingNotificationTitle = null;
                pendingNotificationBody = null;
                showNotificationNative(t, b);
            }
        }
    }

    public class AndroidBridge {
        private final Context context;
        AndroidBridge(Context context) { this.context = context; }

        @JavascriptInterface
        public void copyText(String text) {
            runOnUiThread(() -> {
                ClipboardManager clipboard = (ClipboardManager) getSystemService(Context.CLIPBOARD_SERVICE);
                clipboard.setPrimaryClip(ClipData.newPlainText("Anime Toolbox", text));
                Toast.makeText(context, "Copiado.", Toast.LENGTH_SHORT).show();
            });
        }

        @JavascriptInterface
        public void shareText(String title, String text) {
            runOnUiThread(() -> {
                Intent send = new Intent(Intent.ACTION_SEND);
                send.setType("text/plain");
                send.putExtra(Intent.EXTRA_TEXT, text);
                startActivity(Intent.createChooser(send, title == null || title.isEmpty() ? "Compartilhar" : title));
            });
        }

        @JavascriptInterface
        public void saveTextFile(String filename, String text, String mimeType) {
            byte[] bytes = (text == null ? "" : text).getBytes(StandardCharsets.UTF_8);
            runOnUiThread(() -> saveBytesToDownloads(filename, bytes, mimeType));
        }

        @JavascriptInterface
        public void requestNotificationPermission() {
            runOnUiThread(MainActivity.this::requestNotificationPermissionNative);
        }

        @JavascriptInterface
        public void showNotification(String title, String body) {
            runOnUiThread(() -> showNotificationNative(title, body));
        }
    }

    private String safeFilename(String filename) {
        String f = filename == null || filename.trim().isEmpty() ? "anime_toolbox.txt" : filename.trim();
        return f.replaceAll("[\\\\/:*?\"<>|]", "_");
    }

    private void saveBytesToDownloads(String filename, byte[] data, String mimeType) {
        String safeName = safeFilename(filename);
        String mime = (mimeType == null || mimeType.isEmpty()) ? "text/plain" : mimeType.split(";")[0];
        try {
            if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.Q) {
                ContentResolver resolver = getContentResolver();
                ContentValues values = new ContentValues();
                values.put(MediaStore.Downloads.DISPLAY_NAME, safeName);
                values.put(MediaStore.Downloads.MIME_TYPE, mime);
                values.put(MediaStore.Downloads.RELATIVE_PATH, Environment.DIRECTORY_DOWNLOADS + "/Anime Toolbox");
                values.put(MediaStore.Downloads.IS_PENDING, 1);
                Uri uri = resolver.insert(MediaStore.Downloads.EXTERNAL_CONTENT_URI, values);
                if (uri == null) throw new Exception("Falha ao criar arquivo");
                try (OutputStream output = resolver.openOutputStream(uri)) {
                    if (output == null) throw new Exception("Falha ao abrir arquivo");
                    output.write(data);
                }
                values.clear();
                values.put(MediaStore.Downloads.IS_PENDING, 0);
                resolver.update(uri, values, null, null);
            } else {
                File base = getExternalFilesDir(Environment.DIRECTORY_DOWNLOADS);
                if (base == null) throw new Exception("Armazenamento indisponível");
                File folder = new File(base, "Anime Toolbox");
                if (!folder.exists() && !folder.mkdirs()) throw new Exception("Falha ao criar pasta");
                try (FileOutputStream output = new FileOutputStream(new File(folder, safeName))) {
                    output.write(data);
                }
            }
            Toast.makeText(this, "Salvo em Downloads/Anime Toolbox: " + safeName, Toast.LENGTH_LONG).show();
        } catch (Exception e) {
            Toast.makeText(this, "Não foi possível salvar o arquivo.", Toast.LENGTH_LONG).show();
        }
    }

    @Override
    protected void onDestroy() {
        if (webView != null) {
            webView.removeJavascriptInterface("Android");
            webView.destroy();
        }
        super.onDestroy();
    }
}