/*
 * Copyright 2013 The Board of Trustees of The Leland Stanford Junior University.
 * All Rights Reserved.
 *
 * See the NOTICE and LICENSE files distributed with this work for information
 * regarding copyright ownership and licensing. You may not use this file except
 * in compliance with a written license agreement with Stanford University.
 *
 * Unless required by applicable law or agreed to in writing, software
 * distributed under the License is distributed on an "AS IS" BASIS, WITHOUT
 * WARRANTIES OR CONDITIONS OF ANY KIND, either express or implied. See your
 * License for the specific language governing permissions and limitations under
 * the License.
 */

package edu.stanford.registry.server.imports;

import java.util.LinkedList;
import java.util.List;
import java.util.Queue;

public class ImportDefinitionQueue {
  Queue<ImportDefinition> queue = new LinkedList<>();
  String dataSource;

  public ImportDefinitionQueue(String source) {
    dataSource = source;
  }

  public void add(ImportDefinition definition) {
    queue.add(definition);
  }

  public ImportDefinition peek() {
    return queue.peek();
  }

  public ImportDefinition poll() {
    return queue.poll();
  }

  public int size() {
    return queue.size();
  }

  public List<ImportDefinition> getDefinitions() {
    List<ImportDefinition> copyList = new LinkedList<>();

    for (ImportDefinition aQueue : queue) {
      copyList.add(aQueue);

    }
    return copyList;

  }
}
