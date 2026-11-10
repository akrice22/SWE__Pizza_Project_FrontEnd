// src/AuthModal.jsx
// src/AuthModal.jsx
import { useState } from 'react';
import { supabase } from './supabaseClient';

export default function AuthModal({
  isOpen,
  onClose,
  onAdminLoginSuccess,
  onCustomerLoginSuccess,
}) {
  const [tab, setTab] = useState('customer');
  const [customerPhone, setCustomerPhone] = useState('');
  const [staffUsername, setStaffUsername] = useState('');
  const [staffPassword, setStaffPassword] = useState('');
  const [errorMessage, setErrorMessage] = useState('');
  const [isSubmitting, setIsSubmitting] = useState(false);

  if (!isOpen) return null;

  const handleCustomerSubmit = (e) => {
    e.preventDefault();
    const cleanPhone = customerPhone.replace(/\D/g, '');
    if (cleanPhone.length !== 10) {
      setErrorMessage('Please enter a valid 10-digit phone number.');
      return;
    }
    setErrorMessage('');
    onCustomerLoginSuccess({ phone: cleanPhone });
    onClose();
  };

  const handleStaffSubmit = async (e) => {
    e.preventDefault();
    setErrorMessage('');
    setIsSubmitting(true);

    try {
      if (!supabase) {
        throw new Error('Database client not initialized. Check .env configuration.');
      }

      // Query staff_users table for matching username and password
      const { data: staffMember, error } = await supabase
        .from('staff_users')
        .select('*')
        .eq('username', staffUsername.trim().toLowerCase())
        .eq('password', staffPassword)
        .eq('is_active', true)
        .maybeSingle();

      if (error) throw error;

      if (staffMember) {
        onAdminLoginSuccess({
          id: staffMember.id,
          username: staffMember.username,
          fullName: staffMember.full_name || staffMember.username,
          role: staffMember.role || 'worker',
        });
        onClose();
      } else {
        setErrorMessage('Invalid username or password, or account is inactive.');
      }
    } catch (err) {
      console.error('Staff login error:', err);
      setErrorMessage('Login failed. Please verify credentials or connection.');
    } finally {
      setIsSubmitting(false);
    }
  };

  return (
    <div className="fixed inset-0 z-50 flex items-center justify-center p-4 bg-black/85 backdrop-blur-md animate-in fade-in duration-200">
      <div className="bg-stone-900 border border-stone-800 rounded-3xl max-w-md w-full shadow-2xl flex flex-col overflow-hidden">
        
        {/* Header Tabs */}
        <div className="p-4 border-b border-stone-800 flex justify-between items-center bg-stone-950">
          <div className="flex gap-2">
            <button
              type="button"
              onClick={() => { setTab('customer'); setErrorMessage(''); }}
              className={`text-xs uppercase font-bold tracking-wider px-3 py-1.5 rounded-lg transition-all ${
                tab === 'customer'
                  ? 'bg-amber-200 text-stone-950 shadow-sm'
                  : 'text-stone-400 hover:text-stone-200'
              }`}
            >
              Customer
            </button>
            <button
              type="button"
              onClick={() => { setTab('staff'); setErrorMessage(''); }}
              className={`text-xs uppercase font-bold tracking-wider px-3 py-1.5 rounded-lg transition-all ${
                tab === 'staff'
                  ? 'bg-amber-200 text-stone-950 shadow-sm'
                  : 'text-stone-400 hover:text-stone-200'
              }`}
            >
              Employee Portal
            </button>
          </div>
          <button
            type="button"
            onClick={onClose}
            className="text-stone-400 hover:text-white transition-colors cursor-pointer text-xs"
          >
            ✕ Close
          </button>
        </div>

        {/* Modal Form */}
        <div className="p-6 space-y-4">
          {errorMessage && (
            <div className="p-3 rounded-xl bg-rose-950/40 border border-rose-500/80 text-rose-300 text-xs">
              {errorMessage}
            </div>
          )}

          {tab === 'customer' ? (
            <form onSubmit={handleCustomerSubmit} className="space-y-4 text-xs">
              <div>
                <label className="block text-stone-400 uppercase tracking-wider font-semibold mb-1 text-[10px]">
                  Phone Number
                </label>
                <input
                  type="text"
                  required
                  placeholder="5551234567"
                  value={customerPhone}
                  onChange={(e) => setCustomerPhone(e.target.value)}
                  className="w-full bg-stone-950 border border-stone-800 rounded-xl px-4 py-2.5 text-stone-200 focus:outline-none focus:border-amber-200/60"
                />
              </div>
              <button
                type="submit"
                className="w-full py-3 bg-amber-200 hover:bg-amber-100 text-stone-950 font-bold rounded-xl uppercase tracking-wider transition-all cursor-pointer"
              >
                Access Account →
              </button>
            </form>
          ) : (
            <form onSubmit={handleStaffSubmit} className="space-y-4 text-xs">
              <div>
                <label className="block text-stone-400 uppercase tracking-wider font-semibold mb-1 text-[10px]">
                  Staff Username
                </label>
                <input
                  type="text"
                  required
                  placeholder="e.g. manager1 or worker_jane"
                  value={staffUsername}
                  onChange={(e) => setStaffUsername(e.target.value)}
                  className="w-full bg-stone-950 border border-stone-800 rounded-xl px-4 py-2.5 text-stone-200 focus:outline-none focus:border-amber-200/60 font-mono"
                />
              </div>

              <div>
                <label className="block text-stone-400 uppercase tracking-wider font-semibold mb-1 text-[10px]">
                  Password
                </label>
                <input
                  type="password"
                  required
                  placeholder="••••••••"
                  value={staffPassword}
                  onChange={(e) => setStaffPassword(e.target.value)}
                  className="w-full bg-stone-950 border border-stone-800 rounded-xl px-4 py-2.5 text-stone-200 focus:outline-none focus:border-amber-200/60 font-mono"
                />
              </div>

              <button
                type="submit"
                disabled={isSubmitting}
                className="w-full py-3 bg-amber-200 hover:bg-amber-100 text-stone-950 font-bold rounded-xl uppercase tracking-wider transition-all cursor-pointer disabled:opacity-50"
              >
                {isSubmitting ? 'Authenticating...' : 'Sign In to Employee Dashboard →'}
              </button>
            </form>
          )}
        </div>
      </div>
    </div>
  );
}