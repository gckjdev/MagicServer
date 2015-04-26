package com.orange.game.constants;

import com.orange.common.api.service.error.CommonErrorCode;

public class ErrorCode extends CommonErrorCode {

	
	static public final int ERROR_SUCCESS = 0;

	// Parameter Error
	static public final int ERROR_PARAMETER                 = 10001;
	static public final int ERROR_PARA_METHOD_NOT_FOUND     = 10002;
	static public final int ERROR_PARAMETER_TIMESTAMP_EMPTY = 10003;
	static public final int ERROR_PARAMETER_TIMESTAMP_NULL 	= 10004;
	static public final int ERROR_PARAMETER_MAC_EMPTY 		= 10005;
	static public final int ERROR_PARAMETER_MAC_NULL 		= 10006;		
	static public final int ERROR_PARAMETER_USERID_NULL 	= 10007;  
	static public final int ERROR_PARAMETER_USERID_EMPTY 	= 10008;  
	static public final int ERROR_PARAMETER_APPID_NULL 		= 10009;  
	static public final int ERROR_PARAMETER_APPID_EMPTY 	= 10010;
	static public final int ERROR_PARAMETER_STATUS_NULL 	= 10011;
	static public final int ERROR_PARAMETER_USERTYPE_EMPTY 	= 10012;
	static public final int ERROR_PARAMETER_USERTYPE_NULL 	= 10013;
	static public final int ERROR_PARAMETER_DEVICEID_EMPTY 	= 10014;
	static public final int ERROR_PARAMETER_DEVICEID_NULL 	= 10015;
	static public final int ERROR_PARAMETER_DEVICEMODEL_EMPTY = 10016;
	static public final int ERROR_PARAMETER_DEVICEMODEL_NULL = 10017;
	static public final int ERROR_PARAMETER_DEVICEOS_EMPTY = 10018;
	static public final int ERROR_PARAMETER_DEVICEOS_NULL	 = 10019;
	static public final int ERROR_PARAMETER_PASSWORD_EMPTY 	 = 10020;
	static public final int ERROR_PARAMETER_PASSWORD_NULL 	 = 10021;
	static public final int ERROR_PARAMETER_COUNTRYCODE_EMPTY = 10022;
	static public final int ERROR_PARAMETER_COUNTRYCODE_NULL = 10023;
	static public final int ERROR_PARAMETER_LANGUAGE_EMPTY 	 = 10024;
	static public final int ERROR_PARAMETER_LANGUAGE_NULL 	 = 10025;
	public static final int ERROR_PARAMETER_LONGITUDE_EMPTY  = 10026;
	public static final int ERROR_PARAMETER_LONGITUDE_NULL 	 = 10027;
	public static final int ERROR_PARAMETER_LATITUDE_EMPTY 	 = 10028;
	public static final int ERROR_PARAMETER_LATITUDE_NULL 	 = 10029;
	public static final int ERROR_PARAMETER_NAME_EMPTY 		 = 10030;
	public static final int ERROR_PARAMETER_NAME_NULL 		 = 10031;
	public static final int ERROR_PARAMETER_RADIUS_NULL 	 = 10032;
	public static final int ERROR_PARAMETER_RADIUS_EMPTY 	 = 10033;
	public static final int ERROR_PARAMETER_POSTTYPE_EMPTY 	 = 10034;
	public static final int ERROR_PARAMETER_POSTTYPE_NULL 	 = 10035;
	public static final int ERROR_PARAMETER_DESC_NULL 		 = 10036;
	public static final int ERROR_PARAMETER_DESC_EMPTY 		 = 10037;
	public static final int ERROR_PARAMETER_USER_LONGITUDE_EMPTY = 10038;
	public static final int ERROR_PARAMETER_USER_LONGITUDE_NULL = 10039;
	public static final int ERROR_PARAMETER_USER_LATITUDE_EMPTY = 10040;
	public static final int ERROR_PARAMETER_USER_LATITUDE_NULL 	= 10041;
	public static final int ERROR_PARAMETER_TEXTCONTENT_NULL 	= 10042;
	public static final int ERROR_PARAMETER_TEXTCONTENT_EMPTY 	= 10043;
	public static final int ERROR_PARAMETER_CONTENTTYPE_EMPTY 	= 10044;
	public static final int ERROR_PARAMETER_CONTENTTYPE_NULL 	= 10045;
	public static final int ERROR_PARAMETER_PLACEID_NULL 		= 10046;
	public static final int ERROR_PARAMETER_PLACEID_EMPTY 		= 10047;
	public static final int ERROR_PARAMETER_PRODUCTID_EMPTY 	= 10048;
	public static final int ERROR_PARAMETER_PRODUCTID_NULL 		= 10049;
	public static final int ERROR_PARAMETER_TOUSERID_NULL 		= 10050;
	public static final int ERROR_PARAMETER_TOUSERID_EMPTY 		= 10051;
	public static final int ERROR_PARAMETER_MESSAGECONTENT_EMPTY = 10052;
	public static final int ERROR_PARAMETER_MESSAGECONTENT_NULL = 10053;
	public static final int ERROR_PARAMETER_MESSAGEID_EMPTY 	= 10054;
	public static final int ERROR_PARAMETER_MESSAGEID_NULL 		= 10055;
	public static final int ERROR_PARAMETER_ACTIONNAME_EMPTY 	= 10056;
	public static final int ERROR_PARAMETER_ACTIONNAME_NULL 	= 10057;
	public static final int ERROR_PARAMETER_CATEGORY_NULL 		= 10058;
	public static final int ERROR_PARAMETER_CATEGORY_EMPTY 		= 10059;
	public static final int ERROR_PARAMETER_KEYWORD_NULL 		= 10060;
	public static final int ERROR_PARAMETER_KEYWORD_EMPTY 		= 10061;
	public static final int ERROR_PARAMETER_MOBILE_NULL 		= 10062;
	public static final int ERROR_PARAMETER_MOBILE_EMPTY 		= 10063;
	public static final int ERROR_PARAMETER_EMAIL_NULL 			= 10064;
	public static final int ERROR_PARAMETER_EMAIL_EMPTY 		= 10065;
	public static final int ERROR_PARAMETER_VERIFYCODE_NULL 		= 10066;
	public static final int ERROR_PARAMETER_VERIFYCODE_EMPTY 		= 10067;
	public static final int ERROR_PARAMETER_VERIFICATION_NULL 		= 10068;
	public static final int ERROR_PARAMETER_VERIFICATION_EMPTY 		= 10069;
	public static final int ERROR_PARAMETER_ITEMID_EMPTY 			= 10070;
	public static final int ERROR_PARAMETER_ITEMID_NULL 			= 10071;
	public static final int ERROR_PARAMETER_SNSID_EMPTY             = 10072;
	public static final int ERROR_PARAMETER_SNSID_NULL              = 10073;
	public static final int ERROR_PARAMETER_REGISTER_TYPE_EMPTY     = 10074;
	public static final int ERROR_PARAMETER_REGISTER_TYPE_NULL      = 10075;
	public static final int ERROR_PARAMETER_UNKNOWN_REGISTER_TYPE   = 10076;
	public static final int ERROR_PARAMETER_SEGMENT_EMPTY           = 10077;
	public static final int ERROR_PARAMETER_SEGMENT_NULL            = 10078;
	public static final int ERROR_PARAMETER_COMPAREWORD_EMPTY       = 10079;
	public static final int ERROR_PARAMETER_COMPAREWORD_NULL        = 10080;
	public static final int ERROR_PARAMETER_PRICE_TYPE_NULL 		= 10081;
	public static final int ERROR_PARAMETER_PRICE_TYPE_EMPTY 		= 10082;
	public static final int ERROR_PARAMETER_PRICE_TYPE_ILLEGAL 		= 10083;

