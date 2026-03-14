-- 1. Creamos la función que actualizará el campo updated_at
CREATE OR REPLACE FUNCTION update_updated_at_column() RETURNS TRIGGER AS $$ BEGIN NEW.updated_at = CURRENT_TIMESTAMP;
RETURN NEW;
END;
$$ language 'plpgsql';
-- 2. Creamos la tabla (sin el ON UPDATE de MySQL, que aquí no existe)
CREATE TABLE IF NOT EXISTS usr_profiles (
    user_id UUID PRIMARY KEY,
    first_name VARCHAR(255) NOT NULL,
    last_name VARCHAR(255) NOT NULL,
    email VARCHAR(255) NOT NULL UNIQUE,
    age INT,
    weight_kg DOUBLE PRECISION NOT NULL,
    height_cm DOUBLE PRECISION NOT NULL,
    gender VARCHAR(20),
    activity_level VARCHAR(50) NOT NULL,
    goal VARCHAR(50) NOT NULL,
    target_kcal INT,
    created_at TIMESTAMP WITH TIME ZONE DEFAULT CURRENT_TIMESTAMP,
    updated_at TIMESTAMP WITH TIME ZONE DEFAULT CURRENT_TIMESTAMP
);
-- 3. Creamos el Trigger que llama a la función antes de cada UPDATE
CREATE TRIGGER update_usr_profiles_modtime BEFORE
UPDATE ON usr_profiles FOR EACH ROW EXECUTE FUNCTION update_updated_at_column();