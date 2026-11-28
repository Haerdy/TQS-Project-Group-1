-- =============================================================================
-- TäheLaev Fleet Management System - PostgreSQL Database Initialization Script
-- =============================================================================

CREATE EXTENSION IF NOT EXISTS "uuid-ossp";

-- -----------------------------------------------------------------------------
-- 1. Table Creation (Schema matching JPA Domain Entities)
-- -----------------------------------------------------------------------------

-- Base Users table
CREATE TABLE IF NOT EXISTS users (
    id UUID PRIMARY KEY DEFAULT uuid_generate_v4(),
    name VARCHAR(255) NOT NULL,
    email VARCHAR(255) NOT NULL UNIQUE,
    password VARCHAR(255) NOT NULL
);

-- Platform Managers (extends User)
CREATE TABLE IF NOT EXISTS platform_managers (
    id UUID PRIMARY KEY REFERENCES users(id) ON DELETE CASCADE
);

-- System Admins (extends User)
CREATE TABLE IF NOT EXISTS system_admins (
    id UUID PRIMARY KEY REFERENCES users(id) ON DELETE CASCADE
);

-- Fleets
CREATE TABLE IF NOT EXISTS fleets (
    id UUID PRIMARY KEY DEFAULT uuid_generate_v4(),
    fleet_name VARCHAR(255) NOT NULL UNIQUE,
    no_of_robots INTEGER NOT NULL DEFAULT 0,
    no_of_active_robots INTEGER NOT NULL DEFAULT 0,
    system_admin_id UUID REFERENCES system_admins(id),
    platform_manager_id UUID REFERENCES platform_managers(id)
);

-- End Users (extends User, belongs to a Fleet)
CREATE TABLE IF NOT EXISTS end_users (
    id UUID PRIMARY KEY REFERENCES users(id) ON DELETE CASCADE,
    address VARCHAR(255),
    fleet_id UUID REFERENCES fleets(id)
);

-- Platform Price Policy (1:1 with PlatformManager)
CREATE TABLE IF NOT EXISTS platform_price_policies (
    id UUID PRIMARY KEY DEFAULT uuid_generate_v4(),
    robot_monthly_fee REAL NOT NULL,
    refrig_monthly_fee REAL NOT NULL,
    mileage_percent_fee REAL NOT NULL,
    parcel_percent_fee REAL NOT NULL,
    platform_manager_id UUID UNIQUE REFERENCES platform_managers(id) ON DELETE CASCADE
);

-- Instance Price Policy (1:1 with Fleet)
CREATE TABLE IF NOT EXISTS instance_price_policies (
    id UUID PRIMARY KEY DEFAULT uuid_generate_v4(),
    base_km_fee REAL NOT NULL,
    refrigerated_km_fee REAL NOT NULL,
    parcel_percent_fee REAL NOT NULL,
    fleet_id UUID UNIQUE REFERENCES fleets(id) ON DELETE CASCADE
);

-- Base Robots table
CREATE TABLE IF NOT EXISTS robots (
    id UUID PRIMARY KEY DEFAULT uuid_generate_v4(),
    name VARCHAR(255) NOT NULL,
    status VARCHAR(50) NOT NULL,
    lifecycle VARCHAR(50) NOT NULL,
    battery REAL NOT NULL DEFAULT 100.0,
    mileage_km REAL NOT NULL DEFAULT 0.0,
    max_speed_km REAL NOT NULL,
    max_weight REAL NOT NULL,
    cur_weight REAL NOT NULL DEFAULT 0.0,
    cargo_space_liters DOUBLE PRECISION NOT NULL,
    latitude DOUBLE PRECISION,
    longitude DOUBLE PRECISION,
    fleet_id UUID REFERENCES fleets(id),
    current_parcel_id BIGINT,
    robot_type VARCHAR(31) NOT NULL
);

-- Standard Robots (extends Robot)
CREATE TABLE IF NOT EXISTS standard_robots (
    id UUID PRIMARY KEY REFERENCES robots(id) ON DELETE CASCADE
);

-- Refrigerated Robots (extends Robot)
CREATE TABLE IF NOT EXISTS refrigerated_robots (
    id UUID PRIMARY KEY REFERENCES robots(id) ON DELETE CASCADE,
    min_temp_c REAL,
    max_temp_c REAL,
    target_temp_c REAL,
    current_temp_c REAL
);

