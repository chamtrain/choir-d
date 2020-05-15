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

import edu.stanford.registry.client.EmailSanitizer;
import edu.stanford.registry.client.ErrorDialogWidget;
import edu.stanford.registry.client.RegistryCssResource;
import edu.stanford.registry.client.RegistryResources;
import edu.stanford.registry.client.service.EditService;
import edu.stanford.registry.client.service.EditServiceAsync;
import edu.stanford.registry.client.utils.ClientUtils;
import edu.stanford.registry.client.utils.ClinicUtils;
import edu.stanford.registry.shared.EmailContentType;
import edu.stanford.registry.shared.ServiceUnavailableException;

import java.util.ArrayList;
import java.util.Map;

import org.gwtbootstrap3.extras.summernote.client.event.SummernoteChangeEvent;
import org.gwtbootstrap3.extras.summernote.client.event.SummernoteChangeHandler;
import org.gwtbootstrap3.extras.summernote.client.ui.Summernote;
import org.gwtbootstrap3.extras.summernote.client.ui.base.Toolbar;
import org.gwtbootstrap3.extras.summernote.client.ui.base.ToolbarButton;
import org.gwtbootstrap3.extras.toggleswitch.client.ui.ToggleSwitch;
import org.gwtbootstrap3.extras.toggleswitch.client.ui.base.constants.ColorType;
import org.gwtbootstrap3.extras.toggleswitch.client.ui.base.constants.SizeType;

import com.google.gwt.core.client.GWT;
import com.google.gwt.core.client.Scheduler;
import com.google.gwt.core.client.Scheduler.ScheduledCommand;
import com.google.gwt.dom.client.Style.Unit;
import com.google.gwt.event.dom.client.ChangeEvent;
import com.google.gwt.event.dom.client.ChangeHandler;
import com.google.gwt.event.dom.client.ClickEvent;
import com.google.gwt.event.dom.client.ClickHandler;
import com.google.gwt.event.logical.shared.ValueChangeEvent;
import com.google.gwt.event.logical.shared.ValueChangeHandler;
import com.google.gwt.safehtml.shared.SafeHtml;
import com.google.gwt.user.client.Event;
import com.google.gwt.user.client.rpc.AsyncCallback;
import com.google.gwt.user.client.ui.Button;
import com.google.gwt.user.client.ui.DockLayoutPanel;
import com.google.gwt.user.client.ui.FlexTable;
import com.google.gwt.user.client.ui.HTML;
import com.google.gwt.user.client.ui.HasVerticalAlignment;
import com.google.gwt.user.client.ui.HorizontalPanel;
import com.google.gwt.user.client.ui.Image;
import com.google.gwt.user.client.ui.Label;
import com.google.gwt.user.client.ui.ListBox;
import com.google.gwt.user.client.ui.PopupPanel;
import com.google.gwt.user.client.ui.ScrollPanel;
import com.google.gwt.user.client.ui.VerticalPanel;
import com.google.gwt.user.client.ui.Widget;

public class EmailTemplateWidget extends TabWidget implements RegistryTabIntf, ClickHandler {

  private static final RegistryCssResource css = RegistryResources.INSTANCE.css();
  private final EditServiceAsync editService = GWT.create(EditService.class);

  // display components
  private DockLayoutPanel mainPanel = new DockLayoutPanel(Unit.EM);
  private DockLayoutPanel detailPanel = new DockLayoutPanel(Unit.EM); // below the top message area


  private final Button saveTemplateButton =
      new Button(new Image(RegistryResources.INSTANCE.save()).toString() + " Save changes");
  private final Button undoTemplateButton =
      new Button(new Image(RegistryResources.INSTANCE.close()).toString() + " Undo changes");
  private final Button showSubVarsButton =
      new Button(new Image(RegistryResources.INSTANCE.plus()).toString() + " Substitution Variables");
  private final Button showHTMLTagsButton =
      new Button(new Image(RegistryResources.INSTANCE.plus()).toString() + " HTML tags");

  // pop-ups
  protected ErrorDialogWidget basicErrorPopUp = new ErrorDialogWidget();
  private final ErrorDialogWidget unsavedChangesPopUp = new ErrorDialogWidget();
  private final CloseClickHandler unsavedCloseHandler = new CloseClickHandler();
  SubVar subVar = new SubVar();  // substitution variables popup
  HtmlTags htmlTags = new HtmlTags();

