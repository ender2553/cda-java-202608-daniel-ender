// render.js: everything that puts data on the screen.
// PART 2 (Lessons 6–10): right now every function here builds HTML strings and
// hands them to innerHTML. Any untrusted value becomes live markup.

// TODO Part 2: return the URL only if it is an absolute http: or https: URL, otherwise null.
export function safeUrl(raw) {
  return raw;
}

// TODO Part 2: sanitize with DOMPurify using an ALLOWED_TAGS / ALLOWED_ATTR allow-list.
export function sanitizeDescription(html) {
  return html;
}

export function formatDate(value) {
  const d = value instanceof Date ? value : new Date(value);
  return Number.isNaN(d.getTime())
    ? "Date TBA"
    : d.toLocaleString(undefined, { dateStyle: "medium", timeStyle: "short" });
}

function spotsLeft(ev) {
  const left = Number(ev.capacity) - Number(ev.spotsTaken);
  return Number.isFinite(left) ? `${left} of ${ev.capacity} spots left` : "Spots unknown";
}

// TODO Part 2: build this card with createElement + textContent, not a template string.
export function renderEventCard(ev, { preview = false } = {}) {
  const li = document.createElement("li");
  li.className = "event-card";
  li.innerHTML = `
    <h3 class="event-title">${ev.title}</h3>
    <p class="event-meta">${ev.organizer} · ${formatDate(ev.date)} · ${ev.category}</p>
    <p class="event-spots">${spotsLeft(ev)}</p>
    <div class="event-description">${sanitizeDescription(ev.description)}</div>
    <a class="event-link" href="${safeUrl(ev.link)}">Event website</a>
    ${preview ? "" : `<button type="button" class="details-btn" data-event-id="${ev.id}">Details</button>`}
  `;
  return li;
}

export function renderEventList(listEl, events) {
  if (events.length === 0) {
    listEl.innerHTML = `<li class="empty">No events match your filters.</li>`;
    return;
  }
  listEl.replaceChildren(...events.map((ev) => renderEventCard(ev)));
}

export function renderPreview(previewEl, draft) {
  previewEl.replaceChildren(renderEventCard({ ...draft, organizer: "You", spotsTaken: 0 }, { preview: true }));
}

// TODO Part 2: textContent for every value.
export function renderDetail(detailEl, ev) {
  detailEl.innerHTML = `
    <h3>${ev.title}</h3>
    <dl class="detail-list">
      <dt>Where</dt><dd>${ev.location}</dd>
      <dt>When</dt><dd>${formatDate(ev.date)}</dd>
      <dt>Organizer</dt><dd>${ev.organizer}</dd>
      <dt>Capacity</dt><dd>${ev.capacity}</dd>
    </dl>`;
}

export function renderFeatured(featuredEl, ev) {
  featuredEl.innerHTML = `<div class="featured-card"><h3>Featured: ${ev.title}</h3><p>${formatDate(ev.date)}</p></div>`;
}

// TODO Part 2: the name comes straight from the URL (?welcome=...). Treat it as text.
export function renderBanner(bannerEl, name) {
  bannerEl.innerHTML = `Welcome, ${name}! Thanks for following a friend's invite.`;
  bannerEl.hidden = false;
}

export function renderGreeting(greetingEl, user) {
  greetingEl.innerHTML = `Signed in as <b>${user.name}</b> (${user.role})`;
}

export function showMessage(target, text, kind = "info") {
  target.innerHTML = text;
  target.dataset.kind = kind;
}

export function showErrors(target, errors) {
  target.innerHTML = errors.length ? `<ul>${errors.map((e) => `<li>${e}</li>`).join("")}</ul>` : "";
}
