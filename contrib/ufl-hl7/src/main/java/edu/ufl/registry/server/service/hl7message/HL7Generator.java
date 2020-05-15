package edu.ufl.registry.server.service.hl7message;

import ca.uhn.hl7v2.DefaultHapiContext;
import ca.uhn.hl7v2.HapiContext;
import ca.uhn.hl7v2.model.Varies;
import ca.uhn.hl7v2.model.v26.datatype.*;
import ca.uhn.hl7v2.model.v26.group.ORU_R01_ORDER_OBSERVATION;
import ca.uhn.hl7v2.model.v26.message.ORU_R01;
import ca.uhn.hl7v2.model.v26.segment.*;
import ca.uhn.hl7v2.parser.DefaultModelClassFactory;
import ca.uhn.hl7v2.parser.Parser;
import org.apache.log4j.Logger;

import java.text.SimpleDateFormat;
import java.util.ArrayList;
import java.util.Date;


public class HL7Generator {

    private static Logger logger = Logger.getLogger( HL7Generator.class );

    /**
     * generateHL7
     *
     * Take patient supplied information from a taken survey
     * and translate the data into an HL7 readable message
     *
     * @param surveyData the resulting data from a survey taken
     *
     * @return String - an HL7 message
     *
     * @throws Exception
     *
     * @author kpharvey
     */
    public String generateHL7( EpicExportData surveyData ) throws Exception {

        logger.debug("Generating HL7 message");

        Date today = new Date();
        SimpleDateFormat format = new SimpleDateFormat( "yyyyMMddHHmm" );

        String dateToday = format.format( today );

        //create ORU message type
        ORU_R01 oru = new ORU_R01();

        //auto-populate required segments
        oru.initQuickstart( "ORU", "R01", "P" );

        String encodedMessage;
        String type = "standard";

        String msh = generateMSH( surveyData, oru, dateToday, type );
        String pid = generatePID( surveyData, oru, type );
        String obr = generateOBR( surveyData, oru, dateToday );
        String obx = generateOBX( surveyData, oru );

        //include PID information in message
        encodedMessage = msh + "\n" + pid;

        //include OBR information in message
        encodedMessage += "\n" + obr;

        //include OBX information in message
        encodedMessage += obx;

        logger.debug( "HL7 Message Generated: " + encodedMessage );

        return encodedMessage;
    }

    /**
     * generateHL7PDF
     *
     * Take patient supplied information from a taken survey
     * and translate data to an HL7 message that sends an
     * encoded PDF file
     *
     * @param surveyData the resulting data from a survey taken
     * @param encodedResults encoded PDF file
     *
     * @return String - an HL7 message
     *
     * @throws Exception
     *
     * @author kpharvey
     */
    public String generateHL7PDF( EpicExportData surveyData, String encodedResults ) throws Exception {
        Date today = new Date();
        SimpleDateFormat format = new SimpleDateFormat( "yyyyMMddHHmm" );

        String dateToday = format.format( today );

        //create ORU message type
        ORU_R01 oru = new ORU_R01();

        //auto-populate required segments
        oru.initQuickstart( "ORU", "R01", "P" );

        String encodedMessage;
        String type = "pdf";

        String msh = generateMSH( surveyData, oru, dateToday, type );
        String pid = generatePID( surveyData, oru, type );
        String obr = generateOBR( surveyData, oru, dateToday );
        String pv1 = generatePV1( surveyData, oru );
        String txa = generateTXA( oru, dateToday );
        String obx = generateOBXPdf( oru, encodedResults );

        //include PID information in message
        encodedMessage = msh + "\n" + pid;

        //include OBR information in message
        encodedMessage += "\n" + obr;

        //include PV1 information in message
        encodedMessage += "\n" + pv1;

        //include TXA information in message
        encodedMessage += "\n" + txa;

        //include OBX information in message
        encodedMessage += "\n" + obx;

        logger.debug( "HL7PDF Message Generated: " + encodedMessage );

        return encodedMessage;
    }

