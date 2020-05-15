package edu.ufl.registry.server.service.hl7message;

import edu.stanford.registry.server.ServerUtils;
import edu.stanford.registry.server.database.Database;
import edu.stanford.registry.server.plugin.ScoreProvider;
import edu.stanford.registry.server.reports.PatientReport;
import edu.stanford.registry.server.survey.ChartInfo;
import edu.stanford.registry.server.survey.PrintStudy;
import edu.stanford.registry.server.utils.ChartUtilities;
import edu.stanford.registry.server.utils.DataBaseUtils;
import edu.stanford.registry.shared.*;
import edu.stanford.registry.shared.survey.SurveyQuestionIntf;
import org.apache.commons.math3.distribution.NormalDistribution;
import org.apache.log4j.Logger;

import java.io.IOException;
import java.text.SimpleDateFormat;
import java.util.ArrayList;
import java.util.Date;
import java.util.HashMap;
import java.util.Set;

public class EpicExportData {
    private static Logger logger = Logger.getLogger( EpicExportResult.class );

    private String sender; //msh4, msh6
    private String receiver; //msh5
    private String agencyIdentifier; //msh3, obr4_1 (may be used for obr4_3)
    private String patientId; //pid3_1
    private String empid; //pid3_1 for the pdf HL7 message
    private String providerEid; //obr16_1
    private String assigningAuthority; //pid3_5 (according to Vince, UFHealth has at least 3 sets of assigning authorities)
    private String patientFirstName; //pid5_1
    private String patientLastName; //pid5_2
    private String patientAccountNumber; //pid18 -- currently set to the Survey Registration Encounter EID
    private String externalTestName; //obr4_2 (name of the survey taken?)
    private String obrCodingSystem; //obr4_3 (may be the same as agencyIdentifier)
    private String specimenActionCode; //obr11
    private String identifierTypeCode; //obr16_13
    private String resultsStatus; //obr25
    private String gender; //pid8
    private String ancillaryApplication; //obr3

    private Date dateOfBirth; //pid7
    private Date timeOfObservation; //obr7

    private ArrayList<EpicExportResult> results = new ArrayList<>();

    private SurveyRegistration surveyRegistration;

