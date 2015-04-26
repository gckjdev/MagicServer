package com.orange.game.model.service.mail;


import com.orange.common.mail.CommonMailSender;
import com.orange.game.constants.ErrorCode;
import org.apache.log4j.Logger;

import java.util.ArrayList;
import java.util.List;
import java.util.concurrent.ExecutorService;
import java.util.concurrent.Executors;

/**
 * Created with IntelliJ IDEA.
 * User: qqn_pipi
 * Date: 13-8-15
 * Time: 下午1:34
 * To change this template use File | Settings | File Templates.
 */
public class MailService {

    public static final Logger log = Logger.getLogger(MailService.class.getName());

    private static final String DEFAULT_REPLY_ADDRESS = "noreply@xiaoji.info";
    private static final String DEFAULT_FROM_ADDRESS = "noreply@xiaoji.info";
    private static final String DEFAULT_FROM_NAME = "小吉";

    private ExecutorService service = Executors.newSingleThreadExecutor();
    private static MailService ourInstance = new MailService();

    public static MailService getInstance() {
        return ourInstance;
    }

    private MailService() {
    }


    public void sendEmailVerification(){

    }

    public void checkEmailVerificationResult(){

    }

    public void sendResetPasswordEmail(){

    }

    public int sendEmail(final String emailAddress, final String subject, final String body) {

        try {

            String[] addresses = new String[]{emailAddress};
            CommonMailSender sender = new CommonMailSender(addresses,subject,body,null);
            sender.send();
        } catch (Exception e) {
            log.error("<sendEmail> email = " + emailAddress + " but catch exception, e="+e.toString(), e);
            return ErrorCode.ERROR_SEND_EMAIL_EXCEPTION;
        }

        return 0;

        /*
        Message message = new Message(DEFAULT_FROM_ADDRESS, DEFAULT_FROM_NAME);

        // 正文， 使用html形式，或者纯文本形式
        message.setBody(body); // html

        // 添加to, cc, bcc replyto
        message.setSubject(subject);

        List<String> addressList = new ArrayList<String>();
        addressList.add(emailAddress);

        try {
            message.addRecipients(addressList)
//                .addRecipient("example2@sendcloud.org")
//                .addBcc("bcc@sendcloud.org")
//                .addCc("cc@sendcloud.org")
                .setReplyAddress(DEFAULT_REPLY_ADDRESS);

            // 添加附件
//        message.addAttachment("SendCloud SDK 1.0.5.docx",
//                "/path/to/SendCloud SDK 1.0.5.docx");

            // 组装消息发送邮件
            // 不同于登录SendCloud站点的帐号，您需要登录后台创建发信域名，获得对应发信域名下的帐号和密码才可以进行邮件的发送。
            final SendCloud sendCloud = new SendCloud("postmaster@gckjdev1.sendcloud.org", "7jLJ67Eo");
            sendCloud.setMessage(message);

            service.execute(new Runnable() {

                @Override
                public void run() {

                    // TODO disable debug
                    sendCloud.setDebug(true); //设置调试, 可以看到java mail的调试信息
                    try {
                        log.info("<sendEmail> sending email = " + emailAddress);
                        sendCloud.send();

                        // 获取emailId列表
                        log.info("<sendEmail> OK! email = " + emailAddress + " email id list = " + sendCloud.getEmailIdList());
                    } catch (Exception e) {
                        log.error("<sendEmail> email = " + emailAddress + " but catch exception, e="+e.toString(), e);
                    }
                }
            });

            return 0;
        } catch (Exception e) {
            log.error("<sendEmail> email = " + emailAddress + " but catch exception, e="+e.toString(), e);
            return ErrorCode.ERROR_SEND_EMAIL_EXCEPTION;
        }
        */

    }
}
