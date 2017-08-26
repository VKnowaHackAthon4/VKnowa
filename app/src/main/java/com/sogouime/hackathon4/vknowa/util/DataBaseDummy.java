package com.sogouime.hackathon4.vknowa.util;

/**
 * Created by liugao on 2017/8/26.
 */
import com.sogouime.hackathon4.vknowa.entity.LexerWords;
import com.sogouime.hackathon4.vknowa.middle.Controller;

import java.util.Collection;
import java.util.Collections;
import java.util.HashMap;
import java.util.HashSet;
import java.util.ArrayList;
import java.util.Iterator;
import java.util.List;
import java.util.Map;

public class DataBaseDummy {

/*    public static ArrayList<Integer> Search(String word, int nTag)
    {
        return null;
    }

    public static String GetOriginFilePathByIndex(int index)
    {
        return null;
    }

    public static boolean InsertItem(String parsedText, String filePath, String name, HashSet<LexerWords_lg> lexerWords)
    {
        return true;
    }*/

    public static List<Integer> SearchMuchInList(List<Integer> p_ListInt)
    {
        if ( p_ListInt.isEmpty() )
        {
            return null;
        }
        List<Integer> retInteger = new ArrayList<Integer>();
        HashMap<Integer, Integer> map = new HashMap<Integer, Integer>();
        for ( int i = 0; i < p_ListInt.size(); i++ )
        {
            if ( map.containsKey(p_ListInt.get(i)))
            {
                int formerValue = map.get(p_ListInt.get(i));
                map.put(p_ListInt.get(i), formerValue + 1);
            }
            else
            {
                map.put(p_ListInt.get(i), 1);
            }
        }

        Collection<Integer> count = map.values();
        int maxCount = Collections.max(count);
        int maxNumber = 0;
        for ( Map.Entry<Integer, Integer>entry:map.entrySet() )
        {
            if (entry.getValue() == maxCount )
            {
                retInteger.add(entry.getKey());
            }
        }
        return retInteger;
    }

    // 查找与询问最匹配的录音文件
    public static String QueryAnswer(HashSet<LexerWords> lexerWords)
    {
        List<Integer> listStart = new ArrayList<Integer>();
        for ( Iterator<LexerWords> iter = lexerWords.iterator(); iter.hasNext(); )
        {
            LexerWords element = iter.next();
            List<Integer> listTemp = SqliteUtils.getInstance(Controller.getApplicationContext()).Search(element.GetWord(), element.GetNTag());
            if ( !listTemp.isEmpty() )
            {
                listStart.addAll(listTemp);
            }
        }
        if ( listStart.isEmpty() )
        {
            return null;
        }
        List<Integer> maxList = SearchMuchInList(listStart);

        // 先取一个进行显示
        if ( maxList.isEmpty() )
        {
            return null;
        }
        String voiceOut = SqliteUtils.getInstance(Controller.getApplicationContext()).GetOriginFilePathByIndex(maxList.get(0));
        return voiceOut;
    }
}
