/** 공통 헤더, 검색, 유틸리티 */

function renderHeader(activePage) {
  const navItems = [
    { href: 'index.html', label: '홈', key: 'home' },
    { href: 'list.html', label: '종목리스트', key: 'list' },
    { href: 'index.html#ranking', label: '랭킹', key: 'ranking' },
    { href: '#', label: '관심종목', key: 'watchlist' },
  ];

  const navHtml = navItems
    .map(
      (item) =>
        `<a class="nav-link${activePage === item.key ? ' active' : ''}" href="${item.href}">${item.label}</a>`
    )
    .join('');

  return `
    <header class="header">
      <div class="header-inner">
        <button class="menu-toggle" id="menuToggle" aria-label="메뉴 열기">
          <svg width="24" height="24" fill="none" viewBox="0 0 24 24" stroke="currentColor" stroke-width="2">
            <line x1="3" y1="6" x2="21" y2="6"/><line x1="3" y1="12" x2="21" y2="12"/><line x1="3" y1="18" x2="21" y2="18"/>
          </svg>
        </button>
        <a class="brand" href="index.html">ValuePick</a>
        <nav class="nav" id="mainNav">${navHtml}</nav>
        <div class="header-right">
          <div class="search-wrap">
            <svg width="16" height="16" fill="none" viewBox="0 0 24 24" stroke="currentColor" stroke-width="2">
              <circle cx="11" cy="11" r="8"/><line x1="21" y1="21" x2="16.65" y2="16.65"/>
            </svg>
            <input type="text" class="search-input" id="headerSearch" placeholder="종목명 또는 종목코드 검색" aria-label="종목 검색" />
          </div>
          <a class="btn-text" href="login.html">로그인</a>
          <a class="btn-primary" href="register.html">회원가입</a>
        </div>
      </div>
    </header>
  `;
}

function initHeader(activePage) {
  const placeholder = document.getElementById('header-placeholder');
  if (placeholder) {
    placeholder.outerHTML = renderHeader(activePage);
  }

  const toggle = document.getElementById('menuToggle');
  const nav = document.getElementById('mainNav');
  toggle?.addEventListener('click', () => nav?.classList.toggle('open'));

  const searchInput = document.getElementById('headerSearch');
  searchInput?.addEventListener('keydown', (e) => {
    if (e.key === 'Enter') {
      const q = searchInput.value.trim();
      if (q) {
        window.location.href = `list.html?q=${encodeURIComponent(q)}`;
      }
    }
  });
}

function goToDetail(code) {
  window.location.href = `detail.html?code=${code}`;
}

function renderStockCard(stock) {
  const cls = changeClass(stock.changeRate);
  return `
    <article class="stock-card" data-code="${stock.code}" role="button" tabindex="0" aria-label="${stock.name} 상세 보기">
      <div class="stock-card-header">
        <div>
          <h3 class="stock-name">${stock.name}</h3>
          <p class="stock-code">${stock.code}</p>
        </div>
        <div class="stock-price-wrap">
          <div class="stock-price">${formatPrice(stock.price)}</div>
          <div class="stock-change ${cls}">${formatChange(stock.changeRate)}</div>
        </div>
      </div>
      <div class="stock-metrics">
        <div class="metric-item"><div class="metric-label">시가총액</div><div class="metric-value">${formatMarketCap(stock.marketCap)}</div></div>
        <div class="metric-item"><div class="metric-label">PER</div><div class="metric-value">${fmt2(stock.per)}</div></div>
        <div class="metric-item"><div class="metric-label">PBR</div><div class="metric-value">${fmt2(stock.pbr)}</div></div>
        <div class="metric-item"><div class="metric-label">ROE</div><div class="metric-value">${fmt2(stock.roe, '%')}</div></div>
        <div class="metric-item"><div class="metric-label">배당수익률</div><div class="metric-value">${fmt2(stock.dividendYield, '%')}</div></div>
      </div>
    </article>
  `;
}

function bindStockCards(container) {
  container?.querySelectorAll('.stock-card, .rank-item[data-code]').forEach((el) => {
    const code = el.dataset.code;
    el.addEventListener('click', () => goToDetail(code));
    el.addEventListener('keydown', (e) => {
      if (e.key === 'Enter' || e.key === ' ') {
        e.preventDefault();
        goToDetail(code);
      }
    });
  });
}

function renderRankingList(stocks, key, label) {
  const ranked = getRanking(stocks, key);
  const items = ranked
    .map((s, i) => {
      const cls = changeClass(s.changeRate);
      const value = key === 'roe' || key === 'dividendYield'
        ? fmt2(s[key], '%')
        : fmt2(s[key]);
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
    })
    .join('');
  return items;
}

