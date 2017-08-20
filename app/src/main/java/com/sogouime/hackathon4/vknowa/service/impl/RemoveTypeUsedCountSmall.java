package com.sogouime.hackathon4.vknowa.service.impl;

/**
 * Created by zhusong on 2017-08-20.
 */
import com.sogouime.hackathon4.vknowa.entity.CacheObject;
import com.sogouime.hackathon4.vknowa.service.CacheFullRemoveType;

public class RemoveTypeUsedCountSmall<T> implements CacheFullRemoveType<T> {

    private static final long serialVersionUID = 1L;

    @Override
    public int compare(CacheObject<T> obj1, CacheObject<T> obj2) {
        return (obj1.getUsedCount() > obj2.getUsedCount()) ? 1
                : ((obj1.getUsedCount() == obj2.getUsedCount()) ? 0 : -1);
    }
}
