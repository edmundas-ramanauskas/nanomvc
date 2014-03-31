import org.nanomvc.mvc.Controller;
import org.nanomvc.mvc.Result;

/**
 *
 * @author edmundas
 */
public class ExampleController extends Controller {
    
    public Result index() {
        return text("Hello World!");
    }
    
}
