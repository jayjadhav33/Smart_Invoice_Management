import React, { useState, useEffect } from 'react';
import Sidebar from '../../components/Sidebar';
import { getAllProducts, createProduct, updateProduct, deleteProduct } from '../../api/productApi';
import { toast } from 'react-toastify';

const empty = { name: '', price: '', taxPercentage: '', description: '' };

const Products = () => {
  const [products, setProducts] = useState([]);
  const [loading, setLoading]   = useState(true);
  const [modal, setModal]       = useState(false);
  const [edit, setEdit]         = useState(null);
  const [form, setForm]         = useState(empty);
  const [busy, setBusy]         = useState(false);

  useEffect(() => { load(); }, []);

  const load = async () => {
    setLoading(true);
    try { const r = await getAllProducts(); setProducts(r.data); }
    catch { toast.error('Failed to load products'); }
    finally { setLoading(false); }
  };

  const openAdd  = () => { setForm(empty); setEdit(null); setModal(true); };
  const openEdit = p => { setForm({ name: p.name, price: p.price, taxPercentage: p.taxPercentage || '', description: p.description || '' }); setEdit(p.id); setModal(true); };
  const handle   = e => setForm({ ...form, [e.target.name]: e.target.value });

  const submit = async e => {
    e.preventDefault(); setBusy(true);
    const payload = { ...form, price: parseFloat(form.price), taxPercentage: form.taxPercentage ? parseFloat(form.taxPercentage) : 0 };
    try {
      edit ? await updateProduct(edit, payload) : await createProduct(payload);
      toast.success(edit ? 'Product updated!' : 'Product added!');
      setModal(false); load();
    } catch (err) { toast.error(err.response?.data?.message || 'Failed'); }
    finally { setBusy(false); }
  };

  const del = async (id, name) => {
    if (!window.confirm(`Delete "${name}"?`)) return;
    try { await deleteProduct(id); toast.success('Deleted!'); load(); }
    catch (err) { toast.error(err.response?.data?.message || 'Delete failed'); }
  };

  return (
    <div className="app-layout">
      <Sidebar />
      <div className="main-content">

        <div className="topbar">
          <div className="topbar-title">
            <h1>Products & Services</h1>
            <p>Manage your product catalog</p>
          </div>
          <button className="btn btn-primary" onClick={openAdd}>
            <svg viewBox="0 0 24 24"><path d="M12 5v14M5 12h14"/></svg>
            Add Product
          </button>
        </div>

        {loading ? <div className="loading" /> : (
          <div className="table-wrap">
            <table>
              <thead>
                <tr><th>#</th><th>Product / Service</th><th>Price</th><th>Tax %</th><th>Description</th><th>Actions</th></tr>
              </thead>
              <tbody>
                {products.length === 0 ? (
                  <tr><td colSpan="6">
                    <div className="empty-state">
                      <svg viewBox="0 0 24 24"><path d="M21 16V8a2 2 0 00-1-1.73l-7-4a2 2 0 00-2 0l-7 4A2 2 0 003 8v8a2 2 0 001 1.73l7 4a2 2 0 002 0l7-4A2 2 0 0021 16z"/></svg>
                      <h3>No products yet</h3>
                      <p>Add your first product or service</p>
                    </div>
                  </td></tr>
                ) : products.map((p, i) => (
                  <tr key={p.id}>
                    <td className="td-muted">{i + 1}</td>
                    <td className="td-primary">{p.name}</td>
                    <td><strong style={{ color: '#0d9488' }}>₹{parseFloat(p.price).toLocaleString('en-IN', { minimumFractionDigits: 2 })}</strong></td>
                    <td>
                      <span style={{ background: '#f0fdfa', color: '#0f766e', padding: '2px 8px', borderRadius: '12px', fontSize: '12px', fontWeight: '600' }}>
                        {p.taxPercentage || 0}%
                      </span>
                    </td>
                    <td style={{ maxWidth: '200px', overflow: 'hidden', textOverflow: 'ellipsis', whiteSpace: 'nowrap', color: '#6b7280' }}>
                      {p.description || <span className="td-muted">—</span>}
                    </td>
                    <td>
                      <div style={{ display: 'flex', gap: '6px' }}>
                        <button className="btn-icon" onClick={() => openEdit(p)}>
                          <svg viewBox="0 0 24 24"><path d="M11 4H4a2 2 0 00-2 2v14a2 2 0 002 2h14a2 2 0 002-2v-7"/><path d="M18.5 2.5a2.121 2.121 0 013 3L12 15l-4 1 1-4 9.5-9.5z"/></svg>
                        </button>
                        <button className="btn-icon danger" onClick={() => del(p.id, p.name)}>
                          <svg viewBox="0 0 24 24"><polyline points="3 6 5 6 21 6"/><path d="M19 6l-1 14a2 2 0 01-2 2H8a2 2 0 01-2-2L5 6"/><path d="M10 11v6M14 11v6"/><path d="M9 6V4a1 1 0 011-1h4a1 1 0 011 1v2"/></svg>
                        </button>
                      </div>
                    </td>
                  </tr>
                ))}
              </tbody>
            </table>
          </div>
        )}

        {modal && (
          <div className="modal-overlay">
            <div className="modal">
              <div className="modal-header">
                <h2>{edit ? 'Edit Product' : 'Add New Product'}</h2>
                <button className="btn-icon" onClick={() => setModal(false)}>
                  <svg viewBox="0 0 24 24"><path d="M18 6L6 18M6 6l12 12"/></svg>
                </button>
              </div>
              <form onSubmit={submit}>
                <div className="modal-body">
                  <div className="form-group">
                    <label>Product / Service Name *</label>
                    <input className="form-control" name="name" value={form.name} onChange={handle} placeholder="e.g. Web Development Service" required />
                  </div>
                  <div className="form-row">
                    <div className="form-group">
                      <label>Price (₹) *</label>
                      <input className="form-control" type="number" name="price" value={form.price} onChange={handle} placeholder="0.00" step="0.01" min="0" required />
                    </div>
                    <div className="form-group">
                      <label>Tax Percentage (%)</label>
                      <input className="form-control" type="number" name="taxPercentage" value={form.taxPercentage} onChange={handle} placeholder="18" step="0.01" min="0" max="100" />
                    </div>
                  </div>
                  <div className="form-group">
                    <label>Description</label>
                    <textarea className="form-control" name="description" value={form.description} onChange={handle} placeholder="Brief description" rows="2" style={{ resize: 'vertical' }} />
                  </div>
                </div>
                <div className="modal-footer">
                  <button type="button" className="btn btn-ghost" onClick={() => setModal(false)}>Cancel</button>
                  <button type="submit" className="btn btn-primary" disabled={busy}>{busy ? 'Saving...' : edit ? 'Update' : 'Add Product'}</button>
                </div>
              </form>
            </div>
          </div>
        )}
      </div>
    </div>
  );
};

export default Products;