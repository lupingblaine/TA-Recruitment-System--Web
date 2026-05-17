/**
 * Interactive Canvas Cat for Auth Forms
 * - Fully rendered in HTML5 Canvas (No SVG, No External Assets)
 * - Smooth lerping physics for eye tracking
 * - Procedural eye-covering animation
 */
class LoginCat {
  constructor(containerId) {
    this.container = document.getElementById(containerId);
    if (!this.container) return;
    
    // Canvas Setup
    this.canvas = document.createElement('canvas');
    this.ctx = this.canvas.getContext('2d');
    this.container.innerHTML = '';
    this.container.appendChild(this.canvas);
    
    // Setup high DPI canvas
    this.size = 300;
    this.dpr = window.devicePixelRatio || 1;
    this.canvas.width = this.size * this.dpr;
    this.canvas.height = this.size * this.dpr;
    this.canvas.style.width = `${this.size}px`;
    this.canvas.style.height = `${this.size}px`;
    this.ctx.scale(this.dpr, this.dpr);
    
    // Animation States
    this.targetPupilX = 0;
    this.currentPupilX = 0;
    this.targetPupilY = 0;
    this.currentPupilY = 0;
    
    this.pawY = 160;        // Resting Y position of paws
    this.targetPawY = 160;  // Target Y position
    
    this.eyeScaleY = 1.0;
    this.targetEyeScaleY = 1.0;
    
    this.isCoveringEyes = false;
    
    // Colors
    this.colorCat = '#18181b'; // zinc-950
    this.colorEye = '#ffffff';
    this.colorNose = '#f472b6'; // pink
    this.colorMuzzle = '#ffffff';
    
    this.bindEvents();
    this.animate();
  }
  
  bindEvents() {
    const trackInputs = document.querySelectorAll('input[type="text"], input[type="email"]');
    const passwordInputs = document.querySelectorAll('input[type="password"]');
    
    trackInputs.forEach(input => {
      input.addEventListener('focus', () => this.track(input));
      input.addEventListener('input', () => this.track(input));
      input.addEventListener('blur', () => this.reset());
    });
    
    passwordInputs.forEach(input => {
      input.addEventListener('focus', () => {
        this.isCoveringEyes = true;
        this.targetPawY = 70; // Move paws up
        this.targetEyeScaleY = 0.1; // Squint eyes
        this.targetPupilX = 0;
        this.targetPupilY = 0;
      });
      input.addEventListener('blur', () => {
        this.isCoveringEyes = false;
        this.targetPawY = 160; // Paws down
        this.targetEyeScaleY = 1.0; // Open eyes
      });
    });
  }
  
  track(input) {
    if (this.isCoveringEyes) return;
    const len = Math.min(input.value.length, 25);
    const maxOffset = 8;
    this.targetPupilX = (len / 25) * (maxOffset * 2) - maxOffset;
    this.targetPupilY = 2; // Look slightly down
  }
  
  reset() {
    if (this.isCoveringEyes) return;
    this.targetPupilX = 0;
    this.targetPupilY = 0;
  }
  
  lerp(start, end, amt) {
    return (1 - amt) * start + amt * end;
  }
  
  animate = () => {
    // Physics Updates
    this.currentPupilX = this.lerp(this.currentPupilX, this.targetPupilX, 0.15);
    this.currentPupilY = this.lerp(this.currentPupilY, this.targetPupilY, 0.15);
    this.pawY = this.lerp(this.pawY, this.targetPawY, 0.12);
    this.eyeScaleY = this.lerp(this.eyeScaleY, this.targetEyeScaleY, 0.2);
    
    // Draw
    this.draw();
    
    requestAnimationFrame(this.animate);
  }
  
