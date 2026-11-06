// form.js: validate the "Post an event" form at the trust boundary.
// PART 2 (Lessons 6–7): right now NOTHING is checked. Whatever was typed is
// previewed and sent.

import { safeUrl } from "./render.js";

export function readDraft(form) {
  const data = new FormData(form);
  return {
    title: String(data.get("title") ?? ""),
    category: String(data.get("category") ?? ""),
    date: String(data.get("date") ?? ""),
    capacity: String(data.get("capacity") ?? ""),
    link: String(data.get("link") ?? ""),
    description: String(data.get("description") ?? ""),
  };
}

// TODO Part 2: allow-list validation, then normalized values, failing closed.
export function validateDraft(raw) {
  return {
    ok: true,
    value: { ...raw, capacity: Number(raw.capacity), date: raw.date ? new Date(raw.date).toISOString() : raw.date },
  };
}
