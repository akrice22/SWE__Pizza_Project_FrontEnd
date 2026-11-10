// src/menuData.js
export const SIZES = [
  { id: 'small', label: 'Small (10")', price: 10.99, description: 'Personal 6-slice pie' },
  { id: 'medium', label: 'Medium (12")', price: 13.99, description: 'Great for 2 people (8 slices)' },
  { id: 'large', label: 'Large (14")', price: 16.99, description: 'Family favorite (8 large slices)' },
  { id: 'xlarge', label: 'X-Large (16")', price: 19.99, description: 'Party size (12 slices)' },
];

export const CRUSTS = [
  { id: 'hand-tossed', label: 'Classic Hand-Tossed', tag: 'Traditional' },
  { id: 'thin-crispy', label: 'Thin & Crispy', tag: 'Extra Crunch' },
  { id: 'stone-thick', label: 'Stone-Baked Thick', tag: 'Chewy & Airy' },
  { id: 'garlic-parm', label: 'Garlic Butter Herb', tag: 'House Special' },
];

export const SAUCE_LEVELS = [
  { id: 'light', label: 'Light Sauce' },
  { id: 'normal', label: 'Normal Sauce' },
  { id: 'extra', label: 'Extra Sauce' },
];

export const VEGGIE_OPTIONS = [
  { id: 'mushrooms', name: 'Fresh Mushrooms', price: 1.25 },
  { id: 'red-onions', name: 'Caramelized Red Onion', price: 1.00 },
  { id: 'bell-peppers', name: 'Green Bell Peppers', price: 1.00 },
  { id: 'black-olives', name: 'Kalamata Black Olives', price: 1.25 },
  { id: 'spinach', name: 'Baby Spinach', price: 1.25 },
  { id: 'roma-tomatoes', name: 'Sliced Roma Tomatoes', price: 1.25 },
];

export const MEAT_OPTIONS = [
  { id: 'pepperoni', name: 'Artisan Pepperoni', price: 1.75 },
  { id: 'italian-sausage', name: 'Spicy Italian Sausage', price: 1.75 },
  { id: 'bacon', name: 'Smoked Crispy Bacon', price: 2.00 },
  { id: 'grilled-chicken', name: 'Herbed Grilled Chicken', price: 2.00 },
  { id: 'prosciutto', name: 'Dry-Cured Prosciutto', price: 2.50 },
  { id: 'meatballs', name: 'Handcrafted Beef Meatballs', price: 2.00 },
];

export const DRINK_OPTIONS = [
  { id: 'mexican-coke', name: 'Mexican Cane Sugar Coca-Cola', price: 3.25, detail: '12 oz Glass Bottle' },
  { id: 'san-pellegrino-lemon', name: 'San Pellegrino Limonata', price: 2.95, detail: 'Sparkling Citrus Can' },
  { id: 'san-pellegrino-blood-orange', name: 'San Pellegrino Aranciata Rossa', price: 2.95, detail: 'Blood Orange Sparkling' },
  { id: 'craft-root-beer', name: 'Hearth-Draft Root Beer', price: 3.00, detail: 'Pure Cane & Vanilla' },
  { id: 'iced-tea', name: 'Unsweetened Black Peach Iced Tea', price: 2.75, detail: 'Cold-brewed daily' },
  { id: 'sparkling-water', name: 'Mountain Spring Sparkling Water', price: 2.50, detail: '500ml Glass Bottle' },
];