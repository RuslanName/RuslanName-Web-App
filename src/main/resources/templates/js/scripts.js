document.addEventListener("DOMContentLoaded", async function () {
    try {
        const config = await fetchConfig();
        if (!config || !config.url) return;

        const { isAuthorized, chatId } = await checkAuthorization(config);
        if (!isAuthorized) return;

        if (!config.allowEditing) disableEditingFeatures();

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
        console.error("Ошибка загрузки страницы:", error);
    }
});

async function fetchConfig() {
    try {
        const response = await fetch('/api/config');
        if (!response.ok) throw new Error("Failed: " + response.status);
        return await response.json();
    } catch (error) {
        console.error("Ошибка загрузки конфигурации:", error);
        return null;
    }
}

function blockPage(message) {
    document.body.innerHTML = `
        <div style="
            display: flex;
            justify-content: center;
            align-items: center;
            height: 100vh;
            font-size: 20px;
            font-weight: bold;
            text-align: center;
        ">
            ${message}
        </div>
    `;
}

async function checkUserStatus(chatId, url) {
    try {
        const response = await fetch(`${url}/api/users_data/${chatId}`);
        if (!response.ok) throw new Error("Ошибка проверки пользователя: " + response.status);
        const data = await response.json();

        if (!data.isRegistered) {
            blockPage("Пользователь не зарегистрирован");
            return { isAuthorized: false, isBlocked: false };
        }

        if (data.isBlocked) {
            blockPage("Пользователь заблокирован");
            return { isAuthorized: false, isBlocked: true };
        }

        return { isAuthorized: true, isBlocked: false };
    } catch (error) {
        console.error("Ошибка проверки статуса пользователя:", error);
        return { isAuthorized: false, isBlocked: false };
    }
}

async function checkAuthorization(config) {
    if (!config.requireAuthentication) return { isAuthorized: true, chatId: null };

    if (typeof Telegram === "undefined" || !Telegram.WebApp) {
        blockPage("Отсутствует Telegram WebApp");
        return { isAuthorized: false, chatId: null };
    }

    Telegram.WebApp.ready();
    const chatId = await getChatId();
    if (!chatId) {
        blockPage("Пользователь Telegram WebApp не найден");
        return { isAuthorized: false, chatId: null };
    }

    const { isAuthorized, isBlocked } = await checkUserStatus(chatId, config.url);
    if (!isAuthorized) return { isAuthorized: false, chatId: null };

    return { isAuthorized: true, chatId };
}

async function getChatId() {
    try {
        Telegram.WebApp.ready();

        if (Telegram.WebApp.initDataUnsafe && Telegram.WebApp.initDataUnsafe.user) {
            return Telegram.WebApp.initDataUnsafe.user.id;
        }

        else {
            return null;
        }
    } catch (error) {
        console.error("Error:", error);
        return null;
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
            }

            else {
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