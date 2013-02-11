/**
 * Nativeコマンド実行など
 */
package jp.marijuana.ISTweak.utils;

import java.io.BufferedReader;
import java.io.File;
import java.io.FileNotFoundException;
import java.io.FileOutputStream;
import java.io.FileReader;
import java.io.IOException;
import java.io.InputStreamReader;
import java.io.OutputStream;
import java.io.OutputStreamWriter;

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
		StringBuilder res = new StringBuilder();
		NativeCmd.runScript(ctx, cmd, res, 0, su);
		if ( res.length() > 0 ) {
			return res.toString().trim();
		}
		return "";
	}
	
	/**
	 * コマンドを実行する(戻り値がある場合はアラート)
	 * @param Context ctx
	 * @param String cmd
	 * @param Boolean su
	 */
	public static boolean ExecuteCmdAlert(Context ctx, String cmd, Boolean su)
	{
		StringBuilder res = new StringBuilder();
		NativeCmd.runScript(ctx, cmd, res, 0, su);
		if ( res.length() > 0 ) {
			Log.d("ISTweak", res.toString());
			ISTweakActivity.alert(ctx, res.toString());
			return false;
		}
		return true;
	}
	
	public static void ExecuteCommand(String paramArrayOfString) throws IOException
	{
		String[] arrayOfString = new String[1];
		arrayOfString[0] = (paramArrayOfString + "\n\n");
		ExecuteCommands(arrayOfString);
	}
	
	public static void ExecuteCommands(String[] paramArrayOfString) throws IOException
	{
		OutputStream localOutputStream = Runtime.getRuntime().exec(NativeCmd.au).getOutputStream();
		int i = paramArrayOfString.length;
		for (int j = 0; j < i; j++) {
			localOutputStream.write(paramArrayOfString[j].getBytes());
		}
		localOutputStream.close();
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
		return str.trim();
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
			Runtime.getRuntime().exec("chmod 0777 " + fn).waitFor();
		} catch (Exception e) {
			Log.e("ISTweak", e.toString());
		}
	}
	
	/**
	 * Runs a script, wither as root or as a regular user (multiple commands separated by "\n").
	 * @param ctx mandatory context
	 * @param cmd the script to be executed
	 * @param res the script output response (stdout + stderr)
	 * @param timeout timeout in milliseconds (-1 for none)
	 * @return the script exit code
	 */
	public static int runScript(Context ctx, String cmd, StringBuilder res, long timeout, boolean asroot)
	{
		final ScriptRunner runner = new ScriptRunner(ctx, cmd, res, asroot);
		runner.start();
		try {
			if (timeout > 0) {
				runner.join(timeout);
			} else {
				runner.join();
			}
			if (runner.isAlive()) {
				// Timed-out
				runner.interrupt();
				runner.join(150);
				runner.destroy();
				runner.join(50);
			}
		} catch (InterruptedException ex) {}
		return runner.exitcode;
	}

	/**
	 * Internal thread used to execute scripts (as root or not).
	 */
	private static final class ScriptRunner extends Thread
	{
		private final File execfile;
		private final String script;
		private final StringBuilder res;
		private final boolean asroot;
		public int exitcode = -1;
		private Process exec;
		
		/**
		 * Creates a new script runner.
		 * @param script script to run
		 * @param res response output
		 */
		public ScriptRunner(Context ctx,String script, StringBuilder res, boolean asroot) {
			res.setLength(0);
			this.script = script;
			this.res = res;
			this.asroot = asroot;
			this.execfile = new File(ctx.getCacheDir(), SCRIPT_FILE);
		}
		
		@Override
		public void run() {
			try {
				execfile.createNewFile();
				final String abspath = execfile.getAbsolutePath();
				Runtime.getRuntime().exec("chmod 0777 " + abspath).waitFor();
				final OutputStreamWriter out = new OutputStreamWriter(new FileOutputStream(execfile));
				out.write("#!/system/bin/sh\n");
				out.write("export PATH=/data/root/bin:\"$PATH\"\n");
				out.write(script);
				out.write("\nexit 0\n");
				out.flush();
				out.close();
				
				if ( this.asroot ) {
					exec = Runtime.getRuntime().exec(NativeCmd.au + " -c " + abspath);
				} else {
					exec = Runtime.getRuntime().exec(abspath);
				}
				InputStreamReader r = new InputStreamReader(exec.getInputStream());
				final char buf[] = new char[1024];
				int read = 0;
				// Consume the "stdout"
				while ((read = r.read(buf)) != -1) {
					if (res != null) res.append(buf, 0, read);
				}
				// Consume the "stderr"
				r = new InputStreamReader(exec.getErrorStream());
				read = 0;
				while ((read = r.read(buf)) != -1) {
					if (res != null) res.append(buf, 0, read);
				}
				// get the process exit code
				if (exec != null) this.exitcode = exec.waitFor();
			} catch (InterruptedException ex) {
				if (res != null) res.append("\nOperation timed-out");
			} catch (Exception ex) {
				if (res != null) res.append("\n" + ex);
			} finally {
				destroy();
			}
		}
		/**
		 * Destroy this script runner
		 */
		public synchronized void destroy() {
			if (exec != null) exec.destroy();
			exec = null;
		}
	}
}
