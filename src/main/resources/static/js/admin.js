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

// Filter form auto-submit on change
document.addEventListener('DOMContentLoaded', function() {
    const filterSelects = document.querySelectorAll('.filter-section select');
    filterSelects.forEach(select => {
        select.addEventListener('change', function() {
            this.closest('form').submit();
        });
    });
});
