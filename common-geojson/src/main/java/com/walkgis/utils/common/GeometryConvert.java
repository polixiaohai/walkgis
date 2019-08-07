package com.walkgis.utils.common;

import java.util.Arrays;
import java.util.Collections;
import java.util.List;
import java.util.stream.Collectors;

import org.geojson.GeoJsonObject;
import org.geojson.GeometryCollection;
import org.geojson.LineString;
import org.geojson.LngLatAlt;
import org.geojson.MultiLineString;
import org.geojson.MultiPoint;
import org.geojson.MultiPolygon;
import org.geojson.Point;
import org.geojson.Polygon;
import org.locationtech.jts.algorithm.CGAlgorithms;
import org.locationtech.jts.geom.Coordinate;
import org.locationtech.jts.geom.CoordinateArrays;
import org.locationtech.jts.geom.Geometry;
import org.locationtech.jts.geom.GeometryFactory;
import org.locationtech.jts.geom.LinearRing;
import org.locationtech.jts.geom.PrecisionModel;
import org.locationtech.jts.geom.impl.CoordinateArraySequence;

/**
 * @author JerFer
 * @date 2019/8/7---21:35
 */
public class GeometryConvert {
    private GeometryFactory geometryFactory = new GeometryFactory(new PrecisionModel(), 4326);

    public GeometryConvert() {
    }

    private List<LngLatAlt> coordinatesToLngLatAltsList(Coordinate[] coordinates) {
        return (List) Arrays.stream(coordinates).map((coordinate) -> {
            return new LngLatAlt(coordinate.x, coordinate.y);
        }).collect(Collectors.toList());
    }

    private Point pointSerializable(org.locationtech.jts.geom.Point point) {
        return new Point(point.getX(), point.getY(), 0.0D);
    }

    private LineString lineStringSerializable(org.locationtech.jts.geom.LineString lineString) {
        List<LngLatAlt> lngLatAltList = this.coordinatesToLngLatAltsList(lineString.getCoordinates());
        return new LineString((LngLatAlt[]) lngLatAltList.stream().toArray((x$0) -> {
            return new LngLatAlt[x$0];
        }));
    }

    private Polygon polygonSerializable(org.locationtech.jts.geom.Polygon polygon) {
        Polygon polygonOut = new Polygon();
        int ringCount = polygon.getNumInteriorRing();

        for (int i = 0; i < ringCount; ++i) {
            Coordinate[] coordinates = polygon.getInteriorRingN(i).getCoordinates();
            if (CGAlgorithms.isCCW(coordinates)) {
                coordinates = this.correctPolygonCoordinateRotation(coordinates, true);
            }

            List<LngLatAlt> lngLatAltList = this.coordinatesToLngLatAltsList(coordinates);
            polygonOut.addInteriorRing((LngLatAlt[]) lngLatAltList.stream().toArray((x$0) -> {
                return new LngLatAlt[x$0];
            }));
        }

        Coordinate[] coordinates = this.correctPolygonCoordinateRotation(polygon.getExteriorRing().getCoordinates(), false);
        List<LngLatAlt> lngLatAltList = (List) Arrays.stream(coordinates).map((coordinate) -> {
            return new LngLatAlt(coordinate.x, coordinate.y);
        }).collect(Collectors.toList());
        polygonOut.setExteriorRing(lngLatAltList);
        return polygonOut;
    }

    private MultiPoint multiPointSerializable(org.locationtech.jts.geom.MultiPoint multiPoint) {
        List<LngLatAlt> lngLatAlts = this.coordinatesToLngLatAltsList(multiPoint.getCoordinates());
        return new MultiPoint((LngLatAlt[]) lngLatAlts.stream().toArray((x$0) -> {
            return new LngLatAlt[x$0];
        }));
    }

    private MultiLineString multiLineStringSerializable(org.locationtech.jts.geom.MultiLineString multiLineString) {
        return new MultiLineString(this.coordinatesToLngLatAltsList(multiLineString.getCoordinates()));
    }

    private MultiPolygon multiPolygonSerializable(org.locationtech.jts.geom.MultiPolygon multiPolygon) {
        MultiPolygon result = new MultiPolygon();
        int count = multiPolygon.getNumGeometries();

        for (int i = 0; i < count; ++i) {
            Geometry geometry = multiPolygon.getGeometryN(i);
            if (geometry instanceof org.locationtech.jts.geom.Polygon) {
                result.add(this.polygonSerializable((org.locationtech.jts.geom.Polygon) geometry));
            }
        }

        return result;
    }

