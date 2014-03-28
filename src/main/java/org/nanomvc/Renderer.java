package org.nanomvc;

import java.util.Map;
import javax.servlet.ServletContext;

/**
 *
 * @author edmundas
 */
public interface Renderer
{
    public void configure(ServletContext context, String viewsPath, Map<String, Map<String, Object>> params);
    public String render(String template);
    public String fetch(String view);
    public String fetch(String view, Map<String, Object> params);
}
