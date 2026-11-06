// safe-json.js: turning untrusted TEXT into trusted VALUES.
// PART 4 (Lessons 16–19). The pipeline you'll build:
//   parse (try/catch) -> reviver (drop dangerous keys, revive dates)
//   -> safe merge (Object.create(null)) -> schema (shape, types, ranges, no extras)
//
// Until Part 4, the stubs below are pass-throughs, so the app runs. They do no checking.

// TODO Part 4: drop "__proto__", "constructor", "prototype". Turn ISO date strings into Dates.
export function safeReviver(key, value) {
  return value;
}

// TODO Part 4: JSON.parse(text, safeReviver) inside try/catch -> { ok, value } or { ok: false }.
export function parseUntrusted(text) {
  return { ok: true, value: JSON.parse(text) };
}

// TODO Part 4: a JSON.stringify replacer that drops "token", "password", "authorization".
export function withoutSecrets(key, value) {
  return value;
}

export function isPlainObject(v) {
  if (v === null || typeof v !== "object" || Array.isArray(v)) return false;
  const proto = Object.getPrototypeOf(v);
  return proto === Object.prototype || proto === null;
}

// TODO Part 4: merge into Object.create(null) and skip dangerous keys.
export function safeMerge(target, source) {
  return { ...target, ...source };
}

// ---------------------------------------------------------------------------
// Schema checking. checkRule() is provided. It checks ONE value against ONE rule.
//   rule = { type: "string" | "integer" | "date" | "enum" | "object",
//            required, minLength, maxLength, min, max, values, schema }
// ---------------------------------------------------------------------------
function checkRule(rule, v) {
  switch (rule.type) {
    case "string":
      if (typeof v !== "string") return "must be text";
      if (rule.minLength !== undefined && v.length < rule.minLength) return `must be at least ${rule.minLength} characters`;
      if (rule.maxLength !== undefined && v.length > rule.maxLength) return `must be at most ${rule.maxLength} characters`;
      return null;
    case "integer":
      if (!Number.isInteger(v)) return "must be a whole number";
      if (rule.min !== undefined && v < rule.min) return `must be at least ${rule.min}`;
      if (rule.max !== undefined && v > rule.max) return `must be at most ${rule.max}`;
      return null;
    case "date":
      return v instanceof Date && !Number.isNaN(v.getTime()) ? null : "must be an ISO date";
    case "enum":
      return rule.values.includes(v) ? null : `must be one of: ${rule.values.join(", ")}`;
    case "object": {
      const nested = validate(rule.schema, v);
      return nested.ok ? null : nested.errors.join("; ");
    }
    default:
      return "has an unknown rule";
  }
}

// TODO Part 4: reject unexpected fields, require required fields, run checkRule on
// each field, and return { ok: true, value } (a new Object.create(null) object) or { ok: false, errors }.
export function validate(schema, data) {
  return { ok: true, value: data };
}

// Validate each item and keep only the ones that pass (fail closed per item).
export function validateList(schema, data) {
  if (!Array.isArray(data)) return { ok: false, errors: ["expected a list"] };
  const value = [];
  const errors = [];
  data.forEach((item, i) => {
    const r = validate(schema, item);
    if (r.ok) value.push(r.value);
    else errors.push(`item ${i}: ${r.errors.join("; ")}`);
  });
  if (errors.length) console.warn("[schema] dropped invalid items:", errors);
  return { ok: true, value, errors };
}

// ---------------------------------------------------------------------------
// The contracts this app depends on.
// ---------------------------------------------------------------------------
export const CATEGORIES = ["community", "learning", "volunteering", "sports", "arts", "food"];

export const EVENT_SCHEMA = {
  id: { type: "integer", required: true, min: 1 },
  title: { type: "string", required: true, minLength: 3, maxLength: 80 },
  organizer: { type: "string", required: true, minLength: 1, maxLength: 80 },
  category: { type: "enum", required: true, values: CATEGORIES },
  date: { type: "date", required: true },
  capacity: { type: "integer", required: true, min: 1, max: 500 },
  spotsTaken: { type: "integer", required: true, min: 0, max: 500 },
  link: { type: "string", required: true, maxLength: 300 },
  description: { type: "string", required: true, maxLength: 2000 },
};

// TODO Part 4: the detail view also returns "location" (text, 1–120 characters).
export const EVENT_DETAIL_SCHEMA = {
  ...EVENT_SCHEMA,
};

export const SESSION_SCHEMA = {
  token: { type: "string", required: true, minLength: 20, maxLength: 100 },
  user: {
    type: "object",
    required: true,
    schema: {
      name: { type: "string", required: true, minLength: 1, maxLength: 40 },
      role: { type: "enum", required: true, values: ["member", "organizer"] },
    },
  },
};

// TODO Part 4: category is "all" or one of CATEGORIES. sortBy is "soonest" or "latest". Both are required.
export const FILTERS_SCHEMA = {};

export const ATTENDANCE_SCHEMA = {
  checkedIn: { type: "integer", required: true, min: 0 },
};
