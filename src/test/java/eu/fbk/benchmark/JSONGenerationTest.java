package eu.fbk.benchmark;/*
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

import com.google.common.collect.Iterators;
import com.hp.hpl.jena.query.Query;
import com.hp.hpl.jena.query.QueryExecution;
import com.hp.hpl.jena.query.QueryExecutionFactory;
import com.hp.hpl.jena.query.QueryFactory;
import com.hp.hpl.jena.query.QuerySolution;
import com.hp.hpl.jena.query.ResultSet;
import com.hp.hpl.jena.query.Syntax;
import com.hp.hpl.jena.rdf.model.Model;
import com.hp.hpl.jena.rdf.model.ModelFactory;
import org.codehaus.jackson.JsonFactory;
import org.codehaus.jackson.JsonGenerator;
import org.junit.Test;
import org.rdfhdt.hdt.hdt.HDT;
import org.rdfhdt.hdt.hdt.HDTManager;
import org.rdfhdt.hdtjena.HDTGraph;

import java.io.ByteArrayOutputStream;
import java.io.IOException;

/**
 * @author Michele Mostarda (mostarda@fbk.eu)
 */
public class JSONGenerationTest {

    @Test
    public void testGenerate() throws IOException {
        HDT hdt = HDTManager.mapIndexedHDT("/Users/hardest/Downloads/hdt-data/dblp-2012-11-28.hdt.gz", null);
        HDTGraph hdtGraph = new HDTGraph(hdt);
        Model model = ModelFactory.createModelForGraph(hdtGraph);

        Query query = QueryFactory.create("SELECT * {\n" +
                " ?agent a <http://xmlns.com/foaf/0.1/Agent>.\n" +
                " ?agent <http://www.w3.org/2000/01/rdf-schema#label> ?agentLabel.\n" +
                " ?agent <http://xmlns.com/foaf/0.1/homepage> ?agentHomepage.\n" +
                " ?document <http://purl.org/dc/elements/1.1/creator> ?agent.\n" +
                " \t?document <http://www.w3.org/2000/01/rdf-schema#label> ?documentLabel.\n" +
                " \t?document <http://xmlns.com/foaf/0.1/homepage> ?documentHomepage.\n" +
                " \t?document <http://purl.org/dc/terms/references> ?referreddoc.\n" +
                "\t\t?referreddoc  <http://www.w3.org/2000/01/rdf-schema#label> ?referredLabel.\n" +
                "\t\t?referreddoc <http://xmlns.com/foaf/0.1/homepage> ?referredHomepage.\n" +
                "}\n" +
                "ORDER BY ?agent ?document ?referrerDoc LIMIT 10", Syntax.syntaxSPARQL_11);

        QueryExecution qexec = QueryExecutionFactory.create(query, model);
        ResultSet results = qexec.execSelect();
        JsonFactory factory = new JsonFactory();
        ByteArrayOutputStream baos = new ByteArrayOutputStream();
        JsonGenerator generator = factory.createJsonGenerator(baos);
        generator.writeStartObject();
        String[] vars = null;
        for (; results.hasNext(); ) {
            QuerySolution soln = results.nextSolution();
            if(vars == null) vars = Iterators.toArray(soln.varNames(), String.class);
            for(String varName : vars) {
                generator.writeFieldName(varName);
                generator.writeString(soln.get(varName).toString());
            }
        }
        generator.writeEndObject();
        generator.flush();
        System.out.println(baos.toString());
    }

}
