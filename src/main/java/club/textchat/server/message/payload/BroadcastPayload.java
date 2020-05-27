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
package club.textchat.server.message.payload;

import com.aspectran.core.util.apon.AbstractParameters;
import com.aspectran.core.util.apon.ParameterKey;
import com.aspectran.core.util.apon.ValueType;

/**
 * Represents the payload of a WebSocket frame to broadcast a text message.
 *
 * <p>Created: 2019/10/09</p>
 */
public class BroadcastPayload extends AbstractParameters {

    private static final ParameterKey roomId;
    private static final ParameterKey userNo;
    private static final ParameterKey username;
    private static final ParameterKey content;
    private static final ParameterKey datetime;
    private static final ParameterKey color;

    private static final ParameterKey[] parameterKeys;

    static {
        roomId = new ParameterKey("roomId", ValueType.STRING);
        userNo = new ParameterKey("userNo", ValueType.INT);
        username = new ParameterKey("username", ValueType.STRING);
        content = new ParameterKey("content", ValueType.STRING);
        datetime = new ParameterKey("datetime", ValueType.STRING);
        color = new ParameterKey("color", ValueType.STRING);

        parameterKeys = new ParameterKey[] {
                roomId,
                userNo,
                username,
                content,
                datetime,
                color
        };
    }

    public BroadcastPayload() {
        super(parameterKeys);
    }

    public String getRoomId() {
        return getString(roomId);
    }

    public void setRoomId(String roomId) {
        putValue(BroadcastPayload.roomId, roomId);
    }

    public int getUserNo() {
        return getInt(userNo);
    }

    public void setUserNo(int userNo) {
        putValue(BroadcastPayload.userNo, userNo);
    }

    public String getUsername() {
        return getString(username);
    }

    public void setUsername(String username) {
        putValue(BroadcastPayload.username, username);
    }

    public void setContent(String content) {
        putValue(BroadcastPayload.content, content);
    }

    public void setDatetime(String datetime) {
        putValue(BroadcastPayload.datetime, datetime);
    }
    public void setColor(String color) {
        putValue(BroadcastPayload.color, color);
    }

}
