package com.orange.game.model.manager.utils;

import javax.servlet.http.HttpServletRequest;

import org.apache.log4j.Logger;

import com.orange.common.upload.UploadErrorCode;
import com.orange.common.upload.UploadFileResult;
import com.orange.common.upload.UploadManager;
import com.orange.common.upload.UploadManager.ParseResult;
import com.orange.game.constants.ServiceConstant;

public class ImageUploadManager {

	
	String localDir;
	String remoteDir;
	
	public static final Logger log = Logger.getLogger(ImageUploadManager.class
			.getName());
	
	// thread-safe singleton implementation
	private static ImageUploadManager userAvatarManager = new ImageUploadManager(getAvatarFileUploadLocalDir(), getAvatarFileUploadRemoteDir());
	private static ImageUploadManager messageImageManager = new ImageUploadManager(getMessageImageFileUploadLocalDir(), getMessageImageFileUploadRemoteDir());
	private static ImageUploadManager userBackgroundImageManager = new ImageUploadManager(getBackgroundFileUploadLocalDir(), getBackgroundFileUploadRemoteDir());
    private static ImageUploadManager groupImageManager = new ImageUploadManager(getGroupImageFileUploadLocalDir(), getGroupImageFileUploadRemoteDir());
	
	public ImageUploadManager(String localDir, String remoteDir) {
		this.localDir = localDir;
		this.remoteDir = remoteDir;
	}	
	


	public static ImageUploadManager getUserAvatarManager() {
		return userAvatarManager;
	}

	public static ImageUploadManager getUserBackgroundManager() {
		return userBackgroundImageManager;
	}
	
	public static ImageUploadManager getMessageImageManager() {
		return messageImageManager;
	}

    public static ImageUploadManager getGroupImageManager() {
        return groupImageManager;
    }

    private static String getAvatarFileUploadLocalDir() {
		String dir = System.getProperty("upload.local");
		log.info("getFileUploadLocalDir dir = " + dir);
		return (dir == null ? "" : dir); 
	}

	private static String getAvatarFileUploadRemoteDir() {
		String dir = System.getProperty("upload.remote");
		log.info("getFileUploadRemoteDir dir = " + dir);
		return (dir == null ? "" : dir); 
	}	
	
	private static String getBackgroundFileUploadLocalDir() {			
		String dir = System.getProperty("bg_upload.local");
		log.info("getFileUploadLocalDir dir = " + dir);
		return (dir == null ? "" : dir); 
	}

	private static String getBackgroundFileUploadRemoteDir() {
		String dir = System.getProperty("bg_upload.remote");
		log.info("getFileUploadRemoteDir dir = " + dir);
		return (dir == null ? "" : dir); 
	}	
	
	private static String getMessageImageFileUploadLocalDir() {			
		String dir = System.getProperty("upload.local.messageImage");
		log.info("getMessageImageFileUploadLocalDir dir = " + dir);
		return (dir == null ? "" : dir); 
	}

	private static String getMessageImageFileUploadRemoteDir() {
		String dir = System.getProperty("upload.remote.messageImage");
		log.info("getMessageImageFileUploadRemoteDir dir = " + dir);
		return (dir == null ? "" : dir); 
	}

    private static String getGroupImageFileUploadLocalDir() {
        String dir = System.getProperty("upload.local.groupImage");
        log.info("getMessageImageFileUploadLocalDir dir = " + dir);
        return (dir == null ? "" : dir);
    }

    private static String getGroupImageFileUploadRemoteDir() {
        String dir = System.getProperty("upload.remote.groupImage");
        log.info("getMessageImageFileUploadRemoteDir dir = " + dir);
        return (dir == null ? "" : dir);
    }
	
	public String uploadImageAndReturnAbsoluteURL(HttpServletRequest request){
		UploadFileResult result = UploadManager.uploadFile(request, localDir, remoteDir);
		if (result == null){
			return null;
		}
		
		if (result.getErrorCode() == UploadErrorCode.ERROR_SUCCESS){	
			return result.getRemotePathURL();				
		}
		
		return null;
	}
	
	public String uploadImageAndReturnRelativeURL(HttpServletRequest request){
		UploadFileResult result = UploadManager.uploadFile(request, localDir, remoteDir);
		if (result == null){
			return null;
		}
		
		if (result.getErrorCode() == UploadErrorCode.ERROR_SUCCESS){	
			return result.getRelativeURL();				
		}
		
		return null;
	}

	public ParseResult uploadAndCreateThumbImageAndReturnRelativeURL(HttpServletRequest request){
		
		ParseResult result = UploadManager.getFormDataAndSaveImage(request,
				null, ServiceConstant.PARA_IMAGE, ServiceConstant.PARA_DRAW_BG_IMAGE, localDir, remoteDir,
						true,
						false,
						true,
						null,
						null,
						null);

		return result;
	}



	public String getRemoteURL(String localPath) {
		String urlString = remoteDir + localPath;
		return urlString;
	}


	public String getLocalDir() {
		return localDir;
	}

	public String getRemoteDir() {
		return remoteDir;
	}


    public String getLocalURL(String localPath) {
        String urlString = localDir + localPath;
        return urlString;
    }
}
