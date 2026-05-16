/* ============================================
   MEDICARE+ APPOINTMENT SYSTEM - MAIN JS
   Member 4: Appointment & Schedule Module
   ============================================ */

'use strict';

/* ===== PARTICLES GENERATOR ===== */
function createParticles() {
    const container = document.getElementById('particlesContainer');
    if (!container) return;

    const count = 20;
    const sizes = [4, 6, 7, 8, 9, 10, 11];

    for (let i = 0; i < count; i++) {
        const dot = document.createElement('div');
        dot.classList.add('particle-dot');

        const size = sizes[Math.floor(Math.random() * sizes.length)];
        dot.style.width  = size + 'px';
        dot.style.height = size + 'px';
        dot.style.left   = Math.random() * 100 + '%';
        dot.style.animationDuration  = (10 + Math.random() * 12) + 's';
        dot.style.animationDelay     = (Math.random() * 8) + 's';
        dot.style.opacity = (0.05 + Math.random() * 0.15).toString();

        container.appendChild(dot);
    }
}

/* ===== STEP WIZARD ===== */
const StepWizard = {
    current: 1,
    total: 4,

    go(target) {
        const cur = document.getElementById('step' + this.current);
        const next = document.getElementById('step' + target);
        if (!next) return;

        if (cur) {
            cur.style.animation = 'none';
            cur.style.display = 'none';
        }

        next.style.display = 'block';
        next.style.animation = 'stepSlideIn 0.45s cubic-bezier(0.22, 1, 0.36, 1)';

        this.current = target;
        this.updateIndicators();
        window.scrollTo({ top: 0, behavior: 'smooth' });
    },

    next() {
        if (!Validator.checkStep(this.current)) return;
        if (this.current === 3) Summary.build();
        if (this.current < this.total) this.go(this.current + 1);
    },

    prev() {
        if (this.current > 1) this.go(this.current - 1);
    },

    updateIndicators() {
        for (let i = 1; i <= this.total; i++) {
            const node = document.getElementById('stepNode' + i);
            const line = document.getElementById('stepLine' + i);

            if (!node) continue;

            node.classList.remove('step-active', 'step-done');
            if (i < this.current)  node.classList.add('step-done');
            if (i === this.current) node.classList.add('step-active');

            if (line) {
                line.classList.toggle('filled', i < this.current);
            }
        }
    }
};

/* ===== VALIDATOR ===== */
const Validator = {
    checkStep(step) {
        if (step === 1) return this.validatePatientInfo();
        if (step === 2) return this.validateDoctorSelection();
        if (step === 3) return this.validateSchedule();
        return true;
    },

    validatePatientInfo() {
        const name  = document.getElementById('patientName');
        const email = document.getElementById('patientEmail');
        const phone = document.getElementById('patientPhone');
        let valid = true;

        if (!name || name.value.trim().length < 2) {
            this.setError(name, 'Name must be at least 2 characters');
            valid = false;
        } else {
            this.setOk(name);
        }

        if (!email || !this.isEmail(email.value)) {
            this.setError(email, 'Enter a valid email address');
            valid = false;
        } else {
            this.setOk(email);
        }

        if (!phone || !/^[0-9]{10}$/.test(phone.value)) {
            this.setError(phone, 'Enter a 10-digit phone number');
            valid = false;
        } else {
            this.setOk(phone);
        }

        return valid;
    },

    validateDoctorSelection() {
        const spec = document.getElementById('specSelect');
        const doc  = document.getElementById('docSelect');

        if (!spec || !spec.value) {
            this.showToast('⚠️ Please select a specialization', 'warn');
            return false;
        }
        if (!doc || !doc.value) {
            this.showToast('⚠️ Please select a doctor', 'warn');
            return false;
        }
        return true;
    },

    validateSchedule() {
        const date   = document.getElementById('dateInput');
        const time   = document.getElementById('timeInput');
        const reason = document.getElementById('reasonBox');

        if (!date || !date.value) {
            this.showToast('⚠️ Please select a date', 'warn');
            return false;
        }
        if (!time || !time.value) {
            this.showToast('⚠️ Please select a time', 'warn');
            return false;
        }
        if (!reason || reason.value.trim().length < 5) {
            this.setError(reason, 'Reason must be at least 5 characters');
            return false;
        }
        return true;
    },

    setError(el, msg) {
        if (!el) return;
        el.classList.add('input-err');
        el.classList.remove('input-ok');
        el.focus();

        let errEl = el.parentElement.querySelector('.err-msg');
        if (!errEl) {
            errEl = document.createElement('div');
            errEl.className = 'err-msg';
            el.parentElement.after(errEl);
        }
        errEl.innerHTML = `<i class="fas fa-exclamation-circle"></i> ${msg}`;

        setTimeout(() => {
            el.classList.remove('input-err');
            if (errEl) errEl.remove();
        }, 3500);
    },

    setOk(el) {
        if (!el) return;
        el.classList.remove('input-err');
        el.classList.add('input-ok');
    },

    isEmail(val) {
        return /^[^\s@]+@[^\s@]+\.[^\s@]+$/.test(val);
    },

    showToast(msg, type = 'ok') {
        Toast.show(msg, type);
    }
};

