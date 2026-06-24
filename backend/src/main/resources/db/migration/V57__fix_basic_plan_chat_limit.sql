-- Plan Básico: chatbot libre (sin límite diario de mensajes).
-- La inserción original en V50 estableció chat_daily_limit = 20 por error.
UPDATE product
SET chat_daily_limit = NULL
WHERE plan_code = 'BASIC';
