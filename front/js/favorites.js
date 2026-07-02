document.addEventListener('DOMContentLoaded', async () => {
  initHeader('watchlist');

  if (!localStorage.getItem('accessToken')) {
    window.location.href = 'login.html';
    return;
  }

  const countEl = document.getElementById('stockCount');
  const cardList = document.getElementById('cardList');

  await loadFavoriteState();

  let stocks = [];
  try {
    stocks = await fetchFavorites();
  } catch (e) {
    console.error('관심종목 로드 실패:', e);
    countEl.textContent = '관심종목을 불러올 수 없습니다.';
    cardList.innerHTML = '<div class="empty-state">관심종목을 불러올 수 없습니다.</div>';
    return;
  }

  render(stocks);

  function render(data) {
    countEl.textContent = `총 ${data.length}개 종목`;

    if (data.length === 0) {
      cardList.innerHTML = '<div class="empty-state">관심종목으로 등록한 종목이 없습니다.</div>';
      return;
    }

    cardList.innerHTML = data.map((s) => renderStockCard(s)).join('');
    // 관심종목 페이지에서는 별 해제가 서버에 반영된 직후 목록에서 바로 제거
    bindStockCards(cardList, (code, active) => {
      if (!active) {
        stocks = stocks.filter((s) => s.code !== code);
        render(stocks);
      }
    });
  }
});
