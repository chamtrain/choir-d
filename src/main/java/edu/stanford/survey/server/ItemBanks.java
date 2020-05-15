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
import edu.stanford.survey.server.CatAlgorithm.ItemBank;
import edu.stanford.survey.server.CatAlgorithm.Response;

import java.util.HashMap;
import java.util.Map;

/**
 * Convenience methods for manipulating survey item banks.
 */
public class ItemBanks {
  public static ItemBank itemBank(double priorAlpha, double priorBeta, int minItems, int maxItems,
                                  double minError, final MyItem... items) {
    return new MyItemBank(priorAlpha, priorBeta, minItems, maxItems, minError, items);
  }

  public static MyItem item(final String code, final String context, final String prompt, final String brief,
                            final double alpha, final double[] betas, final int strata, final String category,
                            final MyResponse... responses) {
    return new MyItem(code, context, prompt, brief, alpha, betas, strata, category, responses);
  }

  public static MyResponse response(final String text, final int difficulty) {
    return new MyResponse(text, difficulty);
  }

  public static class MyResponse implements Response {
    private final String text;
    private final int difficulty;
    private Item item;

    public MyResponse(String text, int difficulty) {
      this.text = text;
      this.difficulty = difficulty;
    }

    private void setItem(Item item) {
      this.item = item;
    }

    @Override
    public Item item() {
      return item;
    }

    @Override
    public String text() {
      return text;
    }

    @Override
    public int difficulty() {
      return difficulty;
    }

    @Override
    public int index() {
      Response[] responses = item.responses();
      for (int i = 0; i < responses.length; i++) {
        if (responses[i] == this) {
          return i;
        }
      }
      return -1;
    }
  }

  public static class MyItem implements Item {
    private ItemBank bank;
    private final String code;
    private final String context;
    private final String prompt;
    private final String brief;
    private final MyResponse[] responses;
    private final double alpha;
    private final double[] betas;
    private final int strata;
    private final String category;

    public MyItem(String code, String context, String prompt, String brief, double alpha, double[] betas,
                  int strata, String category, MyResponse... responses) {
      this.code = code;
      this.context = context;
      this.prompt = prompt;
      this.brief = brief;
      this.responses = responses;
      this.alpha = alpha;
      this.betas = betas;
      this.strata = strata;
      this.category = category;

      for (MyResponse response : responses) {
        response.setItem(this);
      }
    }

    @Override
    public ItemBank bank() {
      return bank;
    }

    @Override
    public String code() {
      return code;
    }

    @Override
    public String context() {
      return context;
    }

    @Override
    public String prompt() {
      return prompt;
    }

    @Override
    public String promptBrief() {
      if (brief == null || brief.length() == 0) {
        return prompt;
      }
      return brief;
    }

    @Override
    public Response[] responses() {
      return responses;
    }

    @Override
    public Response response(String text) {
      if (text == null) {
        return null;
      }

      for (Response response : responses) {
        if (text.equals(response.text())) {
          return response;
        }
      }
      return null;
    }

    @Override
    public double alpha() {
      return alpha;
    }

    @Override
    public double[] betas() {
      return betas;
    }

    @Override
    public int strata() {
      return strata;
    }

    @Override
    public String category() {
      return category;
    }

    private void setBank(ItemBank bank) {
      this.bank = bank;
    }
  }

  private static class MyItemBank implements ItemBank {
    private final double priorAlpha;
    private final double priorBeta;
    private final MyItem[] items;
    private final Map<String, Item> itemCodeToItem = new HashMap<>();
    private int minItems;
    private int maxItems;
    private double minError;

    public MyItemBank(double priorAlpha, double priorBeta, int minItems, int maxItems, double minError, MyItem... items) {
      this.priorAlpha = priorAlpha;
      this.priorBeta = priorBeta;
      this.minItems = minItems;
      this.maxItems = maxItems;
      this.minError = minError;
      this.items = items;

      for (MyItem item : items) {
        item.setBank(this);
        itemCodeToItem.put(item.code(), item);
      }
    }

    @Override
    public Item[] items() {
      return items;
    }

    @Override
    public double priorAlpha() {
      return priorAlpha;
    }

    @Override
    public double priorBeta() {
      return priorBeta;
    }

    @Override
    public Item item(String itemCode) {
      if (itemCode == null) {
        return null;
      }
      return itemCodeToItem.get(itemCode);
    }

    @Override
    public Response response(String itemCode, String responseText) {
      Item item = item(itemCode);
      if (item != null) {
        return item.response(responseText);
      }
      return null;
    }

    @Override
    public int minItems() {
      return minItems;
    }

    @Override
    public int maxItems() {
      return maxItems;
    }

    @Override
    public double minError() {
      return minError;
    }
  }
}
