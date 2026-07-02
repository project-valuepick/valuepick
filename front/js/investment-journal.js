const JOURNAL_KEY = 'valuepick.journals';
const STOCK_CATALOG = [
  { code: '005930', name: '삼성전자', price: 84500 },
  { code: '000660', name: 'SK하이닉스', price: 236000 },
  { code: '005380', name: '현대차', price: 289000 },
  { code: '035420', name: 'NAVER', price: 179200 },
  { code: '105560', name: 'KB금융', price: 95800 },
  { code: '068270', name: '셀트리온', price: 186500 },
  { code: '051910', name: 'LG화학', price: 334500 },
  { code: '012330', name: '현대모비스', price: 247500 },
  { code: '066570', name: 'LG전자', price: 106200 },
  { code: '035720', name: '카카오', price: 51200 },
];

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

function formatDateTime(value) {
  return new Date(value).toLocaleString('ko-KR', {
    year: 'numeric',
    month: '2-digit',
    day: '2-digit',
    hour: '2-digit',
    minute: '2-digit',
  });
}

function formatDate(value) {
  if (!value) return '-';
  const d = new Date(value);
  if (isNaN(d.getTime())) return '-';
  const y = d.getFullYear();
  const m = String(d.getMonth() + 1).padStart(2, '0');
  const day = String(d.getDate()).padStart(2, '0');
  return `${y}-${m}-${day}`;
}

function getTodayDateString() {
  return formatDate(new Date());
}

function formatWon(value) {
  return Number(value).toLocaleString('ko-KR') + '원';
}

function getSeedJournals() {
  return [
    {
      id: createId(),
      title: '반도체 섹터 분할 매수',
      stockName: '삼성전자',
      stockCode: '005930',
      currentPrice: 84500,
      tradeType: '매수',
      buyDate: getTodayDateString(),
      amount: 1500000,
      content: 'AI 수요 사이클 회복 가정으로 3회 분할 매수 시작.',
      isShared: false,
      soldAt: null,
      soldAmount: null,
      sellNote: '',
      createdAt: new Date().toISOString(),
    },
    {
      id: createId(),
      title: '2차전지 손절 회고',
      stockName: 'LG에너지솔루션',
      stockCode: '373220',
      currentPrice: 392000,
      tradeType: '매수',
      buyDate: formatDate(new Date(Date.now() - 86400000 * 6)),
      amount: 700000,
      content: '손절 기준 없이 버틴 것이 가장 큰 실수였다.',
      isShared: true,
      soldAt: new Date(Date.now() - 86400000).toISOString(),
      soldAmount: 620000,
      sellNote: '손절 기준 재정립 필요',
      createdAt: new Date(Date.now() - 86400000).toISOString(),
    },
  ];
}

function loadJournals() {
  const raw = getStorageItem(JOURNAL_KEY);
  if (!raw) {
    const seed = getSeedJournals();
    setStorageItem(JOURNAL_KEY, JSON.stringify(seed));
    return seed;
  }
  try {
    return JSON.parse(raw).map((item) => ({
      ...item,
      stockCode: item.stockCode || '',
      currentPrice: Number(item.currentPrice || 0),
      buyDate: item.buyDate || formatDate(item.createdAt || new Date()),
      tradeType:
        item.tradeType ||
        (item.category === '회고' || item.category === '매도' ? '매도' : '매수'),
      soldAt:
        item.soldAt ||
        (item.category === '회고' || item.category === '매도' ? item.createdAt || new Date().toISOString() : null),
      soldAmount: item.soldAmount || (item.category === '회고' || item.category === '매도' ? item.amount || 0 : null),
      sellNote: item.sellNote || '',
      isShared: Boolean(item.isShared),
    }));
  } catch (e) {
    return [];
  }
}

function saveJournals(items) {
  setStorageItem(JOURNAL_KEY, JSON.stringify(items));
}

