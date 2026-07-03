// 백엔드 테이블 설계에 맞춘 3개 테이블 구조를 로컬스토리지로 모사한다.
// 매수(buys) / 매도(sells): 개별 거래 기록(주가 × 수량), positionId(참조id)로 내역에 연결
// 내역(positions): 참조id가 곧 자기 id이며, 보유주가 0이 되는 순간 상태가 "완료"로 취급된다.
const BUY_KEY = 'valuepick.buys.v3';
const SELL_KEY = 'valuepick.sells.v3';
const POSITION_KEY = 'valuepick.positions.v3';
const USER_KEY = 'valuepick.userId';

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

function getCurrentUserId() {
  let uid = getStorageItem(USER_KEY);
  if (!uid) {
    uid = createId();
    setStorageItem(USER_KEY, uid);
  }
  return uid;
}

function formatDateTime(value) {
  if (!value) return '-';
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

function formatShortDate(value) {
  if (!value) return '-';
  const d = new Date(value);
  if (isNaN(d.getTime())) return '-';
  const y = String(d.getFullYear()).slice(-2);
  const m = String(d.getMonth() + 1).padStart(2, '0');
  const day = String(d.getDate()).padStart(2, '0');
  return `${y}.${m}.${day}`;
}

function getNowDateTimeLocal() {
  const d = new Date();
  const y = d.getFullYear();
  const m = String(d.getMonth() + 1).padStart(2, '0');
  const day = String(d.getDate()).padStart(2, '0');
  const h = String(d.getHours()).padStart(2, '0');
  const min = String(d.getMinutes()).padStart(2, '0');
  return `${y}-${m}-${day}T${h}:${min}`;
}

function formatWon(value) {
  return Number(value || 0).toLocaleString('ko-KR') + '원';
}

function formatShares(value) {
  return Number(value || 0).toLocaleString('ko-KR') + '주';
}

function getSeedData() {
  const userId = getCurrentUserId();
  const holdPositionId = createId();
  const dealPositionId = createId();
  const holdBuyAt = getNowDateTimeLocal();
  const dealBuyAt = new Date(Date.now() - 86400000 * 6).toISOString();
  const dealSellAt = new Date(Date.now() - 86400000).toISOString();

  const buys = [
    {
      id: createId(),
      title: '반도체 섹터 분할 매수',
      stockCode: '005930',
      stockName: '삼성전자',
      buyAt: holdBuyAt,
      price: 88235,
      quantity: 17,
      positionId: holdPositionId,
      userId,
      isShared: false,
      createdAt: new Date().toISOString(),
    },
    {
      id: createId(),
      title: '2차전지 손절 회고',
      stockCode: '373220',
      stockName: 'LG에너지솔루션',
      buyAt: dealBuyAt,
      price: 350000,
      quantity: 2,
      positionId: dealPositionId,
      userId,
      isShared: true,
      createdAt: dealBuyAt,
    },
  ];

  const sells = [
    {
      id: createId(),
      title: '2차전지 손절',
      stockCode: '373220',
      stockName: 'LG에너지솔루션',
      sellAt: dealSellAt,
      price: 310000,
      quantity: 2,
      positionId: dealPositionId,
      userId,
      isShared: true,
      createdAt: dealSellAt,
    },
  ];

  const positions = [
    {
      id: holdPositionId,
      title: '반도체 섹터 분할 매수',
      stockCode: '005930',
      stockName: '삼성전자',
      priceFallback: 84500,
      firstBuyAt: holdBuyAt,
      finalSellAt: holdBuyAt,
      note: 'AI 수요 사이클 회복 가정으로 3회 분할 매수 시작.',
      userId,
      isShared: false,
      createdAt: new Date().toISOString(),
    },
    {
      id: dealPositionId,
      title: '2차전지 손절 회고',
      stockCode: '373220',
      stockName: 'LG에너지솔루션',
      priceFallback: 392000,
      firstBuyAt: dealBuyAt,
      finalSellAt: dealSellAt,
      note: '손절 기준 없이 버틴 것이 가장 큰 실수였다. 손절 기준 재정립 필요.',
      userId,
      isShared: true,
      createdAt: dealBuyAt,
    },
  ];

  return { buys, sells, positions };
}

function loadTable(key, fallback) {
  const raw = getStorageItem(key);
  if (!raw) return fallback;
  try {
    const parsed = JSON.parse(raw);
    return Array.isArray(parsed) ? parsed : fallback;
  } catch (e) {
    return fallback;
  }
}

// 예전에 저장된 내역에는 finalSellAt이 없을 수 있으므로, 없으면 매도 기록(완료) 또는 최초 매수 시간(보유)에서 보정한다.
function migratePositionFinalSellAt(positions, sells) {
  return positions.map((p) => {
    if (p.finalSellAt) return p;
    const relatedSells = sells.filter((s) => s.positionId === p.id);
    const derived = relatedSells.reduce(
      (latest, s) => (!latest || new Date(s.sellAt) > new Date(latest) ? s.sellAt : latest),
      null
    );
    return { ...p, finalSellAt: derived || p.firstBuyAt };
  });
}

function loadAll() {
  const rawBuys = getStorageItem(BUY_KEY);
  if (!rawBuys) {
    const seed = getSeedData();
    setStorageItem(BUY_KEY, JSON.stringify(seed.buys));
    setStorageItem(SELL_KEY, JSON.stringify(seed.sells));
    setStorageItem(POSITION_KEY, JSON.stringify(seed.positions));
    return seed;
  }
  const buys = loadTable(BUY_KEY, []);
  const sells = loadTable(SELL_KEY, []);
  const positions = migratePositionFinalSellAt(loadTable(POSITION_KEY, []), sells);
  setStorageItem(POSITION_KEY, JSON.stringify(positions));
  return { buys, sells, positions };
}

document.addEventListener('DOMContentLoaded', () => {
  initHeader('journal');

  const initial = loadAll();
  let buys = initial.buys;
  let sells = initial.sells;
  let positions = initial.positions;
  let activeCategory = 'all';
  let query = '';
  let detailStack = [];
  let currentDetail = null; // { kind: 'buy' | 'sell' | 'position', id }

  const PAGE_SIZE = 10;
  let currentPage = 0;

  const RECORD_PAGE_SIZE = 5;
  let buyRecordsPage = 0;
  let sellRecordsPage = 0;
  let lastPositionDetailId = null;

  function getDefaultDateRange() {
    const today = new Date();
    const from = new Date(today.getFullYear(), today.getMonth() - 1, today.getDate());
    return { from: formatDate(from), to: formatDate(today) };
  }

  const defaultRange = getDefaultDateRange();
  let fromDate = defaultRange.from;
  let toDate = defaultRange.to;

  const listEl = document.getElementById('journalList');
  const tableBodyEl = document.getElementById('journalTableBody');
  const tabsEl = document.getElementById('categoryTabs');
  const searchEl = document.getElementById('journalSearchInput');
  const filterFromDateEl = document.getElementById('filterFromDate');
  const filterToDateEl = document.getElementById('filterToDate');
  const journalPaginationEl = document.getElementById('journalPagination');
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
  const detailBackBtn = document.getElementById('detailBackBtn');
  const detailTitleInput = document.getElementById('detailTitleInput');
  const detailTitleSaveBtn = document.getElementById('detailTitleSaveBtn');
  const actionMetaEl = document.getElementById('actionMeta');
  const toggleShareBtn = document.getElementById('toggleShareBtn');
  const deleteJournalBtn = document.getElementById('deleteJournalBtn');
  const openBuyFormBtn = document.getElementById('openBuyFormBtn');
  const addBuyFormEl = document.getElementById('addBuyForm');
  const cancelAddBuyBtn = document.getElementById('cancelAddBuyBtn');
  const openSellFormBtn = document.getElementById('openSellFormBtn');
  const sellFormEl = document.getElementById('sellForm');
  const cancelSellBtn = document.getElementById('cancelSellBtn');

  function persistAll() {
    setStorageItem(BUY_KEY, JSON.stringify(buys));
    setStorageItem(SELL_KEY, JSON.stringify(sells));
    setStorageItem(POSITION_KEY, JSON.stringify(positions));
  }

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

  // ── 보유/거래(포지션) 파생값: 매수·매도 테이블을 참조id로 집계한다 ──
  function getPositionBuys(positionId) {
    return buys.filter((b) => b.positionId === positionId);
  }

  function getPositionSells(positionId) {
    return sells.filter((s) => s.positionId === positionId);
  }

  function getBoughtQuantity(positionId) {
    return getPositionBuys(positionId).reduce((sum, b) => sum + Number(b.quantity || 0), 0);
  }

  function getSoldQuantity(positionId) {
    return getPositionSells(positionId).reduce((sum, s) => sum + Number(s.quantity || 0), 0);
  }

  function getHoldingQuantity(positionId) {
    return getBoughtQuantity(positionId) - getSoldQuantity(positionId);
  }

  // 사용된 금액 = Σ(매수 당시 주가 × 매수주)
  function getUsedAmount(positionId) {
    return getPositionBuys(positionId).reduce((sum, b) => sum + Number(b.price || 0) * Number(b.quantity || 0), 0);
  }

  // 매도 총액 = Σ(매도 당시 주가 × 매도주)
  function getSoldAmount(positionId) {
    return getPositionSells(positionId).reduce((sum, s) => sum + Number(s.price || 0) * Number(s.quantity || 0), 0);
  }

  // 손익 = 매도 총액 - 매수 총액 (판 금액에서 산 금액을 뺀 실현/평가 손익)
  function getPnl(positionId) {
    return getSoldAmount(positionId) - getUsedAmount(positionId);
  }

  function isPositionClosed(position) {
    return getBoughtQuantity(position.id) > 0 && getHoldingQuantity(position.id) <= 0;
  }

  function getCurrentRecord() {
    if (!currentDetail) return null;
    const { kind, id } = currentDetail;
    if (kind === 'buy') return buys.find((b) => b.id === id) || null;
    if (kind === 'sell') return sells.find((s) => s.id === id) || null;
    return positions.find((p) => p.id === id) || null;
  }

  function detailExists({ kind, id }) {
    if (kind === 'buy') return buys.some((b) => b.id === id);
    if (kind === 'sell') return sells.some((s) => s.id === id);
    return positions.some((p) => p.id === id);
  }

  // 탭별로 노출할 컬럼 구성. 값은 getCellValue()가 인식하는 컬럼 키다.
  const COLUMN_SETS = {
    all: ['제목', '종목', '상태', '시간'],
    shared: ['제목', '종목', '상태', '시간'],
    buy: ['제목', '종목', '매수일', '매수주', '매수가', '공유'],
    sell: ['제목', '종목', '매도일', '매도주', '매도가', '공유'],
    hold: ['제목', '종목', '기간', '보유주', '현재시세', '평가금액', '공유'],
    deal: ['제목', '종목', '기간', '손익', '공유'],
  };

  function getColumns() {
    return COLUMN_SETS[activeCategory] || COLUMN_SETS.all;
  }

  // 활성 탭에 따라 표시할 행 목록을 구성한다.
  // kind: 'buy'(매수 테이블) | 'sell'(매도 테이블) | 'hold'(보유 중인 내역) | 'deal'(완료된 내역)
  function buildRows() {
    if (activeCategory === 'buy') return buys.map((record) => ({ kind: 'buy', record }));
    if (activeCategory === 'sell') return sells.map((record) => ({ kind: 'sell', record }));
    if (activeCategory === 'hold') {
      return positions.filter((p) => !isPositionClosed(p)).map((record) => ({ kind: 'hold', record }));
    }
    if (activeCategory === 'deal') {
      return positions.filter((p) => isPositionClosed(p)).map((record) => ({ kind: 'deal', record }));
    }

    // 전체/공유됨: 매수·매도 개별 기록과 각 내역의 현재 상태(보유 또는 거래)를 모두 합친 통합 뷰
    const all = [
      ...buys.map((record) => ({ kind: 'buy', record })),
      ...sells.map((record) => ({ kind: 'sell', record })),
      ...positions.map((p) => ({ kind: isPositionClosed(p) ? 'deal' : 'hold', record: p })),
    ];
    return activeCategory === 'shared' ? all.filter((row) => row.record.isShared) : all;
  }

  function getRowSortTime(row) {
    if (row.kind === 'buy') return new Date(row.record.buyAt).getTime();
    if (row.kind === 'sell') return new Date(row.record.sellAt).getTime();
    const p = row.record;
    return new Date(p.finalSellAt || p.firstBuyAt).getTime();
  }

  // 필터링 기준일: 매수는 매수일, 매도는 매도일, 보유·거래는 최종 매도일(최종 매도 시간).
  function getRowFilterDate(row) {
    if (row.kind === 'buy') return row.record.buyAt;
    if (row.kind === 'sell') return row.record.sellAt;
    return row.record.finalSellAt || row.record.firstBuyAt;
  }

  function matchesDateRange(row) {
    if (!fromDate && !toDate) return true;
    const d = formatDate(getRowFilterDate(row));
    if (d === '-') return true;
    if (fromDate && d < fromDate) return false;
    if (toDate && d > toDate) return false;
    return true;
  }

  function getRowFields(row) {
    const { kind, record } = row;

    if (kind === 'buy') {
      return {
        recordId: record.id,
        positionId: record.positionId,
        kind,
        title: record.title,
        stockName: record.stockName,
        stockCode: record.stockCode,
        stateLabel: '매수',
        stateClass: 'category',
        dateValue: formatDate(record.buyAt),
        periodValue: null,
        quantity: record.quantity,
        price: record.price,
        currentPrice: null,
        valuation: null,
        pnl: null,
        isShared: record.isShared,
      };
    }
    if (kind === 'sell') {
      return {
        recordId: record.id,
        positionId: record.positionId,
        kind,
        title: record.title,
        stockName: record.stockName,
        stockCode: record.stockCode,
        stateLabel: '매도',
        stateClass: 'retrospect',
        dateValue: formatDate(record.sellAt),
        periodValue: null,
        quantity: record.quantity,
        price: record.price,
        currentPrice: null,
        valuation: null,
        pnl: null,
        isShared: record.isShared,
      };
    }

    // kind === 'hold' | 'deal' — 내역(포지션) 행
    const position = record;
    const closed = kind === 'deal';
    const qty = getHoldingQuantity(position.id);
    const currentPrice = resolveCurrentPrice(position.stockCode, position.priceFallback);
    const periodEnd = position.finalSellAt || new Date().toISOString();
    return {
      recordId: position.id,
      positionId: position.id,
      kind,
      title: position.title,
      stockName: position.stockName,
      stockCode: position.stockCode,
      stateLabel: closed ? '거래' : '보유',
      stateClass: closed ? 'closed' : 'holding',
      dateValue: null,
      periodValue: `${formatShortDate(position.firstBuyAt)} ~ ${formatShortDate(periodEnd)}`,
      quantity: qty,
      price: null,
      currentPrice,
      valuation: qty * currentPrice,
      pnl: getPnl(position.id),
      isShared: position.isShared,
    };
  }

  function getCellValue(column, f) {
    switch (column) {
      case '제목':
        return `<div class="td-name">${f.title}</div>`;
      case '종목':
        return `<div class="td-name">${f.stockName}</div><div class="td-code">${f.stockCode || '-'}</div>`;
      case '상태':
        return `<span class="chip ${f.stateClass}">${f.stateLabel}</span>`;
      case '시간':
        return f.periodValue || f.dateValue || '-';
      case '매수일':
      case '매도일':
        return f.dateValue;
      case '매수주':
      case '보유주':
      case '매도주':
        return formatShares(f.quantity);
      case '매수가':
      case '매도가':
        return formatWon(f.price);
      case '기간':
        return f.periodValue;
      case '현재시세':
        return f.currentPrice > 0 ? formatWon(f.currentPrice) : '-';
      case '평가금액':
        return f.currentPrice > 0 ? formatWon(f.valuation) : '-';
      case '손익':
        return `${f.pnl >= 0 ? '+' : ''}${formatWon(f.pnl)}`;
      case '공유':
        return `<span class="chip ${f.isShared ? 'shared' : 'private'}">${f.isShared ? '공유' : '비공개'}</span>`;
      default:
        return '';
    }
  }

  function matchesQuery(row) {
    if (!query) return true;
    const { kind, record } = row;
    const extra = kind === 'hold' || kind === 'deal' ? record.note || '' : '';
    const target = `${record.title} ${record.stockName} ${extra}`.toLowerCase();
    return target.includes(query);
  }

  function renderTableHead() {
    const headRowEl = document.getElementById('journalTableHeadRow');
    headRowEl.innerHTML = getColumns()
      .map((col) => `<th>${col}</th>`)
      .join('');
  }

  function renderPagination(totalCount) {
    const totalPages = Math.ceil(totalCount / PAGE_SIZE);
    if (totalPages <= 1) {
      journalPaginationEl.innerHTML = '';
      return;
    }

    const GROUP = 10;
    const groupStart = Math.floor(currentPage / GROUP) * GROUP;
    const groupEnd = Math.min(groupStart + GROUP, totalPages);

    let html = '';
    if (currentPage > 0) html += `<button class="page-btn" data-page="${currentPage - 1}">이전</button>`;
    for (let i = groupStart; i < groupEnd; i++) {
      html += `<button class="page-btn${i === currentPage ? ' active' : ''}" data-page="${i}">${i + 1}</button>`;
    }
    if (currentPage < totalPages - 1) html += `<button class="page-btn" data-page="${currentPage + 1}">다음</button>`;

    journalPaginationEl.innerHTML = html;
    journalPaginationEl.querySelectorAll('.page-btn[data-page]').forEach((btn) => {
      btn.addEventListener('click', () => {
        currentPage = parseInt(btn.dataset.page, 10);
        render();
      });
    });
  }

  function render() {
    renderTableHead();
    const columns = getColumns();
    const allRows = buildRows()
      .filter(matchesQuery)
      .filter(matchesDateRange)
      .sort((a, b) => getRowSortTime(b) - getRowSortTime(a));

    const totalPages = Math.max(1, Math.ceil(allRows.length / PAGE_SIZE));
    if (currentPage > totalPages - 1) currentPage = totalPages - 1;
    if (currentPage < 0) currentPage = 0;
    const rows = allRows.slice(currentPage * PAGE_SIZE, (currentPage + 1) * PAGE_SIZE);

    if (rows.length === 0) {
      tableBodyEl.innerHTML = `<tr><td colspan="${columns.length}"><div class="empty">조건에 맞는 투자일지가 없습니다.</div></td></tr>`;
      listEl.innerHTML = '<div class="empty">조건에 맞는 투자일지가 없습니다.</div>';
      journalPaginationEl.innerHTML = '';
      return;
    }

    tableBodyEl.innerHTML = rows
      .map((row) => {
        const f = getRowFields(row);
        const cells = columns.map((col) => `<td>${getCellValue(col, f)}</td>`).join('');
        return `<tr data-id="${f.recordId}" data-kind="${row.kind}">${cells}</tr>`;
      })
      .join('');

    listEl.innerHTML = rows
      .map((row) => {
        const f = getRowFields(row);
        const metricCols = columns.filter((c) => !['제목', '종목', '상태', '공유'].includes(c));
        const metrics = metricCols
          .map(
            (col) => `
            <div class="metric-item">
              <div class="metric-label">${col}</div>
              <div class="metric-value">${getCellValue(col, f)}</div>
            </div>
          `
          )
          .join('');
        return `
        <article class="journal-card" data-id="${f.recordId}" data-kind="${row.kind}" role="button" tabindex="0" aria-label="${f.title} 상세 보기">
          <div class="journal-card-head">
            <div>
              <h2 class="journal-title">${f.title}</h2>
              <p class="meta">${f.stockName}(${f.stockCode || '-'})</p>
            </div>
            <div class="chip-group">
              <span class="chip ${f.stateClass}">${f.stateLabel}</span>
              <span class="chip ${f.isShared ? 'shared' : 'private'}">${f.isShared ? '공유' : '비공개'}</span>
            </div>
          </div>
          <div class="journal-metrics">
            ${metrics}
          </div>
        </article>
      `;
      })
      .join('');

    const openRow = (kind, id) => openDetailRoot(kind, id);
    tableBodyEl.querySelectorAll('tr[data-id]').forEach((row) => {
      row.addEventListener('click', () => openRow(row.dataset.kind, row.dataset.id));
    });
    listEl.querySelectorAll('.journal-card').forEach((card) => {
      const open = () => openRow(card.dataset.kind, card.dataset.id);
      card.addEventListener('click', open);
      card.addEventListener('keydown', (e) => {
        if (e.key === 'Enter' || e.key === ' ') {
          e.preventDefault();
          open();
        }
      });
    });

    renderPagination(allRows.length);
  }

  function openCreateModal() {
    createModalEl.classList.add('open');
    createModalEl.setAttribute('aria-hidden', 'false');
    buyDateInput.value = getNowDateTimeLocal();
    updateCurrentPriceDisplay('');
    stockCodeInput.value = '';
  }

  function closeCreateModal() {
    createModalEl.classList.remove('open');
    createModalEl.setAttribute('aria-hidden', 'true');
    formEl.reset();
    buyDateInput.value = getNowDateTimeLocal();
    updateCurrentPriceDisplay('');
  }

  function closeActionModal() {
    detailStack = [];
    currentDetail = null;
    actionModalEl.classList.remove('open');
    actionModalEl.setAttribute('aria-hidden', 'true');
    sellFormEl.classList.add('hidden');
    sellFormEl.reset();
    addBuyFormEl.classList.add('hidden');
    addBuyFormEl.reset();
  }

  // 매수/매도 행은 각각의 개별 기록 모달을, 보유·거래 행은 내역 모달을 연다.
  function openDetailRoot(kind, id) {
    const modalKind = kind === 'hold' || kind === 'deal' ? 'position' : kind;
    detailStack = [];
    currentDetail = { kind: modalKind, id };
    renderDetailModal();
    actionModalEl.classList.add('open');
    actionModalEl.setAttribute('aria-hidden', 'false');
  }

  // 보유/거래 모달 안에서 매수·매도 기록을 눌러 들어갈 때 사용 — 뒤로가기로 복귀 가능
  function openDetailChild(kind, id) {
    if (currentDetail) detailStack.push(currentDetail);
    currentDetail = { kind, id };
    renderDetailModal();
  }

  function goBackDetail() {
    while (detailStack.length) {
      const prev = detailStack.pop();
      if (detailExists(prev)) {
        currentDetail = prev;
        renderDetailModal();
        return;
      }
    }
    closeActionModal();
  }

  function renderDetailModal() {
    if (!currentDetail || !detailExists(currentDetail)) {
      closeActionModal();
      return;
    }
    detailBackBtn.style.display = detailStack.length ? 'inline-block' : 'none';
    sellFormEl.classList.add('hidden');
    sellFormEl.reset();
    addBuyFormEl.classList.add('hidden');
    addBuyFormEl.reset();

    if (currentDetail.kind === 'buy') return renderBuyDetail();
    if (currentDetail.kind === 'sell') return renderSellDetail();
    renderPositionDetail();
  }

  function renderBuyDetail() {
    const buy = buys.find((b) => b.id === currentDetail.id);
    detailTitleInput.value = buy.title;

    actionMetaEl.innerHTML = `
      <div class="detail-block">
        <p class="meta">종목: ${buy.stockName}(${buy.stockCode || '-'})</p>
        <p class="meta">매수시간: ${formatDateTime(buy.buyAt)}</p>
        <p class="meta">매수주: ${formatShares(buy.quantity)}</p>
        <p class="meta">매수 당시 주가: ${formatWon(buy.price)}</p>
      </div>
    `;
    toggleShareBtn.style.display = 'inline-block';
    toggleShareBtn.textContent = buy.isShared ? '공유 해제' : '공유하기';
    openBuyFormBtn.style.display = 'none';
    openSellFormBtn.style.display = 'none';
    deleteJournalBtn.style.display = 'inline-block';
  }

  function renderSellDetail() {
    const sell = sells.find((s) => s.id === currentDetail.id);
    detailTitleInput.value = sell.title;

    actionMetaEl.innerHTML = `
      <div class="detail-block">
        <p class="meta">종목: ${sell.stockName}(${sell.stockCode || '-'})</p>
        <p class="meta">매도시간: ${formatDateTime(sell.sellAt)}</p>
        <p class="meta">매도주: ${formatShares(sell.quantity)}</p>
        <p class="meta">매도 당시 주가: ${formatWon(sell.price)}</p>
      </div>
    `;
    toggleShareBtn.style.display = 'inline-block';
    toggleShareBtn.textContent = sell.isShared ? '공유 해제' : '공유하기';
    openBuyFormBtn.style.display = 'none';
    openSellFormBtn.style.display = 'none';
    deleteJournalBtn.style.display = 'inline-block';
  }

  // 매수/매도 기록 5개 단위 페이징 — 최신 순으로 정렬 후 각자 별도 페이지 상태를 유지한다.
  function renderRecordSection(kind, position) {
    const all = (kind === 'buy' ? getPositionBuys(position.id) : getPositionSells(position.id))
      .slice()
      .sort((a, b) => {
        const dateA = kind === 'buy' ? a.buyAt : a.sellAt;
        const dateB = kind === 'buy' ? b.buyAt : b.sellAt;
        return new Date(dateB) - new Date(dateA);
      });

    const totalPages = Math.max(1, Math.ceil(all.length / RECORD_PAGE_SIZE));
    let page = kind === 'buy' ? buyRecordsPage : sellRecordsPage;
    page = Math.min(Math.max(0, page), totalPages - 1);
    if (kind === 'buy') buyRecordsPage = page;
    else sellRecordsPage = page;

    const pageItems = all.slice(page * RECORD_PAGE_SIZE, (page + 1) * RECORD_PAGE_SIZE);
    const itemsHtml = pageItems
      .map((item) => {
        const dateVal = kind === 'buy' ? item.buyAt : item.sellAt;
        return `
        <div class="record-row" data-kind="${kind}" data-id="${item.id}">
          <strong>${item.title}</strong> · ${formatWon(item.price)} · ${formatShares(item.quantity)} · 총 ${formatWon(
          item.price * item.quantity
        )} · ${formatDateTime(dateVal)}
        </div>
      `;
      })
      .join('');

    const paginationHtml = all.length
      ? `
        <div class="record-pagination">
          <button type="button" class="btn-outline" data-record-nav="prev" data-record-kind="${kind}" ${
          page === 0 ? 'disabled' : ''
        }>이전</button>
          <span class="record-page-indicator">
            <input type="number" class="record-page-input" data-record-kind="${kind}" min="1" max="${totalPages}" value="${
          page + 1
        }" />
            / ${totalPages}
          </span>
          <button type="button" class="btn-outline" data-record-nav="next" data-record-kind="${kind}" ${
          page >= totalPages - 1 ? 'disabled' : ''
        }>다음</button>
        </div>
      `
      : '';

    return `<div class="record-list">${itemsHtml || `<p class="meta">${kind === 'buy' ? '매수' : '매도'} 기록 없음</p>`}</div>${paginationHtml}`;
  }

  function renderPositionDetail() {
    const position = positions.find((p) => p.id === currentDetail.id);
    detailTitleInput.value = position.title;

    if (lastPositionDetailId !== position.id) {
      buyRecordsPage = 0;
      sellRecordsPage = 0;
      lastPositionDetailId = position.id;
    }

    const closed = isPositionClosed(position);
    const qty = getHoldingQuantity(position.id);
    const currentPrice = resolveCurrentPrice(position.stockCode, position.priceFallback);
    const usedAmount = getUsedAmount(position.id);
    const soldAmount = getSoldAmount(position.id);

    const buyRows = renderRecordSection('buy', position);
    const sellRows = renderRecordSection('sell', position);

    let statusHtml;
    if (!closed) {
      const sellAllNow = qty * currentPrice;
      const currentPnl = soldAmount + sellAllNow - usedAmount;
      statusHtml = `
        <div class="detail-block">
          <h3>보유 현황</h3>
          <p class="meta">보유주 수: ${formatShares(qty)}</p>
          <p class="meta">현재가: ${currentPrice > 0 ? formatWon(currentPrice) : '-'}</p>
          <p class="meta">현재 보유주 전량 매도가: ${currentPrice > 0 ? formatWon(sellAllNow) : '-'}</p>
          <p class="meta">현재 손익: ${currentPnl >= 0 ? '+' : ''}${formatWon(currentPnl)}</p>
          <p class="meta">사용 금액: ${formatWon(usedAmount)}</p>
          <p class="meta">최초 매수 기록: ${formatDateTime(position.firstBuyAt)}</p>
        </div>
      `;
    } else {
      const pnl = getPnl(position.id);
      const pnlRate = usedAmount > 0 ? (pnl / usedAmount) * 100 : 0;
      statusHtml = `
        <div class="detail-block">
          <h3>거래결과</h3>
          <p class="meta">사용 금액: ${formatWon(usedAmount)}</p>
          <p class="meta">손익: ${pnl >= 0 ? '+' : ''}${formatWon(pnl)}</p>
          <p class="meta">손익률: ${pnlRate >= 0 ? '+' : ''}${pnlRate.toFixed(2)}%</p>
          <p class="meta">최초 매수 시간: ${formatDateTime(position.firstBuyAt)}</p>
          <p class="meta">최종 매도 시간: ${formatDateTime(position.finalSellAt)}</p>
        </div>
      `;
    }

    actionMetaEl.innerHTML = `
      <p class="meta">종목: ${position.stockName}(${position.stockCode || '-'})</p>
      ${statusHtml}
      <div class="detail-block">
        <h3>매수 기록</h3>
        ${buyRows}
      </div>
      <div class="detail-block">
        <h3>매도 기록</h3>
        ${sellRows}
      </div>
      <div class="detail-block">
        <h3>메모</h3>
        <textarea id="positionNoteInput" class="memo-textarea" rows="3" placeholder="메모를 입력하세요.">${
          position.note || ''
        }</textarea>
        <button class="btn-outline" id="positionNoteSaveBtn" type="button">메모 저장</button>
      </div>
    `;

    toggleShareBtn.style.display = 'inline-block';
    toggleShareBtn.textContent = position.isShared ? '공유 해제' : '공유하기';
    openBuyFormBtn.style.display = !closed ? 'inline-block' : 'none';
    openSellFormBtn.style.display = !closed && qty > 0 ? 'inline-block' : 'none';
    deleteJournalBtn.style.display = 'inline-block';

    actionMetaEl.querySelectorAll('.record-row').forEach((rowEl) => {
      rowEl.addEventListener('click', () => openDetailChild(rowEl.dataset.kind, rowEl.dataset.id));
    });
    document.getElementById('positionNoteSaveBtn').addEventListener('click', () => {
      position.note = document.getElementById('positionNoteInput').value;
      persistAll();
      window.alert('메모가 저장되었습니다.');
    });
  }

  function getRecordPageCount(kind, position) {
    const list = kind === 'buy' ? getPositionBuys(position.id) : getPositionSells(position.id);
    return Math.max(1, Math.ceil(list.length / RECORD_PAGE_SIZE));
  }

  function setRecordPage(kind, page) {
    if (kind === 'buy') buyRecordsPage = page;
    else sellRecordsPage = page;
  }

  // 매수/매도 기록 페이징 버튼·페이지 직접 입력은 actionMetaEl에 위임 처리(매번 다시 그려지므로 한 번만 등록)한다.
  actionMetaEl.addEventListener('click', (e) => {
    const navBtn = e.target.closest('button[data-record-nav]');
    if (!navBtn || !currentDetail || currentDetail.kind !== 'position') return;
    const position = positions.find((p) => p.id === currentDetail.id);
    if (!position) return;

    const kind = navBtn.dataset.recordKind;
    const totalPages = getRecordPageCount(kind, position);
    const current = kind === 'buy' ? buyRecordsPage : sellRecordsPage;
    const next = navBtn.dataset.recordNav === 'next' ? current + 1 : current - 1;
    setRecordPage(kind, Math.min(Math.max(0, next), totalPages - 1));
    renderPositionDetail();
  });

  actionMetaEl.addEventListener('change', (e) => {
    const input = e.target.closest('input.record-page-input');
    if (!input || !currentDetail || currentDetail.kind !== 'position') return;
    const position = positions.find((p) => p.id === currentDetail.id);
    if (!position) return;

    const kind = input.dataset.recordKind;
    const totalPages = getRecordPageCount(kind, position);
    let page = parseInt(input.value, 10);
    if (isNaN(page)) page = 1;
    page = Math.min(Math.max(1, page), totalPages);
    setRecordPage(kind, page - 1);
    renderPositionDetail();
  });

  tabsEl.addEventListener('click', (e) => {
    const target = e.target.closest('button[data-category]');
    if (!target) return;

    tabsEl.querySelectorAll('.tab').forEach((btn) => btn.classList.remove('active'));
    target.classList.add('active');
    activeCategory = target.dataset.category;
    currentPage = 0;
    render();
  });

  searchEl.addEventListener('input', () => {
    query = searchEl.value.trim().toLowerCase();
    currentPage = 0;
    render();
  });

  filterFromDateEl.value = fromDate;
  filterToDateEl.value = toDate;

  filterFromDateEl.addEventListener('change', () => {
    fromDate = filterFromDateEl.value;
    currentPage = 0;
    render();
  });

  filterToDateEl.addEventListener('change', () => {
    toDate = filterToDateEl.value;
    currentPage = 0;
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
    const now = new Date().toISOString();
    const userId = getCurrentUserId();
    const title = String(formData.get('title') || '').trim();
    const stockName = String(formData.get('stockName') || '').trim();
    const stockCode = String(formData.get('stockCode') || '').trim();
    const buyAt = String(formData.get('buyAt') || '').trim();
    const price = Number(formData.get('price') || 0);
    const quantity = Number(formData.get('quantity') || 0);
    if (!title || !stockName || !buyAt || price <= 0 || quantity <= 0) {
      window.alert('제목, 종목명, 매수 일시, 매수 당시 주가, 매수 수량을 모두 입력해주세요.');
      return;
    }
    const buyAtIso = new Date(buyAt).toISOString();
    const currentPrice = resolveCurrentPrice(stockCode, 0);

    // 종목 추가는 최초 매수(새 보유/거래 시작) 전용이다.
    // 같은 사용자·같은 종목의 진행 중인(보유 상태) 내역이 이미 있으면 여기서 추가하지 않고,
    // 해당 보유 상세의 "매수" 버튼을 이용하도록 안내한다. 이전 거래가 이미 완료(거래)된 종목은 새로 시작할 수 있다.
    const openPosition = positions.find(
      (p) =>
        p.userId === userId &&
        getHoldingQuantity(p.id) > 0 &&
        ((stockCode && p.stockCode === stockCode) || (!stockCode && p.stockName === stockName))
    );

    if (openPosition) {
      window.alert('이미 보유 중인 종목입니다. 추가 매수는 보유 상세 화면의 "매수" 버튼을 이용해주세요.');
      return;
    }

    const positionId = createId();

    buys.push({
      id: createId(),
      title,
      stockCode,
      stockName,
      buyAt: buyAtIso,
      price,
      quantity,
      positionId,
      userId,
      isShared: false,
      createdAt: now,
    });

    positions.unshift({
      id: positionId,
      title,
      stockCode,
      stockName,
      priceFallback: currentPrice,
      firstBuyAt: buyAtIso,
      finalSellAt: buyAtIso,
      note: '',
      userId,
      isShared: false,
      createdAt: now,
    });

    persistAll();
    closeCreateModal();
    render();
  });

  toggleShareBtn.addEventListener('click', () => {
    const record = getCurrentRecord();
    if (!record) return;

    if (!record.isShared) {
      const ok = window.confirm('공유할까요?');
      if (!ok) return;
    }

    record.isShared = !record.isShared;
    persistAll();
    renderDetailModal();
    render();
  });

  // 매수 삭제: 매도 기록이 있으면 삭제 후에도 총 매수주 >= 총 매도주를 반드시 만족해야 한다.
  function deleteBuyRecord(record) {
    const positionId = record.positionId;
    const positionSells = sells.filter((s) => s.positionId === positionId);
    const siblingBuys = buys.filter((b) => b.positionId === positionId);
    const holdingQty = getHoldingQuantity(positionId);

    if (positionSells.length > 0 && record.quantity > holdingQty) {
      window.alert('이미 매도된 수량이 포함되어 있어 해당 매수 기록은 삭제할 수 없습니다.');
      return false;
    }

    const ok = window.confirm('삭제할까요?');
    if (!ok) return false;

    if (positionSells.length === 0 && siblingBuys.length === 1) {
      // 매도 기록 없고 매수 기록이 이 하나뿐이면 거래 자체가 없었던 것처럼 내역까지 삭제
      buys = buys.filter((b) => b.id !== record.id);
      positions = positions.filter((p) => p.id !== positionId);
      return true;
    }

    buys = buys.filter((b) => b.id !== record.id);

    if (positionSells.length === 0) {
      // 매도 기록이 없는 경우에만 수정 후 수량/금액이 바닥나면 내역을 정리한다.
      const remainingQty = getBoughtQuantity(positionId);
      const remainingUsed = getUsedAmount(positionId);
      if (remainingQty <= 0 || remainingUsed <= 0) {
        positions = positions.filter((p) => p.id !== positionId);
      }
    }
    return true;
  }

  deleteJournalBtn.addEventListener('click', () => {
    const record = getCurrentRecord();
    if (!record || !currentDetail) return;

    const { kind } = currentDetail;
    if (kind === 'buy') {
      if (!deleteBuyRecord(record)) return;
    } else if (kind === 'sell') {
      const ok = window.confirm('삭제할까요?');
      if (!ok) return;
      sells = sells.filter((s) => s.id !== record.id);
    } else {
      const ok = window.confirm('삭제할까요?');
      if (!ok) return;
      positions = positions.filter((p) => p.id !== record.id);
      buys = buys.filter((b) => b.positionId !== record.id);
      sells = sells.filter((s) => s.positionId !== record.id);
    }

    persistAll();
    goBackDetail();
    render();
  });

  openBuyFormBtn.addEventListener('click', () => {
    sellFormEl.classList.add('hidden');
    sellFormEl.reset();
    addBuyFormEl.classList.remove('hidden');
    addBuyFormEl.querySelector('input[name=addBuyAt]').value = getNowDateTimeLocal();
  });

  cancelAddBuyBtn.addEventListener('click', () => {
    addBuyFormEl.classList.add('hidden');
    addBuyFormEl.reset();
  });

  addBuyFormEl.addEventListener('submit', (e) => {
    e.preventDefault();
    if (!currentDetail || currentDetail.kind !== 'position') return;
    const current = positions.find((p) => p.id === currentDetail.id);
    if (!current) return;

    const data = new FormData(addBuyFormEl);
    const buyTitle = String(data.get('addBuyTitle') || '').trim();
    const buyAt = String(data.get('addBuyAt') || '').trim();
    const buyQuantity = Number(data.get('addBuyQuantity') || 0);
    const buyPrice = Number(data.get('addBuyPrice') || 0);
    if (!buyTitle || !buyAt || !buyQuantity || !buyPrice) {
      window.alert('제목, 매수 일시, 매수 수량, 매수 당시 주가를 모두 입력해주세요.');
      return;
    }

    buys.push({
      id: createId(),
      title: buyTitle,
      stockCode: current.stockCode,
      stockName: current.stockName,
      buyAt: new Date(buyAt).toISOString(),
      price: buyPrice,
      quantity: buyQuantity,
      positionId: current.id,
      userId: getCurrentUserId(),
      isShared: current.isShared,
      createdAt: new Date().toISOString(),
    });

    persistAll();
    renderDetailModal();
    render();
  });

  openSellFormBtn.addEventListener('click', () => {
    addBuyFormEl.classList.add('hidden');
    addBuyFormEl.reset();
    sellFormEl.classList.remove('hidden');
  });

  cancelSellBtn.addEventListener('click', () => {
    sellFormEl.classList.add('hidden');
    sellFormEl.reset();
  });

  sellFormEl.addEventListener('submit', (e) => {
    e.preventDefault();
    if (!currentDetail || currentDetail.kind !== 'position') return;
    const current = positions.find((p) => p.id === currentDetail.id);
    if (!current) return;

    const data = new FormData(sellFormEl);
    const sellTitle = String(data.get('sellTitle') || '').trim();
    const sellAt = String(data.get('sellAt') || '').trim();
    const sellQuantity = Number(data.get('sellQuantity') || 0);
    const sellPrice = Number(data.get('sellPrice') || 0);
    if (!sellTitle || !sellAt || !sellQuantity || !sellPrice) {
      window.alert('제목, 매도 일시, 매도 수량, 매도 당시 주가를 모두 입력해주세요.');
      return;
    }

    const available = getHoldingQuantity(current.id);
    if (sellQuantity > available) {
      window.alert(`보유 수량(${formatShares(available)})보다 많이 매도할 수 없습니다.`);
      return;
    }

    const sellAtIso = new Date(sellAt).toISOString();

    sells.push({
      id: createId(),
      title: sellTitle,
      stockCode: current.stockCode,
      stockName: current.stockName,
      sellAt: sellAtIso,
      price: sellPrice,
      quantity: sellQuantity,
      positionId: current.id,
      userId: getCurrentUserId(),
      isShared: current.isShared,
      createdAt: new Date().toISOString(),
    });

    if (available - sellQuantity <= 0) {
      current.finalSellAt = sellAtIso;
    }

    persistAll();
    renderDetailModal();
    render();
  });

  detailBackBtn.addEventListener('click', goBackDetail);

  detailTitleSaveBtn.addEventListener('click', () => {
    const record = getCurrentRecord();
    if (!record) return;
    const newTitle = detailTitleInput.value.trim();
    if (!newTitle) {
      window.alert('제목을 입력해주세요.');
      return;
    }
    record.title = newTitle;
    persistAll();
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

  buyDateInput.value = getNowDateTimeLocal();
  updateCurrentPriceDisplay('');
  render();
});
