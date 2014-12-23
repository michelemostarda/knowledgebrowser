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

import java.util.Arrays;

/**
 * Defines a single level in the navigation path.
 *
 * @author Michele Mostarda (mostarda@fbk.eu)
 */
public class Level {

    private final Edge[] edges;
    private final boolean revert;

    public Level(boolean revert, Edge... edges) {
        if(edges == null || edges.length == 0) throw new IllegalArgumentException();
        String l = edges[0].cLeft;
        String r = edges[0].cRight;
        boolean leftEquals = true, rightEquals = true;
        for(Edge e : edges) {
            if(!e.cLeft.equals(l)) leftEquals = false;
            if(!e.cRight.equals(r)) rightEquals = false;
        }
        if(!leftEquals && !rightEquals)
            throw new IllegalArgumentException("All parallel edges must be oriented the same");

        this.revert = revert;
        this.edges = edges;
    }

    public Level(Edge... edges) {
        this(false, edges);
    }

    public boolean isRevert() {
        return revert;
    }

    public Edge[] getEdges() {
        return edges;
    }

    public Edge getHeaviestEdge() {
        int max = Integer.MIN_VALUE;
        Edge target = null;
        for(Edge e : edges) {
            if(max < e.occurrences) {
                max = e.occurrences;
                target = e;
            }
        }
        return target;
    }

    @Override
    public String toString() {
        return String.format("%b %s", revert, Arrays.toString(edges));
    }
}
