CREATE TABLE city (
    id INT NOT NULL AUTO_INCREMENT PRIMARY KEY,
    city_name VARCHAR(255),
    count BIGINT(20),
    last_modified_time TIMESTAMP NOT NULL
);
