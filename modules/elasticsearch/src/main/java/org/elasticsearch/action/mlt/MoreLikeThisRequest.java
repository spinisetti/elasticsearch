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

package org.elasticsearch.action.mlt;

import org.elasticsearch.action.ActionRequest;
import org.elasticsearch.action.ActionRequestValidationException;
import org.elasticsearch.action.Actions;
import org.elasticsearch.action.search.SearchType;
import org.elasticsearch.search.Scroll;
import org.elasticsearch.search.builder.SearchSourceBuilder;
import org.elasticsearch.util.Bytes;
import org.elasticsearch.util.Required;
import org.elasticsearch.util.Strings;
import org.elasticsearch.util.Unicode;

import java.io.DataInput;
import java.io.DataOutput;
import java.io.IOException;

import static org.elasticsearch.search.Scroll.*;

/**
 * A more like this request allowing to search for documents that a "like" the provided document. The document
 * to check against to fetched based on the index, type and id provided. Best created with {@link org.elasticsearch.client.Requests#moreLikeThisRequest(String)}.
 *
 * <p>Note, the {@link #index()}, {@link #type(String)} and {@link #id(String)} are required.
 *
 * @author kimchy (shay.banon)
 * @see org.elasticsearch.client.Client#moreLikeThis(MoreLikeThisRequest)
 * @see org.elasticsearch.client.Requests#moreLikeThisRequest(String)
 * @see org.elasticsearch.action.search.SearchResponse
 */
public class MoreLikeThisRequest implements ActionRequest {

    private String index;

    private String type;

    private String id;

    private String[] fields;

    private float percentTermsToMatch = -1;
    private int minTermFrequency = -1;
    private int maxQueryTerms = -1;
    private String[] stopWords = null;
    private int minDocFreq = -1;
    private int maxDocFreq = -1;
    private int minWordLen = -1;
    private int maxWordLen = -1;
    private Boolean boostTerms = null;
    private float boostTermsFactor = -1;

    private SearchType searchType = SearchType.DEFAULT;
    private String searchQueryHint;
    private String[] searchIndices;
    private String[] searchTypes;
    private Scroll searchScroll;
    private byte[] searchSource;


    private boolean threadedListener = false;

    MoreLikeThisRequest() {
    }

    /**
     * Constructs a new more like this request for a document that will be fetch from the provided index.
     * Use {@link #type(String)} and {@link #id(String)} to specificy the document to load.
     */
    public MoreLikeThisRequest(String index) {
        this.index = index;
    }

    /**
     * The index to load the document from which the "like" query will run with.
     */
    public String index() {
        return index;
    }

    /**
     * The type of document to load from which the "like" query will rutn with.
     */
    public String type() {
        return type;
    }

    /**
     * The type of document to load from which the "like" query will rutn with.
     */
    @Required public MoreLikeThisRequest type(String type) {
        this.type = type;
        return this;
    }

    /**
     * The id of document to load from which the "like" query will rutn with.
     */
    public String id() {
        return id;
    }

    /**
     * The id of document to load from which the "like" query will rutn with.
     */
    @Required public MoreLikeThisRequest id(String id) {
        this.id = id;
        return this;
    }

    /**
     * The fields of the document to use in order to find documents "like" this one. Defaults to run
     * against all the document fields.
     */
    public String[] fields() {
        return this.fields;
    }

    /**
     * The fields of the document to use in order to find documents "like" this one. Defaults to run
     * against all the document fields.
     */
    public MoreLikeThisRequest fields(String... fields) {
        this.fields = fields;
        return this;
    }

    /**
     * The percent of the terms to match for each field. Defaults to <tt>0.3f</tt>.
     */
    public MoreLikeThisRequest percentTermsToMatch(float percentTermsToMatch) {
        this.percentTermsToMatch = percentTermsToMatch;
        return this;
    }

    /**
     * The percent of the terms to match for each field. Defaults to <tt>0.3f</tt>.
     */
    public float percentTermsToMatch() {
        return this.percentTermsToMatch;
    }

