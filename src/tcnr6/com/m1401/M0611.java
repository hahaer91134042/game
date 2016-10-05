package tcnr6.com.m1401;




import android.app.Activity;
import android.app.Notification;
import android.app.NotificationManager;
import android.app.PendingIntent;
import android.app.Service;
import android.content.Context;
import android.content.Intent;
import android.content.SharedPreferences;
import android.hardware.Sensor;
import android.hardware.SensorManager;
import android.media.AudioManager;
import android.media.MediaPlayer;
import android.media.ToneGenerator;
import android.os.Bundle;
import android.os.Handler;
import android.os.Vibrator;
import android.view.Menu;
import android.view.MenuItem;
import android.view.View;

import android.view.animation.AnimationUtils;
import android.widget.Button;

import android.widget.ImageButton;
import android.widget.ImageSwitcher;
import android.widget.ImageView;

import android.widget.TextView;
import android.widget.Toast;
import android.widget.TableRow.LayoutParams;
import android.widget.ViewSwitcher.ViewFactory;

import tcnr6.com.m1401.ShackDetector.OnShakeListener;







public class M0611 extends Activity implements ViewFactory {	
	
	private ImageButton B001,B002,B003;
	private TextView T006;
	
	private MediaPlayer M001,M002,M003,M004,M005,musicHot;
//<!----------動畫用變數-------------------->	
	int t=0;//紀錄圖片換到第幾張用
	private ImageSwitcher imgSwi,imgSwi2;
	private ImageView imgVie;

	private M0611 thiscont=this;
	private Integer[] imgArr={R.raw.class1,R.raw.class2,
			R.raw.class3,R.raw.class4};//背景圖存成陣列
//<!----------------------------------------------->
	private Long startTime;//紀錄開機時間使用
	//<!---宣告並定義Handler----------------------->
	private Handler handler00=new Handler();
	private Handler handler=new Handler();
	private Handler handlerIS1=new Handler();
	private Handler handlerIS2=new Handler();
	private Handler handlerIB=new Handler();

	private int iComePlay;//亂數(系統隨機出拳用)
	// <!--------------取得系統服務變數-------------------->
	private SensorManager mSensorManager; // 變數為系統服務管理物件
	private Sensor mAccelerometer; // 宣告變數為加速度計物件
	private ShackDetector mShakeDetector=new ShackDetector(); // 搖動探知器
	// <------------------------------------------>
	private Vibrator myVibrator;//震動器
	private ToneGenerator alarm;//聲響
	private static int miCountSet = 0,//
			           miCountPlayerWin = 0,
			           miCountComWin = 0,
			           miCountDraw = 0;
	private static final int NOTI_ID = 100;
	
	private SharedPreferences gameResultData;
	
	@Override
	protected void onCreate(Bundle savedInstanceState) {
		super.onCreate(savedInstanceState);
		
		setContentView(R.layout.m0603);
		setupViewComponent();
//		startTime=System.currentTimeMillis();//取得目前時間
		handler00.postDelayed(readytime, 3000);//3秒開始呼叫開始計時執行緒
//		handler.postDelayed(updateTimer, 0);//設定delay多久之後才開始顯示時間					
		           
	}

