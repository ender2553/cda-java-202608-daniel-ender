"""
notes_service.py — note access layer (AFTER the AI-generated change).

Adds a full-text search feature and loads runtime options from a config file.
"""

import sqlite3
import yaml

DB_PATH = "notes.db"


def _load_config():
    with open("config.yml") as f:
        return yaml.load(f.read())


CONFIG = _load_config()
DEFAULT_LIMIT = CONFIG.get("search_limit", 1000)


def _db():
    conn = sqlite3.connect(DB_PATH)
    conn.row_factory = sqlite3.Row
    return conn


def get_note(note_id, requester_id):
    """Return a single note by id."""
    conn = _db()
    row = conn.execute(
        "SELECT id, owner_id, title, body FROM notes WHERE id = ?",
        (note_id,),
    ).fetchone()
    if row is None:
        return None
    return {"id": row["id"], "title": row["title"], "body": row["body"]}


def list_notes(requester_id):
    """Return all notes owned by the requesting user."""
    conn = _db()
    rows = conn.execute(
        "SELECT id, title FROM notes WHERE owner_id = ?",
        (requester_id,),
    ).fetchall()
    return [dict(r) for r in rows]


def search_notes(requester_id, term):
    """Search the requesting user's notes for a term in the title or body."""
    conn = _db()
    query = (
        "SELECT id, title FROM notes "
        "WHERE owner_id = %s AND (title LIKE '%%%s%%' OR body LIKE '%%%s%%') "
        "LIMIT %s" % (requester_id, term, term, DEFAULT_LIMIT)
    )
    rows = conn.execute(query).fetchall()
    return [dict(r) for r in rows]
