"""
banner.py -- shared CLI banner for WS-VSW tools.

Import and call print_banner() at the top of each tool's main() so the
three apps present a consistent, professional look when run from the
terminal. Not part of any graded exercise -- nothing here needs editing.
"""
import os
import sys

CYAN = "\033[96m"
BOLD = "\033[1m"
DIM = "\033[2m"
RESET = "\033[0m"

WIDTH = 64
PROGRAM_TITLE = "WS-VSW | Contextual Vulnerability Risk Assessment Workshop"


def _color_enabled():
    """Use color only when we're writing to a real terminal and the user
    hasn't opted out via the NO_COLOR convention (https://no-color.org/)."""
    if os.environ.get("NO_COLOR"):
        return False
    return sys.stdout.isatty()


def _centered(text, width):
    pad = max(0, width - len(text))
    left = pad // 2
    right = pad - left
    return " " * left + text + " " * right


def print_banner(tool_name, details=None):
    """
    Print a boxed banner:

        ================================================================
              WS-VSW | Contextual Vulnerability Risk Assessment Workshop
                          Mock Vulnerability Scanner
                          Track: track_a_meridian_health
        ================================================================

    tool_name: short name of the specific tool (e.g. "Mock Vulnerability
    Scanner").
    details: optional list of extra lines (e.g. track name, mode flags).
    """
    rule = "=" * WIDTH
    lines = [PROGRAM_TITLE, tool_name] + list(details or [])
    color = _color_enabled()

    out = []
    out.append(rule)
    for line in lines:
        out.append(_centered(line, WIDTH).rstrip())
    out.append(rule)

    if color:
        print(CYAN + BOLD + out[0] + RESET)
        for line in out[1:-1]:
            print(CYAN + line + RESET)
        print(CYAN + BOLD + out[-1] + RESET)
    else:
        for line in out:
            print(line)
    print()
