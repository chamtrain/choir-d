package edu.stanford.registry.server.service.rest;

import java.io.IOException;
import java.io.OutputStream;
import java.io.OutputStreamWriter;
import java.nio.charset.StandardCharsets;
import java.text.SimpleDateFormat;
import java.util.ArrayList;
import java.util.Date;
import java.util.List;
import java.util.Map;

import org.apache.log4j.Logger;
import org.apache.poi.xssf.usermodel.XSSFWorkbook;
import org.restlet.data.Disposition;
import org.restlet.data.MediaType;
import org.restlet.ext.servlet.ServletUtils;
import org.restlet.representation.OutputRepresentation;
import org.restlet.representation.Representation;
import org.restlet.resource.ServerResource;

import au.com.bytecode.opencsv.CSVWriter;
import edu.stanford.registry.server.RegistryServletRequest;
import edu.stanford.registry.server.service.AdministrativeServices;
import edu.stanford.registry.server.utils.ReportUtils;

/**
 * Export the Survey Scores.
 * This is called as a restlet - it should have no instance variables.
 */
public class ScoresExporter extends ServerResource {
  private static Logger logger = Logger.getLogger(ScoresExporter.class);

  static final SimpleDateFormat dateFmt = new SimpleDateFormat("MM/dd/yyyy");

  public ScoresExporter() {
    // A public default construct is required- TomCat creates this as a restlet
  }

  @Override
  protected Representation get() {
    logger.debug("Received request for scores report.");
    RegistryServletRequest regRequest = (RegistryServletRequest) ServletUtils.getRequest(getRequest());
    AdministrativeServices service = (AdministrativeServices) regRequest.getService();
    Map<String,String[]> params = regRequest.getParameterMap();

    // Get the survey scores data
    List<List<Object>> data = service.scoresExportData(params);

    // Return as a CSV file
    String[] values = params.get("type");
    String type = (values != null) ? values[0] : null;
    String filename = "SurveyScores.csv";
    if ((type != null) && !type.equals("")) {
      filename = type + ".csv";
    }
    Representation rep = getCsv(filename, data);
    return rep;
  }

  /**
   * Get a CSV representation of the data.
   */
  protected Representation getCsv(String filename, final List<List<Object>> data) {
    logger.debug("Returning CSV file for scores report data.");

    Representation rep = null;
    rep = new OutputRepresentation(MediaType.TEXT_CSV) {
      @Override
      public void write(OutputStream out) throws IOException {
        CSVWriter csvWriter = new CSVWriter(new OutputStreamWriter(out, StandardCharsets.UTF_8));
        for(List<Object> lineData : data) {
          String[] line = getCsvLine(lineData);
          csvWriter.writeNext(line);
        }
        // Flush the CSVWriter to the underlying OutputStream but
        // do not close the OutputStream as specified in the
        // JavaDoc for Representation.write()
        csvWriter.flush();
        csvWriter.close();
      }
    };
    Disposition dep = new Disposition();
    dep.setFilename(filename);
    dep.setType("inline");
    rep.setDisposition(dep);

    return rep;
  }

  /**
   * Convert a row of data to an array of Strings representing
   * a line in the CSV file.
   */
  protected String[] getCsvLine(List<Object> lineData) {
    String[] line = new String[lineData.size()];
    for(int i=0; i<line.length; i++) {
      Object value = lineData.get(i);
      if (value == null) {
        line[i] = null;
      } else if (value instanceof String) {
        line[i] = (String) value;
      } else if (value instanceof Date) {
        line[i] = dateFmt.format((Date)value);
      } else {
        line[i] = value.toString();
      }
    }    
    return line;
  }

  /**
   * Get an Excel representation of the data.
   */
  protected Representation getExcel(String filename, ArrayList<ArrayList<Object>> data) {
    logger.debug("Returning Excel file for scores report data.");

    Representation rep = null;    
    try {
      RegistryServletRequest regRequest = (RegistryServletRequest) ServletUtils.getRequest(getRequest());
      ReportUtils reportWriter = new ReportUtils(regRequest.getSiteInfo());
      final XSSFWorkbook wb = reportWriter.writeXlsx(data, "SurveyScores.xlsx", "SurveyScores", 0);
      rep = new OutputRepresentation(MediaType.APPLICATION_EXCEL) {
        @Override
        public void write(OutputStream out) throws IOException {
          wb.write(out);
          // Do not close the OutputStream as specified in the
          // JavaDoc for Representation.write()
        }
      };
      Disposition dep = new Disposition();
      dep.setFilename(filename);
      dep.setType("inline");
      rep.setDisposition(dep);
    } catch (IOException e) {
      logger.error(e.getMessage(), e);
    }
    return rep;
  }
}
