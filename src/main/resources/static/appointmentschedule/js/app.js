/* ================================================================
   MEDICARE APPOINTMENT SYSTEM - PREMIUM JAVASCRIPT
   Local Version 3.0 - No CDN Required
   ================================================================ */

'use strict';

/* ============ INIT ON DOM READY ============ */
document.addEventListener('DOMContentLoaded', function() {
    console.log('🏥 MediCare System Ready');

    initNavbar();
    initAlerts();
    initTooltips();
    initDateFields();
    initTimeValidation();
    initFormValidation();
    initPhoneValidation();
    initSearchFeatures();
    initScrollAnimations();
    initCountAnimations();
    initLoadingButtons();
    initConfirmLinks();
    initTableRows();
});

/* ============ NAVBAR ============ */
function initNavbar() {
    var navbar = document.querySelector('.navbar');
    if (!navbar) return;

    window.addEventListener('scroll', function() {
        if (window.scrollY > 20) {
            navbar.classList.add('scrolled');
            navbar.style.boxShadow =
                '0 4px 25px rgba(0,0,0,0.25)';
        } else {
            navbar.classList.remove('scrolled');
            navbar.style.boxShadow =
                '0 4px 15px rgba(0,0,0,0.1)';
        }
    }, { passive: true });

    // Active link highlight
    var currentPath = window.location.pathname;
    document.querySelectorAll('.navbar .nav-link')
        .forEach(function(link) {
            if (link.getAttribute('href') &&
                currentPath.includes(
                    link.getAttribute('href')
                        .replace('/appointmentschedule', '')
                        .split('?')[0])) {
                link.classList.add('active');
            }
        });
}

/* ============ ALERTS ============ */
function initAlerts() {
    var alerts = document.querySelectorAll(
        '.alert:not(.alert-permanent)');

    alerts.forEach(function(alert, i) {
        // Stagger entrance
        alert.style.animationDelay = (i * 0.1) + 's';

        // Auto dismiss after 6 seconds
        var timer = setTimeout(function() {
            dismissAlert(alert);
        }, 6000 + (i * 800));

        // Cancel on hover
        alert.addEventListener('mouseenter', function() {
            clearTimeout(timer);
        });

        alert.addEventListener('mouseleave', function() {
            timer = setTimeout(function() {
                dismissAlert(alert);
            }, 3000);
        });
    });
}

function dismissAlert(alert) {
    if (!alert || !alert.parentNode) return;
    alert.style.transition = 'all 0.5s ease';
    alert.style.opacity = '0';
    alert.style.transform = 'translateY(-15px)';
    alert.style.maxHeight = alert.offsetHeight + 'px';

    setTimeout(function() {
        alert.style.maxHeight = '0';
        alert.style.padding = '0';
        alert.style.margin = '0';
        alert.style.overflow = 'hidden';
        setTimeout(function() {
            if (alert.parentNode) alert.remove();
        }, 300);
    }, 300);
}

/* ============ TOOLTIPS ============ */
function initTooltips() {
    if (typeof bootstrap === 'undefined') return;

    document.querySelectorAll('[title]')
        .forEach(function(el) {
            if (el.tagName === 'A' || el.tagName === 'BUTTON') {
                try {
                    new bootstrap.Tooltip(el, {
                        trigger: 'hover',
                        placement: 'top',
                        delay: { show: 500, hide: 100 }
                    });
                } catch(e) {}
            }
        });
}

/* ============ DATE FIELDS ============ */
function initDateFields() {
    var today = new Date();
    var todayStr = formatDateToISO(today);

    document.querySelectorAll('input[type="date"]')
        .forEach(function(input) {
            // Set minimum date
            if (!input.hasAttribute('min') &&
                !input.classList.contains('allow-past')) {
                input.setAttribute('min', todayStr);
            }

            // Visual feedback
            input.addEventListener('change', function() {
                if (!this.value) {
                    this.classList.remove('is-valid', 'is-invalid');
                    return;
                }

                var selected = new Date(this.value);
                var minDate = this.getAttribute('min') ?
                    new Date(this.getAttribute('min')) :
                    null;

                if (minDate && selected < minDate) {
                    this.classList.add('is-invalid');
                    this.classList.remove('is-valid');
                    showToast(
                        'Please select today or a future date',
                        'warning');
                } else {
                    this.classList.add('is-valid');
                    this.classList.remove('is-invalid');
                }
            });
        });
}

