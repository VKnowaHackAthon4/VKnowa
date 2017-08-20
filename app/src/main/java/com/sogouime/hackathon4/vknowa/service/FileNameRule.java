package com.sogouime.hackathon4.vknowa.service;

/**
 * Created by zhusong on 2017-08-20.
 */
import java.io.Serializable;
import com.sogouime.hackathon4.vknowa.service.impl.ImageSDCardCache;

public interface FileNameRule extends Serializable {

    /**
     * get file name, include suffix, it's optional to include folder.
     *
     * @param imageUrl the url of image
     * @return
     */
    public String getFileName(String imageUrl);
}
