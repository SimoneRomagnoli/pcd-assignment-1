/*
 * Licensed to the Apache Software Foundation (ASF) under one or more
 * contributor license agreements.  See the NOTICE file distributed with
 * this work for additional information regarding copyright ownership.
 * The ASF licenses this file to You under the Apache License, Version 2.0
 * (the "License"); you may not use this file except in compliance with
 * the License.  You may obtain a copy of the License at
 *
 *      http://www.apache.org/licenses/LICENSE-2.0
 *
 * Unless required by applicable law or agreed to in writing, software
 * distributed under the License is distributed on an "AS IS" BASIS,
 * WITHOUT WARRANTIES OR CONDITIONS OF ANY KIND, either express or implied.
 * See the License for the specific language governing permissions and
 * limitations under the License.
 */

import gui.GUI;

import java.io.IOException;
import java.util.*;

/**
 * This is a simple text extraction example to get started. For more advance usage, see the
 * ExtractTextByArea and the DrawPrintTextLocations examples in this subproject, as well as the
 * ExtractText tool in the tools subproject.
 *
 * @author Tilman Hausherr
 */
public class SequentialWordCounter {

    /**
     * This will print the documents text page by page.
     *
     * @param args The command line arguments.
     * @throws IOException If there is an error parsing or extracting the document.
     */
    public static void main(String[] args) throws IOException {
        final GUI gui = new GUI();

        /*
        int cores = Runtime.getRuntime().availableProcessors();
        List<model.Worker> workers = new ArrayList<>();

        for(int i=0; i<cores; i++) {
            workers.add(new model.Worker(i));
        }
        */

    }


}
