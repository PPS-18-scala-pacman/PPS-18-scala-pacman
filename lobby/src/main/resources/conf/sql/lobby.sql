CREATE TABLE lobby (
    lobby_id BIGSERIAL PRIMARY KEY,
    description varchar(255),
    lobby_size smallint NOT NULL CHECK(lobby_size > 0)
);

CREATE TABLE participant (
    username varchar(255) PRIMARY KEY,
    host boolean NOT NULL,
    pacman_type smallint NOT NULL,
    lobby_id bigint NOT NULL,
    CONSTRAINT fk_lobby
        FOREIGN KEY(lobby_id)
    	  REFERENCES lobby(lobby_id)
);

INSERT INTO lobby (description, lobby_size)
VALUES('Lobby A', 4);
INSERT INTO lobby (description, lobby_size)
VALUES('Lobby B', 3);
INSERT INTO lobby (description, lobby_size)
VALUES('Lobby C', 2);

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
