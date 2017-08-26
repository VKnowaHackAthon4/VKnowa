package com.sogouime.hackathon4.vknowa.speech;

import java.io.ByteArrayOutputStream;
import java.io.File;
import java.io.FileOutputStream;
import java.io.IOException;
import java.nio.ByteBuffer;
import java.nio.ByteOrder;
import java.nio.ShortBuffer;
import java.util.ArrayList;
// import Arrays, 2015-05-17
import java.util.Arrays;
import java.util.List;

import android.R.integer;
import android.app.Activity;
// add Service, 2014-04-01
import android.app.Service;
// add BroadcastReceiver, 2014-04-01
import android.content.BroadcastReceiver;
import android.content.Context;
// add IntentFilter, 2014-04-01
import android.content.IntentFilter;
// add Intent, 2014-04-01
import android.content.Intent;
import android.graphics.Bitmap;
import android.graphics.Canvas;
import android.graphics.Color;
import android.graphics.CornerPathEffect;
import android.graphics.Paint;
import android.graphics.Path;
import android.graphics.PathEffect;
import android.graphics.drawable.BitmapDrawable;
// add AudioManager, 2015-08-27
import android.media.AudioManager;
import android.media.AudioManager.OnAudioFocusChangeListener;
import android.net.ConnectivityManager;
import android.os.Bundle;
import android.os.Environment;
import android.os.Handler;
import android.os.Message;
import android.telephony.TelephonyManager;
import android.text.TextUtils;
import android.util.Log;
// add KeyEvent, 2014-04-01
import android.view.KeyEvent;
import android.view.View;
import android.widget.Button;
import android.widget.EditText;
import android.widget.ImageView;
import android.widget.RadioButton;
import android.widget.RadioGroup;
import android.widget.RadioGroup.OnCheckedChangeListener;
import android.widget.TextView;

import com.sogou.speech.framework.CoreControl;
import com.sogou.speech.listener.OutsideCallListener;
import com.sogou.speech.utils.FileOperator;
import com.sogou.speech.utils.LogUtil;
import com.sogouime.hackathon4.vknowa.middle.Controller;
import com.util.WavUtil;
import com.sogouime.hackathon4.vknowa.R;

import com.sogouime.hackathon4.vknowa.R;

//@SuppressWarnings("unused")
public class NewActivity extends Activity implements OutsideCallListener {
	public static final boolean DEBUG = false;
	public static final int MSG_ON_RECORDSTOP = 0;
	public static final int MSG_ON_RESULT = 1;
	public static final int MSG_ON_ERROR = 2;
	public static final int MSG_ON_ENDOFSPEECH = 4;
	// add MSG_ON_PART_RESULT, 2013-09-23
	public static final int MSG_ON_PART_RESULT = 5;
	// add MSG_ON_QUIT_QUIETLY, 2013-09-23
	public static final int MSG_ON_QUIT_QUIETLY = 6;

	// add MSG_ON_UPDATE_TESTVIEW , 2016.4.7
	public static final int MSG_ON_UPDATE_RESULT_TEXTVIEW = 7;

	// add UpdatePartResultThread refresh interval , current is 40 ms, 2016.4.25
	public static final int UPDATE_RESULT_REFRESH_INTERVAL = 40;

	public static final int MSG_ON_BUFFERRECEIVED = 8;
	public static final int MSG_ON_SAVEFILE_FINISH = 9;

	private Button mVoiceButton;
	private TextView mVoiceStatus;
	private EditText mResultsText;
	private ImageView mWaveImage;
	private RadioGroup mRadioGroup;
	
	private UpdatePartResultThread updatePartResultThread;

	private CoreControl mCore;
	private int mStatus = DEFAULT;

	// Used to record part of that state for logging purposes.
	public static final int DEFAULT = 0;
	public static final int LISTENING = 1;
	public static final int WORKING = 2;
	public static final int ERROR = 3;

	final ByteArrayOutputStream mWaveBuffer = new ByteArrayOutputStream();
	// List<short[]> mShortData = new ArrayList<short[]>();
	int mSpeechStart;
	private boolean mEndpointed = false;

	// area : identifier for accent
	// 0 stands for mandarin, 1 stands for cantonese
	private int area = 0;

	// store all the results, 2013-09-23
	List<List<String>> wholeResult;
	
	// add partResult to store partResult, 2016.4.8
	List<List<String>> partResult;

	// add displayContent to store the content the result textview, 2016.4.7
	StringBuilder displayContent;

	// add toPopupResult to store the upcoming partresult, 2016.4.7
	ArrayList<Character> upComingResult;

	// add isReceiveFinalResult , used to control word by word showing effect,
	// 2016.4.7
	private volatile boolean isReceiveFinalResult = false;

	// add BroadcastReceiver for pressing HOME key, 2014-04-01
	private BroadcastReceiver homePressReceiver = new HomeReceiver();
	// add BroadcastReceiver for pressing POWER key, 2014-04-01
	private BroadcastReceiver powerPressReceiver = new PowerReceiver();
	// add BroadcastReceiver for receiving calls, 2014-04-01
	private BroadcastReceiver phoneStateReceiver = new PhoneReceiver();

	// add isRecognizing flag and set default value true, 2015-04-24
	private boolean isRecognizing = true;

	// add maxPureRecordingTime, measured by second, 2015-05-12
	private int maxPureRecordingTime = 60;