function formatDateToISO(date) {
    var y = date.getFullYear();
    var m = String(date.getMonth() + 1).padStart(2, '0');
    var d = String(date.getDate()).padStart(2, '0');
    return y + '-' + m + '-' + d;
}

/* ============ TIME VALIDATION ============ */
function initTimeValidation() {
    var startTime = document.querySelector(
        'input[name="startTime"]');
    var endTime = document.querySelector(
        'input[name="endTime"]');

    if (!startTime || !endTime) return;

    function validate() {
        if (!startTime.value || !endTime.value) return;

        if (endTime.value <= startTime.value) {
            endTime.classList.add('is-invalid');
            endTime.classList.remove('is-valid');
            endTime.setCustomValidity(
                'End time must be after start time');

            var feedback = endTime.nextElementSibling;
            if (!feedback || !feedback.classList
                .contains('invalid-feedback')) {
                feedback = document.createElement('div');
                feedback.className = 'invalid-feedback';
                endTime.parentNode.appendChild(feedback);
            }
            feedback.textContent =
                'End time must be after start time';
        } else {
            endTime.classList.remove('is-invalid');
            endTime.classList.add('is-valid');
            endTime.setCustomValidity('');
        }
    }

    startTime.addEventListener('change', function() {
        endTime.setAttribute('min', this.value);
        validate();
    });

    endTime.addEventListener('change', validate);
}

/* ============ FORM VALIDATION ============ */
function initFormValidation() {
    // Real-time validation for required fields
    document.querySelectorAll(
        'input[required], select[required], textarea[required]')
        .forEach(function(field) {

            field.addEventListener('blur', function() {
                validateField(this);
            });

            field.addEventListener('input', function() {
                if (this.classList.contains('is-invalid')) {
                    validateField(this);
                }
            });
        });

    // Email validation
    document.querySelectorAll('input[type="email"]')
        .forEach(function(input) {
            input.addEventListener('blur', function() {
                if (!this.value) return;

                if (!isValidEmail(this.value)) {
                    this.classList.add('is-invalid');
                    this.classList.remove('is-valid');
                } else {
                    this.classList.remove('is-invalid');
                    this.classList.add('is-valid');
                }
            });
        });

    // Character counter for textareas
    document.querySelectorAll('textarea')
        .forEach(function(ta) {
            var maxLen = ta.getAttribute('maxlength') ||
                ta.getAttribute('data-maxlength');
            if (!maxLen) return;

            var counter = document.createElement('div');
            counter.className =
                'text-end small text-muted mt-1';
            counter.innerHTML =
                '<span class="current">0</span>' +
                ' / ' + maxLen;
            ta.parentNode.appendChild(counter);

            ta.addEventListener('input', function() {
                var len = this.value.length;
                var span = counter.querySelector('.current');
                span.textContent = len;

                if (len > maxLen * 0.9) {
                    counter.className =
                        'text-end small text-danger mt-1';
                } else if (len > maxLen * 0.7) {
                    counter.className =
                        'text-end small text-warning mt-1';
                } else {
                    counter.className =
                        'text-end small text-muted mt-1';
                }
            });
        });

    // Form submit with loading
    document.querySelectorAll('form').forEach(function(form) {
        form.addEventListener('submit', function(e) {
            if (!form.checkValidity()) {
                e.preventDefault();
                e.stopPropagation();
                form.classList.add('was-validated');

                // Scroll to first error
                var firstError = form.querySelector(
                    '.is-invalid, :invalid');
                if (firstError) {
                    firstError.scrollIntoView({
                        behavior: 'smooth',
                        block: 'center'
                    });
                    firstError.focus();
                }
                return;
            }

            // Show loading on submit button
            var btn = form.querySelector('button[type="submit"]');
            if (btn) {
                btn.dataset.originalText = btn.innerHTML;
                btn.innerHTML =
                    '<span class="loading-spinner me-2"></span>' +
                    'Processing...';
                btn.disabled = true;

                // Safety timeout
                setTimeout(function() {
                    if (btn.disabled) {
                        btn.innerHTML = btn.dataset.originalText;
                        btn.disabled = false;
                    }
                }, 10000);
            }
        });
    });
}

