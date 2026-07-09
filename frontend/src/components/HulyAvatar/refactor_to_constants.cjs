const fs = require('fs');

const colorMap = {
  '--huly-skin-base': 'skinBase',
  '--huly-skin-base-2': 'skinBase2',
  '--huly-skin-base-3': 'skinBase3',
  '--huly-skin-shadow-1': 'skinShadow1',
  '--huly-skin-shadow-2': 'skinShadow2',
  '--huly-skin-shadow-3': 'skinShadow3',
  '--huly-skin-shadow-4': 'skinShadow4',
  '--huly-blush': 'blush',
  '--huly-eye-base': 'eyeBase',
  '--huly-eye-shadow': 'eyeShadow',
  '--huly-white': 'white',
  '--huly-gray': 'gray',
  '--huly-mouth-dark': 'mouthDark',
  '--huly-tongue': 'tongue',
  '--huly-tongue-light': 'tongueLight',
  '--huly-shirt-violet-base': 'shirtVioletBase',
  '--huly-shirt-violet-shadow': 'shirtVioletShadow',
  '--huly-shirt-violet-stroke': 'shirtVioletStroke',
  '--huly-overalls-base': 'overallsBase',
  '--huly-overalls-shadow': 'overallsShadow',
  '--huly-pants-green-base': 'pantsGreenBase',
  '--huly-pants-green-shadow-1': 'pantsGreenShadow1',
  '--huly-pants-green-shadow-2': 'pantsGreenShadow2',
  '--huly-pants-green-shadow-3': 'pantsGreenShadow3',
  '--huly-pants-green-light': 'pantsGreenLight',
  '--huly-pants-green-stroke': 'pantsGreenStroke',
  '--huly-shoe-blue-base': 'shoeBlueBase',
  '--huly-shoe-blue-shadow': 'shoeBlueShadow',
  '--huly-shoe-blue-light': 'shoeBlueLight',
  '--huly-shoe-blue-stroke': 'shoeBlueStroke',
  '--huly-shoe-gray-stroke': 'shoeGrayStroke',
  '--huly-shoe-lace-stroke': 'shoeLaceStroke',
  '--huly-black': 'black'
};

const hexValues = {
  skinBase: '#edc89e',
  skinBase2: '#ecc79d',
  skinBase3: '#ecc69a',
  skinShadow1: '#e5af74',
  skinShadow2: '#dfa45c',
  skinShadow3: '#e7b276',
  skinShadow4: '#e5b279',
  blush: '#eab2a2',
  eyeBase: '#363636',
  eyeShadow: '#1d1e1e',
  white: '#fff',
  gray: '#919191',
  mouthDark: '#462727',
  tongue: '#e76767',
  tongueLight: '#e83e8c',
  shirtVioletBase: '#553560',
  shirtVioletShadow: '#412848',
  shirtVioletStroke: '#492c58',
  overallsBase: '#5981b9',
  overallsShadow: '#6991c3',
  pantsGreenBase: '#687139',
  pantsGreenShadow1: '#7a6e41',
  pantsGreenShadow2: '#808c46',
  pantsGreenShadow3: '#6e7c43',
  pantsGreenLight: '#a7af5a',
  pantsGreenStroke: '#8f9b0d',
  shoeBlueBase: '#7185ac',
  shoeBlueShadow: '#4e6b9b',
  shoeBlueLight: '#7787a6',
  shoeBlueStroke: '#4e6984',
  shoeGrayStroke: '#76818f',
  shoeLaceStroke: '#b6afa8',
  black: '#000'
};

// 1. Remove :root from HulyAvatar.css
let cssContent = fs.readFileSync('HulyAvatar.css', 'utf8');
cssContent = cssContent.replace(/:root\s*\{[^}]+\}\s*/g, '');
fs.writeFileSync('HulyAvatar.css', cssContent);

// 2. Add AVATAR_COLORS to HulyAvatar.tsx and replace var()
let tsxContent = fs.readFileSync('HulyAvatar.tsx', 'utf8');

// Build the object string
let objectStr = 'export const AVATAR_COLORS = {\n';
for (const [key, value] of Object.entries(hexValues)) {
  objectStr += `  ${key}: "${value}",\n`;
}
objectStr += '};\n\n';

// Insert AVATAR_COLORS right after imports
tsxContent = tsxContent.replace(/(import [^;]+;\n)+/, (match) => {
  return match + '\n' + objectStr;
});

// Replace 'var(--var-name)' or "var(--var-name)" with {AVATAR_COLORS.propName}
// Because attributes might be string literals like fill="var(--huly-skin-base)"
// we need to match the whole attribute value and replace it with JSX expression.

for (const [cssVar, propName] of Object.entries(colorMap)) {
  const regexDoubleQuotes = new RegExp(`"var\\(${cssVar}\\)"`, 'g');
  const regexSingleQuotes = new RegExp(`'var\\(${cssVar}\\)'`, 'g');
  
  tsxContent = tsxContent.replace(regexDoubleQuotes, `{AVATAR_COLORS.${propName}}`);
  tsxContent = tsxContent.replace(regexSingleQuotes, `{AVATAR_COLORS.${propName}}`);
}

fs.writeFileSync('HulyAvatar.tsx', tsxContent);
console.log("Done refactoring to React constants.");
