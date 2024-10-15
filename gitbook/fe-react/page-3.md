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

* **react-form-hook** for easy form validation and submitting;
* **material-design-for-boostrap MDB** for predefined components and bootstrap support via components;
* **axios** for HTTP requests;
* **toastify** for easy toast support;
* **react-router-dom** for routing support;
* **reactjs-popup** for popup dialogs and forms;

Run the console **in(!)** the folder of the project and execute the following:

```
npm install react-form-hook
npm install mdb-react-ui-kit
npm install axios
npm install react-toastify
npm install react-router-dom
npm install reactjs-popup
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