	// add currentRecordingShorts and maxRecordingShorts for avoiding overflow,
	// 2015-05-12
	private int currentRecordingShorts = 0;
	private int maxRecordingShorts = -1;

	// add wantedSampleRate and realSampleRate, measured by Hz, 2015-05-12
	private int wantedSampleRate = 16000;
	private int realSampleRate = -1;

	// add targetSampleRate for constructing WAV file, 2015-05-12
	private int targetSampleRate = -1;

	// add targetChannels for constructing WAV file, 1 means MONO, 2 means
	// STEREO, 2015-05-17
	private int targetChannels = 1;

	// add parameters for reducing frequency, 2015-05-12
	private final int order = 10;
	private final double gain = 2.851896e-4;
	private final double[] nCoef = { 1.0, 10.0, 45.0, 120.0, 210.0, 252, 210,
			120, 45, 10, 1 };
	private final double[] dCoef = { 1.0, -3.233136, 7.290881, -11.633385,
			14.569716, -14.497679, 11.573554, -7.310712, 3.533306, -1.203051,
			0.238175 };
	private short[] wavInPre = new short[order];
	private double[] dataPre = new double[order];

	private String mVoiceFilePath = "";
	private String mVoiceRawText = "";

	private boolean mAlreadySend = false;

	// add Audio Focus related parameters, 2015-08-27
	private AudioManager mAudioManager = null;
	OnAudioFocusChangeListener afChangeListener = new OnAudioFocusChangeListener() {
		public void onAudioFocusChange(int focusChange) {
			switch (focusChange) {
			case AudioManager.AUDIOFOCUS_LOSS:
				abandonAduioFocus();
				break;

			default:
				break;
			}
		}
	};

