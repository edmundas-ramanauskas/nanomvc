package org.nanomvc.mvc;

import com.google.gson.Gson;
import org.nanomvc.http.LocalFile;
import org.nanomvc.utils.RequestUtil;
import java.io.IOException;
import java.io.InputStream;
import java.lang.reflect.InvocationTargetException;
import java.text.SimpleDateFormat;
import java.util.ArrayList;
import java.util.Date;
import java.util.HashMap;
import java.util.List;
import java.util.Locale;
import java.util.Map;
import java.util.UUID;
import java.util.concurrent.ExecutorService;
import javax.servlet.ServletContext;
import javax.servlet.http.HttpServletRequest;
import javax.servlet.http.HttpServletResponse;
import javax.servlet.http.HttpSession;
import org.apache.commons.beanutils.BeanUtils;
import org.apache.commons.fileupload.FileItem;
import org.apache.commons.lang.StringUtils;
import org.jsoup.Jsoup;
import org.nanomvc.utils.FileUtil;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

public abstract class Controller
{
    private static Logger _log = LoggerFactory.getLogger(Controller.class);
    
    protected HttpServletRequest request;
    protected HttpServletResponse response;
    protected HttpSession session;
    protected ServletContext context;
    private Map<String, FileItem> files;
    private Map<String, String> fields;
    private Router router;
    private String controller;
    private String action;
    private String viewsPath;
    private String template;
    private Map<String, Object> params;
    private Map<String, Object> global;
    private Application app;
    
    protected static final int IMG_CROP = 1;
    protected static final int IMG_RESIZE = 2;
    protected static final int IMG_RESIZE_CROP = 3;
    
    public static final String PATH_PUBLIC_UPL = "/public/upl/";
    public static final String PATH_IMAGES = "/images";
    public static final String SLASH = "/";
    public static final String EMPTY = "";
    
    private static final String XML_HTTP_REQUEST = "xmlhttprequest";

    public final void config(HttpServletRequest request, HttpServletResponse response, 
            ServletContext context, String viewsPath, String controller, 
            String action, Router router, Map files, Map fields) {
        this.request = request;
        this.response = response;
        this.context = context;
        this.session = request.getSession();
        this.files = files;
        this.fields = fields;

        this.router = router;

        this.viewsPath = (viewsPath.endsWith(SLASH) ? viewsPath : 
                new StringBuilder().append(viewsPath).append(SLASH).toString());
        this.controller = controller;
        this.action = action;
        this.template = action;

        this.app = new Application(controller, action, getBaseUrl(), 
                getCurrentUrl(), router, request.getSession());
        
        this.params = new HashMap();
        this.global = new HashMap();
    }

    public void init() {
    }

    protected String getPath() {
        return context.getRealPath(SLASH);
    }

    public void thumb(String image, String path, int width, int height, int method) {
        try {
            FileUtil.thumb(image, getImagesPath(path), width, height, method);
        } catch (IOException ex) {

        }
    }

    protected final String saveImageFromUrl(String url, String filename) {
        return saveImageFromUrl(url, filename, EMPTY);
    }

    protected final String saveImageFromUrl(String url, String filename, String path) {
        try {
            if (path == null) {
                path = "";
            } else if ((!path.equals(EMPTY)) && (!path.startsWith(SLASH))) {
                path = new StringBuilder().append(SLASH).append(path).toString();
            }
            return RequestUtil.saveImage(url, new StringBuilder().append(getPath())
                    .append(PATH_PUBLIC_UPL).append(controller)
                    .append(PATH_IMAGES).append(path).toString(), filename);
        } catch (IOException ex) {
        }
        return null;
    }

    protected final String getImagesPath() {
        return getImagesPath(EMPTY);
    }

    protected final String getImagesPath(String path) {
        if (path == null) {
            path = EMPTY;
        } else if ((!path.equals(EMPTY)) && (!path.startsWith(SLASH))) {
            path = new StringBuilder().append(SLASH).append(path).toString();
        }
        return new StringBuilder().append(getPath()).append(PATH_PUBLIC_UPL)
                .append(controller).append(PATH_IMAGES).append(path).toString();
    }

