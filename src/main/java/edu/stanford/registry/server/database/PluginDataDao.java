/*
 * Copyright 2018 The Board of Trustees of The Leland Stanford Junior University.
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

package edu.stanford.registry.server.database;

import edu.stanford.registry.client.api.ApiObjectFactory;
import edu.stanford.registry.client.api.PluginPatientDataObj;
import edu.stanford.registry.client.api.PluginPatientHistoryDataObj;
import edu.stanford.registry.server.SiteInfo;

import java.util.ArrayList;
import java.util.Date;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import com.github.susom.database.Database;
import com.github.susom.database.Row;
import com.github.susom.database.RowsHandler;
import com.github.susom.database.SqlSelect;
import com.google.web.bindery.autobean.vm.AutoBeanFactorySource;

/**
 * Handles the database calls for plugin data stored and retrieved through the API
 */
public class PluginDataDao {

  private static final Logger logger = LoggerFactory.getLogger(PluginDataDao.class);
  private final static String SELECT_FROM_PLUGIN_PATIENT_DATA =
      "SELECT ppd.data_id, ppd.data_type, ppd.data_version, ppd.patient_id, ppd.dt_created, "
          + "ppd.data_value FROM plugin_patient_data ppd ";
  private final static String SELECT_FROM_PLUGIN_PATIENT_DATA_HISTORY =
      "SELECT ppdh.data_id, ppdh.data_type, ppdh.data_version, ppdh.patient_id, ppdh.dt_created, "
          + "ppdh.data_value FROM plugin_patient_data_history ppdh ";
  private final static String SELECT_FULL_PLUGIN_PATIENT_DATA_HISTORY =
      "SELECT ppdh.data_hist_id, ppdh.change_type, "
          + "ppdh.data_id, ppdh.data_type, ppdh.data_version, ppdh.patient_id, ppdh.dt_created, "
          + "ppdh.data_value FROM plugin_patient_data_history ppdh ";
  private final Database database;
  private final SiteInfo siteInfo;
  private final ApiObjectFactory factory = AutoBeanFactorySource.create(ApiObjectFactory.class);

  public PluginDataDao(Database database, SiteInfo siteInfo) {
    this.database = database;
    this.siteInfo = siteInfo;
  }

  public PluginPatientDataObj findPluginPatientData(Long dataId) {
    if (dataId == null) {
      return null;
    }
    String sql = SELECT_FROM_PLUGIN_PATIENT_DATA + " WHERE ppd.data_id = ? ";
    SqlSelect toSelect = database.toSelect(sql)
        .argLong(dataId);
    return toSelect.query(getFirstRowHandler());

  }

  public PluginPatientDataObj findPluginPatientData(
      final String dataType, final String patientId, final String dataVersion) {
    PluginPatientDataObj data = null;
    if (dataType != null && patientId != null) {
      String sql = SELECT_FROM_PLUGIN_PATIENT_DATA + " WHERE ppd.survey_site_id = :site and ppd.patient_id = ? "
          + " and ppd.data_type = ?";
      if (dataVersion != null && !dataVersion.isEmpty()) {
        sql = sql + " and ppd.data_version = ?";
      }
      SqlSelect toSelect =
          database.toSelect(sql)
              .argLong(":site", siteInfo.getSiteId())
              .argString(patientId)
              .argString(dataType);
      if (dataVersion != null && !dataVersion.isEmpty()) {
        toSelect = toSelect.argString(dataVersion);
      }
      data = toSelect
          .query(
              rs -> {
                if (rs.next()) {
                  return fromRow(rs);
                }
                return null;
              });
    }
    return data;
  }

  public ArrayList<PluginPatientDataObj> findAllPluginPatientData(
      final String dataType, final String patientId, final String dataVersion, Date fromTime, Date toTime) {
    ArrayList<PluginPatientDataObj> allPluginData = new ArrayList<>();
    if (dataType != null && patientId != null) {
      String sql =
          SELECT_FROM_PLUGIN_PATIENT_DATA_HISTORY + " WHERE ppdh.survey_site_id = :site and ppdh.patient_id = :patient "
              + " and ppdh.data_type = :type";
      if (dataVersion != null && !dataVersion.isEmpty()) {
        sql = sql + " and ppdh.data_version = :vs";
      }
      if (fromTime != null) {
        sql = sql + " and ppdh.dt_created >= :from ";
      }
      if (toTime != null) {
        sql = sql + " and ppdh.dt_created <= :to ";
      }
      sql = sql + " order by ppdh.data_type ";
      if (dataVersion != null && !dataVersion.isEmpty()) {
        sql = sql + ", ppdh.data_version";
      }
      sql = sql + ", ppdh.dt_created";
      SqlSelect toSelect =
          database.toSelect(sql)
              .argLong(":site", siteInfo.getSiteId())
              .argString(":patient", patientId)
              .argString(":type", dataType);
      if (dataVersion != null && !dataVersion.isEmpty()) {
        toSelect = toSelect.argString(":vs", dataVersion);
      }
      if (fromTime != null) {
        toSelect = toSelect.argDate(":from", fromTime);
      }
      if (toTime != null) {
        toSelect = toSelect.argDate(":to", toTime);
      }
      allPluginData = toSelect
          .query(
              getAllRowHandler());
    }
    return allPluginData;
  }

