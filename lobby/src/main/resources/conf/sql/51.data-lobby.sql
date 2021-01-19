-- start a transaction
--BEGIN;

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

-- commit the change (or roll it back later)
--COMMIT;
