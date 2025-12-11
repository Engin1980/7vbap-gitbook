# User Authentication - Session / JWT

## User Authentication Basics

When a frontend application communicates with a backend over HTTPS, the communication is **stateless**. This means that each request is independent, and the server does not automatically remember who is sending it. Therefore, the frontend and backend must establish a method through which the backend can recognize the user after login.

### Session-Based Authentication

One classic solution is to use **server-side sessions**. After the user logs in:

* The backend creates a **session** and stores the session data on the server.
* It sends the frontend a **sessionId**, usually stored in a cookie.
* On every subsequent request, the browser automatically sends the cookie.
* The backend receives the `sessionId`, looks up the session data on the server, and identifies the user.

This works well, but the server must maintain a session store, which increases memory usage and makes scaling more difficult.

A session ends when the server deletes it (e.g., user clicks **Logout**), or when it **expires** after inactivity, so the logout is simple: the backend removes the session from its store.

As `sessionId` is typically stored in the cookie, there is also a significant security risk called **CSRF:** Because cookies are sent automatically by the browser, session-based auth is vulnerable to CSRF attacks unless protections like SameSite cookies, CSRF tokens, or custom headers are used.

### JWT-Based Authentication

Another approach is using **JWT (JSON Web Token)**. Unlike a session, a JWT contains all necessary information **inside the token itself**.

Workflow:

* After login, the server issues a signed JWT.
* The frontend stores it (typically in local storage or a cookie).
* On each request, the frontend sends the JWT (often in the Authorization header).
* The server verifies the signature to confirm the token is valid.
* If valid, the server trusts the data inside the JWT (e.g., user ID, roles), without needing to store anything.

Because the token is self-contained, the backend remains stateless—no session storage is required.

However, the logout process is a bit more complicated.  JWTs have a built‑in **expiration time**; once expired, the client must obtain a new one. However, until the expiration time, the token remains valid. Deleting the token from the client storage on the front-end does not mean that the server refuses the token in the future. Server-side invalidation is possible only with additional mechanisms (e.g., blocklists), since the server does not store tokens by default.

### JWT Structure and How It Works

A JWT consists of **three parts**, separated by dots:

1. **Header** – specifies the token type (JWT) and the signing algorithm (e.g., HS256).
2. **Payload** – contains the **claims**, such as user ID, roles, expiration time, and any additional metadata.
3. **Signature** – used to verify that the token has not been tampered with. It is created by taking the header and payload, encoding them, and signing them using a secret key (HMAC) or a private key (RSA/ECDSA).

#### How a JWT Is Created

1. The server creates the **header** and **payload**.
2. Both parts are Base64URL‑encoded.
3. The server signs the encoded header + payload using its secret/private key.
4.  The result is a string in the format:

    `header.payload.signature`

#### How JWT Validation Works

1. The server receives the JWT from the client.
2. It extracts the header and payload.
3. It recalculates the signature using the stored secret/public key.
4. If the signature matches, the token is considered **authentic**.
5. The server also checks **token expiration** and other constraints (see claims below).
6. If everything is valid, the server trusts the data inside the payload (e.g., user identity, roles).

### Common JWT Claims

<table data-header-hidden><thead><tr><th width="107"></th><th width="159"></th><th width="280"></th><th></th></tr></thead><tbody><tr><td>Claim Key</td><td>Name</td><td>Purpose</td><td>Example Value</td></tr><tr><td><code>sub</code></td><td>Subject *)</td><td>Identifies the user or entity the token refers to (id/email/...). </td><td><code>"12345"</code> </td></tr><tr><td><code>iss</code></td><td>Issuer</td><td>Identifies the authority that issued the token</td><td><code>"https://api.example.com"</code></td></tr><tr><td><code>aud</code></td><td>Audience</td><td>Specifies who the token is intended for</td><td><code>"my-frontend-app"</code></td></tr><tr><td><code>exp</code></td><td>Expiration Time *)</td><td>UNIX timestamp when the token expires</td><td><code>1712345678</code></td></tr><tr><td><code>iat</code></td><td>Issued At</td><td>UNIX timestamp when the token was created</td><td><code>1712331234</code></td></tr><tr><td><code>nbf</code></td><td>Not Before</td><td>Token becomes valid only after this time</td><td><code>1712335000</code></td></tr><tr><td><code>jti</code></td><td>JWT ID</td><td>A unique identifier for the token (useful for blacklist)</td><td><code>"token-abc-789"</code></td></tr></tbody></table>

