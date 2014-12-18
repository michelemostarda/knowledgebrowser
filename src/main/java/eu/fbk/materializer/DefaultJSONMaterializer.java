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
import java.util.List;

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
        for (String[] propertyValue : propertyValues(instance)) {
            String propertyName = propertyValue[0];
            generator.writeFieldName(propertyName + (properties.contains(propertyName) ? "__" : ""));
            generator.writeString(propertyValue[1]);
        }

        if (properties.size() > 0) {
            Property nextJump = properties.get(0);
            List<String> adiacentInstances = adiacentInstances(instance, nextJump);
            generator.writeFieldName(nextJump.getPropertyURL());
            generator.writeStartArray();
            for (String adiacentInstance : adiacentInstances) {
                materialize(adiacentInstance, level + 1, properties.subList(1, properties.size()), generator);
            }
            generator.writeEndArray();
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
        final String qryStr = String.format(
                "SELECT DISTINCT ?i (COUNT(?i) AS ?count) " +
                (property.isRevert() ? "WHERE {?o <%s> ?i } " : "WHERE {?i <%s> ?o } ") +
                "GROUP BY ?i " +
                "ORDER BY DESC(?count) " +
                "LIMIT 10",
                property.getPropertyURL()
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

    private List<String> adiacentInstances(String instance, Property property) {
        String qryStr = property.isRevert()
                ?
                String.format("SELECT ?i WHERE { ?i <%s> <%s> }", property.getPropertyURL(), instance)
                :
                String.format("SELECT ?i WHERE { <%s> <%s> ?i }", instance, property.getPropertyURL());
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
        try {
            File in = new File(queryId + ".ser");
            if(!in.exists()) return null;
            FileInputStream fis = new FileInputStream(in);
            ObjectInputStream ois = new ObjectInputStream(fis);
            Object out = ois.readObject();
            ois.close();
            fis.close();
            return (T) out;
        } catch (Exception e) {
            throw new RuntimeException("Error while deserializing result", e);
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
