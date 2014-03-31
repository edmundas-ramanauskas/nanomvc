package org.nanomvc.http;

import java.lang.reflect.Method;
import java.util.Arrays;
import java.util.List;
import java.util.Map;
import org.apache.commons.lang.StringUtils;
import org.nanomvc.mvc.Router;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

public class RequestHandler {
    
    private static final Logger _log = LoggerFactory.getLogger(RequestHandler.class);

    private static final String SLASH = "/";
    private static final String RoutesMethod = "routes";
    private String path;
    private String router;
    private Router routerObject;

    public RequestHandler() {
    }

    public RequestHandler(String path, String router) {
        this.path = (path == null) ? SLASH : path;
        this.router = router;
    }

    public Request parseRequest(String defaultController) {
        Request request = null;
        Map routes = null;
        String controller = null;
        String action = null;
        List args = null;
        if ((path.endsWith(SLASH)) && (!path.equals(SLASH))) {
            path = path.substring(0, path.length() - 1);
        }
        try {
            if (router != null) {
                ClassLoader classLoader = getClass().getClassLoader();
                Class rClass = classLoader.loadClass(router);
                routerObject = ((Router) rClass.getConstructor(new Class[0])
                        .newInstance(new Object[0]));
                Method method = rClass.getMethod(RoutesMethod, new Class[0]);
                routes = (Map) method.invoke(routerObject, new Object[0]);
            }
        } catch (Throwable t) {
            
        }
        Boolean parse = true;
        if (routes != null) {
            String route = null;
            if (path.equals(SLASH)) {
                route = path;
            } else if (routes.containsKey(path)) {
                route = path;
            } else {
                List parts = Arrays.asList(path.split(SLASH));
                parts = parts.subList(1, parts.size());
                for (int i = 0; i < parts.size(); i++) {
                    String tempRoute = SLASH 
                            + StringUtils.join(parts.subList(0, parts.size() - i)
                                    , SLASH);
                    if (routes.containsKey(tempRoute)) {
                        route = tempRoute;
                        try {
                            args = parts.subList(parts.size() - i, parts.size());
                        } catch (Exception e) {
//                            System.out.println(e.toString());
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
        if ((parse) && (!path.equals(SLASH))) {
            List parts = Arrays.asList(path.split(SLASH));
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

        request = new Request(defaultController, controller, action, args);
        return request;
    }

    public Router getRouter() {
        return routerObject;
    }
}