import axios from "axios";

axios.defaults.withCredentials = true;
const baseUrl = "http://localhost:32123";

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

//region Interceptors

axios.interceptors.request.use(
  (cfg) => {
    const csrfToken = tryReadXsrfToken();
    if (csrfToken != null) {
      cfg.headers["X-XSRF-TOKEN"] = csrfToken;
    }
    console.log("REQ " + cfg.url);
    return cfg;
  }
)

axios.interceptors.response.use(
  resp => resp,
  async (err) => {
    const originalRequest = err.config;
    const isFirstAttempt = !originalRequest.isRetry;
    const isOnlyOnceRequest = !originalRequest.url.endsWith("v1/appUser/refresh") && !originalRequest.url.endsWith("v1/appUser/login");
    if (err.response.status === 403 && isFirstAttempt && isOnlyOnceRequest) {
      try {
        // try to get a new access-token to a cookie
        const url = baseUrl + "/v1/appUser/refresh";
        console.log("request to " + url);
        await axios.post(url, null);
        // then, try again original request
        originalRequest.isRetry = true;
        return axios(originalRequest);
      } catch (exc) {
        return Promise.reject(err);
      }
    } else {
      console.error(err);
      return Promise.reject(err);
    }
  }
);


//endregion


function useHttp() {
  const get = async <T, >(url: string) => {
    const call$ = axios.get(baseUrl + url);
    const ret: Promise<T> = call$.then(q => q.data);
    return ret;
  }

  const post = async <T, >(url: string, data: any) => {
    const call$ = axios.post(baseUrl + url, data);
    const ret: Promise<T> = call$.then(q => q.data);
    return ret;
  }

  const del = async (url: string) => {
    const call$ = axios.delete(baseUrl + url);
    const ret: Promise<any> = call$.then(q => q.data);
    return ret;
  }

  return {get, post, del};
}

export default useHttp;