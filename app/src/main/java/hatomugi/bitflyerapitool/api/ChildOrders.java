package hatomugi.bitflyerapitool.api;

import android.content.Context;
import android.os.AsyncTask;
import android.widget.ListView;
import android.widget.SimpleAdapter;

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
import java.text.ParseException;
import java.text.SimpleDateFormat;
import java.util.ArrayList;
import java.util.Date;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.TimeZone;

import javax.crypto.Mac;
import javax.crypto.spec.SecretKeySpec;

import hatomugi.bitflyerapitool.R;

/**
 * 注文の一覧。
 * オープンな注文の一覧を取得。
 * Created by ryo on 2016/08/27.
 */
public class ChildOrders extends AsyncTask<Void, Void, List<ChildOrders.Order>> {

    private static final String BASE_URL = "https://api.bitflyer.jp";
    private static final String PATH = "/v1/me/getchildorders?child_order_state=ACTIVE";
    private static final String METHOD = "GET";

    private String apiKey;
    private String apiSecret;
    private Context cont;
    private ListView listView;

    public ChildOrders(String apiKey, String apiSecret, Context cont, ListView listView) {
        this.apiKey = apiKey;
        this.apiSecret = apiSecret;
        this.cont = cont;
        this.listView = listView;
    }


    @Override
    protected List<Order> doInBackground(Void... params) {

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

        List<Order> result = new ArrayList<Order>();
        if (line != null) {
            try {
                JSONArray array = new JSONArray(line);
                for (int i = 0; i < array.length(); i++) {
                    Order order = new Order();
                    JSONObject obj = array.getJSONObject(i);
                    order.setId(obj.getLong("id"));
                    order.setChildOrderId(obj.getString("child_order_id"));
                    order.setProductCode(obj.getString("product_code"));
                    order.setSide(obj.getString("side"));
                    order.setChildOrderType(obj.getString("child_order_type"));
                    order.setPrice(obj.getInt("price"));
                    order.setAveragePrice(obj.getInt("average_price"));
                    order.setSize(obj.getDouble("size"));
                    order.setChildOrderState(obj.getString("child_order_state"));
                    order.setExpireDate(obj.getString("expire_date"));
                    order.setChildOrderDate(obj.getString("child_order_date"));
                    order.setChildOrderAcceptanceId(obj.getString("child_order_acceptance_id"));
                    order.setOutstandingSize(obj.getDouble("outstanding_size"));
                    order.setCancelSize(obj.getDouble("cancel_size"));
                    order.setExecutedSize(obj.getDouble("executed_size"));
                    order.setTotalCommission(obj.getDouble("total_commission"));
                    result.add(order);
                }
            } catch (JSONException e) {
                e.printStackTrace();
            }
        }

        return result;
    }

    @Override
    protected void onPostExecute(List<Order> orders) {

        List<Map<String, String>> resultList = new ArrayList<Map<String, String>>();
        for (Order order : orders) {
            Map<String, String> item = new HashMap<String, String>();
            String sideJ = order.getSide();
            if (sideJ.equals("BUY")) {
                sideJ = "買い";
            } else if (sideJ.equals("SELL")) {
                sideJ = "売り";
            }
            item.put("side", sideJ);
            item.put("price", Integer.valueOf(order.getPrice()).toString());
            item.put("size", Double.valueOf(order.getSize()).toString());
            item.put("child_order_date", order.getChildOrderDateString());
            resultList.add(item);
        }

        SimpleAdapter adapter = new SimpleAdapter(cont, resultList, R.layout.child_order_list,
                new String[] {"side", "price", "size", "child_order_date"},
                new int[] {R.id.side, R.id.price, R.id.size, R.id.child_order_date});
        listView.setAdapter(adapter);
    }

    class Order {

