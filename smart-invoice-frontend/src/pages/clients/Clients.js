import React, { useState, useEffect } from 'react';
import Sidebar from '../../components/Sidebar';
import { getAllClients, createClient, updateClient, deleteClient } from '../../api/clientApi';
import { toast } from 'react-toastify';

const empty = { name: '', email: '', phone: '', address: '', gstNumber: '' };

const Clients = () => {
  const [clients, setClients] = useState([]);
  const [loading, setLoading] = useState(true);
  const [modal, setModal]     = useState(false);
  const [edit, setEdit]       = useState(null);
  const [form, setForm]       = useState(empty);
  const [busy, setBusy]       = useState(false);

  useEffect(() => { load(); }, []);

  const load = async () => {
    setLoading(true);
    try { const r = await getAllClients(); setClients(r.data); }
    catch { toast.error('Failed to load clients'); }
    finally { setLoading(false); }
  };

  const openAdd  = () => { setForm(empty); setEdit(null); setModal(true); };
  const openEdit = c => { setForm({ name: c.name, email: c.email, phone: c.phone || '', address: c.address || '', gstNumber: c.gstNumber || '' }); setEdit(c.id); setModal(true); };
  const handle   = e => setForm({ ...form, [e.target.name]: e.target.value });

  const submit = async e => {
    e.preventDefault(); setBusy(true);
    try {
      edit ? await updateClient(edit, form) : await createClient(form);
      toast.success(edit ? 'Client updated!' : 'Client added!');
      setModal(false); load();
    } catch (err) { toast.error(err.response?.data?.message || 'Failed'); }
    finally { setBusy(false); }
  };

  const del = async (id, name) => {
    if (!window.confirm(`Delete "${name}"?`)) return;
    try { await deleteClient(id); toast.success('Deleted!'); load(); }
    catch (err) { toast.error(err.response?.data?.message || 'Delete failed'); }
  };

  return (
    <div className="app-layout">
      <Sidebar />
      <div className="main-content">

        <div className="topbar">
          <div className="topbar-title">
            <h1>Clients</h1>
            <p>Manage your client directory</p>
          </div>
          <button className="btn btn-primary" onClick={openAdd}>
            <svg viewBox="0 0 24 24"><path d="M12 5v14M5 12h14"/></svg>
            Add Client
          </button>
        </div>

        {loading ? <div className="loading" /> : (
          <div className="table-wrap">
            <table>
              <thead>
                <tr>
                  <th>#</th><th>Client Name</th><th>Email</th>
                  <th>Phone</th><th>GST Number</th><th>Actions</th>
                </tr>
              </thead>
              <tbody>
                {clients.length === 0 ? (
                  <tr><td colSpan="6">
                    <div className="empty-state">
                      <svg viewBox="0 0 24 24"><path d="M17 21v-2a4 4 0 00-4-4H5a4 4 0 00-4 4v2"/><circle cx="9" cy="7" r="4"/><path d="M23 21v-2a4 4 0 00-3-3.87M16 3.13a4 4 0 010 7.75"/></svg>
                      <h3>No clients yet</h3>
                      <p>Add your first client to get started</p>
                    </div>
                  </td></tr>
                ) : clients.map((c, i) => (
                  <tr key={c.id}>
                    <td className="td-muted">{i + 1}</td>
                    <td className="td-primary">{c.name}</td>
                    <td>{c.email}</td>
                    <td>{c.phone || <span className="td-muted">—</span>}</td>
                    <td><span className="td-mono">{c.gstNumber || <span className="td-muted">—</span>}</span></td>
                    <td>
                      <div style={{ display: 'flex', gap: '6px' }}>
                        <button className="btn-icon" title="Edit" onClick={() => openEdit(c)}>
                          <svg viewBox="0 0 24 24"><path d="M11 4H4a2 2 0 00-2 2v14a2 2 0 002 2h14a2 2 0 002-2v-7"/><path d="M18.5 2.5a2.121 2.121 0 013 3L12 15l-4 1 1-4 9.5-9.5z"/></svg>
                        </button>
                        <button className="btn-icon danger" title="Delete" onClick={() => del(c.id, c.name)}>
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
                <h2>{edit ? 'Edit Client' : 'Add New Client'}</h2>
                <button className="btn-icon" onClick={() => setModal(false)}>
                  <svg viewBox="0 0 24 24"><path d="M18 6L6 18M6 6l12 12"/></svg>
                </button>
              </div>
              <form onSubmit={submit}>
                <div className="modal-body">
                  <div className="form-row">
                    <div className="form-group">
                      <label>Full Name *</label>
                      <input className="form-control" name="name" value={form.name} onChange={handle} placeholder="Client or company name" required />
                    </div>
                    <div className="form-group">
                      <label>Email *</label>
                      <input className="form-control" type="email" name="email" value={form.email} onChange={handle} placeholder="client@email.com" required />
                    </div>
                  </div>
                  <div className="form-row">
                    <div className="form-group">
                      <label>Phone</label>
                      <input className="form-control" name="phone" value={form.phone} onChange={handle} placeholder="+91 98765 43210" />
                    </div>
                    <div className="form-group">
                      <label>GST Number</label>
                      <input className="form-control" name="gstNumber" value={form.gstNumber} onChange={handle} placeholder="27AAPFU0939F1ZV" />
                    </div>
                  </div>
                  <div className="form-group">
                    <label>Address</label>
                    <textarea className="form-control" name="address" value={form.address} onChange={handle} placeholder="Full address" rows="2" style={{ resize: 'vertical' }} />
                  </div>
                </div>
                <div className="modal-footer">
                  <button type="button" className="btn btn-ghost" onClick={() => setModal(false)}>Cancel</button>
                  <button type="submit" className="btn btn-primary" disabled={busy}>{busy ? 'Saving...' : edit ? 'Update Client' : 'Add Client'}</button>
                </div>
              </form>
            </div>
          </div>
        )}
      </div>
    </div>
  );
};

export default Clients;