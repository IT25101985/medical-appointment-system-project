// Global Theme Management
(function() {
    const savedTheme = localStorage.getItem('hc-theme') || 'light';
    if (savedTheme === 'dark') {
        document.documentElement.classList.add('dark-mode');
        document.documentElement.setAttribute('data-theme', 'dark');
    } else {
        document.documentElement.classList.remove('dark-mode');
        document.documentElement.setAttribute('data-theme', 'light');
    }
})();

function toggleGlobalTheme() {
    const isDark = document.documentElement.getAttribute('data-theme') === 'dark';
    const newTheme = isDark ? 'light' : 'dark';
    
    // Apply to current page
    if (newTheme === 'dark') {
        document.documentElement.classList.add('dark-mode');
        document.documentElement.setAttribute('data-theme', 'dark');
    } else {
        document.documentElement.classList.remove('dark-mode');
        document.documentElement.setAttribute('data-theme', 'light');
    }
    
    // Save for other pages
    localStorage.setItem('hc-theme', newTheme);
    
    // Dispatch event for other tabs
    window.dispatchEvent(new Event('storage'));
}

// Listen for changes from other tabs
window.addEventListener('storage', () => {
    const theme = localStorage.getItem('hc-theme');
    if (theme === 'dark') {
        document.documentElement.classList.add('dark-mode');
        document.documentElement.setAttribute('data-theme', 'dark');
    } else {
        document.documentElement.classList.remove('dark-mode');
        document.documentElement.setAttribute('data-theme', 'light');
    }
});
