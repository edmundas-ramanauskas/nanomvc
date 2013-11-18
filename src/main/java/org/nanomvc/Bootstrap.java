package org.nanomvc;

import org.nanomvc.utils.HibernateUtil;
import org.nanomvc.mvc.Router;
import org.nanomvc.http.Request;
import org.nanomvc.http.RequestHandler;
import org.nanomvc.exceptions.ControllerException;
import java.io.File;
import java.io.IOException;
import java.lang.reflect.Constructor;
import java.lang.reflect.InvocationTargetException;
import java.lang.reflect.Method;
import java.lang.annotation.Annotation;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.Iterator;
import java.util.List;
import java.util.Map;
import java.util.Map.Entry;
import javax.servlet.RequestDispatcher;
import javax.servlet.ServletConfig;
import javax.servlet.ServletContext;
import javax.servlet.ServletException;
//import javax.servlet.annotation.MultipartConfig;
import javax.servlet.http.HttpServlet;
import javax.servlet.http.HttpServletRequest;
import javax.servlet.http.HttpServletResponse;
import org.apache.commons.fileupload.FileItem;
import org.apache.commons.fileupload.FileItemFactory;
import org.apache.commons.fileupload.disk.DiskFileItemFactory;
import org.apache.commons.fileupload.servlet.ServletFileUpload;
import org.apache.log4j.BasicConfigurator;
import org.json.JSONException;
import org.json.JSONObject;
import org.nanomvc.mvc.Result;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

//@MultipartConfig(location = "/tmp")
public class Bootstrap extends HttpServlet
{
    private static Logger _log = LoggerFactory.getLogger(Bootstrap.class);
    
    private static final String ActionPrefix = "do";
    private static final String DefaultAction = "index";
    private static final String InitMethod = "init";
    private static final String ConfigMethod = "config";
    private static final String FlushMethod = "flush";
    private static final String RoutesMethod = "routes";
    
    protected Map<String, Object> files;
    protected Map<String, String> fields;
    
    private String viewsPath;
    private String controllersPath;
    private String defaultController;
    private String routerClass;
    private StringBuffer output;
    private Long startTime;

    public void init(ServletConfig config)
            throws ServletException {
        this.viewsPath = config.getInitParameter("viewsPath");
        this.controllersPath = config.getInitParameter("controllersPath");
        this.defaultController = config.getInitParameter("defaultController");
        this.routerClass = config.getInitParameter("routerClass");
        super.init(config);
        try {
            File file = new File(getServletContext().getRealPath("/WEB-INF/conf/hibernate.cfg.xml"));
            HibernateUtil.setConfigurationFile(file);
        } catch (Exception ex) {
            _log.warn("/WEB-INF/conf/hibernate.cfg.xml is missing");
        }
    }

    private void parseMultipartData(HttpServletRequest request) {
        try {
            Boolean isMultipart = ServletFileUpload.isMultipartContent(request);
            if(isMultipart) {
                FileItemFactory factory = new DiskFileItemFactory();
                ServletFileUpload upload = new ServletFileUpload(factory);
                try {
                    List items = upload.parseRequest(request);
                    Iterator iterator = items.iterator();
                    while (iterator.hasNext()) {
                        FileItem item = (FileItem) iterator.next();
                        if (!item.isFormField()) {
                            if (this.files == null) {
                                this.files = new HashMap();
                            }
                            String field = item.getFieldName();
                            if (this.files.containsKey(field)) {
                                if ((this.files.get(field) instanceof List)) {
                                    List files = (List) this.files.get(field);
                                    files.add(item);
                                } else {
                                    List files = new ArrayList();
                                    files.add(item);
                                    this.files.put(field, files);
                                }
                            } else {
                                this.files.put(item.getFieldName(), item);
                            }
                        } else {
                            if (this.fields == null) {
                                this.fields = new HashMap();
                            }
                            this.fields.put(item.getFieldName(), item.getString("UTF-8"));
                        }
                    }
                } catch (Throwable e) {
                    _log.error("exception", e);
                }
            } else {
                this.fields = null;
                this.files = null;
            }
        } catch (Throwable ex) {
            _log.error("exception", ex);
        }
    }

