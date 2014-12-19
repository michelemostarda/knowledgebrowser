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

import java.io.Serializable;

/**
 * Defines a property edge between two classes, brings frequence statistics.
 *
 * @author Michele Mostarda (mostarda@fbk.eu)
 */
public class Edge implements Comparable<Edge>, Serializable {
    final String property;
    final int occurrences;
    final String cLeft;
    final String cRight;

    public Edge(String property, int occurrences, String cLeft, String cRight) {
        this.property = property.trim();
        this.occurrences = occurrences;
        this.cLeft = cLeft.trim();
        this.cRight = cRight.trim();
    }

    @Override
    public int compareTo(Edge other) {
        return other.occurrences - occurrences;
    }

    @Override
    public String toString() {
        return String.format("%s -- %s (%d) --> %s", cLeft, property, occurrences, cRight);
    }

    @Override
    public boolean equals(Object obj) {
        if(obj == null) return false;
        if(obj == this) return true;
        if(obj instanceof Edge) {
            final Edge other = (Edge) obj;
            return cLeft.equals(other.cLeft)
                    &&
                    cRight.equals(other.cRight)
                    &&
                    property.equals(other.property)
                    &&
                    occurrences == other.occurrences;
        }
        return false;
    }

    @Override
    public int hashCode() {
        return cLeft.hashCode() * 2 * cRight.hashCode() * 3 * property.hashCode() * 5 * occurrences;
    }
}
