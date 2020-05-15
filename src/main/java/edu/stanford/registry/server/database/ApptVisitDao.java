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
package edu.stanford.registry.server.database;

import edu.stanford.registry.shared.ApptVisit;

import java.util.ArrayList;
import java.util.function.Supplier;

import com.github.susom.database.Database;

/**
 * @author Teresa Pacht <tpacht@stanford.edu>
 * @since 10/08/2019
 */
public class ApptVisitDao {
  private final Database database;

  private static final String SELECT_APPT_VISIT_SQL = "SELECT appt_visit_id, visit_type, visit_description, visit_eid"
      + " FROM appt_visit ";

  public ApptVisitDao(Supplier<Database> db) {
    database = db.get();
    if (database == null)
      throw new RuntimeException("Database is null");
  }

  public ApptVisitDao(Database dbase) {
    database = dbase;
    if (database == null)
      throw new RuntimeException("Database is null");
  }

  public ApptVisit getApptVisitById(Long visitTypeId) {

    return database.toSelect(
        SELECT_APPT_VISIT_SQL +  " WHERE appt_visit_id = ? ")
        .argLong(visitTypeId).query(rs -> {
          ApptVisit visit = new ApptVisit();
          if (rs.next()) {
            visit.setApptVisitId(rs.getLongOrZero());
            visit.setVisitType(rs.getStringOrNull());
            visit.setVisitDescription(rs.getStringOrNull());
            visit.setVisitEId(rs.getLongOrNull());
          }
          return visit;
        });
  }

  public ArrayList<ApptVisit> getApptVisitByType(String type) {

    return database.toSelect(
        SELECT_APPT_VISIT_SQL +  " WHERE visit_type = ? ")
        .argString(type).query(rs -> {
          ArrayList<ApptVisit> visitArr = new  ArrayList<>();
          while (rs.next()) {
            ApptVisit visit = new ApptVisit();
            visit.setApptVisitId(rs.getLongOrZero());
            visit.setVisitType(rs.getStringOrNull());
            visit.setVisitDescription(rs.getStringOrNull());
            visit.setVisitEId(rs.getLongOrNull());
            visitArr.add(visit);
          }
          return visitArr;
        });
  }

  public ApptVisit getApptVisitByEid(Long visitEid) {

    return database.toSelect(
        SELECT_APPT_VISIT_SQL +  " WHERE visit_eid = ? ")
        .argLong(visitEid).query(rs -> {
          ApptVisit visit = new ApptVisit();
          if (rs.next()) {
            visit.setApptVisitId(rs.getLongOrZero());
            visit.setVisitType(rs.getStringOrNull());
            visit.setVisitDescription(rs.getStringOrNull());
            visit.setVisitEId(rs.getLongOrNull());
          }
          return visit;
        });
  }

  public ArrayList<ApptVisit> getApptVisitByDescription(String description) {

    return database.toSelect(
        SELECT_APPT_VISIT_SQL +  " WHERE visit_description = ? ")
        .argString(description).query(rs -> {
          ArrayList<ApptVisit> visitArr = new  ArrayList<>();
          while (rs.next()) {
            ApptVisit visit = new ApptVisit();
            visit.setApptVisitId(rs.getLongOrZero());
            visit.setVisitType(rs.getStringOrNull());
            visit.setVisitDescription(rs.getStringOrNull());
            visit.setVisitEId(rs.getLongOrNull());
            visitArr.add(visit);
          }
          return visitArr;
        });
  }

  public ApptVisit insertApptVisit(String visitType, String visitDescription, Long visitEid) {
    ApptVisit visit = new ApptVisit();
    visit.setVisitType(visitType);
    visit.setVisitDescription(visitDescription);
    visit.setVisitEId(visitEid);

    String stmt = "INSERT INTO appt_visit (appt_visit_id, visit_type, visit_description, visit_eid)"
        + " VALUES (:pk, ?, ?, ?) ";

    Long visitId = database.toInsert(stmt)
        .argPkSeq(":pk", "appt_visit_seq")
        .argString(visitType)
        .argString(visitDescription)
        .argLong(visitEid)
        .insertReturningPkSeq("appt_visit_id");
    visit.setApptVisitId(visitId);
    return visit;
  }
}
