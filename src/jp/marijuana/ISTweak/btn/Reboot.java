/**
 * 再起動
 */
package jp.marijuana.ISTweak.btn;

import java.io.File;
import java.io.IOException;
import java.util.ArrayList;

import jp.marijuana.ISTweak.ISTweakActivity;
import jp.marijuana.ISTweak.R;
import jp.marijuana.ISTweak.utils.NativeCmd;

import android.app.AlertDialog;
import android.content.Context;
import android.util.Log;
import android.view.View;
import android.view.View.OnClickListener;
import android.widget.AdapterView;
import android.widget.ArrayAdapter;
import android.widget.Button;
import android.widget.ListView;
import android.widget.AdapterView.OnItemClickListener;

public class Reboot {
	private Context ctx;
	private AlertDialog mDlg = null;
	
	public static Button getButton(Context c) {
		Reboot ins = new Reboot(c);
		return ins.makeButton();
	}
	
	private Reboot(Context c) {
		ctx = c;
	}
	
	private Button makeButton() {
		Button btn = new Button(ctx);
		btn.setText(R.string.btn_Reboot);
		btn.setOnClickListener(new OnClickListener(){
			@Override
			public void onClick(View v) {
				showRebootList();
			}
		});
		return btn;
	}
	
	private void showRebootList()
	{
		final ArrayList<String> rows = new ArrayList<String>();
		rows.add(ctx.getString(R.string.RebootNormal));
		rows.add(ctx.getString(R.string.RebootRecovery));
		
		ListView lv = new ListView(ctx);
		lv.setAdapter(new ArrayAdapter<String>(ctx, android.R.layout.simple_list_item_1, rows));
		lv.setScrollingCacheEnabled(false);
		lv.setOnItemClickListener(new OnItemClickListener() {
			@Override
			public void onItemClick(AdapterView<?> items, View view, int position, long id) {
				mDlg.dismiss();
				switch (position) {
					case 0: DoReboot(); break;
					case 1: DoRebootRec(); break;
				}
			}
		});

		mDlg = new AlertDialog.Builder(ctx)
			.setTitle(ctx.getString(R.string.RebootMode))
			.setPositiveButton(R.string.Cancel, null)
			.setView(lv)
			.create();
		mDlg.show();
	}
	
	public void DoReboot()
	{
		File ff = new File(ctx.getDir("bin", 0), "reboot");
		try {
			NativeCmd.ExecuteCommand(ff.getAbsolutePath());
		} catch (IOException e) {
			Log.e("ISTweak", e.toString());
			ISTweakActivity.alert(ctx, e.toString());
		}
	}
	
	public void DoRebootRec()
	{
		File ff = new File(ctx.getDir("bin", 0), "reboot");
		try {
			NativeCmd.ExecuteCommand(ff.getAbsolutePath() + " recovery");
		} catch (IOException e) {
			Log.e("ISTweak", e.toString());
			ISTweakActivity.alert(ctx, e.toString());
		}
	}
}
