package jp.co.trifeed.axyio001.MailUtil;

/**
 * Created by m.takahashi on 2017/11/02.
 */

import android.app.Application;

import javax.mail.*;
import java.util.*;

import jp.co.trifeed.axyio001.R;

public class CheckMail {

    public void setHost(String host) {
        this.host = host;
    }

    public void setImap4Ssl(boolean imap4Ssl) {
        this.imap4Ssl = imap4Ssl;
    }

    public void setImap4Port(String imap4Port) {
        this.imap4Port = imap4Port;
    }

    public void setMailAddress(String mailAddress) {
        this.mailAddress = mailAddress;
    }

    public void setMailPassword(String mailPassword) {
        this.mailPassword = mailPassword;
    }

    private String host;
    private boolean imap4Ssl;
    private String imap4Port;
    private String mailAddress;
    private String mailPassword;

    public void checkMailTest(){

        // properties を設定
        Properties props = System.getProperties();
        props.put("mail.pop3.host", host);

    }

}
