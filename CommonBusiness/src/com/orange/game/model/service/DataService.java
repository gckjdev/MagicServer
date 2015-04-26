package com.orange.game.model.service;

import java.util.ArrayList;
import java.util.List;
import java.util.concurrent.ExecutorService;
import java.util.concurrent.Executors;
import java.util.zip.ZipFile;

import org.apache.log4j.Logger;

import com.google.protobuf.InvalidProtocolBufferException;
import com.orange.common.upload.UploadManager;
import com.orange.common.utils.CompressColorUtil;
import com.orange.common.utils.StringUtil;
import com.orange.common.utils.ZipUtil;
import com.orange.network.game.protocol.model.DrawProtos.PBDraw;
import com.orange.network.game.protocol.model.GameBasicProtos.PBDrawAction;
import com.orange.network.game.protocol.model.GameBasicProtos.PBSize;


public class DataService {
	
	public static final Logger log = Logger.getLogger(DataService.class
			.getName());


	private static final int EXECUTOR_POOL_NUM = 3;
	
	public static final int DRAW_VERSION_1 = 1;
	public static final int DRAW_VERSION_2 = 2;
	
	public static final float DRAW_VERSION_1_WIDTH  = 730.0f;
	public static final float DRAW_VERSION_1_HEIGHT = 698.0f;

	public static final float DRAW_VERSION_1_IPHONE_WIDTH  = 304.0f;	
	public static final float DRAW_VERSION_1_IPHONE_HEIGHT = 320.0f;
	
	public static final float DRAW_VERSION_1_IPAD_WIDTH_SCALE = 2.4013f;
	public static final float DRAW_VERSION_1_IPAD_HEIGHT_SCALE = 2.18125f;

	ExecutorService executor = Executors.newFixedThreadPool(EXECUTOR_POOL_NUM);
	
	// thread-safe singleton implementation
	private static DataService service = new DataService();
	private DataService() {
		
	}
	
	private static final String localDrawFileUploadDir = System.getProperty("upload.local.drawData");
	private static final String remoteDrawFileUploadDir = System.getProperty("upload.remote.drawData");
	
	public static String getDrawFileUploadLocalDir() {
		String dir = localDrawFileUploadDir;
		return (dir == null ? "" : dir);
	}

	public static String getDrawFileUploadRemoteDir() {
		String dir = remoteDrawFileUploadDir;
		return (dir == null ? "" : dir);
	}	

//	private static final String localDataFileUploadDir = System.getProperty("upload.local.drawData");
//	private static final String remoteDataFileUploadDir = System.getProperty("upload.remote.drawData");
	
	public static String getDataFileUploadLocalDir(String category) {
		String para = String.format("upload.local.%sData", category.toLowerCase());		
		String dir = System.getProperty(para);
		return (dir == null ? "" : dir);
	}

	public static String getDataFileUploadRemoteDir(String category) {
		String para = String.format("upload.remote.%sData", category.toLowerCase());		
		String dir = System.getProperty(para);
		return (dir == null ? "" : dir);
	}	
	
	
	public static DataService getInstance() {
		return service;
	}

	public String generateRemoteDrawDataUrl(String localUrl, boolean isCompressed) {
		if (StringUtil.isEmpty(localUrl))
			return null;
				
		String urlString = getDrawFileUploadRemoteDir() + localUrl;
		
		if (isCompressed){
			urlString = urlString.replaceAll(".zip", "_c.zip");
		}

		return urlString;
	}

	public String generateLocalDrawDataUrl(String localUrl, boolean isCompressed) {
		if (StringUtil.isEmpty(localUrl))
			return null;
				
		String path = getDrawFileUploadLocalDir() + localUrl;
		if (isCompressed){
			path = path.replaceAll(".zip", "_c.zip");
		}
		
		return path;
	}
	
	
	public byte[] readDrawByteData(String localUrl, boolean isCompressed) {
		String path = generateLocalDrawDataUrl(localUrl, isCompressed);
		if (path == null)
			return null;
		
		return ZipUtil.unzipFile(path);
	}
	
