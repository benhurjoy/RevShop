DROP PROCEDURE IF EXISTS update_inventory_proc;

DELIMITER $$

CREATE PROCEDURE update_inventory_proc(
    IN p_product_id INT,
    IN p_quantity_change INT,
    IN p_seller_id INT
)
BEGIN
    DECLARE current_stock INT;
    DECLARE product_seller_id INT;

    -- Validate input
    IF p_quantity_change = 0 THEN
        SIGNAL SQLSTATE '45000'
            SET MESSAGE_TEXT = 'Quantity change cannot be zero';
    END IF;

    -- Get current stock and verify seller owns product
    SELECT stock, seller_id INTO current_stock, product_seller_id
    FROM products
    WHERE id = p_product_id FOR UPDATE;

    IF product_seller_id != p_seller_id THEN
        SIGNAL SQLSTATE '45000'
            SET MESSAGE_TEXT = 'Unauthorized: You do not own this product';
    END IF;

    IF (current_stock + p_quantity_change) < 0 THEN
        SIGNAL SQLSTATE '45000'
            SET MESSAGE_TEXT = 'Insufficient stock for this operation';
    END IF;

    -- Update stock
    UPDATE products
    SET stock = stock + p_quantity_change
    WHERE id = p_product_id;

    -- Return updated stock
    SELECT stock FROM products WHERE id = p_product_id;
END $$

DELIMITER ;