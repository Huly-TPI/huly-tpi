-- Disable re-engagement emails for all existing users to conserve email quota.
-- New users will still have them enabled by default.
UPDATE app_user SET reengagement_emails_enabled = false;