    /**
     * The frequency below which terms will be ignored in the source doc. Defaults to <tt>2</tt>.
     */
    public MoreLikeThisRequest minTermFrequency(int minTermFrequency) {
        this.minTermFrequency = minTermFrequency;
        return this;
    }

    /**
     * The frequency below which terms will be ignored in the source doc. Defaults to <tt>2</tt>.
     */
    public int minTermFrequency() {
        return this.minTermFrequency;
    }

    /**
     * The maximum number of query terms that will be included in any generated query. Defaults to <tt>25</tt>.
     */
    public MoreLikeThisRequest maxQueryTerms(int maxQueryTerms) {
        this.maxQueryTerms = maxQueryTerms;
        return this;
    }

    /**
     * The maximum number of query terms that will be included in any generated query. Defaults to <tt>25</tt>.
     */
    public int maxQueryTerms() {
        return this.maxQueryTerms;
    }

    /**
     * Any word in this set is considered "uninteresting" and ignored.
     *
     * <p>Even if your Analyzer allows stopwords, you might want to tell the MoreLikeThis code to ignore them, as
     * for the purposes of document similarity it seems reasonable to assume that "a stop word is never interesting".
     *
     * <p>Defaults to no stop words.
     */
    public MoreLikeThisRequest stopWords(String... stopWords) {
        this.stopWords = stopWords;
        return this;
    }

    /**
     * Any word in this set is considered "uninteresting" and ignored.
     *
     * <p>Even if your Analyzer allows stopwords, you might want to tell the MoreLikeThis code to ignore them, as
     * for the purposes of document similarity it seems reasonable to assume that "a stop word is never interesting".
     *
     * <p>Defaults to no stop words.
     */
    public String[] stopWords() {
        return this.stopWords;
    }

    /**
     * The frequency at which words will be ignored which do not occur in at least this
     * many docs. Defaults to <tt>5</tt>.
     */
    public MoreLikeThisRequest minDocFreq(int minDocFreq) {
        this.minDocFreq = minDocFreq;
        return this;
    }

    /**
     * The frequency at which words will be ignored which do not occur in at least this
     * many docs. Defaults to <tt>5</tt>.
     */
    public int minDocFreq() {
        return this.minDocFreq;
    }

    /**
     * The maximum frequency in which words may still appear. Words that appear
     * in more than this many docs will be ignored. Defaults to unbounded.
     */
    public MoreLikeThisRequest maxDocFreq(int maxDocFreq) {
        this.maxDocFreq = maxDocFreq;
        return this;
    }

    /**
     * The maximum frequency in which words may still appear. Words that appear
     * in more than this many docs will be ignored. Defaults to unbounded.
     */
    public int maxDocFreq() {
        return this.maxDocFreq;
    }

    /**
     * The minimum word length below which words will be ignored. Defaults to <tt>0</tt>.
     */
    public MoreLikeThisRequest minWordLen(int minWordLen) {
        this.minWordLen = minWordLen;
        return this;
    }

    /**
     * The minimum word length below which words will be ignored. Defaults to <tt>0</tt>.
     */
    public int minWordLen() {
        return this.minWordLen;
    }

    /**
     * The maximum word length above which words will be ignored. Defaults to unbounded.
     */
    public MoreLikeThisRequest maxWordLen(int maxWordLen) {
        this.maxWordLen = maxWordLen;
        return this;
    }

    /**
     * The maximum word length above which words will be ignored. Defaults to unbounded.
     */
    public int maxWordLen() {
        return this.maxWordLen;
    }

    /**
     * Whether to boost terms in query based on "score" or not. Defaults to <tt>false</tt>.
     */
    public MoreLikeThisRequest boostTerms(Boolean boostTerms) {
        this.boostTerms = boostTerms;
        return this;
    }

    /**
     * Whether to boost terms in query based on "score" or not. Defaults to <tt>false</tt>.
     */
    public Boolean boostTerms() {
        return this.boostTerms;
    }