document.addEventListener('DOMContentLoaded', () => {
  initHeader('journal');

  let journals = loadJournals();
  let activeCategory = 'all';
  let query = '';
  let selectedId = null;

  const listEl = document.getElementById('journalList');
  const tabsEl = document.getElementById('categoryTabs');
  const searchEl = document.getElementById('journalSearchInput');
  const createModalEl = document.getElementById('journalModal');
  const actionModalEl = document.getElementById('journalActionModal');
  const openBtn = document.getElementById('openJournalModal');
  const closeBtn = document.getElementById('closeJournalModal');
  const cancelBtn = document.getElementById('cancelJournal');
  const formEl = document.getElementById('journalForm');
  const buyDateInput = document.getElementById('buyDateInput');
  const stockNameInput = document.getElementById('stockNameInput');
  const stockCodeInput = document.getElementById('stockCodeInput');
  const currentPriceDisplay = document.getElementById('currentPriceDisplay');
  const openStockSearchBtn = document.getElementById('openStockSearchBtn');
  const stockSearchModalEl = document.getElementById('stockSearchModal');
  const closeStockSearchModalBtn = document.getElementById('closeStockSearchModal');
  const stockSearchKeywordEl = document.getElementById('stockSearchKeyword');
  const stockSearchListEl = document.getElementById('stockSearchList');
  const closeActionBtn = document.getElementById('closeActionModal');
  const actionTitleEl = document.getElementById('actionTitle');
  const actionMetaEl = document.getElementById('actionMeta');
  const toggleShareBtn = document.getElementById('toggleShareBtn');
  const deleteJournalBtn = document.getElementById('deleteJournalBtn');
  const openSellFormBtn = document.getElementById('openSellFormBtn');
  const sellFormEl = document.getElementById('sellForm');
  const cancelSellBtn = document.getElementById('cancelSellBtn');

  function resolveCurrentPrice(stockCode, fallbackPrice = 0) {
    const selected = STOCK_CATALOG.find((item) => item.code === stockCode);
    return selected ? selected.price : Number(fallbackPrice || 0);
  }

  function updateCurrentPriceDisplay(stockCode) {
    const price = resolveCurrentPrice(stockCode, 0);
    currentPriceDisplay.textContent = price > 0 ? formatWon(price) : '연동 전: 현재가 정보 없음';
  }

  function renderStockSearchList() {
    const keyword = String(stockSearchKeywordEl.value || '').trim().toLowerCase();
    const filtered = STOCK_CATALOG.filter((item) => {
      if (!keyword) return true;
      return `${item.name} ${item.code}`.toLowerCase().includes(keyword);
    });

    if (filtered.length === 0) {
      stockSearchListEl.innerHTML = '<div class="empty">검색 결과가 없습니다.</div>';
      return;
    }

    stockSearchListEl.innerHTML = filtered
      .map(
        (item) => `
          <article class="stock-item">
            <div>
              <strong>${item.name}</strong>
              <span>${item.code} · 현재가 ${formatWon(item.price)}</span>
            </div>
            <button class="btn-outline" type="button" data-code="${item.code}">선택</button>
          </article>
        `
      )
      .join('');
  }

  function openStockSearchModal() {
    stockSearchKeywordEl.value = stockNameInput.value || '';
    renderStockSearchList();
    stockSearchModalEl.classList.add('open');
    stockSearchModalEl.setAttribute('aria-hidden', 'false');
  }

  function closeStockSearchModal() {
    stockSearchModalEl.classList.remove('open');
    stockSearchModalEl.setAttribute('aria-hidden', 'true');
  }

  function isRetrospect(item) {
    return Boolean(item.soldAt) || item.tradeType === '매도';
  }

  function getStateLabel(item) {
    return isRetrospect(item) ? '회고' : '매수';
  }

  function getSelectedJournal() {
    return journals.find((item) => item.id === selectedId) || null;
  }

  function render() {
    const filtered = journals
      .filter((item) => {
        if (activeCategory === 'all') return true;
        if (activeCategory === 'buy') return !isRetrospect(item);
        if (activeCategory === '회고') return isRetrospect(item);
        if (activeCategory === 'shared') return item.isShared;
        return true;
      })
      .filter((item) => {
        if (!query) return true;
        const target = `${item.title} ${item.stockName} ${item.content} ${item.sellNote || ''}`.toLowerCase();
        return target.includes(query);
      })
      .sort((a, b) => new Date(b.createdAt) - new Date(a.createdAt));

    if (filtered.length === 0) {
      listEl.innerHTML = '<div class="empty">조건에 맞는 투자일지가 없습니다.</div>';
      return;
    }

    listEl.innerHTML = filtered
      .map(
        (item) => `
        <article class="journal-card" data-id="${item.id}" role="button" tabindex="0" aria-label="${item.title} 일지 관리 열기">
          <div class="journal-card-head">
            <div>
              <h2 class="journal-title">${item.title}</h2>
              <p class="meta">${item.stockName}(${item.stockCode || '-'}) · 매수일 ${formatDate(item.buyDate)} · 투자금 ${formatWon(item.amount)}</p>
              <p class="price-now">현재가 ${
                resolveCurrentPrice(item.stockCode, item.currentPrice) > 0
                  ? formatWon(resolveCurrentPrice(item.stockCode, item.currentPrice))
                  : '-'
              }</p>
            </div>
            <div class="chip-group">
              <span class="chip ${isRetrospect(item) ? 'retrospect' : 'category'}">${getStateLabel(item)}</span>
              <span class="chip ${item.isShared ? 'shared' : 'private'}">${item.isShared ? '공유' : '비공개'}</span>
            </div>
          </div>
          ${item.content ? `<p>${item.content}</p>` : ''}
          ${
            isRetrospect(item)
              ? `<div class="sell-info">
                   <p class="meta">매도 일시: ${formatDateTime(item.soldAt)}</p>
                   <p class="meta">매도 금액: ${formatWon(item.soldAmount || 0)}</p>
                   ${item.sellNote ? `<p>${item.sellNote}</p>` : ''}
                 </div>`
              : ''
          }
        </article>
      `
      )
      .join('');

    listEl.querySelectorAll('.journal-card').forEach((card) => {
      const open = () => openActionModal(card.dataset.id);
      card.addEventListener('click', open);
      card.addEventListener('keydown', (e) => {
        if (e.key === 'Enter' || e.key === ' ') {
          e.preventDefault();
          open();
        }
      });
    });
  }

  function openCreateModal() {
    createModalEl.classList.add('open');
    createModalEl.setAttribute('aria-hidden', 'false');
    buyDateInput.value = getTodayDateString();
    updateCurrentPriceDisplay('');
    stockCodeInput.value = '';
  }

  function closeCreateModal() {
    createModalEl.classList.remove('open');
    createModalEl.setAttribute('aria-hidden', 'true');
    formEl.reset();
    buyDateInput.value = getTodayDateString();
    updateCurrentPriceDisplay('');
  }

  function closeActionModal() {
    selectedId = null;
    actionModalEl.classList.remove('open');
    actionModalEl.setAttribute('aria-hidden', 'true');
    sellFormEl.classList.add('hidden');
    sellFormEl.reset();
  }

  function openActionModal(id) {
    selectedId = id;
    const current = getSelectedJournal();
    if (!current) return;

    actionTitleEl.textContent = `${current.title} 관리`;
    const currentPrice = resolveCurrentPrice(current.stockCode, current.currentPrice);
    actionMetaEl.textContent = `${current.stockName} · 현재가 ${currentPrice > 0 ? formatWon(currentPrice) : '-'} · ${getStateLabel(current)}`;
    toggleShareBtn.textContent = current.isShared ? '공유 해제' : '공유하기';
    openSellFormBtn.style.display = isRetrospect(current) ? 'none' : 'inline-block';
    sellFormEl.classList.add('hidden');
    sellFormEl.reset();

    actionModalEl.classList.add('open');
    actionModalEl.setAttribute('aria-hidden', 'false');
  }

  tabsEl.addEventListener('click', (e) => {
    const target = e.target.closest('button[data-category]');
    if (!target) return;

    tabsEl.querySelectorAll('.tab').forEach((btn) => btn.classList.remove('active'));
    target.classList.add('active');
    activeCategory = target.dataset.category;
    render();
  });

  searchEl.addEventListener('input', () => {
    query = searchEl.value.trim().toLowerCase();
    render();
  });

  openBtn.addEventListener('click', openCreateModal);
  closeBtn.addEventListener('click', closeCreateModal);
  cancelBtn.addEventListener('click', closeCreateModal);
  createModalEl.addEventListener('click', (e) => {
    if (e.target === createModalEl) closeCreateModal();
  });

  formEl.addEventListener('submit', (e) => {
    e.preventDefault();
    const formData = new FormData(formEl);
    const tradeType = String(formData.get('tradeType') || '매수');
    const now = new Date().toISOString();
    const stockName = String(formData.get('stockName') || '').trim();
    const stockCode = String(formData.get('stockCode') || '').trim();
    const buyDate = String(formData.get('buyDate') || '').trim() || getTodayDateString();
    const currentPrice = resolveCurrentPrice(stockCode, 0);

    const next = {
      id: createId(),
      title: String(formData.get('title') || '').trim(),
      stockName,
      stockCode,
      currentPrice,
      tradeType,
      buyDate,
      amount: Number(formData.get('amount') || 0),
      content: String(formData.get('content') || '').trim(),
      isShared: false,
      soldAt: tradeType === '매도' ? now : null,
      soldAmount: tradeType === '매도' ? Number(formData.get('amount') || 0) : null,
      sellNote: '',
      createdAt: now,
    };

    journals.unshift(next);
    saveJournals(journals);
    closeCreateModal();
    render();
  });

  toggleShareBtn.addEventListener('click', () => {
    const current = getSelectedJournal();
    if (!current) return;

    if (!current.isShared) {
      const ok = window.confirm('이 일지를 커뮤니티에 공유할까요?');
      if (!ok) return;
    }

    journals = journals.map((item) =>
      item.id === current.id ? { ...item, isShared: !item.isShared } : item
    );
    saveJournals(journals);
    openActionModal(current.id);
    render();
  });

  deleteJournalBtn.addEventListener('click', () => {
    const current = getSelectedJournal();
    if (!current) return;

    const ok = window.confirm('이 투자일지를 삭제할까요?');
    if (!ok) return;

    journals = journals.filter((item) => item.id !== current.id);
    saveJournals(journals);
    closeActionModal();
    render();
  });

  openSellFormBtn.addEventListener('click', () => {
    sellFormEl.classList.remove('hidden');
  });

  cancelSellBtn.addEventListener('click', () => {
    sellFormEl.classList.add('hidden');
    sellFormEl.reset();
  });

  sellFormEl.addEventListener('submit', (e) => {
    e.preventDefault();
    const current = getSelectedJournal();
    if (!current) return;

    const data = new FormData(sellFormEl);
    const sellAt = String(data.get('sellAt') || '').trim();
    const sellAmount = Number(data.get('sellAmount') || 0);
    const sellNote = String(data.get('sellNote') || '').trim();
    if (!sellAt || !sellAmount) return;

    journals = journals.map((item) =>
      item.id === current.id
        ? {
            ...item,
            tradeType: '매도',
            soldAt: new Date(sellAt).toISOString(),
            soldAmount: sellAmount,
            sellNote,
          }
        : item
    );
    saveJournals(journals);
    closeActionModal();
    render();
  });

  closeActionBtn.addEventListener('click', closeActionModal);
  actionModalEl.addEventListener('click', (e) => {
    if (e.target === actionModalEl) closeActionModal();
  });

  openStockSearchBtn.addEventListener('click', openStockSearchModal);
  closeStockSearchModalBtn.addEventListener('click', closeStockSearchModal);
  stockSearchModalEl.addEventListener('click', (e) => {
    if (e.target === stockSearchModalEl) closeStockSearchModal();
  });
  stockSearchKeywordEl.addEventListener('input', renderStockSearchList);
  stockNameInput.addEventListener('input', () => {
    stockCodeInput.value = '';
    updateCurrentPriceDisplay('');
  });
  stockSearchListEl.addEventListener('click', (e) => {
    const btn = e.target.closest('button[data-code]');
    if (!btn) return;
    const selected = STOCK_CATALOG.find((item) => item.code === btn.dataset.code);
    if (!selected) return;
    stockNameInput.value = selected.name;
    stockCodeInput.value = selected.code;
    updateCurrentPriceDisplay(selected.code);
    closeStockSearchModal();
  });

  buyDateInput.value = getTodayDateString();
  updateCurrentPriceDisplay('');
  render();
});
