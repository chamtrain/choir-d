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

package edu.stanford.survey.server.promis;

import edu.stanford.survey.server.CatAlgorithm.Item;
import edu.stanford.survey.server.CatAlgorithm.ItemBank;
import edu.stanford.survey.server.CatAlgorithm.Response;

import java.util.ArrayList;
import java.util.Collections;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

import org.apache.commons.lang3.ArrayUtils;
import org.json.JSONArray;
import org.json.JSONObject;

/**
 * Dynamically create an item bank from the JSON specification returned by the Assessment
 * Center API:
 * <p/>
 * http://www.assessmentcenter.net/ac_api/2012-01/Forms/{OID}.json
 * http://www.assessmentcenter.net/ac_api/2012-01/Calibrations/{OID}.json
 */
public class PromisItemBank implements ItemBank {
  private final double priorAlpha = 0;
  private final double priorBeta = 0;
  private final MyItem[] items;
  private final Map<String, Item> itemCodeToItem = new HashMap<>();
  private int minItems;
  private int maxItems;
  private double minError;

  public PromisItemBank(String bankJson, String calibrationJson) throws Exception {
    JSONObject bank = new JSONObject(bankJson);
    JSONObject calibration = new JSONObject(calibrationJson);

    JSONArray properties = calibration.getJSONArray("Properties");
    for (int i = 0; i < properties.length(); i++) {
      JSONObject property = properties.getJSONObject(i);
      if (property.has("MinNumItems")) {
        minItems = property.getInt("MinNumItems");
      }
      if (property.has("MaxNumItems")) {
        maxItems = property.getInt("MaxNumItems");
      }
      if (property.has("MaxStdErr")) {
        // Our algorithm uses scaled SE, so multiple by 10 here
        minError = property.getDouble("MaxStdErr") * 10;
      }
    }

    Map<String, JSONObject> itemCodeToItemJson = new HashMap<>();
    List<String> itemCodes = new ArrayList<>();

    JSONArray itemsJson = bank.getJSONArray("Items");
    for (int i = 0; i < itemsJson.length(); i++) {
      JSONObject item = itemsJson.getJSONObject(i);
      String itemCode = item.getString("ID");

      itemCodes.add(itemCode);
      itemCodeToItemJson.put(itemCode, item);
    }

    Collections.sort(itemCodes);

    List<MyItem> itemList = new ArrayList<>();
    for (String itemCode : itemCodes) {
      JSONObject item = itemCodeToItemJson.get(itemCode);

      MyItem newItem = createItem(itemCode, item, calibration);
      if (newItem != null) {
        itemList.add(newItem);
      }
    }
    items = itemList.toArray(new MyItem[itemList.size()]);

    for (MyItem item : items) {
      itemCodeToItem.put(item.code(), item);
      item.setBank(this);
    }
  }

