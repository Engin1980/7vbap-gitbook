import { useState } from "react";
import React, { createContext, useContext, ReactNode } from "react";
import {AppUserView} from "../model/app-user-view";

interface LoggedUserContextType {
  loggedUser: AppUserView | null;
  login: (userData: AppUserView) => void;
  logout: () => void;
}

const LoggedUserContext = createContext<LoggedUserContextType | undefined>(
  undefined
);

const LoggedUserProvider = ({ children }: { children: ReactNode }) => {
  const LOGGED_USER_KEY : string = "__logged_user";

  const loadUserFromStorage : () => AppUserView | null =  () => {
    const storedUser = localStorage.getItem(LOGGED_USER_KEY);
    const ret = storedUser
      ? JSON.parse(storedUser) as AppUserView
      : null;
    return ret;
  };

  const [loggedUser, setLoggedUser] = useState<AppUserView | null>(loadUserFromStorage());

  const login = (userData: AppUserView) => {
    setLoggedUser(userData);
    localStorage.setItem(LOGGED_USER_KEY, JSON.stringify(userData));
  };

  const logout = () => {
    setLoggedUser(null);
    localStorage.removeItem(LOGGED_USER_KEY);
  };

  return (
    <LoggedUserContext.Provider value={{ loggedUser, login, logout }}>
      {children}
    </LoggedUserContext.Provider>
  );
};

// Custom hook for easy use of the UserContext
const useLoggedUser = () => {
  const context = useContext(LoggedUserContext);
  if (!context) {
    throw new Error("useLoggedUser must be used within a LogggedUserProvider");
  }
  return context;
};

export { LoggedUserProvider, useLoggedUser };
