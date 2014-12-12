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

import eu.fbk.Benchmark;
import org.apache.log4j.Logger;
import org.junit.After;
import org.junit.Assert;
import org.junit.Before;
import org.junit.Ignore;
import org.junit.Test;
import org.rdfhdt.hdt.enums.TripleComponentRole;
import org.rdfhdt.hdt.exceptions.NotFoundException;

import java.io.IOException;
import java.util.HashMap;
import java.util.Map;

/**
 * @author Michele Mostarda (me@michelemostarda.it)
 */
public class WNBenchmarkTest {

    private static final String data = "/Users/hardest/Downloads/hdt-data/wordnet31.hdt.gz";

    private static final String[] WN_CLAZZES = new String[] {
            "http://lemon-model.net/lemon#Component",
            "http://lemon-model.net/lemon#Form",
            "http://lemon-model.net/lemon#LexicalEntry",
            "http://lemon-model.net/lemon#LexicalSense",
            "http://wordnet-rdf.princeton.edu/ontology#Synset"
    };

    private static final String WN_INSTANCES_COUNT = "{" +
            "http://lemon-model.net/lemon#LexicalEntry=158105, " +
            "http://lemon-model.net/lemon#Form=161832, " +
            "http://lemon-model.net/lemon#LexicalSense=206598, " +
            "http://lemon-model.net/lemon#Component=1452, " +
            "http://wordnet-rdf.princeton.edu/ontology#Synset=117774" +
            "}";

    private static final Logger logger = Logger.getLogger(WNBenchmarkTest.class);

    private Benchmark benchmark;

    @Before
    public void setUp() throws IOException {
        benchmark = new Benchmark(data);
    }

    @After
    public void tearDown() {
    }

    @Test
    public void testCountStatementsLow() throws IOException, NotFoundException {
        final long start = System.currentTimeMillis();
        final int c = benchmark.countStatementsLow();
        final long end = System.currentTimeMillis();
        logger.info("Time: " + (end - start));
        Assert.assertEquals(5558748, c);
    }

    @Test
    public void testCountInstances() throws IOException, NotFoundException {
        final long start = System.currentTimeMillis();
        final int c = benchmark.countInstances();
        final long end = System.currentTimeMillis();
        logger.info("Time: " + (end - start));
        Assert.assertEquals(645761, c);
    }

    @Test
    public void testCountInstancesLow() throws IOException, NotFoundException {
        final long start = System.currentTimeMillis();
        final int c = benchmark.countInstancesLow();
        final long end = System.currentTimeMillis();
        logger.info("Time: " + (end - start));
        Assert.assertEquals(645761, c);
    }

    @Test
    public void testGetListOfClasses() throws IOException, NotFoundException {
        final long start = System.currentTimeMillis();
        final String[] clazzes = benchmark.getListOfClasses();
        final long end = System.currentTimeMillis();
        logger.info("Time: " + (end - start));
        Assert.assertArrayEquals(WN_CLAZZES, clazzes);
        Assert.assertEquals(5, clazzes.length);
    }

    @Test
    public void testGetListOfClassesLow() throws IOException, NotFoundException {
        final long start = System.currentTimeMillis();
        final String[] clazzes = benchmark.getListOfClassesLow();
        final long end = System.currentTimeMillis();
        logger.info("Time: " + (end - start));
        Assert.assertArrayEquals(WN_CLAZZES, clazzes);
        Assert.assertEquals(5, clazzes.length);
    }

    @Test
    public void testGetInstances() throws IOException, NotFoundException {
        final long start = System.currentTimeMillis();
        final String[] clazzes = benchmark.getListOfClasses();
        final Map<String,Integer> instanceCount = new HashMap<>();
        for(String clazz : clazzes) {
            instanceCount.put(clazz, benchmark.getInstances(clazz).length);
        }
        final long end = System.currentTimeMillis();
        logger.info("Time: " + (end - start));
        Assert.assertEquals(WN_INSTANCES_COUNT, instanceCount.toString());
    }

