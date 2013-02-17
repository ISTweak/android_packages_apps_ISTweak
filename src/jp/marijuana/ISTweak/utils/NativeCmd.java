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

public class NativeCmd {
	private static final String SCRIPT_FILE = "execcmd.sh";
	public static String au = "/sbin/au";
	
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
	 * プロパティ値の取得
	 * @param key
	 * @return
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
	 * コマンドを実行する
	 * @param Context ctx
	 * @param String cmd
	 * @param Boolean su
	 */
	public static String ExecuteCmd(Context ctx, String cmd, Boolean su)
	{
		String[] ret = new String[3];
		String script = "";
		if ( su ) {
			script = NativeCmd.au + " -c " + cmd;
		} else {
			script = cmd;
		}
		ret = NativeCmd.ExecCommand(script);
		return ret[1] + ret[2];
	}
	
	/**
	 * コマンドを実行する(エラーがある場合はアラート)
	 * @param Context ctx
	 * @param String cmd
	 * @param Boolean su
	 */
	public static boolean ExecuteCmdAlert(Context ctx, String cmd, Boolean su)
	{
		String[] ret = new String[2];
		String script = "";
		if ( su ) {
			script = NativeCmd.au + " -c " + cmd;
		} else {
			script = cmd;
		}
    	ret = NativeCmd.ExecCommand(script);
    	
    	if ( ret[2].length() > 0 ) {
    		ISTweakActivity.alert(ctx, ret[2]);
    		Log.d("ISTweak", ret[2]);
    		return false;
    	} 
    	return true;
	}
	
	/**
	 * 戻り値なしのコマンドを実行する
	 * @param String　paramCommand
	 * @throws IOException
	 */
	public static void ExecuteCommand(String paramCommand) throws IOException
	{
		String[] arrayOfString = new String[1];
		arrayOfString[0] = (paramCommand + "\n\n");
		ExecuteCommands(arrayOfString);
	}
	
	/**
	 * 戻り値なしのコマンドを複数実行する
	 * @param String[] paramArrayOfString
	 * @throws IOException
	 */
	public static void ExecuteCommands(String[] paramArrayOfString) throws IOException
	{
		OutputStream localOutputStream = Runtime.getRuntime().exec(NativeCmd.au).getOutputStream();
		int i = paramArrayOfString.length;
		for (int j = 0; j < i; j++) {
			localOutputStream.write(paramArrayOfString[j].getBytes());
		}
		localOutputStream.close();
	}
	
	private static final class StreamGobbler extends Thread
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
	 * コマンドを実行して各種出力を取得
	 * @param String cmd
	 * @return String[]
	 */
	public static String[] ExecCommand(String cmd)
	{
		String[] ret = new String[3];
		
		ByteArrayOutputStream out;
		ByteArrayOutputStream err;
		StreamGobbler outGobbler;
		StreamGobbler errGobbler;
		
		try {
			Process proc = Runtime.getRuntime().exec(cmd);

			out = new ByteArrayOutputStream();
			outGobbler = new StreamGobbler(proc.getInputStream(), out);
			outGobbler.start();

			err = new ByteArrayOutputStream();
			errGobbler = new StreamGobbler(proc.getErrorStream(), err);
			errGobbler.start();

			ret[0] = String.valueOf(proc.waitFor());
			outGobbler.join();
			errGobbler.join();
			
			ret[1] = new String(out.toByteArray());
			ret[2] = new String(err.toByteArray());
			
			out.close();
			err.close();
		} catch (Throwable t) {
			Log.e("ISTweak", t.toString());
		}
		return ret;
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
			ExecuteCommand("chmod 0777 " + fn);
		} catch (Exception e) {
			Log.e("ISTweak", e.toString());
		}
	}
	
	/**
	 * スクリプトを書き出して実行
	 * @param Context　ctx
	 * @param String　cmd　スクリプトに書き出すコマンド
	 * @param boolean　asroot rootで実行するか
	 * @return
	 */
	public static String[] runScript(Context ctx, String cmd, boolean asroot)
	{
		File execfile = new File(ctx.getCacheDir(), SCRIPT_FILE);
		try {
			execfile.createNewFile();
		} catch (IOException e) {
			Log.e("ISTweak", e.toString());
		}
		
		final String fullpath = execfile.getAbsolutePath();
		createExecFile(cmd, fullpath);
		
		String script = "";
		if ( asroot ) {
			script = NativeCmd.au + " -c " + fullpath;
		} else {
			script = fullpath;
		}
		return ExecCommand(script);
	}
}
