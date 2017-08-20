package com.sogouime.hackathon4.vknowa.service.impl;

/**
 * Created by zhusong on 2017-08-20.
 */
import com.sogouime.hackathon4.vknowa.service.FileNameRule;
import com.sogouime.hackathon4.vknowa.util.FileUtils;
import com.sogouime.hackathon4.vknowa.util.StringUtils;

public class FileNameRuleImageUrl implements FileNameRule {

    private static final long  serialVersionUID     = 1L;

    /** default file name if image url is empty **/
    public static final String DEFAULT_FILE_NAME    = "ImageSDCardCacheFile.jpg";
    /** max length of file name, not include suffix **/
    public static final int    MAX_FILE_NAME_LENGTH = 127;

    private String             fileExtension        = null;

    @Override
    public String getFileName(String imageUrl) {
        if (StringUtils.isEmpty(imageUrl)) {
            return DEFAULT_FILE_NAME;
        }

        String ext = (fileExtension == null ? FileUtils.getFileExtension(imageUrl) : fileExtension);
        String fileName = (imageUrl.length() > MAX_FILE_NAME_LENGTH ? imageUrl.substring(imageUrl.length()
                - MAX_FILE_NAME_LENGTH, imageUrl.length()) : imageUrl).replaceAll("[\\W]", "_");
        return StringUtils.isEmpty(ext) ? fileName : (new StringBuilder().append(fileName).append(".")
                .append(ext.replaceAll("[\\W]", "_")).toString());
    }

    public FileNameRuleImageUrl setFileExtension(String fileExtension) {
        this.fileExtension = fileExtension;
        return this;
    }
}

