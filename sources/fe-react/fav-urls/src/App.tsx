import React from 'react';
import './App.css';
import UrlList from "./components/url/url-list";
import 'mdb-react-ui-kit/dist/css/mdb.min.css';
import {ToastContainer} from 'react-toastify';
import "react-toastify/dist/ReactToastify.css";
import 'reactjs-popup/dist/index.css';

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
      </div>
    </div>
  );
}

export default App;
