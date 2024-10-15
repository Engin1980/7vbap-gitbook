---
icon: square-2
description: This page describes how a simple component displaying data can be created.
---

# Url List - first component prototype

{% embed url="https://github.com/Engin1980/7vbap-gitbook/tree/fe-react-url-list-prototype/sources/fe-react/fav-urls" %}
The source code related to this section
{% endembed %}

## Theoretical Introduction

A UI in React is created using **components**. And components, for easy content creation, use JSX.

The programming language used here is TypeScript.

{% embed url="https://gist.github.com/anichitiandreea/" %}
TypeScript Naming Conventions
{% endembed %}

### Component

A React component is a reusable, self-contained piece of code that represents part of a user interface (UI) in a React application. Components are the building blocks of a React app, and each component can manage its own state and logic while rendering UI elements based on that state or input from its parent components.

There are two main types of React components:

1. **Functional Components**: These are simple JavaScript functions that take props (properties) as arguments and return JSX (JavaScript XML) that describes what the UI should look like. They are stateless by default, but since React 16.8 (with the introduction of hooks like `useState`), functional components can manage state.
2. **Class Components** (Older Approach): These are ES6 classes that extend from `React.Component`. They have a `render()` method that returns JSX. Class components can manage their own internal state using `this.state` and handle lifecycle methods like `componentDidMount`, `componentDidUpdate`, etc.

As the second approach is obsolete, it will not be explained nor used here.

```typescript
// simple functional component, see the return value
function Greeting(props) {
  return <h1>Hello, {props.name}!</h1>;
}
```

### JSX

JSX (JavaScript XML) is a syntax extension for JavaScript that allows you to write HTML-like code within JavaScript. It is primarily used in React to describe what the UI should look like. JSX makes it easier to visualize the structure of the UI components by combining HTML and JavaScript in the same file.

Though it looks like HTML, JSX is not actually HTML. Under the hood, it is transformed into JavaScript function calls that create React elements. These elements define how components should render on the web page.

```typescript
const element = <h1>Hello, world!</h1>;
```

n this example, the JSX `<h1>Hello, world!</h1>` gets compiled into `React.createElement('h1', null, 'Hello, world!')`.

Key points about JSX:

* It can include JavaScript expressions within curly braces `{}`. For example: `<h1>{2 + 2}</h1>` will render as `<h1>4</h1>`.
* JSX tags must be properly closed, and you must wrap multiple JSX elements in a single enclosing tag or use React fragments (`<>...</>`).
* You can add attributes to JSX elements similar to HTML, but use camelCase for property names (e.g., `className` instead of `class`).

JSX bridges the gap between the user interface (HTML) and the logic (JavaScript) in a React application.

## List of all urls of an user

{% hint style="info" %}
React (similarly to other FE frameworks) is very free about the content structure in the `src` folder. So, we will create a structure by our own.
{% endhint %}

### DTO for URLs

With respect to the BE, we need a structure representing the data transferred over the REST API from BE to FE. As we are dealing with url, at BE we have a class:

```java
@Data
public class UrlView {
  // ...
  private int urlId;
  private String title;
  private String address;
}
```

At frotnent, we will create its counterpart interface. It should be created at\
`src/model/dtos/url-view.tsx`:

```typescript
export interface UrlView{
    urlId : number;
    title : string;
    address : string;
}
```

TODO a note about why is this an interface.

### UrlList - a component creation

Now, lets create our first component to view urls of an user.&#x20;

Create  a new file at `src/components/urls/url-list.tsx`: and add an initial content:

```typescript
function UrlList(){
  return <div>This is a placeholder for URL list</div>
}

export default UrlList;
```

