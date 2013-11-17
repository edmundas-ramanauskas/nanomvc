/*
 * To change this license header, choose License Headers in Project Properties.
 * To change this template file, choose Tools | Templates
 * and open the template in the editor.
 */

package org.nanomvc.mvc;

import java.util.HashMap;
import java.util.Map;
import org.nanomvc.Renderer;
import org.nanomvc.VelocityRenderer;

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
    
    public static final String TEXT_HTML = "text/html";
    public static final String TEXT_PLAIN = "text/plain";
    public static final String APPLICATON_JSON = "application/json";
    public static final String APPLICATON_JSONP = "application/javascript";
    public static final String APPLICATION_XML = "application/xml";
    public static final String APPLICATION_OCTET_STREAM = "application/octet-stream";
    
    public static final String LOCATION = "Location";
    public static final String CACHE_CONTROL = "Cache-Control";
    public static final String CACHE_CONTROL_DEFAULT_NOCACHE_VALUE = "no-cache, no-store, max-age=0, must-revalidate";
    
    public static final String DATE = "Date";
    public static final String EXPIRES = "Expires";
    
    private boolean renderable = true;
    private int statusCode;
    private String contentType;
    private String charset;
    private String content;
    private String template;
    private Map<String, Map<String, Object>> params;
    
    public Result(int statusCode) {

        this.statusCode = statusCode;
        this.charset = "utf-8";

    }
    
    public int getStatusCode() {
        return statusCode;
    }
    
    public void setContent(String content) {
        this.content = content;
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
    
    public Result content(String content) {
        this.content = content;
        return this;
    }

    public Result text() {
        contentType = TEXT_PLAIN;
        return this;
    }

    public Result html() {
        contentType = TEXT_HTML;
        return this;
    }

    public Result json() {
        contentType = APPLICATON_JSON;
        return this;
    }

    public Result jsonp() {
        contentType = APPLICATON_JSONP;
        return this;
    }

    public Result xml() {
        contentType = APPLICATION_XML;
        return this;
    }
    
    public String getContent() {
        return content;
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
}