	public int createUncompressDataFile(byte[] pbByteData, String drawDataUrl){
		// conver to PB data
		PBDraw pbDraw = null;
		try {
			pbDraw = PBDraw.parseFrom(pbByteData);
			pbByteData = null;
		} catch (InvalidProtocolBufferException e) {
			log.error("<createUncompressDataFile> failure to parse data "+e.toString(), e);
			return 0;
		}
		
		// debug
//		log.info("<createUncompressDataFile> oldPBDraw="+pbDraw.toString());
		
		
		PBDraw.Builder pbUncompressDrawBuilder = PBDraw.newBuilder(pbDraw);

		int actionCount = pbUncompressDrawBuilder.getDrawDataCount();
		
		PBDrawAction drawAction;		
		for (int i=0; i<actionCount; i++){
			
			// get current draw action
			drawAction = pbUncompressDrawBuilder.getDrawData(i);
			PBDrawAction.Builder newDrawActionBuilder = PBDrawAction.newBuilder(drawAction);
			
			// convert data width
			float newWidth = newDrawActionBuilder.getWidth() * 2.0f;
			newDrawActionBuilder.setWidth(newWidth);
			
			// covert data points X & Y and add data
			int pointCount = newDrawActionBuilder.getPointsCount();
			for (int pointIndex=0; pointIndex < pointCount; pointIndex++){
				int point = newDrawActionBuilder.getPoints(pointIndex);
			    int div = 1<< 15;
			    float y = ((float)(point % div)) * DRAW_VERSION_1_IPAD_HEIGHT_SCALE ;
			    float x = ((float)point / (float)div) * DRAW_VERSION_1_IPAD_WIDTH_SCALE;
			    
			    newDrawActionBuilder.addPointsX(x);
			    newDrawActionBuilder.addPointsY(y);
			}
			
			// clear old data
			newDrawActionBuilder.clearPoints();
			
			// convert old color to new color
			long color = newDrawActionBuilder.getColor();
			float red = CompressColorUtil.getRedFromColor6(color);
			float green = CompressColorUtil.getGreenFromColor6(color);
			float blue = CompressColorUtil.getBlueFromColor6(color);
			float alpha = CompressColorUtil.getAlphaFromColor6(color);
			long newColor = CompressColorUtil.compressColor8WithRed(red, green, blue, alpha);
			
			newDrawActionBuilder.clearColor();
			newDrawActionBuilder.setBetterColor((int)newColor);
			
			// write back data
			pbUncompressDrawBuilder.setDrawData(i, newDrawActionBuilder.build());
			
			drawAction = null;
			newDrawActionBuilder = null;
		}

		// set uncompressed flag
		pbUncompressDrawBuilder.setIsCompressed(false);
		
		// set data frame size
		PBSize.Builder sizeBuilder = PBSize.newBuilder();
		sizeBuilder.setHeight(DRAW_VERSION_1_HEIGHT);
		sizeBuilder.setWidth(DRAW_VERSION_1_WIDTH);		
		
		// set version
		pbUncompressDrawBuilder.setVersion(DRAW_VERSION_2);
		pbUncompressDrawBuilder.setCanvasSize(sizeBuilder.build());
		
		// write back PB data
		PBDraw newPBDraw = pbUncompressDrawBuilder.build();				
		pbByteData = newPBDraw.toByteArray();
		
		// debug
//		log.info("<createUncompressDataFile> newPBDraw="+newPBDraw.toString());
		
		// clear data
		newPBDraw = null;
		pbUncompressDrawBuilder = null;
		
		// write into file
		String uncompressedFilePath = generateLocalDrawDataUrl(drawDataUrl, false);		
		ZipUtil.createZipFile(uncompressedFilePath, 
				UploadManager.DEFAULT_ZIP_FILE_NAME, pbByteData);
		
		int len = pbByteData.length;
		pbByteData = null;
		
		return len;		
	}
	
	public boolean createUncompressDataFile(String drawDataUrl) {
		
		// read compress data
		byte[] pbByteData = readDrawByteData(drawDataUrl, true);
		if (pbByteData == null){
			log.warn("<createUncompressDataFile> but not compressed data");
			return false;
		}
		
		return (createUncompressDataFile(pbByteData, drawDataUrl) > 0);
	}

	public static int roundFloatValue(float value){
	    int round = (int)value;
	    if (value - round > 0.5) {
	        ++round;
	    }
	    return round;
	}
	
	public static long getUnsignedInt (int data){     //将int数据转换为0~4294967295 (0xFFFFFFFF即DWORD)。
        return data & 0x0FFFFFFFFl;
     }
	
