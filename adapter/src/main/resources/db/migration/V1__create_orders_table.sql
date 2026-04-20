-- TODO: Delete this migration and replace with the actual schema when cloning for a new service

DROP TABLE IF EXISTS order_items;
DROP TABLE IF EXISTS orders;

CREATE TABLE orders (
    id            UUID         NOT NULL PRIMARY KEY,
    customer_name VARCHAR(255) NOT NULL,
    status        INT          NOT NULL,
    created_at    TIMESTAMP(6) NOT NULL DEFAULT CURRENT_TIMESTAMP(6),
    deleted_at    TIMESTAMP(6) NULL
);

CREATE TABLE order_items (
    id           UUID           NOT NULL PRIMARY KEY,
    order_id     UUID           NOT NULL,
    product_name VARCHAR(255)   NOT NULL,
    quantity     INT            NOT NULL,
    unit_price   DECIMAL(19, 2) NOT NULL,
    status       INT            NOT NULL,
    CONSTRAINT fk_order_items_order FOREIGN KEY (order_id) REFERENCES orders (id)
);

CREATE INDEX idx_orders_status ON orders (status);
CREATE INDEX idx_orders_customer_name ON orders (customer_name);
CREATE INDEX idx_order_items_order_id ON order_items (order_id);
