/*
 * Licensed to Elastic Search and Shay Banon under one
 * or more contributor license agreements.  See the NOTICE file
 * distributed with this work for additional information
 * regarding copyright ownership. Elastic Search licenses this
 * file to you under the Apache License, Version 2.0 (the
 * "License"); you may not use this file except in compliance
 * with the License.  You may obtain a copy of the License at
 *
 *    http://www.apache.org/licenses/LICENSE-2.0
 *
 * Unless required by applicable law or agreed to in writing,
 * software distributed under the License is distributed on an
 * "AS IS" BASIS, WITHOUT WARRANTIES OR CONDITIONS OF ANY
 * KIND, either express or implied.  See the License for the
 * specific language governing permissions and limitations
 * under the License.
 */

package org.elasticsearch.index.mapper.xcontent.geo;

import org.apache.lucene.index.IndexReader;
import org.elasticsearch.common.thread.ThreadLocals;
import org.elasticsearch.common.trove.TDoubleArrayList;
import org.elasticsearch.index.field.data.FieldData;
import org.elasticsearch.index.field.data.FieldDataType;
import org.elasticsearch.index.field.data.support.FieldDataLoader;
import org.elasticsearch.index.search.geo.GeoHashUtils;

import java.io.IOException;

/**
 * @author kimchy (shay.banon)
 */
public abstract class GeoPointFieldData extends FieldData<GeoPointDocFieldData> {

    static ThreadLocal<ThreadLocals.CleanableValue<GeoPoint>> valuesCache = new ThreadLocal<ThreadLocals.CleanableValue<GeoPoint>>() {
        @Override protected ThreadLocals.CleanableValue<GeoPoint> initialValue() {
            return new ThreadLocals.CleanableValue<GeoPoint>(new GeoPoint());
        }
    };

    public static final GeoPoint[] EMPTY_ARRAY = new GeoPoint[0];

    protected final double[] lat;
    protected final double[] lon;

    protected GeoPointFieldData(String fieldName, double[] lat, double[] lon) {
        super(fieldName);
        this.lat = lat;
        this.lon = lon;
    }

    abstract public GeoPoint value(int docId);

    abstract public GeoPoint[] values(int docId);

    abstract public double latValue(int docId);

    abstract public double lonValue(int docId);

    abstract public double[] latValues(int docId);

    abstract public double[] lonValues(int docId);

    @Override public GeoPointDocFieldData docFieldData(int docId) {
        return super.docFieldData(docId);
    }

    @Override public String stringValue(int docId) {
        return value(docId).geohash();
    }

    @Override protected GeoPointDocFieldData createFieldData() {
        return new GeoPointDocFieldData(this);
    }

    @Override public FieldDataType type() {
        return GeoPointFieldDataType.TYPE;
    }

    @Override public void forEachValue(StringValueProc proc) {
        for (int i = 1; i < lat.length; i++) {
            proc.onValue(GeoHashUtils.encode(lat[i], lon[i]));
        }
    }

    public void forEachValue(PointValueProc proc) {
        for (int i = 1; i < lat.length; i++) {
            GeoPoint point = valuesCache.get().get();
            point.latlon(lat[i], lon[i]);
            proc.onValue(point);
        }
    }

    public static interface PointValueProc {
        void onValue(GeoPoint value);
    }

    public void forEachValue(ValueProc proc) {
        for (int i = 1; i < lat.length; i++) {
            proc.onValue(lat[i], lon[i]);
        }
    }

    public static interface ValueProc {
        void onValue(double lat, double lon);
    }

    public static GeoPointFieldData load(IndexReader reader, String field) throws IOException {
        return FieldDataLoader.load(reader, field, new StringTypeLoader());
    }

    static class StringTypeLoader extends FieldDataLoader.FreqsTypeLoader<GeoPointFieldData> {

        private final TDoubleArrayList lat = new TDoubleArrayList();
        private final TDoubleArrayList lon = new TDoubleArrayList();

        StringTypeLoader() {
            super();
            // the first one indicates null value
            lat.add(0);
            lon.add(0);
        }

        @Override public void collectTerm(String term) {
            int comma = term.indexOf(',');
            lat.add(Double.parseDouble(term.substring(0, comma)));
            lon.add(Double.parseDouble(term.substring(comma + 1)));

        }

        @Override public GeoPointFieldData buildSingleValue(String field, int[] order) {
            return new SingleValueGeoPointFieldData(field, order, lat.toNativeArray(), lon.toNativeArray());
        }

        @Override public GeoPointFieldData buildMultiValue(String field, int[][] order) {
            return new MultiValueGeoPointFieldData(field, order, lat.toNativeArray(), lon.toNativeArray());
        }
    }
}
