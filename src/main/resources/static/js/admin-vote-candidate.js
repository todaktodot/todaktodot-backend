function toggleCandidateCard(checkbox) {
    checkbox.closest('.candidate-card').classList.toggle('checked', checkbox.checked);
}

function collectCheckedCards() {
    return Array.from(document.querySelectorAll('.candidate-card'))
        .filter(card => card.querySelector('.candidate-check').checked);
}

// 응답이 올 때까지 버튼을 막는다. 등록은 되돌릴 수 없어 두 번 눌리면 같은 투표가 두 개 올라간다.
function lockToolbar(button) {
    document.querySelectorAll('.candidate-toolbar button').forEach(btn => btn.disabled = true);
    if (button) {
        button.dataset.pending = 'true';
    }
}

function unlockToolbar() {
    document.querySelectorAll('.candidate-toolbar button').forEach(btn => {
        btn.disabled = false;
        delete btn.dataset.pending;
    });
}

function generateCandidates(button) {
    document.getElementById('generateLoading').classList.add('active');
    lockToolbar(button);

    postJson('/admin/vote-candidate/generate', null)
        .then(body => finishCandidateAction(body.message))
        .catch(error => {
            document.getElementById('generateLoading').classList.remove('active');
            unlockToolbar();
            handleCandidateError(error);
        });
}

function approveSelected(button) {
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

    lockToolbar(button);

    postJson('/admin/vote-candidate/approve', payload)
        .then(body => {
            // 일부만 실패할 수 있어 사유를 함께 보여준다.
            const message = (body.failures && body.failures.length > 0)
                ? body.message + '\n\n등록하지 못한 건:\n' + body.failures.join('\n')
                : body.message;
            finishCandidateAction(message);
        })
        .catch(error => {
            unlockToolbar();
            handleCandidateError(error);
        });
}

function rejectSelected(button) {
    const cards = collectCheckedCards();
    if (cards.length === 0) {
        alert('반려할 후보를 선택해주세요.');
        return;
    }

    if (!confirm(cards.length + '개의 후보를 반려할까요?')) {
        return;
    }

    const payload = cards.map(card => Number(card.dataset.candidateId));

    lockToolbar(button);

    postJson('/admin/vote-candidate/reject', payload)
        .then(body => finishCandidateAction(body.message))
        .catch(error => {
            unlockToolbar();
            handleCandidateError(error);
        });
}

function postJson(url, payload) {
    const options = { method: 'POST' };
    if (payload !== null) {
        options.headers = { 'Content-Type': 'application/json' };
        options.body = JSON.stringify(payload);
    }

    return fetch(url, options).then(response => {
        return response.json()
            .catch(() => { throw new Error('처리 중 오류가 발생했습니다.'); })
            .then(body => {
                if (!response.ok) {
                    throw new Error(body.message || '처리 중 오류가 발생했습니다.');
                }
                return body;
            });
    });
}

// 처리 결과를 반드시 보여준 뒤 새로고침한다. 0건 처리를 성공으로 오해하면 안 된다.
function finishCandidateAction(message) {
    if (message) {
        alert(message);
    }
    window.location.reload();
}

function handleCandidateError(error) {
    alert(error.message || '처리 중 오류가 발생했습니다.');
}
