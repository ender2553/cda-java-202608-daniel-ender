# GitHub Actions CI and Branch Protection

In this walkthrough you will build a small Python project, add an automated test pipeline with GitHub Actions, and set up two safety gates that stop broken code from reaching your `main` branch:

| Gate | Where it runs | What it does |
| --- | --- | --- |
| **Pre-push hook** | Your computer | Runs the tests before `git push` sends anything. If a test fails, the push is cancelled. |
| **Branch protection** | GitHub | Blocks a Pull Request from merging into `main` until the CI tests pass. |

**Estimated time:** 30–40 minutes

### Words you'll see

| Term | Meaning |
| --- | --- |
| **CI (Continuous Integration)** | Automatically building and testing code every time someone pushes a change. |
| **GitHub Actions** | GitHub's built-in service for running CI jobs on its own servers. |
| **Workflow** | A file in `.github/workflows/` that tells GitHub Actions what to run and when. |
| **YAML** | The text format workflow files are written in. It uses indentation (spaces) to show structure. |
| **Runner** | The temporary virtual machine GitHub starts to run your workflow. |
| **Status check** | The pass/fail result a workflow reports back on a commit or Pull Request. |
| **Pull Request (PR)** | A request to merge the changes on one branch into another branch, such as `main`. |
| **WSL (Windows Subsystem for Linux)** | A feature of Windows that runs a real Linux system, such as Ubuntu, alongside Windows. |
| **LF / CRLF** | The invisible characters that mark the end of each line. Linux uses `LF`. Windows uses `CRLF`. |

---

## Before You Start

### What you need

- A GitHub account
- Git installed (`git --version`)
- Python 3.10 or newer installed
- Visual Studio Code (VS Code) installed
- **One** of these terminals:
  - **Git Bash** on Windows
  - **WSL** (Ubuntu) on Windows, or a Linux/macOS terminal
- **WSL users only:** the **WSL** extension for VS Code (search for `WSL` in the VS Code Extensions panel)
- Your Git name and email configured (see below)

### Check your Git identity

Git labels every commit with your name and email. Run these two commands in the terminal you plan to use:

```bash
git config --global user.name
git config --global user.email
```

**Checkpoint:** Each command prints a value. If either prints nothing, set it now (WSL is a separate system from Windows, so WSL users often need to do this even if Git Bash is already set up):

```bash
git config --global user.name "Your Name"
git config --global user.email "you@example.com"
```

### Read this before you start

- **Code goes in VS Code. Commands go in the terminal.**
  - `bash` blocks are terminal commands.
  - `python`, `yaml`, and `sh` blocks are code you paste into a file in VS Code.
  - `text` blocks are sample output so you know what to expect. Do not paste them anywhere.
- **Copy one block at a time.** Every terminal command works in both Git Bash and WSL/Linux unless it is labelled with a specific terminal.
- **Use one kind of terminal for the whole walkthrough.** Choose Git Bash *or* WSL and stick with it. Do not clone the repository in Git Bash and then work on it in WSL (or the other way around). Windows and Linux end lines differently (`CRLF` vs `LF`), and mixing them is what causes errors such as `/bin/sh^M: bad interpreter`.
- **WSL users:** work in your Linux home folder (`cd ~`), not in a Windows folder under `/mnt/c/`.
- When a command says `<your-username>`, replace the whole thing, including the `<` and `>`, with your GitHub username.

---

## Step 1: Create a New GitHub Repository