    /**
     * generateMSH
     *
     * @param surveyData the resulting data from a survey taken
     * @param oru oru segment
     * @param dateToday date
     *
     * @return String - msh segment
     *
     * @throws Exception
     *
     * @author kpharvey
     */
    private String generateMSH( EpicExportData surveyData, ORU_R01 oru, String dateToday, String type ) throws Exception {
        //populate MSH segments
        MSH mshSegment = oru.getMSH();

        mshSegment.getSendingApplication().getNamespaceID().setValue( surveyData.getAgencyIdentifier() ); //MSH3
        mshSegment.getSendingFacility().getNamespaceID().setValue( surveyData.getSender() ); //MSH4

        // messagePDFDistinction is here to make sure the normal message and the pdf don't have exactly the same ID
        String messagePDFDistinction = "0";
        if ( type.equalsIgnoreCase( "pdf" ) ) {
            messagePDFDistinction = "1";
            mshSegment.getReceivingApplication().getNamespaceID().setValue( "ONBASE" ); //MSH5
            mshSegment.getVersionID().getVersionID().setValue( "2.6" ); //MSH12 - set message version to 2.6
        } else {
            messagePDFDistinction = "0";
            mshSegment.getReceivingApplication().getNamespaceID().setValue( surveyData.getReceiver() ); //MSH5
            mshSegment.getVersionID().getVersionID().setValue( "2.1" ); //MSH12 - set message version to 2.1
        }

        mshSegment.getReceivingFacility().getNamespaceID().setValue( surveyData.getSender() ); //MSH6
        mshSegment.getDateTimeOfMessage().setValue( dateToday ); //MSH7
        mshSegment.getMessageType().getMessageStructure().setValue( null ); //remove ORU_R01 from third component of MSH9
        mshSegment.getMessageControlID().setValue( dateToday + surveyData.getPatientAccountNumber() + messagePDFDistinction ); //MSH10 - dateToday + CSN number + pdf 1 or 0

        //encode the message
        HapiContext context = new DefaultHapiContext();
        Parser parser = context.getPipeParser();
        String encodedMessage = parser.encode( oru );

        return encodedMessage;
    }

    /**
     * generatePID
     *
     * @param surveyData the resulting data from a survey taken
     * @param oru oru segment
     *
     * @return String - pid segment
     *
     * @throws Exception
     *
     * @author kpharvey
     */
    private String generatePID( EpicExportData surveyData, ORU_R01 oru, String type ) throws Exception {
        //populate the PID segment
        PID pid = new PID( oru, new DefaultModelClassFactory() );

        pid.getSetIDPID().setValue( "1" );

        if ( type.equalsIgnoreCase( "pdf" ) ) {
            pid.getPatientIdentifierList( 0 ).getIDNumber().setValue( surveyData.getEmpid() ); //PID3.1
        } else { //standard HL7 message
            pid.getPatientIdentifierList( 0 ).getIDNumber().setValue( surveyData.getPatientId() ); //PID3.1
        }

        pid.getPatientIdentifierList(0).getIdentifierTypeCode().setValue( determineAssigningAuthority( type, surveyData.getAssigningAuthority() ) ); //PID3.5
        pid.getPatientName( 0 ).getFamilyName().getSurname().setValue( surveyData.getPatientLastName() ); //PID5.1
        pid.getPatientName( 0 ).getGivenName().setValue( surveyData.getPatientFirstName() );//PID5.2
        pid.getDateTimeOfBirth().setValue( surveyData.getDateOfBirth() );//PID7
        pid.getAdministrativeSex().setValue(surveyData.getGender()); //PID8
        pid.getPatientAccountNumber().getIDNumber().setValue( surveyData.getPatientAccountNumber() ); //PID18 - currently the same as patient ID

        String encodedMessage = pid.encode();

        return encodedMessage;
    }

    /**
     * generateOBR
     *
     * @param surveyData the resulting data from a survey taken
     * @param oru oru segment
     * @param dateToday date
     *
     * @return String - obr segment
     *
     * @throws Exception
     *
     * @author kpharvey
     */
    private String generateOBR( EpicExportData surveyData, ORU_R01 oru, String dateToday ) throws Exception {
        //populate OBR segment
        ORU_R01_ORDER_OBSERVATION orderObservation = oru.getPATIENT_RESULT().getORDER_OBSERVATION();
        OBR obr = orderObservation.getOBR();

        obr.getSetIDOBR().setValue( "1" );
        obr.getFillerOrderNumber().getEntityIdentifier().setValue( surveyData.getAncillaryApplication() );
        obr.getUniversalServiceIdentifier().getIdentifier().setValue( surveyData.getAgencyIdentifier() );
        obr.getUniversalServiceIdentifier().getText().setValue( surveyData.getExternalTestName() );
        obr.getUniversalServiceIdentifier().getNameOfCodingSystem().setValue( surveyData.getObrCodingSystem() );
        obr.getObservationDateTime().setValue( surveyData.getTimeOfObservation() );
        obr.getSpecimenActionCode().setValue( surveyData.getSpecimenActionCode() );
        obr.getSpecimenReceivedDateTime().setValue( dateToday );
        obr.getOrderingProvider( 0 ).getIDNumber().setValue( surveyData.getProviderEid() ); //this should be the Provider_EID from the import
        obr.getOrderingProvider( 0 ).getIdentifierTypeCode().setValue( surveyData.getIdentifierTypeCode() );
        obr.getResultsRptStatusChngDateTime().setValue( dateToday );
        obr.getResultStatus().setValue( surveyData.getResultsStatus() );

        String encodedMessage = obr.encode();

        return encodedMessage;
    }

