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

import java.io.PrintWriter;
import java.util.Arrays;

/**
 * @author Michele Mostarda (mostarda@fbk.eu)
 */
public class PrintResultCollector implements ResultCollector {

    private final PrintWriter pw;

    public PrintResultCollector(PrintWriter pw) {
        this.pw = pw;
    }

    @Override
    public void begin() {
        pw.println("Begin");
    }

    @Override
    public void startLevel(int l, String queryName, String[] args) {
        pw.println("Start level " + l + ": " + queryName);
    }

    @Override
    public void collect(String[] bindings, String[] values) {
        pw.println("Collect " + Arrays.toString(bindings) + " " + Arrays.toString(values));
    }

    @Override
    public void endLevel(int l) {
        pw.println("End level " + l);
    }

    @Override
    public void end() {
        pw.println("End");
    }

}
