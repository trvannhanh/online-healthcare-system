import { createContext, useContext, useReducer } from "react";
import { reducer } from "../reducers/MyUserReducer";

const MyUserContext = createContext();
const MyDispatcherContext = createContext();

const initialState = {
    user: null,
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