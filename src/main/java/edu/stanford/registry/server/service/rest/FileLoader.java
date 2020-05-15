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

package edu.stanford.registry.server.service.rest;

import edu.stanford.registry.server.RegistryServletRequest;
import edu.stanford.registry.server.service.AdministrativeServices;
import edu.stanford.registry.server.utils.XchgUtils;
import edu.stanford.registry.server.SiteInfo;

import java.io.File;
import java.io.IOException;
import java.security.SecureRandom;
import java.util.List;

import org.apache.commons.fileupload.FileItem;
import org.apache.commons.fileupload.disk.DiskFileItemFactory;
import org.apache.commons.fileupload.servlet.ServletFileUpload;
import org.apache.log4j.Logger;
import org.json.JSONException;
import org.json.JSONObject;
import org.restlet.data.MediaType;
import org.restlet.data.Status;
import org.restlet.ext.fileupload.RestletFileUpload;
import org.restlet.ext.json.JsonRepresentation;
import org.restlet.ext.servlet.ServletUtils;
import org.restlet.representation.Representation;
import org.restlet.representation.StringRepresentation;
import org.restlet.representation.Variant;
import org.restlet.resource.ServerResource;

public class FileLoader extends ServerResource {

  private static Logger logger = Logger.getLogger(FileLoader.class);
  private static SecureRandom randomGenerator = new SecureRandom();

  /**
   * During construction, the getRequest() returns null...
   */
  public FileLoader() {  // A public default construct is required- TomCat creates this as a restlet
    getVariants().add(new Variant(MediaType.TEXT_HTML));
    // List<Variant> variants = getVariants();
  }

  @Override
  public Representation doHandle(Variant variant) {

    RegistryServletRequest regRequest = (RegistryServletRequest) ServletUtils.getRequest(getRequest());
    logger.debug("req uri='" + regRequest.getRequestURI() + "' pathinfo='" + regRequest.getPathInfo()
        + "' context path='" + regRequest.getContextPath() + "'");
    SiteInfo siteInfo = regRequest.getSiteInfo();

    AdministrativeServices adminService = (AdministrativeServices) regRequest.getService();
    if (variant != null) {
      if (ServletFileUpload.isMultipartContent(regRequest)) {
	  return handleMultipart(adminService, siteInfo);
      } else {
        return handleText(regRequest.getPathInfo(), adminService);
      }
    } else {
      // POST request with no entity.
      setStatus(Status.CLIENT_ERROR_BAD_REQUEST);
    }
    return null;
  }

private Representation handleMultipart(AdministrativeServices adminService, SiteInfo siteInfo) {
    logger.debug("in handleMultipart");
    /**
     * Create the file factory and upload handler
     */
    // 1/
    DiskFileItemFactory factory = new DiskFileItemFactory();
    factory.setSizeThreshold(1000240);
    RestletFileUpload upload = new RestletFileUpload(factory);

    /**
     * Get the file and write it to disk
     */

    List<FileItem> items;
    String message = "";
    try {
      items = upload.parseRequest(getRequest());

      // File loadFileDir =
      // XchgUtils.getImportFilesPendingDirectory(definitionName);
      File defs = null;
      File data = null;
      String type = null;
      for (FileItem fileItem : items) {
        if (fileItem.getFieldName().length() > 10) {
          String fieldName = fileItem.getFieldName();
          type = fieldName.substring(10);

          if (fileItem.getFieldName().startsWith("importdata")) {
            logger.debug("Importing data type " + type);
            String srcFilename = fileItem.getName();
            String extension = srcFilename.toLowerCase().endsWith(".txt") ? ".txt" : ".csv";
            String fileName = randomGenerator.nextInt(99999999) + extension;

            File f = new XchgUtils(siteInfo).getImportFilesProcessedDirectory(type);
            if (f == null) {
              message = siteInfo.getIdString() + "Property importProcessedFileDirectory is not set.";
              continue;
            }
            f = new XchgUtils(siteInfo).getImportFilesPendingDirectory(type);
            if (f == null) {
              message = siteInfo.getIdString() + "Property importPendingFileDirectory is not set.";
              continue;
            }
            data = new File(f.getAbsolutePath(), fileName);
            fileItem.write(data);
            logger.debug("Importing file=" + data.getAbsolutePath());
            adminService.loadPendingImports(type);
          }
        }
        if (fileItem.getFieldName().equals("importdefs")) {
          defs = new File(siteInfo.getProperty("importDefinitionDirectory") + File.separator + type
              + ".xlsx");
          fileItem.write(defs);
        }
      }
      if (data != null && defs != null) {
        logger.debug("FileLoader: loading cssvFile and definition file");
        adminService.loadCsv(defs, data);
      }

    } catch (Throwable e) {
      String msg = "Exception loading file " + e.getMessage();
      logger.error(msg, e);
      setStatus(Status.CLIENT_ERROR_UNPROCESSABLE_ENTITY);
      return new StringRepresentation(msg);
    }
    if (!message.isEmpty())
      return new StringRepresentation(message);

    return new StringRepresentation("SUCCESS");
  }

  /**
   * This accepts a text request to process import directories
   *
   * @param importType   Name of the import directory
   * @param adminService
   * @return
   */
  private Representation handleText(String importType, AdministrativeServices adminService) {

    if (importType != null && importType.startsWith("/") && importType.length() > 1) {
      importType = importType.substring(1);
    }
    logger.debug("in handleText importType:" + importType);
    try {
      adminService.loadPendingImports(importType);
    } catch (IOException e) {
      logger.error("IO Error importing files ", e);
    }
    JsonRepresentation result = null;
    JSONObject jsonObj = null;
    try {
      jsonObj = new JSONObject("{ response : success }");
    } catch (JSONException e) {
      logger.error("JSON error creating response", e);
    }
    result = new JsonRepresentation(jsonObj);
    return result;
  }
}