function drawLineChart(canvas, datasets, labels) {
  const ctx = canvas.getContext('2d');
  const dpr = window.devicePixelRatio || 1;
  const rect = canvas.getBoundingClientRect();
  canvas.width = rect.width * dpr;
  canvas.height = rect.height * dpr;
  ctx.scale(dpr, dpr);

  const w = rect.width;
  const h = rect.height;
  const pad = { top: 20, right: 20, bottom: 36, left: 56 };
  const chartW = w - pad.left - pad.right;
  const chartH = h - pad.top - pad.bottom;

  const allValues = datasets.flatMap((d) => d.data);
  const maxVal = Math.max(...allValues) * 1.1;
  const minVal = 0;

  ctx.clearRect(0, 0, w, h);

  // grid
  ctx.strokeStyle = '#e5e8eb';
  ctx.lineWidth = 1;
  ctx.setLineDash([4, 4]);
  for (let i = 0; i <= 4; i++) {
    const y = pad.top + (chartH / 4) * i;
    ctx.beginPath();
    ctx.moveTo(pad.left, y);
    ctx.lineTo(w - pad.right, y);
    ctx.stroke();
    const val = maxVal - ((maxVal - minVal) / 4) * i;
    ctx.setLineDash([]);
    ctx.fillStyle = '#8b95a1';
    ctx.font = '11px Pretendard, sans-serif';
    ctx.textAlign = 'right';
    ctx.fillText(Math.round(val).toLocaleString(), pad.left - 8, y + 4);
    ctx.setLineDash([4, 4]);
  }
  ctx.setLineDash([]);

  // x labels
  labels.forEach((label, i) => {
    const x = pad.left + (chartW / (labels.length - 1)) * i;
    ctx.fillStyle = '#8b95a1';
    ctx.font = '11px Pretendard, sans-serif';
    ctx.textAlign = 'center';
    ctx.fillText(label, x, h - 10);
  });

  // lines
  datasets.forEach((ds) => {
    ctx.strokeStyle = ds.color;
    ctx.lineWidth = 2.5;
    ctx.beginPath();
    ds.data.forEach((val, i) => {
      const x = pad.left + (chartW / (labels.length - 1)) * i;
      const y = pad.top + chartH - ((val - minVal) / (maxVal - minVal)) * chartH;
      if (i === 0) ctx.moveTo(x, y);
      else ctx.lineTo(x, y);
    });
    ctx.stroke();

    ds.data.forEach((val, i) => {
      const x = pad.left + (chartW / (labels.length - 1)) * i;
      const y = pad.top + chartH - ((val - minVal) / (maxVal - minVal)) * chartH;
      ctx.fillStyle = ds.color;
      ctx.beginPath();
      ctx.arc(x, y, 4, 0, Math.PI * 2);
      ctx.fill();
    });
  });
}

function drawBarChart(canvas, datasets, labels) {
  const ctx = canvas.getContext('2d');
  const dpr = window.devicePixelRatio || 1;
  const rect = canvas.getBoundingClientRect();
  canvas.width = rect.width * dpr;
  canvas.height = rect.height * dpr;
  ctx.scale(dpr, dpr);

  const w = rect.width;
  const h = rect.height;
  const pad = { top: 20, right: 20, bottom: 36, left: 56 };
  const chartW = w - pad.left - pad.right;
  const chartH = h - pad.top - pad.bottom;

  const allValues = datasets.flatMap((d) => d.data);
  const maxVal = Math.max(...allValues) * 1.1;

  ctx.clearRect(0, 0, w, h);

  const groupW = chartW / labels.length;
  const barW = groupW / (datasets.length + 1);

  labels.forEach((label, i) => {
    const gx = pad.left + groupW * i + groupW / 2;
    ctx.fillStyle = '#8b95a1';
    ctx.font = '11px Pretendard, sans-serif';
    ctx.textAlign = 'center';
    ctx.fillText(label, gx, h - 10);

    datasets.forEach((ds, di) => {
      const val = ds.data[i];
      const barH = (val / maxVal) * chartH;
      const x = pad.left + groupW * i + barW * (di + 0.5);
      const y = pad.top + chartH - barH;
      ctx.fillStyle = ds.color;
      ctx.beginPath();
      ctx.roundRect(x, y, barW * 0.8, barH, [3, 3, 0, 0]);
      ctx.fill();
    });
  });
}
