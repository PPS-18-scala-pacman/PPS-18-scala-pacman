CREATE TABLE lobby (
    lobby_id BIGSERIAL PRIMARY KEY,
    description varchar(255),
    size smallint
);

CREATE TABLE participant (
    username varchar(255) PRIMARY KEY,
    host boolean,
    pacman_type smallint,
    lobby_id bigint,
    CONSTRAINT fk_lobby
        FOREIGN KEY(lobby_id)
    	  REFERENCES lobby(lobby_id)
);

INSERT INTO lobby (description)
VALUES('Lobby A');
INSERT INTO lobby (description)
VALUES('Lobby B');
INSERT INTO lobby (description)
VALUES('Lobby C');

INSERT INTO participant (username, host, pacman_type, lobby_id)
VALUES('userA1', true, 0, 1);
INSERT INTO participant (username, host, pacman_type, lobby_id)
VALUES('userA2', false, 1, 1);
INSERT INTO participant (username, host, pacman_type, lobby_id)
VALUES('userA3', false, 2, 1);
INSERT INTO participant (username, host, pacman_type, lobby_id)
VALUES('userB1', true, 0, 2);
INSERT INTO participant (username, host, pacman_type, lobby_id)
VALUES('userC1', true, 0, 3);
