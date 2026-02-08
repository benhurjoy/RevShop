DELIMITER $$

CREATE TRIGGER low_stock_trigger
    AFTER UPDATE ON products
    FOR EACH ROW
BEGIN
    DECLARE threshold INT;
    SET threshold = 10; -- Configurable threshold

    IF NEW.stock <= threshold AND OLD.stock > threshold THEN
        INSERT INTO notifications (user_id, message)
        VALUES (NEW.seller_id,
                CONCAT('Low stock alert: Product "', NEW.name, '" has only ', NEW.stock, ' units left.'));
    END IF;
END $$

DELIMITER ;