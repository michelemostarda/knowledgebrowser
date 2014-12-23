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

/**
 * @author Michele Mostarda (mostarda@fbk.eu)
 */
public class DefaultNestedQuery implements NestedQuery {

    private final Query[] levels;

    public DefaultNestedQuery(Query... levels) {
        this.levels = levels;
    }

    @Override
    public int getLevels() {
        return levels.length;
    }

    @Override
    public boolean hasLevel(int level) {
        return level < levels.length;
    }

    @Override
    public Query getQuery(int level) {
        return levels[level];
    }

    @Override
    public void executeQueryOnBindings(final int level, Result result, QueryExecutor executor, ResultCollector collector) {
        final String[] bindings = result.getBindings();
        String[] values;
        for(;result.next();) {
            values = result.getValues();
            collector.result(bindings, values);
            final Query levelQuery = getQuery(level);
            final Result partial = levelQuery.perform(executor, bindArguments(bindings, values, levelQuery.getInVariables()));
            collector.startLevel(level);
            for(;partial.next();) {
                collector.collect(partial.getBindings(), partial.getValues());
                final int nextLevel = level + 1;
                if(hasLevel(nextLevel)) {
                    collector.startLevel(nextLevel);
                    executeQueryOnBindings(nextLevel, partial, executor, collector);
                    collector.endLevel(nextLevel);
                }
            }
            collector.endLevel(level);
        }
    }

    @Override
    public void executeNestedQuery(QueryExecutor executor, ResultCollector collector, String... args) {
        collector.begin();
        collector.startLevel(0);
        final Query levelOQuery = getQuery(0);
        final Result result = levelOQuery.perform(executor, args);
        if(hasLevel(1))  executeQueryOnBindings(1, result, executor, collector);
        collector.endLevel(0);
        collector.end();
    }

    private String[] bindArguments(String[] bindings, String[] values, String[] inVariables) {
        String[] out = new String[inVariables.length];
        for(int i = 0; i < inVariables.length; i++) {
            out[i] = values[indexOf(bindings, inVariables[i])];
        }
        return out;
    }

    private int indexOf(String[] list, String t) {
        for(int i = 0; i < list.length; i++) {
            if(list[i].equals(t)) return i;
        }
        throw new IllegalArgumentException();
    }


}
