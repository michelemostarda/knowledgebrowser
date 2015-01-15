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
import java.util.Map;

/**
 * @author Michele Mostarda (mostarda@fbk.eu)
 */
public class DefaultQuery implements Query {

    private static final Logger logger = Logger.getLogger(DefaultQuery.class);

    private final String template;

    public DefaultQuery(String template) {
        this.template = template;
    }

    @Override
    public String getTemplate() {
        return template;
    }

    @Override
    public String expand(Map<String,String> args) {
        String out = template;
        for (Map.Entry<String,String> entry : args.entrySet()) {
            out = out.replace("$" + entry.getKey(), entry.getValue());
        }
        return out;
    }

    @Override
    public Result perform(QueryExecutor executor, Map<String,String> args) {
        checkArgs(args);
        ResultSet rs = executor.execSelect(expand(args));
        return new DefaultResult(rs);
    }

    @Override
    public String toString() {
        return template;
    }

    //TODO: replacement causes not matching URIs, replace with escaping.
    private void checkArgs(Map<String,String> args) {
        String arg, newArg;
        for(String k : args.keySet()) {
            arg = args.get(k);
            newArg = arg.replace("{", "").replace("}", "").replace("<", "").replace(">", "").replace("|", "").replace(" ", "");
            if(!arg.equals(newArg)) {
                logger.warn("Replaced " + arg + " with " + newArg);
                args.put(k, newArg);
            }
        }
    }

    private void checkVarExists(String v, String template) {
        final String t = String.format("$%s", v);
        if(!template.contains(t))
            throw new IllegalArgumentException(String.format("Variable '%s' not found in template '%s'", t, template));
    }

}