  //the main content model- ListBox of names and TextArea for body
  Model model = new Model();


  public EmailTemplateWidget(ClinicUtils utils) {
    super(utils);
    initWidget(mainPanel);
  }


  public DockLayoutPanel getMainPanel() {
    return detailPanel;
  }


  @Override
  public void load() {
    setServiceEntryPoint(editService, "editorService");

    getMessageBar().addStyleName(css.emailTemplateHeadingBar()); // from the PageWidget parent
    setEmptyMessage();

    // Create and assemble the detail page components
    // Get the list of process type templates
    editService.getEmailTemplatesList(new AsyncCallback<ArrayList<String>>() {
      @Override
      public void onFailure(Throwable caught) {
        caught.printStackTrace();
        basicErrorPopUp.setText("Encountered the following error on getting list of templates.");
        basicErrorPopUp.setError(caught.getMessage());
      }

      @Override
      public void onSuccess(ArrayList<String> result) {
        if (model.setTemplateNames(result)) {
          showSelectedTemplate(); // populates the textbox
          showEmailContentType(model.getSelectedName());
        }
      }
    });


    detailPanel.addNorth(buildTheHeader(), 3);
    detailPanel.addSouth(buildTheFooter(), 9);

    detailPanel.add(buildTheMiddle());

    // Assemble the main page
    mainPanel.addNorth(getMessageBar(), 4);
    mainPanel.add(detailPanel);

    subVar.initPopUpComponents();
    htmlTags.initPopUpComponents();
  }


  /**
   * The top strip that lets you select a template to edit:
   * <br> &nbsp; Select template: [model.templateList]  hiddenWarning: N Missing!
   */
  private HorizontalPanel buildTheHeader() {
    HorizontalPanel detailHeadingPanel = new HorizontalPanel();
    HorizontalPanel detailHeadingBar = new HorizontalPanel();
    detailHeadingPanel.setStylePrimaryName(css.clTabPgHeadingBar());
    detailHeadingPanel.addStyleName(css.emailTemplateHeadingBar());
    detailHeadingBar.setStylePrimaryName(css.clTabPgHeadingSelectBar());


    final Label templateListLabel = new Label("Select template ");
    templateListLabel.setStylePrimaryName(css.clTabPgHeadingBarLabel());
    templateListLabel.addStyleName(css.emailTemplateLabel());
    detailHeadingBar.add(templateListLabel);

    model.addNameListToPanel(detailHeadingBar);
    model.addMissingTemplatesLabelToPanel(detailHeadingBar);
    model.addEmailContentTypeToggleToPanel(detailHeadingBar);
    detailHeadingPanel.add(detailHeadingBar);
    return detailHeadingPanel;
  }


  private ScrollPanel buildTheMiddle() {
    HorizontalPanel templateTextPanel = new HorizontalPanel();
    templateTextPanel.addStyleName(css.emailTemplateTextPanel());
    model.addTextWidgetToPanel(templateTextPanel);
    ScrollPanel templateScroll = new ScrollPanel();
    templateScroll.add(templateTextPanel);
    templateScroll.addStyleName(css.emailTemplateTextPanel());
    return templateScroll;
  }


  private HorizontalPanel buildTheFooter() {
    HorizontalPanel detailFootingPanel = new HorizontalPanel();
    HorizontalPanel detailButtonBar = new HorizontalPanel(); // left side
    HorizontalPanel detailSubVarBar = new HorizontalPanel(); // right side
    detailFootingPanel.setWidth("98%");
    detailFootingPanel.setStylePrimaryName(css.clTabPgFootingBar());
    detailButtonBar.setStylePrimaryName(css.clTabPgFootingBar());
    saveTemplateButton.addStyleName(css.paddedButton());
    saveTemplateButton.addClickHandler(this);
    undoTemplateButton.addClickHandler(this);
    showSubVarsButton.addClickHandler(this);
    showHTMLTagsButton.addClickHandler(this);
    enableButtons(false);
    detailButtonBar.add(saveTemplateButton);
    detailButtonBar.add(undoTemplateButton);
    detailFootingPanel.add(detailButtonBar);
    detailSubVarBar.setStylePrimaryName(css.clTabPgFootingBar());
    detailSubVarBar.add(showSubVarsButton);
    detailSubVarBar.add(showHTMLTagsButton);
    detailFootingPanel.setHorizontalAlignment(HorizontalPanel.ALIGN_RIGHT);
    detailFootingPanel.add(detailSubVarBar);
    return detailFootingPanel;
  }


