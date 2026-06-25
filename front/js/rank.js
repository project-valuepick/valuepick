document.addEventListener('DOMContentLoaded', async () => {
  initHeader('ranking');

  const tableBody = document.getElementById('tableBody');
  const cardList = document.getElementById('cardList');
  const countEl = document.getElementById('stockCount');

  function rankClass(rank) {
    if (rank <= 3) return 'rank-num top3';
    if (rank <= 10) return 'rank-num top10';
    return 'rank-num';
  }

  function renderTable(stocks) {
    tableBody.innerHTML = stocks.map((s) => {
      const cls = changeClass(s.changeRate);
      return `
        <tr data-code="${s.code}">
          <td><span class="${rankClass(s.rank)}">${s.rank}</span></td>
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
          <td><span class="score-badge">${s.score ?? '-'}</span></td>
        </tr>
      `;
    }).join('');

    tableBody.querySelectorAll('tr[data-code]').forEach((row) => {
      row.addEventListener('click', () => goToDetail(row.dataset.code));
    });
  }

  function renderCards(stocks) {
    cardList.innerHTML = stocks.map((s) => {
      const cls = changeClass(s.changeRate);
      return `
        <article class="rank-card" data-code="${s.code}" role="button" tabindex="0" aria-label="${s.name} 상세 보기">
          <div class="rank-card-header">
            <span class="${rankClass(s.rank)}">${s.rank}</span>
            <div class="rank-card-info">
              <h3 class="stock-name">${s.name}</h3>
              <p class="stock-code">${s.code}</p>
            </div>
            <div class="rank-card-score">
              <span class="score-badge">${s.score ?? '-'}</span>
              <div class="score-label">스코어</div>
            </div>
          </div>
          <div class="rank-card-price">
            <span class="stock-price">${formatPrice(s.price)}</span>
            <span class="stock-change ${cls}">${formatChange(s.changeRate)}</span>
          </div>
          <div class="stock-metrics">
            <div class="metric-item"><div class="metric-label">시가총액</div><div class="metric-value">${formatMarketCap(s.marketCap)}</div></div>
            <div class="metric-item"><div class="metric-label">PER</div><div class="metric-value">${fmt2(s.per)}</div></div>
            <div class="metric-item"><div class="metric-label">PBR</div><div class="metric-value">${fmt2(s.pbr)}</div></div>
            <div class="metric-item"><div class="metric-label">ROE</div><div class="metric-value">${fmt2(s.roe, '%')}</div></div>
            <div class="metric-item"><div class="metric-label">배당수익률</div><div class="metric-value">${fmt2(s.dividendYield, '%')}</div></div>
          </div>
        </article>
      `;
    }).join('');

    bindStockCards(cardList);
  }

  tableBody.innerHTML = `<tr><td colspan="10"><div class="empty-state">데이터를 불러오는 중...</div></td></tr>`;

  try {
    const res = await fetch(`${API_BASE}/info/top100`);
    if (!res.ok) throw new Error(`top100 failed: ${res.status}`);
    const body = await res.json();

    const stocks = (body.list || []).map((s, i) => ({
      rank: i + 1,
      ...normalizeStock(s),
      score: s.score != null ? Number(s.score) : null,
    }));

    countEl.textContent = `총 ${stocks.length}개 종목 (스코어 순)`;

    if (stocks.length === 0) {
      tableBody.innerHTML = `<tr><td colspan="10"><div class="empty-state">데이터가 없습니다.</div></td></tr>`;
      cardList.innerHTML = `<div class="empty-state">데이터가 없습니다.</div>`;
      return;
    }

    renderTable(stocks);
    renderCards(stocks);
  } catch (e) {
    console.error('top100 로드 실패:', e);
    tableBody.innerHTML = `<tr><td colspan="10"><div class="empty-state">데이터를 불러올 수 없습니다.</div></td></tr>`;
    cardList.innerHTML = `<div class="empty-state">데이터를 불러올 수 없습니다.</div>`;
  }
});
