package edu.stanford.registry.server.shc.totaljoint;

import edu.stanford.registry.server.SiteInfo;
import edu.stanford.registry.server.utils.XMLFileUtils;
import edu.stanford.registry.shared.Patient;
import edu.stanford.registry.shared.PatientAttribute;

import java.text.SimpleDateFormat;
import java.util.ArrayList;
import java.util.Calendar;
import java.util.Comparator;
import java.util.Date;
import java.util.List;

/**
 * Bean class to represent a Total Joint follow up. A follow up
 * consists of a surgery and a follow up type.
 */
public class FollowUp {

  private static final SimpleDateFormat dateFormat = new SimpleDateFormat("MM/dd/yyyy");

  private Surgery surgery;
  private FollowUpType followUpType;

  protected Long siteId;
  protected SiteInfo siteInfo;

  public FollowUp(SiteInfo siteInfo, Surgery surgery, FollowUpType followUpType) {
    this.siteInfo = siteInfo;
    this.siteId = siteInfo.getSiteId();
    this.surgery = surgery;
    this.followUpType = followUpType;
  }

  public String getPatientId() {
    return surgery.getPatientId();
  }

  public void setPatientId(String patientId) {
    surgery.setPatientId(patientId);
  }

  public Date getSurgeryDate() {
    return surgery.getSurgeryDate();
  }

  public void setSurgeryDate(Date surgeryDate) {
    surgery.setSurgeryDate(surgeryDate);
  }

  public String getJoint() {
    return surgery.getJoint();
  }

  public void setJoint(String joint) {
    surgery.setJoint(joint);
  }

  public String getSide() {
    return surgery.getSide();
  }

  public void setSide(String side) {
    surgery.setSide(side);
  }

  public String getFollowUpName() {
    return followUpType.getFollowUpName();
  }

  public String getFollowUpCode() {
    return followUpType.getFollowUpCode();
  }

  public Date getScheduledDate() {
    return followUpType.getScheduledDate(getSurgeryDate());
  }

  /**
   * Determine if the survey date is eligible for the follow up. The
   * survey date is eligible for the follow up if it falls within the
   * eligibility window of the follow up.
   */
  public boolean isEligible(Date surveyDate) {
    return followUpType.isEligible(getSurgeryDate(), surveyDate);
  }

  /**
   * Check if the follow up has been completed by a survey in the
   * list of completed surveys.
   */
  public boolean isCompleted(List<CompletedSurvey> surveys) {
    if (surveys != null) {
      for(CompletedSurvey completedSurvey : surveys) {
        if (getJoint().equals(completedSurvey.getJoint()) &&
            getSide().equals(completedSurvey.getSide()) &&
            isEligible(completedSurvey.getScheduledDate())) {
          return true;
        }
      }
    }
    return false;
  }

