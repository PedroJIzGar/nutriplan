-- 1. Si por casualidad existía la columna userId (con camello), la borramos
-- 2. Nos aseguramos de que user_id sea de tipo UUID para que coincida con UserProfile
ALTER TABLE usr_weight_logs 
DROP COLUMN IF EXISTS "userId";

-- 3. Aseguramos la integridad de la relación si no estaba bien definida
ALTER TABLE usr_weight_logs
DROP CONSTRAINT IF EXISTS fk_user_profile;

ALTER TABLE usr_weight_logs
ADD CONSTRAINT fk_user_profile 
FOREIGN KEY (user_id) REFERENCES usr_profiles(user_id) 
ON DELETE CASCADE;