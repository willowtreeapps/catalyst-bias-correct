package org.catalyst.biascorrect.controllers.v1;

import com.fasterxml.jackson.databind.JsonNode;
import org.catalyst.biascorrect.SlackSecrets;
import org.catalyst.biascorrect.db.DbManager;
import play.Logger;
import play.libs.ws.WSBodyReadables;
import play.libs.ws.WSBodyWritables;
import play.libs.ws.WSClient;
import play.libs.ws.WSRequest;
import play.mvc.Result;

import javax.inject.Inject;
import java.util.concurrent.CompletionStage;

public class SlackAuthController extends BaseController implements WSBodyReadables, WSBodyWritables {

    @Inject
    public SlackAuthController(WSClient wsClient) {
        super(wsClient);
    }

    /**
     * Handle all oauth requests from Slack.
     */
    public Result handle() {
        String requestCode = request().getQueryString("code");
        // send a get request to Slack with the code to get token for authed user
        WSRequest request = _wsClient.url("https://slack.com/api/oauth.access").
                addQueryParameter("code", requestCode).
                addQueryParameter("client_id", SlackSecrets.getInstance().getClientId()).
                addQueryParameter("client_secret", SlackSecrets.getInstance().getClientSecret());
        CompletionStage<JsonNode> jsonPromise = request.get()
                .thenApply(r -> r.getBody(json()));
        JsonNode responseNode = extractResponseJson(jsonPromise);
        String teamId = getValueFromJson(responseNode, "team_id");
        String userId = getValueFromJson(responseNode, "user_id");
        String userToken = getValueFromJson(responseNode, "access_token");
        if (teamId != null && userId != null && userToken != null) {
            DbManager.getInstance().addTeamToken(teamId, userToken);
            DbManager.getInstance().addUserToken(teamId, userId, userToken);
        } else {
            Logger.of("play").info("*** Invalid Auth Access request from Slack");
        }
        return found(System.getenv("APP_INSTALL_LANDING_PAGE"));
    }

    public Result signin() {
        return found(System.getenv("APP_SLACK_OAUTH_URL"));
    }
}
