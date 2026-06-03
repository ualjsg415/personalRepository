-- Esquema completo de "El Rey ha Muerto" v2

CREATE TABLE IF NOT EXISTS game_sessions (
    id             BIGSERIAL    PRIMARY KEY,
    player_name    VARCHAR(50)  NOT NULL,
    started_at     TIMESTAMP    NOT NULL DEFAULT NOW(),
    ended_at       TIMESTAMP,
    days_survived  INT          NOT NULL DEFAULT 1,
    is_alive       BOOLEAN      NOT NULL DEFAULT TRUE,
    cause_of_death TEXT
);

CREATE TABLE IF NOT EXISTS king_stats (
    id          BIGSERIAL PRIMARY KEY,
    session_id  BIGINT    NOT NULL REFERENCES game_sessions(id) ON DELETE CASCADE,
    day         INT       NOT NULL DEFAULT 1,
    hygiene     INT       NOT NULL DEFAULT 100 CHECK (hygiene    BETWEEN 0 AND 100),
    hunger      INT       NOT NULL DEFAULT 100 CHECK (hunger     BETWEEN 0 AND 100),
    popularity  INT       NOT NULL DEFAULT 50  CHECK (popularity BETWEEN 0 AND 100),
    wealth      INT       NOT NULL DEFAULT 100 CHECK (wealth     BETWEEN 0 AND 100)
);

CREATE TABLE IF NOT EXISTS game_events (
    id          BIGSERIAL    PRIMARY KEY,
    source      VARCHAR(20)  NOT NULL CHECK (source IN ('manual','scraped','ai_generated')),
    day_target  INT,
    title       VARCHAR(200) NOT NULL,
    description TEXT         NOT NULL,
    scene       VARCHAR(50)  NOT NULL DEFAULT 'bedroom',
    scraped_at  TIMESTAMP,
    created_at  TIMESTAMP    NOT NULL DEFAULT NOW()
);

CREATE TABLE IF NOT EXISTS choices (
    id                 BIGSERIAL PRIMARY KEY,
    event_id           BIGINT    NOT NULL REFERENCES game_events(id) ON DELETE CASCADE,
    label              VARCHAR(1) NOT NULL CHECK (label IN ('A','B','C')),
    text               TEXT      NOT NULL,
    hidden_flag        VARCHAR(50),
    flag_trigger_delay INT       NOT NULL DEFAULT 3,
    stat_hygiene       INT       NOT NULL DEFAULT 0,
    stat_hunger        INT       NOT NULL DEFAULT 0,
    stat_popularity    INT       NOT NULL DEFAULT 0,
    stat_wealth        INT       NOT NULL DEFAULT 0,
    immediate_death    BOOLEAN   NOT NULL DEFAULT FALSE,
    death_message      TEXT
);

CREATE TABLE IF NOT EXISTS session_choices (
    id          BIGSERIAL PRIMARY KEY,
    session_id  BIGINT    NOT NULL REFERENCES game_sessions(id) ON DELETE CASCADE,
    event_id    BIGINT    NOT NULL REFERENCES game_events(id),
    choice_id   BIGINT    NOT NULL REFERENCES choices(id),
    day         INT       NOT NULL,
    chosen_at   TIMESTAMP NOT NULL DEFAULT NOW()
);

CREATE TABLE IF NOT EXISTS active_flags (
    id            BIGSERIAL   PRIMARY KEY,
    session_id    BIGINT      NOT NULL REFERENCES game_sessions(id) ON DELETE CASCADE,
    flag_name     VARCHAR(50) NOT NULL,
    trigger_day   INT         NOT NULL,
    death_message TEXT,
    created_at    TIMESTAMP   NOT NULL DEFAULT NOW()
);

-- ════════════════════════════════════════════
--  DATOS INICIALES — 10 eventos, uno por día
-- ════════════════════════════════════════════

INSERT INTO game_events (source, day_target, title, description, scene, created_at) VALUES
('manual', 1,
 'La Mañana del Rey',
 'El gallo canta. Es el primer día de tu glorioso reinado y lo primero que ves al abrir los ojos es el techo húmedo de tu cámara real. En la mesilla descansan dos objetos: el cepillo de dientes de plata y, por razones que el ama de llaves no puede explicar, la escobilla del váter.',
 'bedroom', NOW()),

