package edu.stanford.registry.server.survey;

import com.github.susom.database.Database;

public interface CustomSurveyServiceIntf extends SurveyServiceIntf {

  String getSurveySystemName();

  int getSurveySystemId(Database database);

  String getStudyName();

  String getTitle();

  void setValue(String value);

}
