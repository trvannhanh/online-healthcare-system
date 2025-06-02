import { createContext, useContext, useReducer, useEffect } from "react";
import { reducer } from "../reducers/MyUserReducer";
import cookie from "react-cookies";
import { authApis, endpoints } from "../configs/Apis";

const MyUserContext = createContext();
const MyDispatcherContext = createContext();

const initialState = {
    user: null,
};


export const MyUserProvider = ({ children }) => {
  const [state, dispatch] = useReducer(reducer, initialState);

  useEffect(() => {
    const loadUser = async () => {
      const token = cookie.load("token");
      if (token) {
        try {
          const userRes = await authApis().get(endpoints["current-user"]);
          let baseUser = userRes.data;

          // Nếu là bác sĩ, lấy thêm thông tin chi tiết
          if (baseUser.role === "DOCTOR") {
            const doctorRes = await authApis().get(
              `${endpoints["doctors"]}/${baseUser.id}`
            );
            baseUser = {
              ...baseUser,
              ...doctorRes.data,
              isVerified: doctorRes.data.isVerified,
            };
          }

          dispatch({ type: "login", payload: baseUser });
        } catch (error) {
          console.error("Error loading user:", error);
          cookie.remove("token");
          dispatch({ type: "logout" });
        }
      }
    };

    loadUser();
  }, []);

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