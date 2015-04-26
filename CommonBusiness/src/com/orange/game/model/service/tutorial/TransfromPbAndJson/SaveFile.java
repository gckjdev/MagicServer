package com.orange.game.model.service.tutorial.TransfromPbAndJson;

import java.io.*;

/**
 * Created by chaoso on 14-9-11.
 */
public class SaveFile {
    public static  final  String savePath = "/Users/chaoso/Documents/";
    public static final String jsonFileName = "tutorialList.json";
    public static final String pbFileName = "tutorial_core.pb";

    public static void saveJsonFile(String pbPath,String jsonPath){
        File file = new File(pbPath);
        File file2 = new File(jsonPath);
        FileOutputStream fos=null;

        Transfromer transfromer = new Transfromer();
//        if (!file.exists()) {
//            try {
//                file.createNewFile();//构建文件
//
//
//            } catch (IOException e) {
//                e.printStackTrace();
//            }
//        }

        try {

            fos = new FileOutputStream(file2,true);
            PrintStream ps = new PrintStream(fos);
            System.setOut(ps);

            String jsonString = transfromer.protoBufToJson(pbPath);
            System.out.println(jsonString);
            
            fos.flush();
        } catch (IOException e) {
            e.printStackTrace();
        } finally {
            if (fos!=null) {
                try {
                    fos.close();
                } catch (IOException e) {
                    System.err.println("文件流关闭失败");
                }
            }
        }
    }

    public static void savePbFile(String jsonPath,String pbPath){
        Transfromer transfromer = new Transfromer();
        transfromer.JsonToProtoBuf(jsonPath,pbPath);
    }

    public static void main(String[] args){

//        saveJsonFile(savePath+pbFileName,savePath+jsonFileName);
        savePbFile(savePath+jsonFileName,savePath+pbFileName);


    }

}
