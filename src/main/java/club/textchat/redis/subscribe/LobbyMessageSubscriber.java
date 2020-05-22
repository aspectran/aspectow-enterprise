package club.textchat.redis.subscribe;

import club.textchat.redis.RedisConnectionPool;
import club.textchat.server.DefaultChatHandler;
import club.textchat.server.LobbyChatHandler;
import club.textchat.server.message.ChatMessage;
import club.textchat.server.message.payload.BroadcastPayload;
import club.textchat.server.message.payload.UserJoinedPayload;
import club.textchat.server.message.payload.UserLeftPayload;
import com.aspectran.core.component.bean.ablility.DisposableBean;
import com.aspectran.core.component.bean.ablility.InitializableBean;
import com.aspectran.core.component.bean.annotation.Autowired;
import com.aspectran.core.component.bean.annotation.Bean;
import com.aspectran.core.component.bean.annotation.Component;
import com.aspectran.core.util.logging.Logger;
import com.aspectran.core.util.logging.LoggerFactory;
import io.lettuce.core.pubsub.RedisPubSubAdapter;
import io.lettuce.core.pubsub.StatefulRedisPubSubConnection;
import io.lettuce.core.pubsub.api.sync.RedisPubSubCommands;

import java.io.IOException;

/**
 * <p>Created: 2020/05/10</p>
 */
@Component
@Bean
public class LobbyMessageSubscriber extends RedisPubSubAdapter<String, String>
        implements InitializableBean, DisposableBean {

    private static final Logger logger = LoggerFactory.getLogger(LobbyMessageSubscriber.class);

    public static final String CHANNEL = "chat:lobby";

    private final StatefulRedisPubSubConnection<String, String> connection;

    private final LobbyChatHandler chatHandler;

    @Autowired
    public LobbyMessageSubscriber(RedisConnectionPool connectionPool, LobbyChatHandler chatHandler) {
        this.connection = connectionPool.getPubSubConnection();
        this.chatHandler = chatHandler;
    }

    @Override
    public void message(String channel, String message) {
        if (logger.isTraceEnabled()) {
            logger.trace(channel + ": " + message);
        }
        ChatMessage chatMessage;
        try {
            chatMessage = new ChatMessage(message);
        } catch (IOException e) {
            logger.warn(e);
            return;
        }
        BroadcastPayload broadcastPayload = chatMessage.getBroadcastPayload();
        if (broadcastPayload != null) {
            chatHandler.broadcast(chatMessage);
            return;
        }
        UserJoinedPayload userJoinedPayload = chatMessage.getUserJoinedPayload();
        if (userJoinedPayload != null) {
            chatHandler.broadcast(chatMessage);
            return;
        }
        UserLeftPayload userLeftPayload = chatMessage.getUserLeftPayload();
        if (userLeftPayload != null) {
            chatHandler.broadcast(chatMessage); // talker already left
        }
    }

    @Override
    public void initialize() throws Exception {
        connection.addListener(this);
        RedisPubSubCommands<String, String> sync = connection.sync();
        sync.subscribe(CHANNEL);
    }

    @Override
    public void destroy() throws Exception {
        if (connection != null) {
            connection.close();
        }
    }

}