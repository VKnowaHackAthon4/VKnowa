package com.sogouime.hackathon4.vknowa.model;

import java.util.ArrayList;
import java.util.HashMap;
import java.util.Iterator;
import java.util.List;
import java.util.Map;

/**
 * Created by huaweidong on 17-8-26.
 */

public class TtidfModel {

    public HashMap<String,HashMap<String, Float>> m_all_tf;     // 每个文件中每个单词的频率
    public HashMap<String, Float> m_idf;    // 全局每个单词的频率
    public HashMap<String, Integer> m_idn;  // 全局每个文件的个数
    public Integer m_totalWordNums;         // 总词数
    public Integer m_totalFileNums;         // 总文件数
    public String m_dbName;
    public String m_tbTFName;
    public String m_tbIDFName;

    public void CreateAllTf()
    {
        m_all_tf = new HashMap<String, HashMap<String, Float>>();
    }

    public void CreateIdf()
    {
        m_idf = new HashMap<String, Float>();
        m_idn = new HashMap<String, Integer>();
    }

    public boolean LoadFromDB(String dbName)
    {
        m_dbName = dbName;
        CreateAllTf();
        CreateIdf();
        return true;
    }

    public boolean SaveToDB(String dbName)
    {

        return true;
    }

    public String queryHighestMatchFile(ArrayList<String> wordsInQuery)
    {
        Iterator iter = m_all_tf.entrySet().iterator();
        HashMap<String, Float> result = new HashMap<String, Float>();
        while(iter.hasNext()) {
            Map.Entry entry = (Map.Entry) iter.next();
            String filename = entry.getKey().toString();
            HashMap<String, Float> tf = m_all_tf.get(filename);
            Float sumFre = 0.0f;
            for (String word : wordsInQuery) {
                Float fre = 0.0f;
                if(tf.get(word) != null && m_idf.get(word) != null)
                {
                    fre = tf.get(word) * m_idf.get(word);
                }
                sumFre += fre;
            }
            result.put(filename, sumFre);
        }

        Float maxFre = 0.0f;
        String resFileName = "";
        Iterator iterRes = result.entrySet().iterator();
        while(iterRes.hasNext()){
            Map.Entry entry = (Map.Entry)iter.next();
            Float fre = Float.parseFloat(entry.getValue().toString());
            if( maxFre < fre ) {
                maxFre = fre;
                resFileName = entry.getKey().toString();
            }
        }
        return resFileName;
    }

    public boolean insertNewMemo(String filename, String rawText, ArrayList<String> words)
    {
        int m_curWordNums = words.size();
        m_totalWordNums += m_curWordNums;
        m_totalFileNums += 1;

        // 计算当前文件的TF
        HashMap<String, Float> resTF = new HashMap<String, Float>();
        HashMap<String, Integer> intTF = new HashMap<String, Integer>();
        for(String word : words){
            if(intTF.get(word) == null){
                intTF.put(word, 1);
            }
            else{
                intTF.put(word, intTF.get(word) + 1);
            }
            if(m_idn.get(word) == null) {
                m_idn.put(word, 1);
            }
            else{
                m_idn.put(word, m_idn.get(word) + 1);
            }
        }
        Iterator iter = intTF.entrySet().iterator(); //iterator for that get from TF
        while(iter.hasNext()){
            Map.Entry entry = (Map.Entry)iter.next();
            resTF.put(entry.getKey().toString(), Float.parseFloat(entry.getValue().toString()) / m_curWordNums);
        }
        m_all_tf.put(filename, resTF);

        // 计算全局的IDF
        Iterator iter_dict = m_idn.entrySet().iterator();
        while(iter_dict.hasNext()){
            Map.Entry entry = (Map.Entry)iter_dict.next();
            float value = (float)Math.log(m_totalFileNums / Float.parseFloat(entry.getValue().toString()));
            m_idf.put(entry.getKey().toString(), value);
        }

        return true;
    }


}
