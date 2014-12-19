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

import com.google.common.collect.Sets;

import java.io.Serializable;
import java.util.ArrayDeque;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.HashMap;
import java.util.HashSet;
import java.util.List;
import java.util.Map;
import java.util.Queue;
import java.util.Set;
import java.util.Stack;

/**
 * Provides functions to analyze path statistics.
 *
 * @author Michele Mostarda (mostarda@fbk.eu)
 */
public class PathAnalysis implements Serializable {

    private static String[] breadthFirst(String s, Edge[] edges) {
        Queue<String> q = new ArrayDeque<>();
        q.add(s);
        Set<String> visited = new HashSet<>();
        visited.add(s);
        while(!q.isEmpty()) {
            String current = q.poll();
            for(Edge e : edges) {
                if(current.equals(e.cLeft) && !visited.contains(e.cRight)) {
                    visited.add(e.cRight);
                    q.add(e.cRight);
                } else if(current.equals(e.cRight) && !visited.contains(e.cLeft)) {
                    visited.add(e.cLeft);
                       q.add(e.cLeft);
                }
            }
        }
        return visited.toArray(new String[visited.size()]);
    }

    protected static List<List<Edge>> buildPathDepthFirst(String s, List<Edge> edges) {
        if(edges.isEmpty()) return new ArrayList<>();
        final Stack<String> stack = new Stack<>();
        stack.push(s);
        final List<List<Edge>> out = new ArrayList<>();
        final Map<String, List<Edge>> concurrents = new HashMap<>();
        final Set<Edge> visited = new HashSet<>();
        while(!stack.isEmpty() && out.size() != edges.size()) {
            String peek = stack.peek();
            boolean added = false;
            for(Edge edge : edges) {
                if (edge.cLeft.equals(peek) && !visited.contains(edge)) {
                    List<Edge> concurrent = concurrents.get(peek);
                    if(concurrent == null) {
                        concurrent = new ArrayList<>();
                        concurrents.put(peek, concurrent);
                        out.add(concurrent);
                    }
                    concurrent.add(edge);
                    visited.add(edge);
                    stack.push(edge.cRight);
                    added = true;
                    break;
                } else if (edge.cRight.equals(peek) && !visited.contains(edge)) {
                    List<Edge> concurrent = concurrents.get(peek);
                    if (concurrent == null) {
                        concurrent = new ArrayList<>();
                        concurrents.put(peek, concurrent);
                        out.add(concurrent);
                    }
                    concurrent.add(edge);
                    visited.add(edge);
                    stack.push(edge.cLeft);
                    added = true;
                    break;
                }
            }
            if(!added) stack.pop();
        }
        return out;
    }

    private final List<Edge> edges = new ArrayList<>();

    public String[] getNodes() {
        final List<String> nodes = new ArrayList<>();
        for(Edge edge : edges) {
            nodes.add(edge.cLeft);
            nodes.add(edge.cRight);
        }
        return nodes.toArray(new String[nodes.size()]);
    }

    public Edge[] getEdges() {
        return edges.toArray(new Edge[edges.size()]);
    }

    public Edge[] getIn(String node) {
        final List<Edge> nodes = new ArrayList<>();
        for (Edge property : edges) {
            if(property.cRight.equals(node)) {
                nodes.add(property);
            }
        }
        return nodes.toArray(new Edge[nodes.size()]);
    }

    public Edge[] getOut(String node) {
        final List<Edge> nodes = new ArrayList<>();
        for (Edge property : edges) {
            if(property.cLeft.equals(node)) {
                nodes.add(property);
            }
        }
        return nodes.toArray(new Edge[nodes.size()]);
    }

    public Edge[] getProperties(String left, String right) {
        final List<Edge> nodes = new ArrayList<>();
        for (Edge property : edges) {
            if (property.cLeft.equals(left) && property.cRight.equals(right)) {
                nodes.add(property);
            }
        }
        return nodes.toArray(new Edge[nodes.size()]);
    }

    public Edge[] getMaxSpanningTree() {
        final Edge[] edges = getEdges();
        Arrays.sort(edges);
        final Set<String> coveredNodes = new HashSet<>();
        final List<Edge> out = new ArrayList<>();
        for(Edge edge : edges) {
            if(!coveredNodes.contains(edge.cLeft) || !coveredNodes.contains(edge.cRight)) {
                out.add(edge);
                coveredNodes.add(edge.cLeft);
                coveredNodes.add(edge.cRight);
            }
        }
        return out.toArray(new Edge[out.size()]);
    }

    public boolean isConnected(Edge[] edges) {
        final Set<String> visited = new HashSet<>(Arrays.asList(breadthFirst(edges[0].cLeft, edges)));
        final Set<String> allNodes = new HashSet<>(Arrays.asList(getNodes()));
        final Set<String> difference = Sets.difference(allNodes, visited);
        return difference.isEmpty();
    }

    public Property[] toPropertyPath(Edge[] edges) {
        if(!isConnected(edges)) throw new IllegalArgumentException("Edges must be connected.");

        final List<List<Edge>> sortedEdges = buildPathDepthFirst(edges[0].cLeft, new ArrayList<>(Arrays.asList(edges)));
        System.out.println(sortedEdges.toString());
//        final Property[] out = new Property[sortedEdges.size()];
//        int i = 0;
//        String lastRight = edges[0].cRight;
//        for(Edge edge : sortedEdges) {
//            boolean inverted = !lastRight.equals(edge.cLeft);
//            out[i++] = new Property(edge.property, inverted);
//            lastRight = inverted ? edge.cRight : edge.cLeft;
//        }
//        return out;
        return null;
    }

    protected void add(String property, int occurrences, String classLeft, String classRight) {
        edges.add(new Edge(property, occurrences, classLeft, classRight));
    }

}
