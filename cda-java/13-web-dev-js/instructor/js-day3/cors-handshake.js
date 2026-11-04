const DND_BASE = 'https://www.dnd5eapi.co';

async function loadMonster() {
    try {
        const [jokeRes, dndRes] = await Promise.all([
            // Direct call - NO allorigins proxy (server sends Access-Control-Allow-Origin: *)
            fetch('https://official-joke-api.appspot.com/random_joke'),
            // Direct call - /api/monsters/... 301-redirects here, so skip the hop
            fetch(`${DND_BASE}/api/2014/monsters/owlbear`)
        ]);

        if (!jokeRes.ok || !dndRes.ok) {
            throw new Error(`Fetch failed: Joke (${jokeRes.status}), D&D (${dndRes.status})`);
        }

        const [jokeData, dndData] = await Promise.all([
            jokeRes.json(),
            dndRes.json()
        ]);

        console.log('Joke:', jokeData.setup);
        console.log('Monster:', dndData.name);

        return { joke: jokeData, monster: dndData };

    } catch (networkError) {
        console.error('Network Failure:', networkError);
        return null;
    }
}

// ---------- Rendering helpers ----------
// Everything from the APIs goes in via textContent / setAttribute,
// never innerHTML, so the remote data can't inject markup.

function el(tag, className, text) {
    const node = document.createElement(tag);
    if (className) node.className = className;
    if (text !== undefined && text !== null) node.textContent = text;
    return node;
}

function renderJoke(joke) {
    const card = el('section', 'card joke');
    card.append(
        el('h2', null, 'Official Joke API'),
        el('p', 'setup', joke.setup),
        el('p', 'punchline', joke.punchline),
        el('span', 'tag', `#${joke.type}`)
    );
    return card;
}

function renderMonster(m) {
    const card = el('section', 'card monster');
    card.append(
        el('h2', null, 'D&D 5e API'),
        el('p', 'name', m.name),
        el('p', 'subtitle', `${m.size} ${m.type}, ${m.alignment}`)
    );

    if (m.image) {
        const img = document.createElement('img');
        img.src = DND_BASE + m.image;
        img.alt = m.name;
        card.append(img);
    }

    const ac = m.armor_class?.[0]?.value ?? '—';
    const speed = Object.entries(m.speed ?? {})
        .map(([kind, dist]) => `${kind} ${dist}`)
        .join(', ');

    const stats = el('dl', 'stats');
    const statPairs = [
        ['Challenge', m.challenge_rating],
        ['XP', m.xp],
        ['Armor Class', ac],
        ['Hit Points', `${m.hit_points} (${m.hit_points_roll})`],
        ['Speed', speed],
        ['Darkvision', m.senses?.darkvision ?? '—'],
        ['STR', m.strength], ['DEX', m.dexterity], ['CON', m.constitution],
        ['INT', m.intelligence], ['WIS', m.wisdom], ['CHA', m.charisma]
    ];
    for (const [label, value] of statPairs) {
        const box = el('div');
        box.append(el('dt', null, label), el('dd', null, value));
        stats.append(box);
    }
    card.append(stats);

    card.append(renderList('Special Abilities', m.special_abilities));
    card.append(renderList('Actions', m.actions));

    return card;
}

function renderList(title, items = []) {
    const frag = document.createDocumentFragment();
    if (items.length === 0) return frag;

    frag.append(el('h3', null, title));
    const ul = el('ul');
    for (const item of items) {
        const li = el('li');
        li.append(el('strong', null, `${item.name}. `), document.createTextNode(item.desc));
        ul.append(li);
    }
    frag.append(ul);
    return frag;
}

// ---------- Wire it up ----------

const statusEl = document.getElementById('status');
const resultsEl = document.getElementById('results');
const reloadBtn = document.getElementById('reload');

async function render() {
    reloadBtn.disabled = true;
    statusEl.className = '';
    statusEl.textContent = 'Loading…';
    resultsEl.replaceChildren();

    const data = await loadMonster();

    if (!data) {
        statusEl.className = 'error';
        statusEl.textContent = 'Could not load data. Check the console for details.';
    } else {
        statusEl.textContent = '';
        resultsEl.append(renderJoke(data.joke), renderMonster(data.monster));
    }
    reloadBtn.disabled = false;
}

reloadBtn.addEventListener('click', render);
render();
