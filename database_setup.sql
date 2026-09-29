-- Setup script for Murach SQL Gateway in PostgreSQL
-- Database: murach

-- 1. Create table "User" (quoted because 'user' is a PostgreSQL reserved keyword)
CREATE TABLE IF NOT EXISTS "User" (
    "UserID" SERIAL PRIMARY KEY,
    "FirstName" VARCHAR(50) NOT NULL,
    "LastName" VARCHAR(50) NOT NULL,
    "Email" VARCHAR(100) NOT NULL
);

-- 2. Create standard lowercase table users as well
CREATE TABLE IF NOT EXISTS users (
    user_id SERIAL PRIMARY KEY,
    first_name VARCHAR(50) NOT NULL,
    last_name VARCHAR(50) NOT NULL,
    email VARCHAR(100) NOT NULL
);

-- 3. Clear existing test data to prevent duplicates if re-run
TRUNCATE TABLE "User" RESTART IDENTITY;
TRUNCATE TABLE users RESTART IDENTITY;

-- 4. Insert sample data into "User"
INSERT INTO "User" ("FirstName", "LastName", "Email") VALUES
('Joel', 'Murach', 'joel@murach.com'),
('Mike', 'Murach', 'mike@murach.com'),
('Andrea', 'Steelman', 'andi@murach.com'),
('John', 'Smith', 'jsmith@gmail.com'),
('Phuoc', 'Anh', 'phuocanh@example.com');

-- 5. Insert sample data into users
INSERT INTO users (first_name, last_name, email) VALUES
('Joel', 'Murach', 'joel@murach.com'),
('Mike', 'Murach', 'mike@murach.com'),
('Andrea', 'Steelman', 'andi@murach.com'),
('John', 'Smith', 'jsmith@gmail.com'),
('Phuoc', 'Anh', 'phuocanh@example.com');

-- 6. Also create Product table for rich SQL testing
CREATE TABLE IF NOT EXISTS products (
    product_id SERIAL PRIMARY KEY,
    product_code VARCHAR(10) NOT NULL UNIQUE,
    product_description VARCHAR(100) NOT NULL,
    product_price NUMERIC(10, 2) NOT NULL
);

TRUNCATE TABLE products RESTART IDENTITY;

INSERT INTO products (product_code, product_description, product_price) VALUES
('8601', '86 (the band) - True Life Songs and Stories', 14.95),
('pf01', 'Paddlefoot - The first CD', 12.95),
('pf02', 'Paddlefoot - The second CD', 14.95),
('jr01', 'Joe Rut - Genuine Wood Grained Finish', 14.95);
