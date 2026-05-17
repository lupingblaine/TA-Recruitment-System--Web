/**
 * Interactive Particle Network Background
 * Renders a full canvas particle system with connecting lines and mouse repelling interaction.
 */
class ParticleNetwork {
  constructor(canvasId, options = {}) {
    this.canvas = document.getElementById(canvasId);
    if (!this.canvas) return;
    
    this.theme = options.theme || 'light'; // 'light' means white particles for dark bg, 'dark' means dark particles for light bg
    this.particleColor = this.theme === 'light' ? 'rgba(255, 255, 255, 0.4)' : 'rgba(0, 0, 0, 0.2)';
    this.lineColorRgb = this.theme === 'light' ? '255, 255, 255' : '0, 0, 0';

    this.ctx = this.canvas.getContext('2d');
    this.particles = [];
    this.mouse = { x: null, y: null, radius: 150 };
    this.resize();

    window.addEventListener('resize', () => this.resize());
    window.addEventListener('mousemove', (e) => {
      this.mouse.x = e.x;
      this.mouse.y = e.y;
    });
    window.addEventListener('mouseout', () => {
      this.mouse.x = null;
      this.mouse.y = null;
    });

    this.init();
    this.animate();
  }

  resize() {
    this.canvas.width = window.innerWidth;
    // We only want it to cover the hero section height, or window height
    this.canvas.height = window.innerHeight;
  }

  init() {
    this.particles = [];
    const numberOfParticles = (this.canvas.width * this.canvas.height) / 12000;
    for (let i = 0; i < numberOfParticles; i++) {
      const size = (Math.random() * 2) + 1;
      const x = (Math.random() * ((innerWidth - size * 2) - (size * 2)) + size * 2);
      const y = (Math.random() * ((innerHeight - size * 2) - (size * 2)) + size * 2);
      const directionX = (Math.random() * 0.8) - 0.4;
      const directionY = (Math.random() * 0.8) - 0.4;
      const color = this.particleColor;
      
      this.particles.push(new Particle(x, y, directionX, directionY, size, color, this.ctx, this.canvas, this.mouse));
    }
  }

  animate() {
    requestAnimationFrame(() => this.animate());
    this.ctx.clearRect(0, 0, this.canvas.width, this.canvas.height);

    for (let i = 0; i < this.particles.length; i++) {
      this.particles[i].update();
    }
    this.connect();
  }

  connect() {
    let opacityValue = 1;
    for (let a = 0; a < this.particles.length; a++) {
      for (let b = a; b < this.particles.length; b++) {
        let distance = ((this.particles[a].x - this.particles[b].x) * (this.particles[a].x - this.particles[b].x))
          + ((this.particles[a].y - this.particles[b].y) * (this.particles[a].y - this.particles[b].y));
        
        if (distance < (this.canvas.width/7) * (this.canvas.height/7)) {
          opacityValue = 1 - (distance / 20000);
          if (opacityValue < 0) opacityValue = 0;
          this.ctx.strokeStyle = 'rgba(' + this.lineColorRgb + ', ' + opacityValue * 0.2 + ')';
          this.ctx.lineWidth = 1;
          this.ctx.beginPath();
          this.ctx.moveTo(this.particles[a].x, this.particles[a].y);
          this.ctx.lineTo(this.particles[b].x, this.particles[b].y);
          this.ctx.stroke();
        }
      }
    }
  }
}

class Particle {
  constructor(x, y, directionX, directionY, size, color, ctx, canvas, mouse) {
    this.x = x;
    this.y = y;
    this.directionX = directionX;
    this.directionY = directionY;
    this.size = size;
    this.color = color;
    this.ctx = ctx;
    this.canvas = canvas;
    this.mouse = mouse;
    this.baseX = this.x;
    this.baseY = this.y;
    this.density = (Math.random() * 30) + 1;
  }

  draw() {
    this.ctx.beginPath();
    this.ctx.arc(this.x, this.y, this.size, 0, Math.PI * 2, false);
    this.ctx.fillStyle = this.color;
    this.ctx.fill();
  }

  update() {
    // Collision detection
    if (this.x > this.canvas.width || this.x < 0) {
      this.directionX = -this.directionX;
    }
    if (this.y > this.canvas.height || this.y < 0) {
      this.directionY = -this.directionY;
    }

    // Mouse interactivity
    if (this.mouse.x != null && this.mouse.y != null) {
      let dx = this.mouse.x - this.x;
      let dy = this.mouse.y - this.y;
      let distance = Math.sqrt(dx * dx + dy * dy);
      let forceDirectionX = dx / distance;
      let forceDirectionY = dy / distance;
      let maxDistance = this.mouse.radius;
      let force = (maxDistance - distance) / maxDistance;
      let directionX = forceDirectionX * force * this.density;
      let directionY = forceDirectionY * force * this.density;

      if (distance < this.mouse.radius) {
        this.x -= directionX;
        this.y -= directionY;
      } else {
        if (this.x !== this.baseX) {
          let dx = this.x - this.baseX;
          this.x -= dx / 10;
        }
        if (this.y !== this.baseY) {
          let dy = this.y - this.baseY;
          this.y -= dy / 10;
        }
      }
    } else {
      if (this.x !== this.baseX) {
        let dx = this.x - this.baseX;
        this.x -= dx / 10;
      }
      if (this.y !== this.baseY) {
        let dy = this.y - this.baseY;
        this.y -= dy / 10;
      }
    }

    // Move particle
    this.x += this.directionX;
    this.y += this.directionY;
    this.baseX += this.directionX;
    this.baseY += this.directionY;

    this.draw();
  }
}

// Global init function
window.initParticles = function(canvasId, options = {}) {
  new ParticleNetwork(canvasId, options);
};
