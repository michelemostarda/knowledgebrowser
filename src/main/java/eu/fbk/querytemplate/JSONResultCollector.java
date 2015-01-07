/*
 * Licensed to the Apache Software Foundation (ASF) under one or more
 * contributor license agreements.  See the NOTICE file distributed with
 * this work for additional information regarding copyright ownership.
 * The ASF licenses this file to You under the Apache License, Version 2.0
 * (the "License"); you may not use this file except in compliance with
 * the License.  You may obtain a copy of the License at
 *
 *  http://www.apache.org/licenses/LICENSE-2.0
 *
 * Unless required by applicable law or agreed to in writing, software
 * distributed under the License is distributed on an "AS IS" BASIS,
 * WITHOUT WARRANTIES OR CONDITIONS OF ANY KIND, either express or implied.
 * See the License for the specific language governing permissions and
 * limitations under the License.
 */

package eu.fbk.querytemplate;

import com.google.common.collect.ArrayListMultimap;
import com.google.common.collect.Multimap;
import org.codehaus.jackson.JsonGenerator;

import java.io.IOException;

/**
 * @author Michele Mostarda (mostarda@fbk.eu)
 */
public class JSONResultCollector implements ResultCollector {

    private final JsonGenerator generator;
    private final Multimap<String,String> multimap = ArrayListMultimap.create();

    public JSONResultCollector(JsonGenerator generator) {
        this.generator = generator;
    }

    @Override
    public void begin() {
        try {
            generator.writeStartObject();
        } catch (IOException e) {
            throw new RuntimeException(e);
        }
    }

    @Override
    public void startLevel(int l, String queryName) {
        try {
            flushMap();
            generator.writeFieldName(queryName);
            generator.writeStartObject();
        } catch (IOException e) {
            throw new RuntimeException(e);
        }
    }

    @Override
    public void collect(String[] bindings, String[] values) {
        for(int i = 0; i < bindings.length; i++) {
            multimap.put(bindings[i], values[i]);
        }
    }

    @Override
    public void endLevel(int l) {
        try {
            flushMap();
            generator.writeEndObject();
        } catch (IOException e) {
            throw new RuntimeException(e);
        }
    }

    @Override
    public void end() {
        try {
            generator.writeEndObject();
        } catch (IOException e) {
            throw new RuntimeException(e);
        }

    }

    private void flushMap() throws IOException {
        if(multimap.isEmpty()) return;
        for (String k : multimap.keySet()) {
            generator.writeFieldName(k);
            generator.writeStartArray();
            for (String v : multimap.get(k)) {
                generator.writeString(v);
            }
            generator.writeEndArray();
        }
        multimap.clear();
    }
}