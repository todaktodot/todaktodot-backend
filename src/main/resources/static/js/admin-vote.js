let voteActionTargetId = null;

function openVoteDetail(voteId) {
    fetch('/admin/vote/' + voteId)
        .then(res => res.text())
        .then(html => {
            document.getElementById('voteDetailModalContent').innerHTML = html;
            openModal('voteDetailModal');
        })
        .catch(handleVoteActionError);
}

function requestVoteHide(voteId) {
    voteActionTargetId = voteId;
    openModal('voteHideModal');
}

function requestVoteRestore(voteId) {
    voteActionTargetId = voteId;
    openModal('voteRestoreModal');
}

function requestVoteDelete(voteId) {
    voteActionTargetId = voteId;
    openModal('voteDeleteModal');
}

function submitVoteHide() {
    fetch('/admin/vote/' + voteActionTargetId + '/hide', { method: 'POST' })
        .then(handleVoteActionResponse)
        .catch(handleVoteActionError);
}

function submitVoteRestore() {
    fetch('/admin/vote/' + voteActionTargetId + '/restore', { method: 'POST' })
        .then(handleVoteActionResponse)
        .catch(handleVoteActionError);
}

function submitVoteDelete() {
    fetch('/admin/vote/' + voteActionTargetId, { method: 'DELETE' })
        .then(handleVoteActionResponse)
        .catch(handleVoteActionError);
}

function handleVoteActionResponse(response) {
    if (!response.ok) {
        return response.json().then(body => { throw new Error(body.message || '처리 중 오류가 발생했습니다.'); });
    }
    window.location.reload();
}

function handleVoteActionError(error) {
    alert(error.message || '처리 중 오류가 발생했습니다.');
}

function validateVoteDateRange() {
    const startDt = document.getElementById('startDt');
    const endDt = document.getElementById('endDt');
    if (startDt.value && endDt.value && startDt.value > endDt.value) {
        alert('시작일은 종료일보다 이전이어야 해요.');
        return false;
    }
    return true;
}