/* ===== APPOINTMENT TYPE SELECTOR ===== */
const TypeSelector = {
    select(type, el) {
        document.querySelectorAll('.type-card').forEach(c => {
            c.classList.remove('type-selected');
        });
        el.classList.add('type-selected');

        const hidden = document.getElementById('appointmentType');
        if (hidden) hidden.value = type;
    }
};

/* ===== TIME SLOT PICKER ===== */
const SlotPicker = {
    select(time, el) {
        if (el.classList.contains('slot-booked')) return;

        document.querySelectorAll('.time-slot-btn').forEach(s => {
            s.classList.remove('slot-active');
        });

        el.classList.add('slot-active');

        const input = document.getElementById('timeInput');
        if (input) {
            input.value = time;
        }
    }
};

/* ===== DOCTOR FILTER ===== */
const DoctorFilter = {
    data: {
        'General Medicine' : ['Dr. Perera', 'Dr. Kumara'],
        'Cardiology'       : ['Dr. Silva', 'Dr. Amarasinghe'],
        'Dermatology'      : ['Dr. Fernando', 'Dr. Wickramasinghe'],
        'Pediatrics'       : ['Dr. Jayawardena', 'Dr. Bandara'],
        'Orthopedics'      : ['Dr. Rathnayake', 'Dr. Dissanayake'],
        'Neurology'        : ['Dr. Karunaratne'],
        'Gynecology'       : ['Dr. Senanayake'],
        'Ophthalmology'    : ['Dr. Weerasinghe']
    },

    load() {
        const spec = document.getElementById('specSelect');
        const doc  = document.getElementById('docSelect');
        if (!spec || !doc) return;

        const doctors = this.data[spec.value] || [];
        doc.innerHTML = '<option value="">-- Select Doctor --</option>';

        doctors.forEach(name => {
            const opt = document.createElement('option');
            opt.value = name;
            opt.textContent = name;
            doc.appendChild(opt);
        });
    }
};

/* ===== SUMMARY BUILDER ===== */
const Summary = {
    build() {
        this.set('sName',   'patientName');
        this.set('sEmail',  'patientEmail');
        this.set('sPhone',  'patientPhone');
        this.set('sDoctor', 'docSelect');
        this.set('sSpec',   'specSelect');

        const dateEl = document.getElementById('dateInput');
        const sDate  = document.getElementById('sDate');
        if (dateEl && sDate && dateEl.value) {
            sDate.textContent = new Date(dateEl.value).toLocaleDateString('en-US', {
                weekday: 'long',
                year: 'numeric',
                month: 'long',
                day: 'numeric'
            });
        }

        const timeEl = document.getElementById('timeInput');
        const sTime  = document.getElementById('sTime');
        if (timeEl && sTime && timeEl.value) {
            sTime.textContent = this.formatTime(timeEl.value);
        }

        const typeEl = document.getElementById('appointmentType');
        const sType  = document.getElementById('sType');
        if (typeEl && sType) {
            sType.textContent = typeEl.value === 'ONLINE'
                ? '💻 Online Consultation'
                : '🏥 In-Person Visit';
        }
    },

    set(targetId, sourceId) {
        const source = document.getElementById(sourceId);
        const target = document.getElementById(targetId);
        if (source && target) {
            target.textContent = source.value || '—';
        }
    },

    formatTime(time24) {
        const [h, m] = time24.split(':');
        const hr = parseInt(h);
        const ampm = hr >= 12 ? 'PM' : 'AM';
        return (hr % 12 || 12) + ':' + m + ' ' + ampm;
    }
};

