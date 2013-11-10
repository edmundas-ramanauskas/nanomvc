package org.nanomvc.http;

import java.io.Serializable;
import java.util.List;

public class Request
  implements Serializable
{
  private static final String ControllerSuffix = "Controller";
  private static final String ActionPrefix = "do";
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
    return this.controller;
  }

  public void setController(String controller) {
    this.controller = controller;
  }

  public String getAction() {
    return this.action;
  }

  public void setAction(String action) {
    this.action = action;
  }

  public List<String> getArguments() {
    return this.arguments;
  }

  public void setArguments(List<String> arguments) {
    this.arguments = arguments;
  }

  public String getDefaultController() {
    return this.defaultController;
  }

  public void setDefaultController(String defaultController) {
    this.defaultController = defaultController;
  }

  public String getControllerClassName() {
    String controllerName = null;
    if (this.controller != null) {
      controllerName = this.controller.substring(0, 1).toUpperCase() + this.controller.substring(1).toLowerCase() + "Controller";
    } else {
      this.defaultController = this.defaultController.toLowerCase();
      if (this.defaultController.endsWith("Controller".toLowerCase()))
        this.controller = this.defaultController.substring(0, this.defaultController.lastIndexOf("Controller".toLowerCase()));
      else {
        this.controller = this.defaultController;
      }
      controllerName = this.controller.substring(0, 1).toUpperCase() + this.controller.substring(1).toLowerCase() + "Controller";
    }
    return controllerName;
  }

  public String getControllerMethodName() {
    this.action = (this.action == null ? "index" : this.action);
    return "do" + this.action.substring(0, 1).toUpperCase() + this.action.substring(1).toLowerCase();
  }
}