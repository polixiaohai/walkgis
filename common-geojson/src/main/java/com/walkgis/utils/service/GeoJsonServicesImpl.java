package com.walkgis.utils.service;

import com.walkgis.utils.common.GeometryConvert;
import com.walkgis.utils.entity.GeoEntity;
import org.apache.commons.beanutils.BeanUtils;
import org.geojson.Feature;
import org.geojson.FeatureCollection;
import org.geojson.GeoJsonObject;
import org.joor.Reflect;
import org.locationtech.jts.geom.Geometry;
import org.locationtech.jts.io.ParseException;
import org.locationtech.jts.io.WKBReader;
import org.locationtech.jts.io.WKBWriter;
import org.postgresql.util.PGobject;

import java.io.Serializable;
import java.lang.reflect.InvocationTargetException;
import java.lang.reflect.Method;
import java.util.Arrays;
import java.util.Collection;
import java.util.List;
import java.util.stream.Collectors;

/**
 * @author JerFer
 * @date 2019/8/7---21:38
 */
public class GeoJsonServicesImpl<T extends GeoEntity<ID>, ID extends Serializable> implements GeoJsonServices<T> {
    public final GeometryConvert geometryConvert = new GeometryConvert();
    public static final ThreadLocal<WKBReader> threadLocalWkbReader = new ThreadLocal();
    public static final ThreadLocal<WKBWriter> threadLocalWKBWriter = new ThreadLocal();

    public GeoJsonServicesImpl() {
    }

    public Feature toFeature(T t) {
        threadLocalWkbReader.set(new WKBReader());
        Feature feature = new Feature();
        GeoJsonObject geometry = null;
        if (t.getShape() instanceof PGobject) {
            PGobject pGeobject = (PGobject)t.getShape();

            try {
                geometry = this.geometryConvert.geometrySerializer(((WKBReader)threadLocalWkbReader.get()).read(WKBReader.hexToBytes(pGeobject.getValue())));
            } catch (ParseException var6) {
                var6.printStackTrace();
            } catch (Exception var7) {
                var7.printStackTrace();
            }
        }

        feature.setId(String.valueOf(t.getId()));
        if (geometry != null) {
            feature.setGeometry(geometry);
        }
        Arrays.stream(t.getClass().getFields()).forEach(field -> {
            if (!field.getName().equalsIgnoreCase("shape")) {
                try {
                    Method method = t.getClass().getMethod("getShape");
                    feature.setProperty(field.getName(), method.invoke(t));
                } catch (NoSuchMethodException e) {
                    e.printStackTrace();
                } catch (IllegalAccessException e) {
                    e.printStackTrace();
                } catch (InvocationTargetException e) {
                    e.printStackTrace();
                }
            }
        });


        Reflect.on(t).fields().forEach((k, v) -> {
            if (!k.equalsIgnoreCase("shape")) {
                feature.setProperty(k, v.get());
            }
        });
        feature.setId(String.valueOf(t.getId()));
        return feature;
    }

    public synchronized void toEntity(Feature feature, T t) {
        try {
            threadLocalWKBWriter.set(new WKBWriter(2, true));
            PGobject pGeobject = new PGobject();
            Geometry geometry = this.geometryConvert.geometryDeserialize(feature.getGeometry());
            geometry.setSRID(4326);
            pGeobject.setValue(WKBWriter.toHex(((WKBWriter)threadLocalWKBWriter.get()).write(geometry)));
            pGeobject.setType("geometry");
            t.setShape(pGeobject);
            BeanUtils.populate(t, feature.getProperties());
        } catch (Exception var5) {
            var5.printStackTrace();
        }

    }

    public FeatureCollection toFeatures(List<T> entities) {
        FeatureCollection collection = new FeatureCollection();
        collection.addAll((Collection)entities.stream().map((a) -> {
            return this.toFeature(a);
        }).filter((a) -> {
            return a != null;
        }).collect(Collectors.toList()));
        return collection;
    }

    public List<T> toEntities(FeatureCollection features, Class t) {
        return (List)features.getFeatures().stream().map((a) -> {
            try {
                T entity = (T) t.newInstance();
                this.toEntity(a, entity);
                return entity;
            } catch (IllegalAccessException var4) {
                var4.printStackTrace();
            } catch (InstantiationException var5) {
                var5.printStackTrace();
            }

            return null;
        }).filter((a) -> {
            return a != null;
        }).collect(Collectors.toList());
    }
}
