# WS-VSW: Contextual Vulnerability Risk Assessment Workshop

Three small Python command-line apps that together simulate a real security
workflow: scan for vulnerabilities, score them with business context, and
visualize the result as a risk heat map.

If the steps below feel like too much at once, see **QUICKSTART.md** for
the same steps with no explanation attached — just the commands.

## Project structure

```
workshop/
├── README.md              <- you are here
├── QUICKSTART.md           <- copy-paste command list, no explanations
├── requirements.txt         <- Python packages this project needs
├── common/                  <- the code you'll edit and run
│   ├── banner.py              <- shared CLI banner (no editing needed)
│   ├── scanner.py            <- Phase 1: mock vulnerability scanner
│   ├── risk_calculator.py    <- Phase 2: contextual risk scoring
│   ├── heatmap.py             <- Phase 2.5: risk heat map generator
│   ├── test_scanner.py        <- self-check tests (don't edit)
│   ├── test_risk_calculator.py
│   └── test_heatmap.py
└── data/                     <- your track's scenario data (don't edit)
    ├── signatures.json
    ├── epss_lookup.json
    ├── exploit_intel.json
    └── track_a_meridian_health/  (or track_b_... / track_c_...)
        ├── assets.json
        └── services.json
```

## Prerequisites

- Python 3.9 or newer. Check your version:
  ```bash
  python3 --version
  ```
- A terminal (macOS/Linux Terminal, or Windows Terminal / PowerShell / Git Bash).

## 1. Change directory into the project

Open a terminal and move into the folder your instructor gave you. Replace
the path below with wherever you actually saved/unzipped it:

```bash
cd path/to/workshop
```

Confirm you're in the right place — this should list `common`, `data`,
`README.md`:

```bash
ls
```

## 2. Create a virtual environment

A virtual environment ("venv") keeps this project's Python packages
separate from everything else on your computer. Create one **once**, in
the `workshop` folder:

**macOS / Linux:**
```bash
python3 -m venv venv
```

**Windows (PowerShell):**
```powershell
py -m venv venv
```

This creates a `venv/` folder — you won't need to touch its contents directly.

## 3. Activate the virtual environment

You need to do this **every time you open a new terminal** to work on this
project. Your terminal prompt should show `(venv)` at the start once it's
active.

**macOS / Linux:**
```bash
source venv/bin/activate
```

**Windows (PowerShell):**
```powershell
venv\Scripts\Activate.ps1
```

**Windows (Command Prompt / cmd.exe):**
```cmd
venv\Scripts\activate.bat
```

To turn it off later: `deactivate`

## 4. Install dependencies

With the venv active:

```bash
pip install -r requirements.txt
```

This installs `pytest` (for running your self-check tests) and
`matplotlib`/`numpy` (for the heat map).

## 5. Move into the code folder

All the commands below assume you're inside `common/`:

```bash
cd common
```

## 6. Run your self-check tests

Do this after finishing each function, not just at the end:

```bash
pytest test_scanner.py -v
pytest test_risk_calculator.py -v
pytest test_heatmap.py -v
```

Green (`PASSED`) means that piece is working. Red (`FAILED`) tells you
which function still needs work and shows you what it expected vs. what
your code returned.

## 7. Run the applications

Each app takes a `--track` argument. Use exactly one of:
`track_a_meridian_health`, `track_b_northbridge_financial`,
`track_c_coastal_logistics` — whichever your instructor assigned you.

**Step 1 — run the scanner** (must run first; everything else depends on its output):
```bash
python3 scanner.py --track track_a_meridian_health
```
Writes `scan_results.json` into your track's `data/` folder.

**Step 2 — run the risk calculator:**
```bash
python3 risk_calculator.py --track track_a_meridian_health
```
Writes `risk_register.json`, ranked highest-risk first.

**Step 3 — generate the heat map:**
```bash
python3 heatmap.py --track track_a_meridian_health
```
Writes `risk_heatmap.png` — open it like any image file.

**Later, in Phase 3** — re-run the calculator with the threat intel update applied:
```bash
python3 risk_calculator.py --track track_a_meridian_health --apply-intel-update
```

## Troubleshooting

| Problem | Likely fix |
|---|---|
| `command not found: python3` | Try `python` instead of `python3` (common on Windows). |
| `ModuleNotFoundError: No module named 'pytest'` (or `matplotlib`) | Your venv isn't active, or you skipped step 4. Re-run steps 3 and 4. |
| `FileNotFoundError` when running `risk_calculator.py` | Run `scanner.py` for the same track first — it produces the file `risk_calculator.py` needs. |
| `error: argument --track: invalid choice` | Check spelling — track names use underscores, e.g. `track_a_meridian_health`, not `track-a` or `Track A`. |
| Nothing happens / prompt just hangs | You're probably missing the `--track` flag entirely — it's required. |
