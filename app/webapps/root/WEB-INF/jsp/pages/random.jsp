<%@ page contentType="text/html; charset=UTF-8" pageEncoding="UTF-8" %>
<%@ taglib uri="http://java.sun.com/jsp/jstl/core" prefix="c" %>
<link rel="stylesheet" type="text/css" href="/assets/css/chat-common.css?v14" />
<link rel="stylesheet" type="text/css" href="/assets/css/chat-random.css?v14" />
<script src="/assets/js/chat-client-default.js?v1.1"></script>
<script src="/assets/js/chat-client-random.js?v1.2"></script>
<script>
    const chatClientSettings = {
        serverEndpoint: "/chat/random/",
        autoConnectEnabled: false,
        admissionToken: "${page.token}",
        homepage: "/"
    }
</script>
<div class="grid-y grid-frame random">
    <%@ include file="includes/chat-header.jsp" %>
    <div class="body shadow cell auto cell-block-container">
        <div class="grid-x full-height">
            <div class="sidebar cell medium-4 large-3 cell-block-y hide-for-small-only">
                <ul id="contacts"></ul>
            </div>
            <div class="cell auto cell-block-y">
                <div id="convo" class="grid-container full-height"></div>
            </div>
        </div>
    </div>
    <div class="footer shadow cell">
        <div class="grid-x grid-padding-x grid-padding-y">
            <div class="sidebar cell medium-4 large-3 cell-block-y hide-for-small-only">
                <%@ include file="includes/sidebar-user.jsp" %>
            </div>
            <div class="message-box cell auto cell-block-y">
                <form id="form-send-message">
                    <div class="input-group">
                        <input id="message" class="input-group-field" type="text" maxlength="150" autocomplete="off" placeholder="Enter your message"/>
                        <input id="for-automata-clear" type="text"/>
                        <div class="input-group-button">
                            <button type="submit" class="button send" title="Send message"><i class="icon-paper-plane"></i></button>
                            <button type="button" class="button next" title="Search for another stranger"><i class="fi-shuffle"></i> Next <i class="fi-torso"></i></button>
                        </div>
                    </div>
                </form>
            </div>
        </div>
    </div>
</div>
<%@ include file="includes/chat-duplicate-join.jsp" %>
<%@ include file="includes/common-wait-popup.jsp" %>
<%@ include file="includes/common-notice-popup.jsp" %>
<%@ include file="includes/common-connection-lost.jsp" %>
<%@ include file="includes/common-browser-not-supported.jsp" %>
<c:if test="${empty user}">
    <%@ include file="includes/common-sign-in.jsp" %>
</c:if>