DROP DATABASE IF EXISTS bills;
CREATE DATABASE bills;
USE bills;
CREATE TABLE bill (
    name VARCHAR(75) NOT NULL, 
    status BIT NOT NULL,
    PRIMARY KEY(name)
);
CREATE TABLE entry (
	id INT NOT NULL AUTO_INCREMENT,
    name VARCHAR(75) NOT NULL,
    date DATE NOT NULL,
    amount NUMERIC(7,2) NOT NULL,
    status INT NOT NULL,
    services VARCHAR(300),
    PRIMARY KEY(id),
    FOREIGN KEY(name) 
		REFERENCES bill(name) 
        ON UPDATE CASCADE
        ON DELETE CASCADE
);
CREATE TABLE payment (
	id INT NOT NULL AUTO_INCREMENT,
    entryID INT NOT NULL,
    date DATE NOT NULL,
    amount NUMERIC(7,2) NOT NULL,
    type VARCHAR(50) NOT NULL,
    medium VARCHAR(50) NOT NULL,
    notes VARCHAR(300),
    PRIMARY KEY(id),
    FOREIGN KEY(entryID) 
		REFERENCES entry(id)
        ON DELETE CASCADE
);