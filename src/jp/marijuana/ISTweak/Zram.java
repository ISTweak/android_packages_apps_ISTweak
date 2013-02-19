package jp.marijuana.ISTweak;

import java.io.File;

import jp.marijuana.ISTweak.utils.NativeCmd;
import android.app.Activity;
import android.os.Bundle;
import android.text.InputFilter;
import android.text.InputType;
import android.view.View;
import android.view.ViewGroup;
import android.view.View.OnClickListener;
import android.view.ViewGroup.LayoutParams;
import android.widget.ArrayAdapter;
import android.widget.Button;
import android.widget.CheckBox;
import android.widget.EditText;
import android.widget.LinearLayout;
import android.widget.Spinner;
import android.widget.TextView;
import android.widget.Toast;

public class Zram extends Activity
{
	private final LayoutParams lparm = new LinearLayout.LayoutParams(ViewGroup.LayoutParams.WRAP_CONTENT, ViewGroup.LayoutParams.WRAP_CONTENT);
	private final String disksize = "/sys/block/zram0/disksize";
	private final String swappiness = "/proc/sys/vm/swappiness";
	private final String compsize = "/data/root/compsize";
	private int zramsize = 0;
	private String mydir = "";
	
	@Override
	public void onCreate(Bundle savedInstanceState)
	{
		super.onCreate(savedInstanceState);
		setContentView(R.layout.overclock);
		
		LinearLayout layout = (LinearLayout) findViewById(R.id.ocLayout01);
		
		mydir = this.getDir("bin", 0).getAbsolutePath();
		
		TextView tit = new TextView(this);
		tit.setText(R.string.ZramSettings);
		tit.setText(tit.getText() + " " + String.valueOf(Integer.parseInt(getZramCurrent()) / 1024 /1024) + "MB");
		layout.addView(tit);
		
		layout.addView(TextBoxZram());
		layout.addView(ButtonZram());
		
		layout.addView(spinner_swappiness());
		layout.addView(button_swappiness());
	}
	
	private LinearLayout TextBoxZram()
	{
		LinearLayout tray = new LinearLayout(this);
		TextView sctit = new TextView(this);
		sctit.setText(R.string.Str_Zramsize);
		tray.addView(sctit, lparm);
		
		zramsize = Integer.parseInt(getZramSize()) / 1024 /1024;
		EditText edit = new EditText(this);
		edit.setId(1);
		edit.setWidth(110);
		edit.setInputType(InputType.TYPE_CLASS_NUMBER);
		
		InputFilter[] inlength = new InputFilter[1];
		inlength[0] = new InputFilter.LengthFilter(3);
		edit.setFilters(inlength);
		
		edit.setText(String.valueOf(zramsize));
		tray.addView(edit, lparm);
		
		TextView par = new TextView(this);
		par.setText("MB");
		tray.addView(par, lparm);
		return tray;
	}
	
	private LinearLayout ButtonZram()
	{
		LinearLayout tray = new LinearLayout(this);
		
		Button btn = new Button(this);
		btn.setText(R.string.ChangeAction);
		btn.setOnClickListener(new OnClickListener() {
			@Override
			public void onClick(View v) {
				EditText edit = (EditText)findViewById(1);
				String cmd = "echo " + Integer.parseInt(edit.getText().toString()) * 1024 * 1024 + " > ";
				NativeCmd.ExecuteCommands(new String[]{cmd + compsize, "chmod 0666 " + compsize}, true);
				Toast.makeText(Zram.this, R.string.str_ToReboot, Toast.LENGTH_LONG).show();
				finish();
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
					finish();
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
				Toast.makeText(Zram.this, R.string.SettingEnd, Toast.LENGTH_SHORT).show();
				finish();
			}
		});
		tray.addView(btn, lparm);

		return tray;
	}

	private String getZramSize()
	{
		if (NativeCmd.fileExists(compsize)) {
			String cmd = "cat " + compsize;
			String[] ret = NativeCmd.ExecCommand(cmd, true);
			return ret[1].trim().replace("\n", "");
		}
		return "0";
	}

	private String getZramCurrent()
	{
		String cmd = "cat " + disksize;
		String[] ret = NativeCmd.ExecCommand(cmd, true);
		return ret[1].trim().replace("\n", "");
	}
	
}
