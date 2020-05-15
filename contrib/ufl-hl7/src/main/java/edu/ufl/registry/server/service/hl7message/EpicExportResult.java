package edu.ufl.registry.server.service.hl7message;

import java.text.SimpleDateFormat;
import java.util.*;

import edu.stanford.registry.server.charts.ChartConfigurationOptions;
import edu.stanford.registry.server.database.Database;
import edu.stanford.registry.server.plugin.ScoreProvider;
import edu.stanford.registry.server.survey.ChartInfo;
import edu.stanford.registry.server.survey.PrintStudy;
import edu.stanford.registry.server.utils.ChartUtilities;
import edu.stanford.registry.server.utils.DataBaseUtils;
import edu.stanford.registry.server.utils.XMLFileUtils;
import edu.stanford.registry.shared.*;
import org.jfree.data.time.TimeSeriesCollection;

import java.io.IOException;
import java.sql.SQLException;
import java.util.ArrayList;
import java.util.Hashtable;

import org.apache.log4j.Logger;

public class EpicExportResult {
    private static Logger logger = Logger.getLogger( EpicExportResult.class );

    private Long score; //obx5
    private String componentId; //obx3_1

    private String observationResultStatus; //obx11

    private Date timeOfObservation; //obx14 -- @TODO will this be the same value as for the obr in EpicExportData?
    private Date patientStudyCreated;
    private Date patientStudyChanged;

    private String componentName; //obx3_2
    private String obxCodingSystem; //obx3_3
    private String valueType;
    private String scoreRange; //obx7
    private String category;
    private String idType;

    /**
     * Epic Export Result
     *
     * @param score Long
     * @param componentId String
     * @param observationResultStatus String
     * @param timeOfObservation Date
     * @param componentName String
     * @param obxCodingSystem String
     * @param valueType String
     * @param scoreRange String
     * @param category String
     * @param idType String
     *
     * @author Frank Correale
     */
    public EpicExportResult( Long score, String componentId, String observationResultStatus, Date timeOfObservation, String componentName, String obxCodingSystem, String valueType,
                             String scoreRange, String category, String idType ) {
        this.score = score;
        this.componentId = componentId;
        this.observationResultStatus = observationResultStatus;
        this.timeOfObservation = timeOfObservation;
        this.componentName = componentName;
        this.obxCodingSystem = obxCodingSystem;
        this.valueType = valueType;
        this.scoreRange = scoreRange;
        this.category = category;
        this.idType = idType;
    }

    /**
     * getChartInfo
     *
     * @param pstudies this is PatientStudies
     * @param chosenStudies this is the chosenStudies
     * @param dbp this is the database connection
     * @return this returns chartInfo
     * @author Frank Correale
     * @throws InvalidDataElementException
     * @throws IOException
     */
    static ChartInfo getChartInfo( ArrayList<PatientStudyExtendedData> pstudies,
                                    ArrayList<PrintStudy> chosenStudies, Database dbp ) throws InvalidDataElementException,
            IOException {

        if ( chosenStudies == null || chosenStudies.size() < 1 ) {
            throw new InvalidDataElementException( "No patient studies" );
        }

        if ( pstudies.size() < 1 ) {
            throw new InvalidDataElementException( "No patient studies" );
        }
        if ( chosenStudies.size() < 1 ) {
            throw new InvalidDataElementException( "Called without any studies" );
        }
        ChartUtilities chartUtils = new ChartUtilities();
        ScoreProvider provider = chartUtils.getScoreProvider( dbp, pstudies, chosenStudies.get( 0 ).getStudyCode() );

        ChartInfo chartInfo = null;

        /*
         * Combine the series collections and the chartScores into one ChartInfo
         */
        TimeSeriesCollection collection = null;
        ArrayList<ChartScore> chartScores = null;

        /*
         * this has been set to null as it appears to be not needed in HL7 messaging, but many of the functions being
         * called are looking for this variable
         */
        ChartConfigurationOptions opts = null;

        for ( int s = 0; s < chosenStudies.size(); s++ ) {
            PrintStudy study = chosenStudies.get( s );
            if ( !provider.acceptsSurveyName( study.getStudyDescription() ) ) {
                provider = chartUtils.getScoreProvider( dbp, pstudies, study.getStudyCode() );
            }

            ChartInfo thisChartInfo = chartUtils.createChartInfo( pstudies, study, provider, opts );
            if ( chartInfo == null ) {
                chartInfo = thisChartInfo;
                if ( thisChartInfo != null ) {
                    collection = ( TimeSeriesCollection ) chartInfo.getDataSet();
                    chartScores = chartInfo.getScores();
                }
            } else {

                if ( thisChartInfo != null ) {

                    TimeSeriesCollection thisCollection = ( TimeSeriesCollection ) thisChartInfo.getDataSet();

                    for ( int d = 1; d < thisCollection.getSeriesCount(); d++ ) {
                        collection.addSeries( thisCollection.getSeries( d ) );
                    }

                    ArrayList<ChartScore> thisScores = thisChartInfo.getScores();

                    for ( int sc = 0; sc < thisScores.size(); sc++ ) {
                        chartScores.add( thisScores.get( sc ) );
                    }
                }
            }
        }
        if ( chartInfo == null ) {
            return null;
        }

        chartInfo.setScores( chartScores );
        chartInfo.setDataSet( collection );
        ArrayList<Study> studies = new ArrayList<Study>();
        for ( int p = 0; p < chosenStudies.size(); p++ ) {
            studies.add( chosenStudies.get( p ) );
        }
        return chartInfo;
    }

