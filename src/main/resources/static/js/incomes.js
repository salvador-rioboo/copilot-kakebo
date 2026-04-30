/**
 * Income Management Module
 * Handles CRUD operations for income records via REST API
 */

const INCOME_API = '/api/incomes';

// ============= VALIDATION =============
const incomeValidations = {
    description: (value) => {
        const trimmed = value.trim();
        if (!trimmed) return 'Description is required';
        if (trimmed.length < 3) return 'Description must be at least 3 characters';
        if (trimmed.length > 100) return 'Description must not exceed 100 characters';
        return null;
    },
    amount: (value) => {
        const num = parseFloat(value);
        if (!value) return 'Amount is required';
        if (isNaN(num)) return 'Amount must be a valid number';
        if (num <= 0) return 'Amount must be greater than 0';
        return null;
    },
    type: (value) => {
        if (!value) return 'Income type is required';
        return null;
    },
    date: (value) => {
        if (!value) return 'Date is required';
        const selectedDate = new Date(value);
        const today = new Date();
        today.setHours(0, 0, 0, 0);
        if (selectedDate > today) return 'Date cannot be in the future';
        const oneYearAgo = new Date();
        oneYearAgo.setFullYear(oneYearAgo.getFullYear() - 1);
        if (selectedDate < oneYearAgo) return 'Date cannot be more than 1 year in the past';
        return null;
    }
};

