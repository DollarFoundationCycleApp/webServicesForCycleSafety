document.getElementById('logout-button').addEventListener('click', async () => {
    try {
        await fetch('/api/logout', {
            method: 'POST',
        });
    } finally {
        window.location.href = '/';
    }
});