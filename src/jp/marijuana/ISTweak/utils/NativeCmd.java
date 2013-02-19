/**
 * Nativeコマンド実行など
 */
package jp.marijuana.ISTweak.utils;

import java.io.BufferedReader;
import java.io.ByteArrayOutputStream;
import java.io.File;
import java.io.FileNotFoundException;
import java.io.FileOutputStream;
import java.io.FileReader;
import java.io.IOException;
import java.io.InputStream;
import java.io.InputStreamReader;
import java.io.OutputStream;
import java.io.OutputStreamWriter;
import java.io.PrintWriter;

import jp.marijuana.ISTweak.ISTweakActivity;

import android.content.Context;
import android.util.Log;

public class NativeCmd
{
	public static String au = "/sbin/au";
	public static String sh = "/system/bin/sh";
	
	private static class StreamGobbler extends Thread
	{
		InputStream is;
		OutputStream os;

		StreamGobbler(InputStream is, OutputStream redirect)
		{
			this.is = is;
			this.os = redirect;
		}
		
		public void run()
		{
			PrintWriter pw = new PrintWriter(os);
			InputStreamReader isr = new InputStreamReader(is);
			BufferedReader br = new BufferedReader(isr);
			String line = null;
			try {
				while ((line = br.readLine()) != null) {
					pw.println(line);
				}
			} catch (IOException exception) {
				exception.printStackTrace();
			}
			pw.flush();
		}
	}	
	
	/**
	 * ファイル（ディレクト）の存在チェック
	 * @param String fn
	 * @return boolean
	 */
	public static boolean fileExists(String fn)
	{
		return new File(fn).exists();
	}
	
	/**
	 * 複数コマンドの実行
	 * @param String[] cmds
	 * @param Boolean su
	 * @return String[]
	 */
	public static String[] ExecCommands(String[] cmds, Boolean su)
	{
		String[] rets = new String[3];
		String shell = NativeCmd.sh;
		if ( su ) {
			shell = NativeCmd.au;
		}

		ByteArrayOutputStream std;
		ByteArrayOutputStream err;
		StreamGobbler stdGobbler;
		StreamGobbler errGobbler;
		
		try {
			Process proc = Runtime.getRuntime().exec(shell);
			OutputStream os = proc.getOutputStream();
			String cmd = "";
			for (int i = 0; i < cmds.length; i++) {
				cmd = cmds[i] + "\n";
				os.write(cmd.getBytes());
			}
			os.close();
			os.flush();
			std = new ByteArrayOutputStream();
			stdGobbler = new StreamGobbler(proc.getInputStream(), std);
			stdGobbler.start();

			err = new ByteArrayOutputStream();
			errGobbler = new StreamGobbler(proc.getErrorStream(), err);
			errGobbler.start();

			rets[0] = String.valueOf(proc.waitFor());
			stdGobbler.join();
			errGobbler.join();
			
			rets[1] = new String(std.toByteArray());
			rets[2] = new String(err.toByteArray());

			std.close();
			err.close();
		} catch (Throwable t) {
			Log.e("ISTweak", t.toString());
		}

		return rets;
	}
	
	/**
	 * コマンドの実行
	 * @param String　cmd
	 * @param Boolean　su
	 * @return　String[]
	 */
	public static String[] ExecCommand(String cmd, Boolean su)
	{
		String[] arrayOfCmd = new String[1];
		arrayOfCmd[0] = cmd;
		return ExecCommands(arrayOfCmd, su);
	}
	
	/**
	 * プロパティ値の取得
	 * @param key
	 * @return　String
	 */
	public static String getProperties(String key)
	{
		String line = "";
		try {
			Process p = Runtime.getRuntime().exec("getprop " + key);
			BufferedReader input = new BufferedReader(new InputStreamReader(p.getInputStream()));
			line = input.readLine();
			input.close();
			p.destroy();
		} catch (Exception err) {
			Log.e("ISTweak", "none " + key);
		}
		return line.trim().replace("\n", "");
	}
	
	/**
	 * コマンドを実行する(戻り値がある場合はアラート)
	 * @param Context ctx
	 * @param String cmd
	 * @param Boolean su
	 */
	public static boolean ExecuteCmdAlert(Context ctx, String cmd, Boolean su)
	{
		String[] ret = new String[3];
    	ret = NativeCmd.ExecCommand(cmd, su);
    	
    	if ( ret[1].length() > 0 ) {
    		ISTweakActivity.alert(ctx, ret[1]);
    		Log.d("ISTweak", ret[1]);
    	}
    	if ( ret[2].length() > 0 ) {
    		ISTweakActivity.alert(ctx, ret[2]);
    		Log.e("ISTweak", ret[2]);
    		return false;
    	} 
    	return true;
	}
	
	/**
	 * 戻り値なしのコマンドを実行する
	 * @param String　paramCommand
	 * @param Boolean su
	 */
	public static void ExecuteCommand(String cmd, Boolean su)
	{
		String[] arrayOfString = new String[1];
		arrayOfString[0] = cmd;
		ExecuteCommands(arrayOfString, su);
	}
	
	/**
	 * 戻り値なしのコマンドを複数実行する
	 * @param String[] cmds
	 * @param Boolean su
	 */
	public static void ExecuteCommands(String[] cmds, Boolean su)
	{
		String shell = NativeCmd.sh;
		if ( su ) {
			shell = NativeCmd.au;
		}
		
		try {
			Process proc = Runtime.getRuntime().exec(shell);
			OutputStream os = proc.getOutputStream();
			
			String cmd = "";
			for (int i = 0; i < cmds.length; i++) {
				cmd = cmds[i] + "\n\n";
				os.write(cmd.getBytes());
			}
			os.close();
		} catch (IOException e) {
			Log.e("ISTweak", e.toString());
		}
	}


	
 	/**
	 * ファイルを1行読み込み
	 * @param fn ファイル名
	 * @return
	 */
	public static String readFile(String fn)
	{
		String str = "";
		try {
			FileReader fr = new FileReader(new File(fn));
			BufferedReader br = new BufferedReader(fr);
			str = br.readLine();
			br.close();
			fr.close();
		} catch (FileNotFoundException e) {
	 		Log.e("ISTweak", e.toString());
	 	} catch (IOException e) {
	 		Log.e("ISTweak", e.toString());
	 	}
		return str.trim().replace("\n", "");
	}
	
	/**
	 * 実行ファイルを作成
	 * @param cmd コマンド
	 * @param fn	ファイル名（フルパス）
	 */
	public static void createExecFile(String cmd, String fn)
	{
		try {
			final OutputStreamWriter out = new OutputStreamWriter(new FileOutputStream(fn));
			out.write("#!/system/bin/sh\n");
			out.write("export PATH=/data/root/bin:\"$PATH\"\n");
			out.write(cmd);
			out.write("\nexit 0\n");
			out.flush();
			out.close();
			ExecuteCommand("chmod 0777 " + fn, true);
		} catch (Exception e) {
			Log.e("ISTweak", e.toString());
		}
	}
}