  private MyItem createItem(String itemCode, JSONObject item, JSONObject calibration) throws Exception {
    String context;
    String prompt;
    JSONArray responsesJson;

    JSONArray elements = item.getJSONArray("Elements");
    if (elements.length() == 2) {
      context = "";
      prompt = elements.getJSONObject(0).getString("Description");
      responsesJson = elements.getJSONObject(1).getJSONArray("Map");
    } else if (elements.length() == 3) {
      context = elements.getJSONObject(0).getString("Description");
      prompt = elements.getJSONObject(1).getString("Description");
      responsesJson = elements.getJSONObject(2).getJSONArray("Map");
    } else {
      throw new Exception("Wrong number of elements: " + elements.length());
    }
    prompt = prompt.replaceAll("\\v", " ").trim();

    JSONObject itemCalibration = null;
    JSONArray itemCalibrations = calibration.getJSONArray("Items");
    for (int i = 0; i < itemCalibrations.length(); i++) {
      JSONObject jsonObject = itemCalibrations.getJSONObject(i);
      if (itemCode.equals(jsonObject.get("ID"))) {
        itemCalibration = jsonObject;
      }
    }
    if (itemCalibration == null) {
      System.err.println("Skipping item " + itemCode + " because it is not in the calibrations!");
      return null;
//      throw new Exception("Could not locate item " + itemCode + " in the calibrations");
    }

    double alpha = itemCalibration.getDouble("A_GRM");
    JSONArray responseCalibrations = itemCalibration.getJSONArray("Map");

    boolean reverse = false;
    double[] betas = new double[responsesJson.length() - 1];
    MyResponse[] responses = new MyResponse[responsesJson.length()];
    for (int i = 0; i < responsesJson.length(); i++) {
      JSONObject responseJson = responsesJson.getJSONObject(i);
      String text = responseJson.getString("Description");
      if (i + 1 != responseJson.getInt("Position")) {
        throw new Exception("Responses are out of order");
      }
      int value = responseJson.getInt("Value");
      String oid = responseJson.getString("ItemResponseOID");

      if (i < betas.length) {
        JSONObject responseCalibration = null;
        for (int j = 0; j < responseCalibrations.length(); j++) {
          if (oid.equals(responseCalibrations.getJSONObject(j).get("ItemResponseOID"))) {
            responseCalibration = responseCalibrations.getJSONObject(j);
            if (i == 0 && j == responseCalibrations.length() - 1) {
              reverse = true;
            }
          }
        }
        if (responseCalibration == null) {
          throw new Exception("Could not locate beta for item " + itemCode + " response " + oid);
        }
        betas[i] = responseCalibration.getDouble("Threshold");
        value = responseCalibration.getInt("StepOrder");
      } else {
        if (reverse) {
          value = 1;
        }
      }

      responses[i] = new MyResponse(text, value);
    }
    if (reverse) {
      ArrayUtils.reverse(betas);
    }

    return new MyItem(itemCode, context, prompt, "", alpha, betas, -1, "", responses);
  }

  public String toJavaString() {
    StringBuilder buf = new StringBuilder();
    buf.append("itemBank(");
    buf.append(priorAlpha);
    buf.append(", ");
    buf.append(priorBeta);
    buf.append(", ");
    buf.append(minItems);
    buf.append(", ");
    buf.append(maxItems);
    buf.append(", ");
    buf.append(minError);
    buf.append(",\n");
    boolean first = true;
    for (MyItem item : items) {
      if (first) {
        first = false;
      } else {
        buf.append(",\n");
      }
      item.toString(buf);
    }
    buf.append("\n  );\n");
    return buf.toString();
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

  private static class MyResponse implements Response {
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

    public void toString(StringBuilder buf) {
      buf.append("          response(");
      buf.append(quotedString(text));
      buf.append(", ");
      buf.append(difficulty);
      buf.append(")");
    }
  }

  private static class MyItem implements Item {
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

    public void toString(StringBuilder buf) {
      buf.append("      item(");
      buf.append(quotedString(code));
      buf.append(", ");
      buf.append(quotedString(context));
      buf.append(", ");
      buf.append(quotedString(prompt));
      buf.append(", ");
      buf.append(quotedString(brief));
      buf.append(", ");
      buf.append(alpha);
      buf.append(", new double[] { ");
      boolean first = true;
      for (double d : betas) {
        if (first) {
          first = false;
        } else {
          buf.append(", ");
        }
        buf.append(d);
      }
      buf.append(" }, ");
      buf.append(strata);
      buf.append(", ");
      buf.append(quotedString(category));
      buf.append(",\n");
      first = true;
      for (MyResponse response : responses) {
        if (first) {
          first = false;
        } else {
          buf.append(",\n");
        }
        response.toString(buf);
      }
      buf.append("\n      )");
    }
  }

  private static String quotedString(String str) {
    if (str == null) {
      return "";
    }
    return "\"" + str.replace("\\", "\\\\").replace("\"", "\\\"") + "\"";
  }
}
