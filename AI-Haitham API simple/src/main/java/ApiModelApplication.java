import org.apache.http.client.methods.HttpPost;
import org.apache.http.entity.StringEntity;

import java.io.IOException;
import java.nio.charset.StandardCharsets;

public class ApiModelApplication extends ModelApplication {

    @Override
    public String call(Model model, boolean print) throws IOException {
        HttpPost httpPost = new HttpPost(model.getApiURL());
        httpPost.setHeader("Content-Type", "application/json; charset=UTF-8");
        httpPost.setHeader("Accept", "application/json; charset=UTF-8");
        httpPost.setHeader("Authorization", model.authorizationHeader());
        httpPost.setEntity(new StringEntity(model.getJson(), StandardCharsets.UTF_8));

        if (print) {
            System.out.print(model.getSpeechIndication());
            return Services.applyAPIRequest(httpPost, chunk -> System.out.print(chunk));
        }

        return Services.applyAPIRequest(httpPost);
    }
}
