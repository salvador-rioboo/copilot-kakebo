/**
 * Dashboard Module
 * Handles loading and rendering dashboard summary and analytics
 */

const DASHBOARD_API = '/api/dashboard/summary';

// ============= UI HELPERS =============
function formatCurrency(amount) {
    return `$${parseFloat(amount).toFixed(2)}`;
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
async function loadDashboardSummary() {
    try {
        const response = await fetch(DASHBOARD_API);
        if (!response.ok) throw new Error('Failed to load dashboard');
        const summary = await response.json();
        updateDashboard(summary);
    } catch (error) {
        console.error('Error loading dashboard:', error);
        showError('Failed to load dashboard data');
    }
}

function updateDashboard(summary) {
    // Update summary cards
    updateSummaryCards(summary);

    // Update savings section
    updateSavingsSection(summary);

    // Update category breakdown
    updateCategoryBreakdown(summary);
}

function updateSummaryCards(summary) {
    const totalIncomeEl = document.getElementById('totalIncome');
    const totalExpensesEl = document.getElementById('totalExpenses');
    const availableMoneyEl = document.getElementById('availableMoney');

    if (totalIncomeEl) {
        totalIncomeEl.textContent = formatCurrency(summary.totalIncome || 0);
    }

    if (totalExpensesEl) {
        totalExpensesEl.textContent = formatCurrency(summary.totalExpenses || 0);
    }

    const availableMoney = (summary.totalIncome || 0) - (summary.totalExpenses || 0);
    if (availableMoneyEl) {
        availableMoneyEl.textContent = formatCurrency(availableMoney);
        // Change color based on positive/negative
        const cardClass = availableMoney >= 0 ? 'text-blue-600' : 'text-red-600';
        availableMoneyEl.className = `text-2xl font-bold ${cardClass}`;
    }
}

function updateSavingsSection(summary) {
    const plannedSavingsEl = document.getElementById('plannedSavings');
    const actualSavingsEl = document.getElementById('actualSavings');

    // Planned savings = 10% of total income (Kakebo method)
    const plannedSavings = (summary.totalIncome || 0) * 0.1;

    if (plannedSavingsEl) {
        plannedSavingsEl.textContent = formatCurrency(plannedSavings);
    }

    // Actual savings = income - expenses
    const actualSavings = (summary.totalIncome || 0) - (summary.totalExpenses || 0);
    if (actualSavingsEl) {
        actualSavingsEl.textContent = formatCurrency(actualSavings);
        const textColor = actualSavings >= plannedSavings ? 'text-green-600' : 'text-orange-600';
        actualSavingsEl.className = `text-xl font-bold ${textColor}`;
    }
}

function updateCategoryBreakdown(summary) {
    const breakdownContainer = document.getElementById('categoryBreakdown');
    if (!breakdownContainer) return;

    breakdownContainer.innerHTML = '';

    if (!summary.categoryTotals || Object.keys(summary.categoryTotals).length === 0) {
        breakdownContainer.innerHTML = '<p class="text-gray-500 text-center py-4">No expenses recorded</p>';
        return;
    }

    const categoryLabels = {
        'SURVIVAL': 'Survival (Food, Transport)',
        'ENTERTAINMENT': 'Entertainment',
        'CULTURE': 'Culture & Education',
        'EXTRAS': 'Extras & Unexpected'
    };

    Object.entries(summary.categoryTotals).forEach(([category, total]) => {
        const label = categoryLabels[category] || category;
        const el = document.createElement('div');
        el.className = 'flex justify-between items-center p-3 bg-gray-50 rounded';
        el.innerHTML = `
            <span class="font-semibold">${label}</span>
            <span class="text-red-600 font-bold">${formatCurrency(total)}</span>
        `;
        breakdownContainer.appendChild(el);
    });
}

// ============= INITIALIZATION =============
document.addEventListener('DOMContentLoaded', () => {
    loadDashboardSummary();

    // Refresh dashboard every 30 seconds
    setInterval(loadDashboardSummary, 30000);
});
