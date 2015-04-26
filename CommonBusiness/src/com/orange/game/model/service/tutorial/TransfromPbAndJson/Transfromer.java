package com.orange.game.model.service.tutorial.TransfromPbAndJson;

import com.google.protobuf.InvalidProtocolBufferException;
import com.googlecode.protobuf.format.JsonFormat;
import com.orange.game.model.dao.Message;
import com.orange.network.game.protocol.model.TutorialProtos;
import org.apache.commons.lang.StringUtils;
import org.springframework.util.CollectionUtils;

import java.io.*;
import java.util.Collection;
import java.util.logging.Logger;

/**
 * Created by chaoso on 14-9-11.
 */
public class Transfromer {

Logger logger = Logger.getLogger(Transfromer.class.getName());

    public String protoBufToJson(String pbPath){
        String jsonString = "";
        File file = new File(pbPath);
        FileOutputStream reader = null;
        InputStream dis=null;
        byte b[]=new byte[(int)file.length()];
        //返回值,使用StringBuffer
        StringBuffer data = new StringBuffer();
        try {

            dis = new FileInputStream(file);
                 //创建合适文件大小的数组
            dis.read(b);

        } catch (IOException e) {
            e.printStackTrace();
        }finally {
            //关闭文件流
            if (reader != null){
                try {
                    reader.close();
                } catch (IOException e) {
                    e.printStackTrace();
                }
            }
        }

        if(data.equals("")){
            return "";
        }

        try {
            TutorialProtos.PBTutorialCore core = TutorialProtos.PBTutorialCore.parseFrom(b);
            jsonString = JsonFormat.printToString(core);
        } catch (InvalidProtocolBufferException e) {
            e.printStackTrace();
        }

        return jsonString;
    }

//    public static String linkEnter(String ss){
//        StringBuilder sb = new StringBuilder(ss);
//        for()
//
//
//        return "";
//    }

    public void JsonToProtoBuf(String jsonPath,String pbPath){

        File file = new File(jsonPath);
        BufferedReader reader = null;
        //返回值,使用StringBuffer
        StringBuffer data = new StringBuffer();
        try {
            reader = new BufferedReader(new FileReader(file));
            //每次读取文件的缓存
            String temp = null;
            while((temp = reader.readLine()) != null){
                data.append(temp);
            }
        } catch (FileNotFoundException e) {
            e.printStackTrace();
        } catch (IOException e) {
            e.printStackTrace();
        }finally {
            //关闭文件流
            if (reader != null){
                try {
                    reader.close();
                } catch (IOException e) {
                    e.printStackTrace();
                }
            }
        }

        if(data.equals("")){
            return ;
        }
        TutorialProtos.PBTutorialCore.Builder builder = TutorialProtos.PBTutorialCore.newBuilder();

        String jsonFormat = data.toString();
        try {
            JsonFormat.merge(jsonFormat,builder);
        } catch (JsonFormat.ParseException e) {
            e.printStackTrace();
        }

        try {
            DataOutputStream out = new DataOutputStream(new FileOutputStream(pbPath));

//            for(int i=0;i<builder.getTutorialsCount();i++){
//
//                out.write(builder.getTutorials(i).toByteArray());
//            }

            TutorialProtos.PBTutorialCore core = builder.build();

            out.write(core.toByteArray());
            out.close();

            logger.info("create pb data is \n"+core.toString());

        } catch (FileNotFoundException e) {
            e.printStackTrace();
        } catch (IOException e) {
            e.printStackTrace();
        }


    }


}
