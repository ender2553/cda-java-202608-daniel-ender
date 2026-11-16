"""
notes_service.py — note access layer (BEFORE the AI-generated change).

Provides read access to notes with an ownership check so a user can only
read their own notes.
"""

import sqlite3

DB_PATH = "notes.db"


def _db():
    conn = sqlite3.connect(DB_PATH)
    conn.row_factory = sqlite3.Row
    return conn


def get_note(note_id, requester_id):
    """Return a single note if it belongs to the requesting user, else None."""
    conn = _db()
    row = conn.execute(
        "SELECT id, owner_id, title, body FROM notes WHERE id = ?",
        (note_id,),
    ).fetchone()
    if row is None:
        return None
    if row["owner_id"] != requester_id:
        # Not your note — do not disclose it.
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
