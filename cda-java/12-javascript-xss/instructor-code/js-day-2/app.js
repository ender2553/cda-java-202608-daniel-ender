let submittedName = 'Ada<img src=x onerror="document.getElementById(\'marker\').textContent = \'FIRED\'">';
// submittedName = "AO";
const preview = document.getElementById("preview");

function validateName(raw) {
    if (!/^[A-Za-z0-9 ]{1,30}$/.test(raw)) {
        throw new Error("Invalid display name");
     }
        return raw;
}

function renderName(raw){
    try {
        preview.textContent = validateName(raw);
    } catch {
        preview.textContent = 'Rejected';
    }
}

renderName(submittedName);