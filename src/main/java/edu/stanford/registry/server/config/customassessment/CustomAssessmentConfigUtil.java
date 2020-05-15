/*
 * Copyright 2019 The Board of Trustees of The Leland Stanford Junior University.
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

package edu.stanford.registry.server.config.customassessment;

import edu.stanford.registry.shared.AssessmentConfig;
import edu.stanford.registry.shared.Constants;

import java.util.ArrayList;
import java.util.Iterator;
import java.util.LinkedHashMap;
import java.util.List;
import java.util.Map;
import java.util.Set;

import org.apache.log4j.Logger;

import com.google.web.bindery.autobean.shared.AutoBean;
import com.google.web.bindery.autobean.shared.AutoBeanCodex;
import com.google.web.bindery.autobean.shared.AutoBeanFactory;
import com.google.web.bindery.autobean.shared.AutoBeanUtils;
import com.google.web.bindery.autobean.vm.AutoBeanFactorySource;

interface ClinicWrapper {
  List<ClinicAssessmentConfig> getValues();

  void setValues(List<ClinicAssessmentConfig> values);
}

// An entry for each clinic
interface ClinicAssessmentConfig {
  String getClinic();

  void setClinic(String clinic);

  List<AssessmentType> getAssessmentType();

  void setAssessmentType(List<AssessmentType> assessmentTypes);

}

// An assessment type - at least one should be configured. Can be multiple in a clinic
interface AssessmentType {
  String getType();

  void setType(String type);

  List<InstrumentEntry> getInstrumentEntry();

  void setInstrumentEntry(List<InstrumentEntry> instrumentEntry);
}


interface AssessmentConfigFactory extends AutoBeanFactory {

  AutoBean<ClinicAssessmentConfig> newClinicAssessmentConfig();

  AutoBean<AssessmentType> newAssessmentType();

  AutoBean<InstrumentEntry> newInstrumentEntry();
}

public class CustomAssessmentConfigUtil {
  private static final Logger logger = Logger.getLogger(CustomAssessmentConfigUtil.class);
  private AssessmentConfigFactory factory = AutoBeanFactorySource.create(AssessmentConfigFactory.class);
  private String configName;
  private ClinicWrapper clinicWrapper;
  private String json;

  public CustomAssessmentConfigUtil(String json) {
    this.configName = Constants.CUSTOM_ASSESSMENT_CONFIG_NAME;
    this.json = json;
    this.clinicWrapper = deserializeFromJson(this.json);
  }

  /**
   * Initializes an assessment config bean from client
   *
   * @param assessmentConfig DTO from client
   */
  public void initObject(AssessmentConfig assessmentConfig) {
    // Need to check if this is a single clinic site or multiple.
    // If multiple, then we need to build the customAssessmentObject prior to setting and saving.
    // If this is the first time the config is created, it should start from null object;
    // Else search the object hierarchy and update
    // Then update the app config entry with AppConfigDao -- look into 695 AdministrativeServicesImpl.java

    ClinicAssessmentConfig clinicConfig = getClinicCustomAssessmentConfigByName(assessmentConfig.getClinicName());

    if (assessmentConfig != null && assessmentConfig.getInstruments() != null
        && !assessmentConfig.getInstruments().isEmpty() && assessmentConfig.getAssessmentType() != null) {

      List<InstrumentEntry> listInstrumentEntry = new ArrayList<>();
      Set<String> instruments = assessmentConfig.getInstruments().keySet();
      for (String instrument : instruments) {
        InstrumentEntry ie = newInstrumentEntry();
        // Set the name of current instrument/questionnaire/study
        if (instrument != null && !instrument.isEmpty()) {
          ie.setName(instrument);
        }
        // Set the frequency
        if (assessmentConfig.getInstruments().get(instrument) != null
            && assessmentConfig.getInstruments().get(instrument) > 0) {
          ie.setFrequency(assessmentConfig.getInstruments().get(instrument));
        }
        listInstrumentEntry.add(ie);
      }

      boolean aUpdated = false;
      if (clinicConfig != null && clinicConfig.getAssessmentType() != null
          && !clinicConfig.getAssessmentType().isEmpty()) {
        for (AssessmentType aType : clinicConfig.getAssessmentType()) {
          if (aType.getType().equalsIgnoreCase(assessmentConfig.getAssessmentType())) {
            aUpdated = true;
            aType.setInstrumentEntry(listInstrumentEntry);
          }
        }
      } else {
        // Create new config
        clinicConfig = newClinicAssessmentConfig();
        List<AssessmentType> assessmentTypeList = new ArrayList<>();
        AssessmentType aType = newAssessmentType();
        aType.setType(assessmentConfig.getAssessmentType());
        aType.setInstrumentEntry(listInstrumentEntry);
        assessmentTypeList.add(aType);
        clinicConfig.setAssessmentType(assessmentTypeList);
        clinicConfig.setClinic(assessmentConfig.getClinicName());
        clinicWrapper.getValues().add(clinicConfig);
        aUpdated = true;

      }
      if (!aUpdated) {
        AssessmentType aType = newAssessmentType();
        aType.setInstrumentEntry(listInstrumentEntry);
        aType.setType(assessmentConfig.getAssessmentType());
        clinicConfig.getAssessmentType().add(aType);
      }
    }
  }

  /**
   * Creates a new clinic assessment config bean
   *
   * @return ClinicAssessmentConfig autobean
   */
  ClinicAssessmentConfig newClinicAssessmentConfig() {
    AutoBean<ClinicAssessmentConfig> bean = factory.newClinicAssessmentConfig();
    return bean.as();
  }

  /**
   * Creates new instrument config bean
   *
   * @return InstrumentEntry autobean
   */
  InstrumentEntry newInstrumentEntry() {
    AutoBean<InstrumentEntry> bean = factory.newInstrumentEntry();
    return bean.as();
  }

  /**
   * Creates new assessment type config bean
   *
   * @return AssessmentType autobean
   */
  AssessmentType newAssessmentType() {
    AutoBean<AssessmentType> bean = factory.newAssessmentType();
    return bean.as();
  }

  private ClinicWrapper deserializeFromJson(String json) {
    AutoBean<ClinicWrapper> bean = AutoBeanCodex.decode(factory, ClinicWrapper.class, json);
    return bean.as();
  }

  public String serializeToJson() {
    AutoBean<ClinicWrapper> bean = AutoBeanUtils.getAutoBean(clinicWrapper);
    return AutoBeanCodex.encode(bean).getPayload();
  }

  /**
   * Returns a list of Instruments/Questionnaires from the JSON config searching by clinic name and assessment type
   * Used by clinic service to list assessment types
   *
   * @param clinicName Clinic name
   * @return List of assessment types configured under a clinic
   */
  public ArrayList<String> getCustomAssessmentTypes(String clinicName) {
    ArrayList<String> assessmentTypes;
    ClinicAssessmentConfig cConfig = getClinicCustomAssessmentConfigByName(clinicName);
    if (cConfig != null && !cConfig.getAssessmentType().isEmpty()) {
      assessmentTypes = new ArrayList<>();
      for (AssessmentType assessmentType : cConfig.getAssessmentType()) {
        assessmentTypes.add(assessmentType.getType());
      }
      return assessmentTypes;
    }
    return null;
  }

  /**
   * Returns a Map of Instruments/Questionnaires from the JSON config searching by clinic name and assessment type
   *
   * @param clinicName        Clinic name
   * @param assessmentTypeVal Assessment Type
   * @return all instruments configured under the passed assessment type
   */
  public Map<String, Integer> getInstruments(String clinicName, String assessmentTypeVal) {

    AssessmentType assessmentType = getAssessmentTypeByType(assessmentTypeVal, getClinicCustomAssessmentConfigByName(clinicName));
    if (assessmentType != null) {
      Map<String, Integer> instruments = new LinkedHashMap<>();
      for (InstrumentEntry item : assessmentType.getInstrumentEntry()) {
        String name = item.getName();
        Integer frequency = item.getFrequency();
        instruments.put(name, frequency);
      }
      return instruments;
    }
    return null;
  }

  /**
   * Returns a single Instrument/Questionnaire entry from the JSON config
   *
   * @param clinicName     Clinic name
   * @param assessmentType Assessment Type
   * @param study          Study/Assessment name
   * @return an instrument entry object if study exists in config
   */
  public InstrumentEntry getInstrument(String clinicName, String assessmentType, String study) {
    Map<String, Integer> instruments = getInstruments(clinicName, assessmentType);
    try {
      if (instruments != null && !instruments.isEmpty()) {
        if (instruments.containsKey(study)) {
          InstrumentEntry instrument = newInstrumentEntry();
          instrument.setName(study);
          instrument.setFrequency(instruments.get(study));
          return instrument;
        }
      }
    } catch (Exception e) {
      logger.error(e);
    }
    return null;
  }

  /**
   * Returns an instrument entry from multiple clinics, if exists, with a smallest frequency value
   *
   * @param study Study/Assessment name
   * @return an instrument entry object if study exists in config
   */
  public InstrumentEntry getMinInstrument(String study) {
    int min = Integer.MAX_VALUE;
    try {
      for (ClinicAssessmentConfig cConfig : clinicWrapper.getValues()) {
        for (AssessmentType at : cConfig.getAssessmentType()) {
          for (InstrumentEntry ie : at.getInstrumentEntry()) {
            if (ie.getName().equals(study) && ie.getFrequency() < min) {
              min = ie.getFrequency();
            }
          }
        }
      }

      if (min < Integer.MAX_VALUE) {
        InstrumentEntry minInstrumentEntry = newInstrumentEntry();
        minInstrumentEntry.setName(study);
        minInstrumentEntry.setFrequency(min);
        return minInstrumentEntry;
      }

    } catch (Exception e) {
      logger.error(e);
    }
    return null;
  }

  private ClinicAssessmentConfig getClinicCustomAssessmentConfigByName(String clinicName) {
    if (clinicWrapper != null) {
      for (ClinicAssessmentConfig cConfig : clinicWrapper.getValues()) {
        if ((clinicName == null && cConfig.getClinic() == null) || (cConfig.getClinic().equals(clinicName))) {
          return cConfig;
        }
      }
    }
    return null;
  }

  private AssessmentType getAssessmentTypeByType(String assessmentType, ClinicAssessmentConfig cConfig) {
    if (cConfig != null && cConfig.getAssessmentType() != null && !cConfig.getAssessmentType().isEmpty()) {
      for (AssessmentType at : cConfig.getAssessmentType()) {
        if (at.getType().equals(assessmentType)) {
          return at;
        }
      }
    }

    return null;
  }

  public String getConfigName() {
    return configName;
  }

  public void setConfigName(String configName) {
    this.configName = configName;
  }

}


