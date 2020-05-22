<%@ page contentType="text/html; charset=UTF-8" pageEncoding="UTF-8" %>
<div id="lobby-not-available" class="reveal popup" data-reveal data-close-on-click="false" data-close-on-esc="false">
    <h3>Oops -.-;;</h3>
    <div class="grid-x">
        <div class="cell content">
            <p class="lead">You cannot enter the Text Chat Club's lobby.</p>
            <p>The cause can be one of the following:</p>
            <ul>
                <li>When connecting with older web browser that does not support websockets</li>
                <li>When an abnormal connection is attempted</li>
            </ul>
        </div>
        <div class="cell buttons">
            <a class="button alert" href="/lobby">Retry</a>
        </div>
    </div>
</div>