CREATE INDEX idx_matches_patch_version
    ON matches (patch_major DESC, patch_minor DESC);