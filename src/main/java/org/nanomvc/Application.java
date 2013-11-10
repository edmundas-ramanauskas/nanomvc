package org.nanomvc;

import org.nanomvc.mvc.Router;
import java.text.SimpleDateFormat;
import java.util.Date;
import java.util.Map;
import javax.servlet.http.HttpSession;
import org.apache.commons.lang.StringUtils;
import org.joda.time.Duration;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

public class Application {

    private static Logger _log = LoggerFactory.getLogger(Application.class);
    private String baseUrl;
    private String currentUrl;
    private Router router;
    private HttpSession session;

    public Application() {
    }

    public Application(String baseUrl, String currentUrl) {
        this.baseUrl = baseUrl;
        this.currentUrl = currentUrl;
    }

    public Application(String baseUrl, String currentUrl, Router router, HttpSession session) {
        this.baseUrl = baseUrl;
        this.currentUrl = currentUrl;
        this.router = router;
        this.session = session;
    }

    public String getBaseUrl() {
        return this.baseUrl;
    }

    public void setBaseUrl(String baseUrl) {
        this.baseUrl = baseUrl;
    }

    public String getCurrentUrl() {
        return this.currentUrl;
    }

    public void setCurrentUrl(String currentUrl) {
        this.currentUrl = currentUrl;
    }

    public String createUrl(String urlPart) {
        return new StringBuilder().append(this.baseUrl).append(urlPart.startsWith("/") ? urlPart : new StringBuilder().append("/").append(urlPart).toString()).toString();
    }

    public String createUrl(String controller, String action) {
        return createUrl(controller, action, null);
    }

    public String createUrl(String controller, String action, Object... params) {
        String route = new StringBuilder().append(controller.substring(0, 1).toUpperCase()).append(controller.substring(1).toLowerCase()).append(".").append(action.toLowerCase()).toString();

        String url = new StringBuilder().append("/").append(controller).append("/").append(action).toString();
        if (this.router.reverseRoutes().containsKey(route)) {
            url = (String) this.router.reverseRoutes().get(route);
        }
        return new StringBuilder().append(this.baseUrl).append(url).append(params != null ? new StringBuilder().append("/").append(StringUtils.join(params, "/")).toString() : "").toString();
    }

    public String getUrl() {
        return this.currentUrl;
    }

    public String truncate(String text, Integer length) {
        return text.substring(0, length.intValue());
    }

    public String getUserPhoto(UserInt user) {
        if (user.getPicture() != null) {
            return new StringBuilder().append(this.baseUrl).append("/public/upl/user/images/").append(user.getPicture()).toString();
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
        String unit = "";
        Long diff = Long.valueOf(0L);
        Duration duration = new Duration(date.getTime(), new Date().getTime());
        if (duration.getStandardDays() > 365L) {
            diff = Long.valueOf(duration.getStandardDays() / 365L);
            unit = "m";
        } else if (duration.getStandardDays() > 0L) {
            diff = Long.valueOf(duration.getStandardDays());
            unit = "d";
        } else if (duration.getStandardHours() > 0L) {
            diff = Long.valueOf(duration.getStandardHours());
            unit = "h";
        } else if (duration.getStandardMinutes() > 0L) {
            diff = Long.valueOf(duration.getStandardMinutes());
            unit = "min";
        } else {
            diff = Long.valueOf(duration.getStandardSeconds());
            unit = "s";
        }
        return new StringBuilder().append(diff.toString()).append(unit).toString();
    }
    
    public String makeSlug(String name) {
        return name;
    }
}