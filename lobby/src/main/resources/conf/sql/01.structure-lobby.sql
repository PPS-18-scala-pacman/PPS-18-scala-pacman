CREATE TABLE lobby (
    lobby_id BIGSERIAL PRIMARY KEY,
    description varchar(255),
    lobby_size smallint NOT NULL CHECK(lobby_size > 0),
    host_username varchar(255) NOT NULL
);

CREATE TABLE participant (
    username varchar(255) PRIMARY KEY,
    pacman_type smallint NOT NULL CHECK(pacman_type >= 0) DEFAULT 0,
    lobby_id bigint NOT NULL,
    CONSTRAINT fk_lobby
        FOREIGN KEY(lobby_id)
    	  REFERENCES lobby(lobby_id)
        ON DELETE CASCADE
);