	public static final int ERROR_PARAMETER_ROOMID_EMPTY 			= 10084;
	public static final int ERROR_PARAMETER_ROOMID_NULL 			= 10085;
    public static final int ERROR_PARAMETER_TARGET_USERID_EMPTY 	= 10086;
	public static final int ERROR_PARAMETER_TARGET_USERID_NULL 		= 10087;
	
	public static final int ERROR_PARAMETER_WORD_EMPTY 				    = 10088;
	public static final int ERROR_PARAMETER_WORD_NULL 					= 10089;

	public static final int ERROR_PARAMETER_ACTIONTYPE_EMPTY 		    = 10090;
	public static final int ERROR_PARAMETER_ACTIONTYPE_NULL 		    = 10091;

	public static final int ERROR_PARAMETER_OPUSID_EMPTY 			    = 10092;
	public static final int ERROR_PARAMETER_OPUSID_NULL 			    = 10093;

	public static final int ERROR_USER_ACTION_INVALID 				    = 10094;
	
	public static final int ERROR_PARAMETER_OPUS_CREATOR_UID_EMPTY      = 10095;
	public static final int ERROR_PARAMETER_OPUS_CREATOR_UID_NULL       = 10096;

	public static final int ERROR_PARAMETER_COMMENT_EMPTY 			    = 10097;
	public static final int ERROR_PARAMETER_COMMENT_NULL 			    = 10098;
	
