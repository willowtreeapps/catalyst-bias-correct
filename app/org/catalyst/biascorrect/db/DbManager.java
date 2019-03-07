package org.catalyst.biascorrect.db;

import org.joda.time.format.DateTimeFormat;
import org.joda.time.format.DateTimeFormatter;

public abstract class DbManager {

    public String getType() {
        throw new UnsupportedOperationException();
    }

    public void addTeamToken(String teamId, String token) {
        throw new UnsupportedOperationException();
    }

    public void addUserToken(String teamId, String userId, String token) {
        throw new UnsupportedOperationException();
    }

    public String getUserToken(String teamId, String userId) {
        throw new UnsupportedOperationException();
    }

    public String getTeamToken(String teamId) {
        throw new UnsupportedOperationException();
    }

    public void register(String teamId, String channelId) {
        throw new UnsupportedOperationException();
    }

    public void updateMessageCounts(String teamId, String channelId) {
        throw new UnsupportedOperationException();
    }

    public void updateCorrectedMessageCounts(String teamId, String channelId) {
        throw new UnsupportedOperationException();
    }

    public void updateIgnoredMessageCounts(String teamId, String channelId) {
        throw new UnsupportedOperationException();
    }

    public void updateLearnMoreMessageCounts(String teamId, String channelId) {
        throw new UnsupportedOperationException();
    }

    private static DbManager INSTANCE = null;

    public static DbManager getInstance() {
        return INSTANCE;
    }

    public static void init() {
        String dbManagerType = System.getenv("ESKALERA_BIAS_CORRECT_DB_MANAGER_TYPE");
        if (dbManagerType.equals("redis")) {
            INSTANCE = new RedisDbManager();
        } else {
            INSTANCE = new LocalDbManager();
        }
    }

    static DateTimeFormatter DATE_FORMATTTER = DateTimeFormat.forPattern("yyyy-MM-dd");

    public static String today() {
        return DATE_FORMATTTER.print(System.currentTimeMillis());
    }
}
