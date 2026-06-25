const YEARS = [2021, 2022, 2023, 2024, 2025];

function formatPrice(price) {
  return (price || 0).toLocaleString('ko-KR') + '원';
}

function formatMarketCap(cap) {
  if (cap == null) return '-';
  return Math.round(cap / 10000).toLocaleString('ko-KR') + '만원';
}

function fmt2(v, suffix = '') {
  if (v == null || isNaN(Number(v))) return '-';
  return Number(v).toFixed(2) + suffix;
}

function formatChange(rate) {
  if (rate == null || isNaN(Number(rate))) return '0.00%';
  const sign = rate >= 0 ? '+' : '';
  return sign + Number(rate).toFixed(2) + '%';
}

function changeClass(rate) {
  return Number(rate) >= 0 ? 'up' : 'down';
}

function sortStocks(stocks, key, dir) {
  return [...stocks].sort((a, b) => {
    const av = a[key] ?? 0;
    const bv = b[key] ?? 0;
    return dir === 'asc' ? av - bv : bv - av;
  });
}

function getRanking(stocks, key, limit = 8) {
  const valid = stocks.filter((s) => s[key] != null && s[key] > 0);
  const asc = key === 'per' || key === 'pbr';
  return sortStocks(valid, key, asc ? 'asc' : 'desc').slice(0, limit);
}

// ──────────────────────────────────────────────
// API Layer
// ──────────────────────────────────────────────

const API_BASE = "http://localhost:8080";

// 목록 전체 필드 정규화 (/info/list, /info/list/filter, /info/search)
function normalizeStock(s) {
  return {
    code:          s.stock_code  || '',
    name:          s.corp_name   || '',
    price:         Number(s.mkp) || 0,
    changeRate:    Number(s.flt_rt) || 0,
    changeAmount:  0,
    marketCap:     Number(s.mrkt_tot_amt) || 0,
    per:           s.per           != null ? Number(s.per)           : null,
    pbr:           s.pbr           != null ? Number(s.pbr)           : null,
    roe:           s.roe           != null ? Number(s.roe)           : null,
    dividendYield: s.dividend_yield != null ? Number(s.dividend_yield) : null,
  };
}

// 랭킹 전용 정규화 (/info/per, /info/pbr, /info/roe, /info/dividend-yield)
function normalizeRankStock(s, indicatorKey) {
  return {
    code:          s.stock_code || '',
    name:          s.corp_name  || '',
    price:         0,
    changeRate:    0,
    changeAmount:  0,
    marketCap:     0,
    per:           s.per            != null ? Number(s.per)            : null,
    pbr:           s.pbr            != null ? Number(s.pbr)            : null,
    roe:           s.roe            != null ? Number(s.roe)            : null,
    dividendYield: s.dividend_yield != null ? Number(s.dividend_yield) : null,
    score:         s.score          != null ? Number(s.score)          : null,
  };
}

// 클라이언트 페이징
function paginate(arr, page, size) {
  const total      = arr.length;
  const totalPages = Math.ceil(total / size) || 1;
  return {
    stocks:     arr.slice(page * size, page * size + size),
    totalCount: total,
    totalPages,
    page,
  };
}

/**
 * 시장 지표 (코스피 + 환율)
 */
async function fetchMarketIndices() {
  const [kospiRes, exchangeRes] = await Promise.allSettled([
    fetch(`${API_BASE}/info/kospi`),
    fetch(`${API_BASE}/info/exchange`),
  ]);

  const result = [];

  if (kospiRes.status === 'fulfilled' && kospiRes.value.ok) {
    const k = await kospiRes.value.json();
    result.push({
      name:         k.idxNm || 'KOSPI',
      value:        (Number(k.clsprcIdx) || 0).toLocaleString('ko-KR'),
      changeRate:   Number(k.flucRt)       || 0,
      changeAmount: Number(k.cmpprevddIdx) || 0,
    });
  }

  if (exchangeRes.status === 'fulfilled' && exchangeRes.value.ok) {
    const e = await exchangeRes.value.json();
    result.push({
      name:         `${e.curUnit || 'USD'} (${e.country || ''})`,
      value:        (Number(e.dealBasR) || 0).toLocaleString('ko-KR') + '원',
      changeRate:   Number(e.changeRate)   || 0,
      changeAmount: Number(e.changeAmount) || 0,
    });
  }

  return result;
}

/**
 * 홈 주요 종목 — /info/top10 (스코어 TOP10 기준)
 */