	private Handler mHandler = new Handler() {

		@Override
		public void handleMessage(Message msg) {
			super.handleMessage(msg);
			switch (msg.what) {
			case MSG_ON_RESULT:
				if(DEBUG){
					Log.d(TAG, "-->MSG_ON_RESULT");
				}
				mStatus = DEFAULT;
			
				// update value of isReceiveFinalResult, to stop the 
				// updateResultThread, 2016.4.7
				isReceiveFinalResult = true;
				
				// wait updating ui thread to end , 2016.4.25
				try {
					updatePartResultThread.join();
				} catch (InterruptedException e) {
					// TODO Auto-generated catch block
					e.printStackTrace();
				}
				// remove all updating ui edittext msgs , 2016.4.25
				mHandler.removeMessages(MSG_ON_UPDATE_RESULT_TEXTVIEW);
			
				mVoiceStatus.setText("start");
				mCore.destroy();
				
				// remove earlier results , current result is replacing not
				// appending , 2016.3.22
				if(wholeResult == null){
					return ;
				}
				wholeResult.clear();
				List<List<String>> ds = (List<List<String>>) msg.obj;
				int tmpAmount = ds.size();
				String deContents = "";

				for (int i = 0; i < tmpAmount; i++) {
					List<String> tmpS = new ArrayList<String>();

					for (int j = 0; j < ds.get(i).size(); j++) {
						tmpS.add(ds.get(i).get(j));
					}

					wholeResult.add(tmpS);
				}

				for (int i = 0; i < wholeResult.size(); i++) {
					// remove ',' in release version, 2013-09-23!!!
					// deContents += wholeResult.get(i).get(0) + ",";
					deContents += wholeResult.get(i).get(0);
				}

				// update mResultsText in onResult in the way consistent with onPartResult. 2016.4.20
//				mResultsText.setText(deContents);
				displayContent.delete(0, displayContent.length());
				displayContent.append(deContents);
				mResultsText.setText(displayContent.toString());

				mVoiceRawText = displayContent.toString();
				if(!mVoiceFilePath.isEmpty())
				{
					Controller.TransVoiceInfo(displayContent.toString(), getVoiceFilePath());
					mAlreadySend = true;
				}
				break;

			case MSG_ON_SAVEFILE_FINISH:
				if(!mAlreadySend && !mVoiceRawText.isEmpty())
				{
					Controller.TransVoiceInfo(displayContent.toString(), getVoiceFilePath());
					mAlreadySend = true;
				}
				break;

			case MSG_ON_RECORDSTOP:

				@SuppressWarnings("unchecked")
				ArrayList<short[]> al = (ArrayList<short[]>) msg.obj;
				int alSize = al.size();
				int alNumSize = 0;
				for (int i = 0; i < alSize; i++) {
					alNumSize += al.get(i).length;
				}
				short[] shtAl = new short[alNumSize];
				short[][] alArrays = (short[][]) al
						.toArray(new short[alSize][]);

				int tmp = 0;
				for (int i = 0; i < alSize; i++) {
					for (int j = 0; j < alArrays[i].length; j++) {
						shtAl[tmp] = alArrays[i][j];
						tmp++;
					}
				}

				// write audio to SD card start, just for debug!!!
//				 String filename = "/sdcard/datadata" + ".pcm";
//
//				 try {
//				 Log.e(TAG, "write begin");
//				 writeShorts(shtAl, filename);
//				 Log.e(TAG, "write end");
//				 } catch (IOException e) {
//
//				 e.printStackTrace();
//				 }
				// write audio to SD card start, just for debug!!!
				break;
			case MSG_ON_ERROR:
				// save audio on error, 2013-08-19
				final byte[] byteData0 = mWaveBuffer.toByteArray();

				// update value of isReceiveFinalResult, to stop
				// updateResultThread, 2016.4.7
				isReceiveFinalResult = true;
				if(DEBUG){
					Log.d(TAG, "-->MSG_ON_ERROR");
				}
				// remove saveDataToSDCardForDebug, 2014-04-01
				/*
				 * new Thread() { public void run() {
				 * saveDataToSDCardForDebug(byteData0, false); }; }.start();
				 */
				mStatus = ERROR;
				Bundle b = msg.getData();
				int err = b.getInt("ERROR");
				mResultsText.setText("error_no:" + err);
				mVoiceStatus.setText("start");
				mCore.destroy();
				break;
			case MSG_ON_ENDOFSPEECH:
				mStatus = WORKING;
				mEndpointed = true;

				final byte[] byteData = mWaveBuffer.toByteArray();
				// remove saveDataToSDCardForDebug, 2014-04-01
				
				 new Thread() { public void run() {
				 saveDataToSDCardForDebug(byteData, true); }; }.start();
				 
				// mShortBufferLength = 0;
				// for(short[] buf : mShortData) {
				// mShortBufferLength += buf.length;
				// }
				// ShortBuffer mShortBuffer = ShortBuffer.wrap(new
				// short[mShortBufferLength]);
				// for(short[] buf : mShortData) {
				// mShortBuffer.put(buf);
				// }
				// mShortBuffer.position(0);
				// showWave(mShortBuffer, 0, mShortBufferLength, mWaveImage2);

				final ShortBuffer buf = ByteBuffer
						.wrap(mWaveBuffer.toByteArray())
						.order(ByteOrder.nativeOrder()).asShortBuffer();
				buf.position(0);
				mWaveBuffer.reset();
				showWave(buf, mSpeechStart / 2, mWaveBuffer.size() / 2,
						mWaveImage);

				// reset UI and call mCore.destroy when isRecognizing is false,
				// 2015-04-24
				if (isRecognizing == true) {
					mVoiceStatus.setText("cancel");
				} else {
					mResultsText.setText("");
					mVoiceStatus.setText("start");
					mCore.destroy();
				}
				break;
			case MSG_ON_BUFFERRECEIVED:
				/*final ShortBuffer buf2 = ByteBuffer
						.wrap(mWaveBuffer.toByteArray())
						.order(ByteOrder.nativeOrder()).asShortBuffer();
				buf2.position(0);
				showWave(buf2, mSpeechStart / 2, mWaveBuffer.size() / 2,
						mWaveImage);*/
				break;

				// add MSG_ON_PART_RESULT, 2013-09-23
			case MSG_ON_PART_RESULT:
				if(DEBUG){
					Log.d(TAG, "-->MSG_ON_PART_RESULT,mStatus:"+mStatus);
				}
				// remove earlier results , current result is replacing not
				// appending , 2016.3.22
				// change wholeResult to partResult, to avoid mutual influence , 2016.4.8
				if(partResult == null){
					return ;
				}
				partResult.clear();
				List<List<String>> ds2 = (List<List<String>>) msg.obj;
				if (ds2 == null) {
					return;
				}
				int tmpAmount2 = ds2.size();
				String deContents2 = "";

				for (int i = 0; i < tmpAmount2; i++) {
					List<String> tmpS2 = new ArrayList<String>();
					for (int j = 0; j < ds2.get(i).size(); j++) {
						tmpS2.add(ds2.get(i).get(j));
					}

					partResult.add(tmpS2);
				}

				for (int i = 0; i < partResult.size(); i++) {
					// remove ',' in release version, 2013-09-23!!!
					// deContents2 += partResult.get(i).get(0) + ",";
					deContents2 += partResult.get(i).get(0);
				}

				// add diffStringContent to control the newer result , make them
				// show verbatim , 2016.4.7
				// mResultsText.setText(deContents2);
				diffStringContent(deContents2, displayContent, upComingResult);
				break;
			// add MSG_ON_QUIT_QUIETLY, 2013-09-23
			// quit quietly indicates that former packets uploaded has results, 
			// but last packet got no result  . 2016.4.8
			case MSG_ON_QUIT_QUIETLY:
				String deContents3 = "";
				// save audio on error, 2013-08-19
				final byte[] byteData2 = mWaveBuffer.toByteArray();

				// set isReceiveFinalResult to true to end result textview update , 2016.4.7
				isReceiveFinalResult = true;
				// wait updatePartResultThread to end and remove all MSG_ON_UPDATE_RESULT_TEXTVIEW , 2016.4.25
				try {
					updatePartResultThread.join();
				} catch (InterruptedException e) {
					// TODO Auto-generated catch block
					e.printStackTrace();
				}
				mHandler.removeMessages(MSG_ON_UPDATE_RESULT_TEXTVIEW);
				mStatus = ERROR;
				mVoiceStatus.setText("start");
				mCore.destroy();

				if(DEBUG){
					Log.d(TAG, "-->MSG_ON_QUIT_QUIETLY");
				}
				if(partResult == null){
					return;
				}
				// remove saveDataToSDCardForDebug, 2014-04-01
				/*
				 * new Thread() { public void run() {
				 * saveDataToSDCardForDebug(byteData2, false); }; }.start();
				 */
				
				Bundle b2 = msg.getData();
				int err2 = b2.getInt("ERROR");
				// mResultsText.setText("error_no:" + err2);
				for (int i = 0; i < partResult.size(); i++) {
					// remove ',' in release version, 2013-10-17
					// deContents3 += wholeResult.get(i).get(0) + ",";
					deContents3 += partResult.get(i).get(0);
				}

				mResultsText.setText(deContents3);

				
				break;
			case MSG_ON_UPDATE_RESULT_TEXTVIEW:
				// if display content is not empty , then update result textview, 2016.4.7
				if(!TextUtils.isEmpty(displayContent.toString())){
					mResultsText.setText(displayContent.toString());
				}
				
				break;
			}
		}
	};

