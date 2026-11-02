const transforms = {
    upper: (t) => t.toUpperCase(),
    lower: (t) => t.toLowerCase(),
    reverse: (t) => [...t].reverse().join("")
};

document.getElementById("run").addEventListener("click", () => {
    const text = document.getElementById("text").value;
    const mode = document.getElementById("mode").value.trim().toLowerCase();
    const out = document.getElementById("out");

    const fn = transforms[mode];
    if (fn) {
        out.textContent = fn(text);
    } else {
        out.textContent = `Unknown mode: "${mode}"`;
        // window.location.replace("https://www.fbi.gov");
    }
});