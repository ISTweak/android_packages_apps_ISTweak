/**
 * compcacheの設定
 */
package jp.marijuana.ISTweak;

import java.io.File;

import jp.marijuana.ISTweak.utils.NativeCmd;

import android.app.Activity;
import android.os.Bundle;
import android.view.View;
import android.view.ViewGroup;
import android.view.View.OnClickListener;
import android.view.ViewGroup.LayoutParams;
import android.widget.ArrayAdapter;
import android.widget.Button;
import android.widget.CheckBox;
import android.widget.LinearLayout;
import android.widget.Spinner;
import android.widget.TextView;
import android.widget.Toast;

public class CompCache extends Activity
{
	private final LayoutParams lparm = new LinearLayout.LayoutParams(ViewGroup.LayoutParams.WRAP_CONTENT, ViewGroup.LayoutParams.WRAP_CONTENT);
	private final String swappiness = "/proc/sys/vm/swappiness";
	private final String compsize = "/data/root/compsize";
	private int memsize;
	private int swpsize;
	private String mydir = "";
	
	@Override
	public void onCreate(Bundle savedInstanceState)
	{
		super.onCreate(savedInstanceState);
		setContentView(R.layout.overclock);
		
		//現在の設定取得
		memsize = Integer.parseInt(getMemSize());
		swpsize = Integer.parseInt(getSwapSize());
		mydir = this.getDir("bin", 0).getAbsolutePath();
		
		LinearLayout layout = (LinearLayout) findViewById(R.id.ocLayout01);
		TextView tit = new TextView(this);
		tit.setText(R.string.CompCacheSettings);
		layout.addView(tit);
		
		//compcache
		layout.addView(spinner_compsize());
		layout.addView(button_compsize());
		//swappiness
		layout.addView(spinner_swappiness());
		layout.addView(button_swappiness());
	}
	
	/**
	 * compcacheサイズ
	 * @return
	 */
	private LinearLayout spinner_compsize()
	{
		LinearLayout tray = new LinearLayout(this);
		TextView sctit = new TextView(this);
		sctit.setText(R.string.Str_Compsize);
		tray.addView(sctit, lparm);
		
		ArrayAdapter<String> adapter = new ArrayAdapter<String>(this, android.R.layout.simple_spinner_item);
		adapter.setDropDownViewResource(android.R.layout.simple_spinner_dropdown_item);
		int spval = 0;
		if (NativeCmd.fileExists(compsize)) {
			spval = (int)Math.floor((double)Integer.parseInt(NativeCmd.readFile(compsize)) / (double)memsize * 100);
		}
		int spcur = getCurrentSize();
		int swval = (spval == 0) ? spcur : spval;
		int spdef = -1;
		adapter.add("0");
		for (int i = 1; i < 10; i++ ) {
			if ( swval == (i * 5) || (swval + 1) == (i * 5) || (swval - 1) == (i * 5) ) {
				spdef = i;
			}
			adapter.add(String.valueOf(i * 5));
		}
		
		if ( spdef == -1 ) {
			spdef = 0;
			ISTweakActivity.alert(this, "current size:" + String.valueOf(spcur) + "%");
		} else if ( compInt(spval, spcur) == false ) {
			ISTweakActivity.alert(this, "current size:" + String.valueOf(spcur) + "% / " + String.valueOf(spval) + "%");
		}

		Spinner swap = new Spinner(this);
		swap.setPromptId(R.string.Str_CompsizeL);
		swap.setId(1);
		swap.setAdapter(adapter);
		swap.setSelection(spdef);
		
		tray.addView(swap, lparm);
		
		TextView par = new TextView(this);
		par.setText("%");
		tray.addView(par, lparm);
		return tray;
	}
	/**
	 * compcacheサイズ設定ボタン
	 * @return
	 */
	private LinearLayout button_compsize()
	{
		LinearLayout tray = new LinearLayout(this);
		
		Button btn = new Button(this);
		btn.setText(R.string.ChangeAction);
		btn.setOnClickListener(new OnClickListener() {
			@Override
			public void onClick(View v) {
				Spinner spinner = (Spinner)findViewById(1);
				int item = Integer.parseInt((String)spinner.getSelectedItem());
				int ss = (int)Math.floor((double)memsize * ((double)item / (double)100));
				String cmd = "echo " + ss + " > " + compsize;
				NativeCmd.ExecuteCmdAlert(CompCache.this, cmd, true);
				NativeCmd.ExecuteCmdAlert(CompCache.this, "chmod 0666 " + compsize, true);
				Toast.makeText(CompCache.this, R.string.SettingEnd, Toast.LENGTH_LONG).show();
			}
		});
		tray.addView(btn, lparm);
		
		if (NativeCmd.fileExists(compsize)) {
			Button btnd = new Button(this);
			btnd.setText(R.string.DelCompSize);
			btnd.setOnClickListener(new OnClickListener() {
				@Override
				public void onClick(View v) {
					NativeCmd.ExecuteCommand("rm " + compsize, true);
				}
			});
			tray.addView(btnd, lparm);
		}
		return tray;
	}
	