    private GeometryCollection geometryCollectionSerializable(org.locationtech.jts.geom.GeometryCollection geometryCollection) {
        GeometryCollection gc = new GeometryCollection();

        for (int i = 0; i < geometryCollection.getNumGeometries(); ++i) {
            Geometry geometry = geometryCollection.getGeometryN(i);

            try {
                gc.add(this.geometrySerializer(geometry));
            } catch (Exception var6) {
                var6.printStackTrace();
            }
        }

        return gc;
    }

    public GeoJsonObject geometrySerializer(Geometry geometry) throws Exception {
        if (geometry.getClass() == org.locationtech.jts.geom.Point.class) {
            return this.pointSerializable((org.locationtech.jts.geom.Point) geometry);
        } else if (geometry.getClass() == org.locationtech.jts.geom.LineString.class) {
            return this.lineStringSerializable((org.locationtech.jts.geom.LineString) geometry);
        } else if (geometry.getClass() == org.locationtech.jts.geom.Polygon.class) {
            return this.polygonSerializable((org.locationtech.jts.geom.Polygon) geometry);
        } else if (geometry.getClass() == org.locationtech.jts.geom.MultiPoint.class) {
            return this.multiPointSerializable((org.locationtech.jts.geom.MultiPoint) geometry);
        } else if (geometry.getClass() == org.locationtech.jts.geom.MultiLineString.class) {
            return this.multiLineStringSerializable((org.locationtech.jts.geom.MultiLineString) geometry);
        } else if (geometry.getClass() == org.locationtech.jts.geom.MultiPolygon.class) {
            return this.multiPolygonSerializable((org.locationtech.jts.geom.MultiPolygon) geometry);
        } else if (geometry.getClass() == org.locationtech.jts.geom.GeometryCollection.class) {
            return this.geometryCollectionSerializable((org.locationtech.jts.geom.GeometryCollection) geometry);
        } else {
            throw new Exception("非法的输入数据");
        }
    }

    private Coordinate[] lngLatAltToCoordinates(List<LngLatAlt> coordinates) {
        return (Coordinate[]) coordinates.stream().map((a) -> {
            return new Coordinate(a.getLongitude(), a.getLatitude());
        }).toArray((x$0) -> {
            return new Coordinate[x$0];
        });
    }

    private org.locationtech.jts.geom.Point pointDeserialize(Point point) {
        return new org.locationtech.jts.geom.Point(new Coordinate(point.getCoordinates().getLongitude(), point.getCoordinates().getLatitude()), this.geometryFactory.getPrecisionModel(), this.geometryFactory.getSRID());
    }

    private org.locationtech.jts.geom.LineString lineStringDeserialize(LineString lineString) {
        Coordinate[] coordinates = this.lngLatAltToCoordinates(lineString.getCoordinates());
        return new org.locationtech.jts.geom.LineString(coordinates, this.geometryFactory.getPrecisionModel(), this.geometryFactory.getSRID());
    }

    private org.locationtech.jts.geom.Polygon polygonDeserialize(Polygon polygon) {
        LinearRing linearRing = new LinearRing(new CoordinateArraySequence(this.lngLatAltToCoordinates(polygon.getExteriorRing())), this.geometryFactory);
        List<List<LngLatAlt>> coordinates1 = polygon.getInteriorRings();
        LinearRing[] linearRings = (LinearRing[]) coordinates1.stream().map((item) -> {
            return new LinearRing(new CoordinateArraySequence(this.lngLatAltToCoordinates(item)), this.geometryFactory);
        }).toArray((x$0) -> {
            return new LinearRing[x$0];
        });
        return new org.locationtech.jts.geom.Polygon(linearRing, linearRings, this.geometryFactory);
    }

    private org.locationtech.jts.geom.MultiPoint multiPointDeserialize(MultiPoint multiPoint) {
        org.locationtech.jts.geom.Point[] points = (org.locationtech.jts.geom.Point[]) multiPoint.getCoordinates().stream().map((item) -> {
            return this.pointDeserialize(new Point(item));
        }).toArray((x$0) -> {
            return new org.locationtech.jts.geom.Point[x$0];
        });
        return new org.locationtech.jts.geom.MultiPoint(points, this.geometryFactory);
    }