function validateField(field) {
    if (!field.value || !field.value.trim()) {
        field.classList.add('is-invalid');
        field.classList.remove('is-valid');
    } else {
        field.classList.remove('is-invalid');
        field.classList.add('is-valid');
    }
}

/* ============ PHONE VALIDATION ============ */
function initPhoneValidation() {
    document.querySelectorAll('input[type="tel"]')
        .forEach(function(input) {

            // Only allow valid chars
            input.addEventListener('input', function() {
                this.value = this.value.replace(
                    /[^\d+\s\-()]/g, '');
            });

            input.addEventListener('blur', function() {
                if (!this.value) return;

                var phone = this.value.trim()
                    .replace(/\s/g, '');

                if (!isValidSLPhone(phone)) {
                    this.classList.add('is-invalid');
                    this.classList.remove('is-valid');

                    // Add hint
                    removeHint(this);
                    var hint = document.createElement('div');
                    hint.className = 'invalid-feedback';
                    hint.textContent =
                        'Valid formats: 07XXXXXXXX or +947XXXXXXXX';
                    this.parentNode.appendChild(hint);
                } else {
                    this.classList.remove('is-invalid');
                    this.classList.add('is-valid');
                    removeHint(this);
                }
            });
        });
}

function removeHint(field) {
    var hint = field.parentNode.querySelector(
        '.phone-hint, .field-hint');
    if (hint) hint.remove();
}

/* ============ SEARCH FEATURES ============ */
function initSearchFeatures() {
    document.querySelectorAll('input[name="search"]')
        .forEach(function(input) {

            // Clear button
            var wrapper = input.closest('.input-group');
            if (wrapper && input.value) {
                addClearBtn(input, wrapper);
            }

            input.addEventListener('input', function() {
                if (!wrapper) return;
                var existing = wrapper.querySelector(
                    '.clear-btn');
                if (this.value && !existing) {
                    addClearBtn(this, wrapper);
                } else if (!this.value && existing) {
                    existing.remove();
                }
            });

            // Enter to submit
            input.addEventListener('keydown', function(e) {
                if (e.key === 'Enter') {
                    e.preventDefault();
                    var form = this.closest('form');
                    if (form) form.submit();
                }
            });
        });
}

function addClearBtn(input, wrapper) {
    var btn = document.createElement('button');
    btn.type = 'button';
    btn.className = 'btn btn-outline-secondary clear-btn';
    btn.innerHTML = '✕';
    btn.title = 'Clear search';
    btn.style.borderRadius = '0 var(--radius-sm) var(--radius-sm) 0';

    btn.addEventListener('click', function() {
        input.value = '';
        this.remove();
        input.focus();
        var form = input.closest('form');
        if (form) form.submit();
    });

    wrapper.appendChild(btn);
}

/* ============ SCROLL ANIMATIONS ============ */
function initScrollAnimations() {
    if (!('IntersectionObserver' in window)) return;

    var observer = new IntersectionObserver(
        function(entries) {
            entries.forEach(function(entry, i) {
                if (entry.isIntersecting) {
                    var el = entry.target;
                    el.style.animationDelay =
                        (i * 0.08) + 's';
                    el.classList.add('fade-in');
                    el.style.opacity = '1';
                    observer.unobserve(el);
                }
            });
        },
        { threshold: 0.08, rootMargin: '0px 0px -40px 0px' }
    );

    // Observe cards that haven't animated
    document.querySelectorAll(
        '.stat-card, .card:not(.fade-in)')
        .forEach(function(el) {
            if (!el.classList.contains('fade-in')) {
                el.style.opacity = '0';
                observer.observe(el);
            }
        });
}

/* ============ COUNT ANIMATIONS ============ */
function initCountAnimations() {
    if (!('IntersectionObserver' in window)) return;

    var observer = new IntersectionObserver(
        function(entries) {
            entries.forEach(function(entry) {
                if (entry.isIntersecting) {
                    animateCount(entry.target);
                    observer.unobserve(entry.target);
                }
            });
        },
        { threshold: 0.5 }
    );

    document.querySelectorAll('.stat-card h3')
        .forEach(function(el) {
            observer.observe(el);
        });
}

