import { StrictMode } from 'react'
import { createRoot } from 'react-dom/client'
import './styles.css'

function App() {
  return (
    <main className="container">
      <h1>MyPortfolio</h1>
      <p>Investment portfolio management for Brazilian and US markets.</p>
      <section className="card">
        <h2>Getting started</h2>
        <p>Backend and portfolio modules will be connected in the next implementation increments.</p>
      </section>
    </main>
  )
}

createRoot(document.getElementById('root')!).render(
  <StrictMode>
    <App />
  </StrictMode>,
)
