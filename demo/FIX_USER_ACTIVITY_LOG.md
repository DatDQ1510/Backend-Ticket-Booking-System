# Fix UserActivityLog Unique Constraint Issue

## Vấn đề
Database có UNIQUE constraint trên `user_id` trong bảng `user_activity_log` từ khi dùng `@OneToOne` relationship. Sau khi đổi sang `@ManyToOne` (1 user có nhiều activities), constraint này gây lỗi duplicate entry.

## Triệu chứng
```
Duplicate entry '767' for key 'user_activity_log.UKs7gebytnwtuosnpesdnig11uw'
```

## Giải pháp

### Option 1: Xóa constraint bằng SQL (Khuyến nghị)
Chạy file `fix_user_activity_log_constraint.sql`:

```bash
mysql -u your_username -p your_database < fix_user_activity_log_constraint.sql
```

Hoặc chạy trực tiếp trong MySQL:
```sql
ALTER TABLE user_activity_log DROP INDEX UKs7gebytnwtuosnpesdnig11uw;
```

### Option 2: Drop và recreate table (Mất dữ liệu)
```sql
DROP TABLE user_activity_log;
```
Sau đó restart application với `spring.jpa.hibernate.ddl-auto=update` để Hibernate tự tạo lại table.

### Option 3: Dùng migration tool
Nếu dùng Flyway hoặc Liquibase, tạo migration script:
```sql
-- V2__fix_user_activity_log_constraint.sql
ALTER TABLE user_activity_log DROP INDEX UKs7gebytnwtuosnpesdnig11uw;
CREATE INDEX idx_user_activity_user_id ON user_activity_log(user_id);
CREATE INDEX idx_user_activity_timestamp ON user_activity_log(activity_timestamp);
```

## Đã thực hiện trong code
1. ✅ Entity `UserActivityLog` đã đổi từ `@OneToOne` sang `@ManyToOne`
2. ✅ Thêm indexes cho performance (không unique)
3. ✅ Service catch `DataIntegrityViolationException` gracefully
4. ✅ Logs chỉ warning thay vì error stacktrace

## Verify sau khi fix
```sql
-- Kiểm tra constraint đã được xóa
SHOW CREATE TABLE user_activity_log;

-- Test insert nhiều records cho cùng 1 user
INSERT INTO user_activity_log (user_id, activity_type, activity_timestamp) 
VALUES (767, 'test', NOW());
```

## Lưu ý
- Sau khi fix database, restart Spring Boot application
- Code đã handle gracefully nên app vẫn chạy được dù constraint chưa xóa
- Recommend: Chạy SQL fix càng sớm càng tốt
