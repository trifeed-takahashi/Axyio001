package jp.co.trifeed.axyio001;

import android.support.v7.app.AppCompatActivity;
import android.os.Bundle;
import android.view.View;
import android.widget.Toast;

import jp.co.trifeed.axyio001.MailUtil.CheckMail;

public class MainActivity extends AppCompatActivity {

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_main);
    }

    public void onTestClick(View view){
        switch (view.getId()) {
            case R.id.testButton:
                Toast.makeText(MainActivity.this, "クリックされました！", Toast.LENGTH_LONG).show();

                AsyncImapRequest task = new AsyncImapRequest(this);
                task.execute();
                break;
        }
    }

}