	// add simple function for reducing frequency, 2015-05-28
	// change value for wavOut, 2015-06-02
	public synchronized int convert16kTo8kSimply(short[] wavIn, short[] wavOut) {
		if (wavIn == null || wavIn.length == 0 || wavOut == null
				|| wavOut.length * 2 != wavIn.length) {
			return -1;
		}

		for (int i = 0; i < wavOut.length; i++) {
			// replace odd value with mean of odd value and even value,
			// 2015-06-02
			wavOut[i] = (short) ((wavIn[2 * i] + wavIn[2 * i + 1]) / 2);
		}

		return 0;
	}

	// add function for reducing frequency, 2015-05-12
	// add synchronized restriction, 2015-05-22
	public synchronized int convert16kTo8k(short[] wavIn, short[] wavOut) {
		if (wavIn == null || wavIn.length == 0 || wavOut == null
				|| wavOut.length == 0) {
			return -1;
		}

		int len = wavIn.length;
		double tmpN = 0;
		double tmpD = 0;
		double[] data = new double[len];

		for (int i = 0; i < len; ++i) {
			tmpN = 0;
			for (int j = 0; j <= order; ++j) {
				if (i - j >= 0) {
					tmpN += nCoef[j] * wavIn[i - j] * gain;
				} else {
					tmpN += nCoef[j] * wavInPre[i - j + order] * gain;
				}
			}

			tmpD = 0;
			for (int j = 1; j <= order; ++j) {
				if (i - j >= 0) {
					tmpD += dCoef[j] * data[i - j];
				} else {
					tmpD += dCoef[j] * dataPre[i - j + order];
				}
			}
			data[i] = (tmpN - tmpD);
		}
		for (int i = 0; i < len; i += 2) {
			if (data[i] + 0.5 > 0) {
				if (data[i] + 0.5 >= 32767) {
					wavOut[i / 2] = 32767;
				} else {
					wavOut[i / 2] = (short) (data[i] + 0.5);
				}
			} else {
				if (data[i] - 0.5 <= -32768) {
					wavOut[i / 2] = -32768;
				} else {
					wavOut[i / 2] = (short) (data[i] - 0.5);
				}
			}
		}
		// For next packet
		for (int i = 0; i < order; ++i) {
			wavInPre[i] = wavIn[len - order + i];
			dataPre[i] = data[len - order + i];
		}

		return 0;
	}

	// add flag for whether it is successful, 2013-08-19
	public void saveDataToSDCardForDebug(byte[] byteData, boolean isSuccessful) {
		FileOutputStream fos = null;
		String suf = null;
		if (isSuccessful == true) {
			suf = "right_";
		} else {
			suf = "wrong_";
		}
		String dir = Environment.getExternalStorageDirectory() + "/research/voice/";
		LogUtil.log("wav dir:"+dir);
		FileOperator.createDirectory(dir , true, false);
		String filename = dir + suf
				+ String.valueOf(System.currentTimeMillis()) + ".wav";
		File outputFile = new File(filename);
		try {
			if (!outputFile.exists()) {
				outputFile.createNewFile();
				fos = new FileOutputStream(outputFile);
			} else {
				if (outputFile.isFile()) {
					FileOperator.deleteFile(outputFile);
				} else {
					FileOperator.deleteDir(outputFile);
				}
				outputFile.createNewFile();
				fos = new FileOutputStream(outputFile);
			}
			// add targetSampleRate, 2015-05-12
			// add targetChannels, 2015-05-17
			WavUtil.constructWav(fos, ByteOrder.nativeOrder(), byteData,
					targetSampleRate, targetChannels);
			fos.flush();
			fos.close();
			mVoiceFilePath = filename;
			Message msg = mHandler.obtainMessage(MSG_ON_SAVEFILE_FINISH);
			msg.sendToTarget();
		} catch (Exception e) {
			e.printStackTrace();
			if (fos != null) {
				try {
					fos.close();
				} catch (Exception e1) {
					e1.printStackTrace();
				}
				fos = null;
			}
		}
	}

	private static final String TAG = "Main Activity";

	public void onError(int err) {
		Message msg = mHandler.obtainMessage(MSG_ON_ERROR);
		// msg.obj = err;
		Bundle b = new Bundle();
		b.putInt("ERROR", err);
		msg.setData(b);
		msg.sendToTarget();
		// show error message
		/*
		 * TextView tx1 = (TextView) findViewById(R.id.text);
		 * tx1.setText("error_no:" + err);
		 */
	}

	public void onRecordStop(ArrayList<short[]> al) {
		Message msg = mHandler.obtainMessage(MSG_ON_RECORDSTOP);
		msg.obj = al;
		msg.sendToTarget();
	}

	private static void writeShorts(short[] shorts, String filename)
			throws IOException {
		File file = createFile(filename);
		FileOutputStream fos = new FileOutputStream(file);
		for (short aShort : shorts) {
			fos.write(shortToByte(aShort));
		}
		fos.close();
	}

