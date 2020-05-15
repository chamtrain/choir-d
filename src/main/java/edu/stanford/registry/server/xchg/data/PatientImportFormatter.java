package edu.stanford.registry.server.xchg.data;

import edu.stanford.registry.server.SiteInfo;
import edu.stanford.registry.server.service.formatter.PatientIdFormatIntf;
import edu.stanford.registry.server.xchg.FormatterIntf;

import java.text.ParseException;
import java.util.Date;

public class PatientImportFormatter<T> implements FormatterIntf<T> {

  public PatientIdFormatIntf formatter;
  public PatientImportFormatter(SiteInfo siteInfo) {
    formatter = siteInfo.getPatientIdFormatter();
  }
  @Override
  public Object format(String strIn) throws ParseException {
    return formatter.format(strIn);
  }

  @Override
  public Date toDate(String dateIn) throws ParseException {
    return null;
  }

  @Override
  public Date toTime(String timeIn) throws ParseException {
    return null;
  }

  @Override
  public Date toTimeStamp(String tstampIn) throws ParseException {
    return null;
  } 



}