-- Parcels table
CREATE TABLE IF NOT EXISTS parcels (
    id BIGSERIAL PRIMARY KEY,
    tracking_code VARCHAR(255) NOT NULL UNIQUE,
    pickup_code VARCHAR(255) NOT NULL,
    size VARCHAR(50) NOT NULL,
    weight REAL NOT NULL,
    needs_refrigeration BOOLEAN NOT NULL DEFAULT FALSE,
    storage_temperature REAL,
    delivery_address VARCHAR(255) NOT NULL,
    status VARCHAR(50) NOT NULL DEFAULT 'CREATED',
    created_at TIMESTAMP WITHOUT TIME ZONE NOT NULL DEFAULT NOW(),
    pickup_deadline TIMESTAMP WITHOUT TIME ZONE,
    pickup_address VARCHAR(255) NOT NULL,
    is_urgent BOOLEAN NOT NULL DEFAULT FALSE,
    distance_km REAL,
    applied_km_rate REAL,
    parcel_items_total REAL,
    transport_cost REAL,
    hospital_percent REAL,
    hospital_fee_amount REAL,
    platform_percent REAL,
    platform_fee_amount REAL,
    total_amount REAL,
    end_user_id UUID REFERENCES end_users(id),
    robot_id UUID REFERENCES robots(id),
    fleet_id UUID REFERENCES fleets(id)
);

-- Foreign key linking robot's current parcel to parcels
ALTER TABLE robots
    DROP CONSTRAINT IF EXISTS fk_robots_current_parcel;
ALTER TABLE robots
    ADD CONSTRAINT fk_robots_current_parcel
    FOREIGN KEY (current_parcel_id) REFERENCES parcels(id) ON DELETE SET NULL;

-- Items table
CREATE TABLE IF NOT EXISTS items (
    id UUID PRIMARY KEY DEFAULT uuid_generate_v4(),
    name VARCHAR(255) NOT NULL,
    value REAL NOT NULL,
    iva REAL NOT NULL,
    weight REAL NOT NULL,
    is_temp_sensitive BOOLEAN NOT NULL DEFAULT FALSE,
    temp REAL,
    parcel_id BIGINT REFERENCES parcels(id) ON DELETE SET NULL
);

-- Alerts table
CREATE TABLE IF NOT EXISTS alerts (
    id UUID PRIMARY KEY DEFAULT uuid_generate_v4(),
    type VARCHAR(50) NOT NULL,
    message VARCHAR(255) NOT NULL,
    value REAL NOT NULL,
    time TIMESTAMP WITHOUT TIME ZONE NOT NULL DEFAULT NOW(),
    acknowledged BOOLEAN NOT NULL DEFAULT FALSE,
    ack_time TIMESTAMP WITHOUT TIME ZONE,
    fleet_id UUID NOT NULL REFERENCES fleets(id),
    robot_id UUID REFERENCES robots(id),
    acknowledged_by_id UUID REFERENCES system_admins(id)
);

-- -----------------------------------------------------------------------------
-- 2. Data Population
-- -----------------------------------------------------------------------------

-- Fixed UUIDs for predictable references
-- Platform Manager: aaaaaaaa-aaaa-aaaa-aaaa-aaaaaaaaaaaa
-- System Admin 1:   bbbbbbbb-bbbb-bbbb-bbbb-bbbbbbbbbbbb
-- System Admin 2:   cccccccc-cccc-cccc-cccc-cccccccccccc
-- Fleet 1:          11111111-1111-1111-1111-111111111111 (Hospital Central de Aveiro)
-- Fleet 2:          22222222-2222-2222-2222-222222222222 (Hospital Universitário de Coimbra)
-- End Users 1..5:   eeeeeeee-eeee-eeee-eeee-eeeeeeeeeee1..5

-- A. Insert Platform Manager
INSERT INTO users (id, name, email, password)
VALUES ('aaaaaaaa-aaaa-aaaa-aaaa-aaaaaaaaaaaa', 'Platform Admin', 'plataformmanager@email.com', 'password')
ON CONFLICT (id) DO NOTHING;

INSERT INTO platform_managers (id)
VALUES ('aaaaaaaa-aaaa-aaaa-aaaa-aaaaaaaaaaaa')
ON CONFLICT (id) DO NOTHING;