    /**
     * Constructor
     *
     * @param database this is the database connection
     * @param pReport this is the patient report
     * @param registration this is the survey registration information
     * @throws IOException
     * @throws InvalidDataElementException
     * @author Frank Correale - 9/16/14
     */
    public EpicExportData( Database database, PatientReport pReport, SurveyRegistration registration ) throws IOException, InvalidDataElementException {

        // creation of a few objects necessary for the calculations later to come
        ServerUtils serverUtils = ServerUtils.getInstance();
        Patient thePatient = DataBaseUtils.getPatient( database, registration.getPatientId() );
        PatientResult thePatientResult = DataBaseUtils.getPatientResult( database, registration.getSurveySiteId(), registration.getSurveyRegId(), pReport.getResultType().getPatientResTypId() );

        // Setting the Patient properties
        patientFirstName = thePatient.getFirstName();
        patientLastName = thePatient.getLastName();
        dateOfBirth = thePatient.getDtBirth();

        //checking that a gender has been set before trying to run toString on it
        PatientAttribute gend = thePatient.getAttribute( "gender" );
        if ( gend != null ) {
            gender = gend.getDataValue().toString();
        } else {
            gender = "Unknown"; //handling blank gender
        }

        PatientAttribute empid = thePatient.getAttribute( "epid" );
        if ( empid != null ) {
            this.empid = empid.getDataValue().toString();
        }

        // checking to make sure the PatientResult has been created
        if ( thePatientResult != null ) {
            timeOfObservation = thePatientResult.getDtCreated(); // this is using the date the patient result was created, at the end of the survey
        } else {
            timeOfObservation = new Date();
        }

        // Setting the properties of the class
        patientId = thePatient.getPatientId();
        patientAccountNumber = registration.getEncounterEid();
        resultsStatus = serverUtils.getParam( "HL7Message.observationResultStatus" );
        agencyIdentifier = serverUtils.getParam( "HL7Message.agencyIdentifier" );
        externalTestName = serverUtils.getParam( "HL7Message.externalTestName" );
        specimenActionCode = serverUtils.getParam( "HL7Message.specimenActionCode" );
        assigningAuthority = serverUtils.getParam( "HL7Message.assigningAuthority" );
        obrCodingSystem = serverUtils.getParam( "HL7Message.codingSystem" );
        sender = serverUtils.getParam( "HL7Message.sender" );
        receiver = serverUtils.getParam( "HL7Message.receiver" );
        identifierTypeCode = serverUtils.getParam( "HL7Message.identifierTypeCode" );
        ancillaryApplication = serverUtils.getParam( "HL7Message.ancillaryApplication" );
        surveyRegistration = registration;

        Long providerId = registration.getProviderId();

        // trying to prevent calling the database if providerId was not supplied
        if ( providerId != null ) {
            // This is the sql statement to get the provider_eid from the provider table
            String sql = "SELECT PROVIDER_EID FROM PROVIDER WHERE PROVIDER_ID = ?";

            // Check that we have a result before trying to utilize the result
            String tempId = database.get().queryString( sql, new Object[] { providerId } ) ;

            if ( tempId != null ) {
                providerEid = tempId;
            } else {
                providerEid = null;
            }
        } else {
            providerEid = null;
        }

        // Debugging all the properties
        logger.debug( "patient first name " + patientFirstName );
        logger.debug( "patient last name " + patientLastName );
        logger.debug( "patient DOB " + dateOfBirth );
        logger.debug( "patient gender " + gender );
        logger.debug( "patient time of Observation " + this.timeOfObservation );
        logger.debug( "patient Id " + patientId );
        logger.debug( "results status " + resultsStatus );
        logger.debug( "patient account number " + patientAccountNumber );
        logger.debug( "agencyIdentifier " + agencyIdentifier );
        logger.debug( "externalTestName " + externalTestName );
        logger.debug( "specimenActionCode " + specimenActionCode );
        logger.debug( "assigningAuthority " + assigningAuthority );
        logger.debug( "obrCodingSystem " + obrCodingSystem );
        logger.debug( "Sender " + sender );
        logger.debug( "Receiver " + receiver );
        logger.debug( "Identifier Type Code " + identifierTypeCode );
        logger.debug( "ProviderEid " + providerEid );
        logger.debug( "SurveyRegistration " + surveyRegistration );

        setEpicExportResults( database, pReport, registration );
    }

