// src/StaffDashboard.jsx
import { useState, useMemo } from 'react';

export default function StaffDashboard({
  isOpen,
  onClose,
  customers = [],
  orders = [],
  employees = [],
  currentStaffRole = 'owner', // 'owner' | 'manager' | 'worker'
  onViewReceipt,
  onUpdateCustomer,
  onDeleteCustomer,
  onCreateEmployee,
  onUpdateEmployee,
  onDeleteEmployee,
  onLogout,
}) {
  const [activeTab, setActiveTab] = useState('orders'); // 'orders' | 'customers' | 'employees'
  const [searchTerm, setSearchTerm] = useState('');
  const [paymentFilter, setPaymentFilter] = useState('all');
  const [orderTypeFilter, setOrderTypeFilter] = useState('all');
  const [dateFilter, setDateFilter] = useState('');

  // Employee View Filters & Forms
  const [employeeStatusTab, setEmployeeStatusTab] = useState('active'); // 'active' | 'archived'
  const [isEmployeeModalOpen, setIsEmployeeModalOpen] = useState(false);
  const [editingEmployeeId, setEditingEmployeeId] = useState(null);
  const [employeeForm, setEmployeeForm] = useState({
    username: '',
    password: '',
    full_name: '',
    role: 'worker',
    birthdate: '',
    phone_number: '',
    email: '',
    date_hired: new Date().toISOString().split('T')[0],
  });

  // Customer Edit State
  const [editingCustomer, setEditingCustomer] = useState(null);
  const [customerEditForm, setCustomerEditForm] = useState({
    first_name: '',
    last_name: '',
    phone_number: '',
    address: '',
    subdivision: '',
    major_intersection: '',
  });

  // Password visibility set for staff table
  const [visiblePasswords, setVisiblePasswords] = useState(new Set());

  const togglePasswordVisibility = (empId) => {
    setVisiblePasswords((prev) => {
      const next = new Set(prev);
      if (next.has(empId)) {
        next.delete(empId);
      } else {
        next.add(empId);
      }
      return next;
    });
  };

  // Safe Array Guards
  const safeOrders = Array.isArray(orders) ? orders : [];
  const safeCustomers = Array.isArray(customers) ? customers : [];
  const safeEmployees = Array.isArray(employees) ? employees : [];

  // Filter Orders
  const filteredOrders = useMemo(() => {
    return safeOrders.filter((o) => {
      const orderIdMatch = String(o.id || '').toLowerCase().includes(searchTerm.toLowerCase());
      const custNameMatch = (o.customer_name || o.customer?.name || '')
        .toLowerCase()
        .includes(searchTerm.toLowerCase());
      const phoneMatch = (o.customer_phone || o.customer?.phone || '')
        .replace(/\D/g, '')
        .includes(searchTerm.replace(/\D/g, ''));

      const matchesSearch = !searchTerm || orderIdMatch || custNameMatch || phoneMatch;
      const matchesPayment = paymentFilter === 'all' || o.payment_method === paymentFilter;
      const matchesType = orderTypeFilter === 'all' || o.order_type === orderTypeFilter;
      const matchesDate = !dateFilter || (o.created_at && o.created_at.startsWith(dateFilter));

      return matchesSearch && matchesPayment && matchesType && matchesDate;
    });
  }, [safeOrders, searchTerm, paymentFilter, orderTypeFilter, dateFilter]);

  // Unique Customers Deduplication
  const uniqueCustomers = useMemo(() => {
    const seen = new Set();
    const result = [];
    for (const c of safeCustomers) {
      const cleanPhone = (c.phone_number || c.phoneNumber || c.phone || '').replace(/\D/g, '');
      const key = cleanPhone || (c.address || c.id || '').toString().trim();
      if (key && !seen.has(key)) {
        seen.add(key);
        result.push(c);
      }
    }
    return result;
  }, [safeCustomers]);

  const filteredCustomers = useMemo(() => {
    return uniqueCustomers.filter((c) => {
      const name = `${c.first_name || ''} ${c.last_name || ''} ${c.name || ''}`.toLowerCase();
      const phone = (c.phone_number || '').replace(/\D/g, '');
      const addr = (c.address || '').toLowerCase();
      const term = searchTerm.toLowerCase();

      return name.includes(term) || phone.includes(searchTerm.replace(/\D/g, '')) || addr.includes(term);
    });
  }, [uniqueCustomers, searchTerm]);

  // Filter Employees (Active vs Former)
  const filteredEmployees = useMemo(() => {
    return safeEmployees
      .filter((emp) => {
        const isActive = emp.is_active !== false;
        return employeeStatusTab === 'active' ? isActive : !isActive;
      })
      .filter((emp) => {
        const term = searchTerm.toLowerCase();
        const nameMatch = (emp.full_name || '').toLowerCase().includes(term);
        const usernameMatch = (emp.username || '').toLowerCase().includes(term);
        const roleMatch = (emp.role || '').toLowerCase().includes(term);
        const phoneMatch = (emp.phone_number || '').replace(/\D/g, '').includes(searchTerm.replace(/\D/g, ''));
        const emailMatch = (emp.email || '').toLowerCase().includes(term);

        return !searchTerm || nameMatch || usernameMatch || roleMatch || phoneMatch || emailMatch;
      });
  }, [safeEmployees, employeeStatusTab, searchTerm]);

  const totalRevenue = useMemo(() => {
    return filteredOrders.reduce((sum, o) => sum + parseFloat(o.total_price || 0), 0);
  }, [filteredOrders]);

  const totalTips = useMemo(() => {
    return filteredOrders.reduce((sum, o) => sum + parseFloat(o.tip || 0), 0);
  }, [filteredOrders]);

  const totalTax = useMemo(() => {
    return filteredOrders.reduce((sum, o) => sum + parseFloat(o.tax || 0), 0);
  }, [filteredOrders]);

  // Rules of Hooks guard: return null AFTER all hooks are defined
  if (!isOpen) return null;

  // Password generator helper
  const generateRandomPassword = () => {
    const chars = 'ABCDEFGHJKLMNPQRSTUVWXYZabcdefghijkmnpqrstuvwxyz23456789!@#$';
    let pass = '';
    for (let i = 0; i < 8; i++) {
      pass += chars.charAt(Math.floor(Math.random() * chars.length));
    }
    setEmployeeForm((prev) => ({ ...prev, password: pass }));
  };

  // Employee Form Handlers
  const handleOpenAddEmployee = () => {
    setEditingEmployeeId(null);
    setEmployeeForm({
      username: '',
      password: '',
      full_name: '',
      role: 'worker',
      birthdate: '',
      phone_number: '',
      email: '',
      date_hired: new Date().toISOString().split('T')[0],
    });
    setIsEmployeeModalOpen(true);
  };

  const handleOpenEditEmployee = (emp) => {
    setEditingEmployeeId(emp.id);
    setEmployeeForm({
      username: emp.username || '',
      password: emp.password || '',
      full_name: emp.full_name || '',
      role: emp.role || 'worker',
      birthdate: emp.birthdate || '',
      phone_number: emp.phone_number || '',
      email: emp.email || '',
      date_hired: emp.date_hired || '',
    });
    setIsEmployeeModalOpen(true);
  };

  const handleSaveEmployeeForm = async (e) => {
    e.preventDefault();
    if (editingEmployeeId) {
      if (onUpdateEmployee) await onUpdateEmployee(editingEmployeeId, employeeForm);
    } else {
      if (onCreateEmployee) await onCreateEmployee(employeeForm);
    }
    setIsEmployeeModalOpen(false);
  };

  const handleDeleteOrArchiveEmployee = async (emp) => {
    const actionLabel = emp.is_active !== false ? 'archive' : 'permanently remove';
    const confirmMsg = `Are you sure you want to ${actionLabel} ${emp.full_name || emp.username}?`;
    if (window.confirm(confirmMsg) && onDeleteEmployee) {
      await onDeleteEmployee(emp.id, emp.is_active !== false);
    }
  };

  // Customer Edit Handlers
  const handleStartEditCustomer = (customer) => {
    setEditingCustomer(customer);
    setCustomerEditForm({
      first_name: customer.first_name || '',
      last_name: customer.last_name || '',
      phone_number: customer.phone_number || customer.phone || '',
      address: customer.address || customer.streetAddress || '',
      subdivision: customer.subdivision || '',
      major_intersection: customer.major_intersection || customer.nearestIntersection || '',
    });
  };

  const handleSaveCustomerEdit = async (e) => {
    e.preventDefault();
    if (!editingCustomer || !onUpdateCustomer) return;
    await onUpdateCustomer(editingCustomer.id, customerEditForm);
    setEditingCustomer(null);
  };

  const handleDeleteCustomerPrompt = async (customer) => {
    const fullName = `${customer.first_name || ''} ${customer.last_name || ''}`.trim() || 'this customer';
    const confirmed = window.confirm(
      `Are you sure you want to delete ${fullName} (#${customer.id})? Note: Linked historical orders may restrict deletion.`
    );
    if (confirmed && onDeleteCustomer) {
      await onDeleteCustomer(customer.id);
    }
  };

  return (
    <div className="fixed inset-0 z-50 flex items-center justify-center p-2 sm:p-4 bg-black/90 backdrop-blur-md animate-in fade-in duration-200 overflow-y-auto">
      <div className="bg-stone-900 border border-stone-800 rounded-3xl max-w-6xl w-full max-h-[95vh] shadow-2xl flex flex-col overflow-hidden my-auto print:max-h-none print:border-none print:shadow-none print:bg-white print:text-black">
        
        {/* Navigation & Header */}
        <div className="p-5 border-b border-stone-800 flex flex-wrap justify-between items-center gap-4 bg-stone-950 print:hidden">
          <div>
            <div className="flex items-center gap-2">
              <span className="w-2.5 h-2.5 rounded-full bg-emerald-400 animate-pulse" />
              <span className="text-[10px] font-mono uppercase tracking-widest text-emerald-400">
                Staff & Owner Terminal ({currentStaffRole.toUpperCase()})
              </span>
            </div>
            <h2 className="text-xl font-serif uppercase tracking-wider text-stone-100">
              Mom & Pop Management Dashboard
            </h2>
          </div>

          <div className="flex items-center gap-2">
            <button
              type="button"
              onClick={() => window.print()}
              className="px-3.5 py-2 bg-stone-800 hover:bg-stone-700 text-stone-200 text-xs font-semibold rounded-xl border border-stone-700 transition-all cursor-pointer flex items-center gap-1.5"
            >
              🖨️ Print Report
            </button>
            <button
              type="button"
              onClick={onLogout}
              className="px-3 py-2 bg-rose-950/40 hover:bg-rose-900/60 border border-rose-800/60 text-rose-300 text-xs font-semibold rounded-xl transition-all cursor-pointer"
            >
              Lock Terminal
            </button>
            <button
              type="button"
              onClick={onClose}
              className="text-stone-400 hover:text-white transition-colors cursor-pointer text-xs ml-2"
            >
              ✕
            </button>
          </div>
        </div>

        {/* Tab & Filter Bar */}
        <div className="p-4 bg-stone-950/60 border-b border-stone-800 space-y-4 print:hidden">
          <div className="flex flex-wrap justify-between items-center gap-3">
            <div className="flex gap-2">
              <button
                type="button"
                onClick={() => setActiveTab('orders')}
                className={`px-4 py-2 rounded-xl text-xs font-bold uppercase tracking-wider transition-all cursor-pointer ${
                  activeTab === 'orders'
                    ? 'bg-amber-200 text-stone-950 shadow-md'
                    : 'bg-stone-900 border border-stone-800 text-stone-400 hover:text-stone-200'
                }`}
              >
                Transactions ({filteredOrders.length})
              </button>
              <button
                type="button"
                onClick={() => setActiveTab('customers')}
                className={`px-4 py-2 rounded-xl text-xs font-bold uppercase tracking-wider transition-all cursor-pointer ${
                  activeTab === 'customers'
                    ? 'bg-amber-200 text-stone-950 shadow-md'
                    : 'bg-stone-900 border border-stone-800 text-stone-400 hover:text-stone-200'
                }`}
              >
                Customers ({filteredCustomers.length})
              </button>

              {/* Staff & Workers tab visible to owners and managers */}
              {(currentStaffRole === 'owner' || currentStaffRole === 'manager') && (
                <button
                  type="button"
                  onClick={() => setActiveTab('employees')}
                  className={`px-4 py-2 rounded-xl text-xs font-bold uppercase tracking-wider transition-all cursor-pointer ${
                    activeTab === 'employees'
                      ? 'bg-amber-200 text-stone-950 shadow-md'
                      : 'bg-stone-900 border border-stone-800 text-stone-400 hover:text-stone-200'
                  }`}
                >
                  Staff & Workers ({safeEmployees.length})
                </button>
              )}
            </div>

            {/* Quick Metrics */}
            {activeTab === 'orders' && (
              <div className="flex items-center gap-4 text-xs font-mono bg-stone-900/90 border border-stone-800 rounded-xl px-4 py-2">
                <div>
                  <span className="text-stone-500 uppercase text-[10px] block">Revenue</span>
                  <span className="text-amber-200 font-bold">${totalRevenue.toFixed(2)}</span>
                </div>
                <div className="border-l border-stone-800 pl-3">
                  <span className="text-stone-500 uppercase text-[10px] block">Tips</span>
                  <span className="text-emerald-400 font-bold">${totalTips.toFixed(2)}</span>
                </div>
                <div className="border-l border-stone-800 pl-3">
                  <span className="text-stone-500 uppercase text-[10px] block">Tax</span>
                  <span className="text-stone-300 font-bold">${totalTax.toFixed(2)}</span>
                </div>
              </div>
            )}
          </div>

          {/* Search Inputs and Controls */}
          <div className="flex flex-wrap items-center justify-between gap-3">
            <div className="flex-1 max-w-sm">
              <input
                type="text"
                placeholder={
                  activeTab === 'employees'
                    ? 'Search staff by name, role, email...'
                    : 'Search records...'
                }
                value={searchTerm}
                onChange={(e) => setSearchTerm(e.target.value)}
                className="w-full bg-stone-900 border border-stone-800 rounded-xl px-3.5 py-2 text-xs text-stone-200 placeholder-stone-500 focus:outline-none focus:border-amber-200/60"
              />
            </div>

            {/* Order Filter Selectors */}
            {activeTab === 'orders' && (
              <div className="flex flex-wrap items-center gap-2 text-xs">
                <select
                  value={paymentFilter}
                  onChange={(e) => setPaymentFilter(e.target.value)}
                  className="bg-stone-900 border border-stone-800 rounded-xl px-3 py-2 text-stone-300 focus:outline-none focus:border-amber-200/60"
                >
                  <option value="all">All Payment Methods</option>
                  <option value="credit">Credit Card</option>
                  <option value="debit">Debit Card</option>
                  <option value="cash">Cash</option>
                  <option value="check">Check</option>
                </select>

                <select
                  value={orderTypeFilter}
                  onChange={(e) => setOrderTypeFilter(e.target.value)}
                  className="bg-stone-900 border border-stone-800 rounded-xl px-3 py-2 text-stone-300 focus:outline-none focus:border-amber-200/60"
                >
                  <option value="all">All Types</option>
                  <option value="delivery">Delivery</option>
                  <option value="pickup">Pickup</option>
                </select>

                <input
                  type="date"
                  value={dateFilter}
                  onChange={(e) => setDateFilter(e.target.value)}
                  className="bg-stone-900 border border-stone-800 rounded-xl px-3 py-2 text-stone-300 focus:outline-none focus:border-amber-200/60"
                />
              </div>
            )}

            {/* Employee Active vs. Prior Archive Switcher */}
            {activeTab === 'employees' && (
              <div className="flex items-center gap-3">
                <div className="flex bg-stone-900 border border-stone-800 p-1 rounded-xl text-xs">
                  <button
                    type="button"
                    onClick={() => setEmployeeStatusTab('active')}
                    className={`px-3 py-1 rounded-lg font-bold transition-all ${
                      employeeStatusTab === 'active'
                        ? 'bg-amber-200 text-stone-950'
                        : 'text-stone-400 hover:text-stone-200'
                    }`}
                  >
                    Active Staff
                  </button>
                  <button
                    type="button"
                    onClick={() => setEmployeeStatusTab('archived')}
                    className={`px-3 py-1 rounded-lg font-bold transition-all ${
                      employeeStatusTab === 'archived'
                        ? 'bg-amber-200 text-stone-950'
                        : 'text-stone-400 hover:text-stone-200'
                    }`}
                  >
                    Prior / Terminated
                  </button>
                </div>

                {currentStaffRole === 'owner' && (
                  <button
                    type="button"
                    onClick={handleOpenAddEmployee}
                    className="px-3.5 py-2 bg-amber-200 hover:bg-amber-100 text-stone-950 text-xs font-bold rounded-xl transition-all cursor-pointer shadow-md"
                  >
                    + Add New Staff
                  </button>
                )}
              </div>
            )}
          </div>
        </div>

        {/* Tab Body */}
        <div className="p-6 overflow-y-auto flex-1 space-y-4 text-xs font-mono">
          {activeTab === 'orders' ? (
            /* ================= 1. TRANSACTIONS TABLE ================= */
            filteredOrders.length === 0 ? (
              <div className="text-center py-12 text-stone-500">No orders match your search criteria.</div>
            ) : (
              <div className="border border-stone-800 rounded-2xl overflow-hidden">
                <table className="w-full text-left border-collapse">
                  <thead>
                    <tr className="bg-stone-950 text-stone-400 text-[10px] uppercase tracking-wider border-b border-stone-800">
                      <th className="p-3">Order / Receipt ID</th>
                      <th className="p-3">Timestamp</th>
                      <th className="p-3">Customer & Phone</th>
                      <th className="p-3">Type</th>
                      <th className="p-3">Subtotal</th>
                      <th className="p-3">Tax</th>
                      <th className="p-3">Tip</th>
                      <th className="p-3">Total</th>
                      <th className="p-3 text-right print:hidden">Receipt</th>
                    </tr>
                  </thead>
                  <tbody className="divide-y divide-stone-800 text-stone-300">
                    {filteredOrders.map((ord) => (
                      <tr key={ord.id} className="hover:bg-stone-850/50 transition-colors">
                        <td className="p-3 font-bold text-amber-200">#{ord.id}</td>
                        <td className="p-3 text-stone-400 text-[11px]">
                          {ord.created_at ? new Date(ord.created_at).toLocaleString() : 'N/A'}
                        </td>
                        <td className="p-3">
                          <span className="font-sans font-semibold text-stone-200 block">
                            {ord.customer_name || 'Walk-In Customer'}
                          </span>
                          <span className="text-[10px] text-stone-500">
                            {ord.customer_phone || 'No phone'}
                          </span>
                        </td>
                        <td className="p-3 uppercase text-[10px] text-stone-400">
                          {ord.order_type} ({ord.payment_method})
                        </td>
                        <td className="p-3">${parseFloat(ord.subtotal || 0).toFixed(2)}</td>
                        <td className="p-3">${parseFloat(ord.tax || 0).toFixed(2)}</td>
                        <td className="p-3 text-emerald-400">+${parseFloat(ord.tip || 0).toFixed(2)}</td>
                        <td className="p-3 font-bold text-stone-100">
                          ${parseFloat(ord.total_price || 0).toFixed(2)}
                        </td>
                        <td className="p-3 text-right print:hidden">
                          <button
                            type="button"
                            onClick={() => onViewReceipt(ord)}
                            className="px-2.5 py-1 bg-stone-800 hover:bg-stone-700 text-amber-200 rounded-lg border border-stone-700 text-[11px] font-bold cursor-pointer transition-all"
                          >
                            Print Receipt
                          </button>
                        </td>
                      </tr>
                    ))}
                  </tbody>
                </table>
              </div>
            )
          ) : activeTab === 'customers' ? (
            /* ================= 2. CUSTOMER DIRECTORY TABLE ================= */
            filteredCustomers.length === 0 ? (
              <div className="text-center py-12 text-stone-500">No customer profiles match your search criteria.</div>
            ) : (
              <div className="border border-stone-800 rounded-2xl overflow-hidden">
                <table className="w-full text-left border-collapse">
                  <thead>
                    <tr className="bg-stone-950 text-stone-400 text-[10px] uppercase tracking-wider border-b border-stone-800">
                      <th className="p-3">Customer ID</th>
                      <th className="p-3">Name</th>
                      <th className="p-3">Phone</th>
                      <th className="p-3">Delivery Address</th>
                      <th className="p-3">Subdivision / Cross Street</th>
                      <th className="p-3 text-right print:hidden">Actions</th>
                    </tr>
                  </thead>
                  <tbody className="divide-y divide-stone-800 text-stone-300">
                    {filteredCustomers.map((c, i) => {
                      const fullName = `${c.first_name || ''} ${c.last_name || ''}`.trim() || c.name || 'Unnamed Customer';
                      const displayPhone = c.phone_number || c.phoneNumber || c.phone || 'No phone';

                      return (
                        <tr key={c.id || i} className="hover:bg-stone-850/50 transition-colors">
                          <td className="p-3 text-stone-500 font-mono">#{c.id || i + 1}</td>
                          <td className="p-3 font-sans font-semibold text-stone-200">
                            {fullName}
                          </td>
                          <td className="p-3 font-mono text-amber-200">
                            {displayPhone}
                          </td>
                          <td className="p-3 font-sans text-stone-300">
                            {c.address || c.streetAddress || 'N/A'}
                          </td>
                          <td className="p-3 text-stone-400 text-[11px]">
                            {c.subdivision ? `${c.subdivision}` : ''}
                            {c.major_intersection || c.nearestIntersection
                              ? ` (${c.major_intersection || c.nearestIntersection})`
                              : ''}
                          </td>
                          <td className="p-3 text-right print:hidden space-x-2">
                            <button
                              type="button"
                              onClick={() => handleStartEditCustomer(c)}
                              className="px-2.5 py-1 bg-stone-800 hover:bg-stone-700 text-amber-200 rounded-lg border border-stone-700 text-[11px] font-bold cursor-pointer transition-all"
                            >
                              Edit
                            </button>
                            <button
                              type="button"
                              onClick={() => handleDeleteCustomerPrompt(c)}
                              className="px-2.5 py-1 bg-rose-950/40 hover:bg-rose-900/60 text-rose-300 rounded-lg border border-rose-800/60 text-[11px] font-bold cursor-pointer transition-all"
                            >
                              Delete
                            </button>
                          </td>
                        </tr>
                      );
                    })}
                  </tbody>
                </table>
              </div>
            )
          ) : (
            /* ================= 3. STAFF DIRECTORY TABLE ================= */
            filteredEmployees.length === 0 ? (
              <div className="text-center py-12 text-stone-500">
                No {employeeStatusTab} employees found.
              </div>
            ) : (
              <div className="border border-stone-800 rounded-2xl overflow-hidden">
                <table className="w-full text-left border-collapse">
                  <thead>
                    <tr className="bg-stone-950 text-stone-400 text-[10px] uppercase tracking-wider border-b border-stone-800">
                      <th className="p-3">Staff ID</th>
                      <th className="p-3">Name & Credentials</th>
                      <th className="p-3">Role</th>
                      <th className="p-3">Date Hired</th>
                      <th className="p-3">Contact (Phone / Email)</th>
                      <th className="p-3">Birthdate</th>
                      <th className="p-3 text-right print:hidden">Actions</th>
                    </tr>
                  </thead>
                  <tbody className="divide-y divide-stone-800 text-stone-300">
                    {filteredEmployees.map((emp) => (
                      <tr key={emp.id} className="hover:bg-stone-850/50 transition-colors">
                        <td className="p-3 text-stone-500 font-mono">#{emp.id}</td>
                        <td className="p-3">
                          <span className="font-sans font-semibold text-stone-200 block text-xs">
                            {emp.full_name || 'N/A'}
                          </span>
                          <div className="text-[11px] text-stone-400 font-mono mt-0.5 space-y-0.5">
                            <div>
                              <span className="text-stone-500">User:</span>{' '}
                              <span className="text-amber-200 font-bold">{emp.username}</span>
                            </div>

                            {/* Password Reveal Toggle (Restricted to Owner/Manager) */}
                            {(currentStaffRole === 'owner' || currentStaffRole === 'manager') && (
                              <div className="flex items-center gap-2">
                                <span className="text-stone-500">Pass:</span>{' '}
                                <span className="text-stone-300">
                                  {visiblePasswords.has(emp.id) ? emp.password : '••••••••'}
                                </span>
                                <button
                                  type="button"
                                  onClick={() => togglePasswordVisibility(emp.id)}
                                  className="text-[10px] text-stone-500 hover:text-amber-200 underline cursor-pointer"
                                >
                                  {visiblePasswords.has(emp.id) ? 'Hide' : 'Show'}
                                </button>
                              </div>
                            )}
                          </div>
                        </td>
                        <td className="p-3">
                          <span
                            className={`px-2 py-0.5 rounded text-[10px] font-bold uppercase tracking-wider ${
                              emp.role === 'owner'
                                ? 'bg-amber-200/20 text-amber-300 border border-amber-300/40'
                                : emp.role === 'manager'
                                ? 'bg-sky-950 text-sky-300 border border-sky-800'
                                : 'bg-stone-800 text-stone-300'
                            }`}
                          >
                            {emp.role || 'worker'}
                          </span>
                        </td>
                        <td className="p-3 text-amber-200 font-mono text-xs">
                          {emp.date_hired || 'Prior to record'}
                        </td>
                        <td className="p-3 font-sans text-xs">
                          <div>{emp.phone_number || 'No phone'}</div>
                          <div className="text-[10px] text-stone-500 font-mono">{emp.email || 'No email'}</div>
                        </td>
                        <td className="p-3 text-stone-400 text-xs">
                          {emp.birthdate || 'N/A'}
                        </td>
                        <td className="p-3 text-right print:hidden space-x-2">
                          {currentStaffRole === 'owner' && (
                            <>
                              <button
                                type="button"
                                onClick={() => handleOpenEditEmployee(emp)}
                                className="px-2.5 py-1 bg-stone-800 hover:bg-stone-700 text-amber-200 rounded-lg border border-stone-700 text-[11px] font-bold cursor-pointer"
                              >
                                Edit
                              </button>
                              <button
                                type="button"
                                onClick={() => handleDeleteOrArchiveEmployee(emp)}
                                className="px-2.5 py-1 bg-rose-950/40 hover:bg-rose-900/60 text-rose-300 rounded-lg border border-rose-800/60 text-[11px] font-bold cursor-pointer"
                              >
                                {emp.is_active !== false ? 'Archive' : 'Delete'}
                              </button>
                            </>
                          )}
                        </td>
                      </tr>
                    ))}
                  </tbody>
                </table>
              </div>
            )
          )}
        </div>
      </div>

      {/* ================= EDIT CUSTOMER MODAL ================= */}
      {editingCustomer && (
        <div className="fixed inset-0 z-60 flex items-center justify-center p-4 bg-black/80 backdrop-blur-sm animate-in fade-in duration-150">
          <div className="bg-stone-900 border border-stone-800 rounded-3xl max-w-md w-full shadow-2xl overflow-hidden">
            <div className="p-4 border-b border-stone-800 flex justify-between items-center bg-stone-950">
              <h3 className="text-sm font-serif uppercase tracking-wider text-stone-100">
                Edit Customer #{editingCustomer.id}
              </h3>
              <button
                type="button"
                onClick={() => setEditingCustomer(null)}
                className="text-stone-400 hover:text-white text-xs cursor-pointer"
              >
                ✕
              </button>
            </div>

            <form onSubmit={handleSaveCustomerEdit} className="p-5 space-y-3.5 text-xs font-sans">
              <div className="grid grid-cols-2 gap-3">
                <div>
                  <label className="block text-[10px] uppercase tracking-wider text-stone-400 mb-1">First Name</label>
                  <input
                    type="text"
                    required
                    value={customerEditForm.first_name}
                    onChange={(e) => setCustomerEditForm((p) => ({ ...p, first_name: e.target.value }))}
                    className="w-full bg-stone-950 border border-stone-800 rounded-xl px-3 py-2 text-stone-200 focus:outline-none focus:border-amber-200/60"
                  />
                </div>
                <div>
                  <label className="block text-[10px] uppercase tracking-wider text-stone-400 mb-1">Last Name</label>
                  <input
                    type="text"
                    value={customerEditForm.last_name}
                    onChange={(e) => setCustomerEditForm((p) => ({ ...p, last_name: e.target.value }))}
                    className="w-full bg-stone-950 border border-stone-800 rounded-xl px-3 py-2 text-stone-200 focus:outline-none focus:border-amber-200/60"
                  />
                </div>
              </div>

              <div>
                <label className="block text-[10px] uppercase tracking-wider text-stone-400 mb-1">Phone Number</label>
                <input
                  type="text"
                  required
                  value={customerEditForm.phone_number}
                  onChange={(e) => setCustomerEditForm((p) => ({ ...p, phone_number: e.target.value }))}
                  className="w-full bg-stone-950 border border-stone-800 rounded-xl px-3 py-2 text-stone-200 focus:outline-none focus:border-amber-200/60"
                />
              </div>

              <div>
                <label className="block text-[10px] uppercase tracking-wider text-stone-400 mb-1">Street Address</label>
                <input
                  type="text"
                  value={customerEditForm.address}
                  onChange={(e) => setCustomerEditForm((p) => ({ ...p, address: e.target.value }))}
                  className="w-full bg-stone-950 border border-stone-800 rounded-xl px-3 py-2 text-stone-200 focus:outline-none focus:border-amber-200/60"
                />
              </div>

              <div className="grid grid-cols-2 gap-3">
                <div>
                  <label className="block text-[10px] uppercase tracking-wider text-stone-400 mb-1">Subdivision</label>
                  <input
                    type="text"
                    value={customerEditForm.subdivision}
                    onChange={(e) => setCustomerEditForm((p) => ({ ...p, subdivision: e.target.value }))}
                    className="w-full bg-stone-950 border border-stone-800 rounded-xl px-3 py-2 text-stone-200 focus:outline-none focus:border-amber-200/60"
                  />
                </div>
                <div>
                  <label className="block text-[10px] uppercase tracking-wider text-stone-400 mb-1">Cross Street</label>
                  <input
                    type="text"
                    value={customerEditForm.major_intersection}
                    onChange={(e) => setCustomerEditForm((p) => ({ ...p, major_intersection: e.target.value }))}
                    className="w-full bg-stone-950 border border-stone-800 rounded-xl px-3 py-2 text-stone-200 focus:outline-none focus:border-amber-200/60"
                  />
                </div>
              </div>

              <div className="pt-2 flex justify-end gap-2">
                <button
                  type="button"
                  onClick={() => setEditingCustomer(null)}
                  className="px-3.5 py-2 bg-stone-800 hover:bg-stone-700 text-stone-300 rounded-xl font-semibold cursor-pointer"
                >
                  Cancel
                </button>
                <button
                  type="submit"
                  className="px-4 py-2 bg-amber-200 hover:bg-amber-100 text-stone-950 font-bold rounded-xl cursor-pointer"
                >
                  Save Changes
                </button>
              </div>
            </form>
          </div>
        </div>
      )}

      {/* ================= ADD / EDIT EMPLOYEE MODAL ================= */}
      {isEmployeeModalOpen && (
        <div className="fixed inset-0 z-60 flex items-center justify-center p-4 bg-black/85 backdrop-blur-sm animate-in fade-in duration-150">
          <div className="bg-stone-900 border border-stone-800 rounded-3xl max-w-lg w-full shadow-2xl overflow-hidden">
            <div className="p-5 border-b border-stone-800 flex justify-between items-center bg-stone-950">
              <div>
                <span className="text-[10px] font-mono uppercase tracking-widest text-amber-200/80">Staff Management</span>
                <h3 className="text-base font-serif uppercase tracking-wider text-stone-100">
                  {editingEmployeeId ? 'Edit Employee Record' : 'Enroll New Staff Member'}
                </h3>
              </div>
              <button
                type="button"
                onClick={() => setIsEmployeeModalOpen(false)}
                className="text-stone-400 hover:text-white text-xs cursor-pointer"
              >
                ✕
              </button>
            </div>

            <form onSubmit={handleSaveEmployeeForm} className="p-6 space-y-4 text-xs font-sans">
              <div className="grid grid-cols-1 sm:grid-cols-2 gap-3">
                <div>
                  <label className="block text-[10px] uppercase font-bold text-stone-400 mb-1">Full Name</label>
                  <input
                    type="text"
                    required
                    placeholder="Jane Doe"
                    value={employeeForm.full_name}
                    onChange={(e) => setEmployeeForm((p) => ({ ...p, full_name: e.target.value }))}
                    className="w-full bg-stone-950 border border-stone-800 rounded-xl px-3 py-2 text-stone-200 focus:border-amber-200/60 outline-none"
                  />
                </div>
                <div>
                  <label className="block text-[10px] uppercase font-bold text-stone-400 mb-1">Role</label>
                  <select
                    value={employeeForm.role}
                    onChange={(e) => setEmployeeForm((p) => ({ ...p, role: e.target.value }))}
                    className="w-full bg-stone-950 border border-stone-800 rounded-xl px-3 py-2 text-stone-200 focus:border-amber-200/60 outline-none"
                  >
                    <option value="worker">Kitchen / Counter Worker</option>
                    <option value="manager">Shift Manager</option>
                    <option value="owner">Store Owner</option>
                  </select>
                </div>
              </div>

              <div className="grid grid-cols-1 sm:grid-cols-2 gap-3">
                <div>
                  <label className="block text-[10px] uppercase font-bold text-stone-400 mb-1">Login Username</label>
                  <input
                    type="text"
                    required
                    placeholder="janedoe"
                    value={employeeForm.username}
                    onChange={(e) => setEmployeeForm((p) => ({ ...p, username: e.target.value }))}
                    className="w-full bg-stone-950 border border-stone-800 rounded-xl px-3 py-2 text-stone-200 focus:border-amber-200/60 outline-none font-mono"
                  />
                </div>
                <div>
                  <div className="flex justify-between items-center mb-1">
                    <label className="text-[10px] uppercase font-bold text-stone-400">
                      {editingEmployeeId ? 'Change Password' : 'Password'}
                    </label>
                    <button
                      type="button"
                      onClick={generateRandomPassword}
                      className="text-[10px] text-amber-200 hover:underline cursor-pointer"
                    >
                      ⚡ Generate Pass
                    </button>
                  </div>
                  <input
                    type="text"
                    required={!editingEmployeeId}
                    placeholder="••••••••"
                    value={employeeForm.password}
                    onChange={(e) => setEmployeeForm((p) => ({ ...p, password: e.target.value }))}
                    className="w-full bg-stone-950 border border-stone-800 rounded-xl px-3 py-2 text-stone-200 focus:border-amber-200/60 outline-none font-mono"
                  />
                </div>
              </div>

              <div className="grid grid-cols-1 sm:grid-cols-2 gap-3">
                <div>
                  <label className="block text-[10px] uppercase font-bold text-stone-400 mb-1">Birthdate</label>
                  <input
                    type="date"
                    required
                    value={employeeForm.birthdate}
                    onChange={(e) => setEmployeeForm((p) => ({ ...p, birthdate: e.target.value }))}
                    className="w-full bg-stone-950 border border-stone-800 rounded-xl px-3 py-2 text-stone-200 focus:border-amber-200/60 outline-none font-mono"
                  />
                </div>
                <div>
                  <label className="block text-[10px] uppercase font-bold text-stone-400 mb-1">Date Hired</label>
                  <input
                    type="date"
                    required
                    value={employeeForm.date_hired}
                    onChange={(e) => setEmployeeForm((p) => ({ ...p, date_hired: e.target.value }))}
                    className="w-full bg-stone-950 border border-stone-800 rounded-xl px-3 py-2 text-stone-200 focus:border-amber-200/60 outline-none font-mono"
                  />
                </div>
              </div>

              <div className="grid grid-cols-1 sm:grid-cols-2 gap-3">
                <div>
                  <label className="block text-[10px] uppercase font-bold text-stone-400 mb-1">Phone Number</label>
                  <input
                    type="text"
                    placeholder="5551234567"
                    value={employeeForm.phone_number}
                    onChange={(e) => setEmployeeForm((p) => ({ ...p, phone_number: e.target.value }))}
                    className="w-full bg-stone-950 border border-stone-800 rounded-xl px-3 py-2 text-stone-200 focus:border-amber-200/60 outline-none"
                  />
                </div>
                <div>
                  <label className="block text-[10px] uppercase font-bold text-stone-400 mb-1">Email Address</label>
                  <input
                    type="email"
                    placeholder="jane@pizzeria.com"
                    value={employeeForm.email}
                    onChange={(e) => setEmployeeForm((p) => ({ ...p, email: e.target.value }))}
                    className="w-full bg-stone-950 border border-stone-800 rounded-xl px-3 py-2 text-stone-200 focus:border-amber-200/60 outline-none"
                  />
                </div>
              </div>

              <div className="pt-3 border-t border-stone-800 flex justify-end gap-2">
                <button
                  type="button"
                  onClick={() => setIsEmployeeModalOpen(false)}
                  className="px-4 py-2 bg-stone-800 hover:bg-stone-700 text-stone-300 rounded-xl font-bold cursor-pointer"
                >
                  Cancel
                </button>
                <button
                  type="submit"
                  className="px-5 py-2 bg-amber-200 hover:bg-amber-100 text-stone-950 font-bold rounded-xl cursor-pointer"
                >
                  {editingEmployeeId ? 'Save Changes' : 'Enroll Staff'}
                </button>
              </div>
            </form>
          </div>
        </div>
      )}
    </div>
  );
}