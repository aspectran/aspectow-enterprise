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
package club.textchat.room;

import club.textchat.common.mybatis.SimpleSqlSession;
import club.textchat.redis.persistence.LobbyChatPersistence;
import com.aspectran.core.component.bean.annotation.Autowired;
import com.aspectran.core.component.bean.annotation.Component;
import com.aspectran.core.util.PBEncryptionUtils;
import com.aspectran.core.util.json.JsonWriter;
import org.apache.ibatis.session.SqlSession;

import java.io.IOException;
import java.util.List;

/**
 * <p>Created: 2020/05/18</p>
 */
@Component
public class PrivateRoomManager {

    private static final String NEW_ROOM_MESSAGE_PREFIX = "newPrivateRoom:";

    private final SqlSession sqlSession;

    private final LobbyChatPersistence lobbyChatPersistence;

    @Autowired
    public PrivateRoomManager(SimpleSqlSession sqlSession, LobbyChatPersistence lobbyChatPersistence) {
        this.sqlSession = sqlSession;
        this.lobbyChatPersistence = lobbyChatPersistence;
    }

    public RoomInfo getRoomInfo(String roomId) {
        RoomInfo roomInfo = sqlSession.selectOne("private.rooms.getRoomInfo", roomId);
        roomInfo.setEncryptedRoomId(PBEncryptionUtils.encrypt(Integer.toString(roomInfo.getRoomId())));
        return roomInfo;
    }

    public String createRoom(RoomInfo roomInfo) {
        int affected = sqlSession.insert("private.rooms.insertRoom", roomInfo);
        if (affected != 1) {
            return null;
        }
        return PBEncryptionUtils.encrypt(Integer.toString(roomInfo.getRoomId()));
    }

    public void checkIn(String roomId) {
        sqlSession.update("private.rooms.updateRoomForCheckIn", roomId);
    }

    public void checkOut(String roomId) {
        sqlSession.update("private.rooms.updateRoomForCheckOut", roomId);
    }

}
