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

/**
 * @author Michele Mostarda (mostarda@fbk.eu)
 */

import org.rdfhdt.hdt.dictionary.Dictionary;
import org.rdfhdt.hdt.enums.ResultEstimationType;
import org.rdfhdt.hdt.enums.TripleComponentOrder;
import org.rdfhdt.hdt.enums.TripleComponentRole;
import org.rdfhdt.hdt.exceptions.NotFoundException;
import org.rdfhdt.hdt.hdt.HDTManager;
import org.rdfhdt.hdt.hdt.impl.HDTImpl;
import org.rdfhdt.hdt.triples.IteratorTripleID;
import org.rdfhdt.hdt.triples.IteratorTripleString;
import org.rdfhdt.hdt.triples.TripleID;
import org.rdfhdt.hdt.triples.TripleString;
import org.rdfhdt.hdt.triples.Triples;

import java.io.IOException;
import java.util.ArrayList;
import java.util.List;
import java.util.Set;
import java.util.TreeSet;

public class Benchmark {

    public static final String RDF_ISA = "http://www.w3.org/1999/02/22-rdf-syntax-ns#type";

    private final HDTImpl hdt;
    private final Dictionary dictionary;
    private final Triples triples;

    private final int RDF_ISA_ID;
    private final TripleID getInstancesLowTripleId;

    public Benchmark(String file) throws IOException {
        hdt = (HDTImpl) HDTManager.loadHDT(file, null);
        dictionary = hdt.getDictionary();
        triples = hdt.getTriples();
        RDF_ISA_ID = hdt.getDictionary().stringToId(RDF_ISA, TripleComponentRole.PREDICATE);
        getInstancesLowTripleId = new TripleID(0, RDF_ISA_ID, 0);
    }

    public String toString(TripleID t) {
        return  String.format(
                "%s %s %s",
                dictionary.idToString(t.getSubject(), TripleComponentRole.SUBJECT).toString(),
                dictionary.idToString(t.getPredicate(), TripleComponentRole.PREDICATE).toString(),
                dictionary.idToString(t.getObject(), TripleComponentRole.OBJECT).toString()
        );
    }

    public TripleID getPattern(String s, String p, String o) {
        return new TripleID(
                dictionary.stringToId(s, TripleComponentRole.SUBJECT),
                dictionary.stringToId(p, TripleComponentRole.PREDICATE),
                dictionary.stringToId(o, TripleComponentRole.OBJECT)
        );
    }

    public IteratorTripleID getIterator(String s, String p, String o) {
        return triples.search(getPattern(s, p, o));
    }

    public int countStatements() throws NotFoundException {
        final IteratorTripleString iter = hdt.search("", "", "");
        int counter = 0;
        while (iter.hasNext()) {
            iter.next().toString();
            counter++;
        }
        return counter;
    }

    public int countStatementsLow() throws NotFoundException {
        final IteratorTripleID iter = triples.search(new TripleID(0, 0, 0));
        int counter = 0;
        while (iter.hasNext()) {
            iter.next();
            counter++;
        }
        return counter;
    }

    public int countInstances() throws NotFoundException {
        final IteratorTripleString iter = hdt.search("", RDF_ISA, "");
        int counter = 0;
        while(iter.hasNext()) {
            iter.next();
            counter++;
        }
        return counter;
    }

    public int countInstancesLow() throws NotFoundException {
        final IteratorTripleID iter = triples.search(new TripleID(0, RDF_ISA_ID, 0));
        int counter = 0;
        while(iter.hasNext()) {
            iter.next();
            counter++;
        }
        return counter;
    }

    public String[] getListOfClasses() throws NotFoundException {
        final IteratorTripleString iter = hdt.search("", RDF_ISA, "");
        final Set<String> clazzes = new TreeSet<>();
        while(iter.hasNext()) {
            clazzes.add(iter.next().getObject().toString());
        }
        return clazzes.toArray(new String[clazzes.size()]);
    }

    public int[] getListOfClassIDsLow() {
        final IteratorTripleID iter = triples.search(
                new TripleID(0, dictionary.stringToId(RDF_ISA, TripleComponentRole.PREDICATE), 0)
        );
        final Set<Integer> clazzes = new TreeSet<>();
        while(iter.hasNext()) {
            clazzes.add(iter.next().getObject());
        }
        final int[] out = new int[clazzes.size()];
        int i = 0;
        for(Integer c : clazzes) {
            out[i++] = c;
        }
        return out;
    }

    public String[] getListOfClassesLow() throws NotFoundException {
        int i = 0;
        final int[] clazzes = getListOfClassIDsLow();
        final String[] out = new String[clazzes.length];
        for(int clazz : clazzes) {
            out[i++] = dictionary.idToString(clazz, TripleComponentRole.OBJECT).toString();
        }
        return out;
    }

    public String[] getInstances(String clazz) throws NotFoundException {
        final IteratorTripleString iter = hdt.search("", RDF_ISA, clazz);
        final List<String> out = new ArrayList<>();
        TripleString triple;
        while(iter.hasNext()) {
            triple = iter.next();
            out.add(triple.getSubject().toString());
        }
        return out.toArray(new String[out.size()]);
    }

    public int[] getInstancesLow(String clazz) throws NotFoundException {
        final IteratorTripleID iter = triples.search(
                new TripleID(
                        0,
                        dictionary.stringToId(RDF_ISA, TripleComponentRole.PREDICATE),
                        dictionary.stringToId(clazz, TripleComponentRole.OBJECT)
                )
        );
        return iterSubjectToArray(iter);
    }

