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

import com.google.common.collect.HashMultimap;
import com.google.common.collect.SetMultimap;
import org.codehaus.jackson.JsonGenerator;

import java.io.IOException;
import java.util.Collection;
import java.util.Stack;

/**
 * @author Michele Mostarda (mostarda@fbk.eu)
 */
public class JSONResultCollector implements ResultCollector {

    private final JsonGenerator generator;
    private final SetMultimap<String,String> multimap = HashMultimap.create();
    private final String fieldBinding;
    private final String valueBinding;
    private final Stack<Boolean> pivotOpen = new Stack<>();

    public JSONResultCollector(JsonGenerator generator, String fieldValue) {
        this.generator = generator;
        final String[] parts = fieldValue.split(":");
        if(parts.length != 2)
            throw new IllegalArgumentException("Invalid pattern, expected: <field-binding>:<value-binding>");
        fieldBinding = parts[0];
        valueBinding = parts[1];
    }

    @Override
    public void values(String[] values) {
        // Empty.
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
        pivotOpen.push(false);
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
        String k, v; k = v = null;
        for(int i = 0; i < bindings.length; i++) {
            if(fieldBinding.equals(bindings[i])) {
                k = values[i];
            } else if(valueBinding.contains(bindings[i])) {
                v = values[i];
            }
        }
        if(k != null && v != null)
            multimap.put(k,v);
    }

    @Override
    public void pivot(String name) {
        try {
            flushMap();
            if(pivotOpen.peek()) {
                generator.writeEndObject();
            } else {
                pivotOpen.pop();
                pivotOpen.push(true);
            }
            generator.writeFieldName(name);
            generator.writeStartObject();
        } catch (IOException e) {
            throw new RuntimeException(e);
        }
    }

    @Override
    public void endLevel(int l) {
        try {
            flushMap();
            if(pivotOpen.peek()) {
                generator.writeEndObject();
            }
            generator.writeEndObject();
        } catch (IOException e) {
            throw new RuntimeException(e);
        }
        pivotOpen.pop();
    }

    @Override
    public void end() {
        try {
            flushMap();
            generator.writeEndObject();
            generator.flush();
        } catch (IOException e) {
            throw new RuntimeException(e);
        }
    }

    private void flushMap() throws IOException {
        if(multimap.isEmpty()) return;
        for (String k : multimap.keySet()) {
            generator.writeFieldName(k);
            final Collection<String> valueSet = multimap.get(k);
            if(valueSet.size() == 1) {
                generator.writeString(valueSet.iterator().next());
            } else {
                generator.writeStartArray();
                for (String v : valueSet) {
                    generator.writeString(v);
                }
                generator.writeEndArray();
            }
        }
        multimap.clear();
    }

}
