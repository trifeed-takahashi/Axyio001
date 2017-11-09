package jp.co.trifeed.axyio001.MailUtil;

/**
 * Created by m.takahashi on 2017/11/02.
 */

import android.app.Application;
import android.content.Context;
import android.content.Intent;
import android.content.SharedPreferences;
import android.preference.PreferenceManager;
import android.util.Log;

import com.sun.mail.imap.IMAPInputStream;

import javax.activation.CommandMap;
import javax.activation.MailcapCommandMap;
import javax.mail.*;
import javax.mail.internet.MimeUtility;
import javax.mail.search.ComparisonTerm;
import javax.mail.search.ReceivedDateTerm;
import javax.mail.search.StringTerm;
import javax.mail.search.SubjectTerm;

import java.io.IOException;
import java.security.Security;
import java.text.ParseException;
import java.text.SimpleDateFormat;
import java.time.ZonedDateTime;
import java.util.*;
import java.util.regex.Matcher;
import java.util.regex.Pattern;

import jp.co.trifeed.axyio001.MainActivity;
import jp.co.trifeed.axyio001.MyApplication;
import jp.co.trifeed.axyio001.MyContext;
import jp.co.trifeed.axyio001.R;

public class CheckMail {

    static final String TAG="CheckMail";

    private String host;
    private boolean imap4Ssl;
    private String imap4Port;
    private String mailAddress;
    private String mailPassword;

    public CheckMail() {
        Context context = MyContext.getInstance().getApplicationContext();

        host = context.getString(R.string.imap4_host);
        imap4Ssl = (context.getString(R.string.imap4_host).equals("YES"));
        imap4Port = context.getString(R.string.imap4_port);
        mailAddress = context.getString(R.string.mail_address);
        mailPassword = context.getString(R.string.mail_password);

    }

