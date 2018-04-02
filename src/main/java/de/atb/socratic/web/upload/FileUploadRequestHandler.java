/**
 *
 */
package de.atb.socratic.web.upload;

/*-
 * #%L
 * socratic-platform
 * %%
 * Copyright (C) 2016 - 2018 Institute for Applied Systems Technology Bremen GmbH (ATB)
 * %%
 * This program is free software: you can redistribute it and/or modify
 * it under the terms of the GNU Affero General Public License as published by
 * the Free Software Foundation, either version 3 of the License, or
 * (at your option) any later version.
 * 
 * This program is distributed in the hope that it will be useful,
 * but WITHOUT ANY WARRANTY; without even the implied warranty of
 * MERCHANTABILITY or FITNESS FOR A PARTICULAR PURPOSE.  See the
 * GNU General Public License for more details.
 * 
 * You should have received a copy of the GNU Affero General Public License
 * along with this program.  If not, see <http://www.gnu.org/licenses/>.
 * #L%
 */

import java.awt.image.BufferedImage;
import java.io.ByteArrayOutputStream;
import java.io.DataInputStream;
import java.io.File;
import java.io.FileInputStream;
import java.io.FileNotFoundException;
import java.io.IOException;
import java.io.UnsupportedEncodingException;
import java.net.URLEncoder;
import java.util.ArrayList;
import java.util.LinkedHashSet;
import java.util.List;
import java.util.Set;
import java.util.UUID;

import javax.imageio.ImageIO;
import javax.inject.Inject;
import javax.servlet.ServletOutputStream;
import javax.servlet.http.HttpServletRequest;
import javax.servlet.http.HttpServletResponse;

import de.atb.socratic.model.FileInfo;
import de.atb.socratic.model.User;
import de.atb.socratic.web.qualifier.FileUploadCache;
import de.atb.socratic.web.qualifier.LoggedInUser;
import de.atb.socratic.web.upload.FileUploadHelper.UploadType;
import org.apache.commons.lang.StringUtils;
import org.apache.wicket.Application;
import org.apache.wicket.ajax.json.JSONArray;
import org.apache.wicket.ajax.json.JSONException;
import org.apache.wicket.ajax.json.JSONObject;
import org.apache.wicket.cdi.CdiContainer;
import org.apache.wicket.markup.html.form.Form;
import org.apache.wicket.markup.html.form.upload.FileUpload;
import org.apache.wicket.protocol.http.IMultipartWebRequest;
import org.apache.wicket.protocol.http.servlet.ServletWebRequest;
import org.apache.wicket.request.IRequestCycle;
import org.apache.wicket.request.IRequestHandler;
import org.apache.wicket.request.Request;
import org.apache.wicket.util.lang.Bytes;
import org.apache.wicket.util.resource.ResourceStreamNotFoundException;
import org.apache.wicket.util.string.Strings;
import org.apache.wicket.util.upload.FileItem;
import org.apache.wicket.util.upload.FileUploadException;
import org.imgscalr.Scalr;
import org.jboss.solder.logging.Logger;

/**
 * @author ATB
 */
public class FileUploadRequestHandler implements IRequestHandler {

    // inject a logger
    @Inject
    Logger logger;

    // inject the currently logged in user
    @Inject
    @LoggedInUser
    User loggedInUser;

    @Inject
    @FileUploadCache
    FileUploadInfo fileUploadInfo;

    // For linking purposes
    public static final FileUploadRequestHandler INSTANCE = new FileUploadRequestHandler();

    private static final String UTF8 = "UTF-8";

    private static FileUploadHelper helper = new FileUploadHelper(UploadType.USER);

    /**
     *
     */
    public FileUploadRequestHandler() {
        // make this thing available in CDI contexts.
        CdiContainer.get().getNonContextualManager().inject(this);
    }

    /*
     * (non-Javadoc)
     *
     * @see
     * org.apache.wicket.request.IRequestHandler#detach(org.apache.wicket.request
     * .IRequestCycle)
     */
    @Override
    public void detach(IRequestCycle requestCycle) {
        logger.debug("detaching FileUploadRequestHandler");
    }