	public static final int ERROR_PARAMETER_DRAWDATA_NULL 			    = 10099;
	public static final int ERROR_PARAMETER_NO_LOCATION 				= 10100;

	public static final int ERROR_PARAMETER_GAMEID_EMPTY 				= 10101;
	public static final int ERROR_PARAMETER_GAMEID_NULL 				= 10102;	
	public static final int ERROR_DEVICE_TYPE_ERROR 					= 10103;
	public static final int ERROR_PARAMETER_REPLY_MESSAGE_ID 		    = 10104;
	
	public static final int ERROR_PARAMETER_WALLID_EMPTY 				= 10105;
	public static final int ERROR_PARAMETER_WALLID_NULL 				= 10106;

	public static final int ERROR_PARAMETER_COUNT_EMPTY 				= 10107;
	public static final int ERROR_PARAMETER_COUNT_NULL 					= 10108;
	public static final int ERROR_PARAMETER_PRICE_EMPTY 				= 10109;
	public static final int ERROR_PARAMETER_PRICE_NULL 					= 10110;
	public static final int ERROR_PARAMETER_CURRENCY_EMPTY 			    = 10111;
	public static final int ERROR_PARAMETER_CURRENCY_NULL 			    = 10112;
	
	public static final int ERROR_PARAMETER_USAGE_INCORRECT 		    = 10113;
	public static final int ERROR_PARAMETER_USER_PHOTO_ID_NULL          = 10114;
	public static final int ERROR_PARAMETER_METADATA_NULL		        = 10115;
    public static final int ERROR_PARAMETER_DATE_NULL                   = 10116;

    public static final int ERROR_PARAMETER_RANK_TYPE_EMPTY             = 10117;
    public static final int ERROR_PARAMETER_RANK_TYPE_VALUE_NOT_MATCH   = 10118;
    public static final int ERROR_PARAMETER_PREFIX_INVALID              = 10119;
    public static final int ERROR_PARAMETER_DATA_LEN_EMPTY              = 10120;
    public static final int ERROR_PARAMETER_TYPE_ILLEGAL 		        = 10121;
    public static final int ERROR_PARAMETER_CREDENTIAL_EMPTY            = 10122;
    public static final int ERROR_PARAMETER_SNSID_INVALID               = 10123;

    public static final int ERROR_PARAMETER_CLASS_EMPTY 			    = 10124;
    public static final int ERROR_PARAMETER_CLASS_NULL 			        = 10125;




    // User Errors
	static public final int ERROR_LOGINID_EXIST             			= 20001;
	static public final int ERROR_DEVICEID_BIND            			= 20002;
	public static final int ERROR_DEVICE_NOT_BIND 					= 20003;
	static public final int ERROR_LOGINID_DEVICE_BOTH_EXIST 	= 20004; 
	static public final int ERROR_USERID_NOT_FOUND          		= 20005;
	static public final int ERROR_CREATE_USER							= 20006;
	public static final int ERROR_USER_GET_NICKNAME 				= 20007;
	public static final int ERROR_EMAIL_EXIST							= 20008;
	public static final int ERROR_EMAIL_VERIFIED						= 20009;
	public static final int ERROR_PASSWORD_NOT_MATCH        	= 20010;
	public static final int ERROR_EMAIL_NOT_VALID        			= 20011;
    public static final int ERROR_DEVICE_TOKEN_NULL         		= 20012;
    public static final int ERROR_USER_EMAIL_NOT_FOUND      	= 20013;
    public static final int ERROR_SNS_ID_EXIST              			= 20014;
    public static final int ERROR_UPDATE_USER_INFO_FAILED   	= 20015;
    public static final int ERROR_PASSWORD_NOT_VALID        	= 20016;
    public static final int ERROR_FOLLOW_USER_NOT_FOUND     	= 20017;
    public static final int ERROR_USER_IS_BLACK_FRIEND 			= 20018;
	public static final int ERROR_LEVEL_INFO_NOT_FOUND 			= 20019;
	public static final int ERROR_ADD_USER_PHOTO 				= 20020;

