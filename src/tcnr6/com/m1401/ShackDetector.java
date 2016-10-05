package tcnr6.com.m1401;


import android.hardware.Sensor;
import android.hardware.SensorEvent;
import android.hardware.SensorEventListener;
import android.hardware.SensorManager;
import android.util.FloatMath;

public class ShackDetector implements SensorEventListener {
	
//  <!--------設定G力所使用到的變數--------->
	private static final float ShakeThresholdGravity=2.1f;//重力閾值  1次要甩多大力
	private static final int ShakeSlopTimeMs=500; //甩1次要在0.5秒內完成才算1次
	private static final int ShakeCountResetTimeMs=3000;//多久沒甩 計數器會重製
//	<---------------------------------->
	// <----------------設定加速度計監聽會使用到的變數------------------->
	private OnShakeListener mListener;// 搖動監聽器
	private long mShakeTimestamp;// 暫存開始搖動的系統時間
	public static int mShakeCount = 0;// 紀錄搖動次數的計數器
	// <!-------------------------------------->
	// <!---------------自建立搖動監聽器----------------------->

	public interface OnShakeListener { // 自建立一個發生搖動時候的監聽器
		public void onShake(int count);
	}

	public void setOnShakeListener(OnShakeListener listener) { // 宣告設定搖動監聽器的method
		this.mListener = listener;
	}

	// <---------------------------------------------------->



	@Override
	public void onSensorChanged(SensorEvent event) {
		// TODO Auto-generated method stub
		if (mListener != null) {
			float x = event.values[0];
			float y = event.values[1];
			float z = event.values[2];
 
			float gX = x / SensorManager.GRAVITY_EARTH;
			float gY = y / SensorManager.GRAVITY_EARTH;
			float gZ = z / SensorManager.GRAVITY_EARTH;
 
			// gForce will be close to 1 when there is no movement.
			float gForce = FloatMath.sqrt(gX * gX + gY * gY + gZ * gZ);
 
			if (gForce > ShakeThresholdGravity) {
				final long now = System.currentTimeMillis();
				// ignore shake events too close to each other (500ms)
				if (mShakeTimestamp + ShakeSlopTimeMs > now) {
					return;
				}
				// reset the shake count after 3 seconds of no shakes
				if (mShakeTimestamp + ShakeCountResetTimeMs < now) {
					mShakeCount = 0;
				}
 
				mShakeTimestamp = now;
				mShakeCount++;
 
				mListener.onShake(mShakeCount);
				
			}
			
		}		
	}

	@Override
	public void onAccuracyChanged(Sensor sensor, int accuracy) {
		// TODO Auto-generated method stub
		
	}

}
