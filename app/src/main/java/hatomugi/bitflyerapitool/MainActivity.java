package hatomugi.bitflyerapitool;

import android.content.Intent;
import android.content.SharedPreferences;
import android.os.Bundle;
import android.support.v7.app.AppCompatActivity;
import android.util.Log;
import android.view.Menu;
import android.view.MenuInflater;
import android.view.MenuItem;
import android.widget.ListView;
import android.widget.TextView;

import java.text.ParseException;
import java.text.SimpleDateFormat;
import java.util.Calendar;
import java.util.Date;
import java.util.Map;
import java.util.TimeZone;
import java.util.Timer;
import java.util.TimerTask;

import hatomugi.bitflyerapitool.api.Chat;
import hatomugi.bitflyerapitool.api.Ticker;

public class MainActivity extends AppCompatActivity {

    /** タイマー */
    private Timer timer;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_main);

        setTitle(getString(R.string.activity_main_name));

        // デフォルトパラメータ生成
        SimpleDateFormat sdf = new SimpleDateFormat("yyyy-MM-dd'T'HH:mm:ss");
        sdf.setTimeZone(TimeZone.getTimeZone("UTC"));
        Calendar cal = Calendar.getInstance();
        cal.add(Calendar.HOUR, -12); // 12時間前を取得
        Date defDate = cal.getTime();
        String initDate = sdf.format(defDate);


        // 前回の日付時間を取得
        SharedPreferences prefer = getPreferences(MODE_PRIVATE);
        String date = prefer.getString("chatDate", "");
        SimpleDateFormat sdfjpn = new SimpleDateFormat("yyyy-MM-dd HH:mm:ss");
        try {
            Date zenkaiDate = sdfjpn.parse(date);
            // デフォルトより古い情報は取得しない
            if (zenkaiDate.compareTo(defDate) > 0) {
                initDate = sdf.format(zenkaiDate);
            }
        } catch (ParseException e) {
            e.printStackTrace();
        }

        // 非同期処理実行（チャット）
        new Chat(this, (ListView) findViewById(R.id.listView)).execute(initDate);
    }

    @Override
    protected void onPause() {
        super.onPause();

        // タイマー実行停止
        timer.cancel();

        // リストから表示位置を取得
        ListView listView = (ListView) findViewById(R.id.listView);
        int pos = listView.getFirstVisiblePosition();

        // 表示位置から日付時間を取得
        Map<String, String> item = (Map<String, String>)listView.getItemAtPosition(pos);
        String date = item.get("date");

        // 日付時間を保存
        SharedPreferences prefer = getPreferences(MODE_PRIVATE);
        SharedPreferences.Editor editor = prefer.edit();
        editor.putString("chatDate", date);
        editor.commit();
    }

    @Override
    protected void onResume() {
        super.onResume();

        // 繰り返し実行（Ticker）
        timer = new Timer(true);
        timer.schedule(new TimerTask() {
            @Override
            public void run() {
                new Ticker(
                        (TextView) findViewById(R.id.textViewAsk),
                        (TextView) findViewById(R.id.textViewLtp),
                        (TextView) findViewById(R.id.textViewBid)).execute(Ticker.PRODUCT_CODE_BTC_JPY);
            }
        }, 0, 6000);
    }

    @Override
    public boolean onCreateOptionsMenu(Menu menu) {
        MenuInflater inflater = getMenuInflater();
        inflater.inflate(R.menu.activity_main, menu);
        return true;
    }

    @Override
    public boolean onOptionsItemSelected(MenuItem item) {
        boolean result = false;
        switch (item.getItemId()) {
            case R.id.trade:
                // 売買
                Intent tradeIntent = new Intent(this, TradeActivity.class);
                startActivity(tradeIntent);
                result = true;
                break;
            case R.id.setting:
                // 設定
                Intent settingIntent = new Intent(this, SettingActivity.class);
                startActivity(settingIntent);
                result = true;
                break;
            default:
                break;
        }
        return result;
    }
}