INSERT INTO platform_price_policies (id, robot_monthly_fee, refrig_monthly_fee, mileage_percent_fee, parcel_percent_fee, platform_manager_id)
VALUES ('a1a1a1a1-a1a1-a1a1-a1a1-a1a1a1a1a1a1', 50.0, 70.0, 10.0, 10.0, 'aaaaaaaa-aaaa-aaaa-aaaa-aaaaaaaaaaaa')
ON CONFLICT (id) DO NOTHING;

-- B. Insert System Admins
INSERT INTO users (id, name, email, password)
VALUES 
    ('bbbbbbbb-bbbb-bbbb-bbbb-bbbbbbbbbbbb', 'System Admin Aveiro', 'sysadmin@email.com', 'password'),
    ('cccccccc-cccc-cccc-cccc-cccccccccccc', 'System Admin Coimbra', 'sysadmin2@email.com', 'password')
ON CONFLICT (id) DO NOTHING;

INSERT INTO system_admins (id)
VALUES 
    ('bbbbbbbb-bbbb-bbbb-bbbb-bbbbbbbbbbbb'),
    ('cccccccc-cccc-cccc-cccc-cccccccccccc')
ON CONFLICT (id) DO NOTHING;

-- C. Insert Fleets
INSERT INTO fleets (id, fleet_name, no_of_robots, no_of_active_robots, system_admin_id, platform_manager_id)
VALUES 
    ('11111111-1111-1111-1111-111111111111', 'Hospital Central de Aveiro', 5, 5, 'bbbbbbbb-bbbb-bbbb-bbbb-bbbbbbbbbbbb', 'aaaaaaaa-aaaa-aaaa-aaaa-aaaaaaaaaaaa'),
    ('22222222-2222-2222-2222-222222222222', 'Hospital Universitário de Coimbra', 5, 5, 'cccccccc-cccc-cccc-cccc-cccccccccccc', 'aaaaaaaa-aaaa-aaaa-aaaa-aaaaaaaaaaaa')
ON CONFLICT (id) DO NOTHING;

-- D. Insert Instance Price Policies for Fleets
INSERT INTO instance_price_policies (id, base_km_fee, refrigerated_km_fee, parcel_percent_fee, fleet_id)
VALUES 
    ('f1f1f1f1-1111-1111-1111-111111111111', 1.50, 2.00, 10.0, '11111111-1111-1111-1111-111111111111'),
    ('f2f2f2f2-2222-2222-2222-222222222222', 1.75, 2.25, 10.0, '22222222-2222-2222-2222-222222222222')
ON CONFLICT (id) DO NOTHING;

-- E. Insert 5 End Users
INSERT INTO users (id, name, email, password)
VALUES 
    ('eeeeeeee-eeee-eeee-eeee-eeeeeeeeeee1', 'Maria Silva', 'enduser1@email.com', 'password'),
    ('eeeeeeee-eeee-eeee-eeee-eeeeeeeeeee2', 'João Santos', 'enduser2@email.com', 'password'),
    ('eeeeeeee-eeee-eeee-eeee-eeeeeeeeeee3', 'Ana Pereira', 'enduser3@email.com', 'password'),
    ('eeeeeeee-eeee-eeee-eeee-eeeeeeeeeee4', 'Carlos Ferreira', 'enduser4@email.com', 'password'),
    ('eeeeeeee-eeee-eeee-eeee-eeeeeeeeeee5', 'Beatriz Costa', 'enduser5@email.com', 'password')
ON CONFLICT (id) DO NOTHING;

INSERT INTO end_users (id, address, fleet_id)
VALUES 
    ('eeeeeeee-eeee-eeee-eeee-eeeeeeeeeee1', 'Rua da Boavista 12, Aveiro', '11111111-1111-1111-1111-111111111111'),
    ('eeeeeeee-eeee-eeee-eeee-eeeeeeeeeee2', 'Avenida Doutor Lourenço Peixinho 145, Aveiro', '11111111-1111-1111-1111-111111111111'),
    ('eeeeeeee-eeee-eeee-eeee-eeeeeeeeeee3', 'Rua de São Martinho 88, Coimbra', '22222222-2222-2222-2222-222222222222'),
    ('eeeeeeee-eeee-eeee-eeee-eeeeeeeeeee4', 'Praça da República 45, Coimbra', '22222222-2222-2222-2222-222222222222'),
    ('eeeeeeee-eeee-eeee-eeee-eeeeeeeeeee5', 'Rua do Brasil 230, Coimbra', '22222222-2222-2222-2222-222222222222')
