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

import org.codehaus.jackson.JsonNode;
import org.codehaus.jackson.JsonParser;
import org.codehaus.jackson.map.ObjectMapper;

import java.io.IOException;
import java.io.InputStream;

/**
 * Builds a {@link eu.fbk.querytemplate.NestedQuery} from a JSON config file.
 *
 * @author Michele Mostarda (mostarda@fbk.eu)
 */
public class NestedQueryBuilder {

    NestedQuery build(JsonNode root) {
        final JsonNode levels = root.get("levels");
        if(!levels.isArray()) throw new IllegalArgumentException("Levels field must be an array");
        final DefaultNestedQuery defaultNestedQuery = new DefaultNestedQuery();
        for(JsonNode queryNode : levels) {
            defaultNestedQuery.addQuery(getName(queryNode), processQuery(queryNode), getPivot(queryNode));
        }
        return defaultNestedQuery;
    }

    NestedQuery build(InputStream is) throws IOException {
        ObjectMapper mapper = new ObjectMapper();
        final JsonParser parser = mapper.getJsonFactory().createJsonParser(is);
        return build(parser.readValueAsTree());
    }

    private String getName(JsonNode queryNode) {
        return queryNode.get("name").asText();
    }

    private String getPivot(JsonNode queryNode) {
        return queryNode.get("pivot").asText();
    }

    private Query processQuery(JsonNode queryNode) {
        final String template = queryNode.get("query").asText();
        return new DefaultQuery(template);
    }

    private String[] toStringArray(JsonNode list) {
        if(!list.isArray()) throw new IllegalArgumentException();
        final String[] out = new String[list.size()];
        int i = 0;
        for(JsonNode e : list) {
            out[i++] = e.asText();
        }
        return out;
    }


}