    protected final String getImagePath(String filename) {
        return getImagePath(filename, EMPTY);
    }

    protected final String getImagePath(String filename, String path) {
        if (path == null) {
            path = EMPTY;
        } else if ((path != null) && (!path.equals(EMPTY)) && (!path.startsWith(SLASH))) {
            path = new StringBuilder().append(SLASH).append(path).toString();
        }
        return new StringBuilder().append(getPath()).append(PATH_PUBLIC_UPL)
                .append(controller).append(PATH_IMAGES).append(path)
                .append(SLASH).append(filename).toString();
    }

    protected final String getImageUrl(String filename) {
        return getImageUrl(filename, "");
    }

    protected final String getImageUrl(String filename, String path) {
        if (path == null) {
            path = EMPTY;
        } else if ((path != null) && (!path.equals(EMPTY)) && (!path.startsWith(SLASH))) {
            path = new StringBuilder().append(SLASH).append(path).toString();
        }
        return new StringBuilder().append(PATH_PUBLIC_UPL).append(controller)
                .append(PATH_IMAGES).append(path).append(SLASH).append(filename)
                .toString();
    }

    protected final LocalFile handleFileUpload(String fieldName) {
        return handleFileUpload(fieldName, null, UUID.randomUUID().toString());
    }

    protected final LocalFile handleFileUpload(String fieldName, String fileName) {
        return handleFileUpload(fieldName, null, fileName);
    }

    protected final LocalFile handleFileUpload(String fieldName, String pathName, 
            String fileName) {
        try {
            if (pathName == null) {
                pathName = EMPTY;
            }
            if (!pathName.equals(EMPTY)) {
                pathName = pathName.startsWith(SLASH) ? pathName : 
                        new StringBuilder().append(SLASH).append(pathName).toString();
                pathName = pathName.endsWith(SLASH) ? pathName : 
                        new StringBuilder().append(pathName).append(SLASH).toString();
            } else {
                pathName = SLASH;
            }
            pathName = new StringBuilder().append(pathName).append(getLocalDate()).toString();

            InputStream is = getFile(fieldName);
            String path = new StringBuilder().append(PATH_PUBLIC_UPL)
                    .append(controller).append(PATH_IMAGES)
                    .append(pathName).toString();
            String file = RequestUtil.saveImage(is, context.getRealPath(path), fileName);
            if (file != null) {
                return new LocalFile(path, pathName, file);
            }
        } catch (Exception ex) {
            
        }
        return null;
    }

    protected final List<LocalFile> handleMultipleFilesUpload(String fieldName) {
        return handleMultipleFilesUpload(fieldName, null, null);
    }

    protected final List<LocalFile> handleMultipleFilesUpload(String fieldName, 
            String fileName) {
        return handleMultipleFilesUpload(fieldName, null, fileName);
    }

    protected final List<LocalFile> handleMultipleFilesUpload(String fieldName, 
            String pathName, String fileName) {
        try {
            if (pathName == null) {
                pathName = EMPTY;
            }
            if (!pathName.equals(EMPTY)) {
                pathName = pathName.startsWith(SLASH) ? pathName : 
                        new StringBuilder().append(SLASH).append(pathName).toString();
                pathName = pathName.endsWith(SLASH) ? pathName : 
                        new StringBuilder().append(pathName).append(SLASH).toString();
            } else {
                pathName = SLASH;
            }
            pathName = new StringBuilder().append(pathName).append(getLocalDate())
                    .toString();

            List<InputStream> streamList = getFiles(fieldName);
            if (streamList == null) {
                return null;
            }
            String path = new StringBuilder().append(PATH_PUBLIC_UPL)
                    .append(controller).append(PATH_IMAGES).append(pathName)
                    .toString();
            List files = new ArrayList();

            Integer i = 0;
            for (InputStream stream : streamList) {
                i++;
                String name = UUID.randomUUID().toString();
                if(fileName != null)
                    name = UUID.randomUUID().toString();
                String file = RequestUtil.saveImage(stream, context.getRealPath(path), name);
                if (file != null) {
                    files.add(new LocalFile(path, pathName, file));
                }
            }

            return files;
        } catch (Exception ex) {
        }
        return null;
    }

