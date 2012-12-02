package jp.marijuana.ISTweak;

import java.io.File;
import java.io.FileOutputStream;
import java.io.IOException;
import java.io.InputStream;

import jp.marijuana.ISTweak.btn.CacheClear;
import jp.marijuana.ISTweak.btn.LcdDensity;
import jp.marijuana.ISTweak.btn.LifeLog;
import jp.marijuana.ISTweak.btn.Market;
import jp.marijuana.ISTweak.btn.Reboot;
import jp.marijuana.ISTweak.btn.btnAppList;
import jp.marijuana.ISTweak.utils.NativeCmd;

import android.app.Activity;
import android.app.AlertDialog;
import android.app.Notification;
import android.app.NotificationManager;
import android.app.PendingIntent;
import android.appwidget.AppWidgetManager;
import android.content.ComponentName;
import android.content.Context;
import android.content.Intent;
import android.content.pm.PackageInfo;
import android.content.pm.PackageManager;
import android.content.pm.PackageManager.NameNotFoundException;
import android.content.res.Resources;
import android.os.Bundle;
import android.util.Log;
import android.view.View;
import android.view.View.OnClickListener;
import android.widget.Button;
import android.widget.LinearLayout;
import android.widget.RemoteViews;
import android.widget.TextView;
import android.widget.Toast;
/**
 * @author Marijuana
 *
 */
public class ISTweakActivity extends Activity
{

	public static String Model = "";
	public String TetheringCmd = "";
	public String versionName = "";
	public static ISTweakActivity ctx;
	public static Boolean ChangeAu = true;
	
	public static String cmdGrep = "grep";
	public static String cmdSed = "sed";
	public static String cmdRm = "rm";
	public static String cmdPkill = "pkill";
	public static String cmdPareL = "[";
	public static String cmdPareR = "]";
	
	/**
	 * コマンドパスの検索
	 * @param cmd
	 * @return　コマンドフルパス
	 */
	private String getCmdPath(String cmd)
	{
		if ( NativeCmd.fileExists("/data/root/bin/" + cmd) ) {
			return "/data/root/bin/" + cmd;
		} else if ( NativeCmd.fileExists("/data/local/bin/" + cmd) ) {
			return "/data/local/bin/" + cmd;
		} else if ( NativeCmd.fileExists("/system/xbin/" + cmd) ) {
			return "/system/xbin/" + cmd;
		}
		return cmd;
	}
	
	/**
	 * コマンドパスの設定
	 */
	private void cmdPath()
	{
		cmdGrep = getCmdPath("grep");
		cmdSed = getCmdPath("sed");
		cmdRm = getCmdPath("rm");
		cmdPkill = getCmdPath("pkill");
		cmdPareL = getCmdPath("[");
		cmdPareR = getCmdPath("]");
	}
	
	/**
	 * メッセージボックス表示
	 * @param Context ctx
	 * @param Integer msg
	 */
	static public void alert(Context ctx, Integer msg)
	{
		new AlertDialog.Builder(ctx).setNeutralButton(android.R.string.ok, null).setMessage(msg).show();
	}
	
	/**
	 * メッセージボックス表示
	 * @param Context ctx
	 * @param String msg
	 */
	static public void alert(Context ctx, String msg)
	{
		new AlertDialog.Builder(ctx).setNeutralButton(android.R.string.ok, null).setMessage(msg).show();
	}
	
	/**
	 * suコマンドを有効にする
	 * @param Context ctx
	 */
	static public void DoEnableSu(Context ctx)
	{
		if (NativeCmd.ExecuteCmdAlert(ctx, "ln -s /sbin/au /sbin/su", true)) {
			Toast.makeText(ctx.getApplicationContext(), ctx.getString(R.string.EnableSu), Toast.LENGTH_SHORT ).show();
			Log.i("ISTweak", "Enable su");
		}
	}
	
	/**
	 * suコマンドを無効にする
	 * @param Context ctx
	 */
	static public void DoDisableSu(Context ctx)
	{
		if (NativeCmd.ExecuteCmdAlert(ctx, "rm /sbin/su", true)) {
			Toast.makeText(ctx.getApplicationContext(), ctx.getString(R.string.DisableSu), Toast.LENGTH_SHORT ).show();
			Log.i("ISTweak", "Disable su");
		}
	}
	
	/**
	 * suボタン
	 */
	private Button makeSuBtn()
	{
		Button btnsu = new Button(this);
		if (NativeCmd.fileExists("/sbin/su")) {
			btnsu.setText(R.string.btn_DelSu);
			btnsu.setOnClickListener(new OnClickListener() {
				@Override
				public void onClick(View v) {
					DoDisableSu(getApplicationContext());
					UpdateWidget();
					ISTweakActivity.this.finish();
				}
			});
		} else {
			btnsu.setText(R.string.btn_PutSu);
			btnsu.setOnClickListener(new OnClickListener() {
				@Override
				public void onClick(View v) {
					DoEnableSu(getApplicationContext());
					UpdateWidget();
					ISTweakActivity.this.finish();
				}
			});
		}
		return btnsu;
	}
	