  draw() {
    const ctx = this.ctx;
    ctx.clearRect(0, 0, this.size, this.size);
    
    // Center everything
    ctx.save();
    ctx.translate(this.size / 2, this.size / 2 - 10);
    
    // 1. Ears
    ctx.fillStyle = this.colorCat;
    ctx.beginPath();
    ctx.moveTo(-60, -30);
    ctx.lineTo(-45, -80);
    ctx.lineTo(-20, -50);
    ctx.fill();
    
    ctx.beginPath();
    ctx.moveTo(60, -30);
    ctx.lineTo(45, -80);
    ctx.lineTo(20, -50);
    ctx.fill();
    
    // 2. Head
    ctx.beginPath();
    ctx.arc(0, 0, 70, 0, Math.PI * 2);
    ctx.fill();
    
    // 3. Eyes
    // Left Eye
    ctx.save();
    ctx.translate(-35, -15);
    ctx.scale(1, this.eyeScaleY);
    ctx.fillStyle = this.colorEye;
    ctx.beginPath();
    ctx.arc(0, 0, 16, 0, Math.PI * 2);
    ctx.fill();
    if (this.eyeScaleY > 0.5) { // Only draw pupil if eye is somewhat open
      ctx.fillStyle = this.colorCat;
      ctx.beginPath();
      ctx.arc(this.currentPupilX, this.currentPupilY, 7, 0, Math.PI * 2);
      ctx.fill();
    }
    ctx.restore();
    
    // Right Eye
    ctx.save();
    ctx.translate(35, -15);
    ctx.scale(1, this.eyeScaleY);
    ctx.fillStyle = this.colorEye;
    ctx.beginPath();
    ctx.arc(0, 0, 16, 0, Math.PI * 2);
    ctx.fill();
    if (this.eyeScaleY > 0.5) {
      ctx.fillStyle = this.colorCat;
      ctx.beginPath();
      ctx.arc(this.currentPupilX, this.currentPupilY, 7, 0, Math.PI * 2);
      ctx.fill();
    }
    ctx.restore();
    
    // 4. Muzzle
    ctx.fillStyle = this.colorMuzzle;
    ctx.beginPath();
    ctx.ellipse(0, 20, 24, 16, 0, 0, Math.PI * 2);
    ctx.fill();
    
    // Nose
    ctx.fillStyle = this.colorNose;
    ctx.beginPath();
    ctx.moveTo(-6, 14);
    ctx.lineTo(6, 14);
    ctx.lineTo(0, 20);
    ctx.fill();
    
    // Mouth
    ctx.strokeStyle = this.colorCat;
    ctx.lineWidth = 2.5;
    ctx.lineCap = 'round';
    ctx.beginPath();
    ctx.moveTo(-10, 24);
    ctx.quadraticCurveTo(0, 32, 0, 20);
    ctx.quadraticCurveTo(0, 32, 10, 24);
    ctx.stroke();
    
    // 5. Paws
    // Map pawY to a rotation angle as they come up
    // When pawY is 160, angle is 0. When pawY is 70, angle is ~ 25 degrees
    const pawProgress = (160 - this.pawY) / (160 - 70); // 0 to 1
    const pawRotation = pawProgress * (Math.PI / 6); // Up to 30 degrees
    
    // Left Paw
    ctx.save();
    ctx.translate(-50, this.pawY - 100); // adjust offset based on center
    ctx.rotate(pawRotation);
    ctx.fillStyle = this.colorCat;
    ctx.beginPath();
    ctx.ellipse(0, 0, 18, 28, 0, 0, Math.PI * 2);
    ctx.fill();
    ctx.restore();
    
    // Right Paw
    ctx.save();
    ctx.translate(50, this.pawY - 100);
    ctx.rotate(-pawRotation);
    ctx.fillStyle = this.colorCat;
    ctx.beginPath();
    ctx.ellipse(0, 0, 18, 28, 0, 0, Math.PI * 2);
    ctx.fill();
    ctx.restore();
    
    ctx.restore();
  }
}

window.initLoginCat = function(containerId) {
  new LoginCat(containerId);
}
