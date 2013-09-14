package com.nanomvc;

import com.nanomvc.util.RequestUtil;
import java.awt.image.BufferedImage;
import java.awt.image.BufferedImageOp;
import java.io.BufferedWriter;
import java.io.File;
import java.io.IOException;
import java.io.InputStream;
import java.io.PrintWriter;
import java.io.StringWriter;
import java.lang.reflect.InvocationTargetException;
import java.lang.reflect.Method;
import java.text.SimpleDateFormat;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.Date;
import java.util.HashMap;
import java.util.List;
import java.util.Locale;
import java.util.Map;
import java.util.Map.Entry;
import java.util.Properties;
import java.util.UUID;
import java.util.logging.Level;
import javax.imageio.ImageIO;
import javax.imageio.stream.ImageInputStream;
import javax.servlet.RequestDispatcher;
import javax.servlet.ServletContext;
import javax.servlet.ServletException;
import javax.servlet.http.HttpServletRequest;
import javax.servlet.http.HttpServletResponse;
import javax.servlet.http.HttpSession;
import org.apache.commons.fileupload.FileItem;
import org.apache.commons.lang.StringUtils;
import org.apache.velocity.Template;
import org.apache.velocity.VelocityContext;
import org.apache.velocity.app.VelocityEngine;
import org.apache.velocity.exception.ParseErrorException;
import org.apache.velocity.exception.ResourceNotFoundException;
import org.apache.velocity.exception.VelocityException;
import org.imgscalr.Scalr;
import org.imgscalr.Scalr.Mode;
import org.jsoup.Jsoup;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

public abstract class Controller {

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
    private static VelocityEngine veloEngine = null;
    private Boolean redirect = Boolean.valueOf(false);
    private StringBuffer output;
    private Map<String, Object> params;
    private Map<String, Object> global;
    private String contentType = "text/html; charset=UTF-8";
    private String title = "";
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

