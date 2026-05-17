/**
 * Interactive Rive Bear for Auth Forms
 * - Uses @rive-app/canvas to render highly fluid vector animations
 * - Binds input events to Rive State Machine inputs
 */
class RiveLoginAnim {
  constructor(containerId, ctxPath) {
    this.container = document.getElementById(containerId);
    if (!this.container) return;
    
    // Create canvas element for Rive
    this.canvas = document.createElement('canvas');
    this.canvas.width = 400;
    this.canvas.height = 400;
    this.canvas.style.width = '100%';
    this.canvas.style.height = '100%';
    this.canvas.style.transform = 'scale(1.2)'; // Make it slightly larger to fit well
    this.canvas.style.transition = 'filter 0.6s cubic-bezier(0.34, 1.56, 0.64, 1)'; // Smooth color transition
    this.container.innerHTML = '';
    this.container.appendChild(this.canvas);
    
    // Rive State Machine Inputs
    this.inputs = {
      isChecking: null,
      numLook: null,
      isHandsUp: null,
      trigSuccess: null,
      trigFail: null
    };
    
    this.initRive(ctxPath);
    this.bindEvents();
  }
  
  initRive(ctxPath) {
    // Determine the path to the riv file
    const rivPath = ctxPath ? `${ctxPath}/static/assets/login.riv` : '/tamo/static/assets/login.riv';

    // The popular teddy bear has "Login Machine" as the State Machine name
    this.riveInstance = new window.rive.Rive({
      src: rivPath,
      canvas: this.canvas,
      autoplay: true,
      stateMachines: 'Login Machine',
      onLoad: () => {
        // Stop any resizing layout issues
        this.riveInstance.resizeDrawingSurfaceToCanvas();
        
        // Find inputs
        const inputs = this.riveInstance.stateMachineInputs('Login Machine');
        if (inputs) {
          inputs.forEach(input => {
            if (input.name === 'isChecking') this.inputs.isChecking = input;
            if (input.name === 'numLook') this.inputs.numLook = input;
            if (input.name === 'isHandsUp') this.inputs.isHandsUp = input;
            if (input.name === 'trigSuccess') this.inputs.trigSuccess = input;
            if (input.name === 'trigFail') this.inputs.trigFail = input;
          });
        }
      }
    });
  }
  
  bindEvents() {
    const trackInputs = document.querySelectorAll('input[type="text"], input[type="email"]');
    const passwordInputs = document.querySelectorAll('input[type="password"]');
    
    trackInputs.forEach(input => {
      input.addEventListener('focus', () => {
        if (this.inputs.isChecking) this.inputs.isChecking.value = true;
        this.track(input);
      });
      input.addEventListener('input', () => this.track(input));
      input.addEventListener('blur', () => {
        if (this.inputs.isChecking) this.inputs.isChecking.value = false;
      });
    });
    
    passwordInputs.forEach(input => {
      input.addEventListener('focus', () => {
        if (this.inputs.isHandsUp) this.inputs.isHandsUp.value = true;
      });
      input.addEventListener('blur', () => {
        if (this.inputs.isHandsUp) this.inputs.isHandsUp.value = false;
      });
    });
  }
  
  track(input) {
    if (this.inputs.isHandsUp && this.inputs.isHandsUp.value) return; // Don't track if covering eyes
    if (!this.inputs.numLook) return;
    
    // The numLook input goes from 0 to 100
    // We map the length of the string to this range
    const len = Math.min(input.value.length, 30);
    // Assuming 0 is left, 100 is right
    const val = (len / 30) * 100;
    this.inputs.numLook.value = val;
  }
  
  triggerSuccess() {
    if (this.inputs.trigSuccess) this.inputs.trigSuccess.fire();
  }
  
  triggerFail() {
    if (this.inputs.trigFail) this.inputs.trigFail.fire();
  }
  
  setRoleColor(role) {
    if (!this.canvas) return;
    
    // Default is Applicant (Brown Bear)
    let filterString = 'none';
    
    if (role === 'organiser') {
      // Rotate hue to make the bear a cool deep purple/blue hue and increase saturation
      filterString = 'hue-rotate(240deg) saturate(1.3) brightness(0.95)';
    } else if (role === 'admin') {
      // Crimson Red for Admin (powerful, clean, high-saturation)
      filterString = 'hue-rotate(330deg) saturate(1.8) brightness(0.95)';
    }
    
    this.canvas.style.filter = filterString;
  }
}

window.initRiveLogin = function(containerId, ctxPath) {
  return new RiveLoginAnim(containerId, ctxPath);
}