	private static File createFile(String filename) throws IOException {
		File file = new File(filename);
		if (!file.exists()) {
			file.createNewFile();
		}
		return file;
	}

	/**
	 * new short to Byte function
	 * 
	 * @name yuanbin
	 * @date 2012-09-26
	 */
	public static byte[] shortToByte(short number) {
		int temp = number;
		byte[] b = new byte[2];
		for (int i = 0; i < b.length; i++) {
			b[i] = new Integer(temp & 0xff).byteValue();
			temp = temp >> 8;
		}
		return b;
	}

	// add function on requiring Audio Focus, 2015-08-27
	private int requireAudioFocus() {
		if (mAudioManager == null) {
			return -1;
		}

		// change AUDIOFOCUS_GAIN to AUDIOFOCUS_GAIN_TRANSIENT, 2015-08-31
		return mAudioManager.requestAudioFocus(afChangeListener,
				AudioManager.STREAM_MUSIC,
				AudioManager.AUDIOFOCUS_GAIN_TRANSIENT);
	}

	// add function on abandoning Audio Focus, 2015-08-27
	private int abandonAduioFocus() {
		if (mAudioManager == null) {
			return -1;
		}

		return mAudioManager.abandonAudioFocus(afChangeListener);
	}

	@Override
	public void onCreate(Bundle savedInstanceState) {
		
		super.onCreate(savedInstanceState);
		setContentView(R.layout.activity_new);
		
		LogUtil.setDebug(true);
		
		bindViews();
		setListeners();
		// call initReceiver, 2014-04-01
		initReceiver();
		// set value for mAudioManager, 2015-08-27
		mAudioManager = (AudioManager) getSystemService(Context.AUDIO_SERVICE);
	}

	// add initReceiver, 2014-04-01
	private void initReceiver() {
		IntentFilter homeFilter = new IntentFilter(
				Intent.ACTION_CLOSE_SYSTEM_DIALOGS);
		registerReceiver(homePressReceiver, homeFilter);
		IntentFilter powerFilter = new IntentFilter(Intent.ACTION_SCREEN_OFF);
		registerReceiver(powerPressReceiver, powerFilter);
		IntentFilter phoneFilter = new IntentFilter(
				"android.intent.action.PHONE_STATE");
		registerReceiver(phoneStateReceiver, phoneFilter);
	}

	// add onDestroy, 2014-04-01
	@Override
	protected void onDestroy() {
		super.onDestroy();

		// unregister homePressReceiver, 2014-04-01
		if (homePressReceiver != null) {
			try {
				unregisterReceiver(homePressReceiver);
				homePressReceiver = null;
			} catch (Exception e) {
				e.printStackTrace();
			}
		}

		// unregister powerPressReceiver, 2014-04-01
		if (powerPressReceiver != null) {
			try {
				unregisterReceiver(powerPressReceiver);
				powerPressReceiver = null;
			} catch (Exception e) {
				e.printStackTrace();
			}
		}

		// unregister phoneStateReceiver, 2014-04-01
		if (phoneStateReceiver != null) {
			try {
				unregisterReceiver(phoneStateReceiver);
				phoneStateReceiver = null;
			} catch (Exception e) {
				e.printStackTrace();
			}
		}
		
		
	}

	private void bindViews() {
		mVoiceButton = (Button) findViewById(R.id.button1);
		mVoiceStatus = (TextView) findViewById(R.id.tvvoicestatus);
		mResultsText = (EditText) findViewById(R.id.text);
		mWaveImage = (ImageView) findViewById(R.id.voice_wave_image);
	}

	private void setListeners() {
		// Click Listener
		mVoiceButton.setOnClickListener(new View.OnClickListener() {

			@Override
			public void onClick(View v) {
				switch (mStatus) {
				case LISTENING:
					stopListening();
					// showDefault();
					break;
				case WORKING:
					cancelListening();
					break;
				case DEFAULT:
					startListening();
					break;
				case ERROR:
					startListening();
					break;
				}
			}
		});
	}

	@Override
	public void onEndOfSpeech() {
		// call abandonAduioFocus, 2015-08-27
		abandonAduioFocus();
		mHandler.obtainMessage(MSG_ON_ENDOFSPEECH).sendToTarget();
	}

