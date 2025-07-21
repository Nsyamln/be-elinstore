CREATE DATABASE ElinStore;
USE ElinStore;

CREATE TABLE users (
    user_id CHAR(5) PRIMARY KEY,
    name VARCHAR (255),
    email VARCHAR(255) NOT NULL,
    password VARCHAR(255) NOT NULL,
    role VARCHAR(32) NOT NULL,
    phone VARCHAR(15) NOT NULL,   
    created_by CHAR(5) NOT NULL,
    updated_by CHAR(5),
    deleted_by CHAR(5),
    created_at TIMESTAMP NOT NULL,
    updated_at TIMESTAMP,
    deleted_at TIMESTAMP
);


SELECT * FROM sales ORDER BY sale_id DESC;



INSERT INTO users (
    name,
    email,
    password,
    role,
    phone,
    created_by,
    updated_by,
    deleted_by,
    created_at,
    updated_at,
    deleted_at
) VALUES (
    'Serena',       -- name
    'serenity@gmail.com', -- email
    '$2a$10$uIBEJjpFQcMlzSM40cBOBOKrmd6QdcHiPiwtBjm/WjMkrsHgarVTO',-- password (sebaiknya hashed)
    'PELANGGAN',          -- role
    '08123876789',   -- phone
    'US001',      -- created_by
    NULL,            -- updated_by
    NULL,            -- deleted_by
    NOW(),           -- created_at
    NULL,            -- updated_at
    NULL             -- deleted_at
);


CREATE TABLE addresses (
    address_id CHAR(5) PRIMARY KEY,
    user_id CHAR(5) NOT NULL,
    street TEXT NOT NULL,
    rt VARCHAR(3) NOT NULL,
    rw VARCHAR(3) NOT NULL,
    village VARCHAR(255) NOT NULL,
    district VARCHAR(255) NOT NULL,
    city VARCHAR(255) NOT NULL,
    postal_code CHAR(5) NOT NULL
    
);


CREATE TABLE products (
    product_id CHAR(5) PRIMARY KEY,
    product_name VARCHAR(255),
    description TEXT,
    unit INT,
    price DECIMAL(10, 2),
    stock INT,
    product_image VARCHAR(255),

    supplier_id CHAR(5),
    purchase_price DECIMAL(10, 2),
    created_by CHAR(5) NOT NULL,
    updated_by CHAR(5),
    deleted_by CHAR(5),
    created_at TIMESTAMP NOT NULL,
    updated_at TIMESTAMP,
    deleted_at TIMESTAMP
);


CREATE TABLE sales (
    sale_id CHAR(5) PRIMARY KEY,
    sale_date TIMESTAMP,
    total_price DECIMAL(10, 2),
    customer_id CHAR(5),
    order_id CHAR(5),
    amount_paid DECIMAL(10, 2),
    payment_method VARCHAR(50)
);


CREATE TABLE sale_details (
    detail_id CHAR(5) PRIMARY KEY,
    sale_id CHAR(5),
    product_id CHAR(5),
    product_name VARCHAR(255),
    quantity INT,
    price INT,
    unit INT
);


-- Create table for orders
CREATE TABLE orders (
    order_id CHAR(5) PRIMARY KEY,
    order_date TIMESTAMP NOT NULL,
    customer_id CHAR(5) NOT NULL,
    delivery_address TEXT NOT NULL,
    phone VARCHAR(15) NOT NULL,  
    status VARCHAR(32) NOT NULL,
    created_by CHAR(5) NOT NULL,
    updated_by CHAR(5),
    created_at TIMESTAMP NOT NULL,
    updated_at TIMESTAMP,
    shipping_cost DECIMAL(10, 2),
    tracking_number VARCHAR(50),
    courier VARCHAR(50),
    shipping_method VARCHAR(50),
    estimated_delivery_date TIMESTAMP,
    actual_delivery_date TIMESTAMP;
);


CREATE TABLE profit_sharing (
    profit_sharing_id CHAR(5) PRIMARY KEY,
    sale_id CHAR(5),
    product_id CHAR(5),
    supplier_id CHAR(5),
    product_quantity INT,
    total_purchase_price DECIMAL(10, 2),
    total_sale_price DECIMAL(10, 2),
    status VARCHAR (32),
    payment_date TIMESTAMP
);

