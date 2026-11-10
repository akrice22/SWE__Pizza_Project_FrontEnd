// src/HomeMenu.jsx
// src/HomeMenu.jsx
import { useState, useEffect } from 'react';
import { getMenu } from './api/api';
import { SIZES, CRUSTS, VEGGIE_OPTIONS, MEAT_OPTIONS, DRINK_OPTIONS } from './menuData';

export default function HomeMenu({ onOrderNow }) {
  // Carousel active index state
  const [activeCardIndex, setActiveCardIndex] = useState(0);

  // live items from Supabase
  const [dbMenuItems, setDbMenuItems] = useState([]);

  // fetching live menu items on mount
  useEffect(() => {
    async function loadMenu() {
        try {
            const data = await getMenu();
            setDbMenuItems(
                data.map(item => ({
                    ...item,
                    category: item.category.toLowerCase()
                }))
            );
            console.log("Menu loaded from Spring Boot:", data);
        } catch (error) {
            console.error("Failed to load menu:", error);
        }
    }
    loadMenu();
}, []);

  // fallback logic --> use DB items if present, otherwise menuData.js
  const liveSizes = dbMenuItems.filter((i) => i.category === 'size').length > 0
    ? dbMenuItems.filter((i) => i.category === 'size').map((s) => ({
      label: s.name,
      description: s.description,
      price: Number(s.price),
    }))
    : SIZES;

  const liveCrusts = dbMenuItems.filter((i) => i.category === 'crust').length > 0
    ? dbMenuItems.filter((i) => i.category === 'crust').map((c) => ({
      label: c.name,
      tag: c.description || 'Signature',
    }))
    : CRUSTS;

  const liveVeggies = dbMenuItems.filter((i) => i.category === 'veggie' || i.category === 'vegetable').length > 0
    ? dbMenuItems.filter((i) => i.category === 'veggie' || i.category === 'vegetable')
    : VEGGIE_OPTIONS;

  const liveMeats = dbMenuItems.filter((i) => i.category === 'meat').length > 0
    ? dbMenuItems.filter((i) => i.category === 'meat')
    : MEAT_OPTIONS;
  // ADDED DRINK OPTIONS
  const liveDrinks = dbMenuItems.filter((i) => i.category === 'drink' || i.category === 'drinks').length > 0
    ? dbMenuItems.filter((i) => i.category === 'drink' || i.category === 'drinks').map((d) => ({
        id: d.id || d.name,
        name: d.name,
        price: Number(d.price),
        detail: d.description || 'Chilled & Refreshing',
      }))
    : DRINK_OPTIONS;

  // Grouped Menu Sections for the Carousel Cards
  const menuSections = [
    {
      id: 'sizes',
      tag: '01',
      title: 'Sizes & Base',
      subtitle: 'Hand-stretched to order',
      priceHeader: 'Priced by diameter',
      icon: '🍕',
      items: liveSizes.map((s) => ({
        name: s.label,
        detail: s.description,
        price: `$${Number(s.price).toFixed(2)}`,
      })),
    },
    {
      id: 'crusts',
      tag: '02',
      title: 'Artisan Crusts',
      subtitle: '48-hr fermented sourdough dough',
      priceHeader: 'Included with base',
      icon: '🌾',
      items: liveCrusts.map((c) => ({
        name: c.label,
        detail: c.tag,
        price: 'Included',
      })),
    },
    {
      id: 'veggies',
      tag: '03',
      title: 'Garden Veggies',
      subtitle: 'Locally grown, sliced fresh daily',
      priceHeader: '+$1.25 each topping',
      icon: '🌿',
      items: liveVeggies.map((v) => ({
        name: v.name,
        detail: 'Crisp & Farm Fresh',
      })),
    },
    {
      id: 'meats',
      tag: '04',
      title: 'Savory Meats',
      subtitle: 'House-cured & roasted butcher cuts',
      priceHeader: '+$1.75 each topping',
      icon: '🥩',
      items: liveMeats.map((m) => ({
        name: m.name,
        detail: 'Hickory-Smoked & Seasoned',
      })),
    },
    {
      id: 'drinks',
      tag: '05',
      title: 'Craft Drinks',
      subtitle: 'Cold-bottled sodas & sparkling infusions',
      priceHeader: 'Ice-cold refreshments',
      icon: '🥤',
      items: liveDrinks.map((d) => ({
        name: d.name,
        detail: d.detail || 'Ice-Cold',
        price: `$${Number(d.price).toFixed(2)}`,
      })),
    },
  ];

  const handlePrev = () => {
    setActiveCardIndex((prev) => (prev === 0 ? menuSections.length - 1 : prev - 1));
  };

  const handleNext = () => {
    setActiveCardIndex((prev) => (prev === menuSections.length - 1 ? 0 : prev + 1));
  };

  return (
    <div className="space-y-16 animate-in fade-in duration-500">
      
      {/* ================= HERO SECTION ================= */}
      <section className="relative rounded-3xl overflow-hidden min-h-[460px] sm:min-h-[500px] flex items-center justify-center border border-stone-800/80 shadow-2xl bg-stone-900">
        
        {/* Background Image with Warm Soft Vignette */}
        <div className="absolute inset-0 z-0">
          <img
            src="/two-kind-pizza-table.jpg"
            alt="Handcrafted Pizzas on Table"
            className="w-full h-full object-cover object-center opacity-45 scale-105 transition-transform duration-1000 ease-out hover:scale-100"
          />
          {/* Subtle warm espresso gradients */}
          <div className="absolute inset-0 bg-gradient-to-t from-stone-900 via-stone-900/60 to-transparent" />
          <div className="absolute inset-0 bg-gradient-to-r from-stone-900/80 via-transparent to-stone-900/80" />
        </div>

        {/* Hero Content */}
        <div className="relative z-10 text-center px-6 py-12 max-w-2xl mx-auto space-y-6">
          <div className="inline-block tracking-[0.3em] uppercase text-amber-200/90 text-xs font-semibold">
            Stone Oven Hearth & Kitchen
          </div>
          
          <h2 className="text-4xl sm:text-6xl font-serif tracking-tight text-stone-100 uppercase leading-none drop-shadow-md">
            Stone-Fired <br />
            <span className="italic font-normal lowercase tracking-normal text-amber-200/90">with</span> Soul
          </h2>

          <p className="text-stone-300 text-sm sm:text-base max-w-md mx-auto leading-relaxed font-light">
            Slow-fermented dough, crushed Italian San Marzano tomatoes, and whole milk mozzarella baked over hickory embers.
          </p>

          <div className="pt-2">
            <button
              type="button"
              onClick={onOrderNow}
              className="inline-flex items-center gap-3 px-8 py-3.5 rounded-full bg-amber-200/10 hover:bg-amber-200/20 text-amber-100 border border-amber-200/30 hover:border-amber-200/70 text-sm font-semibold tracking-wider uppercase transition-all duration-300 hover:scale-105 active:scale-95 cursor-pointer backdrop-blur-xs"
            >
              <span>Start Custom Order</span>
              <svg className="w-4 h-4" fill="none" stroke="currentColor" viewBox="0 0 24 24">
                <path strokeLinecap="round" strokeLinejoin="round" strokeWidth="2" d="M13.5 4.5L21 12m0 0l-7.5 7.5M21 12H3" />
              </svg>
            </button>
          </div>
        </div>
      </section>

      {/* ================= ABOUT SECTION ================= */}
      <section id="about-section" className="grid grid-cols-1 md:grid-cols-3 gap-8 py-6 border-y border-stone-800/80 text-stone-300">
        <div className="space-y-2">
          <h4 className="text-xs font-bold uppercase tracking-widest text-amber-200/90">01. 48-Hour Cold Ferment</h4>
          <p className="text-xs text-stone-400 leading-relaxed font-light">
            Our naturally leavened dough rests cold for two full days to build complex sourdough notes and airy, blistered crusts.
          </p>
        </div>
        <div className="space-y-2">
          <h4 className="text-xs font-bold uppercase tracking-widest text-amber-200/90">02. Hand-Stretched & Fired</h4>
          <p className="text-xs text-stone-400 leading-relaxed font-light">
            Each pie is shaped by hand on marble slabs, topped with local produce, and baked at 800°F on stone hearths.
          </p>
        </div>
        <div className="space-y-2">
          <h4 className="text-xs font-bold uppercase tracking-widest text-amber-200/90">03. Fast Local Delivery</h4>
          <p className="text-xs text-stone-400 leading-relaxed font-light">
            From oven to box to your doorstep within minutes so you get that signature crackle on every bite.
          </p>
        </div>
      </section>

      {/* ================= INTERACTIVE EXPAND-ON-HOVER CAROUSEL MENU ================= */}
      <section id="menu-section" className="space-y-6">
        
        {/* Carousel Header & Controls */}
        <div className="flex flex-col sm:flex-row items-center justify-between gap-4">
          <div>
            <span className="text-xs font-bold uppercase tracking-widest text-stone-400">Provisions & Craft</span>
            <h3 className="text-3xl font-serif uppercase tracking-wider text-stone-100">Interactive Menu</h3>
          </div>

          {/* Left / Right Interactive Navigation Arrows */}
          <div className="flex items-center gap-3">
            <button
              type="button"
              onClick={handlePrev}
              aria-label="Previous menu category"
              className="p-3 rounded-full bg-stone-850 hover:bg-stone-800 border border-stone-700/80 text-stone-300 hover:text-amber-200 hover:scale-110 active:scale-95 transition-all cursor-pointer shadow-md"
            >
              <svg className="w-4 h-4" fill="none" stroke="currentColor" viewBox="0 0 24 24">
                <path strokeLinecap="round" strokeLinejoin="round" strokeWidth="2.5" d="M15 19l-7-7 7-7" />
              </svg>
            </button>

            <span className="text-xs font-mono text-stone-400 tracking-wider">
              0{activeCardIndex + 1} / 0{menuSections.length}
            </span>

            <button
              type="button"
              onClick={handleNext}
              aria-label="Next menu category"
              className="p-3 rounded-full bg-stone-850 hover:bg-stone-800 border border-stone-700/80 text-stone-300 hover:text-amber-200 hover:scale-110 active:scale-95 transition-all cursor-pointer shadow-md"
            >
              <svg className="w-4 h-4" fill="none" stroke="currentColor" viewBox="0 0 24 24">
                <path strokeLinecap="round" strokeLinejoin="round" strokeWidth="2.5" d="M9 5l7 7-7 7" />
              </svg>
            </button>
          </div>
        </div>

        {/* EXPAND-ON-HOVER CARDS CONTAINER */}
        <div className="flex flex-col lg:flex-row gap-4 h-auto lg:h-[460px] w-full select-none">
          {menuSections.map((section, idx) => {
            const isActive = activeCardIndex === idx;

            return (
              <div
                key={section.id}
                onMouseEnter={() => setActiveCardIndex(idx)}
                onClick={() => setActiveCardIndex(idx)}
                className={`group relative rounded-3xl border transition-all duration-500 ease-[cubic-bezier(0.25,1,0.5,1)] overflow-hidden cursor-pointer flex flex-col justify-between p-6 min-w-0 ${
                  isActive
                    ? 'lg:flex-[3] bg-stone-850 border-amber-200/40 shadow-2xl shadow-amber-950/20'
                    : 'lg:flex-[1] bg-stone-900/90 border-stone-800/80 hover:border-stone-700 hover:bg-stone-850/60'
                }`}
              >
                {/* Top of Card: Tag & Category Icon */}
                <div className="flex justify-between items-center">
                  <span className="font-mono text-xs text-amber-200/80 tracking-widest">
                    [{section.tag}]
                  </span>
                  <span className="text-2xl transition-transform duration-300 group-hover:scale-125">
                    {section.icon}
                  </span>
                </div>

                {/* Middle: Expanded Content vs Collapsed Header */}
                <div className="my-4 space-y-3 flex-1 flex flex-col justify-center">
                  <div>
                    <h4 className="font-serif uppercase tracking-wider text-stone-100 text-lg sm:text-xl font-bold">
                      {section.title}
                    </h4>
                    <p className="text-xs text-stone-400 font-light mt-0.5">
                      {section.subtitle}
                    </p>
                  </div>

                  {/* Pricing Badge (Single flat price at the top) */}
                  <div className="inline-block">
                    <span className="text-[11px] font-mono font-bold tracking-wider px-2.5 py-1 rounded-full bg-stone-800 text-amber-200 border border-stone-700/60">
                      {section.priceHeader}
                    </span>
                  </div>

                  {/* Expanded Item List (Smooth Fade in when expanded) */}
                  <div
                     className={`transition-all duration-500 ease-in-out overflow-hidden ${
                       isActive
                         ? 'max-h-80 opacity-100 mt-3 pt-1 pr-1 overflow-y-auto'
                         : 'max-h-0 opacity-0 mt-0 pt-0 pointer-events-none'
                      }`}
                  >
                   <div className="space-y-2">
                     {section.items.map((item, i) => (
                      <div
                        key={i}
                        className="flex justify-between items-center text-xs py-1.5 px-3 rounded-xl bg-stone-900/70 border border-stone-800/60"
                      >
                       <div className="truncate pr-2">
                         <span className="font-semibold text-stone-200 block truncate">{item.name}</span>
                         {item.detail && (
                          <span className="block text-[10px] text-stone-500 font-light truncate">
                           {item.detail}
                          </span>
                        )}
                     </div>
                     {item.price && (
                       <span className="font-mono text-amber-200/90 font-bold shrink-0">
                         {item.price}
                       </span>
                      )}
                    </div>
                   ))}
                 </div>
              </div>
                  
                </div>

                {/* Bottom of Card: Action Hint */}
                <div className="pt-2 border-t border-stone-800/80 flex items-center justify-between text-[11px]">
                  <span className="text-stone-400 font-mono">
                    {isActive ? 'Active Section' : 'Hover to Expand'}
                  </span>
                  <span className={`text-amber-200 transition-transform duration-300 ${isActive ? 'translate-x-1' : ''}`}>
                    →
                  </span>
                </div>
              </div>
            );
          })}
        </div>

        {/* Bottom Callout Banner */}
        <div className="text-center p-8 bg-gradient-to-br from-stone-900 to-stone-850 border border-stone-800 rounded-3xl space-y-3 shadow-lg">
          <h4 className="text-2xl font-serif uppercase tracking-wider text-stone-100">Ready to build your pizza?</h4>
          <p className="text-stone-400 text-xs sm:text-sm max-w-md mx-auto font-light">
            Hit the button below to confirm your delivery address and start selecting your custom toppings.
          </p>
          <div className="pt-2">
            <button
              type="button"
              onClick={onOrderNow}
              className="px-8 py-3.5 bg-amber-200 hover:bg-amber-100 text-stone-950 font-bold rounded-xl text-xs uppercase tracking-wider transition-all duration-300 hover:scale-105 active:scale-95 cursor-pointer shadow-lg shadow-amber-200/10"
            >
              Start Your Custom Order
            </button>
          </div>
        </div>

      </section>
    </div>
  );
}