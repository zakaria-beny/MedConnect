const presets = [
    {
        name: "Signup (Auth)",
        method: "POST",
        path: "/api/auth/signup",
        body: {
            email: "test@example.com",
            password: "TestPassword123!",
            nom: "Doe",
            prenom: "John",
            telephone: "+212600000000"
        }
    },
    {
        name: "Verify email OTP (Auth)",
        method: "POST",
        path: "/api/auth/verify-email",
        body: {
            email: "test@example.com",
            code: "123456"
        }
    },
    {
        name: "Login (Auth)",
        method: "POST",
        path: "/api/auth/login",
        body: {
            email: "test@example.com",
            password: "TestPassword123!"
        }
    },
    {
        name: "Verify login OTP (Auth)",
        method: "POST",
        path: "/api/auth/verify-login",
        body: {
            email: "test@example.com",
            code: "123456"
        }
    },
    {
        name: "Refresh token (Auth)",
        method: "POST",
        path: "/api/auth/refresh",
        body: {
            refreshToken: "paste-refresh-token-here"
        }
    },
    {
        name: "Current user (Users)",
        method: "GET",
        path: "/api/users/me",
        body: null
    },
    {
        name: "All users (Users)",
        method: "GET",
        path: "/api/users",
        query: "page=0&size=20",
        body: null
    },
    {
        name: "Create patient profile (Users)",
        method: "POST",
        path: "/api/users/patients",
        body: {
            userId: "replace-user-id",
            bloodType: "O+",
            medicalHistory: "No major history",
            allergies: ["Penicillin"]
        }
    },
    {
        name: "DMP summary by patient",
        method: "GET",
        path: "/api/dmp/replace-patient-id",
        body: null
    },
    {
        name: "DMP alerts by patient",
        method: "GET",
        path: "/api/dmp/replace-patient-id/alerts",
        body: null
    },
    {
        name: "Patient medications (DMP)",
        method: "GET",
        path: "/api/dmp/replace-patient-id/medications",
        body: null
    }
];

const baseUrlInput = document.getElementById("baseUrl");
const presetSelect = document.getElementById("preset");
const applyPresetBtn = document.getElementById("applyPreset");
const methodInput = document.getElementById("method");
const pathInput = document.getElementById("path");
const queryInput = document.getElementById("query");
const tokenInput = document.getElementById("token");
const useBearerInput = document.getElementById("useBearer");
const bodyInput = document.getElementById("body");
const sendBtn = document.getElementById("sendBtn");
const clearBtn = document.getElementById("clearBtn");
const responseMeta = document.getElementById("responseMeta");
const responseBody = document.getElementById("responseBody");
const curlPreview = document.getElementById("curlPreview");

const TOKEN_STORAGE_KEY = "medconnect.apiTester.token";
const BASE_URL_STORAGE_KEY = "medconnect.apiTester.baseUrl";

bootstrap();

function bootstrap() {
    const storedBaseUrl = localStorage.getItem(BASE_URL_STORAGE_KEY);
    baseUrlInput.value = storedBaseUrl || window.location.origin;
    tokenInput.value = localStorage.getItem(TOKEN_STORAGE_KEY) || "";

    presets.forEach((preset, index) => {
        const option = document.createElement("option");
        option.value = String(index);
        option.textContent = preset.name;
        presetSelect.appendChild(option);
    });

    applyPreset(0);

    applyPresetBtn.addEventListener("click", () => {
        applyPreset(Number(presetSelect.value));
    });

    sendBtn.addEventListener("click", sendRequest);
    clearBtn.addEventListener("click", clearResponse);
}

function applyPreset(index) {
    const preset = presets[index];
    if (!preset) {
        return;
    }

    methodInput.value = preset.method;
    pathInput.value = preset.path;
    queryInput.value = preset.query || "";
    bodyInput.value = preset.body ? JSON.stringify(preset.body, null, 2) : "";
}

