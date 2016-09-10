package hatomugi.bitflyerapitool.api;

import android.os.AsyncTask;
import android.widget.TextView;

import org.json.JSONException;
import org.json.JSONObject;

import java.io.BufferedReader;
import java.io.IOException;
import java.io.InputStreamReader;
import java.net.HttpURLConnection;
import java.net.URL;
import java.text.ParseException;
import java.text.SimpleDateFormat;
import java.util.Date;
import java.util.TimeZone;

/**
 * Ticker。
 * Created by ryo on 2016/08/16.
 */
public class Ticker extends AsyncTask<String, Void, Ticker.Info> {

    public static final String PRODUCT_CODE_BTC_JPY = "BTC_JPY";
    public static final String PRODUCT_CODE_FX_BTC_JPY = "FX_BTC_JPY";
    public static final String PRODUCT_CODE_ETH_BTC = "ETH_BTC";

    private static final String BASE_URL = "https://api.bitflyer.jp/v1/getticker";

    private TextView textViewAsk;
    private TextView textViewLtp;
    private TextView textViewBid;

    public Ticker(TextView textViewAsk, TextView textViewLtp, TextView textViewBid) {
        this.textViewAsk = textViewAsk;
        this.textViewLtp = textViewLtp;
        this.textViewBid = textViewBid;
    }

    @Override
    protected Info doInBackground(String... params) {

        HttpURLConnection conn = null;
        BufferedReader reader = null;
        String line = null;
        try {
            String urlStr = BASE_URL + "?product_code=" + params[0];
            URL url = new URL(urlStr);
            conn = (HttpURLConnection) url.openConnection();
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

        Info info = new Info();
        if (line != null) {
            try {
                JSONObject obj = new JSONObject(line);
                info.setProductCode(obj.getString("product_code"));
                info.setTimestamp(obj.getString("timestamp"));
                info.setTickId(obj.getLong("tick_id"));
                info.setBestBid(obj.getInt("best_bid"));
                info.setBestAsk(obj.getInt("best_ask"));
                info.setBestBidSize(obj.getDouble("best_bid_size"));
                info.setBestAskSize(obj.getDouble("best_ask_size"));
                info.setTotalBidDepth(obj.getDouble("total_bid_depth"));
                info.setTotalAskDepth(obj.getDouble("total_ask_depth"));
                info.setLtp(obj.getInt("ltp"));
                info.setVolume(obj.getDouble("volume"));
                info.setVolumeByProduct(obj.getDouble("volume_by_product"));
            } catch (JSONException e) {
                e.printStackTrace();
            }
        }

        return info;
    }

    @Override
    protected void onPostExecute(Info info) {

        textViewAsk.setText("(ASK:" + Integer.toString(info.getBestAsk()) + ")");
        textViewBid.setText("(BID:" + Integer.toString(info.getBestBid()) + ")");
        textViewLtp.setText(info.getProductCode() + "価格 : " + Integer.toString(info.getLtp()));
    }

    class Info {

        /** product_code */
        private String productCode;
        /** timestamp */
        private Date timestamp;
        /** tick_id */
        private long tickId;
        /** best_bid */
        private int bestBid;
        /** best_ask */
        private int bestAsk;
        /** best_bid_size */
        private double bestBidSize;
        /** best_ask_size */
        private double bestAskSize;
        /** total_bid_depth */
        private double totalBidDepth;
        /** total_ask_depth */
        private double totalAskDepth;
        /** ltp */
        private int ltp;
        /** volume */
        private double volume;
        /** volume_by_product */
        private double volumeByProduct;

        public String getProductCode() {
            return productCode;
        }

        public void setProductCode(String productCode) {
            this.productCode = productCode;
        }

        public Date getTimestamp() {
            return timestamp;
        }

        public String getTimestampString() {
            SimpleDateFormat sdf = new SimpleDateFormat("yyyy-MM-dd HH:mm:ss");
            sdf.setTimeZone(TimeZone.getDefault());
            return sdf.format(timestamp);
        }

        public void setTimestamp(String timestamp) {
            SimpleDateFormat sdf = new SimpleDateFormat("yyyy-MM-dd'T'HH:mm:ss");
            sdf.setTimeZone(TimeZone.getTimeZone("UTC"));
            try {
                this.timestamp = sdf.parse(timestamp);
            } catch (ParseException e) {
                e.printStackTrace();
            }
        }

        public long getTickId() {
            return tickId;
        }

        public void setTickId(long tickId) {
            this.tickId = tickId;
        }

        public int getBestBid() {
            return bestBid;
        }

        public void setBestBid(int bestBid) {
            this.bestBid = bestBid;
        }

        public int getBestAsk() {
            return bestAsk;
        }

        public void setBestAsk(int bestAsk) {
            this.bestAsk = bestAsk;
        }

        public double getBestBidSize() {
            return bestBidSize;
        }

        public void setBestBidSize(double bestBidSize) {
            this.bestBidSize = bestBidSize;
        }

        public double getBestAskSize() {
            return bestAskSize;
        }

        public void setBestAskSize(double bestAskSize) {
            this.bestAskSize = bestAskSize;
        }

        public double getTotalBidDepth() {
            return totalBidDepth;
        }

        public void setTotalBidDepth(double totalBidDepth) {
            this.totalBidDepth = totalBidDepth;
        }

        public double getTotalAskDepth() {
            return totalAskDepth;
        }

        public void setTotalAskDepth(double totalAskDepth) {
            this.totalAskDepth = totalAskDepth;
        }

        public int getLtp() {
            return ltp;
        }

        public void setLtp(int ltp) {
            this.ltp = ltp;
        }

        public double getVolume() {
            return volume;
        }

        public void setVolume(double volume) {
            this.volume = volume;
        }

        public double getVolumeByProduct() {
            return volumeByProduct;
        }

        public void setVolumeByProduct(double volumeByProduct) {
            this.volumeByProduct = volumeByProduct;
        }
    }
}