    /**
     * getProcessessOrdered
     *
     * @param processType this is the process type
     * @param orderType this is the order type
     * @param database this is the database connection
     * @return this returns the processesOrdered
     * @author Frank Correale
     */
    static ArrayList<PrintStudy> getProcessessOrdered( String processType, String orderType, Database database ) {

        ArrayList<SurveySystem> surveySystems = DataBaseUtils.getSurveySystems( database );
        ArrayList<Study> studies = DataBaseUtils.getStudies( database );
        return XMLFileUtils.getInstance().getProcessessOrdered( processType, orderType, surveySystems, studies );
    }

    /**
     * getPatientStudies
     *
     * @param database this is the database connection
     * @param registration this is the survey registration object
     * @return this returns the patient studies
     * @author Frank Correale
     */
    static ArrayList<PatientStudyExtendedData> getPatientStudies( Database database, SurveyRegistration registration ) {
    /*
     * Find all the completed surveys up through the requested id
     */
        ArrayList<edu.stanford.registry.shared.PatientStudyExtendedData> patientStudies =
                DataBaseUtils.getPatientStudyExtendedDataByPatientId( database, registration.getPatientId(),
                        registration.getSurveyDt() );
        try {
            Hashtable<Integer, Boolean> assisted = DataBaseUtils.getPatientAssistedStudyTokens( database, registration.getPatientId() );
            for ( int i = 0; i < patientStudies.size(); i++ ) {
                edu.stanford.registry.shared.PatientStudyExtendedData patientStudy = patientStudies.get( i );
                if ( patientStudy.getToken() != null ) {
                    if ( assisted.get( patientStudy.getToken() ) != null ) {
                        patientStudy.setAssisted( true );
                    }
                }
            }
        } catch ( SQLException sqle ) {
            logger.debug( "checking for assisted surveys for patient " + registration.getPatientId() + " returned: " + sqle.getMessage() );
        }
        return patientStudies;
    }

    /**
     * invertScore
     *
     * inverts the score
     *
     * @param score this is the score
     * @author Frank Correale
     * @return score
     */
    static Long invertScore( Long score ) {
        if ( score < 50 ) {
            score = 50 + ( 50 - score );
        } else if ( score > 50 ) {
            score = 50 - ( score - 50 );
        }
        return score;
    }

    /*
     * Getter Methods
     */

    /**
     * toString()
     *
     * @return the toString
     * @author Frank Correale
     */
    public String toString() {
        return "Score: " + score + ", ObservationResultStatus: " + observationResultStatus + ", TimeOfObservation: " + timeOfObservation +
                ", PatientStudyCreated: " + patientStudyCreated + ", PatientStudyChanged: " + patientStudyChanged + ", ComponentName: " + componentName +
                ", ObxCodingSystem: " + obxCodingSystem + ", ValueType: " + valueType + ", ScoreRange: " + scoreRange + ", Category: " + category + ", IdType";
    }

    /**
     * getScore()
     *
     * @return score as a String
     * @author Frank Correale
     */
    public String getScore() {
        return score.toString();
    }

    /**
     * getComponentId()
     *
     * @return componentId
     * @author Frank Correale
     */
    public String getComponentId() {
        return componentId;
    }

    /**
     * getComponentName()
     *
     * @return componentName
     * @author Frank Correale
     */
    public String getComponentName() {
        return componentName;
    }

    /**
     * getScoreRange()
     *
     * @return scoreRange
     * @author Frank Correale
     */
    public String getScoreRange() {
        return scoreRange;
    }

    /**
     * getValueType()
     *
     * @return valueType
     * @author Frank Correale
     */
    public String getValueType() {
        return valueType;
    }

    /**
     * getCategory()
     *
     * @return category
     * @author Frank Correale
     */
    public String getCategory() {
        return category;
    }

    /**
     * getIdType()
     *
     * @return idType
     * @author Frank Correale
     */
    public String getIdType() {
        return idType;
    }

    /**
     * getPatientStudyCreated()
     *
     * @return studyCreated formatted
     * @author Frank Correale
     */
    public String getPatientStudyCreated() {
        SimpleDateFormat format = new SimpleDateFormat( "yyyyMMddhhmm" );
        String studyCreated;
        studyCreated = format.format( patientStudyCreated );
        return studyCreated;
    }

    /**
     * getObxCodingSystem()
     *
     * @return obxCodingSystem
     * @author Frank Correale
     */
    public String getObxCodingSystem() {
        return obxCodingSystem;
    }

    /**
     * getTimeOfObservation()
     *
     * @return observationTime formatted
     * @author Frank Correale
     */
    public String getTimeOfObservation() {
        SimpleDateFormat format = new SimpleDateFormat( "yyyyMMddhhmm" );
        String observationTime;
        observationTime = format.format( timeOfObservation );
        return observationTime;
    }

    /**
     * getResultsStatus()
     *
     * @return observationResultStatus
     * @author Frank Correale
     */
    public String getResultsStatus() {
        return observationResultStatus;
    }

    /**
     * getPatientStudyChanged()
     *
     * @return studyChanged formatted
     * @author Frank Correale
     */
    public String getPatientStudyChanged() {
        SimpleDateFormat format = new SimpleDateFormat( "yyyyMMddhhmm" );
        String studyChanged;
        studyChanged = format.format( patientStudyChanged );
        return studyChanged;
    }

    /**
     * setValueType
     *
     * @param type user defined type
     */
    public void setValueType( String type ) {
        valueType = type;
    }
}
