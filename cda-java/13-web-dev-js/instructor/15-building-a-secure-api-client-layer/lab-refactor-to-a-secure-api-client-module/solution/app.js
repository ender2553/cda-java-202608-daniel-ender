function validateProfile(data){
    return typeof data === 'object' && data !== null
    && typeof data.id === 'number'
    && typeof data.name === 'string';
}

function validatePing(data){
 return typeof data === 'object' && data !== null
    && typeof data.ok === 'boolean';
}

function validateOrder(data){
    if (typeof data !== 'object' || data === null) return false;
    if (typeof data.customer !== 'object' || data.customer === null) return false;
    if (typeof data.customer.name !== 'string') return false;
    if (typeof data.total !== 'number') return false;
    return true; 
}

function renderFields(el, fields) {
    el.innerHTML = '';
    for (const text of fields){
        const line = document.createElement('div');
        line.append(document.createTextNode(text));
        el.append(line);
    }
}

function renderFailure(el, result){
    console.error('API failure detail:', result);
    const messages = {
        network_error: 'Could not reach the server. Please try again',
        request_failed: 'The server reported a problem with this request',
        unexpected_content_type: 'The server returned an unexpected response format',
        invalid_shape: 'The server returned in an unexpected shape. Like a trapezoid lol.'
    };
    el.textContent = messages[result.error] || 'Something went wrong. Please try again';
}

//Widget 1: Profile
document.getElementById('profile-btn').addEventListener('click', async () =>{
    const el = document.getElementById('profile-result');
    const result = await apiRequest('/api/user', validateProfile);
    if (result.ok) {
        renderFields(el, [`id: ${result.data.id}`, `name: ${result.data.name}`]);
    } else {
        renderFailure(el, result);
    }
});

//Widget 2: Ping
document.getElementById('ping-btn').addEventListener('click', async() =>{
    const el = document.getElementById('ping-result');
    const result = await apiRequest('/api/echo', validatePing);
    if (result.ok){
        renderFields(el, [`ok: ${result.data.ok}`, `method: ${result.data.method}`]);
    }else {
        renderFailure(el, result);
    }
});

const FIXTURES = [
    {url: 'fixtures/valid.json', label: 'valid'},
    {url: 'fixtures/shape-invalid.json', label: 'shape-invalid'},
    {url: 'fixtures/hostile-field.json', label: 'hostile-field'},
];

document.getElementById('fixtures-btn').addEventListener('click', async () => {
    const container = document.getElementById('fixtures-result');
    container.innerHTML = '';
    for (const fixture of FIXTURES){
        const row = document.createElement('div');
        row.className = 'row';
        const label = document.createElement('div');
        label.className = 'label';
        label.textContent = fixture.label;
        row.append(label);
        container.append(row);

        const result = await apiRequest(fixture.url, validateOrder);
        if (result.ok){
            renderFields(row, 
                [`${result.data.customer.name} - $${result.data.total.toFixed(2)}`]);
        } else {
            renderFailure(row, result);
        }
    }
});

(async function checkPolicy() {
    const policyResponse = await fetch('connect-src-policy.json');
    const policy = await policyResponse.json;
    const mismatches = checkConnectSrcAlignment(ENDPOINTS, policy);

    const el = document.getElementById('policy-result');
    if (mismatches.length === 0){
        el.textContent = 'All registered endpoints are covered by connect-src';
        return;
    }

    el.innerHTML = '';
    const heading = document.createElement('div');
    heading.textContent = `${mismatches.length} registered endpoints are not covered by connect-src`;
    el.append(heading);
    for (const m of mismatches){
        const line = document.createElement('div');
        line.append(document.createTextNode(` - ${m.name} (${m.path}) -> origin ${m.origin} not in policy`));
        el.append(line);
    }
})();