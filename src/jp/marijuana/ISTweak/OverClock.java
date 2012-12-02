/**
 * オーバークロック
 */
package jp.marijuana.ISTweak;

import java.io.BufferedReader;
import java.io.File;
import java.io.FileNotFoundException;
import java.io.FileOutputStream;
import java.io.FileReader;
import java.io.IOException;
import java.io.OutputStreamWriter;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.HashMap;
import java.util.Map.Entry;

import jp.marijuana.ISTweak.utils.NativeCmd;

import android.app.Activity;
import android.app.AlertDialog;
import android.content.DialogInterface;
import android.os.Bundle;
import android.util.Log;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.view.View.OnClickListener;
import android.view.ViewGroup.LayoutParams;
import android.widget.AdapterView;
import android.widget.ArrayAdapter;
import android.widget.Button;
import android.widget.CheckBox;
import android.widget.EditText;
import android.widget.LinearLayout;
import android.widget.ListView;
import android.widget.Spinner;
import android.widget.TextView;
import android.widget.Toast;
import android.widget.AdapterView.OnItemClickListener;

public class OverClock extends Activity {
	private HashMap<Integer, String> clockmap = new HashMap<Integer, String>();
	private HashMap<Integer, String> vddmap = new HashMap<Integer, String>();
	private final String scaling_min_freq = "/sys/devices/system/cpu/cpu0/cpufreq/scaling_min_freq";
	private final String scaling_max_freq = "/sys/devices/system/cpu/cpu0/cpufreq/scaling_max_freq";
	private final String vdd_levels = "/sys/devices/system/cpu/cpu0/cpufreq/vdd_levels";
	private final String scaling_governor = "/sys/devices/system/cpu/cpu0/cpufreq/scaling_governor";
	private final String scaling_available_governors = "/sys/devices/system/cpu/cpu0/cpufreq/scaling_available_governors";
	private final String cpuinfo_max_freq = "/sys/devices/system/cpu/cpu0/cpufreq/cpuinfo_max_freq";
	private final String cpuinfo_min_freq = "/sys/devices/system/cpu/cpu0/cpufreq/cpuinfo_min_freq";
	private String scheduler = "/sys/block/mtdblock1/queue/scheduler";
	
	private final LayoutParams lparm = new LinearLayout.LayoutParams(ViewGroup.LayoutParams.WRAP_CONTENT, ViewGroup.LayoutParams.WRAP_CONTENT);
	private String mydir = "";
	private AlertDialog m_Dlg = null;
	private AlertDialog dlgVdd = null;
	
	@Override
	public void onCreate(Bundle savedInstanceState) {
		mydir = this.getDir("bin", 0).getAbsolutePath();
		super.onCreate(savedInstanceState);
		setContentView(R.layout.overclock);
		LinearLayout layout = (LinearLayout) findViewById(R.id.ocLayout01);
		
		TextView tit = new TextView(this);
		tit.setText(R.string.ClockSetting);
		layout.addView(tit);
		
		if ( !NativeCmd.fileExists(scheduler) ) {
			scheduler = "/sys/block/mmcblk0/queue/scheduler";
		}
		
		read_clock();
		//min/max clock Spinner
		makeClockTray(layout);
		//scaling
		makeScalingTray(layout);
		//scheduler
		makeSchedulerTray(layout);
		//起動時有効
		CheckBox chkbox = new CheckBox(this);
		chkbox.setId(4);
		chkbox.setText(R.string.ChkBoot);
		chkbox.setChecked(NativeCmd.fileExists(mydir + "/boot.sh"));
		layout.addView(chkbox, new LinearLayout.LayoutParams(ViewGroup.LayoutParams.WRAP_CONTENT, ViewGroup.LayoutParams.WRAP_CONTENT));
		//VDDの設定を保存する
		CheckBox chkvdd = new CheckBox(this);
		chkvdd.setId(5);
		chkvdd.setText(R.string.Save_VDD);
		chkvdd.setChecked(NativeCmd.fileExists(mydir + "/vdd.sh"));
		layout.addView(chkvdd, new LinearLayout.LayoutParams(ViewGroup.LayoutParams.WRAP_CONTENT, ViewGroup.LayoutParams.WRAP_CONTENT));
		//設定ボタン
		Button btn = new Button(this);
		btn.setText(R.string.ChangeAction);
		btn.setOnClickListener(new OnClickListener(){
			@Override
			public void onClick(View v) {
				saveStat();
			}
		});
		layout.addView(btn);
		//VDD
		Button btnvdd = new Button(this);
		btnvdd.setText(R.string.SettingVddLevel);
		btnvdd.setOnClickListener(new OnClickListener(){
			@Override
			public void onClick(View v) {
				showVddList();
			}
		});
		layout.addView(btnvdd);
	}
	
