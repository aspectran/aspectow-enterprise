<%@ page contentType="text/html; charset=UTF-8" pageEncoding="UTF-8" %>
<div class="wrap row">
    <div class="header columns">
        <h2>Chat <span id="totalPeople"></span></h2>
        <a class="leave" onclick="leaveRoom();">Leave</a>
    </div>
    <div class="sidebar columns medium-4 large-3 hide-for-small-only">
        <div id="contacts"></div>
    </div>
    <div class="main columns small-12 medium-8 large-9">
        <form id="sign-in" method="post" onsubmit="return false;">
            <h3>Type your username</h3>
            <input type="text" id="username" maxlength="50" placeholder="Username" autocomplete="off" autofocus/>
            <button class="button" type="submit" onclick="signIn()">Start Chatting</button>
            <div id="inline-badge"></div>
        </form>
        <div id="messages"></div>
        <form id="chat-controls" onsubmit="sendMessage();return false;">
            <div class="input-group">
                <input class="input-group-field" type="text" id="message" autocomplete="off" placeholder="Type a message..."/>
                <div class="input-group-button">
                    <button type="submit" class="button">Send</button>
                </div>
            </div>
        </form>
    </div>
</div>
<script src="https://www.google.com/recaptcha/api.js?render=explicit&onload=onRecaptchaLoadCallback"></script>
<script>
    function onRecaptchaLoadCallback() {
        var clientId = grecaptcha.render('inline-badge', {
            'sitekey': '6Ldt0r0UAAAAAP4ejDGFZLB0S-zDzWL3ZkB49FvN',
            'badge': 'inline',
            'size': 'invisible'
        });
        grecaptcha.ready(function() {
            grecaptcha.execute(clientId, {
                action: 'sign_in'
            }).then(function(token) {
                recaptchaResponse = token;
            });
        });
    }
</script>
<script>
    var recaptchaResponse;
    var socket;
    var currentUser;

    function signIn() {
        if (!recaptchaResponse) {
            return;
        }
        currentUser = $("#username").val().trim();
        $("#username").val("");
        if (currentUser) {
            $("#sign-in").hide();
            $("#messages").show();
            $("#chat-controls").show();
            $("a.leave").show();
            $("#message").focus();
            openSocket();
        }
    }

    function openSocket() {
        if (socket) {
            socket.close();
        }
        var url = new URL('/chat?' + recaptchaResponse, location.href);
        url.protocol = url.protocol.replace('https:', 'wss:');
        url.protocol = url.protocol.replace('http:', 'ws:');
        socket = new WebSocket(url.href);
        socket.onopen = function (event) {
            var chatMessage = {
                sendTextMessage: {
                    type: 'JOIN',
                    username: currentUser
                }
            };
            socket.send(JSON.stringify(chatMessage));
        };

        socket.onmessage = function (event) {
            if (typeof event.data === "string") {
                var chatMessage = JSON.parse(event.data);
                Object.getOwnPropertyNames(chatMessage).forEach(function(val, idx, array) {
                    var payload = chatMessage[val];
                    if (payload) {
                        switch (val) {
                            case "welcomeUser":
                                displayConnectedUserMessage(payload.username);
                                break;
                            case "duplicatedUser":
                                socket.close();
                                alert("Duplicated user: " + payload.username);
                                location.reload();
                                break;
                            case "broadcastTextMessage":
                                displayMessage(payload.username, payload.content);
                                break;
                            case "broadcastConnectedUser":
                                displayConnectedUserMessage(payload.username);
                                break;
                            case "broadcastDisconnectedUser":
                                displayDisconnectedUserMessage(payload.username);
                                break;
                            case "broadcastAvailableUsers":
                                cleanAvailableUsers();
                                for (var i = 0; i < payload.usernames.length; i++) {
                                    addAvailableUsers(payload.usernames[i]);
                                }
                                break;
                        }
                    }
                });
            }
        };

        socket.onclose = function (event) {
            location.reload();
        };

        socket.onerror = function (event) {
            console.error("WebSocket error observed:", event);
            displayErrorMessage('Could not connect to WebSocket server. Please refresh this page to try again!');
        };
    }

    function leaveRoom() {
        socket.close();
    }

    function sendMessage() {
        var text = $("#message").val().trim();
        $("#message").val('');

        if (text) {
            var chatMessage = {
                sendTextMessage: {
                    type: 'CHAT',
                    username: currentUser,
                    content: text
                }
            };
            socket.send(JSON.stringify(chatMessage));
        }
    }

    function displayMessage(username, text) {
        var sentByCurrentUer = (currentUser === username);
        var message = $("<div/>").addClass(sentByCurrentUer === true ? "message sent" : "message received");
        message.data("sender", username);

        var sender = $("<span/>").addClass("sender");
        sender.text(sentByCurrentUer === true ? "You" : username);
        sender.appendTo(message);

        var content = $("<span/>").addClass("content").text(text);
        content.appendTo(message);

        var lastMessage = $("#messages .message").last();
        if (lastMessage.length && lastMessage.data("sender") === username) {
            message.addClass("same-sender-previous-message");
        }

        $("#messages").append(message);
        $("#messages").animate({scrollTop: $("#messages").prop("scrollHeight")});
    }

    function displayConnectedUserMessage(username) {
        var sentByCurrentUer = currentUser === username;
        var text = (sentByCurrentUer === true ? "Welcome <strong>" + username : username + "</strong> joined the chat");
        displayEventMessage(text);
    }

    function displayDisconnectedUserMessage(username) {
        var text = "<strong>" + username + "</strong> left the chat";
        displayEventMessage(text);
    }

    function addAvailableUsers(username) {
        var contact = $("<div/>").addClass("contact");
        var status = $("<div/>").addClass("status");
        var name = $("<span/>").addClass("name").text(username);
        contact.append(status).append(name).appendTo($("#contacts"));
        updateTotalPeople();
    }

    function cleanAvailableUsers() {
        $("#contacts").empty();
        clearTotalPeople();
    }

    function updateTotalPeople() {
        $("#totalPeople").text("(" + $("#contacts .contact").length + ")");
    }

    function clearTotalPeople() {
        $("#totalPeople").text("");
    }

    function displayEventMessage(text) {
        var div = $("<div/>").addClass("message event");
        $("<p/>").addClass("content").html(text).appendTo(div);
        $("#messages").append(div);
        $("#messages").animate({scrollTop: $("#messages").prop("scrollHeight")});
    }

    function displayErrorMessage(text) {
        var div = $("<div/>").addClass("message event error");
        $("<p/>").addClass("content").html(text).appendTo(div);
        $("#messages").append(div);
        $("#messages").animate({scrollTop: $("#messages").prop("scrollHeight")});
    }
</script>