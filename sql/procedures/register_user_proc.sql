DROP PROCEDURE IF EXISTS register_user_proc;

DELIMITER $$

CREATE PROCEDURE register_user_proc(
    IN p_email VARCHAR(100),
    IN p_password_hash VARCHAR(256),
    IN p_role ENUM('BUYER', 'SELLER')
)
BEGIN
    DECLARE user_count INT;
    DECLARE new_user_id INT;

    -- Check if email exists
    SELECT COUNT(*) INTO user_count FROM users WHERE email = p_email;

    IF user_count > 0 THEN
        SIGNAL SQLSTATE '45000'
            SET MESSAGE_TEXT = 'Email already registered';
    ELSE
        -- Insert user
        INSERT INTO users (email, password_hash, role, is_verified)
        VALUES (p_email, p_password_hash, p_role,
                CASE WHEN p_role = 'SELLER' THEN TRUE ELSE FALSE END);

        SET new_user_id = LAST_INSERT_ID();

        -- For buyers, verification will be done via OTP later
        SELECT new_user_id AS user_id;
    END IF;
END $$

DELIMITER ;