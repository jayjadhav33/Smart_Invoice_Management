import axiosInstance from './axiosConfig';

export const getAllProducts = () =>
  axiosInstance.get('/api/products');

export const getProductById = (id) =>
  axiosInstance.get(`/api/products/${id}`);

export const createProduct = (data) =>
  axiosInstance.post('/api/products', data);

export const updateProduct = (id, data) =>
  axiosInstance.put(`/api/products/${id}`, data);

export const deleteProduct = (id) =>
  axiosInstance.delete(`/api/products/${id}`);