        this.app = new Application(getBaseUrl(), getCurrentUrl(), router, request.getSession());
    }

    public void init() {
    }

    protected String getPath() {
        return this.context.getRealPath("/");
    }

    public final void flush() {
        this.response.setContentType(this.contentType);
        this.files = null;
        this.fields = null;
        if (this.output == null) {
            return;
        }
        try {
            this.response.getWriter().print(this.output.toString());
            this.response.getWriter().flush();
        } catch (IOException ex) {
        }
    }

    protected final void output(String value) {
        if (this.output == null) {
            this.output = new StringBuffer();
        }
        this.output.append(value);
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
        File newImageFile = new File(getImagePath(image, new StringBuilder().append(path).append("/").append(width).append("x").append(height).toString()));
//        if (!newImageFile.exists()) {
            try {
                File dir = new File(getImagesPath(new StringBuilder().append(path).append("/").append(width).append("x").append(height).toString()));
                if (!dir.exists()) {
                    dir.mkdirs();
                }
                File oldImageFile = new File(getImagePath(image, path));
                ImageInputStream is = ImageIO.createImageInputStream(oldImageFile);
                BufferedImage srcImage = ImageIO.read(is);
                BufferedImage scaledImage = null;
                switch (method) {
                    case IMG_CROP:
                        scaledImage = Scalr.crop(srcImage, width, height);
                        break;
                    case IMG_RESIZE:
                    default:
                        scaledImage = Scalr.resize(srcImage, Scalr.Mode.AUTOMATIC, width, height);
                        break;
                    case IMG_RESIZE_CROP:
                        int rWidth = width;
                        int rHeight = height;
                        double fWidth = ((double)srcImage.getWidth() / ((double)srcImage.getHeight() / (double)height));
                        double fHeight = ((double)srcImage.getHeight() / ((double)srcImage.getWidth() / (double)width));
                        Scalr.Mode mode = Scalr.Mode.FIT_TO_WIDTH;
                        if (srcImage.getWidth() > srcImage.getHeight()) {
                            mode = Scalr.Mode.FIT_TO_HEIGHT;
                            if (width > fWidth) {
                                rHeight = (int)fHeight + ((fHeight > (int)fHeight) ? 1 : 0);
                            } else {
                                
                            }
                        } else if (height > fHeight) {
                            rWidth = (int)fWidth + ((fWidth > (int)fWidth) ? 1 : 0);
                        }
                        scaledImage = Scalr.resize(srcImage, mode, rWidth, rHeight);
                        scaledImage = Scalr.crop(scaledImage, width, height);
                }

                String format = RequestUtil.getImageFormat(RequestUtil.getMimeType(oldImageFile));
                if(ImageIO.write(scaledImage, format, newImageFile))
                    _log.info("resize success");
                else
                    _log.info("resize failed");
                scaledImage.flush();
                srcImage.flush();
            } catch (IOException ex) {
//                _log.error(ex.toString());
            }
//        }
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
        return new StringBuilder().append("/public/upl/").append(this.controller).append("/images").append(path).append("/").append(filename).toString();
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

    protected final void render() {
        render(this.action);
    }

    protected final void render(String view) {
        if ((this.redirect.booleanValue()) || (this.output != null)) {
            return;
        }
        this.output = null;

        VelocityEngine engine = getVeloEngine();
        VelocityContext context = new VelocityContext();

        view = new StringBuilder().append(this.controller).append("/").append(view).append(!view.endsWith(".htm") ? ".htm" : "").toString();

        Template tpl = null;
        try {
            tpl = engine.getTemplate("layout/main.htm");
        } catch (ResourceNotFoundException | ParseErrorException  ex) {
            _log.error(ex.toString());
        } catch(Exception ex) {
            
        }
        try {
            PrintWriter out = this.response.getWriter();
            Throwable localThrowable2 = null;
            try {
                BufferedWriter bw = new BufferedWriter(out);

                context = new VelocityContext();
                context.put("APP", this.app);
                context.put("esc", new org.apache.velocity.tools.generic.EscapeTool());
                if (this.global != null) {
                    for (Map.Entry entry : this.global.entrySet()) {
                        context.put((String) entry.getKey(), entry.getValue());
                    }
                }
                context.put("content", fetch(view, this.params));

                if (tpl != null) {
                    tpl.merge(context, bw);
                }
                bw.flush();
                bw.close();
            } catch (Throwable localThrowable1) {
                localThrowable2 = localThrowable1;
                throw localThrowable1;
            } finally {
                if (out != null) {
                    if (localThrowable2 != null) {
                        try {
                            out.close();
                        } catch (Throwable x2) {
                            localThrowable2.addSuppressed(x2);
                        }
                    } else {
                        out.close();
                    }
                }
            }
        } catch (IOException ex) {
            _log.error(ex.toString());
        }
    }

    private final VelocityEngine getVeloEngine() {
        if (veloEngine == null) {
            try {
                Properties properties = new Properties();
                try {
                    properties.setProperty("input.encoding", "UTF-8");
                    properties.setProperty("resource.loader", "webapp");
                    properties.setProperty("webapp.resource.loader.class", "org.apache.velocity.tools.view.WebappResourceLoader");
                    properties.setProperty("webapp.resource.loader.path", this.viewsPath);
                    properties.setProperty("resource.manager.defaultcache.size", "256");
                    properties.setProperty("webapp.resource.loader.cache", "false");
                    properties.setProperty("runtime.log.logsystem.class", "org.apache.velocity.runtime.log.Log4JLogChute");
                    properties.setProperty("runtime.log.logsystem.log4j.logger", VelocityEngine.class.getName());
                } catch (Exception ex) {
                    _log.error(ex.toString());
                }
                veloEngine = new VelocityEngine();
                veloEngine.setApplicationAttribute("javax.servlet.ServletContext", this.context);
                veloEngine.init(properties);
            } catch (Exception ex) {
                java.util.logging.Logger.getLogger(Controller.class.getName()).log(Level.SEVERE, null, ex);
            }
        }
        return veloEngine;
    }

    protected final String fetch(String view) {
        return fetch(view, this.params);
    }

    protected final String fetch(String view, Map<String, Object> params) {
        String result = null;

        VelocityEngine engine = getVeloEngine();
        VelocityContext context = new VelocityContext();

        view = new StringBuilder().append(view).append(!view.endsWith(".htm") ? ".htm" : "").toString();

        org.apache.velocity.tools.generic.EscapeTool esc = new org.apache.velocity.tools.generic.EscapeTool();
        
        context.put("APP", this.app);
        context.put("esc", new org.apache.velocity.tools.generic.EscapeTool());
        if (params != null) {
            for (Map.Entry entry : params.entrySet()) {
                context.put((String) entry.getKey(), entry.getValue());
            }
        }
        try {
            StringWriter sw = new StringWriter();

            engine.mergeTemplate(view, "UTF-8", context, sw);

            result = sw.toString();

            sw.close();
        } catch (IOException ex) {
            result = ex.toString();
        } catch (Exception ex) {
            result = ex.toString();
        }
        return result;
    }
    
    protected final String clean(String input) {
        return Jsoup.parse(input).text();
    }

    protected final void contentType(String value) {
        this.response.setContentType(value);
    }

    protected final void setContentType(String contentType) {
        this.contentType = contentType;
    }

    protected final void assign(String key, Object value) {
        if ((!this.redirect.booleanValue()) && (this.output == null)) {
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
    }

    protected final InputStream getFile(String name) {
        try {
            if ((this.files != null) && (this.files.containsKey(name))) {
                return ((FileItem) this.files.get(name)).getInputStream();
            }
        } catch (Exception e) {
            _log.error("file not found", e);
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

    protected final String[] getValues(String key) {
        return this.request.getParameterValues(key);
    }
    
    protected final void fillPostData(Object obj) {
        try {
            org.apache.commons.beanutils.BeanUtils.populate(obj, request.getParameterMap());
        } catch (IllegalAccessException | InvocationTargetException ex) {
            _log.error("fillPostData", ex);
        }
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

    protected final void redirect(String url) {
        this.output = null;
        this.redirect = Boolean.valueOf(true);
        try {
            if (url.startsWith("http")) {
                this.response.sendRedirect(url);
            } else if (url.startsWith("/")) {
                this.response.sendRedirect(new StringBuilder().append(getBaseUrl()).append(url).toString());
            } else {
                this.response.sendRedirect(new StringBuilder().append(getBaseUrl()).append("/").append(url).toString());
            }
        } catch (IOException ex) {
        }
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

    protected final String createUrl(String controller, String action, Object[] params) {
        String route = new StringBuilder().append(controller.substring(0, 1).toUpperCase()).append(controller.substring(1).toLowerCase()).append(".").append(action.toLowerCase()).toString();

        String url = new StringBuilder().append("/").append(controller).append("/").append(action).toString();
        if (this.router.reverseRoutes().containsKey(route)) {
            url = (String) this.router.reverseRoutes().get(route);
        }
        return new StringBuilder().append(getBaseUrl()).append(url).append(params != null ? new StringBuilder().append("/").append(StringUtils.join(params, "/")).toString() : "").toString();
    }

    protected final Boolean isAjax() {
        String xReq = this.request.getHeader("X-Requested-With");
        return Boolean.valueOf((xReq != null) && (xReq.toLowerCase().startsWith("xmlhttprequest")));
    }

    protected final Boolean isEmpty(String value) {
        return Boolean.valueOf((value == null) || (value.equals("")));
    }

    protected final void renderOld() {
        renderOld(this.action);
    }

    protected final void renderOld(String view) {
        if ((this.redirect.booleanValue()) || (this.output != null)) {
            return;
        }
        this.output = null;
        this.response.setContentType(this.contentType);
        view = new StringBuilder().append("/WEB-INF/views/").append(this.controller).append("/").append(view).append(!view.endsWith(".jsp") ? ".jsp" : "").toString();
        try {
            this.request.setAttribute("siteTitle", this.title);
            this.request.setAttribute("view", view);
            if (this.params != null) {
                for (Map.Entry entry : this.params.entrySet()) {
                    this.request.setAttribute((String) entry.getKey(), entry.getValue());
                }
            }
            RequestDispatcher dispatcher = this.request.getRequestDispatcher("/WEB-INF/views/layout/main.jsp");
            dispatcher.include(this.request, this.response);
        } catch (ServletException ex) {
            output("ServletException");
        } catch (IOException ex) {
            output("IOException");
        }
    }

    protected final void renderPart() {
        renderPart(this.action);
    }

    protected final void renderPart(String view) {
        if ((this.redirect.booleanValue()) || (this.output != null)) {
            return;
        }
        this.output = null;
        contentType(this.contentType);
        view = new StringBuilder().append("/WEB-INF/views/").append(this.controller).append("/").append(view).append(!view.endsWith(".jsp") ? ".jsp" : "").toString();
        try {
            RequestDispatcher dispatcher = this.request.getRequestDispatcher(view);
            dispatcher.forward(this.request, this.response);
        } catch (ServletException ex) {
        } catch (IOException ex) {
        }
    }
}