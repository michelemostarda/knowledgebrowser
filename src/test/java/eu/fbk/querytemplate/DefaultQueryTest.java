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

import java.io.File;
import java.io.IOException;
import java.util.HashMap;
import java.util.Map;

/**
 * @author Michele Mostarda (mostarda@fbk.eu)
 */
public class DefaultQueryTest {

    public static DefaultQueryExecutor getExecutor() throws IOException {
        return new DefaultQueryExecutor(new File("hdt-data/dblp-2012-11-28.hdt.gz"));
    }

    @Test
    public void testQuery() throws IOException {
        final DefaultQuery query = new DefaultQuery("SELECT ?i {?i a <$Type>}");
        final Map<String,String> args = new HashMap<String, String>(){{ put("Type", "http://xmlns.com/foaf/0.1/Agent"); }};
        Assert.assertEquals("SELECT ?i {?i a <http://xmlns.com/foaf/0.1/Agent>}", query.expand(args));
        final DefaultQueryExecutor executor = getExecutor();
        final Result result = query.perform(executor, args);
        int c = 0;
        while(result.next())  {
            Assert.assertNotNull(result.getBindings());
            c++;
        }
        Assert.assertEquals(1201780, c);
    }

}