    /**
     * setEpicExportResults
     *
     * @param database the database connection
     * @param pReport Patient Report
     * @param registration the SurveyRegistration
     * @throws IOException
     * @throws InvalidDataElementException
     *
     * @author Frank Correale v1.0 original coding, v1.2 refactoring code
     * @author kpharvey v1.1 moving code/refactoring code
     *
     */
    public void setEpicExportResults( Database database, PatientReport pReport, SurveyRegistration registration ) throws IOException, InvalidDataElementException {

        ServerUtils serverUtils = ServerUtils.getInstance();
        PatientResult thePatientResult = DataBaseUtils.getPatientResult( database, registration.getSurveySiteId(), registration.getSurveyRegId(), pReport.getResultType().getPatientResTypId() );
        Patient thePatient = DataBaseUtils.getPatient( database, registration.getPatientId() );

        ArrayList<PatientStudyExtendedData> patientStudies = EpicExportResult.getPatientStudies(database, registration);

        ArrayList<PrintStudy> printStudies;

        // Need lastToken to get a process, to get printStudies
        Integer lastToken = 0;
        if ( patientStudies.size() > 0 ) {
            lastToken = patientStudies.get( patientStudies.size() - 1 ).getToken();
        }

        String process = DataBaseUtils.getSurveyType( database, lastToken );
        printStudies = EpicExportResult.getProcessessOrdered(process, Constants.XML_PROCESS_ORDER_PRINT, database);

        // these local variables will be used to track when the Patient Study has been changed and created
        Date pStudyChanged = null;
        Date pStudyCreated = null;

        //need a chartInfo to call getScores() function
        ChartInfo chartInfo;

        chartInfo = EpicExportResult.getChartInfo(patientStudies, printStudies, database);

        ChartUtilities chartUtils = new ChartUtilities();
        ScoreProvider provider = chartUtils.getScoreProvider( database, patientStudies, printStudies.get( 0 ).getStudyCode() );

        // setting up the chartScore object
        ChartScore chartScore = null;

        // iterate through the printStudies
        for ( int count = 0; count < printStudies.size(); count++ ) {

            PrintStudy printStudy = printStudies.get( count );

            Long patientStudyId = 0l;
            if ( chartInfo != null ) {

                ArrayList<ChartScore> scores = chartInfo.getScores();

                // iterating through the scores
                for ( int s = 0; s < scores.size(); s++ ) {
                    PatientStudy patientStudy;
                    ChartScore theScore = scores.get( s );

                    // This loop is trying to extract the correct patient study id for each instance
                    for ( int count2 = 0; count2 < patientStudies.size(); count2++ ) {
                        patientStudy = patientStudies.get( count2 );
                        if ( theScore.getStudyCode().equals( printStudy.getStudyCode() ) && printStudy.getStudyCode().equals( patientStudy.getStudyCode() ) && patientStudy.getSurveyRegId().equals( registration.getSurveyRegId() ) ) {
                            pStudyChanged = patientStudy.getDtChanged();
                            pStudyCreated = patientStudy.getDtCreated();
                        }
                    }

                    // if the study codes match, then we have the desired score
                    if ( scores.get( s ).getStudyCode().intValue() == printStudy.getStudyCode().intValue()) {
                        chartScore = scores.get( s );
                    }
                }
            }
            Long score = null;
            String range = "";
            String categoryLabel = "";
            HashMap< String, Long > painIntensity = null;

            if ( chartScore != null ) {
                // for the 1001 studyCode, which is painIntensity, we want the specific response(s) from the user
                if ( printStudy.getStudyCode() == 1001 ) {
                    painIntensity = painIntensityAnswers( provider, patientStudies, printStudy, thePatient );
                    if ( painIntensity != null ) {
                        score = null;
                        Set<String> keys = painIntensity.keySet();
                        String result = "";
                        for(String key : keys) {
                            result += " [" + key + "]=" + painIntensity.get( key );
                        }
                        logger.debug( "The result from the painIntensityAnswers call was: " + result );
                    } else {
                        logger.debug( "The result from the painIntensityAnswers call was null." );
                        score = chartScore.getScore().longValue();
                    }

                } else {
                    // this is every other study code that isn't Pain Intensity
                    score = chartScore.getScore().longValue();
                }

                // This makes use of the HL7Message.<Study Description> configurations in the context.xml
                range = serverUtils.getParam( "HL7Message." + chartScore.getStudyDescription() );
                categoryLabel = chartScore.getCategoryLabel();

                if ( printStudies.get( count ).getInvert() ) {
                    logger.debug( "printStudy " + printStudies.get( count ).getTitle() + " is inverted " );
                    score = EpicExportResult.invertScore( score );
                    logger.debug( "inverted score " + score );
                }
            }

            // setting the object attributes
            String componentName = printStudy.getTitle();
            Integer studyCode = chartScore.getStudyCode();
            String componentId;
            Date observationTime;

            if ( studyCode != null ) {
                componentId = serverUtils.getParam( "StudyCode" + studyCode.toString() );
            } else {
                componentId = null;
            }

            if ( thePatientResult != null ) {
                observationTime = thePatientResult.getDtCreated(); // this is using the date the patient result was created, at the end of the survey
            } else {
                observationTime = new Date();
            }

            // These dates are included in case they are wanted for future usage
            Date patientStudyChanged = pStudyChanged;
            Date patientStudyCreated = pStudyCreated;

            String scoreRange = range;
            String category = categoryLabel;
            String idType = serverUtils.getParam( "HL7Message.IdType" );
            String obxCodingSystem = serverUtils.getParam( "HL7Message.codingSystem" );
            String observationResultStatus = serverUtils.getParam( "HL7Message.observationResultStatus" );
            String valueType = serverUtils.getParam( "HL7Message.valueType" );

            // determine if the given study is the pain intensity
            if ( printStudy.getStudyCode() == 1001 && ! painIntensity.isEmpty()) {
                Set<String> keys = painIntensity.keySet();
                for(String key : keys) {
                    String painIntensityComponentName = "Pain Intensity " + key + " - PROMIS" ;
                    EpicExportResult exportResult = new EpicExportResult(
                            painIntensity.get( key ), componentId, observationResultStatus, observationTime, painIntensityComponentName,
                            obxCodingSystem,
                            valueType,
                            scoreRange, category, idType
                    );
                    results.add( exportResult );
                }
            }else{

                EpicExportResult exportResult = new EpicExportResult(
                        score, componentId, observationResultStatus, observationTime, componentName, obxCodingSystem,
                        valueType,
                        scoreRange, category, idType
                );
                results.add( exportResult );
            }
        }
    }

