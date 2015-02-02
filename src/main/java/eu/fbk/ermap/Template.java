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
import org.codehaus.jackson.JsonNode;

import java.io.IOException;
import java.util.ArrayList;
import java.util.Iterator;
import java.util.List;
import java.util.Map;
import java.util.Stack;

/**
 */
public class Template {

    private final JsonNode root;

    public Template(JsonNode root) {
        this.root = root;
    }

    public String getRootEntity() {
        return "xx";
    }

    public List<Operation> getOperations() {
        final List<Operation> out = new ArrayList<>();
        final Stack<String> context = new Stack<>();
        context.add(getRootEntity());
        getOperations(root, context, out);
        return out;
    }

    private void getOperations(JsonNode r, Stack<String> context, List<Operation> operations) {
        final Iterator<Map.Entry<String,JsonNode>> rIter = r.getFields();
        Map.Entry<String,JsonNode> entry;
        while (rIter.hasNext()) {
            entry = rIter.next();
            if(entry.getValue().isNull()) {
                operations.add(new ExpandElementOperation(context.peek(), entry.getKey()));
            } else {
                context.push(entry.getKey());
                if (entry.getValue().isObject()) {
                    operations.add(new OpenObjectOperation(context.peek()));
                    getOperations(entry.getValue(), context, operations);
                    operations.add(new CloseObjectOperation(context.peek()));
                } else if (entry.getValue().isArray()) {
                    throw new UnsupportedOperationException();
                } else {
                    throw new IllegalStateException();
                }
                context.pop();
            }
        }
    }

    public interface Operation {
        String getContext();
        void apply(JsonGenerator generator);
    }

    static class OpenArrayOperation implements Operation {
        private final String context;
        OpenArrayOperation(String context) {
            this.context = context;
        }
        @Override
        public String getContext() {
            return context;
        }
        @Override
        public void apply(JsonGenerator generator) {
            try {
                generator.writeStartArray();
            } catch (IOException e) {
                throw new RuntimeException(e);
            }
        }
        @Override
        public String toString() {
            return String.format("Open Array [%s]", getContext());
        }
    }

    static class CloseArrayOperation implements Operation {
        private final String context;
        CloseArrayOperation(String context) {
            this.context = context;
        }
        @Override
        public String getContext() {
            return context;
        }
        @Override
        public void apply(JsonGenerator generator) {
            try {
                generator.writeEndArray();
            } catch (IOException e) {
                throw new RuntimeException(e);
            }
        }
        @Override
        public String toString() {
            return String.format("Close Array [%s]", getContext());
        }
    }

    static class OpenObjectOperation implements Operation {
        private final String context;
        OpenObjectOperation(String context) {
            this.context = context;
        }
        @Override
        public String getContext() {
            return context;
        }
        @Override
        public void apply(JsonGenerator generator) {
            try {
                generator.writeStartObject();
            } catch (IOException e) {
                throw new RuntimeException(e);
            }
        }
        @Override
        public String toString() {
            return String.format("Open Object [%s]", getContext());
        }
    }

    static class CloseObjectOperation implements Operation {
        private final String context;
        CloseObjectOperation(String context) {
            this.context = context;
        }
        @Override
        public String getContext() {
            return context;
        }
        @Override
        public void apply(JsonGenerator generator) {
            try {
                generator.writeEndObject();
            } catch (IOException e) {
                throw new RuntimeException(e);
            }
        }
        @Override
        public String toString() {
            return String.format("Close Object [%s]", getContext());
        }
    }

    static class ExpandElementOperation implements Operation {
        private final String context;
        private final String element;
        ExpandElementOperation(String context, String element) {
            this.context = context;
            this.element = element;
        }
        @Override
        public String getContext() {
            return context;
        }
        public String getElement() {
            return element;
        }
        @Override
        public void apply(JsonGenerator generator) {
            // None
        }
        @Override
        public String toString() {
            return String.format("Expand element %s [%s]", getElement(), getContext());
        }
    }

}
