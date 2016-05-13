package meng.FileManager;

import android.app.Activity;
import android.content.Intent;
import android.os.Bundle;
import android.util.Log;
import android.view.View;
import android.widget.Button;

/**
 * Created by mengyouxu on 2016/5/12.
 */
public class ExoPlayerActivity extends Activity {
    String TAG = "ExoPlayerActivity";
    String file_path = null;
    Button btn1;
    Button btn2;
    Button btn3;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);

        this.setContentView(R.layout.videoplayer);

        Intent intent_1 = this.getIntent();
        Bundle bundle_1 = intent_1.getExtras();

        file_path = bundle_1.getString("file_path");
        Log.i(TAG, "setDataSource: " + file_path);

        btn1 = (Button)this.findViewById(R.id.btn1);
        btn2 = (Button)this.findViewById(R.id.btn2);
        btn3 = (Button)this.findViewById(R.id.btn3);

        btn1.setOnClickListener(buttonListener);
        btn2.setOnClickListener(buttonListener);
        btn3.setOnClickListener(buttonListener);
    }

    private View.OnClickListener buttonListener =  new View.OnClickListener(){
        public void onClick(View v) {
            // TODO Auto-generated method stub
            switch(v.getId()){
                case R.id.btn1:
                    Log.i("VideoPlayer", "R.id.btn1");

                    break;
                case R.id.btn2:
                    Log.i("VideoPlayer","R.id.btn2");

                    break;
                case R.id.btn3:
                    Log.i("VideoPlayer","R.id.btn3");

                    break;
                default:
                    break;
            }
        }
    };
}
