CREATE TABLE lobby (
    lobby_id BIGSERIAL PRIMARY KEY,
    description varchar(255),
    lobby_size smallint NOT NULL CHECK(lobby_size > 0),
    host_username varchar(255) NOT NULL
);

CREATE TABLE participant (
    username varchar(255) PRIMARY KEY,
    pacman_type smallint NOT NULL,
    lobby_id bigint NOT NULL,
    CONSTRAINT fk_lobby
        FOREIGN KEY(lobby_id)
    	  REFERENCES lobby(lobby_id)
        ON DELETE CASCADE
);

INSERT INTO lobby (description, lobby_size, host_username)
VALUES('Lobby A', 4, 'userA1');
INSERT INTO lobby (description, lobby_size, host_username)
VALUES('Lobby B', 3, 'userB1');
INSERT INTO lobby (description, lobby_size, host_username)
VALUES('Lobby C', 2, 'userC1');

INSERT INTO participant (username, pacman_type, lobby_id)
VALUES('userA1', 0, 1);
INSERT INTO participant (username, pacman_type, lobby_id)
VALUES('userA2', 1, 1);
INSERT INTO participant (username, pacman_type, lobby_id)
VALUES('userA3', 2, 1);
INSERT INTO participant (username, pacman_type, lobby_id)
VALUES('userB1', 0, 2);
INSERT INTO participant (username, pacman_type, lobby_id)
VALUES('userC1', 0, 3);

ALTER TABLE lobby
    ADD CONSTRAINT fk_host_username
        FOREIGN KEY(host_username)
        REFERENCES participant(username)
        ON DELETE CASCADE;