    /*
     * (non-Javadoc)
     *
     * @see
     * org.apache.wicket.request.IRequestHandler#respond(org.apache.wicket.request
     * .IRequestCycle)
     */
    @Override
    public void respond(IRequestCycle requestCycle) {
        Request request = requestCycle.getRequest();
        HttpServletResponse response = (HttpServletResponse) requestCycle
                .getResponse().getContainerResponse();

        // we're always returning JSON
        response.setContentType("application/json");

        if (request instanceof ServletWebRequest) {
            ServletWebRequest servletWebRequest = (ServletWebRequest) request;
            HttpServletRequest httpServletRequest = servletWebRequest
                    .getContainerRequest();
            String method = httpServletRequest.getMethod();

            if (Form.METHOD_GET.equalsIgnoreCase(method)) {
                // for retrieving files, their meta-information and thumbnails
                doGET(servletWebRequest, response);

            } else if (Form.METHOD_POST.equalsIgnoreCase(method)) {
                // for uploading files
                doPOST(servletWebRequest, response);

            } else {
                // should never happen... but if it does
                logger.debugf("\tRequest method is neither %s nor %s",
                        Form.METHOD_GET, Form.METHOD_POST);
                writeError(response);
                return;
            }
        } else {
            // should never happen... but if it does
            logger.debug("\tRequest is not a ServletWebRequest");
            writeError(response);
            return;
        }
    }

    /**
     * @param uploadCacheId
     * @param response
     * @return
     */
    private boolean invalidUploadCacheId(String uploadCacheId,
                                         HttpServletResponse response) {
        if (StringUtils.isBlank(uploadCacheId)
                || uploadCacheId.equals("undefined")) {
            // should never happen... but if it does
            logger.debug("\tRequest has no valid uploadCacheId");
            writeError(response);
            return true;
        }
        return false;
    }

    /**
     * @param request
     * @param response
     */
    private void doGET(ServletWebRequest request, HttpServletResponse response) {
        // extract and check uploadCacheId
        String uploadCacheId = request.getRequestParameters()
                .getParameterValue("uploadCacheId").toOptionalString();
        if (invalidUploadCacheId(uploadCacheId, response)) {
            return;
        }
        // handle GET request
        if (request.getRequestParameters().getParameterValue("getfile")
                .toOptionalString() != null) {
            // return cached file
            getFile(request.getRequestParameters().getParameterValue("getfile")
                    .toOptionalString(), uploadCacheId, request, response);

        } else if (request.getRequestParameters().getParameterValue("delfile")
                .toOptionalString() != null) {
            // delete file from cache
            deleteFile(
                    request.getRequestParameters().getParameterValue("delfile")
                            .toOptionalString(), uploadCacheId, request,
                    response);

        } else if (request.getRequestParameters().getParameterValue("getthumb")
                .toOptionalString() != null) {
            // return file if it is an image
            getThumbnail(
                    request.getRequestParameters()
                            .getParameterValue("getthumb").toOptionalString(),
                    uploadCacheId, request, response);

        } else {
            // return existing files meta data
            getFilesData(uploadCacheId, request, response);
        }
    }

    /**
     * @param fileName
     * @param uploadCacheId
     * @param request
     * @param response
     */
    private void getThumbnail(String fileName, String uploadCacheId,
                              ServletWebRequest request, HttpServletResponse response) {
        String filePath = helper.getFilePath(uploadCacheId, fileName);
        FileInfo fileInfo = fileUploadInfo.getFileInfo(uploadCacheId, filePath);
        if (fileInfo != null) {
            sendThumbnail(fileInfo, response);
            return;
        }

        logger.debugf("File %s is not cached", fileName);
        writeError(response);
        return;
    }

    /**
     * @param fileInfo
     * @param response
     */
    private void sendThumbnail(FileInfo fileInfo, HttpServletResponse response) {
        File file = new File(fileInfo.getPath());

        if (file.exists()) {
            String mimeType = fileInfo.getContentType();
            ServletOutputStream os = null;
            ByteArrayOutputStream baos = null;
            try {
                BufferedImage im = ImageIO.read(file);
                BufferedImage thumb = null;

                if (im != null) {
                    thumb = Scalr.resize(im, 32);
                    os = response.getOutputStream();
                    baos = new ByteArrayOutputStream();
                    ImageIO.write(thumb, "PNG", baos);
                    response.setContentType(mimeType);
                    response.setContentLength(baos.size());
                    response.setHeader("Content-Disposition", "inline; filename=\"" + file.getName() + "\"");
                    baos.writeTo(os);
                } else {
                    try {
                        // show mime type icon for other files
                        im = ImageIO.read(FileUploadHelper.iconRef.getResource().getResourceStream().getInputStream());
                        thumb = Scalr.resize(im, 32);
                        os = response.getOutputStream();
                        baos = new ByteArrayOutputStream();
                        ImageIO.write(thumb, "PNG", baos);
                        response.setContentType("image/png");
                        response.setContentLength(baos.size());
                        response.setHeader("Content-Disposition", "inline; filename=\"" + file.getName() + "\"");
                        baos.writeTo(os);
                    } catch (ResourceStreamNotFoundException e) {
                        // if we cannot load this file we don't send any
                        // thumbnail
                        response.setContentType("image/png");
                        response.setContentLength(0);
                    }
                }

            } catch (FileNotFoundException e) {
                logger.debugf("File %s cannot be found", fileInfo.getInternalName());
                writeError(response);
            } catch (IOException e) {
                logger.debugf("File %s cannot be read", fileInfo.getInternalName());
                writeError(response);
            } finally {
                try {
                    if (baos != null) {
                        baos.flush();
                        baos.close();
                    }
                    if (os != null) {
                        os.flush();
                        os.close();
                    }
                } catch (IOException e) {
                    logger.error(e.getMessage(), e);
                    // Swallow, if we can't write anything, we give up
                }
            }
        }
    }

