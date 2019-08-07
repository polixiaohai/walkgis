package com.walkgis.utils.entity;

/**
 * @author JerFer
 * @date 2019/8/7---21:37
 */

public interface GeoEntity<ID> {
    ID getId();

    Object getShape();

    void setShape(Object var1);
}