1. Go to [github.com/new](https://github.com/new).
2. Set **Repository name** to `github-actions-demo`.
3. Set visibility to **Public**.
   > **Why public?** Branch protection rules are free on public repositories. Private repositories need a GitHub Pro, Team, or Enterprise plan.
4. Turn on **Add a README file**.
5. Under **Add .gitignore**, choose the **Python** template.
6. Click **Create repository**.

---

## Step 2: Clone the Repository and Open It in VS Code

### 2.1 Clone the repository

In your terminal, clone your new repository and move into its folder:

```bash
cd ~
git clone https://github.com/<your-username>/github-actions-demo.git
cd github-actions-demo
```

**Checkpoint:** Run `ls -a`. You should see `.git`, `.gitignore`, and `README.md`.

### 2.2 Open the project in VS Code

```bash
code .
```

VS Code opens with `github-actions-demo` in the Explorer panel.

> **WSL users:** The bottom-left corner of VS Code should show **WSL: Ubuntu**. If it does not, install the WSL extension and run `code .` again from your WSL terminal.
>
> **`code: command not found`?** Open VS Code manually, then use **File** > **Open Folder** and choose the `github-actions-demo` folder.

### 2.3 Show the `.git` folder in VS Code

By default, VS Code hides the `.git` folder from the Explorer panel. You will need it in Step 9, so turn it on now.

1. Open **File** > **Preferences** > **Settings** (macOS: **Code** > **Settings** > **Settings**).
2. In the search box, type `files.exclude`.
3. In the **Files: Exclude** list, hover over `**/.git` and click the **X** (Remove Item) icon.

**Checkpoint:** The Explorer panel now shows a `.git` folder at the top of the project.

> Do not edit or delete anything inside `.git` other than the hook file in Step 9. Git stores your project's history there.

### 2.4 Switch to the terminal inside VS Code

From here on, use the terminal built into VS Code so your editor and commands are in one window. It is the same kind of terminal you used in Step 2.1 (Git Bash or WSL), so this does not break the "one kind of terminal" rule. You can close the terminal you used to clone.

**Git Bash users:** VS Code on Windows opens PowerShell by default, and PowerShell will not run these commands. Make Git Bash the default first:

1. Press **Ctrl+Shift+P** to open the Command Palette.
2. Type `Terminal: Select Default Profile` and press **Enter**.
3. Choose **Git Bash**.

**Everyone:** open **Terminal** > **New Terminal** (or press **Ctrl+`**).

**Checkpoint:** The terminal panel's dropdown shows **bash** (Git Bash and WSL) and the prompt is inside `github-actions-demo`.

---

## Step 3: Set Up LF Line Endings

Windows editors save files with `CRLF` line endings by default. Linux and WSL expect `LF`, and Git repositories normally store `LF`.

Most files (`.py`, `.yml`, `.md`) still work with `CRLF`. **Shell scripts do not**: a script with `CRLF` endings fails to run on Linux and WSL. The pre-push hook you write in Step 9 is a shell script, so it is the file that matters most. The two settings below protect different files:

| Setting | Protects |
| --- | --- |
| VS Code **Files: Eol** (3.1) | Every new file you create in VS Code, **including the hook**. |
| `.gitattributes` (3.2) | Files Git tracks (`app.py`, `ci.yml`, and so on). It does **not** cover the hook, because Git does not track the `.git` folder. |

### 3.1 Tell VS Code to use LF for new files

1. Open **File** > **Preferences** > **Settings** (macOS: **Code** > **Settings** > **Settings**).
2. In the search box, type `eol`.
3. Set **Files: Eol** to `\n`.

> This setting only affects files created from now on. Whenever you save a file in this walkthrough, glance at the **status bar** in the bottom-right corner of VS Code. It should say **LF**. If it says **CRLF**, click it, choose **LF**, and save again.

### 3.2 Create `.gitattributes`

A `.gitattributes` file tells Git how to handle line endings for tracked files. In the line below, `text=auto` lets Git detect which files are text, and `eol=lf` makes Git use `LF` for them on every operating system.

In your terminal, run:

```bash
cat > .gitattributes <<'EOF'
* text=auto eol=lf
EOF
```

**Checkpoint:** Run `cat .gitattributes`. It should print `* text=auto eol=lf`.

---

## Step 4: Install pytest on Your Computer

You need `pytest` locally so you can run the tests yourself and so the pre-push hook in Step 9 can run them. Run the command that matches your terminal.

**Git Bash (Windows):**

```bash
python -m pip install pytest
```

> If you see `python: command not found`, use `py -m pip install pytest` instead.

**WSL (Ubuntu) / Linux:**

```bash
sudo apt update && sudo apt install -y python3-pytest
```

**macOS:**

```bash
python3 -m pip install pytest
```

---

## Step 5: Create the Application and Its Tests

### 5.1 Create the application code

In VS Code, create a file named `app.py` in the project root and paste this function, which adds two numbers:

```python
# app.py
def add_numbers(a: int, b: int) -> int:
    return a + b
```

Save the file.

### 5.2 Create the test file

In VS Code, create a file named `test_app.py` in the project root and paste these tests, which check that `add_numbers` gives the right answers:

```python
# test_app.py
from app import add_numbers


def test_add_numbers():
    assert add_numbers(2, 3) == 5
    assert add_numbers(-1, 1) == 0
    assert add_numbers(0, 0) == 0
```

Save the file.

### 5.3 Run the tests locally

In your terminal, run:

**Git Bash (Windows):**

```bash
python -m pytest
```

**WSL (Ubuntu) / Linux / macOS:**

```bash
python3 -m pytest
```

**Checkpoint:** The last line of the output should say `1 passed`.

---

## Step 6: Create the GitHub Actions CI Workflow

GitHub Actions looks for workflow files in the `.github/workflows/` folder. This workflow runs your tests on a GitHub server every time code is pushed to `main` or a Pull Request targets `main`.

### 6.1 Create the workflow file

In your terminal, create the `.github/workflows` folders and an empty `ci.yml` file:

```bash
mkdir -p .github/workflows
touch .github/workflows/ci.yml
```

**Checkpoint:** In the VS Code Explorer, expand `.github` > `workflows`. You should see `ci.yml`.

### 6.2 Add the workflow configuration

In VS Code, open `.github/workflows/ci.yml`, paste the following, and save it.

> **Indentation matters in YAML.** Paste on line 1 of the empty file so nothing gets shifted. Use spaces, not tabs.

```yaml
name: Simple Python CI

on:
  push:
    branches: [ "main" ]
  pull_request:
    branches: [ "main" ]

jobs:
  test:
    runs-on: ubuntu-latest

    steps:
      - name: Checkout repository
        uses: actions/checkout@v7

      - name: Set up Python
        uses: actions/setup-python@v7
        with:
          python-version: "3.12"

      - name: Install dependencies
        run: |
          python -m pip install --upgrade pip
          pip install pytest

      - name: Run test suite
        run: pytest
```

### 6.3 Understand the workflow

| Section | Meaning |
| --- | --- |
| `on:` | **When** the workflow runs: on a push to `main` and on any Pull Request into `main`. |
| `jobs: test:` | A job named `test`. You will select this name in Step 8. |
| `runs-on: ubuntu-latest` | GitHub runs the job on a fresh Linux virtual machine (a **runner**). |
| `uses: actions/...@v7` | Reuses a ready-made action published by GitHub. `@v7` pins the major version. |
| `steps:` | Download your code, install Python and pytest, then run the tests. |

**Checkpoint:** Your Explorer panel should show `.github/workflows/ci.yml`, `.gitattributes`, `.gitignore`, `app.py`, `README.md`, and `test_app.py`.

---

## Step 7: Push the Baseline Code to GitHub

Commit all your files and push them directly to `main`. This first run registers the workflow so GitHub knows about the `test` check.

```bash
git add .
git commit -m "Initialize project with tests and CI workflow"
git push origin main
```

**Checkpoint:**

1. Open your repository on GitHub and click the **Actions** tab.
2. Click the run named **Initialize project with tests and CI workflow**.
3. Wait for a green checkmark ✅. This means the tests passed on GitHub's server.

> Do not continue until this run is green. The `test` check will not appear in Step 8 until it has run at least once.

**If the run is not green:**

- **Red ❌:** click the run, then the **test** job, and expand the step with the red mark to read the error. A YAML error usually means the indentation shifted when pasting. See [YAML errors in the Actions tab](#yaml-errors-in-the-actions-tab-step-7).
- **Green ✅ with yellow warning annotations** (for example, about Node.js or action versions): these are warnings, not failures. You can continue.
- **The push itself failed** (for example, `refusing to allow a Personal Access Token to create or update workflow`): see [`git push` asks for a password or is rejected](#git-push-asks-for-a-password-or-is-rejected).

---

## Step 8: Turn On Branch Protection

GitHub Actions runs **after** your code arrives on GitHub, so it cannot stop a push while it is happening. Instead, you tell GitHub to **refuse to merge** any Pull Request whose tests fail.

1. In your repository on GitHub, open **Settings** > **Branches**.
2. Click **Add classic branch protection rule**.
3. Set **Branch name pattern** to `main`.
4. Check **Require status checks to pass before merging**.
5. Under it, check **Require branches to be up to date before merging**.
6. In the status check search box, type `test` and select it. (`test` is the job name from Step 6.)
7. Check **Do not allow bypassing the above settings** so the rule also applies to repository admins (that includes you).
8. Click **Create**.

**Checkpoint:** Back on **Settings** > **Branches**, you should see a rule for `main`.

> **Only see "Add branch ruleset"?** GitHub is gradually replacing classic rules with *rulesets*. They do the same job here:
>
> 1. Click **Add branch ruleset**.
> 2. Set **Ruleset Name** to `protect-main` and **Enforcement status** to **Active**.
> 3. Under **Target branches**, click **Add target** > **Include default branch**.
> 4. Check **Require status checks to pass**, click **Add checks**, and select `test`.
> 5. Leave the **Bypass list** empty, then click **Create**.

---

## Step 9: Add a Pre-Push Hook (Local Gate)

A **Git hook** is a script that Git runs automatically at certain moments. A `pre-push` hook runs every time you type `git push`. If the script fails, Git cancels the push before anything is sent to GitHub.

Hooks live in the hidden `.git/hooks/` folder. Git does not track this folder, so every developer installs hooks on their own computer.

### 9.1 Create the hook file in VS Code

In the VS Code Explorer, expand `.git` > `hooks` and create a new file named `pre-push` (no file extension).

> **Can't see the `.git` folder?** Follow Step 2.3 to show it.
>
> The `hooks` folder already contains sample files ending in `.sample`. Leave them alone. Git ignores them.

### 9.2 Add the hook script

Paste the following into `pre-push` and save it.

> **This file must use LF line endings.** Before saving, check the status bar. If it says **CRLF**, click it and choose **LF**. A hook saved with `CRLF` fails in WSL and Linux.

```sh
#!/bin/sh
# Runs the test suite before every git push.
# If any test fails, the push is cancelled.

echo "[pre-push] Running tests before push..."

# Find a Python command that has pytest installed
# (python3 on Linux/WSL/macOS, python or py on Windows).
PYTHON=""
for candidate in python3 python py; do
  if "$candidate" -c "import pytest" >/dev/null 2>&1; then
    PYTHON="$candidate"
    break
  fi
done

if [ -z "$PYTHON" ]; then
  echo "[pre-push] pytest is not installed. See Step 4, then push again."
  exit 1
fi

if ! "$PYTHON" -m pytest; then
  echo "[pre-push] Tests failed. Push cancelled."
  exit 1
fi

echo "[pre-push] All tests passed. Continuing with push."
```

### 9.3 Understand the hook

| Part | What it does |
| --- | --- |
| `#!/bin/sh` | The first line, called a *shebang*. It tells the system to run this file with the shell. If the line ends with a hidden `CRLF`, the system looks for a program called `sh` followed by an invisible character, can't find it, and the hook fails. |
| The `for candidate in ...` loop | Python's command name differs between systems: `python3` on Linux, WSL, and macOS, and `python` or `py` on Windows. The loop tries each name and keeps the first one that has pytest installed, so the same script works everywhere. |
| `"$PYTHON" -m pytest` | Runs your tests with the Python that the loop found. |
| `exit 1` | Ends the script with a failure code. Any non-zero exit code tells Git to **cancel the push**. |
| Reaching the end of the script | Ends with exit code 0 (success), so Git continues the push. |

### 9.4 Make the hook executable

In your terminal, run:

```bash
chmod +x .git/hooks/pre-push
```

### 9.5 Confirm the hook has LF line endings

This command counts the hidden carriage-return characters (the `CR` in `CRLF`) in the hook:

```bash
tr -cd '\r' < .git/hooks/pre-push | wc -c
```

**Checkpoint:** The output should be `0`. Any other number means the hook has `CRLF` endings. See [Troubleshooting](#troubleshooting).

> **Second opinion (optional):** `file .git/hooks/pre-push` describes the file. The exact wording varies by system, but it must **not** mention `CRLF`.

---

## Step 10: Break the Code on a Feature Branch

With branch protection on, all changes go through a feature branch and a Pull Request. Now you will push a bug on purpose and watch the gates catch it.

### 10.1 Create a feature branch

```bash
git switch -c feature/update-logic
```

### 10.2 Introduce a bug

In VS Code, open `app.py`, select all of its contents (**Ctrl+A** / **Cmd+A**), and replace them with this version, which subtracts instead of adds:

```python
# app.py
def add_numbers(a: int, b: int) -> int:
    return a - b  # Bug: subtraction instead of addition
```

Save the file.

### 10.3 Commit and try to push

```bash
git add app.py
git commit -m "Refactor addition logic"
git push -u origin feature/update-logic
```

**Expected result:** The pre-push hook runs the tests, they fail, and Git refuses to push. Your output will look similar to this:

```text
[pre-push] Running tests before push...
======================== FAILURES ========================
____________________ test_add_numbers ____________________

    def test_add_numbers():
>       assert add_numbers(2, 3) == 5
E       assert -1 == 5
E        +  where -1 = add_numbers(2, 3)

test_app.py:6: AssertionError
================ short test summary info =================
FAILED test_app.py::test_add_numbers - assert -1 == 5
=================== 1 failed in 0.08s ====================
[pre-push] Tests failed. Push cancelled.
error: failed to push some refs to 'https://github.com/<your-username>/github-actions-demo.git'
```

**The local gate worked:** the broken code never left your computer.

---

## Step 11: See the GitHub Gate Block the Merge

A hook only runs on computers where someone installed it, and anyone can skip it with `--no-verify`. That is why the server-side gate on GitHub matters. Let's skip the hook on purpose and prove GitHub still protects `main`.

### 11.1 Push while skipping the hook

```bash
git push --no-verify -u origin feature/update-logic
```

### 11.2 Open a Pull Request

1. Open your repository on GitHub.
2. Click the **Code** tab.
3. Click **Compare & pull request** in the yellow banner.
4. Add a short description, for example: `Refactor addition logic`.
5. Click **Create pull request**.

**Expected result:** After a minute, the `test` check shows a red ❌, and the **Merge pull request** button is disabled. GitHub will not let the bug into `main`.

> Leave this Pull Request open. You will fix it in the next step.

---

## Step 12: Fix the Code and Merge

### 12.1 Restore the correct code

In VS Code, open `app.py`, select all of its contents, and replace them with the correct version:

```python
# app.py
def add_numbers(a: int, b: int) -> int:
    return a + b
```

Save the file.

### 12.2 Commit and push the fix

This time, do **not** skip the hook. It should run the tests and let the push through.

```bash
git add app.py
git commit -m "Fix addition operator"
git push
```

**Expected result:** You see `[pre-push] All tests passed. Continuing with push.` and the push succeeds.

### 12.3 Merge the Pull Request

1. Go back to your Pull Request on GitHub. It updates automatically with your new commit.
2. Wait for the `test` check to turn into a green ✅.
3. Click **Merge pull request**.
4. Click **Confirm merge**.
5. (Optional) Click **Delete branch** to remove the feature branch from GitHub.

### 12.4 Confirm CI passed on `main`

Open the **Actions** tab. The merge triggers a new run on `main`. Wait for the green ✅.

---

## Step 13: Update Your Local `main` Branch

Your local `main` does not know about the merge yet. Switch to it, download the latest changes, and delete the finished feature branch:

```bash
git switch main
git pull origin main
git branch -d feature/update-logic
```

**Checkpoint:** Run `git log --oneline -5`. You should see the merge commit at the top.

🎉 **You're done!** You now have a project where broken code is stopped on your computer by a pre-push hook, and on GitHub by branch protection.

---

## Troubleshooting

### `bad interpreter`, `^M`, `$'\r': command not found`, or `'sh\r': No such file or directory`

**Cause:** A script has Windows (`CRLF`) line endings and is being run by Linux or Git Bash. This usually happens when the file was saved in VS Code with **CRLF** showing in the status bar, or the repository was cloned in Windows and opened from WSL.

**Fix (choose one):**

- **In VS Code:** open `.git/hooks/pre-push`, click **CRLF** in the status bar, choose **LF**, and save.
- **In the terminal:**

  ```bash
  sed -i 's/\r$//' .git/hooks/pre-push
  ```

Then check it again. The output should be `0`:

```bash
tr -cd '\r' < .git/hooks/pre-push | wc -c
```

**To prevent it:**

- Set **Files: Eol** to `\n` in VS Code (Step 3.1).
- Check that the status bar says **LF** whenever you save a shell script such as the hook.
- Keep the `.gitattributes` file from Step 3.2 in the repository. (It protects tracked files, not the hook.)

### `code: command not found`

- **Windows:** reinstall VS Code and check **Add to PATH**, then close and reopen your terminal.
- **macOS:** in VS Code, press **Cmd+Shift+P**, run **Shell Command: Install 'code' command in PATH**, then reopen your terminal.
- **WSL:** install the **WSL** extension in VS Code on Windows, then reopen your WSL terminal.

You can always use **File** > **Open Folder** or **File** > **Open File** in VS Code instead.

### YAML errors in the Actions tab (Step 7)

The `ci.yml` indentation was probably shifted when pasting. Delete everything in the file, click line 1, paste the YAML from Step 6.2 again, and save. Then commit and push the fix.

### `python: command not found` (Git Bash)

Use `py` instead of `python`, for example `py -m pytest`. If neither works, reinstall Python from [python.org](https://www.python.org/downloads/) and check **Add python.exe to PATH**.

### `externally-managed-environment` when running `pip install` (WSL/Ubuntu)

Newer Ubuntu versions block `pip` from installing system-wide. Use the `apt` command from Step 4 instead.

### `git push` asks for a password or is rejected

Common messages: `Authentication failed`, `Support for password authentication was removed`, or `refusing to allow a Personal Access Token to create or update workflow`.

GitHub no longer accepts account passwords for Git. Use one of these instead:

- **Git Bash:** a browser sign-in window usually appears on your first push. Sign in there.
- **Personal Access Token (PAT), for WSL or anyone without the browser window:**
  1. Go to [github.com/settings/tokens](https://github.com/settings/tokens) and click **Generate new token** > **Generate new token (classic)**.
  2. Check **both** the `repo` and `workflow` scopes. Without `workflow`, GitHub rejects any push that adds or changes a file in `.github/workflows/` (like `ci.yml` in Step 7).
  3. Click **Generate token** and copy it. GitHub shows it only once.
  4. Run `git push` again. Enter your GitHub username, and paste the token when asked for a password. (Nothing appears as you paste. That is normal.)

  > Using a **fine-grained** token instead? Give it access to this repository with **Contents: Read and write** and **Workflows: Read and write**.

- **GitHub CLI (alternative):** if `gh` is installed, run `gh auth login` and follow the prompts. It sets up Git authentication for you.

### The `test` check does not appear in the branch protection search box (Step 8)

The check only appears after the workflow has run at least once. Go back to Step 7 and confirm the **Actions** run finished with a green ✅.

### `git push origin main` is rejected after Step 8

This is expected. Branch protection blocks direct pushes to `main`. Push to a feature branch and open a Pull Request instead.
