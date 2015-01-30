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

import java.util.ArrayList;
import java.util.Arrays;
import java.util.Collections;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.Stack;

/**
 * @author Michele Mostarda (mostarda@fbk.eu)
 */
public class DefaultNestedQuery implements NestedQuery {

    private final List<String> queryNames = new ArrayList<>();
    private final List<Query> levels = new ArrayList<>();
    private final List<String> pivots = new ArrayList<>();
    private final List<PropertyPivot> propertyPivots = new ArrayList<>();
    private final Stack<String> lastPivotValue = new Stack<>();

    void addQuery(String name, Query query, String pivot, PropertyPivot propertyPivot) {
        if(name == null || name.trim().length() == 0) throw new IllegalArgumentException("Invalid name.");
        if(query == null) throw new IllegalArgumentException("Invalid query.");
        if(pivot == null || pivot.trim().length() == 0) throw new IllegalArgumentException("Invalid pivot.");
        queryNames.add(name);
        levels.add(query);
        pivots.add(pivot);
        propertyPivots.add(propertyPivot);
    }

    void addQuery(String name, Query query, String pivot) {
        addQuery(name, query, pivot, null);
    }

    @Override
    public int getLevels() {
        return levels.size();
    }

    @Override
    public boolean hasLevel(int level) {
        return level < levels.size();
    }

    @Override
    public String getName(int level) {
        return queryNames.get(level);
    }

    @Override
    public Query getQuery(int level) {
        return levels.get(level);
    }

    @Override
    public String getPivot(int level) {
        return pivots.get(level);
    }

    @Override
    public PropertyPivot getPropertyPivot(int level) {
        return propertyPivots.get(level);
    }

    @Override
    public void executeNestedQuery(QueryExecutor executor, ResultCollector collector, Map<String,String> args) {
        lastPivotValue.clear();
        collector.begin();
        processNextLevel(0, args, executor, collector);
        collector.end();
    }

    public void executeNestedQuery(QueryExecutor executor, ResultCollector collector) {
       executeNestedQuery(executor, collector, Collections.<String, String>emptyMap());
    }

    @Override
    public void processNextLevel(final int level, Map<String,String> args, QueryExecutor executor, ResultCollector collector) {
        final Result result;
        try {
            result = getQuery(level).perform(executor, args);
        } catch (IllegalArgumentException iae) {
            iae.printStackTrace();
            return;
        }

        collector.startLevel(level, getName(level));
        final String[] bindings = result.getBindings();
        String[] values;
        final String pivot = getPivot(level);
        final PropertyPivot propertyPivot = getPropertyPivot(level);
        for(;result.next();) {
            values = result.getValues();
            collector.values(values);
            final String pivotValue = getValue(bindings, values, pivot);
            if(lastPivotValue.size() < level + 1) {
                lastPivotValue.push(pivotValue);
                collector.pivot(pivotValue);
                processPivot(level + 1, bindings, values, executor, collector);
            } else if(!lastPivotValue.peek().equals(pivotValue)) {
                lastPivotValue.pop();
                lastPivotValue.push(pivotValue);
                collector.pivot(pivotValue);
                processPivot(level + 1, bindings, values, executor, collector);
            }
            //TODO: pivot e property pivot must coordinate
            if(propertyPivot != null) {
                final String propertyPivotValue = getValue(bindings, values, propertyPivot.p);
                if(propertyPivot.v.equals(propertyPivotValue)) {
                    collector.startPropertyPivot(propertyPivotValue);
                    final String[] newBindings = Arrays.copyOf(bindings, bindings.length + 1);
                    final String[] newValues = Arrays.copyOf(values, values.length + 1);
                    newBindings[newBindings.length - 1] = propertyPivot.remap;
                    newValues[newValues.length - 1] = getValue(bindings, values, propertyPivot.remapKey);
                    processPivot(level + 1, newBindings, newValues, executor, collector);
                    collector.endPropertyPivot(propertyPivotValue);
                }
            }
            collector.collect(bindings, values);
        }
        if(lastPivotValue.size() > level) lastPivotValue.pop();
        collector.endLevel(level);
    }

    private void processPivot(int level, String[] bindings, String[] values, QueryExecutor executor, ResultCollector collector) {
        if (!hasLevel(level)) return;
        final Map<String,String> nextArgs = bindArguments(bindings, values);
        processNextLevel(level, nextArgs, executor, collector);
    }

    private Map<String,String> bindArguments(String[] bindings, String[] values) {
        if(bindings.length != values.length) throw new IllegalArgumentException();
        final Map<String,String> out = new HashMap<>();
        for(int i = 0; i < bindings.length; i++) {
            out.put(bindings[i], values[i]);
        }
        return out;
    }

    private String getValue(String[] bindings, String[] values, String target) {
        for(int i = 0; i < bindings.length; i++) {
            if(bindings[i].equals(target)) {
                return values[i];
            }
        }
        throw new IllegalArgumentException();
    }

    private int indexOf(String[] list, String t) {
        for(int i = 0; i < list.length; i++) {
            if(list[i].equals(t)) return i;
        }
        throw new IllegalArgumentException();
    }

}
