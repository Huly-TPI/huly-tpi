UPDATE activity SET
    valence_min = -1.0, valence_max = 0.3,
    arousal_min = 0.5,  arousal_max = 1.0,
    dominance_min = -1.0, dominance_max = 1.0,
    effect_valence = 0.30, effect_arousal = -0.40, effect_dominance = 0.20
WHERE type = 'RESPIRACION';

UPDATE activity SET
    valence_min = -1.0, valence_max = -0.15,
    arousal_min = 0.1,  arousal_max = 0.6,
    dominance_min = -1.0, dominance_max = 0.4,
    effect_valence = 0.30, effect_arousal = -0.20, effect_dominance = 0.20
WHERE type = 'NUBE';

UPDATE activity SET
    valence_min = -1.0, valence_max = 0.15,
    arousal_min = -0.4, arousal_max = 0.60,
    dominance_min = -0.3, dominance_max = 1.0,
    effect_valence = 0.35, effect_arousal = -0.05, effect_dominance = 0.30
WHERE type = 'DIARIO';

UPDATE activity SET
    valence_min = -0.6, valence_max = 0.5,
    arousal_min = -1.0, arousal_max = 0.2,
    dominance_min = -1.0, dominance_max = 0.6,
    effect_valence = 0.30, effect_arousal = 0.10, effect_dominance = 0.15
WHERE type = 'BURBUJA';