ON CONFLICT (id) DO NOTHING;

-- F. Insert Robots (Fleet 1: 3 Standard, 2 Refrigerated)
-- Robot 1: Standard (HCA-STD-01) - Will have Parcel 2 assigned
INSERT INTO robots (id, name, status, lifecycle, battery, mileage_km, max_speed_km, max_weight, cur_weight, cargo_space_liters, latitude, longitude, fleet_id, robot_type)
VALUES ('r1010101-0001-0000-0000-000000000001', 'HCA-STD-01', 'EN_ROUTE', 'ACTIVE', 92.0, 14.5, 20.0, 10.0, 0.24, 45.0, 40.6300, -8.6500, '11111111-1111-1111-1111-111111111111', 'STANDARD')
ON CONFLICT (id) DO NOTHING;
INSERT INTO standard_robots (id) VALUES ('r1010101-0001-0000-0000-000000000001') ON CONFLICT (id) DO NOTHING;

-- Robot 2: Standard (HCA-STD-02) - Idle
INSERT INTO robots (id, name, status, lifecycle, battery, mileage_km, max_speed_km, max_weight, cur_weight, cargo_space_liters, latitude, longitude, fleet_id, robot_type)
VALUES ('r1010101-0002-0000-0000-000000000002', 'HCA-STD-02', 'IDLE', 'ACTIVE', 85.0, 32.0, 20.0, 8.0, 0.0, 40.0, 40.6310, -8.6510, '11111111-1111-1111-1111-111111111111', 'STANDARD')
ON CONFLICT (id) DO NOTHING;
INSERT INTO standard_robots (id) VALUES ('r1010101-0002-0000-0000-000000000002') ON CONFLICT (id) DO NOTHING;

-- Robot 3: Standard (HCA-STD-03) - Idle
INSERT INTO robots (id, name, status, lifecycle, battery, mileage_km, max_speed_km, max_weight, cur_weight, cargo_space_liters, latitude, longitude, fleet_id, robot_type)
VALUES ('r1010101-0003-0000-0000-000000000003', 'HCA-STD-03', 'IDLE', 'ACTIVE', 78.0, 48.0, 20.0, 12.0, 0.0, 50.0, 40.6320, -8.6520, '11111111-1111-1111-1111-111111111111', 'STANDARD')
ON CONFLICT (id) DO NOTHING;
INSERT INTO standard_robots (id) VALUES ('r1010101-0003-0000-0000-000000000003') ON CONFLICT (id) DO NOTHING;

-- Robot 4: Refrigerated (HCA-REF-01) - Will have Parcel 1 assigned
INSERT INTO robots (id, name, status, lifecycle, battery, mileage_km, max_speed_km, max_weight, cur_weight, cargo_space_liters, latitude, longitude, fleet_id, robot_type)
VALUES ('r1010101-0004-0000-0000-000000000004', 'HCA-REF-01', 'EN_ROUTE', 'ACTIVE', 88.0, 21.0, 18.0, 8.0, 0.35, 35.0, 40.6305, -8.6515, '11111111-1111-1111-1111-111111111111', 'REFRIGERATED')
ON CONFLICT (id) DO NOTHING;
INSERT INTO refrigerated_robots (id, min_temp_c, max_temp_c, target_temp_c, current_temp_c)
VALUES ('r1010101-0004-0000-0000-000000000004', 2.0, 8.0, 4.0, 4.2)
ON CONFLICT (id) DO NOTHING;

-- Robot 5: Refrigerated (HCA-REF-02) - Idle
INSERT INTO robots (id, name, status, lifecycle, battery, mileage_km, max_speed_km, max_weight, cur_weight, cargo_space_liters, latitude, longitude, fleet_id, robot_type)
VALUES ('r1010101-0005-0000-0000-000000000005', 'HCA-REF-02', 'IDLE', 'ACTIVE', 95.0, 8.0, 18.0, 10.0, 0.0, 40.0, 40.6315, -8.6525, '11111111-1111-1111-1111-111111111111', 'REFRIGERATED')
ON CONFLICT (id) DO NOTHING;
INSERT INTO refrigerated_robots (id, min_temp_c, max_temp_c, target_temp_c, current_temp_c)
VALUES ('r1010101-0005-0000-0000-000000000005', 2.0, 8.0, 4.0, 3.9)
ON CONFLICT (id) DO NOTHING;

