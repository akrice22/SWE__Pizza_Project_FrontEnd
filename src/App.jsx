// src/App.jsx
import { useState, useEffect, useRef } from 'react';
import { supabase } from './supabaseClient';
import HomeMenu from './HomeMenu';
import { SIZES, CRUSTS, SAUCE_LEVELS, VEGGIE_OPTIONS, MEAT_OPTIONS, DRINK_OPTIONS } from './menuData';
import OrderReceiptModal from './OrderReceiptModal';
import AuthModal from './AuthModal';
import StaffDashboard from './StaffDashboard';
import CustomerProfileModal from './CustomerProfileModal';
import { getCustomerByPhone, createCustomer } from './api/api';

const DEFAULT_PIZZA = {
  size: SIZES[1],
  crust: CRUSTS[0],
  sauce: SAUCE_LEVELS[1],
  selectedVeggies: [],
  selectedMeats: [MEAT_OPTIONS[0]],
  selectedDrinks: [],
};

export default function App() {
  const [currentView, setCurrentView] = useState('landing');
  const [activeStep, setActiveStep] = useState('details');

  // Authentication & Session States
  const [authModalOpen, setAuthModalOpen] = useState(false);
  const [staffDashboardOpen, setStaffDashboardOpen] = useState(false);
  const [customerProfileOpen, setCustomerProfileOpen] = useState(false);
  const [loggedInRole, setLoggedInRole] = useState(null); // 'customer' | 'staff' | null
  const [allStoreCustomers, setAllStoreCustomers] = useState([]);
  const [allStoreOrders, setAllStoreOrders] = useState([]);
  const [allStaffEmployees, setAllStaffEmployees] = useState([]);
  const [currentStaffRole, setCurrentStaffRole] = useState('owner'); // 'owner' | 'manager' | 'worker'

  // Customer State
  const [customerInfo, setCustomerInfo] = useState({
    name: '',
    phone: '',
    streetAddress: '',
    subdivision: '',
    nearestIntersection: '',
    orderType: 'delivery',
  });
  const [customerId, setCustomerId] = useState(null);

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

  // Payment & Signature States
  const [isCheckoutModalOpen, setIsCheckoutModalOpen] = useState(false);
  const [selectedPaymentMethod, setSelectedPaymentMethod] = useState('credit');
  const [selectedTipPercent, setSelectedTipPercent] = useState(15);
  const [customTipAmount, setCustomTipAmount] = useState('');

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

  const [signatureMode, setSignatureMode] = useState('draw');
  const [typedSignature, setTypedSignature] = useState('');
  const [signatureAgreed, setSignatureAgreed] = useState(false);
  const [hasDrawnSignature, setHasDrawnSignature] = useState(false);
  const [signatureError, setSignatureError] = useState('');
  const canvasRef = useRef(null);
  const isDrawingRef = useRef(false);

  // Receipt Modal State
  const [completedOrderReceipt, setCompletedOrderReceipt] = useState(null);
  const [isReceiptModalOpen, setIsReceiptModalOpen] = useState(false);
  const [isSubmittingOrder, setIsSubmittingOrder] = useState(false);

  // Fetch full store data for staff portal
  const loadStaffStoreData = async () => {
    if (!supabase) {
      console.error('loadStaffStoreData aborted: Supabase client is null.');
      return;
    }

    try {
      console.log('Loading Staff Dashboard store data...');

      // 1. Load Customers
      const { data: custData, error: custErr } = await supabase
        .from('customers')
        .select('*')
        .order('id', { ascending: false });

      const { data: staffData, error: staffErr } = await supabase
        .from('staff_users')
        .select('*')
        .order('id', { ascending: false });

      if (!staffErr && staffData) {
        setAllStaffEmployees(staffData);
      }

      if (custErr) {
        console.error('Error loading customers:', custErr);
      } else {
        setAllStoreCustomers(custData || []);
      }

      // 2. Load Orders
      const { data: ordersData, error: ordersErr } = await supabase
        .from('orders')
        .select(`
          *,
          customers:customer_id (
            first_name,
            last_name,
            phone_number,
            address,
            subdivision
          ),
          order_items (*)
        `)
        .order('id', { ascending: false });

      if (ordersErr) {
        console.error('Error loading orders:', ordersErr);
      } else {
        const formatted = (ordersData || []).map((ord) => ({
          ...ord,
          customer_name: ord.customers
            ? `${ord.customers.first_name || ''} ${ord.customers.last_name || ''}`.trim()
            : 'Walk-In Customer',
          customer_phone: ord.customers?.phone_number || '',
        }));
        setAllStoreOrders(formatted);
      }
    } catch (err) {
      console.error('Unexpected error in loadStaffStoreData:', err);
    }
  };

  // Create New Employee
  const handleCreateEmployee = async (formData) => {
    if (!supabase) return;
    try {
      const { data, error } = await supabase
        .from('staff_users')
        .insert([
          {
            username: formData.username.trim().toLowerCase(),
            password: formData.password,
            role: formData.role,
            full_name: formData.full_name,
            birthdate: formData.birthdate || null,
            phone_number: formData.phone_number,
            email: formData.email,
            date_hired: formData.date_hired || new Date().toISOString().split('T')[0],
            is_active: true,
          },
        ])
        .select();

      if (error) {
        alert(`Error adding employee: ${error.message}`);
        return;
      }

      if (data && data.length > 0) {
        setAllStaffEmployees((prev) => [...prev, data[0]]);
        alert(`${data[0].full_name} enrolled successfully!`);
      }
    } catch (err) {
      console.error('Failed to create employee:', err);
    }
  };

  // Update Existing Employee
  const handleUpdateEmployee = async (empId, formData) => {
    if (!supabase) return;
    try {
      const payload = {
        username: formData.username.trim().toLowerCase(),
        role: formData.role,
        full_name: formData.full_name,
        birthdate: formData.birthdate || null,
        phone_number: formData.phone_number,
        email: formData.email,
        date_hired: formData.date_hired,
      };

      if (formData.password) {
        payload.password = formData.password;
      }

      const { error } = await supabase
        .from('staff_users')
        .update(payload)
        .eq('id', empId);

      if (error) {
        alert(`Failed to update employee: ${error.message}`);
        return;
      }

      setAllStaffEmployees((prev) =>
        prev.map((e) => (e.id === empId ? { ...e, ...payload } : e))
      );
      alert('Employee record updated successfully!');
    } catch (err) {
      console.error('Failed to update employee:', err);
    }
  };

  // Soft-Delete (Archive) or Hard-Delete Employee
  const handleDeleteEmployee = async (empId, isCurrentlyActive) => {
    if (!supabase) return;
    try {
      if (isCurrentlyActive) {
        // Soft delete: mark inactive and record termination timestamp
        const { error } = await supabase
          .from('staff_users')
          .update({
            is_active: false,
            terminated_at: new Date().toISOString(),
          })
          .eq('id', empId);

        if (error) throw error;

        setAllStaffEmployees((prev) =>
          prev.map((e) => (e.id === empId ? { ...e, is_active: false } : e))
        );
        alert('Employee moved to Prior / Terminated Employees archive.');
      } else {
        // Hard delete from archive
        const { error } = await supabase
          .from('staff_users')
          .delete()
          .eq('id', empId);

        if (error) throw error;

        setAllStaffEmployees((prev) => prev.filter((e) => e.id !== empId));
        alert('Employee permanently removed.');
      }
    } catch (err) {
      console.error('Error archiving employee:', err);
      alert(`Action failed: ${err.message}`);
    }
  };

  // Handle updating an existing customer from Staff Dashboard
  const handleUpdateCustomerFromStaff = async (customerIdToUpdate, updatedFields) => {
    if (!supabase) return;

    try {
      const cleanPhone = (updatedFields.phone_number || '').replace(/\D/g, '');
      const payload = {
        first_name: updatedFields.first_name,
        last_name: updatedFields.last_name,
        phone_number: cleanPhone,
        address: updatedFields.address,
        subdivision: updatedFields.subdivision,
        major_intersection: updatedFields.major_intersection,
      };

      const { error } = await supabase
        .from('customers')
        .update(payload)
        .eq('id', customerIdToUpdate);

      if (error) {
        console.error('Error updating customer:', error);
        alert(`Failed to update customer: ${error.message}`);
        return;
      }

      setAllStoreCustomers((prev) =>
        prev.map((c) => (c.id === customerIdToUpdate ? { ...c, ...payload } : c))
      );

      if (customerId === customerIdToUpdate) {
        setCustomerInfo((prev) => ({
          ...prev,
          name: `${payload.first_name} ${payload.last_name}`.trim(),
          phone: formatPhoneNumber(cleanPhone),
          streetAddress: payload.address,
          subdivision: payload.subdivision,
          nearestIntersection: payload.major_intersection,
        }));
      }

      alert('Customer updated successfully!');
    } catch (err) {
      console.error('Failed to update customer:', err);
      alert('An unexpected error occurred while updating customer.');
    }
  };

  // Handle deleting a customer from Staff Dashboard
  const handleDeleteCustomerFromStaff = async (customerIdToDelete) => {
    if (!supabase) return;

    try {
      const { error } = await supabase
        .from('customers')
        .delete()
        .eq('id', customerIdToDelete);

      if (error) {
        console.error('Error deleting customer:', error);
        alert(
          `Cannot delete customer #${customerIdToDelete}: ${error.message}. (They may have active orders linked by foreign key).`
        );
        return;
      }

      setAllStoreCustomers((prev) => prev.filter((c) => c.id !== customerIdToDelete));

      if (customerId === customerIdToDelete) {
        setCustomerId(null);
        setCustomerInfo({
          name: '',
          phone: '',
          streetAddress: '',
          subdivision: '',
          nearestIntersection: '',
          orderType: 'delivery',
        });
      }

      alert('Customer deleted successfully!');
    } catch (err) {
      console.error('Failed to delete customer:', err);
      alert('An unexpected error occurred while deleting customer.');
    }
  };

  const handleAdminLoginSuccess = (adminUser) => {
    setLoggedInRole('staff');
    setCurrentStaffRole(adminUser.role || 'owner');
    loadStaffStoreData();
    setStaffDashboardOpen(true);
  };

  const handleCustomerLoginSuccess = async ({ phone, name }) => {
    setLoggedInRole('customer');
    try {
      const foundCustomer = await getCustomerByPhone(phone);
      if (foundCustomer) {
        setCustomerId(foundCustomer.id);
        setCustomerInfo({
          name: `${foundCustomer.firstName || ''} ${foundCustomer.lastName || ''}`.trim() || name,
          phone: formatPhoneNumber(foundCustomer.phoneNumber || phone),
          streetAddress: foundCustomer.address || '',
          subdivision: foundCustomer.subdivision || '',
          nearestIntersection: foundCustomer.majorIntersection || '',
          orderType: 'delivery',
        });
      } else {
        setCustomerInfo((prev) => ({
          ...prev,
          name: name || prev.name,
          phone: formatPhoneNumber(phone),
        }));
      }

      if (supabase) {
        const { data } = await supabase
          .from('orders')
          .select('*, order_items(*)')
          .order('id', { ascending: false });
        if (data) setPastOrders(data);
      }
    } catch (err) {
      console.error('Customer login lookup error:', err);
    }
    setCustomerProfileOpen(true);
  };

  const handleLogout = () => {
    setLoggedInRole(null);
    setStaffDashboardOpen(false);
    setCustomerProfileOpen(false);
  };

  // Convert raw order into a receipt modal object
  const handleOpenReceiptFromRecord = (record) => {
    setCompletedOrderReceipt({
      orderId: record.id,
      timestamp: record.created_at ? new Date(record.created_at).toLocaleString() : 'Recent Order',
      customer: {
        name: record.customer_name || record.customer?.name || customerInfo.name || 'Valued Guest',
        phone: record.customer_phone || record.customer?.phone || customerInfo.phone || '',
        streetAddress: record.streetAddress || customerInfo.streetAddress || '',
        subdivision: record.subdivision || customerInfo.subdivision || '',
        orderType: record.order_type || 'delivery',
      },
      items: (record.order_items || []).map((it) => ({
        size: { label: it.size },
        crust: { label: it.crust },
        sauce: { label: it.sauce || 'Standard' },
        selectedMeats: (it.toppings || []).map((t) => ({ name: t })),
        selectedVeggies: [],
        selectedDrinks: [],
        price: it.price,
      })),
      paymentMethod: record.payment_method || 'credit',
      subtotal: parseFloat(record.subtotal || 0),
      deliveryFee: parseFloat(record.delivery_fee || 0),
      tax: parseFloat(record.tax || 0),
      tip: parseFloat(record.tip || 0),
      grandTotal: parseFloat(record.total_price || 0),
      signatureMode: 'type',
      typedSignature: record.customer_name || 'Signature on File',
    });
    setIsReceiptModalOpen(true);
  };

  const handleCustomerChange = (e) => {
    const { name, value } = e.target;
    setCustomerInfo((prev) => ({ ...prev, [name]: value }));
  };

  const formatPhoneNumber = (value) => {
    if (!value) return '';
    const digits = value.replace(/\D/g, '').slice(0, 10);
    if (digits.length <= 3) return digits;
    if (digits.length <= 6) return `${digits.slice(0, 3)}-${digits.slice(3)}`;
    return `${digits.slice(0, 3)}-${digits.slice(3, 6)}-${digits.slice(6)}`;
  };

  const handleSearchPhoneChange = (e) => {
    const formatted = formatPhoneNumber(e.target.value);
    setSearchPhone(formatted);
    if (searchPhoneError) setSearchPhoneError('');
  };

  const handleFormPhoneChange = (e) => {
    const formatted = formatPhoneNumber(e.target.value);
    setCustomerInfo((prev) => ({ ...prev, phone: formatted }));
    if (formPhoneError) setFormPhoneError('');
  };

  // Search for existing customer by phone number
// Search for existing customer by phone number
  const handlePhoneLookup = async (e) => {
    e.preventDefault();
    const cleanPhone = (searchPhone || '').replace(/\D/g, '');

    if (cleanPhone.length !== 10) {
      setSearchPhoneError('Please enter a valid 10-digit phone number.');
      return;
    }

    setIsSearchingPhone(true);
    setLookupMessage(null);
    setSearchPhoneError('');

    try {
      if (!supabase) {
        throw new Error('Supabase client is not connected.');
      }

      console.log('--- START PHONE LOOKUP ---', cleanPhone);

      // 1. Find Customer by phone_number
      const { data: foundCustomer, error: custError } = await supabase
        .from('customers')
        .select('*')
        .eq('phone_number', cleanPhone)
        .order('id', { ascending: false })
        .limit(1)
        .maybeSingle();

      if (custError) {
        console.error('Customer lookup query error:', custError);
        throw custError;
      }

      console.log('Customer lookup result:', foundCustomer);

      if (foundCustomer) {
        const resolvedId = foundCustomer.id;
        setCustomerId(resolvedId);
        setIsReturningUser(true);
        setSearchPhoneError('');

        // Pre-fill form fields
        setCustomerInfo((prev) => ({
          ...prev,
          name: `${foundCustomer.first_name || ''} ${foundCustomer.last_name || ''}`.trim(),
          phone: formatPhoneNumber(foundCustomer.phone_number || cleanPhone),
          streetAddress: foundCustomer.address || '',
          subdivision: foundCustomer.subdivision || '',
          nearestIntersection: foundCustomer.major_intersection || '',
        }));

        // 2. Fetch orders for this customer (check both exact ID and string representation)
        const { data: ordersData, error: ordersError } = await supabase
          .from('orders')
          .select('*')
          .or(`customer_id.eq.${resolvedId},customer_id.eq.${Number(resolvedId)}`)
          .order('id', { ascending: false });

        if (ordersError) {
          console.error('Orders query error:', ordersError);
        }

        console.log('Raw orders returned from Supabase:', ordersData);

        let ordersWithItems = [];

        if (ordersData && ordersData.length > 0) {
          const orderIds = ordersData.map((o) => o.id);

          // 3. Fetch order items for these specific orders
          const { data: itemsData, error: itemsError } = await supabase
            .from('order_items')
            .select('*')
            .in('order_id', orderIds);

          if (itemsError) {
            console.error('Order items query error:', itemsError);
          }

          console.log('Raw order items returned from Supabase:', itemsData);

          // 4. Combine line items into orders and normalize toppings
          ordersWithItems = ordersData.map((order) => {
            const relatedItems = (itemsData || [])
              .filter((it) => String(it.order_id) === String(order.id))
              .map((it) => {
                let cleanToppings = [];
                if (Array.isArray(it.toppings)) {
                  cleanToppings = it.toppings.map((t) =>
                    typeof t === 'object' && t !== null ? t.name : String(t)
                  );
                } else if (typeof it.toppings === 'string') {
                  try {
                    const parsed = JSON.parse(it.toppings);
                    cleanToppings = Array.isArray(parsed)
                      ? parsed.map((t) =>
                          typeof t === 'object' && t !== null ? t.name : String(t)
                        )
                      : [it.toppings];
                  } catch {
                    cleanToppings = [it.toppings];
                  }
                }

                return {
                  ...it,
                  toppings: cleanToppings,
                };
              });

            return {
              ...order,
              order_items: relatedItems,
            };
          });
        }

        console.log('Final ordersWithItems set to state:', ordersWithItems);
        setPastOrders(ordersWithItems);

        setLookupMessage({
          type: 'success',
          text: `Welcome back, ${foundCustomer.first_name || 'friend'}! We loaded your details and ${ordersWithItems.length} past order(s).`,
        });
      } else {
        setIsReturningUser(false);
        setPastOrders([]);
        setSearchPhoneError('No account found with this phone number.');
        setLookupMessage({
          type: 'info',
          text: 'No prior profile found with that number. Please fill out your details below to get started!',
        });
      }
    } catch (err) {
      console.error('Phone lookup exception:', err);
      setLookupMessage({
        type: 'error',
        text: 'Unable to check records. Please fill in your details below.',
      });
    } finally {
      setIsSearchingPhone(false);
    }
  };

  const handleReorderPizza = (item) => {
    const rawToppings = Array.isArray(item.toppings) ? item.toppings : [];
    const drinks = rawToppings
      .filter((t) => typeof t === 'string' && t.startsWith('Drink: '))
      .map((t) => ({ name: t.replace('Drink: ', ''), price: 0 }));

    const standardToppings = rawToppings
      .filter((t) => typeof t === 'string' && !t.startsWith('Drink: '))
      .map((t) => ({ name: t, price: 0 }));

    const reorderedPizza = {
      id: Date.now(),
      size: { label: item.size },
      crust: { label: item.crust },
      sauce: { label: item.sauce },
      selectedVeggies: standardToppings,
      selectedMeats: [],
      selectedDrinks: drinks,
      price: parseFloat(item.price).toFixed(2),
    };
    setCart((prev) => [...prev, reorderedPizza]);
    setIsCartOpen(true);
  };

  const handleCustomerSubmit = async (e) => {
    e.preventDefault();
    const rawDigitsOnly = (customerInfo.phone || '').replace(/\D/g, '');

    if (rawDigitsOnly.length !== 10) {
      setFormPhoneError('Please enter a valid 10-digit phone number.');
      alert('Please enter a valid phone number.');
      return;
    }

    setFormPhoneError('');

    if (!supabase) {
      alert('Database connection not configured. Check your .env file.');
      return;
    }

    const nameParts = customerInfo.name.trim().split(' ');
    const firstName = nameParts[0] || '';
    const lastName = nameParts.slice(1).join(' ') || '';

    try {
      let resolvedCustomerId = customerId;

      // 1. Check if customer already exists by phone_number
      const { data: existingCust } = await supabase
        .from('customers')
        .select('*')
        .eq('phone_number', rawDigitsOnly)
        .order('id', { ascending: false })
        .limit(1)
        .maybeSingle();

      if (existingCust) {
        resolvedCustomerId = existingCust.id;

        await supabase
          .from('customers')
          .update({
            first_name: firstName,
            last_name: lastName,
            address: customerInfo.streetAddress || existingCust.address,
            subdivision: customerInfo.subdivision || existingCust.subdivision,
            major_intersection: customerInfo.nearestIntersection || existingCust.major_intersection,
          })
          .eq('id', resolvedCustomerId);

        console.log('Updated existing customer record ID:', resolvedCustomerId);
      } else {
        // 2. Insert new customer record
        const { data: newCust, error: insertError } = await supabase
          .from('customers')
          .insert([
            {
              first_name: firstName,
              last_name: lastName,
              phone_number: rawDigitsOnly,
              address: customerInfo.streetAddress || '',
              subdivision: customerInfo.subdivision || '',
              major_intersection: customerInfo.nearestIntersection || '',
            },
          ])
          .select();

        if (insertError) {
          console.error('Supabase customer insert error:', insertError);
          alert(`Failed to save customer: ${insertError.message}`);
          return;
        }

        if (newCust && newCust.length > 0) {
          resolvedCustomerId = newCust[0].id;
          console.log('Created new customer record ID:', resolvedCustomerId);
        }
      }

      setCustomerId(resolvedCustomerId);
      setActiveStep('builder');
    } catch (err) {
      console.error('Customer submit catch error:', err);
      alert('An unexpected error occurred while saving customer info.');
    }
  };

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

  const toggleDrink = (drink) => {
    setPizzaBuilder((prev) => {
      const exists = (prev.selectedDrinks || []).some((item) => item.id === drink.id);
      return {
        ...prev,
        selectedDrinks: exists
          ? prev.selectedDrinks.filter((item) => item.id !== drink.id)
          : [...(prev.selectedDrinks || []), drink],
      };
    });
  };

  const currentPizzaPrice = (
    (pizzaBuilder.size?.price || 0) +
    (pizzaBuilder.selectedVeggies || []).reduce((sum, v) => sum + (v.price || 0), 0) +
    (pizzaBuilder.selectedMeats || []).reduce((sum, m) => sum + (m.price || 0), 0) +
    (pizzaBuilder.selectedDrinks || []).reduce((sum, d) => sum + (d.price || 0), 0)
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
    setPizzaBuilder({
      ...DEFAULT_PIZZA,
      ...pizza,
      selectedDrinks: pizza.selectedDrinks || [],
    });
    setEditingId(pizza.id);
    setIsCartOpen(false);
    setActiveStep('builder');
  };

  const grandTotal = cart.reduce((sum, item) => sum + parseFloat(item.price), 0).toFixed(2);
  const subtotalNumber = parseFloat(grandTotal) || 0;
  const deliveryFee = customerInfo.orderType === 'delivery' ? 3.99 : 0.0;
  const tax = parseFloat((subtotalNumber * 0.08).toFixed(2));
  const tipAmount =
    selectedTipPercent === 'custom'
      ? parseFloat(customTipAmount) || 0
      : parseFloat(((subtotalNumber * selectedTipPercent) / 100).toFixed(2));
  const finalGrandTotal = parseFloat((subtotalNumber + deliveryFee + tax + tipAmount).toFixed(2));

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
    ctx.strokeStyle = '#fef3c7';
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

  const formatCardNumber = (value) => {
    const raw = value.replace(/\D/g, '').slice(0, 16);
    return raw.replace(/(\d{4})(?=\d)/g, '$1 ');
  };

  const formatExpiry = (value) => {
    const raw = value.replace(/\D/g, '').slice(0, 4);
    if (raw.length <= 2) return raw;
    return `${raw.slice(0, 2)}/${raw.slice(2)}`;
  };

  const isCardComplete = () => {
    if (selectedPaymentMethod === 'cash' || selectedPaymentMethod === 'check') return true;
    if (selectedSavedCardId) return true;

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
      customer_id: customerId,
      nickname: cardForm.nickname.trim() || `Card ending in ${cleanNumber.slice(-4)}`,
      brand: cleanNumber.startsWith('4') ? 'Visa' : cleanNumber.startsWith('5') ? 'Mastercard' : 'Card',
      last_four: cleanNumber.slice(-4),
      cardholder_name: cardForm.cardholderName.trim(),
      expiry: cardForm.expiry,
    };

    try {
      if (customerId && supabase) {
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

  const handleFinalPaymentSubmit = async () => {
    if (cart.length === 0) return;

    if (!supabase) {
      alert('Cannot complete order: Supabase is disconnected. Verify .env file.');
      return;
    }

    if (!customerId) {
      alert('Missing Customer Record: Please return to Step 1 and confirm your customer details.');
      setActiveStep('details');
      setIsCheckoutModalOpen(false);
      return;
    }

    if ((selectedPaymentMethod === 'credit' || selectedPaymentMethod === 'debit') && !isCardComplete()) {
      const cleanNumber = cardForm.cardNumber.replace(/\D/g, '');
      const cleanExpiry = cardForm.expiry.replace(/\D/g, '');
      const cleanCvv = cardForm.cvv.replace(/\D/g, '');

      const errors = {};
      if (cleanNumber.length !== 16) errors.cardNumber = 'Card number must be 16 digits.';
      if (!cardForm.cardholderName.trim()) errors.cardholderName = 'Cardholder name is required.';
      if (cleanExpiry.length !== 4) errors.expiry = 'MM/YY required.';
      if (cleanCvv.length < 3) errors.cvv = '3-4 digits required.';
      errors.general = 'Please complete all required card fields above.';

      setCardFormErrors(errors);
      return;
    }

    if (signatureMode === 'draw' && !hasDrawnSignature) {
      setSignatureError('Please draw your signature before placing the order.');
      return;
    }
    if (signatureMode === 'type' && (!typedSignature.trim() || !signatureAgreed)) {
      setSignatureError('Please type your name and check the consent box.');
      return;
    }

    setSignatureError('');
    setIsSubmittingOrder(true);

    try {
      console.log('Submitting order with customer_id:', customerId);

      // 1. Insert into 'orders'
      const { data: orderData, error: orderError } = await supabase
        .from('orders')
        .insert([
          {
            customer_id: customerId,
            order_type: customerInfo.orderType,
            payment_method: selectedPaymentMethod,
            payment_status:
             selectedPaymentMethod === 'cash'
                ? 'cash_on_delivery'
                : selectedPaymentMethod === 'check'
                ? 'check_on_delivery'
                : 'paid',
            subtotal: subtotalNumber,
            delivery_fee: deliveryFee,
            tax: tax,
            tip: tipAmount,
            total_price: finalGrandTotal,
            status: 'received',
          },
        ])
        .select();

      if (orderError) {
        console.error('Order insert error:', orderError);
        alert(`Order failed: ${orderError.message}`);
        setIsSubmittingOrder(false);
        return;
      }

      const newOrderId = orderData[0].id;
      console.log('Order created with ID:', newOrderId);

      // 2. Insert into 'order_items'
      const orderItemsToInsert = cart.map((pizza) => ({
        order_id: newOrderId,
        size: pizza.size.label,
        crust: pizza.crust.label,
        sauce: pizza.sauce.label,
        toppings: [
          ...(pizza.selectedMeats || []),
          ...(pizza.selectedVeggies || []),
          ...(pizza.selectedDrinks || []).map((d) => ({ name: `Drink: ${d.name}` })),
        ].map((t) => t.name),
        price: parseFloat(pizza.price),
      }));

      const { error: itemsError } = await supabase
        .from('order_items')
        .insert(orderItemsToInsert);

      if (itemsError) {
        console.error('Order items insert error:', itemsError);
      }

      // 3. Capture Signature
      let signatureDataUrl = null;
      if (signatureMode === 'draw' && canvasRef.current) {
        signatureDataUrl = canvasRef.current.toDataURL('image/png');
      }

      // 4. Receipt State
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

      setCart([]);
      setIsCheckoutModalOpen(false);
      setIsReceiptModalOpen(true);
    } catch (err) {
      console.error('Error placing order:', err);
      alert('There was an issue saving your order.');
    } finally {
      setIsSubmittingOrder(false);
    }
  };

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
      {/* Navbar with Login and Portal Triggers */}
      <nav className="sticky top-0 z-40 backdrop-blur-md bg-[#1c1917]/90 border-b border-stone-800/80 transition-all">
        <div className="max-w-6xl mx-auto px-4 sm:px-8 h-20 flex items-center justify-between">
          <div className="flex items-center gap-5 z-10">
            <a href="#instagram" aria-label="Instagram" className="text-stone-300 hover:text-amber-200 transition-all duration-300 hover:scale-125 cursor-pointer">
              <svg className="w-5 h-5 fill-currentColor" viewBox="0 0 24 24"><path d="M12 2.163c3.204 0 3.584.012 4.85.07 3.252.148 4.771 1.691 4.919 4.919.058 1.265.069 1.645.069 4.849 0 3.205-.012 3.584-.069 4.849-.149 3.225-1.664 4.771-4.919 4.919-1.266.058-1.644.07-4.85.07-3.204 0-3.584-.012-4.849-.07-3.26-.149-4.771-1.699-4.919-4.92-.058-1.265-.07-1.644-.07-4.849 0-3.204.013-3.583.07-4.849.149-3.227 1.664-4.771 4.919-4.919 1.266-.057 1.645-.069 4.849-.069zm0-2.163c-3.259 0-3.667.014-4.947.072-4.358.2-6.78 2.618-6.98 6.98-.059 1.281-.073 1.689-.073 4.948 0 3.259.014 3.668.072 4.948.2 4.358 2.618 6.78 6.98 6.98 1.281.058 1.689.072 4.948.072 3.259 0 3.668-.014 4.948-.072 4.354-.2 6.782-2.618 6.979-6.98.059-1.28.073-1.689.073-4.948 0-3.259-.014-3.667-.072-4.947-.196-4.354-2.617-6.78-6.979-6.98-1.281-.059-1.69-.073-4.949-.073zm0 5.838c-3.403 0-6.162 2.759-6.162 6.162s2.759 6.163 6.162 6.163 6.162-2.759 6.162-6.163c0-3.403-2.759-6.162-6.162-6.162zm0 10.162c-2.209 0-4-1.79-4-4 0-2.209 1.791-4 4-4s4 1.791 4 4c0 2.21-1.791 4-4 4zm6.406-11.845c-.796 0-1.441.645-1.441 1.44s.645 1.44 1.441 1.44c.795 0 1.439-.645 1.439-1.44s-.644-1.44-1.439-1.44z"/></svg>
            </a>
            <a href="#facebook" aria-label="Facebook" className="text-stone-300 hover:text-amber-200 transition-all duration-300 hover:scale-125 cursor-pointer">
              <svg className="w-5 h-5 fill-currentColor" viewBox="0 0 24 24"><path d="M24 12.073c0-6.627-5.373-12-12-12s-12 5.373-12 12c0 5.99 4.388 10.954 10.125 11.854v-8.385H7.078v-3.47h3.047V9.43c0-3.007 1.792-4.669 4.533-4.669 1.312 0 2.686.235 2.686.235v2.953H15.83c-1.491 0-1.956.925-1.956 1.874v2.25h3.328l-.532 3.47h-2.796v8.385C19.612 23.027 24 18.062 24 12.073z"/></svg>
            </a>
          </div>

          <div onClick={() => setCurrentView('landing')} className="text-center cursor-pointer group select-none">
            <span className="block font-serif text-xl sm:text-2xl tracking-[0.25em] text-stone-100 group-hover:text-amber-200 transition-colors uppercase font-bold">
              Mom & Pop
            </span>
            <span className="block text-[9px] uppercase tracking-[0.4em] text-stone-400 font-light">
              Pizzeria & Hearth
            </span>
          </div>

          <div className="flex items-center gap-4 sm:gap-6">
            <button type="button" onClick={() => navigateToSection('about-section')} className="hidden sm:inline-block text-xs uppercase tracking-widest text-stone-300 hover:text-amber-200 transition-all duration-300 hover:scale-110 cursor-pointer">
              About
            </button>
            <button type="button" onClick={() => navigateToSection('menu-section')} className="text-xs uppercase tracking-widest text-stone-300 hover:text-amber-200 transition-all duration-300 hover:scale-110 cursor-pointer">
              Menu
            </button>

            {/* Account / Worker Login Action */}
            {loggedInRole === 'staff' ? (
              <button
                type="button"
                onClick={() => {
                  loadStaffStoreData();
                  setStaffDashboardOpen(true);
                }}
                className="text-xs uppercase tracking-widest px-3 py-1.5 rounded-lg bg-emerald-950/60 border border-emerald-700/60 text-emerald-300 font-bold hover:scale-105 transition-all cursor-pointer"
              >
                ⚙️ Staff Portal
              </button>
            ) : loggedInRole === 'customer' ? (
              <button
                type="button"
                onClick={() => setCustomerProfileOpen(true)}
                className="text-xs uppercase tracking-widest px-3 py-1.5 rounded-lg bg-stone-850 border border-stone-700 text-amber-200 font-bold hover:scale-105 transition-all cursor-pointer"
              >
                👤 My Account
              </button>
            ) : (
              <button
                type="button"
                onClick={() => setAuthModalOpen(true)}
                className="text-xs uppercase tracking-widest px-3 py-1.5 rounded-lg bg-stone-850 hover:bg-stone-800 border border-stone-700 text-stone-200 hover:text-amber-200 font-bold hover:scale-105 transition-all cursor-pointer"
              >
                Log In
              </button>
            )}

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

      {/* Main App Content */}
      <main className="max-w-5xl mx-auto px-4 sm:px-6 pt-6">
        {currentView === 'landing' ? (
          <HomeMenu
            onOrderNow={() => {
              setCurrentView('order');
              setActiveStep('details');
            }}
          />
        ) : (
          <div className="space-y-6 animate-in fade-in duration-300">
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

            <div className="relative overflow-hidden w-full min-h-[640px]">
              <div
                className={`flex w-[200%] transition-transform duration-700 ease-[cubic-bezier(0.25,1,0.5,1)] ${
                  activeStep === 'details' ? 'translate-x-0' : '-translate-x-1/2'
                }`}
              >
                {/* Step 1: Customer Details */}
                <div className="w-1/2 pr-4">
                  <div className="bg-stone-900/90 border border-stone-800 rounded-3xl p-6 sm:p-10 shadow-2xl backdrop-blur-md space-y-8">
                    <div className="border-b border-stone-800 pb-4">
                      <span className="text-[11px] font-mono tracking-widest uppercase text-amber-200/80">Step 01</span>
                      <h3 className="text-2xl font-serif uppercase tracking-wider text-stone-100">Delivery & Customer Info</h3>
                      <p className="text-xs text-stone-400 mt-1">Returning customer? Enter your phone number below to retrieve your information.</p>
                    </div>

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
                          className={`flex-1 rounded-xl px-4 py-2.5 text-xs text-stone-100 placeholder-stone-500 transition-all duration-300 outline-none ${
                            searchPhoneError
                              ? 'bg-rose-950/30 border-2 border-rose-500 ring-2 ring-rose-500/40 shadow-[0_0_15px_rgba(244,63,94,0.3)]'
                              : 'bg-stone-900 border border-stone-700/80 focus:border-amber-300 focus:ring-2 focus:ring-amber-400/40'
                          }`}
                        />
                        <button
                          type="submit"
                          disabled={isSearchingPhone}
                          className="px-5 py-2.5 bg-stone-800 hover:bg-stone-700 text-amber-200 text-xs font-semibold rounded-xl border border-stone-700 hover:border-amber-200/40 transition-all cursor-pointer whitespace-nowrap disabled:opacity-50"
                        >
                          {isSearchingPhone ? 'Searching...' : 'Find My Info'}
                        </button>
                      </div>

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

                    {/* Past Order History Section */}
                    {isReturningUser && (
                    <div className="p-5 rounded-2xl bg-amber-950/15 border border-amber-900/40 space-y-4">
                      <div className="flex justify-between items-center">
                        <h4 className="font-serif uppercase tracking-wider text-amber-100 text-sm">
                          📜 Past Favorites ({pastOrders.length} previous {pastOrders.length === 1 ? 'order' : 'orders'})
                        </h4>
                        <span className="text-[10px] font-mono uppercase tracking-widest text-amber-300/80">
                          Reorder with 1-click
                        </span>
                      </div>

                      {pastOrders.length > 0 ? (
                        <div className="space-y-3 max-h-64 overflow-y-auto pr-1">
                          {pastOrders.map((order) => (
                            <div
                              key={order.id}
                              className="p-3.5 rounded-xl bg-stone-900/90 border border-stone-800/80 space-y-2.5 text-xs"
                            >
                              <div className="flex justify-between items-center text-stone-400 font-mono text-[11px] border-b border-stone-800/60 pb-1.5">
                                <span>
                                  Order #{order.id} • {order.created_at ? new Date(order.created_at).toLocaleDateString() : 'Recent'}
                                </span>
                                <span className="text-amber-200 font-bold">
                                  ${parseFloat(order.total_price || 0).toFixed(2)}
                                </span>
                              </div>

                              {/* List of items inside this past order */}
                              <div className="space-y-2">
                                {order.order_items && order.order_items.length > 0 ? (
                                  order.order_items.map((item, idx) => (
                                    <div
                                      key={item.id || idx}
                                      className="flex justify-between items-center gap-3 bg-stone-950/40 p-2.5 rounded-lg border border-stone-800/40"
                                    >
                                      <div className="min-w-0 flex-1">
                                        <div className="font-semibold text-stone-200 truncate">
                                          {item.size} • {item.crust}
                                        </div>
                                        <div className="text-[11px] text-stone-400 mt-0.5 space-y-0.5">
                                          <div>Sauce: {item.sauce || 'Standard'}</div>
                                          {Array.isArray(item.toppings) && item.toppings.length > 0 && (
                                            <div className="text-stone-300 truncate">
                                              Toppings: {item.toppings.join(', ')}
                                            </div>
                                          )}
                                        </div>
                                      </div>
                                      <div className="flex flex-col items-end gap-1 shrink-0">
                                        <span className="font-mono text-amber-200 font-semibold text-xs">
                                          ${parseFloat(item.price || 0).toFixed(2)}
                                        </span>
                                        <button
                                          type="button"
                                          onClick={() => handleReorderPizza(item)}
                                          className="px-3 py-1 rounded-lg bg-amber-200/10 hover:bg-amber-200/20 text-amber-200 border border-amber-200/30 text-[10px] font-bold uppercase tracking-wider hover:scale-105 active:scale-95 transition-all cursor-pointer whitespace-nowrap"
                                        >
                                          + Re-order
                                        </button>
                                      </div>
                                    </div>
                                  ))
                                ) : (
                                  <div className="text-[11px] text-stone-500 italic">
                                    No item breakdown saved for this order.
                                  </div>
                                )}
                              </div>
                            </div>
                          ))}
                        </div>
                      ) : (
                        <div className="text-stone-400 text-xs italic bg-stone-900/50 p-3 rounded-xl border border-stone-800">
                          Profile found, but no prior orders are linked to this customer account yet.
                        </div>
                      )}
                    </div>
                  )}

                    <form onSubmit={handleCustomerSubmit} className="space-y-5">
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
                                : 'bg-stone-950 border border-stone-800 focus:border-amber-300 focus:ring-2 focus:ring-amber-400/40'
                            }`}
                          />
                          {formPhoneError && (
                            <p className="text-[11px] text-rose-400 font-medium mt-1">{formPhoneError}</p>
                          )}
                        </div>
                      </div>

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

                      <div className="pt-4 flex flex-col sm:flex-row gap-3">
                        <button
                          type="submit"
                          className="flex-1 py-3.5 bg-amber-200 hover:bg-amber-100 text-stone-950 font-bold rounded-xl text-xs uppercase tracking-wider transition-all duration-300 hover:scale-[1.02] active:scale-95 cursor-pointer shadow-lg shadow-amber-200/10"
                        >
                          {isReturningUser ? 'Confirm Details & Build Custom Order →' : 'Save Details & Start Custom Order →'}
                        </button>
                      </div>
                    </form>
                  </div>
                </div>

                {/* Step 2: Pizza Customizer */}
                <div className="w-1/2 pl-4">
                  <div className="bg-stone-900/80 border border-stone-800 rounded-3xl p-6 sm:p-10 shadow-2xl backdrop-blur-md space-y-6">
                    <div className="flex items-center justify-between border-b border-stone-800 pb-5">
                      <div>
                        <h2 className="text-2xl font-serif uppercase tracking-wider text-stone-100 flex items-center gap-3">
                          <span className="flex h-7 w-7 items-center justify-center rounded-lg bg-amber-200 text-stone-950 text-xs font-bold font-mono">
                            2
                          </span>
                          {editingId ? 'Edit Item' : 'Build Custom Pie & Drinks'}
                        </h2>
                        <p className="text-stone-400 text-xs mt-1">Select crust, base, toppings, and craft beverages.</p>
                      </div>
                      <button
                        type="button"
                        onClick={() => setActiveStep('details')}
                        className="text-xs uppercase tracking-wider text-stone-400 hover:text-stone-100 border border-stone-800 hover:border-stone-600 rounded-xl px-3 py-1.5 transition-all cursor-pointer"
                      >
                        ← Edit Info
                      </button>
                    </div>

                    {/* Crust / Size / Sauce */}
                    <div className="grid grid-cols-1 sm:grid-cols-3 gap-3">
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

                    {/* Toppings Selection */}
                    <div className="space-y-3">
                      <h3 className="text-xs font-bold uppercase tracking-widest text-stone-300">4. Toppings Selection</h3>
                      <div className="grid grid-cols-1 md:grid-cols-2 gap-4">
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

                    {/* Drinks Selection */}
                    <div className="space-y-3">
                      <div className="flex justify-between items-center">
                        <h3 className="text-xs font-bold uppercase tracking-widest text-stone-300">
                          5. Pair with Refreshing Drinks
                        </h3>
                        <span className="text-[10px] text-amber-200 font-mono">Optional</span>
                      </div>
                      <div className="grid grid-cols-1 sm:grid-cols-2 md:grid-cols-3 gap-2.5">
                        {DRINK_OPTIONS.map((drink) => {
                          const isSelected = (pizzaBuilder.selectedDrinks || []).some((d) => d.id === drink.id);
                          return (
                            <div
                              key={drink.id}
                              onClick={() => toggleDrink(drink)}
                              className={`p-3 rounded-xl border transition-all cursor-pointer flex flex-col justify-between gap-2 ${
                                isSelected
                                  ? 'bg-amber-950/40 border-amber-400/80 text-amber-100 ring-1 ring-amber-400/30'
                                  : 'bg-stone-950/60 border-stone-800 text-stone-300 hover:border-stone-700'
                              }`}
                            >
                              <div className="space-y-0.5">
                                <span className="font-semibold text-xs block leading-snug">{drink.name}</span>
                                <span className="text-[10px] text-stone-400 block font-light">{drink.detail}</span>
                              </div>
                              <div className="flex justify-between items-center pt-1 border-t border-stone-800/60">
                                <span className="font-mono text-xs text-amber-200 font-bold">+${drink.price.toFixed(2)}</span>
                                <span className="text-[10px] font-bold uppercase tracking-wider text-stone-400">
                                  {isSelected ? '✓ Added' : '+ Add'}
                                </span>
                              </div>
                            </div>
                          );
                        })}
                      </div>
                    </div>

                    {/* Footer */}
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
                        {editingId ? 'Update Item in Order' : 'Add to Order'}
                      </button>
                    </div>
                  </div>
                </div>
              </div>
            </div>
          </div>
        )}
      </main>

      {/* Cart Popup */}
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
                      {((pizza.selectedVeggies && pizza.selectedVeggies.length > 0) || (pizza.selectedMeats && pizza.selectedMeats.length > 0)) && (
                        <div className="mt-0.5 text-stone-300">
                          Toppings: {[...(pizza.selectedMeats || []), ...(pizza.selectedVeggies || [])].map((t) => t.name).join(', ')}
                        </div>
                      )}
                      {pizza.selectedDrinks && pizza.selectedDrinks.length > 0 && (
                        <div className="mt-0.5 text-amber-200/90 font-medium">
                          Drinks: {pizza.selectedDrinks.map((d) => d.name).join(', ')}
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

      {/* Checkout Modal */}
      {isCheckoutModalOpen && (
        <div className="fixed inset-0 z-50 flex items-center justify-center p-4 bg-black/85 backdrop-blur-md animate-in fade-in duration-200 overflow-y-auto">
          <div className="bg-stone-900 border border-stone-800 rounded-3xl max-w-lg w-full max-h-[92vh] shadow-2xl flex flex-col overflow-hidden my-auto">
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

            <div className="p-6 overflow-y-auto space-y-6 text-xs text-stone-300">
              <div className="space-y-2">
                <label className="block text-[11px] font-bold uppercase tracking-wider text-amber-200/90">
                  Select Payment Method
                </label>
                <div className="grid grid-cols-4 gap-2">
                  {[
                    { id: 'credit', label: '💳 Credit' },
                    { id: 'debit', label: '🏧 Debit' },
                    { id: 'cash', label: '💵 Cash' },
                    { id: 'check', label: '📝 Check' },
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
                              <span className="font-semibold text-xs text-stone-200 block">{card.nickname}</span>
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
                    <div className="space-y-3 pt-1">
                      {cardFormErrors.general && (
                        <div className="p-3 rounded-xl bg-rose-950/40 border border-rose-500/80 text-rose-300 text-xs flex items-center gap-2">
                          <span>⚠️</span>
                          <span>{cardFormErrors.general}</span>
                        </div>
                      )}

                      <div>
                        <label className="block text-[10px] uppercase tracking-wider text-stone-400 mb-1">
                          Card Nickname
                        </label>
                        <input
                          type="text"
                          placeholder="e.g. Primary Card"
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
                          className={`w-full bg-stone-900 rounded-xl px-3.5 py-2 text-xs text-stone-200 font-mono focus:outline-none ${
                            cardFormErrors.cardNumber ? 'border border-red-500' : 'border border-stone-800 focus:border-amber-200/60'
                          }`}
                        />
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
                          className={`w-full bg-stone-900 rounded-xl px-3.5 py-2 text-xs text-stone-200 focus:outline-none ${
                            cardFormErrors.cardholderName ? 'border border-red-500' : 'border border-stone-800 focus:border-amber-200/60'
                          }`}
                        />
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
                            className={`w-full bg-stone-900 rounded-xl px-3.5 py-2 text-xs text-stone-200 font-mono focus:outline-none ${
                              cardFormErrors.expiry ? 'border border-red-500' : 'border border-stone-800 focus:border-amber-200/60'
                            }`}
                          />
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
                            className={`w-full bg-stone-900 rounded-xl px-3.5 py-2 text-xs text-stone-200 font-mono focus:outline-none ${
                              cardFormErrors.cvv ? 'border border-red-500' : 'border border-stone-800 focus:border-amber-200/60'
                            }`}
                          />
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

              {/* Tips */}
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

              {/* Financial Breakdown */}
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

              {/* Signature Pad */}
              <div className="relative space-y-3 pt-2 border-t border-stone-800">
                {!isCardComplete() && (
                  <div className="absolute inset-0 z-10 bg-stone-900/85 backdrop-blur-[2px] rounded-2xl flex flex-col items-center justify-center p-4 text-center border border-dashed border-stone-700">
                    <span className="text-xl mb-1">🔒</span>
                    <p className="text-xs font-semibold text-amber-200/90">Card Details Required</p>
                    <p className="text-[10px] text-stone-400 mt-0.5">Please complete card details or choose a saved card.</p>
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
                    <div className="relative rounded-2xl bg-stone-950 overflow-hidden touch-none border border-stone-700/80">
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
                      {signatureError && <p className="text-[11px] text-rose-400 font-medium">{signatureError}</p>}
                      <button
                        type="button"
                        onClick={clearCanvasSignature}
                        className="text-[10px] text-stone-500 hover:text-stone-300 cursor-pointer ml-auto"
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
                      className="w-full bg-stone-950 border border-stone-800 rounded-xl px-4 py-2 text-xs text-stone-200 font-serif italic text-base focus:border-amber-200/60"
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
                    {signatureError && <p className="text-[11px] text-rose-400 font-medium">{signatureError}</p>}
                  </div>
                )}
              </div>
            </div>

            <div className="p-5 border-t border-stone-800 bg-stone-950">
              <button
                type="button"
                onClick={handleFinalPaymentSubmit}
                disabled={isSubmittingOrder}
                className="w-full py-3.5 bg-amber-200 hover:bg-amber-100 disabled:opacity-50 text-stone-950 font-bold rounded-xl text-xs uppercase tracking-wider transition-all cursor-pointer shadow-lg shadow-amber-200/10"
              >
                {isSubmittingOrder ? 'Authorizing Transaction...' : `Authorize & Pay $${finalGrandTotal.toFixed(2)}`}
              </button>
            </div>
          </div>
        </div>
      )}

      {/* Auth Modal (Staff & Customer) */}
      <AuthModal
        isOpen={authModalOpen}
        onClose={() => setAuthModalOpen(false)}
        onAdminLoginSuccess={handleAdminLoginSuccess}
        onCustomerLoginSuccess={handleCustomerLoginSuccess}
      />

      {/* Staff & Worker Terminal */}
      <StaffDashboard
        isOpen={staffDashboardOpen}
        onClose={() => setStaffDashboardOpen(false)}
        customers={allStoreCustomers}
        orders={allStoreOrders}
        employees={allStaffEmployees}
        currentStaffRole={currentStaffRole}
        onViewReceipt={handleOpenReceiptFromRecord}
        onUpdateCustomer={handleUpdateCustomerFromStaff}
        onDeleteCustomer={handleDeleteCustomerFromStaff}
        onCreateEmployee={handleCreateEmployee}
        onUpdateEmployee={handleUpdateEmployee}
        onDeleteEmployee={handleDeleteEmployee}
        onLogout={handleLogout}
      />

      {/* Customer Account & Order History Portal */}
      <CustomerProfileModal
        isOpen={customerProfileOpen}
        onClose={() => setCustomerProfileOpen(false)}
        customerInfo={customerInfo}
        onUpdateCustomer={(updated) => setCustomerInfo(updated)}
        pastOrders={pastOrders}
        onViewReceipt={handleOpenReceiptFromRecord}
        onReorder={(order) => {
          if (order.order_items?.length > 0) {
            order.order_items.forEach((it) => handleReorderPizza(it));
          }
          setCustomerProfileOpen(false);
        }}
        onLogout={handleLogout}
      />

      {/* Printable Receipt Modal */}
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