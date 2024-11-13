import {useLoggedUser} from "../../hooks/use-logged-user";
import {useEffect, useState} from "react";
import useHttp from "../../hooks/use-http";
import {toast} from "react-toastify";
import {useNavigate} from "react-router-dom";

function Logout() {
  const {logout} = useLoggedUser();
  const http = useHttp();
  const navigate = useNavigate();

  useEffect(() => {
    logout();

    (async () => {
      try {
        await http.post("/v1/appUser/logout", null);
        toast.success("Logged out successfully.");
        navigate("/login");
      } catch (ex) {
        toast.error("There were some issue logging out.")
        console.log(ex);
      }
    })();

  }, []);

  return <div>Logged out...</div>

}

export default Logout;