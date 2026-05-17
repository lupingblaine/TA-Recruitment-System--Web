/**
 * 3D Tilt Effect for Glassmorphism Cards/Images
 */
class VanillaTilt {
  constructor(element, settings = {}) {
    this.element = element;
    this.settings = Object.assign({
      max: 15,
      perspective: 1000,
      scale: 1.05,
      speed: 400,
      glare: true,
      "max-glare": 0.3
    }, settings);

    this.reverse = this.settings.reverse ? -1 : 1;
    this.glare = this.settings.glare;
    this.glarePrerender = this.settings["glare-prerender"];

    this.init();
  }

  init() {
    this.prepareGlare();
    this.addEventListeners();
    this.updateBind = this.update.bind(this);
    this.element.style.transform = `perspective(${this.settings.perspective}px)`;
  }

  prepareGlare() {
    if (!this.glare) return;
    const glareWrapper = document.createElement("div");
    const glare = document.createElement("div");

    glareWrapper.classList.add("js-tilt-glare");
    glare.classList.add("js-tilt-glare-inner");

    glareWrapper.appendChild(glare);
    this.element.appendChild(glareWrapper);

    this.glareElementWrapper = glareWrapper;
    this.glareElement = glare;
  }

  addEventListeners() {
    this.onMouseEnterBind = this.onMouseEnter.bind(this);
    this.onMouseMoveBind = this.onMouseMove.bind(this);
    this.onMouseLeaveBind = this.onMouseLeave.bind(this);

    this.element.addEventListener("mouseenter", this.onMouseEnterBind);
    this.element.addEventListener("mousemove", this.onMouseMoveBind);
    this.element.addEventListener("mouseleave", this.onMouseLeaveBind);
  }

  onMouseEnter() {
    this.updateElementPosition();
    this.element.style.transition = `transform ${this.settings.speed}ms cubic-bezier(.03,.98,.52,.99)`;
    if(this.glare) this.glareElement.style.transition = `opacity ${this.settings.speed}ms cubic-bezier(.03,.98,.52,.99)`;
  }

  onMouseMove(event) {
    window.requestAnimationFrame(this.updateBind);
    this.updateElementPosition();
    this.mouseX = event.clientX - this.left;
    this.mouseY = event.clientY - this.top;
  }

  onMouseLeave() {
    this.element.style.transition = `transform ${this.settings.speed}ms cubic-bezier(.03,.98,.52,.99)`;
    this.element.style.transform = `perspective(${this.settings.perspective}px) rotateX(0deg) rotateY(0deg) scale3d(1, 1, 1)`;
    if(this.glare) {
      this.glareElement.style.transition = `opacity ${this.settings.speed}ms cubic-bezier(.03,.98,.52,.99)`;
      this.glareElement.style.opacity = 0;
    }
  }

  updateElementPosition() {
    const rect = this.element.getBoundingClientRect();
    this.width = rect.width;
    this.height = rect.height;
    this.left = rect.left;
    this.top = rect.top;
  }

  update() {
    let x = (this.mouseX / this.width) * 2 - 1;
    let y = (this.mouseY / this.height) * 2 - 1;
    
    let tiltX = this.reverse * (this.settings.max * y).toFixed(2);
    let tiltY = this.reverse * (this.settings.max * x).toFixed(2);

    this.element.style.transform = `perspective(${this.settings.perspective}px) rotateX(${-tiltX}deg) rotateY(${tiltY}deg) scale3d(${this.settings.scale}, ${this.settings.scale}, ${this.settings.scale})`;

    if (this.glare) {
      let angle = Math.atan2(this.mouseX - (this.width / 2), -(this.mouseY - (this.height / 2))) * (180 / Math.PI);
      this.glareElement.style.transform = `rotate(${angle}deg) translate(-50%, -50%)`;
      this.glareElement.style.opacity = `${(this.mouseX / this.width) * this.settings["max-glare"]}`;
    }
  }
}

window.addEventListener("DOMContentLoaded", () => {
  document.querySelectorAll("[data-tilt]").forEach(el => {
    new VanillaTilt(el);
  });
});
