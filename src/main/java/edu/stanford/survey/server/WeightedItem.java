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

package edu.stanford.survey.server;

import edu.stanford.survey.server.CatAlgorithm.Item;

/**
 * Wrapper class for keeping track of both an item and its information content.
 */
class WeightedItem implements Comparable<WeightedItem> {
  private final Item item;
  private final double informationContent;

  public WeightedItem(Item item, double informationContent) {
    this.item = item;
    this.informationContent = informationContent;
  }

  public Item getItem() {
    return item;
  }

  public double getInformationContent() {
    return informationContent;
  }

  @Override
  public int compareTo(WeightedItem weightedItem) {
    return Double.compare(weightedItem.informationContent, informationContent);
  }

  @Override
  public String toString() {
    return item.code() + "=" + informationContent;
  }
}
