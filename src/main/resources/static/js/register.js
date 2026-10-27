const registerForm = document.getElementById('register-form');
const registerMessage = document.getElementById('register-message');

document.querySelectorAll('.toggle-password').forEach((toggle) => {
    toggle.addEventListener('click', () => togglePasswordVisibility(toggle));
});

registerForm.addEventListener('submit', (event) => {
    // Account creation is not connected to the server yet, so keep the page
    // from submitting and report why nothing happened.
    event.preventDefault();

    registerMessage.textContent = '';

    const password = document.getElementById('password').value;
    const confirmPassword = document.getElementById('confirm-password').value;

    if (password !== confirmPassword) {
        registerMessage.textContent = 'Passwords do not match.';
        return;
    }

    registerMessage.textContent = 'Account creation is not available yet.';
});

function togglePasswordVisibility(toggle) {
    const input = document.getElementById(toggle.dataset.target);
    if (input.type === 'password') {
        input.type = 'text';
        toggle.textContent = 'Hide';
    } else {
        input.type = 'password';
        toggle.textContent = 'Show';
    }
}
