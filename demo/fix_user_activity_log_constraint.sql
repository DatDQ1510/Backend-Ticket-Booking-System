-- Fix UserActivityLog UNIQUE constraint issue
-- This removes the unique constraint on user_id to allow multiple activity logs per user

-- Check current constraint
SHOW CREATE TABLE user_activity_log;

-- Drop the unique constraint
ALTER TABLE user_activity_log DROP INDEX UKs7gebytnwtuosnpesdnig11uw;

-- Verify the constraint is removed
SHOW CREATE TABLE user_activity_log;

-- Optional: Add index for better query performance (non-unique)
CREATE INDEX idx_user_activity_user_id ON user_activity_log(user_id);
CREATE INDEX idx_user_activity_timestamp ON user_activity_log(activity_timestamp);
