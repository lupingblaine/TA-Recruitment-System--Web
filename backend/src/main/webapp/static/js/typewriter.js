/**
 * Simple Text Typewriter Effect (Type -> Delete -> Loop)
 */
class SimpleTypewriter {
  constructor(elementId, words) {
    this.element = document.getElementById(elementId);
    if (!this.element) return;
    
    this.words = words;
    this.wordIndex = 0;
    this.isDeleting = false;
    this.text = '';
    
    // Configurable speeds
    this.typeSpeed = 100;
    this.deleteSpeed = 50;
    this.pauseEnd = 2000;
    this.pauseStart = 500;
    
    this.tick();
  }

  tick() {
    const currentWord = this.words[this.wordIndex % this.words.length];
    
    if (this.isDeleting) {
      // Remove a character
      this.text = currentWord.substring(0, this.text.length - 1);
    } else {
      // Add a character
      this.text = currentWord.substring(0, this.text.length + 1);
    }
    
    this.element.innerHTML = this.text + '<span class="animate-pulse border-r-2 border-indigo-400 ml-1 h-full"></span>';
    
    let typeSpeed = this.isDeleting ? this.deleteSpeed : this.typeSpeed;
    
    if (!this.isDeleting && this.text === currentWord) {
      // Finished typing word, pause before deleting
      typeSpeed = this.pauseEnd;
      this.isDeleting = true;
    } else if (this.isDeleting && this.text === '') {
      // Finished deleting word, move to next and pause before typing
      this.isDeleting = false;
      this.wordIndex++;
      typeSpeed = this.pauseStart;
    }
    
    // Add random variance to typing speed
    const variance = this.isDeleting ? 0 : Math.random() * 50;
    
    setTimeout(() => this.tick(), typeSpeed + variance);
  }
}

window.initSimpleTypewriter = function(elementId, words) {
  new SimpleTypewriter(elementId, words);
};
