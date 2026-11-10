package br.com.bravuz.editor;

import android.app.Activity;
import android.app.AlertDialog;
import android.content.Context;
import android.content.Intent;
import android.content.SharedPreferences;
import android.net.Uri;
import org.json.JSONObject;
import java.io.ByteArrayOutputStream;
import java.io.InputStream;
import java.net.HttpURLConnection;
import java.net.URL;
import java.nio.charset.StandardCharsets;

/** GitHub public metadata; installation is handled by Android and requires user confirmation. */
final class UpdateChecker {
    private static final String CONFIG="https://raw.githubusercontent.com/KaizinBZ/BravuzEditor/main/update.json";
    private static final String RELEASES="https://github.com/KaizinBZ/BravuzEditor/releases";
    private final Activity activity;
    private final SharedPreferences prefs;
    private AlertDialog active;
    UpdateChecker(Activity a){activity=a;prefs=a.getSharedPreferences("updates",Context.MODE_PRIVATE);}
    void check(boolean manual){int current=0;try{current=activity.getPackageManager().getPackageInfo(activity.getPackageName(),0).versionCode;}catch(Exception ignored){}if(prefs.getInt("minimum",0)>current)showResult(false,null,false);new Thread(()->{
        String error=null;boolean ok=false;
        try{HttpURLConnection conn=(HttpURLConnection)new URL(CONFIG).openConnection();conn.setRequestMethod("GET");conn.setConnectTimeout(7000);conn.setReadTimeout(7000);conn.setInstanceFollowRedirects(false);conn.setRequestProperty("Accept","application/json");conn.setRequestProperty("Cache-Control","no-cache");try{if(conn.getResponseCode()!=200)throw new Exception("Não foi possível consultar atualizações.");try(InputStream in=conn.getInputStream();ByteArrayOutputStream out=new ByteArrayOutputStream()){byte[] buf=new byte[1024];int n;while((n=in.read(buf))!=-1){if(out.size()+n>16384)throw new Exception("Resposta de atualização muito grande.");out.write(buf,0,n);}JSONObject o=new JSONObject(new String(out.toByteArray(),StandardCharsets.UTF_8));int latest=o.getInt("latestVersionCode"),minimum=o.getInt("minimumVersionCode");String version=o.getString("latestVersionName");if(latest<1||minimum<0||minimum>latest||version.length()>80)throw new Exception("Dados de atualização inválidos.");prefs.edit().putInt("latest",latest).putInt("minimum",minimum).putString("version",version).apply();ok=true;}}finally{conn.disconnect();}}catch(Exception e){error="Não foi possível verificar agora. Confira a conexão e tente novamente.";}
        final String msg=error;final boolean success=ok;activity.runOnUiThread(()->showResult(success,msg,manual));
    }).start();}
    private void showResult(boolean ok,String error,boolean manual){if(activity.isFinishing()||activity.isDestroyed())return;int current;try{current=activity.getPackageManager().getPackageInfo(activity.getPackageName(),0).versionCode;}catch(Exception e){return;}int minimum=prefs.getInt("minimum",0),latest=prefs.getInt("latest",0);String version=prefs.getString("version","");boolean required=minimum>current;
        if(!ok&&!required){if(manual)new AlertDialog.Builder(activity).setTitle("Atualizações").setMessage(error).setPositiveButton("Fechar",null).show();return;}
        if(required){if(active!=null&&active.isShowing())return;active=new AlertDialog.Builder(activity).setTitle("Atualização obrigatória").setMessage("Esta versão precisa ser atualizada para continuar. "+(ok?"Versão disponível: "+version+".":"A última verificação indicou uma atualização obrigatória; conecte-se para conferir.")+"\n\nO Android solicitará sua confirmação para instalar.").setPositiveButton("Abrir downloads",(d,w)->{open();showResult(false,error,false);}).setNegativeButton("Tentar novamente",(d,w)->check(true)).setNeutralButton("Sair",(d,w)->activity.finish()).create();active.setCancelable(false);active.setCanceledOnTouchOutside(false);active.show();return;}
        if(ok&&latest>current){new AlertDialog.Builder(activity).setTitle("Atualização disponível").setMessage("Nova versão: "+version+". Abra os downloads para atualizar.").setPositiveButton("Abrir downloads",(d,w)->open()).setNegativeButton("Depois",null).show();}else if(manual){new AlertDialog.Builder(activity).setTitle("Atualizações").setMessage("Você está usando a versão mais recente informada.").setPositiveButton("Fechar",null).show();}}
    private void open(){try{activity.startActivity(new Intent(Intent.ACTION_VIEW,Uri.parse(RELEASES)));}catch(Exception e){new AlertDialog.Builder(activity).setTitle("Não foi possível abrir").setMessage("Abra a página de releases do BravuzEditor no navegador.").setPositiveButton("Fechar",null).show();}}
}
