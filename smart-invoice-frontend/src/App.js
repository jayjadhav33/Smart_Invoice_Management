import React from 'react';
import {
  BrowserRouter as Router,
  Routes,
  Route,
  Navigate
} from 'react-router-dom';
import { ToastContainer } from 'react-toastify';
import 'react-toastify/dist/ReactToastify.css';

import { AuthProvider, useAuth } from './context/AuthContext';
import Login     from './pages/auth/Login';
import Register  from './pages/auth/Register';
import Dashboard from './pages/Dashboard';
import Clients   from './pages/clients/Clients';
import Products  from './pages/products/Products';
import Invoices  from './pages/invoices/Invoices';

// Guard wrapper for protected routes
const ProtectedRoute = ({ children }) => {
  const { user } = useAuth();
  return user
    ? children
    : <Navigate to="/login" replace />;
};

function AppRoutes() {
  return (
    <Routes>
      {/* Public */}
      <Route path="/login"    element={<Login />} />
      <Route path="/register" element={<Register />} />

      {/* Protected */}
      <Route path="/dashboard"
        element={
          <ProtectedRoute><Dashboard /></ProtectedRoute>
        }
      />
      <Route path="/clients"
        element={
          <ProtectedRoute><Clients /></ProtectedRoute>
        }
      />
      <Route path="/products"
        element={
          <ProtectedRoute><Products /></ProtectedRoute>
        }
      />
      <Route path="/invoices"
        element={
          <ProtectedRoute><Invoices /></ProtectedRoute>
        }
      />

      {/* Default */}
      <Route path="/"
        element={<Navigate to="/dashboard" replace />} />
      <Route path="*"
        element={<Navigate to="/login" replace />} />
    </Routes>
  );
}

function App() {
  return (
    <AuthProvider>
      <Router>
        <ToastContainer
          position="top-right"
          autoClose={3000}
        />
        <AppRoutes />
      </Router>
    </AuthProvider>
  );
}

export default App;