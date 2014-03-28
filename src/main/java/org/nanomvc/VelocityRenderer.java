package org.nanomvc;

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
    private static final Logger _log = LoggerFactory.getLogger(VelocityRenderer.class);
    
    private static final String KEY_CONTENT = "content";
    private static final String KEY_PRIVATE = "private";
    private static final String KEY_PUBLIC = "public";
    private static final String KEY_ESC = "esc";
    private static final String ENCODING = "UTF-8";
    
    private static VelocityEngine veloEngine;
    
    private final org.apache.velocity.tools.generic.EscapeTool escTool;
    private ServletContext context;
    private String viewsPath;
    private String template;
    private Map<String, Map<String, Object>> params;
    
    public VelocityRenderer() {
        escTool = new org.apache.velocity.tools.generic.EscapeTool();
    }
    
    @Override
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
            // ignore
        }
        try {
            StringWriter sw = new StringWriter();

            context = new VelocityContext();
            context.put(KEY_ESC, escTool);
            if (params.get(KEY_PUBLIC) != null) {
                for (Map.Entry entry : params.get(KEY_PUBLIC).entrySet()) {
                    context.put((String) entry.getKey(), entry.getValue());
                }
            }
            context.put(KEY_CONTENT, fetch(template, params.get(KEY_PRIVATE)));

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
        return fetch(view, params.get(KEY_PRIVATE));
    }

    @Override
    public String fetch(String view, Map<String, Object> params) {
        String result = null;

        VelocityEngine engine = getVeloEngine();
        VelocityContext context = new VelocityContext();

        view = new StringBuilder().append(view).append(!view.endsWith(".htm") ? ".htm" : "").toString();
        
        context.put(KEY_ESC, escTool);
        if (params != null) {
            for (Map.Entry entry : params.entrySet()) {
                context.put((String) entry.getKey(), entry.getValue());
            }
        }
        try {
            StringWriter sw = new StringWriter();

            engine.mergeTemplate(view, ENCODING, context, sw);

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
                properties.setProperty("input.encoding", ENCODING);
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
