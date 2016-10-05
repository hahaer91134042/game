package tcnr6.com.m1401;

import android.app.Activity;

import android.os.Bundle;
import android.view.View;
import android.view.View.OnClickListener;
import android.widget.Button;
import android.widget.EditText;


public class M0611a extends Activity {
      private Button back;
      private EditText totalCunt,playerWin,compWin,drawCunt;
      
     
	@Override
	protected void onCreate(Bundle savedInstanceState) {
		// TODO Auto-generated method stub
		super.onCreate(savedInstanceState);
		setContentView(R.layout.m0603a);
		setupviewcomponent();
		showResult();
	}

	private void setupviewcomponent() {
		// TODO Auto-generated method stub
		back=(Button)findViewById(R.id.btnBackToGame);
		totalCunt=(EditText)findViewById(R.id.edtCountSet);
		playerWin=(EditText)findViewById(R.id.edtCountPlayerWin);
		compWin=(EditText)findViewById(R.id.edtCountComWin);
		drawCunt=(EditText)findViewById(R.id.edtCountDraw);
		
		back.setOnClickListener(new OnClickListener() {
			
			@Override
			public void onClick(View v) {
				// TODO Auto-generated method stub
				M0611a.this.finish();
			}
		});
		
		
	}
	private void showResult() {
		// TODO Auto-generated method stub
		Bundle bundle = this.getIntent().getExtras();
		int iCountSet = bundle.getInt("KEY_COUNT_SET");
		int iCountPlayerWin = bundle.getInt("KEY_COUNT_PLAYER_WIN");
		int iCountComWin = bundle.getInt("KEY_COUNT_COM_WIN");
		int iCountDraw = bundle.getInt("KEY_COUNT_DRAW");
		
		totalCunt.setText(Integer.toString(iCountSet));
		playerWin.setText(Integer.toString(iCountPlayerWin));
		compWin.setText(Integer.toString(iCountComWin));
		drawCunt.setText(Integer.toString(iCountDraw));
		
	}
	
	

}
