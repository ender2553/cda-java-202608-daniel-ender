// Feedback widget behavior.
// Event listeners replace the blocked inline onclick attributes.

function submitFeedback() {
  document.getElementById('log').textContent = 'Feedback submitted.';
}

function resetFeedback() {
  document.getElementById('log').textContent = '(waiting for action)';
}

document.getElementById('submit-btn').addEventListener('click', submitFeedback);
document.getElementById('reset-btn').addEventListener('click', resetFeedback);