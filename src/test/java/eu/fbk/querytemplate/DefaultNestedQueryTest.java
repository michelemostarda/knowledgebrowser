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

import java.io.ByteArrayOutputStream;
import java.io.File;
import java.io.IOException;
import java.io.OutputStreamWriter;
import java.io.PrintWriter;

/**
 * @author Michele Mostarda (mostarda@fbk.eu)
 */
public class DefaultNestedQueryTest {

    @Test
    public void processQueryWithPrinterCollector() throws IOException {
        PrintWriter pw = new PrintWriter(System.out);
        processQuery( new PrintResultCollector(pw) );
    }

    @Test
    public void processQueryWithJSONCollector() throws IOException {
        final JsonFactory factory = new JsonFactory();
        final ByteArrayOutputStream baos = new ByteArrayOutputStream();
        final JsonGenerator generator = factory.createJsonGenerator(new OutputStreamWriter(baos));
        processQuery(new JSONResultCollector(generator, "p:o"));
        generator.flush();

        Assert.assertEquals(
                JSONUtils.parseJSON(this.getClass().getResourceAsStream("nested-query-result.json")),
                JSONUtils.parseJSON(baos.toString())
        );
    }


    private void processQuery(ResultCollector collector) throws IOException {
        final DefaultNestedQuery nestedQuery = new DefaultNestedQuery();
        nestedQuery.addQuery(
                "articles",
                new DefaultQuery(
                        "SELECT * {?Article a <$Type>. ?Article ?p ?o } LIMIT 100"
                        , new String[]{"Type"}, new String[]{"Article", "p", "o"}
                ),
                "Article"
        );
        nestedQuery.addQuery(
                "agents",
                new DefaultQuery(
                        "SELECT * {?Agent a <http://xmlns.com/foaf/0.1/Agent>. <$Article> <http://purl.org/dc/elements/1.1/creator> ?Agent. ?Agent ?p ?o}",
                        new String[]{"Article"}, new String[]{"Agent", "p", "o"}
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

}
