document.addEventListener("DOMContentLoaded", async function () {
    try {
        const response = await fetch('/api/config');
        if (!response.ok) {
            throw new Error(`Failed to load configuration: ${response.status}`);
        }
        const config = await response.json();

        if (!config.url) {
            console.error("URL is not initialized.");
            return;
        }

        if (config.requireTelegramUser) {
            if (typeof Telegram !== "undefined" && Telegram.WebApp) {
                Telegram.WebApp.ready();
            }

            else {
                console.error("Telegram Web App is not available.");
                return;
            }

            const chatId = await getChatId();
            if (!chatId) {
                document.body.innerHTML = "<p>Telegram WebApp user is required.</p>";
                return;
            }

            const isRegistered = await checkUserRegistration(chatId, config.url);
            if (!isRegistered) {
                document.body.innerHTML = "<p>User is not registered.</p>";
                return;
            }
        }

        if (!config.allowEditing) {
            disableEditingFeatures();
        }

        let inactivityTimeout;

        function resetInactivityTimer() {
            clearTimeout(inactivityTimeout);
            inactivityTimeout = setTimeout(() => location.reload(), 45000);
        }

        ['mousemove', 'keydown', 'scroll', 'click', 'touchstart'].forEach(event => {
            document.addEventListener(event, resetInactivityTimer);
        });

        resetInactivityTimer();
        setupProductInteractions();

    } catch (error) {
        console.error("Initialization failed:", error);
    }
});

async function getChatId() {
    if (typeof Telegram === 'undefined' || !Telegram.WebApp) {
        console.error("Telegram WebApp is not initialized.");
        return null;
    }

    try {
        Telegram.WebApp.ready();
        if (Telegram.WebApp.initDataUnsafe && Telegram.WebApp.initDataUnsafe.user) {
            return Telegram.WebApp.initDataUnsafe.user.id;
        } else {
            console.error("Failed to retrieve user data from Telegram WebApp.");
            return null;
        }
    } catch (error) {
        console.error("Error initializing Telegram WebApp:", error);
        return null;
    }
}

async function checkUserRegistration(chatId, url) {
    try {
        const response = await fetch(`${url}/api/user_check?chatId=${chatId}`);
        if (!response.ok) {
            console.error("Failed to check user registration:", response.status);
            return false;
        }
        const data = await response.json();
        return data.isRegistered;
    } catch (error) {
        console.error("Error during user registration check:", error);
        return false;
    }
}

function setupProductInteractions() {
    document.body.addEventListener("click", (event) => {
        const button = event.target.closest("button");
        const product = event.target.closest(".product");

        if (product) {
            if (button) {
                product.classList.add("button-touched");
                setTimeout(() => {
                    product.classList.remove("button-touched");
                }, 100);
            } else {
                product.classList.add("area-touched");
                setTimeout(() => {
                    product.classList.remove("area-touched");
                }, 300);
            }
        }
    });

    document.body.addEventListener("mousedown", (event) => {
        const product = event.target.closest(".product");
        if (product) {
            product.classList.add("area-touched");
        }
    });

    document.body.addEventListener("mouseup", (event) => {
        const product = event.target.closest(".product");
        if (product) {
            product.classList.remove("area-touched");
        }
    });

    document.body.addEventListener("touchstart", (event) => {
        const product = event.target.closest(".product");
        if (product) {
            product.classList.add("area-touched");
        }
    });

    document.body.addEventListener("touchend", (event) => {
        const product = event.target.closest(".product");
        if (product) {
            product.classList.remove("area-touched");
        }
    });

    document.body.addEventListener("mouseenter", (event) => {
        if (event.target.closest(".product") && event.pointerType !== "touch") {
            const product = event.target.closest(".product");
            product.classList.add("button-touched");
        }
    }, true);

    document.body.addEventListener("mouseleave", (event) => {
        if (event.target.closest(".product") && event.pointerType !== "touch") {
            const product = event.target.closest(".product");
            product.classList.remove("button-touched");
        }
    }, true);
}

function disableEditingFeatures() {
    document.addEventListener("copy", (event) => {
        event.preventDefault();
    });

    document.addEventListener("contextmenu", (event) => {
        event.preventDefault();
    });

    document.body.addEventListener("dragstart", (event) => {
        event.preventDefault();
    });
}

function applyTheme() {
    const isTelegram = typeof Telegram !== "undefined" && Telegram.WebApp;
    const isDarkTheme = isTelegram
        ? Telegram.WebApp.colorScheme === 'dark'
        : window.matchMedia('(prefers-color-scheme: dark)').matches;

    const themeProperties = {
        '--tg-theme-bg-color': isDarkTheme ? 'var(--tg-theme-bg-color-dark)' : 'var(--tg-theme-bg-color-light)',
        '--tg-theme-text-color': isDarkTheme ? 'var(--tg-theme-text-color-dark)' : 'var(--tg-theme-text-color-light)',
        '--tg-theme-secondary-bg-color': isDarkTheme ? 'var(--tg-theme-secondary-bg-color-dark)' : 'var(--tg-theme-secondary-bg-color-light)',
        '--tg-theme-button-color': isDarkTheme ? 'var(--tg-theme-button-color-dark)' : 'var(--tg-theme-button-color-light)',
        '--tg-theme-footer-bg-color': isDarkTheme ? 'var(--tg-theme-footer-bg-color-dark)' : 'var(--tg-theme-footer-bg-color-light)',
        '--tg-theme-footer-text-color': isDarkTheme ? 'var(--tg-theme-footer-text-color-dark)' : 'var(--tg-theme-footer-text-color-light)',
        '--tg-checkbox-toggle-color': isDarkTheme ? 'var(--tg-checkbox-toggle-color-dark)' : 'var(--tg-checkbox-toggle-color-light)',
    };

    Object.entries(themeProperties).forEach(([property, value]) => {
        document.documentElement.style.setProperty(property, value);
    });
}

if (typeof Telegram !== "undefined" && Telegram.WebApp) {
    Telegram.WebApp.onThemeChanged = applyTheme;
    Telegram.WebApp.ready(applyTheme);
}

else {
    document.addEventListener('DOMContentLoaded', applyTheme);
}