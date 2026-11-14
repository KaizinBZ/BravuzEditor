package br.com.bravuz.editor;

import android.app.Activity;
import android.graphics.Color;
import android.net.Uri;
import android.os.Bundle;
import android.view.Gravity;
import android.webkit.JsPromptResult;
import android.webkit.JsResult;
import android.webkit.WebChromeClient;
import android.webkit.WebResourceRequest;
import android.webkit.WebResourceResponse;
import android.webkit.WebSettings;
import android.webkit.WebView;
import android.webkit.WebViewClient;
import android.widget.EditText;
import android.widget.LinearLayout;
import android.widget.TextView;
import java.io.ByteArrayInputStream;
import java.io.File;
import java.io.FileInputStream;
import java.io.IOException;
import java.io.InputStream;
import java.util.Locale;

/** Isolated local HTML preview: scripts and controls work, network and external navigation do not. */
public final class HtmlPreviewActivity extends Activity {
    static final String EXTRA_FILE = "br.com.bravuz.editor.HTML_FILE";
    private File root;
    private String virtualHost;
    private WebView web;
    private int dp(float v){return (int)(v*getResources().getDisplayMetrics().density+.5f);}

    @Override protected void onCreate(Bundle state){
        super.onCreate(state);getWindow().setStatusBarColor(Color.rgb(20,20,23));getWindow().setNavigationBarColor(Color.BLACK);
        String path=getIntent().getStringExtra(EXTRA_FILE);
        if(path==null){finish();return;}
        try{
            File html=new File(path).getCanonicalFile();
            root=new File(getCacheDir(),"bravuz-html-preview").getCanonicalFile();
            if(!html.getPath().startsWith(root.getPath()+File.separator)||!html.isFile()||!html.getName().toLowerCase(Locale.ROOT).endsWith(".html")&&!html.getName().toLowerCase(Locale.ROOT).endsWith(".htm"))throw new IOException("Arquivo HTML fora da área de pré-visualização.");
            show(html);
        }catch(Exception e){new android.app.AlertDialog.Builder(this).setTitle("Prévia indisponível").setMessage(e.getMessage()).setPositiveButton("Fechar",(d,w)->finish()).show();}
    }
    private void show(File html){
        if(android.os.Build.VERSION.SDK_INT<21){new android.app.AlertDialog.Builder(this).setTitle("Android não compatível").setMessage("A prévia HTML segura requer Android 5 ou superior.").setPositiveButton("Fechar",(d,w)->finish()).show();return;}
        LinearLayout layout=new LinearLayout(this);layout.setOrientation(LinearLayout.VERTICAL);layout.setBackgroundColor(Color.WHITE);
        LinearLayout bar=new LinearLayout(this);bar.setGravity(Gravity.CENTER_VERTICAL);bar.setPadding(dp(14),dp(8),dp(14),dp(8));bar.setBackgroundColor(Color.rgb(27,27,31));layout.addView(bar,new LinearLayout.LayoutParams(-1,dp(52)));
        TextView title=new TextView(this);title.setText("Pré-visualização HTML • "+html.getName());title.setTextColor(Color.WHITE);title.setTextSize(14);title.setSingleLine(true);title.setEllipsize(android.text.TextUtils.TruncateAt.END);bar.addView(title,new LinearLayout.LayoutParams(0,-2,1));
        TextView close=new TextView(this);close.setText("Fechar");close.setTextColor(Color.WHITE);close.setTextSize(14);close.setGravity(Gravity.CENTER);bar.addView(close,new LinearLayout.LayoutParams(dp(64),-1));close.setOnClickListener(v->finish());
        web=new WebView(this);layout.addView(web,new LinearLayout.LayoutParams(-1,0,1));
        WebSettings settings=web.getSettings();settings.setJavaScriptEnabled(true);settings.setDomStorageEnabled(true);settings.setAllowFileAccess(false);settings.setAllowContentAccess(false);settings.setAllowFileAccessFromFileURLs(false);settings.setAllowUniversalAccessFromFileURLs(false);settings.setJavaScriptCanOpenWindowsAutomatically(false);settings.setSupportMultipleWindows(false);settings.setMixedContentMode(WebSettings.MIXED_CONTENT_NEVER_ALLOW);settings.setGeolocationEnabled(false);settings.setDatabaseEnabled(false);settings.setLoadsImagesAutomatically(true);settings.setBlockNetworkLoads(true);
        web.setWebChromeClient(new WebChromeClient(){
            @Override public boolean onJsAlert(WebView view,String url,String message,JsResult result){new android.app.AlertDialog.Builder(HtmlPreviewActivity.this).setMessage(message).setPositiveButton("OK",(d,w)->result.confirm()).setOnCancelListener(d->result.cancel()).show();return true;}
            @Override public boolean onJsConfirm(WebView view,String url,String message,JsResult result){new android.app.AlertDialog.Builder(HtmlPreviewActivity.this).setMessage(message).setPositiveButton("OK",(d,w)->result.confirm()).setNegativeButton("Cancelar",(d,w)->result.cancel()).setOnCancelListener(d->result.cancel()).show();return true;}
            @Override public boolean onJsPrompt(WebView view,String url,String message,String defaultValue,JsPromptResult result){EditText input=new EditText(HtmlPreviewActivity.this);input.setSingleLine(true);input.setText(defaultValue);new android.app.AlertDialog.Builder(HtmlPreviewActivity.this).setTitle(message).setView(input).setPositiveButton("OK",(d,w)->result.confirm(input.getText().toString())).setNegativeButton("Cancelar",(d,w)->result.cancel()).setOnCancelListener(d->result.cancel()).show();return true;}
        });
        virtualHost="bravuz-preview.local";
        web.setWebViewClient(new WebViewClient(){
            @Override public boolean shouldOverrideUrlLoading(WebView view,WebResourceRequest request){return !isLocalAllowed(request.getUrl());}
            @Override public boolean shouldOverrideUrlLoading(WebView view,String url){try{return !isLocalAllowed(Uri.parse(url));}catch(Exception e){return true;}}
            @Override public WebResourceResponse shouldInterceptRequest(WebView view,WebResourceRequest request){return intercept(request.getUrl());}
            @Override public WebResourceResponse shouldInterceptRequest(WebView view,String url){try{return intercept(Uri.parse(url));}catch(Exception e){return blocked();}}
            private WebResourceResponse intercept(Uri uri){
                if("data".equalsIgnoreCase(uri.getScheme())||"blob".equalsIgnoreCase(uri.getScheme())||"about".equalsIgnoreCase(uri.getScheme()))return null;
                File file=resolve(uri);if(file==null)return blocked();
                String mime=mime(file.getName());String encoding=mime.startsWith("text/")||mime.contains("javascript")||mime.contains("json")?"UTF-8":null;
                try{return new WebResourceResponse(mime,encoding,new FileInputStream(file));}catch(Exception e){return blocked();}
            }
            private File resolve(Uri uri){if(!isLocalAllowed(uri))return null;try{
                if("https".equalsIgnoreCase(uri.getScheme())){String p=uri.getEncodedPath();if(p==null)return null;p=Uri.decode(p);while(p.startsWith("/"))p=p.substring(1);if(p.isEmpty())p="index.html";File f=new File(root,p).getCanonicalFile();return f.getPath().startsWith(root.getPath()+File.separator)&&f.isFile()?f:null;}
                String path=uri.getPath();if(path==null)return null;File f=new File(path).getCanonicalFile();return f.getPath().startsWith(root.getPath()+File.separator)&&f.isFile()?f:null;
            }catch(Exception e){return null;}}
            private boolean isLocalAllowed(Uri uri){String scheme=uri.getScheme();if(!virtualHost.equalsIgnoreCase(uri.getHost()))return false;return "https".equalsIgnoreCase(scheme)||"http".equalsIgnoreCase(scheme);}
            private String mime(String name){String n=name.toLowerCase(Locale.ROOT);if(n.endsWith(".html")||n.endsWith(".htm"))return "text/html";if(n.endsWith(".css"))return "text/css";if(n.endsWith(".js"))return "application/javascript";if(n.endsWith(".json"))return "application/json";if(n.endsWith(".svg"))return "image/svg+xml";if(n.endsWith(".png"))return "image/png";if(n.endsWith(".jpg")||n.endsWith(".jpeg"))return "image/jpeg";if(n.endsWith(".gif"))return "image/gif";if(n.endsWith(".webp"))return "image/webp";if(n.endsWith(".woff2"))return "font/woff2";if(n.endsWith(".woff"))return "font/woff";if(n.endsWith(".ttf"))return "font/ttf";return "application/octet-stream";}
            private WebResourceResponse blocked(){java.util.Map<String,String> headers=new java.util.HashMap<>();headers.put("Content-Length","0");return new WebResourceResponse("text/plain","UTF-8",403,"Blocked",headers,new ByteArrayInputStream(new byte[0]));}
        });
        setContentView(layout);
        web.loadUrl("https://"+virtualHost+"/"+Uri.encode(html.getName()));
    }
    @Override public void onBackPressed(){if(web!=null&&web.canGoBack())web.goBack();else super.onBackPressed();}
    @Override protected void onDestroy(){if(web!=null){web.stopLoading();web.loadUrl("about:blank");web.clearHistory();web.removeAllViews();web.destroy();web=null;}super.onDestroy();}
}
