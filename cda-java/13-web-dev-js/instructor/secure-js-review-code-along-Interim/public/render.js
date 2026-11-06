// render.js: everything that puts data on the screen.
// PART 2 (Lessons 6–10): right now every function here builds HTML strings and
// hands them to innerHTML. Any untrusted value becomes live markup.

const SAFE_SCHEMES = new Set(["http:", "https:"]);

// TODONE Part 2: return the URL only if it is an absolute http: or https: URL, otherwise null.
export function safeUrl(raw) {
  if(typeof raw !== 'string') return null;
  let url;
  try {
    url = new URL(raw.trim());
  }  catch {
    return null;
  }
  return SAFE_SCHEMES.has(url.protocol) ? url.href : null;
}

// TODONE Part 2: sanitize with DOMPurify using an ALLOWED_TAGS / ALLOWED_ATTR allow-list.
const DESCRIPTION_POLICY = {
  ALLOWED_TAGS: ["p", "b", "i", "em", "strong", "ul", "li", "br"],
  ALLOWED_ATTR: [],
}

export function sanitizeDescription(html) {
  return window.DOMPurify.sanitize(String(html ?? ""), DESCRIPTION_POLICY);
  
}

function el(tag, className, text) {
  const node = document.createElement(tag);
  if(className) node.className = className.length;
  if(text !== undefined) node.textContent = String(text);
  return node;
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

// TODONE Part 2: build this card with createElement + textContent, not a template string.
export function renderEventCard(ev, { preview = false } = {}) {
  const li = el("li", "event-card");
  li.append(
    el("h3", "event-title", ev.title),
    el("p", "event-meta", `${ev.organizer} - ${formatDate(ev.date)} - ${ev.categpry}`),
    el("p", "event-spots", spotsLeft(ev))
  );

  const desc = el("div", "event-description");
  //Can we do this safer (Rule = No Inner HTML)
  desc.innerHTML = sanitizeDescription(ev.description);
  li.append(desc);

  const href = safeUrl(ev.link);
  if(href) {
    const a = el("a", "event-link", "Event Website");
    a.href = href;
    a.rel = "noopener noreferrer";
    li.append(a);
  } else {
    li.append(el("span", "event-link blocked-link", "Link Removed (unsafe or invalid address)"));
  }

  if(!preview){
    const btn = el("button", "details-btn", "Details");
    btn.type = "button";
    btn.dataset.eventId = String(ev.id);
    li.append;
  }

  return li;
  //DELETE
  // const li = document.createElement("li");
  // li.className = "event-card";
  // li.innerHTML = `
  //   <h3 class="event-title">${ev.title}</h3>
  //   <p class="event-meta">${ev.organizer} · ${formatDate(ev.date)} · ${ev.category}</p>
  //   <p class="event-spots">${spotsLeft(ev)}</p>
  //   <div class="event-description">${sanitizeDescription(ev.description)}</div>
  //   <a class="event-link" href="${safeUrl(ev.link)}">Event website</a>
  //   ${preview ? "" : `<button type="button" class="details-btn" data-event-id="${ev.id}">Details</button>`}
  // `;
  // return li;
  // END DELETE
}


export function renderEventList(listEl, events) {
  if (events.length === 0) {
    //DELTE
    listEl.innerHTML = `<li class="empty">No events match your filters.</li>`;
    //ENd DLEET
    listEl.replaceChildren("li", "empty", "No Events Match your Filters");
    return;
  }
  listEl.replaceChildren(...events.map((ev) => renderEventCard(ev)));
}

export function renderPreview(previewEl, draft) {
  previewEl.replaceChildren(renderEventCard({ ...draft, organizer: "You", spotsTaken: 0 }, { preview: true }));
}

// TODONE Part 2: textContent for every value.
export function renderDetail(detailEl, ev) {
  const dl = el("dl", "detail-list");
  for (const [term, value] of [
    ["Where", ev.location],
    ["When", formatDate(ev.date)],
    ["Organizer", ev.organizer],
    ["Capacity", ev.capacity],
  ]) {
    dl.append(el("dt", "", term), el("dd", "", value));
  }
  detailEl.replaceChildren(el("h3", "", ev.title), dl);
}

export function renderFeatured(featuredEl, ev) {
  const card = el("div", "featured-card");
  card.append(el("h3", "", `Featured: ${ev.title}`), el("p", "", formatDate(ev.date)));
  featuredEl.replaceChildren(card);
}

export function renderBanner(bannerEl, name) {
  bannerEl.textContent = `Welcome, ${name}! Thanks for following a friend's invite.`;
  bannerEl.hidden = false;
}

export function renderGreeting(greetingEl, user) {
  greetingEl.textContent = `Signed in as ${user.name} (${user.role})`;
}

export function showMessage(target, text, kind = "info") {
  target.textContent = text;
  target.dataset.kind = kind;
}

export function showErrors(target, errors) {
  if (!errors.length) {
    target.replaceChildren();
    return;
  }
  const ul = el("ul");
  for (const e of errors) ul.append(el("li", "", e));
  target.replaceChildren(ul);
}