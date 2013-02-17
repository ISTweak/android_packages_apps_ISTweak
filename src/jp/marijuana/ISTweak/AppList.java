/**
 * プリインストールアプリのリスト
 */
package jp.marijuana.ISTweak;

import java.util.ArrayList;
import java.util.Collections;
import java.util.List;

import jp.marijuana.ISTweak.utils.NativeCmd;

import android.app.Activity;
import android.app.AlertDialog;
import android.app.ProgressDialog;
import android.content.Context;
import android.content.DialogInterface;
import android.os.Bundle;
import android.os.Handler;
import android.os.Message;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.AdapterView;
import android.widget.ArrayAdapter;
import android.widget.ImageView;
import android.widget.ListView;
import android.widget.TextView;
import android.content.pm.ApplicationInfo;
import android.content.pm.PackageManager;
import android.graphics.Color;
import android.graphics.drawable.Drawable;

public class AppList extends Activity  implements Runnable {
	private static ProgressDialog waitDialog;
	private Thread thread;
	public PackageManager pm;
	public MyAdapter<ListData> myA;
	
	public static String AppDir = "";
	
	/** Called when the activity is first created. */
	@Override
	public void onCreate(Bundle savedInstanceState)
	{
		super.onCreate(savedInstanceState);
		setContentView(R.layout.applist);
		
		TextView dsc = (TextView)this.findViewById(R.id.HeaderText);
		dsc.setText(R.string.ActionDefAppDisable);
		TextView dsc2 = (TextView)this.findViewById(R.id.BodyText);
		dsc2.setText(R.string.ActionDefAppDesc);
		ImageView imageView = (ImageView)this.findViewById(R.id.Icon);
		imageView.setImageResource(R.drawable.icon);
		ShowWait();
	}
	
	@Override
	public void run()
	{
		try {
			Thread.sleep(1000);
		} catch (InterruptedException e) {
		}
		mhandler.sendEmptyMessage(0);
	}
	
	private void ShowWait()
	{
		waitDialog = new ProgressDialog(this);
		waitDialog.setMessage(getString(R.string.AppWaitAction));
		waitDialog.setProgressStyle(ProgressDialog.STYLE_SPINNER);
		waitDialog.show();
		thread = new Thread(this);
		thread.start();
	}

	private Handler mhandler = new Handler()
	{
		public void handleMessage(Message msg)
		{
			getAppList();
			waitDialog.dismiss();
			waitDialog = null;
		}
	};
	
	private class MyAdapter<T extends ListData> extends ArrayAdapter<T>
	{
		private ArrayList<T> items;
		private LayoutInflater inflater;
		private int textViewResourceId;
		
		public MyAdapter(Context context, int textViewResourceId, ArrayList<T> items)
		{
			super(context, textViewResourceId, items);
			this.items = items;
			this.textViewResourceId = textViewResourceId;
			inflater = (LayoutInflater) context.getSystemService(Context.LAYOUT_INFLATER_SERVICE);
		}
		
		@Override
		public View getView(int position,View convertView, ViewGroup parent)
		{
			View view;
			if (convertView == null) {
				view = inflater.inflate(this.textViewResourceId, null);
			} else {
				view = convertView;
			}
			T item = items.get(position);
			if (item != null) {
				ImageView icon = (ImageView) view.findViewById(R.id.Icon);
				icon.setImageDrawable(item.getIcon());
				
				item.tv = (TextView) view.findViewById(R.id.HeaderText);
				item.tv.setText(item.getHeaderText());
				item.tv.setTextColor(item.getTextColor());
				
				TextView bT = (TextView) view.findViewById(R.id.BodyText);
				bT.setText(item.getBodyText());
			}
			return view;
		}
	}
	
	private class ListData
	{
		private Drawable icon;
		private String headerText;
		private String bodyText;
		private int txtColor = 0;
		public TextView tv;

		public ListData(Drawable icon, String headerText, String bodyText, int cl)
		{
			this.icon = icon;
			this.headerText = headerText.replace("\n", "");
			this.bodyText = bodyText;
			this.txtColor = cl;
		}

		public String getHeaderText()
		{
			return headerText;
		}

		public String getBodyText()
		{
			return bodyText;
		}

		public Drawable getIcon()
		{
			return icon;
		}
		
		public int getTextColor()
		{
			return txtColor;
		}
		
		public void setTextColor(int cl)
		{
			this.txtColor = cl;
		}
	}
	
	private void getAppList()
	{
		//インストールされたアプリケーションの取得
		pm = this.getPackageManager();
		List<ApplicationInfo> list = pm.getInstalledApplications(0);
		
		//アプリケーションをsystem/app内のみにしてソート
		List<ApplicationInfo> LApps = new ArrayList<ApplicationInfo>();
		for (ApplicationInfo ai : list) {
			if ( ai.publicSourceDir.substring(0,AppDir.length()).equals(AppDir) ) {
				LApps.add(ai);
			}
		}
		Collections.sort(LApps, new ApplicationInfo.DisplayNameComparator(pm));
		
		//リストビューに追加
		ListView lv = (ListView) findViewById(R.id.listview);
		myA = new MyAdapter<ListData>(this, R.layout.applist, new ArrayList<ListData>());
		int cl = 0;
		for (ApplicationInfo lapp : LApps) {
			if (pm.getApplicationEnabledSetting(lapp.packageName)== 2) {
				cl = Color.rgb(0,0,255);
			} else {
				cl = Color.rgb(255, 255, 255);
			}
			myA.add(new ListData(lapp.loadIcon(pm), lapp.loadLabel(pm).toString(), lapp.packageName, cl));
		}
		lv.setAdapter(myA);
		
		// リストビューのアイテムがクリックされた時のコールバック
		lv.setOnItemClickListener(new AdapterView.OnItemClickListener() {
			public ListData item;
			@Override
			public void onItemClick(AdapterView<?> parent, View view, int position, long id) {
				ListView listView = (ListView) parent;
				// アイテムの取得
				item = (ListData)listView.getItemAtPosition(position);
				AlertDialog.Builder alert = new AlertDialog.Builder(AppList.this); 
				alert.setTitle(item.getHeaderText());
				
				if (pm.getApplicationEnabledSetting(item.getBodyText())!= 2) {
					//無効ボタン
					alert.setMessage(item.getBodyText() + getString(R.string.ToDelete));
					alert.setPositiveButton(R.string.DisableAction, new DialogInterface.OnClickListener(){  
						@Override
						public void onClick(DialogInterface dialog, int which) {
							item.setTextColor(Color.rgb(0,0,255));
							item.tv.setTextColor(item.getTextColor());
							NativeCmd.ExecuteCmdAlert(AppList.this, "pm disable " + item.getBodyText(), true);
						}});
				} else if (pm.getApplicationEnabledSetting(item.getBodyText())== 2) {
					//有効ボタン
					alert.setMessage(item.getBodyText() + getString(R.string.ToReturn));
					alert.setNeutralButton(R.string.EnableAction, new DialogInterface.OnClickListener(){
						@Override
						public void onClick(DialogInterface dialog, int which) {
							item.setTextColor(Color.rgb(255,255,255));
							item.tv.setTextColor(item.getTextColor());
							NativeCmd.ExecuteCmdAlert(AppList.this, "pm enable " + item.getBodyText(), true);
						}});
				}
				//キャンセルボタン
				alert.setNegativeButton(R.string.Cancel, new DialogInterface.OnClickListener(){
					@Override
					public void onClick(DialogInterface dialog, int which) {
						return;
					}});
				alert.show();
			}
		});
	}
}