-- G. Insert Robots (Fleet 2: 3 Standard, 2 Refrigerated)
-- Robot 6: Standard (HUC-STD-01) - Will have Parcel 3 assigned
INSERT INTO robots (id, name, status, lifecycle, battery, mileage_km, max_speed_km, max_weight, cur_weight, cargo_space_liters, latitude, longitude, fleet_id, robot_type)
VALUES ('r2020202-0001-0000-0000-000000000001', 'HUC-STD-01', 'EN_ROUTE', 'ACTIVE', 90.0, 18.0, 20.0, 10.0, 0.75, 45.0, 40.2100, -8.4100, '22222222-2222-2222-2222-222222222222', 'STANDARD')
ON CONFLICT (id) DO NOTHING;
INSERT INTO standard_robots (id) VALUES ('r2020202-0001-0000-0000-000000000001') ON CONFLICT (id) DO NOTHING;

-- Robot 7: Standard (HUC-STD-02) - Idle
INSERT INTO robots (id, name, status, lifecycle, battery, mileage_km, max_speed_km, max_weight, cur_weight, cargo_space_liters, latitude, longitude, fleet_id, robot_type)
VALUES ('r2020202-0002-0000-0000-000000000002', 'HUC-STD-02', 'IDLE', 'ACTIVE', 82.0, 25.0, 20.0, 10.0, 0.0, 45.0, 40.2110, -8.4110, '22222222-2222-2222-2222-222222222222', 'STANDARD')
ON CONFLICT (id) DO NOTHING;
INSERT INTO standard_robots (id) VALUES ('r2020202-0002-0000-0000-000000000002') ON CONFLICT (id) DO NOTHING;

-- Robot 8: Standard (HUC-STD-03) - Idle
INSERT INTO robots (id, name, status, lifecycle, battery, mileage_km, max_speed_km, max_weight, cur_weight, cargo_space_liters, latitude, longitude, fleet_id, robot_type)
VALUES ('r2020202-0003-0000-0000-000000000003', 'HUC-STD-03', 'IDLE', 'ACTIVE', 100.0, 5.0, 20.0, 15.0, 0.0, 60.0, 40.2120, -8.4120, '22222222-2222-2222-2222-222222222222', 'STANDARD')
ON CONFLICT (id) DO NOTHING;
INSERT INTO standard_robots (id) VALUES ('r2020202-0003-0000-0000-000000000003') ON CONFLICT (id) DO NOTHING;

-- Robot 9: Refrigerated (HUC-REF-01) - Idle
INSERT INTO robots (id, name, status, lifecycle, battery, mileage_km, max_speed_km, max_weight, cur_weight, cargo_space_liters, latitude, longitude, fleet_id, robot_type)
VALUES ('r2020202-0004-0000-0000-000000000004', 'HUC-REF-01', 'IDLE', 'ACTIVE', 85.0, 12.0, 18.0, 8.0, 0.0, 35.0, 40.2105, -8.4105, '22222222-2222-2222-2222-222222222222', 'REFRIGERATED')
ON CONFLICT (id) DO NOTHING;
INSERT INTO refrigerated_robots (id, min_temp_c, max_temp_c, target_temp_c, current_temp_c)
VALUES ('r2020202-0004-0000-0000-000000000004', 2.0, 8.0, 4.0, 4.0)
ON CONFLICT (id) DO NOTHING;

-- Robot 10: Refrigerated (HUC-REF-02) - Will have Parcel 4 assigned
INSERT INTO robots (id, name, status, lifecycle, battery, mileage_km, max_speed_km, max_weight, cur_weight, cargo_space_liters, latitude, longitude, fleet_id, robot_type)
VALUES ('r2020202-0005-0000-0000-000000000005', 'HUC-REF-02', 'EN_ROUTE', 'ACTIVE', 91.0, 16.0, 18.0, 8.0, 0.15, 35.0, 40.2115, -8.4115, '22222222-2222-2222-2222-222222222222', 'REFRIGERATED')
ON CONFLICT (id) DO NOTHING;
INSERT INTO refrigerated_robots (id, min_temp_c, max_temp_c, target_temp_c, current_temp_c)
VALUES ('r2020202-0005-0000-0000-000000000005', 2.0, 8.0, 4.0, 4.5)
ON CONFLICT (id) DO NOTHING;

