package com.orange.game.model.manager.opusclass;

import com.orange.common.utils.StringUtil;
import com.orange.game.constants.DBConstants;
import com.orange.game.model.dao.UserAction;
import com.orange.game.model.manager.opus.contest.ContestTopOpusManager;
import org.apache.log4j.Logger;

import java.util.Collections;
import java.util.HashMap;
import java.util.List;
import java.util.concurrent.ConcurrentHashMap;

/**
 * Created with IntelliJ IDEA.
 * User: qqn_pipi
 * Date: 14-6-4
 * Time: 下午5:36
 * To change this template use File | Settings | File Templates.
 */
public class OpusClassService {

    public static final Logger log = Logger.getLogger(OpusClassService.class.getName());

    private static OpusClassService ourInstance = new OpusClassService();

    static final Object mapLock = new Object();

    static final ConcurrentHashMap<String, OpusClassHotTopManager> hotTopManagerMap = new ConcurrentHashMap<String, OpusClassHotTopManager>();
    static final ConcurrentHashMap<String, OpusClassAlltimeTopManager> alltimeTopManagerMap = new ConcurrentHashMap<String, OpusClassAlltimeTopManager>();
    static final ConcurrentHashMap<String, OpusClassFeatureManager> featureManagerMap = new ConcurrentHashMap<String, OpusClassFeatureManager>();
    static final ConcurrentHashMap<String, OpusClassLatestManager> latestManagerMap = new ConcurrentHashMap<String, OpusClassLatestManager>();

    public static OpusClassService getInstance() {
        return ourInstance;
    }

    private OpusClassService() {
    }

    private OpusClassHotTopManager hotTopManager(String className) {

        if (StringUtil.isEmpty(className)){
            return null;
        }

        synchronized (mapLock) {

            ConcurrentHashMap<String, OpusClassHotTopManager> map = hotTopManagerMap;

            if (map.containsKey(className)){
                return map.get(className);
            }
            else{
                OpusClassHotTopManager manager = new OpusClassHotTopManager(className);
                map.putIfAbsent(className, manager);
                return map.get(className);
            }
        }
    }

    private OpusClassAlltimeTopManager alltimeTopManager(String className) {

        if (StringUtil.isEmpty(className)){
            return null;
        }

        synchronized (mapLock) {

            ConcurrentHashMap<String, OpusClassAlltimeTopManager> map = alltimeTopManagerMap;

            if (map.containsKey(className)){
                return map.get(className);
            }
            else{
                OpusClassAlltimeTopManager manager = new OpusClassAlltimeTopManager(className);
                map.putIfAbsent(className, manager);
                return map.get(className);
            }
        }
    }

    private OpusClassLatestManager latestManager(String className) {

        if (StringUtil.isEmpty(className)){
            return null;
        }

        synchronized (mapLock) {

            ConcurrentHashMap<String, OpusClassLatestManager> map = latestManagerMap;

            if (map.containsKey(className)){
                return map.get(className);
            }
            else{
                OpusClassLatestManager manager = new OpusClassLatestManager(className);
                map.putIfAbsent(className, manager);
                return map.get(className);
            }
        }
    }

    private OpusClassFeatureManager featureManager(String className) {

        if (StringUtil.isEmpty(className)){
            return null;
        }

        synchronized (mapLock) {

            ConcurrentHashMap<String, OpusClassFeatureManager> map = featureManagerMap;

            if (map.containsKey(className)){
                return map.get(className);
            }
            else{
                OpusClassFeatureManager manager = new OpusClassFeatureManager(className);
                map.putIfAbsent(className, manager);
                return map.get(className);
            }
        }
    }

    public void addOpusClass(UserAction opus, List<String> classList){

        if (opus == null || classList == null || classList.size() == 0){
            return;
        }

        boolean condition1 = (opus.getFlowerTimes() >= DBConstants.C_MIN_OPUS_CLASS_FLOWERS_0);
        boolean condition2 = (opus.getStrokes() >= DBConstants.C_MIN_OPUS_CLASS_STROKES_1 && opus.getFlowerTimes() >= DBConstants.C_MIN_OPUS_CLASS_FLOWERS_1);
        boolean condition3 = (opus.getFlowerTimes() >= DBConstants.C_MIN_OPUS_CLASS_FLOWERS_2 && opus.getStrokes() >= DBConstants.C_MIN_OPUS_CLASS_STROKES_2);
        boolean condition4 = (opus.isFeature());

        if (!condition1 && !condition2 && !condition3 && !condition4){
            log.info("<addOpusClass> but opus("+opus.getActionId()+") flower times("+opus.getFlowerTimes()+
                    ") and strokes("+opus.getStrokes()+") doesn't meet requirement");
            return;
        }

        String opusId = opus.getActionId();
        for (String className : classList){
            hotTopManager(className).updateOpusScore(opusId, opus.getHot());
            alltimeTopManager(className).updateOpusScore(opusId, opus.getHistoryScore());
            latestManager(className).addOpus(opusId, opus.getCreateDate().getTime());
        }

    }

    public void removeOpusClass(String opusId, List<String> classList){
        if (opusId == null || classList == null || classList.size() == 0){
            return;
        }

        for (String className : classList){
            hotTopManager(className).deleteIndex(opusId, true);
            alltimeTopManager(className).deleteIndex(opusId, true);
            latestManager(className).removeOpus(opusId);
            featureManager(className).deleteIndex(opusId, true);
        }
    }

    public List<UserAction> getOpusHotTopList(String className, int offset, int limit){

        OpusClassHotTopManager manager = hotTopManager(className);
        if (manager == null){
            return Collections.emptyList();
        }

        return manager.getTopList(offset, limit);
    }

    public List<UserAction> getOpusAlltimeTopList(String className, int offset, int limit){
        OpusClassAlltimeTopManager manager = alltimeTopManager(className);
        if (manager == null){
            return Collections.emptyList();
        }

        return manager.getTopList(offset, limit);
    }

    public List<UserAction> getOpusFeatureList(String className, int offset, int limit){
        OpusClassFeatureManager manager = featureManager(className);
        if (manager == null){
            return Collections.emptyList();
        }

        return manager.getTopList(offset, limit);

    }

    public List<UserAction> getOpusLatestList(String className, int offset, int limit){
        OpusClassLatestManager manager = latestManager(className);
        if (manager == null){
            return Collections.emptyList();
        }

        return manager.getTopList(offset, limit);
    }

    public void updateOpusClassScore(UserAction action) {

        addOpusClass(action, action.getClassList());
    }

    public void clearOpusClass(UserAction opus) {

        List<String> classList = opus.getClassList();
        if (opus == null || classList == null || classList.size() == 0){
            return;
        }

        String opusId = opus.getActionId();
        for (String className : classList){
            hotTopManager(className).deleteIndex(opusId, true);
            alltimeTopManager(className).deleteIndex(opusId, true);
            latestManager(className).deleteIndex(opusId, true);
        }
    }
}
