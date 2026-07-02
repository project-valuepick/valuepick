document.addEventListener('DOMContentLoaded', () => {
  initHeader('admin');

  const logEl = document.getElementById('syncLog');

  function pushLog(text) {
    const now = new Date().toLocaleTimeString('ko-KR');
    const item = document.createElement('div');
    item.className = 'log-item';
    item.textContent = `[${now}] ${text}`;
    logEl.prepend(item);
  }

  document.querySelectorAll('button[data-sync]').forEach((btn) => {
    btn.addEventListener('click', async () => {
      const type = btn.dataset.sync;
      btn.disabled = true;
      btn.textContent = '실행 중...';
      pushLog(`${type} 데이터 수집 시작`);

      await new Promise((resolve) => setTimeout(resolve, 900));

      btn.disabled = false;
      btn.textContent = '실행';
      pushLog(`${type} 데이터 수집 완료`);
    });
  });
});