    protected void processRequest(HttpServletRequest request, HttpServletResponse response) throws ServletException, IOException {
        BasicConfigurator.configure();
        
        startTime = System.currentTimeMillis();
        output = null;
        String path = request.getServletPath();

        String controllerClassName = null;
        String controllerMethodName = null;

        RequestHandler handler = new RequestHandler(path, this.routerClass);
        Request req = handler.parseRequest();
        req.setDefaultController(defaultController);
        
        parseMultipartData(request);

        controllerClassName = new StringBuilder().append(controllersPath).append(".").append(req.getControllerClassName()).toString();
        controllerMethodName = req.getControllerMethodName();
        List args = req.getArguments();
        try {
            ClassLoader classLoader = getClass().getClassLoader();

            Class cc = classLoader.loadClass(controllerClassName);
            Constructor constructor = cc.getConstructor(new Class[0]);
            Object co = constructor.newInstance(new Object[0]);
            Method method = null;
            try {
                method = cc.getMethod("config", new Class[]{HttpServletRequest.class, HttpServletResponse.class, ServletContext.class, String.class, String.class, String.class, Router.class, Map.class, Map.class});

                method.invoke(co, new Object[]{request, response, getServletContext(), this.viewsPath, req.getController(), req.getAction(), handler.getRouter(), this.files, this.fields});

                method = cc.getMethod("init", new Class[0]);
                method.invoke(co, new Object[0]);

                Method[] allMethods = cc.getMethods();
                method = null;
                Class[] params = null;
                for (Method m : allMethods) {
                    if (m.getName().equalsIgnoreCase(controllerMethodName)) {
                        params = m.getParameterTypes();
                        method = cc.getDeclaredMethod(m.getName(), params);
                        break;
                    }
                }
                controllerMethodName = method.getName();

                if (method == null) {
                    throw new NoSuchMethodException();
                }
                
//                Annotation[][] annotations = method.getParameterAnnotations();
                Object[] arguments = new Object[params.length];
                for (int i = 0; i < params.length; i++) {
                    try {
                        switch (params[i].getSimpleName()) {
                            case "String":
                                arguments[i] = ((String) args.get(i));
                                break;
                            case "Integer":
                                arguments[i] = Integer.valueOf((String) args.get(i));
                                break;
                            case "Long":
                                arguments[i] = Long.valueOf((String) args.get(i));
                                break;
                            case "Float":
                                arguments[i] = Float.valueOf((String) args.get(i));
                                break;
                            case "Double":
                                arguments[i] = Double.valueOf((String) args.get(i));
                                break;
                            case "Boolean":
                                arguments[i] = Boolean.valueOf((String) args.get(i));
                                break;
                            default:
//                                Annotation annotation = null;
//                                if(annotations[i] != null) {
//                                    for(int j = 0; j < annotations[i].length; j++) {
//                                        if(annotations[i][j].annotationType().equals(Extracted.class)) {
//                                            annotation = annotations[i][j];
//                                            break;
//                                        }
//                                    }
//                                }
//                                if(annotation != null) {
//                                    Class obClass = classLoader.loadClass(params[i].getCanonicalName());
//                                    Constructor obConst = obClass.getConstructor(new Class[0]);
//                                    Object ob = obConst.newInstance(new Object[0]);
//                                    org.apache.commons.beanutils.BeanUtils.populate(ob, request.getParameterMap());
//                                    arguments[i] = ob;
//                                } else
                                    arguments[i] = args.get(i);
                        }
                    } catch (Exception ex) {
                        arguments[i] = null;
                    }
                }

                method = cc.getDeclaredMethod(controllerMethodName, params);

                Result result = (Result) method.invoke(co, arguments);
                buildResponse(request, response, result);
//                method = cc.getMethod("flush", new Class[0]);
//                method.setAccessible(true);
//                method.invoke(co, new Object[0]);

                co = null;
                cc = null;
            } catch (NoSuchMethodException | IllegalArgumentException | IllegalAccessException e) {
                _log.error(new StringBuilder().append("Undefined action: ").append(path).toString());
                _log.error(e.toString());
                error(
                        new StringBuilder().append("Undefined action: ")
                            .append(req.getAction()).append(" in <b>")
                            .append(controllerClassName).append("</b>").toString()
                        , request, response);
            } catch (InvocationTargetException e) {
                if ((e.getCause() instanceof ControllerException)) {
                    error(
                            new StringBuilder().append("Error: ").append(e.getCause().getMessage()).toString()
                            , request, response);
                } else {
                    error(e.getCause(), request, response);
                }
            } catch (Exception e) {
                error(e, request, response);
            }
        } catch (InvocationTargetException ex) {
            if ((ex.getCause() instanceof ControllerException)) {
                error(
                        new StringBuilder().append("Error: ")
                            .append(ex.getCause().getMessage()).toString()
                        , request, response);
            } else {
                error(ex.getCause(), request, response);
            }
        } catch (ClassNotFoundException | IllegalArgumentException | IllegalAccessException | InstantiationException | SecurityException | NoSuchMethodException ex) {
            _log.error(new StringBuilder().append("Undefined controller: ").append(path).toString());
            _log.error(ex.toString());
            error(
                    new StringBuilder().append("Undefined controller: ")
                        .append(req.getControllerClassName()).toString()
                    , request, response);
        }
        flush(response);
//        log(request);
    }
    
