package com.orange.game.model.utiils;

import com.orange.common.utils.StringUtil;
import com.orange.game.constants.DBConstants;
import com.orange.game.constants.ErrorCode;
import com.orange.game.constants.ServiceConstant;
import com.orange.game.model.dao.Item;
import com.orange.game.model.dao.User;
import com.orange.game.model.dao.group.Group;
import com.orange.network.game.protocol.message.GameMessageProtos;
import com.orange.network.game.protocol.model.GameBasicProtos;
import org.apache.log4j.Logger;

import java.util.List;

/**
 * Created with IntelliJ IDEA.
 * User: qqn_pipi
 * Date: 14-2-10
 * Time: 下午5:53
 * To change this template use File | Settings | File Templates.
 */
public class DataUtils {

    static Logger log = Logger.getLogger("DataUtils");

    public static byte[] protocolBufferErrorCode(int errorCode) {
        GameMessageProtos.DataQueryResponse response = GameMessageProtos.DataQueryResponse
                .newBuilder().setResultCode(errorCode).build();
        return response.toByteArray();
    }

    private static void setItemIntoUserBuilder(GameBasicProtos.PBGameUser.Builder builder, User user){
        List<Item> list = user.getItems();
        for (Item item : list) {

            GameBasicProtos.PBUserItem.Builder itemBuilder = GameBasicProtos.PBUserItem.newBuilder();

            itemBuilder.setItemId(item.getItemType());
            itemBuilder.setCount(item.getAmount());
            if (item.getExpireDate() > 0){
                itemBuilder.setExpireDate(itemBuilder.getExpireDate());
            }

            builder.addItems(itemBuilder.build());
        }

    }

    public static byte[] userAccountAndItemToPB(User user, String appId) {

        if (user == null) {
            return protocolBufferErrorCode(ErrorCode.ERROR_USERID_NOT_FOUND);
        }

        GameBasicProtos.PBGameUser.Builder builder = GameBasicProtos.PBGameUser.newBuilder();
        builder.setUserId(user.getUserId());
        builder.setNickName("");
        builder.setIngotBalance(user.getIngotBalance());
        builder.setCoinBalance(user.getBalance());
        builder.setLevel(user.getLevelByAppId(appId));
        builder.setExperience(user.getExpByAppId(appId));

        setItemIntoUserBuilder(builder, user);

        GameMessageProtos.DataQueryResponse response = GameMessageProtos.DataQueryResponse
                .newBuilder().setResultCode(ErrorCode.ERROR_SUCCESS)
                .setUser(builder.build()).build();

        log.debug("<userAccountAndItemToPB> response="+response.toString());

        return response.toByteArray();
    }

