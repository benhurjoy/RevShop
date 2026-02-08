DROP PROCEDURE IF EXISTS place_order_proc;

DELIMITER $$

CREATE PROCEDURE place_order_proc(
    IN p_user_id INT,
    IN p_payment_method VARCHAR(20),
    OUT p_order_id INT
)
BEGIN
    DECLARE v_total DECIMAL(10,2) DEFAULT 0;
    DECLARE v_product_id INT;
    DECLARE v_quantity INT;
    DECLARE v_price DECIMAL(10,2);
    DECLARE v_stock INT;
    DECLARE done INT DEFAULT FALSE;

    -- Cursor for cart items
    DECLARE cart_cursor CURSOR FOR
        SELECT c.product_id, c.quantity, p.discount_price
        FROM cart c
                 JOIN products p ON c.product_id = p.id
        WHERE c.user_id = p_user_id;

    DECLARE CONTINUE HANDLER FOR NOT FOUND SET done = TRUE;

    -- Start transaction
    START TRANSACTION;

    -- Lock all products in cart at once to prevent deadlocks
    SELECT id FROM products
    WHERE id IN (
        SELECT product_id FROM cart WHERE user_id = p_user_id
    ) FOR UPDATE;

    -- Calculate total and check stock
    OPEN cart_cursor;

    calc_loop: LOOP
        FETCH cart_cursor INTO v_product_id, v_quantity, v_price;

        IF done THEN
            LEAVE calc_loop;
        END IF;

        -- Check stock (FOR UPDATE removed since we locked above)
        SELECT stock INTO v_stock FROM products WHERE id = v_product_id;

        IF v_stock < v_quantity THEN
            CLOSE cart_cursor;
            ROLLBACK;
            SIGNAL SQLSTATE '45000'
                SET MESSAGE_TEXT = CONCAT('Insufficient stock for product ID: ', v_product_id);
        END IF;

        SET v_total = v_total + (v_price * v_quantity);
    END LOOP;

    CLOSE cart_cursor;

    -- Create order
    INSERT INTO orders (user_id, total_amount, status)
    VALUES (p_user_id, v_total, 'PENDING');

    SET p_order_id = LAST_INSERT_ID();

    -- Insert order items and update stock
    INSERT INTO order_items (order_id, product_id, quantity, price)
    SELECT p_order_id, c.product_id, c.quantity, p.discount_price
    FROM cart c
             JOIN products p ON c.product_id = p.id
    WHERE c.user_id = p_user_id;

    -- Update stock
    UPDATE products p
        JOIN cart c ON p.id = c.product_id
    SET p.stock = p.stock - c.quantity
    WHERE c.user_id = p_user_id;

    -- Clear cart
    DELETE FROM cart WHERE user_id = p_user_id;

    -- Create payment record
    INSERT INTO payments (order_id, method, status)
    VALUES (p_order_id, p_payment_method, 'COMPLETED');

    -- Commit transaction
    COMMIT;
END $$

DELIMITER ;