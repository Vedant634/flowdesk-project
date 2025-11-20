import axios from "axios";

const api = axios.create({
    baseURL: "http://localhost:8080/api",
    headers: {
        "Content-Type": "application/json",
    },
});

api.interceptors.request.use(
    (config) => {
        const token = localStorage.getItem("token");
        if (token) {
            config.headers.Authorization = `Bearer ${token}`;
        }
        console.log("[v0] API Request:", config.method.toUpperCase(), config.url);
        return config;
    },
    (error) => {
        console.error("[v0] Request Error:", error);
        return Promise.reject(error);
    }
);

api.interceptors.response.use(
    (response) => {
        console.log("[v0] API Response:", response.config.url, response.status);
        return response;
    },
    (error) => {
        console.error("[v0] API Error:", {
            url: error.config?.url,
            status: error.response?.status,
            message: error.response?.data?.message || error.message,
            data: error.response?.data
        });

        // Handle 401 unauthorized - redirect to login
        if (error.response?.status === 401) {
            localStorage.removeItem("token");
            localStorage.removeItem("currentUser");
            window.location.href = "/login";
        }

        return Promise.reject(error);
    }
);

export default api;
