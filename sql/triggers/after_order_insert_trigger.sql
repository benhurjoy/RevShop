DELIMITER $$

CREATE TRIGGER after_order_insert_trigger
    AFTER INSERT ON orders
    FOR EACH ROW
BEGIN
    -- Insert notification for buyer
    INSERT INTO notifications (user_id, message)
    VALUES (NEW.user_id,
            CONCAT('Your order #', NEW.id, ' has been placed successfully. Total: ₹', NEW.total_amount));

    -- Insert notification for seller(s)
    INSERT INTO notifications (user_id, message)
    SELECT DISTINCT p.seller_id,
                    CONCAT('New order #', NEW.id, ' received for product: ', p.name)
    FROM order_items oi
             JOIN products p ON oi.product_id = p.id
    WHERE oi.order_id = NEW.id;
END $$

DELIMITER ;