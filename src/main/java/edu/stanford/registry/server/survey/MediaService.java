/*
 * Copyright 2016 The Board of Trustees of The Leland Stanford Junior University.
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
package edu.stanford.registry.server.survey;

import edu.stanford.registry.server.ServerUtils;
import edu.stanford.registry.server.config.AppConfigDao;
import edu.stanford.registry.server.config.AppConfigEntry;
import edu.stanford.registry.server.SiteInfo;
import edu.stanford.registry.server.database.SurveySystDao;
import edu.stanford.registry.shared.DataException;
import edu.stanford.registry.shared.PatientStudyExtendedData;
import edu.stanford.registry.shared.Study;
import edu.stanford.registry.shared.SurveySystem;
import edu.stanford.survey.client.api.DisplayStatus;
import edu.stanford.survey.client.api.FieldType;
import edu.stanford.survey.client.api.FormField;
import edu.stanford.survey.client.api.FormQuestion;
import edu.stanford.survey.client.api.PageConfiguration;
import edu.stanford.survey.client.api.QuestionType;
import edu.stanford.survey.client.api.SubmitStatus;
import edu.stanford.survey.client.api.SurveyFactory;
import edu.stanford.survey.client.api.SurveyPageComponent;

import java.util.ArrayList;
import java.util.List;
import java.util.Map;

import org.apache.log4j.Logger;

import com.github.susom.database.Database;
import com.google.web.bindery.autobean.shared.AutoBean;
import com.google.web.bindery.autobean.shared.AutoBeanCodex;
import com.google.web.bindery.autobean.vm.AutoBeanFactorySource;

/**
 * Created by tpacht on 1/31/17.
 */
public class MediaService extends RegistryAssessmentsService implements SurveyServiceIntf {
  static private String MEDIA_SERVICE_PREFIX = "MediaService";
  private String serviceName;
  private String componentName;
  private String configName;

  private SurveyFactory factory = AutoBeanFactorySource.create(SurveyFactory.class);
  private static Logger logger = Logger.getLogger(MediaService.class);

  public MediaService(SiteInfo siteInfo, String fullServiceName) {
    super(siteInfo);
    if (fullServiceName.startsWith(MEDIA_SERVICE_PREFIX)) {
      // this should be the case in version 2 - a caller shouldn't know how long the prefix is...
      serviceName = fullServiceName.substring(1 + MEDIA_SERVICE_PREFIX.length());
    } else {
      serviceName = fullServiceName; // this is what the version 1 code did
    }
    String[] tokens = serviceName.split(":");
    if (tokens.length > 0) {
      this.configName = tokens[0];
    }
    if (tokens.length > 1) {
      this.componentName = tokens[1];
    }
  }

  @Override
  public NextQuestion handleResponse(Database database, PatientStudyExtendedData patStudyExtended, SubmitStatus submitStatus,
                                     String answerJson) {
    ArrayList<FormField> videoFields = new ArrayList<>();
    getMedia(database, patStudyExtended.getSurveySiteId(), videoFields );
    AutoBean<FormQuestion> bean = factory.formQuestion();
    FormQuestion formQuestion = bean.as();
    formQuestion.setFields(videoFields);
    if (videoFields.size() > 0) {
      for (FormField formField : videoFields) {
        if (formField.getType().equals(FieldType.videoLink)) {
          if (formField.getAttributes() != null) {
            Map<String, String> attributes = formField.getAttributes();
            if (attributes.containsKey("isTerminal") && "true".equals(attributes.get("isTerminal")) ) {
               formQuestion.setTerminal(true);
             }
          }
        }
      }
    } {

    }
    NextQuestion next = new NextQuestion();

    next.setQuestion(bean);

    next.setDisplayStatus(getDisplayStatus(patStudyExtended));
    return next;
  }

  static public boolean isMyService(String serviceName) {
    return serviceName.startsWith(MEDIA_SERVICE_PREFIX);
  }

