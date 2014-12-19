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

import com.hp.hpl.jena.query.Query;
import com.hp.hpl.jena.query.QueryExecution;
import com.hp.hpl.jena.query.QueryExecutionFactory;
import com.hp.hpl.jena.query.QueryFactory;
import com.hp.hpl.jena.query.QuerySolution;
import com.hp.hpl.jena.query.ResultSet;
import com.hp.hpl.jena.query.Syntax;
import com.hp.hpl.jena.rdf.model.Model;
import com.hp.hpl.jena.rdf.model.ModelFactory;
import org.codehaus.jackson.JsonGenerator;
import org.rdfhdt.hdt.hdt.HDT;
import org.rdfhdt.hdt.hdt.HDTManager;
import org.rdfhdt.hdtjena.HDTGraph;

import java.io.File;
import java.io.FileInputStream;
import java.io.FileOutputStream;
import java.io.IOException;
import java.io.ObjectInputStream;
import java.io.ObjectOutputStream;
import java.security.MessageDigest;
import java.util.ArrayList;
import java.util.HashSet;
import java.util.List;
import java.util.Set;

/**
 * @author Michele Mostarda (mostarda@fbk.eu)
 */
public class DefaultJSONMaterializer implements JSONMaterializer {

    private final File hdtFile;
    private Model model;

    public DefaultJSONMaterializer(String hdtFile) {
        this.hdtFile = new File(hdtFile);
        if(!this.hdtFile.exists()) throw new IllegalArgumentException();
    }

    public PathAnalysis getPathAnalysis() {
        final String qryStr =
                "SELECT ?p ?t1 ?t2 (COUNT(?p) AS ?pcount) " +
                "WHERE {?s1 a ?t1. ?s2 a ?t2. ?s1 ?p ?s2} " +
                "GROUP BY ?p ?pcount ?t1 ?t2 " +
                "ORDER BY DESC(?pcount)";
        final String cacheFileName = "frequent-paths-" + md5(qryStr);
        final PathAnalysis cached = getCachedResults(cacheFileName);
        if(cached != null) return cached;

        Query qry = QueryFactory.create(qryStr, Syntax.syntaxSPARQL_11);
        QueryExecution qexec = QueryExecutionFactory.create(qry, getModel());
        ResultSet results = qexec.execSelect();
        final PathAnalysis propertyTree = new PathAnalysis();
        QuerySolution solution;
        while (results.hasNext()) {
            solution = results.next();
            propertyTree.add(
                    solution.get("?p").asResource().getURI(),
                    solution.get("?pcount").asLiteral().getInt(),
                    solution.get("?t1").asResource().getURI(),
                    solution.get("?t2").asResource().getURI()
            );
        }
        cacheResults(cacheFileName, propertyTree);
        return propertyTree;
    }

    @Override
    public void materialize(List<Property> properties, JsonGenerator generator) throws JsonMaterializerException {
        try {
            List<String> instances = moreFrequentInstances(properties.get(0));
            generator.writeStartObject();
            for (String instance : instances) {
                generator.writeFieldName(instance);
                materialize(instance, 0, properties, generator);
            }
            generator.writeEndObject();
        } catch (Exception e) {
            throw new JsonMaterializerException("Error while materializing JSON", e);
        }
    }

    private void materialize(String instance, int level, List<Property> properties, JsonGenerator generator) throws IOException {
        generator.writeStartObject();
        if(level > 0) {
            generator.writeFieldName("@instance");
            generator.writeString(instance);
        }
        final Set<String> propertyURLs = new HashSet<>();
        for(Property property : properties) {
            for(Edge edge : property.getEdges()) {
                propertyURLs.add(edge.property);
            }
        }
        for (String[] propertyValue : propertyValues(instance)) {
            String propertyName = propertyValue[0];
            generator.writeFieldName(propertyName + (propertyURLs.contains(propertyName) ? "__" : ""));
            generator.writeString(propertyValue[1]);
        }

        if (properties.size() > 0) {
            Property nextJump = properties.get(0);
            for(Edge edge : nextJump.getEdges()) {
                List<String> adiacentInstances = adiacentInstances(instance, edge, nextJump.isRevert());
                generator.writeFieldName(edge.property);
                generator.writeStartArray();
                for (String adiacentInstance : adiacentInstances) {
                    materialize(adiacentInstance, level + 1, properties.subList(1, properties.size()), generator);
                }
                generator.writeEndArray();
            }
        }
        generator.writeEndObject();
    }

