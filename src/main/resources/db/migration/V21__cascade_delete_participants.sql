ALTER TABLE participants
    DROP CONSTRAINT participants_match_id_fkey,
    ADD  CONSTRAINT participants_match_id_fkey
        FOREIGN KEY (match_id) REFERENCES matches (id) ON DELETE CASCADE;