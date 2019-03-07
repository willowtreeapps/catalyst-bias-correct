package org.catalyst.biascorrect.controllers.v1;

import org.catalyst.biascorrect.SlackSecrets;
import play.libs.ws.WSBodyReadables;
import play.libs.ws.WSBodyWritables;
import play.libs.ws.WSClient;
import play.mvc.Result;

import javax.inject.Inject;
import java.util.HashMap;
import java.util.Map;
import java.util.Optional;

public class SlackHelpController extends BaseController implements WSBodyReadables, WSBodyWritables {

    @Inject
    public SlackHelpController(WSClient wsClient) {
        super(wsClient);
    }

    private String getFormValue(Map<String, String[]> formData, String key) {
        String value = null;
        if (formData.containsKey(key)) {
            value = formData.get(key)[0];
        }
        return value;
    }

    public Result handle() {
        Map<String, String[]> formData = request().body().asFormUrlEncoded();
        String requestToken = getFormValue(formData, "token");
        String command = getFormValue(formData, "command");
        String commandAction = getFormValue(formData, "text");
        Optional<String> timestampOption = request().header("X-Slack-Request-Timestamp");
        Optional<String> slackSignatureOption = request().header("X-Slack-Signature");
        if (timestampOption.isPresent() && slackSignatureOption.isPresent() &&
                requestToken != null && requestToken.equals(SlackSecrets.getInstance().getSlackToken()) &&
                command.equals("/bias-correct")) {
            Map<String, Object> responseData = new HashMap<String, Object>();
            responseData.put("response_type", "ephemeral");
            if (commandAction != null && !commandAction.isEmpty()) {
                if (commandAction.equals("help")) {
                    responseData.put("text",
                            "The BiasCorrect plug-in helps identify and correct unconscious gender bias in day-to-day messages and " +
                                    "conversations.\n" +
                                    "Examples: 'she is very emotional', or 'she is very dramatic', or " +
                                    "'she is a nag', or 'she is very temperamental'.\n" +
                                    "Try typing one of these messages and see what happens!"
                    );
                } else {
                    responseData.put(
                            "text",
                            String.format("Unsupported Action: %s.  Supported Actions: [help]", commandAction)
                    );
                }
            } else {
                responseData.put(
                        "text",
                        "No Action Specified. Supported Actions: [help]"
                );
            }
            return ok(toJson(responseData));
        } else {
            return unauthorized();
        }
    }
}
