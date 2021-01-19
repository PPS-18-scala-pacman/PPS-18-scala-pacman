ALTER TABLE lobby
    ADD CONSTRAINT fk_host_username
        FOREIGN KEY(host_username)
        REFERENCES participant(username)
        ON DELETE CASCADE;
