import axiosInstance from './axiosConfig';

export const getAllInvoices = () =>
  axiosInstance.get('/api/invoices');

export const getInvoiceById = (id) =>
  axiosInstance.get(`/api/invoices/${id}`);

export const createInvoice = (data) =>
  axiosInstance.post('/api/invoices', data);

export const updateInvoiceStatus = (id, status) =>
  axiosInstance.patch(
    `/api/invoices/${id}/status?status=${status}`);

export const deleteInvoice = (id) =>
  axiosInstance.delete(`/api/invoices/${id}`);

export const getDashboardSummary = () =>
  axiosInstance.get('/api/invoices/dashboard/summary');

export const getInvoicesByStatus = (status) =>
  axiosInstance.get(`/api/invoices/status/${status}`);

export const downloadInvoicePdf = (id) =>
  axiosInstance.get(`/api/invoices/${id}/pdf`, {
    responseType: 'blob'   // ← important for binary data
  });