Here, we define a new component named `UrlList` (and later referenced as `<UrlList />` and make it as a default export from this `.tsx` file.

### &#x20;Adding a component to the Application

The initial addition is simple. Just update the `App.tsx` file:

<pre class="language-typescript" data-line-numbers><code class="lang-typescript">import React from 'react';
import './App.css';
<a data-footnote-ref href="#user-content-fn-1">import UrlList from "./components/url/url-list";</a>

function App() {
  return (
    &#x3C;div className="App">
      &#x3C;header className="App-header">
       FAVOURITE URLs
      &#x3C;/header>
<a data-footnote-ref href="#user-content-fn-2">      &#x3C;UrlList /></a>
    &#x3C;/div>
  );
}

export default App;
</code></pre>

Note the import added at the beginning of the file (line 3) and the component (line 11).

If you now look at the page of the executed application, you can see that the content now shows the text specified in the component.

### UrlList - data fetch

We now add a code **inside** of the `UrlList()` function.

#### Storing the data - UseState hook

Firstly, we need to add a variable holding the data of the users urls. Those data will be obtained via REST API (later). However, we need to preserve data if the component is re-render (for any reason). This re-render will again the invocation of the `UrlList()` function again, so we need to store the data in the way that the React knows that the data were already fetched and are available. So, the local variable is not the valid way. Instead, we will use a `useState` hook.

The `useState` hook is a special function in React that allows functional components to have and manage state. It was introduced in React 16.8 and provides a way to declare state variables in functional components. The `useState` hook returns an array with two elements:

1. The current state value: This is the variable that holds the value of the state.
2. A function to update the state: This function allows you to update the state value, and it triggers a re-render of the component when the state changes.

Moreover, in TypeScript, we use a generic parameter setting up the expected datatype and, generally, an initial value.

```typescript
const [urls, setUrls] = useState<UrlView[]>([]);
```

So, in our case, we:

* use `urls` variable to read out the current list of elements;
* use `setUrls(data)` function invocation to set the new list of the data;
* specified that the datatype of `urls` is a list of `UrlView` - so `UrlView[]`;
* initialized the value with the empty list `[]`;
* `const` keyword defines those values as constants in our function `UrlView()`.

{% embed url="https://react.dev/reference/react/useState" %}
UseState hook explanation - Documentation
{% endembed %}

#### Fetching the data - useEffect hook

Next part is the way how the data are fetched.

Again, we don't wont to fetch the data everytime the component is re-rendered, but only if it is necessary. In our case, only for the first time when the component is rendered. To do so, we will use `useEffect` hook.

The `useEffect` hook in React is used to handle side effects in functional components. It allows you to run code after a component renders or when specific values change. Common side effects include fetching data, updating the DOM, or setting up subscriptions and event listeners.&#x20;

`useEffect` runs after the component renders, and/or when a specified dependency has changed).

The hook is used in the component as a function call with two parameters:

* the first parameter is the lambda doing the invocation,
* the second parameter are the dependencies; their change will trigger repeated first parameter invocation.

```typescript
useEffect(() => {
  const data = getData();  
  setUrls(data)
}, []);
```

In our case, we will:

* invoke a lambda as a first parameter; this lambda will get the data (somehow) and set them via the previously created variable-method `setUrls` from `useState` hook.
* specify empty list of dependencies; that means the `useEffect` hook will be invoked only once, when the component is rendered for the first time.

{% hint style="warning" %}
Note the difference between `useEffect(..., [])` and `useEffect(...)`. The latter one defines **no** dependencies causing the `useEffect` will be invoked **every time** the component is rendered.
{% endhint %}

{% embed url="https://react.dev/reference/react/useEffect" %}
UseEffect hook explanation - Documentation
{% endembed %}

#### Fetching the data - Axios + REST API

The next part is how to get the data from the REST API.

In React, typically one of two libraries are used - `fetch` or `axios`. As axios provide more properties and adjustments, we will stick with it.

{% hint style="info" %}
Note that `axios` dependency reference was added during the project initialization.
{% endhint %}

Let's start with a primitive implementation:

{% code lineNumbers="true" %}
```typescript
axios.get("http://localhost:32123/v1/url/1")
      .then(res => setUrls(res.data))
      .catch(err => console.error(err));
```
{% endcode %}

In the example:

* we do a HTTP GET request at the specified url (line 1),
* when the data are returned, a lambda expression in the  `then` defines what should be done next. In our case, we take the `res` result, extract the `res.data` containing the urls and set them via `setUrls()` hook method.
* when anything fails during the invocation, the error `err` will be captured and `catch` block containing a lambda telling us what do. In our case, we just simply print the output into the console.

Or, we can rewrite the invocation using the try-catch statements as known from other programming languages:

```
try{
  const res = await axios.get("http://localhost:32123/v1/url/1");
  setUrls(res.data);
}catch (err){
  console.error(err);
}
```

What is important here is that we changed from the synchronous invocation into the `async-await` asynchronou invocation. This topic is a bit complex. For now we need to know that the function wrapping the code mentioned in the previous listing must be _an asynchronous function_.

{% embed url="https://axios-http.com/docs/intro" %}
Axios - Documentation
{% endembed %}

#### Putting axios and useEffect together

If we want to stick with try-catch approach, the invocating function must be asynchronous and this makes the whole stuff a bit tricky. The reason is that the original `useEffect(...)` lambda is **not** asynchronous. In this case, our approach is IIFE.

{% hint style="info" %}
An IIFE (Immediately Invoked Function Expression) in TypeScript is a function that is defined and immediately executed as soon as it is declared. It is commonly used to create a private scope to avoid polluting the global scope and to encapsulate logic that doesn’t need to persist beyond the function’s execution.

