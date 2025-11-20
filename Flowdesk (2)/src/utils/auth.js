export const isAuthenticated = () => {
    return localStorage.getItem("token") !== null;
};

export const getCurrentUser = () => {
    try {
        const user = localStorage.getItem("currentUser");
        return user ? JSON.parse(user) : null;
    } catch {
        return null;
    }
};

export const logout = () => {
    localStorage.removeItem("token");
    localStorage.removeItem("currentUser");
};

export const saveUser = (user) => {
    localStorage.setItem("currentUser", JSON.stringify(user));
};