  private String getSurveySystemName() {
    return MEDIA_SERVICE_PREFIX + "-" + serviceName;
  }

  @Override
  public Study registerAssessment(Database database, String name, String title, String explanation) {
    MediaSystem service = MediaSystem.getInstance(getSurveySystemName(), database);
    Study study = new Study(service.getSurveySystemId(), 0, name, 0);
    study.setTitle(title);
    study.setExplanation(explanation);
    SurveySystDao ssDao = new SurveySystDao(database);
    study = ssDao.insertStudy(study);
    return study;
  }

  public SurveySystem getSurveySystem(Database database, String qType) {
    return MediaSystem.getInstance(qType, database);
  }

  private  void  getMedia(Database database, Long siteId, ArrayList<FormField> videoFields  ) {
    /*
     * Find the component with the name configured in process.xml.
     * Currently the only component type we know how to deal with is a FieldType.videoLink
     * and its attributes are those needed for a kaltura player.
     */
    AppConfigDao dao = new AppConfigDao(database, ServerUtils.getAdminUser(database));

    AppConfigEntry configEntry = dao.findAppConfigEntry(siteId, "survey", configName);
    if (configEntry != null) {
      String pageOptionsJson = configEntry.getConfigValue();

      PageConfiguration pageConfiguration = AutoBeanCodex.decode(factory, PageConfiguration.class, pageOptionsJson).as();
      List<SurveyPageComponent> surveyPageComponents = pageConfiguration.getSurveyPageComponents();
      int fieldNumber = 1;

      for (SurveyPageComponent surveyPageComponent : surveyPageComponents) {

        Map<String, String> attributes;
        if (componentName != null && surveyPageComponent != null
            && componentName.equals(surveyPageComponent.getComponentName())
            && "videoLink".equals(surveyPageComponent.getComponentType())) {
          attributes = surveyPageComponent.getAttributes();
          // See if there are qualifying attributes
          if (attributes != null) {
            // Add the heading if there is one
            if (surveyPageComponent.getComponentHeading() != null) {
              FormField headingField = factory.field().as();
              headingField.setType(FieldType.heading);
              headingField.setFieldId("1:" + fieldNumber + ":head");
              fieldNumber++;
              headingField.setLabel(surveyPageComponent.getComponentHeading());
              videoFields.add(headingField);
            }
            // Add the component field
            FormField videoField = factory.field().as();
            videoField.setType(FieldType.videoLink);
            videoField.setFieldId("1:" + fieldNumber + ":link");
            fieldNumber++;
            videoField.setAttributes(attributes);
            videoFields.add(videoField);
          }
        } else {
          logger.error("Component named " + surveyPageComponent.getComponentName() +
              " NOT found in the appconfig value for config_type 'survey' config_name " + configName);
        }
      }
    } else {
      logger.error("AppConfig NOT found for config_type 'survey' config_name " + configName);
    }

  }
  private DisplayStatus getDisplayStatus(PatientStudyExtendedData patStudyExtended) {
    DisplayStatus displayStatus = factory.displayStatus().as();

    displayStatus.setQuestionType(QuestionType.form);
    displayStatus.setQuestionId("Order1");
    displayStatus.setSurveyProviderId(Integer.toString(patStudyExtended.getSurveySystemId()));
    displayStatus.setSurveySectionId(Integer.toString(patStudyExtended.getStudyCode()));
    return displayStatus;
  }
}


class MediaSystem extends SurveySystem {
  private static final long serialVersionUID = -4382364022282098050L;
  private static MediaSystem me = null;

  private MediaSystem(String surveySystemName, Database database) throws DataException {
    SurveySystDao ssDao = new SurveySystDao(database);
    SurveySystem ssys = ssDao.getOrCreateSurveySystem(surveySystemName, null);
    this.copyFrom(ssys);
  }

  public static MediaSystem getInstance(String surveySystemName, Database database) throws DataException {
    if (me == null) {
      me = new MediaSystem(surveySystemName, database);
    }
    return me;
  }
}

