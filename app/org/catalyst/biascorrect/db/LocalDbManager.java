package org.catalyst.biascorrect.db;

import java.util.Map;
import java.util.concurrent.ConcurrentHashMap;

public class LocalDbManager extends DbManager {

    private Map<String, String> _userTokens = new ConcurrentHashMap<>();

    private Map<String, String> _teamTokens = new ConcurrentHashMap<>();

    public String getType() {
        return "local";
    }

    @Override
    public void addUserToken(String teamId, String userId, String token) {
        _userTokens.put(
                String.format("%s_%s", teamId, userId),
                token
        );
    }

    @Override
    public void addTeamToken(String teamId, String token) {
        _teamTokens.putIfAbsent(teamId, token);
    }

    @Override
    public String getTeamToken(String teamId) {
        return _teamTokens.get(teamId);
    }

    public String getUserToken(String teamId, String userId) {
        return _userTokens.get(String.format("%s_%s", teamId, userId));
    }

    public void updateMessageCounts(String teamId, String channelId) {

    }

    public void updateCorrectedMessageCounts(String teamId, String channelId) {

    }

    public void updateIgnoredMessageCounts(String teamId, String channelId) {
    }

    public void updateLearnMoreMessageCounts(String teamId, String channelId) {
    }

    public void register(String teamId, String channelId) {

    }
}
