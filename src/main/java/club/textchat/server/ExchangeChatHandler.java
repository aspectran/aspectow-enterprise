/*
 * Copyright (c) 2020 The Aspectran Project
 *
 * Licensed under the Apache License, Version 2.0 (the "License");
 * you may not use this file except in compliance with the License.
 * You may obtain a copy of the License at
 *
 *     http://www.apache.org/licenses/LICENSE-2.0
 *
 * Unless required by applicable law or agreed to in writing, software
 * distributed under the License is distributed on an "AS IS" BASIS,
 * WITHOUT WARRANTIES OR CONDITIONS OF ANY KIND, either express or implied.
 * See the License for the specific language governing permissions and
 * limitations under the License.
 */
package club.textchat.server;

import club.textchat.redis.persistence.ChatersPersistence;
import club.textchat.redis.persistence.ExchangeChatPersistence;
import club.textchat.redis.persistence.InConvoUsersPersistence;
import club.textchat.redis.persistence.SignedInUsersPersistence;
import club.textchat.server.message.ChatMessage;
import club.textchat.server.message.payload.BroadcastPayload;
import club.textchat.server.message.payload.JoinPayload;
import club.textchat.server.message.payload.MessagePayload;
import club.textchat.server.message.payload.UserJoinedPayload;
import club.textchat.server.message.payload.UserLeftPayload;
import com.aspectran.core.component.bean.annotation.Bean;
import com.aspectran.core.component.bean.annotation.Component;
import com.aspectran.core.util.StringUtils;

import javax.websocket.CloseReason;
import javax.websocket.Session;
import java.util.Set;
import java.util.concurrent.atomic.AtomicInteger;

/**
 * <p>Created: 2020/05/14</p>
 */
@Component
@Bean
public class ExchangeChatHandler extends AbstractChatHandler {

    public static final String CHAT_REQUEST = "request:";

    public static final String CHAT_REQUEST_DECLINED = "request-declined:";

    public static final String CHAT_REQUEST_CANCELED = "request-canceled:";

    public static final String CHAT_REQUEST_ACCEPTED = "request-accepted:";

    public static final String BROADCAST_MESSAGE_PREFIX = "broadcast:";

    public static final String EXCHANGE_LANGUAGES_SEPARATOR = ":";

    private static final String PRIVATE_ROOM_ID_PREFIX = "exc:";

    private static final AtomicInteger privateRoomNumber = new AtomicInteger();

    private final ExchangeChatPersistence exchangeChatPersistence;

    public ExchangeChatHandler(SignedInUsersPersistence signedInUsersPersistence,
                               InConvoUsersPersistence inConvoUsersPersistence,
                               ChatersPersistence chatersPersistence,
                               ExchangeChatPersistence exchangeChatPersistence) {
        super(signedInUsersPersistence, inConvoUsersPersistence, chatersPersistence);
        this.exchangeChatPersistence = exchangeChatPersistence;
    }

    protected void handle(Session session, ChatMessage chatMessage) {
        if (heartBeat(session, chatMessage)) {
            return;
        }
        MessagePayload payload = chatMessage.getMessagePayload();
        if (payload != null) {
            ChaterInfo chaterInfo = getChaterInfo(session);
            switch (payload.getType()) {
                case POST:
                    String content = payload.getContent();
                    if (!StringUtils.isEmpty(content)) {
                        if (content.startsWith(CHAT_REQUEST)) {
                            int targetUserNo = parseTargetUserNo(content);
                            if (!chatersPersistence.isChater(swapExchangeRoomId(chaterInfo), targetUserNo)) {
                                sendChatRequestMessage(chaterInfo, CHAT_REQUEST_CANCELED, targetUserNo);
                            } else {
                                broadcastChatRequestMessage(chaterInfo, content);
                            }
                        } else if (content.startsWith(CHAT_REQUEST_DECLINED) ||
                                content.startsWith(CHAT_REQUEST_CANCELED) ||
                                content.startsWith(CHAT_REQUEST_ACCEPTED)) {
                            broadcastMessage(chaterInfo, content);
                        } else {
                            broadcastMessage(chaterInfo, BROADCAST_MESSAGE_PREFIX + payload.getContent());
                        }
                    }
                    break;
                case JOIN:
                    String username = chaterInfo.getUsername();
                    String username2 = payload.getUsername();
                    if (!username.equals(username2)) {
                        sendAbort(session, chaterInfo, "abnormal");
                        return;
                    }
                    if (existsChater(chaterInfo)) {
                        if (!checkSameUser(chaterInfo)) {
                            sendAbort(session, chaterInfo, "exists");
                            return;
                        }
                        Session session2 = chaters.get(chaterInfo);
                        if (session2 != null) {
                            sendAbort(session2, chaterInfo, "rejoin");
                        }
                        if (!join(session, chaterInfo, true)) {
                            broadcastUserJoined(chaterInfo);
                        }
                    } else {
                        join(session, chaterInfo, false);
                        broadcastUserJoined(chaterInfo);
                    }
                    break;
                default:
                    sendAbort(session, chaterInfo, "abnormal");
            }
        }
    }