  // The whole tab widget is also a click handler for the 3 buttons at the bottom of the tab
  @Override
  public void onClick(ClickEvent event) {
    Widget sender = (Widget) event.getSource();
    if (sender == saveTemplateButton) {
      model.enableTextArea(false);
      String orig = model.getOriginalText();
      saveTemplate(model.getIndexOfEdit(), model.getSelectedName(), orig, model.getValueAndSaveAsOriginal());
      setSuccessMessage("Your changes have been saved");
      model.enableTextArea(true);
      if (!model.isHTML()) {
        model.textWidget.clickCodeView();
      }
      enableButtons(false);
    } else if (sender == undoTemplateButton) {
      model.undoChanges();
      setSuccessMessage("All changes since the last save have been rolled back");
      enableButtons(false);
    } else if (sender == showSubVarsButton) {
      GWT.log("sender is showSubVars");
      subVar.show();
    } else if (sender == showHTMLTagsButton) {
      htmlTags.show();
    }
  }


  /**
   * Puts up the save-unchanged-text window if the current text has changed,
   * and (while it's up, if it's up) fetches the selected template's text
   * and populates the edit area. So if the save-unchanged-text window is up,
   * save or dismiss (hitting the close-window box to abort the item-change)
   * must operate on a saved copy of the old data.
   */
  private void showSelectedTemplate() {
    setEmptyMessage();
    final int selectedIndex = model.getSelectedItemIndex();

    if (selectedIndex == model.getIndexOfEdit()) {
      GWT.log("template selected = saved " + model.getIndexOfEdit());
      return; // nothing changed;
    }

    if (model.isChangedAndNotEmpty()) {
      // If one dismisses the saveChanges/discardChanges window, go back to editing it
      unsavedCloseHandler.setOriginal(model.getIndexOfEdit(), model.getOriginalText(), model.getEmailText());

      final String updatedTemplateName = model.getNameOfEdit();
      final String origText = model.getOriginalText();
      final String updatedTemplateContents = model.getEmailText();
      final int updateTemplateIndex = model.getIndexOfEdit();
      model.setTextAreaVisible(false);  // hide it

      unsavedChangesPopUp.setError(new HTML("There are unsaved changes to the " + updatedTemplateName + " template "));
      ArrayList<Button> popUpButtons = new ArrayList<>();
      Button saveChangesButton = new Button(new Image(RegistryResources.INSTANCE.save()).toString() + " Save changes");
      Button dropChangesButton = new Button(
          new Image(RegistryResources.INSTANCE.delete()).toString() + " Discard changes");

      saveChangesButton.setTitle("Save changes and continue with showing another template");
      dropChangesButton.setTitle("Don't save the changes, continue to showing another template");

      saveChangesButton.addClickHandler(new ClickHandler() {
        @Override
        public void onClick(ClickEvent event) {
          saveTemplate(updateTemplateIndex, updatedTemplateName, origText, updatedTemplateContents);
          unsavedChangesPopUp.hide();
        }
      });

      dropChangesButton.addClickHandler(new ClickHandler() {
        @Override
        public void onClick(ClickEvent event) {
          unsavedChangesPopUp.hide();
        }
      });
      popUpButtons.add(saveChangesButton);
      popUpButtons.add(dropChangesButton);

      unsavedChangesPopUp.setCustomButtons(popUpButtons);
      unsavedChangesPopUp.show();
    }
    getNewEmailTemplate(selectedIndex); // possibly while the save-pop-up is up, we'll fetch the new email template
  }


