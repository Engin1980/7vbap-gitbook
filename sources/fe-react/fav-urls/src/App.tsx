import React from 'react';
import './App.css';
import UrlList from "./components/url/url-list";
import UrlEdit from "./components/url/url-edit";
import 'mdb-react-ui-kit/dist/css/mdb.min.css';
import {ToastContainer} from 'react-toastify';
import "react-toastify/dist/ReactToastify.css";

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
        <header className="App-header">
          FAVOURITE URLs
        </header>
        <UrlList/>
        <UrlEdit/>
      </div>
    </div>
  );
}

export default App;
