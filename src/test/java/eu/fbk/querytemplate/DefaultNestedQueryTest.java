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

import org.codehaus.jackson.JsonFactory;
import org.codehaus.jackson.JsonGenerator;
import org.junit.Test;

import java.io.File;
import java.io.FileOutputStream;
import java.io.IOException;
import java.io.PrintWriter;

/**
 * @author Michele Mostarda (mostarda@fbk.eu)
 */
public class DefaultNestedQueryTest {

    @Test
    public void testNested() throws IOException {
        final DefaultNestedQuery nestedQuery = new DefaultNestedQuery();
        nestedQuery.addQuery(
            "agents",
            new DefaultQuery(
                    "SELECT ?Agent {?Agent a <$Type>}",
                    new String[]{"Type"}, new String[]{"Agent"}
            )
        );
        nestedQuery.addQuery(
            "articles-per-agent",
            new DefaultQuery(
                    "SELECT * {?Article a <http://swrc.ontoware.org/ontology#Article>. ?Article <http://purl.org/dc/elements/1.1/creator> <$Agent>. ?Article ?p ?o }"
                    , new String[]{"Agent"}, new String[]{"p", "o"}
            )
        );
        final QueryExecutor executor = new DefaultQueryExecutor(new File("hdt-data/dblp-2012-11-28.hdt.gz"));

        FileOutputStream fos = new FileOutputStream(new File("./out.txt"));
        final PrintWriter pw = new PrintWriter(fos);
        final JsonFactory factory = new JsonFactory();
        final JsonGenerator generator = factory.createJsonGenerator(System.out);
        nestedQuery.executeNestedQuery(
                executor,
                new JSONResultCollector(generator), //new PrintResultCollector(pw),
                "http://xmlns.com/foaf/0.1/Agent"
        );
        pw.close();
    }

}
