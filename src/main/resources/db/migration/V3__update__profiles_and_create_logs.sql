-- 1. Actualizar la tabla de perfiles con las nuevas columnas de nutrición
ALTER TABLE usr_profiles 
ADD COLUMN target_protein INTEGER,
ADD COLUMN target_fat INTEGER,
ADD COLUMN target_carbs INTEGER,
ADD COLUMN is_configured BOOLEAN DEFAULT FALSE;

-- 2. Crear la tabla de histórico de peso que nos faltaba
CREATE TABLE usr_weight_logs (
    id UUID PRIMARY KEY,
    user_id UUID NOT NULL,
    weight DOUBLE PRECISION NOT NULL,
    log_date TIMESTAMP NOT NULL,
    CONSTRAINT fk_user_profile FOREIGN KEY (user_id) REFERENCES usr_profiles (user_id) ON DELETE CASCADE
);

-- Índice para optimizar consultas de evolución de peso
CREATE INDEX idx_weight_logs_user_date ON usr_weight_logs (user_id, log_date);