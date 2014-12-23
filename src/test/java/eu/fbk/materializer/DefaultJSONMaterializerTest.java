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

import com.google.common.io.Files;
import org.codehaus.jackson.JsonFactory;
import org.codehaus.jackson.JsonGenerator;
import org.codehaus.jackson.util.DefaultPrettyPrinter;
import org.junit.Assert;
import org.junit.Before;
import org.junit.Test;

import java.io.BufferedOutputStream;
import java.io.File;
import java.io.FileOutputStream;
import java.io.IOException;
import java.nio.charset.Charset;
import java.util.Arrays;

/**
 * @author Michele Mostarda (mostarda@fbk.eu)
 */
public class DefaultJSONMaterializerTest {

    private DefaultJSONMaterializer materializer;

    @Before
    public void setUp() {
        materializer = new DefaultJSONMaterializer("/Users/hardest/Downloads/hdt-data/dblp-2012-11-28.hdt.gz");
    }

    @Test
    public void testMaterialize() throws IOException, JsonMaterializerException {
        final File out = new File("./out.json");
        JsonFactory factory = new JsonFactory();
        try(BufferedOutputStream bos = new BufferedOutputStream(new FileOutputStream(out))) {
            JsonGenerator generator = factory.createJsonGenerator(bos);
            generator.setPrettyPrinter(new DefaultPrettyPrinter());
            materializer.materialize(
                    Arrays.asList(
                            new Level(
                                    new Edge("http://purl.org/dc/elements/1.1/creator", 5786365, "http://xmlns.com/foaf/0.1/Document", "http://xmlns.com/foaf/0.1/Agent"),
                                    new Edge("http://swrc.ontoware.org/ontology#series", 1207225, "http://xmlns.com/foaf/0.1/Document", "http://swrc.ontoware.org/ontology#Conference")
                            ),
                            new Level(true, new Edge("http://purl.org/dc/elements/1.1/creator", 2284410, "http://swrc.ontoware.org/ontology#Article", "http://xmlns.com/foaf/0.1/Agent"))
                    ),
                    generator
            );
            generator.flush();
        }
        final String content = Files.toString(out, Charset.defaultCharset());
        Assert.assertTrue(content.contains("http://purl.org/dc/elements/1.1/creator"));
        Assert.assertTrue(content.contains("http://swrc.ontoware.org/ontology#series"));
    }

    @Test
    public void testGetPathAnalysis() {
        final PathAnalysis pathAnalysis = materializer.getPathAnalysis();
        Assert.assertEquals(96, pathAnalysis.getEdges().length);
        Assert.assertEquals(192, pathAnalysis.getNodes().length);
    }

    @Test
    public void testBuildLevels() {
        final PathAnalysis pathAnalysis = materializer.getPathAnalysis();
        final Edge[] edges = pathAnalysis.getMaxSpanningTree();
        Assert.assertEquals(
                "[" +
                "http://xmlns.com/foaf/0.1/Document -- http://purl.org/dc/elements/1.1/creator (5786365) --> http://xmlns.com/foaf/0.1/Agent, " +
                "http://swrc.ontoware.org/ontology#InProceedings -- http://purl.org/dc/elements/1.1/creator (3400645) --> http://xmlns.com/foaf/0.1/Agent, " +
                "http://swrc.ontoware.org/ontology#Article -- http://purl.org/dc/elements/1.1/creator (2284410) --> http://xmlns.com/foaf/0.1/Agent, " +
                "http://xmlns.com/foaf/0.1/Document -- http://www.w3.org/1999/02/22-rdf-syntax-ns#type (2134666) --> http://www.w3.org/2000/01/rdf-schema#Class, " +
                "http://xmlns.com/foaf/0.1/Document -- http://swrc.ontoware.org/ontology#series (1207225) --> http://swrc.ontoware.org/ontology#Conference, " +
                "http://xmlns.com/foaf/0.1/Document -- http://purl.org/dc/terms/partOf (1092689) --> http://swrc.ontoware.org/ontology#Proceedings, " +
                "http://swrc.ontoware.org/ontology#Article -- http://swrc.ontoware.org/ontology#journal (888172) --> http://swrc.ontoware.org/ontology#Journal, " +
                "http://swrc.ontoware.org/ontology#InCollection -- http://purl.org/dc/elements/1.1/creator (34880) --> http://xmlns.com/foaf/0.1/Agent, " +
                "http://xmlns.com/foaf/0.1/Document -- http://purl.org/dc/terms/partOf (22185) --> http://swrc.ontoware.org/ontology#Book, " +
                "http://swrc.ontoware.org/ontology#PhDThesis -- http://purl.org/dc/elements/1.1/creator (6915) --> http://xmlns.com/foaf/0.1/Agent, " +
                "http://swrc.ontoware.org/ontology#InCollection -- http://swrc.ontoware.org/ontology#series (3804) --> http://swrc.ontoware.org/ontology#Collection, " +
                "http://swrc.ontoware.org/ontology#MasterThesis -- http://purl.org/dc/elements/1.1/creator (9) --> http://xmlns.com/foaf/0.1/Agent]",
                Arrays.toString(edges)
        );
        final Level[] levels = pathAnalysis.buildLevels(edges);
        Assert.assertEquals(
                "[" +
                "false [" +
                "http://xmlns.com/foaf/0.1/Document -- http://purl.org/dc/elements/1.1/creator (5786365) --> http://xmlns.com/foaf/0.1/Agent, " +
                "http://xmlns.com/foaf/0.1/Document -- http://www.w3.org/1999/02/22-rdf-syntax-ns#type (2134666) --> http://www.w3.org/2000/01/rdf-schema#Class, " +
                "http://xmlns.com/foaf/0.1/Document -- http://swrc.ontoware.org/ontology#series (1207225) --> http://swrc.ontoware.org/ontology#Conference, " +
                "http://xmlns.com/foaf/0.1/Document -- http://purl.org/dc/terms/partOf (1092689) --> http://swrc.ontoware.org/ontology#Proceedings, " +
                "http://xmlns.com/foaf/0.1/Document -- http://purl.org/dc/terms/partOf (22185) --> http://swrc.ontoware.org/ontology#Book" +
                "], " +
                "true [" +
                "http://swrc.ontoware.org/ontology#InProceedings -- http://purl.org/dc/elements/1.1/creator (3400645) --> http://xmlns.com/foaf/0.1/Agent, " +
                "http://swrc.ontoware.org/ontology#Article -- http://purl.org/dc/elements/1.1/creator (2284410) --> http://xmlns.com/foaf/0.1/Agent, " +
                "http://swrc.ontoware.org/ontology#InCollection -- http://purl.org/dc/elements/1.1/creator (34880) --> http://xmlns.com/foaf/0.1/Agent, " +
                "http://swrc.ontoware.org/ontology#PhDThesis -- http://purl.org/dc/elements/1.1/creator (6915) --> http://xmlns.com/foaf/0.1/Agent, " +
                "http://swrc.ontoware.org/ontology#MasterThesis -- http://purl.org/dc/elements/1.1/creator (9) --> http://xmlns.com/foaf/0.1/Agent" +
                "], " +
                "false [" +
                "http://swrc.ontoware.org/ontology#Article -- http://swrc.ontoware.org/ontology#journal (888172) --> http://swrc.ontoware.org/ontology#Journal" +
                "], " +
                "false [" +
                "http://swrc.ontoware.org/ontology#InCollection -- http://swrc.ontoware.org/ontology#series (3804) --> http://swrc.ontoware.org/ontology#Collection" +
                "]" +
                "]",
                Arrays.toString(levels)
        );
    }

}