    public static byte[] userToPB(User user, String appId) {

        if (user == null){
            return protocolBufferErrorCode(ErrorCode.ERROR_USERID_NOT_FOUND);
        }

        try {
            GameBasicProtos.PBGameUser.Builder builder = GameBasicProtos.PBGameUser.newBuilder();
            builder.setUserId(user.getUserId());

            String nickName = user.getNickName();
            if (nickName != null)
                builder.setNickName(nickName);
            else {
                builder.setNickName("");
            }

            String avatar = user.getAvatar();
            if (avatar != null)
                builder.setAvatar(avatar);

            boolean gender = user.getBoolGender();
            builder.setGender(gender);

            String location = user.getLocation();
            if (location != null)
                builder.setLocation(location);

            int level = user.getLevelByAppId(appId);
            builder.setLevel(level);
            builder.setUserLevel(level);

            long exp = user.getExpByAppId(appId);
            builder.setExperience(exp);

            String email = user.getEmail();
            if (email != null)
                builder.setEmail(email);

            String password = user.getPassword();
            if (password != null)
                builder.setPassword(password);

            builder.setCoinBalance(user.getBalance());
            builder.setIngotBalance(user.getIngotBalance());
            builder.setFeatureOpus(user.getFeatureOpus());
            builder.setTakeCoins(user.getTakeCoins());

            // add login credentials
            for (int i= DBConstants.LOGINID_START; i<=DBConstants.LOGINID_END; i++){
                String key = User.getSNSCredentialKey(i);
                if (key != null){
                    String credential = (String)user.getDbObject().get(key);
                    if (credential != null){
                        GameBasicProtos.PBSNSUserCredential.Builder snsCredentialBuilder = GameBasicProtos.PBSNSUserCredential.newBuilder();
                        snsCredentialBuilder.setType(i);
                        snsCredentialBuilder.setCredential(credential);
                        builder.addSnsCredentials(snsCredentialBuilder.build());
                    }
                }
            }

            if (user.getBlockDeviceIds() != null){
                builder.addAllBlockDeviceIds(user.getBlockDeviceIds());
            }

            // sns user - SINA
            if (!StringUtil.isEmpty(user.getSinaID()) && !StringUtil.isEmpty(user.getSinaNickName())){
                String snsId = user.getSinaID();
                String snsNick = user.getSinaNickName();
                String accessToken = user.getSinaAccessToken();
                String refreshToken = user.getSinaRefreshToken();
                int expireTime = user.getSinaExpireDate();

                GameBasicProtos.PBSNSUser.Builder snsBuilder = GameBasicProtos.PBSNSUser.newBuilder();
                snsBuilder.setType(ServiceConstant.REGISTER_TYPE_SINA);
                snsBuilder.setUserId(snsId);
                snsBuilder.setNickName(snsNick);

                if (accessToken != null){
                    snsBuilder.setAccessToken(accessToken);
                }

                if (refreshToken != null){
                    snsBuilder.setRefreshToken(refreshToken);
                }

                snsBuilder.setExpireTime(expireTime);
                builder.addSnsUsers(snsBuilder.build());
            }

            // sns user - QQ
            if (!StringUtil.isEmpty(user.getQQID()) && !StringUtil.isEmpty(user.getQQNickName())){
                String snsId = user.getQQID();
                String snsNick = user.getQQNickName();
                String accessToken = user.getQQAccessToken();
                String refreshToken = user.getQQRefreshToken();
                int expireTime = user.getQQExpireDate();
                String qqOpenId = user.getQQOpenId();

                GameBasicProtos.PBSNSUser.Builder snsBuilder = GameBasicProtos.PBSNSUser.newBuilder();
                snsBuilder.setType(ServiceConstant.REGISTER_TYPE_QQ);
                snsBuilder.setUserId(snsId);
                snsBuilder.setNickName(snsNick);

                if (accessToken != null){
                    snsBuilder.setAccessToken(accessToken);
                }

                if (refreshToken != null){
                    snsBuilder.setRefreshToken(refreshToken);
                }

                if (qqOpenId != null){
                    snsBuilder.setQqOpenId(qqOpenId);
                }

                snsBuilder.setExpireTime(expireTime);
                builder.addSnsUsers(snsBuilder.build());
            }

            if (!StringUtil.isEmpty(user.getFacebookId())){
                String snsId = user.getFacebookId();
                String snsNick = user.getFacebookNickName();
                String accessToken = user.getFacebookAccessToken();
                String refreshToken = user.getFacebookRefreshToken();
                int expireTime = user.getFacebookExpireDate();

                GameBasicProtos.PBSNSUser.Builder snsBuilder = GameBasicProtos.PBSNSUser.newBuilder();
                snsBuilder.setType(ServiceConstant.REGISTER_TYPE_FACEBOOK);
                snsBuilder.setUserId(snsId);
                snsBuilder.setNickName(snsNick);

                if (accessToken != null){
                    snsBuilder.setAccessToken(accessToken);
                }

                if (refreshToken != null){
                    snsBuilder.setRefreshToken(refreshToken);
                }

                snsBuilder.setExpireTime(expireTime);
                builder.addSnsUsers(snsBuilder.build());
            }

            // user item
            setItemIntoUserBuilder(builder, user);

            String birthday = user.getBirthday();
            if (birthday != null)
                builder.setBirthday(birthday);

            builder.setZodiac(user.getZodiac());
            builder.setGuessWordLanguage(user.getGuessWordLanguage());

            String deviceToken = user.getDeviceToken();
            if (deviceToken != null)
                builder.setDeviceToken(deviceToken);

            String backgroundURL = user.getBackgroundRemoteURL();
            if (backgroundURL != null)
                builder.setBackgroundURL(backgroundURL);

            String signature = user.getSignature();
            if (signature != null)
                builder.setSignature(signature);

            String blood = user.getBlood();
            if (blood != null)
                builder.setBloodGroup(blood);

            int singRecordLimit = user.getSingRecordLimit();
            if (singRecordLimit != 0){
                builder.setSingRecordLimit(singRecordLimit);
            }

            builder.setEmailVerifyStatus(user.getEmailVerifyStatus());

            String xiaojiNumber = user.getXiaojiNumber();
            if (xiaojiNumber != null)
                builder.setXiaojiNumber(xiaojiNumber);

            builder.setCanShakeNumber(user.getCanShakeNumber());
            builder.setShakeNumberTimes(user.getShakeNumberTimes());
            builder.setEmailVerifyStatus(user.getEmailVerifyStatus());

            GameBasicProtos.PBOpenInfoType openInfoType = user.getOpenInfoType();
            builder.setOpenInfoType(openInfoType);

            builder.setFanCount(user.getFanCount());
            builder.setFollowCount(user.getFollowCount());

            //set group
            Group group = user.getGroup();
            if (group != null) {
                GameBasicProtos.PBSimpleGroup.Builder groupBuilder = GameBasicProtos.PBSimpleGroup.newBuilder();
                groupBuilder.setGroupId(group.getGroupId());
                groupBuilder.setGroupName(group.getName());
                if (group.getMedalImage() != null){
                    groupBuilder.setGroupMedal(group.getMedalImage());
                }else{
                    groupBuilder.setGroupMedal("");
                }
                builder.setGroupInfo(groupBuilder.build());
            }

            builder.setVip(user.getVip());
            if (user.getVipExpireDate() != null){
                builder.setVipExpireDate((int)(user.getVipExpireDate().getTime()/1000));
            }
            if (user.getVipLastPayDate() != null){
                builder.setVipLastPayDate((int)(user.getVipLastPayDate().getTime()/1000));
            }

            List<String> awardAppIdList = user.getAwardAppIdList();
            if (awardAppIdList != null && awardAppIdList.size() > 0){
                builder.addAllAwardApps(awardAppIdList);
            }

            // off groups
            List<String> offGroups = user.getOffGroups();
            if (offGroups.size() > 0){
                builder.addAllOffGroupIds(offGroups);
            }

            GameBasicProtos.PBGameUser pbUser = builder.build();

            GameMessageProtos.DataQueryResponse response = GameMessageProtos.DataQueryResponse
                    .newBuilder().setResultCode(ErrorCode.ERROR_SUCCESS)
                    .setUser(pbUser)
                    .setUserRelation(user.getRelation())
                    .build();

            log.debug("<userToPB> user="+response.toString());
            return response.toByteArray();

        } catch (Exception e) {
            log.error("<userToPB> catch exception"+e.toString(), e);
            return protocolBufferErrorCode(ErrorCode.ERROR_PROTOCOL_BUFFER_NULL);
        }
    }
}
