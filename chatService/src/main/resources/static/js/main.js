'use strict';

var usernamePage = document.querySelector('#username-page');
var chatPage = document.querySelector('#chat-page');
var usernameForm = document.querySelector('#usernameForm');
var messageForm = document.querySelector('#messageForm');
var messageInput = document.querySelector('#message');
var recipientInput = document.querySelector('#recipient');
var messageArea = document.querySelector('#messageArea');
var chatsArea = document.querySelector('#chat-list-container');
var connectingElement = document.querySelector('.connecting');
var recipientForm = document.querySelector("#recipientForm");

var stompClient = null;
var username = null;
var recipient = null;
var chats = [];

var colors = [
    '#2196F3', '#32c787', '#00BCD4', '#ff5652',
    '#ffc107', '#ff85af', '#FF9800', '#39bbb0'
];

function connect(event) {
    username = document.querySelector('#name').value.trim();

    if(username) {
        usernamePage.classList.add('hidden');
        chatPage.classList.remove('hidden');

        var socket = new SockJS('/ws?token=' + username);
        stompClient = Stomp.over(socket);

        stompClient.connect({}, onConnected, onError);
    }
    event.preventDefault();
}

function addChat(username) {
    var userChat = document.createElement('li');

    userChat.classList.add('user-chat');

    var avatarElement = document.createElement('i');
    var avatarText = document.createTextNode(username[0]);
    avatarElement.appendChild(avatarText);
    avatarElement.style['background-color'] = getAvatarColor(username);

    userChat.appendChild(avatarElement);

    var usernameElement = document.createElement('span');
    var usernameText = document.createTextNode(username);
    usernameElement.appendChild(usernameText);
    userChat.appendChild(usernameElement);

    chatsArea.appendChild(userChat);
}

function onConnected() {
    fetch(`/chat/list/${username}`)
          .then(response => response.json())
          .then(data => {
            chats = data;
            chats.forEach(c => addChat(c));
            console.log("Chat messages:", data);
          })
          .catch(error => {
            console.error("Error fetching messages:", error);
          });

    stompClient.subscribe('/user/queue/messages', onMessageReceived);

    connectingElement.classList.add('hidden');
}


function onError(error) {
    connectingElement.textContent = 'Could not connect to WebSocket server. Please refresh this page to try again!';
    connectingElement.style.color = 'red';
}


function sendMessage(event) {
    var messageContent = messageInput.value.trim();
    if(messageContent && stompClient) {
        var chatMessage = {
            'senderName': username,
            'recipientName': recipient,
            'content': messageInput.value,
            'date': new Date().toISOString()
        };
        stompClient.send("/app/chat", {}, JSON.stringify(chatMessage));
        messageInput.value = '';
    }
    event.preventDefault();
}

function onMessageReceived(payload) {
var message = JSON.parse(payload.body);
}



function processMessage(message) {



    if (!chats.includes(message.senderName)) {
        chats.push(message.senderName);
        addChat(message.senderName);
    }

    var messageElement = document.createElement('li');

    messageElement.classList.add('chat-message');

    var avatarElement = document.createElement('i');
    var avatarText = document.createTextNode(message.senderName[0]);
    avatarElement.appendChild(avatarText);
    avatarElement.style['background-color'] = getAvatarColor(message.senderName);

    messageElement.appendChild(avatarElement);

    var usernameElement = document.createElement('span');
    var usernameText = document.createTextNode(message.senderName);
    usernameElement.appendChild(usernameText);
    messageElement.appendChild(usernameElement);

    var textElement = document.createElement('p');
    var messageText = document.createTextNode(message.content);
    textElement.appendChild(messageText);

    messageElement.appendChild(textElement);

    messageArea.appendChild(messageElement);
    messageArea.scrollTop = messageArea.scrollHeight;
}


function getAvatarColor(messageSender) {
    var hash = 0;
    for (var i = 0; i < messageSender.length; i++) {
        hash = 31 * hash + messageSender.charCodeAt(i);
    }
    var index = Math.abs(hash % colors.length);
    return colors[index];
}



usernameForm.addEventListener('submit', connect, true)
messageForm.addEventListener('submit', sendMessage, true)
recipientForm.addEventListener('submit', (event) => {
    recipient = recipientInput.value.trim();
    addChat(recipient);
    event.preventDefault();
}, true)