// ============= UI HELPERS =============
function escapeHtml(text) {
    const map = {
        '&': '&amp;',
        '<': '&lt;',
        '>': '&gt;',
        '"': '&quot;',
        "'": '&#039;'
    };
    return text.replace(/[&<>"']/g, m => map[m]);
}

function formatDate(dateStr) {
    const date = new Date(dateStr);
    return date.toLocaleDateString('en-US', { year: 'numeric', month: 'long', day: 'numeric' });
}

function showFieldError(fieldName, error) {
    const errorEl = document.getElementById(fieldName + 'Error');
    const inputEl = document.getElementById(fieldName);
    if (error) {
        errorEl.textContent = error;
        errorEl.classList.remove('hidden');
        inputEl.classList.add('border-red-600');
        inputEl.classList.remove('border-gray-300');
    } else {
        errorEl.classList.add('hidden');
        inputEl.classList.remove('border-red-600');
        inputEl.classList.add('border-gray-300');
    }
}

function validateIncomeForm() {
    let isValid = true;
    Object.keys(incomeValidations).forEach(fieldName => {
        const input = document.getElementById(fieldName);
        if (input) {
            const error = incomeValidations[fieldName](input.value);
            showFieldError(fieldName, error);
            if (error) isValid = false;
        }
    });
    const submitBtn = document.getElementById('submitBtn');
    if (submitBtn) submitBtn.disabled = !isValid;
    return isValid;
}

function showSuccess(message) {
    const msgEl = document.getElementById('successMessage');
    const textEl = document.getElementById('successText');
    if (msgEl && textEl) {
        textEl.textContent = message;
        msgEl.classList.remove('hidden');
        setTimeout(() => msgEl.classList.add('hidden'), 4000);
    }
}

function showError(message) {
    const msgEl = document.getElementById('errorMessage');
    const textEl = document.getElementById('errorText');
    if (msgEl && textEl) {
        textEl.textContent = message;
        msgEl.classList.remove('hidden');
        setTimeout(() => msgEl.classList.add('hidden'), 4000);
    }
}

// ============= API OPERATIONS =============
async function loadIncomes() {
    try {
        const response = await fetch(INCOME_API);
        if (!response.ok) throw new Error('Failed to load incomes');
        const incomes = await response.json();
        displayIncomes(incomes);
    } catch (error) {
        console.error('Error loading incomes:', error);
        showError('Failed to load income data');
    }
}

function displayIncomes(incomes) {
    const incomeList = document.getElementById('incomeList');
    const incomeCount = document.getElementById('incomeCount');
    const emptyState = document.getElementById('emptyState');

    if (!incomeList) return;

    incomeList.innerHTML = '';

    if (incomes.length === 0) {
        if (emptyState) emptyState.classList.remove('hidden');
        if (incomeCount) incomeCount.textContent = '(No records)';
        return;
    }

    if (emptyState) emptyState.classList.add('hidden');
    if (incomeCount) incomeCount.textContent = `(${incomes.length} record${incomes.length !== 1 ? 's' : ''})`;

    incomes.forEach(income => {
        const borderColor = income.type === 'PRINCIPAL' ? 'border-green-600' : 'border-blue-600';
        const typeLabel = income.type === 'PRINCIPAL' ? 'Principal (Salary)' : 'Extra';
        const recurringLabel = income.isRecurring ? ' (Recurring)' : '';

        const incomeEl = document.createElement('div');
        incomeEl.className = `flex justify-between items-center p-4 border-l-4 ${borderColor} bg-gray-50`;
        incomeEl.innerHTML = `
            <div class="flex-1">
                <p class="font-semibold">${escapeHtml(income.description)}</p>
                <p class="text-sm text-gray-500">${typeLabel}${recurringLabel} - ${formatDate(income.date)}</p>
            </div>
            <div class="flex items-center space-x-4">
                <span class="text-green-600 font-bold">$${income.amount.toFixed(2)}</span>
                <button type="button" class="text-red-600 hover:text-red-800 font-semibold text-sm" onclick="deleteIncome(${income.id})">
                    Delete
                </button>
            </div>
        `;
        incomeList.appendChild(incomeEl);
    });
}

async function createIncome(incomeData) {
    const submitBtn = document.getElementById('submitBtn');
    if (submitBtn) {
        submitBtn.disabled = true;
        submitBtn.textContent = 'Creating...';
    }

    try {
        const response = await fetch(INCOME_API, {
            method: 'POST',
            headers: { 'Content-Type': 'application/json' },
            body: JSON.stringify(incomeData)
        });

        if (!response.ok) {
            const error = await response.json();
            throw new Error(error.message || 'Failed to create income');
        }

        const newIncome = await response.json();
        showSuccess(`Income "${newIncome.description}" created successfully!`);

        const form = document.getElementById('incomeForm');
        if (form) form.reset();

        const dateInput = document.getElementById('date');
        if (dateInput) dateInput.value = new Date().toISOString().split('T')[0];

        await loadIncomes();
    } catch (error) {
        console.error('Error creating income:', error);
        showError(error.message || 'Failed to create income');
    } finally {
        if (submitBtn) {
            submitBtn.disabled = false;
            submitBtn.textContent = 'Add Income';
        }
    }
}

async function deleteIncome(id) {
    if (!confirm('Are you sure you want to delete this income record?')) return;

    try {
        const response = await fetch(`${INCOME_API}/${id}`, { method: 'DELETE' });
        if (!response.ok) throw new Error('Failed to delete income');
        showSuccess('Income deleted successfully');
        await loadIncomes();
    } catch (error) {
        console.error('Error deleting income:', error);
        showError('Failed to delete income');
    }
}

// ============= EVENT LISTENERS & INITIALIZATION =============
document.addEventListener('DOMContentLoaded', () => {
    // Set today's date as default
    const dateInput = document.getElementById('date');
    const today = new Date().toISOString().split('T')[0];
    if (dateInput) {
        dateInput.value = today;
        dateInput.max = today;
    }

    // Real-time validation
    ['description', 'amount', 'type', 'date'].forEach(fieldName => {
        const input = document.getElementById(fieldName);
        if (input) {
            if (fieldName === 'date') {
                input.addEventListener('blur', () => {
                    showFieldError('date', incomeValidations.date(input.value));
                    validateIncomeForm();
                });
            } else if (fieldName === 'type') {
                input.addEventListener('change', () => {
                    showFieldError('type', incomeValidations.type(input.value));
                    validateIncomeForm();
                });
            } else {
                input.addEventListener('blur', () => {
                    showFieldError(fieldName, incomeValidations[fieldName](input.value));
                    validateIncomeForm();
                });
            }
        }
    });

    // Form submission
    const form = document.getElementById('incomeForm');
    if (form) {
        form.addEventListener('submit', (e) => {
            e.preventDefault();

            if (!validateIncomeForm()) {
                showError('Please fix the errors before submitting');
                return;
            }

            const incomeData = {
                description: document.getElementById('description').value.trim(),
                amount: parseFloat(document.getElementById('amount').value),
                type: document.getElementById('type').value,
                isRecurring: document.getElementById('isRecurring').checked,
                date: document.getElementById('date').value
            };

            createIncome(incomeData);
        });
    }

    // Load incomes on page load
    loadIncomes();
});
