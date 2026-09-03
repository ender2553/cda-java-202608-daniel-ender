-- Literal expression
SELECT 'SQL Fundamentals' AS course_name;

-- Arithmetic expression
SELECT 25 + 17 AS result;

SELECT 7 / 2 AS per_group;

SELECT 7.0 / 2 AS per_group_decimal;

SELECT 3 * 5 AS total;

SELECT 41 - 17 AS difference;


--SELECT statement - read data from table - limits the columns
SELECT * FROM movie; --DO NOT USE * (star) IN PRODUCTION - NEVER

SELECT title, release_year,	genre FROM movie;

SELECT 
	title AS movie_title, 
	release_year AS year_released,	
	genre
FROM movie;

-- WHERE - limits rows
SELECT * FROM movie WHERE genre = 'Drama';


SELECT * FROM movie WHERE imdb_rating > 8.5;

SELECT * FROM movie WHERE runtime_minutes >= 180;

SELECT * FROM movie WHERE runtime_minutes <= 90;

SELECT * FROM movie WHERE genre = 'Drama' AND release_year > 2000;

SELECT * FROM movie WHERE genre = 'Animation' OR genre = 'Comedy';

SELECT * FROM movie WHERE genre IN ('Crime', 'Thriller', 'Horror');

SELECT * FROM movie WHERE rating_code NOT IN ('R', 'PG-13');

SELECT * FROM movie WHERE title LIKE '%Star%';

SELECT * FROM movie WHERE title ILIKE '%dark%';

SELECT * FROM movie WHERE release_year BETWEEN 1990 AND 1999;

--ORDER BY
SELECT title, imdb_rating FROM movie ORDER BY title ASC;

SELECT title, imdb_rating FROM movie ORDER BY title DESC;

SELECT * FROM movie LIMIT 10;




SELECT 
	title, genre, release_year, imdb_rating
FROM movie
WHERE release_year BETWEEN 1990 AND 2010
  AND genre IN ('Drama', 'Crime', 'Science Fiction')
  AND imdb_rating> 8.4
ORDER BY imdb_rating DESC, release_year DESC LIMIT 10;

--STRING CONCATENATION
SELECT title || ' - ' || release_year AS movie_label FROM movie;

SELECT title, 2026 - release_year AS movie_age FROM movie;


-- DISTINCT - ELIMINATES THE DUPS RETURNED
SELECT DISTINCT director FROM movie WHERE director = 'Christopher Nolan';

-- STRING Functions
SELECT UPPER(title), director FROM movie;
SELECT LOWER(title), director FROM movie;

SELECT TRIM(title), director FROM movie; --remove trailing spaces
SELECT LTRIM(title), director FROM movie; --remove trailing spaces left side only
SELECT RTRIM(title), director FROM movie; --remove trailing spaces right side only

SELECT title, REPLACE(title, 'Dark', 'Gray') FROM movie AS updated_title;