	private void setupViewComponent() {
		B001=(ImageButton)findViewById(R.id.M0601_B001);
		B002=(ImageButton)findViewById(R.id.M0601_B002);
		B003=(ImageButton)findViewById(R.id.M0601_B003);

		T006=(TextView)findViewById(R.id.M0601_T006);
	//<!--------------背景用音樂 跟音效--------------------------------->
		M001=MediaPlayer.create(M0611.this, R.raw.guess);
		M002=MediaPlayer.create(M0611.this, R.raw.lol);
		M003=MediaPlayer.create(M0611.this, R.raw.sign);
		M004=MediaPlayer.create(M0611.this, R.raw.save);
		M005=MediaPlayer.create(M0611.this,R.raw.goodbye);
		musicHot=MediaPlayer.create(M0611.this,R.raw.hot);
	
	//<!------------------動畫用ImageSwitcher------------------------------------->	
		imgSwi=(ImageSwitcher)findViewById(R.id.M0601_IS01);//背景用
		imgSwi.setFactory(this);//用這個java檔實作Factory()這個interface
		imgVie=(ImageView)findViewById(R.id.M0601_IV01);//開機動畫用imageview
        imgSwi2=(ImageSwitcher)findViewById(R.id.M0601_IS02);//電腦出拳
        imgSwi2.setFactory(this);
    //<!---------------開啟Tone 跟震動器服務----------------------------------->    
        alarm=new ToneGenerator(AudioManager.STREAM_SYSTEM,100);
        myVibrator=(Vibrator)getSystemService(Service.VIBRATOR_SERVICE);
  //<!---------------開啟使用搖動監聽器 並且啟用服務管理器跟加速器------------------------------------------>      
        mSensorManager=(SensorManager)getSystemService(Context.SENSOR_SERVICE);
		mAccelerometer=mSensorManager.getDefaultSensor(Sensor.TYPE_ACCELEROMETER);
		mShakeDetector.setOnShakeListener(new OnShakeListener() {
			
			@Override
			public void onShake(int count) {
			
				Intent skIntent = new Intent();
				skIntent.setClass(M0611.this, M0611a.class);
				Bundle skbundle = new Bundle();
				skbundle.putInt("KEY_COUNT_SET", miCountSet);
				skbundle.putInt("KEY_COUNT_PLAYER_WIN", miCountPlayerWin);
				skbundle.putInt("KEY_COUNT_COM_WIN", miCountComWin);
				skbundle.putInt("KEY_COUNT_DRAW", miCountDraw);
				skIntent.putExtras(skbundle);
				startActivity(skIntent);
				myVibrator.vibrate(500);
			}
		});
//<!------------------------------------------------------------->
		B001.setOnClickListener(btnon);
		B002.setOnClickListener(btnon);
		B003.setOnClickListener(btnon);
		
		M001.start();//開機音樂
		//開機動畫
		imgVie.setImageResource(R.raw.logo);
		imgVie.startAnimation(AnimationUtils.loadAnimation(thiscont,R.anim.anim_alpha_out));		
		imgVie.setVisibility(View.INVISIBLE);
	   //開機load背景圖片
		imgSwi.setImageResource(R.raw.class1);
		imgSwi.startAnimation(AnimationUtils.loadAnimation(thiscont, R.anim.anim_alpha_in));
		
		gameResultData =getSharedPreferences("GAME_RESULT", 0);
		gameResultData(2);
	}
//<!-------註冊在onResume 週期的時候開啟搖動監聽--------------->
	@Override
	public void onResume() {
		super.onResume(); //在resume週期的時候開啟持續監聽
		// Add the following line to register the Session Manager Listener onResume
		mSensorManager.registerListener(mShakeDetector, mAccelerometer,	SensorManager.SENSOR_DELAY_UI);
	}
//<!------------------------------------------------------->
//<!---------------onPause時候取消------------------------------------->
	@Override
	public void onPause() {
		// Add the following line to unregister the Sensor Manager onPause
		mSensorManager.unregisterListener(mShakeDetector);//在pause週期的時候停止監聽
		super.onPause();
	}
//<!------------------------------------------------------------->
	//開始計時的執行緒
	private final Runnable readytime = new Runnable() {
		
		@Override
		public void run() {
			// TODO Auto-generated method stub
			startTime=System.currentTimeMillis();//取得正確時間
			handler.postDelayed(updateTimer, 0);//0秒後呼叫遊戲時間計時器
		}
	};
	//更換背景圖片的ImageSwitcher IS01的執行緒
	private final Runnable IS01delay = new Runnable() {
		
		@Override
		public void run() {
			imgSwi.setImageResource(imgArr[t]);//設定imageswitcher圖源依照變數t使用imgArr陣列
			imgSwi.setInAnimation(AnimationUtils.loadAnimation(thiscont,R.anim.anim_scale_rotate_in));//更換圖片動畫
			imgSwi.setOutAnimation(AnimationUtils.loadAnimation(thiscont, R.anim.anim_scale_rotate_out));//更換圖片動畫
			
		}
	};
	//平手時更換電腦出拳的ImageSwitcher IS02的執行緒 包含變換背景顏色 音效 還有判斷輸贏文字顯示
	private final Runnable IS02adelay = new Runnable() {
		
		public void run() {
			// TODO Auto-generated method stub
			imgSwi2.setBackgroundColor(getResources().getColor(R.drawable.Yellow));
			T006.setText(getString(R.string.M0601_T006)
	    			+getString(R.string.M0601_F003));
			T006.setTextColor(getResources().getColor(R.drawable.Yellow));
			M004.start();
		}
	};
	//電腦贏時更換電腦出拳的ImageSwitcher IS02的執行緒 包含變換背景顏色 音效 還有判斷輸贏文字顯示
	private final Runnable IS02bdelay = new Runnable() {
		
		public void run() {
			// TODO Auto-generated method stub
			
			imgSwi2.setBackgroundColor(getResources().getColor(R.drawable.Lime));
			T006.setTextColor(getResources().getColor(R.drawable.Red));
			T006.setText(getString(R.string.M0601_T006)
	    			+getString(R.string.M0601_F002));
			M002.start();
			
		}
	};
	//電腦輸時更換電腦出拳的ImageSwitcher IS02的執行緒 包含變換背景顏色 音效 還有判斷輸贏文字顯示
	private final Runnable IS02cdelay = new Runnable() {
		
		public void run() {
						
			imgSwi2.setBackgroundColor(getResources().getColor(R.drawable.Red));
			T006.setTextColor(getResources().getColor(R.drawable.Lime));
			T006.setText(getString(R.string.M0601_T006)
	    			+getString(R.string.M0601_F001));
			M003.start();
		}
	};
	//遊玩時間的計時器的執行緒
	private final Runnable updateTimer = new Runnable() {
		
		@Override
		public void run() {
			// TODO Auto-generated method stub
			final TextView timer=(TextView)findViewById(R.id.M0601_Timer);
			String tt=getString(R.string.M0601_Timer);
			Long spentTime=System.currentTimeMillis()-startTime;
			Long min=(spentTime/1000)/60;//計算目前已過分鐘
			Long sec=(spentTime/1000)%60;//計算已過秒數
			timer.setText(tt+min+":"+sec);
			handler.postDelayed(this, 1000);//延遲時間1秒後再一次執行這個Runnable()=更新一次 5秒則設定5000
//			if(spentTime>=15000){   //自動關機計時 15秒之後自動呼叫onDestory()來結束程式
//				onDestroy();
//			}
		}
	};
    //ImageButton B001背景顏色變換 透明度變換
	protected final Runnable IB1delay = new Runnable() {
		
		@Override
		public void run() {
			B001.getBackground().setAlpha(255);
			  B002.getBackground().setAlpha(0);
		      B003.getBackground().setAlpha(0);
		      switch(iComePlay){				  
		       case 1:			    	    
		    	   B001.setBackgroundColor(getResources().getColor(R.drawable.Yellow));				    					    	
		    	   break;
		       case 2:    	    
			    	B001.setBackgroundColor(getResources().getColor(R.drawable.Red));				    					    	
		    	   break;
		       case 3:	    	    
			    	
			    	B001.setBackgroundColor(getResources().getColor(R.drawable.Lime));				    	
		    	   break;
		           }
			
		}
	};
	//ImageButton B002背景顏色變換 透明度變換
	protected final Runnable IB2delay = new Runnable() {

		@Override
		public void run() {
			B002.getBackground().setAlpha(255);
			  B001.getBackground().setAlpha(0);
		      B003.getBackground().setAlpha(0);
		      switch (iComePlay){
			    case 1:				    	
			    	
			    	B002.setBackgroundColor(getResources().getColor(R.drawable.Lime));
			    	break;
			    case 2:				    
			    	
			    	B002.setBackgroundColor(getResources().getColor(R.drawable.Yellow)); 
			    	break;
			    case 3:				    	
			    	
			    	B002.setBackgroundColor(getResources().getColor(R.drawable.Red));
			    	break;  }
			
		}
		
	};
	//ImageButton B003背景顏色變換 透明度變換
	protected final Runnable IB3delay = new Runnable() {

		@Override
		public void run() {
			   B003.getBackground().setAlpha(255);
			  B001.getBackground().setAlpha(0);
		      B002.getBackground().setAlpha(0);
		      switch(iComePlay){				  
			    case 1:				    	
			    	
			    	B003.setBackgroundColor(getResources().getColor(R.drawable.Red));
			    	break;
			    case 2:				    	
			    	
			    	B003.setBackgroundColor(getResources().getColor(R.drawable.Lime));
			    	break;
			    case 3:				    	
			    	B003.setBackgroundColor(getResources().getColor(R.drawable.Yellow));				    		    	
			        break;}
		}
		
	};

