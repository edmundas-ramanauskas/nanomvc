import org.nanomvc.mvc.Controller;
import org.nanomvc.mvc.Result;

/**
 *
 * @author edmundas
 */
public class ExampleController extends Controller {
    
    public Result index() {
        String result = "Hello World!";
        if(isAjax())
            return json(result);
        return text(result);
    }
    
}