    /**
     * The boost factor to use when boosting terms. Defaults to <tt>1</tt>.
     */
    public MoreLikeThisRequest boostTermsFactor(float boostTermsFactor) {
        this.boostTermsFactor = boostTermsFactor;
        return this;
    }

    /**
     * The boost factor to use when boosting terms. Defaults to <tt>1</tt>.
     */
    public float boostTermsFactor() {
        return this.boostTermsFactor;
    }

    /**
     * An optional search source request allowing to control the search request for the
     * more like this documents.
     */
    public MoreLikeThisRequest searchSource(SearchSourceBuilder sourceBuilder) {
        return searchSource(sourceBuilder.build());
    }

    /**
     * An optional search source request allowing to control the search request for the
     * more like this documents.
     */
    public MoreLikeThisRequest searchSource(String searchSource) {
        return searchSource(Unicode.fromStringAsBytes(searchSource));
    }

    /**
     * An optional search source request allowing to control the search request for the
     * more like this documents.
     */
    public MoreLikeThisRequest searchSource(byte[] searchSource) {
        this.searchSource = searchSource;
        return this;
    }

    /**
     * An optional search source request allowing to control the search request for the
     * more like this documents.
     */
    public byte[] searchSource() {
        return this.searchSource;
    }

    /**
     * The search type of the mlt search query.
     */
    public MoreLikeThisRequest searchType(SearchType searchType) {
        this.searchType = searchType;
        return this;
    }

    /**
     * The search type of the mlt search query.
     */
    public SearchType searchType() {
        return this.searchType;
    }

    /**
     * The indices the resulting mlt query will run against. If not set, will run
     * against the index the document was fetched from.
     */
    public MoreLikeThisRequest searchIndices(String... searchIndices) {
        this.searchIndices = searchIndices;
        return this;
    }

    /**
     * The indices the resulting mlt query will run against. If not set, will run
     * against the index the document was fetched from.
     */
    public String[] searchIndices() {
        return this.searchIndices;
    }

    /**
     * The types the resulting mlt query will run against. If not set, will run
     * against the type of the document fetched.
     */
    public MoreLikeThisRequest searchTypes(String... searchTypes) {
        this.searchTypes = searchTypes;
        return this;
    }

    /**
     * The types the resulting mlt query will run against. If not set, will run
     * against the type of the document fetched.
     */
    public String[] searchTypes() {
        return this.searchTypes;
    }

    /**
     * Optional search query hint.
     */
    public MoreLikeThisRequest searchQueryHint(String searchQueryHint) {
        this.searchQueryHint = searchQueryHint;
        return this;
    }

    /**
     * Optional search query hint.
     */
    public String searchQueryHint() {
        return this.searchQueryHint;
    }

    /**
     * An optional search scroll request to be able to continue and scroll the search
     * operation.
     */
    public MoreLikeThisRequest searchScroll(Scroll searchScroll) {
        this.searchScroll = searchScroll;
        return this;
    }

    /**
     * An optional search scroll request to be able to continue and scroll the search
     * operation.
     */
    public Scroll searchScroll() {
        return this.searchScroll;
    }

    @Override public ActionRequestValidationException validate() {
        ActionRequestValidationException validationException = null;
        if (index == null) {
            validationException = Actions.addValidationError("index is missing", validationException);
        }
        if (type == null) {
            validationException = Actions.addValidationError("type is missing", validationException);
        }
        if (id == null) {
            validationException = Actions.addValidationError("id is missing", validationException);
        }
        return validationException;
    }

    /**
     * Should the listener be called on a separate thread if needed.
     */
    @Override public boolean listenerThreaded() {
        return threadedListener;
    }

    /**
     * Should the listener be called on a separate thread if needed.
     */
    @Override public ActionRequest listenerThreaded(boolean listenerThreaded) {
        this.threadedListener = listenerThreaded;
        return this;
    }

