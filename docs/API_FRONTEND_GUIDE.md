# 📚 API Documentation for Frontend

## Table of Contents
- [1. Flashcard Study Session](#1-flashcard-study-session)
- [2. Notification System](#2-notification-system)

---

# 1. Flashcard Study Session

## 1.1 Overview

Hệ thống học flashcard hỗ trợ 2 chế độ học:

| Mode | Mô tả |
|------|-------|
| **SPACED_REPETITION** | Học các card đến hạn + card mới (tối ưu ghi nhớ) |
| **REVIEW** | Ôn tập tất cả card khi không còn card SR |

### Session Status

| Status | Mô tả |
|--------|-------|
| `IN_PROGRESS` | Session đang học dở |
| `COMPLETED` | Đã hoàn thành tất cả cards |
| `CANCELLED` | Người dùng hủy session |

---

## 1.2 Flow Diagram

```
┌─────────────────────────────────────────────────────────────────┐
│                    FLASHCARD STUDY FLOW                          │
└─────────────────────────────────────────────────────────────────┘

User clicks "Study"
        │
        ▼
┌───────────────────────────────────┐
│ GET /session/status               │ ◄── Kiểm tra session đang dở
└───────────────────────────────────┘
        │
        ├── Có session IN_PROGRESS ──► Thông báo (tùy hiển thị: popup, alert, ...) "Tiếp tục học?"
        │                                    │
        │                                    ├── Yes ──► POST /session/start
        │                                    └── No ───► DELETE /session/{id} (hủy session cũ - xem như ko quan tâm nữa)
        │                                                      │
        │                                                      ▼
        └── Không có session ─────────────────────────► POST /session/start
                                                              │
                                                              ▼
                                                    ┌─────────────────┐
                                                    │ Response:       │
                                                    │ - studyMode     │
                                                    │ - message       │
                                                    │ - cards[]       │
                                                    └─────────────────┘
                                                              │
                                                              ▼
                                              ┌───────────────────────────┐
                                              │ Hiển thị cards   │
                                              │ và message cho user       │
                                              └───────────────────────────┘
                                                              │
                                                              ▼
                                              ┌───────────────────────────┐
                                              │ User review từng card     │
                                              │ (KNOWN / UNKNOWN)       │
                                              └───────────────────────────┘
                                                              │
                                                              ▼
                Sync mỗi 5-10 cards hoặc định kỳ (hoặc dựa vào size của flashcard mà cân nhắc sao cho tối ưu về hiệu năng, ko call quá nhiều, ko để quá nhiều rồi call vì user có thể thoát ngang)                
                                              ┌───────────────────────────┐
                                              │ PUT /session/{id}/progress│
                                              │ (batch sync)              │     
                                              └───────────────────────────┘
                                                              │
                                                              ▼
                                              ┌───────────────────────────┐
                                              │ Check response.status     │
                                              │ == COMPLETED ?            │
                                              └───────────────────────────┘
                                                              │
                                                ┌─────────────┴─────────────┐
                                                │                           │
                                                ▼                           ▼
                                          Continue                GET /session/{id}/result
                                          learning                Show final result
```

**Study Mode Messages:**

| studyMode | message | UI Action |
|-----------|---------|-----------|
| `SPACED_REPETITION` | "Starting spaced repetition session" | Hiển thị bình thường |
| `REVIEW` | "No cards due for review. Starting review mode with all cards." | Hiển thị banner thông báo, ví dụ: "Bạn đã hoàn thành bài học hôm nay! Hiện tại đang ôn tập." |

---
**Sync Process**

Sync khi user rời trang (để không mất progress), ex:
```
window.addEventListener('beforeunload', syncProgress);
```

---

**Session Result:**
Có thể tự tính progress
---

# 2. Notification System

## 2.1 Overview

Hệ thống notification bao gồm:

| Feature | Mô tả |
|---------|-------|
| **In-App Notifications** | Thông báo trong ứng dụng, lưu DB |
| **Push Notifications** | Thông báo đẩy qua FCM |
| **Device Token Management** | Quản lý FCM tokens |

### Notification Types (Hiện tại chỉ dùng CARD_DUE_REMINDER, còn lại sẽ là in-future :v)

| Type | Mô tả |
|------|-------|
| `CARD_DUE_REMINDER` | Nhắc nhở cards đến hạn |
| `STUDY_SESSION_REMINDER` | Nhắc học bài |
| `STUDY_STREAK` | Chuỗi ngày học liên tiếp |
| `ACHIEVEMENT_UNLOCKED` | Mở khóa thành tựu |
| `SYSTEM_ANNOUNCEMENT` | Thông báo hệ thống |

---

## 2.2 Flow Diagram

### 2.2.1 Push Notification Registration

```
┌─────────────────────────────────────────────────────────────────┐
│              PUSH NOTIFICATION REGISTRATION                      │
└─────────────────────────────────────────────────────────────────┘

App Start / User Login
        │
        ▼
┌───────────────────────────────────┐
│ Request notification permission   │
│ (Browser/Mobile)                  │
└───────────────────────────────────┘
        │
        ├── Denied ──► Skip push notifications
        │
        └── Granted
              │
              ▼
┌───────────────────────────────────┐
│ Get FCM token from Firebase SDK   │ (tự xem nha, firebase/messaging, react-native-firebase)
└───────────────────────────────────┘
        │
        ▼
┌───────────────────────────────────┐
│ POST /notifications/device/register│
│ { token, platform }               │
└───────────────────────────────────┘
        │
        ▼
    Token saved ──► User receives push notifications
```

### 2.2.2 In-App Notification Flow

```
┌─────────────────────────────────────────────────────────────────┐
│                IN-APP NOTIFICATION FLOW                          │
└─────────────────────────────────────────────────────────────────┘

                    ┌─────────────────┐
                    │   App Header    │
                    │   (icon chuông) │
                    └────────┬────────┘
                             │
              ┌──────────────┴──────────────┐
              │                             │
              ▼                             ▼
    GET /notifications/unread/count    Click notification icon
    (Poll every 30-60s)                     │
              │                             ▼
              ▼                    GET /notifications
    Update badge count             (with pagination)
                                           │
                                           ▼
                                  ┌─────────────────┐
                                  │ Notification    │
                                  │ Dropdown/Page   │
                                  └────────┬────────┘
                                           │
                              ┌────────────┴────────────┐
                              │                         │
                              ▼                         ▼
                    Click notification           "Mark all read"
                              │                         │
                              ▼                         ▼
              PATCH /notifications/{id}/read   PATCH /notifications/read-all
                              │
                              ▼
                    Navigate to actionUrl
                    (e.g., /study)
```

---

## 2.3 API Usage

### 2.3.1 Register Device Token

Đăng ký FCM token khi app khởi động.

```
POST /api/notifications/device/register
```

**Request Body:**
```json
{
  "token": "fcm_token_string_here",
  "platform": "WEB"  // WEB | ANDROID | IOS
}
```

**Frontend Logic (Web):**
```javascript
import { initializeApp } from 'firebase/app';
import { getMessaging, getToken, onMessage } from 'firebase/messaging';

const firebaseConfig = { /* your config */ };
const app = initializeApp(firebaseConfig);
const messaging = getMessaging(app);

async function registerPushNotifications() {
  try {
    // Request permission
    const permission = await Notification.requestPermission();
    if (permission !== 'granted') {
      console.log('Notification permission denied');
      return;
    }

    // Get FCM token
    const token = await getToken(messaging, {
      vapidKey: 'YOUR_VAPID_KEY'
    });

    // Register with backend
    await api.registerDevice({
      token: token,
      platform: 'WEB'
    });

    console.log('Push notifications registered');
  } catch (error) {
    console.error('Failed to register push:', error);
  }
}

// Handle foreground messages
onMessage(messaging, (payload) => {
  console.log('Received foreground message:', payload);
  
  // Show in-app notification
  showToast({
    title: payload.notification.title,
    body: payload.notification.body,
    onClick: () => router.push(payload.data.actionUrl)
  });
  
  // Refresh notification count
  refreshNotificationCount();
});
```

---

### 2.3.2 Unregister Device Token

Hủy đăng ký khi user logout.

```
POST /api/notifications/device/unregister
```

**Request Body:**
```json
{
  "token": "fcm_token_string_here"
}
```

**Frontend Logic:**
```javascript
async function logout() {
  // Get current FCM token
  const token = await getToken(messaging);
  
  // Unregister from backend
  if (token) {
    await api.unregisterDevice({ token });
  }
  
  // Clear local storage, redirect to login, etc.
  clearAuthData();
  router.push('/login');
}
```

---

### 2.3.3 Get Unread Count

Lấy số lượng thông báo chưa đọc - dùng cho badge.

```
GET /api/notifications/unread/count
```

**Response:**
```json
{
  "success": true,
  "data": {
    "unreadCount": 5
  }
}
```

**Frontend Logic:**
```javascript
// Poll every 30 seconds
const POLL_INTERVAL = 30000;

function startNotificationPolling() {
  // Initial fetch
  updateNotificationBadge();
  
  // Start polling
  setInterval(updateNotificationBadge, POLL_INTERVAL);
}

async function updateNotificationBadge() {
  const { unreadCount } = await api.getUnreadCount();
  
  // Update UI badge
  document.querySelector('.notification-badge').textContent = 
    unreadCount > 99 ? '99+' : unreadCount;
  
  // Hide badge if 0
  document.querySelector('.notification-badge').hidden = unreadCount === 0;
}
```

---

### 2.3.4 Get Notifications

Lấy danh sách thông báo với pagination.

```
GET /api/notifications?page=0&size=20
GET /api/notifications/unread?page=0&size=20
```

**Response:**
```json
{
  "success": true,
  "data": {
    "notifications": [
      {
        "id": 1,
        "type": "CARD_DUE_REMINDER",
        "title": "Good Morning!",
        "message": "You have 15 cards waiting for review.",
        "data": { "dueCount": 15 },
        "actionUrl": "/study",
        "isRead": false,
        "createdAt": "2024-12-06T08:00:00Z",
        "readAt": null
      }
    ],
    "unreadCount": 5,
    "currentPage": 0,
    "totalPages": 3,
    "totalElements": 45
  }
}
```

**Frontend Logic:**
```javascript
// NotificationList.js
const [notifications, setNotifications] = useState([]);
const [page, setPage] = useState(0);
const [hasMore, setHasMore] = useState(true);

async function loadNotifications(reset = false) {
  const currentPage = reset ? 0 : page;
  
  const response = await api.getNotifications({
    page: currentPage,
    size: 20
  });
  
  if (reset) {
    setNotifications(response.notifications);
  } else {
    setNotifications(prev => [...prev, ...response.notifications]);
  }
  
  setPage(currentPage + 1);
  setHasMore(currentPage < response.totalPages - 1);
}

// Infinite scroll
function handleScroll(e) {
  if (isNearBottom(e) && hasMore) {
    loadNotifications();
  }
}
```

---

### 2.3.5 Mark as Read

Đánh dấu đã đọc.

```
PATCH /api/notifications/{notificationId}/read     # Single
PATCH /api/notifications/read                      # Multiple
PATCH /api/notifications/read-all                  # All
```

**Request Body (Multiple):**
```json
{
  "notificationIds": [1, 2, 3]
}
```

**Frontend Logic:**
```javascript
// Mark single when clicked
async function handleNotificationClick(notification) {
  if (!notification.isRead) {
    await api.markAsRead(notification.id);
    updateNotificationBadge();
  }
  
  // Navigate to action URL
  if (notification.actionUrl) {
    router.push(notification.actionUrl);
  }
}

// Mark all as read
async function handleMarkAllRead() {
  await api.markAllAsRead();
  
  // Update UI
  setNotifications(prev => 
    prev.map(n => ({ ...n, isRead: true }))
  );
  updateNotificationBadge();
}
```

---

### 2.3.6 Delete Notification

Xóa thông báo.

```
DELETE /api/notifications/{notificationId}
```

**Frontend Logic:**
```javascript
async function handleDeleteNotification(id) {
  await api.deleteNotification(id);
  
  // Remove from UI
  setNotifications(prev => prev.filter(n => n.id !== id));
  
  // Update badge if was unread
  updateNotificationBadge();
}
```

---

## 2.4 Complete Frontend Example

```javascript
// NotificationManager.js
class NotificationManager {
  constructor() {
    this.unreadCount = 0;
    this.pollInterval = null;
  }

  async init() {
    // Register push notifications
    await this.registerPush();
    
    // Start polling for unread count
    this.startPolling();
    
    // Listen for foreground messages
    this.listenForMessages();
  }

  async registerPush() {
    if (!('Notification' in window)) return;
    
    const permission = await Notification.requestPermission();
    if (permission !== 'granted') return;

    const token = await getToken(messaging, { vapidKey: VAPID_KEY });
    await api.registerDevice({ token, platform: 'WEB' });
  }

  startPolling() {
    this.updateBadge();
    this.pollInterval = setInterval(() => this.updateBadge(), 30000);
  }

  stopPolling() {
    if (this.pollInterval) {
      clearInterval(this.pollInterval);
    }
  }

  async updateBadge() {
    const { unreadCount } = await api.getUnreadCount();
    this.unreadCount = unreadCount;
    this.renderBadge();
  }

  renderBadge() {
    const badge = document.querySelector('.notification-badge');
    badge.textContent = this.unreadCount > 99 ? '99+' : this.unreadCount;
    badge.hidden = this.unreadCount === 0;
    
    // Update document title
    document.title = this.unreadCount > 0 
      ? `(${this.unreadCount}) ProLearning` 
      : 'ProLearning';
  }

  listenForMessages() {
    onMessage(messaging, (payload) => {
      // Show toast notification
      this.showToast(payload);
      
      // Update badge
      this.updateBadge();
    });
  }

  showToast(payload) {
    toast({
      title: payload.notification.title,
      description: payload.notification.body,
      action: payload.data.actionUrl ? {
        label: 'View',
        onClick: () => router.push(payload.data.actionUrl)
      } : null
    });
  }

  async cleanup() {
    this.stopPolling();
    
    const token = await getToken(messaging);
    if (token) {
      await api.unregisterDevice({ token });
    }
  }
}

// Usage
const notificationManager = new NotificationManager();

// On app start
notificationManager.init();

// On logout
notificationManager.cleanup();
```
---

# 3. Quick Reference

## API Endpoints Summary

### Flashcard Study Session
| Method | Endpoint | Description |
|--------|----------|-------------|
| GET | `/sets/{setId}/flashcards/{flashcardId}/session/status` | Check session status |
| POST | `/sets/{setId}/flashcards/{flashcardId}/session/start` | Start/Resume session |
| PUT | `/sets/{setId}/flashcards/{flashcardId}/session/{sessionId}/progress` | Sync progress |
| DELETE | `/sets/{setId}/flashcards/{flashcardId}/session/{sessionId}` | Cancel session |
| GET | `/sets/{setId}/flashcards/{flashcardId}/session/{sessionId}/result` | Get result |

### Notifications
| Method | Endpoint | Description |
|--------|----------|-------------|
| GET | `/notifications` | Get all notifications |
| GET | `/notifications/unread` | Get unread only |
| GET | `/notifications/unread/count` | Get unread count |
| PATCH | `/notifications/{id}/read` | Mark single as read |
| PATCH | `/notifications/read` | Mark multiple as read |
| PATCH | `/notifications/read-all` | Mark all as read |
| DELETE | `/notifications/{id}` | Delete notification |
| POST | `/notifications/device/register` | Register FCM token |
| POST | `/notifications/device/unregister` | Unregister FCM token |

---

## Notes for Frontend Developers

1. **Authentication**: Tất cả API đều yêu cầu Bearer token trong header
2. **Base URL**: `/api` prefix đã được config trong server
3. **Swagger**: Chi tiết request/response schemas có tại `/api/api-docs`
4. **Error Handling**: Tất cả errors trả về format `{ success: false, message: "...", errors: [...] }`

---

*Document Version: 1.0*  
*Last Updated: December 2024*

