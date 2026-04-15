<template>
  <section class="finance-panel">
    <div class="hero-card">
      <div>
        <p class="eyebrow">{{ eyebrow }}</p>
        <h1>{{ title }}</h1>
        <p class="description">{{ description }}</p>
      </div>
      <div class="hero-metrics">
        <div class="metric-pill">
          <span class="metric-label">Route</span>
          <strong>{{ routeName }}</strong>
        </div>
        <div class="metric-pill">
          <span class="metric-label">Mode</span>
          <strong>Shell Ready</strong>
        </div>
      </div>
    </div>

    <div class="content-grid">
      <article class="panel-card">
        <header>
          <span>Shared foundation</span>
          <h2>What is already wired</h2>
        </header>
        <ul>
          <li v-for="highlight in highlights" :key="highlight">{{ highlight }}</li>
        </ul>
      </article>

      <article class="panel-card accent-card">
        <header>
          <span>Downstream handoff</span>
          <h2>Next implementation layer</h2>
        </header>
        <p>{{ handoff }}</p>
      </article>
    </div>
  </section>
</template>

<script setup>
defineProps({
  eyebrow: {
    type: String,
    default: 'Finance'
  },
  title: {
    type: String,
    default: 'Finance Surface'
  },
  description: {
    type: String,
    default: ''
  },
  routeName: {
    type: String,
    default: ''
  },
  handoff: {
    type: String,
    default: ''
  },
  highlights: {
    type: Array,
    default: () => []
  }
})
</script>

<style scoped>
.finance-panel {
  display: flex;
  flex-direction: column;
  gap: 20px;
}

.hero-card,
.panel-card {
  background: rgba(255, 255, 255, 0.8);
  border: 1px solid rgba(148, 163, 184, 0.24);
  border-radius: 24px;
  box-shadow: 0 20px 40px rgba(15, 23, 42, 0.08);
}

.hero-card {
  display: flex;
  justify-content: space-between;
  gap: 24px;
  padding: 28px;
  background-image: linear-gradient(135deg, rgba(12, 74, 110, 0.08), rgba(217, 249, 157, 0.22));
}

.eyebrow,
.panel-card span,
.metric-label {
  margin: 0 0 8px;
  font-size: 12px;
  letter-spacing: 0.14em;
  text-transform: uppercase;
  color: #0f766e;
}

h1,
h2 {
  margin: 0;
  color: #0f172a;
}

h1 {
  font-size: clamp(28px, 3vw, 40px);
}

h2 {
  font-size: 22px;
}

.description,
.panel-card p,
li {
  color: #475569;
  line-height: 1.6;
}

.description {
  max-width: 60ch;
}

.hero-metrics {
  display: grid;
  gap: 12px;
  min-width: 180px;
}

.metric-pill {
  display: flex;
  flex-direction: column;
  gap: 4px;
  padding: 16px;
  border-radius: 18px;
  background: rgba(255, 255, 255, 0.72);
}

.content-grid {
  display: grid;
  grid-template-columns: repeat(2, minmax(0, 1fr));
  gap: 20px;
}

.panel-card {
  padding: 24px;
}

.panel-card ul {
  margin: 18px 0 0;
  padding-left: 18px;
}

.accent-card {
  background-image: linear-gradient(160deg, rgba(255, 247, 237, 0.96), rgba(254, 249, 195, 0.88));
}

@media (max-width: 900px) {
  .hero-card,
  .content-grid {
    grid-template-columns: 1fr;
    display: grid;
  }

  .hero-metrics {
    min-width: 0;
  }
}
</style>