	/**
	 * Shows waveform of input audio.
	 * 
	 * Copied from version in VoiceSearch's RecognitionActivity.
	 * 
	 * TODO: adjust stroke width based on the size of data. TODO: use dip rather
	 * than pixels.
	 */
	private void showWave(ShortBuffer waveBuffer, int startPosition,
			int endPosition, ImageView imageview) {
		final int w = imageview.getWidth();
		final int h = imageview.getHeight();
		if (w <= 0 || h <= 0) {
			// view is not visible this time. Skip drawing.
			return;
		}
		recylceWaveBitmap(imageview);
		final Bitmap b = Bitmap.createBitmap(w, h, Bitmap.Config.ARGB_8888);
		final Canvas c = new Canvas(b);
		final Paint paint = new Paint();
		paint.setColor(Color.WHITE); // 0xAARRGGBB
		paint.setAntiAlias(true);
		paint.setStyle(Paint.Style.STROKE);
		paint.setAlpha(0x90);

		final PathEffect effect = new CornerPathEffect(3);
		paint.setPathEffect(effect);

		final int numSamples = waveBuffer.remaining();
		int endIndex;
		if (endPosition == 0) {
			endIndex = numSamples;
		} else {
			endIndex = Math.min(endPosition, numSamples);
		}

		int startIndex = startPosition - 2000; // include 250ms before speech
		if (startIndex < 0) {
			startIndex = 0;
		}
		final int numSamplePerWave = 200; // 8KHz 25ms = 200 samples
		final float scale = 10.0f / 65536.0f;

		final int count = (endIndex - startIndex) / numSamplePerWave;
		final float deltaX = 1.0f * w / count;
		int yMax = h / 2 - 8;
		Path path = new Path();
		c.translate(0, yMax);
		float x = 0;
		path.moveTo(x, 0);
		for (int i = 0; i < count; i++) {
			final int avabs = getAverageAbs(waveBuffer, startIndex, i,
					numSamplePerWave);
			int sign = ((i & 01) == 0) ? -1 : 1;
			final float y = Math.min(yMax, avabs * h * scale) * sign;
			path.lineTo(x, y);
			x += deltaX;
			path.lineTo(x, y);
		}
		if (deltaX > 4) {
			paint.setStrokeWidth(3);
		} else {
			paint.setStrokeWidth(Math.max(1, (int) (deltaX - .05)));
		}
		c.drawPath(path, paint);
		imageview.setImageBitmap(b);
		imageview.setVisibility(View.VISIBLE);
		// MarginLayoutParams mProgressParams =
		// (MarginLayoutParams)mProgress.getLayoutParams();
		// mProgressParams.topMargin = (int)
		// TypedValue.applyDimension(TypedValue.COMPLEX_UNIT_PX,
		// -h , mContext.getResources().getDisplayMetrics());

		// Tweak the padding manually to fill out the whole view horizontally.
		// TODO: Do this in the xml layout instead.
		// ((View) mImage.getParent()).setPadding(4, ((View)
		// mImage.getParent()).getPaddingTop(), 3,
		// ((View) mImage.getParent()).getPaddingBottom());
		// mProgress.setLayoutParams(mProgressParams);
	}

	/**
	 * @return an average abs of the specified buffer.
	 */
	private static int getAverageAbs(ShortBuffer buffer, int start, int i,
			int npw) {
		int from = start + i * npw;
		int end = from + npw;
		int total = 0;
		for (int x = from; x < end; x++) {
			total += Math.abs(buffer.get(x));
		}
		return total / npw;
	}

	public void recylceWaveBitmap(ImageView waveImage) {
		if (waveImage != null) {
			BitmapDrawable d = (BitmapDrawable) waveImage.getDrawable();
			if (d != null) {
				d.setCallback(null);
				if (!d.getBitmap().isRecycled()) {
					d.getBitmap().recycle();
				}
				d = null;
			}
			waveImage.setImageDrawable(null);
			waveImage.setImageBitmap(null);
		}
		mVoiceStatus.setText("start");
		mResultsText.setText("");
	}

	private void startListening() {
		// initialize currentRecordingShorts, maxRecordingShorts, realSampleRate
		// and targetSampleRate, 2015-05-15
		currentRecordingShorts = 0;
		maxRecordingShorts = -1;
		realSampleRate = -1;
		targetSampleRate = -1;

		mVoiceFilePath = "";
		mVoiceRawText = "";
		mAlreadySend = false;

		// initialize wavInPre and dataPre, 2015-05-17
		Arrays.fill(wavInPre, (short) 0);
		Arrays.fill(dataPre, 0);

		// initialize wholeResult, 2013-09-23
		// initialize partResult , 2016.4.8
		wholeResult = new ArrayList<List<String>>();
		partResult = new ArrayList<List<String>>();

		// initialize upcomingResult and displayContent, 2016.4.7
		displayContent = new StringBuilder();
		upComingResult = new ArrayList<Character>();

		// add context for CoreControl, 2013-04-12
		Context mCtx = getApplicationContext();

		// remove TelephonyManager and ConnectivityManager to CoreControl
		// inside, give CoreControl mCtx instead of TelephonyManager and
		// ConnectivityManager, update by yuanbin on 2013-08-08
		// add continuous recognition flag, 2013-09-23
		// add isRecognizing flag, 2015-04-24
		// add maxPureRecordingTime, 2015-05-12
		// set isContinuous=true, test continuous online recognition 2016.3.22
		mCore = new CoreControl(0/*area*/, 5, 0, 1, 0, 0, 0, 0, 2, "", mCtx, true,
				"/sdcard/", true, isRecognizing, maxPureRecordingTime);

		mCore.setRecognizingListener(this);
		// get maxRecordingTime measured by second, 2015-04-24
		int maxRecordingTime = mCore.getMaxRecordingTime();
		// set isReceiveFinalResult false, 2016.4.7
		isReceiveFinalResult = false;
		mStatus = LISTENING;
		if (DEBUG) {
			Log.d(TAG, "-->startListening");
		}
		mVoiceStatus.setText("stop");// the code does not work!!!
		// change default information, 2015-04-24
		mResultsText.setText("maxRecordingTime:" + maxRecordingTime + " seconds");
		mCore.startListening();
		// mShortData.clear();
		
		//start update result textview, 2016.4.8
		updatePartResultThread = new UpdatePartResultThread();
		updatePartResultThread.start();
	}

	private void stopListening() {
		mStatus = WORKING;
		mCore.stopListening();
		mVoiceStatus.setText("cancel");
		// set isReceiveFinalResult = true , 2016.4.7
		if (DEBUG) {
			Log.d(TAG, "-->stopListening");
		}
	}

