package org.nanomvc.mvc;

import com.google.gson.Gson;
import org.nanomvc.Application;
import org.nanomvc.http.LocalFile;
import org.nanomvc.utils.RequestUtil;
import java.awt.image.BufferedImage;
import java.io.BufferedWriter;
import java.io.File;
import java.io.IOException;
import java.io.InputStream;
import java.io.PrintWriter;
import java.io.StringWriter;
import java.lang.reflect.Array;
import java.lang.reflect.Field;
import java.lang.reflect.InvocationTargetException;
import java.lang.reflect.Method;
import java.lang.reflect.Modifier;
import java.math.BigDecimal;
import java.text.DecimalFormat;
import java.text.DecimalFormatSymbols;
import java.text.ParseException;
import java.text.SimpleDateFormat;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.Date;
import java.util.Enumeration;
import java.util.HashMap;
import java.util.HashSet;
import java.util.List;
import java.util.Locale;
import java.util.Map;
import java.util.Properties;
import java.util.Set;
import java.util.UUID;
import java.util.concurrent.ExecutorService;
import java.util.logging.Level;
import javax.imageio.ImageIO;
import javax.imageio.stream.ImageInputStream;
import javax.servlet.RequestDispatcher;
import javax.servlet.ServletContext;
import javax.servlet.ServletException;
import javax.servlet.http.HttpServletRequest;
import javax.servlet.http.HttpServletResponse;
import javax.servlet.http.HttpSession;
import org.apache.commons.beanutils.BeanUtils;
import org.apache.commons.fileupload.FileItem;
import org.apache.commons.lang.StringUtils;
import org.apache.velocity.Template;
import org.apache.velocity.VelocityContext;
import org.apache.velocity.app.VelocityEngine;
import org.apache.velocity.exception.ParseErrorException;
import org.apache.velocity.exception.ResourceNotFoundException;
import org.imgscalr.Scalr;
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

    public final void config(HttpServletRequest request, HttpServletResponse response, ServletContext context, String viewsPath, String controller, String action, Router router, Map files, Map fields) {
        this.request = request;
        this.response = response;
        this.context = context;
        this.session = request.getSession();
        this.files = files;
        this.fields = fields;

        this.router = router;

        this.viewsPath = (viewsPath.endsWith("/") ? viewsPath : new StringBuilder().append(viewsPath).append("/").toString());
        this.controller = controller;
        this.action = action;
        this.template = action;

        this.app = new Application(getBaseUrl(), getCurrentUrl(), router, request.getSession());
        
        this.params = new HashMap();
        this.global = new HashMap();
    }

    public void init() {
    }

    protected String getPath() {
        return context.getRealPath("/");
    }

    protected void call(String action) {
        if (action != null) {
            this.action = action;
            String actionName = new StringBuilder().append("do").append(action.substring(0, 1).toUpperCase()).append(action.substring(1).toLowerCase()).toString();
            try {
                String path = this.request.getServletPath();
                List args = null;

                if (!path.equals("/")) {
                    List parts = Arrays.asList(path.split("/"));
                    parts = parts.subList(1, parts.size());
                    switch (parts.size()) {
                        case 0:
                        case 1:
                        case 2:
                            break;
                        default:
                            args = parts.subList(2, parts.size());
                    }

                }

                Method[] allMethods = getClass().getDeclaredMethods();
                for (Method m : allMethods) {
                    if (m.getName().equals(actionName)) {
                        Class[] params = m.getParameterTypes();
                        Object[] arguments = new Object[params.length];
                        for (int i = 0; i < params.length; i++) {
                            try {
                                arguments[i] = args.get(i);
                            } catch (Exception ex) {
                                arguments[i] = null;
                            }
                        }

                        Method method = getClass().getMethod(actionName, params);

                        if (params.length == 0) {
                            method.invoke(this, new Object[0]);
                        } else {
                            method.invoke(this, arguments);
                        }
                    }
                }
            } catch (NoSuchMethodException ex) {
            } catch (SecurityException ex) {
            } catch (IllegalAccessException ex) {
            } catch (IllegalArgumentException ex) {
            } catch (InvocationTargetException ex) {
            }
        }
    }

    public void thumb(String image, String path, int width, int height, int method) {
        try {
            FileUtil.thumb(image, getImagesPath(path), width, height, method);
        } catch (IOException ex) {

        }
    }

    protected final String saveImageFromUrl(String url, String filename) {
        return saveImageFromUrl(url, filename, "");
    }

    protected final String saveImageFromUrl(String url, String filename, String path) {
        try {
            if (path == null) {
                path = "";
            } else if ((!path.equals("")) && (!path.startsWith("/"))) {
                path = new StringBuilder().append("/").append(path).toString();
            }
            return RequestUtil.saveImage(url, new StringBuilder().append(getPath()).append("/public/upl/").append(this.controller).append("/images").append(path).toString(), filename);
        } catch (IOException ex) {
        }
        return null;
    }

    protected final String getImagesPath() {
        return getImagesPath("");
    }

    protected final String getImagesPath(String path) {
        if (path == null) {
            path = "";
        } else if ((path != null) && (!path.equals("")) && (!path.startsWith("/"))) {
            path = new StringBuilder().append("/").append(path).toString();
        }
        return new StringBuilder().append(getPath()).append("/public/upl/").append(this.controller).append("/images").append(path).toString();
    }

    protected final String getImagePath(String filename) {
        return getImagePath(filename, "");
    }

    protected final String getImagePath(String filename, String path) {
        if (path == null) {
            path = "";
        } else if ((path != null) && (!path.equals("")) && (!path.startsWith("/"))) {
            path = new StringBuilder().append("/").append(path).toString();
        }
        return new StringBuilder().append(getPath()).append("/public/upl/").append(this.controller).append("/images").append(path).append("/").append(filename).toString();
    }

    protected final String getImageUrl(String filename) {
        return getImageUrl(filename, "");
    }

    protected final String getImageUrl(String filename, String path) {
        if (path == null) {
            path = "";
        } else if ((path != null) && (!path.equals("")) && (!path.startsWith("/"))) {
            path = new StringBuilder().append("/").append(path).toString();
        }
        return new StringBuilder().append("/public/upl/").append(controller)
                .append("/images").append(path).append("/").append(filename)
                .toString();
    }

    protected final LocalFile handleFileUpload(String fieldName) {
        return handleFileUpload(fieldName, null, UUID.randomUUID().toString());
    }

    protected final LocalFile handleFileUpload(String fieldName, String fileName) {
        return handleFileUpload(fieldName, null, fileName);
    }

    protected final LocalFile handleFileUpload(String fieldName, String pathName, String fileName) {
        try {
            if (pathName == null) {
                pathName = "";
            }
            if (!pathName.equals("")) {
                pathName = pathName.startsWith("/") ? pathName : new StringBuilder().append("/").append(pathName).toString();
                pathName = pathName.endsWith("/") ? pathName : new StringBuilder().append(pathName).append("/").toString();
            } else {
                pathName = "/";
            }
            pathName = new StringBuilder().append(pathName).append(getLocalDate()).toString();

            InputStream is = getFile(fieldName);
            String path = new StringBuilder().append("/public/upl/").append(this.controller).append("/images").append(pathName).toString();
            String file = RequestUtil.saveImage(is, this.context.getRealPath(path), fileName);
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

    protected final List<LocalFile> handleMultipleFilesUpload(String fieldName, String fileName) {
        return handleMultipleFilesUpload(fieldName, null, fileName);
    }

    protected final List<LocalFile> handleMultipleFilesUpload(String fieldName, String pathName, String fileName) {
        try {
            if (pathName == null) {
                pathName = "";
            }
            if (!pathName.equals("")) {
                pathName = pathName.startsWith("/") ? pathName : new StringBuilder().append("/").append(pathName).toString();
                pathName = pathName.endsWith("/") ? pathName : new StringBuilder().append(pathName).append("/").toString();
            } else {
                pathName = "/";
            }
            pathName = new StringBuilder().append(pathName).append(getLocalDate()).toString();

            List<InputStream> streamList = getFiles(fieldName);
            if (streamList == null) {
                return null;
            }
            String path = new StringBuilder().append("/public/upl/").append(this.controller).append("/images").append(pathName).toString();
            List files = new ArrayList();

            Integer i = 0;
            for (InputStream stream : streamList) {
                i++;
                String name = UUID.randomUUID().toString();
                if(fileName != null)
                    name = UUID.randomUUID().toString();
                String file = RequestUtil.saveImage(stream, this.context.getRealPath(path), name);
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

    protected final void assign(String key, Object value) {
        if (key.startsWith("main.")) {
            if (this.global == null) {
                this.global = new HashMap();
            }
            this.global.put(key.substring("main.".length()), value);
        } else {
            if (this.params == null) {
                this.params = new HashMap();
            }
            this.params.put(key, value);
        }
    }

    protected final InputStream getFile(String name) {
        try {
            if ((this.files != null) && (this.files.containsKey(name))) {
                return ((FileItem) this.files.get(name)).getInputStream();
            }
        } catch (Exception ex) {
            _log.error("file not found", ex);
            throw new RuntimeException(ex);
        }
        return null;
    }

    protected final List<InputStream> getFiles(String name) {
        try {
            if ((this.files != null) && (this.files.containsKey(name))) {
                List list = new ArrayList();
                if ((this.files.get(name) instanceof List)) {
                    List<FileItem> files = (List) this.files.get(name);
                    for (FileItem file : files) {
                        list.add(file.getInputStream());
                    }
                    return list;
                }
                FileItem file = (FileItem) this.files.get(name);
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
        return this.request.getParameterValues(key);
    }

    protected final void storeSet(String key, Object value) {
        this.request.getSession().setAttribute(key, value);
    }

    protected final Object storeGet(String key) {
        return this.request.getSession().getAttribute(key);
    }

    protected final void storeClear() {
        this.request.getSession().invalidate();
    }

    protected final String getUrlPath() {
        return this.request.getServletPath();
    }

    protected final String getBaseUrl() {
        if ((this.request.getServerPort() == 80) || (this.request.getServerPort() == 443)) {
            return new StringBuilder().append(this.request.getScheme()).append("://").append(this.request.getServerName()).append(this.request.getContextPath()).toString();
        }

        return new StringBuilder().append(this.request.getScheme()).append("://").append(this.request.getServerName()).append(":").append(this.request.getServerPort()).append(this.request.getContextPath()).toString();
    }

    protected final String getCurrentUrl() {
        String requestUri = this.request.getRequestURI() != null ? this.request.getRequestURI() : "";
        if ((this.request.getServerPort() == 80) || (this.request.getServerPort() == 443)) {
            return new StringBuilder().append(this.request.getScheme()).append("://").append(this.request.getServerName()).append(requestUri).toString();
        }

        return new StringBuilder().append(this.request.getScheme()).append("://").append(this.request.getServerName()).append(":").append(this.request.getServerPort()).append(requestUri).toString();
    }

    protected final String getCurrentUrlFull() {
        String queryString = this.request.getQueryString() != null ? this.request.getQueryString() : "";
        return new StringBuilder().append(getUrlPath()).append(queryString).toString();
    }
    
    protected final String createUrl(String controller, String action) {
        return createUrl(controller, action, null);
    }

    protected final String createUrl(String controller, String action, Object... params) {
        String route = new StringBuilder().append(controller.substring(0, 1).toUpperCase()).append(controller.substring(1).toLowerCase()).append(".").append(action.toLowerCase()).toString();

        String url = new StringBuilder().append("/").append(controller).append("/").append(action).toString();
        if (this.router.reverseRoutes().containsKey(route)) {
            url = (String) this.router.reverseRoutes().get(route);
        }
        return new StringBuilder().append(getBaseUrl()).append(url).append(params != null ? new StringBuilder().append("/").append(StringUtils.join(params, "/")).toString() : "").toString();
    }

    protected final Boolean isAjax() {
        String xReq = request.getHeader("X-Requested-With");
        return Boolean.valueOf((xReq != null) && (xReq.toLowerCase().startsWith("xmlhttprequest")));
    }

    protected final Boolean isEmpty(String value) {
        return Boolean.valueOf((value == null) || (value.equals("")));
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
        return new StringBuilder().append(controller).append("/")
                .append(this.template).append(!this.template.endsWith(".htm") ? ".htm" : "")
                .toString();
    }
    
    private String getTemplate(String template) {
        this.template = template;
        return getTemplate();
    }
    
    public Result status(int statusCode) {
        params.put("APP", app);
        global.put("APP", app);
        
        Result result = new Result(statusCode);
        result.setParams(params, global);
        result.setTemplate(getTemplate());
        
        return result;
    }
    
    public Result redirect(String url) {
        return status(Result.SC_303_SEE_OTHER).link(url);
    }
    
    public Result ok() {
        return status(Result.SC_200_OK);
    }

    public Result notFound() {
        return status(Result.SC_404_NOT_FOUND);
    }

    public Result forbidden() {
        return status(Result.SC_403_FORBIDDEN);
    }

    public Result badRequest() {
        return status(Result.SC_400_BAD_REQUEST);
    }

    public Result internalServerError() {
        return status(Result.SC_500_INTERNAL_SERVER_ERROR);
    }
    
    public Result text() {
        return status(Result.SC_200_OK).renderable(false).text();
    }
    
    public Result text(Object content) {
        return text().content(content.toString());
    }
    
    public Result html() {
        return status(Result.SC_200_OK).html();
    }
    
    public Result html(String template) {
        return html().template(getTemplate(template));
    }

    public Result json() {
        return status(Result.SC_200_OK).renderable(false).json();
    }

    public Result json(Object content) {
        return json().content(toJson(content));
    }

    public Result xml() {       
        return status(Result.SC_200_OK).xml();
    }
    
    protected static String toJson(Object obj) {
        return new Gson().toJson(obj);
    }
}