-- H. Insert 4 Parcels (ASSIGNED to 4 specific robots)
INSERT INTO parcels (
    id, tracking_code, pickup_code, size, weight, needs_refrigeration, storage_temperature,
    delivery_address, status, created_at, pickup_deadline, pickup_address, is_urgent,
    distance_km, applied_km_rate, parcel_items_total, transport_cost, hospital_percent,
    hospital_fee_amount, platform_percent, platform_fee_amount, total_amount,
    end_user_id, robot_id, fleet_id
) VALUES 
    -- Parcel 1: Assigned to HCA-REF-01 (Fleet 1, Refrigerated)
    (
        1, 'TRK-AVEIRO-REF01', '4120', 'M', 0.35, TRUE, 4.0,
        'Rua da Boavista 12, Aveiro', 'ASSIGNED', NOW(), NOW() + INTERVAL '2 hours', 'Farmácia Hospitalar Central Aveiro', TRUE,
        2.5, 2.00, 77.50, 5.00, 10.0, 7.75, 10.0, 8.25, 98.50,
        'eeeeeeee-eeee-eeee-eeee-eeeeeeeeeee1', 'r1010101-0004-0000-0000-000000000004', '11111111-1111-1111-1111-111111111111'
    ),
    -- Parcel 2: Assigned to HCA-STD-01 (Fleet 1, Standard)
    (
        2, 'TRK-AVEIRO-STD01', '8954', 'S', 0.24, FALSE, NULL,
        'Avenida Doutor Lourenço Peixinho 145, Aveiro', 'ASSIGNED', NOW(), NOW() + INTERVAL '3 hours', 'Farmácia Hospitalar Central Aveiro', FALSE,
        2.0, 1.50, 14.10, 3.00, 10.0, 1.41, 10.0, 1.71, 20.22,
        'eeeeeeee-eeee-eeee-eeee-eeeeeeeeeee2', 'r1010101-0001-0000-0000-000000000001', '11111111-1111-1111-1111-111111111111'
    ),
    -- Parcel 3: Assigned to HUC-STD-01 (Fleet 2, Standard)
    (
        3, 'TRK-COIMBRA-STD01', '7231', 'L', 0.75, FALSE, NULL,
        'Rua de São Martinho 88, Coimbra', 'ASSIGNED', NOW(), NOW() + INTERVAL '4 hours', 'Farmácia Geral HUC Coimbra', FALSE,
        3.0, 1.75, 8.74, 5.25, 10.0, 0.87, 10.0, 1.40, 16.26,
        'eeeeeeee-eeee-eeee-eeee-eeeeeeeeeee3', 'r2020202-0001-0000-0000-000000000001', '22222222-2222-2222-2222-222222222222'
    ),
    -- Parcel 4: Assigned to HUC-REF-02 (Fleet 2, Refrigerated)
    (
        4, 'TRK-COIMBRA-REF01', '6619', 'XS', 0.15, TRUE, 4.0,
        'Praça da República 45, Coimbra', 'ASSIGNED', NOW(), NOW() + INTERVAL '1 hour', 'Farmácia Geral HUC Coimbra', TRUE,
        1.8, 2.25, 44.80, 4.05, 10.0, 4.48, 10.0, 4.89, 58.22,
        'eeeeeeee-eeee-eeee-eeee-eeeeeeeeeee4', 'r2020202-0005-0000-0000-000000000005', '22222222-2222-2222-2222-222222222222'
    )
ON CONFLICT (id) DO NOTHING;

-- Reset sequence for parcels
SELECT setval('parcels_id_seq', (SELECT MAX(id) FROM parcels));

-- Update the 4 assigned robots with their current_parcel_id
UPDATE robots SET current_parcel_id = 1 WHERE id = 'r1010101-0004-0000-0000-000000000004';
UPDATE robots SET current_parcel_id = 2 WHERE id = 'r1010101-0001-0000-0000-000000000001';
UPDATE robots SET current_parcel_id = 3 WHERE id = 'r2020202-0001-0000-0000-000000000001';
UPDATE robots SET current_parcel_id = 4 WHERE id = 'r2020202-0005-0000-0000-000000000005';