	public boolean createCompressDataFile(byte[] pbByteData, String drawDataUrl){
		// conver to PB data
		PBDraw pbDraw = null;
		try {
			pbDraw = PBDraw.parseFrom(pbByteData);
			pbByteData = null;
		} catch (InvalidProtocolBufferException e) {
			log.error("<createCompressDataFile> failure to parse data "+e.toString(), e);
			return false;
		}
		
		// calculate scale
		PBSize drawSize = pbDraw.getCanvasSize();
		float drawWidth = DRAW_VERSION_1_WIDTH;
		float drawHeight = DRAW_VERSION_1_HEIGHT;
		if (drawSize != null){
			drawWidth = drawSize.getWidth();
			drawHeight = drawSize.getHeight();
		}
		float widthScale = drawWidth / DRAW_VERSION_1_IPHONE_WIDTH;
		float heightScale = drawHeight / DRAW_VERSION_1_IPHONE_HEIGHT;
		
		float penWidthScale = (widthScale+heightScale)/2.0f;
		
		PBDraw.Builder pbCompressDrawBuilder = PBDraw.newBuilder(pbDraw);

		int actionCount = pbCompressDrawBuilder.getDrawDataCount();
		
		PBDrawAction drawAction;		
		for (int i=0; i<actionCount; i++){
			
			// get current draw action
			drawAction = pbCompressDrawBuilder.getDrawData(i);
			
			if (drawAction.getType() == 3){					
					// change backgroud color set width to 3000
					// (0,1500) -> (3000, 1500)
					
					float FIXED_WIDTH_FOR_CLEAN = 3000.0f;
					int PEN_TYPE_ERASER = 1100;

					long color = getUnsignedInt(drawAction.getBetterColor());	// to fix red color lose issue
					float red = CompressColorUtil.getRedFromColor8(color);
					float green = CompressColorUtil.getGreenFromColor8(color);
					float blue = CompressColorUtil.getBlueFromColor8(color);
					float alpha = CompressColorUtil.getAlphaFromColor8(color);					
					long newColor = CompressColorUtil.compressColor6WithRed(red, green, blue, alpha);		

					float penWidth = FIXED_WIDTH_FOR_CLEAN; // FIXED_WIDTH_FOR_CLEAN / penWidthScale;
					
					// add data points X & Y and add data
					List<Integer> pointList = new ArrayList<Integer>();

					float x1 = 0.0f;
					float y1 = 1500.0f;													
					int point1 = (DataService.roundFloatValue(x1) * (1 << 15)) + DataService.roundFloatValue(y1);
					pointList.add(point1);
					
					float x2 = 3000.0f;
					float y2 = 1500.0f;													
					int point2 = (DataService.roundFloatValue(x2) * (1 << 15)) + DataService.roundFloatValue(y2);
					pointList.add(point2);
					
					PBDrawAction.Builder newDrawActionBuilder = PBDrawAction.newBuilder();
					newDrawActionBuilder.setType(0);
					newDrawActionBuilder.setPenType(PEN_TYPE_ERASER);
					newDrawActionBuilder.setWidth(penWidth);
					newDrawActionBuilder.setColor((int)newColor);
					newDrawActionBuilder.addAllPoints(pointList);
					
					// write back data
					pbCompressDrawBuilder.setDrawData(i, newDrawActionBuilder.build());										
					continue;
			}
			
			PBDrawAction.Builder newDrawActionBuilder = PBDrawAction.newBuilder(drawAction);
			
			// convert data width
			float newWidth = newDrawActionBuilder.getWidth() / penWidthScale;
			newDrawActionBuilder.setWidth(newWidth);
			
			// covert data points X & Y and add data
			int pointCount = newDrawActionBuilder.getPointsXCount();
			for (int pointIndex=0; pointIndex < pointCount; pointIndex++){
				
				float x = newDrawActionBuilder.getPointsX(pointIndex) / widthScale;
				float y = newDrawActionBuilder.getPointsY(pointIndex) / heightScale;
												
				int point = (roundFloatValue(x) * (1 << 15)) + roundFloatValue(y);

			    newDrawActionBuilder.addPoints(point);
			}
			
			// clear old data
			newDrawActionBuilder.clearPointsX();
			newDrawActionBuilder.clearPointsY();
			
			// convert new color to old color
			long color = getUnsignedInt(newDrawActionBuilder.getBetterColor());	// to fix red color lose issue
			float red = CompressColorUtil.getRedFromColor8(color);
			float green = CompressColorUtil.getGreenFromColor8(color);
			float blue = CompressColorUtil.getBlueFromColor8(color);
			float alpha = CompressColorUtil.getAlphaFromColor8(color);
			
			long newColor = CompressColorUtil.compressColor6WithRed(red, green, blue, alpha);
			newDrawActionBuilder.setColor((int)newColor);
			newDrawActionBuilder.clearBetterColor();
			
			// write back data
			pbCompressDrawBuilder.setDrawData(i, newDrawActionBuilder.build());
			
			drawAction = null;
			newDrawActionBuilder = null;
		}

		// set uncompressed flag
		pbCompressDrawBuilder.setIsCompressed(true);
		
		// set data frame size
//		PBSize.Builder sizeBuilder = PBSize.newBuilder();
//		sizeBuilder.setHeight(DRAW_VERSION_1_HEIGHT);
//		sizeBuilder.setWidth(DRAW_VERSION_1_WIDTH);		
		pbCompressDrawBuilder.clearCanvasSize();
		
		
		
		// set version
		int version = pbDraw.getVersion();
		if (version == 0){
			version = DRAW_VERSION_1;
		}
		pbCompressDrawBuilder.setVersion(version);
		
		// write back PB data
		PBDraw newPBDraw = pbCompressDrawBuilder.build();
		pbByteData = newPBDraw.toByteArray();
		
		// clear data
		newPBDraw = null;
		pbCompressDrawBuilder = null;
		
		// write into file
		String compressedFilePath = generateLocalDrawDataUrl(drawDataUrl, true);		
		ZipUtil.createZipFile(compressedFilePath, 
				UploadManager.DEFAULT_ZIP_FILE_NAME, pbByteData);
		
		return true;
		
	}
	
	
	public boolean createCompressDataFile(String drawDataUrl) {
		
		// read uncompress data
		byte[] pbByteData = readDrawByteData(drawDataUrl, false);
		if (pbByteData == null){
			log.warn("<createCompressDataFile> but not uncompressed data read");
			return false;
		}
		
		return createCompressDataFile(pbByteData, drawDataUrl);
	}
	
	
	
}