	private void cancelListening() {
		isReceiveFinalResult = true;
		mStatus = DEFAULT;
		mCore.cancelListening();
		mVoiceStatus.setText("start");
		mResultsText.setText("");
		mCore.destroy();
		//wait updatePartResultThread to finish
		try {
			updatePartResultThread.join();
		} catch (InterruptedException e) {
			// TODO Auto-generated catch block
			e.printStackTrace();
		}
		if (DEBUG) {
			Log.d(TAG, "-->cancelListening");
		}
	}

	/**
	 * compare partResult and displayStr, and store the difference part in
	 * queue, and make content in queue pop up one by one to make verbatim
	 * effect
	 * 
	 * @author liukeang
	 * @date 2016.4.7
	 * @param params
	 */
	public void diffStringContent(String partResult, StringBuilder displayStr,
			ArrayList<Character> queue) {
		if (TextUtils.isEmpty(partResult)) {
			return;
		}

		if (TextUtils.isEmpty(displayStr)) {
			for (Character c : partResult.toCharArray()) {
				queue.add(c);
			}
			return;
		}

		if (partResult.length() <= displayStr.length()) {
			displayStr.delete(0, displayStr.length());
			displayStr.append(partResult);
		} else {
			int displaylen = displayStr.length();
			displayStr.delete(0, displaylen);
			displayStr.append(partResult.substring(0, displaylen));
			for (int i = displaylen; i < partResult.length(); i++) {
				queue.add(partResult.charAt(i));
			}
		}
		// put MSG_ON_UPDATE_RESULT_TEXTVIEW only in UpdateThread, so comment this line. 2016.4.25 
//		mHandler.obtainMessage(MSG_ON_UPDATE_RESULT_TEXTVIEW).sendToTarget();
	}

	@Override
	public void onReadyForSpeech(Bundle params) {
		// call requireAudioFocus, 2015-08-27
		requireAudioFocus();
	}

	@Override
	public void onBeginningOfSpeech() {
		mSpeechStart = mWaveBuffer.size();

		// move here, 2015-05-15
		// set value for realSampleRate, 2015-05-12
		if (realSampleRate == -1) {
			realSampleRate = mCore.getRealSampleRate();
		}

		// set value for maxRecordingShorts, 2015-05-12
		// multiply targetChannels for maxRecordingShorts, 2015-06-09
		if (maxRecordingShorts == -1) {
			maxRecordingShorts = realSampleRate * maxPureRecordingTime
					* targetChannels;
		}

		// set value for targetSampleRate, 2015-05-12
		if (targetSampleRate == -1) {
			if (realSampleRate == 8000 && wantedSampleRate == 16000) {
				targetSampleRate = realSampleRate;
			} else {
				targetSampleRate = wantedSampleRate;
			}
		}
	}

	public String getVoiceFilePath() {
		return  mVoiceFilePath;
	}

	@Override
	public void onRmsChanged(float rmsdB) {
		mHandler.obtainMessage(MSG_ON_BUFFERRECEIVED).sendToTarget();
	}

	// remove onResults(DecodeSpeech results), 2013-09-23

	@Override
	public void onBufferReceived(short[] voiceBuffer) {
		// mShortData.add(voiceBuffer);

		// ByteOrder bo = ByteOrder.nativeOrder();

		// validate parameters, 2015-05-17
		if (voiceBuffer == null || voiceBuffer.length == 0) {
			return;
		}
		// construct STEREO when necessary, 2015-05-17
		if (targetChannels == 2) {
			short[] stereoBuffer = new short[voiceBuffer.length * 2];
			for (int i = 0; i < voiceBuffer.length; i++) {
				stereoBuffer[2 * i] = voiceBuffer[i];
				stereoBuffer[2 * i + 1] = voiceBuffer[i];
			}
			voiceBuffer = stereoBuffer;
		}

		// avoid overflow, 2015-05-12
		if (currentRecordingShorts + voiceBuffer.length > maxRecordingShorts) {
			int lastLen = maxRecordingShorts - currentRecordingShorts;
			short[] lastVoice = new short[lastLen];
			for (int i = 0; i < lastLen; i++) {
				lastVoice[i] = voiceBuffer[i];
			}

			voiceBuffer = lastVoice;
		} else {
			currentRecordingShorts += voiceBuffer.length;
		}

		// reducing frequency when necessary, 2015-05-12
		if (wantedSampleRate == 8000 && realSampleRate == 16000) {
			// store 8KHz data in finalVoice, 2015-05-12
			short[] finalVoice = new short[voiceBuffer.length / 2];
			// replace convert16kTo8k with convert16kTo8kSimply, 2015-05-28
			if (convert16kTo8kSimply(voiceBuffer, finalVoice) == 0) {
				for (short singlebuf : finalVoice) {
					try {
						mWaveBuffer.write((byte) (singlebuf & 0x00ff));
						mWaveBuffer.write((byte) ((singlebuf >> 8) & 0x00ff));
					} catch (Exception e) {
						// TODO: handle exception
					}
				}
			}
			return;
		}

		for (short singlebuf : voiceBuffer) {
			try {
				// if (bo.equals(ByteOrder.BIG_ENDIAN)) {
				mWaveBuffer.write((byte) (singlebuf & 0x00ff));
				mWaveBuffer.write((byte) ((singlebuf >> 8) & 0x00ff));
				// } else {
				// mWaveBuffer.write((byte)((singlebuf >> 8) & 0x00ff));
				// mWaveBuffer.write((byte)(singlebuf & 0x00ff));
				// }
			} catch (Exception e) {
			}
		}
	}

