---
icon: square-3
description: >-
  In this section, an example of a component with form and its handling will be
  shown, including data validation and error management.
---

# Url Edit/Create - form component prototype

As a next example we will create a simple component with a form/editor of URL data. The form will be the same for new and existing URL, so we will refer to it as a UrlEdit component. However, for now, only the creation will be supported.

## Theoretical Introduction

### React-Hook-Form library

To create a form, we will use **React-Hook-Form** libary.

{% hint style="info" %}
We have added this dependency to a project during project creation.
{% endhint %}

**React Hook Form** is a popular library used for handling form validation and state management in React applications. It allows developers to easily create forms with minimal re-renders, improving performance. The library leverages React hooks to manage form state and validation without requiring controlled components, making it lightweight and simple to use. It have some main advantages:

* Performance: It minimizes the number of re-renders and avoids unnecessary computations by using uncontrolled inputs, making it faster than many alternatives.
* Easy Validation: It supports both built-in validation (via attributes like `required`, `minLength`, etc.) and custom validation through schemas like Yup.
* Simplicity: The API is easy to understand and use, making form handling less complex in React applications.

To add the required behavior into the form, you have to:

* declare required variables and callbacks using `useForm` command;
* map the input elements using the `register` function.

#### General "useForm" usage

In general, we have to add a code to access form behavior and data:

```
const { register, handleSubmit, watch, formState: { errors } } = useForm<Inputs>();
```

Here we add:

* **register** to map HTML input elements to form data;
* `handleSubmit` function managing form submission;
* `watch` functionality to access current form values;
* `formState` object to handle primarily the issues and errors.

Moreover, we define a form data type for typescript. In the code above, we need a type `Inputs` defining the data structure.

#### Registering input elements

The next step is mapping html `<input ...` elements to form fields. To do so, `register` definition declared above is used.

```html
<input defaultValue="URL Address" {...register("address")} />
```

Here, we expect type `Inputs` has a field called `address`.

We can also add validation requirements:

```html
<input
  className="mb-3 mt-4"
  label="Address"
  type="url"
  {...register("address", {
    required: "This field is required",
    pattern: {
      value: /^http.+\..+/,
      message: "URL Address must start with http...",
    },
  })} />
```

Here, we have added class name, label, type and a complex `register` declaration defining that the address field is required and match the required regular expression.

The valid validators can be found at the documentation web pages.

{% embed url="https://www.react-hook-form.com/" %}
React-Hook-Form - Documentation
{% endembed %}

Validation handler typically follows the respective `<input>` element using `errors` object of the respective field followed by the element shown in the case of error, like:

```typescript
{errors.address && (
  <span className="invalid-feedback d-block">
          {errors.address.message}
        </span>
)}
```

This parts:

* Firstly validates a condition `errors.address` beeing true -> it means "non-empty" in this case. If the value is `True`, the `&&` operator ensures the rest of the statement is evaluated too;
* The second part of the statement specifies the element shown in the case of the failure.

### Material Design for Bootstrap

Another useful library in the project is Material Design for Bootstrap (for React), which enables a simple bootstrap usage in the React  using predefined components and elements.

We will meet with its component several times. Its components starts with `<MDB...` prefix. For inputs, we will use `<MDBInput>`.

{% embed url="https://mdbootstrap.com/docs/react/" %}

## Creating an input component

Let's create a new file in the project - `src/components/url/url-edit.tsx`:

```typescript
function UrlEdit(){
  return <div></div>;
}

export default UrlEdit;
```

For immediate testing, we can add this component directly into the `App.tsx` file to extend the layout:

```typescript
// ...
function App() {
  return (
    <div className="App">
      <header className="App-header">
       FAVOURITE URLs
      </header>
      <UrlList />
      <UrlEdit />
    </div>
  );
}
// ...
```

From now, when the app is shown in the browser, you should see the edit component underneath the list of urls.

### Creating a form

#### Add the required imports

Firstly, we mention all the required imports. They can be added later as they are used. However, we mention them here for the clarity:

```typescript
import {SubmitHandler, useForm} from "react-hook-form";
import React from "react";
import {MDBBtn, MDBCol, MDBContainer, MDBInput, MDBRow} from "mdb-react-ui-kit";
```

#### Declaring form data object

Firstly, we will create a new type in the file containing form data:

```typescript
type Data = {
  address : string;
  title : string;
  appUserId : number;
};
```

Note this type is defined **outside** of the `UrlEdit()` function.

#### Declaring form data accessors

As a next step, we define object used to work with the form.

```typescript
function UrlEdit(){
  const {
    register,
    handleSubmit,
    formState : {errors}
  } = useForm<Data>();

  return <div></div>;
}
```

#### Prototyping a submit handler

Next, we will prototype a simple submit function as:

```typescript
const submitHandler : SubmitHandler<Data> = data =>{
  // submit data to REST API
}
```

Later, we will extend this function to handle and submit the data into the REST API on BE. For now, we only need a declaration so it can be used in the next step.

#### Creating the HTML form

Now, we can declare the whole form as two input fields and one submit button as a return of the function at once:

{% code lineNumbers="true" %}
```typescript
return (<div>
    <form onSubmit={handleSubmit(submitHandler)}>

      <MDBInput
        className="mb-3 mt-4"
        label="Title"
        type="text"
        {...register("title", {
          required: "This field is required."
        })}
      />
      {errors.title && (
        <span className="invalid-feedback d-block">
                  {errors.title.message}
                </span>
      )}

      <MDBInput
        className="mb-3 mt-4"
        label="Address"
        type="url"
        {...register("address", {
          required: "This field is required",
          pattern: {
            value: /^http.+\..+/,
            message: "URL Address must start with http...",
          },
        })} />
      {errors.address && (
        <span className="invalid-feedback d-block">
                {errors.address.message}
              </span>
      )}

      <MDBBtn type="submit">Save</MDBBtn>
    </form>
  </div>);
```
{% endcode %}

Here, we:

* Declare a form handled by `handleSubmit` method with `submitHandler` parameter obtained from `useForm` above - line 2;
* Create an input for title with its mapping to respective data field - lines 4-11;
* Add an error handling for this field - lines 12-16;
* Do the same for address field - lines 18-28 / 29-33;
* Create a submit button - line 35;

Note that instead of typical `<input ...` elements we use material design `<MDBInput ...` counterparts.

Finally, we can encapsulate the form into the bootstrap layout elements:

```typescript
  return (<div>
    <MDBContainer>
      <MDBRow className="justify-content-center">
        <MDBCol md="6">
          <form onSubmit={handleSubmit(submitHandler)}>
            <!-- form content -->
          </form>
        </MDBCol>
      </MDBRow>
    </MDBContainer>
  </div>);
```

TODO img validating toast

### Implementing the form submission

Now, we can update the content of the submitting function `handleSubmit(...)`.

Firstly, we create a data that will be transfered via the HTTP request. We transfer the data via form data.

{% hint style="info" %}
Note that there are several options how data can be transferred to the server. Several options are via request payload, form data, headers, cookies. The selected technique depends on the data type, size and other requirements.
{% endhint %}

Firstly, we create `FormData` object tranfering the data. We use mock `appUserId` for the new record:

```typescript
const appUserId = 1; // mock appUserId of an existing user

const formData = new FormData();
formData.append("address", data.address);
formData.append("title", data.title);
formData.append("appUserId", appUserId.toString());
```

Then we simply send the request via axios:

```typescript
axios.post("http://localhost:32123/v1/url", formData);
```

Now, lets encapsulate the code in the method and add an error handling:

```typescript
const submitHandler : SubmitHandler<Data> = async data =>{
  const appUserId = 1; // mock appUserId of an existing user

  const formData = new FormData();
  formData.append("address", data.address);
  formData.append("title", data.title);
  formData.append("appUserId", appUserId.toString());

  try{
    await axios.post("http://localhost:32123/v1/url", formData);
    console.log("Successfully stored");
  }catch (err){
    console.log(err);
  }
}
```

## Advice result using toasts

At the end, let's advice the result using the toasts library added before. We just simply add an import at the beginning of the file:

```typescript
import {toast} from "react-toastify";
```

Then, we can add a toast invocation:

```typescript
try{
  await axios.post("http://localhost:32123/v1/url", formData);
  toast.success("Link stored successfully.");
}catch (err){
  console.log(err);
  toast.error("Link stored failed.");
}
```

TODO img ok toast

## Form as a popup/modal

Next step is to use the form as a popup based on a button click.

As we need to integrate together button (to open the popup) and the popup itself, it can be benefical to do the whole stuff in one component.

### Create the popup - Url-Edit-Popup.tsx

So, firstly, we will move the content into the new component (or you can rename the old one into the new one) so we can distinquish from the name that we are dealing with the popup component: `url-edit-popup.tsx`.

Lets start with the with some data we already know and one addition:

{% code lineNumbers="true" %}
```typescript
import {SubmitHandler, useForm} from "react-hook-form";
import {MDBBtn, MDBContainer, MDBInput} from "mdb-react-ui-kit";
import axios from "axios";
import {toast} from "react-toastify";
import {useRef} from "react";
import Popup from "reactjs-popup";

type Data = {
  address : string;
  title : string;
  appUserId : number;
};

type Params = {
  appUserId : number;
  refresh: () => void | undefined;
};

function UrlEditPopup(params : Params){
  const {
    register,
    handleSubmit,
    formState : {errors}
  } = useForm<Data>();
}

export default UrlEditPopup;
```
{% endcode %}

