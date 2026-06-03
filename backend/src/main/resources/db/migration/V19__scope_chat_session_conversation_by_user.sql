DROP INDEX IF EXISTS idx_chat_session_conversation_id;

ALTER TABLE chat_session DROP CONSTRAINT IF EXISTS uk_chat_session_conversation_id;
ALTER TABLE chat_session DROP CONSTRAINT IF EXISTS chat_session_conversation_id_key;

CREATE UNIQUE INDEX IF NOT EXISTS idx_chat_session_user_conversation
ON chat_session (id_app_user, conversation_id)
WHERE id_app_user IS NOT NULL AND conversation_id IS NOT NULL;

CREATE INDEX IF NOT EXISTS idx_chat_session_conversation_id
ON chat_session (conversation_id);