('manual', 2,
 'El Primer Decreto Real',
 'Los nobles esperan tu primer decreto oficial. Esta decisión definirá tu reinado y cómo te recordará la historia. O al menos los próximos ocho días.',
 'throne-room', NOW()),

('manual', 3,
 'El Gran Banquete Real',
 'El cocinero ha preparado un festín épico. Hay más comida en esta mesa que en todo el pueblo durante un mes. Los ojos del rey brillan con codicia gastronómica.',
 'dining-hall', NOW()),

('manual', 4,
 'Los Jardines Reales',
 'El jardinero jefe te invita a admirar su obra: un jardín con siglos de historia, flores que tardaron décadas en cultivarse y un rosal plantado por tu bisabuelo.',
 'gardens', NOW()),

('manual', 5,
 'La Petición del Embajador',
 'El embajador del reino vecino lleva tres horas esperando en la antesala. Parece considerablemente irritado. Su espada reluce de forma inquietante.',
 'throne-room', NOW()),

('manual', 6,
 'Los Prisioneros del Sótano',
 'Mientras inspeccionas la mazmorra, descubres a tres prisioneros que llevan tanto tiempo encerrados que ya no saben por qué están ahí. Ellos tampoco lo saben. El carcelero definitivamente no lo sabe.',
 'dungeon', NOW()),

('manual', 7,
 'La Carta Misteriosa',
 'A medianoche, alguien desliza una carta bajo la puerta de tu cámara. La letra es ilegible, el sello está derretido y huele levemente a ajo. Nadie en el castillo sabe quién la envió.',
 'bedroom', NOW()),

('manual', 8,
 'El Banquete del Octavo Día',
 'El nuevo cocinero te mira fijamente mientras sirve la sopa. Demasiado fijamente. La sopa tiene un color que no recuerdas haber visto en ningún alimento conocido.',
 'dining-hall', NOW()),

('manual', 9,
 'El Ultimátum de los Nobles',
 'Los nobles han convocado una reunión de urgencia. Todos llevan espadas. Dicen que es «por protocolo». El ambiente es tan tenso que el trono cruje de forma audible.',
 'throne-room', NOW()),

('manual', 10,
 'El Décimo Amanecer',
 'Has llegado. Diez días como rey y sigues respirando. El pueblo sale a las calles, los nobles se miran confundidos y el médico real cancela su reserva de funeral.',
 'gardens', NOW());

-- ── DÍA 1 ──────────────────────────────────────────────────────────────────
INSERT INTO choices (event_id, label, text, hidden_flag, flag_trigger_delay,
    stat_hygiene, stat_hunger, stat_popularity, stat_wealth, immediate_death, death_message)
VALUES
  ((SELECT id FROM game_events WHERE day_target=1), 'A',
   'Cepillarse los dientes correctamente, como manda la tradición real',
   NULL, 3, 10, 0, 0, 0, FALSE, NULL),
  ((SELECT id FROM game_events WHERE day_target=1), 'B',
   'Usar la escobilla del váter porque «es más grande y llega mejor»',
   'infeccion_bucal', 4, -5, 0, 0, 0, FALSE,
   'Cuatro días después, una misteriosa infección bucal puso fin a tu reinado. El médico real nunca había visto nada igual. La escobilla del váter, en cambio, quedó impecable.'),
  ((SELECT id FROM game_events WHERE day_target=1), 'C',
   'Ignorar el aseo matutino y volver directamente a dormir',
   NULL, 3, -15, -5, 0, 0, FALSE, NULL);

-- ── DÍA 2 ──────────────────────────────────────────────────────────────────
INSERT INTO choices (event_id, label, text, hidden_flag, flag_trigger_delay,
    stat_hygiene, stat_hunger, stat_popularity, stat_wealth, immediate_death, death_message)
VALUES
  ((SELECT id FROM game_events WHERE day_target=2), 'A',
   'Decretar el Día Nacional del Queso Manchego',
   NULL, 3, 0, 5, 10, 0, FALSE, NULL),
  ((SELECT id FROM game_events WHERE day_target=2), 'B',
   'Abolir todos los impuestos para siempre con efecto inmediato',
   'quiebra_real', 5, 0, 0, 25, -25, FALSE,
   'El quinto día, sin fondos para pagar a la guardia real, los soldados se fueron a buscar trabajo al reino vecino. Quedaste completamente desprotegido. El pueblo te lloró brevemente.'),
  ((SELECT id FROM game_events WHERE day_target=2), 'C',
   'Decretar que todos deben hacerte tres reverencias al cruzarse contigo',
   NULL, 3, 0, 0, -10, 5, FALSE, NULL);

