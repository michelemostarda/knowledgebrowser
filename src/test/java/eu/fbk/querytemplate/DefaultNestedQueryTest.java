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

import eu.fbk.JSONUtils;
import org.codehaus.jackson.JsonFactory;
import org.codehaus.jackson.JsonGenerator;
import org.junit.Assert;
import org.junit.Test;

import java.io.BufferedOutputStream;
import java.io.ByteArrayOutputStream;
import java.io.File;
import java.io.FileOutputStream;
import java.io.IOException;
import java.io.OutputStream;
import java.io.OutputStreamWriter;
import java.io.PrintWriter;
import java.util.zip.GZIPOutputStream;

/**
 * @author Michele Mostarda (mostarda@fbk.eu)
 */
public class DefaultNestedQueryTest {

    @Test
    public void testArticleAgentSysOut() throws IOException {
        PrintWriter pw = new PrintWriter(System.out);
        processArticleAgentQuery(new PrintResultCollector(pw), 100);
    }

    @Test
    public void testArticleAgentJSONLimit() throws IOException {
        final JsonFactory factory = new JsonFactory();
        final ByteArrayOutputStream baos = new ByteArrayOutputStream();
        final JsonGenerator generator = factory.createJsonGenerator(new OutputStreamWriter(baos));
        processArticleAgentQuery(new JSONResultCollector(generator, "p:o"), 100);
        generator.flush();

        Assert.assertEquals(
                JSONUtils.parseJSON(this.getClass().getResourceAsStream("nested-query1-result.json")),
                JSONUtils.parseJSON(baos.toString())
        );
    }

    @Test
    public void testDocumentArticleAgentJSONLimit() throws IOException {
        final JsonFactory factory = new JsonFactory();
        final ByteArrayOutputStream baos = new ByteArrayOutputStream();
        final JsonGenerator generator = factory.createJsonGenerator(new OutputStreamWriter(baos));
        processDocumentArticleAgentQuery(new JSONResultCollector(generator, "p:o"), 10);
        generator.flush();

        Assert.assertEquals(
                JSONUtils.parseJSON(this.getClass().getResourceAsStream("nested-query2-result.json")),
                JSONUtils.parseJSON(baos.toString())
        );
    }

    // process time: 5m 5s  out file: 200MB out1.json.gz
    @Test
    public void testArticleAgentJSONFull() throws IOException {
        final JsonFactory factory = new JsonFactory();
        final File jsonFile = new File("out1.json.gz");
        final OutputStream os = new GZIPOutputStream(new BufferedOutputStream((new FileOutputStream(jsonFile))));
        final JsonGenerator generator = factory.createJsonGenerator(os);
        processArticleAgentQuery(new JSONResultCollector(generator, "p:o"), 2);
        generator.flush();
        os.close();
        Assert.assertTrue(jsonFile.length() >= 1024 * 1024 * 200);
    }

    // process time: ?  out file: ? out2.json.gz
    @Test
    public void testDocumentArticleAgentJSONFull() throws IOException {
        final JsonFactory factory = new JsonFactory();
        final File jsonFile = new File("out2.json.gz");
        final OutputStream os = new GZIPOutputStream(new BufferedOutputStream((new FileOutputStream(jsonFile))));
        final JsonGenerator generator = factory.createJsonGenerator(os);
        processArticleAgentQuery(new JSONResultCollector(generator, "p:o"), 2);
        generator.flush();
        os.close();
    }

    private void processArticleAgentQuery(ResultCollector collector, Integer limit) throws IOException {
        final DefaultNestedQuery nestedQuery = new DefaultNestedQuery();
        nestedQuery.addQuery(
                "articles",
                new DefaultQuery(
                        String.format("SELECT * {?Article a <$Type>. ?Article ?p ?o } %s", limit == null ? "" : "LIMIT " + limit),
                        new String[]{"Type"}
                ),
                "Article"
        );
        nestedQuery.addQuery(
                "agents",
                new DefaultQuery(
                        "SELECT * {?Agent a <http://xmlns.com/foaf/0.1/Agent>. <$Article> <http://purl.org/dc/elements/1.1/creator> ?Agent. ?Agent ?p ?o}",
                        new String[]{"Article"}
                ),
                "Agent"
        );
        final QueryExecutor executor = new DefaultQueryExecutor(new File("hdt-data/dblp-2012-11-28.hdt.gz"));

        nestedQuery.executeNestedQuery(
                executor,
                collector,
                "http://swrc.ontoware.org/ontology#Article"
        );
    }

    private void processDocumentArticleAgentQuery(ResultCollector collector, Integer limit) throws IOException {
        final DefaultNestedQuery nestedQuery = new DefaultNestedQuery();
        nestedQuery.addQuery(
                "documents",
                new DefaultQuery(
                        String.format("SELECT * { ?Article <http://purl.org/dc/terms/references> ?Document } %s", limit == null ? "" : "LIMIT " + limit),
                        new String[]{}
                ),
                "Document"
        );
        nestedQuery.addQuery(
                "articles",
                new DefaultQuery(
                        "SELECT (<$Article> as ?Article) ?p ?o WHERE {<$Article> ?p ?o }",
                        new String[]{"Article"}
                ),
                "Article"
        );
        nestedQuery.addQuery(
                "agents",
                new DefaultQuery(
                        "SELECT * {?Agent a <http://xmlns.com/foaf/0.1/Agent>. <$Article> <http://purl.org/dc/elements/1.1/creator> ?Agent. ?Agent ?p ?o}",
                        new String[]{"Article"}
                ),
                "Agent"
        );
        final QueryExecutor executor = new DefaultQueryExecutor(new File("hdt-data/dblp-2012-11-28.hdt.gz"));

        nestedQuery.executeNestedQuery(
                executor,
                collector
        );
    }

}
