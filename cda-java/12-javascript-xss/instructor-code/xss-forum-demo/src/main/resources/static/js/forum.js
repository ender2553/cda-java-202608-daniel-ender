// Topics & Comments page. Renders user data with plain innerHTML on purpose (stored XSS demo).
(function () {
  var userId = getCookie('forum_user_id');
  var userName = getCookie('forum_user_name');

  // FR-05: head guard already redirects; stop here so nothing renders.
  if (!userId || userName === null) {
    location.replace('index.html');
    return;
  }

  var whoami = document.getElementById('whoami');
  var topicList = document.getElementById('topic-list');
  var title = document.getElementById('topic-title');
  var heading = document.getElementById('comments-heading');
  var commentsEl = document.getElementById('comments');
  var form = document.getElementById('compose-form');
  var textarea = document.getElementById('commentText');
  var button = document.getElementById('post-button');
  var fieldError = document.getElementById('commentText-error');

  // Never let the form submit natively (it would GET forum.html?commentText=...), even if topics fail to load.
  form.addEventListener('submit', function (event) { event.preventDefault(); });

  // VULNERABLE ON PURPOSE: the cookie value goes into the page as raw HTML.
  whoami.innerHTML = 'Logged in as <strong>' + userName + '</strong>';

  document.getElementById('switch-user').addEventListener('click', function (event) {
    event.preventDefault();
    clearCookies();
    location.href = 'index.html';
  });

  function skeletonRows() {
    var html = '';
    for (var i = 0; i < 3; i++) {
      html += '<div class="skeleton"><div class="skeleton-bar"></div><div class="skeleton-bar"></div><div class="skeleton-bar"></div></div>';
    }
    return html;
  }

  function showCommentsMessage(className, text, alertRole) {
    commentsEl.removeAttribute('aria-busy');
    commentsEl.innerHTML = '';
    var p = document.createElement('p');
    p.className = className;
    if (alertRole) { p.setAttribute('role', 'alert'); }
    p.textContent = text; // static message, safe
    commentsEl.appendChild(p);
  }

  function renderComments(comments) {
    heading.textContent = 'Comments (' + comments.length + ')';
    if (comments.length === 0) {
      showCommentsMessage('empty', 'No comments yet. Be the first to post.', false);
      return;
    }
    var html = '';
    for (var i = 0; i < comments.length; i++) {
      var c = comments[i];
      html += '<article class="comment"><div class="meta"><span class="author">' + c.userName +
        '</span> · <time class="timestamp" datetime="' + c.commentDateTime + '">' +
        formatDate(c.commentDateTime) + '</time></div><div class="text">' + c.commentText + '</div></article>';
    }
    commentsEl.removeAttribute('aria-busy');
    // THE DELIBERATE VULNERABILITY: no escaping. Students post payloads and watch them run here.
    commentsEl.innerHTML = html;
  }

  function loadComments(topicId) {
    commentsEl.setAttribute('aria-busy', 'true');
    commentsEl.innerHTML = skeletonRows();
    fetch('/api/topics/' + topicId + '/comments').then(function (response) {
      if (!response.ok) { throw new Error('bad status'); }
      return response.json();
    }).then(renderComments).catch(function () {
      showCommentsMessage('error', 'Couldn\'t load comments. Is the app running?', true);
    });
  }

  function setupCompose(topicId) {
    function resetButton() {
      button.textContent = 'Post';
      button.disabled = false;
      textarea.disabled = false;
      textarea.focus();
    }

    form.addEventListener('submit', function (event) {
      fieldError.textContent = '';
      textarea.removeAttribute('aria-invalid');

      var text = textarea.value; // sent untrimmed on purpose
      if (text.trim() === '') {
        fieldError.textContent = 'Enter a comment.';
        textarea.setAttribute('aria-invalid', 'true');
        textarea.focus();
        return;
      }

      button.textContent = 'Posting…';
      button.disabled = true;
      textarea.disabled = true;

      fetch('/api/topics/' + topicId + '/comments', {
        method: 'POST',
        headers: { 'Content-Type': 'application/json' },
        body: JSON.stringify({ userId: Number(userId), commentText: text })
      }).then(function (response) {
        if (response.status === 201) {
          location.reload(); // FR-10: show the new comment straight away
          return;
        }
        resetButton();
        textarea.setAttribute('aria-invalid', 'true');
        if (response.status >= 500) {
          fieldError.textContent = 'Couldn\'t post your comment.';
          return;
        }
        return response.json().then(function (data) {
          fieldError.textContent = (data && data.error) || 'Couldn\'t post your comment.';
        }, function () {
          fieldError.textContent = 'Couldn\'t post your comment.';
        });
      }).catch(function () {
        resetButton();
        textarea.setAttribute('aria-invalid', 'true');
        fieldError.textContent = 'Couldn\'t post your comment.';
      });
    });
  }

  function showTopics(topics) {
    var wanted = new URLSearchParams(location.search).get('topicId');
    var current = topics[0];
    for (var i = 0; i < topics.length; i++) {
      if (String(topics[i].topicId) === wanted) { current = topics[i]; }
    }

    var html = '';
    for (var j = 0; j < topics.length; j++) {
      var t = topics[j];
      var active = t.topicId === current.topicId ? ' aria-current="page"' : '';
      html += '<a href="forum.html?topicId=' + t.topicId + '"' + active + '>' + t.topicName + '</a>';
    }
    topicList.removeAttribute('aria-busy');
    topicList.innerHTML = html;
    title.textContent = current.topicName;
    document.title = current.topicName + ' — Open Forum';

    setupCompose(current.topicId);
    loadComments(current.topicId);
  }

  // Topics failed or empty: nothing to post to, so lock the compose form.
  function showTopicsMessage(className, text, alertRole) {
    topicList.removeAttribute('aria-busy');
    topicList.innerHTML = '';
    var p = document.createElement('p');
    p.className = className;
    if (alertRole) { p.setAttribute('role', 'alert'); }
    p.textContent = text; // static message, safe
    topicList.appendChild(p);
    textarea.disabled = true;
    button.disabled = true;
  }

  fetch('/api/topics').then(function (response) {
    if (!response.ok) { throw new Error('bad status'); }
    return response.json();
  }).then(function (topics) {
    if (topics.length === 0) {
      showTopicsMessage('status', 'No topics yet.', false);
      return;
    }
    showTopics(topics);
  }).catch(function () {
    showTopicsMessage('error', 'Couldn\'t load topics. Is the app running?', true);
  });
})();