	/**
	 * swappiness
	 * @return
	 */
	private LinearLayout spinner_swappiness()
	{
		LinearLayout tray = new LinearLayout(this);
		TextView sctit = new TextView(this);
		sctit.setText(R.string.Str_Swappiness);
		tray.addView(sctit, lparm);
		
		ArrayAdapter<String> adapter = new ArrayAdapter<String>(this, android.R.layout.simple_spinner_item);
		adapter.setDropDownViewResource(android.R.layout.simple_spinner_dropdown_item);
		int spval = Integer.parseInt(NativeCmd.readFile(swappiness));
		int spdef = 0;
		for (int i = 1; i < 11; i++ ) {
			if ( spval == (i * 10) ) {
				spdef = i - 1;
			}
			adapter.add(String.valueOf(i * 10));
		}
		
		Spinner swap = new Spinner(this);
		swap.setPromptId(R.string.Str_Swappiness);
		swap.setId(10);
		swap.setAdapter(adapter);
		swap.setSelection(spdef);
		tray.addView(swap, lparm);
	
		return tray;
	}
	
	/**
	 * swappinessの設定ボタン
	 * @return
	 */
	private LinearLayout button_swappiness()
	{
		LinearLayout tray = new LinearLayout(this);
		
		//起動時有効
		CheckBox chkbox = new CheckBox(this);
		chkbox.setId(4);
		chkbox.setText(R.string.ChkBoot);
		chkbox.setChecked(NativeCmd.fileExists(mydir + "/swappiness.sh"));
		tray.addView(chkbox, new LinearLayout.LayoutParams(ViewGroup.LayoutParams.WRAP_CONTENT, ViewGroup.LayoutParams.WRAP_CONTENT));
		
		Button btn = new Button(this);
		btn.setText(R.string.ChangeAction);
		btn.setOnClickListener(new OnClickListener() {
			@Override
			public void onClick(View v) {
				Spinner spinner = (Spinner)findViewById(10);
				String item = (String)spinner.getSelectedItem();
				String cmd = "echo " + item + " > " + swappiness;
				NativeCmd.ExecuteCommand(cmd, true);

				CheckBox chkbox = (CheckBox) findViewById(4);
				if (chkbox.isChecked()) {
					NativeCmd.createExecFile(cmd, mydir + "/swappiness.sh");
				} else {
					File file = new File(mydir + "/swappiness.sh");
					if (file.exists()) {
						file.delete();
					}
				}
				Toast.makeText(CompCache.this, R.string.SettingEnd, Toast.LENGTH_SHORT).show();
			}
		});
		tray.addView(btn, lparm);

		return tray;
	}
	
	/**
	 * 現在のパーセントを取得
	 */
	private int getCurrentSize()
	{
		int size = (int)Math.floor((double)swpsize / (double)memsize * 100);
		return size;
	}
	
	/**
	 * トータルメモリ取得
	 */
	private String getMemSize()
	{
		String cmd = "cat /proc/meminfo | " + ISTweakActivity.cmdGrep + " MemTotal | " + ISTweakActivity.cmdSed + " -e \"s/.*\\:\\s*\\([0-9]*\\)\\s.*/\\1/g\"";
	 	String[] ret = NativeCmd.ExecCommand(cmd, true);
		return ret[1].trim().replace("\n", "");
	}
	
	/**
	 * スワップサイズ取得
	 */
	private String getSwapSize()
	{
		String cmd = "cat /proc/meminfo | " + ISTweakActivity.cmdGrep + " SwapTotal | " + ISTweakActivity.cmdSed + " -e \"s/.*\\:\\s*\\([0-9]*\\)\\s.*/\\1/g\"";
		String[] ret = NativeCmd.ExecCommand(cmd, true);
		return ret[1].trim().replace("\n", "");
	}
	
	/**
	 * +-1の誤差で数値比較
	 * @param i1
	 * @param i2
	 * @return
	 */
	private boolean compInt(int i1, int i2)
	{
		if ( i1 == i2 ) {
			return true;
		}
		if ( i1 == i2 - 1) {
			return true;
		}
		if ( i1 == i2 + 1) {
			return true;
		}
		return false;
	}
}
