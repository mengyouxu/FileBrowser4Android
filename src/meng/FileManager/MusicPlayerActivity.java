package meng.FileManager;

import android.app.Activity;
import android.content.Intent;
import android.os.Bundle;

public class MusicPlayerActivity extends Activity{
    String file_path = null;
    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        Intent intent_1 = this.getIntent();
        Bundle bundle_1 = intent_1.getExtras();

        file_path = bundle_1.getString("file_path");

        Intent intent_2 = new Intent(MusicPlayerActivity.this,MusicPlayer.class);
        intent_2.putExtras(bundle_1);

        startActivity(intent_2);
    }


}
