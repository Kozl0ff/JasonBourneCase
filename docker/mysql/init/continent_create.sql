CREATE TABLE continent (
    id INT NOT NULL AUTO_INCREMENT PRIMARY KEY,
    continent_name VARCHAR(255),
    count BIGINT(20),
    last_modified_time TIMESTAMP NOT NULL
);
