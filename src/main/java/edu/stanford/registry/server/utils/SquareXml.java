package edu.stanford.registry.server.utils;

import edu.stanford.registry.server.SiteInfo;
import edu.stanford.registry.shared.DataException;
import edu.stanford.registry.shared.survey.Constants;
import edu.stanford.survey.client.api.FieldType;

import java.io.IOException;
import java.io.StringReader;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.HashMap;
import java.util.LinkedHashMap;
import java.util.Map;

import javax.xml.parsers.DocumentBuilder;
import javax.xml.parsers.DocumentBuilderFactory;
import javax.xml.parsers.ParserConfigurationException;

import org.apache.log4j.Logger;
import org.w3c.dom.Document;
import org.w3c.dom.Element;
import org.w3c.dom.NodeList;
import org.xml.sax.InputSource;
import org.xml.sax.SAXException;

import com.github.susom.database.Database;

/**
 * Created by tpacht on 2/11/2016.
 */
public class SquareXml  {

  private static final Logger logger = Logger.getLogger(SquareXml.class);

  @SuppressWarnings("unused")
  private Database database;

  @SuppressWarnings("unused")
  private String studyName;

  private boolean filevalid = true;
  private String columnPrefix;
  private NodeList itemList;
  public boolean documentation;
  LinkedHashMap<String, String> refItemResp = new LinkedHashMap<>();
  public LinkedHashMap<String, String> columns = new LinkedHashMap<>();
  LinkedHashMap<String, String> refItemRespGroup = new LinkedHashMap<>();

  private ArrayList<String> problemLog = new ArrayList<>();
  private ArrayList<String> documentationLog = new ArrayList<String>();


  public SquareXml(Database database, SiteInfo siteInfo, String studyName, String prefix) {
    this(database, siteInfo, studyName, prefix, false);
  }

  public SquareXml(Database database, SiteInfo siteInfo, String studyName, String prefix, boolean createDocumentation) throws DataException {

    this.studyName = studyName;
    this.columnPrefix = prefix;
    this.documentation = createDocumentation;
    String xmlDocumentString = XMLFileUtils.getInstance(siteInfo).getXML(database, studyName);
    if (xmlDocumentString == null) {
      throw new DataException("File not found");
    }
    try {

      DocumentBuilderFactory dbf = DocumentBuilderFactory.newInstance();
      DocumentBuilder db = dbf.newDocumentBuilder();
      InputSource is = new InputSource();
      is.setCharacterStream(new StringReader(xmlDocumentString));

      Document messageDom = db.parse(is);
      Element docElement = messageDom.getDocumentElement();
      if (docElement.getTagName().equals(Constants.FORM)) {
        NodeList itemsList = messageDom.getElementsByTagName("Items");
        itemList = null;
        if (itemsList != null && itemsList.getLength() > 0) {
          itemList = ((Element) itemsList.item(0)).getElementsByTagName("Item");
        }
        if (itemsList == null || itemsList.getLength() < 1) {
          itemList = messageDom.getElementsByTagName("Item");
        }
        checkItems();
      } else {
        throw new DataException("docElement isn't form its " + docElement.getTagName());
      }
    } catch (ParserConfigurationException | IOException | SAXException ex) {
      throw new DataException("Error parsing docuement " + studyName + ".xml");
    }
  }

