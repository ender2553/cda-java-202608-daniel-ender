# Trace Worksheet — Four Sources, Four Sinks

Fill this in by reading `app.js`. For each of the four panels, follow the value
from where it enters JavaScript to where it is written into the page. Label
each hop **data** or **potential markup** based on *how the value will
ultimately be written* — not on whether the string looks dangerous.

## 1. Welcome banner (form field)

- **Origin (source type):** `__________`
- **Why is this source untrusted?** `__________`
- **Hops (variable → label):**
  | Variable | Label (data / potential markup) |
  |---|---|
  | `__________` | `__________` |
  | `__________` | `__________` |
- **Sink (exact line + DOM operation name):** `__________`
- **What the browser does with an attacker-supplied value at this sink:** `__________`
- **Required defense for this sink:** `__________`

## 2. Profile link (URL query parameter)

- **Origin (source type):** `__________`
- **Why is this source untrusted?** `__________`
- **Hops (variable → label):**
  | Variable | Label (data / potential markup) |
  |---|---|
  | `__________` | `__________` |
- **Sink (exact line + DOM operation name):** `__________`
- **What could an unencoded `returnTo` value alter about the resulting URL?** `__________`
- **Required defense for this sink:** `__________`

## 3. Theme badge (localStorage)

- **Origin (source type):** `__________`
- **Why is this source untrusted, even though nothing "typed" it just now?** `__________`
- **Hops (variable → label):**
  | Variable | Label (data / potential markup) |
  |---|---|
  | `__________` | `__________` |
- **Sink (exact line + DOM operation name):** `__________`
- **Required defense for this sink:** `__________`

## 4. Comment list (simulated API response)

- **Origin (source type):** `__________`
- **Why does the lesson still call API-response data untrusted, even though it "comes from our own backend"?** `__________`
- **Hops (variable → label):**
  | Variable | Label (data / potential markup) |
  |---|---|
  | `__________` | `__________` |
- **Sink (exact line + DOM operation name):** `__________`
- **Required defense for this sink** (name the specific technique — this field needs to keep *some* formatting, not become plain text): `__________`

## 5. Summary table

| # | Source | Sink | Sink type | Required defense |
|---|--------|------|-----------|-------------------|
| 1 | `__________` | `__________` | `__________` | `__________` |
| 2 | `__________` | `__________` | `__________` | `__________` |
| 3 | `__________` | `__________` | `__________` | `__________` |
| 4 | `__________` | `__________` | `__________` | `__________` |
