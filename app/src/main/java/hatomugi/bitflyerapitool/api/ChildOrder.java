package hatomugi.bitflyerapitool.api;

import android.content.Context;
import android.os.AsyncTask;
import android.widget.Toast;

import org.json.JSONException;
import org.json.JSONObject;

import java.io.BufferedReader;
import java.io.IOException;
import java.io.InputStreamReader;
import java.io.PrintWriter;
import java.net.HttpURLConnection;
import java.net.URL;
import java.security.InvalidKeyException;
import java.security.NoSuchAlgorithmException;

import javax.crypto.Mac;
import javax.crypto.spec.SecretKeySpec;

/**
 * 新規注文。
 * Created by ryo on 2016/08/21.
 */
public class ChildOrder extends AsyncTask<Void, Void, String> {

    private static final String BASE_URL = "https://api.bitflyer.jp";
    private static final String PATH = "/v1/me/sendchildorder";
    private static final String METHOD = "POST";

    private static final String PRODUCT_CODE_BTC = "BTC_JPY";
    private static final String PRODUCT_CODE_FXBTC = "FX_BTC_JPY";
    private static final String PRODUCT_CODE_ETH = "ETH_BTC";
    private static final String CHILD_ORDER_TYPE_SASHINE = "LIMIT";
    private static final String CHILD_ORDER_TYPE_NARIYUKI = "MARKET";
    private static final String SIDE_URI = "SELL";
    private static final String SIDE_KAI = "BUY";

    private String apiKey;
    private String apiSecret;
    private Context cont;

    /** プロダクト */
    private String productCode;
    /** 注文方法 */
    private String childOrderType;
    /** 売買 */
    private String side;
    /** 価格 */
    private int price;
    /** 注文数量 */
    private double size;

    public ChildOrder(String apiKey, String apiSecret, Context cont, boolean sashine, boolean uri, int price, double size) {
        this.apiKey = apiKey;
        this.apiSecret = apiSecret;
        this.cont = cont;
        this.productCode = PRODUCT_CODE_BTC;
        if (sashine) {
            this.childOrderType = CHILD_ORDER_TYPE_SASHINE;
        } else {
            this.childOrderType = CHILD_ORDER_TYPE_NARIYUKI;
        }
        if (uri) {
            this.side = SIDE_URI;
        } else {
            this.side = SIDE_KAI;
        }
        this.price = price;
        this.size = size;
    }

    @Override
    protected String doInBackground(Void... params) {

        String unixTimestampStr = Long.toString(System.currentTimeMillis() / 1000);
        JSONObject bodyJson = new JSONObject();
        try {
            bodyJson.put("product_code", productCode);
            bodyJson.put("child_order_type", childOrderType);
            bodyJson.put("side", side);
            bodyJson.put("price", price);
            bodyJson.put("size", size);
        } catch (JSONException e) {
            e.printStackTrace();
        }
        String body = bodyJson.toString();
        String data = unixTimestampStr + METHOD + PATH + body;
        String hash = "";

        try {
            // HMAC-SHA256 署名
            String algo = "HmacSHA256";
            SecretKeySpec secretKeySpec = new SecretKeySpec(apiSecret.getBytes(), algo);
            Mac mac = Mac.getInstance(algo);
            mac.init(secretKeySpec);
            byte[] macBytes = mac.doFinal(data.getBytes());

            StringBuilder builder = new StringBuilder();
            for (byte bite : macBytes) {
                builder.append(String.format("%02x", bite & 0xff));
            }
            hash = builder.toString();

        } catch (NoSuchAlgorithmException e) {
            e.printStackTrace();
        } catch (InvalidKeyException e) {
            e.printStackTrace();
        }

        HttpURLConnection conn = null;
        BufferedReader reader = null;
        String line = null;
        try {
            String urlStr = BASE_URL + PATH;
            URL url = new URL(urlStr);
            conn = (HttpURLConnection) url.openConnection();
            conn.setRequestMethod(METHOD);
            conn.setRequestProperty("ACCESS-KEY", apiKey);
            conn.setRequestProperty("ACCESS-TIMESTAMP", unixTimestampStr);
            conn.setRequestProperty("ACCESS-SIGN", hash);
            conn.setRequestProperty("Content-Type", "application/json");
            PrintWriter printWriter = new PrintWriter(conn.getOutputStream());
            printWriter.print(body);
            printWriter.close();
            reader = new BufferedReader(new InputStreamReader(conn.getInputStream()));
            line = reader.readLine();

        } catch (IOException ioe) {
            ioe.printStackTrace();

        } finally {
            if (conn != null) {
                conn.disconnect();
            }
            if (reader != null) {
                try {
                    reader.close();
                } catch (IOException e) {
                    e.printStackTrace();
                }
            }
        }

        return line;
    }

    @Override
    protected void onPostExecute(String s) {
        if (s != null) {
            Toast.makeText(cont, "注文成功", Toast.LENGTH_LONG).show();
        } else {
            Toast.makeText(cont, "注文失敗", Toast.LENGTH_LONG).show();
        }
    }
}