> \*) Items are typically mandatory.

These claims help define the validity window, intended audience, issuer, and the identity represented by the token. Additional custom claims (e.g., `role`, `permissions`) can also be added depending on application needs.

### Validation of Common Claims

When verifying a JWT, the server not only checks the signature, but also validates key claims to ensure the token is used correctly and safely:

* **`iat`** (Issued At):
  * Ensures the token was not created "in the future" due to clock drift or tampering.
  * The server checks that `iat <= current_time`.
* **`nbf`** (Not Before):
  * The token must not be accepted before this timestamp.
  * The server checks that `current_time >= nbf`.
* **`exp`** (Expiration Time):
  * Defines when the token becomes invalid.
  * The server checks that `current_time < exp`.
  * If expired, the token is rejected.
* **`iss`** (Issuer):
  * Confirms that the token was issued by the expected authority.
  * The server compares the claim with its configured trusted issuer(s).
  * If the value does not match (e.g., `"https://api.example.com"`), the token is rejected.
* **`aud`** (Audience):
  * Ensures the token is intended for the current service.
  * The server verifies that the claim matches its own identifier (e.g., app name, API URL).
  * If the audience does not match, the token must be rejected.

Time‑based claims (`iat`, `nbf`, `exp`) typically allow a small **time skew** to compensate for differences between server clocks in distributed systems.

By validating these claims along with the signature, the server ensures the JWT is both **authentic** (not tampered with) and **used under the correct conditions** (correct time window, issuer, and audience).

{% hint style="info" %}
A significant advantage of JWTs is that the token generation does not need to occur on the target backend; instead, it is often delegated to a trusted third-party service known as an **Identity Provider** (IdP). In this distributed setup, it is crucial to use asymmetric cryptography (a combination of a private and public key). The IdP signs the token using its private key, while the backend services verify it using the corresponding public key. This separation ensures security because the private signing key is never shared with the audience, preventing the backend services from forging valid tokens.
{% endhint %}

### Access Token + Refresh Token

A single long‑lived (hours/days/weeks) JWT is dangerous: if an attacker steals it, they can use it until it expires — and because JWTs are stateless, **they cannot be revoked** without additional mechanisms. On the other hand, if the JWT expires too quickly (seconds/minutes), the user is forced to log in repeatedly, which becomes annoying and harms usability.

Modern authentication systems often use **two tokens instead of one**: an **Access Token** and a **Refresh Token**. The motivation behind this approach comes from the conflicting requirements of security and usability.

* The **Access Token** (JWT) should be **short‑lived** (typically 5–15 minutes) to minimize damage if it gets stolen. A stolen access token expired after a few minutes poses far less risk than one that lives for hours or days.
* The **Refresh Token** (typically any arbitrary token string, e.g., GUID) should allow the user to **stay logged in** without needing to re‑enter credentials. It is **long‑lived** (often days to weeks), but is kept more securely and used much less frequently.

This split provides important benefits:

* **Improved security** — even if the access token leaks, it becomes useless quickly.
* **Better user experience** — the refresh token silently obtains new access tokens, so the user stays authenticated without re‑logging in.
* **Stateless backend** — access tokens can remain self‑contained JWTs, while refresh tokens allow controlled long‑term sessions.

In short, using Access + Refresh Tokens combines the safety of short expiration times with the convenience of persistent login.

### Access + Refresh Token Flow

The cooperation between the access token and refresh token works as follows:

1. **Login:**
   * User provides credentials.
   * Server issues a short-lived access token and a long-lived refresh token and send both of them back to the client. Server also keeps its own list of valid refresh tokens.
2. **Accessing resources:**
   * Client sends the access token (only) in the Authorization header for API requests.
   * Server validates the access token as a common JWT. If valid, request proceeds.
3. **Token expiration:**
   * When the access token expires and is rejected by the server, the client sends immediatelly a new request with the refresh token to a dedicated endpoint.
   * Server validates the refresh token (w.r.t to its own list of valid refresh tokens) and, if valid, issues a new access token (and optionally a new refresh token).
4. **Logout:**
   * Client deletes both access and refresh tokens.
   * Server may revoke the refresh token (if a revocation mechanism exists), effectively ending the session.
   * Note that the access token still remains valid (until it expiration). However, as its life-span is short, it is typically not a big issue.

**Note:** For long-term logins (weeks/months), it is recommended to **rotate the refresh token**. This means issuing a new refresh token each time the old one is used, invalidating the previous one to reduce risk if a token is stolen.
