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
    private String defaultController;

    public Request()
    {
    }

    public Request(String controller, String action, List<String> arguments)
    {
        this.controller = controller;
        this.action = action;
        this.arguments = arguments;
    }

    public String getController() {
        return controller;
    }

    public void setController(String controller) {
        this.controller = controller;
    }

    public String getAction() {
        return action;
    }

    public void setAction(String action) {
        this.action = action;
    }

    public List<String> getArguments() {
        return arguments;
    }

    public void setArguments(List<String> arguments) {
        this.arguments = arguments;
    }

    public String getDefaultController() {
        return defaultController;
    }

    public void setDefaultController(String defaultController) {
        this.defaultController = defaultController;
    }

    public String getControllerClassName() {
        if (controller == null) {
            controller = defaultController.toLowerCase();
        }
        return controller.substring(0, 1).toUpperCase() 
                + controller.substring(1).toLowerCase();
    }

    public String getControllerMethodName() {
        action = (action == null ? DefaultAction : action);
        return action;
    }
}