create table notification(
	notif_id INT primary key ,
	description VARCHAR(255),
	category VARCHAR (25),
	
	created_at TIMESTAMP,
    deleted_at TIMESTAMP
);

-- 1. Tambahkan sequence
CREATE SEQUENCE notification_notif_id_seq
START 1
INCREMENT 1;

-- 2. Atur kolom notif_id untuk menggunakan sequence sebagai default
ALTER TABLE notification
ALTER COLUMN notif_id SET DEFAULT nextval('notification_notif_id_seq');

-- 3. Set kolom notif_id sebagai PRIMARY KEY jika belum

delete from  notification 


alter table addresses add constraint fk_addr1 foreign key (user_id) references users (user_id);
ALTER TABLE sales ADD CONSTRAINT fk_sale2 FOREIGN KEY (order_id) REFERENCES orders (order_id);
ALTER TABLE products ADD CONSTRAINT fk_prod1 FOREIGN KEY (supplier_id) REFERENCES users (user_id);
ALTER TABLE sales ADD CONSTRAINT fk_sale1 FOREIGN KEY (customer_id) REFERENCES users (user_id);
ALTER TABLE sale_details ADD CONSTRAINT fk_detail1 FOREIGN KEY (sale_id) REFERENCES sales (sale_id);
ALTER TABLE sale_details ADD CONSTRAINT fk_detail2 FOREIGN KEY (product_id) REFERENCES products (product_id);
ALTER TABLE profit_sharing ADD CONSTRAINT fk_pros1 FOREIGN KEY (sale_id) REFERENCES sales (sale_id);
ALTER TABLE profit_sharing ADD CONSTRAINT fk_pros2 FOREIGN KEY (product_id) REFERENCES products (product_id);
ALTER TABLE profit_sharing ADD CONSTRAINT fk_pros3 FOREIGN KEY (supplier_id) REFERENCES users (user_id);

-----------------------------------------------------------------------------------------------------------
-- generate id product 
CREATE OR REPLACE FUNCTION generate_product_id() RETURNS TRIGGER AS $$
DECLARE
    last_id INT;
    new_id VARCHAR(10);
BEGIN
    -- Mendapatkan nilai terbesar dari ID yang ada
    SELECT COALESCE(MAX(CAST(SUBSTRING(product_id FROM 3) AS INTEGER)), 0) 
    INTO last_id 
    FROM products;

    -- Menambahkan 1 pada nilai terbesar untuk ID baru
    new_id := CONCAT('PR', LPAD((last_id + 1)::TEXT, 3, '0'));

    -- Mengatur ID baru pada baris yang akan dimasukkan
    NEW.product_id := new_id;

    RETURN NEW;
END;
$$ LANGUAGE plpgsql;

CREATE TRIGGER generate_product_id
BEFORE INSERT ON products
FOR EACH ROW
EXECUTE FUNCTION generate_product_id();
------------------------------------------ generate id address 
CREATE OR REPLACE FUNCTION generate_address_id() RETURNS TRIGGER AS $$
DECLARE
    last_id INT;
    new_id VARCHAR(10);
BEGIN
    -- Mendapatkan nilai terbesar dari ID yang ada
    SELECT COALESCE(MAX(CAST(SUBSTRING(address_id FROM 3) AS INTEGER)), 0) 
    INTO last_id 
    FROM addresses;

    -- Menambahkan 1 pada nilai terbesar untuk ID baru
    new_id := CONCAT('AD', LPAD((last_id + 1)::TEXT, 3, '0'));

    -- Mengatur ID baru pada baris yang akan dimasukkan
    NEW.address_id := new_id;

    RETURN NEW;
END;
$$ LANGUAGE plpgsql;

CREATE TRIGGER generate_address_id
BEFORE INSERT ON addresses
FOR EACH ROW
EXECUTE FUNCTION generate_address_id();

------------------------------------------ generate id user
CREATE OR REPLACE FUNCTION generate_user_id() RETURNS TRIGGER AS $$
DECLARE
    last_id INT;
    new_id VARCHAR(10);