    /**
     * painIntensityAnswers
     *
     * This function tries to retrieve the patient's painIntensity Answers
     *
     * @param provider the Score Provider object
     * @param patientStudies all of the Patient Studies
     * @param printStudy this current printStudy
     * @param thePatient the Patient who took the survey
     * @return currently, this only returns the painIntensityAverage as a String
     *
     * @author Frank Correale
     */
    private HashMap<String, Long> painIntensityAnswers( ScoreProvider provider, ArrayList<PatientStudyExtendedData> patientStudies, PrintStudy printStudy, Patient thePatient ) {
        // @TODO currently, this function relies on the order of the questions to be Worst, Average, Now, Least-- it was deemed at the time that this would be unchanging
        // get the questions for this study
        ArrayList<SurveyQuestionIntf> theSurvey = provider.getSurvey( patientStudies, printStudy, thePatient, true );

        // the attribute itemresponse for the question is the answer given to that specific question
        String painIntensityWorst = theSurvey.get( 0 ).getAttribute( "itemresponse" ); // Worst
        String painIntensityAverage = theSurvey.get( 1 ).getAttribute( "itemresponse" ); // Average
        String painIntensityNow = theSurvey.get( 2 ).getAttribute( "itemresponse" ); // Now
        String painIntensityLeast = theSurvey.get( 3 ).getAttribute( "itemresponse" ); // Least
        logger.debug( "The answer for painIntensityWorst: " + painIntensityWorst );
        logger.debug( "The answer for painIntensityAverage " + painIntensityAverage );
        logger.debug( "The answer for painIntensityNow " + painIntensityNow );
        logger.debug( "The answer for painIntensityLeast " + painIntensityLeast );
        HashMap<String, Long> response = new HashMap<>(  );
        response.put( "Worst", Long.parseLong( painIntensityWorst ) );
        response.put( "Average", Long.parseLong( painIntensityAverage ) );
        response.put( "Now", Long.parseLong( painIntensityNow ) );
        response.put( "Least", Long.parseLong( painIntensityLeast ) );

        return response;
    }

    /*
     *
     * Getter Methods
     *
     */

    /**
     * getSender()
     *
     * @return returns sender
     * @author Frank Correale
     */
    public String getSender() {
        return sender;
    }

    /**
     * getReceiver()
     *
     * @return returns receiver
     * @author Frank Correale
     */
    public String getReceiver() {
        return receiver;
    }

    /**
     * getSpecimenActionCode()
     *
     * @return returns specimenActionCode
     * @author Frank Correale
     */
    public String getSpecimenActionCode() {
        return specimenActionCode;
    }

    /**
     * getExternalTestName()
     *
     * @return returns externalTestName
     * @author Frank Correale
     */
    public String getExternalTestName() {
        return externalTestName;
    }