    public void getMail(){

        Context context = MyContext.getInstance().getApplicationContext();

        // properties を設定
        Properties props = new Properties();
        //Security.addProvider(new com.sun.net.ssl.internal.ssl.Provider());

        props.put("mail.imap.starttls.enable","true");
        props.put("mail.imap.auth", "true");
        //props.put("mail.debug", "true");
        props.put("mail.imap.socketFactory.port", this.imap4Port);
        props.put("mail.imap.socketFactory.class", "javax.net.ssl.SSLSocketFactory");
        props.put("mail.imap.socketFactory.fallback", "false");

        MailcapCommandMap mc = (MailcapCommandMap) CommandMap.getDefaultCommandMap();
        mc.addMailcap("text/html;; x-java-content-handler=com.sun.mail.handlers.text_html");
        mc.addMailcap("text/xml;; x-java-content-handler=com.sun.mail.handlers.text_xml");
        mc.addMailcap("text/plain;; x-java-content-handler=com.sun.mail.handlers.text_plain");
        mc.addMailcap("multipart/*;; x-java-content-handler=com.sun.mail.handlers.multipart_mixed");
        mc.addMailcap("message/rfc822;; x-java-content-handler=com.sun.mail.handlers.message_rfc822");
        CommandMap.setDefaultCommandMap(mc);

        Session session = Session.getDefaultInstance(props, null);
        Store store = null;
        Message[] messages = null;

        try {
            store = session.getStore("imap");
            store.connect(this.host, this.mailAddress, this.mailPassword);

            // 通常の受信フォルダにアクセスる場合は以下固定
            Folder folder = store.getFolder("INBOX");

            // IMAPの場合はラベル名を指定すればそのラベルのメールが取得出来る
            folder.open(Folder.READ_ONLY);

            long startTime = (new Date()).getTime() - context.getResources().getInteger(R.integer.check_duration);
            SimpleDateFormat sdfstart = new SimpleDateFormat("yyyy/MM/dd (EEE) HH:mm:ss");
            sdfstart.setTimeZone(TimeZone.getTimeZone("Asia/Tokyo"));
            Log.i(TAG,"Date : " + sdfstart.format(startTime) + " 以降のメールを取得します。。。");

            // IMAPサーバへの検索条件の作成
            messages = null;
            TimeZone tz = TimeZone.getTimeZone("Asia/Tokyo");
            Calendar calendar = new GregorianCalendar(tz);
            Date trialTime = new Date(startTime);
            calendar.setTime(trialTime);
            messages = folder.search(new ReceivedDateTerm(ComparisonTerm.GE, calendar.getTime()));

            // 警告メールに該当するメールを保存するArrayList
            ArrayList<Message> alMessage = new ArrayList<Message>();

            // メッセージ件数分
            for(Message message : messages) {

                // 受信日時
                long recvDate = message.getReceivedDate().getTime();
                SimpleDateFormat sdf = new SimpleDateFormat("yyyy/MM/dd(EE) HH:mm:ss z");
                sdf.setTimeZone(TimeZone.getTimeZone("Asia/Tokyo"));
                Log.i(TAG, "RECV : " + sdf.format(recvDate) + " : " + message.getSubject());

                if(checkAlertMail(message)){
                    alMessage.add(message);

                }
            }

            SharedPreferences sharedPreferences = PreferenceManager.getDefaultSharedPreferences(context);
            SharedPreferences.Editor editor = sharedPreferences.edit();
            editor.putLong("CHECK_DATE", new Date().getTime());
            editor.commit();

            /////////////////////////////////////////////////////
            // 受信したメッセージのテスト表示
            for(Message message : alMessage){

                Log.i(TAG, "======================================");

                long recvDate = message.getReceivedDate().getTime();
                SimpleDateFormat sdf = new SimpleDateFormat("EEE MMM dd HH:mm:ss z yyyy");
                sdf.setTimeZone(TimeZone.getTimeZone("Asia/Tokyo"));
                Log.i(TAG, "Date : " + sdf.format(recvDate));

                // 件名
                Log.i(TAG, "Subject : [" + message.getSubject() + "]");

                // 送信者
                Log.i(TAG, "From : [" + getFromAddress(message) + "]");

                // ヘッダ（MESSAGE-ID）
                Enumeration<Header> allHeaders = message.getAllHeaders();
                while (allHeaders.hasMoreElements()) {
                    Header header = allHeaders.nextElement();
                    if(header.getName().toUpperCase().trim().equals("MESSAGE-ID")){
                        Log.i(TAG, header.getName() + " : " + header.getValue());
                    }
                }

                // 本文
                Log.i(TAG, getText(message.getContent()));

                Log.i(TAG, "======================================");
            }
            /////////////////////////////////////////////////////

            Log.i(TAG, alMessage.size() + "件の警告メッセージがありました。");

            folder.close(false);

            // 警告メッセージがあった場合、アクティビティを表示
            if(sharedPreferences.getBoolean("NOW_ALERT", false)) {
                Intent intent = new Intent(context, MainActivity.class);
                intent.setFlags(Intent.FLAG_ACTIVITY_NEW_TASK);

                // アラームを鳴らす
                MyApplication ma = (MyApplication)MyContext.getInstance().getApplicationContext();
                ma.startAlarm();
            }

        } catch (Exception e) {
            e.printStackTrace();
        } finally {
            try {
                if (store != null) {
                    store.close();
                }
            } catch (MessagingException e) {
                e.printStackTrace();
            }
        }
    }

