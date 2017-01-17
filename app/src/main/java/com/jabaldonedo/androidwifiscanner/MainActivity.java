package com.jabaldonedo.androidwifiscanner;

import android.app.Activity;
import android.content.BroadcastReceiver;
import android.content.Context;
import android.content.Intent;
import android.content.IntentFilter;
import android.content.res.Configuration;
import android.graphics.Typeface;
import android.os.Bundle;
import android.support.v4.content.LocalBroadcastManager;
import android.util.Log;
import android.view.Gravity;
import android.view.Menu;
import android.widget.LinearLayout;
import android.widget.TableLayout;
import android.widget.TableRow;
import android.widget.TextView;

public class MainActivity extends Activity {

	private final String TAG = "MainActivity";
	private WifiData mWifiData;

	@SuppressWarnings("deprecation")
	@Override
	protected void onCreate(Bundle savedInstanceState) {
		super.onCreate(savedInstanceState);

		mWifiData = null;

		// set receiver
		MainActivityReceiver mReceiver = new MainActivityReceiver();
		LocalBroadcastManager.getInstance(this).registerReceiver(mReceiver, new IntentFilter(Constants.APP_NAME));

		// launch WiFi service
		Intent intent = new Intent(this, WifiService.class);
		startService(intent);

		// recover retained object
		mWifiData = (WifiData) getLastNonConfigurationInstance();

		// set layout
		setContentView(R.layout.activity_main);
		plotData();
	}

	@Override
	public boolean onCreateOptionsMenu(Menu menu) {
		// Inflate the menu; this adds items to the action bar if it is present.
		getMenuInflater().inflate(R.menu.main, menu);
		return true;
	}

	@Override
	public void onPause() {
		super.onPause();
	}

	@Override
	public void onDestroy() {
		super.onDestroy();
	}

	@Override
	public Object onRetainNonConfigurationInstance() {
		return mWifiData;
	}

	public void plotData() {
		LinearLayout linearLayout = (LinearLayout) findViewById(R.id.scanningResultBlock);
		linearLayout.removeAllViews();

		if (mWifiData == null) {
			Log.d(TAG, "Plotting data: no networks");
			TextView noDataView = new TextView(this);
			noDataView.setText(getResources().getString(R.string.wifi_no_data));
			noDataView.setGravity(Gravity.CENTER_HORIZONTAL);
			noDataView.setPadding(0, 50, 0, 0);
			noDataView.setTextSize(18);
			linearLayout.addView(noDataView);
		} else {
			Log.d(TAG, "Plotting data");

			TableLayout.LayoutParams tableParams = new TableLayout.LayoutParams(TableLayout.LayoutParams.WRAP_CONTENT,
					TableLayout.LayoutParams.WRAP_CONTENT);
			TableRow.LayoutParams rowParams = new TableRow.LayoutParams(TableRow.LayoutParams.WRAP_CONTENT,
					TableRow.LayoutParams.WRAP_CONTENT);

			TableLayout tableLayout = new TableLayout(this);
			tableLayout.setLayoutParams(tableParams);
			tableLayout.setStretchAllColumns(true);

			// row header
			TableRow tableRowHeader = new TableRow(this);
			tableRowHeader.setLayoutParams(rowParams);

			TextView ssidText = new TextView(this);
			ssidText.setText(getResources().getString(R.string.ssid_text));
			ssidText.setTypeface(null, Typeface.BOLD);

			TextView chText = new TextView(this);
			chText.setText(getResources().getString(R.string.ch_text));
			chText.setTypeface(null, Typeface.BOLD);

			TextView rxText = new TextView(this);
			rxText.setText(getResources().getString(R.string.rx_text));
			rxText.setTypeface(null, Typeface.BOLD);

			TextView freqText = new TextView(this);
			freqText.setText("freq(MHz)");
			freqText.setTypeface(null, Typeface.BOLD);

			int orientation = getResources().getConfiguration().orientation;
			if (orientation == Configuration.ORIENTATION_LANDSCAPE) {
				TextView bssidText = new TextView(this);
				bssidText.setText(getResources().getString(R.string.bssid_text));
				bssidText.setTypeface(null, Typeface.BOLD);

				tableRowHeader.addView(ssidText);
				tableRowHeader.addView(bssidText);
				tableRowHeader.addView(chText);
				tableRowHeader.addView(rxText);
				tableRowHeader.addView(freqText);
			} else {
				tableRowHeader.addView(ssidText);
				tableRowHeader.addView(chText);
				tableRowHeader.addView(rxText);
			}

			tableLayout.addView(tableRowHeader);

			// rows data
			for (WifiDataNetwork net : mWifiData.getNetworks()) {
				TextView ssidVal = new TextView(this);
				ssidVal.setText(net.getSsid());

				TextView chVal = new TextView(this);
				chVal.setText(String.valueOf(WifiDataNetwork.convertFrequencyToChannel(net.getFrequency())));

				TextView rxVal = new TextView(this);
				rxVal.setText(String.valueOf(net.getLevel()));

				TableRow tableRow = new TableRow(this);
				tableRow.setLayoutParams(rowParams);

				TextView freqVal = new TextView(this);
				freqVal.setText(String.valueOf(net.getFrequency()));


				if (orientation == Configuration.ORIENTATION_LANDSCAPE) {
					TextView bssidVal = new TextView(this);
					bssidVal.setText(net.getBssid());

					rxVal.setText(String.valueOf(net.getLevel()) + " dBm");

					tableRow.addView(ssidVal);
					tableRow.addView(bssidVal);
					tableRow.addView(chVal);
					tableRow.addView(rxVal);
					tableRow.addView(freqVal);


				} else {
					tableRow.addView(ssidVal);
					tableRow.addView(chVal);
					tableRow.addView(rxVal);
				}

				tableLayout.addView(tableRow);
			}

			linearLayout.addView(tableLayout);
		}
	}

	public class MainActivityReceiver extends BroadcastReceiver {

		@Override
		public void onReceive(Context context, Intent intent) {
			WifiData data = (WifiData) intent.getParcelableExtra(Constants.WIFI_DATA);

			if (data != null) {
				mWifiData = data;
				plotData();
			}
		}

	}
}
