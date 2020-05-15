package edu.stanford.registry.client.widgets;

import edu.stanford.registry.client.RegistryCssResource;
import edu.stanford.registry.client.RegistryResources;
import edu.stanford.registry.client.utils.ClinicUtils;
import edu.stanford.registry.shared.Constants;
import edu.stanford.registry.shared.Patient;

import java.util.Date;

import com.google.gwt.user.client.ui.FlexTable;
import com.google.gwt.user.client.ui.HorizontalPanel;
import com.google.gwt.user.client.ui.Label;

/**
 * This is thin red-bordered box at the top of Patient tab.
 *
 * Created by tpacht on 6/24/2015.
 */
public class PatientIdentification  extends HorizontalPanel {
  private static final RegistryCssResource css = RegistryResources.INSTANCE.css();
  private final static int MEDIUM = 0;
  private final static int LARGE = 1;
  private final static int SMALL = 2;
  private int panelSize = MEDIUM;
  private FlexTable grid;
  private String version = "0";
  private ClinicUtils utils;

  public PatientIdentification(Patient patient, ClinicUtils utils) {
    this(patient, utils, LARGE);
  }

  private PatientIdentification(Patient patient, ClinicUtils utils, int size) {
    if (patient == null) {
      return;
    }
    panelSize = size;
    setVersion(utils);
    this.utils = utils;
    String patientLastNameFirst = patient.getLastName() + ", " + patient.getFirstName();
    String gender = patient.getAttributeString(Constants.ATTRIBUTE_GENDER, "NA");
    Date dob = patient.getDtBirth();
    init(patientLastNameFirst, patient.getPatientId(), utils.getAge(dob), gender, utils.getDateString(dob));
  }

  public PatientIdentification(ClinicUtils utils, String patientLastNameFirst, String patientId, int age, String gender, String birthDt) {
    setVersion(utils);
    this.utils = utils;
    init(patientLastNameFirst, patientId, age, gender, birthDt);
  }

  private void setVersion(ClinicUtils utils) {
    this.version = utils.getParam("patientIdentificationViewVs", "0");
  }

  private void init(String patientLastNameFirst, String patientId, int age, String gender, String birthDt) {


    grid = new FlexTable();
    grid.setWidth("100%");
    grid.setHeight("2.0em");
    if ("1".equals(version)) {
      this.addStyleName(css.patientIdentificationPanel());
      if (panelSize == LARGE) {
        this.addStyleName(css.patientIdentificationLarge());
      }
      grid.setWidget(0, 0, getStyledPanel(patientLastNameFirst));
      grid.setWidget(0, 1, getStyledPanel(utils.getParam(Constants.PATIENT_ID_LABEL), patientId));
      if (age > 0) {
        grid.setWidget(0, 2, getStyledPanel("Age:", age + ""));
      } else {
        grid.setWidget(0, 2, getStyledPanel("Age:", "NA"));
      }
      grid.setWidget(0, 3, getStyledPanel("Gender:", gender));
      grid.setWidget(0, 4, getStyledPanel("DOB:", birthDt));
      styleGrid();
    } else {
      this.addStyleName(css.popupBox());
      Label nameLabel = new Label(patientLastNameFirst);
      nameLabel.addStyleName(css.heading());
      grid.setWidget(0, 0, nameLabel);
      Label mrnLabel = new Label(utils.getParam(Constants.PATIENT_ID_LABEL) + " " + patientId);
      mrnLabel.addStyleName(css.heading());
      grid.setWidget(0, 1, mrnLabel);
    }

    this.add(grid);
  }

  public void removeColumn(int column) {
    if (grid.getRowCount() > 0) {
      if (grid.getCellCount(0) > column) {
        grid.removeCell(0, column);
      }
    }
    styleGrid();
  }

  private HorizontalPanel getStyledPanel(String fieldValue) {
    HorizontalPanel styledPanel = new HorizontalPanel();
    styledPanel.addStyleName(css.patientIdentification());
    Label valueLbl = new Label(fieldValue);
    valueLbl.addStyleName(css.patientIdLabel());
    if (panelSize == LARGE) {
      valueLbl.addStyleName(css.patientIdLabelLarge());
    }
    styledPanel.add(valueLbl);
    return styledPanel;
  }

  private HorizontalPanel getStyledPanel(String fieldLabel, String fieldValue) {
    HorizontalPanel styledPanel = new HorizontalPanel();
    styledPanel.addStyleName(css.patientIdentification());
    Label labelLbl = new Label(fieldLabel);
    Label valueLbl = new Label(fieldValue);
    labelLbl.addStyleName(css.patientIdLabel());
    valueLbl.addStyleName(css.patientIdLabel());
    if (panelSize == LARGE) {
      labelLbl.addStyleName(css.patientIdLabelLarge());
      valueLbl.addStyleName(css.patientIdLabelLarge());
    }
    styledPanel.add(labelLbl);
    styledPanel.add(valueLbl);
    return styledPanel;
  }

  private void styleGrid() {
    grid.setWidth("100%");
    for (int i=0; i<grid.getCellCount(0); i++) {
      grid.getCellFormatter().setWidth(0, i, Integer.valueOf(100 / grid.getCellCount(0)).intValue() + "%");
      grid.getCellFormatter().addStyleName(0, i, css.patientIdentification());

      grid.getCellFormatter().getElement(0, i).setAttribute("style", "vertical-align: middle;" );

    }

    switch (panelSize) {
    case SMALL:
      grid.setHeight(("1.6em"));
      break;
    case MEDIUM:
      grid.setHeight("2.5em");
      break;
    case LARGE:
    default:
      //grid.setHeight("3.4em");
      css.patientIdentificationLarge();
      break;
    }
  }
}
