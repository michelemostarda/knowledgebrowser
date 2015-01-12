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
import java.util.List;
import java.util.Stack;

/**
 * @author Michele Mostarda (mostarda@fbk.eu)
 */
public class DefaultNestedQuery implements NestedQuery {

    private final List<String> queryNames = new ArrayList<>();
    private final List<Query> levels = new ArrayList<>();
    private final List<String> pivots = new ArrayList<>();
    private final Stack<String> lastPivotValue = new Stack<>();

    void addQuery(String name, Query query, String pivot) {
        if(name == null || name.trim().length() == 0) throw new IllegalArgumentException("Invalid name.");
        if(query == null) throw new IllegalArgumentException("Invalid query.");
        if(pivot == null || pivot.trim().length() == 0) throw new IllegalArgumentException("Invalid pivot.");
        queryNames.add(name);
        levels.add(query);
        pivots.add(pivot);
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
    public void executeNestedQuery(QueryExecutor executor, ResultCollector collector, String... args) {
        lastPivotValue.clear();
        collector.begin();
        processNextLevel(0, args, executor, collector);
        collector.end();
    }

    @Override
    public void processNextLevel(final int level, String[] args, QueryExecutor executor, ResultCollector collector) {
        collector.startLevel(level, getName(level), args);
        final Result result = getQuery(level).perform(executor, args);
        final String[] bindings = result.getBindings();
        String[] values;
        final String pivot = getPivot(level);
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
            collector.collect(bindings, values);
        }
        lastPivotValue.pop();
        collector.endLevel(level);
    }

    private void processPivot(int level, String[] bindings, String[] values, QueryExecutor executor, ResultCollector collector) {
        if (!hasLevel(level)) return;
        final Query nextQuery = getQuery(level);
        final String[] nextArgs = bindArguments(bindings, values, nextQuery.getInVariables());
        processNextLevel(level, nextArgs, executor, collector);
    }

    private String[] bindArguments(String[] bindings, String[] values, String[] inVariables) {
        String[] out = new String[inVariables.length];
        for(int i = 0; i < inVariables.length; i++) {
            out[i] = values[indexOf(bindings, inVariables[i])];
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
