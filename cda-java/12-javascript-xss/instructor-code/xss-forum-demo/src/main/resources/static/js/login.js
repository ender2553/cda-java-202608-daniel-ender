// Log In page. Does not auto-redirect when cookies exist, so students can switch users freely.
(function () {
  var form = document.getElementById('login-form');
  var input = document.getElementById('userName');
  var button = document.getElementById('login-button');
  var fieldError = document.getElementById('userName-error');
  var formError = document.getElementById('form-error');

  function clearErrors() {
    fieldError.textContent = '';
    formError.textContent = '';
    input.removeAttribute('aria-invalid');
  }

  function showFieldError(message) {
    fieldError.textContent = message;
    input.setAttribute('aria-invalid', 'true');
    input.focus();
  }

  function resetButton() {
    button.textContent = 'Log In';
    button.disabled = false;
    input.disabled = false;
  }

  form.addEventListener('submit', function (event) {
    event.preventDefault();
    clearErrors();

    var userName = input.value; // sent untrimmed on purpose
    if (userName.trim() === '') {
      showFieldError('Enter a username.');
      return;
    }

    button.textContent = 'Logging in…';
    button.disabled = true;
    input.disabled = true;

    fetch('/api/users/login', {
      method: 'POST',
      headers: { 'Content-Type': 'application/json' },
      body: JSON.stringify({ userName: userName })
    }).then(function (response) {
      return response.json().then(function (data) {
        if (response.ok) {
          setCookie('forum_user_id', data.userId);
          setCookie('forum_user_name', data.userName);
          location.href = 'forum.html';
        } else {
          resetButton();
          if (response.status >= 500) {
            formError.textContent = 'Couldn\'t reach the forum. Is the app running?';
          } else {
            showFieldError(data.error || 'Enter a username.');
          }
        }
      });
    }).catch(function () {
      resetButton();
      formError.textContent = 'Couldn\'t reach the forum. Is the app running?';
    });
  });
})();