        /** id */
        private long id;
        /** child_order_id */
        private String childOrderId;
        /** product_code */
        private String productCode;
        /** side */
        private String side;
        /** child_order_type */
        private String childOrderType;
        /** price */
        private int price;
        /** average_price */
        private int averagePrice;
        /** size */
        private double size;
        /** child_order_state */
        private String childOrderState;
        /** expire_date */
        private Date expireDate;
        /** child_order_date */
        private Date childOrderDate;
        /** child_order_acceptance_id */
        private String childOrderAcceptanceId;
        /** outstanding_size */
        private double outstandingSize;
        /** cancel_size */
        private double cancelSize;
        /** executed_size */
        private double executedSize;
        /** total_commission */
        private double totalCommission;

        public long getId() {
            return id;
        }

        public void setId(long id) {
            this.id = id;
        }

        public String getChildOrderId() {
            return childOrderId;
        }

        public void setChildOrderId(String childOrderId) {
            this.childOrderId = childOrderId;
        }

        public String getProductCode() {
            return productCode;
        }

        public void setProductCode(String productCode) {
            this.productCode = productCode;
        }

        public String getSide() {
            return side;
        }

        public void setSide(String side) {
            this.side = side;
        }

        public String getChildOrderType() {
            return childOrderType;
        }

        public void setChildOrderType(String childOrderType) {
            this.childOrderType = childOrderType;
        }

        public int getPrice() {
            return price;
        }

        public void setPrice(int price) {
            this.price = price;
        }

        public int getAveragePrice() {
            return averagePrice;
        }

        public void setAveragePrice(int averagePrice) {
            this.averagePrice = averagePrice;
        }

        public double getSize() {
            return size;
        }

        public void setSize(double size) {
            this.size = size;
        }

        public String getChildOrderState() {
            return childOrderState;
        }

        public void setChildOrderState(String childOrderState) {
            this.childOrderState = childOrderState;
        }

        public Date getExpireDate() {
            return expireDate;
        }

        public String getExpireDateString() {
            SimpleDateFormat sdf = new SimpleDateFormat("yyyy-MM-dd HH:mm:ss");
            sdf.setTimeZone(TimeZone.getDefault());
            return sdf.format(expireDate);
        }

        public void setExpireDate(String expireDate) {
            SimpleDateFormat sdf = new SimpleDateFormat("yyyy-MM-dd'T'HH:mm:ss");
            sdf.setTimeZone(TimeZone.getTimeZone("UTC"));
            try {
                this.expireDate = sdf.parse(expireDate);
            } catch (ParseException e) {
                e.printStackTrace();
            }
        }

        public Date getChildOrderDate() {
            return childOrderDate;
        }

        public String getChildOrderDateString() {
            SimpleDateFormat sdf = new SimpleDateFormat("yyyy-MM-dd HH:mm:ss");
            sdf.setTimeZone(TimeZone.getDefault());
            return sdf.format(childOrderDate);
        }

        public void setChildOrderDate(String childOrderDate) {
            SimpleDateFormat sdf = new SimpleDateFormat("yyyy-MM-dd'T'HH:mm:ss");
            sdf.setTimeZone(TimeZone.getTimeZone("UTC"));
            try {
                this.childOrderDate = sdf.parse(childOrderDate);
            } catch (ParseException e) {
                e.printStackTrace();
            }
        }

        public String getChildOrderAcceptanceId() {
            return childOrderAcceptanceId;
        }

        public void setChildOrderAcceptanceId(String childOrderAcceptanceId) {
            this.childOrderAcceptanceId = childOrderAcceptanceId;
        }

        public double getOutstandingSize() {
            return outstandingSize;
        }

        public void setOutstandingSize(double outstandingSize) {
            this.outstandingSize = outstandingSize;
        }

        public double getCancelSize() {
            return cancelSize;
        }

        public void setCancelSize(double cancelSize) {
            this.cancelSize = cancelSize;
        }

        public double getExecutedSize() {
            return executedSize;
        }

        public void setExecutedSize(double executedSize) {
            this.executedSize = executedSize;
        }

        public double getTotalCommission() {
            return totalCommission;
        }

        public void setTotalCommission(double totalCommission) {
            this.totalCommission = totalCommission;
        }
    }
}