BEGIN
    -- Mendapatkan nilai terbesar dari ID yang ada
    SELECT COALESCE(MAX(CAST(SUBSTRING(user_id FROM 3) AS INTEGER)), 0) 
    INTO last_id 
    FROM users;

    -- Menambahkan 1 pada nilai terbesar untuk ID baru
    new_id := CONCAT('US', LPAD((last_id + 1)::TEXT, 3, '0'));

    -- Mengatur ID baru pada baris yang akan dimasukkan
    NEW.user_id := new_id;

    RETURN NEW;
END;
$$ LANGUAGE plpgsql;

CREATE TRIGGER generate_user_id
BEFORE INSERT ON users
FOR EACH ROW
EXECUTE FUNCTION generate_user_id();

------------------------------------------ generate id sale
CREATE OR REPLACE FUNCTION generate_sale_id() RETURNS TRIGGER AS $$
DECLARE
    last_id INT;
    new_id VARCHAR(10);
BEGIN
    -- Mendapatkan nilai terbesar dari ID yang ada
    SELECT COALESCE(MAX(CAST(SUBSTRING(sale_id FROM 3) AS INTEGER)), 0) 
    INTO last_id 
    FROM sales;

    -- Menambahkan 1 pada nilai terbesar untuk ID baru
    new_id := CONCAT('PJ', LPAD((last_id + 1)::TEXT, 3, '0'));

    -- Mengatur ID baru pada baris yang akan dimasukkan
    NEW.sale_id := new_id;

    RETURN NEW;
END;
$$ LANGUAGE plpgsql;

CREATE TRIGGER generate_sale_id
BEFORE INSERT ON sales
FOR EACH ROW
EXECUTE FUNCTION generate_sale_id();
------------------------------------------ generate id provit sharing
CREATE OR REPLACE FUNCTION generate_sharing_id() RETURNS TRIGGER AS $$
DECLARE
    last_id INT;
    new_id VARCHAR(10);
BEGIN
    -- Mendapatkan nilai terbesar dari ID yang ada
    SELECT COALESCE(MAX(CAST(SUBSTRING(profit_sharing_id FROM 3) AS INTEGER)), 0) 
    INTO last_id 
    FROM profit_sharing;

    -- Menambahkan 1 pada nilai terbesar untuk ID baru
    new_id := CONCAT('PS', LPAD((last_id + 1)::TEXT, 3, '0'));

    -- Mengatur ID baru pada baris yang akan dimasukkan
    NEW.profit_sharing_id := new_id;

    RETURN NEW;
END;
$$ LANGUAGE plpgsql;

CREATE TRIGGER generate_sharing_id
BEFORE INSERT ON profit_sharing
FOR EACH ROW
EXECUTE FUNCTION generate_sharing_id();
------------------------------------------ generate id sale details
CREATE OR REPLACE FUNCTION generate_detail_id() RETURNS TRIGGER AS $$
DECLARE
    last_id INT;
    new_id VARCHAR(10);
BEGIN
    -- Mendapatkan nilai terbesar dari ID yang ada
    SELECT COALESCE(MAX(CAST(SUBSTRING(detail_id FROM 3) AS INTEGER)), 0) 
    INTO last_id 
    FROM sale_details ;

    -- Menambahkan 1 pada nilai terbesar untuk ID baru
    new_id := CONCAT('DP', LPAD((last_id + 1)::TEXT, 3, '0'));

    -- Mengatur ID baru pada baris yang akan dimasukkan
    NEW.detail_id := new_id;

    RETURN NEW;
END;
$$ LANGUAGE plpgsql;

CREATE TRIGGER generate_detail_id
BEFORE INSERT ON sale_details
FOR EACH ROW
EXECUTE FUNCTION generate_detail_id();
------------------------------------------------------------ generate id order
CREATE OR REPLACE FUNCTION generate_order_id() RETURNS TRIGGER AS $$
DECLARE
    last_id INT;
    new_id VARCHAR(10);
