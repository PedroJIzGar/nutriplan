CREATE TABLE planning_groups (
    id UUID PRIMARY KEY,
    name VARCHAR(100) NOT NULL,
    created_by_user_id UUID NOT NULL,
    created_at TIMESTAMP WITH TIME ZONE NOT NULL DEFAULT NOW(),

    CONSTRAINT fk_planning_groups_created_by
        FOREIGN KEY (created_by_user_id)
        REFERENCES usr_profiles(user_id)
        ON DELETE RESTRICT
);

CREATE TABLE planning_group_members (
    id UUID PRIMARY KEY,
    planning_group_id UUID NOT NULL,
    user_id UUID NOT NULL,
    role VARCHAR(20) NOT NULL,
    portion_factor DOUBLE PRECISION NOT NULL,

    CONSTRAINT fk_planning_group_members_group
        FOREIGN KEY (planning_group_id)
        REFERENCES planning_groups(id)
        ON DELETE CASCADE,

    CONSTRAINT fk_planning_group_members_user
        FOREIGN KEY (user_id)
        REFERENCES usr_profiles(user_id)
        ON DELETE RESTRICT,

    CONSTRAINT uk_planning_group_member_group_user
        UNIQUE (planning_group_id, user_id)
);

CREATE INDEX idx_planning_groups_created_by_user_id
    ON planning_groups(created_by_user_id);

CREATE INDEX idx_planning_group_members_group_id
    ON planning_group_members(planning_group_id);

CREATE INDEX idx_planning_group_members_user_id
    ON planning_group_members(user_id);