  private void getNewEmailTemplate(final int selectedIndex) {
    // Get the template for the newly selected index
    final String templateName = model.getSelectedName();

    try {
      editService.getEmailTemplate(templateName, new AsyncCallback<String>() {
        @Override
        public void onFailure(Throwable caught) {
          basicErrorPopUp.setText("Encountered the following error getting the template.");
          basicErrorPopUp.setError(caught.getMessage());
        }

        @Override
        public void onSuccess(String result) {
          if (result != null) {
            model.gotChosenTemplatesBody(selectedIndex, result);
            enableButtons(false);
          } else {
            model.gotChosenTemplatesBody(selectedIndex, "");
          }
          showEmailContentType(templateName);
        }
      });
    } catch (IllegalArgumentException | ServiceUnavailableException e) {
      basicErrorPopUp.setText("Encountered the following error getting the template.");
      basicErrorPopUp.setError(e.getMessage());
      e.printStackTrace();
    }
  }


  public ChangeHandler getTemplateListChangeHandler() {
    return new ChangeHandler() {
      @Override public void onChange(ChangeEvent event) {
        showSelectedTemplate();
      }
    };

  }


  /**
   * This might be called after a new template is chosen, after the "save unsaved changes"
   * pop-up comes up and the fetch-new-template callback is called.
   */
  private void saveTemplate(final int wasForThisIndex, final String name, final String origText, final String chgdText) {
    try {
      final SafeHtml safeHtml = EmailSanitizer.sanitizeHtml(chgdText);

      if (!safeHtml.asString().equals(chgdText)) {
        basicErrorPopUp.setText("See the HTML tags button below for the list of supported tags");
        StringBuilder sbTags = new StringBuilder();
        for (String tag: EmailSanitizer.findInvalidTags(chgdText)) {
          sbTags.append(tag)
              .append(",");
        }
        sbTags.append(" <br>your changes will be saved with the unsuppported tags escaped. These tags will appear in the email");
        HTML errorMsg = new HTML("WARNING: Unsupported HTML tags were found in the email text: <b>" + sbTags.toString());
        errorMsg.addStyleName(css.serverResponseLabelError());
        basicErrorPopUp.setError(errorMsg);
        ArrayList<Button> popUpButtons = new ArrayList<>();
        Button saveChangesButton = new Button(new Image(RegistryResources.INSTANCE.save()).toString() + " Save changes");
        Button dropChangesButton = new Button(
            new Image(RegistryResources.INSTANCE.delete()).toString() + " Cancel");

        saveChangesButton.setTitle("Save the changes anyway");
        dropChangesButton.setTitle("Don't save the changes");

        saveChangesButton.addClickHandler(new ClickHandler() {
          @Override
          public void onClick(ClickEvent event) {
            saveTemplate(wasForThisIndex, name, origText, safeHtml);
            basicErrorPopUp.hide();
          }
        });

        dropChangesButton.addClickHandler(new ClickHandler() {
          @Override
          public void onClick(ClickEvent event) {
            basicErrorPopUp.hide();
            enableButtons(true);
          }
        });
        popUpButtons.add(saveChangesButton);
        popUpButtons.add(dropChangesButton);
        basicErrorPopUp.setCustomButtons(popUpButtons);
      } else {
        saveTemplate(wasForThisIndex, name, origText, safeHtml);
      }
    } catch (IllegalArgumentException | ServiceUnavailableException e) {
      basicErrorPopUp.setText("Encountered the following error getting the template.");
      basicErrorPopUp.setError(e.getMessage());
      e.printStackTrace();
    }
  }

  private void saveTemplate(final int wasForThisIndex, final String name, final String origText, final SafeHtml safeHtml) {
    editService.updateEmailTemplate(name, safeHtml.asString(), new AsyncCallback<String>() {
      @Override
      public void onFailure(Throwable caught) {
        basicErrorPopUp.setText("Encountered the following error getting the template.");
        basicErrorPopUp.setError(caught.getMessage());
        model.goBackToUnsavedState(wasForThisIndex, origText, safeHtml.asString());
      }

      @Override
      public void onSuccess(String result) {
        if (wasForThisIndex == model.getIndexOfEdit()) {
          // only reason to do this is if someone ELSE is editing, so what we saved is wrong...
          model.gotChosenTemplatesBody(wasForThisIndex, result);
        }
        model.noteEmailSaved(wasForThisIndex);
        setSuccessMessage("Template has been saved");
      }
    });
  }
  class SubVar {
    private final PopupPanel subVarPopUp = new PopupPanel();
    private final VerticalPanel subVarPanel = new VerticalPanel();
    private final HorizontalPanel subVarHeadingBar = new HorizontalPanel();
    private Button closeButton = new Button(new Image(RegistryResources.INSTANCE.close()).toString());
    private FlexTable tbl = new FlexTable();

