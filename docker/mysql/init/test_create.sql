CREATE TABLE test(id INT NOT NULL, value INT NOT NULL, data DATE NOT NULL);
CREATE USER 'grafanaReader' IDENTIFIED BY 'password';
GRANT SELECT ON bdata.* TO 'grafanaReader';
