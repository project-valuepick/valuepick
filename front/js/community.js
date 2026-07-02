const COMMUNITY_KEY = 'valuepick.community.posts';
const JOURNAL_KEY = 'valuepick.journals';

function createId() {
  if (window.crypto && typeof window.crypto.randomUUID === 'function') {
    return window.crypto.randomUUID();
  }
  return `id-${Date.now()}-${Math.floor(Math.random() * 1000000)}`;
}

function getStorageItem(key) {
  try {
    return localStorage.getItem(key);
  } catch (e) {
    return null;
  }
}

function setStorageItem(key, value) {
  try {
    localStorage.setItem(key, value);
  } catch (e) {
    // ignore storage failures for preview mode
  }
}

function createCommunitySeed() {
  return [
    {
      id: createId(),
      tab: 'free',
      title: '오늘 장 분위기 어떤가요?',
      content: '거래대금이 줄어드는데도 지수는 버티는 느낌입니다.',
      author: 'value_hunter',
      createdAt: new Date(Date.now() - 1000 * 60 * 80).toISOString(),
      comments: 4,
    },
    {
      id: createId(),
      tab: 'discussion',
      stockName: '삼성전자',
      title: '메모리 업황 회복 타이밍 토론',
      content: 'HBM 수요가 실적에 본격 반영되는 시점을 어떻게 보시나요?',
      author: 'alpha_park',
      createdAt: new Date(Date.now() - 1000 * 60 * 160).toISOString(),
      comments: 12,
    },
  ];
}

function loadCommunityPosts() {
  const raw = getStorageItem(COMMUNITY_KEY);
  if (!raw) {
    const seed = createCommunitySeed();
    setStorageItem(COMMUNITY_KEY, JSON.stringify(seed));
    return seed;
  }
  try {
    return JSON.parse(raw);
  } catch (e) {
    return [];
  }
}

function saveCommunityPosts(items) {
  setStorageItem(COMMUNITY_KEY, JSON.stringify(items));
}

function loadSharedJournals() {
  const raw = getStorageItem(JOURNAL_KEY);
  if (!raw) return [];
  try {
    const journals = JSON.parse(raw);
    return journals.filter((item) => item.isShared);
  } catch (e) {
    return [];
  }
}

function formatDateShort(value) {
  return new Date(value).toLocaleString('ko-KR', {
    month: '2-digit',
    day: '2-digit',
    hour: '2-digit',
    minute: '2-digit',
  });
}

document.addEventListener('DOMContentLoaded', () => {
  initHeader('community');

  let posts = loadCommunityPosts();
  let tab = 'free';
  let query = '';

  const listEl = document.getElementById('communityList');
  const tabsEl = document.getElementById('communityTabs');
  const searchEl = document.getElementById('communitySearchInput');

  const modalEl = document.getElementById('postModal');
  const openBtn = document.getElementById('openPostModal');
  const closeBtn = document.getElementById('closePostModal');
  const cancelBtn = document.getElementById('cancelPost');
  const formEl = document.getElementById('postForm');
  const stockField = document.getElementById('stockField');
  const tabSelectEl = formEl.elements.tab;

  function renderJournalTab() {
    const shared = loadSharedJournals().filter((item) => {
      if (!query) return true;
      const merged = `${item.title} ${item.stockName} ${item.content}`.toLowerCase();
      return merged.includes(query);
    });

    if (shared.length === 0) {
      listEl.innerHTML = '<div class="empty">공유된 투자일지가 아직 없습니다.</div>';
      return;
    }

    listEl.innerHTML = shared
      .map(
        (item) => `
          <article class="post-card">
            <div class="post-card-head">
              <div>
                <div class="post-title">${item.title}</div>
                <div class="meta">${item.stockName} · ${item.category} · ${formatDateShort(item.createdAt)}</div>
              </div>
            </div>
            <p class="post-body">${item.content}</p>
            <div class="post-actions">
              <a class="link-btn" href="investment-journal.html">원문 보기</a>
              <div class="vote-inline">
                <button type="button" data-vote="up">추천</button>
                <button type="button" data-vote="down">비추천</button>
              </div>
            </div>
          </article>
        `
      )
      .join('');
  }

  function renderCommunityTab() {
    const visible = posts
      .filter((item) => item.tab === tab)
      .filter((item) => {
        if (!query) return true;
        const merged = `${item.title} ${item.content} ${item.author} ${item.stockName || ''}`.toLowerCase();
        return merged.includes(query);
      })
      .sort((a, b) => new Date(b.createdAt) - new Date(a.createdAt));

    if (visible.length === 0) {
      listEl.innerHTML = '<div class="empty">조건에 맞는 글이 없습니다.</div>';
      return;
    }

    listEl.innerHTML = visible
      .map((item) => {
        const extra = item.stockName ? ` · ${item.stockName}` : '';
        return `
          <article class="post-card">
            <div class="post-card-head">
              <div>
                <div class="post-title">${item.title}</div>
                <div class="meta">${item.author}${extra} · ${formatDateShort(item.createdAt)} · 댓글 ${item.comments}</div>
              </div>
            </div>
            <p class="post-body">${item.content}</p>
            <div class="post-actions">
              <a class="link-btn" href="community-detail.html?id=${encodeURIComponent(item.id)}">상세 보기</a>
            </div>
          </article>
        `;
      })
      .join('');
  }

  function render() {
    if (tab === 'journal') {
      renderJournalTab();
      return;
    }
    renderCommunityTab();
  }

  function openModal() {
    modalEl.classList.add('open');
    modalEl.setAttribute('aria-hidden', 'false');
  }

  function closeModal() {
    modalEl.classList.remove('open');
    modalEl.setAttribute('aria-hidden', 'true');
    formEl.reset();
    stockField.classList.add('hidden');
  }

  tabsEl.addEventListener('click', (e) => {
    const target = e.target.closest('button[data-tab]');
    if (!target) return;
    tabsEl.querySelectorAll('.tab').forEach((btn) => btn.classList.remove('active'));
    target.classList.add('active');
    tab = target.dataset.tab;
    render();
  });

  searchEl.addEventListener('input', () => {
    query = searchEl.value.trim().toLowerCase();
    render();
  });

  tabSelectEl.addEventListener('change', () => {
    const isDiscussion = tabSelectEl.value === 'discussion';
    stockField.classList.toggle('hidden', !isDiscussion);
    formEl.elements.stockName.required = isDiscussion;
  });

  openBtn.addEventListener('click', openModal);
  closeBtn.addEventListener('click', closeModal);
  cancelBtn.addEventListener('click', closeModal);
  modalEl.addEventListener('click', (e) => {
    if (e.target === modalEl) closeModal();
  });

  formEl.addEventListener('submit', (e) => {
    e.preventDefault();
    const data = new FormData(formEl);
    const next = {
      id: createId(),
      tab: String(data.get('tab')),
      stockName: String(data.get('stockName') || '').trim(),
      title: String(data.get('title') || '').trim(),
      content: String(data.get('content') || '').trim(),
      author: 'me',
      comments: 0,
      createdAt: new Date().toISOString(),
    };
    posts.unshift(next);
    saveCommunityPosts(posts);
    closeModal();
    render();
  });

  render();
});