    void show() {
      subVarPopUp.center();
      subVarPopUp.show();
    }

    void initPopUpComponents() {
      // display substitution variables
      closeButton.setText("Close");
      closeButton.setPixelSize(100, 25);
      closeButton.addClickHandler(new ClickHandler() {
        @Override
        public void onClick(ClickEvent event) {
          subVarPopUp.hide();
        }
      });

      subVarPanel.addStyleName(css.dialogPopUp());
      subVarHeadingBar.addStyleName(css.head());
      subVarHeadingBar.setWidth("100%");
      Label titleLabel = new Label("Substitution Variables");
      titleLabel.setStylePrimaryName(css.titleLabel());
      subVarHeadingBar.add(titleLabel);
      subVarPanel.add(subVarHeadingBar);

      tbl.removeAllRows();
      for (int inx = 0; inx < ClientUtils.EMAIL_TEMPLATE_VARIABLE_NAMES.length; inx++) {
        Label varLbl = new Label(ClientUtils.EMAIL_TEMPLATE_VARIABLE_NAMES[inx]);
        varLbl.setStylePrimaryName(css.subVariable());
        tbl.setWidget(inx, 0, varLbl);
      }
      subVarPanel.add(tbl);
      subVarPanel.add(closeButton);
      subVarPopUp.setWidget(subVarPanel);
      subVarPopUp.hide();

      unsavedChangesPopUp.setModal(false);
      unsavedChangesPopUp.setText("Attention");
      unsavedChangesPopUp.addCloseButtonClickHandler(unsavedCloseHandler);
    }
  }

  class HtmlTags {

    private final PopupPanel tagsPopUp = new PopupPanel();
    private final VerticalPanel tagsPanel = new VerticalPanel();
    private final HorizontalPanel tagsHeadingBar = new HorizontalPanel();
    private Button closeButton = new Button(new Image(RegistryResources.INSTANCE.close()).toString());
    private FlexTable tbl = new FlexTable();

    void show() {
      tagsPopUp.center();
      tagsPopUp.show();
    }

    void initPopUpComponents() {
      // display substitution variables
      closeButton.setText("Close");
      closeButton.setPixelSize(100, 25);
      closeButton.addClickHandler(new ClickHandler() {
        @Override
        public void onClick(ClickEvent event) {
          tagsPopUp.hide();
        }
      });

      tagsPanel.addStyleName(css.dialogPopUp());
      tagsHeadingBar.addStyleName(css.head());
      tagsHeadingBar.setWidth("98%");
      Label titleLabel = new Label("Supported HTML Tags");
      titleLabel.setStylePrimaryName(css.titleLabel());
      titleLabel.addStyleName(css.tableDataHeaderLabel());
      tagsHeadingBar.add(titleLabel);
      tagsPanel.add(tagsHeadingBar);
      tbl.addStyleName(css.dataList());
      tbl.removeAllRows();
      Map<String, String> supportedTags = EmailSanitizer.getSupportedTags();
      int inx=0;
      for (String key : supportedTags.keySet()) {
        Label varLbl = new Label(key);
        Label desLbl = new Label(supportedTags.get(key));
        varLbl.setStylePrimaryName(css.subVariable());
        desLbl.setStylePrimaryName(css.subVariable());
        tbl.setWidget(inx, 0, varLbl);
        tbl.setWidget(inx, 1, desLbl);
        tbl.getFlexCellFormatter().setWidth(inx, 0, "80px");
        inx++;
      }
      tagsPanel.add(tbl);
      tagsPanel.add(closeButton);
      tagsPopUp.setWidget(tagsPanel);
      tagsPopUp.hide();

      unsavedChangesPopUp.setModal(false);
      unsavedChangesPopUp.setText("Attention");
      unsavedChangesPopUp.addCloseButtonClickHandler(unsavedCloseHandler);
    }
  }

