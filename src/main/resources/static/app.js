let stompClient = null;
let currentUserId = null;
let currentUserName = null;
let currentUserToken = null;
let subscriptions = new Set();
let notifications = [];

// Toast Helper
function showToast(message, type = 'info') {
    const container = document.getElementById("toastContainer");
    const toast = document.createElement("div");
    toast.className = `toast ${type}`;
    toast.innerText = message;
    container.appendChild(toast);
    
    setTimeout(() => {
        toast.classList.add("fade-out");
        toast.addEventListener("animationend", () => toast.remove());
    }, 3500);
}

// Screen toggles
function showScreen(screenId) {
    document.querySelectorAll('.screen').forEach(s => s.classList.remove('active'));
    document.getElementById(screenId).classList.add('active');
}

// Connection Action
function handleConnect(event) {
    event.preventDefault();
    const userIdInput = document.getElementById("inputUserId").value;
    const userNameInput = document.getElementById("inputUserName").value;
    if (!userIdInput) return;

    const uId = parseInt(userIdInput);
    
    // Step 1: Perform Auth Login / Registration
    fetch("/api/auth/login", {
        method: "POST",
        headers: { "Content-Type": "application/json" },
        body: JSON.stringify({ id: uId, name: userNameInput })
    })
    .then(res => {
        if (!res.ok) {
            throw new Error("Authentication failed");
        }
        return res.json();
    })
    .then(user => {
        currentUserId = user.id;
        currentUserName = user.name;
        currentUserToken = user.token;
        
        showToast(`Authenticated as ${currentUserName}! Connecting to real-time service...`, "success");
        connectWebSocket();
    })
    .catch(err => {
        console.error(err);
        showToast("Failed to authenticate with backend server.", "error");
    });
}

function connectWebSocket() {
    const socket = new SockJS("/ws");
    stompClient = Stomp.over(socket);
    
    // Suppress STOMP console debug logs to keep browser console clean
    stompClient.debug = null;

    const statusDot = document.getElementById("statusDot");
    const labelUserConnected = document.getElementById("labelUserConnected");

    // Pass login & auth-token headers
    stompClient.connect({ 
        login: currentUserId.toString(),
        "auth-token": currentUserToken
    }, (frame) => {
        statusDot.classList.add("online");
        labelUserConnected.innerText = `${currentUserName} (User ${currentUserId})`;
        
        showScreen("screenDashboard");
        showToast("Connected to real-time notifications!", "success");

        // Subscribe to personal notification queue
        stompClient.subscribe("/user/queue/notifications", (message) => {
            const notif = JSON.parse(message.body);
            handleIncomingNotification(notif);
        });

        // Retrieve history and current subscriptions
        fetchNotificationHistory();
        loadLocalSubscriptions();
    }, (error) => {
        console.error("STOMP Error:", error);
        statusDot.classList.remove("online");
        showToast("Real-time connection failed/refused.", "error");
        handleDisconnect();
    });
}

function handleDisconnect() {
    if (stompClient !== null) {
        stompClient.disconnect(() => {
            showToast("Disconnected from WebSocket server.", "info");
        });
        stompClient = null;
    }
    
    // Clean up state
    currentUserId = null;
    currentUserName = null;
    currentUserToken = null;
    subscriptions.clear();
    notifications = [];
    
    document.getElementById("statusDot").classList.remove("online");
    showScreen("screenConnect");
}

// Subscriptions Management
function loadLocalSubscriptions() {
    updateSubscriptionsUI();
}

function handleSubscribe(event) {
    event.preventDefault();
    const channelIdInput = document.getElementById("inputChannelId");
    const channelId = parseInt(channelIdInput.value);
    if (!channelId) return;

    // Call REST API with auth header
    fetch("/api/subscriptions/subscribe", {
        method: "POST",
        headers: { 
            "Content-Type": "application/json",
            "X-Auth-Token": currentUserToken
        },
        body: JSON.stringify({ userId: currentUserId, channelId: channelId })
    })
    .then(res => {
        if (res.ok) {
            subscriptions.add(channelId);
            updateSubscriptionsUI();
            channelIdInput.value = "";
            showToast(`Subscribed to Channel ${channelId}!`, "success");
        } else {
            showToast("Subscription failed (Unauthorized).", "error");
        }
    })
    .catch(err => {
        console.error(err);
        showToast("Network error occurred during subscription.", "error");
    });
}

function updateSubscriptionsUI() {
    const list = document.getElementById("subList");
    if (subscriptions.size === 0) {
        list.innerHTML = `<li class="sub-empty">No active subscriptions</li>`;
        return;
    }

    list.innerHTML = "";
    subscriptions.forEach(channelId => {
        const li = document.createElement("li");
        li.className = "sub-item";
        li.innerHTML = `
            <span>Channel</span>
            <span class="channel-tag">${channelId}</span>
        `;
        list.appendChild(li);
    });
}

