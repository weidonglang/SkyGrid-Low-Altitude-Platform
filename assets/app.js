
(function(){
  const q = document.getElementById('searchBox');
  if(q){
    q.addEventListener('input', function(){
      const v = q.value.trim().toLowerCase();
      document.querySelectorAll('[data-doc-link]').forEach(a => {
        const hay = (a.textContent + ' ' + a.getAttribute('data-path')).toLowerCase();
        a.style.display = hay.includes(v) ? 'block' : 'none';
      });
    });
  }
  document.querySelectorAll('pre').forEach(pre => {
    const btn = document.createElement('button');
    btn.className = 'copy-btn';
    btn.textContent = '复制';
    btn.addEventListener('click', async () => {
      const text = pre.innerText.replace(/^复制\n?/, '');
      try { await navigator.clipboard.writeText(text); btn.textContent='已复制'; setTimeout(()=>btn.textContent='复制', 1200); }
      catch(e){ btn.textContent='复制失败'; setTimeout(()=>btn.textContent='复制', 1200); }
    });
    pre.appendChild(btn);
  });
})();
