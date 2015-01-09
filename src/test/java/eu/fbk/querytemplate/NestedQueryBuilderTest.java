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

import org.junit.Assert;
import org.junit.Test;

import java.io.IOException;

/**
 * Test case for {@link eu.fbk.querytemplate.NestedQueryBuilder}.
 *
 * @author Michele Mostarda (mostarda@fbk.eu)
 */
public class NestedQueryBuilderTest {

    @Test
    public void testBuild() throws IOException {
        final NestedQueryBuilder builder = new NestedQueryBuilder();
        final NestedQuery nestedQuery = builder.build(this.getClass().getResourceAsStream("query1.json"));
        Assert.assertNotNull(nestedQuery);
        Assert.assertEquals(2, nestedQuery.getLevels());
        Assert.assertEquals("q1", nestedQuery.getName(0));
        Assert.assertEquals("p1", nestedQuery.getPivot(0));
        Assert.assertEquals("SELECT ?s {?s a <$T>. ?s ?p ?o} in: [T] out: [s]", nestedQuery.getQuery(0).toString());
        Assert.assertEquals("q2", nestedQuery.getName(1));
        Assert.assertEquals("p2", nestedQuery.getPivot(1));
        Assert.assertEquals("SELECT ?o {<$S> ?p ?o in: [S] out: [o]", nestedQuery.getQuery(1).toString());
    }

}