/* ===== CHARACTER COUNTER ===== */
const CharCounter = {
    update(textarea, max, displayId) {
        const count = textarea.value.length;
        const el = document.getElementById(displayId);
        if (!el) return;

        el.textContent = count + '/' + max;
        el.className = 'char-count';
        if (count > max * 0.8)  el.classList.add('warn');
        if (count > max * 0.95) el.classList.add('danger');
    }
};

/* ===== TOAST NOTIFICATIONS ===== */
const Toast = {
    show(message, type = 'ok', duration = 4000) {
        const existing = document.getElementById('toastNotif');
        if (existing) existing.remove();

        const toast = document.createElement('div');
        toast.id = 'toastNotif';
        toast.className = 'float-alert';

        const colors = {
            ok:   { bg: '#d4edda', color: '#155724', icon: 'fa-check-circle' },
            err:  { bg: '#f8d7da', color: '#721c24', icon: 'fa-times-circle' },
            warn: { bg: '#fff3cd', color: '#856404', icon: 'fa-exclamation-triangle' },
            info: { bg: '#d1ecf1', color: '#0c5460', icon: 'fa-info-circle' }
        };

        const c = colors[type] || colors.info;
        toast.style.cssText = `background:${c.bg}; color:${c.color};`;
        toast.innerHTML = `
            <i class="fas ${c.icon}"></i>
            <span>${message}</span>
        `;

        document.body.appendChild(toast);

        setTimeout(() => {
            toast.style.animation = 'floatOut 0.4s ease forwards';
            setTimeout(() => toast.remove(), 400);
        }, duration);
    }
};

/* ===== TABLE SEARCH ===== */
const TableSearch = {
    filter(inputId, tableId) {
        const input = document.getElementById(inputId);
        const table = document.getElementById(tableId);
        if (!input || !table) return;

        const term = input.value.toLowerCase();
        const rows = table.querySelectorAll('tbody tr.data-row');

        rows.forEach(row => {
            const text = row.textContent.toLowerCase();
            row.style.display = text.includes(term) ? '' : 'none';
        });
    }
};

/* ===== CONFIRM DIALOGS ===== */
const Confirm = {
    cancel(form) {
        if (window.confirm(
            '⚠️ Cancel this appointment?\n\nThe status will be changed to CANCELLED.'
        )) {
            form.submit();
        }
    },

    delete(form) {
        if (window.confirm(
            '🗑️ Permanently delete this appointment?\n\nThis action CANNOT be undone!'
        )) {
            form.submit();
        }
    },

    deleteSchedule(form) {
        if (window.confirm('Delete this schedule slot?')) {
            form.submit();
        }
    }
};

/* ===== LOADING OVERLAY ===== */
const Loader = {
    show(text = 'Processing...') {
        const overlay = document.getElementById('loadingScreen');
        const textEl  = document.getElementById('loaderText');
        if (overlay) overlay.style.display = 'flex';
        if (textEl)  textEl.textContent = text;
    },

    hide() {
        const overlay = document.getElementById('loadingScreen');
        if (overlay) overlay.style.display = 'none';
    }
};

/* ===== AUTO DISMISS ALERTS ===== */
function autoDismissAlerts(delay = 4500) {
    setTimeout(() => {
        document.querySelectorAll('.float-alert, .auto-dismiss').forEach(el => {
            el.style.transition = 'opacity 0.5s, transform 0.5s';
            el.style.opacity = '0';
            el.style.transform = 'translateX(30px)';
            setTimeout(() => el.remove(), 500);
        });
    }, delay);
}