  /**
   * Get the survey type for the follow up.
   */
  protected String getSurveyType(Patient patient, Date surveyDate) {
    PatientAttribute attr = patient.getAttribute(TotalJointCustomizer.ATTR_HOOS_KOOS_TYPE);
    boolean useFull = (attr != null) && (attr.getDataValue() != null) && (attr.getDataValue().equalsIgnoreCase("full"));

    XMLFileUtils xmlFileUtils = XMLFileUtils.getInstance(siteInfo);
    String joint = getJoint();
    String side = getSide();

    String surveyType = null;
    if (joint.equals(TotalJointCustomizer.JOINT_HIP)) {
      if (side.equals(TotalJointCustomizer.SIDE_LEFT)) {
        if (useFull) {
          surveyType = xmlFileUtils.getActiveProcessForName(TotalJointCustomizer.SURVEY_FOLLOW_UP_LEFT_HIP, surveyDate);
        } else {
          surveyType = xmlFileUtils.getActiveProcessForName(TotalJointCustomizer.SURVEY_FOLLOW_UP_LEFT_HIP_JR, surveyDate);
        }
      } else if (side.equals(TotalJointCustomizer.SIDE_RIGHT)) {
        if (useFull) {
          surveyType = xmlFileUtils.getActiveProcessForName(TotalJointCustomizer.SURVEY_FOLLOW_UP_RIGHT_HIP, surveyDate);
        } else {
          surveyType = xmlFileUtils.getActiveProcessForName(TotalJointCustomizer.SURVEY_FOLLOW_UP_RIGHT_HIP_JR, surveyDate);
        }
      } else if (side.equals(TotalJointCustomizer.SIDE_BILATERAL)) {
        if (useFull) {
          surveyType = xmlFileUtils.getActiveProcessForName(TotalJointCustomizer.SURVEY_FOLLOW_UP_BI_HIP, surveyDate);
        } else {
          surveyType = xmlFileUtils.getActiveProcessForName(TotalJointCustomizer.SURVEY_FOLLOW_UP_BI_HIP_JR, surveyDate);
        }
      }
    } else if (joint.equals(TotalJointCustomizer.JOINT_KNEE)) {
      if (side.equals(TotalJointCustomizer.SIDE_LEFT)) {
        if (useFull) {
          surveyType = xmlFileUtils.getActiveProcessForName(TotalJointCustomizer.SURVEY_FOLLOW_UP_LEFT_KNEE, surveyDate);
        } else {
          surveyType = xmlFileUtils.getActiveProcessForName(TotalJointCustomizer.SURVEY_FOLLOW_UP_LEFT_KNEE_JR, surveyDate);
        }
      } else if (side.equals(TotalJointCustomizer.SIDE_RIGHT)) {
        if (useFull) {
          surveyType = xmlFileUtils.getActiveProcessForName(TotalJointCustomizer.SURVEY_FOLLOW_UP_RIGHT_KNEE, surveyDate);
        } else {
          surveyType = xmlFileUtils.getActiveProcessForName(TotalJointCustomizer.SURVEY_FOLLOW_UP_RIGHT_KNEE_JR, surveyDate);
        }
      } else if (side.equals(TotalJointCustomizer.SIDE_BILATERAL)) {
        if (useFull) {
          surveyType = xmlFileUtils.getActiveProcessForName(TotalJointCustomizer.SURVEY_FOLLOW_UP_BI_KNEE, surveyDate);
        } else {
          surveyType = xmlFileUtils.getActiveProcessForName(TotalJointCustomizer.SURVEY_FOLLOW_UP_BI_KNEE_JR, surveyDate);
        }
      }
    }

    return surveyType;
  }

  public String toString() {
    return surgery + " " + getFollowUpName() + " " + dateFormat.format(getScheduledDate());
  }

  /**
   * Get a list of all follow ups for a surgery.
   */
  public static List<FollowUp> getFollowUps(SiteInfo siteInfo, Surgery surgery) {
    List<FollowUp> result = new ArrayList<>();
    for(FollowUpType followUpType : followUpTypes) {
      result.add( new FollowUp(siteInfo, surgery, followUpType) );
    }
    return result;
  }

  /**
   * Check if the two follow ups differ only in the side of the surgery.
   * If the left and right sides are done at the same time it is considered
   * a bilateral surgery.
   */
  public static boolean isBilateral(FollowUp fup1, FollowUp fup2) {
    return (fup1 != null) && (fup2 != null) &&
        fup1.getPatientId().equals(fup2.getPatientId()) &&
        fup1.getSurgeryDate().equals(fup2.getSurgeryDate()) &&
        fup1.getJoint().equals(fup2.getJoint()) &&
        ( (fup1.getSide().equals(TotalJointCustomizer.SIDE_LEFT) &&
           fup2.getSide().equals(TotalJointCustomizer.SIDE_RIGHT)) ||
          (fup1.getSide().equals(TotalJointCustomizer.SIDE_RIGHT) &&
           fup2.getSide().equals(TotalJointCustomizer.SIDE_LEFT))
        );
  }

