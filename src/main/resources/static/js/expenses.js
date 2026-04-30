/**
 * Expense Management Module
 * Handles CRUD operations for expense records via REST API
 */

const EXPENSE_API = '/api/expenses';

// ============= VALIDATION =============
const expenseValidations = {
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
    category: (value) => {
        if (!value) return 'Category is required';
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

function getCategoryLabel(category) {
    const labels = {
        'SURVIVAL': 'Survival (Essentials)',
        'ENTERTAINMENT': 'Entertainment (Treats)',
        'CULTURE': 'Culture (Education)',
        'EXTRAS': 'Extras (Unexpected)'
    };
    return labels[category] || category;
}

function getCategoryBorderColor(category) {
    const colors = {
        'SURVIVAL': 'border-blue-600',
        'ENTERTAINMENT': 'border-green-600',
        'CULTURE': 'border-purple-600',
        'EXTRAS': 'border-yellow-600'
    };
    return colors[category] || 'border-gray-600';
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

function validateExpenseForm() {
    let isValid = true;
    Object.keys(expenseValidations).forEach(fieldName => {
        const input = document.getElementById(fieldName);
        if (input) {
            const error = expenseValidations[fieldName](input.value);
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
async function loadExpenses() {
    try {
        const response = await fetch(EXPENSE_API);
        if (!response.ok) throw new Error('Failed to load expenses');
        const expenses = await response.json();
        displayExpenses(expenses);
    } catch (error) {
        console.error('Error loading expenses:', error);
        showError('Failed to load expense data');
    }
}

function displayExpenses(expenses) {
    const expenseList = document.getElementById('expenseList');
    const expenseCount = document.getElementById('expenseCount');
    const emptyState = document.getElementById('emptyState');

    if (!expenseList) return;

    expenseList.innerHTML = '';

    if (expenses.length === 0) {
        if (emptyState) emptyState.classList.remove('hidden');
        if (expenseCount) expenseCount.textContent = '(No records)';
        return;
    }

    if (emptyState) emptyState.classList.add('hidden');
    if (expenseCount) expenseCount.textContent = `(${expenses.length} record${expenses.length !== 1 ? 's' : ''})`;

    expenses.forEach(expense => {
        const borderColor = getCategoryBorderColor(expense.category);
        const categoryLabel = getCategoryLabel(expense.category);
        const fixedLabel = expense.fixed ? ' (Fixed)' : '';
        const recurringLabel = expense.recurring ? ' (Recurring)' : '';

        const expenseEl = document.createElement('div');
        expenseEl.className = `flex justify-between items-center p-4 border-l-4 ${borderColor} bg-gray-50`;
        expenseEl.innerHTML = `
            <div class="flex-1">
                <p class="font-semibold">${escapeHtml(expense.description)}</p>
                <p class="text-sm text-gray-500">${categoryLabel}${fixedLabel}${recurringLabel} - ${formatDate(expense.date)}</p>
            </div>
            <div class="flex items-center space-x-4">
                <span class="text-red-600 font-bold">$${expense.amount.toFixed(2)}</span>
                <button type="button" class="text-red-600 hover:text-red-800 font-semibold text-sm" onclick="deleteExpense(${expense.id})">
                    Delete
                </button>
            </div>
        `;
        expenseList.appendChild(expenseEl);
    });
}

async function createExpense(expenseData) {
    const submitBtn = document.getElementById('submitBtn');
    if (submitBtn) {
        submitBtn.disabled = true;
        submitBtn.textContent = 'Creating...';
    }

    try {
        const response = await fetch(EXPENSE_API, {
            method: 'POST',
            headers: { 'Content-Type': 'application/json' },
            body: JSON.stringify(expenseData)
        });

        if (!response.ok) {
            const error = await response.json();
            throw new Error(error.message || 'Failed to create expense');
        }

        const newExpense = await response.json();
        showSuccess(`Expense "${newExpense.description}" created successfully!`);

        const form = document.getElementById('expenseForm');
        if (form) form.reset();

        const dateInput = document.getElementById('date');
        if (dateInput) dateInput.value = new Date().toISOString().split('T')[0];

        await loadExpenses();
    } catch (error) {
        console.error('Error creating expense:', error);
        showError(error.message || 'Failed to create expense');
    } finally {
        if (submitBtn) {
            submitBtn.disabled = false;
            submitBtn.textContent = 'Add Expense';
        }
    }
}

async function deleteExpense(id) {
    if (!confirm('Are you sure you want to delete this expense record?')) return;

    try {
        const response = await fetch(`${EXPENSE_API}/${id}`, { method: 'DELETE' });
        if (!response.ok) throw new Error('Failed to delete expense');
        showSuccess('Expense deleted successfully');
        await loadExpenses();
    } catch (error) {
        console.error('Error deleting expense:', error);
        showError('Failed to delete expense');
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
    ['description', 'amount', 'category', 'date'].forEach(fieldName => {
        const input = document.getElementById(fieldName);
        if (input) {
            if (fieldName === 'date') {
                input.addEventListener('blur', () => {
                    showFieldError('date', expenseValidations.date(input.value));
                    validateExpenseForm();
                });
            } else if (fieldName === 'category') {
                input.addEventListener('change', () => {
                    showFieldError('category', expenseValidations.category(input.value));
                    validateExpenseForm();
                });
            } else {
                input.addEventListener('blur', () => {
                    showFieldError(fieldName, expenseValidations[fieldName](input.value));
                    validateExpenseForm();
                });
            }
        }
    });

    // Form submission
    const form = document.getElementById('expenseForm');
    if (form) {
        form.addEventListener('submit', (e) => {
            e.preventDefault();

            if (!validateExpenseForm()) {
                showError('Please fix the errors before submitting');
                return;
            }

            const expenseData = {
                description: document.getElementById('description').value.trim(),
                amount: parseFloat(document.getElementById('amount').value),
                category: document.getElementById('category').value,
                fixed: document.getElementById('isFixed').checked,
                recurring: document.getElementById('isRecurring').checked,
                date: document.getElementById('date').value
            };

            createExpense(expenseData);
        });
    }

    // Load expenses on page load
    loadExpenses();
});
