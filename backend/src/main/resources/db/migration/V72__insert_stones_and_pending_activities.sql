INSERT INTO activity (type, valence_min, valence_max, arousal_min, arousal_max, dominance_min, dominance_max, effect_valence, effect_arousal, effect_dominance, title, description, goal_keywords, route_path)
SELECT 'STONES', -1.0, 0.4, -0.4, 0.8, -1.0, 0.6, 0.25, -0.20, 0.15, 'Piedras del lago', 'Lanza piedras al lago y observa las ondas para liberar tensiones y relajarte.', 'relajar,relajacion,calma,soltar,distraccion,piedras,lago,ondas,stress,estres', '/stones'
WHERE NOT EXISTS (SELECT 1 FROM activity WHERE type = 'STONES');

INSERT INTO activity (type, valence_min, valence_max, arousal_min, arousal_max, dominance_min, dominance_max, effect_valence, effect_arousal, effect_dominance, title, description, goal_keywords, route_path)
SELECT 'PENDING', -1.0, 0.3, -0.5, 0.7, -1.0, 0.5, 0.20, -0.15, 0.30, 'Pendientes', 'Organiza tus tareas pendientes en el tablero para liberar carga mental.', 'organizar,tareas,pendientes,carga mental,productividad,ordenar,hacer,lista,todo', '/pending'
WHERE NOT EXISTS (SELECT 1 FROM activity WHERE type = 'PENDING');
