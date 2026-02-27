CREATE TABLE trips (
    id BIGSERIAL PRIMARY KEY,
    distance_km DOUBLE PRECISION NOT NULL CHECK (distance_km > 0),
    duration_min DOUBLE PRECISION NOT NULL CHECK (duration_min > 0),
    estimated_price NUMERIC(10,2) NOT NULL CHECK (estimated_price >= 0),
    created_at TIMESTAMP NOT NULL DEFAULT CURRENT_TIMESTAMP
);