/*
 * Copyright 2020 The Board of Trustees of The Leland Stanford Junior University.
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
package edu.stanford.registry.server.shc.pedpain;

import edu.stanford.registry.server.ServerContext;
import edu.stanford.registry.server.ServerUtils;
import edu.stanford.registry.server.SiteInfo;

import java.io.File;
import java.io.FileOutputStream;
import java.io.IOException;

import java.io.OutputStreamWriter;
import java.io.StringWriter;
import java.nio.charset.StandardCharsets;
import java.text.SimpleDateFormat;

import java.util.Date;
import java.util.List;
import java.util.function.Supplier;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import com.github.susom.database.Database;
import com.github.susom.database.DatabaseProvider;

import au.com.bytecode.opencsv.CSVWriter;

public class PedPainDataExtract  {

  private static Logger logger = LoggerFactory.getLogger(PedPainDataExtract.class);

  final static SimpleDateFormat dateFmt = new SimpleDateFormat("MM/dd/yyyy");

  public PedPainDataExtract() {
  }


  protected String getCsv(List<List<Object>> data) {
    StringWriter out = new StringWriter();

    CSVWriter csvWriter = new CSVWriter(out);
    for(List<Object> lineData : data) {
      String[] line = getCsvLine(lineData);
      csvWriter.writeNext(line);
    }
    try {
      csvWriter.close();
    } catch (IOException e) {
      logger.error("Unexpected I/O error", e);
    }

    return out.toString();
  }

  final static protected String[] getCsvLine(List<Object> lineData) {
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

  public static void main(String[] args) {

    DatabaseProvider.fromPropertyFileOrSystemProperties(
        System.getProperty("build.properties", "../properties/ped.build.properties")
    ).withSqlParameterLogging()
        .transact((Supplier<Database> dbp
        ) -> {
          ServerContext serverContext = new ServerContext(dbp);
          SiteInfo siteInfo = serverContext.getSiteInfo(6L);
          ServerUtils.initialize(".");
          PedPainSurveyData painSurveyData = new PedPainSurveyData(dbp.get(), siteInfo);

          Date fromDt = dateFmt.parse("02/01/2020");
          List<List<Object>> data = painSurveyData.getReportData(6L, fromDt, new Date() );
          String fileName = "../scores" + "-" + new SimpleDateFormat("yyyyMMdd-HHmm").format(new Date()) + ".csv";
          File  outputFile = new File(fileName);

          FileOutputStream fileOutputStream = new FileOutputStream(outputFile);
          CSVWriter csvWriter = new CSVWriter(new OutputStreamWriter(fileOutputStream, StandardCharsets.UTF_8));
          for(List<Object> lineData : data) {
            String[] line = getCsvLine(lineData);
            csvWriter.writeNext(line);
          }
          // Flush the CSVWriter to the underlying OutputStream but
          // do not close the OutputStream as specified in the
          // JavaDoc for Representation.write()
          csvWriter.close();

        });

  }
}