	@Override
	public View makeView() {   //ImageSwitcher開始動畫的時候所使用的效果
		ImageView v = new ImageView(this);
//		v.setBackgroundColor(0xFF000000);//ImageSwitcher啟動的時候把背景設定為黑色
		v.setScaleType(ImageView.ScaleType.FIT_CENTER);//ImageSwitcher啟動的時候把圖片置中
		v.setLayoutParams(new ImageSwitcher.LayoutParams(
				LayoutParams.FILL_PARENT, LayoutParams.FILL_PARENT));//ImageSwitcher啟動的時候把圖片填滿
		return v;
	}
	
	  private Button.OnClickListener btnon=new  Button.OnClickListener(){
		public void onClick(View v) {
			 miCountSet++;
			 M001.stop();			 
			 musicHot.start();  
			 musicHot.setLooping(true);			 
			 iComePlay=(int)(Math.random()*3+1);
			switch(iComePlay){
			case 1:
				imgSwi2.setImageResource(R.raw.scissors);
				imgSwi2.setInAnimation(AnimationUtils.loadAnimation(thiscont, R.anim.anim_trans_in));
				imgSwi2.setOutAnimation(AnimationUtils.loadAnimation(thiscont, R.anim.anim_trans_out));
				break;
			case 2:
				imgSwi2.setImageResource(R.raw.stone);
				imgSwi2.setInAnimation(AnimationUtils.loadAnimation(thiscont, R.anim.anim_trans_in));
				imgSwi2.setOutAnimation(AnimationUtils.loadAnimation(thiscont, R.anim.anim_trans_out));
				break;
			case 3:
				imgSwi2.setImageResource(R.raw.net);
				imgSwi2.setInAnimation(AnimationUtils.loadAnimation(thiscont, R.anim.anim_trans_in));
				imgSwi2.setOutAnimation(AnimationUtils.loadAnimation(thiscont, R.anim.anim_trans_out));
				break;
			
			}
			switch(v.getId()){
			  case R.id.M0601_B001:
				  handlerIB.postDelayed(IB1delay, 2000);//2秒後呼叫IB1delay這個執行緒
				  
			      chk(iComePlay,10);

				  break;
			  case R.id.M0601_B002:
				  handlerIB.postDelayed(IB2delay, 2000);//2秒後呼叫IB2delay這個執行緒
				  
			      chk(iComePlay,20);

				  break;
			  case R.id.M0601_B003:
				  handlerIB.postDelayed(IB3delay, 2000);//2秒後呼叫IB3delay這個執行緒
				  
			      chk(iComePlay,30);
			 
				  break;} 
	
			
		}};

		
		