	/**
	 * VDDリスト
	 */
	private void showVddList() {
		read_clock();
		final ArrayList<String> rows = new ArrayList<String>();
		Object[] key = vddmap.keySet().toArray();
		Arrays.sort(key);
		for (int i = 0 ; i < key.length ; i ++ ) {
 			String[] cl = vddmap.get(key[i]).split(":");
 			int s = Integer.parseInt(cl[0].trim());
 			String s1 = String.valueOf(s / 1000) + "MHz";
			String s2 = s1 + ":" + String.valueOf(Integer.parseInt(cl[1].trim())) + "mV";
			rows.add(s2);
		}
		
		ListView lv = new ListView(this);
		lv.setAdapter(new ArrayAdapter<String>(this, android.R.layout.simple_list_item_1, rows));
		lv.setScrollingCacheEnabled(false);
		lv.setOnItemClickListener(new OnItemClickListener() {
			@Override
			 public void onItemClick(AdapterView<?> items, View view, int position, long id) {
				m_Dlg.dismiss();
				showVddDialog(position);
			}
		});

		// ダイアログを表示
		m_Dlg = new AlertDialog.Builder(this)
			.setTitle(R.string.SelectVddLevel)
			.setPositiveButton(R.string.Cancel, null)
			.setView(lv)
			.create();
		m_Dlg.show();
	}
	
	/**
	 * VDDダイアログ
	 * @param i
	 */
	private void showVddDialog(int i) {
		LayoutInflater factory = LayoutInflater.from(getApplicationContext());
		final View entryView = factory.inflate(R.layout.vdd_dialog  , null);
		final EditText edit = (EditText) entryView.findViewById(R.id.int_vdd);
		
		String[] cl = vddmap.get(i).split(":");
		edit.setText(String.valueOf(Integer.parseInt(cl[1].trim())));
		edit.setTag(String.valueOf(Integer.parseInt(cl[0].trim())));
		
		dlgVdd = new AlertDialog.Builder(this)
			.setTitle(String.valueOf(Integer.parseInt(cl[0].trim()) / 1000) + "MHz")
			.setView(entryView)
			.setNeutralButton(R.string.Cancel, null)
			.setPositiveButton(R.string.ChangeAction, new DialogInterface.OnClickListener() {
				public void onClick(DialogInterface dialog, int whichButton) {
					dlgVdd.dismiss();
					String cmd = "echo '" + edit.getTag().toString() + " " + edit.getText() + "' > " + vdd_levels;
					if (NativeCmd.ExecuteCmdAlert(OverClock.this, cmd, true)) {
						Log.i("ISTweak", cmd);
					}
				}
			}).create();
		dlgVdd.show();
	}
	
	/**
	 * ブロックデバイスの取得
	 * @return
	 */
	private String getAllBlockDevice() {
		String path = "/sys/block";
	    File dir = new File(path);
	    File[] files = dir.listFiles();
	    String blockcmd = "";
	    
	    if ( NativeCmd.fileExists(scheduler) ) {
		    String Scheduler = makeScheduler();
		    
		    for (int i = 0; i < files.length; i++) {
		        File file = files[i];
		        if ( file.toString().indexOf("/sys/block/mtd") == 0   ) {
		        	blockcmd += "echo " + Scheduler + " > " + file.toString() + "/queue/scheduler\n";
		        	Log.i("ISTweak", file.toString());
		        } else if ( file.toString().indexOf("/sys/block/mmc") == 0 ) {
		        	blockcmd += "echo " + Scheduler + " > " + file.toString() + "/queue/scheduler\n";
		        	Log.i("ISTweak", file.toString());
		        } else if ( file.toString().indexOf("/sys/block/stheno") == 0 ) {
		        	blockcmd += "echo " + Scheduler + " > " + file.toString() + "/queue/scheduler\n";
		        	Log.i("ISTweak", file.toString());
		        }
		    }
		}
	    return blockcmd;
	}
	