    private String getLocalDate() {
        Locale lt = new Locale("lt", "LT");
        SimpleDateFormat formatter = new SimpleDateFormat("yyyy-MM-dd", lt);
        return formatter.format(new Date());
    }
    
    protected final String clean(String input) {
        return Jsoup.parse(input).text();
    }
    
    protected void setTemplate(String template) {
        this.template = template;
    }
    
    protected final void assignToAll(String key, Object value) {
        assign(key, value);
        assignToMain(key, value);
    }
    
    protected final void assignToMain(String key, Object value) {
        key = (key.startsWith("main")) ? key : "main." + key;
        assign(key, value);
    }

    protected final void assign(String key, Object value) {
        if (key.startsWith("main.")) {
            if (global == null) {
                global = new HashMap();
            }
            global.put(key.substring("main.".length()), value);
        } else {
            if (params == null) {
                params = new HashMap();
            }
            params.put(key, value);
        }
    }

    protected final InputStream getFile(String name) {
        try {
            if ((files != null) && (files.containsKey(name))) {
                return ((FileItem) files.get(name)).getInputStream();
            }
        } catch (Exception ex) {
            _log.error("file not found", ex);
            throw new RuntimeException(ex);
        }
        return null;
    }

    protected final List<InputStream> getFiles(String name) {
        try {
            if ((files != null) && (files.containsKey(name))) {
                List list = new ArrayList();
                if ((files.get(name) instanceof List)) {
                    List<FileItem> tFiles = (List) files.get(name);
                    for (FileItem file : tFiles) {
                        list.add(file.getInputStream());
                    }
                    return list;
                }
                FileItem file = (FileItem) files.get(name);
                list.add(file.getInputStream());

                return list;
            }
        } catch (Exception e) {
            _log.error("file not found", e);
        }
        return null;
    }

    protected final String getParam(String key) {
        if ((this.fields != null) && (this.fields.containsKey(key))) {
            return (String) this.fields.get(key);
        }
        return this.request.getParameter(key);
    }
    
    protected final String getParamString(String key) {
        return getParam(key);
    }
    
    protected final Integer getParamInt(String key) {
        return getParamInteger(key);
    }
    
    protected final Integer getParamInteger(String key) {
        return Integer.valueOf(getParam(key));
    }
    
    protected final Long getParamLong(String key) {
        return Long.valueOf(getParam(key));
    }
    
    protected final Double getParamDouble(String key) {
        return Double.valueOf(getParam(key));
    }
    
    protected final Float getParamFloat(String key) {
        return Float.valueOf(getParam(key));
    }

    protected final String[] getValues(String key) {
        return request.getParameterValues(key);
    }

    protected final void storeSet(String key, Object value) {
        request.getSession().setAttribute(key, value);
    }

    protected final Object storeGet(String key) {
        return request.getSession().getAttribute(key);
    }

    protected final void storeClear() {
        request.getSession().invalidate();
    }

    protected final String getUrlPath() {
        return request.getServletPath();
    }

    protected final String getBaseUrl() {
        if ((request.getServerPort() == 80) || (request.getServerPort() == 443)) {
            return new StringBuilder().append(request.getScheme())
                    .append("://").append(request.getServerName())
                    .append(request.getContextPath()).toString();
        }

        return new StringBuilder().append(request.getScheme())
                .append("://").append(request.getServerName())
                .append(":").append(request.getServerPort())
                .append(request.getContextPath()).toString();
    }

    protected final String getCurrentUrl() {
        String requestUri = request.getRequestURI() != null ? 
                request.getRequestURI() : EMPTY;
        if ((request.getServerPort() == 80) || (request.getServerPort() == 443)) {
            return new StringBuilder().append(request.getScheme()).append("://")
                    .append(request.getServerName()).append(requestUri).toString();
        }

        return new StringBuilder().append(request.getScheme()).append("://")
                .append(request.getServerName()).append(":")
                .append(request.getServerPort()).append(requestUri).toString();
    }

