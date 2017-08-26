package com.sogouime.hackathon4.vknowa.entity;


/**
 * Created by gaoweiwen on 2017/8/26.
 */

public class LexerWords {
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

    public LexerWords(int weight, int tag, int ntag, String word){
        weight_ = weight;
        tag_ = tag;
        nTag_ = ntag;
        word_ = word;
    }

    public int GetWeight(){
        return  weight_;
    }
    public void SetWeight(int weight){
        weight_ = weight;
    }
    public int  GetTag(){
        return tag_;
    }
    public void SetTag(int tag ){
        tag_ = tag;
    }
    public int GetNTag(){
        return  nTag_;
    }
    public void SetNTag(int nTag){
        nTag_ = nTag;
    }
    public String GetWord(){
        return word_;
    }
    public void SetWord(String word){
        word_ = word;
    }
}