    private void buildResponse(HttpServletRequest request, HttpServletResponse response, Result result) {
        response.setStatus(result.getStatusCode());
        for(Entry<String, String> entry : result.getHeaders().entrySet()) {
            response.setHeader(entry.getKey(), entry.getValue());
        }
        if(result.isRenderable()) {
            Renderer renderer = new VelocityRenderer();
            renderer.configure(getServletContext(), viewsPath, result.getParams());
            output(renderer.render(result.getTemplate()));
        } else {
            output(result.getContent().toString());
        }
    }

    private void log(HttpServletRequest request) {
        try {
            Long time = Long.valueOf(System.currentTimeMillis() - this.startTime);
            String path = request.getServletPath();
            if (time > 100) {
                try {
                    JSONObject req = new JSONObject();
                    JSONObject arr = new JSONObject();
                    arr.put("path", path);
                    arr.put("time", time);
                    req.put("request", arr);
                    _log.info(req.toString());
                } catch (JSONException je) {

                }
            }
        } catch (Exception e) {

        }
    }

    private void error(Throwable error, HttpServletRequest request, HttpServletResponse response) {
        StringBuilder message = new StringBuilder();
        message.append("Exception: ").append(error.toString()).append("<br />\n");

        if (error.getMessage() != null) {
            message.append(error.getMessage()).append("<br />\n");
        }
        message.append("<br />\n");

        StackTraceElement[] trace = error.getStackTrace();
        for (int i = 0; i < trace.length; i++) {
            message.append(trace[i]).append("<br />\n");
        }
        error(message.toString(), request, response);
    }

    private void error(String error, HttpServletRequest request, HttpServletResponse response) {
        StringBuilder sb = new StringBuilder();
        sb.append("<!DOCTYPE html>\n");
        sb.append("<html>\n");
        sb.append("<head>\n");
        sb.append("<meta http-equiv=\"Content-Type\" content=\"text/html; charset=UTF-8\">\n");
        sb.append("<title>Error</title>\n");
        sb.append("<link rel=\"stylesheet\" type=\"text/css\" media=\"all\""
                + " href=\"//netdna.bootstrapcdn.com/bootstrap/3.0.0/css/bootstrap.min.css\" />\n");
        sb.append("</head>\n");
        sb.append("<body style=\"padding-top: 10px;\">\n");
        sb.append("<div class=\"container\"><div class=\"alert alert-danger\">\n");
        sb.append(error + "\n");
        sb.append("</div></div>\n");
        sb.append("</body>\n");
        sb.append("</html>\n");
        output(sb.toString());
    }

    private void flush(HttpServletResponse response) throws IOException {
        if (output == null) {
            try {
                response.getWriter().flush();
                response.getWriter().close();
            } catch(Exception e) {
                _log.error(e.getMessage());
            }
            return;
        }
        try {
            response.getWriter().print(output.toString());
            response.getWriter().flush();
            response.getWriter().close();
        } catch(Exception e) {
            _log.error(e.getMessage());
        }
    }

    protected final void output(String value) {
        if (this.output == null) {
            this.output = new StringBuffer();
        }
        output.append(value);
    }

    protected void doGet(HttpServletRequest request, HttpServletResponse response)
            throws ServletException, IOException {
        processRequest(request, response);
    }

    protected void doPost(HttpServletRequest request, HttpServletResponse response)
            throws ServletException, IOException {
        processRequest(request, response);
    }

    public String getServletInfo() {
        return "";
    }
}