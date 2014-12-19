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
import org.junit.Assert;
import org.junit.Before;
import org.junit.Test;

import java.io.ByteArrayOutputStream;
import java.io.IOException;
import java.nio.file.FileSystems;
import java.nio.file.Files;
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
        ByteArrayOutputStream baos = new ByteArrayOutputStream();
        JsonGenerator generator = factory.createJsonGenerator(baos);
        materializer.materialize(
                Arrays.asList(
                        new Property("http://purl.org/dc/elements/1.1/creator", true),
                        new Property("http://swrc.ontoware.org/ontology#series")
                ),
                generator
        );
        generator.flush();
        Files.write(FileSystems.getDefault().getPath("./out"), baos.toByteArray());
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
        System.out.println(Arrays.toString(path));
    }

}