    private Model getModel() {
        if(model == null) {
            final HDT hdt;
            try {
                hdt = HDTManager.loadIndexedHDT(hdtFile.getAbsolutePath(), null);
            } catch (IOException ioe) {
                throw new RuntimeException("Error while loading data.", ioe);
            }
            HDTGraph hdtGraph = new HDTGraph(hdt);
            model = ModelFactory.createModelForGraph(hdtGraph);
        }
        return model;
    }

    private List<String> moreFrequentInstances(Property property) {
        final Edge e = property.getHeaviestEdge();
        final String qryStr = String.format(
                "SELECT DISTINCT ?i (COUNT(?i) AS ?count) " +
                (property.isRevert() ?
                        String.format("{?i a <%s>. ?s a <%s>. ?s <%s> ?i}", e.cRight, e.cLeft, e.property)
                        :
                        String.format("{?i a <%s>. ?o a <%s>. ?i <%s> ?o}", e.cLeft, e.cRight, e.property)
                ) +
                "GROUP BY ?i " +
                "ORDER BY DESC(?count) " +
                "LIMIT 10",
                e.cLeft,
                e.cRight,
                e.property
        );
        final String cacheFileName = "frequent-instances-" + md5(qryStr);
        final List<String> cached = getCachedResults(cacheFileName);
        if(cached != null) return cached;
        Query qry = QueryFactory.create(qryStr, Syntax.syntaxSPARQL_11);
        QueryExecution qexec = QueryExecutionFactory.create(qry, getModel());
        ResultSet results = qexec.execSelect();
        List<String> out = new ArrayList<>();
        QuerySolution solution;
        while (results.hasNext()) {
            solution = results.next();
            out.add(solution.get("?i").toString());
        }
        cacheResults(cacheFileName, out);
        return out;
    }

    private List<String[]> propertyValues(String instance) {
        Query qry = QueryFactory.create(
                String.format("SELECT ?p ?o WHERE { <%s> ?p ?o }", instance),
                Syntax.syntaxSPARQL_11
        );
        QueryExecution qexec = QueryExecutionFactory.create(qry, getModel());
        ResultSet results = qexec.execSelect();
        QuerySolution solution;
        List<String[]> out = new ArrayList<>();
        while(results.hasNext()) {
            solution = results.next();
            out.add(new String[]{solution.get("?p").toString(), solution.get("?o").toString()});
        }
        return out;
    }

    private List<String> adiacentInstances(String instance, Edge e, boolean isRevert) {
        String qryStr = "SELECT ?i " +
                (isRevert ?
                String.format("{?i a <%s>. ?i <%s> <%s>}", e.cLeft, e.property, instance)
                :
                String.format("{?i a <%s>. <%s> <%s> ?i}", e.cRight, instance, e.property)

                );
        Query qry = QueryFactory.create(qryStr, Syntax.syntaxSPARQL_11);
        QueryExecution qexec = QueryExecutionFactory.create(qry, getModel());
        ResultSet results = qexec.execSelect();
        QuerySolution solution;
        List<String> out = new ArrayList<>();
        while (results.hasNext()) {
            solution = results.next();
            out.add(solution.get("?i").toString());
        }
        return out;
    }

    private <T> void cacheResults(String queryId, T data) {
        try {
            FileOutputStream fileOut = new FileOutputStream(queryId + ".ser");
            ObjectOutputStream out = new ObjectOutputStream(fileOut);
            out.writeObject(data);
            out.close();
            fileOut.close();
        } catch (IOException ioe) {
            throw new RuntimeException("Error while serializing result", ioe);
        }
    }

    private <T> T getCachedResults(String queryId) {
        File in = new File(queryId + ".ser");
        try {
            if(!in.exists()) return null;
            FileInputStream fis = new FileInputStream(in);
            ObjectInputStream ois = new ObjectInputStream(fis);
            Object out = ois.readObject();
            ois.close();
            fis.close();
            return (T) out;
        } catch (Exception e) {
            throw new RuntimeException("Error while deserializing result from file " + in.getAbsolutePath(), e);
        }
    }

    private String md5(String in) {
        try {
            MessageDigest md = java.security.MessageDigest.getInstance("MD5");
            byte[] array = md.digest(in.getBytes());
            StringBuffer sb = new StringBuffer();
            for (int i = 0; i < array.length; ++i) {
                sb.append(Integer.toHexString((array[i] & 0xFF) | 0x100).substring(1, 3));
            }
            return sb.toString();
        } catch (java.security.NoSuchAlgorithmException e) {
            throw new IllegalStateException();
        }
    }
}
