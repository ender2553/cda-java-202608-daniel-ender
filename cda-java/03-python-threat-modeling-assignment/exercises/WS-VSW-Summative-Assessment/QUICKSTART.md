# Quick Start

Just the commands, in order. Type each one, press Enter, wait for it to
finish, then go to the next one. Replace `track_a_meridian_health` with
YOUR assigned track name everywhere you see it.

Need the "why" behind any step? See README.md instead.

## One-time setup (do this once)

```
cd path/to/workshop
python3 -m venv venv
```

## Every time you sit down to work (do this every session)

**Mac/Linux:**
```
cd path/to/workshop
source venv/bin/activate
```

**Windows:**
```
cd path/to/workshop
venv\Scripts\activate.bat
```

You'll know it worked because your terminal line now starts with `(venv)`.

## Still one-time setup (do this once, right after your first activate)

```
pip install -r requirements.txt
cd common
```

## Every time after that, you're already in the right folder. Just run:

### Check your work as you code:
```
pytest test_scanner.py -v
```
```
pytest test_risk_calculator.py -v
```
```
pytest test_heatmap.py -v
```
All lines should say `PASSED`. If a line says `FAILED`, read the function
name in it — that's the one to fix next.

### Once scanner tests are all green:
```
python3 scanner.py --track track_a_meridian_health
```

### Once calculator tests are all green:
```
python3 risk_calculator.py --track track_a_meridian_health
```

### Once heatmap tests are all green:
```
python3 heatmap.py --track track_a_meridian_health
```
Open `data/track_a_meridian_health/risk_heatmap.png` to see it.

### When your instructor gives you the Phase 3 threat intel update:
```
python3 risk_calculator.py --track track_a_meridian_health --apply-intel-update
```

## If something breaks

1. Check `(venv)` is still showing at the start of your terminal line. If
   not, run the "every time you sit down" steps above again.
2. Check you're inside the `common` folder. Run `pwd` (Mac/Linux) or `cd`
   (Windows) with no arguments to see where you are — the end of the path
   should say `common`.
3. Check you typed the track name exactly right — all lowercase, underscores,
   no spaces.
4. Still stuck? Ask your instructor rather than guessing.
