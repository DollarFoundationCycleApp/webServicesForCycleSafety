document.getElementById('logout-button').addEventListener('click', async () => {
    try {
        await fetch('/api/logout', {
            method: 'POST',
            headers: {'Authorization': sessionStorage.getItem('sessionToken') || ''}
        });
    } finally {
        sessionStorage.clear();
        window.location.href = '/';
    }
});