/* ===== DATE VALIDATION ===== */
function setMinDate(inputId) {
    const el = document.getElementById(inputId);
    if (!el) return;
    const tomorrow = new Date();
    tomorrow.setDate(tomorrow.getDate() + 1);
    el.min = tomorrow.toISOString().split('T')[0];
}

/* ===== PHONE NUMBER FORMATTER ===== */
function initPhoneInput(inputId) {
    const el = document.getElementById(inputId);
    if (!el) return;

    el.addEventListener('input', function () {
        this.value = this.value.replace(/[^0-9]/g, '').slice(0, 10);
    });
}

/* ===== LIVE EMAIL VALIDATION ===== */
function initEmailValidation(inputId) {
    const el = document.getElementById(inputId);
    if (!el) return;

    el.addEventListener('blur', function () {
        if (this.value) {
            if (/^[^\s@]+@[^\s@]+\.[^\s@]+$/.test(this.value)) {
                Validator.setOk(this);
            } else {
                Validator.setError(this, 'Enter a valid email address');
            }
        }
    });
}

/* ===== LIVE NAME VALIDATION ===== */
function initNameValidation(inputId) {
    const el = document.getElementById(inputId);
    if (!el) return;

    el.addEventListener('blur', function () {
        if (this.value.trim().length >= 2) {
            Validator.setOk(this);
        }
    });
}

/* ===== BOOKING FORM SUBMIT ===== */
function initBookingForm() {
    const form = document.getElementById('bookingForm');
    if (!form) return;

    form.addEventListener('submit', function (e) {
        const terms = document.getElementById('termsCheck');
        if (terms && !terms.checked) {
            e.preventDefault();
            Toast.show('⚠️ Please agree to the Terms & Conditions', 'warn');
            return;
        }
        Loader.show('Booking your appointment...');
    });
}

/* ===== SIDEBAR MOBILE TOGGLE ===== */
function initSidebarToggle() {
    const toggle = document.getElementById('sidebarToggle');
    const sidebar = document.getElementById('sidebarDark');

    if (toggle && sidebar) {
        toggle.addEventListener('click', () => {
            sidebar.classList.toggle('open');
        });

        document.addEventListener('click', (e) => {
            if (!sidebar.contains(e.target) && !toggle.contains(e.target)) {
                sidebar.classList.remove('open');
            }
        });
    }
}

/* ===== ANIMATE STAT NUMBERS ===== */
function animateNumbers() {
    document.querySelectorAll('.stat-number-big').forEach(el => {
        const target = parseInt(el.textContent) || 0;
        let current = 0;
        const step = Math.ceil(target / 30);
        const timer = setInterval(() => {
            current = Math.min(current + step, target);
            el.textContent = current;
            if (current >= target) clearInterval(timer);
        }, 40);
    });
}

/* ===== STEP ANIMATION CSS INJECTION ===== */
(function injectStepAnimation() {
    const style = document.createElement('style');
    style.textContent = `
        @keyframes stepSlideIn {
            from { opacity: 0; transform: translateX(25px); }
            to   { opacity: 1; transform: translateX(0); }
        }
        @keyframes floatOut {
            to { opacity: 0; transform: translateX(30px); }
        }
    `;
    document.head.appendChild(style);
})();

/* ===== INIT ON DOM READY ===== */
document.addEventListener('DOMContentLoaded', function () {

    // Particles
    createParticles();

    // Set min date for all date inputs
    setMinDate('dateInput');
    setMinDate('newDateInput');

    // Live validations
    initPhoneInput('patientPhone');
    initEmailValidation('patientEmail');
    initNameValidation('patientName');

    // Booking form
    initBookingForm();

    // Sidebar mobile
    initSidebarToggle();

    // Auto dismiss alerts
    autoDismissAlerts();

    // Animate numbers
    animateNumbers();

    // Init step wizard indicator
    StepWizard.updateIndicators();

});