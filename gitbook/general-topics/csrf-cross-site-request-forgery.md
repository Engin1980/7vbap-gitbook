# CSRF

Cross-Site Request Forgery (CSRF) is a type of web security attack where a malicious website tricks a user’s browser into performing unwanted actions on another site where the user is already authenticated. Because **browsers automatically include cookies and session tokens with requests**, an attacker can exploit this by making the victim’s browser send requests (like transferring money, changing account settings, or posting data) without the user’s knowledge. In essence, CSRF takes advantage of the trust a web application has in the user’s browser, making it appear as though the user intentionally initiated the action. Proper defenses, such as CSRF tokens or same-site cookie settings, are used to prevent these unauthorized requests. The schema is as follows:

1. **User logs into Site A** The user successfully authenticates on Site A (e.g., their bank or social media platform). The browser stores an authorization cookie or session token for that site.
2. **Browser automatically keeps the cookie** Whenever the user’s browser sends requests to Site A, it automatically includes the stored cookie, proving the user is authenticated.
3. **User visits a malicious Site B** While still logged into Site A, the user unknowingly navigates to Site B, which is controlled by an attacker.
4. **Malicious request is embedded** Site B contains hidden code (like a form, image, or script) that sends a crafted request to Site A. The browser, following its normal behavior, attaches the user’s cookie to this request.
5. **Site A processes the request** Because Site A sees a valid cookie, it assumes the request is legitimate and executes the action (e.g., transferring money, changing account settings).
6. **Result: unintended action** The user’s account on Site A is modified without their knowledge or consent, completing the CSRF attack.

![CSRF Illustration](imgs/csrf.png)

## Mitigating CSRF attacks

To protect applications from CSRF attacks, developers need to implement multiple layers of defense that ensure requests truly come from the intended user and origin. Here are the most common strategies:

* **Use CSRF tokens**: Generate unique, unpredictable tokens for each session or request and validate them server-side.
* **Enable SameSite cookies**: Configure cookies with `SameSite=Lax` or `Strict` to prevent them from being sent with cross-site requests.
* **Check request origin**: Validate the `Origin` or `Referer` headers to confirm that requests come from trusted domains.
* **Restrict sensitive actions to POST/PUT/DELETE**: Avoid using GET requests for state-changing operations, since they can be easily triggered by attackers.
* **Require re-authentication for critical actions**: Ask users to re-enter credentials or confirm actions (like password changes or money transfers).

#### CSRF Tokens

The back-end generates a unique, unpredictable token and sends it to the front-end, often as part of the initial authentication response. The front-end then stores this token securely in memory (e.g., in application state or a JavaScript variable, not in cookies or local storage) and attaches it to every subsequent API request, usually in a custom HTTP header such as `X-CSRF-Token`. On the server side, the back-end validates the token against the user’s session or a server-side store. If the token is missing or invalid, the request is rejected. This ensures that only requests intentionally initiated by the legitimate front-end are accepted, effectively blocking CSRF attacks in REST-based applications.

Alternatively, the **double-submit cookie principle** can be used: In this approach, the server issues a random CSRF token and **stores it in a cookie**. The front-end application must then read this token and include it in each request, typically in a custom header or request body. When the back-end receives a request, it compares the token from the cookie with the token provided in the request. If they match, the request is considered valid; if not, it is rejected. This works because an attacker can force a browser to send cookies, **but (the attacker) cannot read them (the cookies)** to extract and reuse the token, making forged requests fail validation.

* **Step 1: Server issues a CSRF token** When the user authenticates, the back-end generates a random CSRF token and sets it in a cookie (e.g., `csrfToken=abc123`).
* **Step 2: Front-end reads the cookie** The front-end retrieves the token value from the cookie (using JavaScript) and attaches it to every API request in a custom header (e.g., `X-CSRF-Token: abc123`).
* **Step 3: Server validates the request** On each request, the server compares the CSRF token in the cookie with the token in the custom header.
  * If they match → the request is considered legitimate.
  * If they don’t match or the header is missing → the request is rejected.

#### **SameSite cookies**

**SameSite cookies** are a browser security feature designed to help protect against **Cross-Site Request Forgery (CSRF)** and certain types of cross-site tracking. They control whether cookies are sent along with requests that originate from different sites. By setting the `SameSite` attribute on a cookie, developers can specify how strictly the browser should limit its use:

* **SameSite=Strict** 🛡️ Cookies are sent **only** when the request originates from the same site. If the user clicks a link from another site, the cookie will not be included. This offers the strongest CSRF protection but can break some cross-site functionality.
* **SameSite=Lax** ⚖️ Cookies are sent for same-site requests and for top-level navigation (like clicking a link), but not for background requests (like images or iframes). Moreover, those requests must use safe HTTP methods (like GET). This is a balanced default that protects against most CSRF attacks while allowing common user flows (like opening the link from e-mail to view product).
* **SameSite=None** 🌐 Cookies are sent with all requests, including cross-site ones, but must also be marked as `Secure` (only sent over HTTPS). This is necessary for scenarios like third-party APIs or embedded content, but it reintroduces CSRF risks if not combined with other protections.

By configuring `SameSite` correctly, developers reduce the chance that a malicious site can trick a user’s browser into sending cookies to another domain without their knowledge. It’s a simple but powerful layer of defense against CSRF.
