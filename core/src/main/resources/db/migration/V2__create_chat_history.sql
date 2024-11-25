-- Chat table to store chat metadata
CREATE TABLE IF NOT EXISTS chat
(
    chat_id    uuid        DEFAULT uuid_generate_v4() PRIMARY KEY,
    user_id    text NOT NULL,
    title      text NOT NULL,
    created_at timestamptz DEFAULT now()
);

-- Chat history table to store messages
CREATE TABLE IF NOT EXISTS chat_history
(
    message_id bigserial PRIMARY KEY,
    chat_id    uuid    NOT NULL REFERENCES chat (chat_id) ON DELETE CASCADE,
    is_user    boolean NOT NULL,
    content    text    NOT NULL,
    created_at timestamptz DEFAULT now()
);