    /**
     * @param fileName
     * @param uploadCacheId
     * @param request
     * @param response
     */
    private void deleteFile(String fileName, String uploadCacheId,
                            ServletWebRequest request, HttpServletResponse response) {
        String filePath = helper.getFilePath(uploadCacheId, fileName);
        FileInfo fileInfo = fileUploadInfo.getFileInfo(uploadCacheId, filePath);
        if (fileInfo != null) {
            removeFile(uploadCacheId, fileInfo);
            return;
        }

        logger.debugf("File %s is not cached", fileName);
        writeError(response);
        return;
    }

    /**
     * @param cacheId
     * @param fileInfo
     */
    private void removeFile(String cacheId, FileInfo fileInfo) {
        // remove from file upload cache
        fileUploadInfo.removeFileInfo(cacheId, fileInfo);
    }

    /**
     * @param fileName
     * @param uploadCacheId
     * @param request
     * @param response
     */
    private void getFile(String fileName, String uploadCacheId,
                         ServletWebRequest request, HttpServletResponse response) {
        String filePath = helper.getFilePath(uploadCacheId, fileName);
        FileInfo fileInfo = fileUploadInfo.getFileInfo(uploadCacheId, filePath);
        if (fileInfo != null) {
            sendFile(fileInfo, response);
            return;
        }

        logger.debugf("File %s is not cached", fileName);
        writeError(response);
        return;
    }

    /**
     * @param fileInfo
     * @param response
     */
    private void sendFile(FileInfo fileInfo, HttpServletResponse response) {
        File file = new File(fileInfo.getPath());
        if (file.exists()) {
            ServletOutputStream os = null;
            DataInputStream is = null;
            try {
                int bytes = 0;
                os = response.getOutputStream();

                response.setContentType(fileInfo.getContentType());
                response.setContentLength((int) file.length());
                response.setHeader("Content-Disposition", "inline; filename=\""
                        + fileInfo.getDisplayName() + "\"");

                byte[] bbuf = new byte[1024];
                is = new DataInputStream(new FileInputStream(file));

                while ((is != null) && ((bytes = is.read(bbuf)) != -1)) {
                    os.write(bbuf, 0, bytes);
                }
            } catch (FileNotFoundException e) {
                logger.debugf("File %s cannot be found",
                        fileInfo.getInternalName());
                writeError(response);
                return;
            } catch (IOException e) {
                logger.debugf("File %s cannot be read",
                        fileInfo.getInternalName());
                writeError(response);
                return;
            } finally {
                try {
                    if (is != null) {
                        is.close();
                    }
                    if (os != null) {
                        os.flush();
                        os.close();
                    }
                } catch (IOException e) {
                    e.printStackTrace();
                    // Swallow, if we can't write anything, we give up
                }
            }
        }
    }

    /**
     * @param uploadCacheId
     * @param request
     * @param response
     */
    private void getFilesData(String uploadCacheId, ServletWebRequest request,
                              HttpServletResponse response) {
        try {
            // write JSON response
            response.getWriter().write(
                    writeJSONResponse(
                            request.getContextPath() + "/" + request.getUrl(),
                            fileUploadInfo.getFileInfos(uploadCacheId)));
        } catch (Exception e) {
            logger.debug("Error reading existing files", e);
            writeError(response);
            return;
        }
    }

