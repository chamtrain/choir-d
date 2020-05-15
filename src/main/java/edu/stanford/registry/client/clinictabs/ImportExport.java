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

package edu.stanford.registry.client.clinictabs;

import edu.stanford.registry.client.RegistryCssResource;
import edu.stanford.registry.client.RegistryResources;
import edu.stanford.registry.client.RegistryRpcRequestBuilder;
import edu.stanford.registry.client.service.AdminService;
import edu.stanford.registry.client.service.AdminServiceAsync;
import edu.stanford.registry.client.survey.FormWidgets;
import edu.stanford.registry.client.utils.ClinicUtils;
import java.io.IOException;
import java.util.ArrayList;
import java.util.logging.Level;
import java.util.logging.Logger;

import org.gwtbootstrap3.client.ui.Anchor;
import org.gwtbootstrap3.client.ui.Button;
import org.gwtbootstrap3.client.ui.Form;
import org.gwtbootstrap3.client.ui.FormGroup;
import org.gwtbootstrap3.client.ui.FormLabel;
import org.gwtbootstrap3.client.ui.Heading;
import org.gwtbootstrap3.client.ui.Panel;
import org.gwtbootstrap3.client.ui.PanelBody;
import org.gwtbootstrap3.client.ui.PanelHeader;
import org.gwtbootstrap3.client.ui.Well;
import org.gwtbootstrap3.client.ui.constants.ButtonType;
import org.gwtbootstrap3.client.ui.constants.ColumnSize;
import org.gwtbootstrap3.client.ui.constants.FormType;
import org.gwtbootstrap3.client.ui.constants.HeadingSize;
import org.gwtbootstrap3.client.ui.constants.IconType;
import org.gwtbootstrap3.client.ui.constants.PanelType;
import org.gwtbootstrap3.client.ui.constants.Toggle;
import org.gwtbootstrap3.extras.select.client.ui.Option;
import org.gwtbootstrap3.extras.select.client.ui.Select;
import org.restlet.client.Request;
import org.restlet.client.Response;
import org.restlet.client.Uniform;
import org.restlet.client.data.ChallengeScheme;
import org.restlet.client.data.MediaType;
import org.restlet.client.ext.json.JsonRepresentation;
import org.restlet.client.resource.ClientResource;

import com.google.gwt.core.client.GWT;
import com.google.gwt.dom.client.Style.Unit;
import com.google.gwt.event.dom.client.ClickEvent;
import com.google.gwt.event.dom.client.ClickHandler;
import com.google.gwt.json.client.JSONArray;
import com.google.gwt.json.client.JSONObject;
import com.google.gwt.json.client.JSONString;
import com.google.gwt.safehtml.shared.SafeHtmlUtils;
import com.google.gwt.user.client.rpc.AsyncCallback;
import com.google.gwt.user.client.ui.DockLayoutPanel;
import com.google.gwt.user.client.ui.FileUpload;
import com.google.gwt.user.client.ui.FlowPanel;
import com.google.gwt.user.client.ui.FormPanel;
import com.google.gwt.user.client.ui.FormPanel.SubmitCompleteEvent;
import com.google.gwt.user.client.ui.HTML;
import com.google.gwt.user.client.ui.Tree;
import com.google.gwt.user.client.ui.TreeItem;
import com.google.gwt.user.client.ui.Widget;

public class ImportExport extends TabWidget implements ClickHandler, RegistryTabIntf {

  protected final AdminServiceAsync adminService = GWT.create(AdminService.class);

  private Button uploadFileButton = new Button("Upload  ");
  private Button jsonButton = new Button("Test Send sample json data");
  private Button loadPendingButton = new Button("Load pending files");

  private DockLayoutPanel mainPanel = new DockLayoutPanel(Unit.EM);
  private Panel subPanel = new Panel(PanelType.DEFAULT);

  private FormPanel form = new FormPanel();
  Form displayForm = new Form(FormType.DEFAULT);

  private FileUpload uploadData = new FileUpload();
  private Select definitionListBox = new Select();
  final HTML responseHTML = new HTML();
  private final Logger logger = Logger.getLogger(ImportExport.class.getName());
  private static final RegistryCssResource css = RegistryResources.INSTANCE.css();

  public ImportExport(ClinicUtils clinicUtils) {
    super(clinicUtils);
    initWidget(mainPanel);
  }

