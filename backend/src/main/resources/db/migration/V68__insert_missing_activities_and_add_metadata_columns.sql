-- Add new metadata columns to activity table
ALTER TABLE activity ADD COLUMN IF NOT EXISTS title VARCHAR(255);
ALTER TABLE activity ADD COLUMN IF NOT EXISTS description TEXT;
ALTER TABLE activity ADD COLUMN IF NOT EXISTS goal_keywords TEXT;
ALTER TABLE activity ADD COLUMN IF NOT EXISTS route_path VARCHAR(255);

-- Update existing activities with their metadata
UPDATE activity
SET 
    title = 'Respiración guiada',
    description = 'Una práctica breve para bajar la activación y recuperar calma.',
    goal_keywords = 'calmar,calmarme,calmarse,relajar,relajarme,relajarse,ansiedad,ansioso,ansiosa,dormir,estres,stress,respirar,tranquilizar,tranquilizarme,tranquilizarse',
    route_path = '/guided-breathing'
WHERE type = 'BREATHING';

UPDATE activity
SET 
    title = 'Diario emocional',
    description = 'Un espacio para ordenar pensamientos y entender lo que sentís.',
    goal_keywords = 'reflexionar,entender,escribir,ordenar,pensamientos,diario,procesar,comprender,claridad,aclarar',
    route_path = '/diary'
WHERE type = 'DIARY';

UPDATE activity
SET 
    title = 'Farolitos que vuelan',
    description = 'Un ejercicio visual para soltar pensamientos que pesan.',
    goal_keywords = 'soltar,visualizar,nubes,nube,liberar,rumiar,rumia',
    route_path = '/lanterns'
WHERE type = 'LANTERN';

UPDATE activity
SET 
    title = 'Burbujas',
    description = 'Una actividad liviana para cambiar el foco con suavidad.',
    goal_keywords = 'juego,jugar,liviano,liviana,distraer,distraerme,distraerse,burbujas,burbuja,desconectar,despejarme',
    route_path = '/bubbles'
WHERE type = 'BUBBLE';

-- Insert missing activities only if they do not exist
INSERT INTO activity (type, valence_min, valence_max, arousal_min, arousal_max, dominance_min, dominance_max, effect_valence, effect_arousal, effect_dominance, title, description, goal_keywords, route_path)
SELECT 'CHALLENGE', -1.0, 0.4, -0.5, 0.8, -1.0, 0.5, 0.25,  0.10, 0.35, 'Retos Diarios', 'Un pequeño desafío diario para mejorar tu bienestar y cambiar de perspectiva.', 'reto,desafio,accion,hacer,tarea,desafiar,desafiante,reto diario,retos,desafios', '/challenges'
WHERE NOT EXISTS (SELECT 1 FROM activity WHERE type = 'CHALLENGE');

INSERT INTO activity (type, valence_min, valence_max, arousal_min, arousal_max, dominance_min, dominance_max, effect_valence, effect_arousal, effect_dominance, title, description, goal_keywords, route_path)
SELECT 'ZEN_GARDEN', -1.0, 0.5,  0.3, 1.0, -1.0, 1.0, 0.20, -0.35, 0.15, 'Jardín Zen de Arena', 'Interactúa con la arena dibujando formas relajantes para calmar tu mente.', 'dibujar,arena,zen,jardin,calma,relajacion,dibujo,pintar,formas,gardens', '/zen-sand-garden'
WHERE NOT EXISTS (SELECT 1 FROM activity WHERE type = 'ZEN_GARDEN');

INSERT INTO activity (type, valence_min, valence_max, arousal_min, arousal_max, dominance_min, dominance_max, effect_valence, effect_arousal, effect_dominance, title, description, goal_keywords, route_path)
SELECT 'MANDALA',    -1.0, 0.3, -0.2, 0.8, -1.0, 0.8, 0.30, -0.25, 0.20, 'Mandalas', 'Colorea hermosas mandalas para concentrarte y reducir la carga mental.', 'pintar,colorear,mandala,mandalas,dibujar,colores,concentracion,distraccion,arte', '/mandalas'
WHERE NOT EXISTS (SELECT 1 FROM activity WHERE type = 'MANDALA');