  /**
   * A comparator class used to sort a list of follow ups such that
   * follow ups for the same patient, surgery date and joint will
   * be consecutive in the list. This is to make it easier to find
   * bilateral follow ups which differ only on side.
   */
  public static class BilateralSort implements Comparator<FollowUp> {
    public int compare(FollowUp fup1, FollowUp fup2) {
      int i = fup1.getPatientId().compareTo(fup2.getPatientId());
      if (i == 0) {
        i = fup1.getSurgeryDate().compareTo(fup2.getSurgeryDate());
      }
      if (i == 0) {
        i = fup1.getJoint().compareTo(fup2.getJoint());
      }
      return i;
    }
  }

  /**
   * Array of all follow up types
   */
  private static FollowUpType[] followUpTypes = new FollowUpType[]
  {
    new FollowUpType("3mn",  "3 Months", 12,  1, 12, Calendar.WEEK_OF_YEAR), // 3 months (12 weeks)
    new FollowUpType("1yr",  "1 Year",   12,  3, 6,  Calendar.MONTH), // 12 months
    new FollowUpType("2yr",  "2 Years",  24,  3, 6,  Calendar.MONTH), // 24 months
    new FollowUpType("3yr",  "3 Years",  36,  3, 6,  Calendar.MONTH), // 36 months
    new FollowUpType("5yr",  "5 Years",  60,  3, 6,  Calendar.MONTH), // 60 months
    new FollowUpType("7yr",  "7 Years",  84,  3, 6,  Calendar.MONTH), // 84 months
    new FollowUpType("9yr",  "9 Years",  108, 3, 6,  Calendar.MONTH), // 108 months
    new FollowUpType("11yr", "11 Years", 132, 3, 6,  Calendar.MONTH), // 132 months
    new FollowUpType("13yr", "13 Years", 156, 3, 6,  Calendar.MONTH), // 156 months
    new FollowUpType("15yr", "15 Years", 180, 3, 6,  Calendar.MONTH), // 180 months
    new FollowUpType("17yr", "17 Years", 204, 3, 6,  Calendar.MONTH), // 204 months
    new FollowUpType("19yr", "19 Years", 228, 3, 6,  Calendar.MONTH), // 228 months
    new FollowUpType("21yr", "21 Years", 252, 3, 6,  Calendar.MONTH), // 252 months
    new FollowUpType("23yr", "23 Years", 276, 3, 6,  Calendar.MONTH), // 276 months
    new FollowUpType("25yr", "25 Years", 300, 3, 6,  Calendar.MONTH), // 300 months
    new FollowUpType("27yr", "27 Years", 324, 3, 6,  Calendar.MONTH), // 324 months
    new FollowUpType("29yr", "29 Years", 348, 3, 6,  Calendar.MONTH)  // 348 months
  };

  /**
   * Class used to define a follow up type.
   */
  private static class FollowUpType {
    // Short name used in the Registry code
    protected String followUpName;
    // EAV value used in the Stride code
    protected String followUpCode;
    protected int schedule;
    protected int before;
    protected int after;
    protected int unit;

    public FollowUpType(String name, String code, int schedule, int before, int after, int unit) {
      this.followUpName = name;
      this.followUpCode = code;
      this.schedule = schedule;
      this.before = before;
      this.after = after;
      this.unit = unit;
    }

    public String getFollowUpName() {
      return followUpName;
    }

    public String getFollowUpCode() {
      return followUpCode;
    }

    public Date getScheduledDate(Date surgeryDate) {
      Calendar scheduledDate = Calendar.getInstance();
      scheduledDate.setTime(surgeryDate);
      scheduledDate.add(unit, schedule);
      return scheduledDate.getTime();
    }

    public boolean isEligible(Date surgeryDate, Date surveyDate) {
      Date scheduledDate = getScheduledDate(surgeryDate);

      Calendar start = Calendar.getInstance();
      start.setTime(scheduledDate);
      start.add(unit, -before);
      Date startDate = start.getTime();

      Calendar end = Calendar.getInstance();
      end.setTime(scheduledDate);
      end.add(unit, after);
      Date endDate = end.getTime();

      return (surveyDate.after(startDate) && surveyDate.before(endDate));
    }
  }
}