    private org.locationtech.jts.geom.MultiLineString multiLineStringDeserialize(MultiLineString multiLineString) {
        List<org.locationtech.jts.geom.LineString> lineStrings = (List) multiLineString.getCoordinates().stream().map((item) -> {
            return this.lineStringDeserialize(new LineString((LngLatAlt[]) item.stream().toArray((x$0) -> {
                return new LngLatAlt[x$0];
            })));
        }).collect(Collectors.toList());
        return new org.locationtech.jts.geom.MultiLineString((org.locationtech.jts.geom.LineString[]) lineStrings.stream().toArray((x$0) -> {
            return new org.locationtech.jts.geom.LineString[x$0];
        }), this.geometryFactory);
    }

    private org.locationtech.jts.geom.MultiPolygon multiPolygonDeserialize(MultiPolygon multiPolygon) {
        List<org.locationtech.jts.geom.Polygon> lineStrings = (List) multiPolygon.getCoordinates().stream().map((item) -> {
            Polygon p = null;
            if (item.size() == 1) {
                p = new Polygon((LngLatAlt[]) ((List) item.get(0)).stream().toArray((x$0) -> {
                    return new LngLatAlt[x$0];
                }));
            } else {
                p = new Polygon((LngLatAlt[]) ((List) item.get(0)).stream().toArray((x$0) -> {
                    return new LngLatAlt[x$0];
                }));

                for (int i = 1; i < item.size(); ++i) {
                    p.addInteriorRing((LngLatAlt[]) ((List) item.get(i)).stream().toArray((x$0) -> {
                        return new LngLatAlt[x$0];
                    }));
                }
            }

            return this.polygonDeserialize(p);
        }).collect(Collectors.toList());
        return new org.locationtech.jts.geom.MultiPolygon((org.locationtech.jts.geom.Polygon[]) lineStrings.stream().toArray((x$0) -> {
            return new org.locationtech.jts.geom.Polygon[x$0];
        }), this.geometryFactory);
    }

    public org.locationtech.jts.geom.GeometryCollection geometryCollectionDeserialize(GeometryCollection geoJsonObjects) {
        List<Geometry> geometries = (List) geoJsonObjects.getGeometries().stream().map((a) -> {
            try {
                return this.geometryDeserialize(a);
            } catch (Exception var3) {
                var3.printStackTrace();
                return null;
            }
        }).collect(Collectors.toList());
        geometries.removeAll(Collections.singleton((Object) null));
        Geometry[] geometries1 = (Geometry[]) geometries.stream().toArray((x$0) -> {
            return new Geometry[x$0];
        });
        return new org.locationtech.jts.geom.GeometryCollection(geometries1, this.geometryFactory);
    }

    public Geometry geometryDeserialize(GeoJsonObject geometry) throws Exception {
        if (geometry.getClass() == Point.class) {
            return this.pointDeserialize((Point) geometry);
        } else if (geometry.getClass() == LineString.class) {
            return this.lineStringDeserialize((LineString) geometry);
        } else if (geometry.getClass() == Polygon.class) {
            return this.polygonDeserialize((Polygon) geometry);
        } else if (geometry.getClass() == MultiPoint.class) {
            return this.multiPointDeserialize((MultiPoint) geometry);
        } else if (geometry.getClass() == MultiLineString.class) {
            return this.multiLineStringDeserialize((MultiLineString) geometry);
        } else if (geometry.getClass() == MultiPolygon.class) {
            return this.multiPolygonDeserialize((MultiPolygon) geometry);
        } else if (geometry.getClass() == GeometryCollection.class) {
            return this.geometryCollectionDeserialize((GeometryCollection) geometry);
        } else {
            throw new Exception("不支持的几何类型");
        }
    }

    private Coordinate[] correctPolygonCoordinateRotation(Coordinate[] coordinates, boolean representsHole) {
        if (this.isAntiClockwise(coordinates)) {
            if (representsHole) {
                CoordinateArrays.reverse(coordinates);
            }
        } else if (!representsHole) {
            CoordinateArrays.reverse(coordinates);
        }

        return coordinates;
    }

    private boolean isAntiClockwise(Coordinate[] coordinates) {
        return CGAlgorithms.isCCW(coordinates);
    }
}

