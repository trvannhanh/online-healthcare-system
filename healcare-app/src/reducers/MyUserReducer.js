import cookie from "react-cookies";

export const reducer = (state, action) => {
    switch (action.type) {
        case "login":
            return { ...state, user: action.payload };
        case "logout":
            cookie.remove("token");
            return { ...state, user: null };
        default:
            return state;
    }
};