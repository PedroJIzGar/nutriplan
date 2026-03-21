CREATE TABLE weekly_plans (
    id UUID PRIMARY KEY,
    user_id UUID NOT NULL,
    start_date DATE NOT NULL,
    end_date DATE NOT NULL,
    created_at TIMESTAMPTZ NOT NULL DEFAULT NOW(),

    CONSTRAINT fk_weekly_plans_user
        FOREIGN KEY (user_id) REFERENCES usr_profiles(user_id),

    CONSTRAINT uk_weekly_plans_user_start_date
        UNIQUE (user_id, start_date)
);

CREATE INDEX idx_weekly_plans_user_id
    ON weekly_plans (user_id);

CREATE INDEX idx_weekly_plans_start_date
    ON weekly_plans (start_date);


CREATE TABLE planned_meals (
    id UUID PRIMARY KEY,
    weekly_plan_id UUID NOT NULL,
    recipe_id UUID NOT NULL,
    day_of_week VARCHAR(20) NOT NULL,
    meal_type VARCHAR(30) NOT NULL,
    created_at TIMESTAMPTZ NOT NULL DEFAULT NOW(),

    CONSTRAINT fk_planned_meals_weekly_plan
        FOREIGN KEY (weekly_plan_id) REFERENCES weekly_plans(id) ON DELETE CASCADE,

    CONSTRAINT fk_planned_meals_recipe
        FOREIGN KEY (recipe_id) REFERENCES recipes(id),

    CONSTRAINT uk_planned_meals_slot
        UNIQUE (weekly_plan_id, day_of_week, meal_type)
);

CREATE INDEX idx_planned_meals_weekly_plan_id
    ON planned_meals (weekly_plan_id);

CREATE INDEX idx_planned_meals_recipe_id
    ON planned_meals (recipe_id);