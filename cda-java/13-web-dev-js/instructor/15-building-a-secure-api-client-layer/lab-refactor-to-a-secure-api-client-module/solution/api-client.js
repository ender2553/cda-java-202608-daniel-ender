const API_BASE = '';
const TIMEOUT_MS = 5000;
const MAX_RETRY_ATTEMPTS = 2;
const RETRY_BACKOFF_MS = 150;

let authToken = 'demo-token';

function getAuthToken() {
    return authToken;
}

const ENDPOINTS = {
    profile: {path: '/api/user', origin: "'self'"},
    ping: {path: '/api/echo', origin: "'self'"},
    hostile: {path: '/api/hostile', origin: "'self'"},
    fixtures: {path: 'fixtures/', origin: "'self'"},
    analytics: {path: 'https://analytics.example.com/collect', origin: 'https://analytics.example.com'}
};

function checkConnectSrcAlignment(endpoints, policy) {
    const allowed = (policy && policy['connect-src']) || [];
    const mismatches = [];
    for (const [name, cfg] of Object.entries(endpoints)) {
        if (!allowed.includes(cfg.origin)){
            mismatches.push({name, path: cfg.path, origin: cfg.origin});
        }
    }
    return mismatches;
}

async function fetchWithTImeout(url, options, timeoutMs) {
    const controller = new AbortController();
    const timer = setTimeout(() => controller.abort(), timeoutMs);
    try {
        return await fetch(url, { ...options, signal: controller.signal});
    } finally {
        clearTimeout(timer);
    }
}

function sleep(ms) {
    return new Promise(resolve => setTimeout(resolve, ms));
}

async function apiRequest(path, validator, options = {}) {
    const method = (options.method || 'GET').toUpperCase();
    const isIdempotent = method === 'GET';
    const attempts = isIdempotent ? MAX_RETRY_ATTEMPTS : 1;
    let lastError;

    for (let attempt = 1; attempt <= attempts; atempt++) {
      try{  
            const response = await fetchWithTImeout(`${API_BASE}${path}`,
                {...options,
                    headers: {
                        'Authorization': `Bearer ${getAuthToken()}`,
                        ...options.headers
                    }
                }, 
                TIMEOUT_MS);
                
                if (!response.ok){
                    return {ok: false, error: 'request_failed', status: response.status};
                }

                const contentType = response.headers.get('Content-Type') || '';
                if (!contentType.includes('application/json')){
                    return {ok: false, error: 'unexpected_content_type'};
                }

                const data = await response.json();
                if(!validator(data)){
                    return {ok: false, error: 'invalid_shape'};
                }

                return {ok: true, data};
        } catch (err) {
            lastError = err;
            if(!isIdempotent || attempt === attempts) break;
            await sleep(RETRY_BACKOFF_MS * attempt);
        }
    }
    return {ok: false, error: 'network_error', detail: lastError && lastError.message};
}

if(typeof module !== 'undefined' && module.exports) {
    module.exports = {ENDPOINTS, checkConnectSrcAlignment, apiRequest, getAuthToken};
}