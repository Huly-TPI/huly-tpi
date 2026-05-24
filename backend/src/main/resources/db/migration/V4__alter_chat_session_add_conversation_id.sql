-- Add conversation_id column to chat_session for linking conversations
ALTER TABLE chat_session ADD COLUMN conversation_id VARCHAR(255);
CREATE UNIQUE INDEX IF NOT EXISTS idx_chat_session_conversation_id ON chat_session(conversation_id);

-- Make id_app_user nullable (anonymous sessions until auth is implemented)
ALTER TABLE chat_session ALTER COLUMN id_app_user DROP NOT NULL;