function animateCount(el) {
    var target = parseInt(el.textContent.trim(), 10);
    if (isNaN(target) || target === 0) return;

    var start = 0;
    var duration = 1200;
    var startTime = null;

    function step(now) {
        if (!startTime) startTime = now;
        var progress = Math.min(
            (now - startTime) / duration, 1);
        var eased = 1 - Math.pow(1 - progress, 4);
        el.textContent = Math.round(eased * target);
        if (progress < 1) requestAnimationFrame(step);
        else el.textContent = target;
    }

    requestAnimationFrame(step);
}

/* ============ LOADING BUTTONS ============ */
function initLoadingButtons() {
    // Nav links loading
    document.querySelectorAll(
        'a.btn:not([data-no-load]):not([onclick])')
        .forEach(function(link) {
            link.addEventListener('click', function() {
                var href = this.getAttribute('href');
                if (!href || href === '#' ||
                    href.startsWith('javascript') ||
                    href.startsWith('mailto')) return;

                this.style.opacity = '0.75';
                this.style.pointerEvents = 'none';

                // Reset after 5s safety
                var self = this;
                setTimeout(function() {
                    self.style.opacity = '';
                    self.style.pointerEvents = '';
                }, 5000);
            });
        });
}

/* ============ CONFIRM DIALOGS ============ */
function initConfirmLinks() {
    document.querySelectorAll(
        'a[href*="/delete/"], a[href*="/cancel/"]')
        .forEach(function(link) {

            // Skip if already handled
            if (link.dataset.confirmSetup) return;
            link.dataset.confirmSetup = 'true';

            // Remove old onclick
            link.removeAttribute('onclick');

            link.addEventListener('click', function(e) {
                e.preventDefault();
                e.stopPropagation();

                var href = this.getAttribute('href');
                var isDelete = href.includes('/delete/');
                var isCancel = href.includes('/cancel/');

                var config = {
                    title: isDelete ?
                        '🗑️ Delete Permanently?' :
                        '❌ Cancel Appointment?',
                    message: isDelete ?
                        'This will permanently remove the record and cannot be undone.' :
                        'The appointment will be cancelled and the schedule slot will be released.',
                    confirmText: isDelete ?
                        'Yes, Delete' : 'Yes, Cancel',
                    btnClass: 'btn-danger',
                    href: href
                };

                showModal(config);
            });
        });
}

function showModal(cfg) {
    // Remove existing
    var old = document.getElementById('mdcConfirmModal');
    if (old) old.remove();

    var backdrop = document.createElement('div');
    backdrop.id = 'mdcConfirmModal';
    backdrop.style.cssText =
        'position:fixed;inset:0;z-index:9999;' +
        'display:flex;align-items:center;' +
        'justify-content:center;padding:1rem;' +
        'background:rgba(15,23,42,0.6);' +
        'backdrop-filter:blur(4px);' +
        'animation:fadeIn 0.2s ease;';

    backdrop.innerHTML =
        '<div style="background:white;border-radius:20px;' +
        'padding:2rem;max-width:420px;width:100%;' +
        'box-shadow:0 25px 60px rgba(0,0,0,0.25);' +
        'animation:bounceIn 0.4s ease;' +
        'text-align:center;">' +
        '<div style="font-size:3rem;margin-bottom:1rem;">' +
        cfg.title.split(' ')[0] + '</div>' +
        '<h5 style="font-weight:700;margin-bottom:0.75rem;' +
        'color:#1e293b;">' +
        cfg.title.substring(cfg.title.indexOf(' ') + 1) +
        '</h5>' +
        '<p style="color:#64748b;margin-bottom:1.5rem;' +
        'font-size:0.9rem;line-height:1.6;">' +
        cfg.message + '</p>' +
        '<div style="display:flex;gap:0.75rem;' +
        'justify-content:center;">' +
        '<a href="' + cfg.href + '" class="btn ' +
        cfg.btnClass + ' btn-lg px-4">' +
        cfg.confirmText + '</a>' +
        '<button type="button" class="btn btn-outline-secondary btn-lg px-4" ' +
        'onclick="document.getElementById(\'mdcConfirmModal\').remove()">' +
        'Keep It</button>' +
        '</div></div>';

    // Click outside to close
    backdrop.addEventListener('click', function(e) {
        if (e.target === backdrop) backdrop.remove();
    });

    document.body.appendChild(backdrop);

    // ESC to close
    document.addEventListener('keydown', function handler(e) {
        if (e.key === 'Escape') {
            backdrop.remove();
            document.removeEventListener('keydown', handler);
        }
    });
}

