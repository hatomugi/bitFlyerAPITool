package hatomugi.bitflyerapitool.api;

import android.os.AsyncTask;
import android.widget.TextView;

import org.json.JSONArray;
import org.json.JSONException;
import org.json.JSONObject;

import java.io.BufferedReader;
import java.io.IOException;
import java.io.InputStreamReader;
import java.net.HttpURLConnection;
import java.net.URL;
import java.security.InvalidKeyException;
import java.security.NoSuchAlgorithmException;
import java.util.ArrayList;
import java.util.List;

import javax.crypto.Mac;
import javax.crypto.spec.SecretKeySpec;

/**
 * 資産残高。
 * Created by ryo on 2016/08/17.
 */
public class Balance extends AsyncTask<String, Void, List<Balance.Info>> {

    private static final String BASE_URL = "https://api.bitflyer.jp";
    private static final String PATH = "/v1/me/getbalance";
    private static final String METHOD = "GET";

    private String apiKey;
    private String apiSecret;

    private TextView textViewJpy;
    private TextView textViewBtc;

    public Balance(String apiKey, String apiSecret, TextView textViewJpy, TextView textViewBtc) {
        this.apiKey = apiKey;
        this.apiSecret = apiSecret;
        this.textViewJpy = textViewJpy;
        this.textViewBtc = textViewBtc;
    }

    @Override
    protected List<Info> doInBackground(String... params) {

        String unixTimestampStr = Long.toString(System.currentTimeMillis() / 1000);
        String data = unixTimestampStr + METHOD + PATH;
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
            conn.setRequestProperty("ACCESS-KEY", apiKey);
            conn.setRequestProperty("ACCESS-TIMESTAMP", unixTimestampStr);
            conn.setRequestProperty("ACCESS-SIGN", hash);
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

        List<Info> result = new ArrayList<Info>();
        if (line != null) {
            try {
                JSONArray array = new JSONArray(line);
                for (int i = 0; i < array.length(); i++) {
                    Info info = new Info();
                    JSONObject obj = array.getJSONObject(i);
                    info.setCurrencyCode(obj.getString("currency_code"));
                    info.setAmount(obj.getDouble("amount"));
                    info.setAvailable(obj.getDouble("available"));
                    result.add(info);
                }
            } catch (JSONException e) {
                e.printStackTrace();
            }
        }

        return result;
    }

    @Override
    protected void onPostExecute(List<Info> infos) {
        for (Info info : infos) {
            switch (info.getCurrencyCode()) {
                case "JPY":
                    textViewJpy.setText("円余力: " + Double.toString(info.getAvailable()));
                    break;
                case "BTC":
                    textViewBtc.setText("BTC余力: " + Double.toString(info.getAvailable()));
                    break;
                default:
                    break;
            }
        }
    }

    class Info {

        /** currency_code */
        private String currencyCode;
        /** amount */
        private double amount;
        /** available */
        private double available;

        public String getCurrencyCode() {
            return currencyCode;
        }

        public void setCurrencyCode(String currencyCode) {
            this.currencyCode = currencyCode;
        }

        public double getAmount() {
            return amount;
        }

        public void setAmount(double amount) {
            this.amount = amount;
        }

        public double getAvailable() {
            return available;
        }

        public void setAvailable(double available) {
            this.available = available;
        }
    }
}
