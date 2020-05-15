package edu.stanford.registry.server.hl7;

import edu.stanford.registry.server.SiteInfo;
import edu.stanford.registry.server.hl7.Hl7HohServlet.AppointmentStatus;
import edu.stanford.registry.server.hl7.provider.Provider;
import edu.stanford.registry.server.hl7.provider.ProviderFinder;
import edu.stanford.registry.server.imports.data.Hl7AppointmentIntf;
import edu.stanford.registry.server.xchg.ImportException;
import edu.stanford.registry.shared.Constants;
import edu.stanford.registry.shared.Patient;

import java.text.SimpleDateFormat;
import java.util.Date;
import java.util.HashMap;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import com.github.susom.database.Database;

import ca.uhn.hl7v2.HL7Exception;
import ca.uhn.hl7v2.hoh.api.IMessageHandler;
import ca.uhn.hl7v2.hoh.api.IReceivable;
import ca.uhn.hl7v2.hoh.api.IResponseSendable;
import ca.uhn.hl7v2.hoh.api.MessageProcessingException;
import ca.uhn.hl7v2.hoh.raw.api.RawSendable;
import ca.uhn.hl7v2.model.Message;
import ca.uhn.hl7v2.model.v23.message.ACK;
import ca.uhn.hl7v2.parser.PipeParser;
import ca.uhn.hl7v2.util.Terser;

public class IncomingMessageHandler implements IMessageHandler<String> {
  private static final Logger logger =  LoggerFactory.getLogger(IncomingMessageHandler.class);
  private static final SimpleDateFormat msgDateFormatter =  new SimpleDateFormat("yyyyMMddHHmmss");
  private final PipeParser pipeParser = PipeParser.getInstanceWithNoValidation();
  private final Database database;
  private final Hl7CustomizerFactoryIntf hl7CustomizerFactory;
  private final HashMap<String, SiteInfo> siteNames;


  public IncomingMessageHandler(Database database, Hl7CustomizerFactoryIntf hl7CustomizerFactory, HashMap<String, SiteInfo> siteNames) {
    this.database = database;
    this.hl7CustomizerFactory = hl7CustomizerFactory;
    this.siteNames = siteNames;
  }

  /*
   * This method is fired every time a message is received. The return value
   * contains the HL7 response value
   */
  public IResponseSendable<String> messageReceived(IReceivable<String> theReceived)
      throws MessageProcessingException {
    String incomingRawMsg = theReceived.getMessage();
    Hl7CustomizerIntf hl7CustomizerIntf;
    try {
      Message hl7Message = pipeParser.parse(incomingRawMsg);
      Terser terser = new Terser(hl7Message);
      String ackCode = "AR";
      TerserLocationIntf terserLocations = null;
      Message hl7returnMessage = null;
      try {
        hl7CustomizerIntf = handleScheduleMessages(terser);
        if (hl7CustomizerIntf != null) {
          terserLocations = hl7CustomizerIntf.getTerserLocations();
          hl7returnMessage = hl7CustomizerIntf.getScheduleAckMessage(terserLocations, terser);
        } else {
          hl7returnMessage = getFailedAckMessage(terser.get(hl7CustomizerFactory.getDepartmentTerserLocation()));
        }
      } catch (HL7Exception hl7e) {
        logger.error("Error processing hl7 scheduling message", hl7e);
        ackCode = "AE";
      }
      if (hl7returnMessage == null) {
        throw new MessageProcessingException("No return message from customizer!");
      }
      if (hl7returnMessage instanceof ACK) {
        ACK ackResponseMessage = (ACK) hl7returnMessage;
        ackResponseMessage.getMSA().getAcknowledgementCode().setValue(ackCode);
      }
      logger.trace("return message is {}", hl7returnMessage.encode());
      return new RawSendable(hl7returnMessage.encode());
    } catch (HL7Exception e) {
      logger.error("Error parsing message", e);
      throw new MessageProcessingException("Error processing hl7");
    }
  }