BEGIN
    -- Mendapatkan nilai terbesar dari ID yang ada, mengabaikan prefix 'OR'
    SELECT COALESCE(MAX(CAST(SUBSTRING(order_id FROM 3) AS INTEGER)), 0)
    INTO last_id
    FROM orders;

    -- Menambahkan 1 pada nilai terbesar untuk ID baru
    new_id := CONCAT('OR', LPAD((last_id + 1)::TEXT, 3, '0'));

    -- Mengatur ID baru pada baris yang akan dimasukkan
    NEW.order_id := new_id;

    RETURN NEW;
END;
$$ LANGUAGE plpgsql;

CREATE TRIGGER generate_order_id
    BEFORE INSERT ON orders
    FOR EACH ROW
    EXECUTE FUNCTION generate_order_id();


------------------------------------------------------------ update total harga  
CREATE OR REPLACE FUNCTION update_total_price() RETURNS TRIGGER AS $$
DECLARE
    local_order_id CHAR(5);  -- Menggunakan nama variabel yang berbeda untuk menghindari ambiguitas
    local_shipping_cost DECIMAL(10, 2);
BEGIN
    -- Ambil order_id dari tabel sales berdasarkan sale_id
    SELECT s.order_id INTO local_order_id FROM sales s WHERE s.sale_id = NEW.sale_id;

    -- Ambil shipping_cost dari tabel orders berdasarkan local_order_id
    SELECT COALESCE(o.shipping_cost, 0) INTO local_shipping_cost
    FROM orders o
    WHERE o.order_id = local_order_id;

    -- Update total_price di tabel sales
    UPDATE sales
    SET total_price = (
        SELECT COALESCE(SUM(sd.quantity * sd.price), 0) 
        FROM sale_details sd 
        WHERE sd.sale_id = NEW.sale_id
    ) + local_shipping_cost
    WHERE sale_id = NEW.sale_id;

    RETURN NEW;
END;
$$ LANGUAGE plpgsql;



CREATE TRIGGER update_total_price
AFTER INSERT ON sale_details
FOR EACH ROW
EXECUTE FUNCTION update_total_price();

-- Hapus Trigger
DROP TRIGGER IF EXISTS update_total_price ON sale_details;

-- Hapus Fungsi
DROP FUNCTION IF EXISTS update_total_price;



------------------------------------------------------------minus stok 
CREATE OR REPLACE FUNCTION minus_stock() RETURNS TRIGGER AS $$
BEGIN
    UPDATE products
    SET stock = stock - NEW.quantity
    WHERE product_id = NEW.product_id;

    RETURN NEW;
END;
$$ LANGUAGE plpgsql;

CREATE TRIGGER minus_stock
AFTER INSERT ON sale_details
FOR EACH ROW
EXECUTE FUNCTION minus_stock();

------------------------------------------------------------insert revenue 
CREATE OR REPLACE FUNCTION insert_revenue() RETURNS TRIGGER AS $$
BEGIN
    INSERT INTO profit_sharing (sale_id, product_id, supplier_id, product_quantity, total_purchase_price, total_sale_price,status)
    SELECT
        NEW.sale_id,
        NEW.product_id,
        pr.supplier_id,
        NEW.quantity,
        (NEW.quantity * pr.purchase_price) AS total_purchase_price,
        (NEW.quantity * NEW.price) AS total_sale_price,
        'PENDING'
    FROM
        products pr
    WHERE
        pr.product_id = NEW.product_id
    LIMIT 1;

    RETURN NEW;
END;
$$ LANGUAGE plpgsql;

CREATE TRIGGER insert_revenue
AFTER INSERT ON sale_details
FOR EACH ROW
EXECUTE FUNCTION insert_revenue();
-------------------------------------------------------
CREATE OR REPLACE FUNCTION after_order_insert()
RETURNS TRIGGER AS $$
BEGIN
    INSERT INTO notification (notif_id, description, category, created_at)
    VALUES (
        DEFAULT,  -- Asumsi notif_id dikelola secara otomatis
        CONCAT('Pesanan baru dengan ID Pesanan: ', NEW.order_id),
        'Pesanan Baru',
        NOW()
    );
    RETURN NEW;
END;
$$ LANGUAGE plpgsql;

