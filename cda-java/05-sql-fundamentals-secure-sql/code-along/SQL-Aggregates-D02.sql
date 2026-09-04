--=======================================================
-- Section 1 - Review the original movie table
--=======================================================
SELECT * 
FROM movie
ORDER BY release_year;



--=======================================================
-- Section 2 - Aggregate functions
--=======================================================

-- COUNT() counts rows
SELECT COUNT(*) FROM movie AS movie_count;

SELECT COUNT(*) AS movies_since_2000
FROM movie
WHERE release_year >= 2000;

SELECT COUNT(title) AS movies_since_2000  --more on this on day 3; multiple tables
FROM movie
WHERE release_year >= 2000;

--MIN() and MAX() find smallest/largest values.
SELECT MIN(release_year) AS earliest_year
FROM movie;

SELECT MAX(release_year) AS newest_year
FROM movie;

SELECT MIN(release_year) AS earliest_year,
	   MAX(release_year) AS newest_year
FROM movie;

SELECT MIN(runtime_minutes) AS shortest_runtime,
	   MAX(runtime_minutes) AS longest_runtime
FROM movie;

SELECT MIN(imdb_rating) AS lowest_rating,
	   MAX(imdb_rating) AS highest_rating
FROM movie;


-- AVG() calculates an average
SELECT AVG(imdb_rating) AS average_rating
FROM movie;

SELECT AVG(runtime_minutes) AS average_runtime
FROM movie;

SELECT ROUND(AVG(imdb_rating), 2) AS average_rating
FROM movie;

SELECT ROUND(AVG(runtime_minutes), 2) AS average_runtime
FROM movie;

-- SUM() calculates a total
SELECT SUM(runtime_minutes) AS total_runtime_minutes
FROM movie;

SELECT SUM(runtime_minutes) AS total_runtime_minutes
FROM movie
WHERE release_year >= 2000;

SELECT ROUND(SUM(runtime_minutes) / 60.0, 2) AS total_runtime_hours
FROM movie;

-- Multiple aggregates can be returned together.
SELECT COUNT(*) AS movie_count,
	   MIN(imdb_rating) AS lowest_rating,
	   MAX(imdb_rating) AS highest_rating,
	   ROUND(AVG(imdb_rating), 2) AS average_rating
FROM movie;

-- WHERE filters rows before the aggregate runs.
SELECT COUNT(*) AS drama_count,
	   ROUND(AVG(imdb_rating), 2) AS drama_average_rating
FROM movie
WHERE genre = 'Drama';



--=======================================================
-- Section 3 - GROUP BY and HAVING
--=======================================================

-- GROUP BY creates groups of rows before aggregates are calculated

SELECT genre,
	   COUNT(*) AS movie_count
FROM movie
GROUP BY genre
ORDER BY movie_count DESC;

SELECT genre,
	   ROUND(AVG(runtime_minutes), 2) AS average_runtime
FROM movie
GROUP BY genre
ORDER BY average_runtime DESC;

SELECT genre,
       director,
	   ROUND(AVG(runtime_minutes), 2) AS average_runtime
FROM movie
GROUP BY genre, director
ORDER BY genre ASC, average_runtime DESC, director DESC;


-- HAVING filters groups after GROUP BY.
SELECT director,
	   COUNT(*) AS movie_count
FROM movie
GROUP BY director
HAVING COUNT(*) > 1
ORDER BY movie_count DESC, director;


-- WHERE -> GROUP BY -> -> HAVING -> SELECT -> ORDER BY
SELECT genre,
	   COUNT(*) AS movie_count,
	   ROUND(AVG(imdb_rating), 2) AS average_rating
FROM movie
WHERE release_year >= 1990
GROUP BY genre
HAVING COUNT(*) >= 3
ORDER BY average_rating desc;


--=======================================================
-- Section 4 - SUBQUERIES WITH IN
--=======================================================



SELECT title,
	   genre,
	   imdb_rating	   
FROM movie
WHERE genre IN (
				SELECT genre 
				FROM movie
				GROUP BY genre
				HAVING COUNT(*) >= 10
				
				)
ORDER BY genre, imdb_rating DESC;