	/**
	 * 保存
	 */
	private void saveStat() {
		getAllBlockDevice();
		String cmd = "echo " + String.valueOf(makeMinFreq()) + " > " + scaling_min_freq + "\n" +
					 "echo " + String.valueOf(makeMaxFreq()) + " > " + scaling_max_freq + "\n" +
					 "echo " + makeScaling() + " > " + scaling_governor + "\n" +
					 getAllBlockDevice() +
				"";
		if (NativeCmd.ExecuteCmdAlert(this, cmd, true)) {
			Log.i("ISTweak", "set cpu freq and scaling");
		}
		
		if (NativeCmd.ExecuteCmdAlert(this, "echo 3 > /proc/sys/vm/drop_caches", true)) {
			Log.i("ISTweak", "drop_caches");
		}		
		
		CheckBox chkbox = (CheckBox) findViewById(4);
		if (chkbox.isChecked()) {
			createBoot(cmd);
			Log.i("ISTweak", "make: boot.sh");
		} else {
			File file = new File(mydir + "/boot.sh");
			if (file.exists()) {
				file.delete();
				Log.i("ISTweak", "delete: boot.sh");
			}
		}
		
		//saveVDD()
		CheckBox chkvdd = (CheckBox) findViewById(5);
		if (chkvdd.isChecked()) {
			saveVDD();
			Log.i("ISTweak", "make: vdd.sh");
		} else {
			File file = new File(mydir + "/vdd.sh");
			if (file.exists()) {
				file.delete();
				Log.i("ISTweak", "delete: vdd.sh");
			}
		}
		
		Toast.makeText(this, R.string.SettingEnd, Toast.LENGTH_SHORT).show();
	}
	
	/**
	 * boot.shの作成
	 * @param cmd
	 */
	private void createBoot(String cmd) {
		try {
			final OutputStreamWriter out = new OutputStreamWriter(new FileOutputStream(mydir + "/boot.sh"));
			out.write("#!/system/bin/sh\n");
			out.write(cmd);
			out.write("\nexit 0\n");
			out.flush();
			out.close();
			Runtime.getRuntime().exec("chmod 0777 " + mydir + "/boot.sh").waitFor();
		} catch (Exception e) {
			Log.e("ISTweak", e.toString());
		}
	}
	
	/**
	 * スケジューラー
	 * @return
	 */
	private String makeScheduler() {
		Spinner spinner = (Spinner) findViewById(6);
		return (String) spinner.getSelectedItem();
	}
	
	/**
	 * スケーリング
	 * @return
	 */
	private String makeScaling() {
		Spinner spinner = (Spinner) findViewById(3);
	 	return (String) spinner.getSelectedItem();
	}
	
	/**
	 * 最小クロック
	 * @return
	 */
	private int makeMinFreq() {
		Spinner spinner = (Spinner) findViewById(1);
		String item = (String) spinner.getSelectedItem();
		int min = 192000;
		for (Entry<Integer, String> e : clockmap.entrySet()){
			if( e.getValue().equals(item) ) {
				min = e.getKey();
				break;
			}
		}
		return Math.max(min, Integer.parseInt(NativeCmd.readFile(cpuinfo_min_freq)));
	}
	
	/**
	 * 最大クロック
	 * @return
	 */
	private int makeMaxFreq() {
		Spinner spinner = (Spinner) findViewById(2);
		String item = (String) spinner.getSelectedItem();
		int max = 1152000;
		for (Entry<Integer, String> e : clockmap.entrySet()){
			if( e.getValue().equals(item) ) {
				max = e.getKey();
				break;
			}
		}
		return Math.min(max, Integer.parseInt(NativeCmd.readFile(cpuinfo_max_freq)));
	}
	
	/**
	 * スケジューラー
	 * @param layout
	 */
	private void makeSchedulerTray(LinearLayout layout) {
		if ( NativeCmd.fileExists(scheduler) ) {
			LinearLayout tray = new LinearLayout(this);
			
			TextView sctit = new TextView(this);
			sctit.setText(R.string.Scheduler);
			tray.addView(sctit, lparm);
			
			ArrayAdapter<String> adapter = new ArrayAdapter<String>(this, android.R.layout.simple_spinner_item);
			adapter.setDropDownViewResource(android.R.layout.simple_spinner_dropdown_item);
			
			Spinner ssch = new Spinner(this);
			ssch.setPromptId(R.string.Scheduler);
			ssch.setId(6);
			
			//noop anticipatory deadline cfq [sio] bfq vr
			int def = 0;
			String str = NativeCmd.readFile(scheduler);
			String[] sl = str.split(" ");
	 		for ( int i = 0; i < sl.length; i++ ) {
	 			if ( sl[i].substring(0, 1).equals(new String("[")) ) {
	 				def = i;
	 				sl[i] = sl[i].replace("[", "").replace("]", "");
	 			}
	 			adapter.add(sl[i]);
	 		}
	 		ssch.setAdapter(adapter);
	 		ssch.setSelection(def);
	 		tray.addView(ssch, lparm);
	
			layout.addView(tray);
		}
	}
	
