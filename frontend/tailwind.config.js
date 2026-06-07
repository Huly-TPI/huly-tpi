/** @type {import('tailwindcss').Config} */
export default {
  darkMode: 'class',
  content: [
    "./index.html",
    "./src/**/*.{js,ts,jsx,tsx}",
  ],
  theme: {
    extend: {
      fontFamily: {
        sans: ['Nunito', 'ui-sans-serif', 'system-ui', 'sans-serif'],
      },
      colors: {
        fondo: '#E9F1EA',
        menta: '#ACCCA4',
        bosque: '#4C7C64',
        violeta: '#8869AC',
        'violeta-claro': '#D3CCEB',
        anaranjado: '#9C5312',
      },
    },
  },
  plugins: [],
}