CREATE TRIGGER after_order_insert
AFTER INSERT ON orders
FOR EACH ROW
EXECUTE FUNCTION after_order_insert();
--------------------------------------------------------

DROP TRIGGER IF EXISTS insert_revenue ON sale_details;


delete from users where user_id = 'US002';
delete from users where user_id = 'US003';
delete from users where user_id = 'US004';

delete from users where user_id = 'US005';

delete from users where user_id = 'US007';
delete from users where user_id = 'US008';

DELETE FROM addresses WHERE user_id = 'US005';
DELETE FROM addresses WHERE user_id = 'US007';
DELETE FROM addresses WHERE user_id = 'US008';
delete from addresses where address_id = 'AD005';
delete from addresses where address_id = 'AD007';
delete from addresses where address_id = 'AD008';



delete from orders where order_id = 'OR001'

INSERT INTO orders (order_date, customer_id, delivery_address, status, created_by, created_at)
VALUES (CURRENT_TIMESTAMP, 'US003', 'Jl. Budiasih Kecamatan Sindangkasih Kabupaten Ciamis 46268', 'PENDING', 'US003', CURRENT_TIMESTAMP);


delete from orders where order_id = 'w'


SELECT s.*, sd.* FROM sales s LEFT JOIN sale_details sd ON s.sale_id = sd.sale_id ORDER BY CAST(SUBSTRING(s.sale_id FROM '([0-9]+)') AS INTEGER) desc

SELECT o.*,s.*, sd.* FROM orders o JOIN sales s ON o.order_id = s.order_id LEFT JOIN sale_details sd ON s.sale_id = sd.sale_id WHERE o.status = 'PENDING';

UPDATE products
SET deleted_at = NULL
WHERE product_id = 'P001';

UPDATE products
SET deleted_by = NULL
WHERE product_id = 'P001';


insert INTO addresses (
    user_id ,
    street ,
    rt ,
    rw ,
    village ,
    district ,
    city ,
    postal_code 
    
)
values (
'US012',
'Jl Gempol Kulon No. 101',
'033',
'006',
'Citarum',
'Bandung Wetan',
'Bandung',
'40115'
);

insert INTO addresses (
    user_id ,
    street ,
    rt ,
    rw ,
    village ,
    district ,
    city ,
    postal_code 
    
)
values (
'US001',
'Jl Budiasih',
'033',
'006',
'Desa Budiasih',
'Kecamatan Sindangkasih',
'Kabupaten Ciamis',
'46268'
);

SELECT COUNT(*) FROM users WHERE deleted_by IS NULL OR deleted_at IS null;

SELECT SUM(total_price) AS totalPenjualan FROM sales

2024-09-03 00:00:00.0



SELECT SUM(total_price) AS jumlah 
FROM sales 
WHERE sale_date BETWEEN '2024-09-03 00:00:00.0' AND '2024-09-03 23:59:59.999999999' 
AND payment_method = 'CASH';


update orders set phone = '085737492573' where order_id = 'OR023';
update orders set phone = '085793822842' where order_id = 'OR024';
update orders set phone = '085774625836' where order_id = 'OR025';

SELECT * FROM products  WHERE deleted_at IS NULL AND deleted_by IS NULL;



update products set description = 'Swempes dengan isian Pisang' where product_id = 'PR019';


UPDATE products
SET product_image = 'D:\College\PUB\REACT JS\NostStudio\frontend\src\assets\swempes.jpg'
WHERE product_id = 'PR002';

UPDATE products
SET product_image = '/assets/swempes.jpg'
WHERE product_id = 'PR003';

UPDATE products
SET product_image = '/assets/swempes.jpg'
WHERE product_id = 'PR004';

SELECT * FROM sales WHERE sale_date >= ?


SELECT s.*, sd.* FROM sales s LEFT JOIN sale_details sd ON s.sale_id = sd.sale_id 
                            WHERE s.payment_method = 'shopeepay' AND s.sale_date BETWEEN '2024-08-04 00:00:00' AND '2024-08-05 00:00:00' 
                            ORDER BY CAST(SUBSTRING(s.sale_id FROM '([0-9]+)') AS INTEGER) desc;
                           
 select sale_date from sales;

