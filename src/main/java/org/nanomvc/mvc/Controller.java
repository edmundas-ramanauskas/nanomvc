package org.nanomvc.mvc;

import com.google.gson.Gson;
import java.io.IOException;
import java.io.InputStream;
import java.lang.reflect.InvocationTargetException;
import java.lang.reflect.Type;
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
import org.nanomvc.http.LocalFile;
import org.nanomvc.utils.FileUtil;
import org.nanomvc.utils.RequestUtil;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

public abstract class Controller
{
    private static final Logger _log = LoggerFactory.getLogger(Controller.class);
    
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
        // override for initialization stuff
    }

    protected String getPath() {
        return context.getRealPath(SLASH);
    }

    public void thumb(String image, String path, int width, int height, int method) {
        try {
            FileUtil.thumb(image, getImagesPath(path), width, height, method);
        } catch (IOException ex) {
            _log.error("Controller.thumb", ex);
            throw new RuntimeException(ex);
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
            _log.error("Controller.saveImageFromUrl", ex);
            throw new RuntimeException(ex);
        }
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
            _log.error("Controller.handleFileUpload", ex);
            throw new RuntimeException(ex);
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
            List tFiles = new ArrayList();

            Integer i = 0;
            for (InputStream stream : streamList) {
                i++;
                String name = UUID.randomUUID().toString();
                if(fileName != null)
                    name = UUID.randomUUID().toString();
                String file = RequestUtil.saveImage(stream, context.getRealPath(path), name);
                if (file != null) {
                    tFiles.add(new LocalFile(path, pathName, file));
                }
            }

            return tFiles;
        } catch (Exception ex) {
            _log.error("Controller.handleMultipleFilesUpload", ex);
            throw new RuntimeException(ex);
        }
    }

    private String getLocalDate() {
        Locale lt = new Locale("lt", "LT");
        SimpleDateFormat formatter = new SimpleDateFormat("yyyy-MM-dd", lt);
        return formatter.format(new Date());
    }
    
    protected final String clean(String input) {
        return Jsoup.parse(input).text();
    }
    
    /**
     * Set current template
     * @param template 
     */
    protected void setTemplate(String template) {
        this.template = template;
    }
    
    /**
     * Assign value to template and layout template
     * @param key
     * @param value 
     */
    protected final void assignToAll(String key, Object value) {
        assign(key, value);
        assignToMain(key, value);
    }
    
    /**
     * Assign value to layout template
     * @param key
     * @param value 
     */
    protected final void assignToMain(String key, Object value) {
        key = (key.startsWith("main")) ? key : "main." + key;
        assign(key, value);
    }

    /**
     * Assign value to template
     * @param key
     * @param value 
     */
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
            _log.error("Controller.getFile", ex);
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
        } catch (Exception ex) {
            _log.error("Controller.getFile", ex);
            throw new RuntimeException(ex);
        }
        return null;
    }

    protected final String getParam(String key) {
        if ((fields != null) && (fields.containsKey(key))) {
            return (String) fields.get(key);
        }
        return request.getParameter(key);
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

    protected final <T> T storeGet(String key, T value) {
        T obj = (T) storeGet(key);
        return (obj == null) ? value : obj;
    }

    protected final Object storeGet(String key) {
        return request.getSession().getAttribute(key);
    }

    /**
     * Clear user session storage
     */
    protected final void storeClear() {
        request.getSession().invalidate();
    }

    protected final String getUrlPath() {
        return request.getServletPath();
    }

    /**
     * Get application base URL
     * @return 
     */
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

    /**
     * Get current URL without query string
     * @return 
     */
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

    /**
     * Get current URL with query string
     * @return 
     */
    protected final String getCurrentUrlFull() {
        String queryString = request.getQueryString() != null ? 
                request.getQueryString() : EMPTY;
        return new StringBuilder().append(getUrlPath()).append(queryString).toString();
    }

    /**
     * Create URL
     * @param controller
     * @param action
     * @param params
     * @return 
     */
    protected final String createUrl(String controller, String action, Object... params) {
        String route = new StringBuilder().append(controller.substring(0, 1)
                .toUpperCase()).append(controller.substring(1).toLowerCase())
                .append(".").append(action.toLowerCase()).toString();

        String url = new StringBuilder().append(SLASH).append(controller)
                .append(SLASH).append(action).toString();
        if (router.reverseRoutes().containsKey(route)) {
            url = (String) router.reverseRoutes().get(route);
        }
        String result = new StringBuilder().append(getBaseUrl()).append(url)
                .append(params != null ? new StringBuilder().append(SLASH)
                        .append(StringUtils.join(params, SLASH))
                        .toString() : EMPTY).toString();
        
        return result.endsWith("//") ? result.substring(0, result.length()-1) : result;
    }

    /**
     * Check if request is AJAX
     * @return 
     */
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
            _log.error("Controller.parseData", ex);
            throw new RuntimeException(ex);
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
    
    /**
     * Generate redirect response
     * @param controller
     * @param action
     * @param params
     * @return 
     */
    protected Result redirect(String controller, String action, Object... params) {
        return status(Result.SC_303_SEE_OTHER).link(createUrl(controller, action, params));
    }
    
    /**
     * Generate redirect response
     * @param controller
     * @param action
     * @return 
     */
    protected Result redirect(String controller, String action) {
        return status(Result.SC_303_SEE_OTHER).link(createUrl(controller, action));
    }
    
    /**
     * Generate redirect response
     * @param url
     * @return 
     */
    protected Result redirect(String url) {
        return status(Result.SC_303_SEE_OTHER).link(url);
    }
    
    /**
     * Generate "ok" response
     * @return 
     */
    protected Result ok() {
        return status(Result.SC_200_OK);
    }

    /**
     * Generate "not found" response
     * @return 
     */
    protected Result notFound() {
        return status(Result.SC_404_NOT_FOUND);
    }

    /**
     * Generate "forbidden" response
     * @return 
     */
    protected Result forbidden() {
        return status(Result.SC_403_FORBIDDEN);
    }

    /**
     * Generate "bad request" response
     * @return 
     */
    protected Result badRequest() {
        return status(Result.SC_400_BAD_REQUEST);
    }

    /**
     * Generate "internal server error" response
     * @return 
     */
    protected Result internalServerError() {
        return status(Result.SC_500_INTERNAL_SERVER_ERROR);
    }
    
    /**
     * Generate plain text response
     * @return 
     */
    protected Result text() {
        return status(Result.SC_200_OK).renderable(false).text();
    }
    
    /**
     * Generate plain text response
     * @param content
     * @return 
     */
    protected Result text(Object content) {
        return text().content(content.toString());
    }
    
    /**
     * Generate HTML response
     * @return 
     */
    protected Result html() {
        return status(Result.SC_200_OK).html();
    }
    
    /**
     * Generate HTML response
     * @param template
     * @return 
     */
    protected Result html(String template) {
        return html().template(template(template)); // triple template :)
    }

    /**
     * Generate JSON response
     * @return 
     */
    protected Result json() {
        return status(Result.SC_200_OK).renderable(false).json();
    }

    /**
     * Generate JSON response
     * @param content
     * @return 
     */
    protected Result json(Object content) {
        return json().content(toJson(content));
    }

    /**
     * Generate XML response
     * @return 
     */
    protected Result xml() {       
        return status(Result.SC_200_OK).xml();
    }
    
    /**
     * Convert object to JSON
     * @param object
     * @return 
     */
    protected static String toJson(Object object) {
        return new Gson().toJson(object);
    }
    
    protected static <T> T fromJson(String json, Type type) {
        return new Gson().fromJson(json, type);
    }
    
    protected static <T> T fromJson(String json, Class<T> theClass) {
        return new Gson().fromJson(json, theClass);   
    }
}