import org.nanomvc.mvc.Router;
import java.util.HashMap;
import java.util.Map;

public class RouterExample extends Router {

    public Map routes() {
        Map routes = new HashMap();
        
        routes.put("/",               "ExampleController.index");
        
        return routes;
    }
}