-- ── DÍA 3 ──────────────────────────────────────────────────────────────────
INSERT INTO choices (event_id, label, text, hidden_flag, flag_trigger_delay,
    stat_hygiene, stat_hunger, stat_popularity, stat_wealth, immediate_death, death_message)
VALUES
  ((SELECT id FROM game_events WHERE day_target=3), 'A',
   'Comer con la elegancia y moderación que exige la etiqueta real',
   NULL, 3, 5, 20, 0, 0, FALSE, NULL),
  ((SELECT id FROM game_events WHERE day_target=3), 'B',
   'Engullir absolutamente todo el banquete como si no hubiera un mañana',
   'gula_extrema', 3, -20, 30, -5, 0, FALSE,
   'Tres días de excesos culinarios descontrolados desembocaron en lo que los libros de medicina llaman «colapso gástrico real». Tus últimas palabras fueron: «¿hay postre?»'),
  ((SELECT id FROM game_events WHERE day_target=3), 'C',
   'Compartir el banquete con el pueblo hambriento del castillo',
   NULL, 3, 0, 10, 20, -5, FALSE, NULL);

-- ── DÍA 4 ──────────────────────────────────────────────────────────────────
INSERT INTO choices (event_id, label, text, hidden_flag, flag_trigger_delay,
    stat_hygiene, stat_hunger, stat_popularity, stat_wealth, immediate_death, death_message)
VALUES
  ((SELECT id FROM game_events WHERE day_target=4), 'A',
   'Pasear tranquilamente apreciando la belleza del jardín',
   NULL, 3, 5, 0, 5, 0, FALSE, NULL),
  ((SELECT id FROM game_events WHERE day_target=4), 'B',
   'Arrancar todas las flores raras y venderlas en el mercado del pueblo',
   'maldicion_jardinero', 3, 0, 0, -20, 20, FALSE,
   'El jardinero, destrozado por la pérdida de siglos de trabajo, lanzó una maldición ancestral. Tres días después, una erupción inexplicable de raíces en el suelo del castillo acabó con tu vida. El jardinero lo sabía.'),
  ((SELECT id FROM game_events WHERE day_target=4), 'C',
   'Organizar una cacería de pájaros reales en el jardín',
   NULL, 3, 0, 0, -5, 10, FALSE, NULL);

-- ── DÍA 5 ──────────────────────────────────────────────────────────────────
INSERT INTO choices (event_id, label, text, hidden_flag, flag_trigger_delay,
    stat_hygiene, stat_hunger, stat_popularity, stat_wealth, immediate_death, death_message)
VALUES
  ((SELECT id FROM game_events WHERE day_target=5), 'A',
   'Recibirlo con todos los honores y escuchar la propuesta diplomática',
   NULL, 3, 0, 0, 10, 10, FALSE, NULL),
  ((SELECT id FROM game_events WHERE day_target=5), 'B',
   'Mandarle otro mensaje diciendo que sigues «en el baño»',
   'conflicto_diplomatico', 3, 0, 0, -5, 0, FALSE,
   'El embajador, profundamente ofendido tras tres días de excusas sanitarias, declaró la guerra. Las tropas cruzaron la frontera tres días después. Tu reinado terminó de forma poco diplomática.'),
  ((SELECT id FROM game_events WHERE day_target=5), 'C',
   'Pedirle dinero prestado «para el reino» antes de escuchar su propuesta',
   NULL, 3, 0, 0, -10, 15, FALSE, NULL);

-- ── DÍA 6 ──────────────────────────────────────────────────────────────────
INSERT INTO choices (event_id, label, text, hidden_flag, flag_trigger_delay,
    stat_hygiene, stat_hunger, stat_popularity, stat_wealth, immediate_death, death_message)
VALUES
  ((SELECT id FROM game_events WHERE day_target=6), 'A',
   'Liberarlos a cambio de que trabajen para el reino',
   NULL, 3, 0, 0, 15, 5, FALSE, NULL),
  ((SELECT id FROM game_events WHERE day_target=6), 'B',
   'Ponerles más cadenas «por si acaso» y olvidarte del asunto',
   NULL, 3, 0, 0, -10, 0, FALSE, NULL),
  ((SELECT id FROM game_events WHERE day_target=6), 'C',
   'Nombrarlos asesores reales, ya que llevan más tiempo en el castillo que tú',
   NULL, 3, 0, 0, 10, -5, FALSE, NULL);