    protected void close(Session session, CloseReason reason) {
        ChaterInfo chaterInfo = getChaterInfo(session);
        leave(session, chaterInfo);
    }

    private boolean join(Session session, ChaterInfo chaterInfo, boolean rejoin) {
        boolean replaced = false;
        if (session.isOpen()) {
            if (chaters.put(chaterInfo, session) != null) {
                replaced = true;
            }
            inConvoUsersPersistence.put(chaterInfo.getHttpSessionId(), chaterInfo.getRoomId());
            chatersPersistence.put(chaterInfo);
            String exchangeRoomId = swapExchangeRoomId(chaterInfo);
            Set<String> roomChaters = chatersPersistence.getChaters(exchangeRoomId);
            JoinPayload payload = new JoinPayload();
            payload.setChater(chaterInfo);
            payload.setChaters(roomChaters);
            payload.setRejoin(rejoin);
            send(session, new ChatMessage(payload));
        }
        return replaced;
    }

    private void leave(Session session, ChaterInfo chaterInfo) {
        if (chaters.remove(chaterInfo, session)) {
            chatersPersistence.remove(chaterInfo);
            signedInUsersPersistence.tryAbandon(chaterInfo.getUsername(), chaterInfo.getHttpSessionId());
            inConvoUsersPersistence.remove(chaterInfo.getHttpSessionId());
            broadcastUserLeft(chaterInfo);
        }
    }

    private void broadcastUserJoined(ChaterInfo chaterInfo) {
        UserJoinedPayload payload = new UserJoinedPayload();
        payload.setChater(chaterInfo);
        payload.setDatetime(getCurrentDatetime(chaterInfo));
        ChatMessage message = new ChatMessage(payload);
        exchangeChatPersistence.publish(message);
    }

    private void broadcastUserLeft(ChaterInfo chaterInfo) {
        UserLeftPayload payload = new UserLeftPayload();
        payload.setChater(chaterInfo);
        payload.setDatetime(getCurrentDatetime(chaterInfo));
        ChatMessage message = new ChatMessage(payload);
        exchangeChatPersistence.publish(message);
    }

    private void broadcastMessage(ChaterInfo chaterInfo, String content) {
        BroadcastPayload payload = new BroadcastPayload();
        payload.setChater(chaterInfo);
        payload.setContent(content);
        payload.setDatetime(getCurrentDatetime(chaterInfo));
        ChatMessage message = new ChatMessage(payload);
        exchangeChatPersistence.publish(message);
    }

    private void broadcastChatRequestMessage(ChaterInfo chaterInfo, String content) {
        broadcastMessage(chaterInfo, content + ":" + chaterInfo.serialize());
    }

    private void sendChatRequestMessage(ChaterInfo chaterInfo, String requestType, int userNo) {
        BroadcastPayload payload = new BroadcastPayload();
        payload.setChater(chaterInfo);
        payload.setContent(requestType + ":" + userNo);
        ChatMessage message = new ChatMessage(payload);
        String exchangeRoomId = swapExchangeRoomId(chaterInfo);
        send(message, exchangeRoomId, chaterInfo.getUserNo());
    }

    public static int parseTargetUserNo(String content) {
        try {
            String prefix = content.substring(0, content.indexOf(":") + 1);
            int start = prefix.length();
            int end = content.indexOf(":", start);
            if (end == -1) {
                end = content.length();
            }
            return Integer.parseInt(content.substring(start, end));
        } catch (NumberFormatException e) {
            return 0;
        }
    }

    public static String nextPrivateRoomId() {
        int roomNum = privateRoomNumber.incrementAndGet();
        return PRIVATE_ROOM_ID_PREFIX + roomNum;
    }

    public static String swapExchangeRoomId(ChaterInfo chaterInfo) {
        return makeExchangeRoomId(chaterInfo.getConvoLang(), chaterInfo.getNativeLang());
    }

    public static String makeExchangeRoomId(String nativeLang, String convoLang) {
        return nativeLang + EXCHANGE_LANGUAGES_SEPARATOR + convoLang;
    }

    public static String swapExchangeRoomId(String exchangeRoomId) {
        String[] langCodes = StringUtils.split(exchangeRoomId, EXCHANGE_LANGUAGES_SEPARATOR, 2);
        return makeExchangeRoomId(langCodes[1], langCodes[0]);
    }

}
