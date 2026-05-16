const Framework = {
  config: {
    debug: false,
    refreshInterval: 10000,
  },

  init(config = {}) {
    this.config = { ...this.config, ...config };
    if (this.config.debug) console.log("Framework initialized", this.config);
  },

  log(...args) {
    if (this.config.debug) console.log("[Framework]", ...args);
  },

  error(...args) {
    console.error("[Framework]", ...args);
  },
};

const DOM = {
  get(selector) {
    return document.querySelector(selector);
  },

  getAll(selector) {
    return document.querySelectorAll(selector);
  },

  create(tag, attrs = {}, children = []) {
    const element = document.createElement(tag);
    Object.keys(attrs).forEach((key) => {
      element.setAttribute(key, attrs[key]);
    });
    children.forEach((child) => {
      if (typeof child === "string") {
        element.appendChild(document.createTextNode(child));
      } else {
        element.appendChild(child);
      }
    });
    return element;
  },

  addClass(element, ...classes) {
    classes.forEach((cls) => element.classList.add(cls));
  },

  removeClass(element, ...classes) {
    classes.forEach((cls) => element.classList.remove(cls));
  },

  hasClass(element, className) {
    return element.classList.contains(className);
  },

  toggleClass(element, className) {
    element.classList.toggle(className);
  },
};

const Events = {
  on(element, event, handler) {
    element.addEventListener(event, handler);
    return () => this.off(element, event, handler);
  },

  off(element, event, handler) {
    element.removeEventListener(event, handler);
  },

  once(element, event, handler) {
    const wrapper = (...args) => {
      handler(...args);
      this.off(element, event, wrapper);
    };
    this.on(element, event, wrapper);
  },

  delegate(element, event, selector, handler) {
    const wrapper = (e) => {
      if (e.target.matches(selector)) {
        handler(e);
      }
    };
    this.on(element, event, wrapper);
    return () => this.off(element, event, wrapper);
  },
};

const HTTP = {
  async get(url) {
    const response = await fetch(url);
    if (!response.ok) throw new Error(`HTTP ${response.status}`);
    return response.json();
  },

  async post(url, data) {
    const response = await fetch(url, {
      method: "POST",
      headers: { "Content-Type": "application/json" },
      body: JSON.stringify(data),
    });
    if (!response.ok) throw new Error(`HTTP ${response.status}`);
    return response.json();
  },

  async put(url, data) {
    const response = await fetch(url, {
      method: "PUT",
      headers: { "Content-Type": "application/json" },
      body: JSON.stringify(data),
    });
    if (!response.ok) throw new Error(`HTTP ${response.status}`);
    return response.json();
  },

  async delete(url) {
    const response = await fetch(url, { method: "DELETE" });
    if (!response.ok) throw new Error(`HTTP ${response.status}`);
    return response.json();
  },
};

window.Framework = Framework;
window.DOM = DOM;
window.Events = Events;
window.HTTP = HTTP;
