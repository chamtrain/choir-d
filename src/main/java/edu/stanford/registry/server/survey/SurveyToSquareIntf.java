package edu.stanford.registry.server.survey;

import edu.stanford.registry.shared.Study;
import edu.stanford.survey.client.api.FieldType;
import edu.stanford.survey.server.SurveyQuery;

import java.util.ArrayList;
import java.util.LinkedHashMap;

import com.github.susom.database.Database;
import com.github.susom.database.Sql;

/**
 * Created by tpacht on 4/20/2016.
 */
public interface SurveyToSquareIntf {

  public int addCompletedSurveyValues(Database database, Long tokenId, SurveyQuery query, Study study, String prefix, Sql sql, String separator);

  public ArrayList<String> getSurveyDocumentation(Database database, Study study, String prefix);

  public LinkedHashMap<String, FieldType> getSquareTableColumns(Database database, Study study, String prefix);
}