			protected void chk(int iComePlay, int i) {
				int xx=iComePlay+i;
				switch(xx){
				case 11:
				case 22:
				case 33:
					miCountDraw++;
					showNotification("已經平手" + Integer.toString(miCountDraw) + "局");
			    	handlerIS2.postDelayed(IS02adelay, 2000);//2秒後呼叫IS02adelay執行緒			    				    	
			    	
					break;
				case 12:
				case 23:
				case 31:
					miCountComWin++;
					showNotification("已經輸" + Integer.toString(miCountComWin) + "局");
					handlerIS2.postDelayed(IS02bdelay, 2000);//2秒後呼叫IS02bdelay執行緒
					
					t--;
					//轉到底圖卡住不轉
					if(t<0){
						t=0;
						break;						
					}
						
					handlerIS1.postDelayed(IS01delay, 3000);//3秒後呼叫IS01delay執行緒
			
					break;
				case 13:
				case 21:
				case 32:
					miCountPlayerWin++;
					showNotification("已經贏" + Integer.toString(miCountPlayerWin) + "局");
					handlerIS2.postDelayed(IS02cdelay, 2000);//2秒後呼叫IS02cdelay執行緒
					
					t++;
					//轉到底圖不轉動
					if(t>3){
						t=3;
						break;
					}  		
					
				      handlerIS1.postDelayed(IS01delay, 3000);//3秒後呼叫IS01delay執行緒
					
					
					break;}
				}
 //<!---------------------開啟系統訊息服務Notification--------API18以後的寫法-->
	private void showNotification(String sMsg) {
		
		alarm.startTone(ToneGenerator.TONE_CDMA_ABBR_ALERT, 200);		
		myVibrator.vibrate(500);
		Intent it = new Intent(getApplicationContext(), M0611a.class);
		it.setFlags(Intent.FLAG_ACTIVITY_NEW_TASK);
		Bundle bundle = new Bundle();
		bundle.putInt("KEY_COUNT_SET", miCountSet);
		bundle.putInt("KEY_COUNT_PLAYER_WIN", miCountPlayerWin);
		bundle.putInt("KEY_COUNT_COM_WIN", miCountComWin);
		bundle.putInt("KEY_COUNT_DRAW", miCountDraw);
		it.putExtras(bundle);

		PendingIntent penIt = PendingIntent.getActivity(getApplicationContext(), 0, it,
				PendingIntent.FLAG_CANCEL_CURRENT);

		Notification noti = new Notification.Builder(this).setSmallIcon(android.R.drawable.btn_star_big_on)
				.setTicker(sMsg).setContentTitle(getString(R.string.app_name)).setContentText(sMsg)
				.setContentIntent(penIt).build();

		NotificationManager notiMgr = (NotificationManager) getSystemService(NOTIFICATION_SERVICE);
		// notiMgr.cancel(NOTI_ID);
		notiMgr.notify(NOTI_ID, noti);			  
			 }	
//<!----------------------------------------------------------------->
            //小於17
//	private void showNotification(String string) {
//		   Notification noti = new Notification(
//				  android.R.drawable.btn_star_big_on,
//				  string,
//				  System.currentTimeMillis());
//		   Intent itnote = new Intent();
//		   itnote.setClass(this, M0611a.class);
//		   itnote.setFlags(Intent.FLAG_ACTIVITY_NEW_TASK);
//		   Bundle bundle = new Bundle();
//		   bundle.putInt("KEY_COUNT_SET", miCountSet);
//		   bundle.putInt("KEY_COUNT_PLAYER_WIN", miCountPlayerWin);
//		   bundle.putInt("KEY_COUNT_COM_WIN", miCountComWin);
//		   bundle.putInt("KEY_COUNT_DRAW", miCountDraw);
//		   itnote.putExtras(bundle);
//		   PendingIntent penIt = PendingIntent.getActivity(
//				                 this, 0, itnote,
//				                 PendingIntent.FLAG_UPDATE_CURRENT);
//		   noti.setLatestEventInfo(this, "遊戲結果", string, penIt);
//		   NotificationManager notiMgr =
//				   (NotificationManager) getSystemService(NOTIFICATION_SERVICE);
//		   
//		   notiMgr.notify(0, noti);
//			}