-- I. Insert 20 Pharmaceutical Items
INSERT INTO items (id, name, value, iva, weight, is_temp_sensitive, temp, parcel_id)
VALUES 
    -- Refrigerated items assigned to Parcel 1 (Insulinas)
    ('dddddddd-0000-0000-0000-000000000001', 'Insulina Glargina 100 UI/ml', 35.50, 6.0, 0.15, TRUE, 4.0, 1),
    ('dddddddd-0000-0000-0000-000000000002', 'Insulina Asparte Caneta', 42.00, 6.0, 0.20, TRUE, 4.0, 1),

    -- Refrigerated items assigned to Parcel 4 (Vacina + Colírio)
    ('dddddddd-0000-0000-0000-000000000003', 'Vacina Antigripal Tetravalente', 16.50, 6.0, 0.10, TRUE, 5.0, 4),
    ('dddddddd-0000-0000-0000-000000000004', 'Colírio Ciclosporina 0.05%', 28.30, 6.0, 0.05, TRUE, 4.0, 4),

    -- Other Refrigerated items available in catalog (not yet in parcel)
    ('dddddddd-0000-0000-0000-000000000005', 'Ampola de Probiótico Líquido', 19.80, 6.0, 0.12, TRUE, 6.0, NULL),

    -- Standard items assigned to Parcel 2 (Dor e curativos)
    ('dddddddd-0000-0000-0000-000000000006', 'Ibuprofeno 400 mg (20 comp)', 4.50, 6.0, 0.08, FALSE, NULL, 2),
    ('dddddddd-0000-0000-0000-000000000007', 'Paracetamol 500 mg (20 comp)', 3.20, 6.0, 0.07, FALSE, NULL, 2),
    ('dddddddd-0000-0000-0000-000000000010', 'Esparadrapo Hipoalergênico 5cm', 2.90, 23.0, 0.05, FALSE, NULL, 2),
    ('dddddddd-0000-0000-0000-000000000011', 'Bandaid Curativos Sortidos (20 un)', 3.50, 23.0, 0.04, FALSE, NULL, 2),

    -- Standard items assigned to Parcel 3 (Cuidados hospitalares)
    ('dddddddd-0000-0000-0000-000000000012', 'Compressas de Gaze Estéril (10 un)', 1.80, 23.0, 0.05, FALSE, NULL, 3),
    ('dddddddd-0000-0000-0000-000000000015', 'Soro Fisiológico 0.9% 500ml', 1.95, 6.0, 0.52, FALSE, NULL, 3),
    ('dddddddd-0000-0000-0000-000000000020', 'Máscaras Cirúrgicas (caixa 50 un)', 4.99, 6.0, 0.18, FALSE, NULL, 3),

    -- Standard items available in catalog (not yet in parcel)
    ('dddddddd-0000-0000-0000-000000000008', 'Amoxicilina 500 mg (16 cáps)', 6.80, 6.0, 0.10, FALSE, NULL, NULL),
    ('dddddddd-0000-0000-0000-000000000009', 'Omeprazol 20 mg (28 cáps)', 5.10, 6.0, 0.06, FALSE, NULL, NULL),
    ('dddddddd-0000-0000-0000-000000000013', 'Álcool Etílico 70% 250ml', 2.20, 23.0, 0.26, FALSE, NULL, NULL),
    ('dddddddd-0000-0000-0000-000000000014', 'Água Oxigenada 10 Vol 200ml', 1.50, 23.0, 0.22, FALSE, NULL, NULL),
    ('dddddddd-0000-0000-0000-000000000016', 'Pomada Cicatrizante Bepanthene 100g', 8.40, 23.0, 0.12, FALSE, NULL, NULL),
    ('dddddddd-0000-0000-0000-000000000017', 'Termômetro Digital Clínico', 9.90, 23.0, 0.08, FALSE, NULL, NULL),
    ('dddddddd-0000-0000-0000-000000000018', 'Vitamina C Efervescente 1000mg', 7.20, 23.0, 0.12, FALSE, NULL, NULL),
    ('dddddddd-0000-0000-0000-000000000019', 'Antialérgico Cetirizina 10mg', 6.10, 6.0, 0.05, FALSE, NULL, NULL)
ON CONFLICT (id) DO NOTHING;
