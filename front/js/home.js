document.addEventListener('DOMContentLoaded', async () => {
  initHeader('home');

  // 시장 지표
  const marketGrid = document.getElementById('marketGrid');
  marketGrid.innerHTML = '<div>로딩 중...</div>';
  try {
    const indices = await fetchMarketIndices();
    marketGrid.innerHTML = indices.map((idx) => {
      const cls = changeClass(idx.changeRate);
      const sign = idx.changeRate >= 0 ? '+' : '';
      return `
        <div class="market-card">
          <div class="market-name">${idx.name}</div>
          <div class="market-value">${idx.value}</div>
          <div class="market-change ${cls}">${sign}${idx.changeRate}% (${sign}${idx.changeAmount})</div>
        </div>
      `;
    }).join('');
  } catch (err) {
    console.error('fetchMarketIndices 실패:', err);
    marketGrid.innerHTML = '';
  }

  // 주요 종목
  const stocksGrid = document.getElementById('stocksGrid');

  try {
    stocksGrid.innerHTML = '<div>로딩 중...</div>';
    const stocks = await fetchFeaturedStocks();
    stocksGrid.innerHTML = stocks.map(renderStockCard).join('');
    bindStockCards(stocksGrid);
  } catch (err) {
    console.error('fetchFeaturedStocks 실패:', err);
    stocksGrid.innerHTML = '';
  }

  // 랭킹
  const rankings = [
    { id: 'rankPer', key: 'per',           title: '📈 저PER 순위',     type: 'lowPer'  },
    { id: 'rankPbr', key: 'pbr',           title: '📊 저PBR 순위',     type: 'lowPbr'  },
    { id: 'rankRoe', key: 'roe',           title: '💰 고ROE 순위',     type: 'highRoe' },
    { id: 'rankDiv', key: 'dividendYield', title: '💵 배당수익률 순위', type: 'value'   },
  ];

  // 랭킹 섹션 로딩 표시
  rankings.forEach(({ id, title }) => {
    const el = document.getElementById(id);
    if (el) {
      el.innerHTML = `
        <div class="ranking-card">
          <h3>${title}</h3>
          <div>로딩 중...</div>
        </div>
      `;
    }
  });

  function buildRankingItems(stocks, key) {
    return stocks.slice(0, 8).map((s, i) => {
      const cls = changeClass(s.changeRate);
      const value = key === 'roe' || key === 'dividendYield'
        ? s[key] + '%'
        : s[key];
      return `
        <div class="rank-item" data-code="${s.code}" role="button" tabindex="0">
          <div class="rank-left">
            <span class="rank-num${i < 3 ? ' top3' : ''}">${i + 1}</span>
            <div>
              <div class="rank-name">${s.name}</div>
              <div class="rank-code">${s.code}</div>
            </div>
          </div>
          <div class="rank-right">
            <div class="rank-value">${value}</div>
            <div class="stock-change ${cls}">${formatChange(s.changeRate)}</div>
          </div>
        </div>
      `;
    }).join('');
  }

  try {
    const [perStocks, pbrStocks, roeStocks, divStocks] = await Promise.all([
      fetchTop10('lowPer'),
      fetchTop10('lowPbr'),
      fetchTop10('highRoe'),
      fetchTop10('value'),
    ]);

    const apiResults = [perStocks, pbrStocks, roeStocks, divStocks];

    rankings.forEach(({ id, key, title }, idx) => {
      const el = document.getElementById(id);
      if (!el) return;
      el.innerHTML = `
        <div class="ranking-card">
          <h3>${title}</h3>
          ${buildRankingItems(apiResults[idx], key)}
        </div>
      `;
      bindStockCards(el);
    });
  } catch (err) {
    console.error('fetchTop10 실패:', err);
    rankings.forEach(({ id, title }) => {
      const el = document.getElementById(id);
      if (!el) return;
      el.innerHTML = `<div class="ranking-card"><h3>${title}</h3></div>`;
    });
  }
});
