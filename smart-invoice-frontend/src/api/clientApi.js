import axiosInstance from './axiosConfig';

export const getAllClients = () =>
  axiosInstance.get('/api/clients');

export const getClientById = (id) =>
  axiosInstance.get(`/api/clients/${id}`);

export const createClient = (data) =>
  axiosInstance.post('/api/clients', data);

export const updateClient = (id, data) =>
  axiosInstance.put(`/api/clients/${id}`, data);

export const deleteClient = (id) =>
  axiosInstance.delete(`/api/clients/${id}`);