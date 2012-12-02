/**
 * 起動時の自動実行
 */
package jp.marijuana.ISTweak;

import jp.marijuana.ISTweak.utils.NativeCmd;
import android.content.BroadcastReceiver;
import android.content.Context;
import android.content.Intent;

public class autostart  extends BroadcastReceiver {
	@Override
	public void onReceive(Context context, Intent intent) {
		onCreate(context);
	}
	
	public void onCreate(Context ctx) {
		if ( NativeCmd.fileExists("/data/root/autostart.sh") ) {
			NativeCmd.ExecuteCmd(ctx, "/data/root/autostart.sh", true);
		}
		
		final String dir = ctx.getDir("bin", 0).getAbsolutePath();
		if ( NativeCmd.fileExists(dir + "/boot.sh") ) {
			NativeCmd.ExecuteCmd(ctx, dir + "/boot.sh", true);
		}
		if ( NativeCmd.fileExists(dir + "/vdd.sh") ) {
			NativeCmd.ExecuteCmd(ctx, dir + "/vdd.sh", true);
		}
		if ( NativeCmd.fileExists(dir + "/swappiness.sh") ) {
			NativeCmd.ExecuteCmd(ctx, dir + "/swappiness.sh", true);
		}
	}
}
