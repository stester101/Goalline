package com.goalline.tracker;
import android.app.Activity;
import android.app.AlertDialog;
import android.os.Bundle;
import android.os.Build;
import android.webkit.*;
import android.view.*;
import android.widget.FrameLayout;
import android.widget.Toast;
import android.content.Intent;
import android.net.Uri;
import android.graphics.Color;
import java.io.*;
import java.nio.charset.StandardCharsets;

public class MainActivity extends Activity {
 private WebView web;
 private ValueCallback<Uri[]> chooser;
 private String pendingExport;
 private static final String ORIGIN="https://appassets.androidplatform.net";
 @Override public void onCreate(Bundle state) {
  super.onCreate(state);
  getWindow().setStatusBarColor(Color.rgb(238,245,241));
  getWindow().setNavigationBarColor(Color.rgb(238,245,241));
  FrameLayout root=new FrameLayout(this); root.setBackgroundColor(Color.rgb(238,245,241)); setContentView(root);
  if(Build.VERSION.SDK_INT>=30){
   getWindow().setDecorFitsSystemWindows(false);
   root.setOnApplyWindowInsetsListener((v,i)->{android.graphics.Insets b=i.getInsets(WindowInsets.Type.systemBars()|WindowInsets.Type.displayCutout()|WindowInsets.Type.ime());v.setPadding(b.left,b.top,b.right,b.bottom);return WindowInsets.CONSUMED;});
   getWindow().getInsetsController().setSystemBarsAppearance(WindowInsetsController.APPEARANCE_LIGHT_STATUS_BARS|WindowInsetsController.APPEARANCE_LIGHT_NAVIGATION_BARS,WindowInsetsController.APPEARANCE_LIGHT_STATUS_BARS|WindowInsetsController.APPEARANCE_LIGHT_NAVIGATION_BARS);
  }
  web=new WebView(this); root.addView(web,new FrameLayout.LayoutParams(-1,-1));
  WebSettings s=web.getSettings();s.setJavaScriptEnabled(true);s.setDomStorageEnabled(true);s.setAllowFileAccess(false);s.setAllowContentAccess(false);s.setMixedContentMode(WebSettings.MIXED_CONTENT_NEVER_ALLOW);web.setBackgroundColor(Color.rgb(238,245,241));
  web.addJavascriptInterface(new ExportBridge(),"GoallineAndroid");
  web.setWebViewClient(new WebViewClient(){
   @Override public WebResourceResponse shouldInterceptRequest(WebView v,WebResourceRequest r){
    Uri u=r.getUrl(); if(!"https".equals(u.getScheme())||!"appassets.androidplatform.net".equals(u.getHost()))return null;
    String p=u.getPath();if(p==null||p.equals("/"))p="/index.html";
    if(p.contains(".."))return new WebResourceResponse("text/plain","UTF-8",new ByteArrayInputStream(new byte[0]));
    String mime=p.endsWith(".js")?"text/javascript":p.endsWith(".css")?"text/css":p.endsWith(".svg")?"image/svg+xml":p.endsWith(".webmanifest")?"application/manifest+json":"text/html";
    try{return new WebResourceResponse(mime,"UTF-8",getAssets().open("www"+p));}catch(IOException ex){return new WebResourceResponse("text/plain","UTF-8",404,"Not found",null,new ByteArrayInputStream(new byte[0]));}
   }
   @Override public boolean shouldOverrideUrlLoading(WebView v,WebResourceRequest r){Uri u=r.getUrl();if("https".equals(u.getScheme())&&"appassets.androidplatform.net".equals(u.getHost()))return false;if("https".equals(u.getScheme()))try{startActivity(new Intent(Intent.ACTION_VIEW,u));}catch(Exception ignored){}return true;}
  });
  web.setWebChromeClient(new WebChromeClient(){
   @Override public boolean onJsConfirm(WebView v,String url,String message,JsResult result){new AlertDialog.Builder(MainActivity.this).setMessage(message).setPositiveButton("Delete",(d,w)->result.confirm()).setNegativeButton("Cancel",(d,w)->result.cancel()).setOnCancelListener(d->result.cancel()).show();return true;}
   @Override public boolean onShowFileChooser(WebView v,ValueCallback<Uri[]> callback,FileChooserParams params){if(chooser!=null)chooser.onReceiveValue(null);chooser=callback;Intent i=new Intent(Intent.ACTION_OPEN_DOCUMENT);i.addCategory(Intent.CATEGORY_OPENABLE);i.setType("application/json");try{startActivityForResult(i,12);}catch(Exception e){chooser.onReceiveValue(null);chooser=null;}return true;}
  });
  web.loadUrl(ORIGIN+"/index.html");
 }
 public class ExportBridge {
  @JavascriptInterface public void exportBets(String data){if(data==null||data.length()>2000000)return;runOnUiThread(()->{pendingExport=data;Intent i=new Intent(Intent.ACTION_CREATE_DOCUMENT);i.addCategory(Intent.CATEGORY_OPENABLE);i.setType("application/json");i.putExtra(Intent.EXTRA_TITLE,"goalline-bets.json");startActivityForResult(i,13);});}
 }
 @Override protected void onActivityResult(int request,int result,Intent data){super.onActivityResult(request,result,data);if(request==12&&chooser!=null){chooser.onReceiveValue(result==RESULT_OK&&data!=null?new Uri[]{data.getData()}:null);chooser=null;}if(request==13){if(result==RESULT_OK&&data!=null&&pendingExport!=null)try(OutputStream out=getContentResolver().openOutputStream(data.getData())){out.write(pendingExport.getBytes(StandardCharsets.UTF_8));Toast.makeText(this,"Backup saved",Toast.LENGTH_SHORT).show();}catch(Exception e){Toast.makeText(this,"Could not save backup",Toast.LENGTH_LONG).show();}pendingExport=null;}}
 @Override public void onBackPressed(){web.evaluateJavascript("(()=>{const d=document.querySelector('dialog[open]');if(d){d.close();return true}return false})()",value->{if(!"true".equals(value))super.onBackPressed();});}
 @Override protected void onPause(){super.onPause();web.onPause();}
 @Override protected void onResume(){super.onResume();if(web!=null)web.onResume();}
 @Override protected void onDestroy(){if(web!=null)web.destroy();super.onDestroy();}
}
