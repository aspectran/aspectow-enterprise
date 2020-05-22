package club.textchat.lobby;

import club.textchat.room.RoomInfo;
import club.textchat.room.RoomManager;
import club.textchat.server.AdmissionToken;
import club.textchat.user.UserInfo;
import club.textchat.user.UserManager;
import com.aspectran.core.component.bean.annotation.Action;
import com.aspectran.core.component.bean.annotation.Autowired;
import com.aspectran.core.component.bean.annotation.Component;
import com.aspectran.core.component.bean.annotation.Dispatch;
import com.aspectran.core.component.bean.annotation.Request;
import com.aspectran.core.util.security.TimeLimitedPBTokenIssuer;

import java.util.HashMap;
import java.util.List;
import java.util.Map;

@Component
public class LobbyAction {

    private static final String LOBBY_CHATROOM_ID = "0";

    private final UserManager userManager;

    private final RoomManager roomManager;

    @Autowired
    public LobbyAction(UserManager userManager,
                       RoomManager roomManager) {
        this.userManager = userManager;
        this.roomManager = roomManager;
    }

    @Request("/lobby")
    @Dispatch("templates/default")
    @Action("page")
    public Map<String, Object> rooms() {
        UserInfo userInfo = userManager.getUserInfo();
        List<RoomInfo> rooms = roomManager.getRoomList();

        AdmissionToken admissionToken = new AdmissionToken();
        admissionToken.setUserNo(userInfo.getUserNo());
        admissionToken.setUsername(userInfo.getUsername());
        admissionToken.setRoomId(LOBBY_CHATROOM_ID);

        Map<String, Object> map = new HashMap<>();
        map.put("token", TimeLimitedPBTokenIssuer.getToken(admissionToken));
        map.put("rooms", rooms);
        map.put("include", "pages/lobby");
        return map;
    }

}