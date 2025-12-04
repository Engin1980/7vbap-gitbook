# XSS - Cross-Site Scripting

**Cross-Site Scripting (XSS)** is a web security vulnerability that allows attackers to inject malicious scripts into web pages viewed by other users. When the victim’s browser loads the page, the injected script runs as if it were trusted content, giving the attacker access to sensitive data like cookies, session tokens, or user inputs.

The three main types of XSS attacks are Reflected, Stored, and DOM-based. Each differs in how the malicious script is injected and executed:

* **Reflected XSS (Non-persistent)**
  * The malicious script is embedded in a URL or request parameter.
  * When the victim clicks the crafted link, the server reflects the input back in the response (e.g., in a search result or error message) without sanitization.
  * The script runs immediately in the victim’s browser but is not stored on the server.
  * Example: `http://example.com/search?q=<script>alert('XSS')</script>`
* **Stored XSS (Persistent)**
  * The attacker submits malicious input that is **saved in the database** or application storage.
  * Every time another user loads the affected page, the script is retrieved and executed in their browser.
  * This is more dangerous because it affects multiple users over time.
  * Example: Injecting `<script>alert('XSS')</script>` into a comment field, which is then displayed to all visitors.
* **DOM-based XSS**
  * The vulnerability exists entirely in the **client-side JavaScript code**.
  * The script manipulates the Document Object Model (DOM) directly, often using unsanitized values from `location`, `document`, or `window`.
  * No server-side reflection is needed; the attack happens in the browser.
  * Example: A script that reads `location.hash` and writes it directly into the page without escaping.

## Mitigating XSS attacks

There are several techniques used to avoid XSS attacks:

* Input & Output Handling
  * Input validation and sanitization
  * Output encoding/escaping (HTML, JavaScript, CSS, URL contexts)
  * Use filtering libraries for user input
* Framework & Code Practices
  * Use frameworks with built-in XSS protection
  * Avoid unsafe functions like `eval()` or `innerHTML` without sanitization
  * Limit inline JavaScript and unsafe attributes (`onerror`, `onclick`, etc.)
* Browser & Cookie Security
  * Use HTTP-only cookies for sensitive data (prevents JavaScript access)
  * Apply `SameSite` cookie settings to reduce cross-site misuse
  * Apply Content Security Policy (CSP) to restrict script sources and block inline scripts

#### TODO
