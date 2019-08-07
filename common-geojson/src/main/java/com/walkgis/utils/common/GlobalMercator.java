package com.walkgis.utils.common;

/**
 * @author JerFer
 * @date 2019/8/7---21:36
 */
public class GlobalMercator {
    private int tileSize;
    private double initialResolution;
    private double originShift;

    public GlobalMercator(int tileSize) {
        this.tileSize = tileSize;
        this.initialResolution = 4.007501668557849E7D / (double) this.tileSize;
        this.originShift = 2.0037508342789244E7D;
    }

    public double[] latLonToMeters(double lat, double lon) {
        double mx = lon * this.originShift / 180.0D;
        double my = Math.log(Math.tan((90.0D + lat) * 3.141592653589793D / 360.0D)) / 0.017453292519943295D;
        my = my * this.originShift / 180.0D;
        return new double[]{mx, my};
    }

    public double[] metersToLatLon(double mx, double my) {
        double lon = mx / this.originShift * 180.0D;
        double lat = my / this.originShift * 180.0D;
        lat = 57.29577951308232D * (2.0D * Math.atan(Math.exp(lat * 3.141592653589793D / 180.0D)) - 1.5707963267948966D);
        return new double[]{lat, lon};
    }

    public double[] pixelsToMeters(int px, int py, int zoom) {
        double res = this.resolution(zoom);
        double mx = (double) px * res - this.originShift;
        double my = (double) py * res - this.originShift;
        return new double[]{mx, my};
    }

    public double[] metersToPixels(double mx, double my, int zoom) {
        double res = this.resolution(zoom);
        double px = (mx + this.originShift) / res;
        double py = (my + this.originShift) / res;
        return new double[]{px, py};
    }

    public int[] pixelsToTile(double px, double py) {
        int tx = (int) (Math.ceil(px / (double) ((float) this.tileSize)) - 1.0D);
        int ty = (int) (Math.ceil(py / (double) ((float) this.tileSize)) - 1.0D);
        return new int[]{tx, ty};
    }

    public double[] pixelsToRaster(double px, double py, int zoom) {
        double mapSize = (double) (this.tileSize << zoom);
        return new double[]{px, mapSize - py};
    }

    public int[] metersToTile(double mx, double my, int zoom) {
        double[] coordinate = this.metersToPixels(mx, my, zoom);
        return this.pixelsToTile(coordinate[0], coordinate[1]);
    }

    public double[] tileBounds(int tx, int ty, int zoom) {
        double[] minxy = this.pixelsToMeters(tx * this.tileSize, ty * this.tileSize, zoom);
        double[] maxxy = this.pixelsToMeters((tx + 1) * this.tileSize, (ty + 1) * this.tileSize, zoom);
        return new double[]{minxy[0], minxy[1], maxxy[0], maxxy[1]};
    }

    public double[] tileLatLonBounds(int tx, int ty, int zoom) {
        double[] bounds = this.tileBounds(tx, ty, zoom);
        double[] minLatLon = this.metersToLatLon(bounds[0], bounds[1]);
        double[] maxLatlon = this.metersToLatLon(bounds[2], bounds[3]);
        return new double[]{minLatLon[0], minLatLon[1], maxLatlon[0], maxLatlon[1]};
    }

    public double resolution(int zoom) {
        return this.initialResolution / Math.pow(2.0D, (double) zoom);
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

    public int[] googleTile(int tx, int ty, int zoom) {
        return new int[]{tx, (int) Math.pow(2.0D, (double) zoom) - 1 - ty};
    }

    public String tileXYToQuadKey(int tileX, int tileY, int levelOfDetail) {
        StringBuilder quadKey = new StringBuilder();

        for (int i = levelOfDetail; i > 0; --i) {
            char digit = '0';
            int mask = 1 << i - 1;
            if ((tileX & mask) != 0) {
                ++digit;
            }

            if ((tileY & mask) != 0) {
                ++digit;
                ++digit;
            }

            quadKey.append(digit);
        }

        return quadKey.toString();
    }

    public int[] quadKeyToTileXY(String quadKey) throws Exception {
        int tileX = 0;
        int tileY = 0;
        int levelOfDetail = quadKey.length();

        for (int i = levelOfDetail; i > 0; --i) {
            int mask = 1 << i - 1;
            switch (quadKey.charAt(levelOfDetail - i)) {
                case '0':
                    break;
                case '1':
                    tileX |= mask;
                    break;
                case '2':
                    tileY |= mask;
                    break;
                case '3':
                    tileX |= mask;
                    tileY |= mask;
                    break;
                default:
                    throw new Exception("Invalid QuadKey digit sequence.");
            }
        }

        return new int[]{tileX, tileY, levelOfDetail};
    }
}

