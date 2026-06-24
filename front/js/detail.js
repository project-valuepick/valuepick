document.addEventListener('DOMContentLoaded', async () => {
  initHeader('');

  const params = new URLSearchParams(window.location.search);
  const code = params.get('code');
  const main = document.getElementById('detailMain');

  // 로딩 표시
  main.innerHTML = `
    <div class="not-found" id="loadingIndicator">
      <p style="color:var(--text-sub)">데이터를 불러오는 중입니다...</p>
    </div>
  `;

  let stock;
  try {
    stock = await fetchStockFull(code);
  } catch (e) {
    stock = null;
  }

  if (!stock) {
    main.innerHTML = `
      <div class="not-found">
        <h2>종목을 찾을 수 없습니다</h2>
        <p style="color:var(--text-sub);margin-bottom:20px">요청하신 종목 코드(${code || '없음'})가 존재하지 않습니다.</p>
        <a class="btn-primary" href="list.html">종목 리스트로 이동</a>
      </div>
    `;
    return;
  }

  document.title = `${stock.name} — ValuePick`;
  const cls = changeClass(stock.changeRate);
  const sign = stock.changeRate >= 0 ? '+' : '';
  const yearLabels = (stock.years || YEARS).map((y) => y + '년');

  main.innerHTML = `
    <button class="back-btn" id="backBtn" type="button">
      <svg width="16" height="16" fill="none" viewBox="0 0 24 24" stroke="currentColor" stroke-width="2"><polyline points="15 18 9 12 15 6"/></svg>
      돌아가기
    </button>

    <section class="summary-card">
      <div class="summary-top">
        <div class="summary-title">
          <h1>${stock.name}</h1>
          <div class="code">${stock.code}</div>
        </div>
        <div class="summary-price">
          <div class="price">${formatPrice(stock.price)}</div>
          <div class="change-row ${cls}">
            <svg width="14" height="14" fill="none" viewBox="0 0 24 24" stroke="currentColor" stroke-width="2.5">
              ${stock.changeRate >= 0
                ? '<polyline points="18 15 12 9 6 15"/>'
                : '<polyline points="6 9 12 15 18 9"/>'}
            </svg>
            ${formatChange(stock.changeRate)} (${sign}${stock.changeAmount.toLocaleString()}원)
          </div>
        </div>
      </div>
      <div class="summary-metrics">
        <div class="summary-metric"><div class="label">시가총액</div><div class="value">${formatMarketCap(stock.marketCap)}</div></div>
        <div class="summary-metric"><div class="label">PER</div><div class="value">${fmt2(stock.per)}</div></div>
        <div class="summary-metric"><div class="label">PBR</div><div class="value">${fmt2(stock.pbr)}</div></div>
        <div class="summary-metric"><div class="label">ROE</div><div class="value">${fmt2(stock.roe, '%')}</div></div>
        <div class="summary-metric"><div class="label">배당수익률</div><div class="value">${fmt2(stock.dividendYield, '%')}</div></div>
        <div class="summary-metric"><div class="label">주식발행수</div><div class="value">${stock.shares}</div></div>
      </div>
    </section>

    <section class="chart-section">
      <h2>매출 및 수익성 추이 (최근 5년)</h2>
      <div class="chart-legend">
        <span class="legend-item"><span class="legend-dot" style="background:#3182f6"></span>매출액</span>
        <span class="legend-item"><span class="legend-dot" style="background:#00c471"></span>영업이익</span>
        <span class="legend-item"><span class="legend-dot" style="background:#f04452"></span>순이익</span>
      </div>
      <canvas class="chart-canvas" id="revenueChart"></canvas>
    </section>

    <section class="financial-section">
      <h2>재무제표 상세</h2>

      <div class="financial-block">
        <h3>기본 정보</h3>
        <div class="info-grid">
          <div class="info-card"><div class="label">현재가</div><div class="value">${formatPrice(stock.price)}</div></div>
          <div class="info-card"><div class="label">시가총액</div><div class="value">${formatMarketCap(stock.marketCap)}</div></div>
          <div class="info-card"><div class="label">주식발행수</div><div class="value">${stock.shares}</div></div>
          <div class="info-card"><div class="label">업종</div><div class="value">${stock.sector}</div></div>
          <div class="info-card"><div class="label">시장구분</div><div class="value">${stock.market}</div></div>
          <div class="info-card"><div class="label">상장일</div><div class="value">${stock.listedDate}</div></div>
        </div>
      </div>

      <div class="financial-block">
        <h3>투자지표</h3>
        <div class="indicator-grid">
          <div class="indicator-card"><div class="label">PER (주가수익비율)</div><div class="value">${fmt2(stock.per)}</div><div class="hint">낮을수록 저평가</div></div>
          <div class="indicator-card"><div class="label">PBR (주가순자산비율)</div><div class="value">${fmt2(stock.pbr)}</div><div class="hint">낮을수록 저평가</div></div>
          <div class="indicator-card"><div class="label">ROE (자기자본이익률)</div><div class="value">${fmt2(stock.roe, '%')}</div><div class="hint">높을수록 좋음</div></div>
          <div class="indicator-card"><div class="label">배당수익률</div><div class="value">${fmt2(stock.dividendYield, '%')}</div><div class="hint">배당금/주가</div></div>
          <div class="indicator-card"><div class="label">EPS (주당순이익)</div><div class="value">${stock.eps != null ? stock.eps.toLocaleString() : '-'}원</div><div class="hint">높을수록 좋음</div></div>
          <div class="indicator-card"><div class="label">BPS (주당순자산)</div><div class="value">${stock.bps != null ? stock.bps.toLocaleString() : '-'}원</div><div class="hint">높을수록 좋음</div></div>
          <div class="indicator-card"><div class="label">부채비율</div><div class="value">${fmt2(stock.debtRatio, '%')}</div><div class="hint">낮을수록 안정적</div></div>
          <div class="indicator-card"><div class="label">영업이익</div><div class="value">${stock.operatingProfit.toLocaleString()}억</div><div class="hint">최근 연도 기준</div></div>
        </div>
      </div>

      <div class="financial-block">
        <h3>연도별 재무 데이터</h3>
        <div class="tab-header" role="tablist">
          <button class="tab-btn active" role="tab" data-tab="income">손익계산서</button>
          <button class="tab-btn" role="tab" data-tab="balance">재무상태표</button>
          <button class="tab-btn" role="tab" data-tab="indicator">투자지표</button>
        </div>

        <div class="tab-panel active" id="tab-income" role="tabpanel">
          ${renderTable(['매출액', '영업이익', '순이익'], [
            { label: '매출액', data: stock.revenueHistory },
            { label: '영업이익', data: stock.operatingHistory },
            { label: '순이익', data: stock.netIncomeHistory },
          ], stock.years || YEARS)}
        </div>
        <div class="tab-panel" id="tab-balance" role="tabpanel">
          ${renderTable(['자산', '부채', '자본', '부채비율'], [
            { label: '자산', data: stock.assetHistory },
            { label: '부채', data: stock.debtHistory },
            { label: '자본', data: stock.equityHistory },
            { label: '부채비율(%)', data: stock.debtRatioHistory, suffix: '%' },
          ], stock.years || YEARS)}
        </div>
        <div class="tab-panel" id="tab-indicator" role="tabpanel">
          ${renderTable(['EPS', 'BPS', 'PER', 'PBR', 'ROE'], [
            { label: 'EPS', data: stock.epsHistory },
            { label: 'BPS', data: stock.bpsHistory },
            { label: 'PER', data: stock.perHistory },
            { label: 'PBR', data: stock.pbrHistory },
            { label: 'ROE(%)', data: stock.roeHistory, suffix: '%' },
          ], stock.years || YEARS)}
        </div>
      </div>

      <div class="financial-block">
        <h3>최근 5년 자산/부채/자본 추이</h3>
        <div class="chart-legend">
          <span class="legend-item"><span class="legend-dot" style="background:#3182f6"></span>자산</span>
          <span class="legend-item"><span class="legend-dot" style="background:#f04452"></span>부채</span>
          <span class="legend-item"><span class="legend-dot" style="background:#00c471"></span>자본</span>
        </div>
        <canvas class="chart-canvas" id="balanceChart"></canvas>
      </div>
    </section>
  `;

  document.getElementById('backBtn').addEventListener('click', () => history.back());

  // 탭
  document.querySelectorAll('.tab-btn').forEach((btn) => {
    btn.addEventListener('click', () => {
      document.querySelectorAll('.tab-btn').forEach((b) => b.classList.remove('active'));
      document.querySelectorAll('.tab-panel').forEach((p) => p.classList.remove('active'));
      btn.classList.add('active');
      document.getElementById('tab-' + btn.dataset.tab).classList.add('active');
    });
  });

  // 차트
  const revenueCanvas = document.getElementById('revenueChart');
  drawLineChart(revenueCanvas, [
    { data: stock.revenueHistory, color: '#3182f6' },
    { data: stock.operatingHistory, color: '#00c471' },
    { data: stock.netIncomeHistory, color: '#f04452' },
  ], yearLabels);

  const balanceCanvas = document.getElementById('balanceChart');
  drawBarChart(balanceCanvas, [
    { data: stock.assetHistory, color: '#3182f6' },
    { data: stock.debtHistory, color: '#f04452' },
    { data: stock.equityHistory, color: '#00c471' },
  ], yearLabels);

  window.addEventListener('resize', () => {
    drawLineChart(revenueCanvas, [
      { data: stock.revenueHistory, color: '#3182f6' },
      { data: stock.operatingHistory, color: '#00c471' },
      { data: stock.netIncomeHistory, color: '#f04452' },
    ], yearLabels);
    drawBarChart(balanceCanvas, [
      { data: stock.assetHistory, color: '#3182f6' },
      { data: stock.debtHistory, color: '#f04452' },
      { data: stock.equityHistory, color: '#00c471' },
    ], yearLabels);
  });
});

function renderTable(title, rows, years) {
  const headerCells = years.map((y) => `<th>${y}</th>`).join('');
  const bodyRows = rows.map((row) => {
    const cells = row.data.map((v) => {
      if (v == null) return '<td>-</td>';
      const suffix = row.suffix || (row.label.includes('PER') || row.label.includes('PBR') ? '' : '억');
      const isDecimal = suffix === '%' || suffix === '';
      const display = isDecimal
        ? Number(v).toFixed(2)
        : Math.round(v).toLocaleString('ko-KR');
      const unit = suffix === '%' ? '%' : suffix === '억' ? '억' : '';
      return `<td>${display}${unit}</td>`;
    }).join('');
    return `<tr><td>${row.label}</td>${cells}</tr>`;
  }).join('');

  return `
    <div class="data-table-wrap">
      <table class="data-table">
        <thead><tr><th>항목</th>${headerCells}</tr></thead>
        <tbody>${bodyRows}</tbody>
      </table>
    </div>
  `;
}