    private boolean checkAlertMail(Message message) throws MessagingException, ParseException, IOException {

        Context context = MyContext.getInstance().getApplicationContext();

        ////////////////////////////////////////////////////
        // 警報中であれば、警告メール無しとしてリターンします。
        SharedPreferences sharedPreferences = PreferenceManager.getDefaultSharedPreferences(context);
        boolean nowAlert = sharedPreferences.getBoolean("NOW_ALERT", false);

        if(nowAlert){
            return false;
        }

        ////////////////////////////////////////////////////
        // ヘッダ（MESSAGE-ID）の取得
        String messageId = "";
        Enumeration<Header> allHeaders = message.getAllHeaders();
        while (allHeaders.hasMoreElements()) {
            Header header = allHeaders.nextElement();
            if(header.getName().toUpperCase().trim().equals("MESSAGE-ID")){
                messageId = header.getValue();
            }
        }

        ////////////////////////////////////////////////////
        // 受信日時のチェック
        long startTime = (new Date()).getTime() - context.getResources().getInteger(R.integer.check_duration);

        long recvDate = message.getReceivedDate().getTime();
        SimpleDateFormat sdf = new SimpleDateFormat("EEE MMM dd HH:mm:ss z yyyy");
        sdf.setTimeZone(TimeZone.getTimeZone("Asia/Tokyo"));
        Log.i(TAG, "Date : " + sdf.format(recvDate));
        Log.i(TAG, "Mail : " + recvDate);
        Log.i(TAG, "Strt : " + startTime);

        if(recvDate < startTime){
            return false;
        }

        ////////////////////////////////////////////////////
        // 件名のチェック
        String institutionName = context.getString(R.string.check_subject);
        String preSubject = context.getString(R.string.check_presubject);
        String orgSubject = message.getSubject();

        if(!orgSubject.contains(institutionName)){
            return false;
        }

        if(0 != orgSubject.indexOf(preSubject)){
            return false;
        }

        ////////////////////////////////////////////////////
        // 差出人のチェック
        String fromAddress = context.getString(R.string.check_from);
        String orgFromAddress = getFromAddress(message);

        if(!fromAddress.equals(orgFromAddress)){
            return false;
        }

        ////////////////////////////////////////////////////
        // 署名のチェック
        String sign = context.getString(R.string.check_sign);
        String orgBody = getText(message.getContent());

        if(!orgBody.contains(sign)){
            return false;
        }

        ////////////////////////////////////////////////////
        // 警報の登録と、ユニーク情報の格納
        SharedPreferences.Editor editor = sharedPreferences.edit();
        editor.putBoolean("NOW_ALERT", true);
        editor.putLong("RECEIVE_DATE", recvDate);
        editor.putString("MESSAGE_ID", messageId);
        editor.commit();

        return true;
    }


    /**
     * メールアドレス文字列から、メールアドレスだけを抽出する
     * @param message
     * @return
     * @throws javax.mail.MessagingException
     * @throws java.io.UnsupportedEncodingException
     */
    private String getFromAddress(Message message) throws javax.mail.MessagingException, java.io.UnsupportedEncodingException{
        String retval = "";

        Address addresses[] = message.getFrom();
        for (Address address : addresses) {

            // メールアドレスを抽出する
            String decodedAddress = MimeUtility.decodeText(address.toString());
            Pattern pattern = Pattern.compile("[a-zA-Z0-9.!#$%&'*+/=?^_`{|}~-]+@[a-zA-Z0-9-]+(?:\\.[a-zA-Z0-9-]+)*");
            Matcher matcher = pattern.matcher(decodedAddress);
            if (matcher.find()) {
                retval = matcher.group();
            }

        }
        return retval;
    }

    private static String getText(Object content) throws IOException, MessagingException {
        String text = null;
        StringBuffer sb = new StringBuffer();

        if (content instanceof String) {
            sb.append((String) content);
        } else if (content instanceof Multipart) {
            Multipart mp = (Multipart) content;
            for (int i = 0; i < mp.getCount(); i++) {
                BodyPart bp = mp.getBodyPart(i);
                //log.i(TAG, bp.getContentType());
                String contentType = bp.getContentType();
                if(contentType.contains("text/plain")) {
                    sb.append(getText(bp.getContent()));
                }
            }
         }

        text = sb.toString();
        return text;
    }
}