function clearResponse() {
    responseMeta.textContent = "No request sent yet.";
    responseBody.textContent = "";
    curlPreview.textContent = "No request sent yet.";
}

async function sendRequest() {
    const method = methodInput.value;
    const baseUrl = normalizeBaseUrl(baseUrlInput.value);
    const path = normalizePath(pathInput.value);
    const token = tokenInput.value.trim();
    const queryText = queryInput.value.trim();
    const bodyText = bodyInput.value.trim();

    if (!baseUrl || !path) {
        responseMeta.textContent = "Base URL and path are required.";
        responseBody.textContent = "";
        return;
    }

    localStorage.setItem(BASE_URL_STORAGE_KEY, baseUrl);
    if (token) {
        localStorage.setItem(TOKEN_STORAGE_KEY, token);
    }

    const url = buildUrl(baseUrl, path, queryText);
    const headers = new Headers();

    if (token) {
        const authValue = useBearerInput.checked ? `Bearer ${token}` : token;
        headers.set("Authorization", authValue);
    }

    const requestInit = {
        method,
        headers
    };

    if (!["GET", "HEAD"].includes(method) && bodyText) {
        const parsedBody = safeJsonParse(bodyText);
        if (parsedBody.valid) {
            headers.set("Content-Type", "application/json");
            requestInit.body = JSON.stringify(parsedBody.value);
        } else {
            headers.set("Content-Type", "text/plain");
            requestInit.body = bodyText;
        }
    }

    curlPreview.textContent = toCurl(method, url, headers, requestInit.body);
    responseMeta.textContent = "Sending request...";
    responseBody.textContent = "";
    sendBtn.disabled = true;

    const start = performance.now();
    try {
        const response = await fetch(url, requestInit);
        const elapsedMs = Math.round(performance.now() - start);
        const payload = await parseResponsePayload(response);
        const responseHeaders = Object.fromEntries(response.headers.entries());

        responseMeta.textContent =
            `${response.status} ${response.statusText} (${elapsedMs} ms)\n` +
            JSON.stringify(responseHeaders, null, 2);
        responseBody.textContent = typeof payload === "string" ? payload : JSON.stringify(payload, null, 2);

        if (payload && typeof payload === "object" && payload.accessToken && !token) {
            tokenInput.value = payload.accessToken;
            localStorage.setItem(TOKEN_STORAGE_KEY, payload.accessToken);
        }
    } catch (error) {
        responseMeta.textContent = "Request failed.";
        responseBody.textContent = error instanceof Error ? error.message : String(error);
    } finally {
        sendBtn.disabled = false;
    }
}

function normalizeBaseUrl(value) {
    return value.trim().replace(/\/+$/, "");
}

function normalizePath(value) {
    const trimmed = value.trim();
    if (!trimmed) {
        return "";
    }
    return trimmed.startsWith("/") ? trimmed : `/${trimmed}`;
}

function buildUrl(baseUrl, path, queryText) {
    const url = new URL(`${baseUrl}${path}`);
    if (queryText) {
        const params = new URLSearchParams(queryText);
        params.forEach((value, key) => url.searchParams.set(key, value));
    }
    return url.toString();
}

function safeJsonParse(raw) {
    try {
        return { valid: true, value: JSON.parse(raw) };
    } catch {
        return { valid: false, value: null };
    }
}

async function parseResponsePayload(response) {
    const contentType = response.headers.get("content-type") || "";
    if (contentType.includes("application/json")) {
        return await response.json();
    }
    return await response.text();
}

function toCurl(method, url, headers, body) {
    const parts = [`curl -X ${method} "${url}"`];
    headers.forEach((value, key) => {
        parts.push(`-H "${key}: ${escapeForDoubleQuotes(value)}"`);
    });
    if (body) {
        parts.push(`--data "${escapeForDoubleQuotes(body)}"`);
    }
    return parts.join(" \\\n  ");
}

function escapeForDoubleQuotes(value) {
    return String(value).replace(/\\/g, "\\\\").replace(/"/g, '\\"');
}
