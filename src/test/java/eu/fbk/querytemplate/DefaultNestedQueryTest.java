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

import org.junit.Test;

import java.io.File;
import java.io.FileOutputStream;
import java.io.IOException;
import java.io.PrintWriter;
import java.util.Arrays;

/**
 * @author Michele Mostarda (mostarda@fbk.eu)
 */
public class DefaultNestedQueryTest {

    @Test
    public void testNested() throws IOException {
        final NestedQuery nestedQuery = new DefaultNestedQuery(
                new DefaultQuery(
                        "SELECT ?Agent {?Agent a <$Type>}",
                        new String[]{"Type"}, new String[]{"Agent"}
                ),
                new DefaultQuery(
                        "SELECT ?Article {?Article a <http://swrc.ontoware.org/ontology#Article>. ?Article <http://purl.org/dc/elements/1.1/creator> <$Agent> }"
                        , new String[]{"Agent"}, new String[]{"Article"}
                ) // http://swrc.ontoware.org/ontology#Article -- http://purl.org/dc/elements/1.1/creator (2284410) --> http://xmlns.com/foaf/0.1/Agent
        );
        final QueryExecutor executor = new DefaultQueryExecutor(new File("/Users/hardest/Downloads/hdt-data/dblp-2012-11-28.hdt.gz"));

        FileOutputStream fos = new FileOutputStream(new File("./out.txt"));
        final PrintWriter pw = new PrintWriter(fos);
        nestedQuery.executeNestedQuery(executor, new ResultCollector() {
            @Override
            public void begin() {
                pw.println("BEGIN");
            }

            @Override
            public void startLevel(int l) {
                pw.println("START LEVEL " + l);
            }

            @Override
            public void result(String[] bindings, String[] values) {
                pw.println("RESULT " + Arrays.toString(bindings) + " " + Arrays.toString(values));
            }

                    @Override
            public void collect(String[] bindings, String[] values) {
                pw.println("COLLECT " + Arrays.toString(bindings) + " " + Arrays.toString(values));
            }

            @Override
            public void endLevel(int l) {
                pw.println("END LEVEL " + l);
            }

            @Override
            public void end() {
                pw.println("END");
            }
        },
        "http://xmlns.com/foaf/0.1/Agent"
        );
        pw.close();
    }

}
