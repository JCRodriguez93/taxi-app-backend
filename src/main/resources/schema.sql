CREATE TABLE trips (
    id BIGSERIAL PRIMARY KEY,
    distance_km DOUBLE PRECISION NOT NULL CHECK (distance_km > 0),
    duration_min DOUBLE PRECISION NOT NULL CHECK (duration_min > 0),
    estimated_price NUMERIC(10,2) NOT NULL CHECK (estimated_price >= 0),
    origin_zone VARCHAR(50) NOT NULL,
    destination_zone VARCHAR(50) NOT NULL,
    vehicle_type VARCHAR(20) NOT NULL,
    status VARCHAR(20) NOT NULL DEFAULT 'PENDING',
    start_time TIMESTAMP NOT NULL,
    end_time TIMESTAMP NULL,
    created_at TIMESTAMP NOT NULL DEFAULT CURRENT_TIMESTAMP
);