    public int[] getInstancesLow(int clazz) throws NotFoundException {
        getInstancesLowTripleId.setObject(clazz);
        final IteratorTripleID iter = triples.search(getInstancesLowTripleId);
        return iterSubjectToArray(iter);
    }

    public String[] getProperties(String instance) throws NotFoundException {
        final IteratorTripleString iter = hdt.search(instance, "", "");
        final List<String> out = new ArrayList<>();
        TripleString triple;
        while(iter.hasNext()) {
            triple = iter.next();
            out.add(triple.getPredicate().toString());
            out.add(triple.getObject().toString());
        }
        return out.toArray(new String[out.size()]);
    }

    public String[] getPropertiesLow(int instance) throws NotFoundException {
        final IteratorTripleID iter = triples.search(new TripleID(instance, 0, 0));
        final List<String> out = new ArrayList<>();
        final StringBuilder sb = new StringBuilder();
        TripleID triple;
        while(iter.hasNext()) {
            triple = iter.next();
            sb.append(Integer.toString(triple.getPredicate()));
            sb.append(':');
            sb.append(Integer.toBinaryString(triple.getObject()));
            out.add(sb.toString());
            sb.delete(0, sb.length());
        }
        return out.toArray(new String[out.size()]);
    }

    public int joinIterators(IteratorTripleID l, TripleComponentRole lr,  IteratorTripleID r, TripleComponentRole rr) {
        TripleID lCurr, rCurr;
        int lTarget, rTarget;
        int count = 0;
        while (l.hasNext()) {
            lCurr = l.next();
            //System.out.println("LEFT: " + toString(lCurr));
            lTarget = getId(lCurr, lr);
            rCurr = goTo(r, rr, lTarget);
            if(rCurr == null) continue;
            rTarget = getId(rCurr, rr);
            if (!lCurr.equals(rCurr) && lTarget == rTarget && compare(dictionary.idToString(lTarget,lr),(dictionary.idToString(rTarget,rr)))) {
                // emit triple
                //System.out.println("MATCH:" + toString(lCurr) + " WITH " + toString(rCurr));
                count++;
                //continue;
            }
//            lCurr = goTo(l, lr, rTarget);
//            if(lCurr == null) break;
//            lTarget = getId(lCurr, lr);
//            if (lTarget == rTarget) {
//                // emit triple
//                count++;
//            }
        }
        return count;
    }

    private boolean compare(CharSequence a, CharSequence b) {
        if(a.length() != b.length()) return false;
        for(int i = 0; i < a.length(); i++) {
            if(a.charAt(i) != b.charAt(i)) return false;
        }
        return true;
    }

    private static TripleID goTo(IteratorTripleID i, TripleComponentRole r, int target) {
        //if(r != TripleComponentRole.SUBJECT) i.goToStart();
        i.goToStart();
        TripleID curr;
        while(i.hasNext()) {
            curr = i.next();
            if(getId(curr, r) >= target) {
                return curr;
            }
        }
        return null;
    }

    private static int getId(TripleID t, TripleComponentRole r) {
        switch (r) {
            case SUBJECT:
                return t.getSubject();
            case PREDICATE:
                return t.getPredicate();
            case OBJECT:
                return t.getObject();
        }
        throw new UnsupportedOperationException();
    }

    private int[] iterSubjectToArray(IteratorTripleID iter) {
        final List<Integer> buf = new ArrayList<>();
        TripleID triple;
        while (iter.hasNext()) {
            triple = iter.next();
            buf.add(triple.getSubject());
        }
        int[] out = new int[buf.size()];
        int i = 0;
        for(Integer b : buf) {
            out[i++] = b;
        }
        return out;
    }

    public static class JoinIteratorTripleID implements IteratorTripleID {

        private final Benchmark benchmark;
        private final IteratorTripleID l;
        private final TripleComponentRole lr;
        private final IteratorTripleID r;
        private final TripleComponentRole rr;

        private TripleID match;

        public JoinIteratorTripleID(
                Benchmark benchmark,
                IteratorTripleID l, TripleComponentRole lr,
                IteratorTripleID r, TripleComponentRole rr
        ) {
            this.benchmark = benchmark;
            this.l = l;
            this.lr = lr;
            //this.sr = new StepBackIteratorTripleID(sr);
            this.r = r;
            this.rr = rr;
        }

        @Override
        public boolean hasPrevious() {
            throw new UnsupportedOperationException();
        }

        @Override
        public TripleID previous() {
            throw new UnsupportedOperationException();
        }

        @Override
        public void goToStart() {
            throw new UnsupportedOperationException();
        }

        @Override
        public boolean canGoTo() {
            return false;
        }

        @Override
        public void goTo(long pos) {
            throw new UnsupportedOperationException();
        }

        @Override
        public long estimatedNumResults() {
            throw new UnsupportedOperationException();
        }

        @Override
        public ResultEstimationType numResultEstimation() {
            throw new UnsupportedOperationException();
        }

        @Override
        public TripleComponentOrder getOrder() {
            throw new UnsupportedOperationException();
        }

        @Override
        public boolean hasNext() {
            TripleID lTripleId, rTripleId;
            int lId;
            while(l.hasNext()) {
                lTripleId = l.next();
                lId = getId(lTripleId, lr);

                r.goToStart();
                while(r.hasNext()) {
                    rTripleId = r.next();
                    if (getId(rTripleId, rr) == lId) {
                        match = rTripleId;
                        return true;
                    }
                }
            }
            return false;
        }

        @Override
        public TripleID next() {
            if(match == null) throw new IllegalStateException();
            final TripleID o = match;
            match = null;
            return o;
        }

        @Override
        public void remove() {
            throw new UnsupportedOperationException();
        }
    }

}
