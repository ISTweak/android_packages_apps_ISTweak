package jp.marijuana.ISTweak.btn;

import java.util.ArrayList;

import jp.marijuana.ISTweak.AppList;
import jp.marijuana.ISTweak.ISTweakActivity;
import jp.marijuana.ISTweak.R;
import android.app.AlertDialog;
import android.content.Context;
import android.view.View;
import android.view.View.OnClickListener;
import android.widget.AdapterView;
import android.widget.ArrayAdapter;
import android.widget.Button;
import android.widget.ListView;
import android.widget.AdapterView.OnItemClickListener;

public class btnAppList
{
	private Context ctx;
	private AlertDialog mDlg = null;
	
	public static Button getButton(Context c)
	{
		btnAppList ins = new btnAppList(c);
		return ins.makeButton();
	}
	
	private btnAppList(Context c)
	{
		ctx = c;
	}
	
	private Button makeButton()
	{
		Button btn = new Button(ctx);
		btn.setText(R.string.btn_AppList);
		btn.setOnClickListener(new OnClickListener(){
			@Override
			public void onClick(View v) {
				SelectDir();
			}
		});
		return btn;
	}
	
	private void SelectDir()
	{
		final ArrayList<String> rows = new ArrayList<String>();
		rows.add(ctx.getString(R.string.DataDir));
		rows.add(ctx.getString(R.string.SystemDir));
		
		ListView lv = new ListView(ctx);
		lv.setAdapter(new ArrayAdapter<String>(ctx, android.R.layout.simple_list_item_1, rows));
		lv.setScrollingCacheEnabled(false);
		lv.setOnItemClickListener(new OnItemClickListener() {
			@Override
			public void onItemClick(AdapterView<?> items, View view, int position, long id) {
				mDlg.dismiss();
				switch (position) {
					case 0: AppList.AppDir = "/data/app"; break;
					case 1: AppList.AppDir = "/system/app"; break;
				}
				
				ISTweakActivity.ctx.ShowAppList();
			}
		});

		mDlg = new AlertDialog.Builder(ctx)
			.setTitle(ctx.getString(R.string.SelectDirTittle))
			.setPositiveButton(R.string.Cancel, null)
			.setView(lv)
			.create();
		mDlg.show();
	}
}