    @Override public void readFrom(DataInput in) throws IOException, ClassNotFoundException {
        index = in.readUTF();
        type = in.readUTF();
        id = in.readUTF();
        // no need to pass threading over the network, they are always false when coming throw a thread pool
        int size = in.readInt();
        if (size == 0) {
            fields = Strings.EMPTY_ARRAY;
        } else {
            fields = new String[in.readInt()];
            for (int i = 0; i < size; i++) {
                fields[i] = in.readUTF();
            }
        }

        percentTermsToMatch = in.readFloat();
        minTermFrequency = in.readInt();
        maxQueryTerms = in.readInt();
        size = in.readInt();
        if (size > 0) {
            stopWords = new String[size];
            for (int i = 0; i < size; i++) {
                stopWords[i] = in.readUTF();
            }
        }
        minDocFreq = in.readInt();
        maxDocFreq = in.readInt();
        minWordLen = in.readInt();
        maxWordLen = in.readInt();
        if (in.readBoolean()) {
            boostTerms = in.readBoolean();
        }
        boostTermsFactor = in.readFloat();
        searchType = SearchType.fromId(in.readByte());
        if (in.readBoolean()) {
            searchQueryHint = in.readUTF();
        }
        size = in.readInt();
        if (size == -1) {
            searchIndices = null;
        } else if (size == 0) {
            searchIndices = Strings.EMPTY_ARRAY;
        } else {
            searchIndices = new String[size];
            for (int i = 0; i < size; i++) {
                searchIndices[i] = in.readUTF();
            }
        }
        size = in.readInt();
        if (size == -1) {
            searchTypes = null;
        } else if (size == 0) {
            searchTypes = Strings.EMPTY_ARRAY;
        } else {
            searchTypes = new String[size];
            for (int i = 0; i < size; i++) {
                searchTypes[i] = in.readUTF();
            }
        }
        if (in.readBoolean()) {
            searchScroll = readScroll(in);
        }
        size = in.readInt();
        if (size == 0) {
            searchSource = Bytes.EMPTY_ARRAY;
        } else {
            searchSource = new byte[in.readInt()];
            in.readFully(searchSource);
        }
    }

    @Override public void writeTo(DataOutput out) throws IOException {
        out.writeUTF(index);
        out.writeUTF(type);
        out.writeUTF(id);
        if (fields == null) {
            out.writeInt(0);
        } else {
            out.writeInt(fields.length);
            for (String field : fields) {
                out.writeUTF(field);
            }
        }

        out.writeFloat(percentTermsToMatch);
        out.writeInt(minTermFrequency);
        out.writeInt(maxQueryTerms);
        if (stopWords == null) {
            out.writeInt(0);
        } else {
            out.writeInt(stopWords.length);
            for (String stopWord : stopWords) {
                out.writeUTF(stopWord);
            }
        }
        out.writeInt(minDocFreq);
        out.writeInt(maxDocFreq);
        out.writeInt(minWordLen);
        out.writeInt(maxWordLen);
        if (boostTerms == null) {
            out.writeBoolean(false);
        } else {
            out.writeBoolean(true);
            out.writeBoolean(boostTerms);
        }
        out.writeFloat(boostTermsFactor);

        out.writeByte(searchType.id());
        if (searchQueryHint == null) {
            out.writeBoolean(false);
        } else {
            out.writeBoolean(true);
            out.writeUTF(searchQueryHint);
        }
        if (searchIndices == null) {
            out.writeInt(-1);
        } else {
            out.writeInt(searchIndices.length);
            for (String index : searchIndices) {
                out.writeUTF(index);
            }
        }
        if (searchTypes == null) {
            out.writeInt(-1);
        } else {
            out.writeInt(searchTypes.length);
            for (String type : searchTypes) {
                out.writeUTF(type);
            }
        }
        if (searchScroll == null) {
            out.writeBoolean(false);
        } else {
            out.writeBoolean(true);
            searchScroll.writeTo(out);
        }
        if (searchSource == null) {
            out.writeInt(0);
        } else {
            out.writeInt(searchSource.length);
            out.write(searchSource);
        }
    }
}