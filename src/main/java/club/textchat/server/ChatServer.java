/*
 * Copyright (c) 2008-2020 The Aspectran Project
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

import club.textchat.persistence.ChatMessagePersistence;
import club.textchat.recaptcha.ReCaptchaVerifier;
import club.textchat.server.codec.ChatMessageDecoder;
import club.textchat.server.codec.ChatMessageEncoder;
import club.textchat.server.model.ChatMessage;
import com.aspectran.core.component.bean.annotation.Autowired;
import com.aspectran.core.component.bean.annotation.Component;
import com.aspectran.web.socket.jsr356.AspectranConfigurator;

import javax.websocket.CloseReason;
import javax.websocket.OnClose;
import javax.websocket.OnError;
import javax.websocket.OnMessage;
import javax.websocket.OnOpen;
import javax.websocket.Session;
import javax.websocket.server.ServerEndpoint;
import java.io.IOException;

/**
 * WebSocket endpoint for the chat server.
 *
 * <p>Created: 29/09/2019</p>
 */
@Component
@ServerEndpoint(
        value = "/chat",
        encoders = ChatMessageEncoder.class,
        decoders = ChatMessageDecoder.class,
        configurator = AspectranConfigurator.class
)
public class ChatServer extends ChatService {

    @Autowired
    public ChatServer(ChatMessagePersistence chatMessagePersistence) {
        super(chatMessagePersistence);
    }

    @OnOpen
    public void onOpen(Session session) throws IOException {
        String recaptchaResponse = session.getQueryString();
        boolean success = ReCaptchaVerifier.verifySuccess(recaptchaResponse);
        if (!success) {
            String reason = "reCAPTCHA verification failed";
            session.close(new CloseReason(CloseReason.CloseCodes.CANNOT_ACCEPT, reason));
            throw new IOException(reason);
        }
    }

    @OnMessage
    public void onMessage(Session session, ChatMessage chatMessage) throws Exception {
        handle(session, chatMessage);
    }

    @OnClose
    public void onClose(Session session, CloseReason reason) throws Exception {
        close(session, reason);
    }

    @OnError
    public void onError(Session session, Throwable error) {
        error(session, error);
    }

}
