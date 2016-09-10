package hatomugi.bitflyerapitool;

import android.content.SharedPreferences;
import android.os.Bundle;
import android.support.v7.app.AppCompatActivity;
import android.util.Log;
import android.view.View;
import android.widget.Button;
import android.widget.EditText;
import android.widget.ListView;
import android.widget.TextView;

import java.util.Timer;
import java.util.TimerTask;

import hatomugi.bitflyerapitool.api.Balance;
import hatomugi.bitflyerapitool.api.ChildOrder;
import hatomugi.bitflyerapitool.api.ChildOrders;
import hatomugi.bitflyerapitool.api.Ticker;

public class TradeActivity extends AppCompatActivity {

    /** タイマー */
    private Timer timer;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_trade);

        setTitle(getString(R.string.activity_trade_name));

        // 設定を取得
        SharedPreferences prefer = getSharedPreferences("setting", MODE_PRIVATE);
        final String apiKey = prefer.getString("apiKey", "");
        final String apiSecret = prefer.getString("apiSecret", "");

        // 売買ボタン機能設定
        Button sellBtn = (Button) findViewById(R.id.buttonSell);
        sellBtn.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                int price = Integer.parseInt(((EditText) findViewById(R.id.editTextPrice)).getText().toString());
                double size = Double.parseDouble(((EditText) findViewById(R.id.editTextSize)).getText().toString());
                // 売り注文実行
                new ChildOrder(apiKey, apiSecret, TradeActivity.this, true, true, price, size).execute();
            }
        });

        Button buyBtn = (Button) findViewById(R.id.buttonBuy);
        buyBtn.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                int price = Integer.parseInt(((EditText) findViewById(R.id.editTextPrice)).getText().toString());
                double size = Double.parseDouble(((EditText) findViewById(R.id.editTextSize)).getText().toString());
                // 売り注文実行
                new ChildOrder(apiKey, apiSecret, TradeActivity.this, true, false, price, size).execute();
            }
        });

        // 非同期処理実行
        new Balance(apiKey, apiSecret, (TextView) findViewById(R.id.textViewJPY), (TextView) findViewById(R.id.textViewBTC)).execute();
        new ChildOrders(apiKey, apiSecret, this, (ListView) findViewById(R.id.listViewOrders)).execute();
    }

    @Override
    protected void onPause() {
        super.onPause();

        // タイマー実行停止
        timer.cancel();
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
                Log.d(this.getClass().getName(), "Timer実行");
            }
        }, 0, 6000);
    }
}
