package edu.stanford.registry.server.utils;

import edu.stanford.registry.server.SiteInfo;
import edu.stanford.registry.shared.DataException;
import edu.stanford.registry.shared.survey.Constants;

import java.util.ArrayList;
import java.util.HashMap;
import java.util.Map;

import org.apache.log4j.Logger;
import org.w3c.dom.Element;
import org.w3c.dom.NodeList;

import com.github.susom.database.Database;

/**
 * Created by tpacht on 2/11/2016.
 */
public class TxSquareXml extends SquareXml {

  @SuppressWarnings("unused")
  private static final Logger logger = Logger.getLogger(TxSquareXml.class);


  private ArrayList<String> documentationLog = new ArrayList<String>();

  public TxSquareXml(Database database, SiteInfo siteInfo, String studyName, String prefix, boolean createDocumentation) throws DataException {
    super(database, siteInfo, studyName, prefix, createDocumentation);
  }
  public TxSquareXml(Database database, SiteInfo siteInfo, String studyName, boolean createDocumentation) throws DataException {
    super(database, siteInfo, studyName, getTxPrefix(studyName), createDocumentation);
  }

  /*
    Makes sure each item has an order number (questionId), all order #"s are unique
    Responses are of type input/select1/select/radio
    Response types input, select1 and radio have a <ref> attribute
    Response type select: each item child element has a <ref> attributes
    All ref attributes are unique to the survey
   */
  @Override
  public boolean checkItems( ) {

    Map<Number, Number> itemOrderMap = new HashMap<>();
    String tempMedColumnName = "";

    for (int itemInx = 0; itemInx < getItems().getLength(); itemInx++) {
      Element itemNode = (Element) getItems().item(itemInx);
      Integer itemOrder = RegistryAssessmentUtils.getIntegerAttribute(itemNode, Constants.ORDER, -1);
      ArrayList<String> questionText = RegistryAssessmentUtils.getAttributeOrElementList(itemNode, Constants.DESCRIPTION);

      // check unique Order on items
      if (!itemNode.hasAttribute(Constants.ORDER)) {
        logProblem("Item at index [" + itemInx + "] is missing the 'Order' attribute");
      } else if (itemOrderMap.containsKey(itemOrder)) {
        logProblem("Item at index [" + itemInx + "] has the same 'Order' number as the item at index ["
            + itemOrderMap.get(itemOrder) + "]");
      } else {
        itemOrderMap.put(itemOrder, itemInx);
      }

      Map<Number, Number> responseOrder = new HashMap<>();
      NodeList responseList = itemNode.getElementsByTagName(Constants.RESPONSE);
      for (int respInx = 0; respInx < responseList.getLength(); respInx++) {
        Element response = (Element) responseList.item(respInx);
        Integer respOrder = RegistryAssessmentUtils.getIntegerAttribute(response, Constants.ORDER, -1);
        if (!response.hasAttribute(Constants.ORDER)) {
          logProblem("Item[" + itemInx + "] with 'Order' " + itemOrder + " response [" + respInx
              + "] is missing the 'Order' attribute");
        } else if (responseOrder.containsKey(respOrder)) {
          logProblem(
              "Item[" + itemInx + "] with 'Order' " + itemOrder + " response [" + respInx + "] has the same 'Order' "
                  + respOrder + "  as "
                  + "response at index [" + responseOrder.get(respOrder) + "]");
        } else {
          responseOrder.put(respOrder, respInx);
        }

        if (!response.hasAttribute(Constants.TYPE)) {
          logProblem("Item [" + itemInx + "] with 'Order' " + itemOrder + ") response [" + respOrder + "] with 'Order' "
              + respOrder
              + " is missing the 'Type' attribute");
        } else {
          String type = response.getAttribute(Constants.TYPE);
          String ref = RegistryAssessmentUtils.getAttributeOrElementText(response, Constants.XFORM_REF);
          if ("input".equals(type) || "radio".equals(type)) {
            if (ref == null || ref.isEmpty() || "MEDOTHERS".equals(ref) || "TREATOTHERS".equals(ref)) {
              ref = type;
            }
            addRef(ref, itemOrder.toString() + ":" + respOrder.toString(), type);
            if (documentation) {
              int i = 0;
              if (questionText.size() > i) {
                if ("input".equals(type)) {
                  addQuestionText(questionText, "Text");
                } else {
                  addQuestionText(questionText, "Radio", "1");
                }
              }
            }
          } else if ("select1".equals(type) ) {
            NodeList select1ItemList = response.getElementsByTagName(Constants.XFORM_ITEM);
            for (int selectInx = 0; selectInx < select1ItemList.getLength(); selectInx++) {
              Element selectItem = (Element) select1ItemList.item(selectInx);
              ref = RegistryAssessmentUtils.getAttributeOrElementText(selectItem, Constants.XFORM_REF);
              String itemValue = RegistryAssessmentUtils.getAttributeOrElementText(selectItem, Constants.XFORM_VALUE);
              String itemLabel = RegistryAssessmentUtils.getAttributeOrElementText(selectItem, Constants.XFORM_LABEL);
              if (documentation) {
                addQuestionText(questionText,"Radio");
              }
              if ((ref == null || ref.isEmpty()) && ("false".equals(itemNode.getAttribute("Visible")))) {
                if (itemLabel.equals("Improved pain")) {
                  ref = tempMedColumnName + "_EFFECT";
                  addRef(ref, itemOrder.toString() + ":" + respOrder.toString(), type);

                }
                if (documentation) {
                  String colName = "";
                  if (ref != null && !ref.isEmpty())

                    colName = getColumnPrefix() + ref.toUpperCase();
                  documentationLog.add(
                      SquareDocumentationBuilder.option(colName, itemLabel, itemValue));
                }
              } else if (hasRef(ref)) {
                logProblem(
                  "Item [" + itemInx + "] with 'Order' " + itemOrder + ") response [" + respInx + "] with 'Order' "
                      + respOrder
                      + " reference " + ref.toUpperCase() + "  is not unique ");
              } else {
                addRef(ref, itemOrder.toString() + ":" + respOrder.toString(), type);
                if (documentation) {
                  documentationLog.add(SquareDocumentationBuilder.option(
                      getColumnPrefix() + ref.toUpperCase(), itemLabel, "1"));
                }
              }

            }
          } else if ("select".equals(type)) {
            if (documentation) {
              addQuestionText(questionText, "Checkbox" );
            }
            NodeList selectItemList = response.getElementsByTagName(Constants.XFORM_ITEM);
            for (int selectInx = 0; selectInx < selectItemList.getLength(); selectInx++) {
              // use either a ref defined for the item or the ref from the response
              Element selectItem = (Element) selectItemList.item(selectInx);
              String itemRef = RegistryAssessmentUtils.getAttributeOrElementText(selectItem, Constants.XFORM_REF);
              String itemValue = RegistryAssessmentUtils.getAttributeOrElementText(selectItem, Constants.XFORM_VALUE);
              String itemLabel = RegistryAssessmentUtils.getAttributeOrElementText(selectItem, Constants.XFORM_LABEL);
              String itemLabels[] = itemLabel.split(" ");
              if (itemRef == null || itemRef.isEmpty()) {
                if (ref != null && !ref.isEmpty() && itemValue != null && !itemRef.isEmpty()) {
                  itemRef = ref + itemRef;
                }
              }

              if (itemRef == null || itemRef.isEmpty()) {
                String medCol = "";
                if (itemLabel.equals("Still taking")) {
                  medCol = tempMedColumnName + "_STILLTAKE";
                } else if (itemLabel.equals("Effective")) {
                  medCol = tempMedColumnName + "_EFFECTIVE";
                } else if (itemLabel.equals("Experienced side effects")) {
                  medCol = tempMedColumnName + "_SIDEFFECT";
                }
                else {
                  tempMedColumnName = makeColumnName(itemLabels[0]);;
                  if (tempMedColumnName.length() > 10) {
                    tempMedColumnName = tempMedColumnName.substring(0, 10);
                  }

                  medCol = tempMedColumnName;

                }
                addRef(medCol, itemOrder.toString() + ":" + respOrder.toString() + ":" + itemValue, type);
                if (documentation) {
                  documentationLog.add(SquareDocumentationBuilder.option(getColumnPrefix() + medCol.toUpperCase(), itemLabel, "1"));
                }
              } else {
                if (hasRef(itemRef)) {
                  logProblem(
                      "Item [" + itemInx + "] with 'Order' " + itemOrder + ") response [" + respInx + "] with 'Order' "
                          + respOrder
                          + " reference " + itemRef.toUpperCase() + "  is not unique ");
                } else {
                  addRef(itemRef, itemOrder.toString() + ":" + respOrder.toString() + ":" + itemValue, type);
                  if (documentation) {
                    documentationLog.add(SquareDocumentationBuilder.option(getColumnPrefix() +itemRef.toUpperCase(), itemLabel, "1"));
                  }
                }
              }
            }
          } else { // not supporting 'map' yet
            logProblem("Cannot handle response type " + type);
          }
        }
      }
    }
    return isValid();
  }

  public static String getTxPrefix(String treatmentXml) {
    if (treatmentXml.startsWith("med")) {
      if (treatmentXml.length() < 9) {
        return "M_" + treatmentXml.substring(4).toUpperCase() + "_";
      }
      return  "M_" + treatmentXml.substring(4, 8).toUpperCase() + "_";
    } else {
      if (treatmentXml.length() < 11) {
        return "T_" + treatmentXml.toUpperCase() + "_";
      }
      if (treatmentXml.length() < 15) {
        return "T_" + treatmentXml.substring(10).toUpperCase() + "_";
      }
      return "T_" + treatmentXml.substring(10, 14).toUpperCase() + "_";
    }
  }

  private void addQuestionText(ArrayList<String> questionText, String fieldType) {
    documentationLog.addAll(SquareDocumentationBuilder.question(questionText, "", fieldType, ""));
  }

  private void addQuestionText(ArrayList<String> questionText, String fieldType, String value) {
    documentationLog.addAll(SquareDocumentationBuilder.question(questionText, "", fieldType, value));
  }
}
