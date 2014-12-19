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

package eu.fbk.materializer;

/**
 * @author Michele Mostarda (mostarda@fb.eu)
 */
public class Property {

    private final Edge edge;
    private final boolean revert;

    public Property(Edge edge, boolean revert) {
        this.edge = edge;
        this.revert = revert;
    }

    public Property(Edge edge) {
        this(edge, false);
    }

    public Edge getEdge() {
        return edge;
    }

    public boolean isRevert() {
        return revert;
    }

}