    /**
     * getPatientFirstName()
     *
     * @return return patientFirstName
     * @author Frank Correale
     */
    public String getPatientFirstName() {
        return patientFirstName;
    }

    /**
     * getPatientLastName()
     *
     * @return patientLastName
     * @author Frank Correale
     */
    public String getPatientLastName() {
        return patientLastName;
    }

    /**
     * getPatientAccountNumber()
     *
     * @return patientAccountNumber
     * @author Frank Correale
     */
    public String getPatientAccountNumber() {
        return patientAccountNumber;
    }

    /**
     * getGender()
     *
     * @return genderChar (the first letter of the gender)
     * @author Frank Correale
     */
    public String getGender() {
        String genderChar;
        genderChar = gender.substring( 0, 1 );
        return genderChar;
    }

    /**
     * getPatientId()
     *
     * @return patientId
     * @author Frank Correale
     */
    public String getPatientId() {
        return patientId;
    }

    /**
     * getProviderEid()
     *
     * @return providerEid
     * @author Frank Correale
     */
    public String getProviderEid() {
        return providerEid;
    }

    /**
     * getEpicExportResults()
     *
     * @return results
     * @author Frank Correale
     */
    public ArrayList<EpicExportResult> getEpicExportResults() {
        return results;
    }

    /**
     * getResultsStatus()
     *
     * @return resultsStatus
     * @author Frank Correale
     */
    public String getResultsStatus() {
        return resultsStatus;
    }

    /**
     * getAgencyIdentifier()
     *
     * @return agencyIdentifier
     * @author Frank Correale
     */
    public String getAgencyIdentifier() {
        return agencyIdentifier;
    }

    /**
     * getObrCodingSystem()
     *
     * @return obrCodingSystem
     * @author Frank Correale
     */
    public String getObrCodingSystem() {
        return obrCodingSystem;
    }

    /**
     * getAssigningAuthority()
     *
     * @return assigningAuthority
     * @author Frank Correale
     */
    public String getAssigningAuthority() {
        return assigningAuthority;
    }

    /**
     * getDateOfBirth()
     *
     * @return dob formatted
     * @author Frank Correale
     */
    public String getDateOfBirth() {
        SimpleDateFormat format = new SimpleDateFormat( "yyyyMMdd" );
        String dob;
        dob = format.format( dateOfBirth );
        return dob;
    }

    /**
     * getTimeOfObservation()
     *
     * @return observationTime formatted
     * @author Frank Correale
     */
    public String getTimeOfObservation() {
        SimpleDateFormat format = new SimpleDateFormat( "yyyyMMddHHmm" );
        String observationTime;
        observationTime = format.format( timeOfObservation );
        return observationTime;
    }

    /**
     * getIdentifierTypeCode()
     *
     * @return identifierTypeCode
     * @author Frank Correale
     */
    public String getIdentifierTypeCode() {
        return identifierTypeCode;
    }

    /**
     * getAncillaryApplication()
     *
     * @return ancillaryApplication
     * @author Frank Correale
     */
    public String getAncillaryApplication() {
        return ancillaryApplication;
    }

    /**
     * getSurveyRegistration()
     *
     * @return surveyRegistration
     * @author kpharvey
     */
    public SurveyRegistration getSurveyRegistration() { return surveyRegistration; }

    /**
     * calculatePercentile
     * local copy from RegistryShortFormScoreProvider
     *
     * @param score raw score of a questionnaire
     * @return percentile of raw score in relation to max score of questionnaire
     *
     * @author kpharvey
     */
    public Long calculatePercentile( Double score ) {
        NormalDistribution norm = new NormalDistribution(50, 10);
        Double percentile = norm.cumulativeProbability(score) * 100;
        return new Long(Math.round(percentile));
    }

    /**
     * getEmpid
     *
     * @return String empID
     * @author kpharvey
     */
    public String getEmpid() {
        return empid;
    }
}
