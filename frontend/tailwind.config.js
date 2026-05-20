/** @type {import('tailwindcss').Config} */
export default {
  content: [
    "./index.html",
    "./src/**/*.{js,ts,jsx,tsx}",
  ],
  theme: {
    extend: {
      colors: {
        bosque: '#649959',
        menta: '#ABCBA7',
        violeta: '#8869AC',
        'violeta-claro': '#D1CAEF',
      },
    },
  },
  plugins: [],
}