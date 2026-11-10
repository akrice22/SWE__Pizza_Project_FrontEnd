// src/OrderReceiptModal.jsx
export default function OrderReceiptModal({ isOpen, receipt, onClose }) {
  if (!isOpen || !receipt) return null;

  return (
    <div className="fixed inset-0 z-50 flex items-center justify-center p-4 bg-black/85 backdrop-blur-md animate-in fade-in duration-200 overflow-y-auto">
      <div className="bg-stone-900 border border-stone-800 rounded-3xl max-w-lg w-full max-h-[92vh] shadow-2xl flex flex-col overflow-hidden my-auto">
        
        {/* Modal Actions Header */}
        <div className="p-4 border-b border-stone-800 flex justify-between items-center bg-stone-950 print:hidden">
          <span className="text-xs font-mono text-emerald-400 flex items-center gap-1.5">
            <span className="w-2 h-2 rounded-full bg-emerald-400 animate-pulse" />
            Payment Confirmed
          </span>
          <div className="flex gap-2">
            <button
              type="button"
              onClick={() => window.print()}
              className="px-3 py-1.5 bg-stone-800 hover:bg-stone-700 text-stone-200 text-xs font-semibold rounded-lg border border-stone-700 transition-all cursor-pointer"
            >
              🖨️ Print / Save PDF
            </button>
            <button
              type="button"
              onClick={onClose}
              className="px-3 py-1.5 bg-amber-200 text-stone-950 text-xs font-bold rounded-lg hover:bg-amber-100 transition-all cursor-pointer"
            >
              Done
            </button>
          </div>
        </div>

        {/* PRINTABLE RECEIPT CONTENT */}
        <div className="p-8 overflow-y-auto space-y-6 text-stone-800 bg-[#fdfbf7] print:p-0 print:m-0 print:bg-white print:text-black font-mono text-xs">
          
          {/* Restaurant Header */}
          <div className="text-center space-y-1 border-b-2 border-dashed border-stone-400 pb-4">
            <h2 className="text-xl font-serif font-black uppercase tracking-widest text-stone-950">
              STONE OVEN PIZZA CO.
            </h2>
            <p className="text-[11px] text-stone-600">Hearth-Fired Craft Kitchen</p>
            <p className="text-[10px] text-stone-500">Order #{receipt.orderId}</p>
            <p className="text-[10px] text-stone-500">{receipt.timestamp}</p>
          </div>

          {/* Customer & Fulfillment Info */}
          <div className="border-b border-dashed border-stone-300 pb-3 space-y-1 text-[11px]">
            <div className="flex justify-between">
              <span className="text-stone-500">Customer:</span>
              <span className="font-bold text-stone-900">{receipt.customer.name}</span>
            </div>
            <div className="flex justify-between">
              <span className="text-stone-500">Phone:</span>
              <span>{receipt.customer.phone}</span>
            </div>
            <div className="flex justify-between">
              <span className="text-stone-500">Fulfillment:</span>
              <span className="uppercase font-bold text-stone-900">{receipt.customer.orderType}</span>
            </div>
            {receipt.customer.orderType === 'delivery' && (
              <div className="flex justify-between text-left">
                <span className="text-stone-500">Address:</span>
                <span className="text-right max-w-[240px]">
                  {receipt.customer.streetAddress}
                  {receipt.customer.subdivision && `, ${receipt.customer.subdivision}`}
                </span>
              </div>
            )}
            <div className="flex justify-between">
              <span className="text-stone-500">Payment:</span>
              <span className="uppercase font-bold text-stone-900">{receipt.paymentMethod}</span>
            </div>
          </div>

          {/* Itemized Pizzas */}
          <div className="space-y-3 border-b-2 border-dashed border-stone-400 pb-4">
            <div className="font-bold text-stone-900 uppercase tracking-wider text-[11px]">
              Ordered Items
            </div>

            {receipt.items.map((pizza, idx) => (
              <div key={idx} className="space-y-1">
                <div className="flex justify-between font-bold text-stone-950">
                  <span>#{idx + 1} {pizza.size.label}</span>
                  <span>${parseFloat(pizza.price).toFixed(2)}</span>
                </div>
                <div className="text-[10px] text-stone-600 pl-3 space-y-0.5">
                  <div>Crust: {pizza.crust.label}</div>
                  <div>Sauce: {pizza.sauce.label}</div>
                  {(pizza.selectedMeats?.length > 0 || pizza.selectedVeggies?.length > 0) && (
                    <div>
                      Toppings: {[...(pizza.selectedMeats || []), ...(pizza.selectedVeggies || [])].map((t) => t.name).join(', ')}
                    </div>
                  )}
                  
                  {pizza.selectedDrinks && pizza.selectedDrinks.length > 0 && (
                    <div className="font-semibold text-stone-900">
                      Beverages: {pizza.selectedDrinks.map((d) => d.name).join(', ')}
                    </div>
                  )}
                </div>
              </div>
            ))}
          </div>

          {/* Cost Breakdown */}
          <div className="space-y-1.5 text-[11px] border-b-2 border-dashed border-stone-400 pb-3">
            <div className="flex justify-between">
              <span>Food Subtotal:</span>
              <span>${receipt.subtotal.toFixed(2)}</span>
            </div>
            {receipt.deliveryFee > 0 && (
              <div className="flex justify-between">
                <span>Delivery Fee:</span>
                <span>${receipt.deliveryFee.toFixed(2)}</span>
              </div>
            )}
            <div className="flex justify-between">
              <span>Sales Tax (8%):</span>
              <span>${receipt.tax.toFixed(2)}</span>
            </div>
            <div className="flex justify-between">
              <span>Tip:</span>
              <span>${receipt.tip.toFixed(2)}</span>
            </div>
            <div className="flex justify-between font-black text-sm text-stone-950 pt-1 border-t border-stone-300">
              <span>GRAND TOTAL:</span>
              <span>${receipt.grandTotal.toFixed(2)}</span>
            </div>
          </div>

          {/* Signature Display */}
          <div className="pt-2 space-y-2">
            <span className="text-[10px] uppercase tracking-wider text-stone-500 font-bold block">
              Authorized Signature
            </span>

            {receipt.signatureMode === 'draw' && receipt.signatureDataUrl ? (
              <div className="border-b border-stone-900 pb-1">
                <img
                  src={receipt.signatureDataUrl}
                  alt="Customer Signature"
                  className="h-14 max-w-full object-contain filter invert"
                />
              </div>
            ) : (
              <div className="border-b border-stone-900 pb-1 font-serif italic text-base text-stone-900">
                {receipt.typedSignature || receipt.customer.name}
              </div>
            )}
            <div className="flex justify-between text-[9px] text-stone-400">
              <span>Signature on File</span>
              <span>Thank you for your order!</span>
            </div>
          </div>

        </div>
      </div>
    </div>
  );
}