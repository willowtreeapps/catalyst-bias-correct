package org.catalyst.biascorrect;

import play.Logger;

public class SlackSecrets {

    private static SlackSecrets INSTANCE = null;

    private String _clientId;

    private String _clientSecret;

    private String _slackSigningSecret;

    private String _slackToken;

    private String _slackBotUserOAuthToken;

    private String _slackAppOauthToken;

    private String _botId;

    private String _botUserName;

    public static SlackSecrets getInstance() {
        return INSTANCE;
    }

    public static void init() {
        Logger.info("Initializing Slack Secrets...");
        if (INSTANCE == null) {
            INSTANCE = new SlackSecrets();
            INSTANCE.initSecrets();
        }
    }

    private void initSecrets() {
            Logger.info("Fetching Slack Secretsfrom AWS Secrets Manager...");
            _clientId = System.getenv("SLACK_CLIENT_ID");
            _clientSecret = System.getenv("SLACK_CLIENT_SECRET");
            _slackSigningSecret = System.getenv("SLACK_SIGNING_SECRET");
            _slackToken = System.getenv("SLACK_TOKEN");
            _slackBotUserOAuthToken = System.getenv("BOT_USER_OAUTH_TOKEN");
            _botId = System.getenv("APP_BOT_ID");
            _botUserName = System.getenv("APP_BOT_USERNAME");
            _slackAppOauthToken = System.getenv("SLACK_APP_OAUTH_TOKEN");
    }

    public String getClientId() {
        return _clientId;
    }

    public String getClientSecret() {
        return _clientSecret;
    }

    public String getSlackSigningSecret() {
        return _slackSigningSecret;
    }

    public String getSlackToken() {
        return _slackToken;
    }

    public String getBotId() {
        return _botId;
    }

    public String getBotUserName() {
        return _botUserName;
    }

    public String getSlackAppOauthToken() {
        return _slackAppOauthToken;
    }

    public String getSlackBotUserOAuthToken() {
        return _slackBotUserOAuthToken;
    }
}
