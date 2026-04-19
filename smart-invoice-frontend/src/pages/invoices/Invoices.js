import React, { useState, useEffect } from 'react';
import Sidebar from '../../components/Sidebar';
import { getAllInvoices, createInvoice, updateInvoiceStatus, deleteInvoice, downloadInvoicePdf } from '../../api/invoiceApi';
import { getAllClients } from '../../api/clientApi';
import { getAllProducts } from '../../api/productApi';
import { toast } from 'react-toastify';

const fmt = n => parseFloat(n || 0).toLocaleString('en-IN', { minimumFractionDigits: 2 });

const getBadge = s => {
  if (s === 'PAID')    return 'badge badge-paid';
  if (s === 'OVERDUE') return 'badge badge-overdue';
  return 'badge badge-unpaid';
};

const Invoices = () => {
  const [invoices, setInvoices]   = useState([]);
  const [clients, setClients]     = useState([]);
  const [products, setProducts]   = useState([]);
  const [loading, setLoading]     = useState(true);
  const [selected, setSelected]   = useState(null);
  const [modal, setModal]         = useState(false);
  const [busy, setBusy]           = useState(false);
  const [filter, setFilter]       = useState('ALL');

  const [form, setForm] = useState({ clientId: '', dueDate: '', notes: '', invoiceItems: [{ productId: '', quantity: 1 }] });

  useEffect(() => { loadAll(); }, []);

  const loadAll = async () => {
    setLoading(true);
    try {
      const [a, b, c] = await Promise.all([getAllInvoices(), getAllClients(), getAllProducts()]);
      setInvoices(a.data); setClients(b.data); setProducts(c.data);
      if (a.data.length > 0 && !selected) setSelected(a.data[0]);
    } catch { toast.error('Failed to load'); }
    finally { setLoading(false); }
  };

  const addLine    = () => setForm({ ...form, invoiceItems: [...form.invoiceItems, { productId: '', quantity: 1 }] });
  const removeLine = i => setForm({ ...form, invoiceItems: form.invoiceItems.filter((_, j) => j !== i) });
  const updateLine = (i, k, v) => { const items = [...form.invoiceItems]; items[i] = { ...items[i], [k]: v }; setForm({ ...form, invoiceItems: items }); };

  const calcPreview = () => form.invoiceItems.reduce((t, item) => {
    const p = products.find(x => x.id === parseInt(item.productId));
    if (!p) return t;
    const sub = p.price * item.quantity;
    return t + sub + sub * (p.taxPercentage || 0) / 100;
  }, 0);

  const openModal = () => {
    setForm({ clientId: '', dueDate: '', notes: '', invoiceItems: [{ productId: '', quantity: 1 }] });
    setModal(true);
  };

  const submit = async e => {
    e.preventDefault();
    if (form.invoiceItems.some(i => !i.productId)) { toast.error('Select a product for each line'); return; }
    setBusy(true);
    try {
      const payload = { ...form, clientId: parseInt(form.clientId), invoiceItems: form.invoiceItems.map(i => ({ productId: parseInt(i.productId), quantity: parseInt(i.quantity) })) };
      const res = await createInvoice(payload);
      toast.success('Invoice created!');
      setModal(false);
      await loadAll();
      setSelected(res.data);
    } catch (err) { toast.error(err.response?.data?.message || 'Failed'); }
    finally { setBusy(false); }
  };

  const changeStatus = async (id, status) => {
    try {
      await updateInvoiceStatus(id, status);
      toast.success(`Marked as ${status}`);
      await loadAll();
      if (selected?.id === id) setSelected(prev => ({ ...prev, status }));
    } catch { toast.error('Failed to update status'); }
  };

  const del = async id => {
    if (!window.confirm('Delete this invoice?')) return;
    try { await deleteInvoice(id); toast.success('Deleted!'); setSelected(null); loadAll(); }
    catch (err) { toast.error(err.response?.data?.message || 'Delete failed'); }
  };

  const downloadPdf = async (id, num) => {
    try {
      const res = await downloadInvoicePdf(id);
      const url = window.URL.createObjectURL(new Blob([res.data], { type: 'application/pdf' }));
      const a = document.createElement('a');
      a.href = url; a.setAttribute('download', `${num}.pdf`);
      document.body.appendChild(a); a.click(); a.remove();
      window.URL.revokeObjectURL(url);
      toast.success('PDF downloaded!');
    } catch { toast.error('Download failed'); }
  };

  const filtered = filter === 'ALL' ? invoices : invoices.filter(i => i.status === filter);

  return (
    <div className="app-layout">
      <Sidebar />
      <div className="main-content">

        <div className="topbar">
          <div className="topbar-title">
            <h1>Invoices</h1>
            <p>{invoices.length} total invoices</p>
          </div>
          <div className="topbar-actions">
            {/* Filter */}
            <div style={{ display: 'flex', gap: '4px', background: 'white', padding: '4px', borderRadius: '9px', border: '1px solid #e5e7eb' }}>
              {['ALL','UNPAID','PAID','OVERDUE'].map(f => (
                <button key={f} onClick={() => setFilter(f)} style={{ padding: '5px 12px', borderRadius: '6px', border: 'none', cursor: 'pointer', fontSize: '12px', fontWeight: '600', background: filter === f ? '#0d9488' : 'transparent', color: filter === f ? 'white' : '#6b7280', transition: 'all 0.15s' }}>
                  {f}
                </button>
              ))}
            </div>
            <button className="btn btn-primary" onClick={openModal}>
              <svg viewBox="0 0 24 24"><path d="M12 5v14M5 12h14"/></svg>
              New Invoice
            </button>
          </div>
        </div>

        {loading ? <div className="loading" /> : (
          <div style={{ display: 'grid', gridTemplateColumns: '340px 1fr', gap: '20px', alignItems: 'start' }}>

            {/* Left: Invoice List */}
            <div style={{ background: 'white', borderRadius: '12px', border: '1px solid #e5e7eb', overflow: 'hidden' }}>
              <div style={{ padding: '14px 18px', borderBottom: '1px solid #f3f4f6', display: 'flex', justifyContent: 'space-between', alignItems: 'center' }}>
                <span style={{ fontSize: '13px', fontWeight: '600', color: '#374151' }}>Invoice</span>
                <span style={{ fontSize: '12px', color: '#9ca3af' }}>{filtered.length} results</span>
              </div>

              {filtered.length === 0 ? (
                <div className="empty-state">
                  <svg viewBox="0 0 24 24"><path d="M14 2H6a2 2 0 00-2 2v16a2 2 0 002 2h12a2 2 0 002-2V8z"/><path d="M14 2v6h6M16 13H8M16 17H8M10 9H8"/></svg>
                  <h3>No invoices found</h3>
                  <p>Create your first invoice</p>
                </div>
              ) : filtered.map(inv => (
                <div
                  key={inv.id}
                  className={`inv-list-item ${selected?.id === inv.id ? 'active' : ''}`}
                  onClick={() => setSelected(inv)}>
                  <div>
                    <div className="inv-num">{inv.invoiceNumber}</div>
                    <div className="inv-list-item" style={{ padding: 0, border: 'none', cursor: 'default' }}>
                      <h4 style={{ fontSize: '13px', fontWeight: '600', color: '#111827' }}>{inv.clientName}</h4>
                    </div>
                    <div className="inv-date">{inv.invoiceDate} {inv.dueDate && `· Due ${inv.dueDate}`}</div>
                  </div>
                  <div style={{ textAlign: 'right' }}>
                    <div className="inv-amount">₹{fmt(inv.totalAmount)}</div>
                    <span className={getBadge(inv.status)} style={{ marginTop: '4px', display: 'inline-flex' }}>{inv.status}</span>
                  </div>
                </div>
              ))}
            </div>

            {/* Right: Detail Panel */}
            {selected ? (
              <div className="inv-detail">
                <div className="inv-detail-header">
                  <div>
                    <h2>{selected.invoiceNumber}</h2>
                    <div className="inv-date-sub">
                      {selected.status === 'PAID' ? 'Paid' : 'Issued'} on {selected.invoiceDate}
                    </div>
                    <span className={getBadge(selected.status)} style={{ marginTop: '8px', display: 'inline-flex' }}>{selected.status}</span>
                  </div>
                  <div style={{ display: 'flex', gap: '8px', alignItems: 'flex-start' }}>
                    <select
                      className="form-control"
                      style={{ width: 'auto', fontSize: '12px', padding: '6px 10px' }}
                      value={selected.status}
                      onChange={e => changeStatus(selected.id, e.target.value)}>
                      <option value="UNPAID">Mark Unpaid</option>
                      <option value="PAID">Mark Paid</option>
                      <option value="OVERDUE">Mark Overdue</option>
                    </select>
                    <button className="btn btn-primary" style={{ padding: '7px 12px', fontSize: '12px' }} onClick={() => downloadPdf(selected.id, selected.invoiceNumber)}>
                      <svg viewBox="0 0 24 24"><path d="M21 15v4a2 2 0 01-2 2H5a2 2 0 01-2-2v-4"/><polyline points="7 10 12 15 17 10"/><line x1="12" y1="15" x2="12" y2="3"/></svg>
                      PDF
                    </button>
                    <button className="btn-icon danger" onClick={() => del(selected.id)}>
                      <svg viewBox="0 0 24 24"><polyline points="3 6 5 6 21 6"/><path d="M19 6l-1 14a2 2 0 01-2 2H8a2 2 0 01-2-2L5 6"/><path d="M10 11v6M14 11v6"/></svg>
                    </button>
                  </div>
                </div>

                <div className="inv-detail-body">
                  <div className="detail-grid">
                    <div>
                      <div className="detail-label">Client</div>
                      <div className="detail-value">{selected.clientName}</div>
                    </div>
                    <div>
                      <div className="detail-label">Total Amount</div>
                      <div className="detail-value big">₹{fmt(selected.totalAmount)}</div>
                    </div>
                    <div>
                      <div className="detail-label">Invoice Date</div>
                      <div className="detail-value">{selected.invoiceDate}</div>
                    </div>
                    <div>
                      <div className="detail-label">Due Date</div>
                      <div className="detail-value">{selected.dueDate || '—'}</div>
                    </div>
                  </div>

                  {/* Items */}
                  <div className="items-table-wrap">
                    <table>
                      <thead>
                        <tr>
                          <th>#</th><th>Item & Description</th>
                          <th style={{ textAlign: 'right' }}>Qty</th>
                          <th style={{ textAlign: 'right' }}>Rate</th>
                          <th style={{ textAlign: 'right' }}>Tax</th>
                          <th style={{ textAlign: 'right' }}>Amount</th>
                        </tr>
                      </thead>
                      <tbody>
                        {(selected.invoiceItems || []).map((item, i) => (
                          <tr key={i}>
                            <td className="td-muted">{i + 1}</td>
                            <td className="td-primary">{item.productName}</td>
                            <td style={{ textAlign: 'right' }}>{item.quantity}</td>
                            <td style={{ textAlign: 'right' }}>₹{fmt(item.unitPrice)}</td>
                            <td style={{ textAlign: 'right', color: '#6b7280' }}>₹{fmt(item.taxAmount)}</td>
                            <td style={{ textAlign: 'right' }}><strong>₹{fmt(parseFloat(item.subtotal) + parseFloat(item.taxAmount))}</strong></td>
                          </tr>
                        ))}
                      </tbody>
                    </table>
                  </div>

                  {/* Totals */}
                  <div className="totals-section" style={{ marginTop: '16px' }}>
                    {(() => {
                      const sub = (selected.invoiceItems || []).reduce((t, i) => t + parseFloat(i.subtotal || 0), 0);
                      const tax = (selected.invoiceItems || []).reduce((t, i) => t + parseFloat(i.taxAmount || 0), 0);
                      return (<>
                        <div className="total-row"><span className="t-label">Sub Total</span><span className="t-value">₹{fmt(sub)}</span></div>
                        <div className="total-row"><span className="t-label">Total Tax</span><span className="t-value">₹{fmt(tax)}</span></div>
                        <div className="total-row grand"><span className="t-label">Balance Due</span><span className="t-value">₹{fmt(selected.totalAmount)}</span></div>
                      </>);
                    })()}
                  </div>

                  {selected.notes && (
                    <div style={{ marginTop: '20px', padding: '12px 16px', background: '#fffbeb', borderRadius: '8px', border: '1px solid #fde68a', fontSize: '13px', color: '#92400e' }}>
                      <strong>Notes:</strong> {selected.notes}
                    </div>
                  )}
                </div>
              </div>
            ) : (
              <div className="inv-detail" style={{ display: 'flex', alignItems: 'center', justifyContent: 'center', minHeight: '400px' }}>
                <div className="empty-state">
                  <svg viewBox="0 0 24 24"><path d="M14 2H6a2 2 0 00-2 2v16a2 2 0 002 2h12a2 2 0 002-2V8z"/></svg>
                  <h3>Select an invoice</h3>
                  <p>Click any invoice from the list to view details</p>
                </div>
              </div>
            )}
          </div>
        )}

        {/* Create Invoice Modal */}
        {modal && (
          <div className="modal-overlay">
            <div className="modal" style={{ maxWidth: '680px' }}>
              <div className="modal-header">
                <h2>Create New Invoice</h2>
                <button className="btn-icon" onClick={() => setModal(false)}>
                  <svg viewBox="0 0 24 24"><path d="M18 6L6 18M6 6l12 12"/></svg>
                </button>
              </div>
              <form onSubmit={submit}>
                <div className="modal-body">
                  <div className="form-row">
                    <div className="form-group">
                      <label>Client *</label>
                      <select className="form-control" value={form.clientId} onChange={e => setForm({ ...form, clientId: e.target.value })} required>
                        <option value="">— Select Client —</option>
                        {clients.map(c => <option key={c.id} value={c.id}>{c.name}</option>)}
                      </select>
                    </div>
                    <div className="form-group">
                      <label>Due Date</label>
                      <input className="form-control" type="date" value={form.dueDate} onChange={e => setForm({ ...form, dueDate: e.target.value })} />
                    </div>
                  </div>

                  {/* Line Items */}
                  <div style={{ marginBottom: '16px' }}>
                    <div style={{ display: 'flex', justifyContent: 'space-between', alignItems: 'center', marginBottom: '10px' }}>
                      <label style={{ fontSize: '12px', fontWeight: '600', color: '#374151', textTransform: 'uppercase', letterSpacing: '0.4px' }}>Products / Services *</label>
                      <button type="button" className="btn btn-ghost" style={{ padding: '5px 10px', fontSize: '12px' }} onClick={addLine}>
                        <svg viewBox="0 0 24 24"><path d="M12 5v14M5 12h14"/></svg>
                        Add Line
                      </button>
                    </div>

                    <div style={{ border: '1px solid #e5e7eb', borderRadius: '10px', overflow: 'hidden' }}>
                      <div style={{ display: 'grid', gridTemplateColumns: '2.5fr 0.8fr 1.2fr 32px', gap: '0', background: '#f9fafb', padding: '8px 12px', borderBottom: '1px solid #e5e7eb' }}>
                        {['Product', 'Qty', 'Amount', ''].map(h => <span key={h} style={{ fontSize: '11px', fontWeight: '600', color: '#6b7280', textTransform: 'uppercase' }}>{h}</span>)}
                      </div>

                      {form.invoiceItems.map((item, i) => {
                        const prod = products.find(p => p.id === parseInt(item.productId));
                        const sub  = prod ? prod.price * item.quantity : 0;
                        const tax  = prod ? sub * (prod.taxPercentage || 0) / 100 : 0;
                        return (
                          <div key={i} style={{ display: 'grid', gridTemplateColumns: '2.5fr 0.8fr 1.2fr 32px', gap: '8px', padding: '10px 12px', borderBottom: i < form.invoiceItems.length - 1 ? '1px solid #f3f4f6' : 'none', alignItems: 'center' }}>
                            <select className="form-control" style={{ fontSize: '13px', padding: '7px 10px' }} value={item.productId} onChange={e => updateLine(i, 'productId', e.target.value)}>
                              <option value="">— Select Product —</option>
                              {products.map(p => <option key={p.id} value={p.id}>{p.name} (₹{parseFloat(p.price).toLocaleString('en-IN')})</option>)}
                            </select>
                            <input className="form-control" type="number" style={{ fontSize: '13px', padding: '7px 10px' }} value={item.quantity} min="1" onChange={e => updateLine(i, 'quantity', e.target.value)} />
                            <div style={{ fontSize: '13px', fontWeight: '600', color: '#0d9488', textAlign: 'right' }}>
                              ₹{fmt(sub + tax)}
                              {prod && <div style={{ fontSize: '11px', color: '#9ca3af', fontWeight: '400' }}>incl. {prod.taxPercentage || 0}% tax</div>}
                            </div>
                            <button type="button" className="btn-icon danger" style={{ width: '28px', height: '28px' }} onClick={() => removeLine(i)}>
                              <svg viewBox="0 0 24 24"><path d="M18 6L6 18M6 6l12 12"/></svg>
                            </button>
                          </div>
                        );
                      })}
                    </div>

                    <div style={{ display: 'flex', justifyContent: 'flex-end', marginTop: '12px', padding: '12px 16px', background: '#f0fdfa', borderRadius: '8px', border: '1px solid #99f6e4' }}>
                      <span style={{ fontSize: '15px', fontWeight: '700', color: '#0d9488' }}>Total: ₹{fmt(calcPreview())}</span>
                    </div>
                  </div>

                  <div className="form-group">
                    <label>Notes</label>
                    <textarea className="form-control" value={form.notes} onChange={e => setForm({ ...form, notes: e.target.value })} placeholder="Payment terms, delivery notes..." rows="2" style={{ resize: 'vertical' }} />
                  </div>
                </div>
                <div className="modal-footer">
                  <button type="button" className="btn btn-ghost" onClick={() => setModal(false)}>Cancel</button>
                  <button type="submit" className="btn btn-primary" disabled={busy}>{busy ? 'Creating...' : 'Create Invoice'}</button>
                </div>
              </form>
            </div>
          </div>
        )}
      </div>
    </div>
  );
};

export default Invoices;