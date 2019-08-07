package com.walkgis.utils.common;

/**
 * @author JerFer
 * @date 2019/8/7---21:36
 */

public class GlobalGeodetic {
    private int tileSize;
    private double resFact;

    public GlobalGeodetic(String tmscompatible, int tileSize) {
        this.tileSize = tileSize;
        if (tmscompatible != null && tmscompatible.length() > 0) {
            this.resFact = 180.0D / (double) this.tileSize;
        } else {
            this.resFact = 360.0D / (double) this.tileSize;
        }

    }

    public double[] lonlatToPixels(double lon, double lat, int zoom) {
        double res = this.resFact / Math.pow(2.0D, (double) zoom);
        return new double[]{(180.0D + lon) / res, (90.0D + lat) / res};
    }

    public int[] pixelsToTile(double px, double py) {
        int tx = (int) (Math.ceil(px / (double) this.tileSize) - 1.0D);
        int ty = (int) (Math.ceil(py / (double) this.tileSize) - 1.0D);
        return new int[]{tx, ty};
    }

    public int[] lonlatToTile(double lon, double lat, int zoom) {
        double[] pxpy = this.lonlatToPixels(lon, lat, zoom);
        return this.pixelsToTile(pxpy[0], pxpy[1]);
    }

    public double resolution(int zoom) {
        return this.resFact / Math.pow(2.0D, (double) zoom);
    }

    public int zoomForPixelSize(double pixelSize) {
        for (int i = 0; i < 32; ++i) {
            if (pixelSize > this.resolution(i)) {
                if (i != 0) {
                    return i - 1;
                }

                return 0;
            }
        }

        return 0;
    }

    public double[] tileBounds(int tx, int ty, int zoom) {
        double res = this.resFact / Math.pow(2.0D, (double) zoom);
        return new double[]{(double) (tx * this.tileSize) * res - 180.0D, (double) (ty * this.tileSize) * res - 90.0D, (double) ((tx + 1) * this.tileSize) * res - 180.0D, (double) ((ty + 1) * this.tileSize) * res - 90.0D};
    }

    public double[] tileLatLonBounds(int tx, int ty, int zoom) {
        double[] b = this.tileBounds(tx, ty, zoom);
        return new double[]{b[1], b[0], b[3], b[2]};
    }
}

