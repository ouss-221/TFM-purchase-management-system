import axios from "axios";

const api = axios.create({
    baseURL: "http://localhost:8081/api",
});

api.interceptors.request.use((config) => {
    const token = sessionStorage.getItem("authToken");
    if (token) {
        config.headers.Authorization = "Basic " + token;
    }
    return config;
});

api.interceptors.response.use(
    (response) => response,
    (error) => {
        if (error.response && error.response.status === 401) {
            sessionStorage.clear();
            window.location.href = "/login";
        }
        return Promise.reject(error);
    }
);

export default api;