  /*
    Makes sure each item has an order number (questionId), all order #"s are unique
    Responses are of type input/select1/select/radio
    Response types input, select1 and radio have a <ref> attribute
    Response type select: each item child element has a <ref> attributes
    All ref attributes are unique to the survey
   */
  public boolean checkItems( ) {
    logger.info("in squarexml.checkItems");
    Map<Number, Number> itemOrderMap = new HashMap<>();

    for (int itemInx = 0; itemInx < itemList.getLength(); itemInx++) {
      Element itemNode = (Element) itemList.item(itemInx);
      Integer itemOrder = RegistryAssessmentUtils.getIntegerAttribute(itemNode, Constants.ORDER, -1);
      ArrayList<String> questionText = RegistryAssessmentUtils.getAttributeOrElementList(itemNode, Constants.DESCRIPTION);
      boolean isRadio = false;
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
          if ("radio".equals(type)) {
            ref = RegistryAssessmentUtils.getAttributeOrElementText(itemNode, Constants.XFORM_REF);
          }
          if (isEmpty(ref) && !ignoreType(type) && !("select".equals(type))) { // && respInx == 0 ) {
            if (!emptySelect1(response)) {
              String msg = "radio".equals(type) ? " " : " response [" + respInx + "] ";
              logProblem(
                  "Item[" + itemInx + "] with 'Order' " + itemOrder + msg
                      + " missing required <ref/> tag required for type " + type);
            }
          } else {
            if ("input".equals(type) || "select1".equals(type) || "datePicker".equals(type) || "slider".equals(type)
                || "dropdown".equals(type)) {
              addRef(ref, itemOrder.toString() + ":" + respOrder.toString(), type);
              if (documentation) {
                String label = RegistryAssessmentUtils.getAttributeOrElementText(response, Constants.XFORM_LABEL);
                String dataType = null;
                if ("input".equals(type) || "datePicker".equals(type)) {
                  if (response.getElementsByTagName(Constants.XFORM_FORMAT).getLength() > 0) {
                    dataType = RegistryAssessmentUtils.getAttributeOrElementText((Element) response.getElementsByTagName(Constants.XFORM_FORMAT).item(0), "datatype");
                  }
                  if (dataType == null || dataType.isEmpty()) {
                    dataType = "text";
                  }
                  addQuestionText(questionText,columnPrefix + ref.toUpperCase() , dataType, label);
                } else if ("select1".equals(type) || "dropdown".equals(type)) {
                  String displayType = type;
                  if ("select1".equals(type)) {
                    displayType = "radio";
                  }
                  if (!label.isEmpty()) {
                    if (questionText != null) {
                      addQuestionText(questionText, "", displayType, "");
                    }
                    addResponseText( label, columnPrefix + ref.toUpperCase() );
                  } else {
                    addQuestionText(questionText, columnPrefix + ref.toUpperCase(), displayType, "");
                  }
                  NodeList select1ItemList = response.getElementsByTagName(Constants.XFORM_ITEM);
                  for (int selectInx = 0; selectInx < select1ItemList.getLength(); selectInx++) {
                    Element selectItem = (Element) select1ItemList.item(selectInx);
                    String itemValue = RegistryAssessmentUtils.getAttributeOrElementText(selectItem, Constants.XFORM_VALUE);
                    String itemLabel = RegistryAssessmentUtils.getAttributeOrElementText(selectItem, Constants.XFORM_LABEL);
                    // documentationLog.add(SquareDocumentationBuilder.option(itemLabel, itemValue));
                    addOption(itemLabel.toUpperCase(), itemValue);
                  }
                } else if ("slider".equals(type)) {
                  addQuestionText(questionText, columnPrefix + ref.toUpperCase(), "slider", "Number");
                }
              }
            } else if ("select".equals(type)) {
              NodeList selectItemList = response.getElementsByTagName(Constants.XFORM_ITEM);
              if (documentation) {
                addQuestionText(questionText, "", "checkbox", "");
              }
              boolean showNote = false;
              for (int selectInx = 0; selectInx < selectItemList.getLength(); selectInx++) {
                // use either a ref defined for the item or the ref from the response
                Element selectItem = (Element) selectItemList.item(selectInx);
                String itemRef = RegistryAssessmentUtils.getAttributeOrElementText(selectItem, Constants.XFORM_REF);
                String itemValue = RegistryAssessmentUtils.getAttributeOrElementText(selectItem, Constants.XFORM_VALUE);
                String itemLabel = RegistryAssessmentUtils.getAttributeOrElementText(selectItem, Constants.XFORM_LABEL);
                // If no ref tag on the item but there is one on the response (and there are > 1 items)
                // try appending part of the items label to the response ref tag to make the column names unique.
                if (itemRef == null || itemRef.isEmpty()) {
                  if (ref != null && !ref.isEmpty() && onlyLetters(itemLabel) != null ) {
                    if (selectItemList.getLength() > 1) {
                      itemRef = ref + getFirst(onlyLetters(itemLabel).toUpperCase(), 8);
                      showNote = true;
                    } else {
                      itemRef = ref;
                    }
                  }
                }
                if (isEmpty(itemRef)) {
                  String msg =  " select response [" + respInx + "] item [" + itemInx+"]";
                  logProblem(
                      "Item[" + itemInx + "] with 'Order' " + itemOrder + msg + " missing required <ref/> tag required for type " + type);
                } else {
                  if (hasRef(itemRef)) {
                    logProblem(
                        "Item [" + itemInx + "] with 'Order' " + itemOrder + ") response [" + respInx + "] with 'Order' "
                            + respOrder
                            + " reference " + itemRef.toUpperCase() + "  is not unique ");
                  } else {
                    addRef(itemRef, itemOrder.toString() + ":" + respOrder.toString() + ":" + itemValue, type, ref);
                    if (documentation) {
                      String note = showNote ? " *" : "";
                      addOption(columnPrefix + itemRef.toUpperCase() + note, itemLabel, "1");
                    }
                  }
                }
              }
              if (showNote) {
                addQuestionText(getNote(), "", "", "");
              }
            } else if ("radio".equals(type)) {
              // only need to do this section once for all radio option's
              String itemRef = RegistryAssessmentUtils.getAttributeOrElementText(itemNode, Constants.XFORM_REF);
              if (!isRadio) {
                if (itemRef == null || itemRef.isEmpty()) {
                  if (questionText != null && questionText.size() > 0 && !questionText.get(0).isEmpty()) {
                    String[] text = questionText.get(0).split(" ");
                    itemRef = "";
                    for (int t = 0; t < text.length; t++) {
                      itemRef = itemRef + text[t];
                    }
                  } else {
                    itemRef = itemOrder.toString();
                  }
                }
                if ((columnPrefix + itemRef).length() > 30) {
                  itemRef = itemRef.substring(0, (29 - columnPrefix.length()));
                }
                if (itemRef == null || itemRef.isEmpty()) {
                  logProblem("Item [" + itemInx + "] with 'Order' " + itemOrder
                      + " Unable to create a reference for radio type");
                } else if (hasRef(itemRef)) {
                  logProblem(
                      "Item [" + itemInx + "] with 'Order' " + itemOrder + ") response [" + respInx + "] with 'Order' "
                          + respOrder
                          + " reference " + itemRef  + "  is not unique ");
                } else {
                  addRef(itemRef, itemOrder.toString(), type);
                  if (documentation) {
                    addQuestionText(questionText, columnPrefix + itemRef.toUpperCase(), "radio", "");
                  }
                }
                isRadio = true;

              }
              if (documentation) {
                String description = RegistryAssessmentUtils.getAttributeOrElementText(response, Constants.DESCRIPTION);
                if (description != null) {
                  String value = description;
                  description = description + " ";
                  String description2 = RegistryAssessmentUtils.getAttributeOrElementText(response,
                      Constants.DESCRIPTION + "2");
                  if (description2 != null) {
                    description = description + description2;
                  }
                  addOption(description, value);
                }
              }

            } else  if ("textboxset".equalsIgnoreCase(type)) {
              if (documentation) {
                addQuestionText(questionText, "", "textboxset", "");
              }
              NodeList select1ItemList = response.getElementsByTagName(Constants.XFORM_ITEM);
              for (int selectInx = 0; selectInx < select1ItemList.getLength(); selectInx++) {
                Element selectItem = (Element) select1ItemList.item(selectInx);
                String itemLabel = RegistryAssessmentUtils.getAttributeOrElementText(selectItem, Constants.XFORM_LABEL);
                addRef(ref+ Integer.toString(selectInx + 1), itemOrder.toString() + ":" + respOrder.toString() + ":"  + Integer.toString(selectInx + 1), "textboxset");
                if (documentation) {
                  addOption(columnPrefix + ref + Integer.toString(selectInx + 1), "string", itemLabel, "");
                }
              }
            } else if ("radiosetgrid".equalsIgnoreCase(type)) {
              NodeList selectItemList = response.getElementsByTagName(Constants.XFORM_ITEM);
              if (documentation) {
                boolean ranking =
                  (response.hasAttribute("Ranking") && RegistryAssessmentUtils.getBooleanAttribute(response, "Ranking" ));
                addQuestionText(questionText, "", "radiosetgrid", "Ranking ," + ranking);
              }
              ArrayList<Element> xElements = new ArrayList<>();
              ArrayList<Element> yElements = new ArrayList<>();
              for (int i=0; i< selectItemList.getLength(); i++) {
                Element selectItem = (Element) selectItemList.item(i);
                if ("x-axis".equalsIgnoreCase(RegistryAssessmentUtils.getAttributeOrElementText(selectItem, "group" ))) {
                  xElements.add(selectItem);
                } else if ("y-axis".equalsIgnoreCase(RegistryAssessmentUtils.getAttributeOrElementText(selectItem, "group" ))) {
                  yElements.add(selectItem);
                }
              }
              for (Element yElement : yElements) {
                ArrayList<String> yItemLabels = new ArrayList<>();
                yItemLabels.add(RegistryAssessmentUtils.getAttributeOrElementText(yElement, Constants.XFORM_LABEL));
                String yItemRef = RegistryAssessmentUtils.getAttributeOrElementText(yElement, Constants.XFORM_REF);
                if (isEmpty(yItemRef)) {
                  String msg =  "  response [" + respInx + "] item [" + itemInx+"]";
                  logProblem(
                      "Item[" + itemInx + "] with 'Order' " + itemOrder + msg + " missing required <ref/> tag required for type " + type);
                } else if (hasRef(yItemRef)) {
                    logProblem(
                        "Item [" + itemInx + "] with 'Order' " + itemOrder + ") response [" + respInx + "] with 'Order' "
                            + respOrder
                            + " reference " + yItemRef.toUpperCase() + "  is not unique ");
                } else {
                  if ((columnPrefix + yItemRef).length() > 30) {
                    yItemRef = yItemRef.substring(0, (29 - columnPrefix.length()));
                  }
                  addRef(yItemRef, itemOrder.toString() + ":" + respOrder.toString() , type, ref);
                  if (documentation) {
                   //addQuestionText(yItemLabels, columnPrefix +yItemRef.toUpperCase(), "", "");
                    addResponseText(RegistryAssessmentUtils.getAttributeOrElementText(yElement, Constants.XFORM_LABEL), columnPrefix +yItemRef.toUpperCase());
                    for (Element xElement : xElements) {
                      String xLabel = RegistryAssessmentUtils.getAttributeOrElementText(xElement, Constants.XFORM_LABEL);
                      String xValue = RegistryAssessmentUtils.getAttributeOrElementText(xElement, Constants.XFORM_VALUE);
                      addOption( "", "", xLabel, xValue);
                    }
                  }
                }
              }
            } else  if (ignoreType(type)) {


            } else {
              // not supporting 'map' yet
              logProblem("Cannot handle response type " + type);
            }
          }

        }
        questionText = null;
      }
    }   return isValid();
  }

  public boolean isValid() {
    return filevalid;
  }
  public void setValid(boolean b) {
    filevalid = b;
  }

  public LinkedHashMap<String, String> getReferences() {
    return refItemResp;
  }

  public boolean hasRef(String ref) {
    if (ref == null || ref.isEmpty()) {
      return false;
    }
    if (refItemResp.containsKey( ref )) {
      return true;
    } else {
      return false;
    }
  }

  public void addRef(String ref, String val, String type) {

    if (columns.get(columnPrefix + ref.toUpperCase()) != null) {
      // Try adding the item number to the name to make it unique
      if (val.indexOf(":") > 0) {
        ref = ref + val.substring(0, val.indexOf(":"));
      }
    }

    if (30 < (columnPrefix + ref).length()) {
      logProblem("The column value " +  columnPrefix + ref.toUpperCase()
          + " makes a column name which is > 30 characters long");
    }


    if (columns.get(columnPrefix + ref.toUpperCase()) != null) {
        logProblem("The column " + columnPrefix + ref.toUpperCase() + " is not unique");
        setValid(false);
    }
    refItemResp.put(ref, val);
    columns.put(columnPrefix + ref.toUpperCase(), type);
  }

  public void addRef(String ref, String val, String type, String groupRef) {
    addRef(ref, val, type);
    if (groupRef != null) {
      refItemRespGroup.put(ref, groupRef);
    }

  }

  public LinkedHashMap<String, String> getGroupRefs() {
    return refItemRespGroup;
  }

  public LinkedHashMap<String, String> getColumns() {
    return columns;
  }

  public NodeList getItems() {
    return itemList;
  }

  public void logProblem(String problem) {
    //logger.error(problem);
    problemLog.add(problem);
    setValid(false);
  }

  public ArrayList<String> getProblemLog() {
    return problemLog;
  }
  public ArrayList<String> getDocumentationLog() { return documentationLog; }

  private void addOption(String columnName, String description, String value) {
    logger.trace("addOption: " + columnName + " " + description + " " + value);
    documentationLog.add(SquareDocumentationBuilder.option(columnName, description, value));
  }
  private void addOption(String description, String value) {
    logger.trace("addOption: " +  description + " " + value);
    documentationLog.add(SquareDocumentationBuilder.option(description, value));
  }

  private void addOption(String columnName, String fieldType, String description, String value) {
    documentationLog.add(SquareDocumentationBuilder.option(columnName, fieldType, description, value));
  }
  private void addQuestionText(ArrayList<String> questionText, String columnName, String fieldType, String value) {
    logger.trace("addQuestion: " + columnName + " " + fieldType + " " + value);
      documentationLog.addAll(SquareDocumentationBuilder.question(questionText, columnName, fieldType, value));
  }

  private void addResponseText(String responseLabel, String columnName) {
    ArrayList<String> textStrings = new ArrayList<>();
    textStrings.add(responseLabel);
    documentationLog.addAll(SquareDocumentationBuilder.question(textStrings, columnName, "", ""));
  }
  private String getFirst(String str, int size) {
    if (str == null || str.isEmpty()) {
      return "";
    }
    if (str.length() <= size) {
      return str;
    }
    return str.substring(0, size-1);
  }

  public boolean isEmpty(String str) {
    return  (str == null || str.length() ==0) ? true : false;
  }

  public String onlyLetters(String str) {
    if (!isEmpty(str)) {
      return str.replaceAll("[^a-zA-Z]+","");
    }
    return str;
  }

  public String makeColumnName(String str) {
    if (str == null) {
      return "";
    }
    return str.toUpperCase().replaceAll("[^a-zA-Z]+","");
  }

  public String getColumnPrefix() {
    return columnPrefix;
  }

  public LinkedHashMap<String, FieldType> getColumnTypes() {
    LinkedHashMap<String, FieldType> returnColumns = new LinkedHashMap<>();
    for (final String columnName : columns.keySet()) {
      String type = columns.get(columnName);
      if ("select1".equals(type) || "radio".equals(type) || "slider".equals(type) || "dropdown".equals(type)) {
        returnColumns.put(columnName, FieldType.radios);
      } else if ("input".equals(type) || "datePicker".equals(type)) {
        returnColumns.put(columnName, FieldType.text);
      } else if ("select".equals(type)) {
        returnColumns.put(columnName, FieldType.checkboxes);
      } else {
        throw new DataException("CAN NOT translate column type " + type + " to fieldType!");
      }
    }
    return returnColumns;
  }

  private boolean ignoreType(String type) {
    if ("collapsible".equals(type)) {
      return  true;
    } else {
      return false;
    }
  }

  private boolean emptySelect1(Element response) {
    if ("select1".equals(response.getAttribute(Constants.TYPE))) {
      NodeList select1ItemList = response.getElementsByTagName(Constants.XFORM_ITEM);
      if (select1ItemList == null || select1ItemList.getLength() == 0) {
        return true;
      }
    }
    return false;
  }

  private ArrayList<String> getNote() {
    return new ArrayList<>(  Arrays.asList(
    "NOTE: columns marked with '*' are from a shared ref tag",
    "customize addCompletedSurveyValues to get survey responses") );
  }
}
