document.addEventListener('DOMContentLoaded', () => {
    
    // Mobile menu toggle
    const mobileMenu = document.getElementById('mobile-menu');
    const navLinks = document.querySelector('.nav-links');

    if (mobileMenu) {
        mobileMenu.addEventListener('click', () => {
            navLinks.classList.toggle('active');
            const icon = mobileMenu.querySelector('i');
            if (navLinks.classList.contains('active')) {
                icon.classList.remove('fa-bars');
                icon.classList.add('fa-xmark');
            } else {
                icon.classList.remove('fa-xmark');
                icon.classList.add('fa-bars');
            }
        });
    }

    // Set minimum date to today for appointment date input
    const dateInput = document.getElementById('date');
    if (dateInput) {
        const today = new Date().toISOString().split('T')[0];
        dateInput.setAttribute('min', today);
    }

    // Handle form submission
    const form = document.getElementById('appointmentForm');
    
    if (form) {
        form.addEventListener('submit', function(e) {
            e.preventDefault(); // Prevent page reload

            // Get form values
            const firstName = document.getElementById('firstName').value;
            const lastName = document.getElementById('lastName').value;
            const email = document.getElementById('email').value;
            const date = document.getElementById('date').value;
            const time = document.getElementById('time').value;
            
            // Get department text from select dropdown
            const deptSelect = document.getElementById('department');
            const department = deptSelect.options[deptSelect.selectedIndex].text;

            // Format date for display
            const formattedDate = new Date(date).toLocaleDateString('en-US', {
                weekday: 'long', 
                year: 'numeric', 
                month: 'long', 
                day: 'numeric'
            });

            // Populate Modal UI
            document.getElementById('patientNameDisplay').textContent = `${firstName} ${lastName}`;
            document.getElementById('departmentDisplay').textContent = department;
            document.getElementById('dateDisplay').textContent = formattedDate;
            document.getElementById('timeDisplay').textContent = time;
            document.getElementById('emailDisplay').textContent = email;

            // Show success modal
            const modal = document.getElementById('successModal');
            modal.style.display = 'flex';

            // Reset form
            form.reset();
        });
    }

    // Close modal when clicking outside of it
    window.addEventListener('click', (e) => {
        const modal = document.getElementById('successModal');
        if (e.target === modal) {
            closeModal();
        }
    });
});

// Function to close modal
function closeModal() {
    const modal = document.getElementById('successModal');
    modal.style.display = 'none';
}
