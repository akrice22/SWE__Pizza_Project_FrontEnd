// src/App.jsx
// src/App.jsx
import { useState, useEffect, useRef } from 'react';
import { supabase } from './supabaseClient';
import HomeMenu from './HomeMenu';
import { SIZES, CRUSTS, SAUCE_LEVELS, VEGGIE_OPTIONS, MEAT_OPTIONS } from './menuData';
import OrderReceiptModal from './OrderReceiptModal';
import { getCustomerByPhone, createCustomer } from './api/api';


const DEFAULT_PIZZA = {
  size: SIZES[1], // Medium default
  crust: CRUSTS[0],
  sauce: SAUCE_LEVELS[1],
  selectedVeggies: [],
  selectedMeats: [MEAT_OPTIONS[0]],
};

export default function App() {

  // Navigation: 'landing' (Home/Menu showcase) vs 'order' (Customizer flow)
  const [currentView, setCurrentView] = useState('landing');
  const [activeStep, setActiveStep] = useState('details'); // 'details' | 'menu'

  // Customer State
  const [customerInfo, setCustomerInfo] = useState({
    name: '',
    phone: '',
    streetAddress: '',
    subdivision: '',
    nearestIntersection: '',
    orderType: 'delivery',
  });

  // Phone number input Validation
  const [searchPhoneError, setSearchPhoneError] = useState('');
  const [formPhoneError, setFormPhoneError] = useState('');

  // Pizza State
  const [pizzaBuilder, setPizzaBuilder] = useState(DEFAULT_PIZZA);
  const [editingId, setEditingId] = useState(null);
  const [activeDropdown, setActiveDropdown] = useState(null);
  const [lastAddedPrice, setLastAddedPrice] = useState(null);

  // Cart State
  const [cart, setCart] = useState([]);
  const [isCartOpen, setIsCartOpen] = useState(false);

  // Returning Customer & Reorder States
  const [searchPhone, setSearchPhone] = useState('');
  const [isSearchingPhone, setIsSearchingPhone] = useState(false);
  const [pastOrders, setPastOrders] = useState([]);
  const [isReturningUser, setIsReturningUser] = useState(false);
  const [lookupMessage, setLookupMessage] = useState(null);

  // Modal & Payment States
  const [isCheckoutModalOpen, setIsCheckoutModalOpen] = useState(false);
  const [selectedPaymentMethod, setSelectedPaymentMethod] = useState('credit');
  const [selectedTipPercent, setSelectedTipPercent] = useState(15);
  const [customTipAmount, setCustomTipAmount] = useState('');

  // Card Details & Saved Payments States
  const [cardForm, setCardForm] = useState({
    nickname: '',
    cardNumber: '',
    cardholderName: '',
    expiry: '',
    cvv: '',
  });
  const [cardFormErrors, setCardFormErrors] = useState({});
  const [savedCards, setSavedCards] = useState([]);
  const [selectedSavedCardId, setSelectedSavedCardId] = useState(null);
  const [isAddingNewCard, setIsAddingNewCard] = useState(false);

  // Signature States & Refs
  const [signatureMode, setSignatureMode] = useState('draw'); // 'draw' | 'type'
  const [typedSignature, setTypedSignature] = useState('');
  const [signatureAgreed, setSignatureAgreed] = useState(false);
  const [hasDrawnSignature, setHasDrawnSignature] = useState(false);
  const [signatureError, setSignatureError] = useState('');
  const canvasRef = useRef(null);
  const isDrawingRef = useRef(false);

  // Receipt Modal States
  const [completedOrderReceipt, setCompletedOrderReceipt] = useState(null);
  const [isReceiptModalOpen, setIsReceiptModalOpen] = useState(false);

 
  // Input change handler
  const handleCustomerChange = (e) => {
    const { name, value } = e.target;
    setCustomerInfo((prev) => ({ ...prev, [name]: value }));
  };

  // new customer ID creation
  const [customerId, setCustomerId] = useState(null);

  // Formats the phone number entered into 555-555-5555 as the user types
  const formatPhoneNumber = (value) => {
  if (!value) return '';
  const digits = value.replace(/\D/g, '').slice(0, 10);

  if (digits.length <= 3) {
    return digits;
  }
  if (digits.length <= 6) {
    return `${digits.slice(0, 3)}-${digits.slice(3)}`;
  }
  return `${digits.slice(0, 3)}-${digits.slice(3, 6)}-${digits.slice(6)}`;
 
  };

  // Takes the dashes out and verifies exactly 10 digits 
  const isValidTenDigitPhone = (formattedValue) => {
  const cleanDigits = (formattedValue || '').replace(/\D/g, '');
  return cleanDigits.length === 10;
  };

  // Updates Quick Lookup input
  const handleSearchPhoneChange = (e) => {
  const formatted = formatPhoneNumber(e.target.value);
  setSearchPhone(formatted);
  if (searchPhoneError) setSearchPhoneError('');
  };

  // Updates customer number form field as they type
  const handleFormPhoneChange = (e) => {
  const formatted = formatPhoneNumber(e.target.value);
  setCustomerInfo((prev) => ({ ...prev, phone: formatted }));
  if (formPhoneError) setFormPhoneError('');
  };

  // search for existing customer my phone number
  const handlePhoneLookup = async (e) => {
  e.preventDefault();
  const cleanPhone = (searchPhone || '').replace(/\D/g, '');

  if (cleanPhone.length !== 10) {
    setSearchPhoneError('Please enter a valid 10-digit phone number.');
    alert('Please enter a valid phone number (10 digit with no letters).');
    return;
  }

  setIsSearchingPhone(true);
  setLookupMessage(null);
  setSearchPhoneError('');

  try {
    // Look up customer through Spring Boot
    const foundCustomer = await getCustomerByPhone(cleanPhone);

    if (foundCustomer) {
        setCustomerId(foundCustomer.id);
        setIsReturningUser(true);
        setSearchPhoneError('');

        // Pre-fill form fields
        setCustomerInfo((prev) => ({
            ...prev,
            name: `${foundCustomer.firstName || ''} ${foundCustomer.lastName || ''}`.trim(),
            phone: formatPhoneNumber(foundCustomer.phoneNumber || ''),
            streetAddress: foundCustomer.address || '',
            subdivision: foundCustomer.subdivision || '',
            nearestIntersection: foundCustomer.majorIntersection || '',
        }));

        console.log("Customer found through Spring Boot:", foundCustomer);

        // Order history will be connected to Spring Boot later
        setPastOrders([]);

        // Saved payment methods will be connected later
        setSavedCards([]);
        setSelectedSavedCardId(null);


      setLookupMessage({
        type: 'success',
        text: `Welcome back, ${foundCustomer.first_name || 'friend'}! We loaded your details.`,
      });
    } else {
      setIsReturningUser(false);
      setPastOrders([]);
      setSavedCards([]);
      setSelectedSavedCardId(null);
      setSearchPhoneError('No account found with this phone number.');
      // pop-up for testing if the database was working correctly 
      // alert('Phone number not found. Pleaase enter a valid registered phone number or fill in your details below as a new customer.')
      setLookupMessage({
        type: 'info',
        text: 'No prior profile found with that number. Please fill out your details below to get started!',
      });
    }
  } catch (err) {
    console.error('Phone lookup error:', err.message || err);
    setLookupMessage({
      type: 'error',
      text: 'Unable to check records. Please fill in your details below.',
    });
  } finally {
    setIsSearchingPhone(false);
  }
};

// Quick re-order. Add entire past pizza directly into active cart
const handleReorderPizza = (item) => {
  // Map database item back to front-end cart
  const reorderedPizza = {
    id: Date.now(),
    size: { label: item.size },
    crust: { label: item.crust },
    sauce: { label: item.sauce },
    selectedVeggies: (item.toppings || []).map((name) => ({ name })),
    selectedMeats: [],
    price: parseFloat(item.price).toFixed(2),
  };
  setCart((prev) => [...prev, reorderedPizza]);
  setIsCartOpen(true);
};


  // sending customer record to Supabase on submission of form
  const handleCustomerSubmit = async (e) => {
    e.preventDefault();
    const rawDigitsOnly = (customerInfo.phone || '').replace(/\D/g, '');

    if (rawDigitsOnly.length !== 10) {
      setFormPhoneError('Please enter a valid 10-digit phone number.');
      alert('Please enter a valid phone number.');
      return;
    }

    setFormPhoneError('');

    try {
          const nameParts = customerInfo.name.trim().split(' ');
          const firstName = nameParts[0] || '';
          const lastName = nameParts.slice(1).join(' ') || ''
          
          if (!customerId) {
            const newCustomer = await createCustomer({
              firstName: firstName,
              lastName: lastName,
              phoneNumber: rawDigitsOnly,
              address: customerInfo.streetAddress,
              subdivision: customerInfo.subdivision,
              majorIntersection: customerInfo.nearestIntersection,
            });
          
            setCustomerId(newCustomer.id);
          
            console.log("Customer created through Spring Boot:", newCustomer);
          }
          
          setActiveStep('builder');
          
          } catch (err) {
            console.error('Error saving customer:', err.message || err);
            alert('Failed to save customer details.');
          }
          };

  // Toggle Veggie selection
  const toggleVeggie = (veggie) => {
    setPizzaBuilder((prev) => {
      const exists = prev.selectedVeggies.some((item) => item.id === veggie.id);
      return {
        ...prev,
        selectedVeggies: exists
          ? prev.selectedVeggies.filter((item) => item.id !== veggie.id)
          : [...prev.selectedVeggies, veggie],
      };
    });
  };

  // Toggle Meat selection
  const toggleMeat = (meat) => {
    setPizzaBuilder((prev) => {
      const exists = prev.selectedMeats.some((item) => item.id === meat.id);
      return {
        ...prev,
        selectedMeats: exists
          ? prev.selectedMeats.filter((item) => item.id !== meat.id)
          : [...prev.selectedMeats, meat],
      };
    });
  };

  const currentPizzaPrice = (
    (pizzaBuilder.size?.price || 0) +
    pizzaBuilder.selectedVeggies.reduce((sum, v) => sum + (v.price || 0), 0) +
    pizzaBuilder.selectedMeats.reduce((sum, m) => sum + (m.price || 0), 0)
  ).toFixed(2);

  const handleSavePizza = () => {
    setLastAddedPrice(currentPizzaPrice);

    if (editingId) {
      setCart((prev) =>
        prev.map((item) =>
          item.id === editingId
            ? { ...pizzaBuilder, id: editingId, price: currentPizzaPrice }
            : item
        )
      );
      setEditingId(null);
    } else {
      const newPizza = {
        ...pizzaBuilder,
        id: Date.now(),
        price: currentPizzaPrice,
      };
      setCart((prev) => [...prev, newPizza]);
    }
    setPizzaBuilder(DEFAULT_PIZZA);
  };

  const handleDeleteItem = (id) => setCart((prev) => prev.filter((item) => item.id !== id));

  const handleEditItem = (pizza) => {
    setPizzaBuilder(pizza);
    setEditingId(pizza.id);
    setIsCartOpen(false);
    setActiveStep('menu');
  };

  const grandTotal = cart.reduce((sum, item) => sum + parseFloat(item.price), 0).toFixed(2);

   // Financial Calculations
  const subtotalNumber = parseFloat(grandTotal) || 0;
  const deliveryFee = customerInfo.orderType === 'delivery' ? 3.99 : 0.0;
  const tax = parseFloat((subtotalNumber * 0.08).toFixed(2));
  const tipAmount =
    selectedTipPercent === 'custom'
      ? parseFloat(customTipAmount) || 0
      : parseFloat(((subtotalNumber * selectedTipPercent) / 100).toFixed(2));
  const finalGrandTotal = parseFloat((subtotalNumber + deliveryFee + tax + tipAmount).toFixed(2));

  // Signature Drawing 
  const startDrawing = (e) => {
    const canvas = canvasRef.current;
    if (!canvas) return;
    const ctx = canvas.getContext('2d');
    const rect = canvas.getBoundingClientRect();
    const clientX = e.clientX || (e.touches && e.touches[0].clientX);
    const clientY = e.clientY || (e.touches && e.touches[0].clientY);
    const x = clientX - rect.left;
    const y = clientY - rect.top;

    ctx.beginPath();
    ctx.moveTo(x, y);
    isDrawingRef.current = true;
    setHasDrawnSignature(true);
    if (signatureError) setSignatureError('');
  };

  const draw = (e) => {
    if (!isDrawingRef.current) return;
    const canvas = canvasRef.current;
    if (!canvas) return;
    const ctx = canvas.getContext('2d');
    const rect = canvas.getBoundingClientRect();
    const clientX = e.clientX || (e.touches && e.touches[0].clientX);
    const clientY = e.clientY || (e.touches && e.touches[0].clientY);
    const x = clientX - rect.left;
    const y = clientY - rect.top;

    ctx.lineWidth = 2;
    ctx.lineCap = 'round';
    ctx.strokeStyle = '#fef3c7'; // amber/cream color
    ctx.lineTo(x, y);
    ctx.stroke();
  };

  const stopDrawing = () => {
    isDrawingRef.current = false;
  };

  const clearCanvasSignature = () => {
    const canvas = canvasRef.current;
    if (!canvas) return;
    const ctx = canvas.getContext('2d');
    ctx.clearRect(0, 0, canvas.width, canvas.height);
    setHasDrawnSignature(false);
    setSignatureError('');
  };

 // Final Payment and Receipt Submission
  

  // checkout handler
  const [isSubmittingOrder, setIsSubmittingOrder] = useState(false);

  // -------- Card Formatters and Validation --------
  const formatCardNumber = (value) => {
    const raw = value.replace(/\D/g, '').slice(0,16);
    return raw.replace(/(\d{4})(?=\d)/g, '$1 ');
  };

    // Format the Expiriation Date of the Debit/Credit card (MM/YY)
  const formatExpiry = (value) => {
    const raw = value.replace(/\D/g, '').slice(0, 4);
    if (raw.length <= 2) return raw;
    return `${raw.slice(0, 2)}/${raw.slice(2)}`;
  };

    // Check if card information is valid for checkout
  const isCardComplete = () => {
    if (selectedPaymentMethod === 'cash') return true;
    if (selectedSavedCardId) return true; // using an existing saved card

    const cleanNumber = cardForm.cardNumber.replace(/\D/g, '');
    const cleanExpiry = cardForm.expiry.replace(/\D/g, '');
    const cleanCvv = cardForm.cvv.replace(/\D/g, '');

    return (
      cleanNumber.length === 16 &&
      cleanExpiry.length === 4 &&
      cleanCvv.length >= 3 &&
      cardForm.cardholderName.trim().length > 0
    );
  };

    // Save Card Handler
  const handleSaveCard = async (e) => {
    e.preventDefault();
    const cleanNumber = cardForm.cardNumber.replace(/\D/g, '');
    const cleanExpiry = cardForm.expiry.replace(/\D/g, '');
    const cleanCvv = cardForm.cvv.replace(/\D/g, '');

    const errors = {};
    if (cleanNumber.length !== 16) errors.cardNumber = 'Card number must be 16 digits';
    if (!cardForm.cardholderName.trim()) errors.cardholderName = 'Name is required';
    if (cleanExpiry.length !== 4) errors.expiry = 'MM/YY required';
    if (cleanCvv.length < 3) errors.cvv = '3-4 digits required';

    if (Object.keys(errors).length > 0) {
      setCardFormErrors(errors);
      return;
    }

    const cardPayload = {
      customer_id: customerId, // links card to current customer
      nickname: cardForm.nickname.trim() || `Card ending in ${cleanNumber.slice(-4)}`,
      brand: cleanNumber.startsWith('4') ? 'Visa' : cleanNumber.startsWith('5') ? 'Mastercard' : 'Card',
      last_four: cleanNumber.slice(-4), // ONLY store the last 4 digits
      cardholder_name: cardForm.cardholderName.trim(),
      expiry: cardForm.expiry,
    };

    try {
      // Write to Supabase if customerId exists
      if (customerId) {
        const { data, error } = await supabase
          .from('customer_payment_methods')
          .insert([cardPayload])
          .select();

        if (error) throw error;

        if (data && data.length > 0) {
          const inserted = data[0];
          const newCard = {
            id: inserted.id.toString(),
            nickname: inserted.nickname,
            brand: inserted.brand,
            last4: inserted.last_four,
            cardholderName: inserted.cardholder_name,
            expiry: inserted.expiry,
          };
          setSavedCards((prev) => [...prev, newCard]);
          setSelectedSavedCardId(newCard.id);
        }
      } else {
        // Fallback for guest state
        const localCard = {
          id: Date.now().toString(),
          ...cardPayload,
          last4: cardPayload.last_four,
          cardholderName: cardPayload.cardholder_name,
        };
        setSavedCards((prev) => [...prev, localCard]);
        setSelectedSavedCardId(localCard.id);
      }

      setIsAddingNewCard(false);
      setCardFormErrors({});
      // Clear sensitive inputs from form state
      setCardForm({
        nickname: '',
        cardNumber: '',
        cardholderName: '',
        expiry: '',
        cvv: '',
      });
    } catch (err) {
      console.error('Error saving payment card:', err.message || err);
      alert('Could not save card to account.');
    }
  };
    /*

    const newCard = {
      id: Date.now().toString(),
      nickname: cardForm.nickname.trim() || `Card ending in ${cleanNumber.slice(-4)}`,
      brand: cleanNumber.startsWith('4') ? 'Visa' : cleanNumber.startsWith('5') ? 'Mastercard' : 'Card',
      last4: cleanNumber.slice(-4),
      cardholderName: cardForm.cardholderName.trim(),
      expiry: cardForm.expiry,
    };

    setSavedCards((prev) => [...prev, newCard]);
    setSelectedSavedCardId(newCard.id);
    setIsAddingNewCard(false);
    setCardFormErrors({});
    */
  

  // Final Payment and Receipt Submission
  const handleFinalPaymentSubmit = async () => {
    if (cart.length === 0) return;

    // Validate card information completion for credit/debit payments
    if ((selectedPaymentMethod === 'credit' || selectedPaymentMethod === 'debit') && !isCardComplete()) {
      const cleanNumber = cardForm.cardNumber.replace(/\D/g, '');
      const cleanExpiry = cardForm.expiry.replace(/\D/g, '');
      const cleanCvv = cardForm.cvv.replace(/\D/g, '');

      const errors = {};
      if (cleanNumber.length !== 16) errors.cardNumber = 'Card number must be 16 digits.';
      if (!cardForm.cardholderName.trim()) errors.cardholderName = 'Cardholder name is required.';
      if (cleanExpiry.length !== 4) errors.expiry = 'MM/YY required.';
      if (cleanCvv.length < 3) errors.cvv = '3-4 digits required.';
      errors.general = 'Please complete all required card fields above before authorizing payment.';

      setCardFormErrors(errors);
      return;
    }

    // Validate Signature
    if (signatureMode === 'draw' && !hasDrawnSignature) {
      setSignatureError('Please draw your signature before placing the order.');
      return;
    }
    if (signatureMode === 'type' && (!typedSignature.trim() || !signatureAgreed)) {
      setSignatureError('Please type your name and check the consent box to sign.');
      return;
    }

    setSignatureError('');
    setIsSubmittingOrder(true);

    try {
      // 1. Insert order into Supabase
      const { data: orderData, error: orderError } = await supabase
        .from('orders')
        .insert([
          {
            customer_id: customerId,
            order_type: customerInfo.orderType,
            payment_method: selectedPaymentMethod,
            payment_status: selectedPaymentMethod === 'cash' ? 'cash_on_delivery' : 'paid',
            subtotal: subtotalNumber,
            delivery_fee: deliveryFee,
            tax: tax,
            tip: tipAmount,
            total_price: finalGrandTotal,
            status: 'received',
          },
        ])
        .select();

      if (orderError) throw orderError;
      const newOrderId = orderData[0].id;

      // 2. Format order items
      const orderItemsToInsert = cart.map((pizza) => ({
        order_id: newOrderId,
        size: pizza.size.label,
        crust: pizza.crust.label,
        sauce: pizza.sauce.label,
        toppings: [...(pizza.selectedMeats || []), ...(pizza.selectedVeggies || [])].map((t) => t.name),
        price: parseFloat(pizza.price),
      }));

      // 3. Insert items into 'order_items'
      const { error: itemsError } = await supabase
        .from('order_items')
        .insert(orderItemsToInsert);

      if (itemsError) throw itemsError;

      // 4. Capture Signature Data URL if drawn
      let signatureDataUrl = null;
      if (signatureMode === 'draw' && canvasRef.current) {
        signatureDataUrl = canvasRef.current.toDataURL('image/png');
      }

      // 5. Store completed receipt data
      setCompletedOrderReceipt({
        orderId: newOrderId,
        timestamp: new Date().toLocaleString('en-US', {
          dateStyle: 'medium',
          timeStyle: 'short',
        }),
        customer: { ...customerInfo },
        items: [...cart],
        paymentMethod: selectedPaymentMethod,
        subtotal: subtotalNumber,
        deliveryFee,
        tax,
        tip: tipAmount,
        grandTotal: finalGrandTotal,
        signatureMode,
        signatureDataUrl,
        typedSignature,
      });

      // 6. Transition UI
      setCart([]);
      setIsCheckoutModalOpen(false);
      setIsReceiptModalOpen(true);
    } catch (err) {
      console.error('Error placing order:', err.message || err);
      alert('There was an issue saving your order. Check console.');
    } finally {
      setIsSubmittingOrder(false);
    }
  };

  const handleCheckout = async () => {
    if (cart.length === 0) return;
    setIsSubmittingOrder(true);

    // breaking down the total
    const subtotalNumber = parseFloat(grandTotal);
    const deliveryFee = customerInfo.orderType === 'delivery' ? 3.99 : 0.0;
    const tax = parseFloat((subtotalNumber * 0.08).toFixed(2));
    const tip = 0.0; // ** bind to tip buttons later
    const finalTotal = parseFloat((subtotalNumber + deliveryFee + tax + tip).toFixed(2));

    try {
      // putting into 'orders' table
      const { data: orderData, error: orderError } = await supabase
        .from('orders')
        .insert([
          {
            customer_id: customerId,
            order_type: customerInfo.orderType,
            payment_method: 'credit', // default
            payment_status: 'paid',
            subtotal: subtotalNumber,
            delivery_fee: deliveryFee,
            tax: tax,
            tip: tip,
            total_price: finalTotal,
            status: 'received',
          },
        ])
        .select();

        if (orderError) throw orderError;

        const newOrderId = orderData[0].id;
        console.log('Order created with ID:', newOrderId);

        // formatting pizzas in the cart for 'order_items" table
        const orderItemsToInsert = cart.map((pizza) => ({
          order_id: newOrderId,
          size: pizza.size.label,
          crust: pizza.crust.label,
          sauce: pizza.sauce.label,
          // store topping names into the JSNOV column
          toppings: [...pizza.selectedMeats, ...pizza.selectedVeggies].map((t) => t.name),
          price: parseFloat(pizza.price),
        }));

       const { data: orderItemsData, error: orderItemsError } = await supabase
         .from('order_items')
         .insert(orderItemsToInsert)
         .select();

         if (orderItemsError) throw orderItemsError;
         console.log('Order items successfully inserted:', orderItemsData);

         // success UI state updates
         alert(`Order #${newOrderId} successfully placed! Total: $${finalTotal.toFixed(2)}`);
         setCart([]);
         setIsCartOpen(false);
         setCurrentView('landing');

      } catch (err) {
        console.error('Error placing order:', err.message || err);
        alert('There was an issue saving your order. Check console.');
      } finally {
        setIsSubmittingOrder(false);
      }
      
  };

  // Smooth scroll helper for menu/about anchors
  const navigateToSection = (sectionId) => {
    if (currentView !== 'landing') {
      setCurrentView('landing');
      setTimeout(() => {
        document.getElementById(sectionId)?.scrollIntoView({ behavior: 'smooth' });
      }, 100);
    } else {
      document.getElementById(sectionId)?.scrollIntoView({ behavior: 'smooth' });
    }
  };

  return (
    <div className="min-h-screen bg-[#1c1917] text-stone-200 font-sans antialiased selection:bg-amber-200 selection:text-stone-950 pb-16">
      
      {/* ================= FLOATING MODERN NAVBAR ================= */}
      <nav className="sticky top-0 z-40 backdrop-blur-md bg-[#1c1917]/90 border-b border-stone-800/80 transition-all">
        <div className="max-w-6xl mx-auto px-4 sm:px-8 h-20 flex items-center justify-between">
          
          {/* Left: Social Media Icons (Blended floating icons) */}
          <div className="flex items-center gap-5 z-10">
            {/* Instagram */}
            <a
              href="#instagram"
              aria-label="Instagram"
              className="text-stone-300 hover:text-amber-200 transition-all duration-300 hover:scale-125 cursor-pointer"
            >
              <svg className="w-5 h-5 fill-currentColor" viewBox="0 0 24 24">
                <path d="M12 2.163c3.204 0 3.584.012 4.85.07 3.252.148 4.771 1.691 4.919 4.919.058 1.265.069 1.645.069 4.849 0 3.205-.012 3.584-.069 4.849-.149 3.225-1.664 4.771-4.919 4.919-1.266.058-1.644.07-4.85.07-3.204 0-3.584-.012-4.849-.07-3.26-.149-4.771-1.699-4.919-4.92-.058-1.265-.07-1.644-.07-4.849 0-3.204.013-3.583.07-4.849.149-3.227 1.664-4.771 4.919-4.919 1.266-.057 1.645-.069 4.849-.069zm0-2.163c-3.259 0-3.667.014-4.947.072-4.358.2-6.78 2.618-6.98 6.98-.059 1.281-.073 1.689-.073 4.948 0 3.259.014 3.668.072 4.948.2 4.358 2.618 6.78 6.98 6.98 1.281.058 1.689.072 4.948.072 3.259 0 3.668-.014 4.948-.072 4.354-.2 6.782-2.618 6.979-6.98.059-1.28.073-1.689.073-4.948 0-3.259-.014-3.667-.072-4.947-.196-4.354-2.617-6.78-6.979-6.98-1.281-.059-1.69-.073-4.949-.073zm0 5.838c-3.403 0-6.162 2.759-6.162 6.162s2.759 6.163 6.162 6.163 6.162-2.759 6.162-6.163c0-3.403-2.759-6.162-6.162-6.162zm0 10.162c-2.209 0-4-1.79-4-4 0-2.209 1.791-4 4-4s4 1.791 4 4c0 2.21-1.791 4-4 4zm6.406-11.845c-.796 0-1.441.645-1.441 1.44s.645 1.44 1.441 1.44c.795 0 1.439-.645 1.439-1.44s-.644-1.44-1.439-1.44z" />
              </svg>
            </a>

            {/* Facebook */}
            <a
              href="#facebook"
              aria-label="Facebook"
              className="text-stone-300 hover:text-amber-200 transition-all duration-300 hover:scale-125 cursor-pointer"
            >
              <svg className="w-5 h-5 fill-currentColor" viewBox="0 0 24 24">
                <path d="M24 12.073c0-6.627-5.373-12-12-12s-12 5.373-12 12c0 5.99 4.388 10.954 10.125 11.854v-8.385H7.078v-3.47h3.047V9.43c0-3.007 1.792-4.669 4.533-4.669 1.312 0 2.686.235 2.686.235v2.953H15.83c-1.491 0-1.956.925-1.956 1.874v2.25h3.328l-.532 3.47h-2.796v8.385C19.612 23.027 24 18.062 24 12.073z" />
              </svg>
            </a>
          </div>

          {/* Center: Brand Name (Click returns Home) */}
          <div
            onClick={() => setCurrentView('landing')}
            className="text-center cursor-pointer group select-none"
          >
            <span className="block font-serif text-xl sm:text-2xl tracking-[0.25em] text-stone-100 group-hover:text-amber-200 transition-colors uppercase font-bold">
              Mom & Pop
            </span>
            <span className="block text-[9px] uppercase tracking-[0.4em] text-stone-400 font-light">
              Pizzeria & Hearth
            </span>
          </div>

          {/* Right: Floating Navigation Links & Cart */}
          <div className="flex items-center gap-6 sm:gap-8">
            <button
              type="button"
              onClick={() => navigateToSection('about-section')}
              className="hidden sm:inline-block text-xs uppercase tracking-widest text-stone-300 hover:text-amber-200 transition-all duration-300 hover:scale-110 cursor-pointer"
            >
              About
            </button>

            <button
              type="button"
              onClick={() => navigateToSection('menu-section')}
              className="text-xs uppercase tracking-widest text-stone-300 hover:text-amber-200 transition-all duration-300 hover:scale-110 cursor-pointer"
            >
              Menu
            </button>

            <button
              type="button"
              onClick={() => {
                setCurrentView('order');
                setActiveStep('details');
              }}
              className="text-xs uppercase tracking-widest font-bold text-amber-200 hover:text-white transition-all duration-300 hover:scale-110 cursor-pointer"
            >
              Order Now
            </button>

            {/* Cart Trigger Button */}
            <button
              type="button"
              onClick={() => setIsCartOpen(true)}
              className="relative text-stone-300 hover:text-amber-200 transition-all duration-300 hover:scale-110 cursor-pointer flex items-center gap-1.5"
            >
              <svg className="w-5 h-5" fill="none" stroke="currentColor" viewBox="0 0 24 24">
                <path strokeLinecap="round" strokeLinejoin="round" strokeWidth="1.8" d="M16 11V7a4 4 0 00-8 0v4M5 9h14l1 12H4L5 9z" />
              </svg>
              <span className="text-xs font-mono font-bold">({cart.length})</span>
            </button>
          </div>

        </div>
      </nav>

      {/* ================= MAIN CONTENT WRAPPER ================= */}
      <main className="max-w-5xl mx-auto px-4 sm:px-6 pt-6">
        
        {/* VIEW 1: HOME LANDING & MENU DISPLAY */}
        {currentView === 'landing' ? (
          <HomeMenu
            onOrderNow={() => {
              setCurrentView('order');
              setActiveStep('details');
            }}
          />
        ) : (
          /* VIEW 2: ORDER PROCESS (DETAILS -> BUILDER) */
          <div className="space-y-6 animate-in fade-in duration-300">
            
            {/* Sub-nav back to menu showcase */}
            <div className="flex justify-between items-center pb-2">
              <button
                type="button"
                onClick={() => setCurrentView('landing')}
                className="text-xs font-bold uppercase tracking-widest text-stone-400 hover:text-amber-200 transition-colors flex items-center gap-1.5 cursor-pointer"
              >
                ← Return to Overview
              </button>
              <span className="text-xs font-mono text-stone-500">
                Step {activeStep === 'details' ? '1 of 2: Details' : '2 of 2: Pizza Customizer'}
              </span>
            </div>

            {/* Sliding Container */}
            <div className="relative overflow-hidden w-full min-h-[640px]">
              <div
                className={`flex w-[200%] transition-transform duration-700 ease-[cubic-bezier(0.25,1,0.5,1)] ${
                  activeStep === 'details' || activeStep === 'customer' 
                  ? 'translate-x-0'
                   : '-translate-x-1/2'
                }`}
              >
                {/* ================= SECTION 1: CUSTOMER DETAILS & LOOKUP ================= */}
            <div className="w-1/2 pr-4">
              <div className="bg-stone-900/90 border border-stone-800 rounded-3xl p-6 sm:p-10 shadow-2xl backdrop-blur-md space-y-8">
    
                {/* Header */}
                <div className="border-b border-stone-800 pb-4">
                  <span className="text-[11px] font-mono tracking-widest uppercase text-amber-200/80">Step 01</span>
                  <h3 className="text-2xl font-serif uppercase tracking-wider text-stone-100">Delivery & Customer Info</h3>
                  <p className="text-xs text-stone-400 mt-1">Returning customer? Enter your phone number below to retrieve your information and past orders.</p>
               </div>

               {/* Returning Customer Phone Lookup Bar */}
               <form onSubmit={handlePhoneLookup} className="p-4 rounded-2xl bg-stone-950/80 border border-stone-800/90 space-y-3">
                 <label className="block text-[11px] font-bold uppercase tracking-wider text-amber-200/90">
                   ⚡ Quick Lookup (Returning Customers)
                 </label>
                <div className="flex gap-2">
                  <input
                    type="text"
                    inputMode="numeric"
                    autoComplete="tel"
                    placeholder="Enter phone number (e.g. 5551234567)"
                    maxLength={12}
                    value={searchPhone}
                    onChange={handleSearchPhoneChange}
                    style={
                      searchPhoneError
                        ? { boxShadow: '0 0 18px rgba(239, 68, 68, 0.45)' }
                        : undefined
                    }
                    className={`flex-1 rounded-xl px-4 py-2.5 text-xs text-stone-100 placeholder-stone-500 transition-all duration-300 outline-none ${
                      searchPhoneError
                        ? 'bg-rose-950/30 border-2 border-rose-500 ring-2 ring-rose-500/40 shadow-[0_0_15px_rgba(244,63,94,0.3)]'                    
                        : 'bg-stone-900 border border-stone-700/80 focus:border-amber-300 focus:ring-2 focus:ring-amber-400/40 focus:shadow-[0_0_15px_rgba(251,191,36,0.2)]'                    }`}
                  />
                 <button
                    type="submit"
                    disabled={isSearchingPhone}
                    className="px-5 py-2.5 bg-stone-800 hover:bg-stone-700 text-amber-200 text-xs font-semibold rounded-xl border border-stone-700 hover:border-amber-200/40 transition-all cursor-pointer whitespace-nowrap disabled:opacity-50"
                 >
                   {isSearchingPhone ? 'Searching...' : 'Find My Info'}
                 </button>
              </div>

              {/* Status Message Badge */}
              {lookupMessage && (
              <div
                className={`text-xs px-3.5 py-2 rounded-xl border ${
                lookupMessage.type === 'success'
                  ? 'bg-emerald-950/40 border-emerald-800/60 text-emerald-300'
                  : lookupMessage.type === 'error'
                  ? 'bg-rose-950/40 border-rose-800/60 text-rose-300'
                  : 'bg-stone-900 border-stone-700 text-stone-300'
                }`}
             >
               {lookupMessage.text}
             </div>
             )}
            </form>

            {/* Past Order History Section (Only displays if returning user has past orders) */}
            {isReturningUser && pastOrders.length > 0 && (
             <div className="p-5 rounded-2xl bg-amber-950/15 border border-amber-900/40 space-y-4">
               <div className="flex justify-between items-center">
                 <h4 className="font-serif uppercase tracking-wider text-amber-100 text-sm">
                    📜 Past Favorites ({pastOrders.length} previous {pastOrders.length === 1 ? 'order' : 'orders'})
                 </h4>
                <span className="text-[10px] font-mono uppercase tracking-widest text-amber-300/80">Reorder with 1-click</span>
               </div>

               <div className="space-y-3 max-h-60 overflow-y-auto pr-1">
                 {pastOrders.map((order) => (
                  <div key={order.id} className="p-3.5 rounded-xl bg-stone-900/90 border border-stone-800/80 space-y-2 text-xs">
                    <div className="flex justify-between items-center text-stone-400 font-mono text-[11px] border-b border-stone-800/60 pb-1.5">
                      <span>Order #{order.id} • {new Date(order.created_at).toLocaleDateString()}</span>
                      <span className="text-amber-200 font-bold">${parseFloat(order.total_price).toFixed(2)}</span>
                   </div>

                   {/* List of pizzas inside this past order */}
                   <div className="space-y-2">
                     {order.order_items?.map((item) => (
                      <div key={item.id} className="flex justify-between items-center gap-3">
                        <div>
                          <span className="font-semibold text-stone-200">{item.size} • {item.crust}</span>
                          <span className="block text-[11px] text-stone-400">
                            Sauce: {item.sauce} {item.toppings?.length > 0 ? `| Toppings: ${item.toppings.join(', ')}` : ''}
                         </span>
                      </div>
                      <button
                        type="button"
                        onClick={() => handleReorderPizza(item)}
                        className="px-3 py-1 rounded-lg bg-amber-200/10 hover:bg-amber-200/20 text-amber-200 border border-amber-200/30 text-[10px] font-bold uppercase tracking-wider hover:scale-105 active:scale-95 transition-all cursor-pointer whitespace-nowrap"
                      >
                        + Re-order
                     </button>
                   </div>
                  ))}
               </div>
             </div>
           ))}
         </div>
      </div>
    )}

    {/* Standard Customer Details Form */}
    <form onSubmit={handleCustomerSubmit} className="space-y-5">
      
      {/* Order Type Toggle */}
      <div>
        <label className="block text-xs uppercase tracking-wider text-stone-400 font-semibold mb-2">Order Fulfillment</label>
        <div className="grid grid-cols-2 gap-3">
          {['delivery', 'pickup'].map((type) => (
            <button
              key={type}
              type="button"
              onClick={() => setCustomerInfo((prev) => ({ ...prev, orderType: type }))}
              className={`py-2.5 rounded-xl border text-xs uppercase tracking-wider font-semibold transition-all cursor-pointer ${
                customerInfo.orderType === type
                  ? 'bg-amber-200 text-stone-950 border-amber-200 shadow-md'
                  : 'bg-stone-950 border-stone-800 text-stone-400 hover:border-stone-700'
              }`}
            >
              {type}
            </button>
          ))}
        </div>
      </div>

      {/* Primary Inputs */}
      <div className="grid grid-cols-1 sm:grid-cols-2 gap-4">
        <div>
          <label className="block text-xs uppercase tracking-wider text-stone-400 font-semibold mb-1">Full Name</label>
          <input
            type="text"
            name="name"
            required
            value={customerInfo.name}
            onChange={handleCustomerChange}
            placeholder="Jane Doe"
            className="w-full bg-stone-950 border border-stone-800 rounded-xl px-4 py-2.5 text-xs text-stone-200 placeholder-stone-600 focus:outline-none focus:border-amber-200/60"
          />
        </div>

        <div>
          <label className="block text-xs uppercase tracking-wider text-stone-400 font-semibold mb-1">Phone Number</label>
          <input
            type="text"
            inputMode="numeric"
            autoComplete="tel"
            name="phone"
            required
            maxLength={12}
            value={customerInfo.phone}
            onChange={handleFormPhoneChange}
            placeholder="(555) 000-0000"
            className={`w-full rounded-xl px-4 py-2.5 text-xs text-stone-100 placeholder-stone-600 transition-all duration-300 outline-none ${            
              formPhoneError
              ? 'bg-rose-950/30 border-2 border-rose-500 ring-2 ring-rose-500/40 shadow-[0_0_15px_rgba(244,63,94,0.3)]'           
              : 'bg-stone-950 border border-stone-800 focus:border-amber-300 focus:ring-2 focus:ring-amber-400/40 focus:shadow-[0_0_15px_rgba(251,191,36,0.2)]'           }`}
          />
          {formPhoneError && (
            <p className="text-[11px] text-rose-400 font-medium mt-1">{formPhoneError}</p>
          )}
        </div>
      </div>

      {/* Address Inputs (Conditional on Delivery) */}
      {customerInfo.orderType === 'delivery' && (
        <div className="space-y-4 pt-2 border-t border-stone-800/60 animate-in fade-in">
          <div>
            <label className="block text-xs uppercase tracking-wider text-stone-400 font-semibold mb-1">Street Address</label>
            <input
              type="text"
              name="streetAddress"
              required
              value={customerInfo.streetAddress}
              onChange={handleCustomerChange}
              placeholder="123 Stone Oven Way"
              className="w-full bg-stone-950 border border-stone-800 rounded-xl px-4 py-2.5 text-xs text-stone-200 placeholder-stone-600 focus:outline-none focus:border-amber-200/60"
            />
          </div>

          <div className="grid grid-cols-1 sm:grid-cols-2 gap-4">
            <div>
              <label className="block text-xs uppercase tracking-wider text-stone-400 font-semibold mb-1">Subdivision / Complex (Opt)</label>
              <input
                type="text"
                name="subdivision"
                value={customerInfo.subdivision}
                onChange={handleCustomerChange}
                placeholder="e.g. Amber Ridge Apt 4B"
                className="w-full bg-stone-950 border border-stone-800 rounded-xl px-4 py-2.5 text-xs text-stone-200 placeholder-stone-600 focus:outline-none focus:border-amber-200/60"
              />
            </div>

            <div>
              <label className="block text-xs uppercase tracking-wider text-stone-400 font-semibold mb-1">Nearest Major Intersection</label>
              <input
                type="text"
                name="nearestIntersection"
                value={customerInfo.nearestIntersection}
                onChange={handleCustomerChange}
                placeholder="e.g. Main St & 4th Ave"
                className="w-full bg-stone-950 border border-stone-800 rounded-xl px-4 py-2.5 text-xs text-stone-200 placeholder-stone-600 focus:outline-none focus:border-amber-200/60"
              />
            </div>
          </div>
        </div>
      )}

      {/* Submit Actions */}
      <div className="pt-4 flex flex-col sm:flex-row gap-3">
        <button
          type="submit"
          className="flex-1 py-3.5 bg-amber-200 hover:bg-amber-100 text-stone-950 font-bold rounded-xl text-xs uppercase tracking-wider transition-all duration-300 hover:scale-[1.02] active:scale-95 cursor-pointer shadow-lg shadow-amber-200/10"
        >
          {isReturningUser ? 'Confirm Details & Build Custom Pizza →' : 'Save Details & Start Custom Pizza →'}
        </button>
      </div>
    </form>
  </div>
</div>


                {/* SECTION 2: PIZZA BUILDER */}
                <div className="w-1/2 pl-4">
                  <div className="bg-stone-900/80 border border-stone-800 rounded-3xl p-6 sm:p-10 shadow-2xl backdrop-blur-md space-y-6">
                    
                    <div className="flex items-center justify-between border-b border-stone-800 pb-5">
                      <div>
                        <h2 className="text-2xl font-serif uppercase tracking-wider text-stone-100 flex items-center gap-3">
                          <span className="flex h-7 w-7 items-center justify-center rounded-lg bg-amber-200 text-stone-950 text-xs font-bold font-mono">
                            2
                          </span>
                          {editingId ? 'Edit Pizza' : 'Build Custom Pie'}
                        </h2>
                        <p className="text-stone-400 text-xs mt-1">Select crust, base, and toppings.</p>
                      </div>
                      <button
                        type="button"
                        onClick={() => setActiveStep('details')}
                        className="text-xs uppercase tracking-wider text-stone-400 hover:text-stone-100 border border-stone-800 hover:border-stone-600 rounded-xl px-3 py-1.5 transition-all cursor-pointer"
                      >
                        ← Edit Info
                      </button>
                    </div>

                    {/* Dropdowns */}
                    <div className="grid grid-cols-1 sm:grid-cols-3 gap-3">
                      {/* Size */}
                      <div className="relative">
                        <label className="block text-[11px] font-bold uppercase tracking-wider text-stone-400 mb-1">1. Size</label>
                        <button
                          type="button"
                          onClick={() => setActiveDropdown(activeDropdown === 'size' ? null : 'size')}
                          className="w-full flex items-center justify-between bg-stone-950 border border-stone-800 hover:border-amber-200/60 rounded-xl px-3.5 py-2.5 text-left text-xs font-semibold text-stone-200 transition-all cursor-pointer"
                        >
                          <span className="truncate">{pizzaBuilder.size.label}</span>
                          <svg className={`w-3.5 h-3.5 text-stone-500 transition-transform ${activeDropdown === 'size' ? 'rotate-180' : ''}`} fill="none" stroke="currentColor" viewBox="0 0 24 24">
                            <path strokeLinecap="round" strokeLinejoin="round" strokeWidth="2" d="M19 9l-7 7-7-7" />
                          </svg>
                        </button>
                        {activeDropdown === 'size' && (
                          <div className="absolute z-30 mt-2 w-full bg-stone-900 border border-stone-800 rounded-xl shadow-2xl overflow-hidden">
                            {SIZES.map((item) => (
                              <div
                                key={item.id}
                                onClick={() => {
                                  setPizzaBuilder((prev) => ({ ...prev, size: item }));
                                  setActiveDropdown(null);
                                }}
                                className={`px-3.5 py-2 text-xs cursor-pointer flex justify-between items-center transition-colors ${
                                  pizzaBuilder.size.id === item.id ? 'bg-amber-200/10 text-amber-200 font-bold' : 'hover:bg-stone-800 text-stone-300'
                                }`}
                              >
                                <span>{item.label}</span>
                                <span className="font-mono text-stone-400">${item.price.toFixed(2)}</span>
                              </div>
                            ))}
                          </div>
                        )}
                      </div>

                      {/* Crust */}
                      <div className="relative">
                        <label className="block text-[11px] font-bold uppercase tracking-wider text-stone-400 mb-1">2. Crust</label>
                        <button
                          type="button"
                          onClick={() => setActiveDropdown(activeDropdown === 'crust' ? null : 'crust')}
                          className="w-full flex items-center justify-between bg-stone-950 border border-stone-800 hover:border-amber-200/60 rounded-xl px-3.5 py-2.5 text-left text-xs font-semibold text-stone-200 transition-all cursor-pointer"
                        >
                          <span className="truncate">{pizzaBuilder.crust.label}</span>
                          <svg className={`w-3.5 h-3.5 text-stone-500 transition-transform ${activeDropdown === 'crust' ? 'rotate-180' : ''}`} fill="none" stroke="currentColor" viewBox="0 0 24 24">
                            <path strokeLinecap="round" strokeLinejoin="round" strokeWidth="2" d="M19 9l-7 7-7-7" />
                          </svg>
                        </button>
                        {activeDropdown === 'crust' && (
                          <div className="absolute z-30 mt-2 w-full bg-stone-900 border border-stone-800 rounded-xl shadow-2xl overflow-hidden">
                            {CRUSTS.map((item) => (
                              <div
                                key={item.id}
                                onClick={() => {
                                  setPizzaBuilder((prev) => ({ ...prev, crust: item }));
                                  setActiveDropdown(null);
                                }}
                                className={`px-3.5 py-2 text-xs cursor-pointer transition-colors ${
                                  pizzaBuilder.crust.id === item.id ? 'bg-amber-200/10 text-amber-200 font-bold' : 'hover:bg-stone-800 text-stone-300'
                                }`}
                              >
                                {item.label}
                              </div>
                            ))}
                          </div>
                        )}
                      </div>

                      {/* Sauce */}
                      <div className="relative">
                        <label className="block text-[11px] font-bold uppercase tracking-wider text-stone-400 mb-1">3. Sauce</label>
                        <button
                          type="button"
                          onClick={() => setActiveDropdown(activeDropdown === 'sauce' ? null : 'sauce')}
                          className="w-full flex items-center justify-between bg-stone-950 border border-stone-800 hover:border-amber-200/60 rounded-xl px-3.5 py-2.5 text-left text-xs font-semibold text-stone-200 transition-all cursor-pointer"
                        >
                          <span className="truncate">{pizzaBuilder.sauce.label}</span>
                          <svg className={`w-3.5 h-3.5 text-stone-500 transition-transform ${activeDropdown === 'sauce' ? 'rotate-180' : ''}`} fill="none" stroke="currentColor" viewBox="0 0 24 24">
                            <path strokeLinecap="round" strokeLinejoin="round" strokeWidth="2" d="M19 9l-7 7-7-7" />
                          </svg>
                        </button>
                        {activeDropdown === 'sauce' && (
                          <div className="absolute z-30 mt-2 w-full bg-stone-900 border border-stone-800 rounded-xl shadow-2xl overflow-hidden">
                            {SAUCE_LEVELS.map((item) => (
                              <div
                                key={item.id}
                                onClick={() => {
                                  setPizzaBuilder((prev) => ({ ...prev, sauce: item }));
                                  setActiveDropdown(null);
                                }}
                                className={`px-3.5 py-2 text-xs cursor-pointer transition-colors ${
                                  pizzaBuilder.sauce.id === item.id ? 'bg-amber-200/10 text-amber-200 font-bold' : 'hover:bg-stone-800 text-stone-300'
                                }`}
                              >
                                {item.label}
                              </div>
                            ))}
                          </div>
                        )}
                      </div>
                    </div>

                    {/* Toppings Columns */}
                    <div className="space-y-3">
                      <h3 className="text-xs font-bold uppercase tracking-widest text-stone-300">4. Toppings Selection</h3>
                      <div className="grid grid-cols-1 md:grid-cols-2 gap-4">
                        
                        {/* Veggies */}
                        <div className="p-4 rounded-2xl bg-stone-950/60 border border-stone-800 space-y-2">
                          <div className="text-[11px] font-bold text-emerald-400 uppercase tracking-widest mb-2">Garden Veggies</div>
                          <div className="space-y-1.5">
                            {VEGGIE_OPTIONS.map((veggie) => {
                              const isSelected = pizzaBuilder.selectedVeggies.some((v) => v.id === veggie.id);
                              return (
                                <div
                                  key={veggie.id}
                                  onClick={() => toggleVeggie(veggie)}
                                  className={`flex items-center justify-between p-2.5 rounded-xl border transition-all cursor-pointer ${
                                    isSelected
                                      ? 'bg-emerald-950/40 border-emerald-500/80 text-emerald-200'
                                      : 'bg-stone-900/50 border-stone-800/80 text-stone-300 hover:border-stone-700'
                                  }`}
                                >
                                  <span className="text-xs">{veggie.name}</span>
                                  <span className="text-[11px] font-mono text-stone-400">+${veggie.price.toFixed(2)}</span>
                                </div>
                              );
                            })}
                          </div>
                        </div>

                        {/* Meats */}
                        <div className="p-4 rounded-2xl bg-stone-950/60 border border-stone-800 space-y-2">
                          <div className="text-[11px] font-bold text-rose-400 uppercase tracking-widest mb-2">Savory Meats</div>
                          <div className="space-y-1.5">
                            {MEAT_OPTIONS.map((meat) => {
                              const isSelected = pizzaBuilder.selectedMeats.some((m) => m.id === meat.id);
                              return (
                                <div
                                  key={meat.id}
                                  onClick={() => toggleMeat(meat)}
                                  className={`flex items-center justify-between p-2.5 rounded-xl border transition-all cursor-pointer ${
                                    isSelected
                                      ? 'bg-rose-950/40 border-rose-500/80 text-rose-200'
                                      : 'bg-stone-900/50 border-stone-800/80 text-stone-300 hover:border-stone-700'
                                  }`}
                                >
                                  <span className="text-xs">{meat.name}</span>
                                  <span className="text-[11px] font-mono text-stone-400">+${meat.price.toFixed(2)}</span>
                                </div>
                              );
                            })}
                          </div>
                        </div>

                      </div>
                    </div>

                    {/* Builder Footer */}
                    <div className="pt-3 border-t border-stone-800 flex flex-col sm:flex-row items-center justify-between gap-4">
                      <div>
                        {lastAddedPrice ? (
                          <div>
                            <span className="text-[11px] text-stone-400 uppercase tracking-widest block">Last Added Total</span>
                            <span className="text-2xl font-serif text-amber-200">${lastAddedPrice}</span>
                          </div>
                        ) : (
                          <span className="text-xs text-stone-500 italic">Total calculates upon adding</span>
                        )}
                      </div>

                      <button
                        type="button"
                        onClick={handleSavePizza}
                        className="w-full sm:w-auto px-7 py-3 bg-amber-200 hover:bg-amber-100 text-stone-950 font-bold rounded-xl text-xs uppercase tracking-wider transition-all cursor-pointer"
                      >
                        {editingId ? 'Update Pizza in Order' : 'Add to Order'}
                      </button>
                    </div>

                  </div>
                </div>

              </div>
            </div>

          </div>
        )}

      </main>

      {/* ================= CART POPUP DRAWER ================= */}
      {isCartOpen && (
        <div className="fixed inset-0 z-50 flex items-center justify-center p-4 bg-black/80 backdrop-blur-sm animate-in fade-in duration-200">
          <div className="bg-stone-900 border border-stone-800 rounded-3xl max-w-md w-full max-h-[85vh] shadow-2xl flex flex-col overflow-hidden">
            <div className="p-5 border-b border-stone-800 flex justify-between items-center bg-stone-950">
              <h3 className="text-base font-serif uppercase tracking-wider text-stone-100">Order Cart ({cart.length})</h3>
              <button
                type="button"
                onClick={() => setIsCartOpen(false)}
                className="text-stone-400 hover:text-white transition-colors cursor-pointer text-xs"
              >
                ✕ Close
              </button>
            </div>

            <div className="p-5 overflow-y-auto space-y-3 flex-1">
              {cart.length === 0 ? (
                <div className="text-center py-8 text-stone-500 text-xs">Your cart is empty.</div>
              ) : (
                cart.map((pizza, index) => (
                  <div key={pizza.id} className="p-3.5 rounded-xl bg-stone-950/70 border border-stone-800/80 space-y-1.5 text-xs">
                    <div className="flex justify-between font-semibold text-stone-200">
                      <span>#{index + 1}: {pizza.size.label}</span>
                      <span className="font-mono text-amber-200">${pizza.price}</span>
                    </div>
                    <div className="text-[11px] text-stone-400">
                      <div>Crust: {pizza.crust.label} | Sauce: {pizza.sauce.label}</div>
                      {(pizza.selectedVeggies.length > 0 || pizza.selectedMeats.length > 0) && (
                        <div className="mt-0.5 text-stone-300">
                          {[...pizza.selectedMeats, ...pizza.selectedVeggies].map((t) => t.name).join(', ')}
                        </div>
                      )}
                    </div>
                    <div className="flex justify-end gap-3 pt-1 text-[11px]">
                      <button type="button" onClick={() => handleEditItem(pizza)} className="text-stone-300 hover:text-amber-200 cursor-pointer">
                        Edit
                      </button>
                      <button type="button" onClick={() => handleDeleteItem(pizza.id)} className="text-rose-400 hover:text-rose-300 cursor-pointer">
                        Remove
                      </button>
                    </div>
                  </div>
                ))
              )}
            </div>

            {cart.length > 0 && (
              <div className="p-5 border-t border-stone-800 bg-stone-950 space-y-3">
                <div className="flex justify-between text-xs font-bold uppercase tracking-wider text-stone-300">
                  <span>Grand Total:</span>
                  <span className="text-base font-mono text-amber-200">${grandTotal}</span>
                </div>
                <button
                  type="button"
                  onClick={() => {
                   setIsCartOpen(false);
                   setIsCheckoutModalOpen(true);
                 }}
                 className="w-full py-3.5 bg-amber-200 hover:bg-amber-100 text-stone-950 font-bold rounded-xl text-xs uppercase tracking-wider transition-all cursor-pointer shadow-lg shadow-amber-200/10"
                >
                  Proceed to Payment (${grandTotal}) →
                </button>
              </div>
            )}
          </div>
        </div>
      )}

      {/* --------- Checkout and Payment Selection Modal ----------- */}
      {isCheckoutModalOpen && (
        <div className="fixed inset-0 z-50 flex items-center justify-center p-4 bg-black/85 backdrop-blur-md animate-in fade-in duration-200 overflow-y-auto">
          <div className="bg-stone-900 border border-stone-800 rounded-3xl max-w-lg w-full max-h-[92vh] shadow-2xl flex flex-col overflow-hidden my-auto">
            
            {/* Modal Header */}
            <div className="p-5 border-b border-stone-800 flex justify-between items-center bg-stone-950">
              <div>
                <span className="text-[10px] font-mono uppercase tracking-widest text-amber-200/80">Final Step</span>
                <h3 className="text-base font-serif uppercase tracking-wider text-stone-100">Review & Pay</h3>
              </div>
              <button
                type="button"
                onClick={() => setIsCheckoutModalOpen(false)}
                className="text-stone-400 hover:text-white transition-colors cursor-pointer text-xs"
              >
                ✕ Cancel
              </button>
            </div>

            {/* Modal Scrollable Body */}
            <div className="p-6 overflow-y-auto space-y-6 text-xs text-stone-300">
              
              {/* Payment Method Selector */}
              <div className="space-y-2">
                <label className="block text-[11px] font-bold uppercase tracking-wider text-amber-200/90">
                  Select Payment Method
                </label>
                <div className="grid grid-cols-3 gap-2">
                  {[
                    { id: 'credit', label: '💳 Credit' },
                    { id: 'debit', label: '🏧 Debit' },
                    { id: 'cash', label: '💵 Cash' },
                  ].map((m) => (
                    <button
                      key={m.id}
                      type="button"
                      onClick={() => setSelectedPaymentMethod(m.id)}
                      className={`py-3 px-2 rounded-xl border text-center transition-all font-semibold cursor-pointer ${
                        selectedPaymentMethod === m.id
                          ? 'bg-amber-200 text-stone-950 border-amber-200 shadow-md'
                          : 'bg-stone-950/80 border-stone-800 text-stone-400 hover:border-stone-700'
                      }`}
                    >
                      {m.label}
                    </button>
                  ))}
                </div>
              </div>

              {/* Card Details and Saved Payments (For Credit and Debit) */}
              {(selectedPaymentMethod === 'credit' || selectedPaymentMethod === 'debit') && (
  <div className="p-4 rounded-2xl bg-stone-950/80 border border-stone-800 space-y-4 my-3">
    <div className="flex justify-between items-center">
      <label className="text-[11px] font-bold uppercase tracking-wider text-amber-200/90">
        💳 {selectedPaymentMethod.toUpperCase()} Card Details
      </label>
      {savedCards && savedCards.length > 0 && (
        <button
          type="button"
          onClick={() => {
            setIsAddingNewCard((prev) => !prev);
            setSelectedSavedCardId(null);
          }}
          className="text-[10px] text-amber-200/80 hover:text-amber-100 underline cursor-pointer"
        >
          {isAddingNewCard ? 'Select Saved Card' : '+ Use New Card'}
        </button>
      )}
    </div>

    {/* Saved Cards Picker (if cards exist and not adding new) */}
    {!isAddingNewCard && savedCards && savedCards.length > 0 ? (
      <div className="space-y-2">
        <span className="text-[10px] text-stone-400 uppercase tracking-wider font-semibold block">
          Choose from Saved Cards
        </span>
        <div className="grid grid-cols-1 gap-2">
          {savedCards.map((card) => (
            <button
              key={card.id}
              type="button"
              onClick={() => setSelectedSavedCardId(card.id)}
              className={`p-3 rounded-xl border text-left flex justify-between items-center transition-all cursor-pointer ${
                selectedSavedCardId === card.id
                  ? 'bg-amber-200/10 border-amber-300 text-stone-100 ring-1 ring-amber-300/40'
                  : 'bg-stone-900/60 border-stone-800 text-stone-400 hover:border-stone-700'
              }`}
            >
              <div>
                <span className="font-semibold text-xs text-stone-200 block">
                  {card.nickname}
                </span>
                <span className="text-[11px] text-stone-400 font-mono">
                  {card.brand} •••• {card.last4} (Exp: {card.expiry})
                </span>
              </div>
              {selectedSavedCardId === card.id && (
                <span className="text-amber-300 text-xs font-bold">✓ Selected</span>
              )}
            </button>
          ))}
        </div>
      </div>
    ) : (
      /* Manual Card Input Fields */
      <div className="space-y-3 pt-1">
        {/* Card Incomplete Error Banner */}
        {cardFormErrors.general && (
          <div className="p-3 rounded-xl bg-rose-950/40 border border-rose-500/80 text-rose-300 text-xs flex items-center gap-2">
             <span>⚠️</span>
             <span>{cardFormErrors.general}</span>
          </div>
        )}

        <div>
          <label className="block text-[10px] uppercase tracking-wider text-stone-400 mb-1">
            Card Nickname (e.g. Husband's Debit)
          </label>
          <input
            type="text"
            placeholder="e.g. Husband's Debit, Joint Checking"
            value={cardForm.nickname}
            onChange={(e) => setCardForm((prev) => ({ ...prev, nickname: e.target.value }))}
            className="w-full bg-stone-900 border border-stone-800 rounded-xl px-3.5 py-2 text-xs text-stone-200 placeholder-stone-600 focus:outline-none focus:border-amber-200/60"
          />
        </div>

        <div>
          <label className="block text-[10px] uppercase tracking-wider text-stone-400 mb-1">
            Card Number
          </label>
          <input
            type="text"
            inputMode="numeric"
            placeholder="1234 5678 9012 3456"
            maxLength={19}
            value={cardForm.cardNumber}
            onChange={(e) => {
              const formatted = formatCardNumber(e.target.value);
              setCardForm((prev) => ({ ...prev, cardNumber: formatted }));
              if (cardFormErrors.cardNumber || cardFormErrors.general) {
                setCardFormErrors((prev) => ({ ...prev, cardNumber: null }));
              }
            }}
            style={
              cardFormErrors.cardNumber
                ? { boxShadow: '0 0 15px rgba(239, 68, 68, 0.35)' }
                : undefined
            }
            className={`w-full bg-stone-900 rounded-xl px-3.5 py-2 text-xs text-stone-200 font-mono focus:outline-none ${
              cardFormErrors.cardNumber ? 'border border-red-500' : 'border border-stone-800 focus:border-amber-200/60'
            }`}
          />
          {cardFormErrors.cardNumber && (
            <p className="text-[10px] text-red-400 mt-0.5">{cardFormErrors.cardNumber}</p>
          )}
        </div>

        <div>
          <label className="block text-[10px] uppercase tracking-wider text-stone-400 mb-1">
            Name on Card
          </label>
          <input
            type="text"
            placeholder="Full Name as shown on card"
            value={cardForm.cardholderName}
            onChange={(e) => {
              setCardForm((prev) => ({ ...prev, cardholderName: e.target.value }));
              if (cardFormErrors.cardholderName || cardFormErrors.general) {
                setCardFormErrors((prev) => ({ ...prev, cardholderName: null }));
              }
            }}
            style={
              cardFormErrors.cardholderName
                ? { boxShadow: '0 0 15px rgba(239, 68, 68, 0.35)' }
                : undefined
            }
            className={`w-full bg-stone-900 rounded-xl px-3.5 py-2 text-xs text-stone-200 focus:outline-none ${
              cardFormErrors.cardholderName ? 'border border-red-500' : 'border border-stone-800 focus:border-amber-200/60'
            }`}
          />
          {cardFormErrors.cardholderName && (
            <p className="text-[10px] text-red-400 mt-0.5">{cardFormErrors.cardholderName}</p>
          )}
        </div>

        <div className="grid grid-cols-2 gap-3">
          <div>
            <label className="block text-[10px] uppercase tracking-wider text-stone-400 mb-1">
              Expiration (MM/YY)
            </label>
            <input
              type="text"
              inputMode="numeric"
              placeholder="12/28"
              maxLength={5}
              value={cardForm.expiry}
              onChange={(e) => {
                const formatted = formatExpiry(e.target.value);
                setCardForm((prev) => ({ ...prev, expiry: formatted }));
                if (cardFormErrors.expiry || cardFormErrors.general) {
                  setCardFormErrors((prev) => ({ ...prev, expiry: null }));
                }
              }}
              style={
                cardFormErrors.expiry
                  ? { boxShadow: '0 0 15px rgba(239, 68, 68, 0.35)' }
                  : undefined
              }
              className={`w-full bg-stone-900 rounded-xl px-3.5 py-2 text-xs text-stone-200 font-mono focus:outline-none ${
                cardFormErrors.expiry ? 'border border-red-500' : 'border border-stone-800 focus:border-amber-200/60'
              }`}
            />
            {cardFormErrors.expiry && (
              <p className="text-[10px] text-red-400 mt-0.5">{cardFormErrors.expiry}</p>
            )}
          </div>

          <div>
            <label className="block text-[10px] uppercase tracking-wider text-stone-400 mb-1">
              CVV / CVC
            </label>
            <input
              type="password"
              inputMode="numeric"
              placeholder="123"
              maxLength={4}
              value={cardForm.cvv}
              onChange={(e) => {
                const clean = e.target.value.replace(/\D/g, '').slice(0, 4);
                setCardForm((prev) => ({ ...prev, cvv: clean }));
                if (cardFormErrors.cvv || cardFormErrors.general) {
                  setCardFormErrors((prev) => ({ ...prev, cvv: null }));
                }
              }}
              style={
                cardFormErrors.cvv
                  ? { boxShadow: '0 0 15px rgba(239, 68, 68, 0.35)' }
                  : undefined
              }
              className={`w-full bg-stone-900 rounded-xl px-3.5 py-2 text-xs text-stone-200 font-mono focus:outline-none ${
                cardFormErrors.cvv ? 'border border-red-500' : 'border border-stone-800 focus:border-amber-200/60'
              }`}
            />
            {cardFormErrors.cvv && (
              <p className="text-[10px] text-red-400 mt-0.5">{cardFormErrors.cvv}</p>
            )}
          </div>
        </div>

        <button
          type="button"
          onClick={handleSaveCard}
          className="w-full py-2.5 bg-stone-800 hover:bg-stone-700 text-amber-200 text-xs font-semibold rounded-xl border border-stone-700 hover:border-amber-200/40 transition-all cursor-pointer"
        >
          💾 Save Card Info
        </button>
      </div>
    )}
  </div>
)}
{/* ========================================================================= */}










              {/* Gratuity / Tip Selection */}
              <div className="space-y-2">
                <label className="block text-[11px] font-bold uppercase tracking-wider text-amber-200/90">
                  Add Tip for the Kitchen & Drivers
                </label>
                <div className="grid grid-cols-4 gap-2">
                  {[15, 18, 20].map((pct) => (
                    <button
                      key={pct}
                      type="button"
                      onClick={() => setSelectedTipPercent(pct)}
                      className={`py-2 rounded-xl border text-center transition-all cursor-pointer font-mono ${
                        selectedTipPercent === pct
                          ? 'bg-amber-200 text-stone-950 border-amber-200 font-bold'
                          : 'bg-stone-950/80 border-stone-800 text-stone-400 hover:border-stone-700'
                      }`}
                    >
                      {pct}%
                    </button>
                  ))}
                  <button
                    type="button"
                    onClick={() => setSelectedTipPercent('custom')}
                    className={`py-2 rounded-xl border text-center transition-all cursor-pointer font-mono ${
                      selectedTipPercent === 'custom'
                        ? 'bg-amber-200 text-stone-950 border-amber-200 font-bold'
                        : 'bg-stone-950/80 border-stone-800 text-stone-400 hover:border-stone-700'
                    }`}
                  >
                    Custom
                  </button>
                </div>

                {selectedTipPercent === 'custom' && (
                  <input
                    type="number"
                    min="0"
                    step="0.50"
                    placeholder="Enter custom tip amount ($)"
                    value={customTipAmount}
                    onChange={(e) => setCustomTipAmount(e.target.value)}
                    className="w-full bg-stone-950 border border-stone-800 rounded-xl px-4 py-2 text-xs text-stone-200 focus:outline-none focus:border-amber-200/60"
                  />
                )}
              </div>

              {/* Financial Summary */}
              <div className="p-4 rounded-2xl bg-stone-950/80 border border-stone-800/90 space-y-2">
                <div className="flex justify-between text-stone-400">
                  <span>Subtotal:</span>
                  <span className="font-mono text-stone-200">${subtotalNumber.toFixed(2)}</span>
                </div>
                {customerInfo.orderType === 'delivery' && (
                  <div className="flex justify-between text-stone-400">
                    <span>Delivery Fee:</span>
                    <span className="font-mono text-stone-200">${deliveryFee.toFixed(2)}</span>
                  </div>
                )}
                <div className="flex justify-between text-stone-400">
                  <span>Sales Tax (8%):</span>
                  <span className="font-mono text-stone-200">${tax.toFixed(2)}</span>
                </div>
                <div className="flex justify-between text-stone-400">
                  <span>Tip:</span>
                  <span className="font-mono text-amber-200">+${tipAmount.toFixed(2)}</span>
                </div>
                <div className="border-t border-stone-800 pt-2 flex justify-between font-bold text-stone-100 text-sm">
                  <span>Total Amount Due:</span>
                  <span className="font-mono text-amber-200 text-base">${finalGrandTotal.toFixed(2)}</span>
                </div>
              </div>

              {/* Digital Signature Pad */}
              <div className="relative space-y-3 pt-2 border-t border-stone-800">
                 
                 {/* Disabled Overlay if card into is not completed */}
                 {!isCardComplete() && (
                   <div className="absolute inset-0 z-10 bg-stone-900/85 backdrop-blur-[2px] rounded-2xl flex flex-col items-center justify-center p-4 text-center border border-dashed border-stone-700">
                     <span className="text-xl mb-1">🔒</span>
                     <p className="text-xs font-semibold text-amber-200/90">Card Details Required</p>
                     <p className="text-[10px] text-stone-400 mt-0.5">
                       Please complete the card details or select a prior saved card above.
                     </p>
                   </div>
                 )}


                <div className="flex justify-between items-center">
                  <label className="block text-[11px] font-bold uppercase tracking-wider text-amber-200/90">
                    Customer Signature
                  </label>
                  <div className="flex gap-2 text-[10px]">
                    <button
                      type="button"
                      disabled={!isCardComplete()}
                      onClick={() => {
                        setSignatureMode('draw');
                        setSignatureError('');
                               
                      }}
                      className={`px-2 py-0.5 rounded cursor-pointer ${
                        signatureMode === 'draw' ? 'bg-amber-200/20 text-amber-200 font-bold' : 'text-stone-500'
                      }`}
                    >
                      Draw
                    </button>
                    <span>|</span>
                    <button
                      type="button"
                      disabled={!isCardComplete()}
                      onClick={() => {
                        setSignatureMode('type');
                        setSignatureError('');
                      }}
                      className={`px-2 py-0.5 rounded cursor-pointer ${
                        signatureMode === 'type' ? 'bg-amber-200/20 text-amber-200 font-bold' : 'text-stone-500'
                      }`}
                    >
                      Type Name
                    </button>
                  </div>
                </div>

                {signatureMode === 'draw' ? (
                  <div className="space-y-1.5">
                    <div
                      style={
                        signatureError
                          ? { boxShadow: '0 0 18px rgba(239, 68, 68, 0.45)' }
                          : undefined
                       }
                      className={`relative rounded-2xl bg-stone-950 overflow-hidden touch-none transition-all duration-300 ${
                        signatureError
                          ? 'border-2 border-red-500 ring-2 ring-red-500/50 bg-red-950/20'
                          : 'border border-stone-700/80'
                       }`}
                    >
                      <canvas
                        ref={canvasRef}
                        width={400}
                        height={120}
                        onMouseDown={isCardComplete() ? startDrawing : undefined}
                        onMouseMove={isCardComplete() ? draw : undefined}
                        onMouseUp={isCardComplete() ? stopDrawing : undefined}
                        onMouseLeave={isCardComplete() ? stopDrawing : undefined}
                        onTouchStart={isCardComplete() ? startDrawing : undefined}
                        onTouchMove={isCardComplete() ? draw : undefined}
                        onTouchEnd={isCardComplete() ? stopDrawing : undefined}
                        className="w-full h-[120px] cursor-crosshair"
                      />
                      <div className="absolute bottom-2 left-4 text-[10px] text-stone-600 font-mono pointer-events-none select-none">
                        Sign along the line ✍️
                      </div>
                    </div>

                    <div className="flex justify-between items-center">
                      {signatureError ? (
                        <p className="text=[11px] text-rose-400 font-medium">
                          {signatureError}
                        </p>
                      ) : (
                        <span />
                      )}
                      <button
                        type="button"
                        onClick={clearCanvasSignature}
                        className="text-[10px] text-stone-500 hover:text-stone-300 cursor-pointer"
                      >
                        Clear Signature
                      </button>
                    </div>
                  </div>
                ) : (
                  <div className="space-y-2">
                    <input
                      type="text"
                      disabled={!isCardComplete()}
                      placeholder="Type your full legal name"
                      value={typedSignature}
                      onChange={(e) => {
                        setTypedSignature(e.target.value);
                        if (signatureError) setSignatureError('');
                      }}
                      style={
                        signatureError
                          ? { boxShadow: '0 0 18px rgba(239, 68, 68, 0.45)' }
                          : undefined
                      }
                      className={`w-full rounded-xl px-4 py-2 text-xs font-serif italic text-base transition-all duration-300 outline-none ${
                        signatureError
                          ? 'border-2 border-red-500 ring-2 ring-red-500/50 bg-red-950/20 text-red-200'
                          : 'bg-stone-950 border border-stone-800 text-stone-200 focus:border-amber-200/60'
                      }`}
                      /*
                      onChange={(e) => setTypedSignature(e.target.value)}
                      className="w-full bg-stone-950 border border-stone-800 rounded-xl px-4 py-2 text-xs text-stone-200 font-serif italic text-base focus:outline-none focus:border-amber-200/60"
                      */
                    />
                    <label className="flex items-center gap-2 text-[11px] text-stone-400 cursor-pointer">
                      <input
                        type="checkbox"
                        disabled={!isCardComplete()}
                        checked={signatureAgreed}
                        onChange={(e) => {
                          setSignatureAgreed(e.target.checked);
                          if (signatureError) setSignatureError('');
                        }}
                        className="rounded border-stone-700 accent-amber-200 cursor-pointer"
                        />
                      <span>I authorize this transaction and accept this as my signature</span>
                    </label>
                    {signatureError && (
                      <p className="text-[11px] text-rose-400 font-medium">{signatureError}</p>
                    )}
                  </div>
                )}
              </div>

            </div>

            {/* Modal Footer Pay Button */}
            <div className="p-5 border-t border-stone-800 bg-stone-950">
              <button
                type="button"
                onClick={handleFinalPaymentSubmit}
                disabled={isSubmittingOrder}
                className="w-full py-3.5 bg-amber-200 hover:bg-amber-100 disabled:opacity-50 text-stone-950 font-bold rounded-xl text-xs uppercase tracking-wider transition-all cursor-pointer shadow-lg shadow-amber-200/10"
              >
                {isSubmittingOrder
                  ? 'Authorizing Transaction...'
                  : `Authorize & Pay $${finalGrandTotal.toFixed(2)}`}
              </button>
            </div>
          </div>
        </div>
      )}

      {/* ================= PRINTABLE ORDER RECEIPT MODAL ================= */}
     <OrderReceiptModal
       isOpen={isReceiptModalOpen}
       receipt={completedOrderReceipt}
       onClose={() => {
         setIsReceiptModalOpen(false);
         setCurrentView('landing');
       }}
     />

    </div>
  );
}