  /**
   * Process the message
   *
   * @param messageTerser for the incoming message
   * @throws HL7Exception thrown when there is a parsing problem
   */
  private Hl7CustomizerIntf handleScheduleMessages(Terser messageTerser) throws HL7Exception {
    //TerserLocationIntf terserLocations = null;
    Hl7CustomizerIntf hl7Customizer = null;
    try {

      // RGS segment has clinic name and id

      String departmentName = messageTerser.get(hl7CustomizerFactory.getDepartmentTerserLocation());

      // Check the clinic name to ee if this is a CHOIR appointment
      SiteInfo siteInfo = siteNames.get(departmentName.toUpperCase());

      if (siteInfo == null) {
        return null;
      }
      hl7Customizer = hl7CustomizerFactory.create(siteInfo);
      TerserLocationIntf terserLocations = hl7Customizer.getTerserLocations();
      String departmentId = messageTerser.get(terserLocations.getLocation(TerserLocations.DEPARTMENT_ID));

      // MSH segment
      String messageAction = messageTerser.get(terserLocations.getLocation(TerserLocations.APPOINTMENT_ACTION));
      String messageType = messageTerser.get(terserLocations.getLocation(TerserLocations.APPOINTMENT_TYPE));

      // PID Patient Identification segment
      String mrn = messageTerser.get(terserLocations.getLocation(TerserLocations.PATIENT_MRN));
      String lastName = messageTerser.get(terserLocations.getLocation(TerserLocations.PATIENT_LAST_NAME));
      String firstName = messageTerser.get(terserLocations.getLocation(TerserLocations.PATIENT_FIRST_NAME));
      String dobString = messageTerser.get(terserLocations.getLocation(TerserLocations.PATIENT_BIRTH_DATE));
      String genderCode = messageTerser.get(terserLocations.getLocation(TerserLocations.PATIENT_GENDER));
      String raceCode = messageTerser.get(terserLocations.getLocation(TerserLocations.PATIENT_RACE));
      String ethnicityCode = messageTerser.get(terserLocations.getLocation(TerserLocations.PATIENT_ETHNICITY));
      String emailAddress = messageTerser.get(terserLocations.getLocation(TerserLocations.PATIENT_EMAIL));

      // SCH segment
      String apptTm = messageTerser.get(terserLocations.getLocation(TerserLocations.SCHEDULE_APPOINTMENT_START_DATETIME));
      String visitEid = messageTerser.get(terserLocations.getLocation(TerserLocations.VISIT_ID));
      String visitDesc = messageTerser.get(terserLocations.getLocation(TerserLocations.VISIT_DESCRIPTION));
      String apptStatusString = messageTerser.get(terserLocations.getLocation(TerserLocations.SCHEDULE_APPOINTMENT_STATUS));

      // PV1 segment
      String encounterId = messageTerser.get(terserLocations.getLocation(TerserLocations.ENCOUNTER_ID));
      String providerEid = null;
      try {
        Provider provider = ProviderFinder.getProviderId(terserLocations, messageTerser);
        if (provider != null) {
          providerEid = provider.getProviderEId();
        }
      } catch (Exception ex) {
        logger.warn("Error {} occured trying to locate provider id", ex.getLocalizedMessage());
      }
      // messageTerser.get(terserLocations.getLocation(TerserLocations.ATTENDING_PROVIDER_ID)); //("/PV1(0)-7(1)-1");

      // AIP segment
      String race = Race.getRaceDescription(raceCode);
      String ethnicity = Ethnicity.getEthnicityDescription(ethnicityCode);
      String gender = Gender.getGenderDescription(genderCode);
      Hl7AppointmentIntf hl7AppointmentIntf = hl7Customizer.getHl7Appointment(database);
      String visitType = hl7AppointmentIntf.getVisitType(visitEid, visitDesc);

      logger.trace("HL7Message type {} MRN {} last name {} first name {} dob {} gender {}", messageType, mrn, lastName, firstName, dobString, gender);
      logger.trace("action {} encounterId {} appt dt {} appt eid {} desc {} = type {} ", messageAction, encounterId, apptTm, visitEid, visitDesc, visitType);
      logger.trace("emailAddress{} providerId {}", emailAddress, providerEid);
      logger.trace("race code {} = {} ethnicity code {} = {}", raceCode, race, ethnicityCode, ethnicity);
      logger.trace("clinic name {} id {} status {}", departmentName, departmentId, apptStatusString);

      try {
        if (hl7AppointmentIntf != null) {

          Patient patient = hl7AppointmentIntf.processPatient(mrn, firstName, lastName, dobString);
          if (patient == null) {
            logger.trace("skipping message, patient not processed for department {}", departmentName);
          } else {
            if (!empty(gender)) {
              patient.addAttribute(hl7AppointmentIntf.processPatientAttribute(patient,
                  Constants.ATTRIBUTE_GENDER, gender));
            }
            if (!empty(race)) {
              patient.addAttribute(hl7AppointmentIntf.processPatientAttribute(patient,
                  Constants.ATTRIBUTE_RACE, race));
            }
            if (!empty(ethnicity)) {
              patient.addAttribute(hl7AppointmentIntf.processPatientAttribute(patient,
                  Constants.ATTRIBUTE_ETHNICITY, ethnicity));
            }

            hl7AppointmentIntf.processAppointment(patient, apptTm, visitEid, visitDesc,
                  AppointmentStatus.getStatusCode(apptStatusString), encounterId, providerEid, departmentName);

            logger.trace("processing ({}, {}, {}, {}, {}, {}, {}", patient.getPatientId(), apptTm,
                visitDesc, AppointmentStatus.getStatusCode(apptStatusString), encounterId, providerEid, departmentName);
          }
          database.commitNow();
        } else {
          logger.debug("skipping message, hl7Appointment is not initialized");
        }
      } catch (ImportException ime) {
        logger.error("Error importing hl7 appointment", ime);
      }
    } catch (HL7Exception hl7e) {
      logger.error("Error reading segment data", hl7e);
    }
    return hl7Customizer;
  }

  private Message getFailedAckMessage(String clinic) throws HL7Exception {
    Date now = new Date();
    String messageString =
        "MSH|^~\\&||SHC|CHOIR|Hl7HohServlet|" +
            msgDateFormatter.format(now) +
            "||ACK||" + clinic + "|2.3||||||\r\n" + "MSA|AA|0|UnrecognizedClinic-" + clinic + "||";

    return (new PipeParser()).parse(messageString);
  }

  private static boolean empty(String s) {
    return s == null || "".equals(s.trim()) || s.trim().length() == 0;
  }

}