  private void enableButtons(boolean enable) {
    saveTemplateButton.setEnabled(enable);
    undoTemplateButton.setEnabled(enable);
    if (enable) {
      setEmptyMessage();
    }
  }

  private void showEmailContentType(String templateName) {
    editService.getEmailContentType(templateName, new AsyncCallback<EmailContentType>() {
      @Override
      public void onFailure(Throwable caught) {
        caught.printStackTrace();
        basicErrorPopUp.setText("Encountered the following error on getting the templates Content-type.");
        basicErrorPopUp.setError(caught.getMessage());
      }

      @Override
      public void onSuccess(EmailContentType result) {
        model.setTemplateToggle(result);
      }
    });
  }

  /**
   * The model is:
   * <br>- a list of names with one selected
   * <br>- a text box with original text (to know if it changed) and corresponding to an index.
   */
  private class Model {
    private final ListBox templateList = new ListBox();
    private int index = -1;
    private String originalText = "<p><br></p>";  // so can tell if it changed
    private final SensitiveTextArea textWidget;
    private int numEmpty = 0;
    private final String missingTextSuffix = getClientUtils().getParam("emailtemplate.missing.suffix");
    private final Label missingLabel = new Label();  //  "N" + NTemplatesAreMissing);  // initialize, if size is needed
    private final ToggleSwitch emailContentTypeToggle = new ToggleSwitch();

    Model() {
      super();
      textWidget = new SensitiveTextArea(); // so can reach it from anonymous classes
      emailContentTypeToggle.setSize(SizeType.MINI);
      emailContentTypeToggle.setOnColor(ColorType.SUCCESS);
      emailContentTypeToggle.setOnText(EmailContentType.HTML.toString());
      emailContentTypeToggle.setOffText(EmailContentType.Plain.toString());
      emailContentTypeToggle.setLabelText("Format");
    }

    void noteEmailSaved(int wasForThisIndex) {
      String name = templateList.getItemText(wasForThisIndex);
      if (name.contains(missingTextSuffix)) {  // fix the name and decrement the number missing
        templateList.setItemText(wasForThisIndex, name.replace(missingTextSuffix, ""));
        setNumberMissingTemplates(--numEmpty);
      }
    }


    // ==== Interface for the missing items label
    void setNumberMissingTemplates(int n) {
      if (n > 0) {
        missingLabel.setText(n + " Templates are missing!");
        missingLabel.setVisible(true);
      } else {
        missingLabel.setVisible(false);
      }
    }

    void addMissingTemplatesLabelToPanel(HorizontalPanel panel) {
      missingLabel.setStylePrimaryName(css.clTabPgHeadingBarLabel());
      missingLabel.addStyleName(css.emailTemplateMissing());
      panel.add(missingLabel);
    }


    // ==== Interface for the list

    void addNameListToPanel(HorizontalPanel panel) {
      templateList.addChangeHandler(getTemplateListChangeHandler());
      templateList.setStylePrimaryName(css.clTabPgHeadingBarList());
      panel.add(templateList);
    }

    void addEmailContentTypeToggleToPanel(HorizontalPanel panel) {
      emailContentTypeToggle.addValueChangeHandler(new ValueChangeHandler<Boolean>() {
        @Override
        public void onValueChange(ValueChangeEvent<Boolean> event) {
          final EmailContentType contentType = emailContentTypeToggle.getValue() ? EmailContentType.HTML :
              EmailContentType.Plain;
          final String templateName = getSelectedName();
          showHTMLTagsButton.setEnabled(emailContentTypeToggle.getValue());
          emailContentTypeToggle.setEnabled(false);
          editService.updateEmailContentType(templateName, contentType, new AsyncCallback<Boolean>() {
            @Override
            public void onFailure(Throwable caught) {
              caught.printStackTrace();
              basicErrorPopUp.setText("Encountered the following error setting the Content-type to " +
                  contentType.toString() + " for template " + getSelectedName());
              basicErrorPopUp.setError(caught.getMessage());
            }
            @Override
            public void onSuccess(Boolean result) {
              basicErrorPopUp.setText(
                  "Successful setting the Content-type to " + contentType.toString() + " for " + templateName);
            }
          });
          setToolbar(contentType);
          emailContentTypeToggle.setEnabled(true);
        }
      });

      emailContentTypeToggle.getElement().addClassName(css.clTabPgHeadingSelectBar());
      panel.setVerticalAlignment(HasVerticalAlignment.ALIGN_MIDDLE);
      panel.add(emailContentTypeToggle);
    }

