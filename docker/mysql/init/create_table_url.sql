CREATE TABLE urlcount (
     id BIGINT NOT NULL AUTO_INCREMENT PRIMARY KEY,
     referrer_host TEXT,
     count INT,
     dtime DATETIME
 );