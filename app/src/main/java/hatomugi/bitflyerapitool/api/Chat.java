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
import java.text.ParseException;
import java.text.SimpleDateFormat;
import java.util.ArrayList;
import java.util.Date;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.TimeZone;

import hatomugi.bitflyerapitool.R;

/**
 * チャット読み込み。
 * Created by ryo on 2016/08/07.
 */
public class Chat extends AsyncTask<String, Void, List<Chat.Message>> {

    private static final String BASE_URL = "https://api.bitflyer.jp/v1/getchats";

    private Context cont;
    private ListView listView;

    public Chat(Context cont, ListView listView) {
        this.cont = cont;
        this.listView = listView;
    }

    @Override
    protected List<Message> doInBackground(String... params) {

        HttpURLConnection conn = null;
        BufferedReader reader = null;
        String line = null;
        try {
            String urlStr = BASE_URL + "?from_date=" + params[0];
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

        List<Message> result = new ArrayList<Message>();
        if (line != null) {
            try {
                JSONArray array = new JSONArray(line);
                for (int i = 0; i < array.length(); i++) {
                    Message message = new Message();
                    JSONObject obj = array.getJSONObject(i);
                    message.setNickname(obj.getString("nickname"));
                    message.setMessage(obj.getString("message"));
                    message.setDate(obj.getString("date"));
                    result.add(message);
                }
            } catch (JSONException e) {
                e.printStackTrace();
            }
        }

        return result;
    }

    @Override
    protected void onPostExecute(List<Message> result) {

        List<Map<String, String>> resultList = new ArrayList<Map<String, String>>();
        for (Message message : result) {
            Map<String, String> item = new HashMap<String, String>();
            item.put("nickname", message.getNickname());
            item.put("message", message.getMessage());
            item.put("date", message.getDateString());
            resultList.add(item);
        }

        SimpleAdapter adapter = new SimpleAdapter(cont, resultList, R.layout.chat_message_list,
                new String[] {"nickname", "message", "date"},
                new int[] {R.id.nickname, R.id.message, R.id.date});
        listView.setAdapter(adapter);
    }

    class Message {

        /** ニックネーム */
        private String nickname;
        /** メッセージ */
        private String message;
        /** 年月日時分秒 */
        private Date date;

        public String getNickname() {
            return nickname;
        }
        public void setNickname(String nickname) {
            this.nickname = nickname;
        }
        public String getMessage() {
            return message;
        }
        public void setMessage(String message) {
            this.message = message;
        }
        public Date getDate() {
            return date;
        }
        public String getDateString() {
            SimpleDateFormat sdf = new SimpleDateFormat("yyyy-MM-dd HH:mm:ss");
            sdf.setTimeZone(TimeZone.getDefault());
            return sdf.format(date);
        }
        public void setDate(String date) {
            SimpleDateFormat sdf = new SimpleDateFormat("yyyy-MM-dd'T'HH:mm:ss");
            sdf.setTimeZone(TimeZone.getTimeZone("UTC"));
            try {
                this.date = sdf.parse(date);
            } catch (ParseException e) {
                e.printStackTrace();
            }
        }
    }

}
