import { createContext, useContext, useReducer } from "react";

const MyUserContext = createContext();
const MyDispatcherContext = createContext();

const initialState = {
    user: null,
};

const reducer = (state, action) => {
    switch (action.type) {
        case "login":
            return { ...state, user: action.payload };
        case "logout":
            return { ...state, user: null };
        default:
            return state;
    }
};

export const MyUserProvider = ({ children }) => {
    const [state, dispatch] = useReducer(reducer, initialState);

    return (
        <MyUserContext.Provider value={state}>
            <MyDispatcherContext.Provider value={dispatch}>
                {children}
            </MyDispatcherContext.Provider>
        </MyUserContext.Provider>
    );
};

export const useMyUser = () => useContext(MyUserContext);
export const useMyDispatcher = () => useContext(MyDispatcherContext);