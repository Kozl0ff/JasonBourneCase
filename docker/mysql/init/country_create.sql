CREATE TABLE country (
    id INT NOT NULL AUTO_INCREMENT PRIMARY KEY,
    country_iso_code VARCHAR(2),
    count BIGINT(20),
    last_modified_time TIMESTAMP NOT NULL
);
