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
import org.apache.log4j.Logger;

import java.util.Arrays;

/**
 * @author Michele Mostarda (mostarda@fbk.eu)
 */
public class DefaultQuery implements Query {

    private static final Logger logger = Logger.getLogger(DefaultQuery.class);

    private final String template;
    private final String[] inVariables;

    public DefaultQuery(String template, String[] inVariables) {
        if(inVariables == null) throw new IllegalArgumentException("input vars array cannot be null.");
        for(String v : inVariables) {
            checkVarExists(v, template);
        }
        this.template = template;
        this.inVariables = inVariables;
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
    public String expand(String... args) {
        if (inVariables.length != args.length)
            throw new IllegalArgumentException("Unexpected number of arguments.");
        String out = template;
        for (int i = 0; i < inVariables.length; i++) {
            out = out.replace("$" + inVariables[i], args[i]);
        }
        return out;
    }

    @Override
    public Result perform(QueryExecutor executor, String... args) {
        checkArgs(args);
        ResultSet rs = executor.execSelect(expand(args));
        return new DefaultResult(rs);
    }

    @Override
    public String toString() {
        return String.format("%s in: %s", template, Arrays.toString(inVariables));
    }

    //TODO: replacement causes not matching URIs, replace with escaping.
    private void checkArgs(String[] args) {
        String newArg;
        for(int i = 0; i < args.length; i++) {
            newArg = args[i].replace("{", "").replace("}", "").replace("<", "").replace(">", "").replace("|", "").replace(" ", "");
            if(!args[i].equals(newArg)) {
                logger.warn("Replaced " + args[i] + " with " + newArg);
                args[i] = newArg;
            }
        }
    }

    private void checkVarExists(String v, String template) {
        final String t = String.format("$%s", v);
        if(!template.contains(t))
            throw new IllegalArgumentException(String.format("Variable '%s' not found in template '%s'", t, template));
    }

}
