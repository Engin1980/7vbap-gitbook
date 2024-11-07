---
icon: square-5
description: >-
  Here we describe how authentication is handled and the logged user is stored
  at the front end side.
---

# Authentication

{% hint style="danger" %}
Note this chapter is a place where things are getting a bit complicated. Please read the content carefully so you do not miss any important part of the functionality.
{% endhint %}



## How Logged User is stored

This chapter is stronly dependent on `useContext` React hook. We suggest to study this hook first before reading the rest of this implementation.\


{% embed url="https://react.dev/reference/react/useContext" %}
useContext Hook in React
{% endembed %}

The whole logged-user management will be created using definitions placed in the newly createdy `use-logged-user.tsx` file located in the `hooks` folder.

Create this file and add the required imports. Also, an interface representing the functionality related to the logged user is added:

```typescript
import { useState } from "react";
import React, { createContext, useContext, ReactNode } from "react";
import {AppUserView} from "../model/app-user-view";

interface LoggedUserContextType {
  loggedUser: AppUserView | null;
  login: (userData: AppUserView) => void;
  logout: () => void;
}
```

The interface provides:

* information about the logged user (using `AppUserview` interface defined before),
* login and
* logout methods.

As a next step, we create a context holding info about the logged user within the same file:

```typescript
const LoggedUserContext = createContext<LoggedUserContextType | undefined>(
  undefined
);
```

Note that the default value of this context is `undefined` - using this feature, we are able to distinguish if the `useLoggedHook` (created later) is used within the `<LoggedUserProvider />` component.

Now, lets create a component `LoggedUserProvider`. The idea is that our logged-user functionality is available only in components encapsulated with this component:

{% code lineNumbers="true" %}
```typescript
const LoggedUserProvider = ({ children }: { children: ReactNode }) => {
  const LOGGED_USER_KEY : string = "__logged_user";

  const loadUserFromStorage : () => AppUserView | null =  () => {
    const storedUser = localStorage.getItem(LOGGED_USER_KEY);
    const ret = storedUser
      ? JSON.parse(storedUser) as AppUserView
      : null;
    return ret;
  };

  const [loggedUser, setLoggedUser] = 
    useState<AppUserView | null>(loadUserFromStorage());

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
```
{% endcode %}

Here we:

* create a component at line 1. Parameter `children`  allows to define and render nested components inside our provider component;
* define a key used to store the logged user in the local storage of the browser (line 2);
* define a (private) method `loadFromStorage()` providing an implementation of retrieving the data from the local storage using hte key (lines 4-10);
* define a logged user state (lines 12-13);
* define a `login()` method adjusting the logged-user-state and storing the user in the local storage (lines 14-17);
* define a `logout()` method clearing the logged user from logged-user-state and the local storage (lines 19-22);
* finally, return the whole context component using the `useContext.Provider` providing respective implementations for the `value` parameter of type `LoggedUserContextType` (defined above) (lines 24-29).

Finally, we define our `useLoggedUser()` hook and export the content of the `tsx` file.

```
const useLoggedUser = () => {
  const context = useContext(LoggedUserContext);
  if (!context) {
    throw new Error("useLoggedUser must be used within a LogggedUserProvider");
  }
  return context;
};

export { LoggedUserProvider, useLoggedUser };
```

Note that the hook validates if it is used in the component, which is nested inside of the `<LoggedUserProvider>...</LoggedUserProvider>`.&#x20;

## Securing routes

The next part is securing the specific parts of the front end to be used only by logged users. This security is achieved by marking the specific routes to be available only for the logged users. &#x20;

### Secured (Component)

To do so, we will create a new component `Secured` placed in `/src/components/global/secured.tsx`:

{% code lineNumbers="true" %}
```typescript
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
```
{% endcode %}

This component defines a (private) method extracting the relative url from the current full url in the browser (so, from `http://locahost:1234/user/view` we get `/user/view`).

The main component implementation (lines 11-18) checks for the logged user (lines 13) using the `useLoggedUser` hook (line 12). Then, it creates a redirecting url in the format of `.../login?next=WHERE_TO_GO_AFTER_LOGIN` format and depending on the logged user, the navigation or the inner content of the component is used.

