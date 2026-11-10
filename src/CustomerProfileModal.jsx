// src/CustomerProfileModal.jsx
import { useState } from 'react';

export default function CustomerProfileModal({
  isOpen,
  onClose,
  customerInfo,
  onUpdateCustomer,
  pastOrders,
  onViewReceipt,
  onReorder,
  onLogout,
}) {
  const [activeTab, setActiveTab] = useState('orders'); // 'orders' | 'profile'
  const [editForm, setEditForm] = useState({ ...customerInfo });
  const [saveMessage, setSaveMessage] = useState('');

  if (!isOpen) return null;

  const handleSave = (e) => {
    e.preventDefault();
    onUpdateCustomer(editForm);
    setSaveMessage('Profile information saved successfully!');
    setTimeout(() => setSaveMessage(''), 3000);
  };

  return (
    <div className="fixed inset-0 z-50 flex items-center justify-center p-4 bg-black/85 backdrop-blur-md animate-in fade-in duration-200 overflow-y-auto">
      <div className="bg-stone-900 border border-stone-800 rounded-3xl max-w-2xl w-full max-h-[90vh] shadow-2xl flex flex-col overflow-hidden my-auto">
        {/* Header */}
        <div className="p-5 border-b border-stone-800 flex justify-between items-center bg-stone-950">
          <div>
            <span className="text-[10px] font-mono uppercase tracking-widest text-amber-200/80">Account Center</span>
            <h3 className="text-base font-serif uppercase tracking-wider text-stone-100">
              {customerInfo.name || 'Valued Guest'} ({customerInfo.phone})
            </h3>
          </div>
          <div className="flex items-center gap-3">
            <button
              type="button"
              onClick={onLogout}
              className="text-xs text-rose-400 hover:text-rose-300 font-semibold cursor-pointer"
            >
              Sign Out
            </button>
            <button
              type="button"
              onClick={onClose}
              className="text-stone-400 hover:text-white transition-colors cursor-pointer text-xs"
            >
              ✕ Close
            </button>
          </div>
        </div>

        {/* Tab Switcher */}
        <div className="flex border-b border-stone-800 bg-stone-950/50 px-6 pt-3 gap-4">
          <button
            type="button"
            onClick={() => setActiveTab('orders')}
            className={`pb-2.5 text-xs font-bold uppercase tracking-wider transition-all border-b-2 cursor-pointer ${
              activeTab === 'orders'
                ? 'border-amber-200 text-amber-200'
                : 'border-transparent text-stone-400 hover:text-stone-200'
            }`}
          >
            Order History ({pastOrders.length})
          </button>
          <button
            type="button"
            onClick={() => setActiveTab('profile')}
            className={`pb-2.5 text-xs font-bold uppercase tracking-wider transition-all border-b-2 cursor-pointer ${
              activeTab === 'profile'
                ? 'border-amber-200 text-amber-200'
                : 'border-transparent text-stone-400 hover:text-stone-200'
            }`}
          >
            Manage Delivery Address & Details
          </button>
        </div>

        {/* Tab Content */}
        <div className="p-6 overflow-y-auto space-y-4 flex-1 text-xs">
          {activeTab === 'orders' ? (
            <div className="space-y-3">
              {pastOrders.length === 0 ? (
                <div className="text-center py-12 text-stone-500">
                  You do not have any past orders under this phone number yet.
                </div>
              ) : (
                pastOrders.map((order) => (
                  <div
                    key={order.id}
                    className="p-4 rounded-2xl bg-stone-950/70 border border-stone-800 space-y-3"
                  >
                    <div className="flex justify-between items-center border-b border-stone-800 pb-2">
                      <div>
                        <span className="font-bold text-stone-200">Order #{order.id}</span>
                        <span className="text-stone-500 font-mono text-[11px] block">
                          {order.created_at ? new Date(order.created_at).toLocaleString() : 'Recent'}
                        </span>
                      </div>
                      <div className="text-right">
                        <span className="text-amber-200 font-bold font-mono text-sm">
                          ${parseFloat(order.total_price || 0).toFixed(2)}
                        </span>
                        <span className="text-[10px] block uppercase text-stone-400">
                          {order.order_type} • {order.payment_method}
                        </span>
                      </div>
                    </div>

                    {/* Order items listing */}
                    <div className="space-y-1.5 pl-1">
                      {order.order_items?.map((item, idx) => (
                        <div key={idx} className="flex justify-between text-stone-300">
                          <span>
                            {item.size} • {item.crust} ({item.toppings?.join(', ') || 'No extra toppings'})
                          </span>
                          <span className="font-mono text-stone-400">
                            ${parseFloat(item.price || 0).toFixed(2)}
                          </span>
                        </div>
                      ))}
                    </div>

                    <div className="flex justify-end gap-2 pt-2 border-t border-stone-800/60">
                      {onViewReceipt && (
                        <button
                          type="button"
                          onClick={() => onViewReceipt(order)}
                          className="px-3 py-1.5 rounded-lg bg-stone-800 hover:bg-stone-700 text-stone-200 text-[11px] font-semibold cursor-pointer"
                        >
                          View Receipt
                        </button>
                      )}
                      {onReorder && (
                        <button
                          type="button"
                          onClick={() => onReorder(order)}
                          className="px-3 py-1.5 rounded-lg bg-amber-200 hover:bg-amber-100 text-stone-950 text-[11px] font-bold cursor-pointer"
                        >
                          + Reorder Order
                        </button>
                      )}
                    </div>
                  </div>
                ))
              )}
            </div>
          ) : (
            <form onSubmit={handleSave} className="space-y-4 max-w-lg mx-auto">
              {saveMessage && (
                <div className="p-3 rounded-xl bg-emerald-950/40 border border-emerald-800 text-emerald-300">
                  {saveMessage}
                </div>
              )}

              <div>
                <label className="block text-stone-400 uppercase tracking-wider mb-1 text-[10px]">
                  Full Name
                </label>
                <input
                  type="text"
                  value={editForm.name}
                  onChange={(e) => setEditForm((p) => ({ ...p, name: e.target.value }))}
                  className="w-full bg-stone-950 border border-stone-800 rounded-xl px-4 py-2 text-stone-200 focus:outline-none focus:border-amber-200/60"
                />
              </div>

              <div>
                <label className="block text-stone-400 uppercase tracking-wider mb-1 text-[10px]">
                  Phone Number
                </label>
                <input
                  type="text"
                  value={editForm.phone}
                  onChange={(e) => setEditForm((p) => ({ ...p, phone: e.target.value }))}
                  className="w-full bg-stone-950 border border-stone-800 rounded-xl px-4 py-2 text-stone-200 focus:outline-none focus:border-amber-200/60"
                />
              </div>

              <div>
                <label className="block text-stone-400 uppercase tracking-wider mb-1 text-[10px]">
                  Delivery Street Address
                </label>
                <input
                  type="text"
                  value={editForm.streetAddress}
                  onChange={(e) => setEditForm((p) => ({ ...p, streetAddress: e.target.value }))}
                  className="w-full bg-stone-950 border border-stone-800 rounded-xl px-4 py-2 text-stone-200 focus:outline-none focus:border-amber-200/60"
                />
              </div>

              <div className="grid grid-cols-2 gap-3">
                <div>
                  <label className="block text-stone-400 uppercase tracking-wider mb-1 text-[10px]">
                    Subdivision / Unit
                  </label>
                  <input
                    type="text"
                    value={editForm.subdivision}
                    onChange={(e) => setEditForm((p) => ({ ...p, subdivision: e.target.value }))}
                    className="w-full bg-stone-950 border border-stone-800 rounded-xl px-4 py-2 text-stone-200 focus:outline-none focus:border-amber-200/60"
                  />
                </div>
                <div>
                  <label className="block text-stone-400 uppercase tracking-wider mb-1 text-[10px]">
                    Cross Street
                  </label>
                  <input
                    type="text"
                    value={editForm.nearestIntersection}
                    onChange={(e) =>
                      setEditForm((p) => ({ ...p, nearestIntersection: e.target.value }))
                    }
                    className="w-full bg-stone-950 border border-stone-800 rounded-xl px-4 py-2 text-stone-200 focus:outline-none focus:border-amber-200/60"
                  />
                </div>
              </div>

              <button
                type="submit"
                className="w-full py-3 bg-amber-200 hover:bg-amber-100 text-stone-950 font-bold rounded-xl uppercase tracking-wider cursor-pointer"
              >
                Save Details
              </button>
            </form>
          )}
        </div>
      </div>
    </div>
  );
}