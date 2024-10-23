---
icon: square-4
---

# Security

## useHttp - custom hook

Before entering more security implementation, we will rewrite the usage of axios for easier code management and understanding. As a motivation and template, we use the common hooks already implemented in React and introducted in the previous chapters (useEffect, useState, useForm, ...).

So, we will create a `useHttp` hook to manage the HTTP communication instead of the direct `axios` usage.

Firstly, create a new file at `.../src/hooks/` called `use-hook.tsx`. Add the initial content:

```typescript
import axios from "axios";
axios.defaults.withCredentials = true;
const baseUrl = "http://localhost:32123";

// region Local functions

// endregion

// region Interceptors

// endregion


function useHttp(){
  
}

export default useHttp;
```

We declare a simple import, two regions used later (to beter understanding where the following code will be placed) and empty `useHttp()` method representing our hook. We have also introducet first default configuration setting, `withCredentials = true` (among others) telling axios to handle cookies in requests and responses. We also create a `baseUrl` of the backed so we do not need to specfiy the full URL in every request.

### Adding GET

To add HTTP GET support, we create a member (in fact, a variable) in the `useHttp` function with the lambda body declaring the required behavior.  The code is:

{% code lineNumbers="true" %}
```typescript
const get = async <T,> (url: string) => {
  const call$ = axios.get(baseUrl + url);
  const ret : Promise<T> = call$.then(q=>q.data);
  return ret;
}
```
{% endcode %}

Here, we:

* Declare a `get` as a async function doing a mapping from `url: string` into a  `Promise<T>` expecting that HTTP GET operation will return some data.
* The method accepts one argument `url` - line 1 - (a part relative to the base url - see line 2); we do not expect that data will be passed via HTTP GET request.
* We invoke a common axios get request - line 2.
* From the response, we extract only the data part - line 3
* And we return the data part - line 4.

Moreover:

* As we are dealing with TypeScript, we would like to keep type checks for `get` requests. Therefore, the function is generic with a type argument `<T, >`. The user in the usage will specify the expected type returned from the method to keep her/his typehints working.
* As we would like to use `try/catch` syntax over the `useHttp` methods, we are defining the function as `async`. Therefore, the return type from the `get` function is not the value itself, but a `Promise<T>` of the value.

The example usage can be:

```typescript
const http = useHttp();
const res = await http.get<UrlView[]>("/v1/url/1");
```

Here, you can see that we are invoking `http://localhost:32123/v1/url/1` request and telling via the generic parameter `<UrlView[]>` the expected return type. Therefore, the `res` datatype is `UrlView[]`.

### Adding POST (and others)

The HTTP POST implementation is very similar. The only difference is an additional argument for data:

```typescript
const post = async <T, >(url:string, data:any) => {
  const call$ = axios.post(baseUrl + url, data);
  const ret : Promise<T> = call$.then(q=>q.data);
  return ret;
}
```

Now, you can create all other required HTTP methods (PUT/PATCH/DELETE) according to your needs.

{% hint style="info" %}
Note that `delete` is a preserved word in TypeScript and you cannot use it as a variable/method name. You must adjust the name - e.g., `del`.
{% endhint %}

### Finalization

Once all required methods are implemented, you must return those methods from the `useHttp` hook:

{% code lineNumbers="true" %}
```typescript
import axios from "axios";
axios.defaults.withCredentials = true;
const baseUrl = "http://localhost:32123";

// region Local functions

// endregion

// region Interceptors

// endregion


function useHttp(){
  const get = async <T,> (url: string) => {
    // ...
  }

  const post = async <T, >(url:string, data:any) => {
    // ...
  }

  const del = async(url:string) =>{
    // ...
  }

  return {get, post, del};
}

export default useHttp;
```
{% endcode %}

Note line 27 where all variables(=methods=lambdas) are returned from `useHttp`.

Once done, we can adjust the source of the previously created components to use our hook.

{% code title="url-list.tsx" lineNumbers="true" %}
```typescript
// ...
import useHttp from "../../hooks/use-http"; // <--

function UrlList(){
  // ...
  const http = useHttp();

  useEffect(() => {
    (async () => {
      if (refresh){
        setRefresh(false);
        try {
          const res = await http.get<UrlView[]>("/v1/url/1"); // <--
          setUrls(res);
        } catch (err) {
          console.error(err);
        }
      }
    })();
  }, [refresh]);

  return (
    // ...
  );
}

export default UrlList;
```
{% endcode %}

Note hook was added at line 6 and its usage at line 13.

Similarly, for URL create:

{% code title="url-create-popup.tsx" %}
```typescript
// ...
import useHttp from "../../hooks/use-http"; // <--

// ...

function UrlCreatePopup(params : Params){
  // ...
  const http = useHttp();
  // ...

  const submitHandler : SubmitHandler<Data> = async data =>{
    const formData = new FormData();
    formData.append("address", data.address);
    formData.append("title", data.title);
    formData.append("appUserId", params.appUserId.toString());

    try{
      await http.post("/v1/url", formData); // <--
      closePopup();
      params.refresh();
      toast.success("Link stored successfully.");
      reset();
    }catch (err){
      console.log(err);
      toast.error("Link stored failed.");
    }
  }

  return (<div>
    // ...
  </div>);
}

export default UrlCreatePopup;
```
{% endcode %}

## CSRF

As an another part, we will add a CSRF token support into our `useHook`.

{% hint style="info" %}
Note that for this section, you should have correctly implemented BE sending CSRF token.
{% endhint %}

As mentioned in BE-Spring Boot, the CSRF token is returned as a cookie with the response when necessary.&#x20;

Our task is to detect this returned cookie, save its value and append this value in a HTTP Header on "dangerous" (= non-HTTP-GET) requests.

To do so, we will use _interceptors_. An interceptor is a functionality, which allows to tap and process the request before it is being send (or a response, before the value is returned). We will create a request interceptor adding a new HTTP header with CSRF token, if any exists.

To do so, we firstly create a function extracting a CSRF token from cookie, if there is any:

{% code lineNumbers="true" %}
```typescript
// region Local functions

function tryReadXsrfToken() {
  let ret: string | null;
  const xsrfCookie: string | null = document.cookie
    .split(";")
    .map((q) => q.trim())
    .filter((q) => q.startsWith("XSRF-TOKEN"))?.[0];
  if (xsrfCookie != null)
    ret = xsrfCookie.split("=")[1];
  else
    ret = null;
  return ret;
}

// endregion
```
{% endcode %}

The function

* Access all cookies stored in the current document - line 5;
* Split their definitions by their delimiter - line 6;
* Trims all the data to get rid of all unecessary white spaces - line 7;
* Selects only the first of those beginning with `XSRF-TOKEN` (that is the name of the cookie used in SpringBoot so store csrf token);
* If the cookie was found, we simply split its name from its value and take the value - line 10;
* If nothing were found, we return `null`.

### Response interceptor

Now, we will add a simple response interceptor, which adds a csrf token (if exists) to any request:

```typescript
//region Interceptors

axios.interceptors.request.use(
  (cfg) => {
    const csrfToken = tryReadXsrfToken();
    if (csrfToken != null) {
      cfg.headers["X-XSRF-TOKEN"] = csrfToken;
    }
    return cfg;
  }
)

//endregion
```

{% hint style="info" %}
Note that `X-XSRF-TOKEN` is a HTTP header name where SpringBoot expects the CSRF token if exists.
{% endhint %}





## CORS

todo

## Authentication & Authorization

todo
