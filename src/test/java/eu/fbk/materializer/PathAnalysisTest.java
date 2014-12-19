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

import org.junit.Assert;
import org.junit.Test;

import java.util.ArrayList;
import java.util.List;

/**
 * Test case for {@link eu.fbk.materializer.PathAnalysis}.
 *
 * @author Michele Mostarda (mostarda@fbk.eu)
 */
public class PathAnalysisTest {

    @Test
    public void testBuildPathDepthFirst() {
        final List<Edge> edgeList = new ArrayList<>();
        edgeList.add(new Edge("P1", 1, "CL1", "CR1"));
        edgeList.add(new Edge("P2", 1, "CR1", "CR2"));
        edgeList.add(new Edge("P3", 1, "CR1", "CR3"));
        edgeList.add(new Edge("P3", 1, "CR4", "CR3"));
        Assert.assertEquals(
                "[[CL1 -- P1 (1) --> CR1], [CR1 -- P2 (1) --> CR2, CR1 -- P3 (1) --> CR3], [CR4 -- P3 (1) --> CR3]]",
                PathAnalysis.buildPathDepthFirst(edgeList.get(0).cLeft, edgeList).toString()
        );
    }

}