	public static final int ERROR_DELETE_USER_PHOTO             = 20021;
	public static final int ERROR_GET_USER_PHOTO_LIST           = 20022;
    public static final int ERROR_USER_ALREADY_VERIFIED         = 20023;
    public static final int ERROR_USER_CREATE_VERIFY_CODE       = 20024;
    public static final int ERROR_USER_CREATE_NEW_PASSWORD      = 20025;
    public static final int ERROR_USER_UPDATE_VERFICATION_STATUS = 20026;
    public static final int ERROR_USER_VERIFYCODE_NULL           = 20027;
    public static final int ERROR_USER_VERIFYCODE_INVALID        = 20028;
    public static final int ERROR_SET_USER_NUMBER                = 20029;
    public static final int ERROR_DEVICEID_INVALID               = 20030;

    static public final int ERROR_USER_NOT_FOUND          		= 20031;
    static public final int ERROR_USER_APP_ALREADY_AWARD        = 20032;
    public static final int ERROR_USER_ALREADY_EXIST            = 20033;






    // User Shopping Item Error
	public static final int ERROR_ADD_SHOPPING_ITEM 			= 30001;
	public static final int ERROR_UPDATE_SHOPPING_ITEM 		    = 30002;
	public static final int ERROR_DELETE_SHOPPING_ITEM 		    = 30003;

	static public final int ERROR_POST_NOT_FOUND            = 40001;
	public static final int ERROR_CREATE_POST 				= 40002;
	public static final int ERROR_GET_POST_BY_PLACE 		= 40003;
	public static final int ERROR_GET_USER_TIMELINE 		= 40004;
	public static final int ERROR_GET_USER_FOLLOW_PLACE		= 40005;
	public static final int ERROR_GET_RELATED_POST_BY_POST 	= 40006;
	public static final int ERROR_GET_ME_MESSAGE 			= 40007;
	public static final int ERROR_GET_PUBLIC_POST 			= 40008;
	public static final int ERROR_ACTION_ON_POST 			= 40009;
	//Message Error
	
	public static final int ERROR_GET_MY_MESSAGE 			                = 50001;
    public static final int ERROR_GET_MESSAGE_GROUP_NOT_SUPPORT 			= 50002;
    public static final int ERROR_CANNOT_DELETE_GROUP_MESSAGE               = 50003;

	//App Error
	public static final int ERROR_APP_UPDATE_NOT_FOUND 		= 60001;
    public static final int ERROR_APP_NOT_FOUND             = 60002;
    public static final int ERROR_APP_EMPTY_PUSH_INFO       = 60003;
    public static final int ERROR_APP_CERT_NOT_FOUND        = 60004;
    public static final int ERROR_APNS_NOT_FOUND            = 60005;
    public static final int ERROR_PUSH_MANAGER_NULL         = 60006;
    public static final int ERROR_PUSH_EXCEPTION            = 60007;
    public static final int ERROR_DEVICE_TOKEN_EXPIRED      = 60008;

    // account charging
	public static final int ERROR_CHARGE_ACCOUNT 						= 70001;
	public static final int ERROR_DEDUCT_ACCOUNT 						= 70002;
	public static final int ERROR_INVALID_TRANSACTION_ID 			= 70003;
	public static final int ERROR_INVALID_TRANSACTION_RECEIPT 	= 70004;
        public static final int ERROR_BALANCE_NOT_ENOUGH = 70005;

