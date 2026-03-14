DROP TABLE IF EXISTS event_publication;
CREATE TABLE event_publication (
    id UUID PRIMARY KEY,
    event_type VARCHAR(512) NOT NULL,
    listener_id VARCHAR(512) NOT NULL,
    publication_date TIMESTAMP WITH TIME ZONE NOT NULL,
    serialized_event VARCHAR(4000) NOT NULL,
    completion_date TIMESTAMP WITH TIME ZONE,
    completion_attempts INT DEFAULT 0,
    last_resubmission_date TIMESTAMP WITH TIME ZONE,
    status VARCHAR(20) DEFAULT 'PUBLISHED'
);