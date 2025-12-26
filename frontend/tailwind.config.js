/** @type {import('tailwindcss').Config} */
export default {
  content: [
    "./index.html",
    "./src/**/*.{js,ts,jsx,tsx}",
  ],
  theme: {
    extend: {
      colors: {
        'stock-green': '#22c55e',
        'stock-red': '#ef4444',
      },
    },
  },
  plugins: [],
}

