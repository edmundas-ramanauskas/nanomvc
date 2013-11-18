/*
 * To change this license header, choose License Headers in Project Properties.
 * To change this template file, choose Tools | Templates
 * and open the template in the editor.
 */

package org.nanomvc;

import java.io.BufferedWriter;
import java.io.IOException;
import java.io.PrintWriter;
import java.io.StringWriter;
import java.util.Map;
import java.util.Properties;
import javax.servlet.ServletContext;
import org.apache.velocity.Template;
import org.apache.velocity.VelocityContext;
import org.apache.velocity.app.VelocityEngine;
import org.apache.velocity.exception.ParseErrorException;
import org.apache.velocity.exception.ResourceNotFoundException;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

/**
 *
 * @author edmundas
 */
public class VelocityRenderer implements Renderer
{
    private static Logger _log = LoggerFactory.getLogger(VelocityRenderer.class);
    
    private static VelocityEngine veloEngine;
    
    private ServletContext context;
    private String viewsPath;
    private String template;
    private Map<String, Map<String, Object>> params;
    
    public void configure(ServletContext context, String viewsPath, Map<String, Map<String, Object>> params) {
        this.context = context;
        this.viewsPath = viewsPath;
        this.params = params;
    }

    @Override
    public String render(String template)
    {
        String result = null;

        VelocityEngine engine = getVeloEngine();
        VelocityContext context = new VelocityContext();

        Template tpl = null;
        try {
            tpl = engine.getTemplate("layout/main.htm");
        } catch (ResourceNotFoundException | ParseErrorException  ex) {
            _log.error(ex.toString());
        } catch(Exception ex) {
            
        }
        try {
            StringWriter sw = new StringWriter();

            context = new VelocityContext();
            context.put("esc", new org.apache.velocity.tools.generic.EscapeTool());
            if (params.get("public") != null) {
                for (Map.Entry entry : params.get("public").entrySet()) {
                    context.put((String) entry.getKey(), entry.getValue());
                }
            }
            context.put("content", fetch(template, params.get("private")));

            if (tpl != null) {
                tpl.merge(context, sw);
            }

            result = sw.toString();

            sw.close();
        } catch (Exception ex) {
            _log.error("Template render error", ex);
            throw new RuntimeException(ex);
        }
        
        return result;
    }
    
    @Override
    public String fetch(String view) {
        return fetch(view, params.get("private"));
    }

    @Override
    public String fetch(String view, Map<String, Object> params) {
        String result = null;

        VelocityEngine engine = getVeloEngine();
        VelocityContext context = new VelocityContext();

        view = new StringBuilder().append(view).append(!view.endsWith(".htm") ? ".htm" : "").toString();

        org.apache.velocity.tools.generic.EscapeTool esc = new org.apache.velocity.tools.generic.EscapeTool();
        
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
        } catch (Exception ex) {
            _log.error("Template fetch error", ex);
            throw new RuntimeException(ex);
        }
        return result;
    }

    private VelocityEngine getVeloEngine() {
        if (veloEngine == null) {
            try {
                Properties properties = new Properties();
                properties.setProperty("input.encoding", "UTF-8");
                properties.setProperty("resource.loader", "webapp");
                properties.setProperty("webapp.resource.loader.class", "org.apache.velocity.tools.view.WebappResourceLoader");
                properties.setProperty("webapp.resource.loader.path", viewsPath);
                properties.setProperty("resource.manager.defaultcache.size", "256");
                properties.setProperty("webapp.resource.loader.cache", "false");
                properties.setProperty("runtime.log.logsystem.class", "org.apache.velocity.runtime.log.NullLogSystem");
                properties.setProperty("runtime.log.logsystem.log4j.logger", VelocityEngine.class.getName());
                veloEngine = new VelocityEngine();
                veloEngine.setApplicationAttribute("javax.servlet.ServletContext", context);
                veloEngine.init(properties);
            } catch (Exception ex) {
                _log.error("velocity error", ex);
                throw ex;
            }
        }
        return veloEngine;
    }
}
