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

import com.hp.hpl.jena.query.QuerySolution;
import com.hp.hpl.jena.query.ResultSet;

/**
 * @author Michele Mostarda (mostarda@fbk.eu)
 */
public class DefaultResult implements Result {

    private final ResultSet rs;

    private String[] bindings;
    private QuerySolution sltn;

    DefaultResult(ResultSet rs) {
        this.rs = rs;
    }

    @Override
    public String[] getBindings() {
        if(bindings == null) {
            bindings = rs.getResultVars().toArray(new String[0]);
        }
        return bindings;
    }

    @Override
    public boolean next() {
        if(rs.hasNext()) {
            sltn = rs.next();
            return true;
        }
        return false;
    }

    @Override
    public String getValue(String binding) {
        return sltn.get(binding).toString();
    }

    @Override
    public String[] getValues() {
        final String[] out = new String[getBindings().length];
        int i = 0;
        for(String binding : getBindings()) {
            out[i++] = sltn.get(binding).toString();
        }
        return out;
    }
}