	/**
	 * スケーリングリスト
	 * @param layout
	 */
	private void makeScalingTray(LinearLayout layout) {
		LinearLayout tray = new LinearLayout(this);
		
		TextView sctit = new TextView(this);
		sctit.setText(R.string.Scaling);
		tray.addView(sctit, lparm);
		
		ArrayAdapter<String> adapter = new ArrayAdapter<String>(this, android.R.layout.simple_spinner_item);
		adapter.setDropDownViewResource(android.R.layout.simple_spinner_dropdown_item);
		
		Spinner scal = new Spinner(this);
		scal.setPromptId(R.string.Scaling);
		scal.setId(3);
		
		String defsc = NativeCmd.readFile(scaling_governor);
		int def = 0;
		String str = NativeCmd.readFile(scaling_available_governors);
		String[] sl = str.split(" ");
 		for ( int i = 0; i < sl.length; i++ ) {
 			adapter.add(sl[i]);
 			if ( defsc.equals(sl[i].trim()) ) {
 				def = i;
 			}
 		}
 		scal.setAdapter(adapter);
 		scal.setSelection(def);
 		tray.addView(scal, lparm);

		layout.addView(tray);
	}
	
	/**
	 * クロックのリスト
	 * @param layout
	 */
	private void makeClockTray(LinearLayout layout) {
		LinearLayout tray = new LinearLayout(this);
		
		ArrayAdapter<String> minadp = new ArrayAdapter<String>(this, android.R.layout.simple_spinner_item);
		minadp.setDropDownViewResource(android.R.layout.simple_spinner_dropdown_item);
		int min = Integer.parseInt(NativeCmd.readFile(scaling_min_freq));
		int defmin = 0;
		Object[] key = clockmap.keySet().toArray();
		Arrays.sort(key);
		for (int i = 0 ; i < key.length ; i ++ ) {
			minadp.add(clockmap.get(key[i]));
			if ( min == Integer.parseInt(key[i].toString()) ) {
				defmin = i;
			}
		}
		//min clock
		Spinner mincl = new Spinner(this);
		mincl.setPromptId(R.string.MinClock);
		mincl.setId(1);
		mincl.setAdapter(minadp);
		mincl.setSelection(defmin);
		tray.addView(mincl, lparm);
		
		//～
		TextView mincap = new TextView(this);
		mincap.setText(R.string.KARA);
		tray.addView(mincap, lparm);

		ArrayAdapter<String> maxadp = new ArrayAdapter<String>(this, android.R.layout.simple_spinner_item);
		maxadp.setDropDownViewResource(android.R.layout.simple_spinner_dropdown_item);
		int max = Integer.parseInt(NativeCmd.readFile(scaling_max_freq));
		int defmax = 0;
		key = clockmap.keySet().toArray();
		Arrays.sort(key);
		for (int i = 0 ; i < key.length ; i ++ ) {
			maxadp.add(clockmap.get(key[i]));
			if ( max == Integer.parseInt(key[i].toString()) ) {
				defmax = i;
			}
		}
		//max clock
		Spinner maxcl = new Spinner(this);
		maxcl.setPromptId(R.string.MaxClock);
		maxcl.setId(2);
		maxcl.setAdapter(maxadp);
		maxcl.setSelection(defmax);
		tray.addView(maxcl, lparm);
		
		layout.addView(tray);
	}
	
	/**
	 * vdd_levelsからクロックの一覧を読み込む
	 */
	private void read_clock() {
	 	try {
	 		FileReader fr = new FileReader(new File(vdd_levels));
	 		BufferedReader br = new BufferedReader(fr);
			  
	 		String str;
	 		int i = 0;
	 		while((str = br.readLine()) != null){
	 			String[] cl = str.split(":");
	 			int s = Integer.parseInt(cl[0].trim());
	 			String s1 = String.valueOf(s / 1000) + "MHz";
	 			clockmap.put(s, s1);
	 			vddmap.put(i , str);
	 			i++;
	 		}
	 		
	 		br.close();
	 		fr.close();
	 	} catch (FileNotFoundException e) {
	 		Log.e("ISTweak", e.toString());
	 	} catch (IOException e) {
	 		Log.e("ISTweak", e.toString());
	 	}
	}
	
	/**
	 * VDDの保存
	 */
	private void saveVDD() {
	 	try {
			final FileReader fr = new FileReader(new File(vdd_levels));
			final BufferedReader br = new BufferedReader(fr);

	 		String str = "";
	 		String cmd = "";
	 		while((str = br.readLine()) != null){
	 			String[] line = str.split(":");
	 			int cl = Integer.parseInt(line[0].trim());
	 			int vl = Integer.parseInt(line[1].trim());
	 			cmd+= "echo '" + cl + " " + vl + "' > " + vdd_levels + "\n";
	 		}
	 		br.close();
	 		fr.close();

	 		NativeCmd.createExecFile(cmd, mydir + "/vdd.sh");
	 	} catch (FileNotFoundException e) {
	 		Log.e("ISTweak", e.toString());
	 	} catch (IOException e) {
	 		Log.e("ISTweak", e.toString());
		}
	}
}
