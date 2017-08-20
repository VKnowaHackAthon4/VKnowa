package com.sogouime.hackathon4.vknowa.service;

/**
 * Created by zhusong on 2017-08-20.
 */
import java.io.Serializable;
import com.sogouime.hackathon4.vknowa.entity.CacheObject;

public interface CacheFullRemoveType<V> extends Serializable {

    /**
     * compare object <br/>
     * <ul>
     * <strong>About result</strong>
     * <li>if obj1 > obj2, return 1</li>
     * <li>if obj1 = obj2, return 0</li>
     * <li>if obj1 < obj2, return -1</li>
     * </ul>
     *
     * @param obj1
     * @param obj2
     * @return
     */
    public int compare(CacheObject<V> obj1, CacheObject<V> obj2);
}
