package org.nanomvc.http;

import java.io.Serializable;
import java.util.List;

public class Request
  implements Serializable
{
    private static final String ControllerSuffix = "Controller";
    private static final String DefaultAction = "index";
    private String controller;
    private String action;
    private List<String> arguments;

    public Request()
    {
    }

    public Request(String defaultController, String controller, String action, List<String> arguments)
    {
        this.controller = (controller != null ? controller : defaultController);
        this.action = (action == null ? DefaultAction : action);
        this.arguments = arguments;
    }

    public String getController() {
        return controller;
    }

    public String getAction() {
        return action;
    }

    public List<String> getArguments() {
        return arguments;
    }

    public String getControllerClassName() {
        return controller.substring(0, 1).toUpperCase() 
                + controller.substring(1).toLowerCase();
    }

    public String getControllerMethodName() {
        return action;
    }
}