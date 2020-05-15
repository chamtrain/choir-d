package edu.stanford.registry.server.xchg.data;

import edu.stanford.registry.server.SiteInfo;
import edu.stanford.registry.server.service.formatter.PatientIdFormatIntf;
import edu.stanford.registry.server.xchg.QualifierIntf;
import edu.stanford.registry.shared.DataTable;
import edu.stanford.registry.shared.Patient;

import java.lang.reflect.InvocationTargetException;
import java.lang.reflect.Method;

import org.apache.log4j.Logger;

public class PatientIdQualifier implements QualifierIntf<Patient> {

  private static final String METHOD_NAME = "getPatientId";
  private static Logger logger = Logger.getLogger(PatientIdQualifier.class);
  private final PatientIdFormatIntf formatter;

  public PatientIdQualifier(SiteInfo siteInfo) {
    formatter = siteInfo.getPatientIdFormatter();
  }

  @Override
  public String getQualifier() {
    return null;
  }

  @Override
  public String[] getQualifiers() {
    return null;
  }

  @Override
  public boolean qualifies(DataTable dt) {

    try {

      Method method = dt.getClass().getMethod(METHOD_NAME);
      Object value = method.invoke(dt);
      return formatter.isValid(value.toString());
    } catch (IllegalArgumentException e) {
      return returnFailure(e);
    } catch (IllegalAccessException e) {
      return returnFailure(e);
    } catch (InvocationTargetException e) {
      return returnFailure(e);
    } catch (SecurityException e) {
      return returnFailure(e);
    } catch (NoSuchMethodException e) {
      return returnFailure(e);
    }

  }

  private boolean returnFailure(Exception e) {
    logger.debug("Object failed to qualify " + e.getMessage());
    return false;
  }

  @Override
  public boolean qualifies(String string) {
    return formatter.isValid(formatter.format(string));
  }


}
