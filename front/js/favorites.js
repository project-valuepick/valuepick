document.addEventListener('DOMContentLoaded', async () => {
  initHeader('watchlist');

  if (!localStorage.getItem('accessToken')) {
    window.location.href = 'login.html';
    return;
  }

  const countEl = document.getElementById('stockCount');
  const tableBody = document.getElementById('tableBody');
  const cardList = document.getElementById('cardList');

  await loadFavoriteState();

  let stocks = [];
  try {
    stocks = await fetchFavorites();
  } catch (e) {
    console.error('관심종목 로드 실패:', e);
    countEl.textContent = '관심종목을 불러올 수 없습니다.';
    tableBody.innerHTML = `<tr><td colspan="8"><div class="empty-state">관심종목을 불러올 수 없습니다.</div></td></tr>`;
    cardList.innerHTML = '<div class="empty-state">관심종목을 불러올 수 없습니다.</div>';
    return;
  }

  render(stocks);

  function render(data) {
    countEl.textContent = `총 ${data.length}개 종목`;

    if (data.length === 0) {
      tableBody.innerHTML = `<tr><td colspan="8"><div class="empty-state">관심종목으로 등록한 종목이 없습니다.</div></td></tr>`;
      cardList.innerHTML = '<div class="empty-state">관심종목으로 등록한 종목이 없습니다.</div>';
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

    cardList.innerHTML = data.map((s) => renderStockCard(s)).join('');

    tableBody.querySelectorAll('tr[data-code]').forEach((row) => {
      row.addEventListener('click', () => goToDetail(row.dataset.code));
    });

    // 관심종목 페이지에서는 별 해제가 서버에 반영된 직후 목록에서 바로 제거
    bindStockCards(cardList, (code, active) => {
      if (!active) {
        stocks = stocks.filter((s) => s.code !== code);
        render(stocks);
      }
    });
  }
});