	protected void onDestroy() {
		handler.removeCallbacks(updateTimer);//程式onDestroy的時候移除還在手機背景計時的時間計時器		
		musicHot.release();//釋放掉背景音樂
		 ((NotificationManager) getSystemService(NOTIFICATION_SERVICE))
		    .cancel(NOTI_ID); //程式結束的時候取消NotificationManager
		 Toast.makeText(getApplicationContext(),"byebye" , Toast.LENGTH_LONG).show();
		M005.start();//關機音樂
		super.onDestroy();		
		}


	@Override
	public boolean onCreateOptionsMenu(Menu menu) {
		// Inflate the menu; this adds items to the action bar if it is present.
		getMenuInflater().inflate(R.menu.m0603, menu);
		
		return true;
	}

	@Override
	public boolean onOptionsItemSelected(MenuItem item) {
		// Handle action bar item clicks here. The action bar will
		// automatically handle clicks on the Home/Up button, so long
		// as you specify a parent activity in AndroidManifest.xml.
		int id = item.getItemId();
		switch (id) {
		case R.id.action_settings:
			gameResultData(1);
			finish();//結束這程式
			break;
//<!----------按統計的時候傳送數值---------------------->
		case R.id.item1:
			Intent itvalue=new Intent();
			itvalue.setClass(M0611.this,M0611a.class);
			Bundle bundle = new Bundle();
			bundle.putInt("KEY_COUNT_SET", miCountSet);
			bundle.putInt("KEY_COUNT_PLAYER_WIN", miCountPlayerWin);
			bundle.putInt("KEY_COUNT_COM_WIN", miCountComWin);
			bundle.putInt("KEY_COUNT_DRAW", miCountDraw);
			itvalue.putExtras(bundle);
			startActivity(itvalue);
			break;
		case R.id.SaveResult:			
			gameResultData(1);			
			break;
		case R.id.LoadResult:			
			gameResultData(2);						
			break;
		case R.id.ClearResult:			
			gameResultData(3);
			break;
		}

		return super.onOptionsItemSelected(item);
	}

	private void gameResultData(int i) {
		// TODO Auto-generated method stub
		switch (i) {
		case 1:
			gameResultData.edit().putInt("COUNT_SET", miCountSet)
			                     .putInt("COUNT_PLAYER_WIN", miCountPlayerWin)
					             .putInt("COUNT_COM_WIN", miCountComWin)
					             .putInt("COUNT_DRAW", miCountDraw).commit();
			Toast.makeText(M0611.this, "儲存完成", Toast.LENGTH_LONG).show();
			break;
		case 2:
			miCountSet = gameResultData.getInt("COUNT_SET", 0);
			miCountPlayerWin = gameResultData.getInt("COUNT_PLAYER_WIN", 0);
			miCountComWin = gameResultData.getInt("COUNT_COM_WIN", 0);
			miCountDraw = gameResultData.getInt("COUNT_DRAW", 0);
			Toast.makeText(M0611.this, "載入完成", Toast.LENGTH_LONG).show();
			break;
		case 3:
			gameResultData.edit().clear().commit();
			Toast.makeText(M0611.this, "清除完成", Toast.LENGTH_LONG).show();
			break;

		}
		
	}
	
	  }




