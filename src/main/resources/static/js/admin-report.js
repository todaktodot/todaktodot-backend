let reportActionTargetId = null;

function openReportDetail(userId) {
    fetch('/admin/reports/' + userId)
        .then(res => res.text())
        .then(html => {
            document.getElementById('reportDetailModalContent').innerHTML = html;
            openModal('reportDetailModal');
        })
        .catch(handleReportActionError);
}

function requestSuspend(userId) {
    reportActionTargetId = userId;
    resetSuspendModal();
    openModal('reportSuspendModal');
}

function requestRelease(userId) {
    reportActionTargetId = userId;
    resetReleaseModal();
    openModal('reportReleaseModal');
}

const suspendReasonChips = document.querySelectorAll('.suspend-reason-chip');
const suspendReason = document.getElementById('suspendReason');
const suspendReasonType = document.getElementById('suspendReasonType');
const suspendConfirmBtn = document.getElementById('suspendConfirmBtn');

suspendReasonChips.forEach((chip) => {
    chip.addEventListener('click', () => {
        // 1. 기존 선택 제거
        suspendReasonChips.forEach((item) => {
            item.classList.remove('active');
        });
        // 2. 현재 선택
        chip.classList.add('active');
        // 3. 선택한 사유 코드 저장
        suspendReasonType.value = chip.dataset.reason;
        // 4. 대응되는 기본 문구 입력
        suspendReason.value = chip.dataset.message;
        // 5. textarea 활성화 → 직접 편집 가능
        suspendReason.disabled = false;
        // 6. 정지 버튼 활성화
        suspendConfirmBtn.disabled = false;
    });
});

const releaseReasonChips = document.querySelectorAll('.release-reason-chip');
const releaseReason = document.getElementById('releaseReason');
const releaseReasonType = document.getElementById('releaseReasonType');
const releaseConfirmBtn = document.getElementById('releaseConfirmBtn');

releaseReasonChips.forEach((chip) => {
    chip.addEventListener('click', () => {
        // 1. 기존 선택 제거
        releaseReasonChips.forEach((item) => {
            item.classList.remove('active');
        });
        // 2. 현재 선택
        chip.classList.add('active');
        // 3. 선택한 사유 코드 저장
        releaseReasonType.value = chip.dataset.reason;
        // 4. 대응되는 기본 문구 입력
        releaseReason.value = chip.dataset.message;
        // 5. textarea 활성화 → 직접 편집 가능
        releaseReason.disabled = false;
        // 6. 정지 버튼 활성화
        releaseConfirmBtn.disabled = false;
    });
});

function submitReportSuspend() {
    const type = document.getElementById('suspendReasonType').value;
    const text = document.getElementById('suspendReason').value;

    fetch('/admin/reports/suspend', { method: 'POST' ,
        headers: {
            'Content-Type': 'application/json'
        },
        body: JSON.stringify({
            userId: reportActionTargetId,
            type: type,
            reason: text,
        })
    })
        .then(handleReportActionResponse)
        .catch(handleReportActionError);
}

function submitReportRelease() {
    const type = document.getElementById('releaseReasonType').value;
    const text = document.getElementById('releaseReason').value;

    fetch('/admin/reports/release', { method: 'POST' ,
        headers: {
            'Content-Type': 'application/json'
        },
        body: JSON.stringify({
            userId: reportActionTargetId,
            type: type,
            reason: text,
        })})
        .then(handleReportActionResponse)
        .catch(handleReportActionError);
}

function handleReportActionResponse(response) {
    if (!response.ok) {
        return response.json().then(body => { throw new Error(body.message || '처리 중 오류가 발생했습니다.'); });
    }
    window.location.reload();
}

function handleReportActionError(error) {
    alert(error.message || '처리 중 오류가 발생했습니다.');
}

function resetSuspendModal() {
    const suspendReason = document.getElementById('suspendReason');
    const suspendReasonType = document.getElementById('suspendReasonType');
    const suspendConfirmBtn = document.getElementById('suspendConfirmBtn');
    const suspendReasonChips = document.querySelectorAll('.suspend-reason-chip');

    suspendReasonChips.forEach((chip) => {
        chip.classList.remove('active');
    });

    suspendReason.value = '';
    suspendReasonType.value = '';
    suspendReason.disabled = true;
    suspendConfirmBtn.disabled = true;
}

function resetReleaseModal() {
    const releaseReason = document.getElementById('releaseReason');
    const releaseReasonType = document.getElementById('releaseReasonType');
    const releaseConfirmBtn = document.getElementById('releaseConfirmBtn');
    const releaseReasonChips = document.querySelectorAll('.release-reason-chip');

    releaseReasonChips.forEach((chip) => {
        chip.classList.remove('active');
    });

    releaseReason.value = '';
    releaseReasonType.value = '';
    releaseReason.disabled = true;
    releaseConfirmBtn.disabled = true;
}