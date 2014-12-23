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

import com.hp.hpl.jena.query.ResultSet;

/**
 * @author Michele Mostarda (mostarda@fbk.eu)
 */
public class DefaultQuery implements Query {

    private final String template;
    private final String[] inVariables;
    private final String[] outBindings;

    public DefaultQuery(String template, String[] inVariables, String[] outBindings) {
        this.template = template;
        this.inVariables = inVariables;
        this.outBindings = outBindings;
    }

    @Override
    public String getTemplate() {
        return template;
    }

    @Override
    public String[] getInVariables() {
        return inVariables;
    }

    @Override
    public String[] getOutBindings() {
        return outBindings;
    }

    @Override
    public String expand(String... args) {
        if (inVariables.length != args.length) throw new IllegalArgumentException();
        String out = template;
        for (int i = 0; i < inVariables.length; i++) {
            out = out.replace("$" + inVariables[i], args[i]);
        }
        return out;
    }

    @Override
    public Result perform(QueryExecutor executor, String... args) {
        ResultSet rs = executor.execSelect(expand(args));
        return new DefaultResult(rs);
    }

}
