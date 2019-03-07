package org.catalyst.biascorrect.controllers.v1;

import com.fasterxml.jackson.databind.JsonNode;
import org.apache.commons.lang3.tuple.Pair;
import org.catalyst.biascorrect.SlackSecrets;
import org.catalyst.biascorrect.algos.TriggerWordBiasDetector;
import org.catalyst.biascorrect.db.DbManager;
import play.Logger;
import play.libs.ws.WSBodyReadables;
import play.libs.ws.WSBodyWritables;
import play.libs.ws.WSClient;
import play.libs.ws.WSRequest;
import play.mvc.Result;

import javax.inject.Inject;
import java.util.HashMap;
import java.util.LinkedList;
import java.util.List;
import java.util.Map;
import java.util.concurrent.CompletionStage;

public class SlackEventController extends BaseController implements WSBodyReadables, WSBodyWritables {

    @Inject
    public SlackEventController(WSClient wsClient) {
        super(wsClient);
    }

    public Result handle() {
        JsonNode node = request().body().asJson();
        if (node != null && !node.isNull() && !node.isMissingNode()) {
            String requestToken = getValueFromJson(node, "token");
            if (requestToken != null && requestToken.equals(SlackSecrets.getInstance().getSlackToken())) {
                String requestType = getValueFromJson(node, "type");
                if (requestType != null) {
                    if (requestType.equals("url_verification")) {
                        String challenge = getValueFromJson(node, "challenge");
                        if (challenge != null) {
                            return jsonOk("challenge", challenge);
                        } else {
                            return jsonError("Invalid Challenge Request, challenge parameter not found!");
                        }
                    } else if (requestType.equals("event_callback")) {
                        JsonNode eventNode = node.path("event");
                        if (eventNode != null && !eventNode.isNull() && !eventNode.isMissingNode()) {
                            String userName = getValueFromJson(eventNode, "username");
                            String botId = getValueFromJson(eventNode, "bot_id");
                            boolean isBotMessage = botId != null &&
                                    botId.equals(SlackSecrets.getInstance().getBotId()) &&
                                    userName != null &&
                                    userName.equals(SlackSecrets.getInstance().getBotUserName());
                            if (!isBotMessage) {
                                String eventType = getValueFromJson(eventNode, "type");
                                if (eventType.equals("message")) {
                                    String user = getValueFromJson(eventNode, "user");
                                    String team = getValueFromJson(node, "team_id");
                                    String eventSubType = getValueFromJson(eventNode, "subtype");
                                    if (eventSubType == null) {
                                        String channelType = getValueFromJson(eventNode, "channel_type");
                                        String messageText = getValueFromJson(eventNode, "text");
                                        if ((channelType != null && channelType.equals("im")) ||
                                                (messageText.contains("@UF5E8PGAD") || messageText.contains("@UFYJYAUSD"))
                                                && messageText.toLowerCase().contains("help")) {
                                            handleHelpRequest(team, user, eventNode);
                                        } else {
                                            handleUserMessage(
                                                    eventNode,
                                                    team,
                                                    user
                                            );
                                        }
                                    } else if (eventSubType.equals("channel_join") ||
                                            (eventSubType.equals("group_join") && user.equals("UF5E8PGAD"))) {
                                        handleChannelJoin(team, eventNode);
                                    }
                                }
                            }
                            return jsonOk("ok", "true");
                        } else {
                            return jsonError("Invalid Slack event message");
                        }
                    } else {
                        return jsonError("Unsupported Request Type");
                    }
                } else {
                    return jsonError("Missing Request Type");
                }
            } else {
                return jsonError("Invalid Slack Token");
            }
        } else {
            return jsonError("Invalid Request");
        }
    }

    private void handleUserMessage(JsonNode eventNode, String team, String user) {
        String messageId = getValueFromJson(eventNode, "ts");
        String channel = getValueFromJson(eventNode, "channel");
        String messageText = getValueFromJson(eventNode, "text");
        if (user != null && messageText != null) {
            if (team != null && channel != null) {
                DbManager.getInstance().updateMessageCounts(team, channel);
            }
            Pair<Boolean, String> result = TriggerWordBiasDetector.getInstance().
                    getCorrectedMessage(messageText);
            if (result != null && result.getLeft()) {
                String authToken = DbManager.getInstance().getTeamToken(team);
                if (authToken == null) {
                    authToken = SlackSecrets.getInstance().getSlackAppOauthToken();
                }
                String correctedMessage = result.getRight();

                Map<String, Object> attachment = new HashMap<String, Object>();
                attachment.put("fallback", "Darn!  I wish this worked!");
                attachment.put("title", "Before you hit send, think about it...if she was a he, would you have chosen that word?");
                attachment.put("callback_id", messageId);
                attachment.put("attachment_type", "default");
                attachment.put("actions", getActions(messageText));
                List<Map<String, Object>> attachments = new LinkedList<Map<String, Object>>();
                attachments.add(attachment);

                Map<String, Object> responseData = new HashMap<String, Object>();
                responseData.put("ok", "true");
                responseData.put("channel", channel);
                responseData.put("token", authToken);
                responseData.put("user", user);
                responseData.put("as_user", "false");
                responseData.put("text", String.format("Did you mean, \"%s?\"", correctedMessage));
                responseData.put("attachments", attachments);

                WSRequest request = _wsClient.url("https://slack.com/api/chat.postEphemeral").
                        setContentType("application/json").
                        addHeader("Authorization", String.format("Bearer %s", authToken));
                CompletionStage<JsonNode> jsonPromise = request.post(toJson(responseData)).thenApply(r -> r.getBody(json()));
                JsonNode responseNode = extractResponseJson(jsonPromise);
            }
        }
    }