-- ── DÍA 7 ──────────────────────────────────────────────────────────────────
INSERT INTO choices (event_id, label, text, hidden_flag, flag_trigger_delay,
    stat_hygiene, stat_hunger, stat_popularity, stat_wealth, immediate_death, death_message)
VALUES
  ((SELECT id FROM game_events WHERE day_target=7), 'A',
   'Leerla con calma a la luz de una antorcha',
   NULL, 3, 0, 0, 5, 0, FALSE, NULL),
  ((SELECT id FROM game_events WHERE day_target=7), 'B',
   'Quemarla sin leerla «por si es un hechizo maligno»',
   'mensaje_ignorado', 2, 0, 0, -5, 0, FALSE,
   'La carta era un aviso de traición por parte de tus propios guardias. Haberla leído habría cambiado todo. Dos días después, lo descubriste de la peor forma posible.'),
  ((SELECT id FROM game_events WHERE day_target=7), 'C',
   'Devolverla sin abrir con una nota que dice «devuelta al remitente»',
   NULL, 3, 0, 0, 5, -5, FALSE, NULL);

-- ── DÍA 8 ──────────────────────────────────────────────────────────────────
INSERT INTO choices (event_id, label, text, hidden_flag, flag_trigger_delay,
    stat_hygiene, stat_hunger, stat_popularity, stat_wealth, immediate_death, death_message)
VALUES
  ((SELECT id FROM game_events WHERE day_target=8), 'A',
   'Probar la sopa con confianza real: un rey no teme su propia cocina',
   NULL, 3, 0, 15, 5, 0, FALSE, NULL),
  ((SELECT id FROM game_events WHERE day_target=8), 'B',
   'Hacer que el bufón la pruebe primero «como es tradición en el reino»',
   NULL, 3, 0, 5, 5, 0, FALSE, NULL),
  ((SELECT id FROM game_events WHERE day_target=8), 'C',
   'Declarar la sopa oficialmente inaceptable y pedir que repitan todo el menú',
   NULL, 3, 0, -10, -10, -10, FALSE, NULL);

-- ── DÍA 9 ──────────────────────────────────────────────────────────────────
INSERT INTO choices (event_id, label, text, hidden_flag, flag_trigger_delay,
    stat_hygiene, stat_hunger, stat_popularity, stat_wealth, immediate_death, death_message)
VALUES
  ((SELECT id FROM game_events WHERE day_target=9), 'A',
   'Escuchar sus demandas con paciencia y prometer reformas reales',
   NULL, 3, 0, 0, 15, -10, FALSE, NULL),
  ((SELECT id FROM game_events WHERE day_target=9), 'B',
   'Ignorarlos completamente y ordenar que les sirvan té y se vayan',
   NULL, 3, 0, 0, 0, 0, TRUE,
   'Los nobles, hartos de ser ignorados nueve días seguidos, organizaron un golpe de estado en tiempo récord. El historiador lo describió como «la rebelión más rápida de la historia medieval». Reinaste exactamente 9 días.'),
  ((SELECT id FROM game_events WHERE day_target=9), 'C',
   'Prometer todo lo que piden sabiendo perfectamente que no cumplirás nada',
   NULL, 3, 0, 0, 5, -5, FALSE, NULL);

-- ── DÍA 10 ─────────────────────────────────────────────────────────────────
INSERT INTO choices (event_id, label, text, hidden_flag, flag_trigger_delay,
    stat_hygiene, stat_hunger, stat_popularity, stat_wealth, immediate_death, death_message)
VALUES
  ((SELECT id FROM game_events WHERE day_target=10), 'A',
   'Contemplar el reino desde los jardines con orgullo solemne',
   NULL, 3, 5, 5, 5, 5, FALSE, NULL),
  ((SELECT id FROM game_events WHERE day_target=10), 'B',
   'Organizar una fiesta para todo el castillo para celebrar la supervivencia',
   NULL, 3, 0, 10, 20, -15, FALSE, NULL),
  ((SELECT id FROM game_events WHERE day_target=10), 'C',
   'Planear ya los próximos decretos absurdos para la semana que viene',
   NULL, 3, 0, 0, -5, 10, FALSE, NULL);
