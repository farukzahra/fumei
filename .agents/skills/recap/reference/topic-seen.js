/** Embed at end of recap HTML (before </body>). Requires .topic[data-topic-id] cards. */
(function initRecapTopicSeen() {
  const storageKey = (id) => `recap-seen:${location.pathname}:${id}`

  const cards = Array.from(document.querySelectorAll('.topic[data-topic-id]'))
  const counter = document.querySelector('[data-recap-progress]')

  function updateCounter() {
    if (!counter) return
    const seen = cards.filter((c) => c.classList.contains('topic--seen')).length
    counter.textContent = `${seen}/${cards.length} vistos`
    counter.hidden = cards.length === 0
  }

  function setSeen(card, seen) {
    const id = card.dataset.topicId
    if (!id) return
    card.classList.toggle('topic--seen', seen)
    card.setAttribute('aria-pressed', seen ? 'true' : 'false')
    if (seen) localStorage.setItem(storageKey(id), '1')
    else localStorage.removeItem(storageKey(id))
    updateCounter()
  }

  cards.forEach((card) => {
    const id = card.dataset.topicId
    if (localStorage.getItem(storageKey(id)) === '1') {
      card.classList.add('topic--seen')
    }
    card.setAttribute('role', 'button')
    card.setAttribute('tabindex', '0')
    card.setAttribute('aria-pressed', card.classList.contains('topic--seen') ? 'true' : 'false')
    card.setAttribute('aria-label', `${card.querySelector('h2')?.textContent ?? 'Tópico'} — marcar como visto`)

    function toggle() {
      setSeen(card, !card.classList.contains('topic--seen'))
    }

    card.addEventListener('click', toggle)
    card.addEventListener('keydown', (event) => {
      if (event.key === 'Enter' || event.key === ' ') {
        event.preventDefault()
        toggle()
      }
    })
  })

  updateCounter()
})()
