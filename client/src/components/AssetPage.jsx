import React, { useState, useEffect, useRef } from 'react'
import { useParams, useNavigate } from 'react-router-dom'
import { createChart, LineSeries } from 'lightweight-charts'
import { getAssetById, getAssetPriceHistory, createAlert, getAlerts } from '../api'

const AssetPage = () => {
  const { id } = useParams();
  const navigate = useNavigate();
  const chartContainerRef = useRef(null);
  const chartRef = useRef(null);

  const [asset, setAsset] = useState(null);
  const [loading, setLoading] = useState(true);
  const [error, setError] = useState(null);
  const [alertCondition, setAlertCondition] = useState('ABOVE');
  const [alertThreshold, setAlertThreshold] = useState('');
  const [alertMarketplace, setAlertMarketplace] = useState('STEAM');
  const [alertSuccess, setAlertSuccess] = useState(false);
  const [alertError, setAlertError] = useState(null);
  const [existingAlerts, setExistingAlerts] = useState([]);

  useEffect(() => {
    console.log(id);
    loadAsset();
    loadAlerts();

  }, [id]);

  useEffect(() => {
    if (!asset || !chartContainerRef.current) return;
    buildChart();
    return () => {
      if (chartRef.current) {
        chartRef.current.remove();
        chartRef.current = null;
      }
    }
  }, [asset]);

  const loadAsset = async () => {
    setLoading(true);
    try {
      const data = await getAssetById(id);
      setAsset(data);
    } catch (err) {
      setError('Failed to load asset');
    } finally {
      setLoading(false);
    }
  }

  const loadAlerts = async () => {
    try {
      const data = await getAlerts();
      setExistingAlerts(data.filter(a => a.assetId === id));
    } catch (err) {}
  }

  const buildChart = async () => {
    try {
      const history = await getAssetPriceHistory(id);

      if (!history || history.length === 0) return;

      const chart = createChart(chartContainerRef.current, {
        width: chartContainerRef.current.clientWidth,
        height: 300,
        layout: {
          background: { color: '#111827' },
          textColor: '#9CA3AF',
        },
        grid: {
          vertLines: { color: '#1F2937' },
          horzLines: { color: '#1F2937' },
        },
        rightPriceScale: {
          borderColor: '#374151',
        },
        timeScale: {
          borderColor: '#374151',
          timeVisible: true,
        },
      });

      const lineSeries = chart.addSeries(LineSeries, {
        color: '#22C55E',
        lineWidth: 2,
      });

      lineSeries.setData(history.map(p => ({
        time: p.time,
        value: Number(p.value)
      })));

      chart.timeScale().fitContent();
      chartRef.current = chart;

      const handleResize = () => {
        chart.applyOptions({ width: chartContainerRef.current.clientWidth });
      }
      window.addEventListener('resize', handleResize);
      return () => window.removeEventListener('resize', handleResize);

    } catch (err) {
      console.error('Chart error:', err);
    }
  }

  const handleCreateAlert = async (e) => {
    e.preventDefault();
    setAlertError(null);
    setAlertSuccess(false);

    if (!alertThreshold) {
      setAlertError('Please enter a threshold price');
      return;
    }

    try {
      await createAlert({
        assetId: id,
        condition: alertCondition,
        threshold: parseFloat(alertThreshold),
        marketplace: alertMarketplace
      });
      setAlertSuccess(true);
      setAlertThreshold('');
      loadAlerts();
    } catch (err) {
      setAlertError('Failed to create alert');
    }
  }

  const trimExterior = (name) => name?.replace(/\s*\([^)]*\)\s*$/, '').trim();

  if (loading) return (
    <div className="min-h-screen bg-gray-950 flex items-center justify-center">
      <div className="w-6 h-6 border-2 border-green-500 border-t-transparent rounded-full animate-spin" />
    </div>
  );

  if (error || !asset) return (
    <div className="min-h-screen bg-gray-950 flex items-center justify-center text-red-400">
      {error || 'Asset not found'}
    </div>
  );

  const price = asset.prices?.STEAM;
  const exterior = asset.name?.match(/\(([^)]+)\)/)?.[1];
  const isStatTrak = asset.name?.toLowerCase().includes('stattrak');
  const isSouvenir = asset.name?.toLowerCase().includes('souvenir');

  return (
    <div className="min-h-screen bg-gray-950 text-white p-8">

      {/* back button */}
      <button
        onClick={() => navigate(-1)}
        className="text-gray-400 hover:text-white text-sm mb-6 flex items-center gap-1 transition"
      >
        ← Back
      </button>

      <div className="flex gap-8">

        {/* left column */}
        <div className="flex-1">

          {/* asset header */}
          <div className="flex items-center gap-6 mb-8">
            {asset.imageUrl && (
              <img
                src={asset.imageUrl}
                alt={asset.name}
                className="w-32 h-32 object-contain bg-gray-900 rounded-xl p-3"
              />
            )}
            <div>
              <div className="flex items-center gap-2 mb-1">
                {isStatTrak && (
                  <span className="text-xs px-2 py-0.5 rounded bg-yellow-600 text-black font-bold">
                    StatTrak™
                  </span>
                )}
                {isSouvenir && (
                  <span className="text-xs px-2 py-0.5 rounded bg-yellow-400 text-black font-bold">
                    Souvenir
                  </span>
                )}
              </div>
              <h1 className="text-3xl font-bold text-white">
                {trimExterior(asset.name)}
              </h1>
              {exterior && (
                <p className="text-gray-400 mt-1">{exterior}</p>
              )}

              {/* prices */}
              <div className="flex gap-6 mt-4">
                {price?.lowestAsk && (
                  <div>
                    <p className="text-gray-400 text-xs uppercase tracking-wider">Lowest Ask</p>
                    <p className="text-green-400 text-2xl font-bold">
                      ${Number(price.lowestAsk).toFixed(2)}
                    </p>
                  </div>
                )}
                {price?.lastSale && (
                  <div>
                    <p className="text-gray-400 text-xs uppercase tracking-wider">Last Sale</p>
                    <p className="text-white text-2xl font-bold">
                      ${Number(price.lastSale).toFixed(2)}
                    </p>
                  </div>
                )}
                {price?.volume24h && (
                  <div>
                    <p className="text-gray-400 text-xs uppercase tracking-wider">Volume (24h)</p>
                    <p className="text-yellow-400 text-2xl font-bold">
                      {price.volume24h}
                    </p>
                  </div>
                )}
              </div>
            </div>
          </div>

          {/* chart */}
          <div className="bg-gray-900 rounded-xl p-4 mb-6">
            <h2 className="text-white font-bold mb-4">Price History</h2>
            <div ref={chartContainerRef} />
            {(!asset || !price) && (
              <p className="text-gray-500 text-sm text-center py-8">
                Price history will appear once the poller has collected data
              </p>
            )}
          </div>
        </div>

        {/* right column — alerts */}
        <div className="w-80 flex-shrink-0">
          <div className="bg-gray-900 rounded-xl p-6 mb-4">
            <h2 className="text-white font-bold mb-4">Set Alert</h2>

            <form onSubmit={handleCreateAlert} className="flex flex-col gap-3">
              <div>
                <label className="text-gray-400 text-xs uppercase tracking-wider mb-1 block">
                  Condition
                </label>
                <select
                  value={alertCondition}
                  onChange={e => setAlertCondition(e.target.value)}
                  className="w-full px-3 py-2 rounded bg-gray-800 text-white text-sm focus:outline-none focus:ring-2 focus:ring-green-500"
                >
                  <option value="ABOVE">Price goes above</option>
                  <option value="BELOW">Price goes below</option>
                </select>
              </div>

              <div>
                <label className="text-gray-400 text-xs uppercase tracking-wider mb-1 block">
                  Threshold ($)
                </label>
                <input
                  type="number"
                  step="0.01"
                  min="0"
                  value={alertThreshold}
                  onChange={e => setAlertThreshold(e.target.value)}
                  placeholder="0.00"
                  className="w-full px-3 py-2 rounded bg-gray-800 text-white text-sm focus:outline-none focus:ring-2 focus:ring-green-500"
                />
              </div>

              <div>
                <label className="text-gray-400 text-xs uppercase tracking-wider mb-1 block">
                  Marketplace
                </label>
                <select
                  value={alertMarketplace}
                  onChange={e => setAlertMarketplace(e.target.value)}
                  className="w-full px-3 py-2 rounded bg-gray-800 text-white text-sm focus:outline-none focus:ring-2 focus:ring-green-500"
                >
                  <option value="STEAM">Steam</option>
                </select>
              </div>

              {alertError && <p className="text-red-400 text-xs">{alertError}</p>}
              {alertSuccess && <p className="text-green-400 text-xs">Alert created!</p>}

              <button
                type="submit"
                className="bg-green-600 hover:bg-green-700 text-white text-sm py-2 rounded transition font-semibold"
              >
                Create Alert
              </button>
            </form>
          </div>

          {/* existing alerts */}
          {existingAlerts.length > 0 && (
            <div className="bg-gray-900 rounded-xl p-6">
              <h2 className="text-white font-bold mb-4">Active Alerts</h2>
              <div className="flex flex-col gap-2">
                {existingAlerts.map(alert => (
                  <div key={alert.id} className="bg-gray-800 rounded-lg px-3 py-2 flex justify-between items-center">
                    <div>
                      <p className="text-white text-xs font-semibold">
                        {alert.condition === 'ABOVE' ? '↑ Above' : '↓ Below'} ${Number(alert.threshold).toFixed(2)}
                      </p>
                      <p className="text-gray-500 text-xs">{alert.marketplace || 'STEAM'}</p>
                    </div>
                    <span className={`text-xs px-2 py-0.5 rounded ${
                      alert.active ? 'bg-green-900 text-green-400' : 'bg-gray-700 text-gray-500'
                    }`}>
                      {alert.active ? 'Active' : 'Inactive'}
                    </span>
                  </div>
                ))}
              </div>
            </div>
          )}
        </div>
      </div>
    </div>
  )
}

export default AssetPage