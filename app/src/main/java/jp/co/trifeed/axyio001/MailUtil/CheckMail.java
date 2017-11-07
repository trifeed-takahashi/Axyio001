package jp.co.trifeed.axyio001.MailUtil;

/**
 * Created by m.takahashi on 2017/11/02.
 */

import android.app.Application;
import android.content.Context;

import javax.mail.*;
import javax.mail.internet.MimeUtility;
import javax.mail.search.ComparisonTerm;
import javax.mail.search.ReceivedDateTerm;

import java.io.IOException;
import java.security.Security;
import java.text.ParseException;
import java.text.SimpleDateFormat;
import java.time.ZonedDateTime;
import java.util.*;
import java.util.regex.Matcher;
import java.util.regex.Pattern;

import jp.co.trifeed.axyio001.MyApplication;
import jp.co.trifeed.axyio001.MyContext;
import jp.co.trifeed.axyio001.R;

public class CheckMail {

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

    public void checkMailTest(){

        // properties を設定
        Properties props = new Properties();
        //Security.addProvider(new com.sun.net.ssl.internal.ssl.Provider());

        props.put("mail.imap.starttls.enable","true");
        props.put("mail.imap.auth", "true");
        props.put("mail.imap.socketFactory.port", this.imap4Port);
        props.put("mail.imap.socketFactory.class", "javax.net.ssl.SSLSocketFactory");
        props.put("mail.imap.socketFactory.fallback", "false");

        Session session = Session.getDefaultInstance(props, null);
        Store store = null;

        try {
            store = session.getStore("imap");
            store.connect(this.host, this.mailAddress, this.mailPassword);

            // 通常の受信フォルダにアクセスる場合は以下固定
            Folder folder = store.getFolder("INBOX");

            // IMAPの場合はラベル名を指定すればそのラベルのメールが取得出来る
            // (POP3の場合はエラーが発生します)
            folder.open(Folder.READ_ONLY);

            //Message[] messages = folder.getMessages();
            Message[] messages = folder.search(new ReceivedDateTerm(ComparisonTerm.GE,
                    new GregorianCalendar(2017,8-1,25).getTime()
            ));

            // メッセージ件数分
            for(Message message : messages) {

                System.out.println("======================================");

                // 件名
                System.out.println("Subject : " + message.getSubject());

                // 送信者
                System.out.println("From : " + getFromAddress(message));

                // 本文
                //System.out.println(getText(message.getContent()));

                // 受信日時
                Date recvDate = getDateFromString(message.getReceivedDate().toString());
                SimpleDateFormat sdf = new SimpleDateFormat("EEE MMM dd HH:mm:ss z yyyy");
                sdf.setTimeZone(TimeZone.getTimeZone("Asia/Tokyo"));
                System.out.println("Date : " + sdf.format(recvDate.getTime()));

            }
            folder.close(false);
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

    private String getFromAddress(Message message) throws javax.mail.MessagingException, java.io.UnsupportedEncodingException{
        String retval = "";

        Address addresses[] = message.getFrom();
        for (Address address : addresses) {
            String decodedAddress = MimeUtility.decodeText(address.toString());
            Pattern pattern = Pattern.compile("[a-zA-Z0-9.!#$%&'*+/=?^_`{|}~-]+@[a-zA-Z0-9-]+(?:\\.[a-zA-Z0-9-]+)*");
            Matcher matcher = pattern.matcher(decodedAddress);
            if (matcher.find()) {
                retval = matcher.group();
            }
        }
        return retval;
    }

    private Date getDateFromString(String dateStr) throws ParseException {

        // US文字列としてパースする
        SimpleDateFormat sdf = new SimpleDateFormat("EEE MMM dd HH:mm:ss z yyyy", Locale.US);
        sdf.setTimeZone(TimeZone.getTimeZone("GMT"));

        // Date型変換
        Date parseDate = sdf.parse(dateStr);

        // 日本時間に変換
        long unixtime = parseDate.getTime();

        return new Date(unixtime);
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
                sb.append(getText(bp.getContent()));
            }
        }

        text = sb.toString();
        return text;
    }
}
