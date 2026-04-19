import React, { useState, useEffect } from 'react';
import { useNavigate } from 'react-router-dom';
import Sidebar from '../components/Sidebar';
import { getDashboardSummary } from '../api/invoiceApi';
import { useAuth } from '../context/AuthContext';

const StatCard = ({ value, label, color, icon }) => (
  <div className={`stat-card ${color}`}>
    <div className="stat-card-icon">
      <svg viewBox="0 0 24 24" xmlns="http://www.w3.org/2000/svg">
        <path d={icon} />
      </svg>
    </div>
    <h3>{value}</h3>
    <p>{label}</p>
  </div>
);

const Dashboard = () => {
  const [summary, setSummary] = useState(null);
  const [loading, setLoading] = useState(true);
  const { user } = useAuth();
  const navigate = useNavigate();

  useEffect(() => {
    getDashboardSummary()
      .then(r => setSummary(r.data))
      .catch(() => {})
      .finally(() => setLoading(false));
  }, []);

  const stats = summary ? [
    { value: summary.totalInvoices,  label: 'Total Invoices',   color: 'teal',   icon: 'M14 2H6a2 2 0 00-2 2v16a2 2 0 002 2h12a2 2 0 002-2V8z M14 2v6h6' },
    { value: summary.paidInvoices,   label: 'Paid',             color: 'green',  icon: 'M9 11l3 3L22 4 M21 12v7a2 2 0 01-2 2H5a2 2 0 01-2-2V5a2 2 0 012-2h11' },
    { value: summary.unpaidInvoices, label: 'Unpaid',           color: 'amber',  icon: 'M12 22s8-4 8-10V5l-8-3-8 3v7c0 6 8 10 8 10z' },
    { value: summary.overdueInvoices,label: 'Overdue',          color: 'red',    icon: 'M10.29 3.86L1.82 18a2 2 0 001.71 3h16.94a2 2 0 001.71-3L13.71 3.86a2 2 0 00-3.42 0z M12 9v4 M12 17h.01' },
    { value: summary.totalClients,   label: 'Clients',          color: 'blue',   icon: 'M17 21v-2a4 4 0 00-4-4H5a4 4 0 00-4 4v2 M9 11a4 4 0 100-8 4 4 0 000 8z' },
    { value: summary.totalProducts,  label: 'Products',         color: 'teal',   icon: 'M21 16V8a2 2 0 00-1-1.73l-7-4a2 2 0 00-2 0l-7 4A2 2 0 003 8v8a2 2 0 001 1.73l7 4a2 2 0 002 0l7-4A2 2 0 0021 16z' },
  ] : [];

  return (
    <div className="app-layout">
      <Sidebar />
      <div className="main-content">
        <div className="topbar">
          <div className="topbar-title">
            <h1>Invoice Management</h1>
            <p>Dashboard — Welcome back, {user?.username}!</p>
          </div>
          <div className="topbar-actions">
            <button className="btn btn-primary" onClick={() => navigate('/invoices')}>
              <svg viewBox="0 0 24 24"><path d="M12 5v14M5 12h14"/></svg>
              New Invoice
            </button>
          </div>
        </div>

        {loading ? (
          <div className="loading">Loading dashboard...</div>
        ) : (
          <>
            <div className="stat-grid">
              {stats.map((s, i) => <StatCard key={i} {...s} />)}
            </div>

            <div style={{ display: 'grid', gridTemplateColumns: '1fr 1fr', gap: '20px' }}>
              <div className="card">
                <div className="card-header">
                  <h2>Quick Actions</h2>
                </div>
                <div style={{ display: 'flex', flexDirection: 'column', gap: '10px' }}>
                  {[
                    { label: 'Create New Invoice', path: '/invoices', color: 'btn-primary' },
                    { label: 'Add New Client',     path: '/clients',  color: 'btn-ghost' },
                    { label: 'Add New Product',    path: '/products', color: 'btn-ghost' },
                  ].map(a => (
                    <button key={a.label} className={`btn ${a.color}`} style={{ justifyContent: 'flex-start' }} onClick={() => navigate(a.path)}>
                      {a.label}
                    </button>
                  ))}
                </div>
              </div>

              <div className="card">
                <div className="card-header"><h2>Invoice Status</h2></div>
                {summary && [
                  { label: 'Paid',    value: summary.paidInvoices,    color: '#22c55e', total: summary.totalInvoices },
                  { label: 'Unpaid',  value: summary.unpaidInvoices,  color: '#eab308', total: summary.totalInvoices },
                  { label: 'Overdue', value: summary.overdueInvoices, color: '#ef4444', total: summary.totalInvoices },
                ].map(row => (
                  <div key={row.label} style={{ marginBottom: '14px' }}>
                    <div style={{ display: 'flex', justifyContent: 'space-between', fontSize: '12px', color: '#6b7280', marginBottom: '5px' }}>
                      <span>{row.label}</span>
                      <span style={{ fontWeight: '600', color: '#111827' }}>{row.value}</span>
                    </div>
                    <div style={{ height: '6px', background: '#f3f4f6', borderRadius: '3px', overflow: 'hidden' }}>
                      <div style={{ height: '100%', width: `${row.total ? (row.value / row.total) * 100 : 0}%`, background: row.color, borderRadius: '3px', transition: 'width 0.6s ease' }} />
                    </div>
                  </div>
                ))}
              </div>
            </div>
          </>
        )}
      </div>
    </div>
  );
};

export default Dashboard;