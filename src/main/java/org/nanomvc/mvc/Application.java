package org.nanomvc.mvc;

import java.text.SimpleDateFormat;
import java.util.Date;
import javax.servlet.http.HttpSession;
import org.apache.commons.lang.StringUtils;
import org.joda.time.Duration;
import org.nanomvc.UserInt;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

public class Application {

    private static Logger _log = LoggerFactory.getLogger(Application.class);
    
    private String controller;
    private String action;
    private String baseUrl;
    private String currentUrl;
    private Router router;
    private HttpSession session;

    public Application() {
    }

    public Application(String controller, String action, String baseUrl, 
            String currentUrl, Router router, HttpSession session) {
        this.controller = controller;
        this.action = action;
        this.baseUrl = baseUrl;
        this.currentUrl = currentUrl;
        this.router = router;
        this.session = session;
    }

    public String getBaseUrl() {
        return this.baseUrl;
    }

    public String getCurrentUrl() {
        return this.currentUrl;
    }

    public String createUrl(String urlPart) {
        return new StringBuilder().append(this.baseUrl)
                .append(urlPart.startsWith(Controller.SLASH) ? urlPart : 
                        new StringBuilder().append(Controller.SLASH)
                                .append(urlPart).toString()).toString();
    }

    public String createUrl(String controller, String action, Object... params) {
        String route = new StringBuilder().append(controller.substring(0, 1)
                .toUpperCase()).append(controller.substring(1).toLowerCase())
                .append(".").append(action.toLowerCase()).toString();

        String url = new StringBuilder().append(Controller.SLASH)
                .append(controller).append(Controller.SLASH).append(action).toString();
        if (this.router.reverseRoutes().containsKey(route)) {
            url = (String) this.router.reverseRoutes().get(route);
        }
        String result = new StringBuilder().append(this.baseUrl).append(url)
                .append(params != null ? new StringBuilder().append(Controller.SLASH)
                        .append(StringUtils.join(params, Controller.SLASH))
                        .toString() : Controller.EMPTY).toString();
        return result.endsWith("//") ? result.substring(0, result.length()-1) : result;
    }

    public String getUrl() {
        return this.currentUrl;
    }

    public String truncate(String text, Integer length) {
        return text.substring(0, length.intValue());
    }

    public String getUserPhoto(UserInt user) {
        if (user.getPicture() != null) {
            return new StringBuilder().append(this.baseUrl)
                    .append(Controller.PATH_PUBLIC_UPL).append("user/images/")
                    .append(user.getPicture()).toString();
        }
        return null;
    }

    public String getTime(Date date) {
        return new SimpleDateFormat("HH:mm").format(date);
    }

    public String getDate(Date date) {
        return new SimpleDateFormat("yyyy-MM-dd").format(date);
    }

    public String getDateDiff(Date date) {
        String unit;
        Long diff;
        Duration duration = new Duration(date.getTime(), new Date().getTime());
        if (duration.getStandardDays() > 365L) {
            diff = duration.getStandardDays() / 365L;
            unit = "m";
        } else if (duration.getStandardDays() > 0L) {
            diff = duration.getStandardDays();
            unit = "d";
        } else if (duration.getStandardHours() > 0L) {
            diff = duration.getStandardHours();
            unit = "h";
        } else if (duration.getStandardMinutes() > 0L) {
            diff = duration.getStandardMinutes();
            unit = "min";
        } else {
            diff = duration.getStandardSeconds();
            unit = "s";
        }
        return new StringBuilder().append(diff.toString()).append(unit).toString();
    }
    
    public String makeSlug(String name) {
        return name;
    }
    
    public String pagination(Long pagesTotal, Integer currentPage) {
        return pagination(pagesTotal, currentPage, createUrl(controller, action) + "/%d");
    }
    
    public String pagination(Long pagesTotal, Integer currentPage, String pageLink) {
	if(pagesTotal > 1000000)
            return "You're crazy!";
	
	StringBuilder sb = new StringBuilder();
	
	int range = 5;
	
	for(int i = 1; i <= pagesTotal; i++) {
            String attributes = "";
            String cssClass = "";
            Boolean include = false;
            
            if(i == 1)
                cssClass += " first";
            if(i == pagesTotal)
                cssClass += " last";
            if(i == currentPage)
                cssClass += " active";
            
            if(i == 1 || i == pagesTotal || (i > currentPage - range && i < currentPage + range)) {
                include = true;
            } else if(
                    pagesTotal <= 20 || (pagesTotal <= 100 && i % 10 == 0)
                    || (pagesTotal > 100 && pagesTotal <= 1000 && i % 100 == 0)
                    || (pagesTotal > 1000 && pagesTotal <= 10000 && i % 1000 == 0)
                    || (pagesTotal > 10000 && pagesTotal <= 100000 && i % 10000 == 0)
                    || (pagesTotal > 100000 && pagesTotal <= 1000000 && i % 100000 == 0)
                ) {
                include = true;
                cssClass += " grouped";
                attributes = " style=\"display: none;\"";
            }
            if(include) {
                sb.append("<li class=\"").append(cssClass).append("\"")
                    .append(attributes).append(">").append("<a href=\"")
                    .append(String.format(pageLink, i)).append("\">")
                    .append(i).append("</a>").append("</li>");
            }
	}
	
	return sb.toString();
}
}