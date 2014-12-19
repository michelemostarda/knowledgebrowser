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

import org.codehaus.jackson.JsonFactory;
import org.codehaus.jackson.JsonGenerator;
import org.codehaus.jackson.util.DefaultPrettyPrinter;
import org.junit.Assert;
import org.junit.Before;
import org.junit.Test;

import java.io.BufferedOutputStream;
import java.io.FileOutputStream;
import java.io.IOException;
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
        JsonFactory factory = new JsonFactory();
        try(BufferedOutputStream bos = new BufferedOutputStream(new FileOutputStream("./out.json"))) {
            JsonGenerator generator = factory.createJsonGenerator(bos);
            generator.setPrettyPrinter(new DefaultPrettyPrinter());
            materializer.materialize(
                    Arrays.asList(
                            new Property(new Edge("http://purl.org/dc/elements/1.1/creator", 5786365, "http://xmlns.com/foaf/0.1/Document", "http://xmlns.com/foaf/0.1/Agent")),
                            new Property(new Edge("http://purl.org/dc/elements/1.1/creator", 2284410, "http://swrc.ontoware.org/ontology#Article", "http://xmlns.com/foaf/0.1/Agent"), true)
                            //http://xmlns.com/foaf/0.1/Document -- http://swrc.ontoware.org/ontology#series (1207225) --> http://swrc.ontoware.org/ontology#Conference
                    ),
                    generator
            );
            generator.flush();
        }
    }

    @Test
    public void testGetPathAnalysis() {
        final PathAnalysis pathAnalysis = materializer.getPathAnalysis();
        Assert.assertEquals(96, pathAnalysis.getEdges().length);
        Assert.assertEquals(192, pathAnalysis.getNodes().length);
    }

    @Test
    public void testGetMaxSpanningTreeAndPath() {
        final PathAnalysis pathAnalysis = materializer.getPathAnalysis();
        final Edge[] edges = pathAnalysis.getMaxSpanningTree();
        final Property[] path = pathAnalysis.toPropertyPath(edges);
        System.out.println("EDGES: " + Arrays.toString(edges));
        System.out.println(Arrays.toString(path));
    }

}
