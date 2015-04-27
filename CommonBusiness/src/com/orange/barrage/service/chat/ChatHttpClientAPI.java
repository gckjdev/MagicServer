package com.orange.barrage.service.chat;

import com.orange.common.api.service.CommonParameter;
import com.orange.common.utils.DateUtil;
import com.orange.common.utils.PropertyUtil;
import com.orange.protocol.message.MessageProtos;
import com.orange.protocol.message.UserProtos;
import com.sun.javafx.tools.ant.Platform;
import org.apache.commons.httpclient.DefaultHttpMethodRetryHandler;
import org.apache.commons.httpclient.HttpClient;
import org.apache.commons.httpclient.HttpException;
import org.apache.commons.httpclient.HttpStatus;
import org.apache.commons.httpclient.methods.ByteArrayRequestEntity;
import org.apache.commons.httpclient.methods.GetMethod;
import org.apache.commons.httpclient.methods.PostMethod;
import org.apache.commons.httpclient.methods.RequestEntity;
import org.apache.commons.httpclient.params.HttpMethodParams;
import org.apache.log4j.Logger;

import java.io.*;

/**
 * Created by pipi on 15/4/27.
 */
public class ChatHttpClientAPI {

    public static final Logger log = Logger.getLogger(ChatHttpClientAPI.class.getName());

    private static ChatHttpClientAPI ourInstance = new ChatHttpClientAPI();

    public static ChatHttpClientAPI getInstance() {
        return ourInstance;
    }

    private ChatHttpClientAPI() {
    }

    public static final String CHAT_SERVER_URL = PropertyUtil.getStringProperty("chat.url", "http://localhost:8100/?m=req&format=pb&from=api");

    public MessageProtos.PBSendChatResponse sendChat(UserProtos.PBChat pbChat, String fromUserId){

        // init response builder
        MessageProtos.PBSendChatResponse sendChatResponse = null;

        // create send chat
        MessageProtos.PBSendChatRequest.Builder sendChatBuilder = MessageProtos.PBSendChatRequest.newBuilder();
        sendChatBuilder.setChat(pbChat);

        // create data request
        MessageProtos.PBDataRequest.Builder reqBuilder = MessageProtos.PBDataRequest.newBuilder();
        reqBuilder.setUserId(fromUserId);
        reqBuilder.setType(MessageProtos.PBMessageType.MESSAGE_SEND_CHAT_VALUE);
        reqBuilder.setRequestId(DateUtil.getCurrentSeconds());
        reqBuilder.setSendChatRequest(sendChatBuilder.build());

        // Create an instance of HttpClient.
        HttpClient client = new HttpClient();

        // Create a method instance.
        PostMethod method = new PostMethod(CHAT_SERVER_URL);

        // Provide custom retry handler is necessary
        method.getParams().setParameter(HttpMethodParams.RETRY_HANDLER,
                new DefaultHttpMethodRetryHandler(1, false));

        try {

            MessageProtos.PBDataRequest req = reqBuilder.build();
            log.info("<apiSendChat> request="+req.toString());

            // set POST request data
            RequestEntity entity = new ByteArrayRequestEntity(req.toByteArray(), CommonParameter.APPLICATION_PB);
            method.setRequestEntity(entity);
            method.setRequestHeader("Content-Type", CommonParameter.APPLICATION_PB);

            // send POST request
            int statusCode = client.executeMethod(method);

            if (statusCode != HttpStatus.SC_OK) {
                log.error("<apiSendChat> http response status error =" + statusCode + ", " + method.getStatusLine());
            }
            else{
                // Read the response body and parse to PB
                byte[] b = readBytes(method);
                log.info("<apiSendChat> read total bytes="+b.length);

                MessageProtos.PBDataResponse rsp = MessageProtos.PBDataResponse.parseFrom(b);
                sendChatResponse = rsp.getSendChatResponse();
                log.info("<apiSendChat> response="+rsp.toString());
            }

        } catch (HttpException e) {
            log.error("<apiSendChat> catch HttpException=" + e.getMessage(), e);
        } catch (IOException e) {
            log.error("<apiSendChat> catch IOException=" + e.getMessage(), e);
        } catch (Exception e) {
            log.error("<apiSendChat> catch Exception=" + e.getMessage(), e);
        } finally {
            // Release the connection.
            method.releaseConnection();
        }

        return sendChatResponse;
    }

    private byte[] readBytes(PostMethod method){
        InputStream in = null;
        try {
            in = method.getResponseBodyAsStream();
        } catch (IOException e) {
            log.error("<apiSendChat> read response but catch Exception=" + e.getMessage(), e);
            return null;
        }

        BufferedInputStream input = new BufferedInputStream(in);
        ByteArrayOutputStream output = new ByteArrayOutputStream();
        byte b[] = new byte[1024];
        int read = 0;
        try {
            while ((read=input.read(b)) > -1) {
                output.write(b, 0, read);
            }

            b = output.toByteArray();

        } catch (IOException e) {
            b = null;
            log.warn("<apiSendChat> close exception=" + e.getMessage(), e);
        }

        try {
            in.close();
        } catch (IOException e) {
            log.warn("<apiSendChat> close exception=" + e.getMessage(), e);
        }

        try {
            input.close();
        } catch (IOException e) {
            log.warn("<apiSendChat> close exception=" + e.getMessage(), e);
        }

        try {
            output.flush();
        } catch (IOException e) {
            log.warn("<apiSendChat> close exception=" + e.getMessage(), e);
        }

        try {
            output.close();
        } catch (IOException e) {
        }

        return b;
    }
}
