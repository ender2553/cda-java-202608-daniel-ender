SELECT movie_id, title, release_year, runtime_minutes, imdb_rating, director_id, genre_id, rating_id, language_id, country_id
	FROM movie;


SELECT * FROM movie_original;

SELECT * FROM movie_actor;

SELECT * FROM movie;

SELECT * FROM director;


-- 1. INNER JOIN: returns rows that match on both sides.
SELECT title, release_year, director_name
FROM movie 
INNER JOIN director ON movie.director_id = director.director_id
ORDER BY movie.title;

-- Prefer this syntax
SELECT 
	m.title, 
	m.release_year, 
	d.director_name
FROM movie AS m 
INNER JOIN director AS d ON m.director_id = d.director_id
ORDER BY m.title;

--OR - JOIN is shorthand for INNER JOIN.  INNER is optional

SELECT 
	m.title, 
	m.release_year, 
	d.director_name
FROM movie AS m 
JOIN director AS d ON m.director_id = d.director_id
ORDER BY m.title;

-- 2. INNER JOIN across several related tables.
SELECT 
	m.title, 
	m.release_year, 
	d.director_name,
	g.genre_name,
	r.rating_code
FROM movie AS m 
JOIN director AS d ON m.director_id = d.director_id
JOIN genre AS g ON m.genre_id = g.genre_id
JOIN rating AS r ON m.rating_id = r.rating_id
ORDER BY m.title;


-- 3. Many to many JOIN through movie_actor

SELECT
	m.title,
	a.actor_name
FROM movie AS m
JOIN movie_actor AS ma ON m.movie_id = ma.movie_id
JOIN actor AS a ON ma.actor_id = a.actor_id
ORDER BY m.title, a.actor_name;



SELECT
	m.title,
	a.actor_name,
	ma.character_name
FROM movie AS m 
JOIN movie_actor AS ma ON m.movie_id = ma.movie_id 
JOIN actor AS a ON ma.actor_id = a.actor_id
ORDER BY m.title, a.actor_name;


-- 4. LEFT OUTER JOIN keeps every LEFT-side row
--    Use when the left entity must remain visible even without a match.

SELECT 
	m.title, 
	m.release_year, 
	d.director_name
FROM movie AS m 
LEFT OUTER JOIN director AS d ON m.director_id = d.director_id
ORDER BY m.title;

-- OUTER is optional 

SELECT 
	m.title, 
	m.release_year, 
	d.director_name
FROM movie AS m 
LEFT JOIN director AS d ON m.director_id = d.director_id
ORDER BY m.title;



--=========================================================
--  MUSIC TABLES, Movie Tables
--=========================================================
-- 1. INNER JOIN: chart entries + songs

SELECT 
	ce.current_rank, 
	s.title, 
	ce.weeks_on_chart
FROM chart_entry AS ce
JOIN song AS s ON ce.song_id = s.song_id
ORDER BY ce.current_rank;


-- 2. LEFT JOIN: Keep all songs, even songs with no chart entry.

SELECT 
	s.title,
	ce.current_rank,
	ce.chart_date
FROM song AS s
LEFT JOIN chart_entry AS ce ON s.song_id = ce.song_id
ORDER BY ce.current_rank, s.title;

-- AS is optional 
SELECT 
	s.title,
	ce.current_rank,
	ce.chart_date
FROM song s
LEFT JOIN chart_entry ce ON s.song_id = ce.song_id
ORDER BY ce.current_rank, s.title;


-- Songs that have multiple credited artists
--SELECT * FROM song_artist ORDER BY song_id, artist_id;  --discovery

SELECT 
	s.title,
	COUNT(sa.artist_id) AS artist_count
FROM song s
JOIN song_artist sa ON s.song_id = sa.song_id
GROUP BY s.title
HAVING COUNT(sa.artist_id) > 1
ORDER BY artist_count DESC, s.title;


-- Working with NULLs
SELECT * FROM song WHERE release_date IS NOT NULL;
SELECT * FROM song WHERE release_date IS NULL;


SELECT * FROM chart_entry;

-- COALESCE
SELECT 
	s.title, 
	ce.current_rank,
    COALESCE(ce.previous_rank::TEXT, 'NEW / RE-ENTRY') AS previous_rank_display
FROM chart_entry AS ce
JOIN song AS s ON ce.song_id = s.song_id
ORDER BY ce.current_rank;


-- Date Time
SELECT NOW();

SELECT CURRENT_TIMESTAMP;

SELECT CURRENT_DATE;


-- UNION and UNION ALL ==============================
--===================================================
SELECT artist_name name
FROM artist
UNION
SELECT director_name name
FROM director
ORDER BY name;


SELECT title, 'MOVIE' AS media_type
FROM movie
UNION ALL
SELECT title, 'SONG' AS media_type
FROM song
ORDER BY media_type, title


SELECT title FROM movie
UNION 
SELECT title FROM song
ORDER BY title


-- INTERSECT: values present in BOTH result sets.
SELECT actor_name name FROM actor
INTERSECT 
SELECT artist_name name FROM artist


-- EXCEPT: values in first result but not in the second.
SELECT actor_name name FROM actor
EXCEPT 
SELECT artist_name name FROM artist
