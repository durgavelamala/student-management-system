/* =========================================================
   Student Management System - small UI helpers
   (No frameworks: plain JavaScript)
   ========================================================= */
document.addEventListener('DOMContentLoaded', function () {

    // 1. Mobile sidebar open / close
    const sidebar = document.getElementById('sidebar');
    const toggle = document.getElementById('menuToggle');
    const backdrop = document.getElementById('sidebarBackdrop');
    if (sidebar && toggle) {
        toggle.addEventListener('click', function () {
            sidebar.classList.toggle('open');
            if (backdrop) backdrop.classList.toggle('show');
        });
    }
    if (backdrop) {
        backdrop.addEventListener('click', function () {
            sidebar.classList.remove('open');
            backdrop.classList.remove('show');
        });
    }

    // 2. Show / hide password on the login page
    document.querySelectorAll('.toggle-password').forEach(function (button) {
        button.addEventListener('click', function () {
            const input = document.getElementById(button.dataset.target);
            const icon = button.querySelector('i');
            const show = input.type === 'password';
            input.type = show ? 'text' : 'password';
            icon.className = show ? 'bi bi-eye-slash' : 'bi bi-eye';
            button.setAttribute('aria-label', show ? 'Hide password' : 'Show password');
        });
    });

    // 3. Ask for confirmation before delete forms are submitted
    document.querySelectorAll('form[data-confirm]').forEach(function (form) {
        form.addEventListener('submit', function (event) {
            if (!window.confirm(form.dataset.confirm)) {
                event.preventDefault();
            }
        });
    });

    // 4. Close buttons and auto-hide for success messages
    document.querySelectorAll('.alert-close').forEach(function (button) {
        button.addEventListener('click', function () {
            button.closest('.alert').remove();
        });
    });
    document.querySelectorAll('.alert[data-autohide]').forEach(function (alert) {
        setTimeout(function () {
            alert.style.transition = 'opacity 0.4s';
            alert.style.opacity = '0';
            setTimeout(function () { alert.remove(); }, 400);
        }, 5000);
    });

    // 5. "All Present" / "All Absent" buttons on the attendance page
    document.querySelectorAll('[data-mark-all]').forEach(function (button) {
        button.addEventListener('click', function () {
            const value = button.dataset.markAll;
            document.querySelectorAll('input[type="radio"][value="' + value + '"]').forEach(function (radio) {
                radio.checked = true;
            });
        });
    });

    // 6. Instant filter for tables: <input data-table-filter="tableId">
    document.querySelectorAll('[data-table-filter]').forEach(function (input) {
        const table = document.getElementById(input.dataset.tableFilter);
        if (!table) return;
        input.addEventListener('input', function () {
            const term = input.value.toLowerCase();
            table.querySelectorAll('tbody tr').forEach(function (row) {
                row.style.display = row.textContent.toLowerCase().includes(term) ? '' : 'none';
            });
        });
    });
});
