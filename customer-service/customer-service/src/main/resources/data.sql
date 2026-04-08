CREATE TABLE IF NOT EXISTS roles (
    role_id UUID PRIMARY KEY,
    role_name VARCHAR(255) NOT NULL UNIQUE
);

CREATE TABLE IF NOT EXISTS customers (
    customer_id UUID PRIMARY KEY,
    username VARCHAR(255) NOT NULL UNIQUE,
    email VARCHAR(255) NOT NULL UNIQUE,
    password VARCHAR(255) NOT NULL,
    first_name VARCHAR(255),
    last_name VARCHAR(255),
    phone VARCHAR(255),
    delivery_address VARCHAR(255),
    city VARCHAR(255),
    role_id UUID NOT NULL REFERENCES roles (role_id),
    created_at TIMESTAMP NOT NULL,
    updated_at TIMESTAMP
);

INSERT INTO roles (role_id, role_name)
SELECT gen_random_uuid(), v
FROM (
    VALUES
        ('ROLE_CUSTOMER'),
        ('ROLE_ADMIN'),
        ('ROLE_RESTAURANT_OWNER'),
        ('ROLE_DELIVERY_DRIVER')
) AS t (v)
WHERE NOT EXISTS (SELECT 1 FROM roles r WHERE r.role_name = t.v);


INSERT INTO customers (
    customer_id,
    username,
    email,
    password,
    first_name,
    last_name,
    phone,
    delivery_address,
    city,
    role_id,
    created_at,
    updated_at
)
SELECT
    gen_random_uuid(),
    'admin',
    'admin@fooddelivery.com',
    '$2a$10$Tbiy3a/XxebvXrh5jPcewez35TugZOIlGTB9s8ARfofEOZ7qRvxYS',
    'System',
    'Admin',
    NULL,
    NULL,
    NULL,
    r.role_id,
    NOW(),
    NOW()
FROM roles r
WHERE r.role_name = 'ROLE_ADMIN'
  AND NOT EXISTS (SELECT 1 FROM customers c WHERE c.email = 'admin@fooddelivery.com');