    boolean setTemplateNames(ArrayList<String> result) {
      if (result != null) {
        for (String name : result) {
          templateList.addItem(name);
          if (name.endsWith(missingTextSuffix)) {
            numEmpty++;  // there's no way to style a single widget in a ListBox...
          }
        }
        templateList.setSelectedIndex(0);
      }
      setNumberMissingTemplates(numEmpty);

      if (templateList.getItemCount() > 0) {
        templateList.setSelectedIndex(0);
        return true;
      }
      return false;
    }

    void setTemplateToggle(EmailContentType contentType) {

      emailContentTypeToggle.setValue(EmailContentType.HTML == contentType);
      textWidget.setShowToolbar(EmailContentType.HTML == contentType);

      showHTMLTagsButton.setEnabled(emailContentTypeToggle.getValue());
      setToolbar(contentType);
    }
    String getNameOfEdit() {
      return templateList.getItemText(index).replace(missingTextSuffix, "");
    }

    /**
     * Returns the selected template name, removing any appended "MISSING"
     */
    String getSelectedName() {
      return templateList.getItemText(getSelectedItemIndex()).replace(missingTextSuffix, "");
    }

    int getSelectedItemIndex() {
      return templateList.getSelectedIndex();
    }


    // ==== Interface to the text widget and its index

    int getIndexOfEdit() {
      return index;
    }

    void addTextWidgetToPanel(HorizontalPanel panel) {
      panel.add(textWidget);
    }

    void enableTextArea(boolean enabled) {
      textWidget.setEnabled(enabled);
    }

    void setTextAreaVisible(boolean makeVisible) {
      textWidget.setVisible(makeVisible);
    }

    String getEmailText() {
      if (isHTML()) {
        return textWidget.getCode();
      } else {
        return textWidget.getCode().replaceAll("<.*?>", "");// "\\<.*?\\>",
      }
    }

    String getOriginalText() {
      return originalText;
    }

    /**
     * Sets the value and saves it as the original text.
     */
    void gotChosenTemplatesBody(int ix, String value) {
      index = ix;
      textWidget.setCode(value);
      originalText = textWidget.getCode(); // save the value the text area returns
    }

    /**
     * Sets the originalText to the current value. Call this after saving.
     * @return the current text
     */
    String getValueAndSaveAsOriginal() {
      String curVal = getEmailText();
      textWidget.setCode(curVal);
      return curVal;
    }

    public void goBackToUnsavedState(int originalIndex, String origText, String chgdText) {
      index = originalIndex;
      templateList.setSelectedIndex(originalIndex);
      textWidget.setCode(chgdText);
      originalText = origText;
      if (!origText.equals(chgdText)) {
        enableButtons(true);
      }
    }

    /**
     * Don't want to enable Save button if the text area is empty.
     * (On the other hand, if it's emptied, we should enable undo...)
     */
    private boolean isChangedAndNotEmpty() {
      String current = getEmailText();
      return !current.isEmpty() && !originalText.equals(current); // && originalText.length() > 0;
    }

    boolean isHTML() {
      return EmailContentType.HTML ==
          (emailContentTypeToggle.getValue() ? EmailContentType.HTML :
              EmailContentType.Plain);
    }

    public void undoChanges() {
      textWidget.setCode(originalText);
    }

    public void setToolbar(EmailContentType contentType) {
      if (EmailContentType.HTML == contentType) {
        Toolbar htmlToolbar = new Toolbar();
        htmlToolbar.addGroup(ToolbarButton.STYLE, ToolbarButton.BOLD, ToolbarButton.ITALIC, ToolbarButton.UNDERLINE,
            ToolbarButton.UL, ToolbarButton.OL, ToolbarButton.HR, ToolbarButton.PARAGRAPH, ToolbarButton.LINK,
            ToolbarButton.PICTURE, ToolbarButton.FONT_SIZE, ToolbarButton.COLOR, ToolbarButton.UNDO, ToolbarButton.REDO,
            ToolbarButton.CLEAR, ToolbarButton.CODE_VIEW);
        textWidget.setToolbar(htmlToolbar);
      } else {
        Toolbar textToolbar = new Toolbar();
        textToolbar.addGroup(ToolbarButton.UNDO, ToolbarButton.REDO, ToolbarButton.CODE_VIEW);
        textWidget.setToolbar(textToolbar);
      }
      textWidget.reconfigure(contentType);
    }
  }


