# Event-Driven Modular Monolith Architecture

## 📋 Tổng Quan

Dự án TramCamXuc_Thesis đã được refactor theo kiến trúc **Event-Driven Modular Monolith** để loại bỏ circular dependencies và tight coupling giữa các modules.

## 🎯 Mục Tiêu Đạt Được

- ✅ Loại bỏ hoàn toàn circular dependencies
- ✅ Giảm tight coupling giữa các service modules
- ✅ Tăng khả năng test và maintain
- ✅ Async processing cho các side effects (email, statistics)
- ✅ Dễ dàng mở rộng với event listeners mới

## 🏗️ Kiến Trúc

### Cấu Trúc Thư Mục

```
iuh.fit.se.tramcamxuc/
├── common/
│   ├── event/              # 🆕 Domain Events
│   │   ├── SongApprovedEvent.java
│   │   ├── SongRejectedEvent.java
│   │   ├── SongListenedEvent.java
│   │   ├── AlbumApprovedEvent.java
│   │   ├── AlbumRejectedEvent.java
│   │   ├── UserRegisteredEvent.java
│   │   ├── PasswordResetRequestedEvent.java
│   │   └── PasswordChangeRequestedEvent.java
│   │
│   └── listener/           # 🆕 Event Listeners
│       ├── EmailEventListener.java
│       └── StatisticEventListener.java
│
└── modules/
    ├── song/
    │   └── service/impl/SongServiceImpl.java       # ✨ Refactored
    ├── album/
    │   └── service/impl/AlbumServiceImpl.java      # ✨ Refactored
    ├── auth/
    │   └── service/impl/AuthServiceImpl.java       # ✨ Refactored
    └── user/
        └── service/impl/UserServiceImpl.java       # ✨ Refactored
```

## 📊 Event Flow Diagrams

### 1. Song Approval Flow

```
┌─────────────┐         ┌──────────────────┐         ┌────────────────────┐
│   Admin     │────────▶│  SongService     │────────▶│ SongApprovedEvent  │
│  approves   │         │  - Update DB     │         │                    │
│   song      │         │  - Publish Event │         └────────┬───────────┘
└─────────────┘         └──────────────────┘                  │
                                                               ▼
                                                     ┌─────────────────────┐
                                                     │ EmailEventListener  │
                                                     │  @Async             │
                                                     │  - Send approval    │
                                                     │    email to artist  │
                                                     └─────────────────────┘
```

### 2. Song Listen Tracking Flow

```
┌─────────────┐         ┌──────────────────┐         ┌────────────────────┐
│    User     │────────▶│  SongService     │────────▶│ SongListenedEvent  │
│ plays song  │         │  - Update Redis  │         │                    │
│             │         │  - Publish Event │         └────────┬───────────┘
└─────────────┘         └──────────────────┘                  │
                                                               ▼
                                                     ┌──────────────────────┐
                                                     │StatisticEventListener│
                                                     │  @Async              │
                                                     │  - Record history    │
                                                     │    in database       │
                                                     └──────────────────────┘
```

### 3. User Registration Flow

```
┌─────────────┐         ┌──────────────────┐         ┌─────────────────────┐
│   User      │────────▶│  AuthService     │────────▶│ UserRegisteredEvent │
│ registers   │         │  - Create user   │         │                     │
│             │         │  - Generate OTP  │         └────────┬────────────┘
└─────────────┘         │  - Store in Redis│                  │
                        │  - Publish Event │                  ▼
                        └──────────────────┘        ┌─────────────────────┐
                                                     │ EmailEventListener  │
                                                     │  @Async             │
                                                     │  - Send OTP email   │
                                                     └─────────────────────┘
```

## 🔄 Events Chi Tiết

### Song Events

| Event | Publisher | Listeners | Async | Purpose |
|-------|-----------|-----------|-------|---------|
| `SongApprovedEvent` | SongService | EmailEventListener | ✅ | Gửi email thông báo bài hát được duyệt |
| `SongRejectedEvent` | SongService | EmailEventListener | ✅ | Gửi email thông báo bài hát bị từ chối |
| `SongListenedEvent` | SongService | StatisticEventListener | ✅ | Ghi nhận lịch sử nghe nhạc |

### Album Events

| Event | Publisher | Listeners | Async | Purpose |
|-------|-----------|-----------|-------|---------|
| `AlbumApprovedEvent` | AlbumService | EmailEventListener | ✅ | Gửi email thông báo album được duyệt |
| `AlbumRejectedEvent` | AlbumService | EmailEventListener | ✅ | Gửi email thông báo album bị từ chối |

### Auth/User Events

