// Reset page. Shows and loads no stored data, so no payload can run here. The only request is POST /api/reset.
(function () {
  var button = document.getElementById('reset-button');
  var formError = document.getElementById('form-error');

  function fail() {
    button.textContent = 'Reset demo';
    button.disabled = false;
    formError.textContent = 'Reset failed. Re-run schema.sql with psql (see the README).';
  }

  button.addEventListener('click', function () {
    formError.textContent = '';
    button.textContent = 'Resetting…';
    button.disabled = true;

    fetch('/api/reset', { method: 'POST' }).then(function (response) {
      if (response.status === 204) {
        clearCookies();
        location.href = 'index.html';
      } else {
        fail();
      }
    }).catch(fail);
  });
})();
