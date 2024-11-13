# Introduction & Motivation

TODO Will be extended

## Source codes

The source codes to this pages are available via GitHub.

{% embed url="https://github.com/Engin1980/7vbap-gitbook" %}
7VBAP Source Codes
{% endembed %}

Note that the repository is still under the construction and some implementations can be available in a separate branches. If you have doubts, just ask.

## Project realization flow

{% hint style="danger" %}
Note here that the flow provided in following chapters **is not correct in real project realization.**
{% endhint %}

In real agile project, you are realizing functionalities not layer-by-layer (e.g.: database -> services -> API for back-end), but by features. However, as this context should be used as study source, we are aiming to explain everything important in the corresponding section, as we don't want in every part jump between different layers. Therefore, in this text we group and explain the project by layers (or technologies).

In agile development, project parts should be implemented by features. So, an example of the project flow may be:

1. Initial kick off with user
   1. Implement AppUser entity + repository
   2. Implement AppUser controller (to see what is needed in services)
   3. Implement AppUser related service
   4. Add tests
   5. Validate implementation
2. Add Tokes
   1. Implement Token entity + repository
   2. Extend existing/create new respective controllers + endpoints
   3. Extend/create new services
   4. ...
3. Deal with Login/Logout + JWT
4. Deal with CSRF
5. Add Urls (with subsequent tasks)
6. Add url Tags (with subsequent tasks)
7. ...

{% hint style="danger" %}
Note again, that **it is not correct** to define the whole database at the beginning at once, then implement all services, then do all REST API points.
{% endhint %}



