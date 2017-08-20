package com.sogouime.hackathon4.vknowa.service.impl;

/**
 * Created by zhusong on 2017-08-20.
 */
import com.sogouime.hackathon4.vknowa.entity.CacheObject;
import com.sogouime.hackathon4.vknowa.service.CacheFullRemoveType;

public class RemoveTypeNotRemove<T> implements CacheFullRemoveType<T> {

    private static final long serialVersionUID = 1L;

    @Override
    public int compare(CacheObject<T> obj1, CacheObject<T> obj2) {
        return 0;
    }
}
