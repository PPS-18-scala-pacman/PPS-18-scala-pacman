CREATE TABLE lobby (
    id SERIAL PRIMARY KEY,
    description varchar(255),
    size smallint,
);

CREATE TABLE participant (
    id SERIAL PRIMARY KEY,
    description varchar(255)
);

INSERT INTO lobby (description)
VALUES('Lobby 1');
INSERT INTO lobby (description)
VALUES('Lobby 2');
INSERT INTO lobby (description)
VALUES('Lobby 3');
