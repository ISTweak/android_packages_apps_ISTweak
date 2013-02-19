package jp.marijuana.ISTweak.btn;

import jp.marijuana.ISTweak.ISTweakActivity;
import jp.marijuana.ISTweak.R;
import jp.marijuana.ISTweak.utils.NativeCmd;
import android.content.Context;
import android.view.View;
import android.view.View.OnClickListener;
import android.widget.Button;

public class LifeLog
{
	private Context ctx;
	private final String[] tb = {"T_COMMUNICATION", "T_INFORMATION", "T_LIFELOG", "T_MAIL", "T_PERSON", "T_PHONE_BOOK", "T_SPAREPARTS", "T_SUMMARY"};
	private final String[] tbb = {"T_LIFELOG", "T_PERSON", "T_PHONE_BOOK"};
	private final String db = "/ldb/ldb.db";
	private final String dbb = "/ldb/ldbbackup.db";
	
	public static Button getButton(Context c)
	{
		LifeLog ins = new LifeLog(c);
		return ins.makeButton();
	}
	
	private LifeLog(Context c)
	{
		ctx = c;
	}
	
	private Button makeButton()
	{
		Button btn = new Button(ctx);
		if ( chkTriger() ) {
			btn.setText(R.string.btn_LifeLogTri);
			btn.setOnClickListener(new OnClickListener(){
				@Override
				public void onClick(View v) {
					addTriger();
					ISTweakActivity.ctx.finish();
				}
			});
		} else {
			btn.setText(R.string.btn_LifeLogRec);
			btn.setOnClickListener(new OnClickListener(){
				@Override
				public void onClick(View v) {
					delTriger();
					ISTweakActivity.ctx.finish();
				}
			});
		}
		return btn;
	}
	
	private boolean chkTriger()
	{
		String cmd = "sqlite3 " + db + " \".schema T_COMMUNICATION\"|" + ISTweakActivity.cmdGrep + " TRIGGER";
		String[] ret = NativeCmd.ExecCommand(cmd, true);
		return (ret[1].trim().replace("\n", "").length() == 0);
	}
	
	private void addTriger()
	{
		String cmd = "";
		for (int i = 0; i < tb.length; i++) {
			cmd += "sqlite3 " + db + " \"delete from " + tb[i] + ";\"\n";
			cmd += "sqlite3 " + db + " \"" + makeTrigger(tb[i]) + "\"\n";
		}
		cmd += "sqlite3 " + db + " \"vacuum\"\n";
		
		for (int i = 0; i < tbb.length; i++) {
			cmd += "sqlite3 " + dbb + " \"delete from " + tbb[i] + ";\"\n";
			cmd += "sqlite3 " + dbb + " \"" + makeTrigger(tbb[i]) + "\"\n";
		}		
		cmd += "sqlite3 " + dbb + " \"vacuum\"\n";
		
		NativeCmd.ExecuteCommands(cmd.split("\n"), true);
	}
	
	private void delTriger()
	{
		String cmd = "";
		for (int i = 0; i < tb.length; i++) {
			cmd += "sqlite3 " + db + " \"" + dropTriger(tb[i]) + "\"\n";
			cmd += "sqlite3 " + db + " \"delete from " + tb[i] + ";\"\n";
		}
		cmd += "sqlite3 " + db + " \"vacuum\"\n";
		
		for (int i = 0; i < tbb.length; i++) {
			cmd += "sqlite3 " + dbb + " \"" + dropTriger(tbb[i]) + "\"\n";
			cmd += "sqlite3 " + dbb + " \"delete from " + tbb[i] + ";\"\n";
		}		
		cmd += "sqlite3 " + dbb + " \"vacuum\"\n";
		
		NativeCmd.ExecuteCommands(cmd.split("\n"), true);
	}
	
	private String makeTrigger(String t)
	{
		String sql = "create trigger " + t + "_TRI " +
					 "insert on " + t + " " +
					 "begin " +
					 "delete from " + t + "; " +
					 "end;";
		return sql;
	}
	
	private String dropTriger(String t)
	{
		return "drop trigger " + t + "_TRI";
	}
}