    @Test
    public void testGetInstancesLow() throws IOException, NotFoundException {
        final long start = System.currentTimeMillis();
        final String[] clazzes = benchmark.getListOfClassesLow();
        final Map<String,Integer> instanceCount = new HashMap<>();
        for(String clazz : clazzes) {
            instanceCount.put(clazz, benchmark.getInstancesLow(clazz).length);
        }
        final long end = System.currentTimeMillis();
        logger.info("Time: " + (end - start));
        Assert.assertEquals(WN_INSTANCES_COUNT, instanceCount.toString());
    }

    //TODO: do low level test on graph traversal like operators

    @Test
    public void testGetProperties() throws IOException, NotFoundException {
        final long start = System.currentTimeMillis();
        final String[] clazzes = benchmark.getListOfClasses();
        final Map<String, Integer> propertiesCount = new HashMap<>();
        for (String clazz : clazzes) {
            for(String instance : benchmark.getInstances(clazz)) {
                propertiesCount.put(instance, benchmark.getProperties(instance).length);
            }
        }
        final long end = System.currentTimeMillis();
        logger.info("Time: " + (end - start));
        Assert.assertEquals(645761, propertiesCount.size());
    }

    @Test
    public void testGetPropertiesLow() throws IOException, NotFoundException {
        final long start = System.currentTimeMillis();
        final int[] clazzes = benchmark.getListOfClassIDsLow();
        final Map<Integer, Integer> propertiesCount = new HashMap<>();
        for (int clazz : clazzes) {
            for(int instance : benchmark.getInstancesLow(clazz)) {
                propertiesCount.put(instance, benchmark.getPropertiesLow(instance).length);
            }
        }
        final long end = System.currentTimeMillis();
        logger.info("Time: " + (end - start));
        Assert.assertEquals(645761, propertiesCount.size());
    }

    /**
     * ?s a C
     * ?s ?p ?o
     * @throws IOException
     */
    @Test
    public void testJoinIteratorsLow() throws IOException {
        final long start = System.currentTimeMillis();
        final int m = benchmark.joinIterators(
                //benchmark.getIterator("", Benchmark.RDF_ISA, WN_CLAZZES[0]),
                benchmark.getIterator("", Benchmark.RDF_ISA, WN_CLAZZES[0]),
                TripleComponentRole.SUBJECT,
                benchmark.getIterator("", "", ""),
                TripleComponentRole.SUBJECT
        );
        final long end = System.currentTimeMillis();
        logger.info("Time: " + (end - start));
        Assert.assertEquals(1452, m);
    }

    /**
     * ?s ?p ?o
     * ?o ?p1 ?o1
     * @throws IOException
     */
    @Ignore // TODO JoinIteratorTripleID must be completed
    @Test
    public void testJoinIteratorsLow2() throws IOException {
        final long start = System.currentTimeMillis();
        final int m = benchmark.joinIterators(
                benchmark.getIterator("", "", ""),
                TripleComponentRole.OBJECT,
                benchmark.getIterator("", "", ""),
                TripleComponentRole.SUBJECT
        );
        final long end = System.currentTimeMillis();
        logger.info("Time: " + (end - start));
        Assert.assertEquals(5558748, m);
    }

    /**
     * ?s a T
     * ?s ?p ?o
     * ?o ?p1 ?o1
     * @throws IOException
     */
    @Ignore // TODO JoinIteratorTripleID must be completed
    @Test
    public void testJoinIteratorsLow3() throws IOException {
        final long start = System.currentTimeMillis();
        final Benchmark.JoinIteratorTripleID iter = new Benchmark.JoinIteratorTripleID(
                benchmark,
                benchmark.getIterator("", Benchmark.RDF_ISA,  WN_CLAZZES[4]),
                TripleComponentRole.SUBJECT,
                benchmark.getIterator("", "", ""),
                TripleComponentRole.SUBJECT
        );
        int counter = 0;

        final Benchmark.JoinIteratorTripleID iter2 = new Benchmark.JoinIteratorTripleID(
                benchmark,
                iter, TripleComponentRole.OBJECT,
                benchmark.getIterator("", "", ""), TripleComponentRole.SUBJECT
        );

        while(iter2.hasNext()) {
            iter2.next();
            counter++;
        }

        final long end = System.currentTimeMillis();
        logger.info("Time: " + (end - start));
        Assert.assertEquals(0, counter);
    }

}
