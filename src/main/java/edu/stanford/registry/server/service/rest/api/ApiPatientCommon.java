/*
 * Copyright 2017 The Board of Trustees of The Leland Stanford Junior University.
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
package edu.stanford.registry.server.service.rest.api;

import edu.stanford.registry.client.api.PatientAttributeObj;
import edu.stanford.registry.client.api.PatientObj;
import edu.stanford.registry.server.SiteInfo;
import edu.stanford.registry.server.service.ClinicServices;
import edu.stanford.registry.server.service.NotFoundException;
import edu.stanford.registry.server.service.rest.ApiStatusException;
import edu.stanford.registry.server.utils.DateUtils;
import edu.stanford.registry.shared.Patient;
import edu.stanford.registry.shared.PatientAttribute;

import java.util.ArrayList;
import java.util.Date;
import java.util.List;

import org.json.JSONObject;

public abstract class ApiPatientCommon extends ApiCommon {

  protected Patient getPatient(ClinicServices clinicServices, SiteInfo siteInfo, String patientIdString)
      throws NotFoundException, NumberFormatException {
    String patientId = siteInfo.getPatientIdFormatter().format(patientIdString);
    Patient patient = clinicServices.getPatient(patientId);
    if (patient == null) {
      throw new NotFoundException("Patient");
    }
    return patient;
  }

  protected JSONObject getPatientAttribute(SiteInfo siteInfo, ClinicServices clinicServices, String patientId, String attributeName)
      throws NotFoundException {
    Patient patient = getPatient(clinicServices, siteInfo, patientId);
    if (patient.hasAttribute(attributeName)) {
      return makePatientAttributeJson(siteInfo, patient.getAttribute(attributeName));
    } else {
      throw new NotFoundException("attribute " + attributeName);
    }
  }

  protected JSONObject getPatientAttributes(SiteInfo siteInfo, ClinicServices clinicServices, String patientId) throws NotFoundException {
    Patient patient = getPatient(clinicServices, siteInfo, patientId);
    JSONObject json = new JSONObject();
    for (PatientAttribute attribute : patient.getAttributes()) {
      json.append("attributes", makePatientAttributeJson(siteInfo, attribute));
    }
    return json;
  }

  protected Patient makePatient(SiteInfo siteInfo, PatientObj patObj) throws ApiStatusException {
    Patient patient = new Patient();
    siteInfo.getPatientIdFormatter().format(patObj.getPatientId()); // verify format of patient id
    patient.setPatientId(patObj.getPatientId());
    patient.setFirstName(patObj.getFirstName());
    patient.setLastName(patObj.getLastName());
    // Convert the birth date from midnight to noon
    Date dob = DateUtils.getDateNoon(siteInfo,parseDate(siteInfo, patObj.getDtBirth()));
    patient.setDtBirth(dob);
    patient.setDtCreated(parseDate(siteInfo, patObj.getDtCreated()));
    patient.setDtChanged(parseDate(siteInfo, patObj.getDtChanged()));
    List<PatientAttribute> attributes = null;
    if (patObj.getAttributes() != null) {
      attributes = new ArrayList<>();
      for(PatientAttributeObj attrObj : patObj.getAttributes()) {
        PatientAttribute attr = makePatientAttribute(siteInfo, patient, attrObj);
        attributes.add(attr);
      }
    }
    patient.setAttributes(attributes);
    return patient;
  }

  protected PatientAttribute makePatientAttribute(SiteInfo siteInfo, Patient patient, PatientAttributeObj obj) throws ApiStatusException {
    Date dtCreated = parseDate(siteInfo, obj.getDtCreated());
    Date dtChanged = parseDate(siteInfo, obj.getDtChanged());
    if (dtCreated == null) {
      dtCreated = new Date();
    }
    return new PatientAttribute(obj.getAttributeId(), patient.getPatientId(), obj.getName(), obj.getValue(),
        PatientAttribute.STRING, 0, dtCreated, dtChanged);
  }

  protected PatientObj makePatientObj(SiteInfo siteInfo, Patient patient) {
    PatientObj patientObj = factory.patientObj().as();
    patientObj.setPatientId(patient.getPatientId());
    patientObj.setLastName(patient.getLastName());
    patientObj.setFirstName(patient.getFirstName());
    patientObj.setDtBirth(dateString(siteInfo, patient.getDtBirth()));
    patientObj.setDtCreated(dateString(siteInfo, patient.getDtCreated()));
    patientObj.setDtChanged(dateString(siteInfo, patient.getDtChanged()));
    List<PatientAttributeObj> attrObjs = null;
    if (patient.getAttributes() != null) {
      attrObjs = new ArrayList<>();
      for(PatientAttribute attribute : patient.getAttributes()) {
        PatientAttributeObj attrObj = makePatientAttributeObj(siteInfo, attribute);
        attrObjs.add(attrObj);
      }
    }
    patientObj.setAttributes(attrObjs);
    return patientObj;
  }

  protected PatientAttributeObj makePatientAttributeObj(SiteInfo siteInfo, PatientAttribute attribute) {
    PatientAttributeObj patientAttributeObj = factory.patientAttributeObj().as();
    if (attribute != null) {
      patientAttributeObj.setAttributeId(attribute.getPatientAttributeId());
      patientAttributeObj.setName(attribute.getDataName());
      patientAttributeObj.setValue(attribute.getDataValue());
      patientAttributeObj.setAttributeId(attribute.getPatientAttributeId());
      patientAttributeObj.setDtCreated(dateString(siteInfo, attribute.getDtCreated()));
      patientAttributeObj.setDtChanged(dateString(siteInfo, attribute.getDtChanged()));
      patientAttributeObj.setSiteId(attribute.getSurveySiteId());
    }
    return patientAttributeObj;
  }

  public JSONObject makePatientJson(SiteInfo siteInfo, Patient patient) {
    PatientObj patientObj = makePatientObj(siteInfo, patient );
    return beanObjToJsonObj(patientObj);
  }

  public JSONObject makePatientAttributeJson(SiteInfo siteInfo, PatientAttribute attribute) {
    PatientAttributeObj patientAttributeObj = makePatientAttributeObj(siteInfo, attribute);
    return beanObjToJsonObj(patientAttributeObj);
  }
}
