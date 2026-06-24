document.addEventListener('DOMContentLoaded', async () => {
  initHeader('list');

  let stocks = [];
  let sortKey = null;
  let sortDir = 'desc';
  let filters = {};
  let currentPage = 0;
  const PAGE_SIZE = 20;
  let _totalPages = 1;
  let _totalCount = 0;

  const params = new URLSearchParams(window.location.search);
  const searchQuery = params.get('q')?.toLowerCase() || '';
  if (searchQuery) {
    document.getElementById('headerSearch').value = searchQuery;
  }

  const countEl = document.getElementById('stockCount');
  const tableBody = document.getElementById('tableBody');
  const cardList = document.getElementById('cardList');
  const filterBtn = document.getElementById('filterBtn');
  const filterPanel = document.getElementById('filterPanel');
  const resetFilterBtn = document.getElementById('resetFilterBtn');

  // API에서 종목 목록 가져오기 (keyword, market 파라미터 지원)
  async function loadStocks({ market } = {}) {
    tableBody.innerHTML = `<tr><td colspan="8"><div class="empty-state">종목 데이터를 불러오는 중...</div></td></tr>`;
    try {
      const apiParams = { page: currentPage, size: PAGE_SIZE };
      if (searchQuery) apiParams.keyword = searchQuery;
      if (market) apiParams.market = market;
      const { stocks: fetched, totalCount, totalPages } = await fetchStocks(apiParams);
      stocks = fetched;
      _totalCount = totalCount;
      _totalPages = totalPages;
    } catch (e) {
      console.error('API 호출 실패:', e);
      stocks = [];
      _totalCount = 0;
      _totalPages = 1;
    }
  }

  function applyFilters() {
    let result = [...stocks];

    const ranges = [
      { key: 'per', min: 'perMin', max: 'perMax' },
      { key: 'pbr', min: 'pbrMin', max: 'pbrMax' },
      { key: 'roe', min: 'roeMin', max: 'roeMax' },
      { key: 'dividendYield', min: 'divMin', max: 'divMax' },
      { key: 'marketCap', min: 'capMin', max: 'capMax' },
    ];

    ranges.forEach(({ key, min, max }) => {
      if (filters[min] != null) result = result.filter((s) => s[key] >= filters[min]);
      if (filters[max] != null) result = result.filter((s) => s[key] <= filters[max]);
    });

    if (sortKey) {
      result = sortStocks(result, sortKey, sortDir);
    }

    return result;
  }

  function activeFilterCount() {
    return Object.values(filters).filter((v) => v != null && v !== '').length;
  }

  function updateFilterBtn() {
    const count = activeFilterCount();
    filterBtn.innerHTML = count > 0
      ? `필터 (${count}) <svg width="14" height="14" fill="none" viewBox="0 0 24 24" stroke="currentColor" stroke-width="2"><polyline points="6 9 12 15 18 9"/></svg>`
      : `필터 <svg width="14" height="14" fill="none" viewBox="0 0 24 24" stroke="currentColor" stroke-width="2"><polyline points="6 9 12 15 18 9"/></svg>`;
    resetFilterBtn.style.display = count > 0 ? 'inline-block' : 'none';
  }

  function renderTable(data) {
    countEl.textContent = `총 ${data.length}개 종목`;

    if (data.length === 0) {
      tableBody.innerHTML = `<tr><td colspan="8"><div class="empty-state">조건에 맞는 종목이 없습니다.</div></td></tr>`;
      cardList.innerHTML = `<div class="empty-state">조건에 맞는 종목이 없습니다.</div>`;
      return;
    }

    tableBody.innerHTML = data.map((s) => {
      const cls = changeClass(s.changeRate);
      return `
        <tr data-code="${s.code}">
          <td>
            <div class="td-name">${s.name}</div>
            <div class="td-code">${s.code}</div>
          </td>
          <td>${formatPrice(s.price)}</td>
          <td class="${cls}">${formatChange(s.changeRate)}</td>
          <td>${formatMarketCap(s.marketCap)}</td>
          <td>${fmt2(s.per)}</td>
          <td>${fmt2(s.pbr)}</td>
          <td>${fmt2(s.roe, '%')}</td>
          <td>${fmt2(s.dividendYield, '%')}</td>
        </tr>
      `;
    }).join('');

    cardList.innerHTML = data.map(renderStockCard).join('');

    tableBody.querySelectorAll('tr[data-code]').forEach((row) => {
      row.addEventListener('click', () => goToDetail(row.dataset.code));
    });
    bindStockCards(cardList);
  }

  function renderPagination(totalPages, currentPage) {
    const el = document.getElementById('pagination');
    if (!el) return;
    if (totalPages <= 1) { el.innerHTML = ''; return; }

    let html = '';
    if (currentPage > 0) html += `<button class="page-btn" data-page="${currentPage-1}">이전</button>`;
    for (let i = 0; i < totalPages; i++) {
      html += `<button class="page-btn ${i === currentPage ? 'active' : ''}" data-page="${i}">${i+1}</button>`;
    }
    if (currentPage < totalPages - 1) html += `<button class="page-btn" data-page="${currentPage+1}">다음</button>`;

    el.innerHTML = html;
    el.querySelectorAll('.page-btn[data-page]').forEach(btn => {
      btn.addEventListener('click', async () => {
        currentPage = parseInt(btn.dataset.page);
        await loadStocks();
        render();
      });
    });
  }

  function render() {
    const filtered = applyFilters();
    renderTable(filtered);
    updateFilterBtn();
    renderPagination(_totalPages, currentPage);
  }

  // 필터 토글
  filterBtn.addEventListener('click', () => {
    filterPanel.classList.toggle('open');
    filterBtn.classList.toggle('open');
  });

  document.getElementById('applyFilter').addEventListener('click', async () => {
    filters = {};
    ['perMin', 'perMax', 'pbrMin', 'pbrMax', 'roeMin', 'roeMax', 'divMin', 'divMax', 'capMin', 'capMax'].forEach((id) => {
      const el = document.getElementById(id);
      const val = parseFloat(el.value);
      if (!isNaN(val)) filters[id] = val;
    });

    // market 필터 확인 (필터 패널에 market 선택 요소가 있는 경우 반영)
    const marketEl = document.getElementById('marketFilter');
    const marketFilter = marketEl ? marketEl.value : '';

    filterPanel.classList.remove('open');
    filterBtn.classList.remove('open');

    // market 필터가 있으면 API 재호출
    await loadStocks({ market: marketFilter || undefined });
    render();
  });

  document.getElementById('cancelFilter').addEventListener('click', () => {
    filterPanel.classList.remove('open');
    filterBtn.classList.remove('open');
  });

  resetFilterBtn.addEventListener('click', async () => {
    filters = {};
    document.querySelectorAll('.filter-panel input').forEach((el) => { el.value = ''; });
    const marketEl = document.getElementById('marketFilter');
    if (marketEl) marketEl.value = '';

    // 필터 초기화 시 API 재호출 (market 필터 없이)
    await loadStocks();
    render();
  });

  // 정렬
  document.querySelectorAll('.stock-table th[data-sort]').forEach((th) => {
    th.addEventListener('click', () => {
      const key = th.dataset.sort;
      if (sortKey === key) {
        sortDir = sortDir === 'asc' ? 'desc' : 'asc';
      } else {
        sortKey = key;
        sortDir = 'desc';
      }
      document.querySelectorAll('.stock-table th').forEach((h) => h.classList.remove('sorted'));
      th.classList.add('sorted');
      render();
    });
  });

  // 초기 데이터 로드 후 렌더링
  await loadStocks();
  render();
});