    /**
     * @param request
     * @param response
     */
    private void doPOST(ServletWebRequest request, HttpServletResponse response) {

        // convert request to file upload request because of WICKET-4752
        String contentType = request.getContainerRequest().getContentType();
        if (Strings.isEmpty(contentType) == false
                && contentType.toLowerCase().startsWith(
                Form.ENCTYPE_MULTIPART_FORM_DATA)) {
            try {
                request = request.newMultipartWebRequest(
                        Application.get().getApplicationSettings()
                                .getDefaultMaximumUploadSize(), "externalForm");
            } catch (FileUploadException e) {
                throw new RuntimeException(e);
            }
        }

        String uploadCacheId = request.getRequestParameters()
                .getParameterValue("uploadCacheId").toOptionalString();

        if (invalidUploadCacheId(uploadCacheId, response)) {
            return;
        }

        response.setHeader("Connection", "close");

        if (!(request instanceof IMultipartWebRequest)) {
            logger.debug("\tUpload request is not a multipart request");
            writeError(response);
            return;
        }

        final List<FileItem> files = new ArrayList<FileItem>();
        for (List<FileItem> namedFiles : ((IMultipartWebRequest) request)
                .getFiles().values()) {
            files.addAll(namedFiles);
        }

        try {
            // add files to FileUploadInfo
            Set<FileInfo> fileInfos = writeFiles(uploadCacheId, files, response);

            // write JSON response
            response.getWriter().write(
                    writeJSONResponse(
                            request.getContextPath() + "/" + request.getUrl()
                                    + "?uploadCacheId=" + uploadCacheId,
                            fileInfos));

        } catch (Exception e) {
            logger.debug("Error handling upload");
            writeError(response);
            return;
        }
    }

    /**
     * @param uploadCacheId
     * @param files
     * @param response
     * @return
     * @throws IOException
     * @throws JSONException
     */
    private Set<FileInfo> writeFiles(final String uploadCacheId,
                                     final List<FileItem> files, final HttpServletResponse response)
            throws IOException, JSONException {
        Set<FileInfo> fileInfos = new LinkedHashSet<FileInfo>();

        if (!files.isEmpty()) {
            File folder = helper.createUploadFolder(uploadCacheId, null, null);

            for (FileItem fileItem : files) {
                // Ignore form fields
                if (!fileItem.isFormField()) {
                    final FileUpload fileUpload = new FileUpload(fileItem);

                    // add unique identifier to file name to distinguish between
                    // files of same name but different versions or upload times
                    File file = new File(folder.getAbsolutePath()
                            + File.separatorChar + UUID.randomUUID() + "_"
                            + fileUpload.getClientFileName());
                    fileItem.write(file);

                    // Store info
                    logger.debugf("\tUpload registered: %s (%s)",
                            fileUpload.getClientFileName(),
                            Bytes.bytes(fileUpload.getSize()));
                    FileInfo fileInfo = new FileInfo(file.getAbsolutePath(),
                            file.getName(), fileUpload.getClientFileName(),
                            fileUpload.getSize(), fileUpload.getContentType());
                    fileUploadInfo.addFileInfo(uploadCacheId, fileInfo);
                    fileInfos.add(fileInfo);
                }
            }
        }
        return fileInfos;
    }

    /**
     * @param url
     * @param files
     * @return
     * @throws JSONException
     * @throws UnsupportedEncodingException
     */
    private String writeJSONResponse(String url, Set<FileInfo> files)
            throws JSONException, UnsupportedEncodingException {
        JSONArray jsonFiles = new JSONArray();
        for (FileInfo fileInfo : files) {
            JSONObject jsonFile = new JSONObject();
            jsonFile.put("name", fileInfo.getDisplayName());
            jsonFile.put("size", fileInfo.getSize());
            jsonFile.put(
                    "url",
                    url
                            + "&getfile="
                            + URLEncoder.encode(fileInfo.getInternalName(),
                            UTF8));
            jsonFile.put(
                    "thumbnail_url",
                    url
                            + "&getthumb="
                            + URLEncoder.encode(fileInfo.getInternalName(),
                            UTF8));
            jsonFile.put(
                    "delete_url",
                    url
                            + "&delfile="
                            + URLEncoder.encode(fileInfo.getInternalName(),
                            UTF8));
            jsonFile.put("delete_type", "GET");
            jsonFiles.put(jsonFile);
        }
        return jsonFiles.toString();
    }

    /**
     * @param response
     */
    private void writeError(HttpServletResponse response) {
        try {
            response.getWriter().write("{status:\"error\"}");
        } catch (IOException e) {
            e.printStackTrace();
            // Swallow, if we can't write anything, we give up
        }
    }
}
