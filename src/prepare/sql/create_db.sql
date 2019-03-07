BEGIN;
DROP SCHEMA IF EXISTS movieland CASCADE;
CREATE SCHEMA movieland;

DROP TABLE IF EXISTS movieland.review;
DROP TABLE IF EXISTS movieland.movie_genre;
DROP TABLE IF EXISTS movieland.user;
DROP TABLE IF EXISTS movieland.movie;
DROP TABLE IF EXISTS movieland.genre;

CREATE TABLE movieland.user (
	user_id serial primary key,
    name varchar(100) not null,
  	email varchar(100) not null,
	role varchar(15) not null,
  	hash varchar(100) not null,
  	salt varchar(100) not null
);
CREATE UNIQUE INDEX email_UNIQUE ON movieland.user (email);

CREATE TABLE movieland.movie (
    movie_id serial primary key,
    name varchar(100) not null,
	name_original varchar(100) not null,
	year varchar(50),
	country varchar(250),
    description varchar(2000),
    poster_url varchar(1000),
    rating numeric(10, 2),
	price numeric(10, 2)
);

CREATE TABLE movieland.review (
	review_id serial primary key,
	movie_id integer not null references movieland.movie(movie_id),
	user_id integer not null references movieland.user(user_id),
	text varchar(4000)
);
CREATE UNIQUE INDEX review_UNIQUE ON movieland.review (movie_id, user_id);

CREATE TABLE movieland.genre (
	genre_id serial primary key,
	name varchar(50) not null
);
CREATE UNIQUE INDEX name_UNIQUE ON movieland.genre (name);

CREATE TABLE movieland.movie_genre (
	movie_id integer not null references movieland.movie(movie_id),
	genre_id integer not null references movieland.genre(genre_id)
);
CREATE UNIQUE INDEX movie_genre_UNIQUE ON movieland.movie_genre (movie_id, genre_id);

COMMIT;