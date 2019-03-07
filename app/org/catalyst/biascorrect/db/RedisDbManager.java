package org.catalyst.biascorrect.db;

import redis.clients.jedis.Jedis;
import redis.clients.jedis.JedisPool;
import redis.clients.jedis.JedisPoolConfig;

public class RedisDbManager extends DbManager {

    private JedisPool _jedisPool;

    public RedisDbManager() {
        String redisHost = System.getenv("BIAS_CORRECT_REDIS_HOST");
        int redisPort = Integer.valueOf(System.getenv("BIAS_CORRECT_REDIS_PORT"));
        _jedisPool = new JedisPool(
                new JedisPoolConfig(),
                redisHost,
                redisPort
        );
    }

    public String getType() {
        return "redis";
    }

    @Override
    public void addUserToken(String teamId, String userId, String token) {
        try (Jedis jedis = _jedisPool.getResource()) {
            jedis.hset(
                    "user_tokens",
                    String.format("%s_%s", teamId, userId),
                    token
            );
        }
    }

    public String getUserToken(String teamId, String userId) {
        String userToken = null;
        try (Jedis jedis = _jedisPool.getResource()) {
            userToken = jedis.hget(
                    "user_tokens",
                    String.format("%s_%s", teamId, userId)
            );
        }
        return userToken;
    }

    @Override
    public void addTeamToken(String teamId, String token) {
        try (Jedis jedis = _jedisPool.getResource()) {
            String teamToken = jedis.hget(
                    "team_tokens",
                    teamId
            );
            if (teamToken == null) {
                jedis.hset(
                        "team_tokens",
                        teamId,
                        token
                );
            }
        }
    }

    public String getTeamToken(String teamId) {
        String teamToken = null;
        try (Jedis jedis = _jedisPool.getResource()) {
            teamToken = jedis.hget(
                    "team_tokens",
                    teamId
            );
        }
        return teamToken;
    }

    public void register(String teamId, String channelId) {
        try (Jedis jedis = _jedisPool.getResource()) {
            jedis.sadd("slack_teams", teamId);
            jedis.sadd(String.format("slack_channels_%s", channelId));
        }
    }

    public void updateMessageCounts(String teamId, String channelId) {
        updateCounts(teamId, channelId, "total");
    }

    public void updateCorrectedMessageCounts(String teamId, String channelId) {
        updateCounts(teamId, channelId, "corrected");
    }

    public void updateIgnoredMessageCounts(String teamId, String channelId) {
        updateCounts(teamId, channelId, "ignored");
    }

    public void updateLearnMoreMessageCounts(String teamId, String channelId) {
        updateCounts(teamId, channelId, "learn_more");
    }

    private void updateCounts(String teamId, String channelId, String prefix) {
        String dayKey = today();
        try (Jedis jedis = _jedisPool.getResource()) {
            jedis.hincrBy(
                    String.format("%s_team_messages", prefix),
                    teamId,
                    1L
            );
            jedis.hincrBy(
                    String.format("%s_channel_messages", prefix),
                    String.format("%s_%s", teamId, channelId),
                    1L
            );
            jedis.hincrBy(
                    String.format("%s_team_daily_messages", prefix),
                    String.format("%s_%s", teamId, dayKey),
                    1L
            );
            jedis.hincrBy(
                    String.format("%s_channel_daily_messages", prefix),
                    String.format("%s_%s_%s", teamId, channelId, dayKey),
                    1L
            );
            jedis.hincrBy(
                    String.format("%s_daily_messages", prefix),
                    String.format("%s_%s", teamId, dayKey),
                    1L
            );
            jedis.incrBy(
                    String.format("%s_messages", prefix),
                    1L
            );
        }
    }
}
