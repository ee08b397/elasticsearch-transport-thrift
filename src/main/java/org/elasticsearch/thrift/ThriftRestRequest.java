/*
 * Licensed to Elasticsearch under one or more contributor
 * license agreements. See the NOTICE file distributed with
 * this work for additional information regarding copyright
 * ownership. Elasticsearch licenses this file to you under
 * the Apache License, Version 2.0 (the "License"); you may
 * not use this file except in compliance with the License.
 * You may obtain a copy of the License at
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

package org.elasticsearch.thrift;

import org.elasticsearch.common.bytes.BytesArray;
import org.elasticsearch.common.bytes.BytesReference;
import org.elasticsearch.common.collect.ImmutableList;
import org.elasticsearch.rest.support.RestUtils;

import java.util.HashMap;
import java.util.Map;

/**
 */
public class ThriftRestRequest extends org.elasticsearch.rest.RestRequest {

    private final org.elasticsearch.thrift.RestRequest request;

    private final String rawPath;

    private final Map<String, String> params;

    public ThriftRestRequest(org.elasticsearch.thrift.RestRequest request) {
        this.request = request;
        this.params = request.getParameters() == null ? new HashMap<String, String>() : request.getParameters();

        int pathEndPos = request.getUri().indexOf('?');
        if (pathEndPos < 0) {
            this.rawPath = request.getUri();
        } else {
            this.rawPath = request.getUri().substring(0, pathEndPos);
            RestUtils.decodeQueryString(request.getUri(), pathEndPos + 1, params);
        }
    }

    @Override
    public Method method() {
        switch (request.getMethod()) {
            case GET:
                return Method.GET;
            case POST:
                return Method.POST;
            case PUT:
                return Method.PUT;
            case DELETE:
                return Method.DELETE;
            case HEAD:
                return Method.HEAD;
            case OPTIONS:
                return Method.OPTIONS;
        }
        return null;
    }

    @Override
    public String uri() {
        return request.getUri();
    }

    @Override
    public String rawPath() {
        return this.rawPath;
    }

    @Override
    public boolean hasContent() {
        return request.isSetBody() && request.bufferForBody().remaining() > 0;
    }

    @Override
    public BytesReference content() {
        if (!request.isSetBody()) {
            return BytesArray.EMPTY;
        }
        return new BytesArray(request.getBody());
    }

    @Override
    public String header(String name) {
        if (request.getHeaders() == null) {
            return null;
        }
        return request.getHeaders().get(name);
    }

    @Override
    public Iterable<Map.Entry<String, String>> headers() {
        if (request.getHeaders() == null) {
            return null;
        }

        return ImmutableList.copyOf(request.getHeaders().entrySet());
    }

    @Override
    public boolean hasParam(String key) {
        return params.containsKey(key);
    }

    @Override
    public String param(String key) {
        return params.get(key);
    }

    @Override
    public Map<String, String> params() {
        return params;
    }

    @Override
    public String param(String key, String defaultValue) {
        String value = params.get(key);
        if (value == null) {
            return defaultValue;
        }
        return value;
    }
}