SELECT s.*, sd.*
FROM sales s
LEFT JOIN sale_details sd ON s.sale_id = sd.sale_id
WHERE s.payment_method = 'shopeepay'
  AND s.sale_date BETWEEN '2024-08-04 00:00:00.000' AND '2024-08-05 00:00:00.000'
ORDER BY CAST(REGEXP_REPLACE(s.sale_id, '\D', '', 'g') AS INTEGER) DESC;

SELECT s.*, sd.*
FROM sales s
LEFT JOIN sale_details sd ON s.sale_id = sd.sale_id
WHERE s.payment_method = 'shopeepay'
  AND s.sale_date >= '2024-08-04 00:00:00' 
  AND s.sale_date < '2024-08-06 00:00:00'
ORDER BY CAST(REGEXP_REPLACE(s.sale_id, '\D', '', 'g') AS INTEGER) DESC;


SELECT sale_id, sale_date
FROM sales
WHERE sale_date BETWEEN '2024-08-04 00:00:00' AND '2024-08-05 23:59:59';


SELECT s.*, sd.*
FROM sales s
LEFT JOIN sale_details sd ON s.sale_id = sd.sale_id
WHERE s.payment_method = 'NULL'
  AND s.sale_date BETWEEN '2024-08-04 00:00:00' AND '2024-08-05 23:59:59'
ORDER BY CAST(REGEXP_REPLACE(s.sale_id, '\D', '', 'g') AS INTEGER) DESC;


SELECT column_name, data_type
FROM information_schema.columns
WHERE table_name = 'products' AND column_name = 'unit';


SELECT column_name, data_type
FROM information_schema.columns
WHERE table_name = 'sales' AND column_name = 'sale_date';


SELECT s.*, sd.*
FROM sales s
LEFT JOIN sale_details sd ON s.sale_id = sd.sale_id
WHERE s.payment_method = 'shopeepay'
  AND s.sale_date BETWEEN '2024-08-04 00:00:00' AND '2024-08-05 23:59:59'
ORDER BY CAST(REGEXP_REPLACE(s.sale_id, '\D', '', 'g') AS INTEGER) DESC;

                           

UPDATE products
SET product_image = '/assets/swempes.jpg'
WHERE product_id = 'P005';

UPDATE products
SET product_image = 'D:\College\PUB\REACT JS\NostStudio\frontend\src\assets\swempes.jpg'
WHERE product_id = 'P006';

UPDATE products
SET product_image = 'D:\College\PUB\REACT JS\NostStudio\frontend\src\assets\swempes.jpg'
WHERE product_id = 'P007';

SELECT 
    o.order_id, 
    o.order_date, 
    o.customer_id, 
    o.delivery_address, 
    o.status, 
    o.created_by, 
    o.updated_by, 
    o.created_at, 
    o.updated_at, 
    o.shipping_cost, 
    o.tracking_number, 
    o.courier, 
    o.shipping_method, 
    o.estimated_delivery_date, 
    o.actual_delivery_date,
    sd.detail_id, 
    sd.product_id, 
    sd.product_name, 
    sd.quantity, 
    sd.price
FROM 
    orders o
LEFT JOIN 
    sales s ON o.order_id = s.order_id
LEFT JOIN 
    sale_details sd ON s.sale_id = sd.sale_id
WHERE 
    o.order_id = 'OR025';

   
   SELECT * FROM products  WHERE product_name LIKE '%' || 'Batang' || '%'  AND  deleted_at IS NULL OR deleted_by IS NULL

   SELECT * FROM products 
WHERE product_name LIKE '%' || 'rang' || '%' 
AND (deleted_at IS NULL OR deleted_by IS NULL)


SELECT * FROM products 
WHERE ( product_name LIKE '%' || 'Rang' || '%')
AND (deleted_at IS NULL OR deleted_by IS NULL)


SELECT s.*, sd.* FROM sales s LEFT JOIN sale_details sd ON s.sale_id = sd.sale_id WHERE s.sale_date BETWEEN '2024-09-03 00:00:00.0' AND '2024-09-04 00:00:00.0'
