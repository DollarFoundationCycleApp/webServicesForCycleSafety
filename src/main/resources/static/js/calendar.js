(function () {
    var button = document.getElementById('calendar-button');
    var popup = document.getElementById('calendar-popup');
    var title = document.getElementById('calendar-title');
    var days = document.getElementById('calendar-days');
    var shownMonth = new Date();
    shownMonth.setDate(1);

    function render() {
        var year = shownMonth.getFullYear();
        var month = shownMonth.getMonth();
        var today = new Date();
        var firstWeekday = new Date(year, month, 1).getDay();
        var dayCount = new Date(year, month + 1, 0).getDate();

        title.textContent = shownMonth.toLocaleString('en-US', { month: 'long', year: 'numeric' });
        days.innerHTML = '';

        for (var blank = 0; blank < firstWeekday; blank++) {
            days.appendChild(document.createElement('span'));
        }

        for (var day = 1; day <= dayCount; day++) {
            var cell = document.createElement('span');
            cell.className = 'calendar-day';
            cell.textContent = day;
            if (year === today.getFullYear() && month === today.getMonth() && day === today.getDate()) {
                cell.classList.add('today');
            }
            days.appendChild(cell);
        }
    }

    function setOpen(open) {
        popup.hidden = !open;
        button.setAttribute('aria-expanded', String(open));
    }

    function changeMonth(amount) {
        shownMonth.setMonth(shownMonth.getMonth() + amount);
        render();
    }

    button.addEventListener('click', function () {
        if (popup.hidden) {
            render();
        }
        setOpen(popup.hidden);
    });

    document.getElementById('calendar-prev').addEventListener('click', function () {
        changeMonth(-1);
    });

    document.getElementById('calendar-next').addEventListener('click', function () {
        changeMonth(1);
    });

    // Close when clicking outside the date range box or pressing Escape
    document.addEventListener('click', function (event) {
        if (!button.parentElement.contains(event.target)) {
            setOpen(false);
        }
    });

    document.addEventListener('keydown', function (event) {
        if (event.key === 'Escape') {
            setOpen(false);
        }
    });
})();