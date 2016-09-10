package hatomugi.bitflyerapitool;

import android.content.SharedPreferences;
import android.support.v7.app.AppCompatActivity;
import android.os.Bundle;
import android.view.View;
import android.widget.Button;
import android.widget.EditText;

public class SettingActivity extends AppCompatActivity {

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_setting);

        setTitle("設定");

        // 前回の設定を取得
        SharedPreferences prefer = getSharedPreferences("setting", MODE_PRIVATE);
        String apiKey = prefer.getString("apiKey", "");
        String apiSecret = prefer.getString("apiSecret", "");

        ((EditText) findViewById(R.id.editTextAPIKey)).setText(apiKey);
        ((EditText) findViewById(R.id.editTextAPISecret)).setText(apiSecret);

        Button button = (Button) findViewById(R.id.buttonSave);
        button.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {

                String apiKey = ((EditText) findViewById(R.id.editTextAPIKey)).getText().toString();
                String apiSecret = ((EditText) findViewById(R.id.editTextAPISecret)).getText().toString();

                // 設定を保存
                SharedPreferences prefer = getSharedPreferences("setting", MODE_PRIVATE);
                SharedPreferences.Editor editor = prefer.edit();
                editor.putString("apiKey", apiKey);
                editor.putString("apiSecret", apiSecret);
                editor.commit();

                finish();
            }
        });
    }
}
