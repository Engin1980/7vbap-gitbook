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

#### Input & Output Handling

**Input & Output Handling** is one of the most important defenses against XSS because it ensures that any data coming from users or external sources is properly cleaned before being displayed back in the browser. The idea is simple: never trust raw input, and always encode it correctly depending on where it will be used. For example, if a user enters `<script>alert('XSS')</script>` into a comment box, the server should sanitize the input so that the script tags are removed or escaped. On the output side, if you display user data inside HTML, you should encode special characters (`<`, `>`, `"`, `'`) so they appear as text rather than executable code. Similarly, if the data is inserted into JavaScript, you must escape quotes and backslashes to prevent breaking out of strings.&#x20;

Example of input raw HTML:

```html
<!-- User input: <script>alert('XSS')</script> -->
<div class="comment">
  <p><strong>User says:</strong> <script>alert('XSS')</script></p>
</div>
```

Example of sanitized HTML string:

```html
<!-- Sanitized output: &lt;script&gt;alert(&#39;XSS&#39;)&lt;/script&gt; -->
<div class="comment">
  <p><strong>User says:</strong> &lt;script&gt;alert(&#39;XSS&#39;)&lt;/script&gt;</p>
</div>
```

In CSS or URLs, different encoding rules apply (e.g., percent-encoding for URLs). By consistently validating input and encoding output for its specific context, applications prevent malicious scripts from being executed and keep user interactions safe.

#### Framework & Code Practices

**Framework & Code Practices** refers to using secure programming habits and leveraging modern frameworks that have built-in protections against XSS. Many frameworks (like React, Angular, or Vue) automatically escape user input when rendering it into the DOM, reducing the risk of malicious scripts being executed. On the coding side, developers should avoid unsafe functions such as `eval()`, `innerHTML`, or direct DOM manipulation with untrusted data, since these can introduce vulnerabilities. Instead, they should rely on safe APIs, templating engines, or framework utilities that handle escaping correctly. By combining secure coding practices with trusted frameworks, applications gain a strong layer of defense against XSS without requiring developers to manually sanitize every piece of user input.

**Most modern frameworks automatically sanitize or escape user input to reduce XSS risks.** Here’s a grouped list of common front-end and back-end frameworks that provide XSS protection by default:

* Frontend Frameworks (auto-escaping in templates)
  * **React** → Escapes values before rendering into the DOM; only `dangerouslySetInnerHTML` bypasses this.
  * **Angular** → Built-in sanitization for HTML, URLs, styles; unsafe values are blocked unless explicitly marked safe.
  * **Vue.js** → Escapes interpolated data in templates; only `v-html` directive bypasses sanitization.
  * **Svelte** → Escapes template expressions by default; raw HTML requires `{@html}` directive.
  * **Ember.js** → Auto-escapes template output; unsafe HTML requires explicit `htmlSafe()` usage.
* Backend Frameworks (templating engines with escaping)
  * **Django (Python)** → Template system escapes variables by default; `safe` filter required to render raw HTML.
  * **Ruby on Rails** → ERB templates escape output automatically; `raw` or `html_safe` needed to bypass.
  * **Spring MVC / Thymeleaf (Java)** → Escapes expressions in templates unless explicitly marked unescaped.
  * **ASP.NET Core (C#)** → Razor views HTML-encode output by default; `Html.Raw()` bypasses.
  * **Laravel (PHP)** → Blade templates escape variables automatically; `{!! !!}` syntax bypasses.
  * **Express.js with Handlebars/EJS (Node.js)** → Most templating engines escape variables unless explicitly told otherwise.

#### Browser & Cookie Security

**Browser & Cookie Security** focuses on protecting sensitive session data and ensuring cookies cannot be exploited for attacks like XSS or CSRF. Modern browsers provide mechanisms such as the `HttpOnly` flag, which prevents JavaScript from accessing cookies, reducing the risk of theft through injected scripts. The `Secure` flag ensures cookies are only transmitted over HTTPS, protecting them from interception. Additionally, the `SameSite` attribute restricts when cookies are sent with cross-site requests, helping to block CSRF attempts.

Complementing these cookie protections, a **Content Security Policy (CSP)** adds another powerful layer of defense. CSP allows developers to define which sources of scripts, styles, images, and other resources are considered trusted. For example, you can configure CSP to only allow scripts from your own domain and block inline JavaScript, preventing attackers from injecting malicious code even if they find a way to insert it into the page. By combining strict cookie settings with a well‑designed CSP, applications significantly reduce the attack surface for both session hijacking and script injection, creating a robust security posture at the browser level.
