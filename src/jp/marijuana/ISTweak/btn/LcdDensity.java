/**
 * 解像度変更
 */
package jp.marijuana.ISTweak.btn;

import java.io.File;

import jp.marijuana.ISTweak.ISTweakActivity;
import jp.marijuana.ISTweak.R;
import jp.marijuana.ISTweak.utils.NativeCmd;

import android.app.AlertDialog;
import android.content.Context;
import android.content.DialogInterface;
import android.util.Log;
import android.view.LayoutInflater;
import android.view.View;
import android.view.View.OnClickListener;
import android.widget.Button;
import android.widget.EditText;

public class LcdDensity {
	private Context ctx;
	private AlertDialog mDlg = null;
	private Integer MaxDpi = 240;
	private Integer MinDpi = 170;
	private String AutoExec = "/data/root/autoexec.sh";
	
	public static Button getButton(Context c) {
		LcdDensity ins = new LcdDensity(c);
		return ins.makeButton();
	}
	
	private LcdDensity(Context c) {
		ctx = c;
	}
	
	private Button makeButton() {
		Button btn = new Button(ctx);
		btn.setText(R.string.btn_LcdDensity);
		btn.setOnClickListener(new OnClickListener(){
			@Override
			public void onClick(View v) {
				showLcdDensityEdit();
			}
		});
		return btn;
	}
	
	private void showLcdDensityEdit()
	{
		if ( ISTweakActivity.Model.equals("IS03") ) {
			MaxDpi = 320;
			MinDpi = 200;
		}

		LayoutInflater factory = LayoutInflater.from(ctx);
		final View entryView = factory.inflate(R.layout.input_dialog  , null);
		final EditText edit = (EditText) entryView.findViewById(R.id.int_dpi);
		String sct = ctx.getString(R.string.ScreenDPITitle) + String.format("(%d - %d)", MinDpi, MaxDpi);
		
		String lcdval = NativeCmd.ExecuteCmd(ctx, "cat " + AutoExec + "| " + ISTweakActivity.cmdGrep + " 'lcd_density\\s[1-3][0-9][0-9]'", true);
		String lcd = lcdval.replace("${ROOTPATH}/lcd_density ", "");
		if ( ! lcd.matches("^[0-9]{3}$") ) {
			lcd = MaxDpi.toString();
		}
		edit.setText(lcd);
		
		mDlg = new AlertDialog.Builder(ctx)
			.setTitle(sct)
			.setView(entryView)
			.setNeutralButton(R.string.Cancel, null)
			.setPositiveButton(R.string.ChangeAction, new DialogInterface.OnClickListener() {
				public void onClick(DialogInterface dialog, int whichButton) {
					Integer intDpi = Integer.parseInt(edit.getText().toString());
					if ( intDpi > MaxDpi ) {
						intDpi = MaxDpi;
					} else if ( intDpi < MinDpi ) {
						intDpi = MinDpi;
					}
					ChangeDPI(intDpi);
					mDlg.dismiss();
				}
			}).create();
		mDlg.show();
	}
	
	private void ChangeDPI(Integer intDpi)
	{
		String ae = NativeCmd.ExecuteCmd(ctx, "cat " + AutoExec + "| " + ISTweakActivity.cmdGrep + " 'lcd_density'", true);
		String str_preg = (ISTweakActivity.Model.equals("IS03")) ? "[2-3]" : "[1-2]";
		String dpicmd = "";
		Log.d("ISTweak", "lcd_density" + ae.toString());
		if (ae.indexOf("lcd_density") > 0) {
			dpicmd = ISTweakActivity.cmdSed + " -i 's/lcd_density " + str_preg + "[0-9][0-9]/lcd_density " + intDpi + "/' " + AutoExec;
		} else {
			File ff = new File(ctx.getDir("bin", 0), "lcd_density");
			dpicmd = "echo '" + ff.getAbsolutePath() + " " + intDpi + "' >> " + AutoExec;
		}
		NativeCmd.runScript(ctx, dpicmd, true);
	}
}
