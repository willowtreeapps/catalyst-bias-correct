package org.catalyst.biascorrect.controllers.v1;

import com.fasterxml.jackson.databind.JsonNode;
import org.catalyst.biascorrect.SlackSecrets;
import org.catalyst.biascorrect.algos.TriggerWordBiasDetector;
import org.catalyst.biascorrect.db.DbManager;
import org.catalyst.biascorrect.domain.SlackAction;
import play.Logger;
import play.libs.ws.WSBodyReadables;
import play.libs.ws.WSBodyWritables;
import play.libs.ws.WSClient;
import play.libs.ws.WSRequest;
import play.mvc.Result;

import javax.inject.Inject;
import java.io.IOException;
import java.util.HashMap;
import java.util.Map;
import java.util.concurrent.CompletionStage;

public class SlackUserActionController extends BaseController implements WSBodyReadables, WSBodyWritables {

    @Inject
    public SlackUserActionController(WSClient wsClient) {
        super(wsClient);
    }

    public Result handle() {
        Map<String, String[]> formData = request().body().asFormUrlEncoded();
        String[] payloadEntries = formData.get("payload");
        if (payloadEntries != null && payloadEntries.length == 1) {
            String payload = payloadEntries[0];
            try {
                JsonNode payloadNode = OBJECT_MAPPER.readTree(payload);
                String messageID = getValueFromJson(payloadNode, "callback_id");
                String triggerId = getValueFromJson(payloadNode, "trigger_id");
                if (messageID != null && triggerId != null) {
                    JsonNode actionsNode = payloadNode.path("actions");
                    if (actionsNode.isArray()) {
                        int numActions = 0;
                        SlackAction action = null;
                        for (JsonNode actionNode : actionsNode) {
                            if (numActions < 1) {
                                action = OBJECT_MAPPER.convertValue(actionNode, SlackAction.class);
                                numActions++;
                            }
                        }
                        if (action != null && numActions == 1) {
                            JsonNode userNode = payloadNode.path("user");
                            String userId = userNode.path("id").textValue();
                            String userName = userNode.path("name").textValue();
                            JsonNode teamNode = payloadNode.path("team");
                            String teamId = teamNode.path("id").textValue();
                            JsonNode channelNode = payloadNode.path("channel");
                            String channelId = channelNode.path("id").textValue();
                            String answer = action.getValue();
                            String originalMessage = action.getName();
                            String updateUrl = null;
                            String updateMessage = null;
                            String userToken = DbManager.getInstance().getUserToken(teamId, userId);
                            Map<String, String> responseData = new HashMap<String, String>();
                            responseData.put("trigger_id", triggerId);
                            responseData.put("channel", channelId);
                            boolean addUserToken = false;
                            if (answer.equals("no")) {
                                DbManager.getInstance().updateIgnoredMessageCounts(teamId, channelId);
                            } else if (answer.equals("learn_more")) {
                                DbManager.getInstance().updateLearnMoreMessageCounts(teamId, channelId);
                                updateUrl = "https://slack.com/api/chat.postEphemeral";
                                updateMessage = "*Before you hit send, think about it...if she was a he, would you " +
                                        "have chosen that word?*\n" +
                                        "Unconscious gender bias negatively impacts women’s advancement in the workplace.  " +
                                        "Discover tools for overcoming it at <https://catalyst.org/topics/unconscious-bias/>";
                                responseData.put("user", userId);
                                responseData.put("as_user", String.valueOf(Boolean.TRUE));
                            } else if (answer.equals("yes")) {
                                addUserToken = true;
                                String correctedMessage = TriggerWordBiasDetector.getInstance().correctMessage(originalMessage);
                                if (userToken == null) {
                                    updateMessage = String.format(
                                            "%s has replaced \"%s\" with \"%s\"",
                                            userName,
                                            originalMessage,
                                            correctedMessage
                                    );
                                    updateUrl = "https://slack.com/api/chat.postMessage";
                                } else {
                                    updateMessage = correctedMessage;
                                    updateUrl = "https://slack.com/api/chat.update";
                                    responseData.put("ts", messageID);
                                }
                                DbManager.getInstance().updateCorrectedMessageCounts(teamId, channelId);
                            }
                            if (updateMessage != null && updateUrl != null) {
                                if (userToken == null) {
                                    userToken = DbManager.getInstance().getTeamToken(teamId);
                                    if (userToken == null) {
                                        userToken = SlackSecrets.getInstance().getSlackAppOauthToken();
                                    }
                                }
                                if (addUserToken) {
                                    responseData.put("token", userToken);
                                }
                                responseData.put("text", updateMessage);
                                WSRequest request = _wsClient.url(updateUrl).
                                        setContentType("application/json").
                                        addHeader("Authorization", String.format("Bearer %s", userToken));
                                CompletionStage<JsonNode> jsonPromise = request.post(toJson(responseData)).thenApply(r -> r.getBody(json()));
                                JsonNode responseNode = extractResponseJson(jsonPromise);
                                Logger.of("play").info(responseNode.toString());
                            }
                        }
                    }
                }
            } catch (IOException ioe) {
                ioe.printStackTrace();
            }
        }
        return noContent();
    }
}
