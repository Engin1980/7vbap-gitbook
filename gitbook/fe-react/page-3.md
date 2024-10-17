---
icon: square-1
---

# Project creation

## NPM installation

todo

## Creating React Project

{% hint style="info" %}
We will use a basic project with no extending framework. We will use TypeScript instead of JavaScript.
{% endhint %}

### Initializing the project

Firstly, create a new project in the required folder:

```powershell
npm create-react-app fav-urls --template typescript
```

Now, you can look into the `fav-urls` directory and take look at its structure.

TODO more structure info

Then, we will directly add some libraries needed in our project realization:

* **react-hook-form** for easy form validation and submitting;
* **material-design-for-boostrap MDB** for predefined components and bootstrap support via components;
* **axios** for HTTP requests;
* **toastify** for easy toast support;
* **react-router-dom** for routing support;
* **reactjs-popup** for popup dialogs and forms;

Run the console **in(!)** the folder of the project and execute the following:

```
npm install react-hook-form
npm install mdb-react-ui-kit
npm install axios
npm install react-toastify
npm install react-router-dom
npm install reactjs-popup
```

{% hint style="warning" %}
Do not confuse `react-hook-form` with `react-form-hook`.
{% endhint %}

Additional important step to support _mdb-react-ui-kit_ is to add the CSS style either into `App.tsx` or `index.tsx` to be globally available:

```typescript
// ...
import 'mdb-react-ui-kit/dist/css/mdb.min.css';
// ...
```

### Adjust the styles

Now, let's slightly adjust the page style. In file `src/App.css` there are styles for the whole app. We will update the definition of the header style by its replacement with:

```css
.App-header {
  background-color: #282c34;
  min-height: 8vh;
  letter-spacing: 16px;
  display: flex;
  flex-direction: column;
  justify-content: center;
  font-size: calc(10px + 2vmin);
  color: white;
  text-align: left;
  padding-left: calc(10px + 2vmin);
  margin-bottom: 8px;
}
```

### Adjust the global template page

The visualisation of the React App is based on the layout defined in `src/App.tsx` file. Let's replace the default content with a custom one:

```typescript
import React from 'react';
import './App.css';

function App() {
  return (
    <div className="App">
      <header className="App-header">
       FAVOURITE URLs
      </header>
      Here will be the content.
    </div>
  );
}

export default App;
```

### Adding toast support

A **toast** in UI/UX design refers to a small, non-intrusive notification that briefly appears on the screen to provide feedback to the user, usually in response to an action. It typically disappears automatically after a few seconds.

Toasts are often used for messages such as:

* **Success notifications** ("Data saved successfully!")
* **Error or warning messages** ("Unable to connect to the server.")
* **Informative updates** ("You have 2 new messages.")

They are designed to be simple, quick to read, and not require any user interaction. In mobile and web development, toasts are commonly implemented using frameworks like **Android SDK**, **Bootstrap**, or **React** libraries.

To support toasts, we have added `react-toastify` dependency in our project. Now, we need to activate the behavior.

As toasts should be available through the whole application, we will apply the code changes into the `App.tsx`file.

Firstly, add some imports:

```typescript
import {toast, ToastContainer} from 'react-toastify';
import "react-toastify/dist/ReactToastify.css";
```

The `ToastContiner` is used as a definition of a component/element to display the toasts. It will also define the default behavior for the toasts. The `toast` definition here will be used only for testing purposes and can be deleted later. The `css` file contains style definitions for toasts. As a next part, we will add a toast component definition into the content of the component. Also, for testing purposes we will add a `useEffect(..)` call showing a demo toast (the principle and usage of this function will be explained later):

{% code lineNumbers="true" %}
```typescript
import React, {useEffect} from 'react';
import './App.css';
import 'mdb-react-ui-kit/dist/css/mdb.min.css';
import {toast, ToastContainer} from 'react-toastify';
import "react-toastify/dist/ReactToastify.css";

function App() {

  useEffect(() =>{
    toast.success("App Started");
  })

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
        Here will be the content.
      </div>
    </div>
  );
}

export default App;
```
{% endcode %}

Now, the toast will be displayed once the App component is loaded. Once tested, lines 9-11 should be removed from the code.

TODO image toast-demo.png

{% hint style="info" %}
React-toastify offers several options how to adjust the toast behavior. For the more detailed configuration see examples in the documentation.
{% endhint %}

{% embed url="https://www.npmjs.com/package/react-toastify" %}
React-Toastify - Documentation
{% endembed %}

## Starting the application

Now, start the console (PowerShell, Command line, Terminal, ...) in the project directory and start the application by running the command:

```powershell
PS C:\Users\vajgma91\. . . . .\fav-urls> npm start
```

You should see the result:

```
Compiled successfully!

You can now view fav-urls in the browser.

  Local:            http://localhost:3000
  On Your Network:  http://172.30.192.1:3000

Note that the development build is not optimized.
To create a production build, use npm run build.

webpack compiled successfully
Files successfully emitted, waiting for typecheck results...
Issues checking in progress...
No issues found.
```

Also, the default browser should have been opened at the url `http://localhost:3000` and you should see the content.