	public static final int ERROR_BUY_ITEM  				= 70010;
	public static final int ERROR_ITEM_COUNT_INCORRECT 		= 70011;
	public static final int ERROR_USE_ITEM  				= 70012;
    public static final int ERROR_GET_BUY_VIP_USER_COUNT    = 70013;



	
	// Room Error
	public static final int ERROR_ROOM_UPDATE_PERMISSION 	= 71001;
	public static final int ERROR_INVITE_USER 				= 71002;
	public static final int ERROR_REMOVE_ROOM 				= 71003;

	// DB Error
	static public final int ERROR_DATABASE_SAVE             = 80001;
	static public final int ERROR_CASSANDRA_UNAVAILABLE    	= 80002;

	// System Error
	static public final int ERROR_SYSTEM       							= 90001;
	static public final int ERROR_NOT_GET_METHOD       			= 90002;
	static public final int ERROR_INVALID_SECURITY				= 90003;
	static public final int ERROR_NAME_VALUE_NOTMATCH		= 90004;
	static public final int ERROR_JSON 									= 90005;
	static public final int ERROR_UPLOAD_FILE 						= 90006;
	public static final int ERROR_CREATE_THUNMB_FILEPATH 	= 90007;
	public static final int ERROR_CREATE_THUNMB_FILE			= 90008;
    public static final int ERROR_GENERAL_EXCEPTION    		= 90009;
	public static final int ERROR_PROTOCOL_BUFFER_NULL 		= 90010;
	public static final int ERROR_POST_DATA_NULL 			= 90011;
	public static final int ERROR_PROTOCOL_BUFFER_PARSING   = 90012;
    public static final int ERROR_XIAOJI_NULL               = 90013;
    public static final int ERROR_XIAOJI_NUMBER_NULL        = 90014;
    public static final int ERROR_SEND_EMAIL_EXCEPTION      = 90015;
    public static final int ERROR_SEND_TYPE_NOT_SUPPORT     = 90016;
    public static final int ERROR_DATA_NULL                 = 90017;


	
	//Board error
	public static final int ERROR_BOARD_ERROR 				= 100001;
	public static final int ERROR_PARAMETER_BOARDID_EMPTY 	= 100002;
	public static final int ERROR_PARAMETER_BOARDID_NULL 	= 100003;

	//contest error
	public static final int ERROR_PARAMETER_CONTESTID_EMPTY         = 110001;
	public static final int ERROR_PARAMETER_CONTESTID_NULL 	        = 110002;
	public static final int ERROR_DELETE_CONTEST_OPUS 		        = 110003;
	public static final int ERROR_CONTEST_EXCEED_SUBMIT_OPUS        = 110004;
	public static final int ERROR_CONTEST_NOT_FOUND 		        = 110005;
    public static final int ERROR_CONTEST_REACH_MAX_FLOWER          = 110006;
    public static final int ERROR_CONTEST_EXCEED_THROW_FLOWER_DATE  = 110007;
    public static final int ERROR_CONTEST_OLD_CLIENT_THROW_FLOWER   = 110008;
    public static final int ERROR_CONTEST_NO_JUDGE                  = 110006;
    public static final int ERROR_CONTEST_USER_NOT_JUDGE            = 110007;
    public static final int ERROR_CONTEST_RANK_TYPE_NOT_FOUND       = 110008;
    public static final int ERROR_USER_COMMENT_OWN_OPUS_CONTEST     = 110009;
    public static final int ERROR_CONTEST_CREATE_FAIL               = 110010;
    public static final int ERROR_CONTEST_CANNOT_UPDATE_AFTER_START = 110011;




	
	//bbs error
	public static final int ERROR_BBS_POST_TYPE 			= 120001;
	public static final int ERROR_BBS_ACTION_TYPE 			= 120002;
	public static final int ERROR_PARAMETER_POSTID_NULL		= 120003;
	public static final int ERROR_PARAMETER_POSTID_EMPTY 	= 120004;
	public static final int ERROR_BBS_POST_NOT_EXIST	 	= 120005;

