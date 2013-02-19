/**
 * マーケット偽装
 */
package jp.marijuana.ISTweak.btn;

import java.io.File;
import java.io.FileOutputStream;
import java.io.OutputStreamWriter;
import java.util.ArrayList;

import jp.marijuana.ISTweak.ISTweakActivity;
import jp.marijuana.ISTweak.R;
import jp.marijuana.ISTweak.utils.NativeCmd;

import android.app.AlertDialog;
import android.content.Context;
import android.content.DialogInterface;
import android.util.Log;
import android.view.View;
import android.view.View.OnClickListener;
import android.widget.AdapterView;
import android.widget.ArrayAdapter;
import android.widget.Button;
import android.widget.ListView;
import android.widget.AdapterView.OnItemClickListener;

public class Market
{
	private Context ctx;
	private AlertDialog mCamouflage = null;
	private final String db = "/data/data/com.google.android.gsf/databases/gservices.db";
	
	public static Button getButton(Context c)
	{
		Market ins = new Market(c);
		return ins.makeButton();
	}
	
	private Market(Context c) {
		ctx = c;
	}
	
	private Button makeButton()
	{
		Button btn = new Button(ctx);
		btn.setText(R.string.btn_Market);
		btn.setOnClickListener(new OnClickListener(){
			@Override
			public void onClick(View v) {
				showMarketList();
			}
		});
		return btn;
	}
	
	private void showMarketList()
	{
		final ArrayList<String> rows = new ArrayList<String>();
		rows.add(ctx.getString(R.string.MARKET_AU));
		rows.add(ctx.getString(R.string.MARKET_DOCOMO));
		rows.add(ctx.getString(R.string.MARKET_SOFTBANK));
		rows.add(ctx.getString(R.string.MARKET_EMOBILE));
		rows.add(ctx.getString(R.string.MARKET_TMobile));
		rows.add(ctx.getString(R.string.MARKET_Empty));
		
		ListView lv = new ListView(ctx);
		lv.setAdapter(new ArrayAdapter<String>(ctx, android.R.layout.simple_list_item_1, rows));
		lv.setScrollingCacheEnabled(false);
		lv.setOnItemClickListener(new OnItemClickListener() {
			@Override
			 public void onItemClick(AdapterView<?> items, View view, int position, long id) {
				switch (position) {
					case 0: MarketCamouflage("44054", "jp", "KDDI"); break;
					case 1: MarketCamouflage("44010", "jp", "DOCOMO"); break;
					case 2: MarketCamouflage("44020", "jp", "SOFTBANK"); break;
					case 3: MarketCamouflage("44000", "jp", "EMOBILE"); break;
					case 4: MarketCamouflage("310260", "us", "T-Mobile"); break;
					case 5: MarketCamouflage("\"\"", "\"\"", "\"\""); break;
				}
				mCamouflage.dismiss();
			}
		});
		
		AlertDialog.Builder mDialogBuilder = new AlertDialog.Builder(ctx);
		mDialogBuilder.setTitle(R.string.SELECT_CA);
		mDialogBuilder.setView(lv);
		mDialogBuilder.setPositiveButton(R.string.Cancel, null);

		if ( ISTweakActivity.Model.equals("IS01") == false ) {
			if ( !DebugCheck() ) {
				mDialogBuilder.setNegativeButton(R.string.MARKET_DebugOn, new DialogInterface.OnClickListener() {
					@Override
					public void onClick(DialogInterface dialog, int which) {
						String cmd = "sqlite3 " + db + " \"insert into main (name,value) values('finsky.debug_options_enabled', 'true');\"\n" +
								ISTweakActivity.cmdPkill + " -9 com.android.vending\n" +
								"";
						NativeCmd.ExecuteCommands(cmd.split("\n"), true);
						MakeCarrierList();
						ISTweakActivity.ctx.finish();
					}
				});
			} else {
				mDialogBuilder.setNegativeButton(R.string.MARKET_DebugOff, new DialogInterface.OnClickListener() {
					@Override
					public void onClick(DialogInterface dialog, int which) {
						String cmd = "sqlite3 " + db + " \"delete from main where name = 'finsky.debug_options_enabled';\"\n" +
								ISTweakActivity.cmdPkill + " -9 com.android.vending\n" +
								"";
						NativeCmd.ExecuteCommands(cmd.split("\n"), true);
						ISTweakActivity.ctx.finish();
					}
				});
			}
		}
		mCamouflage = mDialogBuilder.create();
		mCamouflage.show();
	}
	
	private boolean DebugCheck()
	{
		String cmd = "sqlite3 " + db + " \"select value from main where name = 'finsky.debug_options_enabled';\"";
		String[] line = NativeCmd.ExecCommand(cmd, true);
		return !(line[1].trim().replace("\n", "") == "");
	}
	
	private void MakeCarrierList()
	{
		File dr = new File("/sdcard/download");
		if ( !dr.exists() ) {
			dr.mkdir();
		}
		String fn = "/sdcard/download/carriers.csv";
		if ( !NativeCmd.fileExists(fn) ) {
			try {
				final OutputStreamWriter out = new OutputStreamWriter(new FileOutputStream(fn));
				out.write("DOCOMO,jp,44010\n");
				out.write("KDDI,jp,44054\n");
				out.write("SOFTBANK,jp,44020\n");
				out.write("EMOBILE,jp,44000\n");
				out.write("T-Mobile,us,310260\n");
				out.flush();
				out.close();
				Runtime.getRuntime().exec("chmod 0666 " + fn).waitFor();
			} catch (Exception e) {
				Log.e("ISTweak", e.toString());
			}
		}
	}

	private void MarketCamouflage(String marketid, String Country, String alpha)
	{
		File ff = new File(ctx.getDir("bin", 0), "setpropex");
		String cmd = "setprop gsm.sim.operator.numeric \"" + marketid + "\"\n" +
					 "setprop gsm.operator.numeric \"" + marketid + "\"\n" +
					 "setprop gsm.sim.operator.iso-country \"" + Country + "\"\n" +
					 "setprop gsm.operator.iso-country \"" + Country + "\"\n" +
					 "setprop gsm.operator.alpha \"" + alpha + "\"\n" +
					 "setprop gsm.sim.operator.alpha \"" + alpha + "\"\n" +
					 ISTweakActivity.cmdPkill + " -9 com.android.vending\n" +
					 ISTweakActivity.cmdRm + " -rf /data/data/com.android.vending/*\n" +
				"";
		
		if ( ISTweakActivity.Model.equals("IS01") == false ) {
			cmd += ff.getAbsolutePath() + " ro.cdma.home.operator.numeric \"" + marketid + "\"\n" +
				   ff.getAbsolutePath() + " ro.cdma.home.operator.alpha \"" + alpha + "\"\n";
		}
		NativeCmd.ExecuteCommands(cmd.split("\n"), true);
	}
}
