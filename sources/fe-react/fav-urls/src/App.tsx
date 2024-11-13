import React from 'react';
import './App.css';
import UrlList from "./components/url/url-list";
import 'mdb-react-ui-kit/dist/css/mdb.min.css';
import {ToastContainer} from 'react-toastify';
import "react-toastify/dist/ReactToastify.css";
import 'reactjs-popup/dist/index.css';
import {BrowserRouter, Route, Routes} from "react-router-dom";
import Login from "./components/user/login";
import {LoggedUserProvider} from "./hooks/use-logged-user";
import Secured from "./components/global/secured";
import AppHeader from "./components/global/app-header";
import MainPanel from "./components/global/main-panel";
import Logout from "./components/user/logout";

function App() {

  return (
    <div>
      <ToastContainer
        position="top-right"
        autoClose={5000}
        hideProgressBar={false}
        closeOnClick
        pauseOnFocusLoss
        pauseOnHover
      />

      <div className="App">
        <AppHeader/>
        <div className="container-md">
          <LoggedUserProvider>
            <BrowserRouter>
              <MainPanel />
              <Routes>
                <Route path="/login" element={<Login/>}/>
                <Route path="/logout" element={<Logout/>}/>
                <Route path="/urls" element={<Secured/>}>
                  <Route path="/urls" element={<UrlList/>}/>
                </Route>
              </Routes>
            </BrowserRouter>
          </LoggedUserProvider>
        </div>
      </div>
    </div>
  );
}

export default App;