  /**
   * A text area that un-grays the buttons if the text has changed.
   */
  class SensitiveTextArea extends Summernote implements SummernoteChangeHandler{
    final Summernote thisArea;  // so it can be accessed inside an anonymous class
    SensitiveTextArea() {
      super();
      thisArea = this; // so can reach it from anonymous classes

      // It was difficult to get the TextArea to respond to paste and delete.
      // Paste can come from a keyboard command, right-mouse context menu, or the browser edit menu.
      sinkEvents(Event.ONPASTE);     // seems to occur BEFORE the paste, sigh... (hint: paste from browser Edit menu)
      sinkEvents(Event.ONCHANGE);    // This would be nice, but seems to never happen
      sinkEvents(Event.ONKEYPRESS);  // Characters come in on this
      sinkEvents(Event.ONKEYUP);     // Delete and Backspace events are not key-press...

      // Create the scroll-able center for the template contents
      // templateText.setSize("100%", "98%");
      setDefaultHeight(350);
      setMinHeight(350);
      setHeight("350px");

      addSummernoteChangeHandler(new SummernoteChangeHandler() {
        @Override
        public void onSummernoteChange(SummernoteChangeEvent event) {
          boolean canBeSaved = model.isChangedAndNotEmpty();
          if (saveTemplateButton.isEnabled() != canBeSaved) {
            enableButtons(canBeSaved);
          }
        }
      });
    }

    @Override
    public void onBrowserEvent(Event event) {
      if (event != null) {
        super.onBrowserEvent(event);
        if (event.getType().equals("paste")) {
          // The actual Paste hasn't happened yet, so add a ChangeEvent to occur after it happens
          Scheduler.get().scheduleDeferred(new ScheduledCommand() {
            @Override public void execute() {
              SummernoteChangeEvent.fire(thisArea);
            }
          });
        }
      }
      boolean canBeSaved = model.isChangedAndNotEmpty();
      if (saveTemplateButton.isEnabled() != canBeSaved) {
        enableButtons(canBeSaved);
      }
    }

    @Override
    public void onSummernoteChange(SummernoteChangeEvent event) {
      if (saveTemplateButton.isEnabled() != model.isChangedAndNotEmpty()) {
        enableButtons(model.isChangedAndNotEmpty());
      }
    }

    void reconfigure(EmailContentType contentType) {
      this.reconfigure();
      if (contentType == EmailContentType.Plain) {
        clickCodeView();
        enableButtons(true); // changeEvent doesn't fire in Codeview mode
      }
    }
    final native void clickCodeView() /*-{
      $wnd.$("div.note-editor button.btn-codeview").click();
      $wnd.$('div.note-editor button.btn-codeview').addClass('hidden');
      $wnd.$("div.note-editor textarea.note-codable").css('background-color', '#f0f0f0').css('color', '#000');
    }-*/;
  }


  // This is called if the person hits the close box (x) on the Save Unsaved Changes pop-up
  private class CloseClickHandler implements ClickHandler {

    private int originalIndex = -1;
    private String originalText = null;
    private String unsavedText = null;
    @Override
    public void onClick(ClickEvent event) {
      GWT.log("clickevent for popupclose, showing templateText");
      if (originalIndex >= 0 && originalIndex < model.templateList.getItemCount() && originalText != null) {
        model.goBackToUnsavedState(originalIndex, originalText, unsavedText);
      }
      setOriginal(-1, null, null);
      model.setTextAreaVisible(true);
    }

    public void setOriginal(int inx, String origTxt, String unsavedTxt) {
      originalIndex = inx;
      originalText = origTxt;
      unsavedText = unsavedTxt;
    }
  }

  @Override
  public String serviceName() {
    return "ClinicServices";
  }

}