	/**
	 * アプリ有効無効ボタン
	 */
	public void ShowAppList()
	{
		Intent intent = new Intent();
		intent.setClassName("jp.marijuana.ISTweak", "jp.marijuana.ISTweak.AppList");
		startActivity(intent);
	}
	
	/**
	 * テザリング有効化
	 */
	private Button makeTethering()
	{
		Button btnr = new Button(this);
		btnr.setText(R.string.btn_Tethering);
		btnr.setOnClickListener(new OnClickListener(){
			@Override
			public void onClick(View v) {
				NativeCmd.ExecuteCmdAlert(ISTweakActivity.this, TetheringCmd, true);
			}
		});
		return btnr;
	}
	
	/**
	 * オーバークロック
	 */
	private Button makeOverClock()
	{
		Button btnoc = new Button(this);
		btnoc.setText(R.string.btn_OverClock);
		btnoc.setOnClickListener(new OnClickListener(){
			@Override
			public void onClick(View v) {
				Intent intent = new Intent();
				intent.setClassName("jp.marijuana.ISTweak", "jp.marijuana.ISTweak.OverClock");
				startActivity(intent);
			}
		});
		return btnoc;
	}
	
	/**
	 * compcache
	 */
	private Button makeCompCache()
	{
		Button btncmp = new Button(this);
		btncmp.setText(R.string.Str_CompCache);
		btncmp.setOnClickListener(new OnClickListener(){
			@Override
			public void onClick(View v) {
				Intent intent = new Intent();
				intent.setClassName("jp.marijuana.ISTweak", "jp.marijuana.ISTweak.CompCache");
				startActivity(intent);
			}
		});
		return btncmp;
	}
	
	/**
	 * Zram
	 */
	private Button makeZram()
	{
		Button btncmp = new Button(this);
		btncmp.setText(R.string.Str_Zram);
		btncmp.setOnClickListener(new OnClickListener(){
			@Override
			public void onClick(View v) {
				Intent intent = new Intent();
				intent.setClassName("jp.marijuana.ISTweak", "jp.marijuana.ISTweak.Zram");
				startActivity(intent);
			}
		});
		return btncmp;
	}
	
	/**
	 * suコマンドの検索
	 * @return
	 */
	private boolean checkSuCmd()
	{
		if (NativeCmd.fileExists("/system/xbin/su")) {
			NativeCmd.au = "/system/xbin/su";
			ChangeAu = false;
		} else if (NativeCmd.fileExists("/system/bin/su")) {
			NativeCmd.au = "/system/bin/su";
			ChangeAu = false;
		} else if (NativeCmd.fileExists("/sbin/su")) {
			NativeCmd.au = "/sbin/su";
			ChangeAu = false;
		} else {
			return false;
		}

		return true;
	}
    
	/** Called when the activity is first created. */
	@Override
	public void onCreate(Bundle savedInstanceState)
	{
		ISTweakActivity.ctx = this;
		
		super.onCreate(savedInstanceState);
		setContentView(R.layout.main);
		LinearLayout layout = (LinearLayout) findViewById(R.id.linearLayout01);
		
        //バージョン取得	
		PackageManager packageManager = this.getPackageManager();
        try {
        	PackageInfo packageInfo = packageManager.getPackageInfo(this.getPackageName(), PackageManager.GET_ACTIVITIES);
        	versionName = packageInfo.versionName;
        } catch (NameNotFoundException e) {
        	Log.e("SHBreak", e.toString());
        }
		
		//バージョン表示
		TextView dsc = new TextView(this);
		dsc.setText("Version:" + versionName);
		layout.addView(dsc);
		
		//auコマンドが無いとき
		if (!NativeCmd.fileExists("/sbin/au")) {
			if ( checkSuCmd() == false ) {
				TextView none = new TextView(this);
				none.setText(R.string.Description);
				layout.addView(none);
				return;
			}
		}
		
		//
		cmdPath();
		
		//setpropex
		assertBinaries();
		
		//Type取得
		Model = NativeCmd.getProperties("ro.product.model").trim().replace("\n", "");
		if ( Model.equals("IS06") ) {
			TetheringCmd = "/system/bin/am start -n com.pantech.app.wifitest/.TetheringActivation > /dev/null 2>&1";
		} else if ( Model.equals("IS11PT") ) {
			TetheringCmd = "/system/bin/am start -n com.android.settings/.wifi.TetheringActivation > /dev/null 2>&1";
		} else if ( Model.equals("IS11N") ) {
			TetheringCmd = "/system/bin/am start -n com.android.settings/.TetherSettings > /dev/null 2>&1";
		//} else {
		//	TetheringCmd = "/system/bin/am start -n com.android.settings/.TetherSettings > /dev/null 2>&1";
		}
		
		//ボタン作成
		if ( ChangeAu == true ) {
			layout.addView(makeSuBtn());
		}
		layout.addView(btnAppList.getButton(this));
		layout.addView(Market.getButton(this));
		layout.addView(Reboot.getButton(this));
		if (NativeCmd.fileExists("/data/root/autoexec.sh")) {
			layout.addView(LcdDensity.getButton(this));
		}
		if ( TetheringCmd != "" ) {
			layout.addView(makeTethering());
		}
		layout.addView(CacheClear.getButton(this));
		if (NativeCmd.fileExists("/sys/devices/system/cpu/cpu0/cpufreq/vdd_levels")) {
			layout.addView(makeOverClock());
		}
		if (NativeCmd.fileExists("/sbin/compcache")) {
			layout.addView(makeCompCache());
		}
		if (NativeCmd.fileExists("/sbin/zram")) {
			layout.addView(makeZram());
		}
		if (NativeCmd.fileExists("/ldb/")) {
			layout.addView(LifeLog.getButton(this));
		}
		
    	Button btnext = new Button(this);
    	btnext.setText(R.string.str_exit);
    	btnext.setOnClickListener(new OnClickListener(){
			@Override
			public void onClick(View v) {
				finish();
 			}
		});
    	layout.addView(btnext);
	}
	
