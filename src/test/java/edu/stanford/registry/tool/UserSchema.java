/*
 * Copyright 2014-2017 The Board of Trustees of The Leland Stanford Junior University.
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
package edu.stanford.registry.tool;

import com.github.susom.database.Schema;

/**
 * Database objects related to user identity and authority.
 *
 * Note:  The database layer adds an index on a foreign key if one is needed.
 *
 * @author garricko
 */
public class UserSchema {
  public static Schema create() {
    return new Schema()
      .addTable("user_idp")
        .withComment("Each row represents one identity provider")
        .addColumn("idp_id").primaryKey().table()
        .addColumn("abbr_name").asString(16).notNull().unique("u_idp_abbr_uq").table()
        .addColumn("display_name").asString(256).notNull().table()
        .addCheck("u_idp_abbr_lower_ck", "abbr_name=lower(abbr_name)").table().schema()

      .addTable("user_principal")
        .withComment("Each row represents one user identity that can authenticate and (maybe) access this system."
            + "Users are never deleted. Removing all of a user's authorities effectively disables them.")
        .addColumn("user_principal_id").primaryKey().table()
        .addColumn("display_name").asString(128)
          .withComment("A friendly name for this user").table()
        .addColumn("email_addr").asString(255).table().schema()

      .addTable("user_credential")
        .withComment("Each row represents a users credentials for an identity provider")
        .addColumn("idp_id").asLong().notNull().table()
        .addColumn("user_principal_id").asLong().notNull().table()
        .addColumn("username").asString(128).notNull().unique("u_princ_username_uq").table()
        .addColumn("enabled").asStringFixed(1).notNull() // TODO missing default 'Y'
        .withComment("Disable to prevent this user from logging in or accessing anything (effectively "
            + "a soft delete, since we don't hard delete these)").table()
        .addPrimaryKey("u_cred_pk","idp_id", "user_principal_id").table()
        .addForeignKey("u_cred_idp_fk", "idp_id").references("user_idp").table()
        .addForeignKey("u_cred_principal_fk", "user_principal_id").references("user_principal").table()
        .addCheck("u_cred_username_lower_ck", "username=lower(username)").table()
        .addCheck("u_cred_enabled_yn_ck", "enabled in ('Y','N')").table().schema()

      .addTable("user_authority")
        .withComment("Table representing the \"authority\" a.k.a. \"permissions\" assigned to "
            + "a user (what someone can access)")
        .addColumn("user_principal_id").asLong().notNull().table()
        .addColumn("authority").asString(4000).notNull()
          .withComment("The authority granted to the user - right now we support two forms.\n"
              + "Grant access to a service (all caps portion references a Service enum):\n"
              + "/service/BMT_TISSUE_BANK\n"
              + "Grant access to an optional capability within a service (last portion "
              + "references a ServiceExtension enum):\n"
              + "/service/COHORT_SEARCH/extension/COHORT_EXPORT_TO_DATA_REVIEW\n"
              + "Note that access to an extension does not imply access to the service. The service authority\n"
              + "must be explicitly provided separately.").table()
        .addPrimaryKey("user_authority_pk", "user_principal_id", "authority").table()
        .addForeignKey("u_auth_principal_fk", "user_principal_id").references("user_principal").table().schema()

      .addTable("user_change_history")
        .withComment("There should be a row in this table for every change to user_principal or user_authority\n"
            + "(we record in a structured way anything that changes a user's effective permissions).")
        .addColumn("revision_number").primaryKey().table()
        .addColumn("user_principal_id").asLong().notNull().table()
        .addColumn("changed_at_time").asDate().notNull().table() // TODO missing default sysdate
        .addColumn("authenticated_user").asLong().notNull().table()
        .addColumn("authorizing_user").asLong().table()
        .addColumn("change_type").asStringFixed(1)
          .withComment("Flag: A=add user, E=enable user, D=disable user, M=modify user, "
              + "G=grant authority, R=revoke authority").table()
        .addColumn("authority").asString(4000).table()
        .addColumn("description").asString(4000).table()
        .addCheck("u_ch_hist_ch_type_ck", "change_type in ('A','E','D','M','G','R')").table()
        .addForeignKey("u_ch_hist_principal_fk", "user_principal_id").references("user_principal").table()
        .addForeignKey("u_ch_hist_authenticated_fk", "authenticated_user").references("user_principal").table()
        .addForeignKey("u_ch_hist_authorizing_fk", "authorizing_user").references("user_principal").table().schema()

      .addSequence("user_principal_sequence").order().schema()
      .addSequence("user_change_sequence").order().schema()
      .addSequence("user_idp_sequence").order().schema();
  }
}
