/*
 * To change this license header, choose License Headers in Project Properties.
 * To change this template file, choose Tools | Templates
 * and open the template in the editor.
 */

package org.nanomvc.mvc;

import java.util.HashMap;
import java.util.Map;

/**
 *
 * @author edmundas
 */
public class Result
{
    public static int SC_200_OK = 200;
    public static int SC_204_NO_CONTENT = 204;

    public static int SC_300_MULTIPLE_CHOICES = 300;
    public static int SC_301_MOVED_PERMANENTLY = 301;
    public static int SC_302_FOUND = 302;
    public static int SC_303_SEE_OTHER = 303;
    public static int SC_304_NOT_MODIFIED = 304;
    public static int SC_307_TEMPORARY_REDIRECT = 307;

    public static int SC_400_BAD_REQUEST = 400;
    public static int SC_403_FORBIDDEN = 403;
    public static int SC_404_NOT_FOUND = 404;

    public static int SC_500_INTERNAL_SERVER_ERROR = 500;
    public static int SC_501_NOT_IMPLEMENTED = 501;
    
    public static final String CT_TEXT_HTML = "text/html";
    public static final String CT_TEXT_PLAIN = "text/plain";
    public static final String CT_APPLICATON_JSON = "application/json";
    public static final String CT_APPLICATON_JSONP = "application/javascript";
    public static final String CT_APPLICATION_XML = "application/xml";
    public static final String CT_APPLICATION_OCTET_STREAM = "application/octet-stream";
    
    public static final String LOCATION = "Location";
    public static final String CACHE_CONTROL = "Cache-Control";
    public static final String CACHE_CONTROL_DEFAULT_NOCACHE_VALUE = "no-cache, no-store, max-age=0, must-revalidate";
    
    public static final String DATE = "Date";
    public static final String EXPIRES = "Expires";
    
    private boolean renderable = true;
    private boolean layoutEnabled = true;
    private final int statusCode;
    private final String charset;
    private String contentType;
    private Object content;
    private String template;
    private String link;
    private Map<String, Map<String, Object>> params;
    
    public Result(int statusCode) {

        this.statusCode = statusCode;
        this.charset = "utf-8";

    }
    
    public int getStatusCode() {
        return statusCode;
    }

    public String getLink() {
        return link;
    }

    public void setLink(String link) {
        this.link = link;
    }
    
    public void setContent(String content) {
        this.content = content;
    }
    
    public Object getContent() {
        return content;
    }
    
    public String getTemplate() {
        return template;
    }
    
    public void setTemplate(String template) {
        this.template = template;
    }

    public Map<String, Map<String, Object>> getParams() {
        return params;
    }

    public void setParams(Map<String, Object> paramsPrivate, Map<String, Object> paramsPublic) {
        params = new HashMap<>();
        params.put("private", paramsPrivate);
        params.put("public", paramsPublic);
    }

    public void setParams(Map<String, Map<String, Object>> params) {
        this.params = params;
    }
    
    public Result link(String link) {
        this.link = link;
        return this;
    }
    
    public Result content(Object content) {
        this.content = content;
        return this;
    }
    
    public Result template(String template) {
        this.template = template;
        return this;
    }

    public Result text() {
        contentType = CT_TEXT_PLAIN;
        return this;
    }

    public Result html() {
        contentType = CT_TEXT_HTML;
        return this;
    }

    public Result json() {
        contentType = CT_APPLICATON_JSON;
        return this;
    }

    public Result xml() {
        contentType = CT_APPLICATION_XML;
        return this;
    }
    
    public Map<String, String> getHeaders() {
        Map<String, String> headers = new HashMap<>();
        headers.put("Content-Type", contentType + "; charset=" + charset);
        headers.put("Server", "Server");
        headers.put(CACHE_CONTROL, CACHE_CONTROL_DEFAULT_NOCACHE_VALUE);
        
        return headers;
    }
    
    public boolean isRenderable() {
        return renderable;
    }
    
    public Result renderable(boolean renderable) {
        this.renderable = renderable;
        return this;
    }
    
    public Result disableLayout() {
        layoutEnabled = false;
        return this;
    }
    
    public boolean isLayoutEnabled() {
        return layoutEnabled;
    }
}