| Event | Publisher | Listeners | Async | Purpose |
|-------|-----------|-----------|-------|---------|
| `UserRegisteredEvent` | AuthService | EmailEventListener | ✅ | Gửi OTP xác thực tài khoản |
| `PasswordResetRequestedEvent` | AuthService | EmailEventListener | ✅ | Gửi OTP đặt lại mật khẩu |
| `PasswordChangeRequestedEvent` | UserService | EmailEventListener | ✅ | Gửi OTP đổi mật khẩu |

## 📝 Patterns Áp Dụng

### 1. Domain Events Pattern
- Các module giao tiếp qua events thay vì direct method calls
- Events được định nghĩa trong `common.event` package
- Mỗi event là immutable (final fields + @AllArgsConstructor)

### 2. Publisher-Subscriber Pattern
- Services publish events qua `ApplicationEventPublisher`
- Event Listeners subscribe bằng `@EventListener` annotation
- Decoupling hoàn toàn giữa publisher và subscriber

### 3. Async Processing Pattern
- Tất cả listeners được đánh dấu `@Async("taskExecutor")`
- Non-blocking cho các side effects (email, logging)
- Sử dụng ThreadPool được cấu hình trong `AsyncConfig`

## 🔧 Configuration

### Async Executor Settings
```java
// AsyncConfig.java
@Configuration
@EnableAsync
public class AsyncConfig {
    @Bean(name = "taskExecutor")
    public Executor taskExecutor() {
        ThreadPoolTaskExecutor executor = new ThreadPoolTaskExecutor();
        executor.setCorePoolSize(executorProperties.getCorePoolSize());
        executor.setMaxPoolSize(executorProperties.getMaxPoolSize());
        executor.setQueueCapacity(executorProperties.getQueueCapacity());
        // ...
        return executor;
    }
}
```

## 📈 Benefits

### 1. **Loose Coupling**
- Services không còn phụ thuộc trực tiếp vào nhau
- Thay đổi một service không ảnh hưởng services khác
- Dễ dàng mock/test riêng biệt

### 2. **Scalability**
- Có thể thêm listeners mới mà không cần sửa code cũ
- Async processing giảm response time
- Dễ dàng chuyển sang distributed events (Kafka, RabbitMQ) sau này

### 3. **Maintainability**
- Code rõ ràng, dễ đọc hơn
- Business logic tách biệt với side effects
- Dễ debug và trace events

### 4. **Testability**
- Unit test services mà không cần mock nhiều dependencies
- Integration test dễ dàng với event publishing
- Có thể disable listeners khi test

## 🎨 Code Examples

### Before Refactoring ❌
```java
@Service
public class SongServiceImpl {
    private final EmailService emailService;  // ❌ Direct dependency
    
    public void approveSong(UUID songId) {
        // Update song status
        song.setStatus(SongStatus.PUBLIC);
        songRepository.save(song);
        
        // Direct call - tight coupling
        emailService.sendSongStatusEmail(...);  // ❌
    }
}
```

### After Refactoring ✅
```java
@Service
public class SongServiceImpl {
    private final ApplicationEventPublisher eventPublisher;  // ✅
    
    public void approveSong(UUID songId) {
        // Update song status
        song.setStatus(SongStatus.PUBLIC);
        songRepository.save(song);
        
        // Publish event - loose coupling
        eventPublisher.publishEvent(new SongApprovedEvent(...));  // ✅
    }
}
```

### Event Listener ✅
```java
@Component
public class EmailEventListener {
    private final EmailService emailService;
    
    @Async("taskExecutor")  // ✅ Non-blocking
    @EventListener
    public void handleSongApproved(SongApprovedEvent event) {
        emailService.sendSongStatusEmail(...);
    }
}
```

## 🚀 Future Enhancements

### 1. Event Sourcing
- Lưu trữ tất cả events vào database
- Có thể replay events để rebuild state
- Audit log chi tiết

### 2. CQRS (Command Query Responsibility Segregation)
- Tách read model và write model
- Tối ưu performance cho queries
- Scale read và write độc lập

### 3. Distributed Events
- Chuyển sang message broker (Kafka/RabbitMQ)
- Hỗ trợ multiple instances
- Guaranteed delivery với persistent queue

### 4. Event Versioning
- Hỗ trợ nhiều phiên bản event
- Backward compatibility
- Graceful migration

## 📚 References

- **Domain-Driven Design** by Eric Evans
- **Implementing Domain-Driven Design** by Vaughn Vernon
- **Spring Event-Driven Architecture** - Spring Documentation
- **Modular Monolith Architecture** - Simon Brown

## 👥 Team & Contribution

Refactoring được thực hiện bởi Senior Software Architect với mục tiêu:
- Cải thiện code quality
- Tăng maintainability
- Chuẩn bị cho microservices migration trong tương lai

---

**Last Updated:** February 2, 2026  
**Status:** ✅ Completed & Verified  
**Build Status:** ✅ Compilation Successful
