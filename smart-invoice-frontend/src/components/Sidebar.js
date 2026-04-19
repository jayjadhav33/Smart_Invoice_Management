import React from 'react';
import { NavLink, useNavigate } from 'react-router-dom';
import { useAuth } from '../context/AuthContext';

const Icon = ({ d, viewBox = '0 0 24 24' }) => (
  <svg viewBox={viewBox} xmlns="http://www.w3.org/2000/svg">
    <path d={d} />
  </svg>
);

const navItems = [
  {
    to: '/dashboard', label: 'Home',
    d: 'M3 9l9-7 9 7v11a2 2 0 01-2 2H5a2 2 0 01-2-2z M9 22V12h6v10'
  },
  {
    to: '/clients', label: 'Clients',
    d: 'M17 21v-2a4 4 0 00-4-4H5a4 4 0 00-4 4v2 M9 11a4 4 0 100-8 4 4 0 000 8z M23 21v-2a4 4 0 00-3-3.87 M16 3.13a4 4 0 010 7.75'
  },
  {
    to: '/products', label: 'Products',
    d: 'M21 16V8a2 2 0 00-1-1.73l-7-4a2 2 0 00-2 0l-7 4A2 2 0 003 8v8a2 2 0 001 1.73l7 4a2 2 0 002 0l7-4A2 2 0 0021 16z'
  },
  {
    to: '/invoices', label: 'Invoices',
    d: 'M14 2H6a2 2 0 00-2 2v16a2 2 0 002 2h12a2 2 0 002-2V8z M14 2v6h6 M16 13H8 M16 17H8 M10 9H8'
  },
];

const Sidebar = () => {
  const { user, logout } = useAuth();
  const navigate = useNavigate();

  const initials = user?.username
    ? user.username.substring(0, 2).toUpperCase()
    : 'U';

  return (
    <div className="sidebar">
      <div className="sidebar-logo">
        <svg viewBox="0 0 24 24" xmlns="http://www.w3.org/2000/svg">
          <path d="M14 2H6a2 2 0 00-2 2v16a2 2 0 002 2h12a2 2 0 002-2V8z"/>
          <path d="M14 2v6h6M16 13H8M16 17H8M10 9H8" stroke="white" strokeWidth="2" fill="none" strokeLinecap="round"/>
        </svg>
      </div>

      <ul className="sidebar-nav">
        {navItems.map(item => (
          <li key={item.to}>
            <NavLink to={item.to}>
              <svg viewBox="0 0 24 24" xmlns="http://www.w3.org/2000/svg">
                {item.d.split(' M').map((seg, i) => (
                  <path key={i} d={i === 0 ? seg : 'M' + seg} />
                ))}
              </svg>
              {item.label}
            </NavLink>
          </li>
        ))}
      </ul>

      <div className="sidebar-footer">
        <div className="sidebar-avatar" title={user?.username}>
          {initials}
        </div>
        <button
          className="sidebar-logout"
          onClick={() => { logout(); navigate('/login'); }}>
          <svg viewBox="0 0 24 24" xmlns="http://www.w3.org/2000/svg">
            <path d="M9 21H5a2 2 0 01-2-2V5a2 2 0 012-2h4M16 17l5-5-5-5M21 12H9"/>
          </svg>
          Out
        </button>
      </div>
    </div>
  );
};

export default Sidebar;