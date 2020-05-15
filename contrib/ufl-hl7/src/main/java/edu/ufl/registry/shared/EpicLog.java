/*
 * Copyright 2013 The Board of Trustees of The Leland Stanford Junior University.
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

package edu.ufl.registry.shared;

import com.google.gwt.user.client.rpc.IsSerializable;
import edu.stanford.registry.shared.DataTable;
import edu.stanford.registry.shared.DataTableBase;
import edu.stanford.registry.shared.DateUtilsIntf;
import edu.stanford.registry.shared.InvalidDataElementException;

import java.sql.Timestamp;

/**
 * Epic Log
 *
 * Records status and content of messages sent to Epic and stores them in the database
 *
 * @property  id                   Identification number of the Epic Log
 * @property  surveyRegistrationId Identification number of the Survey Registration associated with the Epic Log
 * @property  patientId            Identification number of the Patient associated with the Epic Log
 * @property  success              Indication of whether the message sent to Epic passed or failed
 * @property  message              Content of the message sent to Epic
 * @property  meta_version         Meta data version
 * @property  dtCreated            Date and time when the Log was created
 * @property  dtChanged            Date and time when the Log was last modified
 *
 * @author jrpence
 */
public class EpicLog extends DataTableBase implements IsSerializable, DataTable {

    private Long id;
    private Long surveyRegistrationId;
    private String  patientId;
    private String  success;
    private String  message;
    private String  outgoing;

    public static final String[] HEADERS           = { "Survey Registration Id", "Patient Id", "Success", "Message",
                                                       "Date Created", "Date Changed", "Outgoing" };
    public static final int[]    CHANGE_INDICATORS = { 0, 0, 1, 1, 0, 0 };

    public EpicLog() {

    }

    /**
     * Constructs an Epic Log with no date information
     *
     * @param surveyRegistrationId Identification number of the Survey Registration to associate with the Epic Log
     * @param patientId            Identification number of the Patient to associate with the Epic Log
     * @param success              Indication of whether the message sent to Epic passed or failed
     * @param message              Content of the message received from Epic
     * @param version              Meta data version
     * @param outgoingMessage      Content of the message sent to Epic
     *
     * @author jrpence
     */
    public EpicLog( Long surveyRegistrationId, String patientId, String success, String message, Integer version, String outgoingMessage ) {
        setSurveyRegistrationId( surveyRegistrationId );
        setPatientId( patientId );
        setSuccess( success );
        setMessage( message );
        setMetaVersion( version );
        setOutgoing( outgoingMessage );
    }

    /**
     * Constructs an Epic Log with date information
     *
     * @param surveyRegistrationId Identification number of the Survey Registration to associate with the Epic Log
     * @param patientId            Identification number of the Patient to associate with the Epic Log
     * @param success              Indication of whether the message sent to Epic passed or failed
     * @param message              Content of the message sent to Epic
     * @param version              Meta data version
     * @param outgoingMessage      Content of the message sent to Epic
     * @param dtCreated            Date and time when the Log was created
     * @param dtChanged            Date and time when the Log was last modified
     *
     * @author jrpence
     */
    public EpicLog( Long surveyRegistrationId, String patientId, String success, String message, Integer version,
                    String outgoingMessage, Timestamp dtCreated, Timestamp dtChanged ) {
        this( surveyRegistrationId, patientId, success, message, version, outgoingMessage );
        setDtCreated( dtCreated );
        setDtChanged( dtChanged );
    }

    /**
     * @return Long id of the Epic Log
     */
    public Long getId() {
        return id;
    }

    /**
     * @param id id to be set for the Epic Log
     */
    public void setId( Long id ) {
        this.id = id;
    }

    /**
     * @return Long id of the Survey Registration for the Epic Log
     */
    public Long getSurveyRegistrationId() {
        return surveyRegistrationId;
    }

    /**
     * @param id id of the Survey Registration to be set for the Epic Log
     */
    public void setSurveyRegistrationId( Long id ) {
        surveyRegistrationId = id;
    }

    /**
     * @return String id of the Patient for the Epic Log
     */
    public String getPatientId() {
        return patientId;
    }

    /**
     * @param patient id of the Patient to be set for the Epic Log
     */
    public void setPatientId( String patient ) {
        patientId = patient;
    }

    /**
     * @return String indication of whether the message succeeded or failed
     */
    public String getSuccess() {
        return success;
    }

    /**
     * @param successful indication to be set of whether the message succeeded or failed
     */
    public void setSuccess( String successful ) {
        success = successful;
    }

    /**
     * @return String content of the message received from Epic
     */
    public String getMessage() {
        return message;
    }

    /**
     * @param mess content to be set for the message received from Epic
     */
    public void setMessage( String mess ) {
        message = mess;
    }

    /**
     * @return String content of the message sent to Epic
     */
    public String getOutgoing() {
        return outgoing;
    }

    /**
     * @param mess content to be set for the message sent to Epic
     */
    public void setOutgoing( String mess ) {
        outgoing = mess;
    }

    /**
     * @return the HEADERS constant, which includes all table headers
     */
    public String[] getAllHeaders() {
        return HEADERS;
    }

    /**
     * Returns an int array indicating which data elements can be modified. 0 = no, 1 = yes.
     *
     * @return int array
     */
    public int[] getChangeIndicators() {
        return CHANGE_INDICATORS;
    }

    public String[] getData( DateUtilsIntf utils ) {
        String data[] = new String[9];
        if ( getId() == null) {
            data[0] = "";
        } else {
            data[0] = getId().toString();
        }
        data[1] = getSurveyRegistrationId().toString();
        data[2] = getPatientId();
        data[3] = getSuccess();
        data[4] = getMessage();
        data[5] = getMetaVersion().toString();
        data[6] = utils.getDateString( getDtCreated() );
        if ( getDtChanged() == null ) {
            data[7] = "";
        } else {
            data[7] = utils.getDateString( getDtChanged() );
        }
        data[8] = getOutgoing();
        return data;
    }

    /**
     * Sets the local values for data properties that can be changed
     * Sets the dtChanged value to the current time
     * The 0th field in the array is reserved for the ID of the EpicLog
     */
    public void setData( String data[] ) throws InvalidDataElementException {
        if ( data == null || data.length != 9 ) {
            throw new InvalidDataElementException( "Invalid number of data elements " );
        }
        
        try {
            setId( new Long( data[0] ) );
            setSurveyRegistrationId( new Long( data[1] ) );
            setPatientId( data[2] );
            setSuccess( data[3] );
            setMessage( data[4] );
            setMetaVersion( new Integer( data[5] ) );

            setDtChanged( getNow() );
            setOutgoing( data[8] );
        }
        catch ( Exception e ) {
            throw new InvalidDataElementException( e.getMessage(), e );
        }
    }

}