### Logged User Panel (MainPanel component)

Now, we will create a simple application menu containing the panel (row) with the info about the logged user. It is a simple component using `useLoggedUser` hook located in `src/components/global/main-panel.tsx`:

```typescript
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
```

Aditionally, we will create a main title page of the app at `src/components/global/app-header.tsx`:

```typescript
import React from "react";

function AppHeader() {
  return (
    <div>
      <header className="App-header">
        FAVOURITE URLs
      </header>
    </div>
  );
}

export default AppHeader;
```

{% hint style="info" %}
The `AppHeader` component has no relation with the logged-user. However, we will use it later when specifying the layout of the app in the `App.tsx` file/component.
{% endhint %}

### Adjusting App component

Now, we need to adjust the content of the main component window:

{% code lineNumbers="true" %}
```typescript
// ...

function App() {

  return (
    <div>
      <ToastContainer
        <!-- ... -->
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
```
{% endcode %}

We can see that:

* All the stuff is encapsulated with the `LoggedUserProvider` component (from line 14 to line 25). Everything nested inside of this component can use our `useLoggedUser` hook.
* We have added a `<Main Panel />` to show logged user data (if any).
* We have secured the route `.../urls` (lines 20+21) using `Secured` component so an anonymous user cannot reach it.
* We have also declared two new routes, `/login`  and `/logout`.

## Login

Next part is the login component. We will define it in two parts - firstly the application logic, then the form itself. The `Login` component will be created in `/src/components/user/login.tsx`:

{% code lineNumbers="true" %}
```typescript
//...

type Data = {
  email: string;
  password: string;
}

function Login() {
  const {
    register,
    handleSubmit,
    formState: {errors}
  } = useForm<Data>();
  const http = useHttp();
  const {login} = useLoggedUser();
  const navigate = useNavigate();

  const submitHandler: SubmitHandler<Data> = async data => {
    const formData = new FormData();
    formData.append("email", data.email);
    formData.append("password", data.password);

    try {
      const user = await http.post<AppUserView>("/v1/appUser/login", formData);
      login(user);
      toast.success("Logged in successfully.");
      navigate("/urls");
    } catch (err) {
      console.log(err);
      toast.error("Login failed.");
    }
  }

  return (<div>
    // ... the form, see below
  </div>);
}

export default Login;
```
{% endcode %}

The form itself is now quite simple:

* It defines a data object (lines 3-6).
* Then the `Login` component follows (lines 8-83):
  * `useForm` hook to work with form is defined (lines 9-13).
  * Hooks for http, logged-user and navigation are introduced (lines 14-16).
  * `submitHandler` handling the form submission is created. The handler creates a form data (lines 19-21), and then sends the log-in request via httpx at the back-end endpoint.&#x20;
    * On success, the user is navigated to his `/urls` list of urls (lines 26-27).
    * On failure, the error message is shown (lines 29-31).

The form itself is quite simple:

```typescript
// ...

function Login() {
  // ... app logic introduced before
  
  return (<div>

    <MDBContainer>

      <MDBRow>
        <MDBCol md="6" className="mx-auto">
          <h1>Login</h1>
          <form onSubmit={handleSubmit(submitHandler)}>

            <MDBInput
              className="mb-3 mt-4"
              label="Email"
              type="email"
              {...register("email", {
                required: "This field is required."
              })}
            />
            {errors.email && (
              <span className="invalid-feedback d-block">
                          {errors.email.message}
                        </span>
            )}

            <MDBInput
              className="mb-3 mt-4"
              label="Password"
              type="password"
              {...register("password", {
                required: "This field is required"
              })} />
            {errors.password && (
              <span className="invalid-feedback d-block">
                        {errors.password.message}
                      </span>
            )}

            <MDBRow className="mt-5">
              <MDBCol>
                <MDBBtn type="submit">Log In</MDBBtn>
              </MDBCol>
              <MDBCol>
                <NavLink to="/register">Register</NavLink>
              </MDBCol>
            </MDBRow>
          </form>
        </MDBCol>
      </MDBRow>
    </MDBContainer>
  </div>);
}

export default Login;
```

In the form, there is nothing special compared to the forms introduced previously.

## Token Refresh

TODO

## Logout

TODO
