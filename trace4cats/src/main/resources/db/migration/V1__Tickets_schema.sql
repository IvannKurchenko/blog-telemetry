create table "tickets"
(
    "id"          BIGSERIAL NOT NULL PRIMARY KEY,
    "project"     BIGINT    NOT NULL,
    "title"       VARCHAR   NOT NULL,
    "description" VARCHAR   NOT NULL,
    "created_at"  BIGINT    NOT NULL,
    "created_by"  VARCHAR   NOT NULL,
    "modified_at" BIGINT    NOT NULL,
    "modified_by" VARCHAR   NOT NULL
)