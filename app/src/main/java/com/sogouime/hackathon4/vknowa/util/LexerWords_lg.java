package com.sogouime.hackathon4.vknowa.util;

/**
 * Created by liugao on 2017/8/26.
 */

public class LexerWords_lg {

    public static final int NOUN = 1;   //名词
    public static final int PRONOUN = 2;  //代词
    public static final int ADJ = 4;      //形容词
    public static final int ADV = 8;      //副词
    public static final int VERB = 16;     //动词
    public static final int NUM = 32;     //数词
    public static final int AUX = 64;      //
    public static final int CLAS = 128;    //

    private  int weight_;
    private  int tag_;
    private  int nTag_;
    String   word_;

    public void SetWeight(int weight){
        weight_ = weight;
    }
    public void SetTag(int tag ){
        tag_ = tag;
    }
    public void SetNTag(int nTag){
        nTag_ = nTag;
    }
    public void SetNTag(String p_strNTag)
    {
        if ( p_strNTag.equals("n"))
        {
            nTag_ = 1;
        }
        else if ( p_strNTag.equals("pron"))
        {
            nTag_ = 2;
        }
        else if ( p_strNTag.equals("adj"))
        {
            nTag_ = 4;
        }
        else if ( p_strNTag.equals("adv"))
        {
            nTag_ = 8;
        }
        else if ( p_strNTag.equals("num"))
        {
            nTag_ = 32;
        }
        else if ( p_strNTag.equals("aux"))
        {
            nTag_ = 64;
        }
        else
        {
            nTag_ = 128;
        }
    }

    public int GetWeight(){
        return  weight_;
    }

    public int  GetTag(){
        return tag_;
    }

    public int GetNTag(){
        return  nTag_;
    }

    public String GetWord(){
        return word_;
    }
    public void SetWord(String word){
        word_ = word;
    }
}