	private boolean assertBinaries()
	{
		File bindir = this.getDir("bin", 0);
		try {
			File file = new File(bindir, "setpropex");
			if ( !file.exists() ) {
				copyRawFile(this, R.raw.setpropex, file, "0755", false);
				Toast.makeText(this, "copy setpropex", Toast.LENGTH_LONG).show();
			}

			file = new File(bindir, "lcd_density");
			if ( !file.exists() ) {
				copyRawFile(this, R.raw.lcddensity, file, "0755", false);
				Toast.makeText(this, "copy lcd_density", Toast.LENGTH_LONG).show();
			}
			
			file = new File(bindir, "reboot");
			if ( !file.exists() ) {
				copyRawFile(this, R.raw.reboot, file, "0755", false);
				Toast.makeText(this, "copy reboot", Toast.LENGTH_LONG).show();
			}
		} catch (Exception e) {
			alert(this, "Error installing binary files: " + e);
			Log.e("ISTweak", e.toString());
			return false;
		}
		return true;
	}
	
	private void copyRawFile(Context ctx, int resid, File file, String mode, boolean root) throws IOException, InterruptedException
	{
		final String abspath = file.getAbsolutePath();
		final FileOutputStream out = new FileOutputStream(file);
		final InputStream is = ctx.getResources().openRawResource(resid);
		byte buf[] = new byte[1024];
		int len;
		while ((len = is.read(buf)) > 0) {
			out.write(buf, 0, len);
		}
		out.close();
		is.close();
		// Change the permissions
		try {
			if ( root ) {
				Runtime.getRuntime().exec("chown root.root " + abspath).waitFor();
			}
			Runtime.getRuntime().exec("chmod " + mode + " " + abspath).waitFor();
		} catch (InterruptedException e) {
			Log.e("ISTweak", e.toString());
		} catch (IOException e) {
			Log.e("ISTweak", e.toString());
		}
	}
	
	/**
	 * ウィジットのアイコン更新
	 * @param Context ctx
	 * @param RemoteViews rViews
	 */
	static public void WidgetUpdate(Context ctx, RemoteViews rViews)
	{
		int imgid;
		if (NativeCmd.fileExists("/sbin/su")) {
			imgid = R.drawable.widget_on;
			NUpdate(ctx, true);
		} else if (NativeCmd.fileExists("/sbin/pu")) {
			imgid = R.drawable.widget_off;
			NUpdate(ctx, false);
		} else {
			imgid = R.drawable.widget_blank;
		}
		rViews.setImageViewResource(R.id.ImageView01, imgid);
		ComponentName thisWidget = new ComponentName(ctx, RootSwitchWidgetProvider.class);
		AppWidgetManager manager = AppWidgetManager.getInstance(ctx);
		manager.updateAppWidget(thisWidget, rViews);
	}
	
	/**
	 * ウィジットのアイコン更新
	 */	
	private void UpdateWidget()
	{
		RemoteViews remoteViews = new RemoteViews(getPackageName(), R.layout.widget);
		WidgetUpdate(getApplicationContext(), remoteViews);
	}	
	
	/**
	 * 通知バーの設定
	 * @param ctx
	 * @param cl
	 */
	public static void NUpdate(Context ctx, Boolean cl)
	{
		NotificationManager manager = (NotificationManager)(ctx.getSystemService(NOTIFICATION_SERVICE));
		Resources res = ctx.getResources();
		
		if ( cl ) {
			Notification notification = new Notification(R.drawable.notification, res.getString(R.string.EnableSu), System.currentTimeMillis());
			//PendingIntent pendingIntent = PendingIntent.getActivity(ctx, 0, null, Intent.FLAG_ACTIVITY_NEW_TASK);
			Intent intent = new Intent(ctx, ISTweakActivity.class);
			PendingIntent pendingIntent = PendingIntent.getService(ctx, 0, intent, 0);
			notification.setLatestEventInfo(ctx, res.getString(R.string.EnableSuSubject), res.getString(R.string.EnableSuDesc), pendingIntent);
			notification.flags = Notification.FLAG_NO_CLEAR | Notification.FLAG_ONGOING_EVENT;
			manager.notify(R.string.app_name, notification);
		} else {
			manager.cancel(R.string.app_name);
		}
	}
}