async function fetchFeaturedStocks() {
  const res = await fetch(`${API_BASE}/info/top10`);
  if (!res.ok) throw new Error(`fetchFeaturedStocks failed: ${res.status}`);
  const body = await res.json();
  return (body.list || []).map(normalizeStock);
}

/**
 * TOP10 랭킹 타입별 조회
 * @param {"value"|"lowPer"|"highRoe"|"lowPbr"|"value"} type
 */
async function fetchTop10(type = "value") {
  const urlMap = {
    lowPer:      '/info/per',
    lowPbr:      '/info/pbr',
    highRoe:     '/info/roe',
    dividendYield: '/info/dividend-yield',
    value:       '/info/top10',
  };
  const url = `${API_BASE}${urlMap[type] || '/info/top10'}`;
  const res = await fetch(url);
  if (!res.ok) throw new Error(`fetchTop10(${type}) failed: ${res.status}`);
  const data = await res.json();
  const arr = type === 'value' ? (data.list || []) : data;
  return arr.map((s) => normalizeRankStock(s, type));
}

/**
 * 종목 목록 (list.js 용)
 * params: { keyword, page, size, perMin, perMax, pbrMin, pbrMax, roeMin, roeMax, divMin, divMax }
 */
async function fetchStocks(params = {}) {
  const { keyword, page = 0, size = 20,
          perMin, perMax, pbrMin, pbrMax, roeMin, roeMax, divMin, divMax } = params;

  // 키워드 검색
  if (keyword) {
    const res = await fetch(`${API_BASE}/info/search?keyword=${encodeURIComponent(keyword)}`);
    if (!res.ok) throw new Error(`fetchStocks(search) failed: ${res.status}`);
    const body = await res.json();
    return paginate((body.result || []).map(normalizeStock), page, size);
  }

  // 필터 적용
  const hasFilter = [perMin, perMax, pbrMin, pbrMax, roeMin, roeMax, divMin, divMax]
    .some((v) => v != null);

  if (hasFilter) {
    const q = new URLSearchParams();
    if (perMin != null) q.set('perMin', perMin);
    if (perMax != null) q.set('perMax', perMax);
    if (roeMin != null) q.set('roeMin', roeMin);
    if (roeMax != null) q.set('roeMax', roeMax);
    if (pbrMin != null) q.set('pbrMin', pbrMin);
    if (pbrMax != null) q.set('pbrMax', pbrMax);
    if (divMin != null) q.set('dyMin', divMin);
    if (divMax != null) q.set('dyMax', divMax);
    const res = await fetch(`${API_BASE}/info/list/filter?${q.toString()}`);
    if (!res.ok) throw new Error(`fetchStocks(filter) failed: ${res.status}`);
    const body = await res.json();
    return paginate((body.list || []).map(normalizeStock), page, size);
  }

  // 전체 목록
  const res = await fetch(`${API_BASE}/info/list`);
  if (!res.ok) throw new Error(`fetchStocks(list) failed: ${res.status}`);
  const body = await res.json();
  return paginate((body.list || []).map(normalizeStock), page, size);
}

/**
 * 종목 상세 — stock_code로 검색 후 반환 (detail.js 용)
 * ※ 재무제표 상세 API 미구현 — 지표 데이터만 표시
 */
async function fetchStockFull(code) {
  const res = await fetch(`${API_BASE}/info/search?keyword=${encodeURIComponent(code)}`);
  if (!res.ok) throw new Error(`fetchStockFull failed: ${res.status}`);
  const body = await res.json();
  const found = (body.result || []).find((s) => s.stock_code === code);
  if (!found) throw new Error('종목을 찾을 수 없습니다.');

  const s = normalizeStock(found);
  const empty = YEARS.map(() => 0);

  return {
    ...s,
    shares:           '-',
    eps:              null,
    bps:              null,
    debtRatio:        null,
    operatingMargin:  null,
    sector:           '-',
    listedDate:       '-',
    ceo:              '-',
    operatingProfit:  0,
    years:            YEARS,
    revenueHistory:   empty,
    operatingHistory: empty,
    netIncomeHistory: empty,
    assetHistory:     empty,
    debtHistory:      empty,
    equityHistory:    empty,
    roeHistory:       empty,
    debtRatioHistory: empty,
    perHistory:       empty,
    pbrHistory:       empty,
    epsHistory:       empty,
    bpsHistory:       empty,
  };
}
