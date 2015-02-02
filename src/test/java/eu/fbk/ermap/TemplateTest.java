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

package eu.fbk.ermap;

import eu.fbk.JSONUtils;
import org.junit.Assert;
import org.junit.Test;

import java.util.List;

/**
 */
public class TemplateTest {

    @Test
    public void testGetOperations() {
        final String JSON =
                "{\n" +
                "    \"News\": {\n" +
                "        \"KVs\": null,\n" +
                "        \"NC\": {\n" +
                "            \"KVs\": null,\n" +
                "            \"CeP\": {\n" +
                "                \"KVs\": null\n" +
                "            },\n" +
                "            \"CiP\": {\n" +
                "                \"KVs\": null\n" +
                "            }\n" +
                "        }\n" +
                "    }\n" +
                "}";
        final Template template = new Template(JSONUtils.parseJSON(JSON));
        final List<Template.Operation> operations = template.getOperations();
        Assert.assertEquals(
                "[" +
                        "Open Object [News], " +
                            "Expand element KVs [News], " +
                            "Open Object [NC], " +
                            "Expand element KVs [NC], " +
                                "Open Object [CeP], " +
                                "Expand element KVs [CeP], " +
                                "Close Object [CeP], " +
                                "Open Object [CiP], " +
                                "Expand element KVs [CiP], " +
                                "Close Object [CiP], " +
                            "Close Object [NC], " +
                        "Close Object [News]]",
                operations.toString()
        );
    }

}
