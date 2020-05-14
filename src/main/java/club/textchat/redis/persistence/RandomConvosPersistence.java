package club.textchat.redis.persistence;

import club.textchat.redis.RedisConnectionPool;
import club.textchat.redis.pubsub.RandomMessageSubscriber;
import club.textchat.server.message.ChatMessage;

import java.util.List;

/**
 * <p>Created: 2020/05/03</p>
 */
public class RandomConvosPersistence extends AbstractPersistence implements ConvosPersistence {

    public RandomConvosPersistence(RedisConnectionPool connectionPool) {
        super(connectionPool);
    }

    @Override
    public void put(String roomId, ChatMessage message) {
        publish(RandomMessageSubscriber.CHANNEL, message.toString());
    }

    @Override
    public List<ChatMessage> getRecentConvo(String roomId) {
        throw new UnsupportedOperationException();
    }

}
