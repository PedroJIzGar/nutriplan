CREATE TABLE ingredients (
    id UUID PRIMARY KEY,
    name VARCHAR(100) NOT NULL,
    default_unit VARCHAR(30) NOT NULL,
    category VARCHAR(30) NOT NULL,
    created_at TIMESTAMP WITH TIME ZONE NOT NULL DEFAULT NOW()
);

CREATE UNIQUE INDEX uk_ingredient_name_lower
    ON ingredients (LOWER(name));

CREATE TABLE recipes (
    id UUID PRIMARY KEY,
    name VARCHAR(150) NOT NULL,
    description VARCHAR(1000),
    meal_type VARCHAR(30) NOT NULL,
    servings INTEGER NOT NULL CHECK (servings > 0),
    target_kcal NUMERIC(10,2) NOT NULL CHECK (target_kcal > 0),
    protein NUMERIC(10,2) NOT NULL CHECK (protein >= 0),
    carbs NUMERIC(10,2) NOT NULL CHECK (carbs >= 0),
    fat NUMERIC(10,2) NOT NULL CHECK (fat >= 0),
    prep_time_minutes INTEGER NOT NULL CHECK (prep_time_minutes > 0),
    active BOOLEAN NOT NULL DEFAULT TRUE,
    created_at TIMESTAMP WITH TIME ZONE NOT NULL DEFAULT NOW()
);

CREATE TABLE recipe_ingredients (
    id UUID PRIMARY KEY,
    recipe_id UUID NOT NULL,
    ingredient_id UUID NOT NULL,
    quantity NUMERIC(10,2) NOT NULL CHECK (quantity > 0),
    unit VARCHAR(30) NOT NULL,
    notes VARCHAR(255),

    CONSTRAINT fk_recipe_ingredients_recipe
        FOREIGN KEY (recipe_id)
        REFERENCES recipes (id)
        ON DELETE CASCADE,

    CONSTRAINT fk_recipe_ingredients_ingredient
        FOREIGN KEY (ingredient_id)
        REFERENCES ingredients (id),

    CONSTRAINT uk_recipe_ingredient_recipe_id_ingredient_id
        UNIQUE (recipe_id, ingredient_id)
);