    protected final String getCurrentUrlFull() {
        String queryString = request.getQueryString() != null ? 
                request.getQueryString() : EMPTY;
        return new StringBuilder().append(getUrlPath()).append(queryString).toString();
    }
    
    protected final String createUrl(String controller, String action) {
        return createUrl(controller, action, (Object) null);
    }

    protected final String createUrl(String controller, String action, Object... params) {
        String route = new StringBuilder().append(controller.substring(0, 1)
                .toUpperCase()).append(controller.substring(1).toLowerCase())
                .append(".").append(action.toLowerCase()).toString();

        String url = new StringBuilder().append(SLASH).append(controller)
                .append(SLASH).append(action).toString();
        if (router.reverseRoutes().containsKey(route)) {
            url = (String) router.reverseRoutes().get(route);
        }
        return new StringBuilder().append(getBaseUrl()).append(url)
                .append(params != null ? new StringBuilder().append(SLASH)
                        .append(StringUtils.join(params, SLASH))
                        .toString() : EMPTY).toString();
    }

    protected final Boolean isAjax() {
        String xReq = request.getHeader("X-Requested-With");
        return Boolean.valueOf((xReq != null)
                && (xReq.toLowerCase().startsWith(XML_HTTP_REQUEST)));
    }

    protected final Boolean isEmpty(String value) {
        return (value == null || value.equals(EMPTY));
    }
    
    protected final void parseData(Object bean) {
        try {
            BeanUtils.populate(bean, request.getParameterMap());
            BeanUtils.populate(bean, fields);
        } catch (IllegalAccessException | InvocationTargetException ex) {
            _log.error("BeanUtils", ex);
        }
    }
    
    protected final void submitJob(Runnable job) {
        ExecutorService executor = (ExecutorService) context.getAttribute("NANOMVC_EXECUTOR");
        executor.submit(job);
    }
    
    private String getTemplate() {
        String template = (this.template.indexOf(SLASH) > 0) ? this.template : 
                new StringBuilder().append(controller).append(SLASH)
                .append(this.template).toString();
        return template + (!this.template.endsWith(".htm") ? ".htm" : EMPTY);
    }
    
    private String template(String template) {
        this.template = template;
        return getTemplate();
    }
    
    protected Result status(int statusCode) {
        params.put("APP", app);
        global.put("APP", app);
        params.put("app", app);
        global.put("app", app);
        
        Result result = new Result(statusCode);
        result.setParams(params, global);
        result.setTemplate(getTemplate());
        
        return result;
    }
    
    protected Result redirect(String url) {
        return status(Result.SC_303_SEE_OTHER).link(url);
    }
    
    protected Result ok() {
        return status(Result.SC_200_OK);
    }

    protected Result notFound() {
        return status(Result.SC_404_NOT_FOUND);
    }

    protected Result forbidden() {
        return status(Result.SC_403_FORBIDDEN);
    }

    protected Result badRequest() {
        return status(Result.SC_400_BAD_REQUEST);
    }

    protected Result internalServerError() {
        return status(Result.SC_500_INTERNAL_SERVER_ERROR);
    }
    
    protected Result text() {
        return status(Result.SC_200_OK).renderable(false).text();
    }
    
    protected Result text(Object content) {
        return text().content(content.toString());
    }
    
    protected Result html() {
        return status(Result.SC_200_OK).html();
    }
    
    protected Result html(String template) {
        return html().template(template(template)); // triple template :)
    }

    protected Result json() {
        return status(Result.SC_200_OK).renderable(false).json();
    }

    protected Result json(Object content) {
        return json().content(toJson(content));
    }

    protected Result xml() {       
        return status(Result.SC_200_OK).xml();
    }
    
    protected static String toJson(Object obj) {
        return new Gson().toJson(obj);
    }
}