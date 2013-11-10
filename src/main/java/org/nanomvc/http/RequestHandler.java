package org.nanomvc.http;

import java.io.PrintStream;
import java.lang.reflect.Constructor;
import java.lang.reflect.Method;
import java.util.Arrays;
import java.util.List;
import java.util.Map;
import org.apache.commons.lang.StringUtils;
import org.nanomvc.mvc.Router;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

public class RequestHandler {
    
    private static Logger _log = LoggerFactory.getLogger(RequestHandler.class);

    private static final String RoutesMethod = "routes";
    private String path;
    private String router;
    private Router routerObject;

    public RequestHandler() {
    }

    public RequestHandler(String path, String router) {
        this.path = (path == null) ? "/" : path;
        this.router = router;
    }

    public Request parseRequest() {
        Request request = null;
        Map routes = null;
        String controller = null;
        String action = null;
        List args = null;
        if ((this.path.endsWith("/")) && (!this.path.equals("/"))) {
            this.path = this.path.substring(0, this.path.length() - 1);
        }
        try {
            if (this.router != null) {
                ClassLoader classLoader = getClass().getClassLoader();
                Class rClass = classLoader.loadClass(this.router);
                this.routerObject = ((Router) rClass.getConstructor(new Class[0]).newInstance(new Object[0]));
                Method method = rClass.getMethod("routes", new Class[0]);
                routes = (Map) method.invoke(this.routerObject, new Object[0]);
            }
        } catch (Throwable t) {
            
        }
        Boolean parse = true;
        if (routes != null) {
            String route = null;
            if (this.path.equals("/")) {
                route = this.path;
            } else if (routes.containsKey(this.path)) {
                route = this.path;
            } else {
                List parts = Arrays.asList(this.path.split("/"));
                parts = parts.subList(1, parts.size());
                for (int i = 0; i < parts.size(); i++) {
                    String tempRoute = "/" + StringUtils.join(parts.subList(0, parts.size() - i), "/");
                    if (routes.containsKey(tempRoute)) {
                        route = tempRoute;
                        try {
                            args = parts.subList(parts.size() - i, parts.size());
                        } catch (Exception e) {
                            System.out.println(e.toString());
                        }
                        break;
                    }
                }

            }

            if (route != null) {
                route = (String) routes.get(route);
                if (route != null) {
                    String[] rest = route.split("\\.");
                    controller = rest[0].toLowerCase();
                    action = rest[1].toLowerCase();
                    parse = Boolean.valueOf(false);
                }
            }
        }
        if ((parse) && (!this.path.equals("/"))) {
            List parts = Arrays.asList(this.path.split("/"));
            parts = parts.subList(1, parts.size());
            controller = (String) parts.get(0);
            switch (parts.size()) {
                case 0:
                case 1:
                    break;
                case 2:
                    action = (String) parts.get(1);
                    break;
                default:
                    action = (String) parts.get(1);
                    args = parts.subList(2, parts.size());
            }
        }

        request = new Request(controller, action, args);
        return request;
    }

    public Router getRouter() {
        return this.routerObject;
    }
}