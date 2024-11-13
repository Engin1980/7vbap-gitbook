import React from "react";
import {useLoggedUser} from "../../hooks/use-logged-user";
import {NavLink} from "react-router-dom";

function MainPanel() {
  const {loggedUser} = useLoggedUser();

  const renderUserPanel = () => {
    if (loggedUser) {
      return (<div>
        <span>{loggedUser.email}</span>&nbsp;<NavLink to="/logout">Log out</NavLink>
      </div>);
    } else {
      return (<div>
        <NavLink to="/login">Log In</NavLink>
      </div>);
    }
  }

  return (
    <div>
      <div className="text-end mt-2">
        {renderUserPanel()}
      </div>
    </div>
  );
}

export default MainPanel;