    /**
     * generateOBX
     *
     * @param surveyData the resulting data from a survey taken
     * @param oru oru segment
     *
     * @return String - the obx segment
     *
     * @throws Exception
     *
     * @author kpharvey
     */
    private String generateOBX( EpicExportData surveyData, ORU_R01 oru ) throws Exception {
        ORU_R01_ORDER_OBSERVATION orderObservation = oru.getPATIENT_RESULT().getORDER_OBSERVATION();

        int count = 0;
        int obxID = 1;
        OBX obx;
        String encodedMessage = "";

        ArrayList<EpicExportResult> resultList = surveyData.getEpicExportResults();

        for ( int i = 0; i < 2; i++ ) {
            //loop through surveys taken and populate HL7 with information
            for ( EpicExportResult result : resultList ) {
                //populate the OBX segment
                obx = orderObservation.getOBSERVATION( count ).getOBX();

            //only include OBX data for configured surveys
            if ( result.getComponentId() != null ) {
                obx.getSetIDOBX().setValue( String.valueOf( obxID ) );
                String[] componentIds = result.getComponentId().split( "," ); //in the case of Pain Intensity questionnaires

                    obx.getObservationIdentifier().getText().setValue( result.getComponentName() );
                    obx.getObservationIdentifier().getNameOfCodingSystem().setValue( result.getIdType() );

                    int score = Integer.parseInt( result.getScore() );

                    String range = result.getScoreRange();
                    ArrayList<String> rangeWithCategory;

                    if ( range.equals( "No Range" ) ) {
                        rangeWithCategory = new ArrayList<>();
                        rangeWithCategory.add( range );
                        rangeWithCategory.add( "" ); //no category possible for no range
                    } else {
                        String[] rangeSplit = range.split( "," );
                        String[] labels = {"None/Minimal", "Mild", "Moderate", "Severe", "High"};
                        rangeWithCategory = rangeAndCategory( score, rangeSplit, labels );
                    }

                    obx.getObservationResultStatus().setValue( result.getResultsStatus() );
                    obx.getDateTimeOfTheObservation().setValue( surveyData.getTimeOfObservation() );

                    //set the numeric score
                    NM obxValueType = new NM( oru ); //create an NM object because OBX-2 will represent a numeric value

                    //populate OBX-5 with score, check which type is being requested and set OBX-5 accordingly
                    if ( i == 0 ) { // Handling the base scores.
                        //logic for different pain intensity questionnaires
                        switch ( result.getComponentName() ) {
                            case "Pain Intensity Least - PROMIS":
                                obx.getObservationIdentifier().getIdentifier().setValue( componentIds[3] );
                                break;
                            case "Pain Intensity Now - PROMIS":
                                obx.getObservationIdentifier().getIdentifier().setValue( componentIds[1] );
                                break;
                            case "Pain Intensity Average - PROMIS":
                                obx.getObservationIdentifier().getIdentifier().setValue( componentIds[0] );
                                break;
                            case "Pain Intensity Worst - PROMIS":
                                obx.getObservationIdentifier().getIdentifier().setValue( componentIds[2] );
                                break;
                            case "PROMIS Pain Interference Bank":
                            case "PROMIS Fatigue":
                            case "PROMIS Sleep Disturbance":
                            case "PROMIS Anxiety":
                            case "PROMIS Depression":
                            case "PROMIS Pain Behavior":
                            case "PROMIS Physical Function":
                            case "PROMIS Sleep-Related Impairment":
                            case "PROMIS Anger":
                                obx.getObservationIdentifier().getIdentifier().setValue( componentIds[0] );
                                break;
                            default:
                                obx.getObservationIdentifier().getIdentifier().setValue( result.getComponentId() );
                                break;
                        }

                        obxValueType.setValue( result.getScore() ); //OBX5.2
                        Varies observationValue = obx.getObservationValue( 0 );
                        observationValue.setData( obxValueType );
                        obx.getReferencesRange().setValue( rangeWithCategory.get( 1 ) );
                    } else if ( i == 1 ) { // Handling the percentile scores on specific Questionnaires.
                        switch ( result.getComponentName() ) {
                            // These Questionnaires do not process percentiles.
                            case "Pain Intensity Least - PROMIS":
                            case "Pain Intensity Now - PROMIS":
                            case "Pain Intensity Average - PROMIS":
                            case "Pain Intensity Worst - PROMIS":
                            case "Pain Catastrophizing Scale":
                            case "Opioid Risk":
                                continue;
                            // These Questionnaires DO process percentiles.
                            case "PROMIS Pain Interference Bank":
                            case "PROMIS Fatigue":
                            case "PROMIS Sleep Disturbance":
                            case "PROMIS Anxiety":
                            case "PROMIS Depression":
                            case "PROMIS Pain Behavior":
                            case "PROMIS Physical Function":
                            case "PROMIS Sleep-Related Impairment":
                            case "PROMIS Anger":
                                obx.getObservationIdentifier().getIdentifier().setValue( componentIds[1] );
                            default:
                                //get the score
                                double scoreToConvert = Double.parseDouble( result.getScore() );
                                //call the percentile function
                                Long percentile = surveyData.calculatePercentile( scoreToConvert );
                                obxValueType.setValue( percentile.toString() );
                                Varies observationValue = obx.getObservationValue( 0 );
                                observationValue.setData( obxValueType );
                                break;
                        }
                    }

                    obx.getValueType().setValue( result.getValueType() );

                    encodedMessage += "\n" + obx.encode();

                    obxID++;
                }

                count++;
            }
        }

        return encodedMessage;
    }

