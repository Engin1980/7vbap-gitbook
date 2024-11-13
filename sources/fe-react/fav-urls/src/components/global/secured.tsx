import React, { ReactNode } from "react";
import { Navigate, Outlet } from "react-router-dom";
import {useLoggedUser} from "../../hooks/use-logged-user";

function getCurrentRelativeUrl() : string{
  const url = new URL(window.location.href);
  const ret = url.toString().substring(url.origin.length);
  return ret;
}

function Secured () {
  const {loggedUser} = useLoggedUser();
  const isLogged: boolean = loggedUser != null;
  const loginUrl = "/login?next=" + getCurrentRelativeUrl();
  return isLogged
    ? <Outlet />
    : <Navigate to={loginUrl} />;
}

export default Secured;
