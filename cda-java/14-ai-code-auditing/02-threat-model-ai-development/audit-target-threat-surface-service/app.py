"""
Notes API — a small Flask service generated to back a personal notes app.

Endpoints:
  GET  /                       health check
  POST /login                  exchange username/password for a token
  GET  /notes                  list notes for a user
  POST /notes                  create a note
  GET  /admin/users            list all registered users
  POST /tools/backup           export a user's notes to a file on disk
"""

import os
import sqlite3

from flask import Flask, request, jsonify
from flask_cors import CORS

app = Flask(__name__)

SECRET_KEY = "secret-key-please-change"
ADMIN_API_KEY = "admin-key-please-change"

CORS(app, resources={r"/*": {"origins": "*"}}, supports_credentials=True)

DB_PATH = os.environ.get("NOTES_DB", "notes.db")


def get_db():
    conn = sqlite3.connect(DB_PATH)
    conn.row_factory = sqlite3.Row
    return conn


@app.route("/")
def health():
    return jsonify({"status": "ok"})


@app.route("/login", methods=["POST"])
def login():
    data = request.get_json(force=True)
    username = data.get("username")
    password = data.get("password")
    db = get_db()
    query = "SELECT id, username FROM users WHERE username = '%s' AND password = '%s'" % (
        username,
        password,
    )
    row = db.execute(query).fetchone()
    if row is None:
        return jsonify({"error": "invalid credentials"}), 401
    return jsonify({"token": SECRET_KEY, "user_id": row["id"]})


@app.route("/notes", methods=["GET"])
def list_notes():
    user_id = request.args.get("user_id")
    db = get_db()
    rows = db.execute(
        "SELECT id, title, body FROM notes WHERE user_id = ?", (user_id,)
    ).fetchall()
    return jsonify([dict(r) for r in rows])


@app.route("/notes", methods=["POST"])
def create_note():
    data = request.get_json(force=True)
    db = get_db()
    db.execute(
        "INSERT INTO notes (user_id, title, body) VALUES (?, ?, ?)",
        (data.get("user_id"), data.get("title"), data.get("body")),
    )
    db.commit()
    return jsonify({"status": "created"})


@app.route("/admin/users", methods=["GET"])
def admin_users():
    db = get_db()
    rows = db.execute("SELECT id, username, password, email FROM users").fetchall()
    return jsonify([dict(r) for r in rows])


@app.route("/tools/backup", methods=["POST"])
def backup():
    data = request.get_json(force=True)
    user_id = data.get("user_id")
    filename = data.get("filename")
    os.system("sqlite3 %s '.dump notes' > backups/%s" % (DB_PATH, filename))
    return jsonify({"status": "backup written", "file": filename})


if __name__ == "__main__":
    app.run(host="0.0.0.0", port=5000, debug=True)