    /**
     * generateOBXPdf
     *
     * @param oru oru segment
     * @param encodedQuestionnaireResults encoded file byte stream
     *
     * @return String - obx segment
     *
     * @throws Exception
     *
     * @author kpharvey
     */
    private String generateOBXPdf( ORU_R01 oru, String encodedQuestionnaireResults ) throws Exception {
        ORU_R01_ORDER_OBSERVATION orderObservation = oru.getPATIENT_RESULT().getORDER_OBSERVATION();
        OBX obx;

        //populate the OBX segment
        obx = orderObservation.getOBSERVATION( 0 ).getOBX();
        obx.getSetIDOBX().setValue( String.valueOf( 1 ) ); //OBX1
        obx.getValueType().setValue( "ED" ); //OBX2
//        obx.getObservationIdentifier().getIdentifier().setValue( "Some value TBD" ); //OBX3.1 - left as placeholder in case we get data to insert b/c API says it's a required field
        obx.getObservationIdentifier().getText().setValue( "BASE64ENCODE" ); //OBX3.2
//        obx.getObservationSubID().setValue( String.valueOf( 1 ) ); //OBX4 - left as placeholder in case we get data to insert b/c API says it's a required field

        //set up OBX5 for being populated
        ED obxValueType = new ED( oru ); //type ED needed to signal encapsulated data in reference to the pdf file
        obxValueType.getSourceApplication().getNamespaceID().setValue( "PDF" ); //OBX5.1
        obxValueType.getData().setValue( encodedQuestionnaireResults ); //OBX5.5

        //populate OBX5
        Varies value = obx.getObservationValue( 0 );
        value.setData( obxValueType );

        String encodedMessage = obx.encode();

        return encodedMessage;
    }

    /**
     * generatePV1
     *
     * @param surveyData the resulting data from a survey taken
     * @param oru oru segment
     *
     * @return String - pv1 segment
     *
     * @throws Exception
     *
     * @author kpharvey
     */
    private String generatePV1( EpicExportData surveyData, ORU_R01 oru ) throws Exception {
        PV1 pv1 = new PV1( oru, new DefaultModelClassFactory() );

//        pv1.getPatientClass().setValue( "Patient Class Needed" ); //PV12 - left as placeholder in case we get data to insert b/c API says it's a required field
        pv1.getPatientType().setValue( surveyData.getPatientAccountNumber() ); //PV118

        String encodedMessage = pv1.encode();

        return encodedMessage;
    }

