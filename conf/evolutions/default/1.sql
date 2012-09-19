# --- First database schema
 
# --- !Ups
 
CREATE TABLE philcollins (
  id                        SERIAL PRIMARY KEY,
  drumsolo                  VARCHAR(255) NOT NULL,
  album                     VARCHAR(255) NOT NULL
);