An IIFE (Immediately Invoked Function Expression) can also be used with `async`/`await` to handle asynchronous code. This is particularly useful when you want to run some asynchronous logic immediately without having to define an explicit function and call it separately.
{% endhint %}

The whole code block will be:

{% code lineNumbers="true" %}
```typescript
useEffect(() => {
  (async () => {
    try {
      const res = await axios.get("http://localhost:32123/v1/url/1");
      setUrls(res.data);
    } catch (err) {
      console.error(err);
    }
  })();
}, []);
```
{% endcode %}

Here:

* We declare async anonymous method/lambda (starts at line 2, ends at line 9),
* In this async lambda, we do the get-data-stuff (lines 3-9),
* We immediatelly invocate the lambda (last `()` at line 9).
* The whole call (lines 2-9) are a content of lambda invoked and evaluated with `useEffect`.

### UrlList - show the data

Displaying the data is easy, acquired with simple HTML + JSX collaboration.

{% code lineNumbers="true" %}
```typescript
return (
    <div>
      <h1>Your Links</h1>
      <div>
        {urls.map(url => (<div className="urlRow" key={url.urlId}>
          <div className="urlTitle">{url.title}</div>
          <div><a href={url.address} rel="noreferrer" target="_blank">{url.address}</a></div>
        </div>))}
      </div>
    </div>
  );
```
{% endcode %}

We have a `return` statement. This statement returns a complex expression encapsulated in `()`.

* We return some HTML intro tags (lines 2-4).
* We take known urls in `urls` variable. We map every url from this variable into another complex expression ( `(...)` between lines 5 - 8).
  * The inner expression creates a `div` with css class `urlRow` and **mandatory** key attribute `url.UrlId` (line 5)
  * Then shows urls title - line 6.
  * Lastly, it shows an url address - line 7.
* Finally, the expression is enclosed with ending HTML tags (lines 9+).

{% hint style="info" %}
Note how `map` function over a list is used to create HTML content for every item in the array. It is a common technique.
{% endhint %}

{% hint style="info" %}
Note that when expanding items in a list into elements, every block must be uniquely identified. It is due to react ability to identificate and manage every element using DOM. Therefore, the `key` attribute must be added with a unique key, when such elements are created.
{% endhint %}

{% hint style="info" %}
Note that `rel="noreferrer"` should be used together with `target="_blank"` in `a` html tag **to avoid a potential security risk**.&#x20;
{% endhint %}

{% embed url="https://www.jitbit.com/alexblog/256-targetblank---the-most-underestimated-vulnerability-ever/" %}
About target="\_blank"
{% endembed %}

### UrlList - adding a custom style

In the code above, we have specified custom CSS classes with the elements.

{% hint style="info" %}
Note that CSS styles are assigned to the elements using `className` property, not ~~`class`~~.
{% endhint %}

We can define a CSS file valid only for a component file. Let's create a new file `url-list.css` located at the same folder as `url-list.tsx`:

```css
.urlTitle{
    font-weight: bold;
    font-size: large;
    color: purple;
}

.urlRow{
    margin-top:8px;
    padding-top:8px;
    padding-bottom:8px;
}

.urlRow:nth-of-type(odd) {
    background-color: #eee;
}
```

Now, add a reference to this style in the import section of the original `.tsx` file:

```typescript
// ....
import "./url-list.css";
// ...
```



## Summary

The final code of the created component follows:

```typescript
import {useEffect, useState} from "react";
import {UrlView} from "../../model/dtos/url-view";
import axios from "axios";
import "./url-list.css";

function UrlList(){
  const [urls, setUrls] = useState<UrlView[]>([]);

  useEffect(() => {
    (async () => {
      try {
        const res = await axios.get("http://localhost:32123/v1/url/1");
        setUrls(res.data);
      } catch (err) {
        console.error(err);
      }
    })();
  }, []);

  return (
    <div>
      <h1>Your Links</h1>
      <div>
        {urls.map(url => (<div className="urlRow" key={url.urlId}>
          <div className="urlTitle">{url.title}</div>
          <div><a href={url.address} rel="noreferrer" target="_blank">{url.address}</a></div>
        </div>))}
      </div>
    </div>
  );
}

export default UrlList;
```

If the application is started, the result can be immediatelly visible in the final page.

Note that to see the result, you must have your BE running. Also, change the target URL if required.

If data are provided by the BE, they are visible as a list in the output form. If BE is not running or some error occurs, you can see the issue in the browser's console window (typically opened by `F12` key).





[^1]: Added component reference

[^2]: Added component.