    private void handleChannelJoin(String team, JsonNode eventNode) {
        String authToken = DbManager.getInstance().getTeamToken(team);
        if (authToken == null) {
            authToken = SlackSecrets.getInstance().getSlackAppOauthToken();
        }
        Map<String, Object> responseData = new HashMap<String, Object>();
        String channel = getValueFromJson(eventNode, "channel");
        responseData.put("ok", "true");
        responseData.put("channel", channel);
        responseData.put("token", authToken);
        responseData.put("as_user", "false");
        responseData.put(
                "text",
                "The Catalyst *#BiasCorrect Plug-In* has been added to your channel.\n" +
                        "Studies show that unconscious gender bias fuels the gender gap. " +
                        "This plug-in is designed to help empower its users to become a catalyst for change, " +
                        "by flagging unconscious gender bias in real-time conversations and offering up alternative " +
                        "bias-free words or phrases for users to consider instead. " +
                        "Hit *Authorize* now to help #BiasCorrect the workplace, once and for all. " +
                        "Use the *bias-correct* command for usage information"
        );
        Map<String, Object> attachment = new HashMap<String, Object>();
        attachment.put("fallback", "Darn!  I wish this worked!");
        attachment.put("actions", getInstallLinkActions());
        List<Map<String, Object>> attachments = new LinkedList<Map<String, Object>>();
        attachments.add(attachment);
        responseData.put("attachments", attachments);

        WSRequest request = _wsClient.url("https://slack.com/api/chat.postMessage").
                setContentType("application/json").
                addHeader("Authorization", String.format("Bearer %s", authToken));
        CompletionStage<JsonNode> jsonPromise = request.post(toJson(responseData)).thenApply(r -> r.getBody(json()));
        JsonNode responseNode = extractResponseJson(jsonPromise);
        Logger.of("play").info(responseNode.toString());
    }

    private void handleHelpRequest(String team, String user, JsonNode eventNode) {
        String authToken = DbManager.getInstance().getTeamToken(team);
        if (authToken == null) {
            authToken = SlackSecrets.getInstance().getSlackAppOauthToken();
        }
        Map<String, Object> responseData = new HashMap<String, Object>();
        String channel = getValueFromJson(eventNode, "channel");
        responseData.put("ok", "true");
        responseData.put("channel", channel);
        responseData.put("token", authToken);
        responseData.put("user", user);
        responseData.put("text",
                "The BiasCorrect plug-in helps identify and correct unconscious gender bias in day-to-day messages and " +
                        "conversations.\n" +
                        "Examples: 'she is very emotional', or 'she is very dramatic', or " +
                        "'she is a nag', or 'she is very temperamental'.\n" +
                        "Try typing one of these messages and see what happens!"
        );

        WSRequest request = _wsClient.url("https://slack.com/api/chat.postMessage").
                setContentType("application/json").
                addHeader("Authorization", String.format("Bearer %s", authToken));
        CompletionStage<JsonNode> jsonPromise = request.post(toJson(responseData)).thenApply(r -> r.getBody(json()));
        JsonNode responseNode = extractResponseJson(jsonPromise);
        Logger.of("play").info(responseNode.toString());
    }

    private List<Map<String, String>> getActions(String messageText) {
        List<Map<String, String>> actions = new LinkedList<Map<String, String>>();
        actions.add(getAction(messageText, "Bias Correct", "yes", "primary"));
        actions.add(getAction(messageText, "No", "no", "danger"));
        actions.add(getAction(messageText, "Learn More", "learn_more", null));
        return actions;
    }

    private Map<String, String> getAction(String name, String text, String value, String style) {
        Map<String, String> action = new HashMap<String, String>();
        action.put("name", name);
        action.put("text", text);
        action.put("type", "button");
        action.put("value", value);
        if (style != null) {
            action.put("style", style);
        }
        return action;
    }

    private List<Map<String, String>> getInstallLinkActions() {
        List<Map<String, String>> actions = new LinkedList<Map<String, String>>();
        actions.add(getInstallLinkAction("Authorize", System.getenv("APP_SLACK_SIGNIN_URL")));
        actions.add(getInstallLinkAction("Learn More", System.getenv("APP_SLACK_LEARN_MORE_URL")));
        return actions;
    }

    private Map<String, String> getInstallLinkAction(String title, String titleLink) {
        Map<String, String> action = new HashMap<String, String>();
        action.put("type", "button");
        action.put("text", title);
        action.put("url", titleLink);
        return action;
    }
}