What has been added:

* We need a smart way how to pass data to this component. We need to know I) the id of the user; II) the way how to tell the `<UrlList />` that it should reload the data from the server. So, we use the `Params` type (lines 14-16)
  * `appUserId` is simply a numerical id of the user to whom the new/edited url will be asigned;
  * `refresh` function callback invoked when the data has been changed and the reload is needed.
* We add this type as a parameter of `UrlEditPopup` - line 18.

Next, we need to define callback functions to open and close the popup. To do so, we will use `useRef` hook and add the necessary code into the component:

```typescript
function UrlEditPopup(params : Params){
  // ...
  const cbRef = useRef<any>();

  const openPopup = () => cbRef.current.open();
  const closePopup = () => cbRef.current.close();
}
```

UseRef hook is used, when we need to keep in the component a value, that will not be used for rendering. So, its usage is very similar to `useState` except the rendering condition. It contains a single value `current`, when the current value is stored. In our case, this value will contain methods to open and close the popup. We will initialize the value later (see the HTML code), but now we use those values to define opening and closing functions.

{% embed url="https://react.dev/reference/react/useRef" %}

Next step is a slight adjustment of the data submitting function:

{% code lineNumbers="true" %}
```typescript
// ...

function UrlEditPopup(params : Params){
  // ...

  const submitHandler : SubmitHandler<Data> = async data =>{
    // ...

    try{
      await axios.post("http://localhost:32123/v1/url", formData);
      closePopup();
      params.refresh();
      toast.success("Link stored successfully.");
    }catch (err){
      console.log(err);
      toast.error("Link stored failed.");
    }
  }
}

export default UrlEditPopup;
```
{% endcode %}

Here, we have added popup close (line 11) and refresh invocation (line 12).

Finally, the updated code of the HTML/JSX returned from the component:

{% code lineNumbers="true" %}
```typescript
// ...

function UrlEditPopup(params : Params){
  // ...

  return (<div>
    <MDBBtn onClick={openPopup}>Add URL</MDBBtn>
    <Popup ref={cbRef}>
      <MDBContainer>
        <h1>Edit link details</h1>
        <form onSubmit={handleSubmit(submitHandler)}>
        // ... form remains the same
        </form>
      </MDBContainer>
    </Popup>
  </div>);
}

export default UrlEditPopup;
```
{% endcode %}

We have:

* added a button invoking `openPopup` function - line 7
* added a popup with the initialization of `cbRef` variable using `ref` attribute - line 7; this is the place where the value is set into the `cbRef` variable declared using `useRef` handler above;
* added a caption (line 10) and remove bootstrap row/cols (you can adjust those to your needs); the rest of the HTML form same as in the previous version.

### Update the popup display

So far we were displaying the edit form in the `App.tsx` file. Firstly, lets remove `<UrlEdit />` element from there as the form will be displayed via the button on `<UrlList />` component.

Secodly, lets add the component into the url list.&#x20;

Firstly, we need to create a `refresh` state variable - when its value is true, the data should be refreshed:

{% code lineNumbers="true" %}
```typescript
// ...

function UrlList(){
  const [urls, setUrls] = useState<UrlView[]>([]);
  const [refresh, setRefresh] = useState<boolean>(true);
  const doRefresh : () => void =  () => setRefresh(true);

  useEffect(() => {
    (async () => {
      if (refresh){
        setRefresh(false);
        try {
          const res = await axios.get("http://localhost:32123/v1/url/1");
          setUrls(res.data);
        } catch (err) {
          console.error(err);
        }
      }
    })();
  }, [refresh]);

  // ...
}
```
{% endcode %}

We have created a `refresh`/ `setRefresh` state variable - line 5 - with default value `true` as data needs to be loaded for the first time. We have created `doRefresh` function simply changing the `refresh` value to `true`; this function will be used as the refresh callback passed into the popup. Finally, do the `refresh` value check / behavior in the component initializatio - lines 10, 11.

Next part is the updated of the HTML/JSX code:

{% code lineNumbers="true" %}
```typescript
// ...

function UrlList(){
  // ...

  return (
    <div>
      <h1>Your Links</h1>
      <div>
        <UrlEditPopup appUserId={1} refresh={doRefresh} />
        {urls.map(url => (<div className="urlRow" key={url.urlId}>
          <div className="urlTitle">{url.title}</div>
          <div><a href={url.address} rel="noreferrer" target="_blank">{url.address}</a></div>
        </div>))}
      </div>
    </div>
  );
}
```
{% endcode %}

Here, we only declare a `<UrlEditPopup ...` element (line 10). We pass the `appUserId` and `refresh` parameters to the component.

TODO popup image.

