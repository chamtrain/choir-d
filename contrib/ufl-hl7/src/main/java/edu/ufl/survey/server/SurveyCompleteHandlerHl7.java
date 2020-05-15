/*
 * Copyright 2014 The Board of Trustees of The Leland Stanford Junior University.
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

package edu.ufl.survey.server;

import edu.stanford.registry.server.database.Database;
import edu.stanford.registry.server.reports.PatientReport;
import edu.stanford.registry.server.service.Provider;
import edu.stanford.registry.server.utils.DataBaseUtils;
import edu.stanford.registry.shared.InvalidDataElementException;
import edu.stanford.registry.shared.SurveyRegistration;
import edu.stanford.survey.server.SurveyComplete;
import edu.stanford.survey.server.SurveyCompleteHandler;
import edu.ufl.registry.server.service.hl7message.EpicExportData;
import edu.ufl.registry.server.service.hl7message.HL7Generator;
import edu.ufl.registry.server.service.hl7message.HL7Transmission.HL7Sender;
import edu.ufl.registry.shared.EpicLog;
import org.apache.log4j.Logger;

import java.io.IOException;

/**
 * Create HL7 message upon completion of each survey.
 */
public class SurveyCompleteHandlerHl7 implements SurveyCompleteHandler {
    private static Logger logger = Logger.getLogger( SurveyCompleteHandlerHl7.class );

    public Long getSurveyRegistrationId( SurveyComplete survey, Supplier<Database> database ) {

        return database.get().queryLong( "select survey_reg_id from survey_registration sr, survey_token st, survey_complete sc "
                + " where st.survey_site_id = sc.survey_site_id and st.survey_token_id = sc.survey_token_id "
                + "   and sr.survey_site_id = sc.survey_site_id and to_char(nvl(sr.token,0)) = st.survey_token "
                + "   and sc.survey_site_id = ? and sc.survey_token_id = ?", new Object[] { survey.getSurveySiteId(), survey.getSurveyTokenId() } );
    }

    @Override

    /**
     * surveyCompleted
     *
     * @author Frank Correale
     */
    public void surveyCompleted( SurveyComplete survey, Supplier<Database> dbp ) {

        Long surveyRegId = getSurveyRegistrationId( survey, dbp );
        if ( surveyRegId == null ) {
            logger.debug( "surveyRegId not found for site " + survey.getSurveySiteId( ) + " token_id " + survey.getSurveyTokenId( ) );
        }

        SurveyRegistration registration = DataBaseUtils.getSurveyRegistrationByRegId( dbp.get( ), surveyRegId );
        if ( registration == null ) {
            throw new RuntimeException( "no registration for site " + surveyRegId + " token_id " + surveyRegId );
        }

        EpicExportData surveyData = null;
        try {
            surveyData = new EpicExportData( dbp.get( ), new PatientReport( dbp.get( ) ), registration );
        } catch ( IOException | InvalidDataElementException e ) {
            e.printStackTrace();
        }

        // create the HL7 Generator object, and call the generateHL7 function and send it surveyData object filled with the data it needs for the message
        HL7Generator hl7Generator = new HL7Generator();
        String hl7MessageNumericOBX = null;
        try {
            // this is the HL7Message
            hl7MessageNumericOBX = hl7Generator.generateHL7( surveyData );
        } catch ( Exception e ) {
            e.printStackTrace();
        }

        HL7Sender hl7Sender = new HL7Sender();
        String responseNumericOBX = hl7Sender.translate( hl7MessageNumericOBX );

        //Create EpicLogs for messages
        String successStatus = "Failure";
        Long surveyRegistrationId = surveyData.getSurveyRegistration().getSurveyRegId();
        if ( !responseNumericOBX.contains( "Error" ) ) {
            successStatus = "Success";
        }

        EpicLog logMessage;

        //log numeric response based off translate method using epic log object
        if ( responseNumericOBX.contains( "MSA|AA" ) ) {
            //success
            successStatus = "Success";
        } else {
            //failure
            successStatus = "Failure";
        }

        logMessage = new EpicLog( surveyRegistrationId, surveyData.getPatientId(), successStatus, responseNumericOBX, 0, hl7MessageNumericOBX );
        DataBaseUtils.addEpicLog( dbp.get(), logMessage );

        logger.debug( "Translator responseNumeric = " + responseNumericOBX );
    }
}