  @Override
  public void load() {
    if (isLoaded()) {
      logger.log(Level.INFO, "already loaded");
      return;
    }
    setServiceEntryPoint(adminService, "admin");
    loadDefinitionList();
    setEmptyMessage();
    mainPanel.addNorth(getMessageBar(), 4);
    uploadData.getElement().setAttribute("style", "width: 500px; height: 40px; padding: 0;");
    jsonButton.setTitle("json test");
    jsonButton.addClickHandler(this);

    uploadFileButton.setType(ButtonType.DEFAULT);
    uploadFileButton.setTitle("Upload an Import from file ");
    uploadFileButton.addClickHandler(this);

    loadPendingButton.setType(ButtonType.DEFAULT);
    loadPendingButton.setTitle("Load pending files ");
    loadPendingButton.addClickHandler(this);


    Form uploadForm = new Form(FormType.DEFAULT);

    FormGroup uploadFormGroup = new FormGroup();
    FormLabel fileLabel = FormWidgets.formLabelFor("Data file", "dataFile", ColumnSize.LG_2);
    uploadFormGroup.add(fileLabel);

    uploadData.getElement().setId("dataFile");
    form.add(uploadData);
    form.addSubmitCompleteHandler(new FormPanel.SubmitCompleteHandler() {
      @Override
      public void onSubmitComplete(SubmitCompleteEvent event) {
        String result = event.getResults();
        if (result == null) {
          setErrorMessage("No response from server!");
        } else {
          if (result.contains("SUCCESS")) {
            setSuccessMessage("File has been loaded");
          } else {
            setErrorMessage("File not loaded, server responsed: " + result);
          }
        }
      }
    });
    uploadFormGroup.add(form);
    uploadForm.add(uploadFormGroup);

    FormGroup definitionGroup = new FormGroup();
    definitionGroup.add(FormWidgets.formLabelFor("Data type", "definition", ColumnSize.LG_2));
    definitionListBox.getElement().setId("definition");
    FlowPanel listBoxPanel = new org.gwtbootstrap3.client.ui.gwt.FlowPanel();
    listBoxPanel.addStyleName(css.leftButton());
    listBoxPanel.add(definitionListBox);
    definitionGroup.add(listBoxPanel);
    FlowPanel buttonPanel = new org.gwtbootstrap3.client.ui.gwt.FlowPanel();
    buttonPanel.addStyleName(css.leftButton());
    buttonPanel.add(uploadFileButton);
    definitionGroup.add(buttonPanel);
    uploadForm.add(definitionGroup);
    Well uploadWell = new Well();
    uploadWell.setHeight("130px");
    uploadWell.add(uploadForm);
    displayForm.add(uploadWell);


    Form pendingForm = new Form();
    FormLabel pendingLabel = FormWidgets.formLabelFor("Import directory", "importDir", ColumnSize.LG_2);
    FormGroup pendingGroup = new FormGroup();
    loadPendingButton.getElement().setId("importDir");
    pendingGroup.addStyleName(css.leftButton());
    pendingGroup.add(loadPendingButton);
    pendingForm.add(pendingLabel);
    pendingForm.add(pendingGroup);

    Well pendingWell = new Well();
    pendingWell.setHeight("100px");
    pendingWell.add(pendingForm);
    displayForm.add(pendingWell);

    PanelBody panelBody = new PanelBody();
    panelBody.add(displayForm);

    PanelHeader panelHeader = new PanelHeader() {{
      setDataToggle( Toggle.COLLAPSE );
      setDataParent( getId() );
      add( new Heading( HeadingSize.H4 ) {{
        add( new Anchor() {{
          setIcon( IconType.UPLOAD );
          setText( "Import Data" );
        }} );
      }} );
    }};

    subPanel.add(panelHeader);
    subPanel.add(panelBody);
    mainPanel.add(subPanel);
    setLoaded(true);
    logger.log(Level.INFO, "Finished loading");
  }

  static private class ApptListJSON {

    public JsonRepresentation getJSON() {

      JSONArray jsonObject = new JSONArray();

      JSONString name = new JSONString("APPOINTMENTS");
      JSONArray lines = new JSONArray();
      lines.set(0, new JSONString("10001-1"));
      lines.set(1, new JSONString("first-name"));

      jsonObject.set(0, name);
      jsonObject.set(1, lines);

      JsonRepresentation rep = new JsonRepresentation(MediaType.ALL, jsonObject);
      return rep;
    }
  }

