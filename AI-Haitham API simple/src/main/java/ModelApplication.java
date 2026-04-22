import java.io.IOException;

public abstract class ModelApplication {

    public abstract String call(Model model, boolean print) throws IOException;
}