// Notification management
function fetchNotificationHistory() {
    fetch(`/api/notifications?userId=${currentUserId}`, {
        headers: { "X-Auth-Token": currentUserToken }
    })
    .then(res => {
        if (!res.ok) {
            throw new Error("Unauthorized history access");
        }
        return res.json();
    })
    .then(data => {
        notifications = data;
        // Sort by timestamp desc
        notifications.sort((a, b) => new Date(b.timestamp) - new Date(a.timestamp));
        renderNotifications();
    })
    .catch(err => {
        console.error(err);
        showToast("Failed to fetch notification history (Unauthorized).", "error");
    });
}

function handleIncomingNotification(notif) {
    // Check if it already exists to avoid duplicates
    if (!notifications.some(n => n.id === notif.id)) {
        notifications.unshift(notif); // Prepend new notification
        renderNotifications();
        showToast(`New Notification from Channel ${notif.channelId}!`);
    }
}

function renderNotifications() {
    const container = document.getElementById("feedContainer");
    const emptyState = document.getElementById("feedEmptyState");
    const unreadBadge = document.getElementById("notifUnreadBadge");

    if (notifications.length === 0) {
        emptyState.style.display = "flex";
        // Hide actual notifications but keep empty state
        document.querySelectorAll('.notif-card').forEach(el => el.remove());
        unreadBadge.innerText = "0";
        return;
    }

    emptyState.style.display = "none";
    
    // Clean up existing notification cards
    document.querySelectorAll('.notif-card').forEach(el => el.remove());

    let unreadCount = 0;

    notifications.forEach(notif => {
        if (!notif.read) unreadCount++;

        const card = document.createElement("div");
        card.className = `notif-card ${notif.read ? '' : 'unread'}`;
        card.id = `notif-${notif.id}`;

        const date = new Date(notif.timestamp);
        const timeStr = date.toLocaleTimeString([], { hour: '2-digit', minute: '2-digit' }) + ' - ' + date.toLocaleDateString();

        card.innerHTML = `
            <div class="notif-meta">
                <span class="channel-tag">Channel ${notif.channelId}</span>
                <span class="notif-time">${timeStr}</span>
            </div>
            <div class="notif-body">${notif.message}</div>
            ${!notif.read ? `
                <div class="notif-card-actions">
                    <button class="btn-card-read" onclick="handleMarkAsRead('${notif.id}')">Mark as read</button>
                </div>
            ` : ''}
        `;

        container.appendChild(card);
    });

    unreadBadge.innerText = unreadCount;
    const btnMarkAllRead = document.getElementById("btnMarkAllRead");
    if (unreadCount === 0) {
        btnMarkAllRead.style.display = "none";
    } else {
        btnMarkAllRead.style.display = "block";
    }
}

function handleMarkAsRead(id) {
    fetch(`/api/notifications/${id}/read`, {
        method: "PUT",
        headers: { "X-Auth-Token": currentUserToken }
    })
    .then(res => {
        if (res.ok) {
            // Update locally
            const notif = notifications.find(n => n.id === id);
            if (notif) {
                notif.read = true;
                renderNotifications();
                showToast("Notification marked as read.", "success");
            }
        } else {
            showToast("Failed to mark as read (Unauthorized).", "error");
        }
    })
    .catch(err => console.error(err));
}

function handleMarkAllAsRead() {
    fetch(`/api/notifications/user/${currentUserId}/read-all`, {
        method: "PUT",
        headers: { "X-Auth-Token": currentUserToken }
    })
    .then(res => {
        if (res.ok) {
            notifications.forEach(notif => notif.read = true);
            renderNotifications();
            showToast("All notifications marked as read.", "success");
        } else {
            showToast("Failed to mark all as read (Unauthorized).", "error");
        }
    })
    .catch(err => console.error(err));
}

// Publishing simulation
function handlePublish(event) {
    event.preventDefault();
    const channelIdInput = document.getElementById("pubChannelId");
    const videoTitleInput = document.getElementById("pubVideoTitle");

    const channelId = parseInt(channelIdInput.value);
    const videoTitle = videoTitleInput.value;

    if (!channelId || !videoTitle) return;

    fetch("/api/notifications/publish", {
        method: "POST",
        headers: { 
            "Content-Type": "application/json",
            "X-Auth-Token": currentUserToken
        },
        body: JSON.stringify({
            channelId: channelId,
            videoId: "sim-" + Math.floor(Math.random() * 10000),
            videoTitle: videoTitle
        })
    })
    .then(res => {
        if (res.ok) {
            showToast("Event published successfully!", "success");
            videoTitleInput.value = "";
        } else {
            showToast("Failed to publish event (Unauthorized).", "error");
        }
    })
    .catch(err => {
        console.error(err);
        showToast("Network error occurred during publishing.", "error");
    });
}