    /**
     * generateTXA
     *
     * @param oru oru segment
     * @param dateToday date
     *
     * @return String - txa segment
     *
     * @throws Exception
     *
     * @author kpharvey
     */
    private String generateTXA( ORU_R01 oru, String dateToday ) throws Exception {
        TXA txa = new TXA( oru, new DefaultModelClassFactory() );

        String documentTitle = "CHOIR-PAIN";
        String txaId = String.valueOf( 1 );
        String txaDocType = "CHFM Clinic";

        txa.getSetIDTXA().setValue( txaId ); //TXA1
        txa.getDocumentType().setValue( txaDocType ); //TXA2
        txa.getDocumentContentPresentation().setValue( documentTitle ); //TXA3
        txa.getOriginationDateTime().setValue( dateToday ); //TXA6
//        txa.getUniqueDocumentNumber().getEntityIdentifier().setValue( "Unique document number" ); //TXA12 - left as placeholder in case we get data to insert b/c API says it's a required field
//        txa.getDocumentCompletionStatus().setValue( "Document Completion Status" ); //TXA17 - left as placeholder in case we get data to insert b/c API says it's a required field

        String encodedMessage = txa.encode();

        return encodedMessage;
    }

    /**
     * rangeAndCategory
     *
     * Check a score and determine what range it falls in
     * as well as the category for said range
     *
     * @param score the questionnaire score
     * @param scoreRange all possible score ranges
     * @param labels all possible labels
     *
     * @return ArrayList - The score range the score falls in and the associated category for that range
     *
     * @author kpharvey
     */
    public static ArrayList<String> rangeAndCategory( int score, String[] scoreRange, String[] labels ) {

        //need two indexes for the range the category that range belongs to
        ArrayList<String> rangeAndCategory = new ArrayList<>();

        if ( score == 0 ) {
            rangeAndCategory.add( scoreRange[0] );
            rangeAndCategory.add( labels[0] );
            return rangeAndCategory;
        }

        if ( scoreRange.length < 2 ) {
            String[] range = scoreRange[0].split( ">" );

            if ( score > Double.parseDouble( range[1] ) ) {
                rangeAndCategory.add( scoreRange[0] );
                rangeAndCategory.add( labels[4] );
            } else {
                rangeAndCategory.add( "" );
                rangeAndCategory.add( "" );
            }

            return rangeAndCategory;
        }

        String[] splitMildRange = scoreRange[1].split( "-" );
        String[] splitModerateRange = scoreRange[2].split( "-" );

        double mildRangeLow = Double.parseDouble( splitMildRange[0] );
        double mildRangeHi = Double.parseDouble( splitMildRange[1] );

        double moderateRangeLow = Double.parseDouble( splitModerateRange[0] );
        double moderateRangeHi = Double.parseDouble( splitModerateRange[1] );

        if ( score >= mildRangeLow && score <= mildRangeHi ) {
            rangeAndCategory.add( scoreRange[1] );
            rangeAndCategory.add( labels[1] );
        } else if ( score >= moderateRangeLow && score <= moderateRangeHi ) {
            rangeAndCategory.add( scoreRange[2] );
            rangeAndCategory.add( labels[2] );
        } else if ( score > moderateRangeHi ) {
            rangeAndCategory.add( scoreRange[3] );
            rangeAndCategory.add( labels[3] );
        } else {
            rangeAndCategory.add( scoreRange[0] );
            rangeAndCategory.add( labels[0] );
        }

        return rangeAndCategory;
    }

    /**
     * determineAssigningAuthority
     *
     * Determine which assigning authority to use based on the
     * given type of HL7 message
     *
     * @param type the style of HL7 message to generate
     * @param assigningAuthority the assigningAuthority for patient information
     * @return String
     *
     * @author kpharvey
     */
    private String determineAssigningAuthority( String type, String assigningAuthority ) {
        String[] typesOfAuthorities = assigningAuthority.split( "," );
        String authorityAssigned;

        if ( type.equalsIgnoreCase( "pdf" ) ) {
            authorityAssigned = typesOfAuthorities[1];
        } else { //standard HL7 message
            authorityAssigned = typesOfAuthorities[0];
        }

        return authorityAssigned;
    }
}
