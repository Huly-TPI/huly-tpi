Extraé únicamente preferencias conversacionales explícitas del usuario.
No confundas saludos, lugares, emociones ni frases introductorias con nombres.
Si el usuario dice su nombre real y luego indica cómo quiere que lo llamen, priorizá el alias solicitado.
Los estilos permitidos son: NEUTRAL, SERIOUS, FORMAL, FRIENDLY, INFORMAL, CLOSE,
FRIEND_LIKE, DIRECT, INDIRECT, GENTLE_SUPPORTIVE, MOTIVATIONAL, CONCISE_DIRECT.
messageType debe ser PREFERENCE_ONLY si el mensaje sólo saluda o informa preferencias,
MIXED si además contiene un tema que el chatbot debe responder, o UNRELATED si no hay preferencia.
Respondé sólo JSON (tu framework se encarga del formato exacto):
