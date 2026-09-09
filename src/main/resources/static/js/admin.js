// Admin JS

// Modal Functions
function openModal(modalId) {
    const modal = document.getElementById(modalId);
    if (modal) {
        modal.classList.add('active');
        document.body.style.overflow = 'hidden';
    }
}

function closeModal(modalId) {
    const modal = document.getElementById(modalId);
    if (modal) {
        modal.classList.remove('active');
        document.body.style.overflow = '';
    }
}

// Close modal when clicking outside
document.addEventListener('click', function(e) {
    if (e.target.classList.contains('modal-overlay')) {
        e.target.classList.remove('active');
        document.body.style.overflow = '';
    }
});

// Delete confirmation
function confirmDelete(cardId, cardTitle) {
    if (confirm(`"${cardTitle}" 카드를 삭제하시겠습니까?\n이 작업은 되돌릴 수 없습니다.`)) {
        document.getElementById('deleteForm-' + cardId).submit();
    }
}

// Delete confirmation from data attributes (for Thymeleaf security)
function confirmDeleteFromData(element) {
    const cardId = element.dataset.cardId;
    const cardTitle = element.dataset.cardTitle;
    confirmDelete(cardId, cardTitle);
}

// Form validation
function validateForm(formId) {
    const form = document.getElementById(formId);
    if (!form) return true;

    const requiredFields = form.querySelectorAll('[required]');
    let isValid = true;

    requiredFields.forEach(field => {
        if (!field.value.trim()) {
            field.style.borderColor = '#e74c3c';
            isValid = false;
        } else {
            field.style.borderColor = '#dce4ec';
        }
    });

    return isValid;
}

// Sidebar tree navigation toggle with localStorage persistence
var SIDEBAR_STORAGE_KEY = 'sidebar-open-categories';

document.addEventListener('DOMContentLoaded', function() {
    var categories = document.querySelectorAll('.nav-category');
    var allSubmenus = document.querySelectorAll('.nav-submenu');
    var saved = JSON.parse(localStorage.getItem(SIDEBAR_STORAGE_KEY) || 'null');

    // 상태 복원 중 애니메이션 비활성화
    allSubmenus.forEach(function(submenu) {
        submenu.style.transition = 'none';
    });

    // localStorage에 저장된 상태가 있으면 복원
    if (saved !== null) {
        categories.forEach(function(cat) {
            var key = cat.dataset.category;
            var submenu = cat.querySelector('.nav-submenu');
            if (saved.indexOf(key) !== -1) {
                cat.classList.add('open');
                submenu.style.height = submenu.scrollHeight + 'px';
            } else {
                cat.classList.remove('open');
                submenu.style.height = '0';
            }
        });
    }

    // 현재 activeMenu가 속한 카테고리는 항상 열림 보장
    var activeLink = document.querySelector('.nav-submenu a.active');
    if (activeLink) {
        var activeCat = activeLink.closest('.nav-category');
        if (activeCat && !activeCat.classList.contains('open')) {
            activeCat.classList.add('open');
            var submenu = activeCat.querySelector('.nav-submenu');
            submenu.style.height = submenu.scrollHeight + 'px';
            saveState();
        }
    }

    // 저장된 상태가 없으면 서버 기본값 기준으로 높이 설정
    if (saved === null) {
        document.querySelectorAll('.nav-category.open > .nav-submenu').forEach(function(submenu) {
            submenu.style.height = submenu.scrollHeight + 'px';
        });
    }

    // 복원 완료 후 다음 프레임에서 트랜지션 복원
    requestAnimationFrame(function() {
        requestAnimationFrame(function() {
            allSubmenus.forEach(function(submenu) {
                submenu.style.transition = '';
            });
        });
    });

    function saveState() {
        var openKeys = [];
        document.querySelectorAll('.nav-category.open').forEach(function(cat) {
            openKeys.push(cat.dataset.category);
        });
        localStorage.setItem(SIDEBAR_STORAGE_KEY, JSON.stringify(openKeys));
    }

    // 토글 클릭 이벤트
    document.querySelectorAll('.nav-category-toggle').forEach(function(toggle) {
        toggle.addEventListener('click', function() {
            var category = this.closest('.nav-category');
            var submenu = category.querySelector('.nav-submenu');

            if (category.classList.contains('open')) {
                submenu.style.height = submenu.scrollHeight + 'px';
                requestAnimationFrame(function() {
                    submenu.style.height = '0';
                });
                category.classList.remove('open');
            } else {
                category.classList.add('open');
                submenu.style.height = submenu.scrollHeight + 'px';
                submenu.addEventListener('transitionend', function handler() {
                    if (category.classList.contains('open')) {
                        submenu.style.height = 'auto';
                    }
                    submenu.removeEventListener('transitionend', handler);
                });
            }
            saveState();
        });
    });
});

// Auto-hide alerts after 5 seconds
document.addEventListener('DOMContentLoaded', function() {
    const alerts = document.querySelectorAll('.alert');
    alerts.forEach(alert => {
        setTimeout(() => {
            alert.style.opacity = '0';
            alert.style.transition = 'opacity 0.5s ease';
            setTimeout(() => alert.remove(), 500);
        }, 5000);
    });
});

// Dynamic option management for card edit
let optionCount = {};

function addOption(questionNo) {
    const container = document.getElementById('options-' + questionNo);
    if (!container) return;

    if (!optionCount[questionNo]) {
        optionCount[questionNo] = container.querySelectorAll('.option-input').length;
    }
    optionCount[questionNo]++;

    const optionDiv = document.createElement('div');
    optionDiv.className = 'option-item';
    optionDiv.innerHTML = `
        <span class="option-number">${optionCount[questionNo]}</span>
        <input type="text"
               name="questions[${questionNo}].options[${optionCount[questionNo] - 1}].optionCnts"
               class="option-input"
               placeholder="선택지 내용">
        <button type="button" class="btn btn-danger btn-sm" onclick="removeOption(this)">삭제</button>
    `;
    container.appendChild(optionDiv);
}

function removeOption(button) {
    const optionItem = button.closest('.option-item');
    if (optionItem) {
        optionItem.remove();
    }
}

// Filter form auto-submit on change (opt out with class="no-auto-submit")
document.addEventListener('DOMContentLoaded', function() {
    const filterSelects = document.querySelectorAll('.filter-section select:not(.no-auto-submit)');
    filterSelects.forEach(select => {
        select.addEventListener('change', function() {
            this.closest('form').submit();
        });
    });
});
