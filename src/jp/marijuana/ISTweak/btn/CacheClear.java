/**
 * キャッシュクリア
 */
package jp.marijuana.ISTweak.btn;

import jp.marijuana.ISTweak.ISTweakActivity;
import jp.marijuana.ISTweak.R;
import jp.marijuana.ISTweak.utils.NativeCmd;
import android.app.AlertDialog;
import android.content.Context;
import android.content.DialogInterface;
import android.view.View;
import android.view.View.OnClickListener;
import android.widget.Button;

public class CacheClear
{
	private Context ctx;
	
	public static Button getButton(Context c)
	{
		CacheClear ins = new CacheClear(c);
		return ins.makeButton();
	}
	
	private CacheClear(Context c)
	{
		ctx = c;
	}
	
	private Button makeButton()
	{
		Button btn = new Button(ctx);
		btn.setText(R.string.btn_CacheClear);
		btn.setOnClickListener(new OnClickListener(){
			@Override
			public void onClick(View v) {
				showCacheClear();
			}
		});
		return btn;
	}
	
	private void showCacheClear()
	{
		AlertDialog.Builder confirm = new AlertDialog.Builder(ctx); 
		confirm.setTitle(R.string.CACHE_CLEAR);
		confirm.setMessage(R.string.CACHE_CLEAR_DESC);
		confirm.setPositiveButton(R.string.CLEAR, new DialogInterface.OnClickListener(){  
			@Override
			public void onClick(DialogInterface dialog, int which) {
				String cmd = ISTweakActivity.cmdRm + " -rf /data/dalvik-cache/*" + "\n" +
						"if " + ISTweakActivity.cmdPareL + " -d /data/cache/ " + ISTweakActivity.cmdPareR + " ; then" + "\n" +
						ISTweakActivity.cmdRm + " -rf /data/cache/*" + "\n" +
						"fi" + "\n";
				if ( ISTweakActivity.Model.equals("IS11PT") ) {
					cmd += ISTweakActivity.cmdRm + " -rf /data/bugreports/*" + "\n";
				}
				NativeCmd.ExecCommands(cmd.split("\n"), true);
			}});
		confirm.setNegativeButton(R.string.Cancel, new DialogInterface.OnClickListener(){
			@Override
			public void onClick(DialogInterface dialog, int which) {
				return;
			}});
		confirm.show();
	}
}
