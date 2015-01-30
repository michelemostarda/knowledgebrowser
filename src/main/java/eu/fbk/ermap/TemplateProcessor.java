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

package eu.fbk.ermap;

import org.codehaus.jackson.JsonGenerator;

import java.io.IOException;
import java.util.Map;

/**
 */
public class TemplateProcessor {

    public void process(
            Template template,
            Map<String,Entity> entities, Map<String,Relashionship> relashionships,
            QueryExecutor executor, JsonGenerator generator
    ) throws IOException {
        String rootEntity = template.getRootEntity();
        generator.writeStartObject();
        generator.writeFieldName(rootEntity);
        iterateTemplateEntities(template, entities, relashionships, generator);
        generator.writeEndObject();
    }

    private void iterateTemplateEntities(Template template, Map<String,Entity> entities, Map<String,Relashionship> relashionships, JsonGenerator generator) {
        for(Template.Operation operation : template.getOperations()) {
            if(operation instanceof Template.ExpandElementOperation) {
                final String element = ((Template.ExpandElementOperation) operation).getElement();
                final Entity entity = entities.get(element);
                if(entity != null) {
                    //
                    return;
                }
                final Relashionship relashionship = relashionships.get(element);
                if(relashionship != null) {
                    //
                    return;
                }
                throw new IllegalArgumentException("Invalid element in template: " + element);
            } else {
                operation.apply(generator);
            }
        }
    }

}
