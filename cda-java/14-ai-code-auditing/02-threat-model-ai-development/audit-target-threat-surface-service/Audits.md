| Endpoint | Credentials required? | Main risk |
|---|---|---|
| `GET /` | No | Public health information |
| `POST /login` | No | SQL injection; exposes hardcoded token |
| `GET /notes` | No | Unauthorized access to other users' notes |
| `POST /notes` | No | Unauthorized note creation for arbitrary users |
| `GET /admin/users` | No | Exposes usernames, passwords, and emails |
| `POST /tools/backup` | No | Command injection and filesystem path traversal |

| Attack path | Security boundary crossed | Potential impact |
|---|---|---|
| `/login` → SQL query | HTTP input → database | Authentication bypass |
| `/admin/users` → user records | Public → administrative data | Credential exposure |
| `/notes` → database | Public → private user data | Data disclosure |
| `/notes` POST → database | Public → another user's records | Unauthorized modification |
| `/tools/backup` → `os.system()` | HTTP input → operating-system shell | Server compromise |
| `/tools/backup` → filesystem | HTTP input → filesystem | Unauthorized file writes |
| Global CORS | External browser origin → API | Cross-origin abuse |
| Debug configuration | External network → debugging interface | Information disclosure or possible code execution |