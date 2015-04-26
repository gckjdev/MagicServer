package com.orange.game.model.manager.word;

import java.io.BufferedReader;
import java.io.FileNotFoundException;
import java.io.FileReader;
import java.io.IOException;
import java.util.ArrayList;
import java.util.HashSet;
import java.util.List;
import java.util.Set;

import com.orange.common.utils.PropertyUtil;
import com.orange.common.utils.StringUtil;
import org.apache.log4j.Logger;

import com.orange.common.utils.RandomUtil;

public class WordManager {
	
	public static final Logger log = Logger.getLogger(WordManager.class.getName()); 
	
	// thread-safe singleton implementation
    private static WordManager manager = new WordManager();     
    private WordManager(){
    	super();
        String filePath = PropertyUtil.getStringProperty("word.chinese", "/data/web/app_res/smart_data/words.txt");
    	initWordData(filePath, chineseWordList);

        SKIP_WORDS.add("画");
        SKIP_WORDS.add("少女");
        SKIP_WORDS.add("女孩");
        SKIP_WORDS.add("眼睛");
        SKIP_WORDS.add("教程");
	}

    public static WordManager getInstance() {
    	return manager; 
    }
    
    public final Set<String> chineseWordList = new HashSet<String>();
    public final List<String> englishWordList = new ArrayList<String>();

    public void initWordData(String filename, Set<String> list) {
		String read;
		FileReader fileread;
		try {
			fileread = new FileReader(filename);
			BufferedReader bufread = new BufferedReader(fileread);
			try {

				while ((read = bufread.readLine()) != null) {
//					String[] reads = read.split(" ");
					if (read != null && read.length() > 0){
						list.add(read);
					}
				}
			} catch (IOException e) {
				log.error("<initWordData> but catch exception="+e.toString(), e);
			}
		} 
		catch (FileNotFoundException e) {
			log.error("<initWordData> but catch exception="+e.toString(), e);
		}
		catch (Exception e){
			log.error("<initWordData> but catch exception="+e.toString(), e);			
		}

	}

    public boolean isValidWord(String word){
        if (StringUtil.isEmpty(word)){
            return false;
        }

        if (isInSkipWords(word)){
            return false;
        }

        return chineseWordList.contains(word);
    }

    public final HashSet<String> SKIP_WORDS = new HashSet<String>();

    private boolean isInSkipWords(String word) {
        if (SKIP_WORDS.contains(word)){
            return true;
        }
        else{
            return false;
        }
    }

//    public String randomGetWord(int language, int wordLen, boolean isMatchWordLen){
//    	List<String> wordList = null;
//        wordList = chineseWordList;
//    	return randomGetWord(wordList, wordLen, isMatchWordLen);
//    }
//
//	private String randomGetWord(List<String> wordList, int wordLen, boolean isMatchWordLen) {
//		int size = wordList.size();
//		int index = RandomUtil.random(size);
//		String retString = null;
//
//		for (int i=index; i<size; i++){
//			if (isMatchWordLen){
//				retString = wordList.get(i);
//				if (wordLen == retString.length()){
//					return retString;
//				}
//			}
//			else{
//				return wordList.get(i);
//			}
//		}
//
//		for (int j=index; j>=0; j--){
//			if (isMatchWordLen){
//				retString = wordList.get(j);
//				if (wordLen == retString.length()){
//					return retString;
//				}
//			}
//			else{
//				return wordList.get(j);
//			}
//		}
//
//		return null;
//	}
}