  public ArrayList<PluginPatientHistoryDataObj> findAllPluginPatientDataHistory(
      final String dataType, final String patientId, final String dataVersion) {
    ArrayList<PluginPatientHistoryDataObj> allPluginData = new ArrayList<>();
    if (dataType != null && patientId != null) {
      String sql =
          SELECT_FULL_PLUGIN_PATIENT_DATA_HISTORY + " WHERE ppdh.survey_site_id = :site and ppdh.patient_id = :patient "
              + " and ppdh.data_type = :type";
      if (dataVersion != null && !dataVersion.isEmpty()) {
        sql = sql + " and ppdh.data_version = :vs";
      }
      SqlSelect toSelect =
          database.toSelect(sql)
              .argLong(":site", siteInfo.getSiteId())
              .argString(":patient", patientId)
              .argString(":type", dataType);
      if (dataVersion != null && !dataVersion.isEmpty()) {
        toSelect = toSelect.argString(":vs", dataVersion);
      }
      allPluginData = toSelect
          .query(
              getHistoryRowHandler());
    }
    return allPluginData;
  }

  public PluginPatientDataObj addPluginPatientData(String dataType, String patientId, String dataVersion, String dataValue) {
    PluginPatientDataObj pluginPatientDataObj = findPluginPatientData(dataType, patientId, dataVersion);

    if (pluginPatientDataObj == null) {
      Long dataId = database.toInsert("insert into plugin_patient_data  "
          + " (data_id, data_type, data_version, survey_site_id, patient_id, dt_created) "
          + "values (:pk, ?, ?, :site, ?, :now)")
          .argPkSeq(":pk", "plugin_patient_sequence")
          .argString(dataType)
          .argString(dataVersion)
          .argLong(":site", siteInfo.getSiteId())
          .argString(patientId)
          .argDateNowPerDb(":now")
          .insertReturningPkSeq("data_id");
      database.toUpdate("update plugin_patient_data set data_value = ? where data_id = ?")
          .argClobString(dataValue)
          .argLong(dataId)
          .update(1);

      pluginPatientDataObj = findPluginPatientData(dataId);
      insertChangeHistory("A", pluginPatientDataObj);
    } else {
      database.toUpdate("update plugin_patient_data  set data_value = ?, dt_created = :now where data_id = ?")
          .argClobString(dataValue)
          .argDateNowPerDb(":now")
          .argLong(pluginPatientDataObj.getDataId())
          .update(1);
      pluginPatientDataObj = findPluginPatientData(pluginPatientDataObj.getDataId());
      insertChangeHistory("M", pluginPatientDataObj);
    }
    return pluginPatientDataObj;
  }

  private void insertChangeHistory(String changeType, PluginPatientDataObj existing) {

    Long histId = database.toInsert("insert into plugin_patient_data_history (data_hist_id, change_type, "
        + "data_id, data_type, data_version, survey_site_id, patient_id, dt_created, data_value) "
        + "values (:pk, :type, ?, ?, ?, :site, ?, :dt, ?)")
        .argPkSeq(":pk", "plugin_patient_sequence")
        .argString(":type", changeType)
        .argLong(existing.getDataId())
        .argString(existing.getDataType())
        .argString(existing.getDataVersion())
        .argLong(":site", siteInfo.getSiteId())
        .argString(existing.getPatientId())
        .argDate(":dt", new Date(existing.getCreatedTime()))
        .argString(existing.getDataValue())
        .insertReturningPkSeq("data_hist_id");

    if (existing.getDataValue() != null) {
      try {
        database.toUpdate("update plugin_patient_data_history set data_value = ? where data_hist_id = ?")
            .argClobString(existing.getDataValue())
            .argLong(histId)
            .update(1);
      } catch (Exception ex) {
        logger.error("Error updating history with clob value: {} " + existing.getDataValue());
      }
    }
  }

  private RowsHandler<PluginPatientDataObj> getFirstRowHandler() {
    return
        rs -> {
          if (rs.next()) {
            return fromRow(rs);
          }
          return null;
        };
  }

  private RowsHandler<ArrayList<PluginPatientDataObj>> getAllRowHandler() {
    return
        rs -> {
          ArrayList<PluginPatientDataObj> ppdList = new ArrayList<>();
          while (rs.next()) {
            ppdList.add(fromRow(rs));
          }
          return ppdList;
        };
  }

  private RowsHandler<ArrayList<PluginPatientHistoryDataObj>> getHistoryRowHandler() {
    return
        rs -> {
          ArrayList<PluginPatientHistoryDataObj> ppdhList = new ArrayList<>();
          while (rs.next()) {
            ppdhList.add(fromHistory(rs));
          }
          return ppdhList;
        };
  }

  private PluginPatientDataObj fromRow(Row rs) {
    PluginPatientDataObj ppd = factory.pluginPatientData().as();
    ppd.setDataId(rs.getLongOrZero());
    ppd.setDataType(rs.getStringOrEmpty());
    ppd.setDataVersion(rs.getStringOrEmpty());
    ppd.setPatientId(rs.getStringOrEmpty());
    Date createdTime = rs.getDateOrNull();
    if (createdTime != null) {
      ppd.setCreatedTime(createdTime.getTime());
    }
    ppd.setDataValue(rs.getClobStringOrEmpty());
    return ppd;
  }

  private PluginPatientHistoryDataObj fromHistory(Row rs) {
    PluginPatientHistoryDataObj ppdh = factory.pluginPatientHistoryData().as();
    ppdh.setHistoryId(rs.getLongOrZero());
    ppdh.setChangeType(rs.getStringOrEmpty());
    ppdh.setDataId(rs.getLongOrZero());
    ppdh.setDataType(rs.getStringOrEmpty());
    ppdh.setDataVersion(rs.getStringOrEmpty());
    ppdh.setPatientId(rs.getStringOrEmpty());
    Date createdTime = rs.getDateOrNull();
    if (createdTime != null) {
      ppdh.setCreatedTime(createdTime.getTime());
    }
    ppdh.setDataValue(rs.getClobStringOrEmpty());
    return ppdh;
  }

}