	@Override
	public void onResults(List<List<String>> results) {
		Message msg = mHandler.obtainMessage(MSG_ON_RESULT);
		msg.obj = results;
		msg.sendToTarget();
	}

	@Override
	public void onPartResults(List<List<String>> results) {
		Message msg = mHandler.obtainMessage(MSG_ON_PART_RESULT);
		msg.obj = results;
		msg.sendToTarget();
	}

	public void onQuitQuietly(int err) {
		Message msg = mHandler.obtainMessage(MSG_ON_QUIT_QUIETLY);
		Bundle b = new Bundle();
		b.putInt("ERROR", err);
		msg.setData(b);
		msg.sendToTarget();
	}

	public void onClientClick(int sentenceSquenceNo) {
		// TODO:when use continuous recognition and allow user modify displayed
		// sentences with candidates, realize it, 2013-09-23
	}

	public void onClientChoose(int sentenceSequenceNo, int resultOrderId) {
		// TODO:when use continuous recognition and allow user modify displayed
		// sentences with candidates, realize it, 2013-09-23
	}

	public void onClientUpdate(int sentenceSequenceNo, int resultOrderId) {
		// TODO:when use continuous recognition and allow user modify displayed
		// sentences with candidates, realize it, 2013-09-23
	}

	// when click BACK key, if it is active, call cancelListening,
	// 2014-04-01
	@Override
	public boolean onKeyDown(int keyCode, KeyEvent keyDownEvent) {
		if (keyCode == KeyEvent.KEYCODE_BACK) {
			if (mStatus != DEFAULT) {
				cancelListening();
				// just for debug!!!
				// Log.e("onKeyDown", "keyCode:" + keyCode);
			}
		}

		return super.onKeyDown(keyCode, keyDownEvent);
	}

	// add pressing HOME key broadcast receiver, 2014-04-01
	class HomeReceiver extends BroadcastReceiver {
		final String SYSTEM_DIALOG_REASON_KEY = "reason";
		final String SYSTEM_DIALOG_REASON_HOME_KEY = "homekey";

		@Override
		public void onReceive(Context homeContext, Intent homeIntent) {
			String homeAction = homeIntent.getAction();
			if (homeAction.equals(Intent.ACTION_CLOSE_SYSTEM_DIALOGS)) {
				String homeReason = homeIntent
						.getStringExtra(SYSTEM_DIALOG_REASON_KEY);
				if (homeReason != null
						&& homeReason.equals(SYSTEM_DIALOG_REASON_HOME_KEY)) {
					if (mStatus != DEFAULT) {
						cancelListening();
						// just for debug!!!
						// Log.e("HomePressReceiver", "Home key is pressed.");
					}
				}
			}
		}
	};

	// add pressing POWER key broadcast receiver, 2014-04-01
	class PowerReceiver extends BroadcastReceiver {
		@Override
		public void onReceive(Context powerContext, Intent powerIntent) {
			final String powerAction = powerIntent.getAction();
			if (Intent.ACTION_SCREEN_OFF.equals(powerAction)) {
				if (mStatus != DEFAULT) {
					cancelListening();
					// just for debug!!!
					// Log.e("PowerReceiver", "Power key is pressed.");
				}
			}
		}
	};

	// add receiving calls broadcast receiver, 2014-04-01
	class PhoneReceiver extends BroadcastReceiver {
		private boolean incomingFlag = false;

		@Override
		public void onReceive(Context phoneContext, Intent phoneIntent) {
			if (phoneIntent.getAction().equals(
					"android.intent.action.PHONE_STATE")) {
				TelephonyManager tm = (TelephonyManager) phoneContext
						.getSystemService(Service.TELEPHONY_SERVICE);
				if (tm == null) {
					return;
				}

				switch (tm.getCallState()) {
				case TelephonyManager.CALL_STATE_RINGING:
					// incoming ringing state
					incomingFlag = true;
					// just for debug!!!
					// Log.e("PhoneStateReciver", "ringing:" + mStatus);
					break;

				case TelephonyManager.CALL_STATE_OFFHOOK:
					if (incomingFlag) {
						// incoming offhook state
						if (mStatus != DEFAULT) {
							cancelListening();
							// just for debug!!!
							// Log.e("PhoneStateReciver", "offhook:" + mStatus);
						}
					}
					break;

				case TelephonyManager.CALL_STATE_IDLE:
				default:
					// just for debug!!!
					// Log.e("PhoneStateReciver", "idle:" + mStatus);
					break;
				}
			}
		}
	}

	class UpdatePartResultThread extends Thread {
		/*
		 * (non-Javadoc)
		 * 
		 * @see java.lang.Thread#run()
		 */
		@Override
		public void run() {
			super.run();
			while (!isReceiveFinalResult) {

				try {
					Thread.sleep(UPDATE_RESULT_REFRESH_INTERVAL);
					if (upComingResult.isEmpty()) {
						continue;
					} else {
						Character word = upComingResult.remove(0);
						displayContent.append(word);
						mHandler.obtainMessage(MSG_ON_UPDATE_RESULT_TEXTVIEW)
								.sendToTarget();
					}
				} catch (InterruptedException e) {
					// TODO Auto-generated catch block
					e.printStackTrace();
				}

			}

		}
	}
}
