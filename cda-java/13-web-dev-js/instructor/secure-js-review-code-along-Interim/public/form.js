// form.js: validate the "Post an event" form at the trust boundary.
// PART 2 (Lessons 6–7): validate and normalize everything before previewing
// or sending it.

import { safeUrl } from "./render.js";
import { CATEGORIES } from "./safe-json.js";

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
  const errors = [];

  const title = String(raw.title ?? "").trim();
  const category = String(raw.category ?? "");
  const date = String(raw.date ?? "");
  const capacityText = String(raw.capacity ?? "").trim();
  const link = String(raw.link ?? "").trim();
  const description = String(raw.description ?? "");

  // Validate title.
  if (title.length < 3 || title.length > 80) {
    errors.push("Title must be 3–80 characters.");
  }

  // Validate category against the shared allow-list.
  if (!CATEGORIES.includes(category)) {
    errors.push("Choose a category from the list.");
  }

  // Validate date.
  const when = new Date(date);

  if (!date || Number.isNaN(when.getTime())) {
    errors.push("Enter a valid date and time.");
  }

  // Validate capacity.
  const capacity = Number(capacityText);

  if (
    !/^\d+$/.test(capacityText) ||
    !Number.isInteger(capacity) ||
    capacity < 1 ||
    capacity > 500
  ) {
    errors.push("Capacity must be a whole number from 1 to 500.");
  }

  // Validate URL.
  if (!safeUrl(link)) {
    errors.push("Link must be a full http:// or https:// address.");
  }

  // Validate description length.
  if (description.length > 2000) {
    errors.push("Description must be 2000 characters or fewer.");
  }

  // Fail closed if any validation failed.
  if (errors.length > 0) {
    return { ok: false, errors };
  }

  return {
    ok: true,
    value: {
      title,
      category,
      date: when.toISOString(),
      capacity,
      link,
      description,
    },
  };
}