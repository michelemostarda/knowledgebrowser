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

import com.hp.hpl.jena.query.Query;
import com.hp.hpl.jena.query.QueryExecution;
import com.hp.hpl.jena.query.QueryExecutionFactory;
import com.hp.hpl.jena.query.QueryFactory;
import com.hp.hpl.jena.query.QuerySolution;
import com.hp.hpl.jena.query.ResultSet;
import com.hp.hpl.jena.query.Syntax;
import com.hp.hpl.jena.rdf.model.Model;
import com.hp.hpl.jena.rdf.model.ModelFactory;
import org.junit.Test;
import org.rdfhdt.hdt.hdt.HDT;
import org.rdfhdt.hdt.hdt.HDTManager;
import org.rdfhdt.hdtjena.HDTGraph;

import java.io.IOException;

/**
 * @author Michele Mostarda (me@michelemostarda.it)
 */
public class HDTJenaTest {

    //TODO: not running
    private final String PROPS_BY_CLASS_QRY =
            "SELECT  ?class ?property ?total (COUNT(?s) AS ?count ) { \n" +
            "  {\n" +
            "   SELECT ?class (COUNT(?s) AS ?total)\n" +
            "   WHERE{\n" +
            "     ?s a ?class.\n" +
            "   } GROUP BY ?class\n" +
            "   } ?s a ?class.\n" +
            "   \n" +
            "   ?s ?property ?o " +
            "} GROUP BY ?class ?property ?total ORDER BY ?total ?count";

    /**
     * Test benchmark results here: https://docs.google.com/spreadsheets/d/1YF9DLUu9lb-iAan8yBS_G9kjECH4w198vS17R5N-zLg
     *
     * @throws IOException
     */
    @Test
    public void testHDTJenaJoin() throws IOException {
        //HDT hdt = HDTManager.mapIndexedHDT("/Users/hardest/Downloads/hdt-data/wordnet31.hdt.gz", null); // 177MB Heap size
        //HDT hdt = HDTManager.loadIndexedHDT("/Users/hardest/Downloads/hdt-data/wordnet31.hdt.gz", null);
        //HDT hdt = HDTManager.mapIndexedHDT("/Users/hardest/Downloads/hdt-data/dblp-2012-11-28.hdt.gz", null);
        HDT hdt = HDTManager.loadIndexedHDT("/Users/hardest/Downloads/hdt-data/dblp-2012-11-28.hdt.gz", null);
        HDTGraph graph = new HDTGraph(hdt);
        Model model = ModelFactory.createModelForGraph(graph);
        //Query query = QueryFactory.create("SELECT DISTINCT ?type { ?s a ?type }", Syntax.syntaxSPARQL_11);
        //Query query = QueryFactory.create("SELECT * WHERE { ?s ?p ?o }", Syntax.syntaxSPARQL_11);
        //Query query = QueryFactory.create("SELECT (count(*) as ?count) WHERE { ?s ?p ?o }", Syntax.syntaxSPARQL_11);
        //Query query = QueryFactory.create("SELECT (COUNT(*) as ?count) WHERE { ?s a ?t }", Syntax.syntaxSPARQL_11);
        //Query query = QueryFactory.create("SELECT (COUNT(DISTINCT ?t) as ?count) WHERE { ?s a ?t }", Syntax.syntaxSPARQL_11);
        //Query query = QueryFactory.create("SELECT ?class (COUNT(?s) AS ?count) WHERE { ?s a ?class } GROUP BY ?class ORDER BY ?count", Syntax.syntaxSPARQL_11);
        //Query query = QueryFactory.create("SELECT ?class ?property (COUNT(?s) AS ?count ) WHERE {?s a ?class. ?s ?property ?o } GROUP BY ?class ?property ORDER BY ?count", Syntax.syntaxSPARQL_11);
        //Query query = QueryFactory.create("SELECT * WHERE {?s ?p ?o. ?o ?p1 ?o1} ", Syntax.syntaxSPARQL_11);
        //Query query = QueryFactory.create("SELECT * WHERE {?s ?p ?o. ?o ?p1 ?o1. ?o1 ?p2 ?o2} ", Syntax.syntaxSPARQL_11);
        //Query query = QueryFactory.create("SELECT * WHERE {?s a <http://lemon-model.net/lemon#LexicalSense>. ?s ?p ?o. ?o ?p1 ?o1} ", Syntax.syntaxSPARQL_11);
        //Query query = QueryFactory.create("SELECT DISTINCT ?p (COUNT (?p) as ?pcount) ?ca ?cb {?a a ?ca . ?b a ?cb . ?a ?p ?b } GROUP BY ?p ?ca ?cb", Syntax.syntaxSPARQL_11);
        Query query = QueryFactory.create(PROPS_BY_CLASS_QRY, Syntax.syntaxSPARQL_11);

        //Query query = QueryFactory.create("select distinct ?o where {?s ?p ?o. ?o ?p1 ?o1. ?o1 ?p2 ?o2}", Syntax.syntaxSPARQL_11);
        //Query query = QueryFactory.create("SELECT count(distinct ?o) where { ?s ?p ?o }");
        //Query query = QueryFactory.create("SELECT ?class (COUNT(?s) AS ?count) { ?s a ?class } GROUP BY ?class ORDER BY ?count", Syntax.syntaxSPARQL_11);
        //Query query = QueryFactory.create("SELECT * where { ?s a <http://lemon-model.net/lemon#LexicalSense>. ?s ?p ?o. ?o ?p1 ?o1}", Syntax.syntaxSPARQL_11);
        //Query query = QueryFactory.create("SELECT DISTINCT ?class ?p (COUNT(?s) AS ?count1) (COUNT(?o) AS ?count2) { ?s a ?class. ?s ?p ?o } GROUP BY ?class ?p ORDER BY ?count1", Syntax.syntaxSPARQL_11);
        //Query query = QueryFactory.create("SELECT  ?class ?property (COUNT(?s) AS ?count ) WHERE {?s a ?class. ?s ?property ?o } GROUP BY ?class ?property ORDER BY ?count", Syntax.syntaxSPARQL_11);
        //Query query = QueryFactory.create("SELECT * WHERE {?s ?p ?o. ?s1 ?p1 ?o} ", Syntax.syntaxSPARQL_11);

        while(true) {
            QueryExecution qexec = QueryExecutionFactory.create(query, model);
            final long start = System.currentTimeMillis();
            ResultSet results = qexec.execSelect();
            int c = 0;
            for (; results.hasNext(); ) {
                QuerySolution soln = results.nextSolution();
                //soln.getResource("?s");
                //soln.get("?o1");
                System.out.println(soln);
                c++;
            }
            final long end = System.currentTimeMillis();
            System.out.println("count:" + c);
            System.out.println("elapsed:" + (end - start));
        }
    }

}
