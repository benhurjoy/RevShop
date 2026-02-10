DELIMITER $$

CREATE PROCEDURE register_user_proc(
    IN p_email VARCHAR(255),
    IN p_password_hash VARCHAR(255),
    IN p_role VARCHAR(20)
)
BEGIN
    DECLARE user_is_verified BOOLEAN;

    -- Sellers are auto-verified, buyers need email verification
    IF p_role = 'SELLER' THEN
        SET user_is_verified = TRUE;
    ELSE
        SET user_is_verified = FALSE;
    END IF;

    INSERT INTO users (email, password_hash, role, is_verified, created_at)
    VALUES (p_email, p_password_hash, p_role, user_is_verified, NOW());

    SELECT LAST_INSERT_ID() as user_id;
END$$

DELIMITER ;