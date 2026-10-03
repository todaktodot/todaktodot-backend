function toggleCandidateCard(checkbox) {
    checkbox.closest('.candidate-card').classList.toggle('checked', checkbox.checked);
}

function collectCheckedCards() {
    return Array.from(document.querySelectorAll('.candidate-card'))
        .filter(card => card.querySelector('.candidate-check').checked);
}

function generateCandidates(button) {
    const loading = document.getElementById('generateLoading');
    loading.classList.add('active');

    // AI 호출이 길어 두 번 눌리기 쉽다. 응답이 올 때까지 막는다.
    button.disabled = true;

    fetch('/admin/vote-candidate/generate', { method: 'POST' })
        .then(handleCandidateResponse)
        .catch(error => {
            loading.classList.remove('active');
            button.disabled = false;
            handleCandidateError(error);
        });
}

function approveSelected() {
    const cards = collectCheckedCards();
    if (cards.length === 0) {
        alert('등록할 후보를 선택해주세요.');
        return;
    }

    if (!confirm(cards.length + '개의 투표를 지금 등록할까요? 등록하면 바로 앱에 게시되며 되돌릴 수 없습니다.')) {
        return;
    }

    const payload = cards.map(card => ({
        candidateId: Number(card.dataset.candidateId),
        title: card.querySelector('.candidate-title').value.trim(),
        // 빈 값을 걸러내지 않는다. 걸러내면 어드민이 지운 선택지가 서버에서 원래 값으로 되돌아간다.
        options: Array.from(card.querySelectorAll('.candidate-option'))
            .map(input => input.value.trim())
    }));

    fetch('/admin/vote-candidate/approve', {
        method: 'POST',
        headers: { 'Content-Type': 'application/json' },
        body: JSON.stringify(payload)
    })
        .then(response => {
            if (!response.ok) {
                return response.json().then(body => { throw new Error(body.message || '처리 중 오류가 발생했습니다.'); });
            }
            return response.json();
        })
        .then(body => {
            // 일부만 실패할 수 있어 사유를 함께 보여준다.
            if (body.failures && body.failures.length > 0) {
                alert(body.message + '\n\n등록하지 못한 건:\n' + body.failures.join('\n'));
            }
            window.location.reload();
        })
        .catch(handleCandidateError);
}

function rejectSelected() {
    const cards = collectCheckedCards();
    if (cards.length === 0) {
        alert('반려할 후보를 선택해주세요.');
        return;
    }

    if (!confirm(cards.length + '개의 후보를 반려할까요?')) {
        return;
    }

    const payload = cards.map(card => Number(card.dataset.candidateId));

    fetch('/admin/vote-candidate/reject', {
        method: 'POST',
        headers: { 'Content-Type': 'application/json' },
        body: JSON.stringify(payload)
    })
        .then(handleCandidateResponse)
        .catch(handleCandidateError);
}

function handleCandidateResponse(response) {
    if (!response.ok) {
        return response.json().then(body => { throw new Error(body.message || '처리 중 오류가 발생했습니다.'); });
    }
    window.location.reload();
}

function handleCandidateError(error) {
    alert(error.message || '처리 중 오류가 발생했습니다.');
}