  @Override
  public void onClick(ClickEvent event) {
    Widget sender = (Widget) event.getSource();
    setEmptyMessage();
    if (sender == uploadFileButton) {
      doUploadFile();
    }

    if (sender == jsonButton) {
      doSendJson();
    }
    if (sender == loadPendingButton) {
      doLoadFiles();
    }
  }

  public void loadDefinitionList() {
    adminService.getFileImportDefinitions(new AsyncCallback<ArrayList<String>>() {
      @Override
      public void onFailure(Throwable caught) {
        setErrorMessage("Load definitions failed " + caught.getMessage());
      }

      @Override
      public void onSuccess(ArrayList<String> result) {
        for (String aResult : result) {
          Option opt = new Option();
          opt.setText(aResult);
          opt.setValue(aResult);
          definitionListBox.add(opt);
        }
        if (result.size() > 0) {
          definitionListBox.setValue(result.get(0));
        }
        definitionListBox.refresh();
      }
    });
    definitionListBox.setWidth("200px");
  }

  public void doLoadFiles() {
    if (definitionListBox != null && definitionListBox.getSelectedItem() != null) {
      adminService.loadPendingImports( definitionListBox.getSelectedItem().getValue() ,
          new AsyncCallback<Integer>() {
            @Override
            public void onFailure(Throwable caught) {
              setErrorMessage("Data import failed," + caught.getMessage());
            }

            @Override
            public void onSuccess(Integer result) {
              setSuccessMessage(result.toString() + " files loaded.");
            }
          });
    }
  }

  public void doSendJson() {
    setEmptyMessage();
    // Get the file name

    ClientResource r = new ClientResource(RegistryRpcRequestBuilder.createApiUrl("jsonload"));
    addAuthentication(r);

    logger.log(Level.INFO, "Sending json data:setonresponse");
    Tree jsonTree = new Tree();
    final TreeItem jsonRoot = jsonTree.addItem(SafeHtmlUtils.fromSafeConstant("Data returned from Json call: "));

    r.setOnResponse(new Uniform() {
      @Override
      public void handle(Request request, Response response) {
        if (response.getStatus().isSuccess()) {
          try {
            JsonRepresentation rep = new JsonRepresentation(response.getEntity());

            // Displays the properties and values.
            try {
              JSONObject object = rep.getValue().isObject();
              if (object != null) {
                for (String key : object.keySet()) {
                  jsonRoot.addItem(SafeHtmlUtils.fromString(key + ":" + object.get(key)));
                }
              }
            } catch (IOException e) {
              e.printStackTrace();
            }
            setSuccessMessage("Json has been loaded");

          } catch (Exception e) {
            setErrorMessage("There was an error loading the file:" + e.getMessage());
          }
        } else {
          setErrorMessage("There was an error loading the file:" + response.getStatus().getDescription());
        }
      }
    });

    ApptListJSON apptList = new ApptListJSON();
    org.restlet.client.ext.json.JsonRepresentation json = apptList.getJSON();
    r.post(json, MediaType.APPLICATION_JSON);
  }

  public void doUploadFile() {
    setEmptyMessage();

    if (uploadData.getFilename() == null || uploadData.getFilename().trim().length() < 1) {
      setErrorMessage("You must select a data file!");
      return;
    }

    String uploadDataType = "";
    if (definitionListBox != null && definitionListBox.getSelectedItem() != null) {
      uploadDataType = definitionListBox.getSelectedItem().getValue();
    }

    uploadData.setName("importdata" + uploadDataType);
    form.setAction(RegistryRpcRequestBuilder.createApiUrl(uploadDataType));
    form.setEncoding(FormPanel.ENCODING_MULTIPART);
    form.setMethod(FormPanel.METHOD_POST);

    logger.log(Level.INFO, "Log: sending to " + form.getAction());
    form.submit();

  }

  private void addAuthentication(ClientResource r) {
    r.setChallengeResponse(ChallengeScheme.HTTP_BASIC, "scott", "tiger");
  }

  @Override
  public String serviceName() {

    return null;
  }

}