	public static final int ERROR_PARAMETER_BBSBOARDID_EMPTY= 120006;
	public static final int ERROR_PARAMETER_BBSBOARDID_NULL = 120007;
	public static final int ERROR_PARAMETER_BBSACTIONID_EMPTY = 120008;
	public static final int ERROR_PARAMETER_BBSACTIONID_NULL 	= 120009;
	public static final int ERROR_GET_ME_BBSACTION_LIST 	= 120010;
	public static final int ERROR_GET_BBS_DRAWDATA 			= 120011;
	public static final int ERROR_GET_BBSPOST_LIST 			= 120012;
	public static final int ERROR_PARAMETER_BBSACTIONUID_EMPTY = 120013;	
	public static final int ERROR_PARAMETER_BBSACTIONUID_NULL = 120014;
	public static final int ERROR_BBS_FORBIDUSER_ERROR              = 120015;
	public static final int ERROR_BBS_NO_PRIVILIGE                  = 120016;
	public static final int ERROR_PARAMETER_PERMISSION_NOT_ENOUGH   = 120017;
    public static final int ERROR_CREATE_BOARD_FAIL                 = 120018;
    public static final int ERROR_UPDATE_BOARD_FAIL                 = 120019;
    public static final int ERROR_DELETE_BOARD_FAIL                 = 120020;
    public static final int ERROR_USER_IS_BLACK_BOARD               = 120021;


    // wall error
	public static final int ERROR_CREATE_WALL 					= 160001;
	public static final int ERROR_WALL_NOT_FOUND 			= 160002;
	public static final int ERROR_UPDATE_WALL 					= 160003;


	// opus
	public static final int ERROR_ACTION_NOT_SUPPORT 	          = 170001;
	public static final int ERROR_OPUS_DRAW_DATA_ERROR            = 180001;
	public static final int ERROR_CREATE_OPUS 					  = 180002;

    // user guess opus
    public static final int ERROR_USER_GUESS_MODE                 = 190001;


    //group error code.

    public static final int ERROR_GROUP = 200000;
    public static final int ERROR_GROUP_DUPLICATE_NAME = 200001;
    public static final int ERROR_PARAMETER_GROUPID_EMPTY = 200002;
    public static final int ERROR_PARAMETER_GROUPID_NULL = 200003;
    public static final int ERROR_GROUP_MULTIJOINED = 200006;
    public static final int ERROR_GROUP_MULTIREQUESTED = 200007;
    public static final int ERROR_GROUP_PERMISSION = 200008;
    public static final int ERROR_GROUP_FULL = 200009;
    public static final int ERROR_GROUP_REJECTED = 200010;
    public static final int ERROR_GROUP_USER_NOT_REQUESTSTATUS = 200011;
    public static final int ERROR_GROUP_INVALIDATE_ROLE = 200012;
    public static final int ERROR_GROUP_MEMBER_UNFOLLOW = 200013;
    public static final int ERROR_GROUP_REPEAT_FOLLOW = 200014;
    public static final int ERROR_GROUP_NOTEXIST = 200015;
    public static final int ERROR_GROUP_LEVEL_SMALL = 200016;
    public static final int ERROR_PARAMETER_NOTICEID_EMPTY = 200017;
    public static final int ERROR_PARAMETER_NOTICEID_NULL = 200018;
    public static final int ERROR_GROUP_REQUEST_HANDLE_TYPE_INVALID = 200019;
    public static final int ERROR_GROUP_NOTICE_NOTFOUND = 200020;
    public static final int ERROR_GROUP_TITLEID_EXISTED    = 200021;
    public static final int ERROR_GROUP_NOT_MEMBER = 200022;
    public static final int ERROR_GROUP_NOT_ADMIN = 200023;
    public static final int ERROR_GROUP_NOT_INVITEE = 200024;
    public static final int ERROR_GROUP_TITLEID_NOTEXISTED  = 200025;
    public static final int ERROR_GROUP_INVITATION = 200026;
    public static final int ERROR_GROUP_DELETE_HAS_GUEST = 200027;
    public static final int ERROR_GROUP_DELETE_HAS_MEMBER = 200028;

    public static final int ERROR_GROUP_BALANCE_NOT_ENOUGH = 200052;

    // for tutorial
    public static final int ERROR_USER_TUTORIAL_NULL                = 210001;
    public static final int ERROR_USER_REMOTEID_NULL                = 210002;
    public static final int ERROR_UNKNOWN_USER_TUTORIAL_ACTION      = 210003;


    static public String getJSONByErrorCode(int errorCode){
		return String.format("{\"%s\":%d}", ServiceConstant.RET_CODE, errorCode);		
	}


}
