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

package eu.fbk.benchmark;

import org.junit.Assert;
import org.junit.Test;
import org.rdfhdt.hdt.enums.TripleComponentRole;
import org.rdfhdt.hdt.triples.IteratorTripleID;
import org.rdfhdt.hdt.triples.TripleID;

import java.io.IOException;

/**
 * @author Michele Mostarda (mostarda@fbk.eu)
 */
public class JoinTest {

    @Test
    public void testPrint() throws IOException {
        final Benchmark benchmark = new Benchmark("/Users/hardest/Downloads/hdt-data/hdt-test.hdt");
        IteratorTripleID iter = benchmark.getIterator("", "", "");
        TripleID curr;
        while(iter.hasNext()) {
            curr = iter.next();
            System.out.print("RAW: " + curr);
            System.out.println(" ITEM: " + benchmark.toString(curr));
        }
    }

    /**
     * ?s ?p ?o
     * ?o ?p1 ?o1
     *
     * @throws java.io.IOException
     */
    @Test
    public void testJoinIterators1Level() throws IOException {
        //final Benchmark benchmark = new Benchmark("/Users/hardest/Downloads/hdt-data/hdt-test.hdt");
        final Benchmark benchmark = new Benchmark("/Users/hardest/Downloads/hdt-data/wordnet31.hdt");
        final int c = benchmark.joinIterators(
                benchmark.getIterator("", "", ""),
                TripleComponentRole.OBJECT,
                benchmark.getIterator("", "", ""),
                TripleComponentRole.SUBJECT
        );
        Assert.assertEquals(2, c);
    }

    /**
     * ?s ?p ?o
     * ?o ?p1 ?o1
     *
     * @throws java.io.IOException
     */
    @Test
    public void testJoinIterator1LevelsJoin() throws IOException {
        final Benchmark benchmark = new Benchmark("/Users/hardest/Downloads/hdt-data/hdt-test.hdt");
        final Benchmark.JoinIteratorTripleID iter = new Benchmark.JoinIteratorTripleID(
                benchmark,
                benchmark.getIterator("", "", ""),
                TripleComponentRole.OBJECT,
                benchmark.getIterator("", "", ""),
                TripleComponentRole.SUBJECT
        );


        int counter = 0;
        while (iter.hasNext()) {
            System.out.println("MATCH:" + benchmark.toString(iter.next()));
            counter++;
        }
        Assert.assertEquals(0, counter);
    }

    /**
     * ?s ?p ?o
     * ?o ?p1 ?o1
     * ?o1 ?p2 ?o2
     *
     * @throws java.io.IOException
     */
    @Test
    public void testJoinIterator2LevelsJoin() throws IOException {
        final Benchmark benchmark = new Benchmark("/Users/hardest/Downloads/hdt-data/hdt-test.hdt");
        final Benchmark.JoinIteratorTripleID iter = new Benchmark.JoinIteratorTripleID(
                benchmark,
                benchmark.getIterator("", "", ""),
                TripleComponentRole.OBJECT,
                benchmark.getIterator("", "", ""),
                TripleComponentRole.SUBJECT
        );
        final  Benchmark.JoinIteratorTripleID iter2 = new Benchmark.JoinIteratorTripleID(
                benchmark,
                iter, TripleComponentRole.OBJECT,
                benchmark.getIterator("", "", ""),
                TripleComponentRole.SUBJECT
        );

        int counter = 0;
        while (iter2.hasNext()) {
            System.out.println("MATCH:" + benchmark.toString(iter2.next()));
            counter++;
        }
        Assert.assertEquals(0, counter);
    }

}