/* ============ TABLE ROWS ============ */
function initTableRows() {
    document.querySelectorAll('.appointment-row, tr[data-href]')
        .forEach(function(row) {
            row.style.cursor = 'pointer';
            row.addEventListener('click', function(e) {
                // Don't navigate if clicking a button/link
                if (e.target.closest('a, button, .btn')) return;
                var href = this.dataset.href;
                if (href) window.location.href = href;
            });
        });
}

/* ============ UTILITY: TOAST ============ */
function showToast(message, type) {
    type = type || 'info';

    var config = {
        success: { icon: '✅', color: '#10b981', bg: '#d1fae5' },
        danger: { icon: '❌', color: '#ef4444', bg: '#fee2e2' },
        warning: { icon: '⚠️', color: '#f59e0b', bg: '#fef3c7' },
        info: { icon: 'ℹ️', color: '#06b6d4', bg: '#cffafe' }
    };

    var c = config[type] || config.info;
    var id = 'toast-' + Date.now();

    var toast = document.createElement('div');
    toast.id = id;
    toast.style.cssText =
        'position:fixed;bottom:24px;right:24px;z-index:99999;' +
        'max-width:380px;min-width:250px;' +
        'background:white;border-radius:14px;' +
        'padding:1rem 1.25rem;' +
        'box-shadow:0 10px 40px rgba(0,0,0,0.15);' +
        'display:flex;align-items:flex-start;gap:12px;' +
        'border-left:4px solid ' + c.color + ';' +
        'animation:slideUp 0.4s ease;' +
        'transition:all 0.4s ease;';

    toast.innerHTML =
        '<span style="font-size:1.2rem;flex-shrink:0;">' +
        c.icon + '</span>' +
        '<span style="flex:1;font-size:0.875rem;' +
        'font-weight:500;color:#334155;line-height:1.5;">' +
        message + '</span>' +
        '<button onclick="this.closest(\'div\').remove()" ' +
        'style="background:none;border:none;color:#94a3b8;' +
        'cursor:pointer;padding:0;font-size:1.1rem;' +
        'line-height:1;flex-shrink:0;' +
        'transition:color 0.2s;">✕</button>';

    document.body.appendChild(toast);

    // Auto remove
    setTimeout(function() {
        if (toast.parentNode) {
            toast.style.opacity = '0';
            toast.style.transform = 'translateY(15px)';
            setTimeout(function() {
                if (toast.parentNode) toast.remove();
            }, 400);
        }
    }, 4500);
}

/* ============ UTILITY: COPY ============ */
function copyToClipboard(text) {
    if (navigator.clipboard && navigator.clipboard.writeText) {
        navigator.clipboard.writeText(text)
            .then(function() {
                showToast('Copied to clipboard!', 'success');
            })
            .catch(function() {
                fallbackCopy(text);
            });
    } else {
        fallbackCopy(text);
    }
}

function fallbackCopy(text) {
    var ta = document.createElement('textarea');
    ta.value = text;
    ta.style.cssText = 'position:fixed;opacity:0;';
    document.body.appendChild(ta);
    ta.focus();
    ta.select();
    try {
        document.execCommand('copy');
        showToast('Copied!', 'success');
    } catch(e) {
        showToast('Copy failed', 'danger');
    }
    document.body.removeChild(ta);
}

/* ============ UTILITY: PRINT ============ */
function printPage() {
    window.print();
}

/* ============ VALIDATORS ============ */
function isValidEmail(email) {
    return /^[a-zA-Z0-9._%+\-]+@[a-zA-Z0-9.\-]+\.[a-zA-Z]{2,}$/
        .test(email);
}

function isValidSLPhone(phone) {
    return /^(\+94|0)[0-9]{9}$/.test(phone);
}