package com.walkgis.utils.service;

import org.geojson.Feature;

/**
 * @author JerFer
 * @date 2019/8/7---21:37
 */
public interface GeoJsonServices<T> {
    Feature toFeature(T var1);

    void toEntity(Feature var1, T var2);
}

