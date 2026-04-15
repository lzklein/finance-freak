const BASE_URL = 'http://localhost:8080/api';

const getToken = () => localStorage.getItem('token');

const headers = () => ({
  'Content-Type': 'application/json',
  ...(getToken() && { 'Authorization': `Bearer ${getToken()}` })
});

// AUTH
export const registerUser = (data) =>
  fetch(`${BASE_URL}/auth/register`, {
    method: 'POST',
    headers: headers(),
    body: JSON.stringify(data)
  }).then(res => res.json());

export const loginUser = (data) =>
  fetch(`${BASE_URL}/auth/login`, {
    method: 'POST',
    headers: headers(),
    body: JSON.stringify(data)
  }).then(res => res.json());

// USER
export const getCurrentUser = () =>
  fetch(`${BASE_URL}/users/me`, {
    headers: headers()
  }).then(res => res.json());

export const updatePassword = (newPassword) =>
  fetch(`${BASE_URL}/users/me/password`, {
    method: 'PATCH',
    headers: headers(),
    body: JSON.stringify(newPassword)
  }).then(res => res.json());

export const deleteUser = () =>
  fetch(`${BASE_URL}/users/me`, {
    method: 'DELETE',
    headers: headers()
  });

// WATCHLISTS
export const getWatchlists = () =>
  fetch(`${BASE_URL}/watchlists`, {
    headers: headers()
  }).then(res => res.json());

export const createWatchlist = (data) =>
  fetch(`${BASE_URL}/watchlists`, {
    method: 'POST',
    headers: headers(),
    body: JSON.stringify(data)
  }).then(res => res.json());

export const renameWatchlist = (id, data) =>
  fetch(`${BASE_URL}/watchlists/${id}`, {
    method: 'PATCH',
    headers: headers(),
    body: JSON.stringify(data)
  }).then(res => res.json());

export const deleteWatchlist = (id) =>
  fetch(`${BASE_URL}/watchlists/${id}`, {
    method: 'DELETE',
    headers: headers()
  });

export const addAssetToWatchlist = (watchlistId, assetId) =>
  fetch(`${BASE_URL}/watchlists/${watchlistId}/assets`, {
    method: 'POST',
    headers: headers(),
    body: JSON.stringify(assetId)
  }).then(res => res.json());

export const removeAssetFromWatchlist = (watchlistId, assetId) =>
  fetch(`${BASE_URL}/watchlists/${watchlistId}/assets/${assetId}`, {
    method: 'DELETE',
    headers: headers()
  });

// ASSETS
export const searchAssets = (query) =>
  fetch(`${BASE_URL}/assets/search?name=${query}`, {
    headers: headers()
  }).then(res => res.json());

export const getAssetBySymbol = (symbol) =>
  fetch(`${BASE_URL}/assets/symbol/${symbol}`, {
    headers: headers()
  }).then(res => res.json());

// ALERTS
export const getAlerts = () =>
  fetch(`${BASE_URL}/alerts`, {
    headers: headers()
  }).then(res => res.json());

export const getActiveAlerts = () =>
  fetch(`${BASE_URL}/alerts/active`, {
    headers: headers()
  }).then(res => res.json());

export const createAlert = (data) =>
  fetch(`${BASE_URL}/alerts`, {
    method: 'POST',
    headers: headers(),
    body: JSON.stringify(data)
  }).then(res => res.json());

export const toggleAlert = (id) =>
  fetch(`${BASE_URL}/alerts/${id}/toggle`, {
    method: 'PATCH',
    headers: headers()
  }).then(res => res.json());

export const deleteAlert = (id) =>
  fetch(`${BASE_URL}/alerts/${id}`, {
    method: 'DELETE',
    headers: headers()
  });

export const getAlertHistory = () =>
  fetch(`${BASE_URL}/alerts/history`, {
    headers: headers()
  }).then(res => res.json());