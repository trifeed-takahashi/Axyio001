package jp.co.trifeed.axyio001;

import android.os.Build;
import android.telephony.TelephonyManager;

import java.io.IOException;

import org.json.JSONException;
import org.json.JSONObject;

import okhttp3.OkHttpClient;
import okhttp3.Request;
import okhttp3.RequestBody;
import okhttp3.Response;

import static android.content.Context.TELEPHONY_SERVICE;

/**
 * Created by takahashimasatsugu on 2018/01/24.
 */

public class Log2SV {

    static String server = "https://axyio.daijoubu.tech/";
    static String path = "api/anLogging.php";
    public static final okhttp3.MediaType JSON
            = okhttp3.MediaType.parse("application/json; charset=utf-8");

    public static void Log(String msg){

        TelephonyManager telephonyManager = (TelephonyManager) MyContext.getInstance().getApplicationContext().getSystemService(TELEPHONY_SERVICE);
        String line1Number = telephonyManager.getLine1Number();
        if(line1Number == null){ line1Number = "000-0000-0000"; }

        JSONObject json = new JSONObject();
        try {
            json.put("BUILD",   ((MyApplication)MyContext.getInstance().getApplicationContext()).getUpdateTime());
            json.put("LINENO",  line1Number);
            json.put("MESSAGE", msg);
        } catch (JSONException e) {
            e.printStackTrace();
        }

        try {
            System.out.println(json.toString(2));
        } catch (JSONException e) {
            e.printStackTrace();
        }

        OkHttpClient client = new OkHttpClient();

        RequestBody body = RequestBody.create(JSON, json.toString());
        final Request request = new Request.Builder()
                .header("Content-Type", "application/json; charset=utf-8")
                .url(server + path)
                .post(body)
                .build();
        try {
            Response response = client.newCall(request).execute();
        } catch (IOException e) {
            e.printStackTrace();
        }

        return;
    }

}
